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
package org.netbeans.modules.gradle.htmlui;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotEquals;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;

public class CreateArchetypeTest extends NbTestCase {

    private FileObject workFo;

    public CreateArchetypeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        workFo = lfs.getRoot();
    }



    public void testCreateFromArchetype() throws Exception {
        FileObject dir = FileUtil.getConfigFile("Templates/Project/Gradle/org-netbeans-modules-gradle-htmlui-HtmlJavaApplicationProjectWizard");
        assertNotNull("Templates directory found", dir);

        FileObject dest = FileUtil.createFolder(workFo, "sample/dest");

        Map<String,Object> map = new HashMap<>();
        map.put("packageBase", "my.pkg.x");
        GradleArchetype ga = new GradleArchetype(dir, dest, map);
        ga.copyTemplates();
        assertFile("Build script found", dest, "build.gradle");
        assertFile("Main class found", dest, "src", "main", "java", "my", "pkg", "x", "Demo.java").
                assertPackage("my.pkg.x").
                assertName("Demo").
                assertNoLicense();
        assertFile("index.html found", dest, "src", "main", "webapp", "pages", "index.html").
                assertNoLicense();
        assertFile("DesktopMain class found", dest, "desktop", "src", "main", "java", "my", "pkg", "x", "DesktopMain.java").
                assertPackage("my.pkg.x").
                assertName("DesktopMain").
                assertNoLicense();
        assertFile("Desktop script found", dest, "desktop", "build.gradle").
                assertText("mainClassName = 'my.pkg.x.DesktopMain'").
                assertNoLicense();
        assertFile("Browser script found", dest, "web", "build.gradle").
                assertText("mainClassName = 'my.pkg.x.BrowserMain'").
                assertNoLicense();
        assertFile("BrowserMain class found", dest, "web", "src", "main", "java", "my", "pkg", "x", "BrowserMain.java").
                assertPackage("my.pkg.x").
                assertName("BrowserMain").
                assertNoLicense();
    }

    private AssertContent assertFile(String msg, FileObject root, String... path) throws IOException {
        FileObject at = root;
        for (String element : path) {
            FileObject next = at.getFileObject(element);
            if (next == null) {
                fail(msg +
                    "\nCannot find " + Arrays.toString(path) +
                    " found only " + at.getPath() +
                    " and it contains:\n" +
                    Arrays.toString(at.getChildren())
                );
                break;
            }
            at = next;
        }
        assertTrue("Expecting data " + at, at.isData());
        return new AssertContent(at);
    }

    private static final class AssertContent {
        private final FileObject fo;
        private final String data;

        AssertContent(FileObject fo) throws IOException {
            this.fo = fo;
            data = fo.asText();
        }

        public AssertContent assertPackage(String pkg) {
            String toFind = "package " + pkg + ";";
            int at = data.indexOf(toFind);
            if (at == -1) {
                fail("Cannot find " + pkg + " in:\n" + data);
            }
            return this;
        }

        public AssertContent assertName(String name) {
            String toFind = "class " + name + " implements";
            int at = data.indexOf(toFind);
            if (at == -1) {
                toFind = "class " + name + " {";
                at = data.indexOf(toFind);
                if (at == -1) {
                    fail("Cannot find " + toFind + " in:\n" + data);
                }
            }
            return this;

        }

        private AssertContent assertNoLicense() {
            if (data.startsWith("package")) {
                return this;
            }
            assertFalse(data, data.startsWith("/"));
            return this;
        }

        private AssertContent assertText(String txt) {
            assertNotEquals(txt + " found in\n" + data, -1, data.indexOf(txt));
            return this;
        }
    }
}
