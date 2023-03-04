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

package org.netbeans.performance.enterprise.actions;

import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test create CreateCompositeApplication
 *
 * @author  rashid@netbeans.org, mrkam@netbeans.org
 */
public class CreateCompositeApplicationTest extends PerformanceTestCase {
    
    private NewJavaProjectNameLocationStepOperator wizard_location;
    private String category, project, project_name;
    
    /**
     * Creates a new instance of CreateCompositeApplication
     * @param testName the name of the test
     */
    public CreateCompositeApplicationTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }

    /**
     * Creates a new instance of CreateCompositeApplication
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateCompositeApplicationTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=10000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(CreateCompositeApplicationTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCreateCompositeApplication() {
        doMeasurement();
    }

    @Override
    public void initialize(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.Bundle", "OpenIDE-Module-Display-Category"); // "SOA"
        project = "Composite Application"; // NOI18N
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        closeAllModal();
    }
    
    public void prepare(){
        // Workaround for issue 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewJavaProjectNameLocationStepOperator();
        String directory = CommonUtilities.getTempDir() + "createdProjects";
        wizard_location.txtProjectLocation().setText(directory);
        project_name = "CompositeApp_" + System.currentTimeMillis();
        wizard_location.txtProjectName().setText("");
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    @Override
    public void close(){
        CommonUtilities.actionOnProject(project_name, "Close");
    }

    public void shutdown() {
        repaintManager().resetRegionFilters();
    }
    
}
