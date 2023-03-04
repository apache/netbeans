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
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of finishing dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class MeasureEntityBeanActionTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private static NbDialogOperator dialog;

    private String popup_menu;
    private String title;
    private String name;
    private Node beanNode;

    /**
     * Creates a new instance of MeasureEntityBeanActionTest
     *
     * @param testName
     */
    public MeasureEntityBeanActionTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of MeasureEntityBeanActionTest
     *
     * @param testName
     * @param performanceDataName
     */
    public MeasureEntityBeanActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(MeasureEntityBeanActionTest.class).suite();
    }

    public void testAddBusinessMethod() {
        WAIT_AFTER_OPEN = 2000;
        expectedTime = 2000;
        popup_menu = "Add|Business Method";
        title = "Business Method";
        name = "testBusinessMethod";
        doMeasurement();
    }

    public void testAddSelectMethod() {
        WAIT_AFTER_OPEN = 1000;
        popup_menu = "Add|Select Method";
        title = "Select Method";
        name = "ejbSelectByTest";
        doMeasurement();
    }

    @Override
    public void initialize() {
        // open a java file in the editor
        beanNode = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestEntityEB");
        final ActionNoBlock action = new ActionNoBlock(null, popup_menu);
        try {
            new Waiter(new Waitable() {

                @Override
                public Object actionProduced(Object param) {
                    return action.isEnabled(beanNode) ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return "wait menu is enabled";
                }
            }).waitAction(null);
        } catch (InterruptedException e) {
            throw new JemmyException("Interrupted.", e);
        }
        new OpenAction().performAPI(beanNode);
        editor = new EditorOperator("TestEntityBean.java");
    }

    @Override
    public void prepare() {
        new ActionNoBlock(null, popup_menu).perform(beanNode);
        dialog = new NbDialogOperator(title);
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        name += CommonUtilities.getTimeIndex();
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(name);
    }

    @Override
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
