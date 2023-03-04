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
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Test of opening files.
 *
 * @author mmirilovic@netbeans.org
 */
public class OpenWebFilesWithOpenedEditorTest extends OpenWebFilesTest {

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     */
    public OpenWebFilesWithOpenedEditorTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of OpenFiles
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public OpenWebFilesWithOpenedEditorTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(WebSetup.class)
                .addTest(OpenWebFilesWithOpenedEditorTest.class)
                .suite();
    }

    @Override
    public void testOpeningWebXmlFile() {
        super.testOpeningWebXmlFile();
    }

    @Override
    public void testOpeningJSPFile() {
        super.testOpeningJSPFile();
    }

    @Override
    public void testOpeningBigJSPFile() {
        super.testOpeningBigJSPFile();
    }

    @Override
    public void testOpeningHTMLFile() {
        super.testOpeningHTMLFile();
    }

    @Override
    public void testOpeningTagFile() {
        super.testOpeningTagFile();
    }

    @Override
    public void testOpeningTldFile() {
        super.testOpeningTldFile();
    }

    /**
     * Initialize test - open Main.java file in the Source Editor.
     */
    @Override
    public void initialize() {
        super.initialize();
        new OpenAction().perform(new Node(new ProjectsTabOperator().getProjectRootNode("TestWebProject"), "Source Packages|test|Test.java"));
    }
}
