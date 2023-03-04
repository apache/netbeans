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
package org.netbeans.performance.j2ee.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class InvokeEJBActionTest extends PerformanceTestCase {

    private JPopupMenuOperator jmpo;
    private Node openFile;
    private String popupMenu = null;
    private String dialogTitle = null;

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     */
    public InvokeEJBActionTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     * @param performanceDataName
     */
    public InvokeEJBActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(InvokeEJBActionTest.class).suite();
    }

    public void testAddBusinessMethodDialogInEJB() {
        popupMenu = "Add|Business Method";
        dialogTitle = "Business Method";
        doMeasurement();
    }

    public void testCreateMethodDialogInEJB() {
        popupMenu = "Add|Create Method";
        dialogTitle = "Create Method";
        doMeasurement();
    }

    public void testFinderMethodDialogInEJB() {
        popupMenu = "Add|Finder Method";
        dialogTitle = "Finder Method";
        doMeasurement();
    }

    public void testHomeMethodDialogInEJB() {
        popupMenu = "Add|Home Method";
        dialogTitle = "Home Method";
        doMeasurement();
    }

    public void testSelectMethodDialogInEJB() {
        popupMenu = "Add|Select Method";
        dialogTitle = "Select Method";
        doMeasurement();
    }

    @Override
    public void initialize() {
        openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Enterprise Beans|TestEntityEB");
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
    }

    public void prepare() {
        jmpo = openFile.callPopup();
        // do nothing
    }

    public ComponentOperator open() {
        jmpo.pushMenu(popupMenu);
        return new NbDialogOperator(dialogTitle);
    }

    @Override
    public void shutdown() {
    }
}
