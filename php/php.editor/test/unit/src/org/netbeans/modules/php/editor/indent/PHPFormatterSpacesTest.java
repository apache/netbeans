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
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPFormatterSpacesTest extends PHPFormatterTestBase {

    public PHPFormatterSpacesTest(String testName) {
        super(testName);
    }

    public void testSpacesBeforeAfterComma01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, false);
        options.put(FmtOptions.SPACE_AFTER_COMMA, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma01.php", options);
    }

    public void testSpacesBeforeAfterComma02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, false);
        options.put(FmtOptions.SPACE_AFTER_COMMA, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma02.php", options);
    }

    public void testSpacesBeforeAfterComma03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, true);
        options.put(FmtOptions.SPACE_AFTER_COMMA, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma03.php", options);
    }

    public void testSpacesBeforeAfterComma04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, true);
        options.put(FmtOptions.SPACE_AFTER_COMMA, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma04.php", options);
    }

    public void testSpacesBeforeUnaryOps01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps01.php", options);
    }

    public void testSpacesBeforeUnaryOps02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_IF_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps02.php", options);
    }

    public void testSpacesBeforeUnaryOps03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_IF_PARENS, true);
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps03.php", options);
    }

    public void testSpacesBeforeUseStatementPart01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart01.php", options);
    }

    public void testSpacesBeforeUseStatementPart02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart02.php", options);
    }

    public void testSpacesBeforeUseStatementPart03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart03.php", options);
    }

    public void testSpacesBeforeUseStatementPart04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart04.php", options);
    }

    public void testSpacesBeforeUseStatementPart05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart05.php", options);
    }

    public void testSpacesBeforeUseStatementPart06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart06.php", options);
    }

    public void testSpacesBeforeKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords01.php", options);
    }

    public void testSpacesBeforeKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords02.php", options);
    }

    public void testSpacesBeforeKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, false);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords03.php", options);
    }

    public void testSpacesBeforeKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords04.php", options);
    }

    public void testIssue180859_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/issue180859_01.php", options);
    }

    public void testIssue180859_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/issue180859_02.php", options);
    }

    public void testSpaceAfterShortPHPTag_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_SHORT_PHP_TAG, true);
        options.put(FmtOptions.SPACE_BEFORE_CLOSE_PHP_TAG, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterShortPHPTag01.php", options);
    }

    public void testSpaceAfterShortPHPTag_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_SHORT_PHP_TAG, false);
        options.put(FmtOptions.SPACE_BEFORE_CLOSE_PHP_TAG, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterShortPHPTag02.php", options);
    }

    public void testSpacesBeforeAfterSemi01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, false);
        options.put(FmtOptions.SPACE_AFTER_SEMI, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi01.php", options);
    }

    public void testSpacesBeforeAfterSemi02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, false);
        options.put(FmtOptions.SPACE_AFTER_SEMI, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi02.php", options);
    }

    public void testSpacesBeforeAfterSemi03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, true);
        options.put(FmtOptions.SPACE_AFTER_SEMI, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi03.php", options);
    }

    public void testSpacesBeforeAfterSemi04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, true);
        options.put(FmtOptions.SPACE_AFTER_SEMI, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi04.php", options);
    }

    public void testSpacesCheckAfterKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_CHECK_AFTER_KEYWORDS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords01.php", options);
    }

    public void testSpacesCheckAfterKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_CHECK_AFTER_KEYWORDS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords02.php", options);
    }

    public void testSpacesBeforeAnonymousFunctionParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAnonymousFunction01.php", options);
    }

    public void testSpacesBeforeAnonymousFunctionParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ANONYMOUS_FUNCTION_PAREN, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAnonymousFunction02.php", options);
    }

    public void testIssue210617() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, false);
        options.put(FmtOptions.TAB_SIZE, 4);
        reformatFileContents("testfiles/formatting/alignment/issue210617.php", options);
    }

    public void testIssue210617_TabSize8() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, false);
        options.put(FmtOptions.TAB_SIZE, 8);
        options.put(FmtOptions.INDENT_SIZE, 8);
        options.put(FmtOptions.ITEMS_IN_ARRAY_DECLARATION_INDENT_SIZE, 8);
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 8);
        reformatFileContents("testfiles/formatting/alignment/issue210617.php", options, false, true);
    }

    public void testIssue181624_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue181624_01.php", options);
    }

    public void testIssue186183_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue186183_01.php", options);
    }

    public void testIssue187665_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187665_01.php", options);
    }

    public void testIssue187665_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187665_02.php", options);
    }

    public void testIssue187888_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187888_01.php", options);
    }

    public void testIssue187888_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187888_02.php", options);
    }

    public void testIssue187864_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187864_01.php", options);
    }

    public void testIssue188810_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue188810_01.php", options);
    }

    public void testIssue191893_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue191893_01.php", options);
    }

    public void testIssue195562() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/spaces/issue195562.php", options);
    }

    public void testIssue203160() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue203160_01.php", options);
    }

    public void testTraitUsesSpaces_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/TraitUses01.php", options);
    }

    public void testTraitUsesSpaces_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/TraitUses02.php", options);
    }

    public void testIssue202940_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_01.php", options);
    }

    public void testIssue202940_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_02.php", options);
    }

    public void testIssue202940_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_03.php", options);
    }

    public void testIssue202940_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_04.php", options);
    }

    public void testIssue202940_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_05.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace01.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace02.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace03.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace01.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace02.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace03.php", options);
    }

    public void testSpacesBeforeIfElseIfLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfElseIfLeftBrace01.php", options);
    }

    public void testSpacesBeforeElseLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElseLeftBrace01.php", options);
    }

    public void testSpacesBeforeWhileLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileLeftBrace01.php", options);
    }

    public void testSpacesBeforeDoLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeDoLeftBrace01.php", options);
    }

    public void testSpacesBeforeForLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForLeftBrace01.php", options);
    }

    public void testSpacesBeforeSwitchLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchLeftBrace01.php", options);
    }

    public void testSpacesBeforeTryLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeTryLeftBrace01.php", options);
    }

    public void testSpacesBeforeCatchLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchLeftBrace01.php", options);
    }

    public void testSpacesBeforeWhile01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile01.php", options);
    }

    public void testSpacesBeforeWhile02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile02.php", options);
    }

    public void testSpacesBeforeWhile03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile03.php", options);
    }

    public void testSpacesBeforeElse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse01.php", options);
    }

    public void testSpacesBeforeElse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse02.php", options);
    }

    public void testSpacesBeforeElse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse03.php", options);
    }

    public void testSpacesBeforeElse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse04.php", options);
    }

    public void testSpacesBeforeCatch01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch01.php", options);
    }

    public void testSpacesBeforeCatch02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch02.php", options);
    }

    public void testSpacesBeforeMethodCallParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_CALL_PAREN, true);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen01.php", options);
    }

    public void testSpacesBeforeMethodCallParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_CALL_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen02.php", options);
    }

    public void testSpacesBeforeMethodDeclParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen01.php", options);
    }

    public void testSpacesBeforeMethodDeclParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen02.php", options);
    }

    public void testSpacesBeforeIfParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_IF_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen01.php", options);
    }

    public void testSpacesBeforeIfParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_IF_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen02.php", options);
    }

    public void testSpacesBeforeForParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FOR_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen01.php", options);
    }

    public void testSpacesBeforeForParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FOR_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen02.php", options);
    }

    public void testSpacesBeforeWhileParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen01.php", options);
    }

    public void testSpacesBeforeWhileParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen02.php", options);
    }

    public void testSpacesBeforeCatchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen01.php", options);
    }

    public void testSpacesBeforeCatchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen02.php", options);
    }

    public void testSpacesBeforeSwitchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen01.php", options);
    }

    public void testSpacesBeforeSwitchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen02.php", options);
    }

    public void testSpacesAroundStringConcat01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_PAREN, true);
        options.put(FmtOptions.SPACE_AROUND_STRING_CONCAT_OPS, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundStringConcat01.php", options);
    }

    public void testSpacesAroundTernaryOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp01.php", options);
    }

    public void testSpacesAroundTernaryOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp02.php", options);
    }

    public void testSpacesAroundTernaryOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp03.php", options);
    }

    public void testSpacesAroundTernaryOp04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp04.php", options);
    }

    public void testSpacesAroundTernaryOp05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp05.php", options);
    }

    public void testSpacesAroundTernaryOp06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp06.php", options);
    }

    public void testSpacesAroundTernaryOp07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp07.php", options);
    }

    public void testSpacesAroundTernaryOp08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp08.php", options);
    }

    public void testSpacesAroundCoalescingOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_COALESCING_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundCoalescingOp01.php", options);
    }

    public void testSpacesAroundCoalescingOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_COALESCING_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundCoalescingOp02.php", options);
    }

    public void testSpacesAroundCoalescingOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_COALESCING_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundCoalescingOp03.php", options);
    }

    public void testSpacesAroundCoalescingOp04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_COALESCING_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundCoalescingOp04.php", options);
    }

    public void testSpacesAroundKeyValue01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_KEY_VALUE_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp01.php", options);
    }

    public void testSpacesAroundKeyValue02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_KEY_VALUE_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp02.php", options);
    }

    public void testSpacesWithinIfParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_IF_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens01.php", options);
    }

    public void testSpacesWithinForParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_FOR_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens02.php", options);
    }

    public void testSpacesWithinWhileParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_WHILE_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens03.php", options);
    }

    public void testSpacesWithinSwitchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_SWITCH_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens04.php", options);
    }

    public void testSpacesWithinCatchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_CATCH_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens05.php", options);
    }

    public void testSpacesWithinParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens06.php", options);
    }

    public void testSpacesWithinMethodDeclParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens07.php", options);
    }

    public void testSpacesWithinMethodCallParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens08.php", options);
    }

    public void testSpacesWithinMethodDeclParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, true);
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinMethodDecl01.php", options);
    }

    public void testSpacesWithinMethodDeclParens03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, false);
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinMethodDecl02.php", options);
    }

    public void testSpacesWithinTypeCastParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_TYPE_CAST_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinTypeCastParens01.php", options);
    }

    public void testSpacesWithinTypeCastParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_TYPE_CAST_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinTypeCastParens02.php", options);
    }

    public void testSpacesWithinArrayDeclParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens01.php", options);
    }

    public void testSpacesWithinArrayDeclParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens02.php", options);
    }

    public void testSpacesWithinArrayBrackets01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets01.php", options);
    }

    public void testSpacesWithinArrayBrackets02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets02.php", options);
    }

    public void testSpacesWithinArrayBrackets03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets03.php", options);
    }

    public void testSpacesWithinArrayBrackets04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets04.php", options);
    }

    public void testSpacesAfterTypeCast01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_TYPE_CAST, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast01.php", options);
    }

    public void testSpacesAfterTypeCast02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_TYPE_CAST, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast02.php", options);
    }

    public void testIssue228422_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/issue228422_01.php", options);
    }

    public void testIssue228422_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/issue228422_02.php", options);
    }

    public void testIssue230779_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/issue230779_01.php", options);
    }

    public void testIssue230779_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/issue230779_02.php", options);
    }

    public void testIssue231387() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue231387.php", options);
    }

    public void testIssue233050_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, true);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/issue233050_01.php", options);
    }

    public void testIssue233050_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, true);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/issue233050_02.php", options);
    }

    public void testFinally_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FINALLY, true);
        reformatFileContents("testfiles/formatting/spaces/finally_01.php", options);
    }

    public void testFinally_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FINALLY, false);
        reformatFileContents("testfiles/formatting/spaces/finally_02.php", options);
    }

    public void testFinally_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FINALLY_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/finally_03.php", options);
    }

    public void testFinally_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FINALLY_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/finally_04.php", options);
    }

    public void testIssue240274() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/issue240274.php", options);
    }

    public void testGH4635_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/gh4635_01.php", options);
    }

    public void testGH4635_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/gh4635_02.php", options);
    }

    public void testSpacesAroundReturnType01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType01.php", options);
    }

    public void testSpacesAroundReturnType02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType02.php", options);
    }

    public void testSpacesAroundReturnType03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType03.php", options);
    }

    public void testSpacesAroundReturnType04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType04.php", options);
    }

    public void testSpacesAroundReturnType05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType05.php", options);
    }

    public void testSpacesAroundReturnType06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType06.php", options);
    }

    public void testSpacesAroundReturnType07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType07.php", options);
    }

    public void testSpacesAroundReturnType08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType08.php", options);
    }

    public void testSpacesAroundReturnType09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType09.php", options);
    }

    public void testSpacesAroundReturnType10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType10.php", options);
    }

    public void testSpacesAroundReturnType11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType11.php", options);
    }

    public void testSpacesAroundReturnType12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundReturnType12.php", options);
    }

    public void testSpacesBeforeAnonymousClassParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAnonymousClassParen01.php", options);
    }

    public void testSpacesBeforeAnonymousClassParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ANONYMOUS_CLASS_PAREN, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAnonymousClassParen02.php", options);
    }

    public void testSpacesWithinAnonymousClassParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceWithinAnonymousClassParen01.php", options);
    }

    public void testSpacesWithinAnonymousClassParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ANONYMOUS_CLASS_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinAnonymousClassParen02.php", options);
    }

    public void testSpacesWithinAnonymousClassParen03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceWithinAnonymousClassParen03.php", options);
    }

    public void testSpacesWithinAnonymousClassParen04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ANONYMOUS_CLASS_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinAnonymousClassParen04.php", options);
    }

    public void testIssue253093a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, true);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/issue253093a.php", options);
    }

    public void testIssue253093b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, false);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/issue253093b.php", options);
    }

    public void testIssue253093c() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, true);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/issue253093c.php", options);
    }

    public void testIssue253093d() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, false);
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/issue253093d.php", options);
    }

    // PHP7.1
    public void testSpacesAfterNullableTypePrefixForReturn01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn01.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn02.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn03.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn04.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn05.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn06.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn07.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn08.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForReturn09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForReturn09.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter01.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter02.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter03.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter04.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter05.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter06.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter07.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter08.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForParameter09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForParameter09.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed01.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed02.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed03.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed04.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed05.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed06.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed07.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed08.php", options);
    }

    public void testSpacesAfterNullableTypePrefixForMixed09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAfterNullableTypePrefixForMixed09.php", options);
    }

    public void testSpacesAroundMultiCatch01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch01.php", options);
    }

    public void testSpacesAroundMultiCatch02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch02.php", options);
    }

    public void testSpacesAroundMultiCatch03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch03.php", options);
    }

    public void testSpacesAroundMultiCatch04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch04.php", options);
    }

    public void testSpacesAroundMultiCatch05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch05.php", options);
    }

    public void testSpacesAroundMultiCatch06() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch06.php", options);
    }

    public void testSpacesAroundMultiCatch07() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch07.php", options);
    }

    public void testSpacesAroundMultiCatch08() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch08.php", options);
    }

    public void testSpacesAroundMultiCatch09() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch09.php", options);
    }

    public void testSpacesAroundMultiCatch10() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundMultiCatch10.php", options);
    }

    // PHP 8.0
    public void testSpacesAroundNonCapturingCatches01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundNonCapturingCatches01.php", options);
    }

    public void testSpacesAroundDeclareEqual01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual01a.php", options);
    }

    public void testSpacesAroundDeclareEqual02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual02a.php", options);
    }

    public void testSpacesAroundDeclareEqual03a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual03a.php", options);
    }

    public void testSpacesAroundDeclareEqual04a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual04a.php", options);
    }

    public void testSpacesAroundDeclareEqual01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual01b.php", options);
    }

    public void testSpacesAroundDeclareEqual02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual02b.php", options);
    }

    public void testSpacesAroundDeclareEqual03b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual03b.php", options);
    }

    public void testSpacesAroundDeclareEqual04b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_DECLARE_EQUAL, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundDeclareEqual04b.php", options);
    }

    public void testIssue268541_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue268541_01.php", options);
    }

    public void testIssue268541_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue268541_02.php", options);
    }

    public void testIssue268541_03() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue268541_03.php", options);
    }

    public void testIssue268541_04() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue268541_04.php", options);
    }

    public void testIssue268541_05() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue268541_05.php", options);
    }

    // PHP 7.4
    public void testSpacesBeforeArrowFunctionDeclLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_PAREN, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeArrowFunctionParen01.php", options);
    }

    // NETBEANS-2971
    public void testSpacesWithinLambdaFunctionDeclParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/netbeans2971_01.php", options);
    }

    public void testSpacesWithinLambdaFunctionDeclParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/netbeans2971_02.php", options);
    }

    // NETBEANS-2994
    public void testSpacesAroundUnaryOperator_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/netbeans2994_01.php", options);
    }

    public void testSpacesAroundUnaryOperator_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/netbeans2994_02.php", options);
    }

    // NETBEANS-2149
    public void testSpacesAroundBinaryOperatorsOnly() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, true);
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/netbeans2149_01.php", options);
    }

    public void testSpacesAroundAssignmentOperatorsOnly() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, false);
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/netbeans2149_02.php", options);
    }

    public void testSpacesAroundAssignmentAndBinaryOperators() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, true);
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/netbeans2149_03.php", options);
    }

    public void testSpacesWithoutAroundAssignmentAndBinaryOperators() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, false);
        options.put(FmtOptions.SPACE_AROUND_ASSIGN_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/netbeans2149_04.php", options);
    }

    // NETBEANS-4443 PHP 8.0
    public void testSpacesBeforeMatchParen_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_MATCH_PAREN, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeMatchParen_01.php", options);
    }

    public void testSpacesBeforeMatchParen_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_MATCH_PAREN, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeMatchParen_02.php", options);
    }

    public void testSpacesWithinMatchParen_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_MATCH_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinMatchParen_01.php", options);
    }

    public void testSpacesWithinMatchParen_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_MATCH_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinMatchParen_02.php", options);
    }

    public void testSpacesBeforeMatchLeftBrace_01() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_MATCH_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeMatchLeftBrace_01.php", options);
    }

    public void testSpacesBeforeMatchLeftBrace_02() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_MATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeMatchLeftBrace_02.php", options);
    }

    public void testSpacesAroundUnionTypeSeparator_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNION_TYPE_SEPARATOR, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundUnionTypeSeparator_01a.php", options);
    }

    public void testSpacesAroundUnionTypeSeparator_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNION_TYPE_SEPARATOR, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundUnionTypeSeparator_01b.php", options);
    }

    public void testSpacesAroundUnionTypeSeparator_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNION_TYPE_SEPARATOR, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundUnionTypeSeparator_02a.php", options);
    }

    public void testSpacesAroundUnionTypeSeparator_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNION_TYPE_SEPARATOR, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundUnionTypeSeparator_02b.php", options);
    }

    public void testSpacesAroundUnionTypeSeparator_03a() throws Exception {
        // static return type
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNION_TYPE_SEPARATOR, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundUnionTypeSeparator_03a.php", options);
    }

    public void testSpacesAroundUnionTypeSeparator_03b() throws Exception {
        // static return type
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNION_TYPE_SEPARATOR, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundUnionTypeSeparator_03b.php", options);
    }

    public void testSpacesAroundNullsafeOperator_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_NULLSAFE_OBJECT_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundNullsafeOperator_01a.php", options);
    }

    public void testSpacesAroundNullsafeOperator_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_NULLSAFE_OBJECT_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceAroundNullsafeOperator_01b.php", options);
    }

    public void testSpacesBeforeAttributeDeclParens_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ATTRIBUTE_DECL_PAREN, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeAttributeDeclParen_01a.php", options);
    }

    public void testSpacesBeforeAttributeDeclParens_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ATTRIBUTE_DECL_PAREN, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeAttributeDeclParen_01b.php", options);
    }

    public void testSpacesBeforeAttributeDeclParens_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ATTRIBUTE_DECL_PAREN, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeAttributeDeclParen_02a.php", options);
    }

    public void testSpacesBeforeAttributeDeclParens_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ATTRIBUTE_DECL_PAREN, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceBeforeAttributeDeclParen_02b.php", options);
    }

    public void testSpacesWithinAttributeBrackets_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeBrackets_01a.php", options);
    }

    public void testSpacesWithinAttributeBrackets_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_BRACKETS, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeBrackets_01b.php", options);
    }

    public void testSpacesWithinAttributeBrackets_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeBrackets_02a.php", options);
    }

    public void testSpacesWithinAttributeBrackets_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_BRACKETS, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeBrackets_02b.php", options);
    }

    public void testSpacesWithinAttributeDeclParens_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeDeclParens_01a.php", options);
    }

    public void testSpacesWithinAttributeDeclParens_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_DECL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeDeclParens_01b.php", options);
    }

    public void testSpacesWithinAttributeDeclParens_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeDeclParens_02a.php", options);
    }

    public void testSpacesWithinAttributeDeclParens_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ATTRIBUTE_DECL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/php80/spaceWithinAttributeDeclParens_02b.php", options);
    }

    public void testSpacesAroundIntersectionTypeSeparator_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR, true);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceAroundPureIntersectionTypeSeparator_01a.php", options);
    }

    public void testSpacesAroundIntersectionTypeSeparator_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR, false);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceAroundPureIntersectionTypeSeparator_01b.php", options);
    }


    public void testSpacesAroundIntersectionTypeSeparator_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR, true);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceAroundPureIntersectionTypeSeparator_02a.php", options);
    }

    public void testSpacesAroundIntersectionTypeSeparator_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR, false);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceAroundPureIntersectionTypeSeparator_02b.php", options);
    }

    public void testSpacesBeforeEnumDecLeftBrace_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceBeforeEnumDecLeftBrace_01a.php", options);
    }

    public void testSpacesBeforeEnumDecLeftBrace_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceBeforeEnumDecLeftBrace_01b.php", options);
    }

    public void testSpacesWithinMethodCallParensWithFirstClassCallable_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceWithinMethodCallParensWithFirstClassCallable_01a.php", options);
    }

    public void testSpacesWithinMethodCallParensWithFirstClassCallable_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceWithinMethodCallParensWithFirstClassCallable_01b.php", options);
    }

    public void testSpacesWithinMethodCallParensWithFirstClassCallable_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceWithinMethodCallParensWithFirstClassCallable_02a.php", options);
    }

    public void testSpacesWithinMethodCallParensWithFirstClassCallable_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/php81/spaceWithinMethodCallParensWithFirstClassCallable_02b.php", options);
    }

    public void testGH5380_01a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/gh5380_01.php", options,  false, true);
    }

    public void testGH5380_01b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/gh5380_01.php", options,  false, true);
    }

    public void testGH5380_02a() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/gh5380_02.php", options,  false, true);
    }

    public void testGH5380_02b() throws Exception {
        HashMap<String, Object> options = new HashMap<>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/gh5380_02.php", options,  false, true);
    }
}
