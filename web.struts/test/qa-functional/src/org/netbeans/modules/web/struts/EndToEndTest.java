/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 */
package org.netbeans.modules.web.struts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.openide.util.Exceptions;

/**
 * End-to-end scenario test based on
 * http://wiki.netbeans.org/TS_71_StrutsSupport
 *
 * @author Jiri Skrivanek
 */
public class EndToEndTest extends J2eeTestCase {

    public static final String PROJECT_NAME = "StrutsWebApplication";

    /**
     * Constructor required by JUnit
     */
    public EndToEndTest(String name) {
        super(name);
    }

    /**
     * Creates suite from particular test cases. You can define order of
     * testcases here.
     */
    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, EndToEndTest.class,
                "testSetupStrutsProject", "testCreateLoginPage", "testCreateLoginBean",
                "testCreateLoginAction", "testCreateSecurityManager", "testCreateForward", "testCreateShopPage",
                "testCreateLogoutPage", "testCreateForwardInclude", "testCreateAction", "testCreateException",
                "testCreateActionFormBean", "testCreateActionFormBeanProperty", "testRunApplication");
    }

    /**
     * Called before every test case.
     */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    /**
     * Called after every test case.
     */
    @Override
    public void tearDown() {
    }

    /**
     * Create web application with struts support and check correctness.
     */
    public void testSetupStrutsProject() throws IOException {
        // "Web"
        String web = org.netbeans.jellytools.Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "OpenIDE-Module-Display-Category");
        // "Web Application"
        String webApplication = org.netbeans.jellytools.Bundle.getStringTrimmed(
                "org.netbeans.modules.web.project.ui.wizards.Bundle",
                "Templates/Project/Web/emptyWeb.xml");
        NewProjectWizardOperator nop = NewProjectWizardOperator.invoke();
        nop.selectCategory(web);
        nop.selectProject(webApplication);
        nop.next();
        NewWebProjectNameLocationStepOperator lop = new NewWebProjectNameLocationStepOperator();
        lop.setProjectName(PROJECT_NAME);
        lop.setProjectLocation(getDataDir().getCanonicalPath());
        lop.next();
        lop.next();
        NewProjectWizardOperator frameworkStep = new NewProjectWizardOperator();
        // select Struts
        JTableOperator tableOper = new JTableOperator(frameworkStep);
        for (int i = 0; i < tableOper.getRowCount(); i++) {
            if (tableOper.getValueAt(i, 1).toString().startsWith("org.netbeans.modules.web.struts.StrutsFrameworkProvider")) { // NOI18N
                tableOper.selectCell(i, 0);
                break;
            }
        }
        // set ApplicationResource location
        new JTextFieldOperator(
                (JTextField) new JLabelOperator(frameworkStep, "Application Resource:").getLabelFor()).setText("com.mycompany.eshop.struts.ApplicationResource");
        frameworkStep.btFinish().pushNoBlock();
        frameworkStep.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        frameworkStep.waitClosed();
        // wait label of progress bar "Opening Projects" and possibly "Scanning" dismiss
        JLabelOperator lblOpeningProjects = new JLabelOperator(MainWindowOperator.getDefault(), "Opening Projects");
        lblOpeningProjects.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        lblOpeningProjects.waitComponentShowing(false);
        // let project tree generate
        new EventTool().waitNoEvent(300);
        // Check project contains all needed files.
        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
        Node welcomeNode = new Node(webPages, "welcomeStruts.jsp");
        Node strutsConfig = new Node(webPages, "WEB-INF|struts-config.xml");
        new OpenAction().performAPI(strutsConfig);
        webPages.setComparator(new DefaultStringComparator(true, true));
        Node webXML = new Node(webPages, "WEB-INF|web.xml");
        new EditAction().performAPI(webXML);
        EditorOperator webXMLEditor = new EditorOperator("web.xml");
        String expected = "<servlet-class>org.apache.struts.action.ActionServlet</servlet-class>";
        assertTrue("ActionServlet should be created in web.xml.", webXMLEditor.getText().indexOf(expected) > -1);
        webXMLEditor.replace("index.jsp", "login.jsp");
        webXMLEditor.save();
        waitScanFinished();
    }

    /**
     * Create login.jsp and insert prepared source code to it.
     */
    public void testCreateLoginPage() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Web");
        newWizardOper.selectFileType("JSP");
        newWizardOper.next();
        NewJspFileNameStepOperator jspStepOper = new NewJspFileNameStepOperator();
        jspStepOper.setJSPFileName("login");
        jspStepOper.setFolder("");
        jspStepOper.finish();
        // verify
        EditorOperator loginEditorOper = new EditorOperator("login.jsp");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("EndToEndTest.properties"));
        String sourceCode = properties.getProperty("login");
        // wait for text to be displayed
        loginEditorOper.txtEditorPane().waitText("JSP Page");
        loginEditorOper.replace(loginEditorOper.txtEditorPane().getDisplayedText(), sourceCode);
        loginEditorOper.save();
    }

    /**
     * Create bean which handles login form.
     */
    public void testCreateLoginBean() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Struts");
        newWizardOper.selectFileType("Struts ActionForm Bean");
        newWizardOper.next();
        NewJavaFileNameLocationStepOperator nameStepOper = new NewJavaFileNameLocationStepOperator();
        nameStepOper.setObjectName("LoginForm");
        nameStepOper.setPackage("com.mycompany.eshop.struts.forms");
        nameStepOper.finish();
        EditorOperator loginEditorOper = new EditorOperator("LoginForm.java");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("EndToEndTest.properties"));
        String sourceCode = properties.getProperty("LoginForm");
        loginEditorOper.replace(loginEditorOper.txtEditorPane().getDisplayedText(), sourceCode);
        loginEditorOper.save();
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        String expected = "<form-bean name=\"LoginForm\" type=\"com.mycompany.eshop.struts.forms.LoginForm\"/>";
        assertTrue("form-bean record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
    }

    /**
     * Create struts action which verify input fields in login form.
     */
    public void testCreateLoginAction() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Struts");
        newWizardOper.selectFileType("Struts Action");
        newWizardOper.next();
        NewJavaFileNameLocationStepOperator nameStepOper = new NewJavaFileNameLocationStepOperator();
        nameStepOper.setObjectName("LoginVerifyAction");
        nameStepOper.setPackage("com.mycompany.eshop.struts.actions");
        JTextFieldOperator txtActionPath = new JTextFieldOperator(
                (JTextField) new JLabelOperator(nameStepOper, "Action Path:").getLabelFor());
        txtActionPath.setText("/Login/Verify");
        nameStepOper.next();
        // "ActionForm Bean, Parameter" page
        NewFileWizardOperator actionBeanStepOper = new NewFileWizardOperator();
        // set Input Resource
        new JTextFieldOperator(actionBeanStepOper, "/").setText("/login.jsp");
        new JRadioButtonOperator(actionBeanStepOper, "Request").push();
        actionBeanStepOper.finish();
        EditorOperator loginEditorOper = new EditorOperator("LoginVerifyAction.java");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("EndToEndTest.properties"));
        String sourceCode = properties.getProperty("LoginVerifyAction");
        loginEditorOper.replace(loginEditorOper.txtEditorPane().getDisplayedText(), sourceCode);
        loginEditorOper.save();
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        String expected = "<action input=\"/login.jsp\" name=\"LoginForm\" path=\"/Login/Verify\" scope=\"request\" type=\"com.mycompany.eshop.struts.actions.LoginVerifyAction\"/>";
        assertTrue("action record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
    }

    /**
     * Create SecurityManager class.
     */
    public void testCreateSecurityManager() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        // need to distinguish Java and Java Server Faces
        newWizardOper.treeCategories().setComparator(new DefaultStringComparator(true, true));
        newWizardOper.selectCategory("Java");
        newWizardOper.selectFileType("Empty Java File");
        newWizardOper.next();
        NewJavaFileNameLocationStepOperator nfnlso = new NewJavaFileNameLocationStepOperator();
        nfnlso.setObjectName("SecurityManager");
        nfnlso.setPackage("com.mycompany.eshop.security");
        nfnlso.finish();
        EditorOperator editorOper = new EditorOperator("SecurityManager.java");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("EndToEndTest.properties"));
        String sourceCode = properties.getProperty("SecurityManager");
        editorOper.replace(editorOper.txtEditorPane().getDisplayedText(), sourceCode);
        editorOper.save();
    }

    /**
     * Call "Add Forward" action in struts-config.xml and fill in the dialog
     * values.
     */
    public void testCreateForward() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        strutsConfigEditor.select(18);
        ActionNoBlock addForwardAction = new ActionNoBlock(null, "Struts|Add Forward");
        addForwardAction.setComparator(new DefaultStringComparator(true, true));
        addForwardAction.perform(strutsConfigEditor);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        NbDialogOperator addForwardOper = new NbDialogOperator("Add Forward");
        JTextFieldOperator txtForwardName = new JTextFieldOperator(
                (JTextField) new JLabelOperator(addForwardOper, "Forward Name:").getLabelFor());
        txtForwardName.setText("success");
        new JTextFieldOperator(addForwardOper, "/").setText("/shop.jsp");
        // set Redirect check box
        new JCheckBoxOperator(addForwardOper).push();
        // select Action as Location
        new JRadioButtonOperator(addForwardOper, "Action:", 1).push();
        new JButtonOperator(addForwardOper, "Add").push();
        String expected = "<forward name=\"success\" path=\"/shop.jsp\" redirect=\"true\"/>";
        assertTrue("forward record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }

    /**
     * Create shop.jsp and insert prepared source code to it.
     */
    public void testCreateShopPage() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Web");
        newWizardOper.selectFileType("JSP");
        newWizardOper.next();
        NewJspFileNameStepOperator jspStepOper = new NewJspFileNameStepOperator();
        jspStepOper.setJSPFileName("shop");
        jspStepOper.setFolder("");
        jspStepOper.finish();
        // verify
        EditorOperator editorOper = new EditorOperator("shop.jsp");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("EndToEndTest.properties"));
        String sourceCode = properties.getProperty("shop");
        // wait for text to be displayed
        editorOper.txtEditorPane().waitText("JSP Page", -1);
        editorOper.replace(editorOper.txtEditorPane().getDisplayedText(), sourceCode);
        editorOper.save();
    }

    /**
     * Create logout.jsp and insert prepared source code to it.
     */
    public void testCreateLogoutPage() throws IOException {
        NewFileWizardOperator newWizardOper = NewFileWizardOperator.invoke();
        newWizardOper.selectProject(PROJECT_NAME);
        newWizardOper.selectCategory("Web");
        newWizardOper.selectFileType("JSP");
        newWizardOper.next();
        NewJspFileNameStepOperator jspStepOper = new NewJspFileNameStepOperator();
        jspStepOper.setJSPFileName("logout");
        jspStepOper.setFolder("");
        jspStepOper.finish();
        // verify
        EditorOperator editorOper = new EditorOperator("logout.jsp");
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("EndToEndTest.properties"));
        String sourceCode = properties.getProperty("logout");
        // wait for text to be displayed
        editorOper.txtEditorPane().waitText("JSP Page", -1);
        editorOper.replace(editorOper.txtEditorPane().getDisplayedText(), sourceCode);
        editorOper.save();
    }

    /**
     * Call "Add Forward/Include" action in struts-config.xml and fill in the
     * dialog values.
     */
    public void testCreateForwardInclude() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        ActionNoBlock addForwardAction = new ActionNoBlock(null, "Struts|Add Forward/Include");
        addForwardAction.perform(strutsConfigEditor);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        NbDialogOperator addForwardOper = new NbDialogOperator("Add Forward/Include Action");
        // set Action Path
        new JTextFieldOperator(addForwardOper, "/").setText("/Logout");
        new JButtonOperator(addForwardOper, "Browse").pushNoBlock();
        NbDialogOperator browseOper = new NbDialogOperator("Browse Files");
        new Node(new JTreeOperator(browseOper), "Web Pages|logout.jsp").select();
        new JButtonOperator(browseOper, "Select File").push();
        new JButtonOperator(addForwardOper, "Add").push();
        String expected = "<action forward=\"/logout.jsp\" path=\"/Logout\"/>";
        assertTrue("forward record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }

    /**
     * Call "Add Action" action in struts-config.xml and fill in the dialog
     * values.
     */
    public void testCreateAction() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        ActionNoBlock addAction = new ActionNoBlock(null, "Struts|Add Action");
        addAction.perform(strutsConfigEditor);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        NbDialogOperator addActionOper = new NbDialogOperator("Add Action");

        JTextFieldOperator txtActionClass = new JTextFieldOperator(
                (JTextField) new JLabelOperator(addActionOper, "Action Class:").getLabelFor());
        txtActionClass.setText("com.mycompany.eshop.struts.forms.LoginForm");
        JTextFieldOperator txtActionPath = new JTextFieldOperator(
                (JTextField) new JLabelOperator(addActionOper, "Action Path:").getLabelFor());
        txtActionPath.setText("/LoginForm");
        new JRadioButtonOperator(addActionOper, "Input Action:").push();
        new JButtonOperator(addActionOper, "Add").push();
        String expected = "<action input=\"/Login/Verify\" name=\"LoginForm\" path=\"/LoginForm\" scope=\"session\" type=\"com.mycompany.eshop.struts.forms.LoginForm\"/>";
        assertTrue("action record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }

    /**
     * Call "Add Exception" action in struts-config.xml and fill in the dialog
     * values.
     */
    public void testCreateException() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        ActionNoBlock addException = new ActionNoBlock(null, "Struts|Add Exception");
        addException.perform(strutsConfigEditor);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        NbDialogOperator addExceptionOper = new NbDialogOperator("Add Exception");

        JTextFieldOperator txtBundleKey = new JTextFieldOperator(
                (JTextField) new JLabelOperator(addExceptionOper, "Bundle Key:").getLabelFor());
        txtBundleKey.setText("exception");
        new JButtonOperator(addExceptionOper, "Browse", 1).pushNoBlock();
        NbDialogOperator browseOper = new NbDialogOperator("Browse Files");
        new Node(new JTreeOperator(browseOper), "Web Pages|login.jsp").select();
        new JButtonOperator(browseOper, "Select File").push();
        new JButtonOperator(addExceptionOper, "Add").push();
        String expected = "<exception key=\"exception\" path=\"/login.jsp\" type=\"java.lang.NumberFormatException\"/>";
        assertTrue("exception record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }

    /**
     * Call "Add ActionForm Bean" action in struts-config.xml and fill in the
     * dialog values.
     */
    public void testCreateActionFormBean() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        ActionNoBlock addActionFormBean = new ActionNoBlock(null, "Struts|Add ActionForm Bean");
        addActionFormBean.perform(strutsConfigEditor);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        NbDialogOperator addActionFormBeanOper = new NbDialogOperator("Add ActionForm Bean");

        JTextFieldOperator txtActionFormBeanName = new JTextFieldOperator(
                (JTextField) new JLabelOperator(addActionFormBeanOper, "ActionForm Bean Name:").getLabelFor());
        txtActionFormBeanName.setText("ActionFormBean");
        new JTextFieldOperator(addActionFormBeanOper, 1).setText("com.mycompany.eshop.struts.forms.LoginForm");
        new JButtonOperator(addActionFormBeanOper, "Add").push();
        String expected = "<form-bean name=\"ActionFormBean\" type=\"com.mycompany.eshop.struts.forms.LoginForm\"/>";
        assertTrue("actionform bean record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }

    /**
     * Call "Add ActionForm Bean Property" action in struts-config.xml and fill
     * in the dialog values.
     */
    public void testCreateActionFormBeanProperty() {
        EditorOperator strutsConfigEditor = new EditorOperator("struts-config.xml");
        ActionNoBlock addActionFormBeanProp = new ActionNoBlock(null, "Struts|Add ActionForm Bean Property");
        addActionFormBeanProp.perform(strutsConfigEditor);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        NbDialogOperator addActionFormBeanPropOper = new NbDialogOperator("Add ActionForm Bean Property");

        JTextFieldOperator txtPropertyName = new JTextFieldOperator(
                (JTextField) new JLabelOperator(addActionFormBeanPropOper, "Property Name:").getLabelFor());
        txtPropertyName.setText("property");
        new JButtonOperator(addActionFormBeanPropOper, "Add").push();
        String expected = "<form-property initial=\"\" name=\"property\" type=\"java.lang.String\"/>";
        assertTrue("actionform bean property record should be added to struts-config.xml.", strutsConfigEditor.getText().indexOf(expected) > -1);
        strutsConfigEditor.save();
    }

    /**
     * Run created application.
     */
    public void testRunApplication() {
        // not display browser on run
        // open project properties
        ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME).properties();
        // "Project Properties"
        String projectPropertiesTitle = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        String displayBrowserLabel = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
        new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
        // confirm properties dialog
        propertiesDialogOper.ok();

        try {
            new Action(null, "Run").perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME));
            waitText(PROJECT_NAME, 360000, "Login");
        } finally {
            // log messages from output
            getLog("RunOutput").print(new OutputTabOperator(PROJECT_NAME).getText());
            getLog("ServerLog").print(new OutputTabOperator("GlassFish").getText());
        }
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
}
