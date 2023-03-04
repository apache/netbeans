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

import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.enterprise.setup.EnterpriseSetup;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
//import org.netbeans.jellytools.actions.BuildProjectAction;
//import org.netbeans.jellytools.actions.CleanProjectAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Build Complex Project
 *
 * @author  rashid@netbeans.org, mmirilovic@netbeans.org
 */
public class BuildComplexProjectTest extends PerformanceTestCase {
    private String project_name = "TravelReservationServiceApplication";
    private Node projectNode;
    
    /**
     * Creates a new instance of Build Complex Project
     * @param testName the name of the test
     */
    public BuildComplexProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 10000;
        //MY_END_EVENT = ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE;
    }
    
    /**
     * Creates a new instance of Build Complex Project
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public BuildComplexProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 10000;
        //MY_END_EVENT = ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE;
    }

    public void testBuildComplexProject() {
        doMeasurement();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(EnterpriseSetup.class)
             .addTest(BuildComplexProjectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void close() {
        if (projectNode != null) {
//            new CleanProjectAction().performPopup(projectNode);
            MainWindowOperator.getDefault().waitStatusText("Finished building"); // NOI18N
        }
        projectNode = null;
    }
    
    public void prepare(){
    }
    
    public ComponentOperator open(){
        projectNode = new ProjectsTabOperator().getProjectRootNode(project_name);
//        new BuildProjectAction().performPopup(projectNode);
        Timeouts temp = JemmyProperties.getProperties().getTimeouts().cloneThis();
        JemmyProperties.getProperties().getTimeouts().setTimeout("Waiter.WaitingTime", expectedTime * 5);
        MainWindowOperator.getDefault().waitStatusText("Finished building build.xml (jbi-build)"); // NOI18N
        JemmyProperties.setCurrentTimeouts(temp);
        return null;
    }
 
}
