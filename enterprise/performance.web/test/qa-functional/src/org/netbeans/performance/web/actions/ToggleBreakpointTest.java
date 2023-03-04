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

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of Paste text to opened source editor.
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class ToggleBreakpointTest extends PerformanceTestCase {

    private String file;
    private int line;
    private EditorOperator editorOperator;

    /**
     * Creates a new instance of ToggleBreakpointTest
     *
     * @param testName test name
     */
    public ToggleBreakpointTest(String testName) {
        super(testName);
        init();
    }

    /**
     * Creates a new instance of ToggleBreakpointTest
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ToggleBreakpointTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        init();
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(ToggleBreakpointTest.class)
                .suite();
    }

    private void init() {
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
        line = 99;
        file = "Test.jsp";
    }

    public void testToggleBreakpoint() {
        doMeasurement();
    }

    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"), "Web Pages|" + file));
        editorOperator = new EditorOperator(file);

    }

    @Override
    public void prepare() {
        editorOperator.setCaretPositionToLine(line);
        line--;
    }

    @Override
    public ComponentOperator open() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_MASK)).perform(editorOperator);
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    protected void shutdown() {
        repaintManager().resetRegionFilters();
        super.shutdown();
    }
}
