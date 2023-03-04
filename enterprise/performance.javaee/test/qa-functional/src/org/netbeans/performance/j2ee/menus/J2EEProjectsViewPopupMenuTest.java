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
package org.netbeans.performance.j2ee.menus;

import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of popup menu on nodes in Projects View.
 *
 * @author lmartinek@netbeans.org
 */
public class J2EEProjectsViewPopupMenuTest extends PerformanceTestCase {

    private static ProjectsTabOperator projectsTab = null;
    protected static Node dataObjectNode;

    private static final String JAVA_EE_MODULES = "Java EE Modules";

    /**
     * Creates a new instance of J2EEProjectsViewPopupMenuTest
     *
     * @param testName
     */
    public J2EEProjectsViewPopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of J2EEProjectsViewPopupMenuTest
     *
     * @param testName
     * @param performanceDataName
     */
    public J2EEProjectsViewPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(J2EEProjectsViewPopupMenuTest.class).suite();
    }

    public void testEARProjectNodePopupMenu() {
        testNode(getEARProjectNode(), null);
    }

    public void testEARConfFilesNodePopupMenu() {
        testNode(getEARProjectNode(), "Configuration Files");
    }

    public void testEARServerFilesNodePopupMenu() {
        testNode(getEARProjectNode(), "Server Resources");
    }

    public void testApplicationXmlPopupMenu() {
        testNode(getEARProjectNode(), "Configuration Files|application.xml");
    }

    public void testSunApplicationXmlPopupMenu() {
        testNode(getEARProjectNode(), "Configuration Files|sun-application.xml");
    }

    public void testJ2eeModulesNodePopupMenu() {
        testNode(getEARProjectNode(), JAVA_EE_MODULES);
    }

    public void testJ2eeModulesEJBNodePopupMenu() {
        testNode(getEARProjectNode(), JAVA_EE_MODULES + "|TestApplication-ejb.jar");
    }

    public void testJ2eeModulesWebNodePopupMenu() {
        testNode(getEARProjectNode(), JAVA_EE_MODULES + "|TestApplication-war.war");
    }

    public void testEJBProjectNodePopupMenu() {
        testNode(getEJBProjectNode(), null);
    }

    public void testWebProjectNodePopupMenu() {
        testNode(getWebProjectNode(), null);
    }

    public void testWebPagesNodePopupMenu() {
        expectedTime = 600;
        testNode(getWebProjectNode(), "Web Pages");
    }

    public void testEJBsNodePopupMenu() {
        testNode(getEJBProjectNode(), "Enterprise Beans");
    }

    public void testEJBsSourceNodePopupMenu() {
        testNode(getEJBProjectNode(), "Source Packages");
    }

    public void testSessionBeanNodePopupMenu() {
        expectedTime = 500;
        testNode(getEJBProjectNode(), "Enterprise Beans|TestSessionSB");
    }

    public void testEjbJarXmlPopupMenu() {
        testNode(getEJBProjectNode(), "Configuration Files|ejb-jar.xml");
    }

    public void testSunEjbJarXmlPopupMenu() {
        testNode(getEJBProjectNode(), "Configuration Files|sun-ejb-jar.xml");
    }

    public void testNode(Node rootNode, String path) {
        try {
            if (path == null) {
                dataObjectNode = rootNode;
            } else {
                dataObjectNode = new Node(rootNode, path);
            }
            doMeasurement();
        } catch (Exception e) {
            throw new Error("Exception thrown", e);
        }

    }

    private Node getEARProjectNode() {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode("TestApplication");
    }

    private Node getWebProjectNode() {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode("TestApplication-war");
    }

    private Node getEJBProjectNode() {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode("TestApplication-ejb");
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
        dataObjectNode.tree().clickForPopup(point.x, point.y);
        return new JPopupMenuOperator();
    }
}
