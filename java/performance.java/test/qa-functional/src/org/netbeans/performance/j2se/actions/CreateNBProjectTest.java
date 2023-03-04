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
package org.netbeans.performance.j2se.actions;

import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Test create projects
 *
 * @author mmirilovic@netbeans.org
 */
public class CreateNBProjectTest extends PerformanceTestCase {

    private NewJavaProjectNameLocationStepOperator wizard_location;
    private String category, project, project_name, project_type;
    private NbDialogOperator next;

    /**
     * Creates a new instance of CreateNBProject
     *
     * @param testName the name of the test
     */
    public CreateNBProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 8000;
    }

    /**
     * Creates a new instance of CreateNBProject
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateNBProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 8000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2SESetup.class).addTest(CreateNBProjectTest.class).suite();
    }

    public void testCreateModuleProject() {
        // "NetBeans Modules"
        category = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle", "Templates/Project/APISupport");
        project = "Module";
        project_type = "moduleProject";
        doMeasurement();
    }

    public void testCreateModuleSuiteProject() {
        // "NetBeans Modules"
        category = Bundle.getStringTrimmed("org.netbeans.modules.apisupport.project.ui.wizard.Bundle", "Templates/Project/APISupport");
        project = "Module Suite";
        project_type = "moduleSuiteProject";
        doMeasurement();
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
        wizard_location = new NewJavaProjectNameLocationStepOperator();
        String directory = System.getProperty("nbjunit.workdir");
        wizard_location.txtProjectLocation().setText(directory);
        project_name = project_type + "_" + CommonUtilities.getTimeIndex();
        wizard_location.txtProjectName().setText(project_name);
    }

    @Override
    public ComponentOperator open() {
        if (project_type.equalsIgnoreCase("moduleProject")) {
            wizard_location.next();
            next = new NbDialogOperator("New Module");
            new JTextFieldOperator((JTextField) new JLabelOperator(next, "Code Name Base").getLabelFor()).setText("test");
        }
        wizard_location.finish();
        new ProjectsTabOperator().getProjectRootNode(project_name);
        return null;
    }

    @Override
    public void close() {
        CommonUtilities.actionOnProject(project_name, "Close");
    }
}
