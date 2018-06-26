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
package org.netbeans.performance.j2ee.actions;

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author mmirilovic@netbeans.org
 */
public class ExpandEJBNodesProjectsViewTest extends PerformanceTestCase {

    /**
     * Name of the folder which test creates and expands
     */
    private static String project;

    /**
     * Path to the folder which test creates and expands
     */
    private static String pathToFolderNode;

    /**
     * Node represantation of the folder which test creates and expands
     */
    private static Node nodeToBeExpanded;

    /**
     * Projects tab
     */
    private static ProjectsTabOperator projectTab;

    /**
     * Project with data for these tests
     */
    private static String testDataProject = "TestApplication-ejb";

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     */
    public ExpandEJBNodesProjectsViewTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandEJBNodesProjectsViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(ExpandEJBNodesProjectsViewTest.class).suite();
    }

    public void testExpandEjbProjectNode() {
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = "";
        doMeasurement();
    }

    public void testExpandEjbNode() {
        WAIT_AFTER_OPEN = 1000;
        WAIT_AFTER_PREPARE = 2000;
        project = testDataProject;
        pathToFolderNode = "Enterprise Beans";
        doMeasurement();
    }

    @Override
    public void initialize() {
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode(testDataProject).collapse();
        repaintManager().addRegionFilter(repaintManager().EXPLORER_FILTER);
    }

    public void prepare() {
        if (pathToFolderNode.equals("")) {
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        } else {
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        }
    }

    public ComponentOperator open() {
        nodeToBeExpanded.tree().doExpandPath(nodeToBeExpanded.getTreePath());
        nodeToBeExpanded.expand();
        return null;
    }

    @Override
    public void close() {
        nodeToBeExpanded.collapse();
    }

    @Override
    public void shutdown() {
        repaintManager().resetRegionFilters();
        projectTab.getProjectRootNode(testDataProject).collapse();
        new RestoreWindowAction().performAPI(projectTab);
    }
}
