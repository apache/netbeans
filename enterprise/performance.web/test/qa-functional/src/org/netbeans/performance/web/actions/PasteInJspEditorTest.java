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
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of Paste text to opened source editor.
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class PasteInJspEditorTest extends PerformanceTestCase {

    private String file;
    private EditorOperator editorOperator1, editorOperator2;

    /**
     * Creates a new instance of PasteInEditor
     *
     * @param testName test name
     */
    public PasteInJspEditorTest(String testName) {
        super(testName);
        init();
    }

    /**
     * Creates a new instance of PasteInEditor
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public PasteInJspEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        init();
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(PasteInJspEditorTest.class)
                .suite();
    }

    private void init() {
        expectedTime = 1300;
        WAIT_AFTER_OPEN = 2000;
    }

    public void testPasteInJspEditor() {
        file = "Test.jsp";
        doMeasurement();
    }

    public void testPasteInJspEditorWithLargeFile() {
        file = "BigJSP.jsp";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        EditorOperator.closeDiscardAll();

        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"), "Web Pages|Test.jsp"));
        editorOperator1 = new EditorOperator("Test.jsp");
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"), "Web Pages|" + file));
        editorOperator2 = new EditorOperator(file);
        editorOperator1.makeComponentVisible();
        editorOperator1.select(12, 18);
        new CopyAction().perform();
        editorOperator2.makeComponentVisible();
        editorOperator2.setCaretPositionToLine(1);
        new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator2);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK)).perform(editorOperator2);
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    protected void shutdown() {
        super.shutdown();
        repaintManager().resetRegionFilters();
        editorOperator1.closeDiscard();
        editorOperator2.closeDiscard();
    }
}
