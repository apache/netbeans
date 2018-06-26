/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
