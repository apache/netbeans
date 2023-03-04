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

package org.netbeans.performance.mobility.dialogs;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.mobility.setup.MobilitySetup;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Close Project Property
 *
 * @author  rashid@netbeans.org
 */
public class CloseProjectPropertyTest extends PerformanceTestCase {

    private NbDialogOperator jdo ;
    private static String testProjectName = "MobileApplicationVisualMIDlet";  
    
    /**
     * Creates a new instance of CloseProjectProperty
     * @param testName the name of the test
     */
    public CloseProjectPropertyTest(String testName) {
        super(testName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN=2000;
    }
    
    /**
     * Creates a new instance of CloseProjectProperty
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseProjectPropertyTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN=2000;
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(CloseProjectPropertyTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCloseProjectProperty() {
        doMeasurement();
    }

    @Override
    public void initialize(){
       repaintManager().addRegionFilter(repaintManager().IGNORE_STATUS_LINE_FILTER);
    }
    
    public void prepare(){
  
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();
        pNode.performPopupAction("Properties");
         
       jdo = new NbDialogOperator(testProjectName);
       JTreeOperator cattree = new JTreeOperator(jdo);       
       Node cNode = new Node(cattree,"Abilities") ;
       cNode.select();
        
       JButtonOperator addButton = new JButtonOperator(jdo,"Add");
       addButton.pushNoBlock();

       NbDialogOperator add_abil = new NbDialogOperator("Add Ability");
       JComboBoxOperator abilityCombo = new JComboBoxOperator(add_abil); 
       abilityCombo.clearText();
       abilityCombo.typeText("Ability_"+System.currentTimeMillis());
       JButtonOperator abil_okButton = new JButtonOperator(add_abil,"OK");
       abil_okButton.push();
       
    }
    
    public ComponentOperator open(){
       JButtonOperator okButton = new JButtonOperator(jdo,"OK");
       okButton.push();
       return null;
    }
    
    @Override
    public void close(){
    }

    public void shutdown() {
        repaintManager().resetRegionFilters();
    }

}
