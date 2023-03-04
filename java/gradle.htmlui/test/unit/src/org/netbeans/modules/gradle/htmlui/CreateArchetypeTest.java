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
package org.netbeans.modules.gradle.htmlui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import junit.framework.Test;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertNotEquals;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.spi.actions.AfterBuildActionHook;
import org.netbeans.modules.gradle.spi.newproject.TemplateOperation;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

public class CreateArchetypeTest extends NbTestCase {

    private FileObject workFo;

    public CreateArchetypeTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(CreateArchetypeTest.class).
                enableClasspathModules(true).
                gui(true).
                clusters(".*").
                enableModules(".*").
                honorAutoloadEager(true).
                suite();
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

        File rootDir = new File(getWorkDir(), "sample/dest");

        Map<String,Object> map = new HashMap<>();
        map.put("packageBase", "my.pkg.x");
        TemplateOperation ops = new TemplateOperation();
        GradleArchetype ga = new GradleArchetype(dir, rootDir, map);
        ga.copyTemplates(ops);
        ops.run();
        FileObject dest = FileUtil.toFileObject(rootDir);
        assertFile("Build script found", dest, "build.gradle");
        assertFile("Main class found", dest, "src", "main", "java", "my", "pkg", "x", "Demo.java").
                assertPackage("my.pkg.x").
                assertName("Demo").
                assertNoLicense().
                appendLine("applyBindings(model);", "System.exit(0);");
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
        assertFile("settings include only desktop and web", dest, "settings.gradle").
                assertText("//include 'app'").
                assertText("//include 'ios'").
                assertText("include 'desktop'").
                assertText("include 'web'");

        Project mainPrj = ProjectManager.getDefault().findProject(dest);
        assertNotNull("Project found", mainPrj);
        OpenProjects.getDefault().open(new Project[] { mainPrj }, true);


        ActionProvider actions = mainPrj.getLookup().lookup(ActionProvider.class);
        assertTrue(Arrays.asList(actions.getSupportedActions()).contains(ActionProvider.COMMAND_BUILD));
        actions.isActionEnabled(ActionProvider.COMMAND_BUILD, mainPrj.getLookup());

        assertLogicalView(mainPrj);

        invokeCommand(actions, ActionProvider.COMMAND_BUILD, mainPrj);
        assertFile("JAR created", dest, "build", "libs", "dest-1.0-SNAPSHOT.jar");

        FileObject desktopFo = mainPrj.getProjectDirectory().getFileObject("desktop");
        assertNotNull("desktop dir found", desktopFo);
        Project desktopPrj = ProjectManager.getDefault().findProject(desktopFo);
        assertNotNull("desktop project found", desktopPrj);

        invokeCommand(actions, ActionProvider.COMMAND_RUN, desktopPrj);
        assertFile("JAR created", dest, "desktop", "build", "libs", "desktop.jar");

        FileObject webFo = mainPrj.getProjectDirectory().getFileObject("web");
        assertNotNull("web dir found", webFo);
        Project webPrj = ProjectManager.getDefault().findProject(webFo);
        assertNotNull("web project found", webPrj);

        invokeCommand(actions, ActionProvider.COMMAND_RUN, webPrj);
        assertFile("Main script created", dest, "web", "build", "web", "bck2brwsr.js");
    }

    private void assertLogicalView(Project mainPrj) {
        LogicalViewProvider lvp = mainPrj.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("Logical view found", lvp);
        Node logicalView = lvp.createLogicalView();
        final Node[] children = logicalView.getChildren().getNodes(true);
        for (Node ch : children) {
            if (ch.getName().equals("pages") && "Frontend UI Pages".equals(ch.getDisplayName())) {
                FileObject pages = ch.getLookup().lookup(FileObject.class);
                assertNotNull("Pages node provides FileObject: " + ch, pages);
                assertNotNull("There is index.html", pages.getFileObject("index.html"));
                return;
            }
        }
        fail("Cannot find Frontend Pages in " + Arrays.toString(children));
    }

    protected void invokeCommand(ActionProvider actions, String cmd, Project prj) throws IllegalArgumentException, InterruptedException {
        CountDownLatch waiter = new CountDownLatch(1);
        boolean[] status = { false, false };
        ActionProgress ap = new ActionProgress() {
            @Override
            protected void started() {
                status[0] = true;
            }

            @Override
            public void finished(boolean success) {
                status[1] = success;
                waiter.countDown();
            }
        };
        actions.invokeAction(cmd, Lookups.fixed(prj, ap));
        assertTrue("ActionProgress was started", status[0]);
        waiter.await();
        assertTrue("ActionProgress was successfully finished", status[1]);
    }

    private AssertContent assertFile(String msg, FileObject root, String... path) throws IOException {
        FileObject at = root;
        for (String element : path) {
            at.refresh();
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

        private void appendLine(String pivot, String text) throws IOException {
            String newData = data.replace(pivot, pivot + "\n" + text);
            try (OutputStream os = fo.getOutputStream()) {
                os.write(newData.getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
