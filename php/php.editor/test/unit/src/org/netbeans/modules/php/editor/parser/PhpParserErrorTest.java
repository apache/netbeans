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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.netbeans.modules.php.editor.PHPTestBase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpParserErrorTest extends PHPTestBase {

    public PhpParserErrorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setInstances(new FileEncodingQueryImplementation() {
            @Override
            public Charset getEncoding(FileObject file) {
                return StandardCharsets.UTF_8;
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testInclude() throws Exception {
        checkErrors("testfiles/parser/include.php");
    }

    public void testIssue189630() throws Exception {
        checkErrors("testfiles/parser/issue189630.php");
    }

    public void testFieldModificators_01() throws Exception {
        checkErrors("testfiles/parser/fieldModificators_01.php");
    }

    public void testFieldModificators_02() throws Exception {
        checkErrors("testfiles/parser/fieldModificators_02.php");
    }

    public void testFieldModificators_03() throws Exception {
        checkErrors("testfiles/parser/fieldModificators_03.php");
    }

    public void testFieldModificators_04() throws Exception {
        checkErrors("testfiles/parser/fieldModificators_04.php");
    }

    public void testFieldModificators_05() throws Exception {
        checkErrors("testfiles/parser/fieldModificators_05.php");
    }

    public void testMethodModificators_01() throws Exception {
        checkErrors("testfiles/parser/methodModificators_01.php");
    }

    public void testMethodModificators_02() throws Exception {
        checkErrors("testfiles/parser/methodModificators_02.php");
    }

    public void testMethodModificators_03() throws Exception {
        checkErrors("testfiles/parser/methodModificators_03.php");
    }

    public void testMethodModificators_04() throws Exception {
        checkErrors("testfiles/parser/methodModificators_04.php");
    }

    public void testMethodModificators_05() throws Exception {
        checkErrors("testfiles/parser/methodModificators_05.php");
    }

    public void testTraits_01() throws Exception {
        checkErrors("testfiles/parser/traits_01.php");
    }

    public void testTraits_02() throws Exception {
        checkErrors("testfiles/parser/traits_02.php");
    }

    public void testTraits_03() throws Exception {
        checkErrors("testfiles/parser/traits_03.php");
    }

    public void testTraits_04() throws Exception {
        checkErrors("testfiles/parser/traits_04.php");
    }

    public void testTraits_05() throws Exception {
        checkErrors("testfiles/parser/traits_05.php");
    }

    public void testTraits_06() throws Exception {
        checkErrors("testfiles/parser/traits_06.php");
    }

    public void testTraits_07() throws Exception {
        checkErrors("testfiles/parser/traits_07.php");
    }

    public void testShortArray_01() throws Exception {
        checkErrors("testfiles/parser/shortArrays_01.php");
    }

    public void testShortArray_02() throws Exception {
        checkErrors("testfiles/parser/shortArrays_02.php");
    }

    public void testShortArraysStaticScalar_01() throws Exception {
        checkErrors("testfiles/parser/shortArraysStaticScalar_01.php");
    }

    public void testShortArraysStaticScalar_02() throws Exception {
        checkErrors("testfiles/parser/shortArraysStaticScalar_02.php");
    }

    public void testAnonymousObjectVariable() throws Exception {
        checkErrors("testfiles/parser/anonymousObjectVariable.php");
    }

    public void testArrayDereferencing_01() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_01.php");
    }

    public void testArrayDereferencing_02() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_02.php");
    }

    public void testArrayDereferencing_03() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_03.php");
    }

    public void testArrayDereferencing_04() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_04.php");
    }

    public void testArrayDereferencing_05() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_05.php");
    }

    public void testArrayDereferencing_06() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_06.php");
    }

    public void testArrayDereferencing_07() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_07.php");
    }

    public void testArrayDereferencing_08() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_08.php");
    }

    public void testArrayDereferencing_09() throws Exception {
        checkErrors("testfiles/parser/arrayDereferencing_09.php");
    }

    public void testBinaryNotation_01() throws Exception {
        checkErrors("testfiles/parser/binaryNotation_01.php");
    }

    public void testBinaryNotation_02() throws Exception {
        checkErrors("testfiles/parser/binaryNotation_02.php");
    }

    public void testBinaryNotation_03() throws Exception {
        checkErrors("testfiles/parser/binaryNotation_03.php");
    }

    public void testBinaryNotation_04() throws Exception {
        checkErrors("testfiles/parser/binaryNotation_04.php");
    }

    public void testStaticExpressionCall_01() throws Exception {
        checkErrors("testfiles/parser/staticExpressionCall_01.php");
    }

    public void testStaticExpressionCall_02() throws Exception {
        checkErrors("testfiles/parser/staticExpressionCall_02.php");
    }

    public void testCfunction() throws Exception {
        checkErrors("testfiles/parser/cfunction.php");
    }

    public void testNowDoc_01() throws Exception {
        checkErrors("testfiles/parser/nowdoc_01.php");
    }

    public void testNowDoc_02() throws Exception {
        checkErrors("testfiles/parser/nowdoc_02.php");
    }

    public void testNowDoc_03() throws Exception {
        checkErrors("testfiles/parser/nowdoc_03.php");
    }

    public void testNowDoc_04() throws Exception {
        checkErrors("testfiles/parser/nowdoc_04.php");
    }

    public void testNowDoc_05() throws Exception {
        checkErrors("testfiles/parser/nowdoc_05.php");
    }

    public void testNowDoc_06() throws Exception {
        checkErrors("testfiles/parser/nowdoc_06.php");
    }

    public void testIssueGH4345_01() throws Exception {
        checkErrors("testfiles/parser/issueGH4345_01.php");
    }

    public void testIssueGH4345_02() throws Exception {
        checkErrors("testfiles/parser/issueGH4345_02.php");
    }

    // HEREDOC
    public void testNetBeans1563_01() throws Exception {
        checkErrors("testfiles/parser/netbeans1563_01.php");
    }

    public void testNetBeans1563_02() throws Exception {
        checkErrors("testfiles/parser/netbeans1563_02.php");
    }

    public void testNetBeans1563_03() throws Exception {
        checkErrors("testfiles/parser/netbeans1563_03.php");
    }

    public void testNetBeans1563_04() throws Exception {
        checkErrors("testfiles/parser/netbeans1563_04.php");
    }

    public void testIssue198572() throws Exception {
        // fails on Mac
        checkErrors("testfiles/parser/issue198572.php");
    }

    public void testIssue213080() throws Exception {
        checkErrors("testfiles/parser/issue213080.php");
    }

    public void testIssue190105_01() throws Exception {
        checkErrors("testfiles/parser/issue190105_01.php");
    }

    public void testIssue190105_02() throws Exception {
        checkErrors("testfiles/parser/issue190105_02.php");
    }

    public void testIssue190105_03() throws Exception {
        checkErrors("testfiles/parser/issue190105_03.php");
    }

    public void testIssue211165_01() throws Exception {
        checkErrors("testfiles/parser/issue211165_01.php");
    }

    public void testIssue211165_02() throws Exception {
        checkErrors("testfiles/parser/issue211165_02.php");
    }

    public void testIssue211165_03() throws Exception {
        checkErrors("testfiles/parser/issue211165_03.php");
    }

    public void testIssue211165_04() throws Exception {
        checkErrors("testfiles/parser/issue211165_04.php");
    }

    public void testIssue211165_05() throws Exception {
        checkErrors("testfiles/parser/issue211165_05.php");
    }

    public void testIssue211165_06() throws Exception {
        checkErrors("testfiles/parser/issue211165_06.php");
    }

    public void testIssue211165_07() throws Exception {
        checkErrors("testfiles/parser/issue211165_07.php");
    }

    public void testIssue211165_08() throws Exception {
        checkErrors("testfiles/parser/issue211165_08.php");
    }

    public void testIssue211165_09() throws Exception {
        checkErrors("testfiles/parser/issue211165_09.php");
    }

    public void testFunctionCallParam() throws Exception {
        checkErrors("testfiles/parser/functionCallParam.php");
    }

    public void testIssue222857() throws Exception {
        checkErrors("testfiles/parser/issue222857.php");
    }

    public void testFinally_01() throws Exception {
        checkErrors("testfiles/parser/finally_01.php");
    }

    public void testFinally_02() throws Exception {
        checkErrors("testfiles/parser/finally_02.php");
    }

    public void testFinally_03() throws Exception {
        checkErrors("testfiles/parser/finally_03.php");
    }

    public void testListInForeach_01() throws Exception {
        checkErrors("testfiles/parser/listInForeach_01.php");
    }

    public void testListInForeach_02() throws Exception {
        // #257945
        checkErrors("testfiles/parser/listInForeach_02.php");
    }

    public void testExpressionArrayAccess_01() throws Exception {
        checkErrors("testfiles/parser/expressionArrayAccess_01.php");
    }

    public void testExpressionArrayAccess_02() throws Exception {
        checkErrors("testfiles/parser/expressionArrayAccess_02.php");
    }

    public void testExpressionArrayAccess_03() throws Exception {
        checkErrors("testfiles/parser/expressionArrayAccess_03.php");
    }

    public void testYield_01() throws Exception {
        checkErrors("testfiles/parser/yield_01.php");
    }

    public void testYield_02() throws Exception {
        checkErrors("testfiles/parser/yield_02.php");
    }

    public void testYield_03() throws Exception {
        checkErrors("testfiles/parser/yield_03.php");
    }

    public void testYield_04() throws Exception {
        checkErrors("testfiles/parser/yield_04.php");
    }

    public void testYield_05() throws Exception {
        checkErrors("testfiles/parser/yield_05.php");
    }

    public void testYield_06() throws Exception {
        checkErrors("testfiles/parser/yield_06.php");
    }

    public void testYield_07() throws Exception {
        checkErrors("testfiles/parser/yield_07.php");
    }

    public void testYield_08() throws Exception {
        checkErrors("testfiles/parser/yield_08.php");
    }

    public void testClassConstant() throws Exception {
        checkErrors("testfiles/parser/classConstant.php");
    }

    public void testClassConstantWithWhitespace() throws Exception {
        checkErrors("testfiles/parser/classConstantWithWhitespace.php");
    }

    public void testConstDefine_01() throws Exception {
        // const DEFINE = ""; is not a syntax error
        checkErrors("testfiles/parser/constDefine_01.php");
    }

    public void testConstDefine_02() throws Exception {
        checkErrors("testfiles/parser/constDefine_02.php");
    }

    // #250579
    public void testConstantArrayAccess_01() throws Exception {
        checkErrors("testfiles/parser/constantArrayAccess_01.php");
    }

    public void testConstantArrayAccess_02() throws Exception {
        checkErrors("testfiles/parser/constantArrayAccess_02.php");
    }

    public void testConstantArrayAccess_03() throws Exception {
        checkErrors("testfiles/parser/constantArrayAccess_03.php");
    }

    public void testIssue237220() throws Exception {
        checkErrors("testfiles/parser/issue237220.php");
    }

    public void testExponentiation_01() throws Exception {
        checkErrors("testfiles/parser/exponentiation_01.php");
    }

    public void testConstantScalarExpressions_01() throws Exception {
        checkErrors("testfiles/parser/constantScalarExpressions_01.php");
    }

    public void testVariadicFunctions_01() throws Exception {
        checkErrors("testfiles/parser/testVariadicFunctions_01.php");
    }

    public void testArgumentUnpacking_01() throws Exception {
        checkErrors("testfiles/parser/argumentUnpacking_01.php");
    }

    public void testUseFuncAndConst_01() throws Exception {
        checkErrors("testfiles/parser/useFuncAndConst_01.php");
    }

    public void testIssue243409() throws Exception {
        checkErrors("testfiles/parser/issue243409.php");
    }

    public void testIssue245320() throws Exception {
        checkErrors("testfiles/parser/issue245320.php");
    }

    public void testIssue243512() throws Exception {
        checkErrors("testfiles/parser/issue243512.php");
    }

    public void testSpaceship01() throws Exception {
        checkErrors("testfiles/parser/spaceship_01.php");
    }

    public void testSpaceship02() throws Exception {
        checkErrors("testfiles/parser/spaceship_02.php");
    }

    public void testElvis01() throws Exception {
        checkErrors("testfiles/parser/elvis_01.php");
    }

    public void testElvis02() throws Exception {
        checkErrors("testfiles/parser/elvis_02.php");
    }

    public void testCoalesce01() throws Exception {
        checkErrors("testfiles/parser/coalesce_01.php");
    }

    public void testCoalesce02() throws Exception {
        checkErrors("testfiles/parser/coalesce_02.php");
    }

    public void testReturnTypes01() throws Exception {
        checkErrors("testfiles/parser/returnTypes_01.php");
    }

    public void testReturnTypes02() throws Exception {
        checkErrors("testfiles/parser/returnTypes_02.php");
    }

    public void testReturnTypes03() throws Exception {
        checkErrors("testfiles/parser/returnTypes_03.php");
    }

    public void testReturnTypes04() throws Exception {
        checkErrors("testfiles/parser/returnTypes_04.php");
    }

    public void testReturnTypes05() throws Exception {
        checkErrors("testfiles/parser/returnTypes_05.php");
    }

    public void testYieldFrom01() throws Exception {
        checkErrors("testfiles/parser/yieldFrom_01.php");
    }

    public void testYieldFrom02() throws Exception {
        checkErrors("testfiles/parser/yieldFrom_02.php");
    }

    public void testYieldFrom03() throws Exception {
        checkErrors("testfiles/parser/yieldFrom_03.php");
    }

    public void testYieldFrom04() throws Exception {
        checkErrors("testfiles/parser/yieldFrom_04.php");
    }

    public void testYieldFrom05() throws Exception {
        // it's available since PHP7
        checkErrors("testfiles/parser/yieldFrom_05.php");
    }

    public void testYieldFrom06() throws Exception {
        // it's available since PHP7
        checkErrors("testfiles/parser/yieldFrom_06.php");
    }

    public void testAnonymousClass01() throws Exception {
        checkErrors("testfiles/parser/anonymousClass_01.php");
    }

    public void testUniformVariableSyntax_01() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_01.php");
    }

    public void testUniformVariableSyntax_02() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_02.php");
    }

    public void testUniformVariableSyntax_03() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_03.php");
    }

    public void testUniformVariableSyntax_04() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_04.php");
    }

    public void testUniformVariableSyntax_05() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_05.php");
    }

    public void testUniformVariableSyntax_06() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_06.php");
    }

    public void testUniformVariableSyntax_07() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_07.php");
    }

    public void testUniformVariableSyntax_08() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_08.php");
    }

    public void testUniformVariableSyntax_09() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_09.php");
    }

    public void testUniformVariableSyntax_10() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_10.php");
    }

    public void testUniformVariableSyntax_11() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_11.php");
    }

    public void testUniformVariableSyntax_12() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_12.php");
    }

    public void testUniformVariableSyntax_13() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_13.php");
    }

    public void testUniformVariableSyntax_14() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_14.php");
    }

    public void testUniformVariableSyntax_15() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_15.php");
    }

    public void testUniformVariableSyntax_16() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_16.php");
    }

    public void testUniformVariableSyntax_17() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_17.php");
    }

    public void testUniformVariableSyntax_18() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_18.php");
    }

    public void testUniformVariableSyntax_19() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_19.php");
    }

    public void testUniformVariableSyntax_20() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_20.php");
    }

    // #262141
    public void testUniformVariableSyntax_21() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_21.php");
    }

    // #262141
    public void testUniformVariableSyntax_22() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_22.php");
    }

    public void testUniformVariableSyntax_23() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_23.php");
    }

    public void testUniformVariableSyntax_24() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_24.php");
    }

    public void testUniformVariableSyntax_25() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_25.php");
    }

    public void testUniformVariableSyntax_26() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_26.php");
    }

    public void testUniformVariableSyntax_27() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_27.php");
    }

    public void testUniformVariableSyntax_28() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_28.php");
    }

    public void testUniformVariableSyntax_29() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_29.php");
    }

    public void testUniformVariableSyntax_30() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_30.php");
    }

    public void testUniformVariableSyntax_31() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_31.php");
    }

    public void testUniformVariableSyntax_32() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_32.php");
    }

    public void testUniformVariableSyntax_33() throws Exception {
        checkErrors("testfiles/parser/uniformVariableSyntax_33.php");
    }

    // #262141
    public void testUniformVariableSyntax_34() throws Exception {
        // ... -> ... (->|::)
        checkErrors("testfiles/parser/uniformVariableSyntax_34.php");
    }

    public void testUniformVariableSyntax_35() throws Exception {
        // ... :: ... (->|::)
        checkErrors("testfiles/parser/uniformVariableSyntax_35.php");
    }

    public void testUniformVariableSyntax_36() throws Exception {
        // dereferencable
        checkErrors("testfiles/parser/uniformVariableSyntax_36.php");
    }

    public void testUniformVariableSyntax_37() throws Exception {
        // [$object1, $object2][0]->property;
        checkErrors("testfiles/parser/uniformVariableSyntax_37.php");
    }

    // NETBEANS-1552
    public void testUniformVariableSyntax_38() throws Exception {
        // ($test = new UVS())->isTest('test');
        checkErrors("testfiles/parser/uniformVariableSyntax_38.php");
    }

    public void testUniformVariableSyntax_39() throws Exception {
        // Test::{test('foo')}()::{test('bar')}();
        checkErrors("testfiles/parser/uniformVariableSyntax_39.php");
    }

    public void testCloneExpression_01() throws Exception {
        checkErrors("testfiles/parser/cloneExpression_01.php");
    }

    public void testGroupUse_01() throws Exception {
        checkErrors("testfiles/parser/groupUse_01.php");
    }

    public void testGroupUse_02() throws Exception {
        checkErrors("testfiles/parser/groupUse_02.php");
    }

    public void testGroupUse_03() throws Exception {
        checkErrors("testfiles/parser/groupUse_03.php");
    }

    public void testGroupUse_04() throws Exception {
        checkErrors("testfiles/parser/groupUse_04.php");
    }

    public void testIssue258959() throws Exception {
        checkErrors("testfiles/parser/issue258959.php");
    }

    public void testIssue268496_01() throws Exception {
        checkErrors("testfiles/parser/issue268496_01.php");
    }

    public void testIssue268496_02() throws Exception {
        // syntax error : callable
        checkErrors("testfiles/parser/issue268496_02.php");
    }

    public void testIssue268496_03() throws Exception {
        // syntax error : __TRAIT__
        checkErrors("testfiles/parser/issue268496_03.php");
    }

    public void testIssue268712() throws Exception {
        checkErrors("testfiles/parser/issue268712.php");
    }

    public void testIssue262144() throws Exception {
        // yeild and yield from expressions for PHP7
        checkErrors("testfiles/parser/issue262144.php");
    }

    // PHP7.1
    public void testNullableTypes_01() throws Exception {
        checkErrors("testfiles/parser/nullableTypes_01.php");
    }

    public void testNullableTypes_02() throws Exception {
        checkErrors("testfiles/parser/nullableTypes_02.php");
    }

    public void testNullableTypes_03() throws Exception {
        checkErrors("testfiles/parser/nullableTypes_03.php");
    }

    public void testMultiCatchInFirstCatchClause() throws Exception {
        checkErrors("testfiles/parser/multiCatchInFirstCatchClause.php");
    }

    public void testMultiCatchInSecondCatchClause() throws Exception {
        checkErrors("testfiles/parser/multiCatchInSecondCatchClause.php");
    }

    public void testMultiCatchWithFinally() throws Exception {
        checkErrors("testfiles/parser/multiCatchWithFinally.php");
    }

    public void testClassConstantVisibility_01() throws Exception {
        checkErrors("testfiles/parser/classConstantVisibility_01.php");
    }

    public void testKeyedList_01() throws Exception {
        checkErrors("testfiles/parser/keyedList_01.php");
    }

    public void testSymmetricArrayDestructuring_01() throws Exception {
        checkErrors("testfiles/parser/symmetricArrayDestructuring_01.php");
    }

    // #262141 PHP7.0
    public void testContextSensitiveLexer_01() throws Exception {
        checkErrors("testfiles/parser/contextSensitiveLexer_01.php");
    }

    public void testContextSensitiveLexer_02() throws Exception {
        checkErrors("testfiles/parser/contextSensitiveLexer_02.php");
    }

    public void testContextSensitiveLexer_03() throws Exception {
        checkErrors("testfiles/parser/contextSensitiveLexer_03.php");
    }

    public void testContextSensitiveLexer_04() throws Exception {
        checkErrors("testfiles/parser/contextSensitiveLexer_04.php");
    }

    public void testContextSensitiveLexer_05() throws Exception {
        checkErrors("testfiles/parser/contextSensitiveLexer_05.php");
    }

    public void testContextSensitiveLexerWithConstVisibility_01() throws Exception {
        checkErrors("testfiles/parser/contextSensitiveLexerWithConstVisibility_01.php");
    }

    public void testIssue271109() throws Exception {
        // PHP7
        checkErrors("testfiles/parser/issue271109.php");
    }

    public void testGroupUseTrailingCommas_01() throws Exception {
        checkErrors("testfiles/parser/groupUseTrailingCommas_01.php");
    }

    public void testGroupUseTrailingCommas_02() throws Exception {
        checkErrors("testfiles/parser/groupUseTrailingCommas_02.php");
    }

    public void testGroupUseTrailingCommas_03() throws Exception {
        checkErrors("testfiles/parser/groupUseTrailingCommas_03.php");
    }

    public void testGroupUseTrailingCommas_04() throws Exception {
        checkErrors("testfiles/parser/groupUseTrailingCommas_04.php");
    }

    // PHP 7.3
    public void testFunctionCallTrailingCommas_01() throws Exception {
        checkErrors("testfiles/parser/php73/functionCallTrailingCommas_01.php");
    }

    public void testFunctionCallTrailingCommas_02() throws Exception {
        checkErrors("testfiles/parser/php73/functionCallTrailingCommas_02.php");
    }

    public void testFunctionCallTrailingCommas_03() throws Exception {
        checkErrors("testfiles/parser/php73/functionCallTrailingCommas_03.php");
    }

    public void testFunctionCallTrailingCommas_04() throws Exception {
        checkErrors("testfiles/parser/php73/functionCallTrailingCommas_04.php");
    }

    public void testListReferenceAssignment_01() throws Exception {
        checkErrors("testfiles/parser/php73/listReferenceAssignment_01.php");
    }

    public void testFlexibleHeredocIndentSpaces_01() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_indent_spaces_01.php");
    }

    public void testFlexibleHeredocIndentSpaces_02() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_indent_spaces_02.php");
    }

    public void testFlexibleHeredocIndentSpaces_03() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_indent_spaces_03.php");
    }

    public void testFlexibleHeredocIndentSpaces_04() throws Exception {
        // no new line at EOF
        checkErrors("testfiles/parser/php73/heredoc_indent_spaces_04.php");
    }

    public void testFlexibleHeredocIndentTabs_01() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_indent_tabs_01.php");
    }

    public void testFlexibleHeredocIndentTabs_02() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_indent_tabs_02.php");
    }

    public void testFlexibleHeredocIndentTabs_03() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_indent_tabs_03.php");
    }

    public void testFlexibleHeredocNewLine_01() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_new_line_01.php");
    }

    public void testFlexibleHeredocNewLine_02() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_new_line_02.php");
    }

    public void testFlexibleHeredocNewLine_03() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_new_line_03.php");
    }

    public void testFlexibleHeredocNewLine_04() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_new_line_04.php");
    }

    public void testFlexibleHeredocNewLineError_01() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_new_line_error_01.php");
    }

    public void testFlexibleHeredocNewLineError_02() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_new_line_error_02.php");
    }

    public void testFlexibleHeredocMixed_01() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_mixed_01.php");
    }

    public void testFlexibleHeredocMixed_02() throws Exception {
        checkErrors("testfiles/parser/php73/heredoc_mixed_02.php");
    }

    public void testFlexibleHeredocMixed_03() throws Exception {
        // no new line at EOF
        checkErrors("testfiles/parser/php73/heredoc_mixed_03.php");
    }

    public void testFlexibleNowdocIndentSpaces_01() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_indent_spaces_01.php");
    }

    public void testFlexibleNowdocIndentSpaces_02() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_indent_spaces_02.php");
    }

    public void testFlexibleNowdocIndentSpaces_03() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_indent_spaces_03.php");
    }

    public void testFlexibleNowdocIndentSpaces_04() throws Exception {
        // no new line at EOF
        checkErrors("testfiles/parser/php73/nowdoc_indent_spaces_04.php");
    }

    public void testFlexibleNowdocIndentTabs_01() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_indent_tabs_01.php");
    }

    public void testFlexibleNowdocIndentTabs_02() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_indent_tabs_02.php");
    }

    public void testFlexibleNowdocIndentTabs_03() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_indent_tabs_03.php");
    }

    public void testFlexibleNowdocNewLine_01() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_new_line_01.php");
    }

    public void testFlexibleNowdocNewLine_02() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_new_line_02.php");
    }

    public void testFlexibleNowdocNewLine_03() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_new_line_03.php");
    }

    public void testFlexibleNowdocNewLine_04() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_new_line_04.php");
    }

    public void testFlexibleNowdocNewLineError_01() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_new_line_error_01.php");
    }

    public void testFlexibleNowdocNewLineError_02() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_new_line_error_02.php");
    }

    public void testFlexibleNowdocMixed_01() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_mixed_01.php");
    }

    public void testFlexibleNowdocMixed_02() throws Exception {
        checkErrors("testfiles/parser/php73/nowdoc_mixed_02.php");
    }

    public void testFlexibleNowdocMixed_03() throws Exception {
        // no new line at EOF
        checkErrors("testfiles/parser/php73/nowdoc_mixed_03.php");
    }

    // PHP 7.4
    public void testNullCoalescingAssignmentOperator_01() throws Exception {
        // "??="
        checkErrors("testfiles/parser/php74/nullCoalescingAssignmentOperator_01.php");
    }

    public void testTypedProperties20Class() throws Exception {
        checkErrors("testfiles/parser/php74/typedPropertiesClass.php");
    }

    public void testTypedProperties20Trait() throws Exception {
        checkErrors("testfiles/parser/php74/typedPropertiesTrait.php");
    }

    public void testSpreadOperatorInArrayExpression_01() throws Exception {
        checkErrors("testfiles/parser/php74/spreadOperatorInArrayExpression_01.php");
    }

    public void testSpreadOperatorInArrayExpression_02() throws Exception {
        checkErrors("testfiles/parser/php74/spreadOperatorInArrayExpression_02.php");
    }

    public void testSpreadOperatorInArrayExpression_03() throws Exception {
        checkErrors("testfiles/parser/php74/spreadOperatorInArrayExpression_03.php");
    }

    public void testSpreadOperatorInArrayExpression_04() throws Exception {
        checkErrors("testfiles/parser/php74/spreadOperatorInArrayExpression_04.php");
    }

    public void testNumericLiteralSeparator_01() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparator_01.php");
    }

    public void testNumericLiteralSeparatorParseError_01() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_01.php");
    }

    public void testNumericLiteralSeparatorParseError_02() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_02.php");
    }

    public void testNumericLiteralSeparatorParseError_03() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_03.php");
    }

    public void testNumericLiteralSeparatorParseError_04() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_04.php");
    }

    public void testNumericLiteralSeparatorParseError_05() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_05.php");
    }

    public void testNumericLiteralSeparatorParseError_06() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_06.php");
    }

    public void testNumericLiteralSeparatorParseError_07() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_07.php");
    }

    public void testNumericLiteralSeparatorParseError_08() throws Exception {
        checkErrors("testfiles/parser/php74/numericLiteralSeparatorParseError_08.php");
    }

    public void testArrowFunctions_01() throws Exception {
        checkErrors("testfiles/parser/php74/arrowFunctions_01.php");
    }

    public void testArrowFunctionsParseError_01() throws Exception {
        checkErrors("testfiles/parser/php74/arrowFunctionsParseError_01.php");
    }

    public void testArrowFunctionsParseError_02() throws Exception {
        checkErrors("testfiles/parser/php74/arrowFunctionsParseError_02.php");
    }

    public void testArrowFunctionsParseError_03() throws Exception {
        checkErrors("testfiles/parser/php74/arrowFunctionsParseError_03.php");
    }

    public void testArrowFunctionsParseError_04() throws Exception {
        checkErrors("testfiles/parser/php74/arrowFunctionsParseError_04.php");
    }

    // PHP 8.0
    public void testNonCapturingCatches_01() throws Exception {
        checkErrors("testfiles/parser/php80/nonCapturingCatches_01.php");
    }

    public void testAllowTrailinCommaInParameterList_01() throws Exception {
        checkErrors("testfiles/parser/php80/allowTrailingCommaInParameterList_01.php");
    }

    public void testThrowExpression_01() throws Exception {
        checkErrors("testfiles/parser/php80/throwExpression_01.php");
    }

    public void testClassNameLiteralOnObjects_01() throws Exception {
        checkErrors("testfiles/parser/php80/classNameLiteralOnObjects_01.php");
    }

    public void testMatchExpression_01() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpression_01.php");
    }

    public void testMatchExpression_02() throws Exception {
        // nested match
        checkErrors("testfiles/parser/php80/matchExpression_02.php");
    }

    public void testMatchExpression_03() throws Exception {
        // multiple conditions
        checkErrors("testfiles/parser/php80/matchExpression_03.php");
    }

    public void testMatchExpression_04() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpression_04.php");
    }

    public void testMatchExpression_05() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpression_05.php");
    }

    public void testMatchExpressionError_01() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpressionError_01.php");
    }

    public void testMatchExpressionError_02() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpressionError_02.php");
    }

    public void testMatchExpressionError_03() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpressionError_03.php");
    }

    public void testMatchExpressionError_04() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpressionError_04.php");
    }

    public void testMatchExpressionError_05() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpressionError_05.php");
    }

    public void testMatchExpressionError_06() throws Exception {
        checkErrors("testfiles/parser/php80/matchExpressionError_06.php");
    }

    public void testUnionTypesTypes() throws Exception {
        checkErrors("testfiles/parser/php80/unionTypesTypes.php");
    }

    public void testUnionTypesFunctions() throws Exception {
        checkErrors("testfiles/parser/php80/unionTypesFunctions.php");
    }

    public void testUnionTypesError_01() throws Exception {
        checkErrors("testfiles/parser/php80/unionTypesError_01.php");
    }

    public void testUnionTypesError_02() throws Exception {
        checkErrors("testfiles/parser/php80/unionTypesError_02.php");
    }

    public void testUnionTypesError_03() throws Exception {
        checkErrors("testfiles/parser/php80/unionTypesError_03.php");
    }

    public void testStaticReturnType_01() throws Exception {
        checkErrors("testfiles/parser/php80/staticReturnType_01.php");
    }

    public void testStaticReturnTypeErrorWithFieldType_01() throws Exception {
        // nullable type
        checkErrors("testfiles/parser/php80/staticReturnTypeErrorWithFieldType_01.php");
    }

    public void testStaticReturnTypeErrorWithFieldType_02() throws Exception {
        // union type
        checkErrors("testfiles/parser/php80/staticReturnTypeErrorWithFieldType_02.php");
    }

    public void testStaticReturnTypeErrorWithParameterType_01() throws Exception {
        checkErrors("testfiles/parser/php80/staticReturnTypeErrorWithParameterType_01.php");
    }

    public void testStaticReturnTypeErrorWithParameterType_02() throws Exception {
        // nullable type
        checkErrors("testfiles/parser/php80/staticReturnTypeErrorWithParameterType_02.php");
    }

    public void testStaticReturnTypeErrorWithParameterType_03() throws Exception {
        // union type
        checkErrors("testfiles/parser/php80/staticReturnTypeErrorWithParameterType_03.php");
    }

    public void testMixedType_01() throws Exception {
        checkErrors("testfiles/parser/php80/mixedType_01.php");
    }

    public void testMixedError_01() throws Exception {
        checkErrors("testfiles/parser/php80/mixedTypeError_01.php");
    }

    public void testNullsafeOperator_01() throws Exception {
        checkErrors("testfiles/parser/php80/nullsafeOperator_01.php");
    }

    public void testNullsafeOperator_02() throws Exception {
        checkErrors("testfiles/parser/php80/nullsafeOperator_02.php");
    }

    public void testNullsafeOperator_03() throws Exception {
        checkErrors("testfiles/parser/php80/nullsafeOperator_03.php");
    }

    public void testAttributeSyntax_01() throws Exception {
        checkErrors("testfiles/parser/php80/attributeSyntax_01.php");
    }

    public void testAttributeSyntax_02() throws Exception {
        checkErrors("testfiles/parser/php80/attributeSyntax_02.php");
    }

    // [NETBEANS-6193] attributes of interface constants and methods
    public void testAttributeSyntax_03() throws Exception {
        checkErrors("testfiles/parser/php80/attributeSyntax_03.php");
    }

    public void testAttributeSyntaxError_01() throws Exception {
        checkErrors("testfiles/parser/php80/attributeSyntaxError_01.php");
    }

    public void testAttributeSyntaxError_02() throws Exception {
        checkErrors("testfiles/parser/php80/attributeSyntaxError_02.php");
    }

    public void testConstructorPropertyPromotion_01() throws Exception {
        checkErrors("testfiles/parser/php80/constructorPropertyPromotion_01.php");
    }

    public void testConstructorPropertyPromotion_02() throws Exception {
        checkErrors("testfiles/parser/php80/constructorPropertyPromotion_02.php");
    }

    public void testConstructorPropertyPromotionErrorWithStatic() throws Exception {
        checkErrors("testfiles/parser/php80/constructorPropertyPromotionErrorWithStatic.php");
    }

    public void testAllowTrailingCommaInClosureUseLists_01() throws Exception {
        checkErrors("testfiles/parser/php80/allowTrailingCommaInClosureUseLists_01.php");
    }

    public void testNamedArguments_01() throws Exception {
        checkErrors("testfiles/parser/php80/namedArguments_01.php");
    }

    public void testNamedArgumentsWithAttribute_01() throws Exception {
        checkErrors("testfiles/parser/php80/namedArgumentsWithAttribute_01.php");
    }

    // [NETBEANS-5599] PHP 8.1
    public void testFinalClassConstants_01() throws Exception {
        checkErrors("testfiles/parser/php81/finalClassConstants_01.php");
    }

    public void testFinalClassConstantsWithAttributes_01() throws Exception {
        checkErrors("testfiles/parser/php81/finalClassConstantsWithAttributes_01.php");
    }

    public void testFinalClassMethods_01() throws Exception {
        checkErrors("testfiles/parser/finalClassMethods_01.php");
    }

    public void testFinalTraitMethods_01() throws Exception {
        checkErrors("testfiles/parser/finalTraitMethods_01.php");
    }

    public void testReadonlyProperties_01() throws Exception {
        checkErrors("testfiles/parser/php81/readonlyProperties_01.php");
    }

    public void testReadonlyPropertiesWithConstError() throws Exception {
        checkErrors("testfiles/parser/php81/readonlyPropertiesWithConstError.php");
    }

    public void testReadonlyPropertiesWithStaticError() throws Exception {
        checkErrors("testfiles/parser/php81/readonlyPropertiesWithStaticError.php");
    }

    public void testNewInInitializers_01() throws Exception {
        checkErrors("testfiles/parser/php81/newInInitializers_01.php");
    }

    public void testNewInInitializers_02() throws Exception {
        checkErrors("testfiles/parser/php81/newInInitializers_02.php");
    }

    public void testNewInInitializersWithClassConstantError() throws Exception {
        checkErrors("testfiles/parser/php81/newInInitializersWithClassConstantError.php");
    }

    public void testNewInInitializersWithPropertyError() throws Exception {
        checkErrors("testfiles/parser/php81/newInInitializersWithPropertyError.php");
    }

    public void testPureIntersectionTypes_01() throws Exception {
        checkErrors("testfiles/parser/php81/pureIntersectionTypes_01.php");
    }

    public void testPureIntersectionTypesWithUnionTypeError_01() throws Exception {
        checkErrors("testfiles/parser/php81/pureIntersectionTypesWithUnionTypeError_01.php");
    }

    public void testPureIntersectionTypesWithUnionTypeError_02() throws Exception {
        checkErrors("testfiles/parser/php81/pureIntersectionTypesWithUnionTypeError_02.php");
    }

    public void testPureIntersectionTypesWithUnionTypeError_03() throws Exception {
        checkErrors("testfiles/parser/php81/pureIntersectionTypesWithUnionTypeError_03.php");
    }

    public void testEnumerations_01() throws Exception {
        checkErrors("testfiles/parser/php81/enumerations_01.php");
    }

    public void testEnumerations_02() throws Exception {
        checkErrors("testfiles/parser/php81/enumerations_02.php");
    }

    public void testEnumerations_03() throws Exception {
        checkErrors("testfiles/parser/php81/enumerations_03.php");
    }

    public void testEnumAsTypeName() throws Exception {
        // We can use "enum" as a type name
        checkErrors("testfiles/parser/php81/enumAsTypeName.php");
    }

    public void testEnumCasesWithError() throws Exception {
        checkErrors("testfiles/parser/php81/enumCasesWithError.php");
    }

    public void testFirstClassCallableSyntax_01() throws Exception {
        checkErrors("testfiles/parser/php81/firstClassCallableSyntax_01.php");
    }

    public void testExplicitOctalIntegerLiteralNotation_01() throws Exception {
        checkErrors("testfiles/parser/php81/explicitOctalIntegerLiteralNotation_01.php");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_01() throws Exception {
        checkErrors("testfiles/parser/php81/explicitOctalIntegerLiteralNotationParseError_01.php");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_02() throws Exception {
        checkErrors("testfiles/parser/php81/explicitOctalIntegerLiteralNotationParseError_02.php");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_03() throws Exception {
        checkErrors("testfiles/parser/php81/explicitOctalIntegerLiteralNotationParseError_03.php");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_04() throws Exception {
        checkErrors("testfiles/parser/php81/explicitOctalIntegerLiteralNotationParseError_04.php");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_05() throws Exception {
        checkErrors("testfiles/parser/php81/explicitOctalIntegerLiteralNotationParseError_05.php");
    }

    public void testExplicitOctalIntegerLiteralNotationParseError_06() throws Exception {
        checkErrors("testfiles/parser/php81/explicitOctalIntegerLiteralNotationParseError_06.php");
    }

    public void testTreatNamespacedNamesAsSingleToken_01a() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleToken_01a.php");
    }

    public void testTreatNamespacedNamesAsSingleToken_01b() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleToken_01b.php");
    }

    public void testTreatNamespacedNamesAsSingleToken_02a() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleToken_02a.php");
    }

    public void testTreatNamespacedNamesAsSingleToken_02b() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleToken_02b.php");
    }

    public void testTreatNamespacedNamesAsSingleToken_03() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleToken_03.php");
    }

    public void testTreatNamespacedNamesAsSingleToken_04() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleToken_04.php");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_01a() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleTokenError_01a.php");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_01b() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleTokenError_01b.php");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_02a() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleTokenError_02a.php");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_02b() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleTokenError_02b.php");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_03a() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleTokenError_03a.php");
    }

    public void testTreatNamespacedNamesAsSingleTokenError_03b() throws Exception {
        checkErrors("testfiles/parser/php80/treatNamespacedNamesAsSingleTokenError_03b.php");
    }

    public void testGH4684() throws Exception {
        checkErrors("testfiles/parser/gh4684.php");
    }

    public void testReadonlyClass_01() throws Exception {
        checkErrors("testfiles/parser/php82/readonlyClass_01.php");
    }

    public void testFetchPropertiesInConstExpressions_01() throws Exception {
        checkErrors("testfiles/parser/php82/fetchPropertiesInConstExpressions_01.php");
    }

    public void testReadonlyTraitError_01() throws Exception {
        checkErrors("testfiles/parser/php82/readonlyTraitError_01.php");
    }

    public void testReadonlyInterfaceError_01() throws Exception {
        checkErrors("testfiles/parser/php82/readonlyInterfaceError_01.php");
    }

    public void testReadonlyEnumError_01() throws Exception {
        checkErrors("testfiles/parser/php82/readonlyEnumError_01.php");
    }

    public void testConstantsInTraits_01() throws Exception {
        checkErrors("testfiles/parser/php82/constantsInTraits_01.php");
    }

    public void testDnfTypes_01() throws Exception {
        checkErrors("testfiles/parser/php82/dnfTypes_01.php");
    }

    public void testIssueGH5585_01() throws Exception {
        checkErrors("testfiles/parser/issueGH5585_01.php");
    }

    public void testIssueGH5585_02() throws Exception {
        checkErrors("testfiles/parser/issueGH5585_02.php");
    }

    public void testIssueGH5933_01() throws Exception {
        checkErrors("testfiles/parser/issueGH5933_01.php");
    }

    // PHP 8.3
    public void testDynamicClassConstantFetch_01() throws Exception {
        checkErrors("testfiles/parser/php83/dynamicClassConstantFetch_01.php");
    }

    public void testTypedClassConstants_01() throws Exception {
        checkErrors("testfiles/parser/php83/typedClassConstants_01.php");
    }
}
