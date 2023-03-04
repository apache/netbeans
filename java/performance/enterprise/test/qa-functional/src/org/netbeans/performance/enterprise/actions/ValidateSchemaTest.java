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

import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org
 *
 */
public class ValidateSchemaTest extends PerformanceTestCase {
    
    private Node schemaNode;
    private OutputOperator oot;
    
    /** Creates a new instance of ValidateSchema */
    public ValidateSchemaTest(String testName) {
        super(testName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN=2000;
    }
    
    public ValidateSchemaTest(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN=2000;
    }

    public void testValidateSchema() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(ValidateSchemaTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
        schemaNode = new Node(new EPUtilities().getProcessFilesNode("TravelReservationService"),"OTA_TravelItinerary.xsd");
        schemaNode.select();
        new OpenAction().perform(schemaNode);
    }
    
    public void prepare() {
        oot = OutputOperator.invoke();
    }
    
    public ComponentOperator open() {
        schemaNode.performPopupAction("Validate XML"); // NOI18N
        OutputTabOperator asot = oot.getOutputTab("XML check"); // NOI18N
        asot.waitText("XML validation finished"); // NOI18N
        return null;
    }
    
    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
    }

}
