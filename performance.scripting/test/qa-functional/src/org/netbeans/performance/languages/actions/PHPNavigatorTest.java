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

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager.RegionFilter;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.ScriptingUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import java.awt.Container;
import javax.swing.JComponent;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 *
 * @author Administrator
 */
public class PHPNavigatorTest extends PerformanceTestCase {

    protected Node fileToBeOpened;
    protected String testProject;
    protected String fileName;
    protected String nodePath;
    protected int lineNumber;
    protected int column;
    protected String textToType;
    private EditorOperator editorOperator;
    protected static ProjectsTabOperator projectsTab = null;

    public PHPNavigatorTest(String testName) {
        super(testName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN = 2000;
    }

    public PHPNavigatorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 1000;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(PHPNavigatorTest.class).suite();
    }

    @Override
    public void initialize() {
        closeAllModal();
        String path = nodePath + "|" + fileName;
        fileToBeOpened = new Node(getProjectNode(testProject), path);
        final Container navigator = (Container) (new NavigatorOperator().getSource());

        repaintManager().addRegionFilter(new RegionFilter() {

            public boolean accept(JComponent c) {
                return navigator.isAncestorOf(c);
            }

            public String getFilterName() {
                return "Accept paints only from Navigator";
            }
        });
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
    }

    @Override
    public void prepare() {
        new OpenAction().performAPI(fileToBeOpened);
        editorOperator = EditorWindowOperator.getEditor(fileName);
        editorOperator.setCaretPosition(lineNumber, column);
        new NavigatorOperator().getTree();
    }

    @Override
    public ComponentOperator open() {
        editorOperator.txtEditorPane().setVerification(false);
        editorOperator.txtEditorPane().typeText(textToType);
        return null;
    }

    @Override
    public void close() {
        editorOperator.txtEditorPane().setVerification(true);
        EditorOperator.closeDiscardAll();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = ScriptingUtilities.invokePTO();
        }
        return projectsTab.getProjectRootNode(projectName);
    }

    public void testCreatingNodesInPHPNavigator() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        lineNumber = 91;
        column = 1;
        textToType = "function closeDB3(){\n";
        doMeasurement();
    }

    public void testRefreshingNodesInPHPNavigator() {
        testProject = Projects.PHP_PROJECT;
        fileName = "php20kb.php";
        nodePath = "Source Files";
        lineNumber = 88;
        column = 18;
        textToType = "Test";
        doMeasurement();
    }

}
