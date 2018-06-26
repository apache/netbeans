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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
