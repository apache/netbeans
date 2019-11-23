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
package org.netbeans.test.web;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JTextField;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.NewWebProjectSourcesStepOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.MockServices;

/**
 * Test web project Java EE 7. It is a base class for other sub classes.
 */
public class WebProjectValidation extends J2eeTestCase {

    public static String[] TESTS = {
        "testNewWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testNewHTML", "testCreateTLD", "testCreateTagHandler", "testNewSegment",
        "testNewDocument", "testJSPNavigator", "testHTMLNavigator",
        "testCompileAllJSP", "testCompileJSP", "testCleanAndBuildProject",
        "testRedeployProject", "testRunProject", "testRunJSP",
        "testViewServlet", "testRunServlet", "testRunHTML", "testRunTag",
        "testFinish"
    };
    public static final String J2EE_4 = "J2EE 1.4";
    public static final String JAVA_EE_5 = "Java EE 5";
    public static final String JAVA_EE_6 = "Java EE 6";
    public static final String JAVA_EE_7 = "Java EE 7";
    public static final String JAVA_EE_8 = "Java EE 8";
    // location of sample project
    protected static String PROJECT_LOCATION;
    // name of sample project
    protected String PROJECT_NAME = "SampleProject"; // NOI18N
    protected TestURLDisplayer urlDisplayer;
    private static final String BUILD_SUCCESSFUL = "BUILD SUCCESSFUL";
    protected static ServerInstance server;
    protected static int logIdx = 0;
    private Server currentServer = Server.GLASSFISH;

    /** Need to be defined because of JUnit */
    public WebProjectValidation(String name) {
        super(name);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, WebProjectValidation.class, TESTS);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("########  " + this.getClass().getSimpleName() + "." + getName() + "  #######");
        if (server == null) {
            server = new ServerInstance();
        }
        if (PROJECT_LOCATION == null) {
            clearWorkDir();
            PROJECT_LOCATION = getWorkDirPath();
            System.out.println("PROJECT_LOCATION=" + PROJECT_LOCATION);
        }
    }

    @Override
    public void tearDown() {
        logAndCloseOutputs();
    }

    protected String getEEVersion() {
        return JAVA_EE_7;
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.jsp is opened
     */
    public void testNewWebProject() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.selectProject("Web Application");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep =
                new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectLocation().setText(PROJECT_LOCATION);
        nameStep.txtProjectName().setText(PROJECT_NAME);
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.cboServer().selectItem(0);
        serverStep.selectJavaEEVersion(getEEVersion());
        serverStep.next();
        NewWebProjectSourcesStepOperator frameworkStep = new NewWebProjectSourcesStepOperator();
        frameworkStep.finish();
        frameworkStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        frameworkStep.waitClosed();
        // wait project appear in projects view
        // wait 30 second
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); // NOI18N
        if (JAVA_EE_7.equals(getEEVersion())) {
            verifyWebPagesNode("index.html");
        } else {
            verifyWebPagesNode("index.jsp");
        }
        if (J2EE_4.equals(getEEVersion())) {
            verifyWebPagesNode("WEB-INF|web.xml");
        }
        waitScanFinished();
        // not display browser on run
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).properties();
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title"));
        new Node(new JTreeOperator(propertiesDialogOper),
                Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run")).select();
        new JCheckBoxOperator(propertiesDialogOper,
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle",
                        "LBL_CustomizeRun_DisplayBrowser_JCheckBox")).setSelected(false);
        propertiesDialogOper.ok();
    }

    protected void verifyProjectNode(String nodePath) {
        Node prjNode = ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME);
        Node node = new Node(prjNode, nodePath);//NOI18N
        assertTrue(node.isPresent());
        node.select();
    }

    protected void verifySourcePackageNode(String nodePath) {
        SourcePackagesNode sourceNode = new SourcePackagesNode(PROJECT_NAME);
        Node node = new Node(sourceNode, nodePath);
        assertTrue(node.isPresent());
        node.select();
    }

    protected void verifyWebPagesNode(String nodePath) {
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node node = new Node(webPages, nodePath);//NOI18N
        assertTrue(node.isPresent());
        node.select();
    }

    /** Test new JSP wizard.
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Web|JSP file type
     * - in the next panel type name
     * - finish the wizard
     * - check file is open in editor and close all opened documents
     */
    public void testNewJSP() throws IOException {
        NewJspFileNameStepOperator nameStep = NewJspFileNameStepOperator.invoke();
        nameStep.setJSPFileName("page1");
        nameStep.finish();
        // check class is opened in Editor and then close it
        EditorOperator jsp1Editor = new EditorOperator("page1.jsp");
        jsp1Editor.close();
    }

    /** Test new JSP wizard.
     * - open New File wizard from context menu on Web Pages (New|File)
     * - select JSP file type
     * - in the next panel type name in
     * - finish the wizard
     * - check file is open in editor and close all opened documents
     */
    public void testNewJSP2() throws IOException {
        Node webPages = new WebPagesNode(PROJECT_NAME);
        // create new .jsp
        new ActionNoBlock(null, "New|JSP").perform(webPages);
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName("page2");
        nameStep.finish();
        // check class is opened in Editor and then close the  document
        new EditorOperator("page2.jsp").close();
    }

    /** Test new servlet wizard.
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Web|Servlet file type
     * - in the next panel type name
     * - finish the wizard
     * - check file is open in editor and close it
     */
    public void testNewServlet() throws IOException {
        // create a new package
        new ActionNoBlock("File|New File", null).perform();
        NewFileWizardOperator newFileWizard = new NewFileWizardOperator("New File");
        newFileWizard.selectProject(PROJECT_NAME);
        newFileWizard.selectCategory("Web");
        newFileWizard.selectFileType("Servlet");
        newFileWizard.next();
        JTextFieldOperator txtPackageName = new JTextFieldOperator(newFileWizard);
        // clear text field
        txtPackageName.setText("");
        txtPackageName.typeText("Servlet1");
        JComboBoxOperator txtPackage = new JComboBoxOperator(newFileWizard, 1);
        // clear text field
        txtPackage.clearText();
        txtPackage.typeText("test1");
        newFileWizard.next();
        new EventTool().waitNoEvent(300);
        if (JCheckBoxOperator.findJCheckBox((Container) newFileWizard.getSource(), "", false, false) != null) {
            // Add information to deployment descriptor (web.xml)
            new JCheckBoxOperator(newFileWizard).setSelected(true);
        }
        newFileWizard.finish();
        // check class is opened in Editor and close it
        new EditorOperator("Servlet1.java").close();
        // check the servlet is specified in web.xml
        if (J2EE_4.equals(getEEVersion()) && !PROJECT_NAME.equals("WebModuleNB36")) {
            WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
            webPages.setComparator(new Operator.DefaultStringComparator(true, true));
            Node webXml = new Node(webPages, "WEB-INF|web.xml");
            new EditAction().performPopup(webXml);
            String xmlText = new EditorOperator("web.xml").getText();
            new EditorOperator("web.xml").closeAllDocuments();
            String[] content = new String[]{
                "<servlet-name>Servlet1</servlet-name>",
                "<servlet-class>test1.Servlet1</servlet-class>",
                "<url-pattern>/Servlet1</url-pattern>"
            };
            for (int i = 0; i < content.length; i++) {
                assertTrue("Servlet is not correctly specifeid in web.xml."
                        + " Following line is missing in the web.xml:\n" + content[i],
                        xmlText.indexOf(content[i]) != -1);
            }
        }
    }

    /** Test new Servlet wizard.
     * - open New File wizard from main menu (File|New File)
     * - select sample project as target
     * - select Java|Package file type
     * - in the next panel type package name in
     * - finish the wizard
     * - open New File wizard from context menu on created package node (New|File)
     * - select Web|Servlet file type
     * - in the next panel type class name in
     * - finish the wizard
     * - check class is open in editor and close it
     */
    public void testNewServlet2() throws IOException {
        createJavaPackage(PROJECT_NAME, "test2");
        Node sample1Node = new Node(new SourcePackagesNode(PROJECT_NAME), "test2");
        // create a new class
        new ActionNoBlock(null, "New|Servlet").perform(sample1Node);
        WizardOperator newFileWizard = new WizardOperator("New Servlet");
        new JTextFieldOperator(newFileWizard).typeText("Servlet2");
        newFileWizard.next();
        new JTextFieldOperator(newFileWizard, 1).setText("");
        new JTextFieldOperator(newFileWizard, 1).typeText("Servlet2Name");
        new JTextFieldOperator(newFileWizard, 2).setText("");
        new JTextFieldOperator(newFileWizard, 2).typeText("/Servlet2URL");
        newFileWizard.finish();
        // check class is opened in Editor and then close it
        new EditorOperator("Servlet2.java").close();
    }

    // TODO workaround for defective:
    // NewFileWizardOperator.create(PROJECT_NAME, "Java", "Java Package", null, "test2");
    // Should be fixed in jemmy/jelly
    private void createJavaPackage(String projectName, String pkgName) {
        String wizardTitle = Bundle.getString("org.netbeans.modules.project.ui.Bundle",
                "LBL_NewFileWizard_Title");
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke(wizardTitle);
        nfwo.selectProject(projectName);
        nfwo.setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.selectCategory("Java");
        nfwo.selectFileType("Java Package");
        nfwo.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName(pkgName);
        nfnlso.finish();
    }

    public void testCleanAndBuildProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new Action(null, "Clean and Build").perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
    }

    public void testBuildProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new BuildJavaProjectAction().perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
    }

    public void testCompileAllJSP() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new PropertiesAction().perform(rootNode);
        NbDialogOperator properties = new NbDialogOperator("Project Properties");
        new Node(new JTreeOperator(properties), "Build|Compiling").select();
        String compileJSPLabel = "Test compile all JSP files during builds";
        new JCheckBoxOperator(properties, compileJSPLabel).changeSelection(true);
        properties.ok();
        try {
            testCleanAndBuildProject();
            logAndCloseOutputs();
            testCleanAndBuildProject();
            logAndCloseOutputs();
        } finally {
            new Action(null, "Properties").perform(rootNode);
            properties = new NbDialogOperator("Project Properties");
            new Node(new JTreeOperator(properties), "Build|Compiling").select();
            new JCheckBoxOperator(properties, compileJSPLabel).changeSelection(false);
            properties.ok();
        }
    }

    public void testCompileJSP() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Node jspNode = new Node(rootNode, "Web Pages|page2.jsp");
        Util.cleanStatusBar();
        new Action(null, "Compile File").perform(jspNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
    }

    public void testCleanProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Action clean = new Action(null, "Clean and Build");
        // can clash with 'Clean and Build' action
        clean.setComparator(new Operator.DefaultStringComparator(true, true));
        Util.cleanStatusBar();
        clean.perform(rootNode);
        MainWindowOperator.getDefault().waitStatusText("Finished building");
    }

    public void testRedeployProject() throws IOException {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new RedeployProjectAction().perform(rootNode);
        waitBuildSuccessful();
        logAndCloseOutputs();
    }

    public void testRunProject() throws Exception {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        EditorOperator editor;
        if (JAVA_EE_7.equals(getEEVersion())) {
            new Node(rootNode, "Web Pages|index.html").performPopupAction("Open");
            editor = new EditorOperator("index.html");
            editor.replace("<title>",
                    "<title>SampleProject Index Page");
            editor.replace("<body>", "<body>Running Project\n");
        } else {
            new Node(rootNode, "Web Pages|index.jsp").performPopupAction("Open");
            editor = new EditorOperator("index.jsp");
            editor.replace("<title>JSP Page</title>",
                    "<title>SampleProject Index Page</title>");
            editor.replace("<body>", "<body>Running Project\n");
        }
        new Action(null, "Run").perform(rootNode);
        waitBuildSuccessful();
        waitText(PROJECT_NAME, 24000, "<title>SampleProject Index Page");
        editor.replace("Running Project", "");
        editor.save();
        EditorOperator.closeDiscardAll();
    }

    public void testRunJSP() {
        String filename = "pageRunJSP";
        // create new .jsp
        new ActionNoBlock(null, "New|JSP").perform(new WebPagesNode(PROJECT_NAME));
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName(filename);
        nameStep.finish();
        EditorOperator editor = new EditorOperator(filename);
        editor.replace("<title>JSP Page</title>", "<title>" + filename + "</title>");
        editor.save();
        new Action("Run|Run File", null).perform();
        waitBuildSuccessful();
        waitText(PROJECT_NAME + "/" + filename + ".jsp", 24000, "<title>" + filename + "</title>");
        editor.close();
    }

    public void testViewServlet() throws IOException {
        // enable open browser on run
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).properties();
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title"));
        new Node(new JTreeOperator(propertiesDialogOper),
                Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run")).select();
        new JCheckBoxOperator(propertiesDialogOper,
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle",
                        "LBL_CustomizeRun_DisplayBrowser_JCheckBox")).setSelected(true);
        propertiesDialogOper.ok();
        String jspCode = "new String().toString();";
        String runningViewServlet = "Running View Servlet";
        String filename = "pageViewServlet";
        // create new .jsp
        new ActionNoBlock(null, "New|JSP").perform(new WebPagesNode(PROJECT_NAME));
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName(filename);
        nameStep.finish();
        EditorOperator editor = new EditorOperator(filename);
        editor.insert("<h1>" + filename + "</h1>\n", 16, 1);
        editor.insert(runningViewServlet + "\n", 17, 1);
        editor.insert("<% " + jspCode + " %>\n", 18, 1);
        editor.save();
        new Action("Run|Run File", null).perform();
        waitBuildSuccessful();
        waitText(PROJECT_NAME + "/" + filename + ".jsp", 24000, "<h1>" + filename + "</h1>");
        new Node(new WebPagesNode(PROJECT_NAME), filename).performPopupAction("View Servlet");
        EditorOperator servlet = new EditorOperator(filename + "_jsp.java");
        String text = "file: " + filename + "_jsp.java\n" + servlet.getText();
        for (String str : new String[]{"<h1>", runningViewServlet, "JspWriter out", jspCode}) {
            assertContains(text, str);
        }
        editor.close();
        servlet.close();
        // disable open browser on run
        new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME).properties();
        propertiesDialogOper = new NbDialogOperator(
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title"));
        new Node(new JTreeOperator(propertiesDialogOper),
                Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run")).select();
        new JCheckBoxOperator(propertiesDialogOper,
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle",
                        "LBL_CustomizeRun_DisplayBrowser_JCheckBox")).setSelected(false);
        propertiesDialogOper.ok();
    }

    public void testRunServlet() {
        Node sourceNode = new SourcePackagesNode(PROJECT_NAME);
        new Node(sourceNode, "test1|Servlet1.java").performPopupAction("Open");
        EditorOperator editor = new EditorOperator("Servlet1.java");
        String expected = "response.setContentType(\"text/html;charset=UTF-8\");";
        assertTrue("Servlet1.java should contain " + expected, editor.contains(expected));
        editor.replace("out.println(\"<title>Servlet Servlet1</title>\");",
                "out.println(\"<title>Servlet with name=\"+request.getParameter(\"name\")+\"</title>\");");
        new ActionNoBlock(null, "Run File").perform(editor);
        NbDialogOperator dialog = new NbDialogOperator("Set Servlet Execution URI");
        JComboBoxOperator combo = new JComboBoxOperator(dialog);
        combo.setSelectedItem(combo.getSelectedItem() + "?name=Servlet1");
        dialog.ok();
        waitBuildSuccessful();
        editor.close();
        waitText(PROJECT_NAME + "/" + "Servlet1?name=Servlet1", 24000,
                "<title>Servlet with name=Servlet1</title>");
    }

    public void testCreateTLD() {
        NewFileWizardOperator newFileWizard = NewFileWizardOperator.invoke();
        // prevent NPE when initializing tooltip (path selection)
        new EventTool().waitNoEvent(1000);
        newFileWizard.selectProject(PROJECT_NAME);
        newFileWizard.selectCategory("Web");
        newFileWizard.selectFileType("Tag Library Descriptor");
        newFileWizard.next();
        NewJavaFileNameLocationStepOperator nameAndLocationOper = new NewJavaFileNameLocationStepOperator();
        nameAndLocationOper.setObjectName("MyTags");
        nameAndLocationOper.cboLocation().selectItem("Web Pages");
        JLabelOperator jle = new JLabelOperator(nameAndLocationOper, "Folder");
        JTextFieldOperator folder = new JTextFieldOperator((JTextField) jle.getLabelFor());
        folder.setText("WEB-INF/tlds");
        nameAndLocationOper.finish();
        Node node = new Node(new WebPagesNode(PROJECT_NAME), "WEB-INF|tlds|MyTags.tld");
        // check class is opened in Editor and then close it
        new EditorOperator("MyTags.tld").close();
    }

    public void testCreateTagHandler() throws InterruptedException {
        NewFileWizardOperator newFileWizard = NewFileWizardOperator.invoke();
        // prevent NPE when initializing tooltip (path selection)
        new EventTool().waitNoEvent(1000);
        newFileWizard.selectProject(PROJECT_NAME);
        newFileWizard.selectCategory("Web");
        newFileWizard.selectFileType("Tag Handler");
        newFileWizard.next();
        NewJavaFileNameLocationStepOperator nameAndLocationOper = new NewJavaFileNameLocationStepOperator();
        nameAndLocationOper.setObjectName("MyTag");
        nameAndLocationOper.setPackage("tags");
        nameAndLocationOper.next();
        new JButtonOperator(newFileWizard, "Browse").push();
        NbDialogOperator dialog = new NbDialogOperator("Browse Files");
        new Node(new JTreeOperator(dialog), "Web Pages|WEB-INF|tlds|MyTags.tld").select();
        new JButtonOperator(dialog, "Select File").push();
        newFileWizard.finish();
        // check class is opened in Editor and then close it
        EditorOperator editor = new EditorOperator("MyTag.java");
        editor.replace("// out.println(\"    </blockquote>\");", getTagHandlerCode());
        editor.save();
        editor.close();
    }

    protected String getTagHandlerCode() {
        return "out.print(\"TagOutput\"); \n";
    }

    public void testRunTag() throws Throwable {
        if (J2EE_4.equals(getEEVersion())) {
            // servlet is not compilable with 1.4 source target
            return;
        }
        String filename = "pageRunTag";
        // create new .jsp
        new ActionNoBlock(null, "New|JSP").perform(new WebPagesNode(PROJECT_NAME));
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName(filename);
        nameStep.finish();
        EditorOperator editor = new EditorOperator(filename);
        editor.replace("<title>JSP Page</title>",
                "<title><my:MyTag/></title>");
        editor.insert("<%@taglib prefix=\"my\" uri=\"/WEB-INF/tlds/MyTags\"%>\n", 6, 1);
        editor.save();
        new Action(null, "Run File").perform(new Node(new WebPagesNode(PROJECT_NAME), filename));
        waitBuildSuccessful();
        editor.close();
        waitText(PROJECT_NAME + "/" + filename + ".jsp", 24000, "<title>TagOutput</title>");
    }

    public void testNewHTML() throws IOException {
        WebPagesNode webPagesNode = new WebPagesNode(PROJECT_NAME);
        webPagesNode.newFile("HTML");
        NewJavaFileNameLocationStepOperator wizard = new NewJavaFileNameLocationStepOperator("New HTML");
        wizard.setObjectName("HTML");
        wizard.finish();
        // check class is opened in Editor and then close it
        new EditorOperator("HTML.html").close();
        Node node = new Node(webPagesNode, "HTML.html");
    }

    public void testHTMLNavigator() throws Exception {
        Node webPages = new WebPagesNode(PROJECT_NAME);
        new Node(webPages, "HTML.html").performPopupAction("Open");
        verifyNavigator(new EditorOperator("HTML.html"));
        EditorOperator.closeDiscardAll();
    }

    public void testJSPNavigator() throws Exception {
        Node webPages = new WebPagesNode(PROJECT_NAME);
        // create new .jsp
        new ActionNoBlock(null, "New|JSP").perform(webPages);
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName("page3");
        nameStep.finish();
        verifyNavigator(new EditorOperator("page3.jsp"));
        EditorOperator.closeDiscardAll();
    }

    private void dumpNode(TreeModel model, Object node, int offset) {
        for (int i = 0; i < offset; i++) {
            getRef().print("\t");
        }
        getRef().println(node.toString());
        if (model.isLeaf(node)) {
            return;
        }
        for (int i = 0; i < model.getChildCount(node); i++) {
            Object object = model.getChild(node, i);
            dumpNode(model, object, offset + 1);
        }
    }

    private void verifyNavigator(EditorOperator eOperator) throws Exception {
        eOperator.replace("</body>", "<table border=\"1\">\n <tr> \n<td></td> \n  <td></td>\n</tr> \n"
                + "<tr>\n<td></td>\n<td></td>\n</tr>\n</table>\n</body>");
        eOperator.save();
        //refresh navigator
        Node webPages = new WebPagesNode(PROJECT_NAME);
        new Node(webPages, "index").performPopupAction("Open");
        new EditorOperator("index").closeDiscard();
        //wait for editor update
        new EventTool().waitNoEvent(300);
        int startCaretPos = eOperator.txtEditorPane().getCaretPosition();
        final NavigatorOperator navigatorOperator = NavigatorOperator.invokeNavigator();
        assertNotNull(navigatorOperator);
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object obj) {
                JTreeOperator treeOper = navigatorOperator.getTree();
                Object root = treeOper.getModel().getRoot();
                if (root != null) {
                    if (root.toString() != null && root.toString().contains("Wait")) {
                        // still please wait node
                        return null;
                    } else if (treeOper.getChildCount(root) > 0) {
                        return Boolean.TRUE;
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return "root node in navigator tree has one child";
            }
        }).waitAction(null);
        JTreeOperator treeOperator = navigatorOperator.getTree();
        Object root = treeOperator.getModel().getRoot();
        dumpNode(treeOperator.getModel(), root, 0);
        assertEquals(1, treeOperator.getChildCount(root));
        Object htmlChild = treeOperator.getChild(root, 0);//HTML
        assertNotNull(htmlChild);
        assertEquals(2, treeOperator.getChildCount(htmlChild));// HEAD, BODY
        Object bodyChild = treeOperator.getChild(htmlChild, 1);
        assertEquals(2, treeOperator.getChildCount(bodyChild));// H1, TABLE
        Object tableChild = treeOperator.getChild(bodyChild, 1);
        assertEquals(2, treeOperator.getChildCount(tableChild));// 2 rows
        Object[] pathObjects = {root, htmlChild, bodyChild, tableChild};
        TreePath path = new TreePath(pathObjects);
        treeOperator.clickOnPath(path, 2);
        // wait for editor update
        new EventTool().waitNoEvent(300);
        assertFalse("No caret move in document.", eOperator.txtEditorPane().getCaretPosition() == startCaretPos);
        assertTrue("Caret not moved to opening table tag.", eOperator.getText(eOperator.getLineNumber()).contains("table border="));
    }

    public void testRunHTML() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        new Node(rootNode, "Web Pages|HTML.html").performPopupAction("Open");
        new EditorOperator("HTML.html").replace("<title>TODO supply a title</title>",
                "<title>HTML Page</title>");
        new Action("Run|Run File", null).perform();
        //waitBuildSuccessful();
        new EditorOperator("HTML.html").close();
        waitText(PROJECT_NAME + "/HTML.html" , 24000, "<title>HTML Page</title>");
    }

    public void testNewSegment() throws IOException {
        WebPagesNode webPagesNode = new WebPagesNode(PROJECT_NAME);
        webPagesNode.newFile("JSP");
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName("segment");
        new JCheckBoxOperator(nameStep).changeSelection(true);
        nameStep.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator("segment.jspf").close();
        Node node = new Node(webPagesNode, "segment.jspf");
    }

    public void testNewDocument() throws IOException {
        WebPagesNode webPagesNode = new WebPagesNode(PROJECT_NAME);
        webPagesNode.newFile("JSP");
        NewJspFileNameStepOperator nameStep = new NewJspFileNameStepOperator();
        nameStep.setJSPFileName("document");
        new JRadioButtonOperator(nameStep, 1).changeSelection(true);
        nameStep.finish();
        // check class is opened in Editor and then close all documents
        new EditorOperator("document.jspx").close();
        Node node = new Node(webPagesNode, "document.jspx");
    }

    public void testFinish() throws Exception {
        new ActionNoBlock(null, "Close").perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME));
    }

    //********************************************************

    public void logAndCloseOutputs() {
        OutputTabOperator outputTab;
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 300);
        try {
            do {
                try {
                    outputTab = new OutputTabOperator("");
                } catch (TimeoutExpiredException e) {
                    // probably no more tabs so ignore it and continue
                    break;
                }
                String logName = "Output" + logIdx++ + ".log";
                log(logName, outputTab.getName() + "\n-------------\n\n" + outputTab.getText());
                outputTab.close();
            } while (true);
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
        }
    }

    public void waitBuildSuccessfulInActualTab() {
        final OutputOperator oo = new OutputOperator();
        oo.waitState(new ComponentChooser() {

            @Override
            public boolean checkComponent(Component comp) {
                return oo.getText().contains(BUILD_SUCCESSFUL);
            }

            @Override
            public String getDescription() {
                return ("\"" + BUILD_SUCCESSFUL + "\" text");
            }
        });
    }

    public void waitBuildSuccessful() {
        new Action(Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window")
                + "|" + Bundle.getStringTrimmed("org.netbeans.core.output2.Bundle", "OutputWindow"),
                null).performMenu();
        OutputTabOperator console = new OutputTabOperator(PROJECT_NAME);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        boolean ok = false;
        try {
            console.waitText(BUILD_SUCCESSFUL);
            ok = true;
        } finally {
            if (!ok) {
                System.err.println(console.getText());
            }
        }
    }

    public void initDisplayer() {
        MockServices.setServices(TestURLDisplayer.class);
        if (urlDisplayer == null) {
            urlDisplayer = TestURLDisplayer.getInstance();
        }
        urlDisplayer.invalidateURL();
    }

    public void assertDisplayerContent(String substr) {
        try {
            urlDisplayer.waitURL();
        } catch (InterruptedException ex) {
            throw new JemmyException("Waiting interrupted.", ex);
        }
        String page = urlDisplayer.readURL();
        boolean contains = page.indexOf(substr) > -1;
        if (!contains) {
            log("DUMP OF: " + urlDisplayer.getURL() + "\n");
            log(page);
        }
        assertTrue("The '" + urlDisplayer.getURL() + "' page does not contain '" + substr + "'", contains);
    }

    public void assertContains(String text, String value) {
        assertTrue("Assertation failed, cannot find:\n" + value + "\nin the following text:\n" + text, text.contains(value));
    }
    
    /**
     * Opens URL connection and waits for given text. It throws
     * TimeoutExpiredException if timeout expires.
     *
     * @param urlSuffix suffix added to server URL
     * @param timeout time to wait
     * @param text text to be found
     */
    public static void waitText(final String urlSuffix, final long timeout, final String text) {
        Waitable waitable = new Waitable() {
            @Override
            public Object actionProduced(Object obj) {
                InputStream is = null;
                try {
                    URLConnection connection = new URI("http://localhost:8080/" + urlSuffix).toURL().openConnection();
                    connection.setReadTimeout(Long.valueOf(timeout).intValue());
                    is = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = br.readLine();
                    while (line != null) {
                        if (line.indexOf(text) > -1) {
                            return Boolean.TRUE;
                        }
                        line = br.readLine();
                    }
                    is.close();
                } catch (Exception e) {
                    //e.printStackTrace();
                    return null;
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return ("Text \"" + text + "\" at http://localhost:8080/" + urlSuffix);
            }
        };
        Waiter waiter = new Waiter(waitable);
        waiter.getTimeouts().setTimeout("Waiter.WaitingTime", timeout);
        try {
            waiter.waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Exception while waiting for connection.", e);
        }
    }

    protected class ServerInstance {

        private String host;
        private int serverPort;
        private URL serverURL;

        private ServerInstance() {
            host = "localhost";
            if (currentServer == Server.TOMCAT) {
                serverPort = 8084;
            } else if (currentServer == Server.GLASSFISH) {
                serverPort = 8080;
            }
        }

        public URL getServerURL() {
            if (serverURL == null) {
                try {
                    serverURL = new URL("http", host, serverPort, "");
                } catch (MalformedURLException mue) {
                    throw new JemmyException("Cannot create server URL.", mue);
                }
            }
            System.out.println("serverurl:" + serverURL);
            return serverURL;
        }

        public J2eeServerNode getServerNode() {
            return WebProjectValidation.this.getServerNode(currentServer);
        }

        public void stop() {
            getServerNode().stop();
        }

        public void start() {
            getServerNode().start();
        }
    }
}
