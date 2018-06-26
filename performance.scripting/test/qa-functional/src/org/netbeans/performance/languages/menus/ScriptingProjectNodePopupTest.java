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
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.Projects;
import org.netbeans.performance.languages.setup.ScriptingSetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ScriptingProjectNodePopupTest extends PerformanceTestCase {

    protected static Node dataObjectNode;
    protected static ProjectsTabOperator projectsTab = null;

    public ScriptingProjectNodePopupTest(String testName) {
        super(testName);
        expectedTime = 100;
    }

    public ScriptingProjectNodePopupTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 100;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(ScriptingSetup.class).addTest(ScriptingProjectNodePopupTest.class).suite();
    }

    /**
     * Selects node whose popup menu will be tested.
     */
    public void prepare() {
        dataObjectNode.select();
    }

    /**
     * Directly sends mouse events causing popup menu displaying to the selected
     * node.
     * <p>
     * Using Jemmy/Jelly to call popup can cause reselecting of node and more
     * events than is desirable for this case.
     *
     * @return JPopupMenuOperator instance
     */
    public ComponentOperator open() {
        /* it stopped to work after a while, see issue 58790
         java.awt.Point p = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
         JPopupMenu menu = callPopup(dataObjectNode.tree(), p.x, p.y, java.awt.event.InputEvent.BUTTON3_MASK);
         return new JPopupMenuOperator(menu);
         */
        java.awt.Point point = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        dataObjectNode.tree().clickForPopup(point.x, point.y);
        return new JPopupMenuOperator();
    }

    /**
     * Closes the popup by sending ESC key event.
     */
    @Override
    public void close() {
        //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        // Above sometimes fails in QUEUE mode waiting to menu become visible.
        // This pushes Escape on underlying JTree which should be always visible
        dataObjectNode.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    public void testPHPProjectNodePopupMenu() {
        testNode(getProjectNode(Projects.PHP_PROJECT));
    }

    public void testScriptingProjectNodePopupMenu() {
        testNode(getProjectNode(Projects.SCRIPTING_PROJECT));
    }

    public void testNode(Node node) {
        dataObjectNode = node;
        doMeasurement();
    }

    protected Node getProjectNode(String projectName) {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }
        return projectsTab.getProjectRootNode(projectName);
    }
}
