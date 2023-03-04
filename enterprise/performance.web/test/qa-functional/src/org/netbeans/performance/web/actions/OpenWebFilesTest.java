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
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
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
public class OpenWebFilesTest extends PerformanceTestCase {

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
    public static String fileFolder;

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

    protected static String WEB_PAGES = "Web Pages"; //NOI18N

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     */
    public OpenWebFilesTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenWebFilesTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(OpenWebFilesTest.class)
                .suite();
    }

    public void testOpeningWebXmlFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "TestWebProject";
        fileFolder = "WEB-INF";
        fileName = "web.xml";
        menuItem = EDIT;
        doMeasurement();
    }

    public void testOpeningJSPFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "TestWebProject";
        fileFolder = "";
        fileName = "Test.jsp";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpeningBigJSPFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "TestWebProject";
        fileFolder = "";
        fileName = "BigJSP.jsp";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpeningHTMLFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "TestWebProject";
        fileFolder = "";
        fileName = "HTML.html";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpeningTagFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "TestWebProject";
        fileFolder = "WEB-INF|tags";
        fileName = "mytag.tag";
        menuItem = OPEN;
        doMeasurement();
    }

    public void testOpeningTldFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "TestWebProject";
        fileFolder = "WEB-INF";
        fileName = "MyTLD.tld";
        menuItem = OPEN;
        doMeasurement();
    }

    @Override
    public void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        addEditorPhaseHandler();
        disableEditorCaretBlinking();
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
        removeEditorPhaseHandler();

    }

    @Override
    public void prepare() {
        System.out.println("PREPARE: " + WEB_PAGES + (fileFolder.equals("") ? "" : "|") + fileFolder + '|' + fileName);
        openNode = new Node(new ProjectsTabOperator().getProjectRootNode(fileProject), WEB_PAGES + (fileFolder.equals("") ? "" : "|") + fileFolder + '|' + fileName);

        if (openNode == null) {
            fail("Cannot find node [" + WEB_PAGES + (fileFolder.equals("") ? "" : "|") + fileFolder + '|' + fileName + "] in project [" + fileProject + "]");
        }
        log("========== Open file path =" + openNode.getPath());
    }

    @Override
    public ComponentOperator open() {
        JPopupMenuOperator popup = openNode.callPopup();
        if (popup == null) {
            fail("Cannot get context menu for node [" + WEB_PAGES + (fileFolder.equals("") ? "" : "|") + fileFolder + '|' + fileName + "] in project [" + fileProject + "]");
        } else {
            log("------------------------- after popup invocation ------------");
            try {
                popup.pushMenu(menuItem);
            } catch (org.netbeans.jemmy.TimeoutExpiredException tee) {
                fail("Cannot push menu item " + menuItem + " of node [" + WEB_PAGES + (fileFolder.equals("") ? "" : "|") + fileFolder + '|' + fileName + "] in project [" + fileProject + "]");
            }
        }
        log("------------------------- after open ------------");
        return null;
    }

    @Override
    public void close() {
        new EditorOperator(fileName).closeDiscard();
    }
}
