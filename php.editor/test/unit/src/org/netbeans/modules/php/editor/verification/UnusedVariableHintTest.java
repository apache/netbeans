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

    private class UnusedVariableHintStub extends UnusedVariableHint {
        private final boolean unusedFormalParameters;

        public UnusedVariableHintStub(boolean unusedFormalParameters) {
            this.unusedFormalParameters = unusedFormalParameters;
        }

        @Override
        public boolean checkUnusedFormalParameters(Preferences preferences) {
            return unusedFormalParameters;
        }

    }

}
