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
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.EditorOperator;
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.actions.MaximizeWindowAction;
import org.netbeans.jellytools.actions.RestoreWindowAction;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.web.setup.WebSetup;

/**
 *
 *
 */
public class FileSwitchingTest extends PerformanceTestCase {

    String filenameFrom;
    String filenameTo;

    /**
     * Creates a new instance of FileSwitchingTest
     *
     * @param testName test name
     */
    public FileSwitchingTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of FileSwitchingTest
     *
     * @param testName test name
     * @param performanceDataName test name
     */
    public FileSwitchingTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(FileSwitchingTest.class)
                .suite();
    }

    public void testSwitchJavaToJava() {
        filenameFrom = "Main.java";
        filenameTo = "Test.java";
        doMeasurement();
    }

    public void testSwitchJSPToJSP() {
        filenameFrom = "Test.jsp";
        filenameTo = "BigJsp.jsp";
        expectedTime = 2500;
        doMeasurement();
    }

    public void testSwitchJavaToJSP() {
        filenameFrom = "Test.java";
        filenameTo = "BigJsp.jsp";
        expectedTime = 1200;
        doMeasurement();
    }

    public void testSwitchJSPToXML() {
        filenameFrom = "BigJSP.jsp";
        filenameTo = "build.xml";
        doMeasurement();
    }

    public void testSwitchXMLToJava() {
        filenameFrom = "build.xml";
        filenameTo = "Main.java";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        disableEditorCaretBlinking();

        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node prn = pto.getProjectRootNode("TestWebProject");

        new OpenAction().performAPI(new Node(new SourcePackagesNode("TestWebProject"), "test|Main.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("TestWebProject"), "test|Test.java"));
        new OpenAction().performAPI(new Node(prn, "web|Test.jsp"));
        new OpenAction().performAPI(new Node(prn, "web|BigJSP.jsp"));

        FilesTabOperator fto = FilesTabOperator.invoke();
        Node f = fto.getProjectNode("TestWebProject");

        new OpenAction().performAPI(new Node(f, "build.xml"));
        // maximize to eliminate navigator events
        new MaximizeWindowAction().perform(new EditorOperator("build.xml"));
        CommonUtilities.setSpellcheckerEnabled(false);
    }

    @Override
    public void prepare() {
        EditorOperator eo = new EditorOperator(filenameFrom);
    }

    @Override
    public ComponentOperator open() {
        MY_START_EVENT = ActionTracker.TRACK_OPEN_BEFORE_TRACE_MESSAGE;
        return new EditorOperator(filenameTo);
    }

    @Override
    public void close() {
    }

    @Override
    protected void shutdown() {
        new RestoreWindowAction().performAPI();
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
        CommonUtilities.setSpellcheckerEnabled(true);
    }
}
