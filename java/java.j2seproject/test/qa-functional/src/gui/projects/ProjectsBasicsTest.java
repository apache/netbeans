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

package gui.projects;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import junit.framework.Test;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.NbModuleSuite;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.JarFileSystem;
import org.openide.util.Exceptions;

/**
 * Test project and project dependencies on JAR or library
 *
 * @author Tomas Musil
 */
public class ProjectsBasicsTest extends JellyTestCase {

    public static String sampleName = "sample1";
    public static String sampleNameSharable = "sharable1";
    public static String location  = System.getProperty("netbeans.user");
    public static String jarName = "TestJar.jar";
    public static String libJarName = "LibraryJar.jar";
    public static String libJarSourcesName = "src.zip";
    public static String libraryName = "MyLibrary";

    /** Need to be defined because of JUnit */
    public ProjectsBasicsTest(String name) {
        super(name);
    }

    public static Test suite() {
        return NbModuleSuite.create(ProjectsBasicsTest.class, ".*", ".*");
    }

    public @Override void setUp() {
        System.out.println("########  "+getName()+"  #######");
    }


    /** Create J2SE project */
    public void testCreateJ2SEproject(){
        createProject(sampleName, Labels.standard, Labels.javaApp, false);
    }

    /** Add JAR dependency to J2SE project and check classpath */
    public void testAddDependencyOnJar(){
        File jarFile = new File(getDataDir(), jarName);
        assert jarFile.exists();
        addJarDependency(sampleName, jarName, false);
        assert isJarOnClasspath(jarFile, sampleName);

    }

    /** Create sharable J2SE project */
    public void testCreateSharableJ2SEproject(){
        createProject(sampleNameSharable, Labels.standard, Labels.javaApp, true);
        assert isProjectSharable(sampleNameSharable);
    }

    /** Add JAR dependency to sharable J2SE project and check classpath */
    public void testAddJarDependencyForSharable(){
        File jarFile = new File(getDataDir(), jarName);
        assert jarFile.exists();
        assert !sharableProjectContains(sampleNameSharable, jarName);
        assert !isJarOnClasspath(jarFile, sampleNameSharable);
        addJarDependency(sampleNameSharable, jarName, true);
        assert sharableProjectContains(sampleNameSharable, jarName);
        assert isJarOnClasspath(jarFile, sampleNameSharable);
    }

    /** Create J2SE library with JAR and sources */
    public void testCreateLibrary(){
        new ActionNoBlock(Labels.tools+"|"+Labels.libraries,null).perform();
        NbDialogOperator lmo = new NbDialogOperator(Labels.libMgrDlgLbl);
        new JButtonOperator(lmo, Labels.addNewLibraryBtnLbl).push();
        NbDialogOperator nlDlg = new NbDialogOperator(Labels.newLibraryDlgLbl);
        nlDlg.ok();
        assert new JTextFieldOperator(lmo).getText().equals(libraryName);
        new JButtonOperator(lmo,Labels.addToCpBtnLbl).push();
        selectInFileChooserFromDatadir(libJarName);
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(lmo);
        jtpo.setSelectedIndex(1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        assert jtpo.getSelectedIndex() == 1;
        new JButtonOperator(lmo,Labels.addSrcBtnLbl).push();
        selectInFileChooserFromDatadir(libJarSourcesName);
        lmo.ok();
    }


    /** Add library dependency to J2SE project and check classpath and sources */
    public void testAddLibraryDependency(){
        File libraryJarFile = new File(getDataDir(),libJarName);
        assert !isJarOnClasspath(libraryJarFile, sampleName);
        addLibraryDependency(sampleName, libraryName);
        assert isJarOnClasspath(libraryJarFile, sampleName);
        assert isSourceForJarAvailable(libraryJarFile, sampleName);
    }


    //---- Private methods ------------

    private void selectInFileChooserFromDatadir(String fileName){
        JFileChooserOperator fco1 = new JFileChooserOperator();
        fco1.setCurrentDirectory(getDataDir());
        fco1.selectFile(fileName);
        fco1.approve();

    }

    private boolean isProjectSharable(String prjName){
        return sharableProjectContains(prjName, "nblibraries.properties"); //NOI18N
    }

    private boolean sharableProjectContains(String prjName, String child){
        boolean res = false;
        File prj = new File(location, prjName);
        File lib = new File(prj,"lib"); //NOI18N
        if (lib.isDirectory()){
            List ch = Arrays.asList(lib.list());
            res = ch.contains(child);
        }
        return res;

    }


    private void addJarDependency(String project, String jarName, boolean toSharableLib){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node libNode = new Node(pto.tree(),project+"|"+Labels.librariesNode); //NOI18N
        assert libNode.isPresent();
        libNode.performPopupActionNoBlock("Add JAR/Folder...");
        JFileChooserOperator fco = new JFileChooserOperator();
        fco.setCurrentDirectory(getDataDir());
        fco.selectFile(jarName);
        if (toSharableLib){
            JRadioButtonOperator rb = new JRadioButtonOperator(fco, Labels.copyToShLibRadioLbl);
            rb.push();
        }
        fco.approve();
        waitScanFinished();
        assert libNode.isChildPresent(jarName);
        libNode.expand();
    }

    private void addLibraryDependency(String project, String libName){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node libNode = new Node(pto.tree(),project+"|"+Labels.librariesNode); //NOI18N
        assert libNode.isPresent();
        int countNodes = libNode.getChildren().length;
        libNode.performPopupActionNoBlock(Labels.addLibraryActionLbl);
        NbDialogOperator libo = new NbDialogOperator(Labels.addLibraryDlg);
        JTreeOperator jto = new JTreeOperator(libo);
        Node n = new Node(jto, Labels.globalLibraries + "|"+libName);
        n.select();
        new JButtonOperator(libo, Labels.addLibraryBtn).push();
        if (!libNode.isExpanded()) libNode.expand();
        assert countNodes + 1 == libNode.getChildren().length;
    }




    private void createProject(String name, String category, String project, boolean sharable){
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory(category);
        npwo.selectProject(project);
        npwo.next();
        NewJavaProjectNameLocationStepOperator npnlso = new NewJavaProjectNameLocationStepOperator();
        npnlso.txtProjectLocation().setText(location); // NOI18N
        npnlso.txtProjectName().setText(name);
        if(sharable) new JCheckBoxOperator(npnlso).push();
        npnlso.finish();
        waitScanFinished();
    }

    private  boolean  isSourceForJarAvailable(File jarFile, String projectName){
        boolean result = false;
        try {
            ClassPath cp = getCpForProjectSrcRoot(projectName);
            List<Entry> le = cp.entries();
            Entry e = null;
            for (Entry entry : le) {
                if (entry.getURL().toString().contains(jarFile.getName())) {
                    e = entry;
                }
            }
            assert e != null;
            URL url = e.getURL();
            FileObject[] src = SourceForBinaryQuery.findSourceRoots(url).getRoots();
            result = src.length == 1 && src[0].getFileSystem() instanceof JarFileSystem && src[0].getName().equals("src");
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result;
    }

    private boolean isJarOnClasspath(File jarfile, String projectName){
        ClassPath cp = getCpForProjectSrcRoot(projectName);
        return cp.toString().contains(jarfile.getName());
    }

    private ClassPath getCpForProjectSrcRoot(String projectName){
        ProjectManager pm = ProjectManager.getDefault();
        ClassPath cp = null;
        try {
            File fPrj = new File(location, projectName);
            assert fPrj.exists();
            FileObject foPrj = FileUtil.toFileObject(fPrj);
            assert foPrj!=null && pm.isProject(foPrj);
            Project p = pm.findProject(foPrj);
            Sources s = ProjectUtils.getSources(p);
            SourceGroup[] sgArr = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            assert sgArr.length>0;
            //take sources
            SourceGroup sg = sgArr[0];
            cp = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return cp;


    }

}
