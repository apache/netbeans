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
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of java completion in opened source editor.
 *
 * @author anebuzelsky@netbeans.org, mmirilovic@netbeans.org
 */
public class JSPCompletionInJspEditorTest extends PerformanceTestCase {

    private String text;
    private EditorOperator editorOperator;

    protected LoggingRepaintManager.RegionFilter COMPLETION_DIALOG_FILTER
            = new LoggingRepaintManager.RegionFilter() {
                @Override
                public boolean accept(JComponent comp) {
                    return comp.getClass().getName().startsWith("org.netbeans.editor.ext.");
                }

                @Override
                public String getFilterName() {
                    return "Completion Dialog Filter (accepts only componenets from"
                    + "'org.netbeans.editor.ext.**' packages";
                }
            };

    /**
     * Creates a new instance of JavaCompletionInEditor
     *
     * @param testName test name
     */
    public JSPCompletionInJspEditorTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of JavaCompletionInEditor
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public JSPCompletionInJspEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(JSPCompletionInJspEditorTest.class)
                .suite();
    }

    public void testScriptletCC() {
        text = "<%";
        measureTime();
    }

    public void testExpressionCC() {
        text = "<%= request.";
        measureTime();
    }

    public void testDeclarationCC() {
        text = "<%! java.";
        measureTime();
    }

    public void testAllTags() {
        text = "<";
        measureTime();
    }

    public void testTagAttribute1() {
        text = "<%@page ";
        measureTime();
    }

    public void testTagAttribute2() {
        text = "<jsp:useBean ";
        measureTime();
    }

    public void testAttributeValue1() {
        text = "<%@page import=\"";
        measureTime();
    }

    public void testAttributeValue2() {
        text = "<%@include file=\"";
        measureTime();
    }

    public void testAttributeValue3() {
        text = "<jsp:useBean id=\"bean\" scope=\"";
        measureTime();
    }

    public void testAttributeValue4() {
        text = "<jsp:useBean id=\"beanInstanceName\" scope=\"session\" class=\"";
        measureTime();
    }

    public void testAttributeValue5() {
        text = "<jsp:getProperty name=\"bean\" property=\"";
        measureTime();
    }

    public void testAttributeValue6() {
        text = "<%@taglib prefix=\"d\" tagdir=\"";
        measureTime();
    }

    @Override
    protected void initialize() {
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().
                getProjectRootNode("TestWebProject"), "Web Pages|index.jsp"));
        editorOperator = EditorWindowOperator.getEditor("index.jsp");
    }

    @Override
    public void prepare() {
        // scroll to the place where we start
        editorOperator.makeComponentVisible();
        clearTestLine();
        editorOperator.setCaretPositionToLine(8);
        // insert the initial text
        editorOperator.insert(text);
        // wait
        waitNoEvent(500);
    }

    @Override
    public ComponentOperator open() {
        KeyStroke ctrlSpace = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK);
        repaintManager().addRegionFilter(COMPLETION_DIALOG_FILTER);
        // invoke the completion dialog
        new ActionNoBlock(null, null, ctrlSpace).perform(editorOperator);
        return null;
    }

    @Override
    public void close() {
        repaintManager().resetRegionFilters();
        new ActionNoBlock(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)).perform(editorOperator);
        clearTestLine();
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        editorOperator.closeDiscard();
    }

    private void clearTestLine() {
        int linelength = editorOperator.getText(8).length();
        if (linelength > 1) {
            editorOperator.delete(8, 1, linelength - 1);
        }
    }
}
