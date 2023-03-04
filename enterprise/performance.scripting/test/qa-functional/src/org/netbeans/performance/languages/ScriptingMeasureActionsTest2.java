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
package org.netbeans.performance.languages;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.languages.actions.*;
import org.netbeans.performance.languages.setup.ScriptingSetup;

/**
 *
 * @author mkhramov@netbeans.org, mrkam@netbeans.org
 */
public class ScriptingMeasureActionsTest2 {

    public static NbTestSuite suite() {
        PerformanceTestCase.prepareForMeasurements();

        NbTestSuite suite = new NbTestSuite("Scripting UI Responsiveness Actions suite");
        System.setProperty("suitename", ScriptingMeasureActionsTest2.class.getCanonicalName());
        System.setProperty("suite", "UI Responsiveness Scripting Actions suite");

        suite.addTest(JellyTestCase.emptyConfiguration().reuseUserDir(true)
                .addTest(ScriptingSetup.class)
                .addTest(OpenScriptingFilesTest.class)
                .addTest(TypingInScriptingEditorTest.class)
                .addTest(ScriptingCodeCompletionInEditorTest.class)
                .addTest(PageUpPageDownScriptingEditorTest.class)
                .addTest(CreatePHPProjectTest.class)
                .addTest(CreatePHPSampleProjectTest.class)
                .suite());
        return suite;
    }
}
