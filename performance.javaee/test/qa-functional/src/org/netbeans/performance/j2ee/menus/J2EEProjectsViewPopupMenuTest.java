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
package org.netbeans.performance.j2ee.menus;

import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of popup menu on nodes in Projects View.
 *
 * @author lmartinek@netbeans.org
 */
public class J2EEProjectsViewPopupMenuTest extends PerformanceTestCase {

    private static ProjectsTabOperator projectsTab = null;
    protected static Node dataObjectNode;

    private static final String JAVA_EE_MODULES = "Java EE Modules";

    /**
     * Creates a new instance of J2EEProjectsViewPopupMenuTest
     *
     * @param testName
     */
    public J2EEProjectsViewPopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of J2EEProjectsViewPopupMenuTest
     *
     * @param testName
     * @param performanceDataName
     */
    public J2EEProjectsViewPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(J2EEProjectsViewPopupMenuTest.class).suite();
    }

    public void testEARProjectNodePopupMenu() {
        testNode(getEARProjectNode(), null);
    }

    public void testEARConfFilesNodePopupMenu() {
        testNode(getEARProjectNode(), "Configuration Files");
    }

    public void testEARServerFilesNodePopupMenu() {
        testNode(getEARProjectNode(), "Server Resources");
    }

    public void testApplicationXmlPopupMenu() {
        testNode(getEARProjectNode(), "Configuration Files|application.xml");
    }

    public void testSunApplicationXmlPopupMenu() {
        testNode(getEARProjectNode(), "Configuration Files|sun-application.xml");
    }

    public void testJ2eeModulesNodePopupMenu() {
        testNode(getEARProjectNode(), JAVA_EE_MODULES);
    }

    public void testJ2eeModulesEJBNodePopupMenu() {
        testNode(getEARProjectNode(), JAVA_EE_MODULES + "|TestApplication-ejb.jar");
    }

    public void testJ2eeModulesWebNodePopupMenu() {
        testNode(getEARProjectNode(), JAVA_EE_MODULES + "|TestApplication-war.war");
    }

    public void testEJBProjectNodePopupMenu() {
        testNode(getEJBProjectNode(), null);
    }

    public void testWebProjectNodePopupMenu() {
        testNode(getWebProjectNode(), null);
    }

    public void testWebPagesNodePopupMenu() {
        expectedTime = 600;
        testNode(getWebProjectNode(), "Web Pages");
    }

    public void testEJBsNodePopupMenu() {
        testNode(getEJBProjectNode(), "Enterprise Beans");
    }

    public void testEJBsSourceNodePopupMenu() {
        testNode(getEJBProjectNode(), "Source Packages");
    }

    public void testSessionBeanNodePopupMenu() {
        expectedTime = 500;
        testNode(getEJBProjectNode(), "Enterprise Beans|TestSessionSB");
    }

    public void testEjbJarXmlPopupMenu() {
        testNode(getEJBProjectNode(), "Configuration Files|ejb-jar.xml");
    }

    public void testSunEjbJarXmlPopupMenu() {
        testNode(getEJBProjectNode(), "Configuration Files|sun-ejb-jar.xml");
    }

    public void testNode(Node rootNode, String path) {
        try {
            if (path == null) {
                dataObjectNode = rootNode;
            } else {
                dataObjectNode = new Node(rootNode, path);
            }
            doMeasurement();
        } catch (Exception e) {
            throw new Error("Exception thrown", e);
        }

    }

    private Node getEARProjectNode() {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode("TestApplication");
    }

    private Node getWebProjectNode() {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode("TestApplication-war");
    }

    private Node getEJBProjectNode() {
        if (projectsTab == null) {
            projectsTab = new ProjectsTabOperator();
        }

        return projectsTab.getProjectRootNode("TestApplication-ejb");
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

    @Override
    public void prepare() {
        dataObjectNode.select();
    }

    @Override
    public ComponentOperator open() {
        java.awt.Point point = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        dataObjectNode.tree().clickForPopup(point.x, point.y);
        return new JPopupMenuOperator();
    }
}
