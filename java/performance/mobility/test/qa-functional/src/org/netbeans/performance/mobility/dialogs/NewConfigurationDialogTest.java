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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Administrator
 */
public class NewConfigurationDialogTest  extends PerformanceTestCase {

    private Node testNode;    
    private String targetProject;
    
    public NewConfigurationDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        targetProject = "MobileApplicationVisualMIDlet";              
    }

    public NewConfigurationDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        targetProject = "MobileApplicationVisualMIDlet";              
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(NewConfigurationDialogTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testNewConfigurationDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        String projectConfNodeName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.mobility.project.ui.Bundle", "LBL_ProjectConfigurations");
        testNode = new Node(new ProjectsTabOperator().getProjectRootNode(targetProject),projectConfNodeName);
        testNode.select();
    }

    public void prepare() {
        testNode.select();
    }

    public ComponentOperator open() {
        String cmdName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.mobility.project.ui.customizer.Bundle", "LBL_VCS_AddConfiguration");
        new ActionNoBlock(null,cmdName).performPopup(testNode);
        return new NbDialogOperator(cmdName);
    }
    
    @Override
    public void close() {
        if(testedComponentOperator != null) {
            ((NbDialogOperator)testedComponentOperator).close();
        }
    }

}
