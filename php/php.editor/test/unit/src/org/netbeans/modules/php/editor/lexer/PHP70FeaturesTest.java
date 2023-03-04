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

public class PHP70FeaturesTest extends PHPLexerTestBase {

    public PHP70FeaturesTest(String testName) {
        super(testName);
    }

    public void testSpaceship01() throws Exception {
        performTest("lexer/spaceship_01");
    }

    public void testSpaceship02() throws Exception {
        performTest("lexer/spaceship_02");
    }

    public void testCoalesce01() throws Exception {
        performTest("lexer/coalesce_01");
    }

    public void testCoalesce02() throws Exception {
        performTest("lexer/coalesce_02");
    }

    public void testCoalesce03() throws Exception {
        performTest("lexer/coalesce_03");
    }

    public void testCoalesce04() throws Exception {
        performTest("lexer/coalesce_04");
    }

    public void testCoalesce05() throws Exception {
        performTest("lexer/coalesce_05");
    }

    public void testReturnTypes01() throws Exception {
        performTest("lexer/return_types_01");
    }

    public void testReturnTypes02() throws Exception {
        performTest("lexer/return_types_02");
    }

    public void testReturnTypes03() throws Exception {
        performTest("lexer/return_types_03");
    }

    public void testYieldFrom01() throws Exception {
        performTest("lexer/yield_from_01");
    }

    public void testYieldFrom02() throws Exception {
        performTest("lexer/yield_from_02");
    }

    public void testYieldFrom03() throws Exception {
        performTest("lexer/yield_from_03");
    }

    public void testYieldFrom04() throws Exception {
        performTest("lexer/yield_from_04");
    }

    public void testYieldFrom05() throws Exception {
        performTest("lexer/yield_from_05");
    }

    public void testYieldFrom06() throws Exception {
        performTest("lexer/yield_from_06");
    }

    // #262631
    public void testScalarTypes01() throws Exception {
        performTest("lexer/scalar_types_01");
    }

    // #262141
    public void testContextSensitiveLexer_01() throws Exception {
        performTest("lexer/context_sensitive_lexer_01"); // class method
    }

    public void testContextSensitiveLexer_02() throws Exception {
        performTest("lexer/context_sensitive_lexer_02"); // trait method
    }

    public void testContextSensitiveLexer_03() throws Exception {
        performTest("lexer/context_sensitive_lexer_03"); // interface method
    }

    public void testContextSensitiveLexer_04() throws Exception {
        performTest("lexer/context_sensitive_lexer_04"); // class const
    }

    public void testContextSensitiveLexer_05() throws Exception {
        performTest("lexer/context_sensitive_lexer_05"); // interface const
    }

    // check the "function"(PHP_FUNCTION) token after the const keyword
    // it's not PHP_STRING but PHP_FUNCTION
    public void testContextSensitiveLexer_06() throws Exception {
        performTest("lexer/context_sensitive_lexer_06"); // in mixed group uses
    }

    public void testContextSensitiveLexer_07() throws Exception {
        performTest("lexer/context_sensitive_lexer_07"); // in mixed group uses
    }

    public void testContextSensitiveLexer_08() throws Exception {
        // const CONST = [1,3], GOTO = 2;
        performTest("lexer/context_sensitive_lexer_08");
    }

    public void testContextSensitiveLexer_09() throws Exception {
        // const CONST = array("test", array("foo" => 1)), GOTO = 2;
        performTest("lexer/context_sensitive_lexer_09");
    }

}
