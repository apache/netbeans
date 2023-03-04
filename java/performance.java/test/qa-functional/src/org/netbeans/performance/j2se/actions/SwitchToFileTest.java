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
package org.netbeans.performance.j2se.actions;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import static org.netbeans.junit.NbModuleSuite.emptyConfiguration;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 *
 *
 */
public class SwitchToFileTest extends PerformanceTestCase {

    String filenameFrom;
    String filenameTo;

    /**
     * Creates a new instance of SwitchToFile
     *
     * @param testName test name
     */
    public SwitchToFileTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of SwitchToFile
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public SwitchToFileTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenFoldersProject")
                .addTest(SwitchToFileTest.class)
                .suite();
    }

    public void testSwitchJavaToJava() {
        filenameFrom = "SampleJavaClass001.java";
        filenameTo = "SampleJavaClass000.java";
        doMeasurement();
    }

    public void testSwitchJSPToJSP() {
        filenameFrom = "Test.jsp";
        filenameTo = "index.jsp";
        doMeasurement();
    }

    public void testSwitchJavaToJSP() {
        filenameFrom = "SampleJavaClass000.java";
        filenameTo = "test.jsp";
        doMeasurement();
    }

    public void testSwitchJSPToXML() {
        filenameFrom = "Test.jsp";
        filenameTo = "build.xml";
        doMeasurement();
    }

    public void testSwitchXMLToJSP() {
        filenameFrom = "build.xml";
        filenameTo = "Test.jsp";
        doMeasurement();
    }

    @Override
    protected void initialize() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|SampleJavaClass000.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders.javaFolder50|SampleJavaClass001.java"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders|Test.jsp"));
        new OpenAction().performAPI(new Node(new SourcePackagesNode("PerformanceTestFoldersData"), "folders|index.jsp"));
        FilesTabOperator fto = FilesTabOperator.invoke();
        Node f = fto.getProjectNode("PerformanceTestFoldersData");
        new OpenAction().performAPI(new Node(f, "build.xml"));
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
    public void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
        CommonUtilities.setSpellcheckerEnabled(true);
    }
}
