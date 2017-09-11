/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        // Note MaxPermSize in project.properties:
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
