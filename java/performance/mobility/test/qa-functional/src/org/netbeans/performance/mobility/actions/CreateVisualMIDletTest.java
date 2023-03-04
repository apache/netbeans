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

import java.awt.Container;
import javax.swing.JComponent;

import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.mobility.setup.MobilitySetup;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;

import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.actions.CloseAllDocumentsAction;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 * Test Create Visual MIDlet
 *
 * @author  rashid@netbeans.org, mrkam@netbeans.org
 */
public class CreateVisualMIDletTest extends PerformanceTestCase {

    private NewJavaFileNameLocationStepOperator location;
    private static String testProjectName = "MobileApplicationVisualMIDlet";
    private String midletName;

    /**
     * Creates a new instance of CreateVisualMIDlet
     * @param testName the name of the test
     */
    public CreateVisualMIDletTest(String testName) {
        super(testName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 5000;
    }

    /**
     * Creates a new instance of CreateVisualMIDlet
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateVisualMIDletTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN = 5000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(CreateVisualMIDletTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testCreateVisualMIDlet() {
        doMeasurement();
    }

    @Override
    public void initialize() {

        new CloseAllDocumentsAction().performAPI();

        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_EXPLORER_TREE_FILTER);
        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {

            public boolean accept(JComponent c) {
                Container cont = c;
                do {
                    if ("org.netbeans.modules.palette.ui.PalettePanel".equals(cont.getClass().getName())) {
                        return false;
                    }
                    cont = cont.getParent();
                } while (cont != null);
                return true;
            }

            public String getFilterName() {
                return "Filters out PalettePanel";
            }
        });
    }

    public void prepare() {

        Node pNode = new ProjectsTabOperator().getProjectRootNode(testProjectName);
        pNode.select();

        // Workaround for issue 143497
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        NewFileWizardOperator wizard = NewFileWizardOperator.invoke();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        wizard.selectCategory("MIDP"); //NOI18N
        wizard.selectFileType("Visual MIDlet"); //NOI18N
        wizard.next();
        location = new NewJavaFileNameLocationStepOperator();
        midletName = "VisualMIDlet_" + System.currentTimeMillis();
        location.txtObjectName().setText(midletName);
    }

    public ComponentOperator open() {
        location.finish();
        return new TopComponentOperator(midletName + ".java");
    }

    @Override
    public void close() {
        new Thread("Question dialog discarder") {
            @Override
            public void run() {
                try {
                    new JButtonOperator(new JDialogOperator("Question"), "Discard").push();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        if (midletName != null) {
            MIDletEditorOperator.findMIDletEditorOperator(midletName + ".java").close();
        }
    }

    public void shutdown() {
        repaintManager().resetRegionFilters();
    }
}
