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
package org.netbeans.performance.j2ee.actions;

import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.j2ee.setup.J2EESetup;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of finishing dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class MeasureSessionBeanActionTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private static NbDialogOperator dialog;
    private String name;

    /**
     * Creates a new instance of MeasureSessionBeanActionTest
     *
     * @param testName
     */
    public MeasureSessionBeanActionTest(String testName) {
        super(testName);
        expectedTime = 2000;
    }

    /**
     * Creates a new instance of MeasureSessionBeanActionTest
     *
     * @param testName
     * @param performanceDataName
     */
    public MeasureSessionBeanActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(MeasureSessionBeanActionTest.class).suite();
    }

    public void testAddBusinessMethod() {
        WAIT_AFTER_OPEN = 1000;
        doMeasurement();
    }

    @Override
    public void initialize() {
        // open a java file in the editor
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestSessionSB");
        new OpenAction().performAPI(openFile);
        editor = new EditorOperator("TestSessionBean.java");
    }

    public void prepare() {
        Node beanNode = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestSessionSB");
        new ActionNoBlock(null, "Add|Business Method...").perform(beanNode);
        dialog = new NbDialogOperator("Business Method...");
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        name = "testBusinessMethod" + CommonUtilities.getTimeIndex();
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(name);
    }

    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        dialog.ok();
        editor.txtEditorPane().waitText(name);
        return null;
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        editor.closeDiscard();
    }
}
