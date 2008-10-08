/**
 * Copyright (C) 2008 Rafael Farias Silva <rafaferry@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.jsigner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.scannotation.AnnotationDB;

import br.com.jsigner.annotations.Domain;
import br.com.jsigner.designer.JsignerDesigner;
import br.com.jsigner.diagram.ClassDiagram;
import br.com.jsigner.diagram.DiagramBuilder;
import br.com.jsigner.log.JsignerLog;

public final class Jsigner {

	private static JsignerLog log = JsignerConfiguration.getLog();

	public static void design(URL[] urls, File outputFolder)
			throws MalformedURLException {
		AnnotationDB db = new AnnotationDB();
		try {
			db.setScanClassAnnotations(true);
			db.setScanFieldAnnotations(false);
			db.setScanMethodAnnotations(false);
			db.setScanParameterAnnotations(false);

			db.scanArchives(urls);
			Set<String> classes = db.getAnnotationIndex().get(
					Domain.class.getName());

			List<Class<?>> diagramClasses = new ArrayList<Class<?>>();

			if (classes == null) {
				log.error("No @Domain annotations found, aborting execution!");
				throw new RuntimeException(
						"No @Domain annotations found, aborting execution!");
			}
			Iterator<String> iterator = classes.iterator();

			log.info("Starting loading classes");
			while (iterator.hasNext()) {
				String nextClazz = iterator.next();
				log.debug("loading class: " + nextClazz);
				URLClassLoader classLoader = URLClassLoader.newInstance(urls,
						Thread.currentThread().getContextClassLoader());
				Class<?> clazz = classLoader.loadClass(nextClazz);
				diagramClasses.add(clazz);
			}

			log.info(diagramClasses.size() + " Classes loaded");

			if (diagramClasses.size() == 0) {
				throw new RuntimeException(
						"Aborting execution, 0 classes found!");
			}

			DiagramBuilder builder = new DiagramBuilder();
			builder.build(diagramClasses);

			Collection<ClassDiagram> diagrams = builder.getDiagrams();

			JsignerDesigner designer = JsignerConfiguration
					.getJsignerDesigner();
			for (ClassDiagram classDiagram : diagrams) {
				designer.execute(classDiagram, outputFolder);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
