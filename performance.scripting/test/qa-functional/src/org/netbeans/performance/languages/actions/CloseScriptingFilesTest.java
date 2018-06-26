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
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class CloseScriptingFilesTest extends PerformanceTestCase {

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

    /**
     * Menu item name that opens the editor
     */
    public static String menuItem;

    protected static String OPEN = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
    protected static String EDIT = org.netbeans.jellytools.Bundle.getStringTrimmed("org.openide.actions.Bundle", "Edit");
    protected EditorOperator editor;

    public CloseScriptingFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    public CloseScriptingFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(CloseScriptingFilesTest.class).suite();
    }

    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        closeAllModal();
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
        JPopupMenuOperator popup = fileToBeOpened.callPopup();
        if (popup == null) {
            throw new Error("Cannot get context menu for node [" + fileToBeOpened.getPath() + "] in project [" + testProject + "]");
        }
        try {
            popup.pushMenu(menuItem);
        } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
            tee.printStackTrace(getLog());
            throw new Error("Cannot push menu item [" + menuItem + "] of node [" + fileToBeOpened.getPath() + "] in project [" + testProject + "]");
        }
        editor = new EditorOperator(this.fileName);
    }

    @Override
    public ComponentOperator open() {
        editor.close();
        return null;
    }

    @Override
    protected void shutdown() {
        testedComponentOperator = null; // allow GC of editor and documents
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }

    public void testClose20kbPHPFile() {
        testProject = Projects.PHP_PROJECT;
        WAIT_AFTER_OPEN = 1500;
        menuItem = OPEN;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        MY_START_EVENT = ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE;
        doMeasurement();
    }

}
