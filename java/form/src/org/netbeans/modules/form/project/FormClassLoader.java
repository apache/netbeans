/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.project;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import org.openide.ErrorManager;

/**
 * A special classloader capable to combine system classpath (IDE modules) and
 * user project classpath into one. Classes loaded by this classloader can link
 * with module classes running in the IDE and access resources on project
 * classpath at the same time.
 *
 * @author Tomas Pavek
 */

final class FormClassLoader extends ClassLoader {

    private ClassLoader systemClassLoader;
    private ClassLoader projectClassLoader;

    FormClassLoader(ClassLoader projectClassLoader) {
        super(null);
        this.systemClassLoader = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
        this.projectClassLoader = projectClassLoader;
    }

    ClassLoader getProjectClassLoader() {
        return projectClassLoader;
    }

    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        ClassPathUtils.ClassLoadingType type = ClassPathUtils.getClassLoadingType(name);
        if (type == null) {
            if (projectClassLoader == null) {
                throw new ClassNotFoundException(ClassPathUtils.getBundleString("MSG_NullClassPath")); // NOI18N
            }
            return projectClassLoader.loadClass(name);
        }
        if (type == ClassPathUtils.SYSTEM_CLASS) {
            return systemClassLoader.loadClass(name);
        }
        // otherwise type == ClassPathUtils.SYSTEM_CLASS_WITH_PROJECT

        Class c = null;
        String filename = name.replace('.', '/').concat(".class"); // NOI18N
        URL url = systemClassLoader.getResource(filename);
        if (url == null && projectClassLoader != null)
            url = projectClassLoader.getResource(filename);
        if (url != null) {
            InputStream is = null;
            try {
                is = url.openStream();
                byte[] data = null;
                int first;
                int available = is.available();
                while ((first = is.read()) != -1) {
                    int length = is.available();
                    if (length != available) { // Workaround for issue 4401122
                        length++;
                    }
                    byte[] b = new byte[length];
                    b[0] = (byte) first;
                    int count = 1;
                    while (count < length) {
                        int read = is.read(b, count, length - count);
                        assert (read != -1);
                        count += read;
                    }
                    if (data == null) {
                        data = b;
                    }
                    else {
                        byte[] temp = new byte[data.length + count];
                        System.arraycopy(data, 0, temp, 0, data.length);
                        System.arraycopy(b, 0, temp, data.length, count);
                        data = temp;
                    }
                }
                int dot = name.lastIndexOf('.');
                if (dot != -1) { // Is there anything we should do for the default package?
                    String packageName = name.substring(0, dot);
                    Package pakcage = getPackage(packageName);
                    if (pakcage == null) {
                        // PENDING are we able to determine the attributes somehow?
                        definePackage(packageName, null, null, null, null, null, null, null);
                    }
                }
                c = defineClass(name, data, 0, data.length);
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ioex) {
                        // ignore
                    }
                }
            }
        }
        if (c == null)
            throw new ClassNotFoundException(name);

        return c;
    }

    @Override
    public URL getResource(String name) {
        URL url = projectClassLoader != null ? projectClassLoader.getResource(name) : null;
        if (url == null)
            url = systemClassLoader.getResource(name);
        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> e = projectClassLoader != null ? projectClassLoader.getResources(name) : null;
        if (e == null) {
            e = systemClassLoader.getResources(name);
        }
        return e;
    }

}
