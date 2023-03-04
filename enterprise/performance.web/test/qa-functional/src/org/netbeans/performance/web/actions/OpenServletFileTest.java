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
package org.netbeans.performance.web.actions;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of opening files.
 *
 * @author mmirilovic@netbeans.org
 */
public class OpenServletFileTest extends PerformanceTestCase {

    /**
     * Node to be opened/edited
     */
    public static Node openNode;

    /**
     * Folder with data
     */
    public static String fileProject;

    /**
     * Folder with data
     */
    public static String filePackage;

    /**
     * Name of file to open
     */
    public static String fileName;

    /**
     * Menu item name that opens the editor
     */
    public static String menuItem;

    protected static String OPEN = "Open"; //NOI18N

    protected static String EDIT = "Edit"; //NOI18N

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     */
    public OpenServletFileTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenServletFileTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(OpenServletFileTest.class)
                .suite();
    }

    public void testOpeningServletFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "TestWebProject";
        filePackage = "test";
        fileName = "TestServlet.java";
        menuItem = OPEN;
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        disableEditorCaretBlinking();
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
        removeEditorPhaseHandler();
    }

    @Override
    public void prepare() {
        addEditorPhaseHandler();
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode(fileProject), "Source Packages" + '|' + filePackage + '|' + fileName);

        if (openNode == null) {
            fail("Cannot find node [" + "Source Packages" + '|' + filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("========== Open file path =" + openNode.getPath());
    }

    @Override
    public ComponentOperator open() {
        JPopupMenuOperator popup = openNode.callPopup();
        if (popup == null) {
            fail("Cannot get context menu for node [" + "Source Packages" + '|' + filePackage + '|' + fileName + "] in project [" + fileProject + "]");
        } else {
            log("------------------------- after popup invocation ------------");
            try {
                repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
                popup.pushMenu(menuItem);
            } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
                fail("Cannot push menu item " + menuItem + " of node [" + "Source Packages" + '|' + filePackage + '|' + fileName + "] in project [" + fileProject + "]");
            }
        }
        log("------------------------- after open ------------");
        return new EditorOperator(fileName);
    }

    @Override
    public void close() {
        repaintManager().resetRegionFilters(); // added - was missing
        if (testedComponentOperator != null) {
            ((EditorOperator) testedComponentOperator).closeDiscard();
        } else {
            fail("no component to close");
        }
    }
}
