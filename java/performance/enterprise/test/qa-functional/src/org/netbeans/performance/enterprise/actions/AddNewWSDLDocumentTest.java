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

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.EPUtilities;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;

import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Add New WSDL Document
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class AddNewWSDLDocumentTest extends PerformanceTestCase {
    
    private NewJavaFileNameLocationStepOperator location;
    
    /**
     * Creates a new instance of AddNewWSDLDocument
     * @param testName the name of the test
     */
    public AddNewWSDLDocumentTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }
    
    /**
     * Creates a new instance of AddNewWSDLDocument
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public AddNewWSDLDocumentTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(AddNewWSDLDocumentTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testAddNewWSDLDocumentTest() {
        doMeasurement();
    }

    @Override
    public void initialize(){
    }
    
    public void prepare(){
        new EPUtilities().getProcessFilesNode("BPELTestProject").select();
        // Workaround for issue 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        wizard.selectCategory("XML"); //NOI18N
        wizard.selectFileType("WSDL Document"); //NOI18N
        wizard.next();
        location = new NewJavaFileNameLocationStepOperator();
        location.txtObjectName().setText("WSDLDoc_"+System.currentTimeMillis());
    }
    
    public ComponentOperator open(){
        location.finish();
        return null;
    }
    
    @Override
    public void close(){
    }

}
