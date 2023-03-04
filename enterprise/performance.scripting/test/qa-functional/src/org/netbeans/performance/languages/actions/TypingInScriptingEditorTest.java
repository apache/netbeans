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
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
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
import org.netbeans.modules.performance.guitracker.ActionTracker;

/**
 * Measure time of typing in editor. In fact it measures time when first letter
 * appears in document and it is possible to type another letter.

 * @author Jiri Skrivanek
 */
public class TypingInScriptingEditorTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName;
    protected String nodePath;
    protected String afterTextStartTyping;
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;

    public TypingInScriptingEditorTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 200;
    }

    public TypingInScriptingEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 200;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(TypingInScriptingEditorTest.class).suite();
    }

    @Override
    public void initialize() {
        String path = nodePath + "|" + fileName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        editorOperator.setCaretPosition(afterTextStartTyping, false);
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        waitNoEvent(500);
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        // measure two sub sequent key types (in fact time when first letter appears
        // in document and it is possible to type another letter)
        MY_START_EVENT = ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE;
        MY_END_EVENT = ActionTracker.TRACK_KEY_RELEASE;
        editorOperator.typeKey('z');
        editorOperator.typeKey('z');
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();
        if (projectsTab != null) {
            projectsTab.collapseAll();
        }
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
            projectsTab.collapseAll();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    public void test_JScript_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "javascript20kb.js";
        afterTextStartTyping = "headers[0] = 'ID";
        nodePath = "Web Pages";
        expectedTime = 400;
        doMeasurement();
    }

    public void test_JScript_EditorTypingBig() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "javascript_200kb.js";
        afterTextStartTyping = "if(browser.klient=='op";
        nodePath = "Web Pages";
        expectedTime = 1500;
        doMeasurement();
    }

    public void test_PHP_EditorTyping() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        afterTextStartTyping = "include(\"";
        nodePath = "Source Files";
        expectedTime = 400;
        doMeasurement();
    }

    public void test_JSON_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "json20kB.json";
        afterTextStartTyping = "\"firstName0\": \"";
        nodePath = "Web Pages";
        expectedTime = 400;
        doMeasurement();
    }

    public void test_CSS_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "css20kB.css";
        afterTextStartTyping = "font: small-caps 40px/40px \"";
        nodePath = "Web Pages";
        expectedTime = 1000;
        doMeasurement();
    }

    public void test_BAT_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "bat20kB.bat";
        afterTextStartTyping = "cd ..";
        nodePath = "Web Pages";
        expectedTime = 400;
        doMeasurement();
    }

    public void test_DIFF_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "diff20kB.diff";
        afterTextStartTyping = "LinkageError";
        nodePath = "Web Pages";
        expectedTime = 300;
        doMeasurement();
    }

    public void test_MANIFEST_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "manifest20kB.mf";
        afterTextStartTyping = "OpenIDE-Module-Implementation-Version-1: 1";
        nodePath = "Web Pages";
        expectedTime = 400;
        doMeasurement();
    }

    public void test_SH_EditorTyping() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "sh20kB.sh";
        afterTextStartTyping = "echo \"";
        nodePath = "Web Pages";
        expectedTime = 300;
        doMeasurement();
    }
}
