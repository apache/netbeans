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
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Closing All Documents.
 *
 * @author mmirilovic@netbeans.org
 */
public class CloseAllEditorsTest extends PerformanceTestCase {

    /**
     * Nodes represent files to be opened
     */
    private static Node[] openFileNodes;
    private EditorOperator editor;

    /**
     * Creates a new instance of CloseAllEditors
     *
     * @param testName the name of the test
     */
    public CloseAllEditorsTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 2000;
    }

    /**
     * Creates a new instance of CloseAllEditors
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseAllEditorsTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 2000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(CloseAllEditorsTest.class)
                .suite();
    }

    public void testCloseAllEditors() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        prepareFiles();
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void prepare() {
        new OpenAction().performAPI(openFileNodes);
        editor = new EditorOperator("SampleJavaClass000.java");
    }

    @Override
    public ComponentOperator open() {
        editor.pushKey(java.awt.event.KeyEvent.VK_W, java.awt.event.KeyEvent.SHIFT_MASK + java.awt.event.KeyEvent.CTRL_MASK);
        return null;
    }

    /**
     * Prepare ten selected file from project
     */
    protected void prepareFiles() {
        String[][] files_path = getTenSelectedFiles();
        openFileNodes = new Node[files_path.length];
        SourcePackagesNode sourcePackagesNode = new SourcePackagesNode("PerformanceTestFoldersData");
        for (int i = 0; i < files_path.length; i++) {
            openFileNodes[i] = new Node(sourcePackagesNode, files_path[i][0] + '|' + files_path[i][1]);
        }
    }

    private static String[][] getTenSelectedFiles() {
        String[][] files_path = {
            {"folders.javaFolder100", "SampleJavaClass099.java"},
            {"folders.javaFolder100", "SampleJavaClass098.java"},
            {"folders.javaFolder100", "SampleJavaClass097.java"},
            {"folders.javaFolder100", "SampleJavaClass096.java"},
            {"folders.javaFolder100", "SampleJavaClass095.java"},
            {"folders.javaFolder50", "SampleJavaClass000.java"},
            {"folders.javaFolder50", "SampleJavaClass001.java"},
            {"folders.javaFolder50", "SampleJavaClass002.java"},
            {"folders.javaFolder50", "SampleJavaClass003.java"},
            {"folders.javaFolder50", "SampleJavaClass004.java"}
        };
        return files_path;
    }
}
