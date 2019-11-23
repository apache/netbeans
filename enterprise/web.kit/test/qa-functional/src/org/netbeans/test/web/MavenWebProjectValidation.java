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
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author Jindrich Sedek
 */
public class MavenWebProjectValidation extends WebProjectValidation {

    @SuppressWarnings("hiding")
    public static final String[] TESTS = {
        "testNewMavenWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testCreateTLD", "testCreateTagHandler", "testNewHTML",
        "testNewSegment", "testNewDocument",
        "testCleanAndBuildProject", "testRunProject", "testRunJSP",
        "testViewServlet", "testRunServlet", "testRunTag", "testRunHTML",
        "testFinish"
    };

    public MavenWebProjectValidation(String name) {
        super(name);
        PROJECT_NAME = "WebMavenProject";
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, MavenWebProjectValidation.class, TESTS);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        cancelIndexing();
    }

    @Override
    protected String getEEVersion() {
        return JAVA_EE_8;
    }

    public void testNewMavenWebProject() throws IOException {
        runAndCancelUpdateIndex("central");
        runAndCancelUpdateIndex("Local");
        NewProjectWizardOperator projectWizard = NewProjectWizardOperator.invoke();
        projectWizard.selectCategory("Maven");
        projectWizard.selectProject("Web Application");
        projectWizard.next();
        WizardOperator mavenWebAppWizardOperator = new WizardOperator(projectWizard.getTitle());
        Component pnComp = new JLabelOperator(mavenWebAppWizardOperator, "Project Name").getLabelFor();
        JTextFieldOperator projectName = new JTextFieldOperator((JTextField) pnComp);
        projectName.setText(PROJECT_NAME);

        Component plComp = new JLabelOperator(mavenWebAppWizardOperator, "Project Location").getLabelFor();
        JTextFieldOperator projectLocation = new JTextFieldOperator((JTextField) plComp);
        projectLocation.setText(PROJECT_LOCATION);
        mavenWebAppWizardOperator.next();
        NewWebProjectServerSettingsStepOperator serverStep = new NewWebProjectServerSettingsStepOperator();
        if (JAVA_EE_5.equals(getEEVersion())) {
            serverStep.cboJavaEEVersion().selectItem("1.5");
        } else if (JAVA_EE_6.equals(getEEVersion())) {
            serverStep.cboJavaEEVersion().selectItem("1.6-web");
        } else if (JAVA_EE_7.equals(getEEVersion())) {
            serverStep.cboJavaEEVersion().selectItem("1.7-web");
        } else if (JAVA_EE_8.equals(getEEVersion())) {
            serverStep.cboJavaEEVersion().selectItem("1.8-web");
        }
        serverStep.finish();
        // need to increase time to wait for project node
        long oldTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 240000);
        try {
            if (JAVA_EE_7.equals(getEEVersion())) {
                verifyWebPagesNode("index.html");
            } else {
                verifyWebPagesNode("index.jsp");
            }
        } finally {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", oldTimeout);
        }
        // disable compile on save
        new ActionNoBlock(null, "Properties").perform(new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME));
        // "Project Properties"
        NbDialogOperator propertiesDialogOper = new NbDialogOperator("Project Properties");
        // select "Build|Compile" category
        new Node(new JTreeOperator(propertiesDialogOper), "Build|Compile").select();
        // untick checkbox
        JCheckBoxOperator cosCheckBox = new JCheckBoxOperator(propertiesDialogOper,
                Bundle.getStringTrimmed("org.netbeans.modules.maven.customizer.Bundle",
                        "CompilePanel.cbCompileOnSave.text"));
        cosCheckBox.doClick();
        // not display browser on run
        new Node(new JTreeOperator(propertiesDialogOper),
                Bundle.getString("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Config_Run")).select();
        new JCheckBoxOperator(propertiesDialogOper,
                Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle",
                        "LBL_CustomizeRun_DisplayBrowser_JCheckBox")).setSelected(false);
        // confirm properties dialog
        propertiesDialogOper.ok();
        waitScanFinished();
    }

    @Override
    public void testCleanAndBuildProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(PROJECT_NAME);
        Util.cleanStatusBar();
        new Action(null, "Clean and Build").perform(rootNode);
        waitBuildSuccessful();
    }

    @Override
    public void waitBuildSuccessful() {
        new Action(Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window")
                + "|" + Bundle.getStringTrimmed("org.netbeans.core.output2.Bundle", "OutputWindow"),
                null).performMenu();
        String outputName = PROJECT_NAME;
        String runningTest = getName();
        if (runningTest.contains("testRunJSP")) {
            outputName = "pageRunJSP";
        } else if (runningTest.contains("testViewServlet")) {
            outputName = "pageViewServlet";
        } else if (runningTest.contains("testRunServlet")) {
            outputName = "Servlet1";
        } else if (runningTest.contains("testRunTag")) {
            outputName = "pageRunTag";
        }
        OutputTabOperator console = new OutputTabOperator(outputName);
        console.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000);
        console.waitText("BUILD SUCCESS");
    }

    /**
     * Cancel Indexing Maven repository or Unpacking index tasks which are not
     * necessary for tests.
     */
    protected void cancelIndexing() {
        String[] labels = {"Indexing Maven repository", "Transferring Maven repository index", "Unpacking index"};
        for (String label : labels) {
            Object lblIndexing = JLabelOperator.findJLabel(
                    (Container) MainWindowOperator.getDefault().getSource(),
                    label, false, false);
            if (lblIndexing != null) {
                JButton cancelJButton = (JButton) JButtonOperator.findJComponent(
                        (Container) MainWindowOperator.getDefault().getSource(),
                        "Click to cancel process", true, true);
                if (cancelJButton != null) {
                    JButtonOperator btnCancel = new JButtonOperator(cancelJButton);
                    btnCancel.pushNoBlock();
                    try {
                        new NbDialogOperator("Cancel Running Task").yes();
                    } catch (TimeoutExpiredException tee) {
                        // ignore if not opened
                    }
                }
            }
        }
    }

    /**
     * Opens Services tab, go to specified Maven repository, call Update Index
     * and immediately cancel this action.
     *
     * @param repositoryName
     */
    protected void runAndCancelUpdateIndex(String repositoryName) {
        RuntimeTabOperator servicesOper = RuntimeTabOperator.invoke();
        Node node = new Node(servicesOper.getRootNode(), "Maven Repositories|" + repositoryName);
        new Action(null, "Update Index").perform(node);
        try {
            // wait for task to start
            JButtonOperator btnCancel = new JButtonOperator(
                    (JButton) JButtonOperator.waitJComponent(
                    (Container) MainWindowOperator.getDefault().getSource(),
                    "Click to cancel process", true, true));
        } catch (TimeoutExpiredException tee) {
            // ignore if not opened
        }
        cancelIndexing();
    }
}
