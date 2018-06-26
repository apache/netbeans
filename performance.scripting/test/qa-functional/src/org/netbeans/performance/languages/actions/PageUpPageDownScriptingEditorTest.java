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

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author Administrator
 */
public class PageUpPageDownScriptingEditorTest extends PerformanceTestCase {

    private boolean pgup;
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;
    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName;
    protected String nodePath;
    /**
     * After jump in editor QuietEditorPane is refreshed by events from
     * breadcrumbs, annotations, code folding and editor status panel. For
     * editors without such effects is expected time set individually.
     * TODO - rather measure time of some objective event than end of event
     * queue activity (e.g. possibility of typing after jump in editor - see
     * TypingInScriptingEditorTest)
     */
    private static final long EXPECTED_TIME = 800;

    public PageUpPageDownScriptingEditorTest(String testName) {
        super(testName);
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 1000;
    }

    public PageUpPageDownScriptingEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = EXPECTED_TIME;
        WAIT_AFTER_OPEN = 1000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(PageUpPageDownScriptingEditorTest.class).suite();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        String path = nodePath + "|" + fileName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        new EventTool().waitNoEvent(3000);
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    @Override
    public void prepare() {
        if (pgup) {
            new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator);
        } else {
            editorOperator.setCaretPositionToLine(1);
        }
        new EventTool().waitNoEvent(1000);
    }

    @Override
    public ComponentOperator open() {
        // use Ctrl+Home or Ctrl+End to minimalize effects of tooltips in the middle of source code
        if (pgup) {
            new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_HOME, KeyEvent.CTRL_MASK)).perform(editorOperator);
        } else {
            new Action(null, null, KeyStroke.getKeyStroke(KeyEvent.VK_END, KeyEvent.CTRL_MASK)).perform(editorOperator);
        }
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void shutdown() {
        super.shutdown();
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();
    }

    public void testPgUp_In_PHPEditor() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        pgup = true;
        expectedTime = 200;
        doMeasurement();
    }

    public void testPgDn_In_PHPEditor() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        pgup = false;
        expectedTime = 400;
        doMeasurement();
    }

    public void testPgUp_In_JSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "javascript20kb.js";
        pgup = true;
        doMeasurement();
    }

    public void testPgDn_In_JSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "javascript20kb.js";
        pgup = false;
        doMeasurement();
    }

    public void testPgUp_In_JSEditorBig() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "javascript_200kb.js";
        pgup = true;
        doMeasurement();
    }

    public void testPgDn_In_JSEditorBig() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "javascript_200kb.js";
        pgup = false;
        doMeasurement();
    }

    public void testPgUp_In_CSSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "css20kb.css";
        pgup = true;
        doMeasurement();
    }

    public void testPgUp_In_JSONditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "json20kb.json";
        pgup = true;
        doMeasurement();
    }

    public void testPgDn_In_JSONditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "json20kb.json";
        pgup = false;
        doMeasurement();
    }

    public void testPgDn_In_CSSEditor() {
        testProject = Projects.SCRIPTING_PROJECT;
        nodePath = "Web Pages";
        fileName = "css20kb.css";
        pgup = false;
        doMeasurement();
    }

}
