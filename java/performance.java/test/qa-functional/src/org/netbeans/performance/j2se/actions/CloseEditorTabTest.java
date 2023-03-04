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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseViewAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import static org.netbeans.junit.NbModuleSuite.emptyConfiguration;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test Closing Editor tab.
 *
 * @author mmirilovic@netbeans.org
 */
public class CloseEditorTabTest extends PerformanceTestCase {

    /**
     * Nodes represent files to be opened
     */
    private static Node[] openFileNodes;

    /**
     * Creates a new instance of CloseEditorTab
     *
     * @param testName the name of the test
     */
    public CloseEditorTabTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of CloseEditorTab
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CloseEditorTabTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(CloseEditorTabTest.class)
                .suite();
    }

    public void testCloseEditorTab() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        prepareFiles();
    }

    @Override
    public void shutdown() {
        new ProjectsTabOperator().getProjectRootNode("PerformanceTestFoldersData").collapse();
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void prepare() {
        new OpenAction().performAPI(openFileNodes);
    }

    @Override
    public ComponentOperator open() {
        new CloseViewAction().performMenu(new EditorOperator("SampleJavaClass000.java"));
        return null;
    }

    @Override
    public void close() {
        EditorOperator.closeDiscardAll();
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
            {"folders.javaFolder50", "SampleJavaClass000.java"},
            {"folders.javaFolder50", "SampleJavaClass001.java"},
            {"folders.javaFolder50", "SampleJavaClass002.java"},
            {"folders.javaFolder50", "SampleJavaClass003.java"},
            {"folders.javaFolder50", "SampleJavaClass004.java"},
            {"folders.javaFolder50", "SampleJavaClass005.java"},
            {"folders.javaFolder50", "SampleJavaClass006.java"},
            {"folders.javaFolder50", "SampleJavaClass007.java"},
            {"folders.javaFolder50", "SampleJavaClass008.java"},
            {"folders.javaFolder50", "SampleJavaClass009.java"}
        };
        return files_path;
    }
}
