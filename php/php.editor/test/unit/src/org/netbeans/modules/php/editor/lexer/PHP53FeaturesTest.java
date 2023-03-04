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

/**
 *
 * @author Petr Pisl
 */
public class PHP53FeaturesTest extends PHPLexerTestBase {

    public PHP53FeaturesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGoto_01() throws Exception {
        performTest("lexer/jump01");
    }

    public void testGoto_02() throws Exception {
        performTest("lexer/jump02");
    }

    public void testGoto_03() throws Exception {
        performTest("lexer/jump03");
    }

    public void testGoto_04() throws Exception {
        performTest("lexer/jump04");
    }

    public void testGoto_05() throws Exception {
        performTest("lexer/jump05");
    }

    public void testGoto_06() throws Exception {
        performTest("lexer/jump06");
    }

    public void testGoto_07() throws Exception {
        performTest("lexer/jump07");
    }

    public void testGoto_08() throws Exception {
        performTest("lexer/jump08");
    }

    public void testGoto_09() throws Exception {
        performTest("lexer/jump09");
    }

    public void testGoto_10() throws Exception {
        performTest("lexer/jump10");
    }

    public void testGoto_11() throws Exception {
        performTest("lexer/jump11");
    }

    public void testGoto_12() throws Exception {
        performTest("lexer/jump12");
    }

    public void testGoto_13() throws Exception {
        performTest("lexer/jump13");
    }

    public void testNowDoc_00() throws Exception {
        performTest("lexer/nowdoc_000");
    }

    public void testNowDoc_01() throws Exception {
        performTest("lexer/nowdoc_001");
    }

    public void testNowDoc_02() throws Exception {
        performTest("lexer/nowdoc_002");
    }

    public void testNowDoc_03() throws Exception {
        performTest("lexer/nowdoc_003");
    }

    public void testNowDoc_04() throws Exception {
        performTest("lexer/nowdoc_004");
    }

    public void testNowDoc_05() throws Exception {
        performTest("lexer/nowdoc_005");
    }

    public void testNowDoc_06() throws Exception {
        performTest("lexer/nowdoc_006");
    }

    public void testNowDoc_07() throws Exception {
        performTest("lexer/nowdoc_007");
    }

    public void testNowDoc_08() throws Exception {
        performTest("lexer/nowdoc_008");
    }

    // 09, 10: old syntax tests
    // we don't provide support for them any longer
    // we can use the new syntax since PHP 7.3

    public void testNowDoc_11() throws Exception {
        performTest("lexer/nowdoc_011");
    }

    public void testNowDoc_12() throws Exception {
        performTest("lexer/nowdoc_012");
    }

    public void testNowDoc_13() throws Exception {
        performTest("lexer/nowdoc_013");
    }

    public void testNowDoc_14() throws Exception {
        performTest("lexer/nowdoc_014");
    }

    public void testNowDoc_15() throws Exception {
        performTest("lexer/nowdoc_015");
    }

    public void testHereDoc53_01() throws Exception {
        performTest("lexer/heredoc_001");
    }

    public void testHereDoc53_02() throws Exception {
        performTest("lexer/heredoc_002");
    }

    public void testHereDoc53_03() throws Exception {
        performTest("lexer/heredoc_003");
    }

    public void testHereDoc53_04() throws Exception {
        performTest("lexer/heredoc_004");
    }

    public void testHereDoc53_05() throws Exception {
        performTest("lexer/heredoc_005");
    }

    public void testHereDoc53_06() throws Exception {
        performTest("lexer/heredoc_006");
    }

    public void testHereDoc53_07() throws Exception {
        performTest("lexer/heredoc_007");
    }

    public void testHereDoc53_08() throws Exception {
        performTest("lexer/heredoc_008");
    }

    // 09, 10: old syntax tests
    // we don't provide support for them any longer
    // we can use the new syntax since PHP 7.3

    public void testHereDoc53_11() throws Exception {
        performTest("lexer/heredoc_011");
    }

    public void testHereDoc53_12() throws Exception {
        performTest("lexer/heredoc_012");
    }

    public void testHereDoc53_13() throws Exception {
        performTest("lexer/heredoc_013");
    }

    public void testHereDoc53_14() throws Exception {
        performTest("lexer/heredoc_014");
    }

    public void testHereDoc53_15() throws Exception {
        performTest("lexer/heredoc_015");
    }

    public void testElvis01() throws Exception {
        performTest("lexer/elvis_01");
    }

    public void testElvis02() throws Exception {
        performTest("lexer/elvis_02");
    }

    public void testElvis03() throws Exception {
        performTest("lexer/elvis_03");
    }

    public void testElvis04() throws Exception {
        performTest("lexer/elvis_04");
    }

    public void testElvis05() throws Exception {
        performTest("lexer/elvis_05");
    }

    public void testElvis06() throws Exception {
        performTest("lexer/elvis_06");
    }

    public void testIssue225549() throws Exception {
        performTest("lexer/issue225549");
    }
}
