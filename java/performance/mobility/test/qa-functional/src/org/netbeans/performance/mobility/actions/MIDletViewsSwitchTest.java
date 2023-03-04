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

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.mobility.setup.MobilitySetup;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class MIDletViewsSwitchTest extends PerformanceTestCase {

    public String fromView;
    public String toView;
    private String targetProject;
    private String midletName;
    private Node openNode;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    private MIDletEditorOperator targetMIDletEditor;

    public MIDletViewsSwitchTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public MIDletViewsSwitchTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=2000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(MIDletViewsSwitchTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    @Override
    public void initialize() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";

        String documentPath = CommonUtilities.SOURCE_PACKAGES + "|" + "allComponents" + "|" + midletName;
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode(targetProject), documentPath);

        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        openNode.select();

        JPopupMenuOperator popup = this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }
        targetMIDletEditor = MIDletEditorOperator.findMIDletEditorOperator(midletName);

        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {
            public boolean accept(JComponent c) {
                Container cont = c;
                do {
                    if ("o.n.m.vmd.io.editor.EditorTopComponent".equals(cont.getClass().getName())) {
                        return true;
                    }
                    cont = cont.getParent();
                } while (cont != null);
                return false;
            }

            public String getFilterName() {
                return "Filter for MobilityEditor";
            }
        });


         repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {
            public boolean accept(JComponent c) {
            String cn = c.getClass().getName();

                Container cont = c;
                do {
                  cn = cont.getName();
                if ("JScrollPane".equalsIgnoreCase(cn)) {
                    return false;
                }
                cont = cont.getParent();

                } while (cont != null);
                return false;
            }

            public String getFilterName() {
                return "no scrolls";
            }
        });

    }

    public void prepare() {
        targetMIDletEditor.switchToViewByName(fromView);
    }

    public ComponentOperator open() {
        targetMIDletEditor.switchToViewByName(toView);
        return null;
    }

    @Override
    public void close() {
    }

    public void shutdown() {
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        repaintManager().resetRegionFilters();
    }

    public void testFlowToDesignSwitch() {
        fromView = "Flow";
        toView = "Screen";
        doMeasurement();
    }

    public void testDesignToFlowSwitch() {
        fromView = "Screen";
        toView = "Flow";
        doMeasurement();
    }

    public void testFlowToSourceSwitch() {
        fromView = "Flow";
        toView = "Source";
        doMeasurement();
    }

    public void testSourceToFlowSwitch() {
        fromView = "Source";
        toView = "Flow";
        doMeasurement();
    }

    public void testSourceToAnalyzeSwitch() {
        fromView = "Source";
        toView = "Analyze";
        doMeasurement();
    }

    public void testAnalyzeToSourceSwitch() {
        fromView = "Analyze";
        toView = "Source";
        doMeasurement();
    }

}
