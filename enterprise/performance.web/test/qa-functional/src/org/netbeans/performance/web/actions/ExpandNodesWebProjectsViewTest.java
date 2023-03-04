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

import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author mmirilovic@netbeans.org
 */
public class ExpandNodesWebProjectsViewTest extends PerformanceTestCase {

    /**
     * Name of the folder which test creates and expands
     */
    private static String project;
    /**
     * Path to the folder which test creates and expands
     */
    private static String pathToFolderNode;
    /**
     * Node representation of the folder which test creates and expands
     */
    private static Node nodeToBeExpanded;
    /**
     * Projects tab
     */
    private static ProjectsTabOperator projectTab;
    /**
     * Project with data for these tests
     */
    private static String testDataProject = "PerformanceTestFolderWebApp";

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     */
    public ExpandNodesWebProjectsViewTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesWebProjectsViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(ExpandNodesWebProjectsViewTest.class)
                .suite();
    }

    public void testExpandProjectNode() {
        pathToFolderNode = "";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        doMeasurement();
    }

    public void testExpandSourcePackagesNode() {
        pathToFolderNode = "Source Packages";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith50JspFiles() {
        pathToFolderNode = "Web Pages|jsp50";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith100JspFiles() {
        pathToFolderNode = "Web Pages|jsp100";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        doMeasurement();
    }

    public void testExpandFolderWith1000JspFiles() {
        pathToFolderNode = "Web Pages|jsp1000";
        project = testDataProject;
        WAIT_AFTER_OPEN = 1000;
        doMeasurement();
    }

    @Override
    public void initialize() {
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode("TestWebProject").collapse();
        projectTab.getProjectRootNode(testDataProject).collapse();
        System.setProperty("perf.dont.resolve.java.badges", "true");
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
        System.setProperty("perf.dont.resolve.java.badges", "false");
        projectTab.getProjectRootNode(project).collapse();
        projectTab.close();
        ProjectsTabOperator.invoke();
    }
}
