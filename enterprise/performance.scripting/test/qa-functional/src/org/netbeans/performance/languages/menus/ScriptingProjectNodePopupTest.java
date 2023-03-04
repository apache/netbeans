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
package org.netbeans.performance.languages.menus;

import junit.framework.Test;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingProjectNodePopupTest extends PerformanceTestCase {

    protected static Node dataObjectNode;
    protected static ProjectsTabOperator projectsTab = null;

    public ScriptingProjectNodePopupTest(String testName) {
        super(testName);
        expectedTime = 100;
    }

    public ScriptingProjectNodePopupTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 100;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(ScriptingProjectNodePopupTest.class).suite();
    }

    /**
     * Selects node whose popup menu will be tested.
     */
    public void prepare() {
        dataObjectNode.select();
    }

    /**
     * Directly sends mouse events causing popup menu displaying to the selected
     * node.
     * <p>
     * Using Jemmy/Jelly to call popup can cause reselecting of node and more
     * events than is desirable for this case.
     *
     * @return JPopupMenuOperator instance
     */
    public ComponentOperator open() {
        /* it stopped to work after a while, see issue 58790
         java.awt.Point p = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
         JPopupMenu menu = callPopup(dataObjectNode.tree(), p.x, p.y, java.awt.event.InputEvent.BUTTON3_MASK);
         return new JPopupMenuOperator(menu);
         */
        java.awt.Point point = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        dataObjectNode.tree().clickForPopup(point.x, point.y);
        return new JPopupMenuOperator();
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

    public void testPHPProjectNodePopupMenu() {
        testNode(getProjectNode(Projects.PHP_PROJECT));
    }

    public void testScriptingProjectNodePopupMenu() {
        testNode(getProjectNode(Projects.SCRIPTING_PROJECT));
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
