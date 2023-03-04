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
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Save modified file.
 *
 * @author mmirilovic@netbeans.org
 */
public class SaveModifiedFileTest extends PerformanceTestCase {

    /**
     * Editor with opened file
     */
    public static EditorOperator editorOperator;

    /**
     * Creates a new instance of SaveModifiedFile
     *
     * @param testName the name of the test
     */
    public SaveModifiedFileTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of SaveModifiedFile
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public SaveModifiedFileTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(SaveModifiedFileTest.class)
                .suite();
    }

    public void testSaveModifiedFile() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        SourcePackagesNode spn = new SourcePackagesNode("PerformanceTestData");
        Node n = new Node(spn, "org.netbeans.test.performance|Main.java");
        new OpenAction().performAPI(n);
        editorOperator = new EditorOperator("Main.java");
        WAIT_AFTER_OPEN = 100;
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void prepare() {
        editorOperator.replace("DO NOT ALTER", "DO NOT ALTER");
        editorOperator.waitModified(true);
    }

    @Override
    public ComponentOperator open() {
        // wait only for saving and ignore other repaint events (badging etc.)
        MY_END_EVENT = ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE;
        new SaveAction().performShortcut(editorOperator);
        editorOperator.waitModified(false);
        return null;
    }
}
