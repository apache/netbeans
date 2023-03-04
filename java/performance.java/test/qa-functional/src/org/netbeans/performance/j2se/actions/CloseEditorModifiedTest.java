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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Closing Editor tabs.
 *
 * @author mmirilovic@netbeans.org
 */
public class CloseEditorModifiedTest extends PerformanceTestCase {

    /**
     * Editor with opened file
     */
    public static EditorOperator editorOperator;
    /**
     * Dialog with asking for Save
     */
    private static NbDialogOperator dialog;

    /**
     * Creates a new instance of CloseEditor
     *
     * @param testName the name of the test
     */
    public CloseEditorModifiedTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 100;
    }

    /**
     * Creates a new instance of CloseEditor
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseEditorModifiedTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 100;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(CloseEditorModifiedTest.class)
                .suite();
    }

    public void testCloseEditorModified() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        new OpenAction().performPopup(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main.java"));
        editorOperator = new EditorOperator("Main.java");
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }

    @Override
    public void prepare() {
        editorOperator.txtEditorPane().typeText("XXX");
    }

    @Override
    public ComponentOperator open() {
        new CloseViewAction().performMenu(editorOperator);
        dialog = new NbDialogOperator(org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.text.Bundle", "LBL_SaveFile_Title"));
        return dialog;
    }

    @Override
    public void close() {
        dialog.cancel();
    }
}
