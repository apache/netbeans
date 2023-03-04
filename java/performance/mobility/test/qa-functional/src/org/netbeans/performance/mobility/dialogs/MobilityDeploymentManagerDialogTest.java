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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class MobilityDeploymentManagerDialogTest extends  PerformanceTestCase {

    private NbDialogOperator manager;
    protected String cmdName, wzdName;
    private String toolsMenuPath;

    /**
     * Creates a new instance of MobilityDeploymentManagerDialog
     * @param testName the name of the test
     */
    public MobilityDeploymentManagerDialogTest(String testName) {
        super(testName);
        cmdName = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.deployment.Bundle", "Title_DeploymentManager");        
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of MobilityDeploymentManagerDialog
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public MobilityDeploymentManagerDialogTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        cmdName = Bundle.getStringTrimmed("org.netbeans.modules.mobility.project.deployment.Bundle", "Title_DeploymentManager");
        expectedTime = WINDOW_OPEN;
    }
    
        public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(MobilityDeploymentManagerDialogTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testMobilityDeploymentManagerDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        toolsMenuPath = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools") + "|";
        if (wzdName == null) wzdName = cmdName;
    }

    public void prepare() {
    }

    public ComponentOperator open() {
        new ActionNoBlock(toolsMenuPath+cmdName,null).performMenu();
        manager = new NbDialogOperator(wzdName);
        return null;
    }

    @Override
    public void close() {
        if (manager != null ) {
            manager.close();
        }
    }

}
