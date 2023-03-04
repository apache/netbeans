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

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of shift code in opened source editor.
 *
 * @author anebuzelsky@netbeans.org
 */
public class ShiftCodeInEditorTest extends PerformanceTestCase {

    private int line;
    private EditorOperator editorOperator;

    /**
     * Creates a new instance of PageUpPageDownInEditor
     *
     * @param testName test name
     */
    public ShiftCodeInEditorTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    /**
     * Creates a new instance of PageUpPageDownInEditor
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ShiftCodeInEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(ShiftCodeInEditorTest.class)
                .suite();
    }

    public void testShiftCode1Line() {
        line = 1;
        doMeasurement();
    }

    public void testShiftCode100Lines() {
        line = 100;
        doMeasurement();
    }

    public void testShiftCode1000Lines() {
        line = 1000;
        doMeasurement();
    }

    @Override
    public void initialize() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|BigJavaFile.java"));
        editorOperator = new EditorOperator("BigJavaFile.java");
    }

    @Override
    public void prepare() {
        editorOperator.select(1, line);
    }

    @Override
    public ComponentOperator open() {
        new Action("Source|Shift Right", null).perform(editorOperator);
        editorOperator.clickMouse();
        return null;
    }

    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }
}
