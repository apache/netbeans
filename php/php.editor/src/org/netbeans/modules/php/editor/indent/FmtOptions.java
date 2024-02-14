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

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.options.editor.spi.PreviewProvider;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.indent.CodeStyle.WrapStyle;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author phrebejk
 *
 */
public final class FmtOptions {

    private static final Logger LOGGER = Logger.getLogger(FmtOptions.class.getName());
    public static final String EXPAND_TAB_TO_SPACES = SimpleValueNames.EXPAND_TABS;
    public static final String TAB_SIZE = SimpleValueNames.TAB_SIZE;
    public static final String SPACES_PER_TAB = SimpleValueNames.SPACES_PER_TAB;
    public static final String INDENT_SIZE = SimpleValueNames.INDENT_SHIFT_WIDTH;
    public static final String CONTINUATION_INDENT_SIZE = "continuationIndentSize"; //NOI18N
    public static final String ITEMS_IN_ARRAY_DECLARATION_INDENT_SIZE = "itemsInArrayDeclarationIndentSize"; //NOI18N
    public static final String REFORMAT_COMMENTS = "reformatComments"; //NOI18N
    public static final String INDENT_HTML = "indentHtml"; //NOI18N
    public static final String RIGHT_MARGIN = SimpleValueNames.TEXT_LIMIT_WIDTH;
    public static final String INITIAL_INDENT = "init.indent"; //NOI18N
    public static final String CLASS_DECL_BRACE_PLACEMENT = "classDeclBracePlacement"; //NOI18N
    public static final String ANONYMOUS_CLASS_BRACE_PLACEMENT = "anonymousClassBracePlacement"; //NOI18N
    public static final String METHOD_DECL_BRACE_PLACEMENT = "methodDeclBracePlacement"; //NOI18N
    public static final String IF_BRACE_PLACEMENT = "ifBracePlacement"; //NOI18N
    public static final String FOR_BRACE_PLACEMENT = "forBracePlacement"; //NOI18N
    public static final String WHILE_BRACE_PLACEMENT = "whileBracePlacement"; //NOI18N
    public static final String SWITCH_BRACE_PLACEMENT = "switchBracePlacement"; //NOI18N
    public static final String MATCH_BRACE_PLACEMENT = "matchBracePlacement"; //NOI18N
    public static final String CATCH_BRACE_PLACEMENT = "catchBracePlacement"; //NOI18N
    public static final String USE_TRAIT_BODY_BRACE_PLACEMENT = "useTraitBodyBracePlacement"; //NOI18N
    public static final String GROUP_USE_BRACE_PLACEMENT = "groupUseBracePlacement"; //NOI18N
    public static final String OTHER_BRACE_PLACEMENT = "otherBracePlacement"; //NOI18N
    public static final String BLANK_LINES_BEFORE_NAMESPACE = "blankLinesBeforeNamespace"; //NOI18N
    public static final String BLANK_LINES_AFTER_NAMESPACE = "blankLinesAfterNamespace"; //NOI18N
    public static final String BLANK_LINES_BEFORE_USE = "blankLinesBeforeUse"; //NOI18N
    public static final String BLANK_LINES_BEFORE_USE_TRAIT = "blankLinesBeforeUseTrait"; //NOI18N
    public static final String BLANK_LINES_AFTER_USE_TRAIT = "blankLinesAfterUseTrait"; //NOI18N
    public static final String BLANK_LINES_AFTER_USE = "blankLinesAfterUse"; //NOI18N
    public static final String BLANK_LINES_BETWEEN_USE_TYPES = "blankLinesBetweenUseType"; //NOI18N
    public static final String BLANK_LINES_BEFORE_CLASS = "blankLinesBeforeClass"; //NOI18N
    public static final String BLANK_LINES_BEFORE_CLASS_END = "blankLinesBeforeClassEnd"; //NOI18N
    public static final String BLANK_LINES_AFTER_CLASS = "blankLinesAfterClass"; //NOI18N
    public static final String BLANK_LINES_AFTER_CLASS_HEADER = "blankLinesAfterClassHeader"; //NOI18N
    public static final String BLANK_LINES_BEFORE_FIELDS = "blankLinesBeforeField"; //NOI18N
    public static final String BLANK_LINES_BETWEEN_FIELDS = "blankLinesBetweenField"; //NOI18N
    public static final String BLANK_LINES_AFTER_FIELDS = "blankLinesAfterField"; //NOI18N
    public static final String BLANK_LINES_EOF = "blankLinesEndOfFile"; //NOI18N
    public static final String BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES = "blankLinesGroupFieldsWithoutDocAndAttributes"; //NOI18N
    public static final String BLANK_LINES_BEFORE_FUNCTION = "blankLinesBeforeFunction"; //NOI18N
    public static final String BLANK_LINES_AFTER_FUNCTION = "blankLinesAfterFunction"; //NOI18N
    public static final String BLANK_LINES_BEFORE_FUNCTION_END = "blankLinesBeforeFunctionEnd"; //NOI18N
    public static final String BLANK_LINES_AFTER_OPEN_PHP_TAG = "blankLinesAfterOpenPHPTag"; //NOI18N
    public static final String BLANK_LINES_AFTER_OPEN_PHP_TAG_IN_HTML = "blankLinesAfterOpenPHPTagInHTML"; //NOI18N
    public static final String BLANK_LINES_BEFORE_CLOSE_PHP_TAG = "blankLinesBeforeClosePHPTag"; //NOI18N
    public static final String BLANK_LINES_MAX_PRESERVED = "blankLinesMaxPreserved"; //NOI18N
    public static final String SPACE_BEFORE_WHILE = "spaceBeforeWhile"; //NOI18N
    public static final String SPACE_BEFORE_ELSE = "spaceBeforeElse"; //NOI18N
    public static final String SPACE_BEFORE_CATCH = "spaceBeforeCatch"; //NOI18N
    public static final String SPACE_BEFORE_FINALLY = "spaceBeforeFinally"; //NOI18N
    public static final String SPACE_BEFORE_ANONYMOUS_CLASS_PAREN = "spaceBeforeAnonymousClassParen"; //NOI18N
    public static final String SPACE_BEFORE_ANONYMOUS_FUNCTION_PAREN = "spaceBeforeAnonymousFunctionParen"; //NOI18N
    public static final String SPACE_BEFORE_ATTRIBUTE_DECL_PAREN = "spaceBeforeAttributeDeclParen"; //NOI18N
    public static final String SPACE_BEFORE_METHOD_DECL_PAREN = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final String SPACE_BEFORE_METHOD_CALL_PAREN = "spaceBeforeMethodCallParen"; //NOI18N
    public static final String SPACE_BEFORE_IF_PAREN = "spaceBeforeIfParen"; //NOI18N
    public static final String SPACE_BEFORE_FOR_PAREN = "spaceBeforeForParen"; //NOI18N
    public static final String SPACE_BEFORE_WHILE_PAREN = "spaceBeforeWhileParen"; //NOI18N
    public static final String SPACE_BEFORE_CATCH_PAREN = "spaceBeforeCatchParen"; //NOI18N
    public static final String SPACE_BEFORE_SWITCH_PAREN = "spaceBeforeSwitchParen"; //NOI18N
    public static final String SPACE_BEFORE_MATCH_PAREN = "spaceBeforeMatchParen"; //NOI18N
    public static final String SPACE_BEFORE_ARRAY_DECL_PAREN = "spaceBeforeArrayDeclParen"; //NOI18N
    public static final String SPACE_AROUND_UNARY_OPS = "spaceAroundUnaryOps"; //NOI18N
    public static final String SPACE_AROUND_BINARY_OPS = "spaceAroundBinaryOps"; //NOI18N
    public static final String SPACE_AROUND_TERNARY_OPS = "spaceAroundTernaryOps"; //NOI18N
    public static final String SPACE_AROUND_COALESCING_OPS = "spaceAroundCoalescingOps"; //NOI18N
    public static final String SPACE_AROUND_STRING_CONCAT_OPS = "spaceAroundStringConcatOps"; //NOI18N
    public static final String SPACE_AROUND_ASSIGN_OPS = "spaceAroundAssignOps"; //NOI18N
    public static final String SPACE_AROUND_KEY_VALUE_OPS = "spaceAroundKeyValueOps"; //NOI18N
    public static final String SPACE_AROUND_SCOPE_RESOLUTION_OPS = "spaceAroundScopeResolutionOps"; //NOI18N
    public static final String SPACE_AROUND_OBJECT_OPS = "spaceAroundObjectOps"; //NOI18N
    public static final String SPACE_AROUND_NULLSAFE_OBJECT_OPS = "spaceAroundNullsafeObjectOps"; //NOI18N
    public static final String SPACE_AROUND_DECLARE_EQUAL = "spaceAroundDeclareEqual"; //NOI18N
    public static final String SPACE_AROUND_UNION_TYPE_SEPARATOR = "spaceAroundUnionTypeSeparator"; //NOI18N
    public static final String SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR = "spaceAroundIntersectionTypeSeparator"; //NOI18N
    public static final String SPACE_BEFORE_CLASS_DECL_LEFT_BRACE = "spaceBeforeClassDeclLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_ANONYMOUS_CLASS_LEFT_BRACE = "spaceBeforeAnonymousClassLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_METHOD_DECL_LEFT_BRACE = "spaceBeforeMethodDeclLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_IF_LEFT_BRACE = "spaceBeforeIfLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_ELSE_LEFT_BRACE = "spaceBeforeElseLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_WHILE_LEFT_BRACE = "spaceBeforeWhileLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_FOR_LEFT_BRACE = "spaceBeforeForLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_DO_LEFT_BRACE = "spaceBeforeDoLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_SWITCH_LEFT_BRACE = "spaceBeforeSwitchLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_MATCH_LEFT_BRACE = "spaceBeforeMatchLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_TRY_LEFT_BRACE = "spaceBeforeTryLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_CATCH_LEFT_BRACE = "spaceBeforeCatchLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_FINALLY_LEFT_BRACE = "spaceBeforeFinallyLeftBrace"; //NOI18N
    public static final String SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE = "spaceBeforeUseTraitBodyLeftBrace"; //NOI18N
    public static final String SPACE_WITHIN_ARRAY_DECL_PARENS = "spaceWithinArrayDeclParens"; //NOI18N
    public static final String SPACE_WITHIN_ANONYMOUS_CLASS_PARENS = "spaceWithinAnonymousClassParens"; //NOI18N
    public static final String SPACE_WITHIN_METHOD_DECL_PARENS = "spaceWithinMethodDeclParens"; //NOI18N
    public static final String SPACE_WITHIN_METHOD_CALL_PARENS = "spaceWithinMethodCallParens"; //NOI18N
    public static final String SPACE_WITHIN_IF_PARENS = "spaceWithinIfParens"; //NOI18N
    public static final String SPACE_WITHIN_FOR_PARENS = "spaceWithinForParens"; //NOI18N
    public static final String SPACE_WITHIN_WHILE_PARENS = "spaceWithinWhileParens"; //NOI18N
    public static final String SPACE_WITHIN_SWITCH_PARENS = "spaceWithinSwitchParens"; //NOI18N
    public static final String SPACE_WITHIN_MATCH_PARENS = "spaceWithinMatchParens"; //NOI18N
    public static final String SPACE_WITHIN_CATCH_PARENS = "spaceWithinCatchParens"; //NOI18N
    public static final String SPACE_WITHIN_TYPE_CAST_PARENS = "spaceWithinTypeCastParens"; //NOI18N
    public static final String SPACE_WITHIN_ARRAY_BRACKETS = "spaceWithinArrayBrackets"; //NOI18N
    public static final String SPACE_WITHIN_ATTRIBUTE_BRACKETS = "spaceWithinAttributeBrackets"; //NOI18N
    public static final String SPACE_WITHIN_ATTRIBUTE_DECL_PARENS = "spaceWithinAttributeDeclParens"; //NOI18N
    public static final String SPACE_BEFORE_COMMA = "spaceBeforeComma"; //NOI18N
    public static final String SPACE_AFTER_COMMA = "spaceAfterComma"; //NOI18N
    public static final String SPACE_BEFORE_SEMI = "spaceBeforeSemi"; //NOI18N
    public static final String SPACE_AFTER_SEMI = "spaceAfterSemi"; //NOI18N
    public static final String SPACE_AFTER_TYPE_CAST = "spaceAfterTypeCast"; //NOI18N
    public static final String SPACE_CHECK_AFTER_KEYWORDS = "spaceCheckAfterKeywords"; //NOI18N
    public static final String SPACE_AFTER_SHORT_PHP_TAG = "spaceAfterShortPHPTag"; //NOI18N
    public static final String SPACE_BEFORE_CLOSE_PHP_TAG = "spaceBeforeClosePHPTag"; //NOI18N
    public static final String SPACE_BETWEEN_OPEN_PHP_TAG_AND_NAMESPACE = "spaceBetweenOpenPHPTagAndNamespace"; //NOI18N
    public static final String PLACE_ELSE_ON_NEW_LINE = "placeElseOnNewLine"; //NOI18N
    public static final String PLACE_WHILE_ON_NEW_LINE = "placeWhileOnNewLine"; //NOI18N
    public static final String PLACE_CATCH_ON_NEW_LINE = "placeCatchOnNewLine"; //NOI18N
    public static final String PLACE_FINALLY_ON_NEW_LINE = "placeFinallyOnNewLine"; //NOI18N
    public static final String PLACE_NEW_LINE_AFTER_MODIFIERS = "placeNewLineAfterModifiers"; //NOI18N
    public static final String ALIGN_MULTILINE_METHOD_PARAMS = "alignMultilineMethodParams"; //NOI18N
    public static final String ALIGN_MULTILINE_CALL_ARGS = "alignMultilineCallArgs"; //NOI18N
    public static final String ALIGN_MULTILINE_IMPLEMENTS = "alignMultilineImplements"; //NOI18N
    public static final String ALIGN_MULTILINE_PARENTHESIZED = "alignMultilineParenthesized"; //NOI18N
    public static final String ALIGN_MULTILINE_BINARY_OP = "alignMultilineBinaryOp"; //NOI18N
    public static final String ALIGN_MULTILINE_TERNARY_OP = "alignMultilineTernaryOp"; //NOI18N
    public static final String ALIGN_MULTILINE_ASSIGNMENT = "alignMultilineAssignment"; //NOI18N
    public static final String ALIGN_MULTILINE_FOR = "alignMultilineFor"; //NOI18N
    public static final String ALIGN_MULTILINE_ARRAY_INIT = "alignMultilineArrayInit"; //NOI18N
    public static final String GROUP_ALIGNMENT_ASSIGNMENT = "groupAlignmentAssignment"; //NOI18N
    public static final String GROUP_ALIGNMENT_ARRAY_INIT = "groupAlignmentArrayInit"; //NOI18N
    public static final String GROUP_ALIGNMENT_MATCH_ARM_ARROW = "groupAlignmentMatchArmArrow"; //NOI18N
    public static final String WRAP_GROUP_USE_LIST = "wrapGroupUseList"; //NOI18N
    public static final String WRAP_EXTENDS_IMPLEMENTS_KEYWORD = "wrapExtendsImplementsKeyword"; //NOI18N
    public static final String WRAP_EXTENDS_IMPLEMENTS_LIST = "wrapExtendsImplementsList"; //NOI18N
    public static final String WRAP_METHOD_PARAMS = "wrapMethodParams"; //NOI18N
    public static final String WRAP_METHOD_PARAMS_AFTER_LEFT_PAREN = "wrapMethodParamsAfterLeftParen"; // NOI18N
    public static final String WRAP_METHOD_PARAMS_RIGHT_PAREN = "wrapMethodParamsRightParen"; // NOI18N
    public static final String WRAP_METHOD_PARAMS_KEEP_PAREN_AND_BRACE_ON_THE_SAME_LINE = "wrapKeepParenAndBraceOnTheSameLine"; // NOI18N
    public static final String WRAP_METHOD_CALL_ARGS = "wrapMethodCallArgs"; //NOI18N
    public static final String WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN = "wrapMethodCallArgsAfterLeftParen"; // NOI18N
    public static final String WRAP_METHOD_CALL_ARGS_RIGHT_PAREN = "wrapMethodCallArgsRightParen"; // NOI18N
    public static final String WRAP_CHAINED_METHOD_CALLS = "wrapChainedMethodCalls"; //NOI18N
    public static final String WRAP_ARRAY_INIT = "wrapArrayInit"; //NOI18N
    public static final String WRAP_FOR = "wrapFor"; //NOI18N
    public static final String WRAP_FOR_AFTER_LEFT_PAREN = "wrapForAfterLeftParen"; // NOI18N
    public static final String WRAP_FOR_RIGHT_PAREN = "wrapForRightParen"; // NOI18N
    public static final String WRAP_FOR_STATEMENT = "wrapForStatement"; //NOI18N
    public static final String WRAP_IF_STATEMENT = "wrapIfStatement"; //NOI18N
    public static final String WRAP_WHILE_STATEMENT = "wrapWhileStatement"; //NOI18N
    public static final String WRAP_DO_WHILE_STATEMENT = "wrapDoWhileStatement"; //NOI18N
    public static final String WRAP_BINARY_OPS = "wrapBinaryOps"; //NOI18N
    public static final String WRAP_TERNARY_OPS = "wrapTernaryOps"; //NOI18N
    public static final String WRAP_COALESCING_OPS = "wrapCoalescingOps"; //NOI18N
    public static final String WRAP_ASSIGN_OPS = "wrapAssignOps"; //NOI18N
    public static final String WRAP_BLOCK_BRACES = "wrapBlockBraces";  //NOI18N
    public static final String WRAP_GROUP_USE_BRACES = "wrapGroupUseBraces"; // NOI18N
    public static final String WRAP_STATEMENTS_ON_THE_LINE = "wrapStateMentsOnTheLine"; // NOI18N
    public static final String WRAP_AFTER_BIN_OPS = "wrapAfterBinOps"; // NOI18N
    public static final String WRAP_AFTER_ASSIGN_OPS = "wrapAfterAssignOps"; // NOI18N
    public static final String PREFER_FULLY_QUALIFIED_NAMES = "preferFullyQualifiedNames"; //NOI18N
    public static final String PREFER_MULTIPLE_USE_STATEMENTS_COMBINED = "preferMultipleUseStatementsCombined"; //NOI18N
    public static final String PREFER_GROUP_USES = "preferGroupUses"; // NOI18N
    public static final String START_USE_WITH_NAMESPACE_SEPARATOR = "startUseWithNamespaceSeparator"; //NOI18N
    public static final String ALIASES_CAPITALS_OF_NAMESPACES = "aliasesCapitalsOfNamespacesNames"; //NOI18N
    public static final String PUT_IN_PSR12_ORDER = "putInPSR12Order"; //NOI18N
    public static final String USES_KEEP_EXISTING_TYPE_ORDER = "usesKeepExistingTypeOrder"; //NOI18N
    public static CodeStyleProducer codeStyleProducer;

    private FmtOptions() {
    }

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }

    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }

    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }
    // Private section ---------------------------------------------------------
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    //opening brace styles
    public static final String OBRACE_NEWLINE = CodeStyle.BracePlacement.NEW_LINE.name();
    public static final String OBRACE_SAMELINE = CodeStyle.BracePlacement.SAME_LINE.name();
    public static final String OBRACE_PRESERVE = CodeStyle.BracePlacement.PRESERVE_EXISTING.name();
    public static final String OBRACE_NEWLINE_INDENTED = CodeStyle.BracePlacement.NEW_LINE_INDENTED.name();
    public static final String WRAP_ALWAYS = CodeStyle.WrapStyle.WRAP_ALWAYS.name();
    public static final String WRAP_IF_LONG = CodeStyle.WrapStyle.WRAP_IF_LONG.name();
    public static final String WRAP_NEVER = CodeStyle.WrapStyle.WRAP_NEVER.name();
    private static Map<String, String> defaults;

    static {
        createDefaults();
    }

    private static void createDefaults() {
        String[][] defaultValues = {
            {EXPAND_TAB_TO_SPACES, TRUE}, //NOI18N
            {TAB_SIZE, "8"}, //NOI18N
            {INDENT_SIZE, "4"}, //NOI18N
            {CONTINUATION_INDENT_SIZE, "8"}, //NOI18N
            {ITEMS_IN_ARRAY_DECLARATION_INDENT_SIZE, "4"}, // NOI18N
            {REFORMAT_COMMENTS, FALSE}, //NOI18N
            {INDENT_HTML, TRUE}, //NOI18N
            {RIGHT_MARGIN, "80"}, //NOI18N
            {INITIAL_INDENT, "0"}, //NOI18N

            {CLASS_DECL_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {ANONYMOUS_CLASS_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {METHOD_DECL_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {IF_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {FOR_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {WHILE_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {SWITCH_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {MATCH_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {CATCH_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {USE_TRAIT_BODY_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {GROUP_USE_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {OTHER_BRACE_PLACEMENT, OBRACE_SAMELINE},
            {BLANK_LINES_BEFORE_NAMESPACE, "1"}, //NOI18N
            {BLANK_LINES_AFTER_NAMESPACE, "1"}, //NOI18N
            {BLANK_LINES_BEFORE_USE, "1"}, //NOI18N
            {BLANK_LINES_BEFORE_USE_TRAIT, "1"}, //NOI18N
            {BLANK_LINES_AFTER_USE_TRAIT, "1"}, //NOI18N
            {BLANK_LINES_AFTER_USE, "1"}, //NOI18N
            {BLANK_LINES_BETWEEN_USE_TYPES, "0"}, //NOI18N
            {BLANK_LINES_BEFORE_CLASS, "1"}, //NOI18N
            {BLANK_LINES_AFTER_CLASS, "1"}, //NOI18N
            {BLANK_LINES_AFTER_CLASS_HEADER, "0"}, //NOI18N
            {BLANK_LINES_BEFORE_CLASS_END, "0"}, //NOI18N
            {BLANK_LINES_BEFORE_FIELDS, "1"}, //NOI18N
            {BLANK_LINES_EOF, FALSE},
            {BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES, TRUE},
            {BLANK_LINES_BETWEEN_FIELDS, "1"}, //NOI18N
            {BLANK_LINES_AFTER_FIELDS, "1"}, //NOI18N
            {BLANK_LINES_BEFORE_FUNCTION, "1"}, //NOI18N
            {BLANK_LINES_AFTER_FUNCTION, "1"}, //NOI18N
            {BLANK_LINES_BEFORE_FUNCTION_END, "0"}, //NOI18N
            {BLANK_LINES_AFTER_OPEN_PHP_TAG, "1"}, //NOI18N
            {BLANK_LINES_AFTER_OPEN_PHP_TAG_IN_HTML, "0"}, //NOI18N
            {BLANK_LINES_BEFORE_CLOSE_PHP_TAG, "0"}, //NOI18N
            {BLANK_LINES_MAX_PRESERVED, "1"}, //NOI18N

            {SPACE_BEFORE_WHILE, TRUE},
            {SPACE_BEFORE_ELSE, TRUE},
            {SPACE_BEFORE_CATCH, TRUE},
            {SPACE_BEFORE_FINALLY, TRUE},
            {SPACE_BEFORE_ANONYMOUS_CLASS_PAREN, FALSE},
            {SPACE_BEFORE_ANONYMOUS_FUNCTION_PAREN, TRUE},
            {SPACE_BEFORE_ATTRIBUTE_DECL_PAREN, FALSE},
            {SPACE_BEFORE_METHOD_DECL_PAREN, FALSE},
            {SPACE_BEFORE_METHOD_CALL_PAREN, FALSE},
            {SPACE_BEFORE_IF_PAREN, TRUE},
            {SPACE_BEFORE_FOR_PAREN, TRUE},
            {SPACE_BEFORE_WHILE_PAREN, TRUE},
            {SPACE_BEFORE_CATCH_PAREN, TRUE},
            {SPACE_BEFORE_SWITCH_PAREN, TRUE},
            {SPACE_BEFORE_MATCH_PAREN, TRUE},
            {SPACE_BEFORE_ARRAY_DECL_PAREN, FALSE},
            {SPACE_AROUND_UNARY_OPS, FALSE},
            {SPACE_AROUND_BINARY_OPS, TRUE},
            {SPACE_AROUND_TERNARY_OPS, TRUE},
            {SPACE_AROUND_COALESCING_OPS, TRUE},
            {SPACE_AROUND_STRING_CONCAT_OPS, TRUE},
            {SPACE_AROUND_KEY_VALUE_OPS, TRUE},
            {SPACE_AROUND_ASSIGN_OPS, TRUE},
            {SPACE_AROUND_SCOPE_RESOLUTION_OPS, FALSE},
            {SPACE_AROUND_OBJECT_OPS, FALSE},
            {SPACE_AROUND_NULLSAFE_OBJECT_OPS, FALSE},
            {SPACE_AROUND_DECLARE_EQUAL, FALSE},
            {SPACE_AROUND_UNION_TYPE_SEPARATOR, FALSE},
            {SPACE_AROUND_INTERSECTION_TYPE_SEPARATOR, FALSE},
            {SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_IF_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_ELSE_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_WHILE_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_FOR_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_DO_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_SWITCH_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_MATCH_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_TRY_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_CATCH_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_FINALLY_LEFT_BRACE, TRUE},
            {SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE, TRUE},
            {SPACE_WITHIN_ARRAY_DECL_PARENS, FALSE},
            {SPACE_WITHIN_ANONYMOUS_CLASS_PARENS, FALSE},
            {SPACE_WITHIN_METHOD_DECL_PARENS, FALSE},
            {SPACE_WITHIN_METHOD_CALL_PARENS, FALSE},
            {SPACE_WITHIN_IF_PARENS, FALSE},
            {SPACE_WITHIN_FOR_PARENS, FALSE},
            {SPACE_WITHIN_WHILE_PARENS, FALSE},
            {SPACE_WITHIN_SWITCH_PARENS, FALSE},
            {SPACE_WITHIN_MATCH_PARENS, FALSE},
            {SPACE_WITHIN_CATCH_PARENS, FALSE},
            {SPACE_WITHIN_TYPE_CAST_PARENS, FALSE},
            {SPACE_WITHIN_ARRAY_BRACKETS, FALSE},
            {SPACE_WITHIN_ATTRIBUTE_BRACKETS, FALSE},
            {SPACE_WITHIN_ATTRIBUTE_DECL_PARENS, FALSE},
            {SPACE_BEFORE_COMMA, FALSE},
            {SPACE_AFTER_COMMA, TRUE},
            {SPACE_BEFORE_SEMI, FALSE},
            {SPACE_AFTER_SEMI, TRUE},
            {SPACE_AFTER_TYPE_CAST, TRUE},
            {SPACE_CHECK_AFTER_KEYWORDS, TRUE},
            {SPACE_AFTER_SHORT_PHP_TAG, TRUE},
            {SPACE_BEFORE_CLOSE_PHP_TAG, TRUE},
            {SPACE_BETWEEN_OPEN_PHP_TAG_AND_NAMESPACE, FALSE},
            {ALIGN_MULTILINE_METHOD_PARAMS, FALSE}, //NOI18N
            {ALIGN_MULTILINE_CALL_ARGS, FALSE}, //NOI18N
            {ALIGN_MULTILINE_IMPLEMENTS, FALSE}, //NOI18N
            {ALIGN_MULTILINE_PARENTHESIZED, FALSE}, //NOI18N
            {ALIGN_MULTILINE_BINARY_OP, FALSE}, //NOI18N
            {ALIGN_MULTILINE_TERNARY_OP, FALSE}, //NOI18N
            {ALIGN_MULTILINE_ASSIGNMENT, FALSE}, //NOI18N
            {ALIGN_MULTILINE_FOR, FALSE}, //NOI18N
            {ALIGN_MULTILINE_ARRAY_INIT, FALSE}, //NOI18N
            {PLACE_ELSE_ON_NEW_LINE, FALSE}, //NOI18N
            {PLACE_WHILE_ON_NEW_LINE, FALSE}, //NOI18N
            {PLACE_CATCH_ON_NEW_LINE, FALSE}, //NOI18N
            {PLACE_FINALLY_ON_NEW_LINE, FALSE}, //NOI18N
            {PLACE_NEW_LINE_AFTER_MODIFIERS, FALSE}, //NOI18N

            {GROUP_ALIGNMENT_ARRAY_INIT, FALSE},
            {GROUP_ALIGNMENT_MATCH_ARM_ARROW, FALSE},
            {GROUP_ALIGNMENT_ASSIGNMENT, FALSE},
            {WRAP_GROUP_USE_LIST, WRAP_ALWAYS},
            {WRAP_EXTENDS_IMPLEMENTS_KEYWORD, WRAP_NEVER}, //NOI18N
            {WRAP_EXTENDS_IMPLEMENTS_LIST, WRAP_NEVER}, //NOI18N
            {WRAP_METHOD_PARAMS, WRAP_NEVER}, //NOI18N
            {WRAP_METHOD_PARAMS_AFTER_LEFT_PAREN, FALSE}, //NOI18N
            {WRAP_METHOD_PARAMS_RIGHT_PAREN, FALSE}, //NOI18N
            {WRAP_METHOD_PARAMS_KEEP_PAREN_AND_BRACE_ON_THE_SAME_LINE, FALSE},
            {WRAP_METHOD_CALL_ARGS, WRAP_NEVER}, //NOI18N
            {WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN, FALSE}, //NOI18N
            {WRAP_METHOD_CALL_ARGS_RIGHT_PAREN, FALSE}, //NOI18N
            {WRAP_CHAINED_METHOD_CALLS, WRAP_NEVER}, //NOI18N
            {WRAP_ARRAY_INIT, WRAP_NEVER}, //NOI18N
            {WRAP_FOR, WRAP_NEVER}, //NOI18N
            {WRAP_FOR_AFTER_LEFT_PAREN, FALSE}, //NOI18N
            {WRAP_FOR_RIGHT_PAREN, FALSE}, //NOI18N
            {WRAP_FOR_STATEMENT, WRAP_ALWAYS}, //NOI18N
            {WRAP_IF_STATEMENT, WRAP_ALWAYS}, //NOI18N
            {WRAP_WHILE_STATEMENT, WRAP_ALWAYS}, //NOI18N
            {WRAP_DO_WHILE_STATEMENT, WRAP_ALWAYS}, //NOI18N
            {WRAP_BINARY_OPS, WRAP_NEVER}, //NOI18N
            {WRAP_TERNARY_OPS, WRAP_NEVER},
            {WRAP_COALESCING_OPS, WRAP_NEVER},
            {WRAP_ASSIGN_OPS, WRAP_NEVER},
            {WRAP_BLOCK_BRACES, TRUE},
            {WRAP_GROUP_USE_BRACES, TRUE},
            {WRAP_STATEMENTS_ON_THE_LINE, TRUE},
            {WRAP_AFTER_BIN_OPS, FALSE},
            {WRAP_AFTER_ASSIGN_OPS, FALSE},
            {PREFER_FULLY_QUALIFIED_NAMES, FALSE},
            {PREFER_MULTIPLE_USE_STATEMENTS_COMBINED, FALSE},
            {PREFER_GROUP_USES, FALSE},
            {START_USE_WITH_NAMESPACE_SEPARATOR, FALSE},
            {ALIASES_CAPITALS_OF_NAMESPACES, FALSE},
            {PUT_IN_PSR12_ORDER, FALSE},
            {USES_KEEP_EXISTING_TYPE_ORDER, TRUE},
        };

        defaults = new HashMap<>();

        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }

    public static Map<String, String> getDefaults() {
        return defaults;
    }

    // Support section ---------------------------------------------------------
    public static class CategorySupport implements ActionListener, DocumentListener, PreviewProvider, PreferencesCustomizer {

        private static enum Operation {
            LOAD,
            STORE,
            ADD_LISTENERS
        }

        public static final String OPTION_ID = "org.netbeans.modules.php.editor.indent.FormatingOptions.ID";
        private static final ComboItem[] BRACE_PLACEMENT = new ComboItem[]{
            new ComboItem(OBRACE_NEWLINE, "LBL_bp_NEWLINE"), // NOI18N
            new ComboItem(OBRACE_NEWLINE_INDENTED, "LBL_bp_NEWLINE_INDENTED"), // NOI18N
            new ComboItem(OBRACE_SAMELINE, "LBL_bp_SAMELINE"), // NOI18N
            new ComboItem(OBRACE_PRESERVE, "LBL_bp_PRESERVE"), // NOI18N
        };
        private static final ComboItem[] WRAP = new ComboItem[]{
            new ComboItem(WrapStyle.WRAP_ALWAYS.name(), "LBL_wrp_WRAP_ALWAYS"), // NOI18N
            new ComboItem(WrapStyle.WRAP_IF_LONG.name(), "LBL_wrp_WRAP_IF_LONG"), // NOI18N
            new ComboItem(WrapStyle.WRAP_NEVER.name(), "LBL_wrp_WRAP_NEVER") // NOI18N
        };
        private final String previewText;
        private final String id;
        protected final JPanel panel;
        private final List<JComponent> components = new LinkedList<>();
        private JEditorPane previewPane;
        private final Preferences preferences;
        private final Preferences previewPrefs;

        protected CategorySupport(Preferences preferences, String id, JPanel panel, String previewText, String[]... forcedOptions) {
            this.preferences = preferences;
            this.id = id;
            this.panel = panel;
            this.previewText = previewText != null ? previewText : NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default"); //NOI18N

            // Scan the panel for its components
            scan(panel, components);

            // Initialize the preview preferences
            Preferences forcedPrefs = new PreviewPreferences();
            for (String[] option : forcedOptions) {
                forcedPrefs.put(option[0], option[1]);
            }
            this.previewPrefs = new ProxyPreferences(preferences, forcedPrefs);

            // Load and hook up all the components
            loadFrom(preferences);
            addListeners();
        }

        protected void addListeners() {
            scan(Operation.ADD_LISTENERS, null);
        }

        protected void loadFrom(Preferences preferences) {
            scan(Operation.LOAD, preferences);
        }

        protected void storeTo(Preferences p) {
            scan(Operation.STORE, p);
        }

        public void notifyChanged() {
            storeTo(preferences);
            refreshPreview();
        }

        // ActionListener implementation ---------------------------------------
        @Override
        public void actionPerformed(ActionEvent e) {
            notifyChanged();
        }

        // DocumentListener implementation -------------------------------------
        @Override
        public void insertUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            notifyChanged();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            notifyChanged();
        }

        // PreviewProvider methods -----------------------------------------------------
        @Override
        public JComponent getPreviewComponent() {
            if (previewPane == null) {
                previewPane = new JEditorPane();
                previewPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtOptions.class, "AN_Preview")); //NOI18N
                previewPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtOptions.class, "AD_Preview")); //NOI18N
                previewPane.setEditorKit(CloneableEditorSupport.getEditorKit(FileUtils.PHP_MIME_TYPE));
                previewPane.setEditable(false);
            }
            return previewPane;
        }

        @Override
        public void refreshPreview() {
            JEditorPane pane = (JEditorPane) getPreviewComponent();
            try {
                int rm = previewPrefs.getInt(RIGHT_MARGIN, getDefaultAsInt(RIGHT_MARGIN));
                pane.putClientProperty("TextLimitLine", rm); //NOI18N
            } catch (NumberFormatException e) {
                // Ignore it
            }

            // keep the caret position
            // to avoid being scrolled to the end of the editor
            int caretPosition = pane.getCaretPosition();

            Rectangle visibleRectangle = pane.getVisibleRect();
            pane.setText(previewText);
            pane.setIgnoreRepaint(true);

            final Document doc = pane.getDocument();
            if (doc instanceof BaseDocument) {
                final Reformat reformat = Reformat.get(doc);
                reformat.lock();
                try {
                    ((BaseDocument) doc).runAtomic(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                reformat.reformat(0, doc.getLength());
                            } catch (BadLocationException ble) {
                                LOGGER.log(Level.WARNING, null, ble);
                            }
                        }
                    });
                } finally {
                    reformat.unlock();
                }
                // avoid being set to an invalid position
                caretPosition = Integer.min(caretPosition, doc.getLength());
            } else {
                LOGGER.warning(String.format("Can't format %s; it's not BaseDocument.", doc)); //NOI18N
            }
            pane.setCaretPosition(caretPosition);
            pane.setIgnoreRepaint(false);
            // invoke later because the preview pane is scrolled to the caret position when we change options after we scroll it anywhere
            SwingUtilities.invokeLater(() -> pane.scrollRectToVisible(visibleRectangle));
            pane.repaint(100);

        }

        // PreferencesCustomizer implementation --------------------------------
        @Override
        public JComponent getComponent() {
            return panel;
        }

        @Override
        public String getDisplayName() {
            return panel.getName();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }

        // PreferencesCustomizer.Factory implementation ------------------------
        public static final class Factory implements PreferencesCustomizer.Factory {

            private final String id;
            private final Class<? extends JPanel> panelClass;
            private final String previewText;
            private final String[][] forcedOptions;

            public Factory(String id, Class<? extends JPanel> panelClass, String previewText, String[]... forcedOptions) {
                this.id = id;
                this.panelClass = panelClass;
                this.previewText = previewText;
                this.forcedOptions = forcedOptions;
            }

            @Override
            public PreferencesCustomizer create(Preferences preferences) {
                try {
                    return new CategorySupport(preferences, id, panelClass.getDeclaredConstructor().newInstance(), previewText, forcedOptions);
                } catch (ReflectiveOperationException e) {
                    LOGGER.log(Level.WARNING, "Exception during creating formatter customiezer", e);
                    return null;
                }
            }
        } // End of CategorySupport.Factory class

        // Private methods -----------------------------------------------------
        private void performOperation(Operation operation, JComponent jc, String optionID, Preferences p) {
            switch (operation) {
                case LOAD:
                    loadData(jc, optionID, p);
                    break;
                case STORE:
                    storeData(jc, optionID, p);
                    break;
                case ADD_LISTENERS:
                    addListener(jc);
                    break;
                default:
                    assert false : operation;
            }
        }

        private void scan(Operation what, Preferences p) {
            for (JComponent jc : components) {
                Object o = jc.getClientProperty(OPTION_ID);
                if (o instanceof String) {
                    performOperation(what, jc, (String) o, p);
                } else if (o instanceof String[]) {
                    for (String oid : (String[]) o) {
                        performOperation(what, jc, oid, p);
                    }
                }
            }
        }

        private void scan(Container container, List<JComponent> components) {
            for (Component c : container.getComponents()) {
                if (c instanceof JComponent) {
                    JComponent jc = (JComponent) c;
                    Object o = jc.getClientProperty(OPTION_ID);
                    if (o instanceof String || o instanceof String[]) {
                        components.add(jc);
                    }
                }
                if (c instanceof Container) {
                    scan((Container) c, components);
                }
            }
        }

        /**
         * Very smart method which tries to set the values in the components
         * correctly
         */
        private void loadData(JComponent jc, String optionID, Preferences node) {

            if (jc instanceof JTextField) {
                JTextField field = (JTextField) jc;
                field.setText(node.get(optionID, getDefaultAsString(optionID)));
            } else if (jc instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) jc;
                boolean df = getDefaultAsBoolean(optionID);
                checkBox.setSelected(node.getBoolean(optionID, df));
            } else if (jc instanceof JComboBox) {
                JComboBox cb = (JComboBox) jc;
                String value = node.get(optionID, getDefaultAsString(optionID));
                ComboBoxModel model = createModel(value);
                cb.setModel(model);
                ComboItem item = whichItem(value, model);
                cb.setSelectedItem(item);
            } else if (jc instanceof JRadioButton) {
                JRadioButton radioButton = (JRadioButton) jc;
                boolean df = getDefaultAsBoolean(optionID);
                radioButton.setSelected(node.getBoolean(optionID, df));
            }

        }

        private void storeData(JComponent jc, String optionID, Preferences node) {

            if (jc instanceof JTextField) {
                JTextField field = (JTextField) jc;

                String text = field.getText();

                // XXX test for numbers
                if (isInteger(optionID)) {
                    try {
                        Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }

                // XXX: watch out, tabSize, spacesPerTab, indentSize and expandTabToSpaces
                // fall back on getGlopalXXX() values and not getDefaultAsXXX value,
                // which is why we must not remove them. Proper solution would be to
                // store formatting preferences to MimeLookup and not use NbPreferences.
                // The problem currently is that MimeLookup based Preferences do not support subnodes.
                if (!optionID.equals(TAB_SIZE)
                        && !optionID.equals(SPACES_PER_TAB) && !optionID.equals(INDENT_SIZE)
                        && getDefaultAsString(optionID).equals(text)) {
                    node.remove(optionID);
                } else {
                    node.put(optionID, text);
                }
            } else if (jc instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) jc;
                if (!optionID.equals(EXPAND_TAB_TO_SPACES) && getDefaultAsBoolean(optionID) == checkBox.isSelected()) {
                    node.remove(optionID);
                } else {
                    node.putBoolean(optionID, checkBox.isSelected());
                }
            } else if (jc instanceof JComboBox) {
                JComboBox cb = (JComboBox) jc;
                ComboItem comboItem = ((ComboItem) cb.getSelectedItem());
                String value = comboItem == null ? getDefaultAsString(optionID) : comboItem.value;

                if (getDefaultAsString(optionID).equals(value)) {
                    node.remove(optionID);
                } else {
                    node.put(optionID, value);
                }
            }
        }

        private void addListener(JComponent jc) {
            if (jc instanceof JTextField) {
                JTextField field = (JTextField) jc;
                field.addActionListener(this);
                field.getDocument().addDocumentListener(this);
            } else if (jc instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) jc;
                checkBox.addActionListener(this);
            } else if (jc instanceof JComboBox) {
                JComboBox cb = (JComboBox) jc;
                cb.addActionListener(this);
            }
        }

        private ComboBoxModel createModel(String value) {

            // is it braces placement?
            for (ComboItem comboItem : BRACE_PLACEMENT) {
                if (value.equals(comboItem.value)) {
                    return new DefaultComboBoxModel(BRACE_PLACEMENT);
                }
            }

            // is it wrap
            for (ComboItem comboItem : WRAP) {
                if (value.equals(comboItem.value)) {
                    return new DefaultComboBoxModel(WRAP);
                }
            }

            return null;
        }

        private static ComboItem whichItem(String value, ComboBoxModel model) {

            for (int i = 0; i < model.getSize(); i++) {
                ComboItem item = (ComboItem) model.getElementAt(i);
                if (value.equals(item.value)) {
                    return item;
                }
            }
            return null;
        }

        private static class ComboItem {

            String value;
            String displayName;

            public ComboItem(String value, String key) {
                this.value = value;
                this.displayName = NbBundle.getMessage(FmtOptions.class, key);
            }

            @Override
            public String toString() {
                return displayName;
            }
        }
    }

    public static class PreviewPreferences extends AbstractPreferences {

        private Map<String, Object> map = new HashMap<>();

        public PreviewPreferences() {
            super(null, ""); // NOI18N
        }

        @Override
        protected void putSpi(String key, String value) {
            map.put(key, value);
        }

        @Override
        protected String getSpi(String key) {
            return (String) map.get(key);
        }

        @Override
        protected void removeSpi(String key) {
            map.remove(key);
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            String[] array = new String[map.keySet().size()];
            return map.keySet().toArray(array);
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // read-only, no subnodes
    public static final class ProxyPreferences extends AbstractPreferences {

        private final Preferences[] delegates;

        public ProxyPreferences(Preferences... delegates) {
            super(null, ""); // NOI18N
            this.delegates = delegates;
        }

        @Override
        protected void putSpi(String key, String value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String getSpi(String key) {
            for (Preferences p : delegates) {
                String value = p.get(key, null);
                if (value != null) {
                    return value;
                }
            }
            return null;
        }

        @Override
        protected void removeSpi(String key) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            Set<String> keys = new HashSet<>();
            for (Preferences p : delegates) {
                keys.addAll(Arrays.asList(p.keys()));
            }
            return keys.toArray(new String[0]);
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    } // End of ProxyPreferences class

    public interface CodeStyleProducer {

        CodeStyle create(Preferences preferences);
    }

    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);

        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
}
