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

import java.io.IOException;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CloseAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.mobility.setup.MobilitySetup;
import org.openide.util.Exceptions;

/**
 * Test Open Mobile project
 *
 * @author  rashid@netbeans.org
 */
public class OpenMobileProjectTest extends PerformanceTestCase {

    private static String projectName = "MobileApplicationVisualMIDlet";
    private JButtonOperator openButton;
    protected static ProjectsTabOperator projectsTab = null;

    /**
     * Creates a new instance of OpenMobileProject
     * @param testName the name of the test
     */
    public OpenMobileProjectTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    /**
     * Creates a new instance of OpenMobileProject
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenMobileProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 4000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(OpenMobileProjectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testOpenMobileProject() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        new CloseAction().perform(getProjectNode(projectName));
    }

    public void prepare() {
        new ActionNoBlock("File|Open Project...", null).perform(); //NOI18N
        WizardOperator opd = new WizardOperator("Open Project"); //NOI18N
        JTextComponentOperator path = new JTextComponentOperator(opd, 1);
        openButton = new JButtonOperator(opd, "Open Project"); //NOI18N
        String paths = getDataDir().toString()+ java.io.File.separator + projectName;
        path.setText(paths);
    }

    public ComponentOperator open() {
        openButton.pushNoBlock();
        return null;
    }

    @Override
    public void close() {
        new CloseAction().perform(getProjectNode(projectName));
    }

    public void shutdown() {
        try {
            this.openDataProjects(projectName);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

}
