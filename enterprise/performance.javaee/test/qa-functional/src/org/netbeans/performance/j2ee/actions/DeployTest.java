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
package org.netbeans.performance.j2ee.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import junit.framework.Test;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EEBaseSetup;

/**
 * Test create projects
 *
 * @author lmartinek@netbeans.org
 */
public class DeployTest extends PerformanceTestCase {

    private Node node;
    private static final String PROJECT_NAME = "WebApp" + CommonUtilities.getTimeIndex();

    /**
     * Creates a new instance of CreateJ2EEProject
     *
     * @param testName the name of the test
     */
    public DeployTest(String testName) {
        super(testName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN = 5000;
    }

    /**
     * Creates a new instance of CreateJ2EEProject
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public DeployTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 60000;
        WAIT_AFTER_OPEN = 5000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2EEBaseSetup.class)
                .addTest(DeployTest.class)
                .suite();
    }

    public void testDeploy() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        new J2eeServerNode("GlassFish").start();
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory("Java Web");
        wizard.selectProject("Web Application");
        wizard.next();
        NewWebProjectNameLocationStepOperator wizardLocation = new NewWebProjectNameLocationStepOperator();
        if (System.getProperty("os.name", "").contains("Windows")) {
            // #238007 - wizard too wide
            wizardLocation.txtProjectLocation().setText("C:\\tmp");
        } else {
            wizardLocation.txtProjectLocation().setText(getWorkDirPath());
        }
        wizardLocation.txtProjectName().setText(PROJECT_NAME);
        wizardLocation.next();
        wizardLocation.finish();
        node = pto.getProjectRootNode(PROJECT_NAME);
        node.performPopupAction("Build");
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        MainWindowOperator.getDefault().waitStatusText("Finished building " + node.getText() + " (dist)");
        waitScanFinished();
    }

    @Override
    public void shutdown() {
        J2eeServerNode glassFishNode = J2eeServerNode.invoke("GlassFish");
        Node applicationsNode = new Node(glassFishNode, "Applications");
        new Node(applicationsNode, node.getText()).performPopupAction("Undeploy");
        applicationsNode.waitChildNotPresent(node.getText());
        glassFishNode.stop();
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        node.performPopupAction("Deploy");
        MainWindowOperator.getDefault().waitStatusText("Finished building " + node.getText() + " (run-deploy).");
        return null;
    }

    @Override
    public void close() {
        try {
            URL url = new URL("http://localhost:8080/" + node.getText());
            InputStream stream = url.openStream();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException("Deployed application unavailable.", e);
        }
    }
}
