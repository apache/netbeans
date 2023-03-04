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
package org.netbeans.performance.j2se.menus;

import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.ValidatePopupMenuOnNodes;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of popup menu on nodes in Projects View.
 *
 * @author mmirilovic@netbeans.org
 */
public class ProjectsViewPopupMenuTest extends ValidatePopupMenuOnNodes {

    protected static ProjectsTabOperator projectsTab = null;

    /**
     * Creates a new instance of ProjectsViewPopupMenu
     *
     * @param testName test name
     */
    public ProjectsViewPopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of ProjectsViewPopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ProjectsViewPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(ProjectsViewPopupMenuTest.class)
                .suite();
    }

    public void testProjectNodePopupMenuProjects() {
        expectedTime = 250;
        testNode(getProjectNode("PerformanceTestData"));
    }

    public void testSourcePackagesPopupMenuProjects() {
        testNode(new SourcePackagesNode("PerformanceTestData"));
    }

    public void testTestPackagesPopupMenuProjects() {
        testNode(new Node(getProjectNode("PerformanceTestData"), CommonUtilities.TEST_PACKAGES));
    }

    public void testPackagePopupMenuProjects() {
        testNode(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance"));
    }

    public void testJavaFilePopupMenuProjects() {
        expectedTime = 250;
        testNode(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main.java"));
    }

    public void testTxtFilePopupMenuProjects() {
        testNode(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|textfile.txt"));
    }

    public void testPropertiesFilePopupMenuProjects() {
        testNode(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Bundle20kB.properties"));
    }

    public void testXmlFilePopupMenuProjects() {
        expectedTime = 400;
        testNode(new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|xmlfile.xml"));
    }

    public void testNode(Node node) {
        dataObjectNode = node;
        doMeasurement();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode(projectName);
    }
}
