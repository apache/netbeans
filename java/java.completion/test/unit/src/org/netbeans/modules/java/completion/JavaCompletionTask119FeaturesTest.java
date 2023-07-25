/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.completion;

import org.netbeans.modules.java.source.parsing.JavacParser;

/**
 *
 * @author Dusan Balek
 */
public class JavaCompletionTask119FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask119FeaturesTest(String testName) {
        super(testName);
    }

    public void testRecordPatternCompletion_1() throws Exception {
        performTest("RecordPattern", 930, null, "AutoCompletion_RecordPattern_1.pass", "21");
    }

    public void testRecordPatternCompletion_2() throws Exception {
        performTest("RecordPattern", 1013, null, "AutoCompletion_RecordPattern_2.pass", "21");
    }

    public void testRecordPatternCompletion_3() throws Exception {
        performTest("RecordPattern", 1107, null, "AutoCompletion_RecordPattern_3.pass", "21");
    }

    public void testCasePatternGuard_1() throws Exception {
        performTest("SwitchPatternMatching", 1080, null, "AutoCompletion_Guard_PatternMatchingSwitch.pass", "21");
    }

    public void testCasePatternGuard_2() throws Exception {
        performTest("SwitchPatternMatching", 1193, null, "AutoCompletion_Guard_PatternMatchingSwitch.pass", "21");
    }

    public void TODOtestNoCrash() throws Exception {
        performTest("SwitchPatternMatching", 1275, "case R(var s,", "AutoCompletion_Guard_PatternMatchingSwitch.pass", "21");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }

}
