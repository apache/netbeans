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

package org.netbeans.junit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.openide.util.test.TestFileUtils;

public class NbModuleSuiteTurnClassPathModulesTest extends NbTestCase {

    public NbModuleSuiteTurnClassPathModulesTest(String name) {
        super(name);
    }

    protected @Override void setUp() throws Exception {
        clearWorkDir();
    }

    public void testTurnClassPathModules() throws Exception {
        File randomJar = new File(getWorkDir(), "random.jar");
        TestFileUtils.writeZipFile(randomJar, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\n\n");
        File plainModule = new File(getWorkDir(), "plainModule.jar");
        TestFileUtils.writeZipFile(plainModule, "META-INF/MANIFEST.MF:OpenIDE-Module: plain.modul\n e/2\n\n");
        File pseudoModule = new File(getWorkDir(), "pseudoModule.jar");
        TestFileUtils.writeZipFile(pseudoModule, "META-INF/MANIFEST.MF:OpenIDE-Module: org.netbeans.core.startup/1\n\n");
        File mavenLibWrapper = new File(getWorkDir(), "mavenLibWrapper.jar");
        TestFileUtils.writeZipFile(mavenLibWrapper, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\nOpenIDE-Module: maven.lib.wrapper\nMaven-Class-Path: some.group:lib:1.0\n\n");
        File mavenLib = new File(getWorkDir(), "repo/some/group/lib/1.0/lib-1.0.jar");
        if (!mavenLib.getParentFile().mkdirs()) {
            throw new IOException();
        }
        TestFileUtils.writeZipFile(mavenLib, "META-INF/MANIFEST.MF:Manifest-Version: 1.0\n\n");
        File ud = new File(getWorkDir(), "ud");
        File[] jars = {randomJar, plainModule, pseudoModule, mavenLibWrapper, mavenLib};
        StringBuilder jcp = new StringBuilder();
        List<URL> urls = new ArrayList<URL>(jars.length);
        for (File jar : jars) {
            if (jcp.length() > 0) {
                jcp.append(File.pathSeparatorChar);
            }
            jcp.append(jar);
            urls.add(jar.toURI().toURL());
        }
        System.setProperty("java.class.path", jcp.toString());
        NbModuleSuite.S.turnClassPathModules(ud, new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader().getParent()));
        File configModules = new File(ud, "config/Modules");
        assertEquals("[maven-lib-wrapper.xml, plain-module.xml]", new TreeSet<String>(Arrays.asList(configModules.list())).toString());
        assertEquals(plainModule, jarForConfig(new File(configModules, "plain-module.xml")));
        File mavenLibWrapper2 = jarForConfig(new File(configModules, "maven-lib-wrapper.xml"));
        assertFalse(mavenLibWrapper2.equals(mavenLibWrapper));
        Attributes attr;
        JarFile jf = new JarFile(mavenLibWrapper2);
        try {
            attr = jf.getManifest().getMainAttributes();
        } finally {
            jf.close();
        }
        assertEquals(mavenLibWrapper2.getAbsolutePath(), "maven.lib.wrapper", attr.getValue("OpenIDE-Module"));
        String cp = attr.getValue("Class-Path");
        assertNotNull(attr.toString(), cp);
        File mavenLib2 = new File(mavenLibWrapper2.getParentFile(), cp);
        assertEquals(mavenLib.length(), mavenLib2.length());
    }
    private static File jarForConfig(File configFile) throws IOException {
        return new File(TestFileUtils.readFile(configFile).replaceFirst("(?s).+<param name=\"jar\">([^<]+).+", "$1"));
    }

}
