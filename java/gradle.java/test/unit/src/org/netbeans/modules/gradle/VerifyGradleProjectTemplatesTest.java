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
package org.netbeans.modules.gradle;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.LogManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class VerifyGradleProjectTemplatesTest extends NbTestCase {

    public VerifyGradleProjectTemplatesTest(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.createConfiguration(VerifyGradleProjectTemplatesTest.class).
            gui(false).
            enableModules(".*").
            honorAutoloadEager(true).
            suite();
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testGradleProjectTemplates() throws Exception {
        FileObject root = FileUtil.getConfigFile("Templates/Project/Gradle");
        assertNotNull("Gradle project folder found", root);
        Enumeration<? extends FileObject> projectTemplates = root.getChildren(true);

        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);
        int cnt = 0;
        int err = 0;
        while (projectTemplates.hasMoreElements()) {
            FileObject fo = projectTemplates.nextElement();

            DataObject template = DataObject.find(fo);
            if (!template.isTemplate()) {
                continue;
            }
            try {
                verifySingleTemplate(++cnt, template);
                pw.append("Successfully instantiated ").append(fo.getPath()).append("\n");
            } catch (Exception | Error ex) {
                pw.append("Exception instantiating ").append(fo.getPath()).append("\n");
                ex.printStackTrace(pw);
                err++;
                
                try {
                    LogManager.getLogManager().getLogger("org.netbeans.modules.gradle.loaders.LegacyProjectLoader").setLevel(Level.FINER);
                    pw.append("Running again with increased loglevel:").append(fo.getPath()).append("\n");
                    verifySingleTemplate(cnt + 1000, template);
                } finally {
                    LogManager.getLogManager().getLogger("org.netbeans.modules.gradle.loaders.LegacyProjectLoader").setLevel(Level.INFO);
                }
            }
        }
        pw.flush();
        if (err > 0) {
            throw new IOException("Some projects failed (" + err + "/" + cnt + "):\n" + w.toString());
        }
    }

    private void verifySingleTemplate(int cnt, DataObject template) throws IllegalArgumentException, IOException, InterruptedException {
        final FileObject targetRoot = FileUtil.toFileObject(getWorkDir());
        final FileObject targetFolder = targetRoot.createFolder(template.getName() + "at" + cnt);
        final String id = "prj" + ++cnt;
        final DataFolder target = DataFolder.findFolder(targetFolder);


        Map<String,Object> params = new HashMap<>();
        params.put("groupId", "org.netbeans.test");
        params.put("artifactId", id);
        params.put("packageBase", "org.netbeans.test." + id);

        DataObject newPrj = template.createFromTemplate(target, id, params);
        Project prj = ProjectManager.getDefault().findProject(newPrj.getPrimaryFile());
        assertNotNull("Project found for " + newPrj, prj);

        tryProjectAction(prj, ActionProvider.COMMAND_PRIME);
        assertProjectAction(prj, ActionProvider.COMMAND_BUILD);

        assertGradlew("Gradlew properties", prj, "gradle/wrapper/gradle-wrapper.properties");
        assertGradlew("Gradlew JAR", prj, "gradle/wrapper/gradle-wrapper.jar");
        assertGradlew("Gradlew shell", prj, "gradlew");
        assertGradlew("Gradlew bat", prj, "gradlew.bat");
    }

    private static void tryProjectAction(Project prj, final String action) throws IllegalArgumentException, InterruptedException {
        invokeProjectAction(prj, action, true);
    }

    private static void assertProjectAction(Project prj, final String action) throws IllegalArgumentException, InterruptedException {
        invokeProjectAction(prj, action, false);
    }

    private static void invokeProjectAction(Project prj, final String action, boolean canBeMissing) throws IllegalArgumentException, InterruptedException {
        boolean[] status = { false, false };
        CountDownLatch cdl = new CountDownLatch(1);
        ActionProgress progress = new ActionProgress() {
            @Override
            protected void started() {
                status[0] = true;
            }

            @Override
            public void finished(boolean success) {
                status[1] = success;
                cdl.countDown();
            }

        };

        final Lookup lkp = new ProxyLookup(prj.getLookup(), Lookups.fixed(progress));
        ActionProvider ap = lkp.lookup(ActionProvider.class);
        assertNotNull("Action provider found", ap);
        final boolean enabled = ap.isActionEnabled(action, lkp);
        if (!enabled && canBeMissing) {
            return;
        }
        assertTrue("action " + action + " is supported", enabled);
        ap.invokeAction(action, lkp);
        assertTrue(action + " action started for " + prj, status[0]);
        cdl.await();
        assertTrue(action + " finished for " + prj.getProjectDirectory(), status[1]);
    }

    private void assertGradlew(String msg, Project prj, String path) {
        final FileObject pd = prj.getProjectDirectory();
        pd.refresh(true);
        final FileObject fo = pd.getFileObject(path);
        if (fo == null) {
            fail(names(msg + " cannot find " + path + " in " + pd + " only found: ", pd));
        }
    }

    private static String names(String msg, final FileObject pd) {
        StringBuilder sb = new StringBuilder(msg);
        String sep = ": ";
        for (FileObject fo : pd.getChildren()) {
            sb.append(sep);
            sb.append(fo.getNameExt());
            sep = ", ";
        }
        return sb.toString();
    }
}
