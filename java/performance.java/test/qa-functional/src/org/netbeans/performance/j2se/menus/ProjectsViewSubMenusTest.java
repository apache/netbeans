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
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Test of submenu in popup menu on nodes in Projects View.
 *
 * @author mmirilovic@netbeans.org
 */
public class ProjectsViewSubMenusTest extends PerformanceTestCase {

    private static ProjectsTabOperator projectsTab = null;
    private String testedSubmenu;
    protected static Node dataObjectNode;
    private JMenuItemOperator mio;
    private int dispatchingModel;

    /**
     * Creates a new instance
     *
     * @param testName test name
     */
    public ProjectsViewSubMenusTest(String testName) {
        super(testName);
        expectedTime = 250;
        WAIT_AFTER_OPEN = 500;
    }

    /**
     * Creates a new instance
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ProjectsViewSubMenusTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 250;
        WAIT_AFTER_OPEN = 500;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(ProjectsViewSubMenusTest.class)
                .suite();
    }

    public void testProjectNodeConfigurationSubmenu() {
        testedSubmenu = "Set Configuration";
        testNode(getProjectNode("PerformanceTestData"));
    }

    public void testProjectNodeVersioningSubmenu() {
        testedSubmenu = "Versioning";
        testNode(getProjectNode("PerformanceTestData"));
    }

    public void testProjectNodeLocalHistorySubmenu() {
        testedSubmenu = "History";
        testNode(getProjectNode("PerformanceTestData"));
    }

    public void testProjectNodeNewSubmenu() {
        testedSubmenu = "New";
        testNode(getProjectNode("PerformanceTestData"));
    }

    public void testNode(Node node) {
        dataObjectNode = node;
        doMeasurement();
    }

    private Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode(projectName);
    }
    
    @Override
    public void initialize() {
        dispatchingModel = JemmyProperties.getCurrentDispatchingModel();
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
    }

    @Override
    public void prepare() {
        JPopupMenuOperator popupMenu = dataObjectNode.callPopup();
        mio = new JMenuItemOperator(popupMenu, testedSubmenu);
    }

    @Override
    public ComponentOperator open() {
        mio.clickMouse();
        return mio;
    }

    @Override
    public void close() {
        MainWindowOperator.getDefault().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
    
    @Override
    public void shutdown() {
        JemmyProperties.setCurrentDispatchingModel(dispatchingModel);
    }
}
