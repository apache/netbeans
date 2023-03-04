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

import javax.swing.tree.TreePath;

import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author rashid@netbeans.org, mmirilovic@netbeans.org, mrkam@netbeans.org
 *
 */
public class OpenComplexDiagramTest extends PerformanceTestCase {
    
    /** Creates a new instance of OpenComplexDiagram */
    
    public OpenComplexDiagramTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public OpenComplexDiagramTest(String testName, String  performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=4000;
    }

    public void testOpenComplexDiagram() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(OpenComplexDiagramTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize(){
    }
    
    public void prepare() {
    }

    public ComponentOperator open() {
        Node processFilesNode = new EPUtilities().getProcessFilesNode("TravelReservationService");
        Node doc = new Node(processFilesNode,"TravelReservationService.bpel");

        // Use double click instead of Open because Open opens Source view
        // while double click opens Schema view
        TreePath treePath = doc.getTreePath();
        doc.tree().clickOnPath(treePath, 2);
        return new TopComponentOperator("TravelReservationService.bpel");
    }

    @Override
    public void close(){
        new CloseAllDocumentsAction().performAPI();        
    }

}
