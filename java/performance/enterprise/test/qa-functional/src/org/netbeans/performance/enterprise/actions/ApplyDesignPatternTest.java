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
import org.netbeans.performance.enterprise.XMLSchemaComponentOperator;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author  mrkam@netbeans.org
 */
public class ApplyDesignPatternTest  extends PerformanceTestCase {
    
    private Node processNode, schemaNode;
    private String schemaName;
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public ApplyDesignPatternTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }
    
    /** Creates a new instance of SchemaNavigatorDesignView */
    public ApplyDesignPatternTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public void testApplyDesignPattern() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(ApplyDesignPatternTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    protected void initialize() {
        processNode = new EPUtilities().getProcessFilesNode("TravelReservationService");
        schemaName = "OTA_TravelItinerary.xsd";
        schemaNode = new Node(processNode, schemaName);
        String open = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        schemaNode.callPopup().pushMenuNoBlock(open);
        XMLSchemaComponentOperator.findXMLSchemaComponentOperator(schemaName);
    }
    
    public void prepare() {
    }
    
    public ComponentOperator open() {
        // "Apply Design Pattern...";
        String applyDesignPattern
                = org.netbeans.jellytools.Bundle.getStringTrimmed(
                "org.netbeans.modules.xml.schema.abe.wizard.Bundle", "TITLE_SchemaTransform");
        schemaNode.callPopup().pushMenuNoBlock(applyDesignPattern);
        applyDesignPattern = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.xml.schema.abe.wizard.Bundle", "TITLE_SchemaTransform");
        return new WizardOperator(applyDesignPattern);
    }
    
    @Override
    public void close() {
        closeAllModal();
    }
 
}
