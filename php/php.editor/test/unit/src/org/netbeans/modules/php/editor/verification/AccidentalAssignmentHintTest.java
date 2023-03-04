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
package org.netbeans.modules.php.editor.verification;

import java.util.prefs.Preferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AccidentalAssignmentHintTest extends PHPHintsTestBase {

    public AccidentalAssignmentHintTest(String testName) {
        super(testName);
    }

    public void testInSubAndInWhile() throws Exception {
        checkHints(new AccidentalAssignmentHintStub(true, true), "testAccidentalAssignmentHint.php");
    }

    public void testInSubAndNotInWhile() throws Exception {
        checkHints(new AccidentalAssignmentHintStub(true, false), "testAccidentalAssignmentHint.php");
    }

    public void testNotInSubAndNotInWhile() throws Exception {
        checkHints(new AccidentalAssignmentHintStub(false, false), "testAccidentalAssignmentHint.php");
    }

    public void testNotInSubAndInWhile() throws Exception {
        checkHints(new AccidentalAssignmentHintStub(false, true), "testAccidentalAssignmentHint.php");
    }

    private class AccidentalAssignmentHintStub extends AccidentalAssignmentHint {
        private final boolean assignmentsInSubStatements;
        private final boolean assignmentsInWhileStatements;

        public AccidentalAssignmentHintStub(boolean assignmentsInSubStatements, boolean assignmentsInWhileStatements) {
            this.assignmentsInSubStatements = assignmentsInSubStatements;
            this.assignmentsInWhileStatements = assignmentsInWhileStatements;
        }

        @Override
        public boolean checkAssignmentsInSubStatements(Preferences preferences) {
            return assignmentsInSubStatements;
        }

        @Override
        public boolean checkAssignmentsInWhileStatements(Preferences preferences) {
            return assignmentsInWhileStatements;
        }

    }

}
