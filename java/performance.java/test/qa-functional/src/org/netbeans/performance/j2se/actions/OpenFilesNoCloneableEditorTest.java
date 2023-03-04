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
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of opening files, where CloneableEditor isn't super class of their
 * editor.
 *
 * @author mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditorTest extends PerformanceTestCase {

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
     * Creates a new instance of OpenFilesNoCloneableEditor
     *
     * @param testName the name of the test
     */
    public OpenFilesNoCloneableEditorTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesNoCloneableEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(OpenFilesNoCloneableEditorTest.class)
                .suite();
    }

    public void testOpening20kBPropertiesFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Bundle20kB.properties";
        doMeasurement();
    }

    public void testOpening20kBPictureFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        doMeasurement();
    }

    public void testOpening20kBFormFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "JFrame20kB.java";
        expectedTime = 4000;
        doMeasurement();
    }

    @Override
    protected void initialize() {
        EditorOperator.closeDiscardAll();
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);
    }

    @Override
    protected void shutdown() {
        EditorOperator.closeDiscardAll();
        repaintManager().resetRegionFilters();
    }

    @Override
    public ComponentOperator open() {
        JPopupMenuOperator popup = openNode.callPopup();
        popup.pushMenu("Open");
        return new TopComponentOperator(fileName);
    }

    @Override
    public void close() {
        new TopComponentOperator(fileName).closeDiscard();
    }

    @Override
    public void prepare() {
        openNode = new Node(new SourcePackagesNode(fileProject), filePackage + '|' + fileName);
    }
}
