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
import org.netbeans.jellytools.EditorWindowOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 * Test of Page Up and Page Down in opened source editor.
 *
 * @author anebuzelsky@netbeans.org
 */
public class PageUpPageDownInEditorTest extends PerformanceTestCase {

    private boolean pgup;
    private EditorOperator editorOperator;

    /**
     * Creates a new instance of PageUpPageDownInEditor
     *
     * @param testName test name
     */
    public PageUpPageDownInEditorTest(String testName) {
        super(testName);
        expectedTime = 100;
        WAIT_AFTER_OPEN = 200;
        pgup = true;
    }

    /**
     * Creates a new instance of PageUpPageDownInEditor
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public PageUpPageDownInEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 100;
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(PageUpPageDownInEditorTest.class)
                .suite();
    }

    public void testPageUp() {
        pgup = true;
        doMeasurement();
    }

    public void testPageDown() {
        pgup = false;
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main20kB.java"));
        editorOperator = EditorWindowOperator.getEditor("Main20kB.java");
    }

    @Override
    public void prepare() {
        if (pgup) {
            new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator);
        } else {
            editorOperator.setCaretPositionToLine(1);
        }
    }

    @Override
    public ComponentOperator open() {
        if (pgup) {
            new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0)).perform(editorOperator);
        } else {
            new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0)).perform(editorOperator);
        }
        return null;
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        repaintManager().resetRegionFilters();
    }
}
