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

        PrintStream ps = new PrintStream(extlib);
        ps.println("content");
        ps.close();


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
        FileInputStream propsIs = new FileInputStream(testProperties);
        props.load(propsIs);
        propsIs.close();
        assertEquals("extra.test.libs.dir", "${extra.test.libs.dir}/extlib.jar", props.getProperty("extra.test.libs"));
        assertEquals("test.run.cp", "${nb.root.test.dir}/module.jar", props.getProperty("test.run.cp"));
    }

    private void generateJar(File jarFile, String[] content, Manifest manifest) throws IOException {
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jarFile), manifest);
        for (int i = 0; i < content.length; i++) {
            os.putNextEntry(new JarEntry(content[i]));
            os.closeEntry();
        }
        os.closeEntry();
        os.close();
    }
}
