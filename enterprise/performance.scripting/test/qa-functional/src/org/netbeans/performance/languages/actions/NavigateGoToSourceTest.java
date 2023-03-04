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

import java.awt.Rectangle;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mrkam@netbeans.org
 */
public class NavigateGoToSourceTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String testProject;
    protected String docName, openedDocName;
    protected String pathName;
    protected String textToFind, openedText;
    protected static ProjectsTabOperator projectsTab = null;
    private EditorOperator editorOperator;
    private int caretBlinkRate;

    public NavigateGoToSourceTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 2000;
    }

    public NavigateGoToSourceTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(NavigateGoToSourceTest.class).suite();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    @Override
    public void initialize() {
        closeAllModal();
        String path = pathName + docName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(docName);
        caretBlinkRate = editorOperator.txtEditorPane().getCaret().getBlinkRate();
        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
    }

    @Override
    public void prepare() {
        try {
            editorOperator = EditorWindowOperator.getEditor(docName);
            editorOperator.setCaretPosition(textToFind, false);
            new EventTool().waitNoEvent(200);
            Rectangle r = editorOperator.txtEditorPane().getUI().modelToView((JTextComponent) editorOperator.txtEditorPane().getSource(), editorOperator.txtEditorPane().getCaretPosition());
            editorOperator.txtEditorPane().clickForPopup((int) r.getCenterX(), (int) r.getCenterY());
        } catch (BadLocationException ex) {
            throw new RuntimeException("Failed to obtain caret position", ex);
        }
    }

    @Override
    public ComponentOperator open() {
        new JPopupMenuOperator().pushMenu("Navigate|Go To Declaration");
        EditorOperator op = new EditorOperator(openedDocName);
        return null;
    }

    public void test_PHP_NavigateGoToSourceInTheCurrentClass() {
        testProject = Projects.PHP_PROJECT;
        pathName = "Source Files|classes|";
        docName = "guestitinerary.php";
        openedDocName = "guestitinerary.php";
        textToFind = "return $this->fNam";
        openedText = "private $fName;";
        expectedTime = 1000;
        doMeasurement();
    }

    public void test_PHP_NavigateGoToSourceInTheParentClass() {
        testProject = Projects.PHP_PROJECT;
        pathName = "Source Files|classes|";
        docName = "guestitinerary.php";
        openedDocName = "guest.php";
        textToFind = "return $this->get_firstNam";
        openedText = "function get_firstName(){";
        expectedTime = 1000;
        doMeasurement();
    }

    @Override
    public void shutdown() {
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        repaintManager().resetRegionFilters();
    }

}
