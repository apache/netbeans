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
public class UninitializedVariableHintTest extends PHPHintsTestBase {

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "UninitializedVariableHint/";
    }

    public UninitializedVariableHintTest(String testName) {
        super(testName);
    }

    public void testWithRefs() throws Exception {
        checkHints(new UninitializedVariableHintStub(true), "testUninitializedVariableHint.php");
    }

    public void testWithoutRefs() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testUninitializedVariableHint.php");
    }

    public void testIssue225818() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testIssue225818.php");
    }

    public void testGlobalContext() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testGlobalContext.php");
    }

    public void testIssue226041() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testIssue226041.php");
    }

    public void testIssue233268() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testIssue233268.php");
    }

    public void testIssue246125() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testIssue246125.php");
    }

    public void testIssue257454() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testIssue257454.php");
    }

    public void testIssue249508() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testIssue249508.php");
    }

    // PHP 7.4
    public void testArrowFunctions() throws Exception {
        checkHints(new UninitializedVariableHintStub(false), "testArrowFunctions.php");
    }

    private class UninitializedVariableHintStub extends UninitializedVariableHint {
        private final boolean uninitializedVariable;

        public UninitializedVariableHintStub(boolean uninitializedVariable) {
            this.uninitializedVariable = uninitializedVariable;
        }

        @Override
        public boolean checkVariablesInitializedByReference(Preferences preferences) {
            return uninitializedVariable;
        }

    }

}
