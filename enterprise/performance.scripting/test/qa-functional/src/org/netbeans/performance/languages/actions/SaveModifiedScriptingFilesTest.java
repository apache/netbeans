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
import org.netbeans.jellytools.EditorWindowOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class SaveModifiedScriptingFilesTest extends PerformanceTestCase {

    /**
     * Editor with opened file
     */
    public static EditorOperator editorOperator;
    protected Node fileToBeOpened;
    protected String testProject;
    protected String docName;
    protected String pathName;
    protected static ProjectsTabOperator projectsTab = null;

    public SaveModifiedScriptingFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 2000;
    }

    public SaveModifiedScriptingFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_PREPARE = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(SaveModifiedScriptingFilesTest.class).suite();
    }

    public void test_SavePHP_File() {
        testProject = Projects.PHP_PROJECT;
        pathName = "Source Files" + "|";
        docName = "php20kb.php";
        doMeasurement();
    }

    public void test_SaveJS_File() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "web" + "|";
        docName = "javascript20kb.js";
        doMeasurement();
    }

    public void test_SaveJSON_File() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "web" + "|";
        docName = "json20kb.json";
        doMeasurement();
    }

    public void test_SaveCSS_File() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "web" + "|";
        docName = "css20kb.css";
        doMeasurement();
    }

    public void test_SaveBAT_File() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "web" + "|";
        docName = "bat20kb.bat";
        doMeasurement();
    }

    public void test_SaveDIFF_File() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "web" + "|";
        docName = "diff20kb.diff";
        doMeasurement();
    }

    public void test_SaveMANIFEST_File() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "web" + "|";
        docName = "manifest20kb.mf";
        doMeasurement();
    }

    public void test_SaveSH_File() {
        testProject = Projects.SCRIPTING_PROJECT;
        pathName = "web" + "|";
        docName = "sh20kb.sh";
        doMeasurement();
    }

    @Override
    public void initialize() {
        closeAllModal();
        EditorOperator.closeDiscardAll();
        String path = pathName + docName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(docName);
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    @Override
    public void prepare() {
        editorOperator.setCaretPosition(1, 3);
        editorOperator.txtEditorPane().typeText("XXX");
        editorOperator.pushKey(java.awt.event.KeyEvent.VK_BACK_SPACE);
        editorOperator.pushKey(java.awt.event.KeyEvent.VK_BACK_SPACE);
        editorOperator.pushKey(java.awt.event.KeyEvent.VK_BACK_SPACE);

    }

    @Override
    public ComponentOperator open() {
        new SaveAction().performShortcut(editorOperator);
        editorOperator.waitModified(false);
        return null;
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
    }
}
