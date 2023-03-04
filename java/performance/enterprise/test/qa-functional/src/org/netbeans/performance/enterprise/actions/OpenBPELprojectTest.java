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

import java.io.IOException;
import org.openide.util.Exceptions;

import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;

import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Open BPEL project
 *
 * @author  rashid@netbeans.org
 */
public class OpenBPELprojectTest extends PerformanceTestCase {
    
    private static String projectName = "BPELTestProject";
    private JButtonOperator openButton;
    
    /**
     * Creates a new instance of OpenBPELproject
     * @param testName the name of the test
     */
    public OpenBPELprojectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of CreateProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenBPELprojectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public void testOpenBPELproject() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(OpenBPELprojectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
        CommonUtilities.actionOnProject(projectName, "Close");
    }
    
    public void prepare(){
        new ActionNoBlock("File|Open Project...",null).perform(); //NOI18N
        WizardOperator opd = new WizardOperator("Open Project"); //NOI18N
        JTextComponentOperator path = new JTextComponentOperator(opd,1);
        openButton = new JButtonOperator(opd,"Open Project"); //NOI18N
        String paths = getDataDir() + java.io.File.separator + projectName;
        path.setText(paths);
    }
    
    public ComponentOperator open(){
        openButton.pushNoBlock();
        return null;
    }
    
    @Override
    public void close(){
        CommonUtilities.actionOnProject(projectName, "Close");
    }

    public void shutdown() {
        try {
            this.openDataProjects("BPELTestProject");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
