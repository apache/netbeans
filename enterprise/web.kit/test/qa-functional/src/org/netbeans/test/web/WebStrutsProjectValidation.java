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
package org.netbeans.test.web;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewWebProjectServerSettingsStepOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;

/**
 *
 * @author dkolar
 */
public class WebStrutsProjectValidation extends WebProjectValidationEE5 {

    public static final String[] TESTS = new String[]{
        "testNewStrutsWebProject",
        "testCleanAndBuildProject",
        "testCompileAllJSP",
        "testRedeployProject",
        "testFinish"
    };

    /** Need to be defined because of JUnit */
    public WebStrutsProjectValidation(String name) {
        super(name);
        PROJECT_NAME = "WebStrutsProject";
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, WebStrutsProjectValidation.class, TESTS);
    }

    /** Test creation of web project.
     * - open New Project wizard from main menu (File|New Project)
     * - select Web|Web Application
     * - in the next panel type project name and project location
     * - in next panel sets server to Glassfish and J2EE version to Java EE 5
     * - in Framework panel set Struts framework
     * - finish the wizard
     * - wait until scanning of java files is finished
     */
    public void testNewStrutsWebProject() {
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        String category = Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet");
        projectWizard.selectCategory(category);
        projectWizard.selectProject("Web Application");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep = new NewWebProjectNameLocationStepOperator();
        nameStep.txtProjectName().setText(PROJECT_NAME);
        nameStep.txtProjectLocation().setText(PROJECT_LOCATION);
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.selectJavaEEVersion(getEEVersion());
        serverStep.next();

        NewWebProjectStrutsFrameworkStepOperator frameworkStep = new NewWebProjectStrutsFrameworkStepOperator();
        assertTrue("Struts framework not present!", frameworkStep.setStrutsFrameworkCheckbox());
        // set ApplicationResource location
        frameworkStep.cboActionURLPattern().clearText();

        assertEquals(Bundle.getString("org.netbeans.modules.web.struts.ui.Bundle", "MSG_URLPatternIsEmpty"), frameworkStep.getErrorMessage());
        frameworkStep.cboActionURLPattern().getTextField().typeText("*");
        assertEquals(Bundle.getString("org.netbeans.modules.web.struts.ui.Bundle", "MSG_URLPatternIsNotValid"), frameworkStep.getErrorMessage());
        frameworkStep.cboActionURLPattern().getTextField().typeText(".do");
        frameworkStep.cbAddStrutsTLDs().push();
        frameworkStep.finish();
        waitScanFinished();
        // Check project contains all needed files.
        verifyWebPagesNode("WEB-INF|web.xml");
        verifyWebPagesNode("welcomeStruts.jsp");
        verifyWebPagesNode("WEB-INF|struts-config.xml");

        WebPagesNode webPages = new WebPagesNode(PROJECT_NAME);
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
    }
}
