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

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Test web application project from existing sources.
 *
 * @author lm97939
 */
public class WebProjectValidationNb36WebModule extends WebProjectValidation {

    public static final String[] TESTS = new String[]{
        "testNewWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testCompileAllJSP", "testCompileJSP",
        "testCleanAndBuildProject", "testRunProject", "testRunJSP",
        "testRunServlet", "testCreateTLD", "testCreateTagHandler", "testRunTag",
        "testNewHTML", "testRunHTML", "testNewSegment", "testNewDocument",
        "testFinish"
    };

    /** Need to be defined because of JUnit */
    public WebProjectValidationNb36WebModule(String name) {
        super(name);
        PROJECT_NAME = "WebModuleNB36"; // NOI18N
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, WebProjectValidationNb36WebModule.class, TESTS);
    }

    @Override
    protected String getEEVersion() {
        return J2EE_4;
    }

    /** Test creation of web application.
     * - open New Project wizard from main menu (File|New Project)
     * - select Java Web|Web Application with Existing Sources
     * - in the next panel type project name and project location
     * - finish the wizard
     * - wait until scanning of java files is finished
     */
    @Override
    public void testNewWebProject() throws IOException {
        new NewProjectAction().perform();
        NewProjectWizardOperator projectWizard = new NewProjectWizardOperator();
        projectWizard.selectCategory("Java Web"); // XXX use Bundle.getString instead
        projectWizard.selectProject("Web Application with Existing Sources");
        projectWizard.next();
        NewWebProjectNameLocationStepOperator nameStep = new NewWebProjectNameLocationStepOperator();
        nameStep.txtLocation().setText(getDataDir().getAbsolutePath()
                + File.separator + PROJECT_NAME);
        nameStep.txtProjectName().setText(PROJECT_NAME);
        nameStep.txtProjectFolder().setText(getWorkDirPath()
                + File.separator + PROJECT_NAME + "Prj");
        nameStep.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        serverStep.cboServer().selectItem(0);
        serverStep.next();
        NewWebProjectSourcesStepOperator srcStep = new NewWebProjectSourcesStepOperator();
        srcStep.finish();
        // wait for project creation
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
}
