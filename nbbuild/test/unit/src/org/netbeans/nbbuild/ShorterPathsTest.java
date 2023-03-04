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
package org.netbeans.nbbuild;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 *
 * @author pzajac
 */
public class ShorterPathsTest extends TestBase {

    public ShorterPathsTest(java.lang.String testName) {
        super(testName);
    }

    public void testShorterPaths() throws Exception {
        // create test
        File wd = getWorkDir();
        File modules = new File(wd, "modules");
        modules.mkdirs();
        File module = new File(modules, "module.jar");
        module.createNewFile();
        File extlib = new File(wd, "extlib.jar");
        File extraLibsDir = new File(wd, "extralibs");
        File testProperties = new File(wd, "outtest.properties");
        extraLibsDir.mkdirs();

        try (PrintStream ps = new PrintStream(extlib)) {
            ps.println("content");
        }


        execute("ShorterPathsTest.xml", new String[]{
                    "-verbose",
                    "-Dtest.ext.lib=" + extlib.getPath(),
                    "-Dtest.modules.dir=" + modules.getPath(),
                    "-Dextra.test.libs.dir=" + extraLibsDir.getPath(),
                    "-Dtest.properties=" + testProperties.getPath(),
                    "all"
                });
        File extralibCopy = new File(extraLibsDir, "extlib.jar");

        assertTrue("No extra library has been copied", extralibCopy.exists());
        BufferedReader reader = new BufferedReader(new FileReader(extralibCopy));
        assertEquals("Different content in copy of extra library:", "content", reader.readLine());

        Properties props = new Properties();
        FileInputStream propsIs = new FileInputStream(testProperties);
        props.load(propsIs);
        propsIs.close();
        assertEquals("extra.test.libs.dir", "${extra.test.libs.dir}/extlib.jar", props.getProperty("extra.test.libs"));
        assertEquals("test.run.cp", "${nb.root.test.dir}/module.jar", props.getProperty("test.run.cp"));
        assertEquals("test-sys-prop.prop1", "value1", props.getProperty("test-sys-prop.prop1"));
        assertEquals("test-sys-prop.prop2", "${nb.root.test.dir}/module.jar", props.getProperty("test-sys-prop.prop2"));
        assertEquals("test-sys-prop.prop3", "${nb.root.test.dir}/module.jar:${nb.root.test.dir}/not-exists.jar", props.getProperty("test-sys-prop.prop3"));
        assertNull(props.getProperty("test-unit-sys-prop.xtest.data"));
        assertEquals("props.size()", 5, props.size());


        // test dist 
    }

    /** Tests that if an extra library has Class-Path attribute (extlib.jar), 
     * then extra jar (ext/ext/cpext1.jar) is copied to binary distribution
     * (extralibs) with proper relative path.
     */
    public void testClassPathExtensions() throws Exception {
        // create test
        File wd = getWorkDir();
        File modules = new File(wd, "modules");
        modules.mkdirs();
        File module = new File(modules, "module.jar");
        module.createNewFile();
        File extraLibsDir = new File(wd, "extralibs");
        File testProperties = new File(wd, "outtest.properties");
        extraLibsDir.mkdirs();

        // create ext lib with Class-Path extension
        String cpExtensionRelativePath = "ext/ext/cpext1.jar";
        File classPathExtension = new File(getWorkDir(), cpExtensionRelativePath);
        classPathExtension.getParentFile().mkdirs();
        classPathExtension.createNewFile();
        Manifest m = createManifest();
        m.getMainAttributes().putValue("Class-Path", cpExtensionRelativePath);
        File extlib = new File(getWorkDir(), "extlib.jar");
        generateJar(extlib, new String[]{"a/b/c/Class1.class", "a/b/c/Class2.class"}, m);

        execute("ShorterPathsTest.xml", new String[]{
                    "-verbose",
                    "-Dtest.ext.lib=" + extlib.getPath(),
                    "-Dtest.modules.dir=" + modules.getPath(),
                    "-Dextra.test.libs.dir=" + extraLibsDir.getPath(),
                    "-Dtest.properties=" + testProperties.getPath(),
                    "all"
                });
        File extralibCopy = new File(extraLibsDir, "extlib.jar");
        assertTrue("Extra library not copied.", extralibCopy.exists());
        File classPathExtensionCopy = new File(extraLibsDir, cpExtensionRelativePath);
        assertTrue("Class-Path extension not copied.", classPathExtensionCopy.exists());

        Properties props = new Properties();
        try (FileInputStream propsIs = new FileInputStream(testProperties)) {
            props.load(propsIs);
        }
        assertEquals("extra.test.libs.dir", "${extra.test.libs.dir}/extlib.jar", props.getProperty("extra.test.libs"));
        assertEquals("test.run.cp", "${nb.root.test.dir}/module.jar", props.getProperty("test.run.cp"));
    }

    private void generateJar(File jarFile, String[] content, Manifest manifest) throws IOException {
        try (JarOutputStream os = new JarOutputStream(new FileOutputStream(jarFile), manifest)) {
            for (int i = 0; i < content.length; i++) {
                os.putNextEntry(new JarEntry(content[i]));
                os.closeEntry();
            }
            os.closeEntry();
        }
    }
}
