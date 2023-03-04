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

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class SchemaNavigatorSchemaViewTest  extends PerformanceTestCase {
    
    private Node processNode, schemaNode, anotherSchemaNode;
    
    //public final long EXPECTED_TIME = 10000; // After fix of issue 128585 "Please wait" text is shown
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public SchemaNavigatorSchemaViewTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public SchemaNavigatorSchemaViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public void testSchemaNavigatorSchemaView() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(SchemaNavigatorSchemaViewTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    protected void initialize() {
        processNode = new EPUtilities().getProcessFilesNode("TravelReservationService");
        processNode.select();
        schemaNode = new Node(processNode, "OTA_TravelItinerary.xsd");
        anotherSchemaNode = new Node(processNode, "OTA_TravelItinerary_1.xsd");
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open() {
        schemaNode.select();
        TopComponentOperator topComponentOperator = new TopComponentOperator("Navigator");
        JComboBoxOperator combo = new JComboBoxOperator(topComponentOperator); // NOI18N
        combo.selectItem("Schema View"); // NOI18N
        return topComponentOperator;
    }
    
    @Override
    public void close() {
        anotherSchemaNode.select();
        JComboBoxOperator combo = new JComboBoxOperator(new TopComponentOperator("Navigator")); // NOI18N
        combo.selectItem("Design View"); // NOI18N
    }

}
