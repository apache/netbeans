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
