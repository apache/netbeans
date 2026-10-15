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
package org.netbeans.modules.micronaut.completion;

/**
 *
 * @author Dusan Balek
 */
public class MicronautExpressionLanguageOperatorsCompletionTest extends MicronautExpressionLanguageCompletionTestBase {

    public MicronautExpressionLanguageOperatorsCompletionTest(String name) {
        super(name);
    }

    public void testNumOperators() throws Exception {
        performTest("10 ", 3, "numOperators.pass");
    }

    public void testBeforeNumOperatorPrefix() throws Exception {
        performTest("10 d", 3, "numOperators.pass");
    }

    public void testNumOperatorPrefix() throws Exception {
        performTest("10 d", 4, "div.pass");
    }

    public void testBeforeNumOperator() throws Exception {
        performTest("10 div", 3, "numOperators.pass");
    }

    public void testAfterNumOperator() throws Exception {
        performTest("10 div", 6, "div.pass");
    }

    public void testAfterNumOperatorAndSpace() throws Exception {
        performTest("10 div ", 7, "valueBase.pass");
    }

    public void testAfterNumExpression() throws Exception {
        performTest("10 div 2", 8, "empty.pass");
    }

    public void testAfterNumExpressionAndSpace() throws Exception {
        performTest("10 div 2 ", 9, "numOperators.pass");
    }

    public void testComplexNumExpression1() throws Exception {
        performTest("7 * (3 + 1)", 2, "numOperators.pass");
    }

    public void testComplexNumExpression2() throws Exception {
        performTest("7 * (3 + 1)", 4, "valueBase.pass");
    }

    public void testComplexNumExpression3() throws Exception {
        performTest("7 * (3 + 1)", 4, "valueBase.pass");
    }

    public void testComplexNumExpression4() throws Exception {
        performTest("7 * (3 + 1)", 7, "numOperators.pass");
    }

    public void testComplexNumExpression5() throws Exception {
        performTest("7 * (3 + 1)", 9, "valueBase.pass");
    }

    public void testBeforeCompOperator() throws Exception {
        performTest("10 <=", 3, "numOperators.pass");
    }

    public void testAfterCompOperator() throws Exception {
        performTest("10 <=", 5, "valueBase.pass");
    }

    public void testAfterCompOperatorAndSpace() throws Exception {
        performTest("10 >= ", 6, "valueBase.pass");
    }

    public void testAfterCompExpression() throws Exception {
        performTest("10 >= 2", 7, "empty.pass");
    }

    public void testAfterCompExpressionAndSpace() throws Exception {
        performTest("10 >= 2 ", 8, "allOperators.pass");
    }

    public void testComplexCompExpression1() throws Exception {
        performTest("9 == 3 ^ 2", 2, "numOperators.pass");
    }

    public void testComplexCompExpression2() throws Exception {
        performTest("9 == 3 ^ 2", 5, "nullBase.pass");
    }

    public void testComplexCompExpression3() throws Exception {
        performTest("9 == 3 ^ 2", 7, "allOperators.pass");
    }

    public void testComplexCompExpression4() throws Exception {
        performTest("9 == 3 ^ 2", 9, "valueBase.pass");
    }

    public void testBoolOperators() throws Exception {
        performTest("true ", 5, "boolOperators.pass");
    }

    public void testBeforeBoolOperatorPrefix() throws Exception {
        performTest("true a", 5, "boolOperators.pass");
    }

    public void testBoolOperatorPrefix() throws Exception {
        performTest("true a", 6, "and.pass");
    }

    public void testBeforeBoolOperator() throws Exception {
        performTest("true and", 5, "boolOperators.pass");
    }

    public void testAfterBoolOperator() throws Exception {
        performTest("true and", 8, "and.pass");
    }

    public void testAfterBoolOperatorAndSpace() throws Exception {
        performTest("true and ", 9, "boolBase.pass");
    }

    public void testAfterBoolExpressionAndSpace() throws Exception {
        performTest("true and false ", 15, "boolOperators.pass");
    }

    public void testComplexBoolExpression1() throws Exception {
        performTest("not empty '' or !false", 4, "boolBase.pass");
    }

    public void testComplexBoolExpression2() throws Exception {
        performTest("not empty '' or !false", 10, "valueBase.pass");
    }

    public void testComplexBoolExpression3() throws Exception {
        performTest("not empty '' or !false", 13, "boolOperators.pass");
    }

    public void testComplexBoolExpression4() throws Exception {
        performTest("not empty '' or !false", 16, "boolBase.pass");
    }

    public void testComplexBoolExpression5() throws Exception {
        performTest("not empty '' or !false", 17, "boolBase.pass");
    }

    public void testStringOperators() throws Exception {
        performTest("'ab' ", 5, "stringOperators.pass");
    }

    public void testBeforeStringOperatorPrefix() throws Exception {
        performTest("'ab' m", 5, "stringOperators.pass");
    }

    public void testStringOperatorPrefix() throws Exception {
        performTest("'ab' m", 6, "matches.pass");
    }

    public void testBeforeMatchesOperator() throws Exception {
        performTest("'ab' matches", 5, "stringOperators.pass");
    }

    public void testAfterMatchesOperator() throws Exception {
        performTest("'ab' matches", 12, "matches.pass");
    }

    public void testAfterMatchesOperatorAndSpace() throws Exception {
        performTest("'ab' matches ", 13, "valueBase.pass");
    }

    public void testBeforPatternInMatchesExpression() throws Exception {
        performTest("'ab' matches '[a-z]*'", 13, "valueBase.pass");
    }

    public void testAfterMatchesExpressionAndSpace() throws Exception {
        performTest("'ab' matches '[a-z]*' ", 22, "boolOperators.pass");
    }

    public void testBeforeInstanceOfOperator() throws Exception {
        performTest("'ab' instanceof", 5, "stringOperators.pass");
    }

    public void testAfterInstanceOfOperator() throws Exception {
        performTest("'ab' instanceof", 15, "instanceOf.pass");
    }

    public void testAfterInstanceOfOperatorAndSpace() throws Exception {
        performTest("'ab' instanceof ", 16, "typeReference.pass");
    }

    public void testBeforeTypeInInstanceOfExpression() throws Exception {
        performTest("'ab' instanceof T(String)", 16, "typeReference.pass");
    }

    public void testAfterInstanceOfExpressionAndSpace() throws Exception {
        performTest("'ab' instanceof T(String) ", 26, "boolOperators.pass");
    }

    public void testBeforeQustionInTernaryExpression() throws Exception {
        performTest("15 > 10 ?", 8, "allOperators.pass");
    }

    public void testAfterQustionInTernaryExpression() throws Exception {
        performTest("15 > 10 ?", 9, "base.pass");
    }

    public void testAfterQustionAndSpaceInTernaryExpression() throws Exception {
        performTest("15 > 10 ? ", 10, "base.pass");
    }

    public void testAfterTrueExprInTernaryExpression() throws Exception {
        performTest("15 > 10 ? 'a'", 13, "empty.pass");
    }

    public void testAfterTrueExprAndSpaceInTernaryExpression() throws Exception {
        performTest("15 > 10 ? 'a' ", 14, "empty.pass");
    }

    public void testBeforeColonInTernaryExpression() throws Exception {
        performTest("15 > 10 ? 'a' :", 14, "empty.pass");
    }

    public void testAfterColonInTernaryExpression() throws Exception {
        performTest("15 > 10 ? 'a' :", 15, "base.pass");
    }

    public void testAfterColonAndSpaceInTernaryExpression() throws Exception {
        performTest("15 > 10 ? 'a' : ", 16, "base.pass");
    }

    public void testTernaryExpression1() throws Exception {
        performTest("15 > 10 ? 'a' : 'b'", 3, "numOperators.pass");
    }

    public void testTernaryExpression2() throws Exception {
        performTest("15 > 10 ? 'a' : 'b'", 5, "valueBase.pass");
    }

    public void testTernaryExpression3() throws Exception {
        performTest("15 > 10 ? 'a' : 'b'", 8, "allOperators.pass");
    }

    public void testTernaryExpression4() throws Exception {
        performTest("15 > 10 ? 'a' : 'b'", 10, "base.pass");
    }

    public void testTernaryExpression5() throws Exception {
        performTest("15 > 10 ? 'a' : 'b'", 14, "empty.pass");
    }

    public void testTernaryExpression6() throws Exception {
        performTest("15 > 10 ? 'a' : 'b'", 16, "base.pass");
    }
}
