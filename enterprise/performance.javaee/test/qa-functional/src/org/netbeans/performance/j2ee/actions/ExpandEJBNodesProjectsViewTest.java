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
package org.netbeans.performance.j2ee.actions;

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author mmirilovic@netbeans.org
 */
public class ExpandEJBNodesProjectsViewTest extends PerformanceTestCase {

    /**
     * Name of the folder which test creates and expands
     */
    private static String project;

    /**
     * Path to the folder which test creates and expands
     */
    private static String pathToFolderNode;

    /**
     * Node represantation of the folder which test creates and expands
     */
    private static Node nodeToBeExpanded;

    /**
     * Projects tab
     */
    private static ProjectsTabOperator projectTab;

    /**
     * Project with data for these tests
     */
    private static String testDataProject = "TestApplication-ejb";

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     */
    public ExpandEJBNodesProjectsViewTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandEJBNodesProjectsViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(ExpandEJBNodesProjectsViewTest.class).suite();
    }

    public void testExpandEjbProjectNode() {
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = "";
        doMeasurement();
    }

    public void testExpandEjbNode() {
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = "Enterprise Beans";
        doMeasurement();
    }

    @Override
    public void initialize() {
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode(testDataProject).collapse();
        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
    }

    public void prepare() {
        if (pathToFolderNode.equals("")) {
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        } else {
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        }
    }

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
        projectTab.getProjectRootNode(testDataProject).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }
}
