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

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.Dialog;
import java.beans.BeanInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import junit.framework.Test;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ProjectEnableFailureTest extends NbTestCase {
    FileObject root;
    private static final InstanceContent ic = new InstanceContent();

    static {
        FeatureManager.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private int change;
    
    
    public ProjectEnableFailureTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(ProjectEnableFailureTest.class).
            gui(false)
        );
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        ic.set(Collections.emptyList(), null);

        URI uri = ModuleInfo.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        File jar = new File(uri);
        System.setProperty("netbeans.home", jar.getParentFile().getParent());
        System.setProperty("netbeans.user", getWorkDirPath());
        StringBuffer sb = new StringBuffer();
        boolean found = false;
        Exception ex2 = null;

        File module = createModule("non-existent.jar",
"Manifest-Version", "1.0",
"OpenIDE-Module", "org.netbeans.cannot.enable",
"OpenIDE-Module-Specification-Version", "1.0",
"OpenIDE-Module-Requires", "org.netbeans.does.not.exist.token"
        );
        ModuleManager man = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        Module m = man.create(module, this, false, false, false);

        FeatureInfo info = FeatureInfo.create(
            "cluster",
            ProjectEnableFailureTest.class.getResource("FeatureInfo.xml"),
            ProjectEnableFailureTest.class.getResource("TestBundleBroken.properties")
        );
        ic.add(info);
        
        File dbp = new File(new File(getWorkDir(), "1st"), "dbproject");
        File db = new File(dbp, "project.properties");
        dbp.mkdirs();
        db.createNewFile();

        File dbp2 = new File(new File(getWorkDir(), "2nd"), "dbproject");
        File db2 = new File(dbp2, "project.properties");
        dbp2.mkdirs();
        db2.createNewFile();

        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("fileobject found", root);

        OpenProjects.getDefault().open(new Project[0], false);
        assertEquals("Empty", 0, OpenProjects.getDefault().getOpenProjects().length);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRecognizeDocBookProjectButCannotOpenIt() throws Exception {
        FileObject prjFO1 = root.getFileObject("1st");
        FileObject prjFO2 = root.getFileObject("2nd");

        assertTrue("Recognized as project", ProjectManager.getDefault().isProject(prjFO1));
        Project p = ProjectManager.getDefault().findProject(prjFO1);
        assertNotNull("Project found", p);

        assertTrue("Recognized as project", ProjectManager.getDefault().isProject(prjFO2));
        Project p2 = ProjectManager.getDefault().findProject(prjFO2);
        assertNotNull("Project found", p2);
        
        ProjectOpenedHook open = p.getLookup().lookup(ProjectOpenedHook.class);
        assertNotNull("Open hook found", open);

        ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
        assertNotNull("Info about icon", info);
        assertNotNull("Icon provided", info.getIcon());

        TestFactory.recognize.add(prjFO1);
        OpenProjects.getDefault().open(new Project[] { p }, false);
        
        assertEquals("No Dialog currently created", 0, DD.cnt);
        
        List<Project> arr = Arrays.asList(OpenProjects.getDefault().openProjects().get());
        assertEquals("However one instance is there", 1, arr.size());
        Project newP = arr.get(0).getLookup().lookup(Project.class);
        if (p == newP) {
            fail("New project is made available, not old: " + newP);
        }

        Class<?> brokenClass = Class.forName("org.netbeans.modules.ide.ergonomics.fod.BrokenProject");
        assertEquals("The project is marked as broken", brokenClass, newP.getClass());
        ProjectInformation pi = newP.getLookup().lookup(ProjectInformation.class);
        assertNotNull("Info present", pi);
        if (!pi.getDisplayName().contains("(broken)")) {
            fail("Display name shall indicate that it is broken: " + pi.getDisplayName());
        }
        LogicalViewProvider lvp = newP.getLookup().lookup(LogicalViewProvider.class);
        assertNotNull("We have logical view provider", lvp);
        Node n = lvp.createLogicalView();
        if (!n.getDisplayName().contains("(broken)")) {
            fail("Display name shall indicate that it is broken: " + n.getDisplayName());
        }

        assertEquals("Same icons",
            ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16)),
            pi.getIcon()
        );
        assertEquals("Project is in lookup", newP, n.getLookup().lookup(Project.class));


        OpenProjects.getDefault().close(new Project[] { newP });
    }


    public static final class DD extends DialogDisplayer {
        static int cnt = 0;
        
        @Override
        public Object notify(NotifyDescriptor descriptor) {
            cnt++;
            return NotifyDescriptor.OK_OPTION;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }

    private File createModule(String fileName, String... attribs) throws IOException {
        File d = new File(getWorkDir(), "modules");
        d.mkdirs();
        File m = new File(d, fileName);
        FileOutputStream out = new FileOutputStream(m);
        Manifest man = new Manifest();
        for (int i = 0; i < attribs.length; i += 2) {
            man.getMainAttributes().putValue(attribs[i], attribs[i + 1]);
        }
        JarOutputStream os = new JarOutputStream(out, man);
        os.close();
        return m;
    }
}
