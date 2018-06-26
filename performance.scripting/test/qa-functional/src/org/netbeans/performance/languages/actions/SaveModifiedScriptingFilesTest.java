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
