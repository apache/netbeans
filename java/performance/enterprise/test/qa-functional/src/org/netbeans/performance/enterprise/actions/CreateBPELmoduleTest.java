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

import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewJavaProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test create BPELmodule
 *
 * @author  rashid@netbeans.org, mrkam@netbeans.org
 */
public class CreateBPELmoduleTest extends PerformanceTestCase {
    
    private NewJavaProjectNameLocationStepOperator wizard_location;
    private NewProjectWizardOperator wizard;
    private String category, project, project_name, project_type;
    
    /**
     * Creates a new instance of CreateBPELmodule
     * @param testName the name of the test
     */
    public CreateBPELmoduleTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateBPELmoduleTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(CreateBPELmoduleTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCreateBPELModule() {
        doMeasurement();
    }

    @Override
    public void initialize(){
        category = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.Bundle", "OpenIDE-Module-Display-Category"); // "SOA"
        project = Bundle.getStringTrimmed("org.netbeans.modules.bpel.project.wizards.Bundle", "LBL_BPEL_Wizard_Title"); // "BPEL Module"
        project_type="BPELModule";
        MainWindowOperator.getDefault().maximize();
    }
    
    public void prepare(){
        wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.move(0, 0);    
        wizard.next();
        wizard_location = new NewJavaProjectNameLocationStepOperator();
        String directory = CommonUtilities.getTempDir() + "createdProjects";
        wizard_location.txtProjectLocation().clearText();
        wizard_location.txtProjectLocation().typeText(directory);
        project_name = project_type + "_" + System.currentTimeMillis();
        wizard_location.txtProjectName().clearText();
        wizard_location.txtProjectName().typeText(project_name);
    }
    
    public ComponentOperator open(){
        wizard_location.finish();
        return null;
    }
    
    @Override
    public void close(){
        closeAllModal(); 
    }

}
