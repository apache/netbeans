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

package org.netbeans.test.java.gui.wizards;


import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.java.Utilities;
import org.netbeans.test.java.gui.GuiUtilities;
import java.io.*;
import junit.framework.Test;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.java.Common;
import org.netbeans.test.java.JavaTestCase;

/**
 * Tests the New File Wizard.
 * @author Roman Strobl
 */
public class NewFileWizardTest extends JavaTestCase {
    
    // default path to bundle file
    private static final String JAVA_BUNDLE_PATH = "org.netbeans.modules.java.project.Bundle";
    
    // default timeout for actions in miliseconds
    private static final int ACTION_TIMEOUT = 1000;
    
    // name of sample project
    private static final String TEST_PROJECT_NAME = "TestProject";
    
    // name of sample package
    private static final String TEST_PACKAGE_NAME = "test";
    
    // name of sample class
    private static final String TEST_CLASS_NAME = "TestClass";
    
    // name of invalid package
    private static final String TEST_PACKAGE_NAME_INVALID = "a/b";
    
    /**
     * error log
     */
    protected static PrintStream err;
    /**
     * standard log
     */
    protected static PrintStream log;
    
    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";
    
    static String projectDir;
        
    /**
     * Main method for standalone execution.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    /**
     * Sets up logging facilities.
     */
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        err = getLog();
        log = getRef();
        JemmyProperties.getProperties().setOutput(new TestOut(null,
                new PrintWriter(err, true), new PrintWriter(err, false), null));
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) { }
    }
    
    /**
     * Creates a new instance of Main
     * @param testName name of test
     */
    public NewFileWizardTest(String testName) {
        super(testName);
    }
    
    public void testCreateProject() {
        projectDir = GuiUtilities.createProject(TEST_PROJECT_NAME, workDir);
    }
    
    /**
     * Tests creating a project.
     */
    public void testCreateProject(String projectName) {
        projectDir = GuiUtilities.createProject(projectName, workDir);
    }
    
    public void testDeleteProject() {
        GuiUtilities.deleteProject(TEST_PROJECT_NAME, null, projectDir, false);
    }
    
    /**
     * Tests deleting a project including files on hard drive.
     */
    public void testDeleteProject(String projectName) {
        GuiUtilities.deleteProject(projectName, null, projectDir, false);
    }
    
    /**
     * Tests creating of a package.
     */
    public void testCreatePackage() {
        createIfNotOpened(TEST_PROJECT_NAME, TEST_PACKAGE_NAME);
        GuiUtilities.createPackage(TEST_PROJECT_NAME,TEST_PACKAGE_NAME+TEST_PACKAGE_NAME);
    }
    
    public void testCreatePackage(String projName,String packName) {        
        GuiUtilities.createPackage(projName,packName);        
    }
    
    /**
     * Tests deleting of a package.
     */
    public void testDeletePackage() {
        // delete a package
        createIfNotOpened(TEST_PROJECT_NAME, TEST_PACKAGE_NAME);
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME);
        n.select();
        n.performPopupAction("Delete");
        
        // confirm
        new NbDialogOperator(Bundle.getString("org.openide.explorer.Bundle",
                "MSG_ConfirmDeleteObjectTitle")).yes();
        
    }
    
    /**
     * Tests New File wizard.
     * - create test project
     * - create test package
     * - create test class through New File wizard (core of the test)
     * - close opened file and project
     * - delete the project incl. all files on disc
     */
    public void testNewFileWizardComplex() {
        // create test project
        //testCreateProject(TEST_PROJECT_NAME);
        
        // create test package
        //testCreatePackage(TEST_PROJECT_NAME,TEST_PACKAGE_NAME);

        createIfNotOpened(TEST_PROJECT_NAME, TEST_PACKAGE_NAME);

        // select project node
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        
        // create test class
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        
        op.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes"));
        op.selectFileType("Java Class");
        op.next();
        
        JTextFieldOperator tf = new JTextFieldOperator(op);
        tf.setText(TEST_CLASS_NAME);
        
        op.finish();
        
        // check generated source
        EditorOperator editor = new EditorOperator(TEST_CLASS_NAME);
        String text = editor.getText();
        
        // check if class name is generated 4 times in the source code
        int oldIndex = 0;
        for (int i=0; i<1; i++) {
            oldIndex = text.indexOf(TEST_CLASS_NAME, oldIndex);
            if (oldIndex>-1) oldIndex++;
        }        
        assertTrue("Error in generated class "+TEST_CLASS_NAME+".java.",oldIndex!=-1);        
        editor.close();
        
        // delete test package
        testDeletePackage();
        
        // delete test project
        testDeleteProject(TEST_PROJECT_NAME);
        
    }
    
    /**
     * Negative test for creating of a package.
     */
    public void testCreatePackageFailure() {
        createIfNotOpened(TEST_PROJECT_NAME, TEST_PACKAGE_NAME);
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        
        // wait till all fields are loaded
        JDialogOperator jdo = new JDialogOperator(
                org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewFileWizard_Title"));
        JTreeOperator jto = new JTreeOperator(jdo, 0);
        boolean exitLoop = false;
        for (int i=0; i<10; i++) {
            for (int j=0; j<jto.getChildCount(jto.getRoot()); j++) {
                if (jto.getChild(jto.getRoot(), j).toString()==
                        Bundle.getString(JAVA_BUNDLE_PATH,
                        "Templates/Classes")) {
                    exitLoop = true;
                    break;
                }
            }
            if (exitLoop) break;
            Utilities.takeANap(1000);
        }
        
        // choose package
        op.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,
                "Templates/Classes"));
        op.selectFileType("Java Package");
        op.next();
        
        // try to set an invalid name
        JTextFieldOperator tfp = new JTextFieldOperator(op, 0);
        tfp.setText(TEST_PACKAGE_NAME_INVALID);
        //for (int i=0; i<10; i++) {
        //    JButtonOperator jbo = new JButtonOperator(op,
        //        Bundle.getString("org.openide.Bundle", "CTL_FINISH"));
        //    if (!jbo.isEnabled()) break;
        //    Utilities.takeANap(1000);
        //}
        Utilities.takeANap(1000);
        
        // check finish button
        //JButtonOperator jbo = new JButtonOperator(op,
        //        Bundle.getString("org.openide.Bundle", "CTL_FINISH"));
        
        //this should be replaced with line above
        JButtonOperator jbo = new JButtonOperator(op, "Finish");
        
        assertFalse("Finish button should be disabled for package with "
                +"invalid name.", jbo.isEnabled());
        
        new NbDialogOperator(Bundle.getString(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewProjectWizard_Subtitle")+" "
                +"Java Package").cancel();
    }
    
    public void testCreateInterface() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyIface" , "Java Interface",
                Common.unify(expected),true);
        
    }
    
    public void testCreateAnnotation() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyAnnot" , "Java Annotation Type",
                Common.unify(expected),true);
    }
    
    public void testCreateEnum() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyEnum" ,"Java Enum",
                Common.unify(expected),true);
    }
    
    public void testCreateRecord() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyRecord", "Java Record",
                Common.unify(expected), true);
    }
    
    public void testCreateException() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyExp" ,"Java Exception",
                Common.unify(expected),true);
    }
    
    
    public void testCreateJApplet() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyJApplet" , "JApplet",
                Common.unify(expected),true);
    }
    
    public void testCreateEmptyFile() {
        String expected = getContentOfGoldenFile();
        createAndVerify("Empty" ,"Empty Java File", 
                Common.unify(expected),true);
    }
    
    public void testCreateMainClass() {
        String expected = getContentOfGoldenFile();
        createAndVerify("MyMain" , "Java Main Class",
                Common.unify(expected),true);
    }
    
    public void testCreatePackageInfo() {
        String expected = getContentOfGoldenFile();
        createAndVerify("package-info" , "Java Package Info",
                Common.unify(expected),true);
    }
    
    public void testCreateClass() {
        String expected = getContentOfGoldenFile();
        createAndVerify("JavaClass" , "Java Class",
                Common.unify(expected),true);
    }
    
    
    
    public void testInvalidName() {
        String expected = "";
        createAndVerify("Name,invalid" ,"Java Class",
                Common.unify(expected),false);
    }
    
    public void testExistingName() {
        createFile("TestExisting", "Java Class", true);
        createAndVerify("TestExisting" , "Java Class", "",false);
    }
    
    private void createIfNotOpened(String projName,String packName) {
        Node pn = null;
        try {
            pn = new ProjectsTabOperator().getProjectRootNode(projName);
        } catch(TimeoutExpiredException tee) {
            System.out.println("Project is not opened, creating new one");
            testCreateProject(projName);
            pn = new ProjectsTabOperator().getProjectRootNode(projName);
        }
        Node n = null;
        try {
            n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                    "org.netbeans.modules.java.j2seproject.Bundle",
                    "NAME_src.dir")+"|"+packName);
        } catch (TimeoutExpiredException tee) {
            System.out.println("Expected package not present, creating new one");
            testCreatePackage(projName,packName);
            n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                    "org.netbeans.modules.java.j2seproject.Bundle",
                    "NAME_src.dir")+"|"+packName);
        }
        n.select();
        
    }
    
    /**
     * Method for creation file from new file wizard
     *
     * @param name Name of new file
     * @param type String expression of new file type
     * @param expectedContent Expected content of the new file
     * @param shouldPass Indicated it there is expected error in the wizard
     */
    private void createAndVerify(String name, String type, String expectedContent, boolean shouldPass) {
        if(createFile(type, name, shouldPass)) return;
        // check generated source
        EditorOperator editor = null;
        try {
            editor = new EditorOperator(name);
            String text = Common.unify(editor.getText());
            log(expectedContent);
            log(text);
            assertEquals("File doesnt have expected content",expectedContent,text);
        } finally {
            if(editor!=null) editor.close();
        }
        
        
    }

    /**
     * Creates new file
     * @param type Type of new file
     * @param name Name of new file
     * @param shouldPass Indicated it there is expected error in the wizard
     * @return True is file was created successfully 
     */
    private boolean createFile(String type, String name, boolean shouldPass) {
        createIfNotOpened(TEST_PROJECT_NAME, TEST_PACKAGE_NAME);
        // select project node
        Node pn = new ProjectsTabOperator().getProjectRootNode(TEST_PROJECT_NAME);
        pn.select();
        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir")+"|"+TEST_PACKAGE_NAME);
        n.select();
        // create test class
        NewFileWizardOperator op = NewFileWizardOperator.invoke();
        op.selectCategory(Bundle.getString(JAVA_BUNDLE_PATH,"Templates/Classes"));
        op.selectFileType(type);
        op.next();
        JTextFieldOperator tf = new JTextFieldOperator(op);
        tf.setText(name);
        if (!shouldPass) {
            Utilities.takeANap(1000);
            JButtonOperator jbo = new JButtonOperator(op,"Finish");
            assertFalse("Finish button should be disabled", jbo.isEnabled());
            // closing wizard
            new NbDialogOperator(Bundle.getString(
                    "org.netbeans.modules.project.ui.Bundle",
                    "LBL_NewProjectWizard_Subtitle")+" "
                    +type).cancel();
            return true;
        }
        op.finish();
        return false;
    }
    
    /**
     *
     * @return
     */
    public String getContentOfGoldenFile() {
        try {
            File golden = getGoldenFile();
            BufferedReader br = new BufferedReader(new FileReader(golden));
            StringBuilder res = new StringBuilder();
            String line;
            while((line = br.readLine())!=null) {
                res.append(line);
                res.append("\n");
            }
            return res.toString();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }
        return null;
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(NewFileWizardTest.class).enableModules(".*").clusters(".*"));
    }
}
