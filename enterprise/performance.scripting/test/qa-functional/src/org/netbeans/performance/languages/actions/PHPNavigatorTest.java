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

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager.RegionFilter;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import java.awt.Container;
import javax.swing.JComponent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author Administrator
 */
public class PHPNavigatorTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName;
    protected String nodePath;
    protected int lineNumber;
    protected int column;
    protected String textToType;
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;

    public PHPNavigatorTest(String testName) {
        super(testName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN = 2000;
    }

    public PHPNavigatorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(PHPNavigatorTest.class).suite();
    }

    @Override
    public void initialize() {
        closeAllModal();
        String path = nodePath + "|" + fileName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        final Container navigator = (Container) (new NavigatorOperator().getSource());

        repaintManager().addRegionFilter(new RegionFilter() {

            public boolean accept(JComponent c) {
                return navigator.isAncestorOf(c);
            }

            public String getFilterName() {
                return "Accept paints only from Navigator";
            }
        });
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
    }

    @Override
    public void prepare() {
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        editorOperator.setCaretPosition(lineNumber, column);
        new NavigatorOperator().getTree();
    }

    @Override
    public ComponentOperator open() {
        editorOperator.txtEditorPane().setVerification(false);
        editorOperator.txtEditorPane().typeText(textToType);
        return null;
    }

    @Override
    public void close() {
        editorOperator.txtEditorPane().setVerification(true);
        EditorOperator.closeDiscardAll();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    public void testCreatingNodesInPHPNavigator() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        lineNumber = 91;
        column = 1;
        textToType = "function closeDB3(){\n";
        doMeasurement();
    }

    public void testRefreshingNodesInPHPNavigator() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        lineNumber = 88;
        column = 18;
        textToType = "Test";
        doMeasurement();
    }

}
