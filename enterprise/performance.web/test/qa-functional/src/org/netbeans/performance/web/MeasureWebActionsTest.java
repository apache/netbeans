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
package org.netbeans.performance.web;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.web.actions.*;
import org.netbeans.performance.web.setup.WebSetup;

/**
 * Measure UI-RESPONSIVENES and WINDOW_OPENING.
 *
 * @author mmirilovic@netbeans.org
 */
public class MeasureWebActionsTest {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("UI Responsiveness Web Actions suite");
        System.setProperty("suitename", MeasureWebActionsTest.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness Web Actions suite");

        suite.addTest(JellyTestCase.emptyConfiguration().reuseUserDir(true)
                .addTest(WebSetup.class)
                .addTest(ExpandNodesWebProjectsViewTest.class)
                .addTest(FileSwitchingTest.class)
                .addTest(JSPCompletionInJspEditorTest.class)
                .addTest(OpenServletFileTest.class)
                .addTest(OpenServletFileWithOpenedEditorTest.class)
                .addTest(OpenWebFilesTest.class)
                .addTest(OpenWebFilesWithOpenedEditorTest.class)
                .addTest(PageUpPageDownInJspEditorTest.class)
                .addTest(PasteInJspEditorTest.class)
                .addTest(ToggleBreakpointTest.class)
                .addTest(TypingInJspEditorTest.class)
                .suite());
        return suite;
    }
}
