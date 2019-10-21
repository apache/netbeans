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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.netbeans.junit.NbTestCase;

/**
 *This test ensures that package level annotations (annotations to package-info) are correctly loaded 
 * by the NetBeans classloader system in Java 11. 
 * 
 * At present, this test can't be run with JDK 11 unless "<property name="permit.jdk9.builds" value="true"/>" is added
 * to build.xml
 * 
 * @author boris.heithecker@gmx.net
 */
public class ProxyClassLoaderNB11PackageAnnotationsTest extends NbTestCase {

    private static final String TEST_PACKAGE = "org.netbeans.pkgannottest";

    public ProxyClassLoaderNB11PackageAnnotationsTest(String name) {
        super(name);
    }

    public void testPackageAnnotation() throws Exception {
        
        final String testBuildBase = ProxyClassLoaderNB11PackageAnnotationsTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();

        class PackageClassLoader extends ProxyClassLoader {

            PackageClassLoader() {
                super(new ClassLoader[0], false);
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                if (name.startsWith(TEST_PACKAGE)) {
                    return selfLoadPackageClass(name);
                }
                return super.findClass(name);
            }

            @Override
            protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith(TEST_PACKAGE)) {
                    return selfLoadPackageClass(name);
                }
                return super.loadClass(name, resolve);
            }

            private synchronized Class selfLoadPackageClass(String name) throws ClassNotFoundException {
                Class cls = findLoadedClass(name);
                if (cls == null) {
                    try {
                        final Path p = Paths.get(testBuildBase, name.replace('.', '/') + ".class");
                        byte[] bytes = Files.readAllBytes(p);
                        cls = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException ex) {
                        throw new ClassNotFoundException(null, ex);
                    }
                    final Package pkg = getPackageFast(TEST_PACKAGE, true);
                    if (pkg == null) {
                        definePackage(TEST_PACKAGE, null, null, null, null, null, null, null);
                    }
                }
                return cls;
            }

        }
        final ProxyClassLoader cl = new PackageClassLoader();
        final Class<? extends Annotation> annotClz = (Class<? extends Annotation>) cl.loadClass(TEST_PACKAGE + ".NB11PackageTestAnnotation");
        final Package pkg = annotClz.getPackage();
        final Object annot = pkg.getAnnotation(annotClz);
        assertTrue("Annotation not found", annot != null);
    }

}
