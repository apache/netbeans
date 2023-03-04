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
package org.netbeans.modules.java.completion;

import javax.lang.model.SourceVersion;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author arusinha
 */
public class JavaCompletionTask112FeaturesTest extends CompletionTestBase {

    private static String SOURCE_LEVEL = "1.12"; //NOI18N

    public JavaCompletionTask112FeaturesTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        try {
            SourceVersion.valueOf("RELEASE_12"); //NOI18N
            suite.addTestSuite(JavaCompletionTask112FeaturesTest.class);
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_12, skip tests
            suite.addTest(new JavaCompletionTask112FeaturesTest("noop")); //NOI18N
        }
        return suite;
    }

    public void testEnumSwitchValue() throws Exception {
        performTest("SwitchWithEnumValues", 987, null, "switchEnumCaseValues.pass");
    }

    public void testEnumRuleSwitchValue() throws Exception {
        performTest("RuleSwitchWithEnumValues", 987, null, "switchEnumCaseValues.pass");
    }

    public void testSwitchExprEnumCaseValue() throws Exception {
        performTest("SwitchExprWithEnumValues", 994, null, "switchEnumCaseValues.pass");
    }

    public void testSwitchExprEnumCaseValue2() throws Exception {
        performTest("SwitchExprWithEnumValues2", 1020, null, "switchEnumCaseValues2.pass");
    }

    public void testSwitchExprMultiEnumCaseValue() throws Exception {
        performTest("SwitchExprWithMultiEnumValues", 994, null, "switchExprEnumCaseValues.pass");
    }

    public void testSwitchStatementMultiEnumCaseValue() throws Exception {
        performTest("SwitchStatementWithMultiEnumValues", 990, null, "switchExprEnumCaseValues.pass");
    }

    public void testRuleSwitchMultiEnumCaseValue() throws Exception {
        performTest("RuleSwitchWithMultiEnumValues", 1024, null, "ruleSwitchEnumCaseValues.pass");
    }

    public void testRuleSwitchAutoCompleteCaseValue() throws Exception {
        performTest("RuleSwitchAutoCompleteCaseValue", 1013, "ca", "ruleSwitchAutoCompleteCaseValues.pass");
    }

    public void noop() {
    }

}
