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
package org.netbeans.modules.php.editor.verification;

import java.util.prefs.Preferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UnusedVariableHintTest extends PHPHintsTestBase {

    public UnusedVariableHintTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "UnusedVariableHint/";
    }

    public void testWithParams() throws Exception {
        checkHints(new UnusedVariableHintStub(true), "testUnusedVariableHint.php");
    }

    public void testWithoutParams() throws Exception {
        checkHints(new UnusedVariableHintStub(false), "testUnusedVariableHint.php");
    }

    public void testUnusedInGlobalProgramContext() throws Exception {
        checkHints(new UnusedVariableHintStub(false), "testUnusedInGlobalProgramContext.php");
    }

    public void testUnusedInGlobalNamespaceContext() throws Exception {
        checkHints(new UnusedVariableHintStub(false), "testUnusedInGlobalNamespaceContext.php");
    }

    public void testIssue230297() throws Exception {
        checkHints(new UnusedVariableHintStub(false), "testIssue230297.php");
    }

    public void testIssue246230() throws Exception {
        checkHints(new UnusedVariableHintStub(false), "testIssue246230.php");
    }

    // NETBEANS-1718
    public void testUnusedVariableInInheritedMethodParameters_01() throws Exception {
        checkHints(new UnusedVariableHintStub(true, true), "testUnusedVariableInInheritedMethodParameters.php");
    }

    public void testUnusedVariableInInheritedMethodParameters_02() throws Exception {
        checkHints(new UnusedVariableHintStub(true, false), "testUnusedVariableInInheritedMethodParameters.php");
    }

    public void testUnusedVariableInInheritedMethodParameters_03() throws Exception {
        checkHints(new UnusedVariableHintStub(false, true), "testUnusedVariableInInheritedMethodParameters.php");
    }

    public void testUnusedVariableInInheritedMethodParameters_04() throws Exception {
        checkHints(new UnusedVariableHintStub(false, false), "testUnusedVariableInInheritedMethodParameters.php");
    }

    // PHP 7.4
    public void testUnusedVariableArrowFunctionsWithParams_01() throws Exception {
        checkHints(new UnusedVariableHintStub(true), "testUnusedVariableHintArrowFunctions.php");
    }

    public void testUnusedVariableArrowFunctionsWithoutParams_01() throws Exception {
        checkHints(new UnusedVariableHintStub(false), "testUnusedVariableHintArrowFunctions.php");
    }

    public void testUnusedVariableArrowFunctionsNestedWithParams_01() throws Exception {
        checkHints(new UnusedVariableHintStub(true), "testUnusedVariableHintArrowFunctionsNested.php");
    }

    public void testUnusedVariableArrowFunctionsNestedWithoutParams_01() throws Exception {
        checkHints(new UnusedVariableHintStub(false), "testUnusedVariableHintArrowFunctionsNested.php");
    }

    private class UnusedVariableHintStub extends UnusedVariableHint {
        private final boolean unusedFormalParameters;
        private final boolean inheritedMethodParameters;

        public UnusedVariableHintStub(boolean unusedFormalParameters) {
            this.unusedFormalParameters = unusedFormalParameters;
            this.inheritedMethodParameters = false;
        }

        public UnusedVariableHintStub(boolean unusedFormalParameters, boolean inheritedMethodParameters) {
            this.unusedFormalParameters = unusedFormalParameters;
            this.inheritedMethodParameters = inheritedMethodParameters;
        }

        @Override
        public boolean checkUnusedFormalParameters(Preferences preferences) {
            return unusedFormalParameters;
        }

        @Override
        public boolean checkInheritedMethodParameters(Preferences preferences) {
            return inheritedMethodParameters;
        }

    }

}
