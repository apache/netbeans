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
package org.netbeans.modules.php.editor.lexer;


public class PHP80FeaturesTest extends PHPLexerTestBase {

    public PHP80FeaturesTest(String testName) {
        super(testName);
    }

    public void testMatchExpression_01() throws Exception {
        performTest("lexer/php80/matchExpression_01");
    }

    public void testMatchExpression_02() throws Exception {
        performTest("lexer/php80/matchExpression_02");
    }

    public void testMatchExpression_03() throws Exception {
        performTest("lexer/php80/matchExpression_03");
    }

    public void testMatchExpression_04() throws Exception {
        performTest("lexer/php80/matchExpression_04");
    }

    public void testMixedType_01() throws Exception {
        performTest("lexer/php80/mixedType_01");
    }

    public void testNullsafeOperator_01() throws Exception {
        performTest("lexer/php80/nullsafeOperator_01");
    }

    public void testNullsafeOperator_02() throws Exception {
        performTest("lexer/php80/nullsafeOperator_02");
    }

    public void testNullsafeOperator_03() throws Exception {
        performTest("lexer/php80/nullsafeOperator_03");
    }

    public void testAttributeSyntax_01() throws Exception {
        performTest("lexer/php80/attributeSyntax_01");
    }

    public void testAttributeSyntax_02() throws Exception {
        performTest("lexer/php80/attributeSyntax_02");
    }

    // keyword names are available
    public void testNamedArgumentsWithKeyword_01() throws Exception {
        // no whitespace
        performTest("lexer/php80/namedArgumentsWithKeyword_01");
    }

    public void testNamedArgumentsWithKeyword_02() throws Exception {
        // whitespace " "
        performTest("lexer/php80/namedArgumentsWithKeyword_02");
    }

    public void testNamedArgumentsWithKeyword_03() throws Exception {
        // whitespace "\n"
        performTest("lexer/php80/namedArgumentsWithKeyword_03");
    }

    public void testNamedArgumentsWithKeyword_04() throws Exception {
        // whitespace "\t"
        performTest("lexer/php80/namedArgumentsWithKeyword_04");
    }

    public void testNamedArgumentsWithKeyword_05() throws Exception {
        // contains "parent", "static", "self"
        // "default" in a switch statement (default:)
        // in a match expression, "default" is used with "=>" i.g. (default =>)
        performTest("lexer/php80/namedArgumentsWithKeyword_05");
    }

    public void testNamedArgumentsTypingNullableReturnType_01() throws Exception {
        performTest("lexer/php80/namedArgumentsTypingNullableReturnType_01");
    }

    public void testNamedArgumentsTypingNullableReturnType_02() throws Exception {
        performTest("lexer/php80/namedArgumentsTypingNullableReturnType_02");
    }

    public void testNamedArgumentsTypingNullableReturnType_03() throws Exception {
        performTest("lexer/php80/namedArgumentsTypingNullableReturnType_03");
    }

    public void testNamedArgumentsWithParentSelfStatic_01_lowercase() throws Exception {
        performTest("lexer/php80/namedArgumentsWithParentSelfStatic_01_lowercase");
    }

    public void testNamedArgumentsWithParentSelfStatic_01_uppercase() throws Exception {
        performTest("lexer/php80/namedArgumentsWithParentSelfStatic_01_uppercase");
    }

    public void testNamedArgumentsWithTrueFalseNull_01_lowercase() throws Exception {
        performTest("lexer/php80/namedArgumentsWithTrueFalseNull_01_lowercase");
    }

    public void testNamedArgumentsWithTrueFalseNull_01_uppercase() throws Exception {
        performTest("lexer/php80/namedArgumentsWithTrueFalseNull_01_uppercase");
    }

    public void testNamedArgumentsWithTrueFalseNull_02_lowercase() throws Exception {
        performTest("lexer/php80/namedArgumentsWithTrueFalseNull_02_lowercase");
    }

    public void testNamedArgumentsWithTrueFalseNull_02_uppercase() throws Exception {
        performTest("lexer/php80/namedArgumentsWithTrueFalseNull_02_uppercase");
    }

    public void testNamedArgumentsWithElse_01() throws Exception {
        performTest("lexer/php80/namedArgumentsWithElse_01");
    }

    public void testNamedArgumentsWithElse_02() throws Exception {
        performTest("lexer/php80/namedArgumentsWithElse_02");
    }
}
