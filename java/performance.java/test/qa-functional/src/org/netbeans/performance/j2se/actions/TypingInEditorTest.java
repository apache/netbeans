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
package org.netbeans.performance.j2se.actions;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of typing in opened source editor.
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class TypingInEditorTest extends PerformanceTestCase {

    private EditorOperator editorOperator;
    protected String fileName;
    protected int caretPositionX, caretPositionY;
    Node fileToBeOpened;
    JEditorPaneOperator epo;
    Robot r;
    private int keyCode = KeyEvent.VK_SPACE;
    private int repeatTimes = 1;

    /**
     * Creates a new instance of TypingInEditor
     *
     * @param testName test name
     */
    public TypingInEditorTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Creates a new instance of TypingInEditor
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public TypingInEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(TypingInEditorTest.class)
                .suite();
    }

    public void testTxtEditor() {
        expectedTime = 100;
        fileName = "textfile.txt";
        caretPositionX = 9;
        caretPositionY = 1;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
    }

    public void testTxtEditor10() {
        expectedTime = 300;
        fileName = "textfile.txt";
        repeatTimes = 10;
        caretPositionX = 9;
        caretPositionY = 1;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
        repeatTimes = 1;
    }

    public void testJavaEditor() {
        expectedTime = 100;
        fileName = "Main.java";
        caretPositionX = 9;
        caretPositionY = 1;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
    }

    public void testJavaEditor10() {
        expectedTime = 300;
        repeatTimes = 10;
        fileName = "Main.java";
        caretPositionX = 9;
        caretPositionY = 1;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
        repeatTimes = 1;
    }

    public void testJavaEditor10Enter() {
        expectedTime = 400;
        keyCode = KeyEvent.VK_ENTER;
        repeatTimes = 10;
        fileName = "Main.java";
        caretPositionX = 9;
        caretPositionY = 1;
        fileToBeOpened = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + fileName);
        doMeasurement();
        keyCode = KeyEvent.VK_SPACE;
        repeatTimes = 1;
    }

    @Override
    public void initialize() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = new EditorOperator(fileName);
        editorOperator.setCaretPosition(caretPositionX, caretPositionY);
    }

    @Override
    public void prepare() {
        try {
            r = new Robot();
        } catch (AWTException e) {
            fail(e);
        }
    }

    @Override
    public ComponentOperator open() {
        // measure typing events and do not take into account repaint events
        // in editor afterwards because they are asynchronous
        MY_START_EVENT = ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE;
        MY_END_EVENT = ActionTracker.TRACK_KEY_RELEASE;
        for (int i = 0; i < repeatTimes; i++) {
            r.keyPress(keyCode);
            r.keyRelease(keyCode);
        }
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        editorOperator.closeDiscard();
        new ProjectsTabOperator().collapseAll();
    }
}
