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

package org.netbeans.performance.web.actions;

import junit.framework.Test;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of typing in opened source editor.
 *
 * @author anebuzelsky@netbeans.org
 */
public class TypingInJspEditorTest extends PerformanceTestCase {

    private String file;
    private int line;

    /**
     * Creates a new instance of TypingInEditor
     *
     * @param testName test name
     */
    public TypingInJspEditorTest(String testName) {
        super(testName);
        init();
    }

    /**
     * Creates a new instance of TypingInEditor
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public TypingInJspEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        init();
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(TypingInJspEditorTest.class)
                .suite();
    }

    private void init() {
        expectedTime = 400;
        WAIT_AFTER_OPEN = 100;
        line = 10;
    }

    private EditorOperator editorOperator;

    public void testTypingInJspEditor() {
        file = "Test.jsp";
        doMeasurement();
    }

    public void testTypingInJspEditorWithLargeFile() {
        file = "BigJSP.jsp";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"), "Web Pages|" + file));
        editorOperator = new EditorOperator(file);
        editorOperator.setCaretPositionToLine(line);
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        // measure two sub sequent key types (in fact time when first letter appears
        // in document and it is possible to type another letter)
        MY_START_EVENT = ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE;
        MY_END_EVENT = ActionTracker.TRACK_KEY_RELEASE;
        editorOperator.typeKey('a');
        editorOperator.typeKey('a');
        return null;
    }

    @Override
    public void close() {
        repaintManager().resetRegionFilters();

    }

    @Override
    protected void shutdown() {
        editorOperator.closeDiscard();
        super.shutdown();
        new ProjectsTabOperator().collapseAll();
    }
}
