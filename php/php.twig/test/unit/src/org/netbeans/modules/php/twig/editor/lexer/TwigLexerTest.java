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
package org.netbeans.modules.php.twig.editor.lexer;

import java.io.File;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.twig.editor.util.TestUtils;

/**
 * Tests for Twig lexer.
 */
public class TwigLexerTest extends TwigLexerTestBase {

    public TwigLexerTest(String testName) {
        super(testName);
    }

    public void testExceptionsUnclosedTag() throws Exception {
        performTest("exceptions-unclosed_tag");
    }

    public void testExpressionsArrayCall() throws Exception {
        performTest("expressions-array_call");
    }

    public void testExpressionsArray() throws Exception {
        performTest("expressions-array");
    }

    public void testExpressionsBinary() throws Exception {
        performTest("expressions-binary");
    }

    public void testExpressionsComparison() throws Exception {
        performTest("expressions-comparison");
    }

    public void testExpressionsDotdot() throws Exception {
        performTest("expressions-dotdot");
    }

    public void testExpressionsGrouping() throws Exception {
        performTest("expressions-grouping");
    }

    public void testExpressionsLiterals() throws Exception {
        performTest("expressions-literals");
    }

    public void testExpressionsMagicCall() throws Exception {
        performTest("expressions-magic_call");
    }

    public void testExpressionsMethodCall() throws Exception {
        performTest("expressions-method_call");
    }

    public void testExpressionsPostfix() throws Exception {
        performTest("expressions-postfix");
    }

    public void testExpressionsStrings() throws Exception {
        performTest("expressions-strings");
    }

    public void testExpressionsTernaryOperator() throws Exception {
        performTest("expressions-ternary_operator");
    }

    public void testExpressionsUnary() throws Exception {
        performTest("expressions-unary");
    }

    // #246638
    public void testExpressionsNestedArray_01() throws Exception {
        performTest("expressions-nested-array_01");
    }

    public void testExpressionsNestedArray_02() throws Exception {
        performTest("expressions-nested-array_02");
    }

    public void testExpressionsNestedArray_03() throws Exception {
        performTest("expressions-nested-array_03");
    }

    public void testFiltersConvertEncoding() throws Exception {
        performTest("filters-convert_encoding");
    }

    public void testFiltersDateDefaultFormat() throws Exception {
        performTest("filters-date_default_format");
    }

    public void testFiltersDate() throws Exception {
        performTest("filters-date");
    }

    public void testFiltersDefault() throws Exception {
        performTest("filters-default");
    }

    public void testFiltersDynamicFilter() throws Exception {
        performTest("filters-dynamic_filter");
    }

    public void testFiltersEscapeNonSupportedCharset() throws Exception {
        performTest("filters-escape_non_supported_charset");
    }

    public void testFiltersEscape() throws Exception {
        performTest("filters-escape");
    }

    public void testFiltersForceEscape() throws Exception {
        performTest("filters-force_escape");
    }

    public void testFiltersFormat() throws Exception {
        performTest("filters-format");
    }

    public void testFiltersJsonEncode() throws Exception {
        performTest("filters-json_encode");
    }

    public void testFiltersLength() throws Exception {
        performTest("filters-length");
    }

    public void testFiltersMerge() throws Exception {
        performTest("filters-merge");
    }

    public void testFiltersNl2br() throws Exception {
        performTest("filters-nl2br");
    }

    public void testFiltersReplace() throws Exception {
        performTest("filters-replace");
    }

    public void testFiltersSort() throws Exception {
        performTest("filters-sort");
    }

    public void testFiltersSpecialChars() throws Exception {
        performTest("filters-special_chars");
    }

    public void testFunctionsAttribute() throws Exception {
        performTest("functions-attribute");
    }

    public void testFunctionsConstant() throws Exception {
        performTest("functions-constant");
    }

    public void testFunctionsCycle() throws Exception {
        performTest("functions-cycle");
    }

    public void testFunctionsDump() throws Exception {
        performTest("functions-dump");
    }

    public void testFunctionsDynamicFunction() throws Exception {
        performTest("functions-dynamic_function");
    }

    public void testFunctionsSpecialChars() throws Exception {
        performTest("functions-special_chars");
    }

    public void testMacrosWithFilters() throws Exception {
        performTest("macros-with_filters");
    }

    public void testRegressionEmptyToken() throws Exception {
        performTest("regression-empty_token");
    }

    public void testTagsAutoescapeBasic() throws Exception {
        performTest("tags-autoescape-basic");
    }

    public void testTagsAutoescapeBlocks() throws Exception {
        performTest("tags-autoescape-blocks");
    }

    public void testTagsAutoescapeDoubleEscaping() throws Exception {
        performTest("tags-autoescape-double_escaping");
    }

    public void testTagsAutoescapeFunctions() throws Exception {
        performTest("tags-autoescape-functions");
    }

    public void testTagsAutoescapeLiteral() throws Exception {
        performTest("tags-autoescape-literal");
    }

    public void testTagsAutoescapeNested() throws Exception {
        performTest("tags-autoescape-nested");
    }

    public void testTagsAutoescapeObjects() throws Exception {
        performTest("tags-autoescape-objects");
    }

    public void testTagsAutoescapeRaw() throws Exception {
        performTest("tags-autoescape-raw");
    }

    public void testTagsAutoescapeStrategy() throws Exception {
        performTest("tags-autoescape-strategy");
    }

    public void testTagsAutoescapeType() throws Exception {
        performTest("tags-autoescape-type");
    }

    public void testTagsAutoescapeWithFiltersArguments() throws Exception {
        performTest("tags-autoescape-with_filters_arguments");
    }

    public void testTagsAutoescapeWithFilters() throws Exception {
        performTest("tags-autoescape-with_filters");
    }

    public void testTagsAutoescapeWithPreEscapeFilters() throws Exception {
        performTest("tags-autoescape-with_pre_escape_filters");
    }

    public void testTagsBlockBasic() throws Exception {
        performTest("tags-block-basic");
    }

    public void testTagsBlockSpecialChars() throws Exception {
        performTest("tags-block-special_chars");
    }

    public void testTagsFilterBasic() throws Exception {
        performTest("tags-filter-basic");
    }

    public void testTagsFilterJsonEncode() throws Exception {
        performTest("tags-filter-json_encode");
    }

    public void testTagsFilterMultiple() throws Exception {
        performTest("tags-filter-multiple");
    }

    public void testTagsFilterNested() throws Exception {
        performTest("tags-filter-nested");
    }

    public void testTagsFilterWithForTag() throws Exception {
        performTest("tags-filter-with_for_tag");
    }

    public void testTagsFilterWithIfTag() throws Exception {
        performTest("tags-filter-with_if_tag");
    }

    public void testTagsForCondition() throws Exception {
        performTest("tags-for-condition");
    }

    public void testTagsForContext() throws Exception {
        performTest("tags-for-context");
    }

    public void testTagsForElse() throws Exception {
        performTest("tags-for-else");
    }

    public void testTagsForInnerVariables() throws Exception {
        performTest("tags-for-inner_variables");
    }

    public void testTagsForKeysAndValues() throws Exception {
        performTest("tags-for-keys_and_values");
    }

    public void testTagsForKeys() throws Exception {
        performTest("tags-for-keys");
    }

    public void testTagsForLoopContextLocal() throws Exception {
        performTest("tags-for-loop_context_local");
    }

    public void testTagsForLoopContext() throws Exception {
        performTest("tags-for-loop_context");
    }

    public void testTagsForNestedElse() throws Exception {
        performTest("tags-for-nested_else");
    }

    public void testTagsForObjectsCountable() throws Exception {
        performTest("tags-for-objects_countable");
    }

    public void testTagsForObjects() throws Exception {
        performTest("tags-for-objects");
    }

    public void testTagsForRecursive() throws Exception {
        performTest("tags-for-recursive");
    }

    public void testTagsForValues() throws Exception {
        performTest("tags-for-values");
    }

    public void testTagsFrom() throws Exception {
        performTest("tags-from");
    }

    public void testTagsIfBasic() throws Exception {
        performTest("tags-if-basic");
    }

    public void testTagsIfExpression() throws Exception {
        performTest("tags-if-expression");
    }

    public void testTagsIncludeBasic() throws Exception {
        performTest("tags-include-basic");
    }

    public void testTagsIncludeExpression() throws Exception {
        performTest("tags-include-expression");
    }

    public void testTagsIncludeIgnoreMissing() throws Exception {
        performTest("tags-include-ignore_missing");
    }

    public void testTagsIncludeOnly() throws Exception {
        performTest("tags-include-only");
    }

    public void testTagsIncludeTemplateInstance() throws Exception {
        performTest("tags-include-template_instance");
    }

    public void testTagsIncludeTemplatesAsArray() throws Exception {
        performTest("tags-include-templates_as_array");
    }

    public void testTagsIncludeWithVariables() throws Exception {
        performTest("tags-include-with_variables");
    }

    public void testTagsInheritanceBasic() throws Exception {
        performTest("tags-inheritance-basic");
    }

    public void testTagsInheritanceConditional() throws Exception {
        performTest("tags-inheritance-conditional");
    }

    public void testTagsInheritanceDynamic() throws Exception {
        performTest("tags-inheritance-dynamic");
    }

    public void testTagsInheritanceExtendsAsArray() throws Exception {
        performTest("tags-inheritance-extends_as_array");
    }

    public void testTagsInheritanceMultiple() throws Exception {
        performTest("tags-inheritance-multiple");
    }

    public void testTagsInheritanceNestedInheritance() throws Exception {
        performTest("tags-inheritance-nested_inheritance");
    }

    public void testTagsInheritanceParentChange() throws Exception {
        performTest("tags-inheritance-parent_change");
    }

    public void testTagsInheritanceParentInABlock() throws Exception {
        performTest("tags-inheritance-parent_in_a_block");
    }

    public void testTagsInheritanceParentIsolation() throws Exception {
        performTest("tags-inheritance-parent_isolation");
    }

    public void testTagsInheritanceParentNested() throws Exception {
        performTest("tags-inheritance-parent_nested");
    }

    public void testTagsInheritanceParent() throws Exception {
        performTest("tags-inheritance-parent");
    }

    public void testTagsInheritanceParentWithoutExtendsButTraits() throws Exception {
        performTest("tags-inheritance-parent_without_extends_but_traits");
    }

    public void testTagsInheritanceParentWithoutExtends() throws Exception {
        performTest("tags-inheritance-parent_without_extends");
    }

    public void testTagsInheritanceTemplateInstance() throws Exception {
        performTest("tags-inheritance-template_instance");
    }

    public void testTagsInheritanceUse() throws Exception {
        performTest("tags-inheritance-use");
    }

    public void testTagsMacroBasic() throws Exception {
        performTest("tags-macro-basic");
    }

    public void testTagsMacroEndmacroName() throws Exception {
        performTest("tags-macro-endmacro_name");
    }

    public void testTagsMacroExternal() throws Exception {
        performTest("tags-macro-external");
    }

    public void testTagsMacroFrom() throws Exception {
        performTest("tags-macro-from");
    }

    public void testTagsMacroSelfImport() throws Exception {
        performTest("tags-macro-self_import");
    }

    public void testTagsMacroSpecialChars() throws Exception {
        performTest("tags-macro-special_chars");
    }

    public void testTagsRawBasic() throws Exception {
        performTest("tags-raw-basic");
    }

    public void testTagsSetBasic() throws Exception {
        performTest("tags-set-basic");
    }

    public void testTagsSetCapture() throws Exception {
        performTest("tags-set-capture");
    }

    public void testTagsSetExpression() throws Exception {
        performTest("tags-set-expression");
    }

    public void testTagsSpacelessSimple() throws Exception {
        performTest("tags-spaceless-simple");
    }

    public void testTagsSpecialChars() throws Exception {
        performTest("tags-special_chars");
    }

    public void testTagsTrimBlock() throws Exception {
        performTest("tags-trim_block");
    }

    public void testTagsUseAliases() throws Exception {
        performTest("tags-use-aliases");
    }

    public void testTagsUseBasic() throws Exception {
        performTest("tags-use-basic");
    }

    public void testTagsUseDeepEmpty() throws Exception {
        performTest("tags-use-deep_empty");
    }

    public void testTagsUseDeep() throws Exception {
        performTest("tags-use-deep");
    }

    public void testTagsUseMultipleAliases() throws Exception {
        performTest("tags-use-multiple_aliases");
    }

    public void testTagsUseMultiple() throws Exception {
        performTest("tags-use-multiple");
    }

    public void testTagsVerbatimBasic() throws Exception {
        performTest("tags-verbatim-basic");
    }

    public void testTestsArray() throws Exception {
        performTest("tests-array");
    }

    public void testTestsConstant() throws Exception {
        performTest("tests-constant");
    }

    public void testTestsDefined() throws Exception {
        performTest("tests-defined");
    }

    public void testTestsEmpty() throws Exception {
        performTest("tests-empty");
    }

    public void testTestsEven() throws Exception {
        performTest("tests-even");
    }

    public void testTestsIn() throws Exception {
        performTest("tests-in");
    }

    public void testTestsOdd() throws Exception {
        performTest("tests-odd");
    }

    public void testComment() throws Exception {
        performTest("comment");
    }

    public void testMultiInterpolation() throws Exception {
        performTest("multi-interpolation");
    }

    public void testIssue227066_01() throws Exception {
        performTest("issue227066_01");
    }

    public void testIssue227066_02() throws Exception {
        performTest("issue227066_02");
    }

    public void testIssue227066_03() throws Exception {
        performTest("issue227066_03");
    }

    public void testIssue227066_04() throws Exception {
        performTest("issue227066_04");
    }

    public void testIssue227066_05() throws Exception {
        performTest("issue227066_05");
    }

    public void testIssue227066_06() throws Exception {
        performTest("issue227066_06");
    }

    public void testIssue227066_07() throws Exception {
        performTest("issue227066_07");
    }

    public void testIssue227066_08() throws Exception {
        performTest("issue227066_08");
    }

    public void testIssue227066_09() throws Exception {
        performTest("issue227066_09");
    }

    public void testIssue227066_10() throws Exception {
        performTest("issue227066_10");
    }

    public void testIssue243277_01() throws Exception {
        performTest("issue243277_01");
    }

    public void testIssue243277_02() throws Exception {
        performTest("issue243277_02");
    }

    public void testIssue243277_03() throws Exception {
        performTest("issue243277_03");
    }

    public void testIssue243421_01() throws Exception {
        performTest("issue243421_01");
    }

    public void testIssue243421_02() throws Exception {
        performTest("issue243421_02");
    }

    public void testIssue242400_01() throws Exception {
        performTest("issue242400_01");
    }

    public void testIssue242400_02() throws Exception {
        performTest("issue242400_02");
    }

    // #248316
    // variable
    public void testPrecededSingleQuoteByBackslash_01() throws Exception {
        performTest("preceded-single-quote-by-backslash_01");
    }

    public void testPrecededSingleQuoteByBackslash_02() throws Exception {
        performTest("preceded-single-quote-by-backslash_02");
    }

    public void testPrecededSingleQuoteByBackslash_03() throws Exception {
        performTest("preceded-single-quote-by-backslash_03");
    }

    public void testPrecededSingleQuoteByBackslash_04() throws Exception {
        performTest("preceded-single-quote-by-backslash_04");
    }

    // block
    public void testPrecededSingleQuoteByBackslash_05() throws Exception {
        performTest("preceded-single-quote-by-backslash_05");
    }

    public void testPrecededSingleQuoteByBackslash_06() throws Exception {
        performTest("preceded-single-quote-by-backslash_06");
    }

    public void testPrecededSingleQuoteByBackslash_07() throws Exception {
        performTest("preceded-single-quote-by-backslash_07");
    }

    // variable
    public void testPrecededDoubleQuoteByBackslash_01() throws Exception {
        performTest("preceded-double-quote-by-backslash_01");
    }

    public void testPrecededDoubleQuoteByBackslash_02() throws Exception {
        performTest("preceded-double-quote-by-backslash_02");
    }

    public void testPrecededDoubleQuoteByBackslash_03() throws Exception {
        performTest("preceded-double-quote-by-backslash_03");
    }

    public void testPrecededDoubleQuoteByBackslash_04() throws Exception {
        performTest("preceded-double-quote-by-backslash_04");
    }

    // block
    public void testPrecededDoubleQuoteByBackslash_05() throws Exception {
        performTest("preceded-double-quote-by-backslash_05");
    }

    public void testPrecededDoubleQuoteByBackslash_06() throws Exception {
        performTest("preceded-double-quote-by-backslash_06");
    }

    public void testPrecededDoubleQuoteByBackslash_07() throws Exception {
        performTest("preceded-double-quote-by-backslash_07");
    }

    public void testCommentInlineExpression() throws Exception {
        performTest("comment-inline-expression");
    }

    public void testCommentInline() throws Exception {
        performTest("comment-inline");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        String content = TestUtils.getFileContent(new File(getDataDir(), "testfiles/lexer/twig/" + filename + ".twig"));
        Language<TwigTopTokenId> language = TwigTopTokenId.language();
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(content, language);
        return createResult(hierarchy.tokenSequence(language));
    }

    private String createResult(TokenSequence<TwigTopTokenId> ts) throws Exception {
        StringBuilder result = new StringBuilder();
        while (ts.moveNext()) {
            TwigTopTokenId tokenId = ts.token().id();
            if (TwigTopTokenId.T_TWIG_BLOCK.equals(tokenId)) {
                result.append(getEmbeddedBlockResult(ts.token().text()));
            } else if (TwigTopTokenId.T_TWIG_VAR.equals(tokenId)) {
                result.append(getEmbeddedVariableResult(ts.token().text()));
            } else {
                CharSequence text = ts.token().text();
                result.append("TOP token #");
                result.append(ts.index());
                result.append(" ");
                result.append(tokenId.name());
                String token = TestUtils.replaceLinesAndTabs(text.toString());
                if (!token.isEmpty()) {
                    result.append(" ");
                    result.append("[");
                    result.append(token);
                    result.append("]");
                }
                result.append("\n");
            }
        }
        return result.toString();
    }

    private String getEmbeddedBlockResult(final CharSequence text) throws Exception {
        Language<TwigBlockTokenId> language = TwigBlockTokenId.language();
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(text, language);
        return createEmbeddedResult(hierarchy.tokenSequence(language));
    }

    private String getEmbeddedVariableResult(final CharSequence text) throws Exception {
        Language<TwigVariableTokenId> language = TwigVariableTokenId.language();
        TokenHierarchy<?> hierarchy = TokenHierarchy.create(text, language);
        return createEmbeddedResult(hierarchy.tokenSequence(language));
    }

    private String createEmbeddedResult(TokenSequence<? extends TokenId> ts) throws Exception {
        StringBuilder result = new StringBuilder();
        while (ts.moveNext()) {
            TokenId tokenId = ts.token().id();
            CharSequence text = ts.token().text();
            result.append("token #");
            result.append(ts.index());
            result.append(" ");
            result.append(tokenId.name());
            String token = TestUtils.replaceLinesAndTabs(text.toString());
            if (!token.isEmpty()) {
                result.append(" ");
                result.append("[");
                result.append(token);
                result.append("]");
            }
            result.append("\n");
        }
        return result.toString();
    }

}
