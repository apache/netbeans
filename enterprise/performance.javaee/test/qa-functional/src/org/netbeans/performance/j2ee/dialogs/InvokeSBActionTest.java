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

import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of dialogs from EJB source editor.
 *
 * @author lmartinek@netbeans.org
 */
public class InvokeSBActionTest extends PerformanceTestCase {

    private static EditorOperator editor;
    private JDialogOperator jdo;
    private Node openFile;
    private int listItem = 0;
    private String dialogTitle = null;

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     */
    public InvokeSBActionTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    /**
     * Creates a new instance of InvokeEJBActionTest
     * @param testName
     * @param performanceDataName
     */
    public InvokeSBActionTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(InvokeSBActionTest.class).suite();
    }

    public void testAddBusinessMethodDialogInSB() {
        dialogTitle = "Add Business Method";
        listItem = 0;
        doMeasurement();
    }

    public void testConstructorDialogInSB() {
        dialogTitle = "Generate Constructor";
        listItem = 1;
        doMeasurement();
    }

    public void testAddGetterSetterDialogInSB() {
        dialogTitle = "Generate Getters and Setters";
        listItem = 5;
        doMeasurement();
    }

    public void testEqualsAndHashDialogInSB() {
        dialogTitle = "Generate Equals";
        listItem = 6;
        doMeasurement();
    }

    public void testToStringDialogInSB() {
        dialogTitle = "Generate toString";
        listItem = 7;
        doMeasurement();
    }

    public void testDelegateDialogInSB() {
        dialogTitle = "Generate Delegate";
        listItem = 8;
        expectedTime = 1500;
        doMeasurement();
    }

    public void testOverrideDialogInSB() {
        dialogTitle = "Generate Override";
        listItem = 9;
        doMeasurement();
    }

    public void testAddPropertyDialogInSB() {
        dialogTitle = "Add Property";
        listItem = 10;
        doMeasurement();
    }

    public void testCallEnterpriseBeanDialogInSB() {
        dialogTitle = "Call Enterprise Bean";
        listItem = 11;
        doMeasurement();
    }

    public void testSendEmailDialogInSB() {
        dialogTitle = "Specify Mail Resource";
        listItem = 14;
        doMeasurement();
    }

    public void testCallWebServiceDialogInSB() {
        dialogTitle = "Select Operation";
        listItem = 15;
        doMeasurement();
    }

    public void testGenerateRESTDialogInSB() {
        dialogTitle = "Available REST";
        listItem = 16;
        doMeasurement();
    }

    public void initialize() {
        openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-ejb"), "Source Packages|test|TestSessionBean");
        new OpenAction().performAPI(openFile);
        editor = new EditorOperator("TestSessionBean.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
    }

    public void prepare() {
        editor.setCaretPosition(105, 1);
        editor.pushKey(java.awt.event.KeyEvent.VK_INSERT, java.awt.event.KeyEvent.ALT_MASK);
        jdo = new JDialogOperator();
        JListOperator list = new JListOperator(jdo);
        list.setSelectedIndex(listItem);
    }

    public ComponentOperator open() {
        jdo.pushKey(KeyEvent.VK_ENTER);
        return null;
    }

    public void close() {
        new NbDialogOperator(dialogTitle).cancel();
    }

    public void shutdown() {
        editor.closeDiscard();
    }

}
