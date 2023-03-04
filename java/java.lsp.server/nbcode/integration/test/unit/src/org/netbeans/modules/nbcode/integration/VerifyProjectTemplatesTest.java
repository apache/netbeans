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
package org.netbeans.modules.nbcode.integration;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
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

public class VerifyProjectTemplatesTest extends NbTestCase {

    public VerifyProjectTemplatesTest(String name) {
        super(name);
    }

    public static junit.framework.Test suite() {
        return NbModuleSuite.createConfiguration(VerifyProjectTemplatesTest.class).
            gui(false).
            enableModules(".*").
            honorAutoloadEager(true).
            suite();
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
    }

    public void testProjectTemplates() throws Exception {
        FileObject root = FileUtil.getConfigFile("Templates/Project");
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
}
