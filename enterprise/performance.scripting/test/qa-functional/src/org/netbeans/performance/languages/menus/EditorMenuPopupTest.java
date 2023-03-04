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
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager.RegionFilter;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class EditorMenuPopupTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String testProject;
    protected String docName;
    protected String pathName;
    protected static ProjectsTabOperator projectsTab = null;
    private EditorOperator editorOperator;

    public EditorMenuPopupTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 200;
    }

    public EditorMenuPopupTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(EditorMenuPopupTest.class).suite();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    @Override
    public void initialize() {
        String path = pathName + docName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(docName);
    }

    @Override
    public void prepare() {
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_EXPLORER_TREE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_DIFF_SIDEBAR_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        repaintManager().addRegionFilter(NE_FILTER);
    }

    @Override
    public ComponentOperator open() {
        editorOperator.clickForPopup();
        return new JPopupMenuOperator();
    }

    @Override
    public void close() {
        editorOperator.pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        repaintManager().resetRegionFilters();
    }

    public void test_PHP_EditorPopup() {
        testProject = Projects.PHP_PROJECT;
        pathName = "Source Files" + "|";
        docName = "php20kb.php";
        expectedTime = 500;
        doMeasurement();
    }

    public void test_JS_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages" + "|";
        docName = "javascript20kb.js";
        expectedTime = 200;
        doMeasurement();
    }

    public void test_JSON_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages" + "|";
        docName = "json20kb.json";
        expectedTime = 200;
        doMeasurement();
    }

    public void test_CSS_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages" + "|";
        docName = "css20kb.css";
        expectedTime = 400;
        doMeasurement();
    }

    public void test_BAT_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages" + "|";
        docName = "bat20kb.bat";
        expectedTime = 200;
        doMeasurement();
    }

    public void test_DIFF_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages" + "|";
        docName = "diff20kb.diff";
        expectedTime = 100;
        doMeasurement();
    }

    public void test_MANIFEST_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages" + "|";
        docName = "manifest20kb.mf";
        expectedTime = 100;
        doMeasurement();
    }

    public void test_SH_EditorPopup() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "Web Pages" + "|";
        docName = "sh20kb.sh";
        expectedTime = 100;
        doMeasurement();
    }

    private static final RegionFilter NE_FILTER
            = new RegionFilter() {

                @Override
                public boolean accept(javax.swing.JComponent c) {
                    return !(c.getClass().getName().equals("org.openide.text.QuietEditorPane") || c.getClass().getName().equals("org.netbeans.modules.editor.errorstripe.AnnotationView"));
                }

                @Override
                public String getFilterName() {
                    return "Accept paints from org.netbeans.modules.editor.completion.CompletionScrollPane || org.openide.text.QuietEditorPane";
                }
            };
}
