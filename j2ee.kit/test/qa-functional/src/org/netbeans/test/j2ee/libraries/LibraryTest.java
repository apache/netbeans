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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.test.j2ee.libraries;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.j2ee.lib.ContentComparator;
import org.netbeans.test.j2ee.lib.FilteringLineDiff;
import org.netbeans.test.j2ee.lib.Utils;
import org.netbeans.test.j2ee.wizard.WizardUtils;

/**
 *
 * @author jungi
 */
public class LibraryTest extends J2eeTestCase {
    
    private static boolean CREATE_GOLDEN_FILES = Boolean.getBoolean("org.netbeans.test.j2ee.libraries.golden");
    private static final String CATEGORY_JAVA_EE = "Java EE";
    //private static boolean CREATE_GOLDEN_FILES = true;
    
    protected String appName = "LibsInclusionTestApp";
    protected String libName = "My_Math_Library-1.0";
    protected String ejbName = "MultiSrcRootEjb";
    protected String webName = "MultiSrcRootWar";
    protected ProjectsTabOperator pto = ProjectsTabOperator.invoke();
    
    /** Creates a new instance of LibraryTest */
    public LibraryTest(String s) {
        super(s);
    }
    
    private static final String EAR_BUNDLE
            = "org.netbeans.modules.j2ee.earproject.ui.actions.Bundle";
    
    /**
     * Tests if Add Java EE module action in EAR project also adds entries
     * to standard deployment descriptor.
     */
    public void testDD() throws Exception {
        //create library
        Utils.createLibrary(libName,
                new String[] {getDataDir().getAbsolutePath() + File.separator + "libs" + File.separator + "MathLib.jar"},
                new String[] {getDataDir().getAbsolutePath() + File.separator + "libs" + File.separator + "math.zip"},
                null);
        //create empty j2ee project
        WizardUtils.createNewProject(CATEGORY_JAVA_EE,"Enterprise Application");
        NewJavaProjectNameLocationStepOperator npnlso =
                WizardUtils.setProjectNameLocation(appName, getProjectPath());
        WizardUtils.setJ2eeSpecVersion(npnlso, "5");
        //Create EJB Module:
        String moduleLabel = Bundle.getStringTrimmed("org.netbeans.modules.javaee.project.api.ant.ui.wizard.Bundle", "LBL_NEAP_CreateEjbModule");
        JCheckBoxOperator jcbo = new JCheckBoxOperator(npnlso, moduleLabel);
        jcbo.setSelected(false);
        //Create Web Application Module:
        moduleLabel = Bundle.getStringTrimmed("org.netbeans.modules.javaee.project.api.ant.ui.wizard.Bundle", "LBL_NEAP_CreatWebAppModule");
        jcbo = new JCheckBoxOperator(npnlso, moduleLabel);
        jcbo.setSelected(false);
        npnlso.finish();
        //add modules to j2ee app
        addJ2eeModule(pto, appName, ejbName);
        Node modulesNode = new Node(ProjectsTabOperator.invoke().getProjectRootNode(appName), "Java EE Modules");
        Node ejbNode = new Node(modulesNode, ejbName);
        addJ2eeModule(pto, appName, webName);
        Node webNode = new Node(modulesNode, webName);
        //build ear
        Utils.buildProject(appName);
    }
    
    /**
     * Tests if call EJB action adds EJB module to WEB project,
     * if all necessary changes in manifest files are done
     * and if final EAR application contains all modules and libraries
     */
    public void testDDMs() throws Exception {
        //add library to EJB module
        addLibrary(pto, ejbName, libName);
        //call EJB from websvc in web => should add ejbs on web's classpath
        EditorOperator eo = EditorWindowOperator.getEditor("ServletForEJB.java");
        String ejbjar_bundle
                = "org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle";
        eo.setCaretPosition(37, 7);
        GenerateCodeOperator.openDialog(
                Bundle.getStringTrimmed(ejbjar_bundle, "LBL_CallEjbAction"), eo);
        NbDialogOperator ndo = new NbDialogOperator(
                Bundle.getStringTrimmed(ejbjar_bundle, "LBL_CallEjbActionTitle"));
        Node n = new Node(new JTreeOperator(ndo), "MultiSrcRootEjb|LocalSessionSB");
        n.select();
        JRadioButtonOperator jrbo = new JRadioButtonOperator(ndo, 0);
        jrbo.setSelected(true);
        ndo.ok();
        eo.txtEditorPane().waitText("lookupLocalSessionLocal()");  //NOI18N
        new EventTool().waitNoEvent(1000);  // wait for better stability
        eo.save();
        //check servlet impl class if code is added correctly
        File ws = new File(getDataDir(), "projects/MultiSrcRootWar/src/java/servlet/ServletForEJB.java");
        checkFiles(Arrays.asList(new File[] {ws}));
        //edit manifests
        editManifest(pto, ejbName);
        editManifest(pto, webName);
        editManifest(pto, appName);
        //build ear
        Utils.cleanProject(appName);
        Utils.buildProject(appName);
        //check ear's & MFs in all components
        List l = new ArrayList();
        File f = new File(getProjectPath(), appName);
        JarFile ear = null;
        try {
            f = new File(f, "build");
            l.add(getManifest(new JarFile(new File(f, "MultiSrcRootEjb.jar")),
                    new File(System.getProperty("xtest.tmpdir"), "libtest-ejb.mf")));
            l.add(getManifest(new JarFile(new File(f, "MultiSrcRootWar.war")),
                    new File(System.getProperty("xtest.tmpdir"), "libtest-web.mf")));
            ear = new JarFile(new File(f.getParentFile(), "dist/LibsInclusionTestApp.ear"));
            l.add(getManifest(ear,
                    new File(System.getProperty("xtest.tmpdir"), "libtest-ear.mf")));
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
        checkFiles(l);
        assertNotNull("don't have ear", ear);
        //check if all components are in ear
        assertNotNull("MultiSrcRootWar.war is not in created ear",
                ear.getEntry("MultiSrcRootWar.war"));
        assertNotNull("MultiSrcRootEjb.jar is not in created ear",
                ear.getEntry("MultiSrcRootEjb.jar"));
        assertNotNull("MathLib.jar is not in created ear",
                ear.getEntry("lib/MathLib.jar"));
    }
    
    protected void addJ2eeModule(ProjectsTabOperator pto, String appName,
            String moduleName) {
        Node node = pto.getProjectRootNode(appName);
        node.performPopupActionNoBlock(Bundle.getStringTrimmed(
                EAR_BUNDLE, "LBL_AddModuleAction"));
        NbDialogOperator ndo = new NbDialogOperator(Bundle.getStringTrimmed(
                EAR_BUNDLE, "LBL_ModuleSelectorTitle"));
        JTreeOperator jto = new JTreeOperator(ndo);
        jto.selectPath(jto.findPath(moduleName));
        ndo.ok();
        new EventTool().waitNoEvent(2500);
    }
    
    protected void addLibrary(ProjectsTabOperator pto, String moduleName,
            String libName) {
        Node node = new Node(pto.getProjectRootNode(moduleName), "Libraries");
        node.performPopupActionNoBlock("Add Library...");
        NbDialogOperator ndo = new NbDialogOperator("Add Library");
        new EventTool().waitNoEvent(1000);
        JTreeOperator jto = new JTreeOperator(ndo);
        jto.selectPath(jto.findPath("Global Libraries|" + libName));
        new JButtonOperator(ndo, "Add Library").push();
    }
    
    protected void editManifest(ProjectsTabOperator pto, String moduleName) {
        Node n = new Node(pto.getProjectRootNode(moduleName),
                "Configuration Files|MANIFEST.MF");
        n.performPopupAction("Open");
        EditorOperator eo = EditorWindowOperator.getEditor("MANIFEST.MF");
        eo.insert("Implementation-Vendor: example.com", 2, 1);
        eo.close(true);
        new EventTool().waitNoEvent(1000);
    }
    
    private File getManifest(JarFile jf, File f) throws IOException {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
        jf.getManifest().write(os);
        if (os != null) {
            os.close();
        }
        return f;
    }
    
    /** Returns path to project root which is shared between all test cases. */
    private String getProjectPath() throws Exception {
        return getWorkDir().getParentFile().getParentFile().getCanonicalPath();
    }
    
    protected void checkFiles(List newFiles) {
        if (!CREATE_GOLDEN_FILES) {
            List l = new ArrayList(newFiles.size());
            for (Iterator i = newFiles.iterator(); i.hasNext();) {
                File newFile = (File) i.next();
                try {
                    if (newFile.getName().endsWith(".xml") && !newFile.getName().startsWith("glassfish-")) {
                        assertTrue(ContentComparator.equalsXML(getGoldenFile(getName() + "/" + newFile.getName() + ".pass"), newFile));
                    } else if (!newFile.getName().endsWith(".mf")) {
                        assertFile(newFile, getGoldenFile(getName() + "/" + newFile.getName() + ".pass"),
                                new File(getWorkDirPath(), newFile.getName() + ".diff"), new FilteringLineDiff());
                    } else {
                        assertTrue(ContentComparator.equalsManifest(
                                newFile,
                                getGoldenFile(getName() + "/" + newFile.getName() + ".pass"),
                                new String[] {"Created-By", "Ant-Version"}));
                    }
                } catch (Throwable t) {
                    Utils.copyFile(newFile, new File(getWorkDirPath(), newFile.getName() + ".bad"));
                    Utils.copyFile(getGoldenFile(getName() + "/" + newFile.getName() + ".pass"),
                            new File(getWorkDirPath(), newFile.getName() + ".gf"));
                    l.add(newFile.getName());
                }
            }
            assertTrue("File(s) " + l.toString() + " differ(s) from golden files.", l.isEmpty());
        } else {
            createGoldenFiles(newFiles);
        }
    }
    
    private void createGoldenFiles(List/*File*/ from) {
        File f = getDataDir();
        List names = new ArrayList();
        names.add("goldenfiles");
        while (!f.getName().equals("test")) {
            if (!f.getName().equals("sys") && !f.getName().equals("work") &&!f.getName().equals("tests")) {
                names.add(f.getName());
            }
            f=f.getParentFile();
        }
        for (int i=names.size()-1;i > -1;i--) {
            f=new File(f,(String)(names.get(i)));
        }
        f = new File(f, getClass().getName().replace('.', File.separatorChar));
        File destDir = new File(f, getName());
        destDir.mkdirs();
        for (Iterator i = from.iterator(); i.hasNext();) {
            File src = (File) i.next();
            Utils.copyFile(src, new File(destDir, src.getName() + ".pass"));
        }
        assertTrue("Golden files generated.", false);
    }
    
}
