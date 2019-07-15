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
public class ImmutableVariablesHintTest extends PHPHintsTestBase {

    public ImmutableVariablesHintTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "ImmutableVariablesHint/";
    }

    public void testWith1AllowedAssignments() throws Exception {
        checkHints(new ImmutablevariablesHintStub(1), "testImmutableVariablesHint.php");
    }

    public void testArrowFunctionsWith1AllowedAssignments() throws Exception {
        checkHints(new ImmutablevariablesHintStub(1), "testImmutableVariablesHintArrowFunctions.php");
    }

    private class ImmutablevariablesHintStub extends ImmutableVariablesHint {
        private final int numberOfAllowedAssignments;

        public ImmutablevariablesHintStub(final int numberOfAllowedAssignments) {
            this.numberOfAllowedAssignments = numberOfAllowedAssignments;
        }

        @Override
        public int getNumberOfAllowedAssignments(Preferences preferences) {
            return numberOfAllowedAssignments;
        }

    }

}
