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
package org.netbeans.performance.j2se.actions;

import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of expanding nodes/folders in the Explorer.
 *
 * @author mmirilovic@netbeans.org
 */
public class ExpandNodesProjectsViewTest extends PerformanceTestCase {

    /**
     * Name of the folder which test creates and expands
     */
    protected String project;
    /**
     * Path to the folder which test creates and expands
     */
    protected String pathToFolderNode;
    /**
     * Node representation of the folder which test creates and expands
     */
    protected Node nodeToBeExpanded;
    /**
     * Projects tab
     */
    protected ProjectsTabOperator projectTab;

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     */
    public ExpandNodesProjectsViewTest(String testName) {
        super(testName);
        WAIT_AFTER_OPEN = 100;
    }

    /**
     * Creates a new instance of ExpandNodesInExplorer
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public ExpandNodesProjectsViewTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        WAIT_AFTER_OPEN = 100;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(ExpandNodesProjectsViewTest.class)
                .suite();
    }

    public void testExpandProjectNode() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = "";
        expectedTime = 200;
        doMeasurement();
    }

    public void testExpandSourcePackagesNode() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES;
        expectedTime = 200;
        doMeasurement();
    }

    public void testExpandFolderWith50JavaFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder50";
        expectedTime = 300;
        doMeasurement();
    }

    public void testExpandFolderWith100JavaFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder100";
        expectedTime = 500;
        doMeasurement();
    }

    public void testExpandFolderWith1000JavaFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.javaFolder1000";
        expectedTime = 2500;
        doMeasurement();
    }

    public void testExpandFolderWith100XmlFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.xmlFolder100";
        expectedTime = 2000;
        doMeasurement();
    }

    public void testExpandFolderWith100TxtFiles() {
        project = "PerformanceTestFoldersData";
        pathToFolderNode = CommonUtilities.SOURCE_PACKAGES + "|folders.txtFolder100";
        expectedTime = 600;
        doMeasurement();
    }

    @Override
    public void initialize() {
        projectTab = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(projectTab);
        projectTab.getProjectRootNode("PerformanceTestFoldersData").collapse();
        repaintManager().addRegionFilter(LoggingRepaintManager.EXPLORER_FILTER);
    }

    @Override
    public void prepare() {
        if (pathToFolderNode.equals("")) {
            nodeToBeExpanded = projectTab.getProjectRootNode(project);
        } else {
            nodeToBeExpanded = new Node(projectTab.getProjectRootNode(project), pathToFolderNode);
        }
        nodeToBeExpanded.collapse();
    }

    @Override
    public ComponentOperator open() {
        // wait only for expansion and ignore other repaint events (badging etc.)
        MY_END_EVENT = ActionTracker.TRACK_OPEN_AFTER_TRACE_MESSAGE;
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
        projectTab.getProjectRootNode(project).collapse();
        projectTab.close();
        ProjectsTabOperator.invoke();
    }
}
