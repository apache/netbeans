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

package org.netbeans.modules.php.editor.parser;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java_cup.runtime.Symbol;
import org.netbeans.modules.php.editor.lexer.PHPLexerUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Program;

/**
 *
 * @author Petr Pisl
 */
public class ASTPHP5ParserTest extends ParserTestBase {

    public ASTPHP5ParserTest(String testName) {
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

    public void testInclude() throws Exception {
        performTest("parser/include");
    }

    public void testGotoStatment() throws Exception {
        performTest("parser/gotostatement");
    }
    public void testLambdaFunction() throws Exception {
        performTest("parser/lambdaFunction");
    }
    public void testLambdaFunctionWithParams() throws Exception {
        performTest("parser/lambdaFunctionWithParams");
    }
    public void testLambdaFunctionWithParamsWithVars() throws Exception {
        performTest("parser/lambdaFunctionWithParamsWithVars");
    }
    public void testLambdaFunctionWithParamsWithVarsWithStatements() throws Exception {
        performTest("parser/lambdaFunctionWithParamsWithVarsWithStatements");
    }
    public void testMultipleBracketedNamespaces() throws Exception {
        performTest("parser/multipleBracketedNamespaces");
    }
    public void testMultipleUnBracketedNamespaces1() throws Exception {
        performTest("parser/multipleUnBracketedNamespaces1");
    }
    public void testMultipleUnBracketedNamespaces2() throws Exception {
        performTest("parser/multipleUnBracketedNamespaces2");
    }
    public void testNamespaceDeclaration() throws Exception {
        performTest("parser/namespaceDeclaration");
    }
    public void testSubNamespaceDeclaration() throws Exception {
        performTest("parser/subNamespaceDeclaration");
    }
    public void testNamespaceElementDeclarations() throws Exception {
        performTest("parser/namespaceElementDeclarations");
    }
    public void testNowDoc() throws Exception {
        performTest("parser/nowDoc");
    }
    public void testRefLambdaFunctionWithParamsWithVarsWithStatements() throws Exception {
        performTest("parser/refLambdaFunctionWithParamsWithVarsWithStatements");
    }
    public void testTernaryOperator() throws Exception {
        performTest("parser/ternaryOperator");
    }
    public void testUseGlobal() throws Exception {
        performTest("parser/useGlobal");
    }
    public void testUseGlobalSubNamespace() throws Exception {
        performTest("parser/useGlobalSubNamespace");
    }
    public void testUseNamespaceAs() throws Exception {
        performTest("parser/useNamespaceAs");
    }
    public void testUseSimple() throws Exception {
        performTest("parser/useSimple");
    }
    public void testUseSubNamespace() throws Exception {
        performTest("parser/useSubNamespace");
    }
    public void testTextSearchQuery () throws Exception {
        // testing real file from phpwiki
        performTest("parser/TextSearchQuery");
    }

    public void testPHPDoc () throws Exception {
        //unfinished phpdoc
        performTest("parser/test01");
    }

    public void testNowdoc_01 () throws Exception {
        performTest("parser/nowdoc01");
    }

    public void testNowdoc_02() throws Exception {
        performTest("parser/nowdoc02");
    }

    public void testNowdoc_03() throws Exception {
        performTest("parser/nowdoc_000");
    }

    public void testNowdoc_04() throws Exception {
        performTest("parser/nowdoc_001");
    }

    public void testNowdoc_05() throws Exception {
        performTest("parser/nowdoc_002");
    }

    public void testNowdoc_06() throws Exception {
        performTest("parser/nowdoc_003");
    }

    public void testNowdoc_07() throws Exception {
        performTest("parser/nowdoc_004");
    }

    public void testNowdoc_08() throws Exception {
        performTest("parser/nowdoc_005");
    }

    public void testNowdoc_09() throws Exception {
        performTest("parser/nowdoc_006");
    }

    public void testNowdoc_10() throws Exception {
        performTest("parser/nowdoc_007");
    }

    public void testNowdoc_11() throws Exception {
        performTest("parser/nowdoc_008");
    }

    // 12(nowdoc_009), 13(nowdoc_010): old syntax tests
    // we don't provide support for them any longer
    // we can use the new syntax since PHP 7.3

    public void testNowdoc_14() throws Exception {
        performTest("parser/nowdoc_011");
    }

    public void testNowdoc_15() throws Exception {
        performTest("parser/nowdoc_012");
    }

    public void testNowdoc_16() throws Exception {
        performTest("parser/nowdoc_013");
    }

    public void testNowdoc_17() throws Exception {
        performTest("parser/nowdoc_014");
    }

    public void testNowdoc_18() throws Exception {
        performTest("parser/nowdoc_015");
    }

    public void testIssueGH4345_01() throws Exception {
        performTest("parser/issueGH4345_01");
    }

    public void testIssueGH4345_02() throws Exception {
        performTest("parser/issueGH4345_02");
    }

    public void testHereDoc_01() throws Exception {
        performTest("parser/heredoc00");
    }

    public void testHereDoc_02() throws Exception {
        performTest("parser/heredoc01");
    }

    public void testHereDoc_03() throws Exception {
        performTest("parser/heredoc_001");
    }

    public void testHereDoc_04() throws Exception {
        performTest("parser/heredoc_002");
    }

    public void testHereDoc_05() throws Exception {
        performTest("parser/heredoc_003");
    }

    public void testHereDoc_06() throws Exception {
        performTest("parser/heredoc_004");
    }

    public void testHereDoc_07() throws Exception {
        performTest("parser/heredoc_005");
    }

    public void testHereDoc_08() throws Exception {
        performTest("parser/heredoc_006");
    }

    public void testHereDoc_09() throws Exception {
        performTest("parser/heredoc_007");
    }

    public void testHereDoc_10() throws Exception {
        performTest("parser/heredoc_008");
    }

    // 11(heredoc_009), 12(heredoc_010): old syntax tests
    // we don't provide support for them any longer
    // we can use the new syntax since PHP 7.3

    public void testHereDoc_13() throws Exception {
        performTest("parser/heredoc_011");
    }

    public void testHereDoc_14() throws Exception {
        performTest("parser/heredoc_012");
    }

    public void testHereDoc_15() throws Exception {
        performTest("parser/heredoc_013");
    }

    public void testHereDoc_16() throws Exception {
        performTest("parser/heredoc_014");
    }

    public void testHereDoc_17() throws Exception {
        performTest("parser/heredoc_015");
    }

    // HEREDOC
    public void testNetBeans1563_01() throws Exception {
        performTest("parser/netbeans1563_01");
    }

    public void testNetBeans1563_02() throws Exception {
        performTest("parser/netbeans1563_02");
    }

    public void testNetBeans1563_03() throws Exception {
        performTest("parser/netbeans1563_03");
    }

    public void testNetBeans1563_04() throws Exception {
        performTest("parser/netbeans1563_04");
    }

    public void testVarCommentSimple01() throws Exception {
        performTest("parser/simple01");
    }

    public void testVarCommentSimple02() throws Exception {
        performTest("parser/simple02");
    }

    public void testVarCommentMixedType01() throws Exception {
        performTest("parser/mixed01");
    }

    public void testVarCommentMixedType02() throws Exception {
        performTest("parser/mixed02");
    }

    public void testMagicMethod01() throws Exception {
        performTest("parser/magicMethod01");
    }

    public void testMagicMethod02() throws Exception {
        performTest("parser/magicMethod02");
    }

    public void testMagicMethod03() throws Exception {
        performTest("parser/magicMethod03");
    }

    public void testMagicMethod04() throws Exception {
        performTest("parser/magicMethod04");
    }

    public void testMagicMethod05() throws Exception {
        performTest("parser/magicMethod05");
    }

    public void testMagicMethod06() throws Exception {
        performTest("parser/magicMethod06");
    }

    public void testMagicMethod07() throws Exception {
        performTest("parser/magicMethod07");
    }

    // NETBEANS-1861
    public void testMagicMethodStatic01() throws Exception {
        performTest("parser/magicMethodStatic01");
    }

    public void testMagicMethodStatic02() throws Exception {
        performTest("parser/magicMethodStatic02");
    }

    public void testMagicMethodStatic03() throws Exception {
        performTest("parser/magicMethodStatic03");
    }

    public void testMagicMethodStatic04() throws Exception {
        performTest("parser/magicMethodStatic04");
    }

    public void testMagicMethodStatic05() throws Exception {
        performTest("parser/magicMethodStatic05");
    }

    public void testMagicMethodStatic06() throws Exception {
        performTest("parser/magicMethodStatic06");
    }

    public void testMagicMethodStatic07() throws Exception {
        performTest("parser/magicMethodStatic07");
    }

    public void testTraits_01() throws Exception {
        performTest("parser/traits_01");
    }

    public void testTraits_02() throws Exception {
        performTest("parser/traits_02");
    }

    public void testTraits_03() throws Exception {
        performTest("parser/traits_03");
    }

    public void testTraits_04() throws Exception {
        performTest("parser/traits_04");
    }

    public void testTraits_05() throws Exception {
        performTest("parser/traits_05");
    }

    public void testTraits_06() throws Exception {
        performTest("parser/traits_06");
    }

    public void testShortArrays_01() throws Exception {
        performTest("parser/shortArrays_01");
    }

    public void testShortArrays_02() throws Exception {
        performTest("parser/shortArrays_02");
    }

    public void testShortArraysStaticScalar_01() throws Exception {
        performTest("parser/shortArraysStaticScalar_01");
    }

    public void testShortArraysStaticScalar_02() throws Exception {
        performTest("parser/shortArraysStaticScalar_02");
    }

    public void testShortEchoSyntax() throws Exception {
        performTest("parser/shortEchoSyntax");
    }

    public void testAnonymousObjectVariable() throws Exception {
        performTest("parser/anonymousObjectVariable");
    }

    public void testFieldArraysWithArrayDereferencing() throws Exception {
        performTest("parser/fieldArraysWithArrayDereferencing");
    }

    public void testArrayDereferencing_01() throws Exception {
        performTest("parser/arrayDereferencing_01");
    }

    public void testArrayDereferencing_02() throws Exception {
        performTest("parser/arrayDereferencing_02");
    }

    public void testArrayDereferencing_03() throws Exception {
        performTest("parser/arrayDereferencing_03");
    }

    public void testArrayDereferencing_04() throws Exception {
        performTest("parser/arrayDereferencing_04");
    }

    public void testArrayDereferencing_05() throws Exception {
        performTest("parser/arrayDereferencing_05");
    }

    public void testArrayDereferencing_06() throws Exception {
        performTest("parser/arrayDereferencing_06");
    }

    public void testArrayDereferencing_07() throws Exception {
        performTest("parser/arrayDereferencing_07");
    }

    public void testArrayDereferencing_08() throws Exception {
        performTest("parser/arrayDereferencing_08");
    }

    public void testArrayDereferencing_09() throws Exception {
        performTest("parser/arrayDereferencing_09");
    }

    public void testBinaryNotation_01() throws Exception {
        performTest("parser/binaryNotation_01");
    }

    public void testBinaryNotation_02() throws Exception {
        performTest("parser/binaryNotation_02");
    }

    public void testBinaryNotation_03() throws Exception {
        performTest("parser/binaryNotation_03");
    }

    public void testBinaryNotation_04() throws Exception {
        performTest("parser/binaryNotation_04");
    }

    public void testStaticExpressionCall_01() throws Exception {
        performTest("parser/staticExpressionCall_01");
    }

    public void testStaticExpressionCall_02() throws Exception {
        performTest("parser/staticExpressionCall_02");
    }

    public void testCfunction() throws Exception {
        performTest("parser/cfunction");
    }

    public void testInstanceOfExpression() throws Exception {
        performTest("parser/instanceOfExpression");
    }

    public void testIssue170712() throws Exception {
        performTest("parser/issue170712");
    }

    public void testIssue200501() throws Exception {
        performTest("parser/issue200501");
    }

    public void testIssue213423() throws Exception {
        performTest("parser/issue213423");
    }

    public void testFinally_01() throws Exception {
        performTest("parser/finally_01");
    }

    public void testFinally_02() throws Exception {
        performTest("parser/finally_02");
    }

    public void testFinally_03() throws Exception {
        performTest("parser/finally_03");
    }

    public void testListInForeach_01() throws Exception {
        performTest("parser/listInForeach_01");
    }

    public void testListInForeach_02() throws Exception {
        // #257945
        performTest("parser/listInForeach_02");
    }

    public void testExpressionArrayAccess_01() throws Exception {
        performTest("parser/expressionArrayAccess_01");
    }

    public void testExpressionArrayAccess_02() throws Exception {
        performTest("parser/expressionArrayAccess_02");
    }

    public void testExpressionArrayAccess_03() throws Exception {
        performTest("parser/expressionArrayAccess_03");
    }

    public void testYield_01() throws Exception {
        performTest("parser/yield_01");
    }

    public void testYield_02() throws Exception {
        performTest("parser/yield_02");
    }

    public void testYield_03() throws Exception {
        performTest("parser/yield_03");
    }

    public void testYield_04() throws Exception {
        performTest("parser/yield_04");
    }

    public void testYield_05() throws Exception {
        // it's available since PHP7
        performTest("parser/yield_05");
    }

    public void testYield_06() throws Exception {
        // it's available since PHP7
        performTest("parser/yield_06");
    }

    public void testYield_07() throws Exception {
        performTest("parser/yield_07");
    }

    public void testExponentiation_01() throws Exception {
        performTest("parser/exponentiation_01");
    }

    // #250579
    public void testConstantArrayAccess_01() throws Exception {
        performTest("parser/constantArrayAccess_01");
    }

    public void testConstantScalarExpressions_01() throws Exception {
        performTest("parser/constantScalarExpressions_01");
    }

    public void testVariadicFunctions_01() throws Exception {
        performTest("parser/testVariadicFunctions_01");
    }

    public void testVariadicFunctions_02() throws Exception {
        performTest("parser/testVariadicFunctions_02");
    }

    public void testArgumentUnpacking_01() throws Exception {
        performTest("parser/argumentUnpacking_01");
    }

    public void testUseFuncAndConst_01() throws Exception {
        performTest("parser/useFuncAndConst_01");
    }

    public void testSpaceship01() throws Exception {
        performTest("parser/spaceship_01");
    }

    public void testSpaceship02() throws Exception {
        performTest("parser/spaceship_02");
    }

    public void testCoalesce01() throws Exception {
        performTest("parser/coalesce_01");
    }

    public void testCoalesce02() throws Exception {
        performTest("parser/coalesce_02");
    }

    public void testReturnTypes01() throws Exception {
        performTest("parser/returnTypes_01");
    }

    public void testReturnTypes02() throws Exception {
        performTest("parser/returnTypes_02");
    }

    public void testReturnTypes03() throws Exception {
        performTest("parser/returnTypes_03");
    }

    public void testReturnTypes04() throws Exception {
        performTest("parser/returnTypes_04");
    }

    public void testReturnTypes05() throws Exception {
        performTest("parser/returnTypes_05");
    }

    public void testYieldFrom01() throws Exception {
        performTest("parser/yieldFrom_01");
    }

    public void testYieldFrom02() throws Exception {
        performTest("parser/yieldFrom_02");
    }

    public void testYieldFrom03() throws Exception {
        performTest("parser/yieldFrom_03");
    }

    public void testYieldFrom04() throws Exception {
        performTest("parser/yieldFrom_04");
    }

    public void testYieldFrom05() throws Exception {
        performTest("parser/yieldFrom_05");
    }

    public void testYieldFrom06() throws Exception {
        performTest("parser/yieldFrom_06");
    }

    public void testAnonymousClass01() throws Exception {
        performTest("parser/anonymousClass_01");
    }

    public void testUniformVariableSyntax_01() throws Exception {
        performTest("parser/uniformVariableSyntax_01");
    }

    public void testUniformVariableSyntax_02() throws Exception {
        performTest("parser/uniformVariableSyntax_02");
    }

    public void testUniformVariableSyntax_03() throws Exception {
        performTest("parser/uniformVariableSyntax_03");
    }

    public void testUniformVariableSyntax_04() throws Exception {
        performTest("parser/uniformVariableSyntax_04");
    }

    public void testUniformVariableSyntax_05() throws Exception {
        performTest("parser/uniformVariableSyntax_05");
    }

    public void testUniformVariableSyntax_06() throws Exception {
        performTest("parser/uniformVariableSyntax_06");
    }

    public void testUniformVariableSyntax_07() throws Exception {
        performTest("parser/uniformVariableSyntax_07");
    }

    public void testUniformVariableSyntax_08() throws Exception {
        performTest("parser/uniformVariableSyntax_08");
    }

    public void testUniformVariableSyntax_09() throws Exception {
        performTest("parser/uniformVariableSyntax_09");
    }

    public void testUniformVariableSyntax_10() throws Exception {
        performTest("parser/uniformVariableSyntax_10");
    }

    public void testUniformVariableSyntax_11() throws Exception {
        performTest("parser/uniformVariableSyntax_11");
    }

    public void testUniformVariableSyntax_12() throws Exception {
        performTest("parser/uniformVariableSyntax_12");
    }

    public void testUniformVariableSyntax_13() throws Exception {
        performTest("parser/uniformVariableSyntax_13");
    }

    public void testUniformVariableSyntax_14() throws Exception {
        performTest("parser/uniformVariableSyntax_14");
    }

    // incorrect AST (pre php7)
    public void testUniformVariableSyntax_15() throws Exception {
        performTest("parser/uniformVariableSyntax_15");
    }

    // incorrect AST (pre php7)
    public void testUniformVariableSyntax_16() throws Exception {
        performTest("parser/uniformVariableSyntax_16");
    }

    public void testUniformVariableSyntax_17() throws Exception {
        performTest("parser/uniformVariableSyntax_17");
    }

    public void testUniformVariableSyntax_18() throws Exception {
        performTest("parser/uniformVariableSyntax_18");
    }

    public void testUniformVariableSyntax_19() throws Exception {
        performTest("parser/uniformVariableSyntax_19");
    }

    public void testUniformVariableSyntax_20() throws Exception {
        performTest("parser/uniformVariableSyntax_20");
    }

    // #262141
    public void testUniformVariableSyntax_21() throws Exception {
        performTest("parser/uniformVariableSyntax_21");
    }

    // #262141
    public void testUniformVariableSyntax_22() throws Exception {
        performTest("parser/uniformVariableSyntax_22");
    }

    public void testUniformVariableSyntax_23() throws Exception {
        performTest("parser/uniformVariableSyntax_23");
    }

    public void testUniformVariableSyntax_24() throws Exception {
        performTest("parser/uniformVariableSyntax_24");
    }

    public void testUniformVariableSyntax_25() throws Exception {
        performTest("parser/uniformVariableSyntax_25");
    }

    public void testUniformVariableSyntax_26() throws Exception {
        performTest("parser/uniformVariableSyntax_26");
    }

    public void testUniformVariableSyntax_27() throws Exception {
        performTest("parser/uniformVariableSyntax_27");
    }

    public void testUniformVariableSyntax_28() throws Exception {
        performTest("parser/uniformVariableSyntax_28");
    }

    public void testUniformVariableSyntax_29() throws Exception {
        performTest("parser/uniformVariableSyntax_29");
    }

    public void testUniformVariableSyntax_30() throws Exception {
        performTest("parser/uniformVariableSyntax_30");
    }

    public void testUniformVariableSyntax_31() throws Exception {
        performTest("parser/uniformVariableSyntax_31");
    }

    public void testUniformVariableSyntax_32() throws Exception {
        performTest("parser/uniformVariableSyntax_32");
    }

    public void testUniformVariableSyntax_33() throws Exception {
        performTest("parser/uniformVariableSyntax_33");
    }

    // #262141
    public void testUniformVariableSyntax_34() throws Exception {
        // ... -> ... (->|::)
        performTest("parser/uniformVariableSyntax_34");
    }

    public void testUniformVariableSyntax_35() throws Exception {
        // ... :: ... (->|::)
        performTest("parser/uniformVariableSyntax_35");
    }

    public void testUniformVariableSyntax_36() throws Exception {
        // dereferencable
        performTest("parser/uniformVariableSyntax_36");
    }

    public void testUniformVariableSyntax_37() throws Exception {
        // [$object1, $object2][0]->property;
        performTest("parser/uniformVariableSyntax_37");
    }

    // NETBEANS-1552
    public void testUniformVariableSyntax_38() throws Exception {
        // ($test = new UVS())->isTest('test');
        performTest("parser/uniformVariableSyntax_38");
    }

    public void testUniformVariableSyntax_39() throws Exception {
        // Test::{test('foo')}()::{test('bar')}();
        performTest("parser/uniformVariableSyntax_39");
    }

    public void testCloneExpression_01() throws Exception {
        performTest("parser/cloneExpression_01");
    }

    public void testGroupUse_01() throws Exception {
        performTest("parser/groupUse_01");
    }

    public void testGroupUse_02() throws Exception {
        performTest("parser/groupUse_02");
    }

    public void testGroupUse_03() throws Exception {
        performTest("parser/groupUse_03");
    }

    public void testGroupUse_04() throws Exception {
        performTest("parser/groupUse_04");
    }

    public void testIssue258959() throws Exception {
        performTest("parser/issue258959");
    }

    public void testIssue268496_01() throws Exception {
        performTest("parser/issue268496_01");
    }

    public void testIssue268712() throws Exception {
        performTest("parser/issue268712");
    }

    public void testIssue262144() throws Exception {
        // yeild and yield from expressions for PHP7
        performTest("parser/issue262144");
    }

    // PHP7.1
    public void testNullableTypes_01() throws Exception {
        performTest("parser/nullableTypes_01");
    }

    public void testNullableTypes_02() throws Exception {
        performTest("parser/nullableTypes_02");
    }

    public void testNullableTypes_03() throws Exception {
        performTest("parser/nullableTypes_03");
    }

    public void testMultiCatchInFirstCatchClause() throws Exception {
        performTest("parser/multiCatchInFirstCatchClause");
    }

    public void testMultiCatchInSecondCatchClause() throws Exception {
        performTest("parser/multiCatchInSecondCatchClause");
    }

    public void testMultiCatchWithFinally() throws Exception {
        performTest("parser/multiCatchWithFinally");
    }

    public void testClassConstantVisibility_01() throws Exception {
        performTest("parser/classConstantVisibility_01");
    }

    public void testKeyedList_01() throws Exception {
        performTest("parser/keyedList_01");
    }

    public void testSymmetricArrayDestructuring_01() throws Exception {
        performTest("parser/symmetricArrayDestructuring_01");
    }

    public void testIssue269707() throws Exception {
        performTest("parser/issue269707");
    }

    // #262141 PHP7.0
    public void testContextSensitiveLexer_01() throws Exception {
        performTest("parser/contextSensitiveLexer_01");
    }

    public void testContextSensitiveLexer_02() throws Exception {
        performTest("parser/contextSensitiveLexer_02");
    }

    public void testContextSensitiveLexer_03() throws Exception {
        performTest("parser/contextSensitiveLexer_03");
    }

    public void testContextSensitiveLexer_04() throws Exception {
        performTest("parser/contextSensitiveLexer_04");
    }

    public void testContextSensitiveLexer_05() throws Exception {
        performTest("parser/contextSensitiveLexer_05");
    }

    public void testContextSensitiveLexerWithConstVisibility_01() throws Exception {
        performTest("parser/contextSensitiveLexerWithConstVisibility_01");
    }

    public void testIssue271109() throws Exception {
        // PHP7
        performTest("parser/issue271109");
    }

    // PHP 7.2
    public void testGroupUseTrailingCommas_01() throws Exception {
        performTest("parser/groupUseTrailingCommas_01");
    }

    public void testGroupUseTrailingCommas_02() throws Exception {
        performTest("parser/groupUseTrailingCommas_02");
    }

    public void testGroupUseTrailingCommas_03() throws Exception {
        performTest("parser/groupUseTrailingCommas_03");
    }

    public void testGroupUseTrailingCommas_04() throws Exception {
        performTest("parser/groupUseTrailingCommas_04");
    }

    // PHP 7.3
    public void testFunctionCallTrailingCommas_01() throws Exception {
        performTest("parser/php73/functionCallTrailingCommas_01");
    }

    public void testListReferenceAssignment_01() throws Exception {
        performTest("parser/php73/listReferenceAssignment_01");
    }

    public void testFlexibleHeredocIndentSpaces_01() throws Exception {
        performTest("parser/php73/heredoc_indent_spaces_01");
    }

    public void testFlexibleHeredocIndentSpaces_02() throws Exception {
        performTest("parser/php73/heredoc_indent_spaces_02");
    }

    public void testFlexibleHeredocIndentSpaces_03() throws Exception {
        performTest("parser/php73/heredoc_indent_spaces_03");
    }

    public void testFlexibleHeredocIndentSpaces_04() throws Exception {
        // no new line at EOF
        performTest("parser/php73/heredoc_indent_spaces_04");
    }

    public void testFlexibleHeredocIndentTabs_01() throws Exception {
        performTest("parser/php73/heredoc_indent_tabs_01");
    }

    public void testFlexibleHeredocIndentTabs_02() throws Exception {
        performTest("parser/php73/heredoc_indent_tabs_02");
    }

    public void testFlexibleHeredocIndentTabs_03() throws Exception {
        performTest("parser/php73/heredoc_indent_tabs_03");
    }

    public void testFlexibleHeredocNewLine_01() throws Exception {
        performTest("parser/php73/heredoc_new_line_01");
    }

    public void testFlexibleHeredocNewLine_02() throws Exception {
        performTest("parser/php73/heredoc_new_line_02");
    }

    public void testFlexibleHeredocNewLine_03() throws Exception {
        performTest("parser/php73/heredoc_new_line_03");
    }

    public void testFlexibleHeredocNewLine_04() throws Exception {
        performTest("parser/php73/heredoc_new_line_04");
    }

    public void testFlexibleHeredocMixed_01() throws Exception {
        performTest("parser/php73/heredoc_mixed_01");
    }

    public void testFlexibleHeredocMixed_02() throws Exception {
        performTest("parser/php73/heredoc_mixed_02");
    }

    public void testFlexibleHeredocMixed_03() throws Exception {
        // no new line at EOF
        performTest("parser/php73/heredoc_mixed_03");
    }

    public void testFlexibleNowdocIndentSpaces_01() throws Exception {
        performTest("parser/php73/nowdoc_indent_spaces_01");
    }

    public void testFlexibleNowdocIndentSpaces_02() throws Exception {
        performTest("parser/php73/nowdoc_indent_spaces_02");
    }

    public void testFlexibleNowdocIndentSpaces_03() throws Exception {
        performTest("parser/php73/nowdoc_indent_spaces_03");
    }

    public void testFlexibleNowdocIndentSpaces_04() throws Exception {
        // no new line at EOF
        performTest("parser/php73/nowdoc_indent_spaces_04");
    }

    public void testFlexibleNowdocIndentTabs_01() throws Exception {
        performTest("parser/php73/nowdoc_indent_tabs_01");
    }

    public void testFlexibleNowdocIndentTabs_02() throws Exception {
        performTest("parser/php73/nowdoc_indent_tabs_02");
    }

    public void testFlexibleNowdocIndentTabs_03() throws Exception {
        performTest("parser/php73/nowdoc_indent_tabs_03");
    }

    public void testFlexibleNowdocNewLine_01() throws Exception {
        performTest("parser/php73/nowdoc_new_line_01");
    }

    public void testFlexibleNowdocNewLine_02() throws Exception {
        performTest("parser/php73/nowdoc_new_line_02");
    }

    public void testFlexibleNowdocNewLine_03() throws Exception {
        performTest("parser/php73/nowdoc_new_line_03");
    }

    public void testFlexibleNowdocNewLine_04() throws Exception {
        performTest("parser/php73/nowdoc_new_line_04");
    }

    public void testFlexibleNowdocMixed_01() throws Exception {
        performTest("parser/php73/nowdoc_mixed_01");
    }

    public void testFlexibleNowdocMixed_02() throws Exception {
        performTest("parser/php73/nowdoc_mixed_02");
    }

    public void testFlexibleNowdocMixed_03() throws Exception {
        // no new line at EOF
        performTest("parser/php73/nowdoc_mixed_03");
    }

    // PHP 7.4
    public void testNullCoalescingAssignmentOperator_01() throws Exception {
        // "??="
        performTest("parser/php74/nullCoalescingAssignmentOperator_01");
    }

    public void testTypedProperties20Class() throws Exception {
        performTest("parser/php74/typedPropertiesClass");
    }

    public void testTypedProperties20Trait() throws Exception {
        performTest("parser/php74/typedPropertiesTrait");
    }

    public void testSpreadOperatorInArrayExpression_01() throws Exception {
        performTest("parser/php74/spreadOperatorInArrayExpression_01");
    }

    public void testSpreadOperatorInArrayExpression_02() throws Exception {
        performTest("parser/php74/spreadOperatorInArrayExpression_02");
    }

    public void testSpreadOperatorInArrayExpression_03() throws Exception {
        performTest("parser/php74/spreadOperatorInArrayExpression_03");
    }

    public void testSpreadOperatorInArrayExpression_04() throws Exception {
        performTest("parser/php74/spreadOperatorInArrayExpression_04");
    }

    public void testNumericLiteralSeparator_01() throws Exception {
        performTest("parser/php74/numericLiteralSeparator_01");
    }

    public void testNumericLiteralSeparatorParseError_01() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_01");
    }

    public void testNumericLiteralSeparatorParseError_02() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_02");
    }

    public void testNumericLiteralSeparatorParseError_03() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_03");
    }

    public void testNumericLiteralSeparatorParseError_04() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_04");
    }

    public void testNumericLiteralSeparatorParseError_05() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_05");
    }

    public void testNumericLiteralSeparatorParseError_06() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_06");
    }

    public void testNumericLiteralSeparatorParseError_07() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_07");
    }

    public void testNumericLiteralSeparatorParseError_08() throws Exception {
        performTest("parser/php74/numericLiteralSeparatorParseError_08");
    }

    public void testArrowFunctions_01() throws Exception {
        performTest("parser/php74/arrowFunctions_01");
    }

    public void testArrowFunctionsParseError_01() throws Exception {
        performTest("parser/php74/arrowFunctionsParseError_01");
    }

    public void testArrowFunctionsParseError_02() throws Exception {
        performTest("parser/php74/arrowFunctionsParseError_02");
    }

    public void testArrowFunctionsParseError_03() throws Exception {
        performTest("parser/php74/arrowFunctionsParseError_03");
    }

    public void testArrowFunctionsParseError_04() throws Exception {
        performTest("parser/php74/arrowFunctionsParseError_04");
    }

    // PHP 8.0
    public void testNonCapturingCatches_01() throws Exception {
        performTest("parser/php80/nonCapturingCatches_01");
    }

    public void testAllowTrailingCommaInParameterList_01() throws Exception {
        performTest("parser/php80/allowTrailingCommaInParameterList_01");
    }

    public void testThrowExpression_01() throws Exception {
        performTest("parser/php80/throwExpression_01");
    }

    public void testClassNameLiteralOnObjects_01() throws Exception {
        performTest("parser/php80/classNameLiteralOnObjects_01");
    }

    public void testMatchExpression_01() throws Exception {
        performTest("parser/php80/matchExpression_01");
    }

    public void testMatchExpression_02() throws Exception {
        // nested match
        performTest("parser/php80/matchExpression_02");
    }

    public void testMatchExpression_03() throws Exception {
        // multiple conditions
        performTest("parser/php80/matchExpression_03");
    }

    public void testMatchExpression_04() throws Exception {
        performTest("parser/php80/matchExpression_04");
    }

    public void testMatchExpression_05() throws Exception {
        performTest("parser/php80/matchExpression_05");
    }

    public void testMatchExpressionError_01() throws Exception {
        performTest("parser/php80/matchExpressionError_01");
    }

    public void testMatchExpressionError_02() throws Exception {
        performTest("parser/php80/matchExpressionError_02");
    }

    public void testMatchExpressionError_03() throws Exception {
        performTest("parser/php80/matchExpressionError_03");
    }

    public void testMatchExpressionError_04() throws Exception {
        performTest("parser/php80/matchExpressionError_04");
    }

    public void testMatchExpressionError_05() throws Exception {
        performTest("parser/php80/matchExpressionError_05");
    }

    public void testMatchExpressionError_06() throws Exception {
        performTest("parser/php80/matchExpressionError_06");
    }

    public void testUnionTypesTypes() throws Exception {
        performTest("parser/php80/unionTypesTypes");
    }

    public void testUnionTypesFunctions() throws Exception {
        performTest("parser/php80/unionTypesFunctions");
    }

    public void testUnionTypesError_01() throws Exception {
        performTest("parser/php80/unionTypesError_01");
    }

    public void testUnionTypesError_02() throws Exception {
        performTest("parser/php80/unionTypesError_02");
    }

    public void testUnionTypesError_03() throws Exception {
        performTest("parser/php80/unionTypesError_03");
    }

    public void testStaticReturnType_01() throws Exception {
        performTest("parser/php80/staticReturnType_01");
    }

    public void testStaticReturnTypeErrorWithFieldType_01() throws Exception {
        // nullable type
        performTest("parser/php80/staticReturnTypeErrorWithFieldType_01");
    }

    public void testStaticReturnTypeErrorWithFieldType_02() throws Exception {
        // union type
        performTest("parser/php80/staticReturnTypeErrorWithFieldType_02");
    }

    public void testStaticReturnTypeErrorWithParameterType_01() throws Exception {
        performTest("parser/php80/staticReturnTypeErrorWithParameterType_01");
    }

    public void testStaticReturnTypeErrorWithParameterType_02() throws Exception {
        // nullable type
        performTest("parser/php80/staticReturnTypeErrorWithParameterType_02");
    }

    public void testStaticReturnTypeErrorWithParameterType_03() throws Exception {
        // union type
        performTest("parser/php80/staticReturnTypeErrorWithParameterType_03");
    }

    public void testMixedType_01() throws Exception {
        performTest("parser/php80/mixedType_01");
    }

    public void testMixedTypeError_01() throws Exception {
        performTest("parser/php80/mixedTypeError_01");
    }

    public void testNullsafeOperator_01() throws Exception {
        performTest("parser/php80/nullsafeOperator_01");
    }

    public void testNullsafeOperator_02() throws Exception {
        performTest("parser/php80/nullsafeOperator_02");
    }

    public void testNullsafeOperator_03() throws Exception {
        performTest("parser/php80/nullsafeOperator_03");
    }

    public void testAttributeSyntax_01() throws Exception {
        performTest("parser/php80/attributeSyntax_01");
    }

    public void testAttributeSyntax_02() throws Exception {
        performTest("parser/php80/attributeSyntax_02");
    }

    // [NETBEANS-6193] attributes of interface constants and methods
    public void testAttributeSyntax_03() throws Exception {
        performTest("parser/php80/attributeSyntax_03");
    }

    public void testAttributeSyntaxError_01() throws Exception {
        performTest("parser/php80/attributeSyntaxError_01");
    }

    public void testAttributeSyntaxError_02() throws Exception {
        performTest("parser/php80/attributeSyntaxError_02");
    }

    public void testConstructorPropertyPromotion_01() throws Exception {
        performTest("parser/php80/constructorPropertyPromotion_01");
    }

    public void testConstructorPropertyPromotion_02() throws Exception {
        performTest("parser/php80/constructorPropertyPromotion_02");
    }

    public void testConstructorPropertyPromotionErrorWithStatic() throws Exception {
        performTest("parser/php80/constructorPropertyPromotionErrorWithStatic");
    }

    public void testAllowTrailingCommaInClosureUseLists_01() throws Exception {
        performTest("parser/php80/allowTrailingCommaInClosureUseLists_01");
    }

    public void testNamedArguments_01() throws Exception {
        performTest("parser/php80/namedArguments_01");
    }

    public void testNamedArgumentsWithAttribute_01() throws Exception {
        performTest("parser/php80/namedArgumentsWithAttribute_01");
    }

    // [NETBEANS-5599] PHP 8.1
    public void testFinalClassConstants_01() throws Exception {
        performTest("parser/php81/finalClassConstants_01");
    }

    public void testFinalClassConstantsWithAttributes_01() throws Exception {
        performTest("parser/php81/finalClassConstantsWithAttributes_01");
    }

    public void testFinalClassMethods_01() throws Exception {
        performTest("parser/finalClassMethods_01");
    }

    public void testFinalTraitMethods_01() throws Exception {
        performTest("parser/finalTraitMethods_01");
    }

    public void testReadonlyProperties_01() throws Exception {
        performTest("parser/php81/readonlyProperties_01");
    }

    public void testReadonlyPropertiesWithConstError() throws Exception {
        performTest("parser/php81/readonlyPropertiesWithConstError");
    }

    public void testReadonlyPropertiesWithStaticError() throws Exception {
        performTest("parser/php81/readonlyPropertiesWithStaticError");
    }

    public void testNewInInitializers_01() throws Exception {
        performTest("parser/php81/newInInitializers_01");
    }

    public void testNewInInitializers_02() throws Exception {
        performTest("parser/php81/newInInitializers_02");
    }

    public void testNewInInitializersWithClassConstantError() throws Exception {
        performTest("parser/php81/newInInitializersWithClassConstantError");
    }

    public void testNewInInitializersWithPropertyError() throws Exception {
        performTest("parser/php81/newInInitializersWithPropertyError");
    }

    public void testPureIntersectionTypes_01() throws Exception {
        performTest("parser/php81/pureIntersectionTypes_01");
    }

    public void testPureIntersectionTypesWithUnionTypeError_01() throws Exception {
        performTest("parser/php81/pureIntersectionTypesWithUnionTypeError_01");
    }

    public void testPureIntersectionTypesWithUnionTypeError_02() throws Exception {
        performTest("parser/php81/pureIntersectionTypesWithUnionTypeError_02");
    }

    public void testPureIntersectionTypesWithUnionTypeError_03() throws Exception {
        performTest("parser/php81/pureIntersectionTypesWithUnionTypeError_03");
    }

    public void testEnumerations_01() throws Exception {
        performTest("parser/php81/enumerations_01");
    }

    public void testEnumerations_02() throws Exception {
        performTest("parser/php81/enumerations_02");
    }

    public void testEnumerations_03() throws Exception {
        performTest("parser/php81/enumerations_03");
    }

    public void testEnumAsTypeName() throws Exception {
        // We can use "enum" as a type name
        performTest("parser/php81/enumAsTypeName");
    }

    public void testEnumCasesWithError() throws Exception {
        performTest("parser/php81/enumCasesWithError");
    }

    public void testFirstClassCallableSyntax_01() throws Exception {
        performTest("parser/php81/firstClassCallableSyntax_01");
    }

    public void testTreatNamespacedNamesAsSingleToken_01a() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleToken_01a");
    }

    public void testExplicitOctalIntegerLiteralNotation_01() throws Exception {
        performTest("parser/php81/explicitOctalIntegerLiteralNotation_01");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_01() throws Exception {
        performTest("parser/php81/explicitOctalIntegerLiteralNotationParseError_01");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_02() throws Exception {
        performTest("parser/php81/explicitOctalIntegerLiteralNotationParseError_02");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_03() throws Exception {
        performTest("parser/php81/explicitOctalIntegerLiteralNotationParseError_03");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_04() throws Exception {
        performTest("parser/php81/explicitOctalIntegerLiteralNotationParseError_04");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_05() throws Exception {
        performTest("parser/php81/explicitOctalIntegerLiteralNotationParseError_05");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_06() throws Exception {
        performTest("parser/php81/explicitOctalIntegerLiteralNotationParseError_06");
    }

    public void testTreatNamespacedNamesAsSingleToken_01b() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleToken_01b");
    }

    public void testTreatNamespacedNamesAsSingleToken_02a() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleToken_02a");
    }

    public void testTreatNamespacedNamesAsSingleToken_02b() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleToken_02b");
    }

    public void testTreatNamespacedNamesAsSingleToken_03() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleToken_03");
    }

    public void testTreatNamespacedNamesAsSingleToken_04() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleToken_04");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_01a() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleTokenError_01a");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_01b() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleTokenError_01b");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_02a() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleTokenError_02a");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_02b() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleTokenError_02b");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_03a() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleTokenError_03a");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_03b() throws Exception {
        performTest("parser/php80/treatNamespacedNamesAsSingleTokenError_03b");
    }

    public void testGH4684() throws Exception {
        performTest("parser/gh4684");
    }

    // PHP 8.2 gh-4725
    public void testReadonlyClass_01() throws Exception {
        performTest("parser/php82/readonlyClass_01");
    }

    public void testReadonlyTraitError_01() throws Exception {
        performTest("parser/php82/readonlyTraitError_01");
    }

    public void testReadonlyInterfaceError_01() throws Exception {
        performTest("parser/php82/readonlyInterfaceError_01");
    }

    public void testReadonlyEnumError_01() throws Exception {
        performTest("parser/php82/readonlyEnumError_01");
    }

    public void testFetchPropertiesInConstExpressions_01() throws Exception {
        performTest("parser/php82/fetchPropertiesInConstExpressions_01");
    }

    public void testConstantsInTraits_01() throws Exception {
        performTest("parser/php82/constantsInTraits_01");
    }

    public void testDnfTypes_01() throws Exception {
        performTest("parser/php82/dnfTypes_01");
    }

    public void testIssueGH5585_01() throws Exception {
        performTest("parser/issueGH5585_01");
    }

    public void testIssueGH5585_02() throws Exception {
        performTest("parser/issueGH5585_02");
    }

    public void testTrueFalseNull() throws Exception {
        performTest("parser/TrueFalseNull");
    }

    public void testIssueGH5933_01() throws Exception {
        performTest("parser/issueGH5933_01");
    }

    public void testIssueGH6075_01() throws Exception {
        performTest("parser/issueGH6075_01");
    }

    // PHP 8.3
    public void testDynamicClassConstantFetch_01() throws Exception {
        performTest("parser/php83/dynamicClassConstantFetch_01");
    }

    public void testTypedClassConstants_01() throws Exception {
        performTest("parser/php83/typedClassConstants_01");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        // the same <Comment /> is shown twice becase the scanner is used twice
        File testFile = new File(getDataDir(), "testfiles/" + filename + ".php");
        StringBuilder result = new StringBuilder();
        String content = PHPLexerUtils.getFileContent(testFile);
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(content));

        Symbol symbol;
        result.append("<testresult testFile='").append(testFile.getName()).append("'>\n");
        result.append("    <scanner>\n");
        do {
            symbol = scanner.next_token();
            result.append("        <token id='").append(Utils.getASTScannerTokenName(symbol.sym)).append("' start='");
            result.append(symbol.left).append("' end='").append(symbol.right).append("'>\n");
            result.append("            <text>");
            result.append(PHPLexerUtils.getXmlStringValue(content.substring(symbol.left, symbol.right)));
            result.append("</text>\n");
            result.append("        </token>\n");
        } while (symbol.sym != ASTPHP5Symbols.EOF);
        result.append("    </scanner>\n");

        scanner.reset(new FileReader(testFile));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        if (root != null){
            Program rootnode = (Program)root.value;

            result.append((new PrintASTVisitor()).printTree(rootnode, 1));
        }
        result.append("</testresult>\n");
        return result.toString();
    }
}
