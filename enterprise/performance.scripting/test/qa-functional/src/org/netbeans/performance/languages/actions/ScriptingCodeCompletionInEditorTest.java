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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
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
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingCodeCompletionInEditorTest extends PerformanceTestCase {

    private int lineNumber = 39;
    private EditorOperator editorOperator;
    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName;
    protected String nodePath;
    protected static ProjectsTabOperator projectsTab = null;

    public ScriptingCodeCompletionInEditorTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public ScriptingCodeCompletionInEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(ScriptingCodeCompletionInEditorTest.class).suite();
    }

    @Override
    public void initialize() {
        repaintManager().addRegionFilter(COMPLETION_FILTER);
        closeAllModal();

        String path = nodePath + "|" + fileName;

        fileToBeOpened = new Node(getProjectNode(testProject), path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }

        return projectsTab.getProjectRootNode(projectName);
    }

    @Override
    public void prepare() {
        editorOperator.setCaretPositionToEndOfLine(lineNumber);
    }

    @Override
    public ComponentOperator open() {
        editorOperator.txtEditorPane().pushKey(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK);
        return new CompletionJListOperator();
    }

    @Override
    public void close() {
        editorOperator.txtEditorPane().pushKey(KeyEvent.VK_ESCAPE);
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        editorOperator.closeDiscard();
    }

    public void testCC_InJavaScriptEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "javascript20kb.js";
        nodePath = "Web Pages";
        lineNumber = 41;
        doMeasurement();
    }

    public void testCC_InPHPEditor() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        lineNumber = 29;
        expectedTime = 4000;
        doMeasurement();
    }

    public void testCC_InCSSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "css20kb.css";
        lineNumber = 39;
        doMeasurement();
    }

    public void testCC_InJSONEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        fileName = "json20kB.json";
        nodePath = "Web Pages";
        lineNumber = 39;
        doMeasurement();
    }

    private static final RegionFilter COMPLETION_FILTER
            = new RegionFilter() {

                @Override
                public boolean accept(javax.swing.JComponent c) {
                    return c.getClass().getName().equals("org.netbeans.modules.editor.completion.CompletionScrollPane")
                    || c.getClass().getName().equals("org.openide.text.QuietEditorPane");
                }

                @Override
                public String getFilterName() {
                    return "Accept paints from org.netbeans.modules.editor.completion.CompletionScrollPane || org.openide.text.QuietEditorPane";
                }
            };
}
