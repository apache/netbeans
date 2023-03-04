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

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of Paste text to opened source editor.
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class PasteInEditorTest extends PerformanceTestCase {

    private EditorOperator editorOperator1, editorOperator2;

    /**
     * Creates a new instance of PasteInEditor
     *
     * @param testName test name
     */
    public PasteInEditorTest(String testName) {
        super(testName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN = 400;
    }

    /**
     * Creates a new instance of PasteInEditor
     *
     * @param testName test name
     * @param performanceDataName perf name
     */
    public PasteInEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN = 400;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(PasteInEditorTest.class)
                .suite();
    }

    public void testPasteInEditor() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("PerformanceTestData");
        new OpenAction().performAPI(new Node(sourcePackagesNode, "org.netbeans.test.performance|TestClassForCopyPaste.java"));
        editorOperator1 = new EditorOperator("TestClassForCopyPaste.java");
        editorOperator1.makeComponentVisible();
        int start = editorOperator1.txtEditorPane().getPositionByText("private int newField1;");
        int end = editorOperator1.txtEditorPane().getPositionByText("private int newField10;");
        editorOperator1.txtEditorPane().select(start, end);
        editorOperator1.txtEditorPane().copy();
    }

    @Override
    public void prepare() {
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("PerformanceTestData");
        new OpenAction().performAPI(new Node(sourcePackagesNode, "org.netbeans.test.performance|Main20kB.java"));
        editorOperator2 = new EditorOperator("Main20kB.java");
        editorOperator2.setCaretPosition("Main20kB {", false);
        waitScanFinished();
    }

    @Override
    public ComponentOperator open() {
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK)).perform(editorOperator2);
        return null;
    }

    @Override
    public void close() {
        editorOperator2.closeDiscard();
    }

    @Override
    public void shutdown() {
        editorOperator1.closeDiscard();
        repaintManager().resetRegionFilters();
    }
}
