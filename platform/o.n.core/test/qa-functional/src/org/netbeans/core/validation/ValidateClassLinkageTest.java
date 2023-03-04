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

package org.netbeans.core.validation;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;

/**
 * Test that all classes in the system can load and link.
 * @author Jesse Glick
 */
public class ValidateClassLinkageTest extends NbTestCase {

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ValidateClassLinkageTest.class).clusters("(?!extra$).*").enableModules(".*").gui(false).enableClasspathModules(false).suite();
    }
    
    public ValidateClassLinkageTest(String name) {
        super(name);
    }
    
    /**
     * Try to load every class we can find.
     * @see org.netbeans.core.startup.NbInstaller#preresolveClasses
     */
    public void testClassLinkage() throws Exception {
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        assertNotNull("Context CL has some autoloads in it", l.getResource("org/openide/windows/InputOutput.class"));
        Set<File> jars = new TreeSet<File>();
        for (URL manifest : NbCollections.iterable(l.getResources("META-INF/MANIFEST.MF"))) {
            String murl = manifest.toExternalForm();
            assertTrue(murl.endsWith("/META-INF/MANIFEST.MF"));
            if (murl.startsWith("jar:")) {
                assertTrue(murl.endsWith("!/META-INF/MANIFEST.MF"));
                String jarfileurl = murl.substring(4, murl.length() - "!/META-INF/MANIFEST.MF".length());
                assertTrue(jarfileurl.startsWith("file:/"));
                assertTrue(jarfileurl.endsWith(".jar"));
                if (jarfileurl.indexOf("/jre/lib/") != -1) {
                    System.err.println("Skipping " + jarfileurl);
                    continue;
                }
                File f = Utilities.toFile(new URI(jarfileurl));
                jars.add(f);
            }
        }
        Map<String,Throwable> errorsByClazz = new TreeMap<String,Throwable>();
        Map<String,File> locationsByClass = new HashMap<String,File>();
        for (File jar : jars) {
            System.err.println("Checking JAR: " + jar);
            JarFile jarfile = new JarFile(jar);
            try {
                for (JarEntry entry : NbCollections.iterable(jarfile.entries())) {
                    String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }
                    String clazz = name.substring(0, name.length() - 6).replace('/', '.');
                    // Only check classes developed on nb.org; there will be numerous linkage errors among 3rd-party libraries, since we do not attempt to bundle every dependency.
                    // XXX also include e.g. com.sun.collablet, com.sun.javacard, ...
                    if (!clazz.matches("org[.](netbeans|openide)[.].+")) {
                        continue;
                    }
                    if (clazz.matches("org[.]netbeans[.]core[.]osgi[.].+")) {
                        continue; // links against OSGi core which is not in CP for whatever reason
                    }
                    if (clazz.matches("org[.]netbeans[.](modules[.](s2banttask|ide[.]ergonomics[.]ant)|mobility[.]antext)[.].+")) {
                        continue; // links against ant.jar
                    }
                    if (clazz.equals("org.netbeans.modules.identity.server.manager.ui.NodeExtensionImpl")) {
                        continue; // seems to be loaded specially w/ link against appsrvbridge.jar
                    }
                    if (clazz.equals("org.netbeans.modules.javacard.source.JavaCardErrorProcessor")) {
                        continue; // seems to link against JDK 7 javac's AbstractTypeProcessor
                    }
                    if (clazz.startsWith("org.netbeans.modules.javahelp")) {
                        // javahelp is not distributed with Apache
                        continue; 
                    }
                        Throwable t = null;
                        try {
                            Class.forName(clazz, false, l);
                        } catch (ClassNotFoundException cnfe) {
                            t = cnfe;
                        } catch (LinkageError le) {
                            t = le;
                        } catch (RuntimeException re) { // e.g. IllegalArgumentException from package defs
                            t = re;
                        }
                        if (t != null) {
                            errorsByClazz.put(clazz, t);
                            locationsByClass.put(clazz, jar);
                        }
                }
            } finally {
                jarfile.close();
            }
        }
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memoryBean.getNonHeapMemoryUsage();
        System.err.println("Non-heap memory usage: " + (usage.getUsed() / 1024 / 1024) + "/" + (usage.getMax() / 1024 / 1024) + "M");
        if (!errorsByClazz.isEmpty()) {
            for (Map.Entry<String,Throwable> entry : errorsByClazz.entrySet()) {
                String clazz = entry.getKey();
                // Will go the logs:
                System.err.println("From " + clazz + " in " + locationsByClass.get(clazz) + ":");
                entry.getValue().printStackTrace();
            }
            fail("Linkage or class loading errors encountered in " + errorsByClazz.keySet() + " (see logs for details)");
        }
    }
    
}
