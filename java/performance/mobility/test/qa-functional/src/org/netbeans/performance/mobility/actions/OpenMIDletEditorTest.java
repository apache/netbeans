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
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.mobility.setup.MobilitySetup;
import org.netbeans.performance.mobility.window.MIDletEditorOperator;

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class OpenMIDletEditorTest extends PerformanceTestCase {

    private Node openNode;
    private String targetProject;
    private String midletName;
    private ProjectsTabOperator pto;
    public static final long EXPECTED_TIME = 10000;
    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     */
    public OpenMIDletEditorTest(String testName) {
        super(testName);
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 20000;
    }

    /**
     * Creates a new instance of OpenMIDletEditor
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenMIDletEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        targetProject = "MobileApplicationVisualMIDlet";
        midletName = "VisualMIDletMIDP20.java";
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 20000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(MobilitySetup.class)
             .addTest(OpenMIDletEditorTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

    public void testOpenMIDletEditor() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        pto = ProjectsTabOperator.invoke();
    }

    public void prepare() {
        String documentPath = CommonUtilities.SOURCE_PACKAGES + "|" + "allComponents" + "|" + midletName;
        long nodeTimeout = pto.getTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");

        try {
            openNode = new Node(pto.getProjectRootNode(targetProject), documentPath);
        } catch (TimeoutExpiredException ex) {
            pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", nodeTimeout);
            throw new Error("Cannot find expected node because of Timeout");
        }
        pto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", nodeTimeout);

        if (this.openNode == null) {
            throw new Error("Cannot find expected node ");
        }
        openNode.select();
    }

    public ComponentOperator open() {
        JPopupMenuOperator popup = this.openNode.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node ");
        }
        try {
            popup.pushMenu(OPEN);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            throw new Error("Cannot push menu item ");
        }

        return MIDletEditorOperator.findMIDletEditorOperator(midletName);
    }

    @Override
    public void close() {
        if (testedComponentOperator != null) {
            new Thread("Question dialog discarder") {

                @Override
                public void run() {
                    try {
                        new JButtonOperator(new JDialogOperator("Question"), "Discard").push();
                    } catch (Exception e) {
                        //  There is no need to care about this exception as this dialog is optional
                        e.printStackTrace();
                    }
                }
            }.start();
            ((MIDletEditorOperator) testedComponentOperator).close();
        }
    }

}
