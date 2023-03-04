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
package org.netbeans.qa.form;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.diff.Diff;

/**
 * @author Adam Senk adam.senk@oracle.com
 * 
 **/
public class TemplateTest extends ExtJellyTestCase {

    public String DATA_PROJECT_NAME = "SampleProject";
    public String PACKAGE_NAME = "Source Package";
    public String PROJECT_NAME = "Java";
    public String workdirpath;
    public String jdkVersion = ExtJellyTestCase.getJDKVersionCode();
    MainWindowOperator mainWindow;
    ProjectsTabOperator pto;
    ComponentInspectorOperator cio;

    /** Constructor required by JUnit */
    public TemplateTest(String name) {
        super(name);

    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(TemplateTest.class).addTest(
                //SWING
                "testApplet",
                "testDialog",
                "testFrame",
                "testPanel",
                "testInter",
                "testMidi",
                
                "testBean",
                "testAppl",
                "testOkCancel",
                //AWT
                "testAWTApplet",
                "testAWTDialog",
                "testAWTFrame",
                "testAWTPanel"
                ).gui(true).enableModules(".*").clusters(".*"));

    }

    /** Called before every test case. */
    @Override
    public void setUp() throws IOException {

        System.out.println("########  " + getName() + "  #######");

    }

    /** Called after every test case. */
    @Override
    public void tearDown() {
    }

    /**
     * Some initial settings
     * 
     * @throws IOException
     *
     */
    public void begin() throws IOException {

        openDataProjects(DATA_PROJECT_NAME);
        workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        setTestPackageName("<default package>");
        setTestProjectName(DATA_PROJECT_NAME);


    }

    /**
     * Not used in this test case, but it can be usable in the future
     * 
     * @throws InterruptedException 
     */
    public void deleteProject() throws InterruptedException {
        //Project Deleting
        pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();

        DeleteAction delProject = new DeleteAction();
        delProject.perform();

        NbDialogOperator ndo = new NbDialogOperator("Delete Project");
        JCheckBoxOperator cbo = new JCheckBoxOperator(ndo);
        cbo.changeSelection(true);
        ndo.yes();

        Thread.sleep(2000);
        //check if project was really deleted from disc
        File f = new File(workdirpath + System.getProperty("file.separator") + DATA_PROJECT_NAME);
        System.out.println("adresar:" + f);
        if (f.exists()) {
            log("File " + DATA_PROJECT_NAME + " was not deleted correctly");
            System.exit(1);
        } else {
            log("File " + DATA_PROJECT_NAME + " was deleted correctly");
        }
    }

    /*
     * Switch to source editor
     */
    public void switchToSource(String documentName) throws InterruptedException {
        FormDesignerOperator fdo = new FormDesignerOperator(documentName);
        fdo.source();
        //removeFile(documentName);

    }

    public void openTemplate(String templateName, String category) throws InterruptedException {

        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        nfwo.selectProject(DATA_PROJECT_NAME);
        Thread.sleep(3000);
        nfwo.selectCategory(category);
        nfwo.selectFileType(templateName);
        nfwo.next();
        JComboBoxOperator jcb_package = new JComboBoxOperator(nfwo, 1);
        jcb_package.clearText();
       // jcb_package.enterText("data");
        Thread.sleep(1000);
        
        if (templateName.equals("Bean Form")) {
            nfwo.next();
            JTextFieldOperator class_name = new JTextFieldOperator(nfwo);
            class_name.setText("javax.swing.JButton");
            //nfwo.finish();

        } 
        nfwo.finish();
        Thread.sleep(2000);
        log(templateName + " is created correctly");

    }

    public void testTemplateMethod(String templateName, String category, String name) throws InterruptedException, IOException {

        openTemplate(templateName, category);

        System.out.println(getWorkDir());
        createFormAndJavaRefFile(name);
        Thread.sleep(500);
        switchToSource(name);
        Thread.sleep(500);
        boolean testForm=testFormFile(name);
        Thread.sleep(500);
        boolean testJava=testJavaFile(name);
        if(testForm&&testJava){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
        //Thread.sleep(500);


    }

    /** Test case 1.
     *Create new JApplet template in default package
     */
    public void testApplet() throws InterruptedException, IOException, Exception {


        begin();
        testTemplateMethod("JApplet Form", "Swing GUI Forms", "NewJApplet");

    }

    /** Test case 2.
     * Create new JDialog template in default package
     */
    public void testDialog() throws InterruptedException, IOException {

        testTemplateMethod("JDialog Form", "Swing GUI Forms", "NewJDialog");


    }

    /** Test case 3.
     * Create new JFrame template in default package
     */
    public void testFrame() throws InterruptedException, IOException {

        testTemplateMethod("JFrame Form", "Swing GUI Forms", "NewJFrame");

    }

    /** Test case 4.
     * Create new JInternalFrame template in default package
     */
    public void testInter() throws InterruptedException, IOException {

        testTemplateMethod("JInternalFrame Form", "Swing GUI Forms", "NewJInternalFrame");

    }

    public void testAppl() throws InterruptedException, IOException, Exception {

        testTemplateMethod("Application Sample Form", "Swing GUI Forms", "NewApplication");
    }

    public void testMidi() throws InterruptedException, IOException, Exception {

        testTemplateMethod("MDI Application Sample Form", "Swing GUI Forms", "NewMDIApplication");
    }

    /** Test case 5.
     * Create new JPanel template in default package
     */
    public void testPanel() throws InterruptedException, IOException {

        testTemplateMethod("JPanel Form", "Swing GUI Forms", "NewJPanel");

    }

    /** Test case 6. oa
     * Create new Bean template in default package
     */
    public void testBean() throws InterruptedException, IOException {

        testTemplateMethod("Bean Form", "Swing GUI Forms", "NewBeanForm");

    }

    public void testOkCancel() throws InterruptedException, IOException {

        testTemplateMethod("OK / Cancel Dialog Sample Form", "Swing GUI Forms", "NewOkCancelDialog");

    }

    /**AWT Test case 1.
     * Create new Panel template in default package
     */
    public void testAWTPanel() throws InterruptedException, IOException {

        testTemplateMethod("Panel Form", "AWT GUI Forms", "NewPanel");

    }

    /**AWT Test case 2.
     * Create new Dialog template in default package
     */
    public void testAWTApplet() throws InterruptedException, IOException, Exception {

        testTemplateMethod("Applet Form", "AWT GUI Forms", "NewApplet");

    }

    /**AWT Test case 3.
     * Create new Dialog template in default package
     */
    public void testAWTDialog() throws InterruptedException, IOException {

        testTemplateMethod("Dialog Form", "AWT GUI Forms", "NewDialog");


    }

    /**AWT Test case 4.
     * Create new Frame template in default package
     */
    public void testAWTFrame() throws InterruptedException, IOException {

        testTemplateMethod("Frame Form", "AWT GUI Forms", "NewFrame");

    }

    public boolean testFormFile(String formfile) throws IOException {

        File ref=new File(getWorkDir(), formfile + "Form.ref");
        File goldenBig=getGoldenFile(formfile + "FormFileBig.pass");
        File goldenSmall=getGoldenFile(formfile + "FormFileSmall.pass");
        File diffBig=new File(getWorkDir(), formfile + "FormBig.diff");
        diffBig.createNewFile();
        File diffSmall=new File(getWorkDir(), formfile + "FormSmall.diff");
        diffSmall.createNewFile();
        
        Diff d = Manager.getSystemDiff();
        
        boolean bigger = d.diff(ref,goldenBig,diffBig);
        boolean smaller = d.diff(ref,goldenSmall,diffSmall);
        //assertFile(new File(getWorkDir() + File.separator + formfile + "Form.ref"), getGoldenFile(File.separatorChar+System.getProperty("os.name")+File.separatorChar+formfile + "FormFile.pass"), new File(getWorkDir(), formfile + ".diff"));

        if (!bigger || !smaller) {
            return true;
        } else {
            return false;
        }
    }

    public void createFormAndJavaRefFile(String filename) throws IOException {
        try {
            String refFileForm = VisualDevelopmentUtil.readFromFile(getDataDir().getAbsolutePath()
                    + File.separatorChar + DATA_PROJECT_NAME + File.separatorChar + "src" + File.separatorChar +  File.separatorChar+ filename + ".form");

            getLog(filename + "Form.ref").print(refFileForm);

            String refFileJava = VisualDevelopmentUtil.readFromFile(getDataDir().getAbsolutePath()
                    + File.separatorChar + DATA_PROJECT_NAME + File.separatorChar + "src" + File.separatorChar +  File.separatorChar+ filename + ".java");

            getLog(filename + "Java.ref").print(createRefFile(refFileJava));
            
            //getRef().print(createRefFile(refFileJava));


        } catch (Exception e) {
            fail("Fail during create reffile: " + e.getMessage());
        }

    }

    public boolean testJavaFile(String javafile) throws IOException {

        File ref=new File(getWorkDir() ,javafile + "Java.ref");
        File goldenBig=getGoldenFile(javafile + "JavaFileBig.pass");
        File goldenSmall=getGoldenFile(javafile + "JavaFileSmall.pass");
        File diffBig=new File(getWorkDir(), javafile + "JavaBig.diff");
        diffBig.createNewFile();
        File diffSmall=new File(getWorkDir(), javafile + "JavaSmall.diff");
        diffSmall.createNewFile();
        
        
        
        Diff d = Manager.getSystemDiff();
        
        boolean bigger = d.diff(ref,goldenBig,diffBig);
        boolean smaller = d.diff(ref,goldenSmall,diffSmall);

        //assertFile(ref,golden, diff);


        if (!bigger || !smaller) {
            return true;
        } else {
            return false;
        }

    }

    public String createRefFile(String test) {
        int start = test.indexOf("/*");
        int end = test.indexOf("*/");
        test = test.substring(0, start) + test.substring(end + 2);

        start = test.indexOf("/*");
        end = test.indexOf("*/");
        test = test.substring(0, start) + test.substring(end + 2);

        start = test.indexOf("/**");
        end = test.indexOf("*/");
        test = test.substring(0, start) + test.substring(end + 2);
        return test;
    }
}
