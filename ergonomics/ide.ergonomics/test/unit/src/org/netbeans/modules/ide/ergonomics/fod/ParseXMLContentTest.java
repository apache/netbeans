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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
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
public class ParseXMLContentTest extends NbTestCase implements PropertyChangeListener {
    FileObject root;
    private static final InstanceContent ic = new InstanceContent();

    static {
        FeatureManager.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private int change;
    private ModuleInfo fav;
    private ModuleInfo au;
    
    
    public ParseXMLContentTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(ParseXMLContentTest.class).
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
        int found = 0;
        Exception ex2 = null;
        for (ModuleInfo info : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            boolean disable = false;
            if (
                info.getCodeNameBase().equals("org.netbeans.modules.subversion")
            ) {
                disable = true;
                au = info;
            }
            if (
                info.getCodeNameBase().equals("org.netbeans.modules.favorites")
            ) {
                disable = true;
                fav = info;
            }
            if (disable) {
             Method m = null;
                Class<?> c = info.getClass();
                for (;;) {
                    if (c == null) {
                        throw ex2;
                    }
                    try {
                        m = c.getDeclaredMethod("setEnabled", Boolean.TYPE);
                    } catch (Exception ex) {
                        ex2 = ex;
                    }
                    if (m != null) {
                        break;
                    }
                    c = c.getSuperclass();
                }
                m.setAccessible(true);
                m.invoke(info, false);
                assertFalse("Module is disabled", info.isEnabled());
                found++;
            }
            sb.append(info.getCodeNameBase()).append('\n');
        }
        if (found != 2) {
            fail("Two shall be found, was " + found + ":\n" + sb);
        }

        FeatureInfo info = FeatureInfo.create(
            "TestFactory",
            ParseXMLContentTest.class.getResource("FeatureInfo.xml"),
            ParseXMLContentTest.class.getResource("TestBundle.properties")
        );
        FeatureInfo info3 = FeatureInfo.create(
            "TestFactory3",
            ParseXMLContentTest.class.getResource("FeatureInfo3.xml"),
            ParseXMLContentTest.class.getResource("TestBundle3.properties")
        );
        ic.add(info);
        ic.add(info3);
        
        File dbp = new File(new File(getWorkDir(), "1st"), "dbproject");
        File db = new File(dbp, "project.properties");
        dbp.mkdirs();
        FileOutputStream os = new FileOutputStream(db);
        os.write(
            ("<db>\n" +
            "  <element>jarda</element>" +
            "</db>").getBytes()
        );
        os.close();

        db.createNewFile();

        File dbp2 = new File(new File(getWorkDir(), "2nd"), "dbproject");
        File db2 = new File(dbp2, "project.properties");
        dbp2.mkdirs();
        FileOutputStream os2 = new FileOutputStream(db2);
        os2.write(
            ("<db>\n" +
            "  <element>osel</element>" +
            "</db>").getBytes()
        );
        os2.close();

        root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("fileobject found", root);

        OpenProjects.getDefault().open(new Project[0], false);
        assertEquals("Empty", 0, OpenProjects.getDefault().getOpenProjects().length);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRecognizeTwoFeaturesProject() throws Exception {
        assertFalse("Autoupdate is not enabled", au.isEnabled());
        assertFalse("Favorites is not enabled", fav.isEnabled());

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
        info.addPropertyChangeListener(this);

        assertNull("No test factory in project", p.getLookup().lookup(TestFactory.class));
        assertNull("No test factory in project", p2.getLookup().lookup(TestFactory.class));
        assertNull("No test factory2 in project", p.getLookup().lookup(TestFactory2.class));
        assertNull("No test factory2 in project", p2.getLookup().lookup(TestFactory2.class));

        TestFactory.recognize.add(prjFO1);
        TestFactory2.recognize.add(prjFO2);
        OpenProjects.getDefault().open(new Project[] { p, p2 }, false);
        
        assertEquals("No Dialog currently created", 0, DD.cnt);
        
        List<Project> arr = Arrays.asList(OpenProjects.getDefault().openProjects().get());
        assertEquals("Both projects open is there", 2, arr.size());
        Project newP = arr.get(0).getLookup().lookup(Project.class);
        if (p == newP) {
            fail("New project is made available, not old: " + newP);
        }
        Project newP2 = arr.get(1).getLookup().lookup(Project.class);
        if (p2 == newP2) {
            fail("New project is made available, not old: " + newP2);
        }
        if (newP.getClass() == newP2.getClass()) {
            fail("Types of the project shall be different: " + newP + " == " + newP2);
        }
        assertEquals("Right type", TestFactory.class, newP.getClass());
        TestFactory tf = (TestFactory)newP;
        assertEquals("Open hook called", 1, tf.opened);

        assertEquals("Project info changed", 1, change);
        tf.assertListeners("One listener attached");
        assertEquals("Info delegates", "x", info.getName());
        assertEquals("Info delegates2", "y", info.getDisplayName());
        assertEquals("Info delegates icon", null, info.getIcon());

        OpenProjects.getDefault().close (new Project[] { newP, newP2 });
        if (OpenProjects.getDefault().getOpenProjects().length != 0) {
            fail("All projects shall be closed: " + Arrays.asList(OpenProjects.getDefault().getOpenProjects()));
        }

        assertNotNull("Test factory in opened project", p.getLookup().lookup(TestFactory.class));
        assertNotNull("Test factory2 in not yet opened project", p2.getLookup().lookup(TestFactory2.class));

        assertTrue("Favorites is enabled", fav.isEnabled());
        assertTrue("Autoupdate is enabled", au.isEnabled());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        change++;
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
