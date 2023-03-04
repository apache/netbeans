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
import org.netbeans.modules.php.editor.verification.TooManyLinesHint.ClassLinesHint;
import org.netbeans.modules.php.editor.verification.TooManyLinesHint.FunctionLinesHint;
import org.netbeans.modules.php.editor.verification.TooManyLinesHint.InterfaceLinesHint;
import org.netbeans.modules.php.editor.verification.TooManyLinesHint.TraitLinesHint;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class TooManyLinesHintTest extends PHPHintsTestBase {

    public TooManyLinesHintTest(String testName) {
        super(testName);
    }

    public void testFunctionsLinesFail() throws Exception {
        checkHints(new FunctionLinesHintStub(5), "testFunctionsLines.php");
    }

    public void testFunctionsLinesOk() throws Exception {
        checkHints(new FunctionLinesHintStub(10), "testFunctionsLines.php");
    }

    public void testClassesLinesFail() throws Exception {
        checkHints(new ClassLinesHintStub(5), "testClassesLines.php");
    }

    public void testClassesLinesOk() throws Exception {
        checkHints(new ClassLinesHintStub(10), "testClassesLines.php");
    }

    public void testInterfacesLinesFail() throws Exception {
        checkHints(new InterfaceLinesHintStub(5), "testInterfacesLines.php");
    }

    public void testInterfacesLinesOk() throws Exception {
        checkHints(new InterfaceLinesHintStub(10), "testInterfacesLines.php");
    }

    public void testTraitsLinesFail() throws Exception {
        checkHints(new TraitLinesHintStub(5), "testTraitsLines.php");
    }

    public void testTraitsLinesOk() throws Exception {
        checkHints(new TraitLinesHintStub(10), "testTraitsLines.php");
    }

    public void testIssue226425() throws Exception {
        checkHints(new FunctionLinesHintStub(5), "testIssue226425.php");
    }

    public void testIssue237620_01() throws Exception {
        checkHints(new FunctionLinesHintStub(10), "testIssue237620_01.php");
    }

    public void testIssue237620_02() throws Exception {
        checkHints(new FunctionLinesHintStub(5), "testIssue237620_02.php");
    }

    public void testIssue237620_03() throws Exception {
        checkHints(new ClassLinesHintStub(14), "testIssue237620_03.php");
    }

    public void testIssue237620_04() throws Exception {
        checkHints(new ClassLinesHintStub(9), "testIssue237620_04.php");
    }

    private static final class FunctionLinesHintStub extends FunctionLinesHint {
        private final int maxAllowedLines;

        public FunctionLinesHintStub(int maxAllowedLines) {
            this.maxAllowedLines = maxAllowedLines;
        }

        @Override
        public int getMaxAllowedLines(Preferences preferences) {
            return maxAllowedLines;
        }

    }

    private static final class ClassLinesHintStub extends ClassLinesHint {
        private final int maxAllowedLines;

        public ClassLinesHintStub(int maxAllowedLines) {
            this.maxAllowedLines = maxAllowedLines;
        }

        @Override
        public int getMaxAllowedLines(Preferences preferences) {
            return maxAllowedLines;
        }

    }

    private static final class InterfaceLinesHintStub extends InterfaceLinesHint {
        private final int maxAllowedLines;

        public InterfaceLinesHintStub(int maxAllowedLines) {
            this.maxAllowedLines = maxAllowedLines;
        }

        @Override
        public int getMaxAllowedLines(Preferences preferences) {
            return maxAllowedLines;
        }

    }

    private static final class TraitLinesHintStub extends TraitLinesHint {
        private final int maxAllowedLines;

        public TraitLinesHintStub(int maxAllowedLines) {
            this.maxAllowedLines = maxAllowedLines;
        }

        @Override
        public int getMaxAllowedLines(Preferences preferences) {
            return maxAllowedLines;
        }

    }

}
