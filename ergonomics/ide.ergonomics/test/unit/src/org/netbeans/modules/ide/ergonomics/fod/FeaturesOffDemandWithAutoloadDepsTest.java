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

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
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
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FeaturesOffDemandWithAutoloadDepsTest extends NbTestCase implements PropertyChangeListener {
    FileObject root;
    private static final InstanceContent ic = new InstanceContent();

    static {
        FeatureManager.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private int change;


    public FeaturesOffDemandWithAutoloadDepsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(FeaturesOffDemandWithAutoloadDepsTest.class).
            clusters("ergonomics\\d*").
            clusters("ide\\d*|extide\\d*|java\\d*").
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
        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (info.getCodeNameBase().equals("org.netbeans.modules.java.kit")) {
                assertFalse("Module is disabled", info.isEnabled());
                found = true;
            }
            sb.append(info.getCodeNameBase()).append('\n');
        }
        if (!found) {
            fail("No module found:\n" + sb);
        }

        FeatureInfo info = FeatureInfo.create(
            "java",
            FeaturesOffDemandWithDepsTest.class.getResource("FeatureInfo.xml"),
            FeaturesOffDemandWithDepsTest.class.getResource("TestBundle5.properties")
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

    public void testFoDModuleFilesAreAnnotatedWithAttributes() throws Exception {
        FileObject sub = FileUtil.getConfigFile("Modules/org-netbeans-modules-java-kit.xml");
        assertNotNull("Module config file found", sub);

        String origContent = sub.asText("UTF-8");

        FileObject prjFO1 = root.getFileObject("1st");
        FileObject prjFO2 = root.getFileObject("2nd");

        assertTrue("Recognized as project", ProjectManager.getDefault().isProject(prjFO1));
        Project p = ProjectManager.getDefault().findProject(prjFO1);
        assertNotNull("Project found", p);

        long before = System.currentTimeMillis();
        Thread.sleep(1000);

        TestFactory.recognize.add(prjFO1);
        TestFactory.recognize.add(prjFO2);
        OpenProjects.getDefault().open(new Project[] { p }, false);

        Thread.sleep(1000);
        long after = System.currentTimeMillis();

        List<Project> arr = Arrays.asList(OpenProjects.getDefault().openProjects().get());
        assertEquals("However one instance is there", 1, arr.size());
        Project newP = arr.get(0).getLookup().lookup(Project.class);
        if (p == newP) {
            fail("New project is made available, not old: " + newP);
        }
        assertEquals("Right type", TestFactory.class, newP.getClass());

        OpenProjects.getDefault().close (new Project[] { newP });
        if (OpenProjects.getDefault().getOpenProjects().length != 0) {
            fail("All projects shall be closed: " + Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        }

        assertNotNull("File is found in userdir", FileUtil.toFile(sub));
        final Object when = sub.getAttribute("ergonomicsEnabled");
        final Object cnt = sub.getAttribute("ergonomicsUnused");
        assertNotNull("Not enabled manually", cnt);
        assertNotNull("Not enabled manually", when);
        assertEquals("Integer", Integer.class, cnt.getClass());
        assertEquals("Set to zero", Integer.valueOf(0), cnt);

        Long modified = FeaturesOffDemandWithDepsTest.findLastModified(sub);
        assertEquals("enabled attribute is same as modification day", when, modified);
        final String middleState = sub.asText("UTF-8");
        if (origContent.equals(middleState)) {
            fail("The module shall be enabled right now:\n" + sub.asText("UTF-8"));
        }
        FeatureManager.incrementUnused(OpenProjects.getDefault().getOpenProjects());

        String mf = "" +
                "OpenIDE-Module: org.depends.on.java.kit\n" +
                "OpenIDE-Module-Module-Dependencies: org.netbeans.modules.java.platform > 1.37\n" +
                "\n" +
                "\n" +
                "";
        File jar = emptyJAR("org-depends-on-java-kit.jar", mf);
        Module module = Main.getModuleSystem().getManager().create(jar, jar, false, false, false);
        assertFalse("Not yet enabled", module.isEnabled());
        ModuleManager man = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        try {
            man.mutexPrivileged().enterWriteAccess();
            man.enable(module);
            assertTrue("enabled now", module.isEnabled());
        } finally {
            man.mutexPrivileged().exitWriteAccess();
        }
        FileObject dep = FileUtil.getConfigFile("Modules/org-depends-on-java-kit.xml");
        assertNotNull("Module config file found", dep);

        FeatureManager.incrementUnused(OpenProjects.getDefault().getOpenProjects());
        FeatureManager.disableUnused(2);

        assertTrue("Config file exists", sub.canRead());
        String newContent = sub.asText("UTF-8");

        assertEquals("The java.kit's state remains unmodified", middleState, newContent);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        change++;
    }

    private File emptyJAR(String name, String manifest) throws IOException {
        File f = new File(getWorkDir(), name);
        Manifest mf = new Manifest(new ByteArrayInputStream(manifest.getBytes("utf-8")));
        mf.getMainAttributes().putValue("Manifest-Version", "1.0");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(f), mf);
        os.close();

        return f;
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


}
