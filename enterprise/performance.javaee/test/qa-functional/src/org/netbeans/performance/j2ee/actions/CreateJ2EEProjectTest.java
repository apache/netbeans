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

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.performance.j2ee.setup.J2EEBaseSetup;

/**
 * Test create projects
 *
 * @author lmartinek@netbeans.org
 */
public class CreateJ2EEProjectTest extends PerformanceTestCase {

    private NewWebProjectNameLocationStepOperator wizard_location;

    private String category, project, projectType, projectName;
    private boolean createSubProjects = false;
    public static final String WEB_PROJECT_NAME = "WebApp";

    /**
     * Creates a new instance of CreateJ2EEProjectTest
     *
     * @param testName the name of the test
     */
    public CreateJ2EEProjectTest(String testName) {
        super(testName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN = 5000;
    }

    /**
     * Creates a new instance of CreateJ2EEProjectTest
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateJ2EEProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN = 5000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EEBaseSetup.class).addTest(CreateJ2EEProjectTest.class,
                "testCreateWebProject",
                "testCreateEJBModuleProject",
                "testCreateEnterpriseApplicationProject",
                "testCreateStandaloneEnterpriseApplicationProject",
                "testCreateEnterpriseApplicationClient"
                ).suite();
    }

    public void testCreateWebProject() {
        category = "Java Web";
        project = "Web Application";
        projectType = "WebApp";
        addEditorPhaseHandler();
        doMeasurement();
        removeEditorPhaseHandler();
    }

    public void testCreateEnterpriseApplicationProject() {
        category = "Java EE";
        project = "Enterprise Application";
        projectType = WEB_PROJECT_NAME;
        createSubProjects = true;
        doMeasurement();
    }

    public void testCreateStandaloneEnterpriseApplicationProject() {
        category = "Java EE";
        project = "Enterprise Application";
        projectType = "MyStandaloneApp";
        createSubProjects = false;
        doMeasurement();
    }

    public void testCreateEJBModuleProject() {
        category = "Java EE";
        project = "EJB Module";
        projectType = "MyEJBModule";
        doMeasurement();
    }

    public void testCreateEnterpriseApplicationClient() {
        category = "Java EE";
        project = "Enterprise Application Client";
        projectType = "MyEntAppClient";
        addEditorPhaseHandler();
        doMeasurement();
        removeEditorPhaseHandler();
    }

    @Override
    public void initialize() {
    }

    @Override
    public void prepare() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewWebProjectNameLocationStepOperator();
        if (System.getProperty("os.name", "").contains("Windows")) {
            // #238007 - wizard too wide
            wizard_location.txtProjectLocation().setText("C:\\tmp");
        } else {
            wizard_location.txtProjectLocation().setText(System.getProperty("nbjunit.workdir") + java.io.File.separator + "tmpdir");
        }
        projectName = projectType + CommonUtilities.getTimeIndex();
        wizard_location.txtProjectName().setText(projectName);
        wizard_location.next();
        if (project.equals("Enterprise Application")) {
            JCheckBoxOperator createEjb = new JCheckBoxOperator(wizard_location, "Ejb");
            JCheckBoxOperator createWeb = new JCheckBoxOperator(wizard_location, "Web");
            createEjb.setSelected(createSubProjects);
            createWeb.setSelected(createSubProjects);
        }
    }

    @Override
    public ComponentOperator open() {
        wizard_location.finish();
        wizard_location.waitClosed();
        new ProjectsTabOperator().getProjectRootNode(projectName);
        return null;
    }

    @Override
    public void close() {
        new CloseAction().perform(new ProjectsTabOperator().getProjectRootNode(projectName));
        waitScanFinished();
    }

    @Override
    public void shutdown() {
    }
}
