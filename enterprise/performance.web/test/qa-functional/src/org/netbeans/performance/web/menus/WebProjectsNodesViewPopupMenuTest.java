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
package org.netbeans.performance.web.menus;

import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Test of popup menu on nodes in Projects View.
 *
 * @author mmirilovic@netbeans.org
 */
public class WebProjectsNodesViewPopupMenuTest extends PerformanceTestCase {

    private static ProjectsTabOperator projectsTab = null;
    protected static Node dataObjectNode;

    public static final String suiteName = "UI Responsiveness Web Menus suite";

    /**
     * Creates a new instance of ProjectsViewPopupMenu
     *
     * @param testName test name
     */
    public WebProjectsNodesViewPopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of ProjectsViewPopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public WebProjectsNodesViewPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(WebProjectsNodesViewPopupMenuTest.class)
                .suite();
    }

    public void testProjectNodePopupMenuProjects() {
        testNode(getProjectNode());
    }

    public void testSourcePackagesPopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Source Packages"));
    }

    public void testPackagePopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "test"));
    }

    public void testServletPopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Source Packages" + '|' + "test" + '|' + "TestServlet.java"));
    }

    public void testWebPagesPopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Web Pages"));
    }

    public void testJspFilePopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "Test.jsp"));
    }

    public void testHtmlFilePopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "HTML.html"));
    }

    public void testWebInfPopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF"));
    }

    public void testWebXmlFilePopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF" + '|' + "web.xml"));
    }

    public void testTagFilePopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF" + '|' + "tags" + '|' + "mytag.tag"));
    }

    public void testTldPopupMenuProjects() {
        testNode(new Node(getProjectNode(), "Web Pages" + '|' + "WEB-INF" + '|' + "MyTLD.tld"));
    }

    public void testNode(Node node) {
        dataObjectNode = node;
        doMeasurement();
    }

    private Node getProjectNode() {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode("TestWebProject");
    }

    /**
     * Closes the popup by sending ESC key event.
     */
    @Override
    public void close() {
        //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        // Above sometimes fails in QUEUE mode waiting to menu become visible.
        // This pushes Escape on underlying JTree which should be always visible
        dataObjectNode.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    @Override
    public void prepare() {
        dataObjectNode.select();
    }

    @Override
    public ComponentOperator open() {
        java.awt.Point point = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        int button = JTreeOperator.getPopupMouseButton();
        dataObjectNode.tree().clickMouse(point.x, point.y, 1, button);
        return new JPopupMenuOperator();
    }
}
