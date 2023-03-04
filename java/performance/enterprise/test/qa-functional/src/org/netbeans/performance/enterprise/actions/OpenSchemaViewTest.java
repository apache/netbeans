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

import java.awt.Container;
import javax.swing.JComponent;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.performance.enterprise.EPUtilities;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;
import org.netbeans.performance.enterprise.XMLSchemaComponentOperator;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mkhramov@netbeans.org, mmirilovic@netbeans.org
 *
 */
public class OpenSchemaViewTest extends PerformanceTestCase {
    
    private static String projectName, schemaName;
    private Node schemaNode;
    
    /** Creates a new instance of OpenSchemaView */
    public OpenSchemaViewTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }
    
    public OpenSchemaViewTest(String testName, String  performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(OpenSchemaViewTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testOpenSchemaView(){
        projectName = "TravelReservationService";
        schemaName = "OTA_TravelItinerary";
        doMeasurement();
    }

    public void initialize() {
        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {
            public boolean accept(JComponent c) {
                Container cont = c;
                do {
                    if ("o.n.core.multiview.MultiViewCloneableTopComponent".equals(cont.getClass().getName())) {
                        return true;
                    }
                    cont = cont.getParent();
                } while (cont != null);
                return false;
            }

            public String getFilterName() {
                return "Filter for MultiViewCloneableTopComponent";
            }
        });
    }

    public void prepare() {
    }
    
    public ComponentOperator open() {
                schemaNode = new Node(new EPUtilities().getProcessFilesNode(projectName),schemaName+".xsd");

        schemaNode.callPopup().pushMenuNoBlock("Open");
        //return null;
        return XMLSchemaComponentOperator.findXMLSchemaComponentOperator(schemaName+".xsd");
    }

    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }

    @Override
    public void close(){
    }
 
}
