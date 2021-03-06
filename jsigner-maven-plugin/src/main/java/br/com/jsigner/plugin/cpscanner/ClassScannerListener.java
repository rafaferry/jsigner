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
package br.com.jsigner.plugin.cpscanner;

import java.net.URL;

public abstract class ClassScannerListener implements ScannerListener {

    public void resourceFound(URL base, URL resource) {
        try {
            if (isClass(resource)) {
                String str = resource.toString();
                String baseStr = base.toString();
                StringBuilder builder = new StringBuilder(str);
                int idx = builder.indexOf("!");
                if (idx < 0) {
                    idx = baseStr.length();
                } else {
                    idx += 2;
                }
                builder.delete(0, idx);
                builder.delete(builder.length() - 6, builder.length());
                str = builder.toString().replaceAll("/", ".");
                if (isValidClass(str)) {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(str);
                    // Class<?> clazz = Class.forName(str);
                    // Class<?> clazz =
                    // Thread.currentThread().getContextClassLoader().getClass().forName(str);
                    classFound(base, clazz);
                }
            }
        } catch (ClassNotFoundException e) {
            classNotFound(base, resource, e);
        }
    }

    public boolean isValidClass(String str) {
        return true;
    }

    protected abstract void classNotFound(URL base, URL resource, Throwable e);

    public boolean isClass(URL url) {
        return url.toString().endsWith(".class");
    }

    public abstract void classFound(URL base, Class<?> clazz);

}
