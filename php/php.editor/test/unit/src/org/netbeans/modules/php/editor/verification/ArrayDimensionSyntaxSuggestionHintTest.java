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

public class ArrayDimensionSyntaxSuggestionHintTest extends PHPHintsTestBase {

    public ArrayDimensionSyntaxSuggestionHintTest(String testName) {
        super(testName);
    }

    @Override
    protected String getTestDirectory() {
        return TEST_DIRECTORY + "ArrayDimensionSyntaxSuggestion/";
    }

    public void testDeprecatedCurlyBraces() throws Exception {
        checkHints(new ArrayDimensionSyntaxSuggestionHint(), "deprecatedCurlyBraces.php");
    }

    public void testDeprecatedCurlyBracesFixAll() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHint(), "deprecatedCurlyBraces.php", "$array^{1};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_01() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_01.php", "$array^{1};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_02() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_02.php", "$array{1}{2}{\"t^est\"};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_03a() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_03.php", "$array^{1}{2}{$test};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_03b() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_03.php", "$array{1}^{2}{$test};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_03c() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_03.php", "$array{1}{2}{^$test};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_04() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_04.php", "$array^{getIndex()};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_05() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_05.php", "myFunction(){\"t^est\"};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_06() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_06.php", "[1,2,3]^{0};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_07() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_07.php", "\"string\"{0^};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_08() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_08.php", "CONSTANT{1}{^2};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_09() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_09.php", "MyClass::CONSTANT{^0};", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_10() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_10.php", "((string) $variable->something){0}^;", "Use Bracket Syntax");
    }

    public void testDeprecatedCurlyBracesFix_11() throws Exception {
        applyHint(new ArrayDimensionSyntaxSuggestionHintStub(false), "deprecatedCurlyBraces_11.php", "($a){\"tes^t\"};", "Use Bracket Syntax");
    }

    private static final class ArrayDimensionSyntaxSuggestionHintStub extends ArrayDimensionSyntaxSuggestionHint {

        private final boolean isFixAllEnabled;

        public ArrayDimensionSyntaxSuggestionHintStub(boolean isFixAllEnabled) {
            this.isFixAllEnabled = isFixAllEnabled;
        }

        @Override
        boolean isFixAllEnabled() {
            return isFixAllEnabled;
        }
    }
}
