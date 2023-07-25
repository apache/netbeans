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
 * @author aksinsin
 */
public class JavaCompletionTask117FeaturesTest extends CompletionTestBase {

    public JavaCompletionTask117FeaturesTest(String testName) {
        super(testName);
    }

    public void testCaseLabels_1() throws Exception {
        performTest("SwitchPatternMatching", 998, null, "AutoCompletion_CaseLabels_PatternMatchingSwitch_1.pass", "21");
    }

    public void testCaseLabels_2() throws Exception {
        performTest("SwitchPatternMatching", 1004, null, "AutoCompletion_CaseLabels_PatternMatchingSwitch_2.pass", "21");
    }

    public void testCaseLabels_3() throws Exception {
        performTest("SwitchPatternMatching", 1011, "s, ", "AutoCompletion_CaseLabels_PatternMatchingSwitch_3.pass", "21");
    }

    public void testVariableNameSuggestion() throws Exception {
        performTest("SwitchPatternMatching", 1011, null, "AutoCompletion_VarNameSuggestion_PatternMatchingSwitch.pass", "21");
    } 

    public void testClassMembersAutoCompletion_GuardedPattern() throws Exception {
        performTest("SwitchPatternMatching", 1087, null, "AutoCompletion_MembersSelect_GuardedPatternMatchingSwitch.pass", "21");
    }

    public void testClassMembersAutoCompletion_GuardedPattern_1() throws Exception {
        performTest("SwitchPatternMatching", 1095, null, "AutoCompletion_MembersSelect_GuardedPatternMatchingSwitch_1.pass", "21");
    }
    public void testClassMembersAutoCompletion_GuardedPattern_2() throws Exception {
        performTest("SwitchPatternMatching", 1115, null, "AutoCompletion_MembersSelect_GuardedPatternMatchingSwitch_2.pass", "21");
    }
    public void testClassMembersAutoCompletion_ParanthesizedPattern() throws Exception {
        performTest("SwitchPatternMatching", 1204, null, "AutoCompletion_MembersSelect_ParenthesizedPatternMatchingSwitch.pass", "21");
    }
    public void testClassMembersAutoCompletion_ParanthesizedPattern_1() throws Exception {
        performTest("SwitchPatternMatching", 1227, null, "AutoCompletion_MembersSelect_ParenthesizedPatternMatchingSwitch_1.pass", "21");
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
    
}
