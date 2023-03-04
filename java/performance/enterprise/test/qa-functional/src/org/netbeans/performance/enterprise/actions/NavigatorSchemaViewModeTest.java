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
public class NavigatorSchemaViewModeTest  extends PerformanceTestCase {
    
    private Node processNode; 
    
    // After fix of 128596 "Please wait" text is shown so it is okay to wait up to 10 sec
    public final long EXPECTED_TIME = 10000;
    
    /** Creates a new instance of SchemaDesignView */
    public NavigatorSchemaViewModeTest(String testName) {
        super(testName);
        expectedTime = EXPECTED_TIME; 
    }
    
    public NavigatorSchemaViewModeTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = EXPECTED_TIME; 
    }

    public void testNavigatorSchemaViewMode() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(NavigatorSchemaViewModeTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    protected void initialize() {
        processNode = new EPUtilities().getProcessFilesNode("TravelReservationService");
        Node doc1 = new Node(processNode,"OTA_TravelItinerary_1.xsd");
        doc1.select();
        
        JComboBoxOperator combo = new JComboBoxOperator(new TopComponentOperator("Navigator")); // NOI18N
        combo.selectItem("Schema View"); // NOI18N
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open() {
        Node doc = new Node(processNode,"OTA_TravelItinerary.xsd");
        doc.select();
        return new TopComponentOperator("OTA_TravelItinerary.xsd");
    }
    
    @Override
    public void close() {
        Node doc1 = new Node(processNode,"OTA_TravelItinerary_1.xsd");
        doc1.select();
    }
  
}
