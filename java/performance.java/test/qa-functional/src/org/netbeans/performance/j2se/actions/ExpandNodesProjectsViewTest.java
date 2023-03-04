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
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author mmirilovic@netbeans.org
 */
public class ExpandNodesProjectsViewTest extends PerformanceTestCase {

    /**
     * Name of the folder which test creates and expands
     */
    protected String project;
    /**
     * Path to the folder which test creates and expands
     */
    protected String pathToFolderNode;
    /**
     * Node representation of the folder which test creates and expands
     */
    protected Node nodeToBeExpanded;
    /**
     * Projects tab
     */
    protected ProjectsTabOperator projectTab;

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     */
    public ExpandNodesProjectsViewTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 100;
    }

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesProjectsViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 100;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(ExpandNodesProjectsViewTest.class)
                .suite();
    }

    public void testExpandProjectNode() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = "";
        expectedTime = 200;
        doMeasurement();
    }

    public void testExpandSourcePackagesNode() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES;
        expectedTime = 200;
        doMeasurement();
    }

    public void testExpandFolderWith50JavaFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder50";
        expectedTime = 300;
        doMeasurement();
    }

    public void testExpandFolderWith100JavaFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder100";
        expectedTime = 500;
        doMeasurement();
    }

    public void testExpandFolderWith1000JavaFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder1000";
        expectedTime = 2500;
        doMeasurement();
    }

    public void testExpandFolderWith100XmlFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.xmlFolder100";
        expectedTime = 2000;
        doMeasurement();
    }

    public void testExpandFolderWith100TxtFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.txtFolder100";
        expectedTime = 600;
        doMeasurement();
    }

    @Override
    public void initialize() {
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode("PerformanceTestFoldersData").collapse();
        repaintManager().addRegionFilter(LoggingRepaintManager.EXPLORER_FILTER);
    }

    @Override
    public void prepare() {
        if (pathToFolderNode.equals("")) {
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        } else {
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        }
        nodeToBeExpanded.collapse();
    }

    @Override
    public ComponentOperator open() {
        // wait only for expansion and ignore other repaint events (badging etc.)
        MY_END_EVENT = ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE;
        nodeToBeExpanded.tree().doExpandPath(nodeToBeExpanded.getTreePath());
        nodeToBeExpanded.expand();
        return null;
    }

    @Override
    public void close() {
        nodeToBeExpanded.collapse();
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        projectTab.getProjectRootNode(project).collapse();
        projectTab.close();
        ProjectsTabOperator.invoke();
    }
}
