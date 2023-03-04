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

package org.netbeans.performance.mobility.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.mobility.setup.MobilitySetup;

import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Create MIDlet
 *
 * @author  rashid@netbeans.org
 */
public class CreateMIDletTest extends PerformanceTestCase {

    private NewJavaFileNameLocationStepOperator location;
    private static String testProjectName = "MobileApplicationVisualMIDlet";

    /**
     * Creates a new instance of CreateVisualMIDlet
     * @param testName the name of the test
     */
    public CreateMIDletTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    /**
     * Creates a new instance of CreateMIDlet
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateMIDletTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(CreateMIDletTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCreateMIDlet() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }

    public void prepare() {
 
        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();

        // Workaround for issue 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        wizard.selectCategory("MIDP"); //NOI18N
        wizard.selectFileType("MIDlet"); //NOI18N
        wizard.next();
        location = new NewJavaFileNameLocationStepOperator();
        location.txtObjectName().setText("MIDlet_" + System.currentTimeMillis());
    }

    public ComponentOperator open() {
        location.finish();
        return null;
    }

    @Override
    protected void shutdown() {
        repaintManager().resetRegionFilters();
    }

}
