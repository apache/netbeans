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
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of opening files if Editor is already opened. OpenFilesNoCloneableEditor
 * is used as a base for tests of opening files when editor is already opened.
 *
 * @author mmirilovic@netbeans.org
 */
public class OpenFilesNoCloneableEditorWithOpenedEditorTest extends OpenFilesNoCloneableEditorTest {

    /**
     * Name of file to pre-open
     */
    public static String fileName_preopen;

    /**
     * Creates a new instance of OpenFilesNoCloneableEditor
     *
     * @param testName the name of the test
     */
    public OpenFilesNoCloneableEditorWithOpenedEditorTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of OpenFilesNoCloneableEditorWithOpenedEditor
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenFilesNoCloneableEditorWithOpenedEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(OpenFilesNoCloneableEditorWithOpenedEditorTest.class)
                .suite();
    }

    @Override
    public void testOpening20kBPropertiesFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "Bundle20kB.properties";
        fileName_preopen = "Bundle.properties";
        doMeasurement();
    }

    @Override
    public void testOpening20kBPictureFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "splash.gif";
        fileName_preopen = "Main.java";
        doMeasurement();
    }

    @Override
    public void testOpening20kBFormFile() {
        WAIT_AFTER_OPEN = 2000;
        fileProject = "PerformanceTestData";
        filePackage = "org.netbeans.test.performance";
        fileName = "JFrame20kB.java";
        fileName_preopen = "Main.java";
        expectedTime = 2000;
        doMeasurement();
    }

    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    @Override
    public void initialize() {
        super.initialize();
        SourcePackagesNode spn = new SourcePackagesNode("PerformanceTestData");
        Node n = new Node(spn, "org.netbeans.test.performance|" + fileName_preopen);
        new OpenAction().performAPI(n);
    }
}
