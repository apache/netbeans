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

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class OpenScriptingFilesTest extends PerformanceTestCase {

    /**
     * Node to be opened/edited
     */
    public static Node fileToBeOpened;
    protected static ProjectsTabOperator projectsTab = null;

    /**
     * Folder with data
     */
    public static String testProject;
    protected String nodePath;
    protected String fileName;
    private static JPopupMenuOperator popup;

    public OpenScriptingFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    public OpenScriptingFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(OpenScriptingFilesTest.class).suite();
    }

    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        addEditorPhaseHandler();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    @Override
    public void prepare() {
        String path = nodePath + "|" + fileName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        popup = fileToBeOpened.callPopup();
    }

    @Override
    public ComponentOperator open() {
        popup.pushMenu("Open");
        return new EditorOperator(fileName);
    }

    @Override
    public void close() {
        new EditorOperator(fileName).closeDiscard();
    }

    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
        removeEditorPhaseHandler();
    }

    public void testOpening20kbJSFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        fileName = "javascript20kb.js";
        nodePath = "Web Pages";
        doMeasurement();
    }

    public void testOpening20kbPHPFile() {
        testProject = Projects.PHP_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        doMeasurement();
    }

    public void testOpening20kbDIFFFile() {
        testProject = Projects.SCRIPTING_PROJECT;
        WAIT_AFTER_OPEN = 2000;
        nodePath = "Web Pages";
        fileName = "diff20kb.diff";
        doMeasurement();
    }
}
