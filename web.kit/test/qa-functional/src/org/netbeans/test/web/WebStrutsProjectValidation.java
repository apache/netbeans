/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
