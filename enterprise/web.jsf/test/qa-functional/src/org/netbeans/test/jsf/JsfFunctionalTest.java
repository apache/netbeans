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
package org.netbeans.test.jsf;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.netbeans.spi.db.explorer.DatabaseRuntime;
import org.netbeans.test.web.NewWebProjectJSFFrameworkStepOperator;
import org.netbeans.test.web.WebProjectValidationEE5;

/**
 * Test JSF support in Java EE 7 project.
 *
 * @author Lukasz Grela
 * @author Jiri Skrivanek
 * @author Jindrich Sedek
 */
public class JsfFunctionalTest extends WebProjectValidationEE5 {

    @SuppressWarnings("hiding")
    public static final String[] TESTS = {
        "testNewJSFWebProject",
        "testManagedBeanWizard",
        "testManagedBeanDelete",
        "testCreateFacesConfig",
        /* actions are no longer available in projects with JSF 2
        "testAddManagedBean",
        "testAddNavigationRule",
        "testAddNavigationCase",
        "testAddNavigationCaseWithNewRule",
        */
        "testAddJSFToProject",
        "testJSFPalette",
        "testCreateEntityClassAndPU",
        "testCleanAndBuildProject",
        "testCompileAllJSP",
        "testRedeployProject",
        "testShutdownDb",
        "testFinish"
    };
    public static final String WELCOME_JSP = "welcomeJSF.jsp";
    public static final String INDEX_JSP = "index.jsp";
    public static final String FROM_ACTION1 = "FromAction1";
    public static final String FROM_ACTION2 = "FromAction2";
    public static final String FROM_OUTCOME1 = "FromOutcome1";
    public static final String FROM_OUTCOME2 = "FromOutcome2";
    public static final String DESCRIPTION_BEAN = "DescriptionBean";
    public static final String DESCRIPTION_RULE = "DescriptionRule";
    public static final String DESCRIPTION_CASE1 = "DescriptionCase1";
    public static final String DESCRIPTION_CASE2 = "DescriptionCase2";
    protected static String URL_PATTERN_NULL = "The URL Pattern has to be entered.";
    protected static String URL_PATTERN_INVALID = "The URL Pattern is not valid.";
    // folder of sample project

    /** Need to be defined because of JUnit */
    public JsfFunctionalTest(String name) {
        super(name);
        PROJECT_NAME = "WebJSFProjectEE7";
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, JsfFunctionalTest.class, TESTS);
    }

    @Override
    protected String getEEVersion() {
        return JAVA_EE_7;
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Java Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel set server to GlassFish and J2EE version to Java EE 5
     * - in Frameworks panel set JSF framework
     * - finish the wizard
     * - wait until scanning of java files is finished
     * - check index.xhtml is opened
     */
    public void testNewJSFWebProject() throws IOException {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.selectProject("Web Application");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep = new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectName().setText("");
        nameStep.txtProjectName().typeText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText("");
        nameStep.txtProjectLocation().typeText(PROJECT_LOCATION);
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectJavaEEVersion(getEEVersion());
        serverStep.next();

        NewWebProjectJSFFrameworkStepOperator frameworkStep = new NewWebProjectJSFFrameworkStepOperator();
        boolean exists = frameworkStep.setJSFFrameworkCheckbox();
        assertTrue("JSF framework not present!", exists);
        frameworkStep.txtServletURLMapping().setText("");
        assertEquals(URL_PATTERN_NULL, frameworkStep.getErrorMessage());
        frameworkStep.txtServletURLMapping().typeText("hhhhhh*", 0);
        assertEquals(URL_PATTERN_INVALID, frameworkStep.getErrorMessage());
        frameworkStep.txtServletURLMapping().setText("");
        frameworkStep.txtServletURLMapping().typeText("/faces/*", 0);
        frameworkStep.selectPageLibraries();
        frameworkStep.rbCreateNewLibrary().push();
        assertEquals("\"\" does not contain JavaServer Faces API classes.", frameworkStep.getErrorMessage());
        frameworkStep.rbRegisteredLibraries().push();
        frameworkStep.rbServerLibrary().push();
        frameworkStep.finish();
        verifyWebPagesNode("index.xhtml");
        waitScanFinished();
        EditorOperator.closeDiscardAll();
    }

    /** Test JSF Managed Bean Wizard. */
    public void testManagedBeanWizard() {
        // if scanning starts later we have to wait here
        waitScanFinished();
        NewFileWizardOperator newFileWizard = NewFileWizardOperator.invoke();
        // "Java Server Faces"
        String category = Bundle.getStringTrimmed(
                "org/netbeans/modules/web/jsf/resources/Bundle",
                "Templates/JSF");
        // "JSF Managed Bean"
        String filetype = Bundle.getStringTrimmed(
                "org/netbeans/modules/web/jsf/resources/Bundle",
                "Templates/JSF/JSFManagedBean.java");
        newFileWizard.selectCategory(category);
        newFileWizard.selectFileType(filetype);
        newFileWizard.next();
        NewJSFBeanStepOperator bean = new NewJSFBeanStepOperator();
        bean.setClassName("MyManagedBean");
        bean.selectScope("session");
        bean.cboPackage().getTextField().setText("mypackage");
        bean.finish();
        new EditorOperator("MyManagedBean.java").close();
    }

    /** Test that delete safely bean removes record from faces-config.xml. */
    public void testManagedBeanDelete() {
        // if scanning starts later we have to wait here
        waitScanFinished();
        Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "mypackage|MyManagedBean.java");
        new ActionNoBlock(null, "Refactor|Safely Delete...").perform(node);
        NbDialogOperator safeDeleteDialog = new NbDialogOperator("Safely Delete");
        new JButtonOperator(safeDeleteDialog, "Refactor").push();
        node.waitNotPresent();
    }

    /** Test creation of faces-config.xml. */
    public void testCreateFacesConfig() {
        NewFileWizardOperator newFileWizard = NewFileWizardOperator.invoke();
        newFileWizard.selectCategory("JavaServer Faces");
        newFileWizard.selectFileType("JSF Faces Configuration");
        newFileWizard.next();
        newFileWizard.finish();
        getFacesConfig().close();
    }

    /** Test adding JSF Managed Bean from faces-config.xml. */
    public void testAddManagedBean() {
        EditorOperator editor = getFacesConfig();
        // sometimes Insert menu item is not available so we need to wait a bit
        new EventTool().waitNoEvent(500);
        Action addBeanAction = new ActionNoBlock(null, "Insert|Managed Bean...");
        addBeanAction.perform(editor);
        AddManagedBeanOperator addBeanOper;
        try {
            addBeanOper = new AddManagedBeanOperator();
        } catch (TimeoutExpiredException tee) {
            // sometimes Insert menu item is not available so try it once more
            editor.close();
            editor = getFacesConfig();
            new EventTool().waitNoEvent(500);
            addBeanAction.perform(editor);
            addBeanOper = new AddManagedBeanOperator();
        }
        addBeanOper.setBeanName("SecondBean");
        addBeanOper.setBeanClass("mypackage.MyManagedBean");
        addBeanOper.selectScope("application");
        addBeanOper.setBeanDescription(DESCRIPTION_BEAN);
        addBeanOper.add();
        // verify
        EditorOperator facesEditor = getFacesConfig();
        String expected = "<managed-bean>";
        assertTrue("faces-config.xml should contain " + expected, facesEditor.contains(expected));
        expected = "<managed-bean-name>SecondBean</managed-bean-name>";
        assertTrue("faces-config.xml should contain " + expected, facesEditor.contains(expected));
        expected = "<managed-bean-class>mypackage.MyManagedBean</managed-bean-class>";
        assertTrue("faces-config.xml should contain " + expected, facesEditor.contains(expected));
        expected = "<managed-bean-scope>application</managed-bean-scope>";
        assertTrue("faces-config.xml should contain " + expected, facesEditor.contains(expected));
    }

    /** Test adding navigation rule from faces-config.xml. */
    public void testAddNavigationRule() throws IOException {
        EditorOperator editor = getFacesConfig();
        Action addRule = new ActionNoBlock(null, "Insert|Navigation Rule...");
        addRule.perform(editor);
        AddNavigationRuleDialogOperator rule = new AddNavigationRuleDialogOperator();
        rule.setRuleFromView("/" + WELCOME_JSP);
        rule.setRuleDescription(DESCRIPTION_RULE);
        rule.add();
        editor.waitModified(true);
        editor.save();
        // verify
        String expected = "<from-view-id>/welcomeJSF.jsp</from-view-id>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<navigation-rule>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "</navigation-rule>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = DESCRIPTION_RULE;
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
    }

    /** Test adding navigation case from faces-config.xml. */
    public void testAddNavigationCase() throws IOException {
        EditorOperator editor = getFacesConfig();
        Action addCase = new ActionNoBlock(null, "Insert|Navigation Case...");
        addCase.perform(editor);
        AddNavigationCaseDialogOperator caseOper = new AddNavigationCaseDialogOperator();
        caseOper.selectFromView("/" + WELCOME_JSP);
        caseOper.selectToView("/" + WELCOME_JSP);
        caseOper.setFromAction(FROM_ACTION1);
        caseOper.setFromOutcome(FROM_OUTCOME1);
        caseOper.setRuleDescription(DESCRIPTION_CASE1);
        caseOper.add();
        editor.waitModified(true);
        editor.save();
        // verify
        String expected = "<from-action>" + FROM_ACTION1 + "</from-action>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<from-outcome>" + FROM_OUTCOME1 + "</from-outcome>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<to-view-id>/" + WELCOME_JSP + "</to-view-id>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<navigation-case>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "</navigation-case>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = DESCRIPTION_CASE1;
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
    }

    /** Test adding navigation case with new rule from faces-config.xml. */
    public void testAddNavigationCaseWithNewRule() throws IOException {
        EditorOperator editor = getFacesConfig();
        Action addCase = new ActionNoBlock(null, "Insert|Navigation Case...");
        addCase.perform(editor);
        AddNavigationCaseDialogOperator caseOper = new AddNavigationCaseDialogOperator();
        caseOper.cboFromView().getTextField().setText("/" + INDEX_JSP);
        caseOper.setFromAction(FROM_ACTION2);
        caseOper.setFromOutcome(FROM_OUTCOME2);
        caseOper.setRuleDescription(DESCRIPTION_CASE2);
        caseOper.checkRedirect(true);
        caseOper.cboToView().getTextField().setText("/" + INDEX_JSP);
        caseOper.add();
        editor.waitModified(true);
        editor.save();
        // verify
        String expected = "<from-view-id>/" + INDEX_JSP + "</from-view-id>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<from-action>" + FROM_ACTION2 + "</from-action>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<from-outcome>" + FROM_OUTCOME2 + "</from-outcome>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<to-view-id>/" + INDEX_JSP + "</to-view-id>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = "<redirect/>";
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
        expected = DESCRIPTION_CASE2;
        assertTrue("faces-config.xml should contain " + expected, editor.contains(expected));
    }

    /** Test adding JSF framework to existing web application. */
    public void testAddJSFToProject() throws IOException {
        // "Java Web"
        String web = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "OpenIDE-Module-Display-Category");
        // "Web Application"
        String webApplication = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        NewProjectWizardOperator nop = NewProjectWizardOperator.invoke();
        nop.selectCategory(web);
        nop.selectProject(webApplication);
        nop.next();
        NewWebProjectNameLocationStepOperator lop = new NewWebProjectNameLocationStepOperator();
        lop.setProjectName(PROJECT_NAME + "2");
        lop.setProjectLocation(getDataDir().getCanonicalPath());
        lop.next();

        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectJavaEEVersion(getEEVersion());
        serverStep.finish();

        // add JSF framework using project properties
        // open project properties
        ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME + "2").properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Frameworks" category
        new Node(new JTreeOperator(propertiesDialogOper), "Frameworks").select();
        new JButtonOperator(propertiesDialogOper, "Add").pushNoBlock();
        NbDialogOperator addFrameworkOper = new NbDialogOperator("Add a Framework");
        // select "JavaServer Faces" but item is instance of org.netbeans.modules.web.jsf.JSFFrameworkProvider which we need to select
        new JListOperator(addFrameworkOper).selectItem("org.netbeans.modules.web.jsf.JSFFrameworkProvider");

        addFrameworkOper.ok();
        // confirm properties dialog
        propertiesDialogOper.ok();

        // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME + "2");
        if (J2EE_4.equals(getEEVersion())) {
            assertNotNull(new Node(webPages, "WEB-INF|faces-config.xml"));
        }
        webPages.setComparator(new DefaultStringComparator(true, true));
        Node webXML = new Node(webPages, "WEB-INF|web.xml");
        new EditAction().performAPI(webXML);
        final EditorOperator webXMLEditor = new EditorOperator("web.xml");
        webXMLEditor.waitState(new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                return webXMLEditor.contains("/faces/*");
            }

            @Override
            public String getDescription() {
                return "web.xml contains /faces/*";
            }
        });
        webXMLEditor.close();
        new CloseAction().perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME + "2"));
    }

    /** Test JSF Palette. */
    public void testJSFPalette() {
        new OpenAction().performAPI(new Node(new WebPagesNode(PROJECT_NAME), "index.xhtml"));
        EditorOperator editorOper = new EditorOperator("index.xhtml");
        editorOper.select("Hello from Facelets");
        PaletteOperator paletteOper = PaletteOperator.invoke();
        // collapse HTML category
        JCheckBoxOperator htmlCategoryOper = new JCheckBoxOperator(paletteOper, "HTML");
        if (htmlCategoryOper.isSelected()) {
            htmlCategoryOper.push();
        }
        // expand JSF category
        JCheckBoxOperator jsfCategoryOper = new JCheckBoxOperator(paletteOper, "JSF");
        if (!jsfCategoryOper.isSelected()) {
            jsfCategoryOper.push();
        }
        editorOper.makeComponentVisible();
        paletteOper.selectComponent("JSF Form");
        paletteOper.pushKey(KeyEvent.VK_ENTER);
        String expected = "<f:view>";
        assertTrue("index.jsp should contain " + expected + ".", editorOper.contains(expected));
        expected = "<h:form>";
        assertTrue("index.jsp should contain " + expected + ".", editorOper.contains(expected));
        expected = "</h:form>";
        assertTrue("index.jsp should contain " + expected + ".", editorOper.contains(expected));
        expected = "</f:view>";
        assertTrue("index.jsp should contain " + expected + ".", editorOper.contains(expected));

        editorOper.makeComponentVisible();
        paletteOper.selectComponent("JSF Data Table");
        paletteOper.pushKey(KeyEvent.VK_ENTER);
        expected = "<h:dataTable value=\"#{}\" var=\"item\">";
        assertTrue("index.jsp should contain " + expected + ".", editorOper.contains(expected));
        expected = "</h:dataTable>";
        assertTrue("index.jsp should contain " + expected + ".", editorOper.contains(expected));
        EditorOperator.closeDiscardAll();
    }

    /** Create Entity class and persistence unit. */
    public void testCreateEntityClassAndPU() {
        NewFileWizardOperator entity = NewFileWizardOperator.invoke();
        entity.selectProject(PROJECT_NAME);
        entity.selectCategory("Persistence");
        entity.selectFileType("Entity Class");
        entity.next();
        NewJavaFileNameLocationStepOperator locationOper = new NewJavaFileNameLocationStepOperator();
        locationOper.setPackage("mypackage");
        locationOper.next();
        locationOper.finish();
        new EditorOperator("NewEntity.java").close();
        Node persistenceNode = new Node(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME), "Configuration Files|persistence.xml");
    }

    /** Shutdown databases */
    public void testShutdownDb() {
        DatabaseRuntime[] runtimes = DatabaseRuntimeManager.getDefault().getRuntimes();
        for (DatabaseRuntime runtime : runtimes) {
            if (runtime.isRunning()) {
                runtime.stop();
            }
        }
    }

    /** Opens faces-config.xml and returns EditorOperator. 
     * @return EditorOperator instance of faces-config.xml
     */
    public EditorOperator getFacesConfig() {
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node facesconfig = new Node(webPages, "WEB-INF|faces-config.xml");
        new OpenAction().performAPI(facesconfig);
        return new EditorOperator("faces-config.xml");
    }
}
