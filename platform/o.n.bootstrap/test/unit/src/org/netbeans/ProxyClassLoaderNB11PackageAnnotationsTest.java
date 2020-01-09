/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.Collections;
import org.netbeans.junit.NbTestCase;

/**
 * This test ensures that package level annotations (annotations to
 * package-info) are correctly loaded by the NetBeans classloader system in Java
 * 11.
 */
public class ProxyClassLoaderNB11PackageAnnotationsTest extends NbTestCase {

    private static final String TEST_PACKAGE = "org.netbeans.pkgannottest";

    public ProxyClassLoaderNB11PackageAnnotationsTest(String name) {
        super(name);
    }

    public void testPackageAnnotation() throws Exception {

        class PackageClassLoader extends ProxyClassLoader {

            PackageClassLoader() {
                super(new ClassLoader[0], false);
                addCoveredPackages(Collections.singleton(TEST_PACKAGE));
                definePackage(TEST_PACKAGE, null, null, null, null, null, null, null);
            }

            @Override
            protected Class<?> doLoadClass(String pkg, String name) {
                if (name.startsWith(TEST_PACKAGE)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    InputStream is = PackageClassLoader.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class");
                    byte[] buf = new byte[4096];
                    int read;
                    try {
                        while ((read = is.read(buf)) != -1) {
                            baos.write(buf, 0, read);
                        }
                    } catch (IOException x) {
                        assert false : x;
                    }
                    return defineClass(name, baos.toByteArray(), 0, baos.size());
                }
                return null;
            }

            @Override
            protected boolean shouldDelegateResource(String pkg, ClassLoader parent) {
                return parent != null || !pkg.equals(TEST_PACKAGE.replace('.', '/') + "/");
            }

            @Override
            public String toString() {
                return PackageClassLoader.class.getName();
            }

        }

        final ProxyClassLoader cl = new PackageClassLoader();
        final Class<? extends Annotation> annotClz = cl.loadClass(TEST_PACKAGE + ".NB11PackageTestAnnotation").asSubclass(Annotation.class);
        final Package pkg = annotClz.getPackage();
        final Object annot = pkg.getAnnotation(annotClz);
        assertTrue("Annotation not found", annot != null);
    }

}
