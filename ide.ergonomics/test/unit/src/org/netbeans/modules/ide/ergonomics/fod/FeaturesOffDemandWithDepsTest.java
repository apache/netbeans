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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
public class FeaturesOffDemandWithDepsTest extends NbTestCase implements PropertyChangeListener {
    FileObject root;
    private static final InstanceContent ic = new InstanceContent();

    static {
        FeatureManager.assignFeatureTypesLookup(new AbstractLookup(ic));
    }
    private int change;
    
    
    public FeaturesOffDemandWithDepsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        System.setProperty("org.netbeans.core.startup.level", "OFF");

        return NbModuleSuite.create(
            NbModuleSuite.emptyConfiguration().
            addTest(FeaturesOffDemandWithDepsTest.class).
            clusters("ergonomics[0-9]*").
            clusters("ide[0-9]*|extide[0-9]*|java[0-9]*").
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

        Long modified = findLastModified(sub);
        assertEquals("enabled attribute is same as modification day", when, modified);
        final String middleState = sub.asText("UTF-8");
        if (origContent.equals(middleState)) {
            fail("The module shall be enabled right now:\n" + sub.asText("UTF-8"));
        }
        FeatureManager.incrementUnused(OpenProjects.getDefault().getOpenProjects());

        String mf = "" +
                "OpenIDE-Module: org.depends.on.java.kit\n" +
                "OpenIDE-Module-Module-Dependencies: org.netbeans.modules.java.kit\n" +
                "\n" +
                "\n" +
                "";
        File jar = emptyJAR("org-depends-on-java-kit.jar", mf);
        ModuleManager man = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        try {
            man.mutexPrivileged().enterWriteAccess();
            Module module = man.create(jar, jar, false, false, false);
            assertFalse("Not yet enabled", module.isEnabled());
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

    static long findLastModified(FileObject sub) {
        long w = 0;
        for (FileObject sblng : sub.getParent().getChildren()) {
            long t = sblng.lastModified().getTime();
            if (t > w) {
                w = t;
            }
        }
        return w;
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
