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
package org.netbeans.performance.languages.actions;

import java.awt.Rectangle;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 *
 * @author mrkam@netbeans.org
 */
public class GoToSourceTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName;
    protected String nodePath;
    protected String functionName;
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;

    public GoToSourceTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 2000;
    }

    public GoToSourceTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(GoToSourceTest.class).suite();
    }

    @Override
    public void initialize() {
        closeAllModal();
        String path = nodePath + "|" + fileName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
    }

    @Override
    public void prepare() {
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = new EditorOperator(fileName);
        editorOperator.waitComponentShowing(true);
        editorOperator.setCaretPosition(1, 1);
        new NavigatorOperator().getTree().waitRow("cancelReservation", 1);
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
    }

    @Override
    public ComponentOperator open() {
        JTreeOperator tree = new NavigatorOperator().getTree();
        int row = tree.findRow(new JTreeOperator.TreeRowChooser() {

            @Override
            public boolean checkRow(JTreeOperator oper, int row) {
                return oper.getPathForRow(row).toString().contains(functionName);
            }

            @Override
            public String getDescription() {
                return "Looking for a row '" + functionName + "'";
            }
        });
        Rectangle r = tree.getRowBounds(row);
        int x = (int) r.getCenterX();
        int y = (int) r.getCenterY();
        tree.clickForPopup(x, y);
        // Go to source
        new JPopupMenuOperator().pushMenu("Go to Source");
        return null;
    }

    @Override
    public void close() {
        EditorOperator.closeDiscardAll();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    public void test_PHP_NavigateToAFunctionViaNavigator() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        functionName = "getItinerary2";
        expectedTime = 1000;
        doMeasurement();
    }
}
