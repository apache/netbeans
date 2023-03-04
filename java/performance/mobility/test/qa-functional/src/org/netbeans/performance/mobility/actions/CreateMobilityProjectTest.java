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

package org.netbeans.performance.mobility.actions;

import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.mobility.setup.MobilitySetup;

import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test create CreateMobilityProject
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org, mrkam@netbeans.org
 */
public class CreateMobilityProjectTest extends PerformanceTestCase {

    private NewJavaProjectNameLocationStepOperator wizard_location;
    private String category,  project,  project_name,  project_type;
    private int index;

    /**
     * Creates a new instance of CreateMobilityProject
     * @param testName the name of the test
     */
    public CreateMobilityProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 10000;
    }

    /**
     * Creates a new instance of CreateMobilityProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateMobilityProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 10000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(CreateMobilityProjectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCreateMobilityProject() {
        category = "Java ME"; // NOI18N
        project = "Mobile Application"; // NOI18N
        project_type = "MobileApp";
        index = 1;
        doMeasurement();
    }

    public void testCreateMobilityLibrary() {
        category = "Java ME"; // NOI18N
        project = "Mobile Class Library"; // NOI18N
        project_type = "MobileLib";
        index = 1;
        doMeasurement();
    }

    @Override
    public void initialize() {
    }

    public void prepare() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewJavaProjectNameLocationStepOperator();
        String directory = CommonUtilities.getTempDir() + "createdProjects";
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(directory);
        project_name = project_type + "_" + (index++);
        wizard_location.txtProjectName().clearText();
        wizard_location.txtProjectName().typeText(project_name);
    }

    public ComponentOperator open() {
        wizard_location.finish();
        return null;
    }

    @Override
    public void close() {
    }

}
