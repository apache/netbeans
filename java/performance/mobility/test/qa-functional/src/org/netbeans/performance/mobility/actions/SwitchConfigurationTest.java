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

import javax.swing.JComponent;

import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.mobility.setup.MobilitySetup;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mmirilovic@netbeans.org
 */
public class SwitchConfigurationTest extends PerformanceTestCase {

    private Node openNode;
    private ProjectRootNode projectNode;
    private String targetProject,  midletName;
    private WizardOperator propertiesWindow;
    private MIDletEditorOperator editor;
    private LoggingRepaintManager.RegionFilter filter;

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     */
    public SwitchConfigurationTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public SwitchConfigurationTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(SwitchConfigurationTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testSwitchConfiguration() {
        doMeasurement();
    }
    
    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        targetProject = "MobileApplicationSwitchConfiguration";
        midletName = "Midlet.java";
        String documentPath = CommonUtilities.SOURCE_PACKAGES + "|" + "switchit" + "|" + midletName;
        projectNode = new ProjectsTabOperator().getProjectRootNode(targetProject);
        openNode = new Node(projectNode, documentPath);

        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }

        new OpenAction().perform(openNode);

     /*   filter = new LoggingRepaintManager.RegionFilter() {
            boolean done = false;

            public boolean accept(JComponent comp) {
                if (done) {
                    return false;
                }
                if (comp.getClass().getName().equals("org.netbeans.modules.editor.errorstripe.AnnotationView")) {
                    done = true;
                    return false;
                }
                return true;
            }

            public String getFilterName() {
                return "Filters out all Regions starting with org.netbeans.modules.editor.errorstripe.AnnotationView";
            }
        };
        repaintManager().addRegionFilter(filter);*/
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
    }

    public void prepare() {
        editor = MIDletEditorOperator.findMIDletEditorOperator(midletName);
        projectNode.properties();
        propertiesWindow = new WizardOperator(targetProject);
    }

    public ComponentOperator open() {
        JComboBoxOperator combo = new JComboBoxOperator(propertiesWindow);
        combo.selectItem(1); // NotDefaultConfiguration
        propertiesWindow.ok();
        return null;
        //return MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }

    @Override
    public void close() {
        //repaintManager().resetRegionFilters();
        if (projectNode != null) {
            projectNode.properties();
            propertiesWindow = new WizardOperator(targetProject);
            JComboBoxOperator combo = new JComboBoxOperator(propertiesWindow, 0);
            combo.selectItem(0); //DefaultConfiguration
            propertiesWindow.ok();
        }
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        if (editor != null) {
            editor.close();
        }
    }

}
