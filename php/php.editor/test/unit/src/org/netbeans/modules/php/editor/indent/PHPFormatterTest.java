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
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;

/**
 *
 * @author Petr Pisl, Ondrej Brejla
 */
public class PHPFormatterTest extends PHPFormatterTestBase {

    public PHPFormatterTest(String testName) {
        super(testName);
    }

    private void reformatFileContents(String file) throws Exception {
        reformatFileContents(file, 0);
    }

    private void reformatFileContents(String file, int initialIndent) throws Exception {
        reformatFileContents(file, new IndentPrefs(2, 2), initialIndent);
    }

    public void test174595() throws Exception {
        reformatFileContents("testfiles/formatting/issue174595.php");
    }

    public void testContinuedExpression() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, false);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, false);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/continued_expression.php", options);
    }

    // NETBEANS-3391
    public void testContinuedExpression_psr12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, true);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/continued_expression_psr12.php", options);
    }

    public void testContinuedExpression2() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, false);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, false);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/continued_expression2.php", options);
    }

    public void testIfelseNobrackets() throws Exception {
        reformatFileContents("testfiles/formatting/ifelse_nobrackets.php");
    }

    public void testMultilineFunctionHeader() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_PARAMS_AFTER_LEFT_PAREN, false);
        options.put(FmtOptions.WRAP_METHOD_PARAMS_RIGHT_PAREN, false);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/multiline_function_header.php", options);
    }

    // NETBEANS-3391
    public void testMultilineFunctionHeader_psr12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_PARAMS_AFTER_LEFT_PAREN, true);
        options.put(FmtOptions.WRAP_METHOD_PARAMS_RIGHT_PAREN, true);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/multiline_function_header_psr12.php", options);
    }

    public void testLineSplitting1() throws Exception {
        reformatFileContents("testfiles/formatting/line_splitting1.php");
    }

    public void testLineSplitting2() throws Exception {
        reformatFileContents("testfiles/formatting/line_splitting2.php");
    }

    public void testHereDoc() throws Exception {
        reformatFileContents("testfiles/formatting/heredoc.php");
    }

    public void testSimpleClassDef() throws Exception {
        reformatFileContents("testfiles/formatting/simple_class_def.php");
    }

    public void testSwitchStmt() throws Exception {
        reformatFileContents("testfiles/formatting/switch_stmt.php");
    }

    public void testSwitchStmt01() throws Exception {
        reformatFileContents("testfiles/formatting/switch_stmt01.php");
    }

    public void testArrays1() throws Exception {
        reformatFileContents("testfiles/formatting/arrays1.php");
    }

    public void testArrays2() throws Exception {
        reformatFileContents("testfiles/formatting/arrays2.php");
    }

    public void testArrays3() throws Exception {
        reformatFileContents("testfiles/formatting/arrays3.php");
    }

    public void testArrays4() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.ITEMS_IN_ARRAY_DECLARATION_INDENT_SIZE, 6);
        reformatFileContents("testfiles/formatting/arrays4.php", options);
    }

    public void testArrays05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/arrays5.php", options);
    }

    public void testShortArrays1() throws Exception {
        reformatFileContents("testfiles/formatting/shortArrays1.php");
    }

    public void testShortArrays2() throws Exception {
        reformatFileContents("testfiles/formatting/shortArrays2.php");
    }

    public void testShortArrays3() throws Exception {
        reformatFileContents("testfiles/formatting/shortArrays3.php");
    }

    public void testShortArrays4() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        options.put(FmtOptions.ITEMS_IN_ARRAY_DECLARATION_INDENT_SIZE, 6);
        reformatFileContents("testfiles/formatting/shortArrays4.php", options);
    }

    public void testShortArrays05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/shortArrays5.php", options);
    }

    public void testFragment1() throws Exception {
        reformatFileContents("testfiles/formatting/format_fragment1.php");
    }

    public void testNestedArrays1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/nested_array1.php", options);
    }

    public void testNestedShortArrays1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/nested_short_array1.php", options);
    }

    public void testSubsequentQuotes() throws Exception {
        reformatFileContents("testfiles/formatting/subsequentquotes.php");
    }

    public void testMultilineString() throws Exception {
        reformatFileContents("testfiles/formatting/multiline_string.php");
    }

    public void testInitialIndent1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 5);
        reformatFileContents("testfiles/formatting/initial_indent1.php", options);
    }

    public void testInitialIndent01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 0);
        reformatFileContents("testfiles/formatting/initialIndent01.php", options);
    }

    public void testIfElseAlternativeSyntax() throws Exception {
        reformatFileContents("testfiles/formatting/ifelse_alternative_syntax.php");
    }

    public void testNamespaces1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces1.php", options);
    }

    public void testNamespaces02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces_02.php", options);
    }

    public void testNamespaces03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/namespaces_03.php", options);
    }

    public void testNamespaces04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/namespaces_04.php", options);
    }

    public void testNamespaces05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/namespaces_05.php", options);
    }

    public void test161049() throws Exception {
        reformatFileContents("testfiles/formatting/issue161049.php");
    }

    public void test172259() throws Exception {
        reformatFileContents("testfiles/formatting/issue172259.php");
    }

    public void test171309() throws Exception {
        reformatFileContents("testfiles/formatting/issue171309.php");
    }

    public void test162126() throws Exception {
        reformatFileContents("testfiles/formatting/issue162126.php");
    }

    public void test162785() throws Exception {
        reformatFileContents("testfiles/formatting/issue162785.php");
    }

    public void test162586() throws Exception {
        reformatFileContents("testfiles/formatting/issue162586.php");
    }

    public void test176453() throws Exception {
        reformatFileContents("testfiles/formatting/issue176453.php");
    }

    public void test165762() throws Exception {
        reformatFileContents("testfiles/formatting/issue165762.php");
    }

    public void test166550() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue166550.php", options);
    }

    public void test159339_161408() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issues_159339_161408.php", options);
    }

    public void test164219() throws Exception {
        reformatFileContents("testfiles/formatting/issue164219.php");
    }

    public void test162320() throws Exception {
        reformatFileContents("testfiles/formatting/issue162320.php");
    }

    public void test173906_dowhile() throws Exception {
        reformatFileContents("testfiles/formatting/issue173906_dowhile.php");
    }

    public void test164381() throws Exception {
        reformatFileContents("testfiles/formatting/issue164381.php");
    }

    public void test174544() throws Exception {
        reformatFileContents("testfiles/formatting/issue174544.php");
    }

    public void test174563() throws Exception {
        reformatFileContents("testfiles/formatting/issue174563.php");
    }

    public void test172475() throws Exception {
        reformatFileContents("testfiles/formatting/issue172475.php");
    }

    public void test167791() throws Exception {
        reformatFileContents("testfiles/formatting/issue167791.php", 5);
    }

    public void test176224() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, false);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, false);
        reformatFileContents("testfiles/formatting/issue176224.php", options);
    }

    // NETBEANS-3391
    public void test176224_psr12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, true);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, true);
        reformatFileContents("testfiles/formatting/issue176224_psr12.php", options);
    }

    public void testIssue181588() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue181588.php", options);
    }

    public void testLineComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/lineComment01.php", options);
    }

    public void testLineComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/lineComment02.php", options);
    }

    public void testLineComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/lineComment03.php", options);
    }

    public void testLineComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/lineComment04.php", options);
    }

    public void testLineComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/lineComment05.php", options);
    }

    public void testComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment01.php", options);
    }

    public void testComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment02.php", options);
    }

    public void testComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment03.php", options);
    }

    public void testComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment04.php", options);
    }

    public void testComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment05.php", options);
    }

    public void testComment06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment06.php", options);
    }

    public void testComment07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        // Be careful during editing the test file. The space after /*  is important.
        reformatFileContents("testfiles/formatting/comment07.php", options);
    }

    public void testComment08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment08.php", options);
    }

    public void testComment09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment09.php", options);
    }

    public void testComment10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment10.php", options);
    }

    public void testComment11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/comment11.php", options);
    }

    public void test183200_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_01.php", options);
    }

    public void test183200_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_02.php", options);
    }

    public void test182072_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue182072_01.php", options);
    }

    public void test180332_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue180332_01.php", options);
    }

    public void test168396_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, false);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, false);
        reformatFileContents("testfiles/formatting/issue168396_01.php", options);
    }

    // NETBEANS-3391
    public void test168396_01_psr12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, true);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, true);
        reformatFileContents("testfiles/formatting/issue168396_01_psr12.php", options);
    }

    public void testIssue184687_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/issue184687_01.php", options);
    }

    public void testIssue184687_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.INITIAL_INDENT, 4);
        reformatFileContents("testfiles/formatting/issue184687_02.php", options);
    }

    public void testIssue185353_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_01.php", options);
    }

    public void testIssue185353_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_02.php", options);
    }

    public void testIssue185353_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_03.php", options);
    }

    public void testIssue185353_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_04.php", options);
    }

    public void testIssue185353_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_05.php", options);
    }

    public void testIssue185353_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_06.php", options, true);
    }

    public void testIssue189835_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_01.php", options);
    }

    public void testIssue189835_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_02.php", options);
    }

    public void testIssue189835_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_03.php", options);
    }

    public void testIssue189835_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_04.php", options);
    }

    public void testIssue189835_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_05.php", options);
    }

    public void testIssue189835_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_06.php", options);
    }

    public void testIssue190426() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue190426.php", options);
    }

    public void testIssue197617_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue197617_01.php", options);
    }

    public void testIssue197304_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue197304_01.php", options);
    }

    public void testIssue199298_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue199298_01.php", options);
    }

    public void testIssue196405() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, false);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, false);
        reformatFileContents("testfiles/formatting/issue196405.php", options);
    }

    // NETBEANS-3391
    public void testIssue196405_psr12() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, true);
        options.put(FmtOptions.WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, true);
        reformatFileContents("testfiles/formatting/issue196405_psr12.php", options);
    }

    public void testIssue197698() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue197698.php", options);
    }

    public void testIssue199654() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue199654.php", options);
    }

    public void testTraitUsesBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/TraitUses01.php", options);
    }

    public void testTraitUsesBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/TraitUses02.php", options);
    }

    public void testTraitUsesBracePlacement_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/TraitUses03.php", options);
    }

    public void testSpaceship01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaceship01.php", options);
    }

    public void testIssue187757() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue187757.php", options);
    }

    public void testIssue218877() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue218877.php", options);
    }

    public void testIssue218013() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue218013.php", options);
    }

    public void testIssue190010_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue190010_01.php", options);
    }

    public void testIssue190010_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue190010_02.php", options);
    }

    public void testIssue190010_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue190010_03.php", options);
    }

    public void testIssue190010_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue190010_04.php", options);
    }

    public void testIssue188431_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue188431_01.php", options);
    }

    public void testIssue188431_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue188431_02.php", options);
    }

    public void testIssue188431_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue188431_03.php", options);
    }

    public void testIssue229961() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue229961.php", options);
    }

    public void testIssue227287() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue227287.php", options);
    }

    public void testIssue233353_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue233353_01.php", options);
    }

    public void testIssue233353_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue233353_02.php", options);
    }

    public void testIssue233353_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue233353_03.php", options);
    }

    public void testIssue233353_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue233353_04.php", options);
    }

    public void testIssue235239_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue235239_01.php", options);
    }

    public void testIssue235239_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue235239_02.php", options);
    }

    public void testIssue228401() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue228401.php", options);
    }

    public void testIssue235181() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue235181.php", options);
    }

    public void testIssue240649() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue240649.php", options);
    }

    public void testIssue243593() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue243593.php", options);
    }

    public void testIssue257241() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue257241.php", options);
    }

    public void testIssue247047() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue247047.php", options);
    }

    public void testAnonymousClass() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/anonymousClass.php", options);
    }

    public void testAnonymousClassBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/anonymousClassBP_01.php", options);
    }

    public void testAnonymousClassBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/anonymousClassBP_02.php", options);
    }

    public void testAnonymousClassBracePlacement_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/anonymousClassBP_03.php", options);
    }

    public void testAnonymousClassBracePlacement_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.ANONYMOUS_CLASS_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/anonymousClassBP_04.php", options);
    }

    public void testGroupUseBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_USE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.WRAP_GROUP_USE_BRACES, true);
        reformatFileContents("testfiles/formatting/groupUseBP_01.php", options);
    }

    public void testGroupUseBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_USE_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.WRAP_GROUP_USE_BRACES, true);
        reformatFileContents("testfiles/formatting/groupUseBP_02.php", options);
    }

    public void testGroupUseBracePlacement_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_USE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WRAP_GROUP_USE_BRACES, true);
        reformatFileContents("testfiles/formatting/groupUseBP_03.php", options);
    }

    public void testGroupUseBracePlacement_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_USE_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.WRAP_GROUP_USE_BRACES, true);
        reformatFileContents("testfiles/formatting/groupUseBP_04.php", options);
    }

    public void testGroupUseInNamespaceBlock() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/groupUseInNamespaceBlock.php", options);
    }

    public void testIssue261521() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue261521.php", options);
    }

    public void testIssue253030a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue253030a.php", options);
    }

    public void testIssue253030b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue253030b.php", options);
    }

    public void testIssue253030c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue253030c.php", options);
    }

    public void testIssue259111() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue259111.php", options);
    }

    public void testIssue268171() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue268171.php", options);
    }

    public void testIssue268920() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue268920.php", options);
    }

    public void testIssue269707() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue269707.php", options);
    }

    public void testIssue270385_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/issue270385_01.php", options);
    }

    public void testIssue270385_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/issue270385_02.php", options);
    }

    public void testIssue270385_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/issue270385_03.php", options);
    }

    public void testIssue270385_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/issue270385_04.php", options);
    }

    public void testIssue271062() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue271062.php", options);
    }

    // PHP 7.3
    public void testFlexibleHeredoc_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/flexibleHeredoc_01.php", options);
    }

    public void testFlexibleNowdoc_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/flexibleNowdoc_01.php", options);
    }

    // PHP 7.4
    public void testNullCoalescingAssignment01() throws Exception {
        // "??="
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/nullCoalescingAssignment01.php", options);
    }

    public void testTypedProperties20_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/typedProperties20_01.php", options);
    }

    public void testSpeadOperatorInArrayExpression_01() throws Exception {
        // $array = [...$array, 1];
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spreadOperatorInArrayExpression.php", options);
    }

    public void testSpeadOperatorInArrayExpression_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spreadOperatorInClassConst.php", options);
    }

    public void testArrowFunctions_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/arrowFunctions01.php", options);
    }

    public void testArrowFunctions_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/arrowFunctions02.php", options);
    }

    public void testNetBeans1047() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/netbeans1047.php", options);
    }

    // PHP 8.0
    public void testThrowExpression_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/throwExpression01.php", options);
    }

    public void testMatchExpressionNewLineBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/php80/matchExpression_NewLine_01.php", options);
    }

    public void testMatchExpressionNewLineBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/php80/matchExpression_NewLine_02.php", options);
    }

    public void testMatchExpressionNewLineIndentedBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/php80/matchExpression_NewLineIndented_01.php", options);
    }

    public void testMatchExpressionNewLineIndentedBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/php80/matchExpression_NewLineIndented_02.php", options);
    }

    public void testMatchExpressionPreserveExistingBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/php80/matchExpression_PreserveExisting_01.php", options);
    }

    public void testMatchExpressionPreserveExistingBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/php80/matchExpression_PreserveExisting_02.php", options);
    }

    public void testMatchExpressionSameLineBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/php80/matchExpression_SameLine_01.php", options);
    }

    public void testMatchExpressionSameLineBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.MATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/php80/matchExpression_SameLine_02.php", options);
    }

    // GH-5186
    public void testMatchExpressionWithArray_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/matchExpressionWithArray_01.php", options);
    }

    public void testAttributeSyntax_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/attributeSyntax_01.php", options);
    }

    public void testAttributeSyntax_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/attributeSyntax_02a.php", options);
    }

    public void testAttributeSyntax_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/php80/attributeSyntax_02b.php", options);
    }

    public void testAttributeSyntax_03a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/attributeSyntax_03a.php", options);
    }

    public void testAttributeSyntax_03b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/php80/attributeSyntax_03b.php", options);
    }

    public void testConstructorPropertyPromotion_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/constructorPropertyPromotion_01a.php", options);
    }

    public void testConstructorPropertyPromotion_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/php80/constructorPropertyPromotion_01b.php", options);
    }

    public void testAllowTrailingCommaInClosureUseLists_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/allowTrailingCommaInClosureUseLists_01a.php", options);
    }

    public void testAllowTrailingCommaInClosureUseLists_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/php80/allowTrailingCommaInClosureUseLists_01b.php", options);
    }

    public void testNamedArguments_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/namedArguments_01a.php", options);
    }

    public void testNamedArguments_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/php80/namedArguments_01b.php", options);
    }

    public void testNamedArguments_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/namedArguments_02.php", options);
    }

    public void testNamedArgumentsWithLambdaFunction_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php80/namedArgumentsWithLambdaFunction_01a.php", options);
    }

    public void testNamedArgumentsWithLambdaFunction_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/php80/namedArgumentsWithLambdaFunction_01b.php", options);
    }

    public void testNetBeans4970() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/netbeans4970.php", options);
    }

    public void testSpaceAfterType() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaceAfterType.php", options);
    }

    // [NETBEANS-5599] PHP 8.1
    public void testFinalClassConstants_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php81/finalClassConstants_01.php", options);
    }

    public void testFinalClassConstantsWithAttributes_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php81/finalClassConstantsWithAttributes_01.php", options);
    }

    public void testReadonlyProperties_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php81/readonlyProperties_01.php", options);
    }

    public void testNewInInitializers_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php81/newInInitializers_01.php", options);
    }

    public void testNewInInitializers_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php81/newInInitializers_02.php", options);
    }

    public void testEnumerations_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php81/enumerations_01.php", options);
    }

    public void testEnumDeclarationBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/php81/enumerationsBP_01.php", options);
    }

    public void testEnumDeclarationBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/php81/enumerationsBP_02.php", options);
    }

    public void testEnumDeclarationBracePlacement_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/php81/enumerationsBP_03.php", options);
    }

    public void testEnumDeclarationBracePlacement_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/php81/enumerationsBP_04.php", options);
    }

    public void testReadonlyClasses_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php82/readonlyClasses_01a.php", options);
    }

    public void testReadonlyClasses_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php82/readonlyClasses_01b.php", options);
    }

    public void testDNFTypes_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php82/dnfTypes_01.php", options);
    }

    public void testDynamicClassConstantFetch_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_SCOPE_RESOLUTION_OPS, false);
        reformatFileContents("testfiles/formatting/php83/dynamicClassConstantFetch_01.php", options, false, true);
    }

    public void testDynamicClassConstantFetch_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_SCOPE_RESOLUTION_OPS, true);
        reformatFileContents("testfiles/formatting/php83/dynamicClassConstantFetch_01.php", options, false, true);
    }

    public void testDynamicClassConstantFetch_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_SCOPE_RESOLUTION_OPS, false);
        reformatFileContents("testfiles/formatting/php83/dynamicClassConstantFetch_02.php", options, false, true);
    }

    public void testDynamicClassConstantFetch_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_SCOPE_RESOLUTION_OPS, true);
        reformatFileContents("testfiles/formatting/php83/dynamicClassConstantFetch_02.php", options, false, true);
    }

    public void testTypedClassConstants_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php83/typedClassConstants_01.php", options, false, true);
    }

    public void testTypedClassConstants_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/php83/typedClassConstants_02.php", options, false, true);
    }

}
