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

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import static org.netbeans.modules.php.editor.indent.FmtOptions.*;

/**
 *  XXX make sure the getters get the defaults from somewhere
 *  XXX add support for profiles
 *  XXX get the preferences node from somewhere else in odrer to be able not to
 *      use the getters and to be able to write to it.
 *
 * @author Dusan Balek
 * @author Petr Pisl
 */
public final class CodeStyle {

    static {
        FmtOptions.codeStyleProducer = new Producer();
    }

    private Preferences preferences;

    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

    /** For testing purposes only. */
    public static CodeStyle get(Preferences prefs) {
        return new CodeStyle(prefs);
    }

    public static CodeStyle get(Document doc) {
        return new CodeStyle(CodeStylePreferences.get(doc).getPreferences());
    }

    // General tabs and indents ------------------------------------------------

    public boolean expandTabToSpaces() {
        return preferences.getBoolean(EXPAND_TAB_TO_SPACES,  getDefaultAsBoolean(EXPAND_TAB_TO_SPACES));
    }

    public int getTabSize() {
        return preferences.getInt(TAB_SIZE, getDefaultAsInt(TAB_SIZE));
    }

    public int getIndentSize() {
        return preferences.getInt(INDENT_SIZE, getDefaultAsInt(INDENT_SIZE));
    }

    public int getContinuationIndentSize() {
        return preferences.getInt(CONTINUATION_INDENT_SIZE, getDefaultAsInt(CONTINUATION_INDENT_SIZE));
    }

    public int getItemsInArrayDeclarationIndentSize() {
        return preferences.getInt(ITEMS_IN_ARRAY_DECLARATION_INDENT_SIZE, getDefaultAsInt(ITEMS_IN_ARRAY_DECLARATION_INDENT_SIZE));
    }

    public int getInitialIndent() {
        return preferences.getInt(INITIAL_INDENT, getDefaultAsInt(INITIAL_INDENT));
    }

    public boolean reformatComments() {
        return preferences.getBoolean(REFORMAT_COMMENTS, getDefaultAsBoolean(REFORMAT_COMMENTS));
    }

    public boolean indentHtml() {
        return preferences.getBoolean(INDENT_HTML, getDefaultAsBoolean(INDENT_HTML));
    }

    public int getRightMargin() {
        return preferences.getInt(RIGHT_MARGIN, getDefaultAsInt(RIGHT_MARGIN));
    }

    // Brace placement --------------------------------------------------------

    public BracePlacement getClassDeclBracePlacement() {
        String placement = preferences.get(CLASS_DECL_BRACE_PLACEMENT, getDefaultAsString(CLASS_DECL_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getAnonymousClassBracePlacement() {
        String placement = preferences.get(ANONYMOUS_CLASS_BRACE_PLACEMENT, getDefaultAsString(ANONYMOUS_CLASS_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getMethodDeclBracePlacement() {
        String placement = preferences.get(METHOD_DECL_BRACE_PLACEMENT, getDefaultAsString(METHOD_DECL_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getIfBracePlacement() {
        String placement = preferences.get(IF_BRACE_PLACEMENT, getDefaultAsString(IF_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getForBracePlacement() {
        String placement = preferences.get(FOR_BRACE_PLACEMENT, getDefaultAsString(FOR_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getWhileBracePlacement() {
        String placement = preferences.get(WHILE_BRACE_PLACEMENT, getDefaultAsString(WHILE_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getSwitchBracePlacement() {
        String placement = preferences.get(SWITCH_BRACE_PLACEMENT, getDefaultAsString(SWITCH_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getCatchBracePlacement() {
        String placement = preferences.get(CATCH_BRACE_PLACEMENT, getDefaultAsString(CATCH_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getMatchBracePlacement() {
        String placement = preferences.get(MATCH_BRACE_PLACEMENT, getDefaultAsString(MATCH_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getUseTraitBodyBracePlacement() {
        String placement = preferences.get(USE_TRAIT_BODY_BRACE_PLACEMENT, getDefaultAsString(USE_TRAIT_BODY_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getGroupUseBracePlacement() {
        String placement = preferences.get(GROUP_USE_BRACE_PLACEMENT, getDefaultAsString(GROUP_USE_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getOtherBracePlacement() {
        String placement = preferences.get(OTHER_BRACE_PLACEMENT, getDefaultAsString(OTHER_BRACE_PLACEMENT));
        return BracePlacement.valueOf(placement);
    }

    // Blank lines -------------------------------------------------------------

    public int getBlankLinesBeforeNamespace() {
        return preferences.getInt(BLANK_LINES_BEFORE_NAMESPACE, getDefaultAsInt(BLANK_LINES_BEFORE_NAMESPACE));
    }

    public int getBlankLinesAfterNamespace() {
        return preferences.getInt(BLANK_LINES_AFTER_NAMESPACE, getDefaultAsInt(BLANK_LINES_AFTER_NAMESPACE));
    }

    public int getBlankLinesBeforeUse() {
        return preferences.getInt(BLANK_LINES_BEFORE_USE, getDefaultAsInt(BLANK_LINES_BEFORE_USE));
    }

    public int getBlankLinesBeforeUseTrait() {
        return preferences.getInt(BLANK_LINES_BEFORE_USE_TRAIT, getDefaultAsInt(BLANK_LINES_BEFORE_USE_TRAIT));
    }

    public int getBlankLinesAfterUseTrait() {
        return preferences.getInt(BLANK_LINES_AFTER_USE_TRAIT, getDefaultAsInt(BLANK_LINES_AFTER_USE_TRAIT));
    }

    public int getBlankLinesAfterUse() {
        return preferences.getInt(BLANK_LINES_AFTER_USE, getDefaultAsInt(BLANK_LINES_AFTER_USE));
    }

    public int getBlankLinesBetweenUseTypes() {
        return preferences.getInt(BLANK_LINES_BETWEEN_USE_TYPES, getDefaultAsInt(BLANK_LINES_BETWEEN_USE_TYPES));
    }

    public int getBlankLinesBeforeClass() {
        return preferences.getInt(BLANK_LINES_BEFORE_CLASS, getDefaultAsInt(BLANK_LINES_BEFORE_CLASS));
    }

    public int getBlankLinesAfterClass() {
        return preferences.getInt(BLANK_LINES_AFTER_CLASS, getDefaultAsInt(BLANK_LINES_AFTER_CLASS));
    }

    public int getBlankLinesAfterClassHeader() {
        return preferences.getInt(BLANK_LINES_AFTER_CLASS_HEADER, getDefaultAsInt(BLANK_LINES_AFTER_CLASS_HEADER));
    }

    public int getBlankLinesBeforeClassEnd() {
        return preferences.getInt(BLANK_LINES_BEFORE_CLASS_END, getDefaultAsInt(BLANK_LINES_BEFORE_CLASS_END));
    }

    public int getBlankLinesBeforeFields() {
        return preferences.getInt(BLANK_LINES_BEFORE_FIELDS, getDefaultAsInt(BLANK_LINES_BEFORE_FIELDS));
    }

    public int getBlankLinesBetweenFields() {
        return preferences.getInt(BLANK_LINES_BETWEEN_FIELDS, getDefaultAsInt(BLANK_LINES_BETWEEN_FIELDS));
    }

    public int getBlankLinesAfterFields() {
        return preferences.getInt(BLANK_LINES_AFTER_FIELDS, getDefaultAsInt(BLANK_LINES_AFTER_FIELDS));
    }

    public boolean getBlankLinesEOF() {
        return preferences.getBoolean(BLANK_LINES_EOF, getDefaultAsBoolean(BLANK_LINES_EOF));
    }

    /**
     *
     * @return true it the fields will be group without php doc together (no empty line between them)
     */
    public boolean getBlankLinesGroupFieldsWithoutDoc() {
        return preferences.getBoolean(BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, getDefaultAsBoolean(BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES));
    }

    public int getBlankLinesBeforeFunction() {
        return preferences.getInt(BLANK_LINES_BEFORE_FUNCTION, getDefaultAsInt(BLANK_LINES_BEFORE_FUNCTION));
    }

    public int getBlankLinesAfterFunction() {
        return preferences.getInt(BLANK_LINES_AFTER_FUNCTION, getDefaultAsInt(BLANK_LINES_AFTER_FUNCTION));
    }

    public int getBlankLinesBeforeFunctionEnd() {
        return preferences.getInt(BLANK_LINES_BEFORE_FUNCTION_END, getDefaultAsInt(BLANK_LINES_BEFORE_FUNCTION_END));
    }

    public int getBlankLinesAfterOpenPHPTag() {
        return preferences.getInt(BLANK_LINES_AFTER_OPEN_PHP_TAG, getDefaultAsInt(BLANK_LINES_AFTER_OPEN_PHP_TAG));
    }

    public int getBlankLinesAfterOpenPHPTagInHTML() {
        return preferences.getInt(BLANK_LINES_AFTER_OPEN_PHP_TAG_IN_HTML, getDefaultAsInt(BLANK_LINES_AFTER_OPEN_PHP_TAG_IN_HTML));
    }

    public int getBlankLinesBeforeClosePHPTag() {
        return preferences.getInt(BLANK_LINES_BEFORE_CLOSE_PHP_TAG, getDefaultAsInt(BLANK_LINES_BEFORE_CLOSE_PHP_TAG));
    }

    public int getBlankLinesMaxPreserved() {
        return preferences.getInt(BLANK_LINES_MAX_PRESERVED, getDefaultAsInt(BLANK_LINES_MAX_PRESERVED));
    }

    // Spaces ------------------------------------------------------------------

    public boolean spaceBeforeWhile() {
        return preferences.getBoolean(SPACE_BEFORE_WHILE, getDefaultAsBoolean(SPACE_BEFORE_WHILE));
    }

    public boolean spaceBeforeElse() {
        return preferences.getBoolean(SPACE_BEFORE_ELSE, getDefaultAsBoolean(SPACE_BEFORE_ELSE));
    }

    public boolean spaceBeforeCatch() {
        return preferences.getBoolean(SPACE_BEFORE_CATCH, getDefaultAsBoolean(SPACE_BEFORE_CATCH));
    }

    public boolean spaceBeforeFinally() {
        return preferences.getBoolean(SPACE_BEFORE_FINALLY, getDefaultAsBoolean(SPACE_BEFORE_FINALLY));
    }

    public boolean spaceBeforeAnonymousClassParen() {
        return preferences.getBoolean(SPACE_BEFORE_ANONYMOUS_CLASS_PAREN, getDefaultAsBoolean(SPACE_BEFORE_ANONYMOUS_CLASS_PAREN));
    }

    public boolean spaceBeforeAnonymousFunctionParen() {
        return preferences.getBoolean(SPACE_BEFORE_ANONYMOUS_FUNCTION_PAREN, getDefaultAsBoolean(SPACE_BEFORE_ANONYMOUS_FUNCTION_PAREN));
    }

    public boolean spaceBeforeMethodDeclParen() {
        return preferences.getBoolean(SPACE_BEFORE_METHOD_DECL_PAREN, getDefaultAsBoolean(SPACE_BEFORE_METHOD_DECL_PAREN));
    }

    public boolean spaceBeforeMethodCallParen() {
        return preferences.getBoolean(SPACE_BEFORE_METHOD_CALL_PAREN, getDefaultAsBoolean(SPACE_BEFORE_METHOD_CALL_PAREN));
    }

    public boolean spaceBeforeAttributeDeclParen() {
        return preferences.getBoolean(SPACE_BEFORE_ATTRIBUTE_DECL_PAREN, getDefaultAsBoolean(SPACE_BEFORE_ATTRIBUTE_DECL_PAREN));
    }

    public boolean spaceBeforeIfParen() {
        return preferences.getBoolean(SPACE_BEFORE_IF_PAREN, getDefaultAsBoolean(SPACE_BEFORE_IF_PAREN));
    }

    public boolean spaceBeforeForParen() {
        return preferences.getBoolean(SPACE_BEFORE_FOR_PAREN, getDefaultAsBoolean(SPACE_BEFORE_FOR_PAREN));
    }

    public boolean spaceBeforeWhileParen() {
        return preferences.getBoolean(SPACE_BEFORE_WHILE_PAREN, getDefaultAsBoolean(SPACE_BEFORE_WHILE_PAREN));
    }

    public boolean spaceBeforeCatchParen() {
        return preferences.getBoolean(SPACE_BEFORE_CATCH_PAREN, getDefaultAsBoolean(SPACE_BEFORE_CATCH_PAREN));
    }

    public boolean spaceBeforeSwitchParen() {
        return preferences.getBoolean(SPACE_BEFORE_SWITCH_PAREN, getDefaultAsBoolean(SPACE_BEFORE_SWITCH_PAREN));
    }

    public boolean spaceBeforeMatchParen() {
        return preferences.getBoolean(SPACE_BEFORE_MATCH_PAREN, getDefaultAsBoolean(SPACE_BEFORE_MATCH_PAREN));
    }

    public boolean spaceBeforeArrayDeclParen() {
        return preferences.getBoolean(SPACE_BEFORE_ARRAY_DECL_PAREN, getDefaultAsBoolean(SPACE_BEFORE_ARRAY_DECL_PAREN));
    }

    public boolean spaceAroundUnaryOps() {
        return preferences.getBoolean(SPACE_AROUND_UNARY_OPS, getDefaultAsBoolean(SPACE_AROUND_UNARY_OPS));
    }

    public boolean spaceAroundBinaryOps() {
        return preferences.getBoolean(SPACE_AROUND_BINARY_OPS, getDefaultAsBoolean(SPACE_AROUND_BINARY_OPS));
    }

    public boolean spaceAroundStringConcatOps() {
        return preferences.getBoolean(SPACE_AROUND_STRING_CONCAT_OPS, getDefaultAsBoolean(SPACE_AROUND_STRING_CONCAT_OPS));
    }

    public boolean spaceAroundTernaryOps() {
        return preferences.getBoolean(SPACE_AROUND_TERNARY_OPS, getDefaultAsBoolean(SPACE_AROUND_TERNARY_OPS));
    }

    public boolean spaceAroundCoalescingOps() {
        return preferences.getBoolean(SPACE_AROUND_COALESCING_OPS, getDefaultAsBoolean(SPACE_AROUND_COALESCING_OPS));
    }

    public boolean spaceAroundKeyValueOps() {
        return preferences.getBoolean(SPACE_AROUND_KEY_VALUE_OPS, getDefaultAsBoolean(SPACE_AROUND_KEY_VALUE_OPS));
    }

    public boolean spaceAroundAssignOps() {
        return preferences.getBoolean(SPACE_AROUND_ASSIGN_OPS, getDefaultAsBoolean(SPACE_AROUND_ASSIGN_OPS));
    }

    public boolean spaceAroundScopeResolutionOps() {
        return preferences.getBoolean(SPACE_AROUND_SCOPE_RESOLUTION_OPS, getDefaultAsBoolean(SPACE_AROUND_SCOPE_RESOLUTION_OPS));
    }

    public boolean spaceAroundObjectOps() {
        return preferences.getBoolean(SPACE_AROUND_OBJECT_OPS, getDefaultAsBoolean(SPACE_AROUND_OBJECT_OPS));
    }

    public boolean spaceAroundNullsafeObjectOps() {
        return preferences.getBoolean(SPACE_AROUND_NULLSAFE_OBJECT_OPS, getDefaultAsBoolean(SPACE_AROUND_NULLSAFE_OBJECT_OPS));
    }

    public boolean spaceAroundDeclareEqual() {
        return preferences.getBoolean(SPACE_AROUND_DECLARE_EQUAL, getDefaultAsBoolean(SPACE_AROUND_DECLARE_EQUAL));
    }

    public boolean spaceAroundUnionTypeSeparator() {
        return preferences.getBoolean(SPACE_AROUND_UNION_TYPE_SEPARATOR, getDefaultAsBoolean(SPACE_AROUND_UNION_TYPE_SEPARATOR));
    }

    public boolean spaceAroundIntersectionTypeSeparator() {
        return preferences.getBoolean(SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR, getDefaultAsBoolean(SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR));
    }

    public boolean spaceBeforeClassDeclLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_CLASS_DECL_LEFT_BRACE));
    }

    public boolean spaceBeforeAnonymousClassLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_ANONYMOUS_CLASS_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_ANONYMOUS_CLASS_LEFT_BRACE));
    }

    public boolean spaceBeforeMethodDeclLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_METHOD_DECL_LEFT_BRACE));
    }

    public boolean spaceBeforeIfLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_IF_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_IF_LEFT_BRACE));
    }

    public boolean spaceBeforeElseLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_ELSE_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_ELSE_LEFT_BRACE));
    }

    public boolean spaceBeforeWhileLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_WHILE_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_WHILE_LEFT_BRACE));
    }

    public boolean spaceBeforeForLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_FOR_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_FOR_LEFT_BRACE));
    }

    public boolean spaceBeforeDoLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_DO_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_DO_LEFT_BRACE));
    }

    public boolean spaceBeforeSwitchLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_SWITCH_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_SWITCH_LEFT_BRACE));
    }

    public boolean spaceBeforeMatchLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_MATCH_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_MATCH_LEFT_BRACE));
    }

    public boolean spaceBeforeTryLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_TRY_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_TRY_LEFT_BRACE));
    }

    public boolean spaceBeforeCatchLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_CATCH_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_CATCH_LEFT_BRACE));
    }

    public boolean spaceBeforeFinallyLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_FINALLY_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_FINALLY_LEFT_BRACE));
    }

    public boolean spaceBeforeUseTraitBodyLeftBrace() {
        return preferences.getBoolean(SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE, getDefaultAsBoolean(SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE));
    }

    public boolean spaceWithinAnonymousClassParens() {
        return preferences.getBoolean(SPACE_WITHIN_ANONYMOUS_CLASS_PARENS, getDefaultAsBoolean(SPACE_WITHIN_ANONYMOUS_CLASS_PARENS));
    }

    public boolean spaceWithinMethodDeclParens() {
        return preferences.getBoolean(SPACE_WITHIN_METHOD_DECL_PARENS, getDefaultAsBoolean(SPACE_WITHIN_METHOD_DECL_PARENS));
    }

    public boolean spaceWithinMethodCallParens() {
        return preferences.getBoolean(SPACE_WITHIN_METHOD_CALL_PARENS, getDefaultAsBoolean(SPACE_WITHIN_METHOD_CALL_PARENS));
    }

    public boolean spaceWithinIfParens() {
        return preferences.getBoolean(SPACE_WITHIN_IF_PARENS, getDefaultAsBoolean(SPACE_WITHIN_IF_PARENS));
    }

    public boolean spaceWithinForParens() {
        return preferences.getBoolean(SPACE_WITHIN_FOR_PARENS, getDefaultAsBoolean(SPACE_WITHIN_FOR_PARENS));
    }

    public boolean spaceWithinWhileParens() {
        return preferences.getBoolean(SPACE_WITHIN_WHILE_PARENS, getDefaultAsBoolean(SPACE_WITHIN_WHILE_PARENS));
    }

    public boolean spaceWithinSwitchParens() {
        return preferences.getBoolean(SPACE_WITHIN_SWITCH_PARENS, getDefaultAsBoolean(SPACE_WITHIN_SWITCH_PARENS));
    }

    public boolean spaceWithinMatchParens() {
        return preferences.getBoolean(SPACE_WITHIN_MATCH_PARENS, getDefaultAsBoolean(SPACE_WITHIN_MATCH_PARENS));
    }

    public boolean spaceWithinCatchParens() {
        return preferences.getBoolean(SPACE_WITHIN_CATCH_PARENS, getDefaultAsBoolean(SPACE_WITHIN_CATCH_PARENS));
    }

    public boolean spaceWithinTypeCastParens() {
        return preferences.getBoolean(SPACE_WITHIN_TYPE_CAST_PARENS, getDefaultAsBoolean(SPACE_WITHIN_TYPE_CAST_PARENS));
    }

    public boolean spaceWithinArrayDeclParens() {
        return preferences.getBoolean(SPACE_WITHIN_ARRAY_DECL_PARENS, getDefaultAsBoolean(SPACE_WITHIN_ARRAY_DECL_PARENS));
    }

    public boolean spaceWithinArrayBrackets() {
        return preferences.getBoolean(SPACE_WITHIN_ARRAY_BRACKETS, getDefaultAsBoolean(SPACE_WITHIN_ARRAY_BRACKETS));
    }

    public boolean spaceWithinAttributeBrackets() {
        return preferences.getBoolean(SPACE_WITHIN_ATTRIBUTE_BRACKETS, getDefaultAsBoolean(SPACE_WITHIN_ATTRIBUTE_BRACKETS));
    }

    public boolean spaceWithinAttributeDeclParens() {
        return preferences.getBoolean(SPACE_WITHIN_ATTRIBUTE_DECL_PARENS, getDefaultAsBoolean(SPACE_WITHIN_ATTRIBUTE_DECL_PARENS));
    }

    public boolean spaceBeforeComma() {
        return preferences.getBoolean(SPACE_BEFORE_COMMA, getDefaultAsBoolean(SPACE_BEFORE_COMMA));
    }

    public boolean spaceAfterComma() {
        return preferences.getBoolean(SPACE_AFTER_COMMA, getDefaultAsBoolean(SPACE_AFTER_COMMA));
    }

    public boolean spaceBeforeSemi() {
        return preferences.getBoolean(SPACE_BEFORE_SEMI, getDefaultAsBoolean(SPACE_BEFORE_SEMI));
    }

    public boolean spaceAfterSemi() {
        return preferences.getBoolean(SPACE_AFTER_SEMI, getDefaultAsBoolean(SPACE_AFTER_SEMI));
    }

    public boolean spaceAfterTypeCast() {
        return preferences.getBoolean(SPACE_AFTER_TYPE_CAST, getDefaultAsBoolean(SPACE_AFTER_TYPE_CAST));
    }

    public boolean spaceCheckAfterKeywords() {
        return preferences.getBoolean(SPACE_CHECK_AFTER_KEYWORDS, getDefaultAsBoolean(SPACE_CHECK_AFTER_KEYWORDS));
    }

    public boolean spaceAfterShortPHPTag() {
        return preferences.getBoolean(SPACE_AFTER_SHORT_PHP_TAG, getDefaultAsBoolean(SPACE_AFTER_SHORT_PHP_TAG));
    }

    public boolean spaceBeforeClosePHPTag() {
        return preferences.getBoolean(SPACE_BEFORE_CLOSE_PHP_TAG, getDefaultAsBoolean(SPACE_BEFORE_CLOSE_PHP_TAG));
    }

    public boolean spaceBetweenOpenPHPTagAndNamespace() {
        return preferences.getBoolean(SPACE_BETWEEN_OPEN_PHP_TAG_AND_NAMESPACE, getDefaultAsBoolean(SPACE_BETWEEN_OPEN_PHP_TAG_AND_NAMESPACE));
    }

    // alignment
    public boolean alignMultilineMethodParams() {
        return preferences.getBoolean(ALIGN_MULTILINE_METHOD_PARAMS, getDefaultAsBoolean(ALIGN_MULTILINE_METHOD_PARAMS));
    }

    public boolean alignMultilineCallArgs() {
        return preferences.getBoolean(ALIGN_MULTILINE_CALL_ARGS, getDefaultAsBoolean(ALIGN_MULTILINE_CALL_ARGS));
    }

    public boolean alignMultilineImplements() {
        return preferences.getBoolean(ALIGN_MULTILINE_IMPLEMENTS, getDefaultAsBoolean(ALIGN_MULTILINE_IMPLEMENTS));
    }

    public boolean alignMultilineParenthesized() {
        return preferences.getBoolean(ALIGN_MULTILINE_PARENTHESIZED, getDefaultAsBoolean(ALIGN_MULTILINE_PARENTHESIZED));
    }

    public boolean alignMultilineBinaryOp() {
        return preferences.getBoolean(ALIGN_MULTILINE_BINARY_OP, getDefaultAsBoolean(ALIGN_MULTILINE_BINARY_OP));
    }

    public boolean alignMultilineTernaryOp() {
        return preferences.getBoolean(ALIGN_MULTILINE_TERNARY_OP, getDefaultAsBoolean(ALIGN_MULTILINE_TERNARY_OP));
    }

    public boolean alignMultilineAssignment() {
        return preferences.getBoolean(ALIGN_MULTILINE_ASSIGNMENT, getDefaultAsBoolean(ALIGN_MULTILINE_ASSIGNMENT));
    }

    public boolean alignMultilineFor() {
        return preferences.getBoolean(ALIGN_MULTILINE_FOR, getDefaultAsBoolean(ALIGN_MULTILINE_FOR));
    }

    public boolean alignMultilineArrayInit() {
        //return preferences.getBoolean(alignMultilineArrayInit, getDefaultAsBoolean(alignMultilineArrayInit));
        return false;
    }

    public boolean placeElseOnNewLine() {
        return preferences.getBoolean(PLACE_ELSE_ON_NEW_LINE, getDefaultAsBoolean(PLACE_ELSE_ON_NEW_LINE));
    }

    public boolean placeWhileOnNewLine() {
        return preferences.getBoolean(PLACE_WHILE_ON_NEW_LINE, getDefaultAsBoolean(PLACE_WHILE_ON_NEW_LINE));
    }

    public boolean placeCatchOnNewLine() {
        return preferences.getBoolean(PLACE_CATCH_ON_NEW_LINE, getDefaultAsBoolean(PLACE_CATCH_ON_NEW_LINE));
    }

    public boolean placeFinallyOnNewLine() {
        return preferences.getBoolean(PLACE_FINALLY_ON_NEW_LINE, getDefaultAsBoolean(PLACE_FINALLY_ON_NEW_LINE));
    }

    public boolean placeNewLineAfterModifiers() {
        return preferences.getBoolean(PLACE_NEW_LINE_AFTER_MODIFIERS, getDefaultAsBoolean(PLACE_NEW_LINE_AFTER_MODIFIERS));
    }

    public boolean groupMultilineAssignment() {
        return preferences.getBoolean(GROUP_ALIGNMENT_ASSIGNMENT, getDefaultAsBoolean(GROUP_ALIGNMENT_ASSIGNMENT));
    }

    public boolean groupMultilineArrayInit() {
        return preferences.getBoolean(GROUP_ALIGNMENT_ARRAY_INIT, getDefaultAsBoolean(GROUP_ALIGNMENT_ARRAY_INIT));
    }

    public boolean groupMultilineMatchArmArrow() {
        return preferences.getBoolean(GROUP_ALIGNMENT_MATCH_ARM_ARROW, getDefaultAsBoolean(GROUP_ALIGNMENT_MATCH_ARM_ARROW));
    }

    // Wrapping ----------------------------------------------------------------

    public WrapStyle wrapGroupUseList() {
        String wrap = preferences.get(WRAP_GROUP_USE_LIST, getDefaultAsString(WRAP_GROUP_USE_LIST));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapExtendsImplementsKeyword() {
        String wrap = preferences.get(WRAP_EXTENDS_IMPLEMENTS_KEYWORD, getDefaultAsString(WRAP_EXTENDS_IMPLEMENTS_KEYWORD));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapExtendsImplementsList() {
        String wrap = preferences.get(WRAP_EXTENDS_IMPLEMENTS_LIST, getDefaultAsString(WRAP_EXTENDS_IMPLEMENTS_LIST));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapMethodParams() {
        String wrap = preferences.get(WRAP_METHOD_PARAMS, getDefaultAsString(WRAP_METHOD_PARAMS));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapMethodParamsAfterLeftParen() {
        return preferences.getBoolean(
                WRAP_METHOD_PARAMS_AFTER_LEFT_PAREN,
                getDefaultAsBoolean(WRAP_METHOD_PARAMS_AFTER_LEFT_PAREN)
        );
    }

    public boolean wrapMethodParamsRightParen() {
        return preferences.getBoolean(
                WRAP_METHOD_PARAMS_RIGHT_PAREN,
                getDefaultAsBoolean(WRAP_METHOD_PARAMS_RIGHT_PAREN)
        );
    }

    public boolean wrapMethodParamsKeepParenAndBraceOnTheSameLine() {
        return preferences.getBoolean(
                WRAP_METHOD_PARAMS_KEEP_PAREN_AND_BRACE_ON_THE_SAME_LINE,
                getDefaultAsBoolean(WRAP_METHOD_PARAMS_KEEP_PAREN_AND_BRACE_ON_THE_SAME_LINE)
        );
    }

    public WrapStyle wrapMethodCallArgs() {
        String wrap = preferences.get(WRAP_METHOD_CALL_ARGS, getDefaultAsString(WRAP_METHOD_CALL_ARGS));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapMethodCallArgsAfterLeftParen() {
        return preferences.getBoolean(
                WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN,
                getDefaultAsBoolean(WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN)
        );
    }

    public boolean wrapMethodCallArgsRightParen() {
        return preferences.getBoolean(
                WRAP_METHOD_CALL_ARGS_RIGHT_PAREN,
                getDefaultAsBoolean(WRAP_METHOD_CALL_ARGS_RIGHT_PAREN)
        );
    }

    public WrapStyle wrapChainedMethodCalls() {
        String wrap = preferences.get(WRAP_CHAINED_METHOD_CALLS, getDefaultAsString(WRAP_CHAINED_METHOD_CALLS));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapArrayInit() {
        String wrap = preferences.get(WRAP_ARRAY_INIT, getDefaultAsString(WRAP_ARRAY_INIT));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapFor() {
        String wrap = preferences.get(WRAP_FOR, getDefaultAsString(WRAP_FOR));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapForAfterLeftParen() {
        return preferences.getBoolean(
                WRAP_FOR_AFTER_LEFT_PAREN,
                getDefaultAsBoolean(WRAP_FOR_AFTER_LEFT_PAREN)
        );
    }

    public boolean wrapForRightParen() {
        return preferences.getBoolean(
                WRAP_FOR_RIGHT_PAREN,
                getDefaultAsBoolean(WRAP_FOR_RIGHT_PAREN)
        );
    }

    public WrapStyle wrapForStatement() {
        String wrap = preferences.get(WRAP_FOR_STATEMENT, getDefaultAsString(WRAP_FOR_STATEMENT));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapIfStatement() {
        String wrap = preferences.get(WRAP_IF_STATEMENT, getDefaultAsString(WRAP_IF_STATEMENT));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapWhileStatement() {
        String wrap = preferences.get(WRAP_WHILE_STATEMENT, getDefaultAsString(WRAP_WHILE_STATEMENT));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapDoWhileStatement() {
        String wrap = preferences.get(WRAP_DO_WHILE_STATEMENT, getDefaultAsString(WRAP_DO_WHILE_STATEMENT));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapBinaryOps() {
        String wrap = preferences.get(WRAP_BINARY_OPS, getDefaultAsString(WRAP_BINARY_OPS));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapTernaryOps() {
        String wrap = preferences.get(WRAP_TERNARY_OPS, getDefaultAsString(WRAP_TERNARY_OPS));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapCoalescingOps() {
        String wrap = preferences.get(WRAP_COALESCING_OPS, getDefaultAsString(WRAP_COALESCING_OPS));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAssignOps() {
        String wrap = preferences.get(WRAP_ASSIGN_OPS, getDefaultAsString(WRAP_ASSIGN_OPS));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapBlockBrace() {
        return preferences.getBoolean(WRAP_BLOCK_BRACES, getDefaultAsBoolean(WRAP_BLOCK_BRACES));
    }

    public boolean wrapGroupUseBraces() {
        return preferences.getBoolean(WRAP_GROUP_USE_BRACES, getDefaultAsBoolean(WRAP_GROUP_USE_BRACES));
    }

    public boolean wrapStatementsOnTheSameLine() {
        return preferences.getBoolean(WRAP_STATEMENTS_ON_THE_LINE, getDefaultAsBoolean(WRAP_STATEMENTS_ON_THE_LINE));
    }

    public boolean wrapAfterBinOps() {
        return preferences.getBoolean(WRAP_AFTER_BIN_OPS, getDefaultAsBoolean(WRAP_AFTER_BIN_OPS));
    }

    public boolean wrapAfterAssignOps() {
        return preferences.getBoolean(WRAP_AFTER_ASSIGN_OPS, getDefaultAsBoolean(WRAP_AFTER_ASSIGN_OPS));
    }

    // Uses

    public boolean preferFullyQualifiedNames() {
        return preferences.getBoolean(PREFER_FULLY_QUALIFIED_NAMES, getDefaultAsBoolean(PREFER_FULLY_QUALIFIED_NAMES));
    }

    public boolean preferMultipleUseStatementsCombined() {
        return preferences.getBoolean(PREFER_MULTIPLE_USE_STATEMENTS_COMBINED, getDefaultAsBoolean(PREFER_MULTIPLE_USE_STATEMENTS_COMBINED));
    }

    public boolean preferGroupUses() {
        return preferences.getBoolean(PREFER_GROUP_USES, getDefaultAsBoolean(PREFER_GROUP_USES));
    }

    public boolean startUseWithNamespaceSeparator() {
        return preferences.getBoolean(START_USE_WITH_NAMESPACE_SEPARATOR, getDefaultAsBoolean(START_USE_WITH_NAMESPACE_SEPARATOR));
    }

    public boolean aliasesFromCapitalsOfNamespaces() {
        return preferences.getBoolean(ALIASES_CAPITALS_OF_NAMESPACES, getDefaultAsBoolean(ALIASES_CAPITALS_OF_NAMESPACES));
    }

    public boolean putInPSR12Order() {
        return preferences.getBoolean(PUT_IN_PSR12_ORDER, getDefaultAsBoolean(PUT_IN_PSR12_ORDER));
    }

    public boolean usesKeepExistingTypeOrder() {
        return preferences.getBoolean(USES_KEEP_EXISTING_TYPE_ORDER, getDefaultAsBoolean(USES_KEEP_EXISTING_TYPE_ORDER));
    }

    private static class Producer implements FmtOptions.CodeStyleProducer {

        @Override
        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
    }

    public enum BracePlacement {
        SAME_LINE,
        NEW_LINE,
        NEW_LINE_INDENTED,
        PRESERVE_EXISTING
    }

    public enum WrapStyle {
        WRAP_ALWAYS,
        WRAP_IF_LONG,
        WRAP_NEVER
    }
}
