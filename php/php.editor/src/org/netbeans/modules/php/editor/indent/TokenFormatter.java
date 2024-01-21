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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.indent.FormatToken.AssignmentAnchorToken;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.spi.lexer.MutableTextInput;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class TokenFormatter {
    protected static final String TEMPLATE_HANDLER_PROPERTY = "code-template-insert-handler";
    private static final String EMPTY_STRING = "";
    private static final Logger LOGGER = Logger.getLogger(TokenFormatter.class.getName());
    // it's for testing
    private static int unitTestCarretPosition = -1;

    public TokenFormatter() {
    }

    protected static void setUnitTestCarretPosition(int unitTestCarretPosition) {
        TokenFormatter.unitTestCarretPosition = unitTestCarretPosition;
    }

    protected static class DocumentOptions {
        public int continualIndentSize;
        public int initialIndent;
        public int indentSize;
        public int indentArrayItems;
        public int margin;
        public int tabSize;
        public boolean expandTabsToSpaces;
        public CodeStyle.BracePlacement classDeclBracePlacement;
        public CodeStyle.BracePlacement anonymousClassBracePlacement;
        public CodeStyle.BracePlacement methodDeclBracePlacement;
        public CodeStyle.BracePlacement ifBracePlacement;
        public CodeStyle.BracePlacement forBracePlacement;
        public CodeStyle.BracePlacement whileBracePlacement;
        public CodeStyle.BracePlacement switchBracePlacement;
        public CodeStyle.BracePlacement matchBracePlacement;
        public CodeStyle.BracePlacement useTraitBodyBracePlacement;
        public CodeStyle.BracePlacement groupUseBracePlacement;
        public CodeStyle.BracePlacement catchBracePlacement;
        public CodeStyle.BracePlacement otherBracePlacement;
        public boolean spaceBeforeClassDeclLeftBrace;
        public boolean spaceBeforeMethodDeclLeftBrace;
        public boolean spaceBeforeIfLeftBrace;
        public boolean spaceBeforeElseLeftBrace;
        public boolean spaceBeforeWhileLeftBrace;
        public boolean spaceBeforeForLeftBrace;
        public boolean spaceBeforeDoLeftBrace;
        public boolean spaceBeforeSwitchLeftBrace;
        public boolean spaceBeforeMatchLeftBrace;
        public boolean spaceBeforeTryLeftBrace;
        public boolean spaceBeforeCatchLeftBrace;
        public boolean spaceBeforeFinallyLeftBrace;
        public boolean spaceBeforeUseTraitBodyLeftBrace;
        public boolean spaceBeforeAnonymousClassParen;
        public boolean spaceBeforeAnonymousFunctionParen;
        public boolean spaceBeforeAttributeDeclParen;
        public boolean spaceBeforeMethodDeclParen;
        public boolean spaceBeforeMethodCallParen;
        public boolean spaceBeforeIfParen;
        public boolean spaceBeforeForParen;
        public boolean spaceBeforeWhileParen;
        public boolean spaceBeforeCatchParen;
        public boolean spaceBeforeSwitchParen;
        public boolean spaceBeforeMatchParen;
        public boolean spaceBeforeArrayDeclParen;
        public boolean spaceBeforeWhile;
        public boolean spaceBeforeElse;
        public boolean spaceBeforeCatch;
        public boolean spaceBeforeFinally;
        public boolean spaceAroundScopeResolutionOp;
        public boolean spaceAroundObjectOp;
        public boolean spaceAroundNullsafeObjectOp;
        public boolean spaceAroundDeclareEqual;
        public boolean spaceAroundUnionTypeSeparator;
        public boolean spaceAroundIntersectionTypeSeparator;
        public boolean spaceAroundStringConcatOp;
        public boolean spaceAroundUnaryOps;
        public boolean spaceAroundBinaryOps;
        public boolean spaceAroundTernaryOps;
        public boolean spaceAroundCoalescingOps;
        public boolean spaceAroundAssignOps;
        public boolean spaceAroundKeyValueOps;
        public boolean spaceWithinArrayDeclParens;
        public boolean spaceWithinAnonymousClassParens;
        public boolean spaceWithinMethodDeclParens;
        public boolean spaceWithinMethodCallParens;
        public boolean spaceWithinIfParens;
        public boolean spaceWithinForParens;
        public boolean spaceWithinWhileParens;
        public boolean spaceWithinSwitchParens;
        public boolean spaceWithinMatchParens;
        public boolean spaceWithinCatchParens;
        public boolean spaceWithinArrayBrackets;
        public boolean spaceWithinAttributeBrackets;
        public boolean spaceWithinAttributeDeclParens;
        public boolean spaceWithinTypeCastParens;
        public boolean spaceBeforeComma;
        public boolean spaceAfterComma;
        public boolean spaceBeforeSemi;
        public boolean spaceAfterSemi;
        public boolean spaceAfterTypeCast;
        public boolean spaceAfterShortTag;
        public boolean spaceBeforeClosePHPTag;
        public boolean spaceBetweenOpenPHPTagAndNamespace;
        public boolean placeElseOnNewLine;
        public boolean placeWhileOnNewLine;
        public boolean placeCatchOnNewLine;
        public boolean placeFinallyOnNewLine;
        public boolean placeNewLineAfterModifiers;
        public int blankLinesBeforeNamespace;
        public int blankLinesAfterNamespace;
        public int blankLinesBeforeUse;
        public int blankLinesBeforeUseTrait;
        public int blankLinesAfterUseTrait;
        public int blankLinesAfterUse;
        public int blankLinesBetweenUseTypes;
        public int blankLinesBeforeClass;
        public int blankLinesBeforeClassEnd;
        public int blankLinesAfterClass;
        public int blankLinesAfterClassHeader;
        public int blankLinesBeforeFields;
        public int blankLinesBetweenFields;
        public boolean blankLinesEOF;
        public boolean blankLinesGroupFields;
        public int blankLinesAfterFields;
        public int blankLinesBeforeFunction;
        public int blankLinesAfterFunction;
        public int blankLinesBeforeFunctionEnd;
        public int blankLinesAfterOpenPHPTag;
        public int blankLinesAfterOpenPHPTagInHTML;
        public int blankLinesBeforeClosePHPTag;
        public int blankLinesMaxPreserved;
        public CodeStyle.WrapStyle wrapGroupUseList;
        public CodeStyle.WrapStyle wrapExtendsImplementsKeyword;
        public CodeStyle.WrapStyle wrapExtendsImplementsList;
        public CodeStyle.WrapStyle wrapMethodParams;
        public CodeStyle.WrapStyle wrapMethodCallArgs;
        public CodeStyle.WrapStyle wrapChainedMethodCalls;
        public CodeStyle.WrapStyle wrapArrayInit;
        public CodeStyle.WrapStyle wrapFor;
        public CodeStyle.WrapStyle wrapForStatement;
        public CodeStyle.WrapStyle wrapIfStatement;
        public CodeStyle.WrapStyle wrapWhileStatement;
        public CodeStyle.WrapStyle wrapDoWhileStatement;
        public CodeStyle.WrapStyle wrapBinaryOps;
        public CodeStyle.WrapStyle wrapTernaryOps;
        public CodeStyle.WrapStyle wrapCoalescingOps;
        public CodeStyle.WrapStyle wrapAssignOps;
        public boolean wrapBlockBrace;
        public boolean wrapGroupUseBraces;
        public boolean wrapStatementsOnTheSameLine;
        public boolean wrapAfterBinOps;
        public boolean wrapAfterAssignOps;
        public boolean wrapMethodParamsAfterLeftParen;
        public boolean wrapMethodParamsRightParen;
        public boolean wrapMethodParamsKeepParenAndBraceOnTheSameLine;
        public boolean wrapMethodCallArgsAfterLeftParen;
        public boolean wrapMethodCallArgsRightParen;
        public boolean wrapForAfterLeftParen;
        public boolean wrapForRightParen;
        public boolean alignMultilineMethodParams;
        public boolean alignMultilineCallArgs;
        public boolean alignMultilineImplements;
        @org.netbeans.api.annotations.common.SuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
        public boolean alignMultilineParenthesized; // not implemented yet
        @org.netbeans.api.annotations.common.SuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
        public boolean alignMultilineBinaryOp; // not implemented yet
        @org.netbeans.api.annotations.common.SuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
        public boolean alignMultilineTernaryOp; // not implemented yet
        @org.netbeans.api.annotations.common.SuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
        public boolean alignMultilineAssignment; // not implemented yet
        @org.netbeans.api.annotations.common.SuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
        public boolean alignMultilineFor; // not implemented yet
        @org.netbeans.api.annotations.common.SuppressWarnings({"URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD"})
        public boolean alignMultilineArrayInit; //not implemented yet
        public boolean groupMultilineAssignment;
        public boolean groupMultilineArrayInit;
        public boolean groupMultilineMatchArmArrow;

        boolean wrapNeverKeepLines = Boolean.getBoolean("nb.php.editor.formatting.never.keep.lines"); // NOI18N

        public DocumentOptions(BaseDocument doc) {
            CodeStyle codeStyle = CodeStyle.get(doc);
            continualIndentSize = codeStyle.getContinuationIndentSize();
            initialIndent = codeStyle.getInitialIndent();
            indentSize = codeStyle.getIndentSize();
            indentArrayItems = codeStyle.getItemsInArrayDeclarationIndentSize();
            margin = codeStyle.getRightMargin();
            tabSize = codeStyle.getTabSize();
            expandTabsToSpaces = codeStyle.expandTabToSpaces();

            classDeclBracePlacement = codeStyle.getClassDeclBracePlacement();
            anonymousClassBracePlacement = codeStyle.getAnonymousClassBracePlacement();
            methodDeclBracePlacement = codeStyle.getMethodDeclBracePlacement();
            ifBracePlacement = codeStyle.getIfBracePlacement();
            forBracePlacement = codeStyle.getForBracePlacement();
            whileBracePlacement = codeStyle.getWhileBracePlacement();
            switchBracePlacement = codeStyle.getSwitchBracePlacement();
            matchBracePlacement = codeStyle.getMatchBracePlacement();
            catchBracePlacement = codeStyle.getCatchBracePlacement();
            useTraitBodyBracePlacement = codeStyle.getUseTraitBodyBracePlacement();
            groupUseBracePlacement = codeStyle.getGroupUseBracePlacement();
            otherBracePlacement = codeStyle.getOtherBracePlacement();

            spaceBeforeClassDeclLeftBrace = codeStyle.spaceBeforeClassDeclLeftBrace();
            spaceBeforeMethodDeclLeftBrace = codeStyle.spaceBeforeMethodDeclLeftBrace();
            spaceBeforeIfLeftBrace = codeStyle.spaceBeforeIfLeftBrace();
            spaceBeforeElseLeftBrace = codeStyle.spaceBeforeElseLeftBrace();
            spaceBeforeWhileLeftBrace = codeStyle.spaceBeforeWhileLeftBrace();
            spaceBeforeForLeftBrace = codeStyle.spaceBeforeForLeftBrace();
            spaceBeforeDoLeftBrace = codeStyle.spaceBeforeDoLeftBrace();
            spaceBeforeSwitchLeftBrace = codeStyle.spaceBeforeSwitchLeftBrace();
            spaceBeforeMatchLeftBrace = codeStyle.spaceBeforeMatchLeftBrace();
            spaceBeforeTryLeftBrace = codeStyle.spaceBeforeTryLeftBrace();
            spaceBeforeCatchLeftBrace = codeStyle.spaceBeforeCatchLeftBrace();
            spaceBeforeFinallyLeftBrace = codeStyle.spaceBeforeFinallyLeftBrace();
            spaceBeforeUseTraitBodyLeftBrace = codeStyle.spaceBeforeUseTraitBodyLeftBrace();

            spaceBeforeAnonymousClassParen = codeStyle.spaceBeforeAnonymousClassParen();
            spaceBeforeAnonymousFunctionParen = codeStyle.spaceBeforeAnonymousFunctionParen();
            spaceBeforeAttributeDeclParen = codeStyle.spaceBeforeAttributeDeclParen();
            spaceBeforeMethodDeclParen = codeStyle.spaceBeforeMethodDeclParen();
            spaceBeforeMethodCallParen = codeStyle.spaceBeforeMethodCallParen();
            spaceBeforeIfParen = codeStyle.spaceBeforeIfParen();
            spaceBeforeForParen = codeStyle.spaceBeforeForParen();
            spaceBeforeWhileParen = codeStyle.spaceBeforeWhileParen();
            spaceBeforeCatchParen = codeStyle.spaceBeforeCatchParen();
            spaceBeforeSwitchParen = codeStyle.spaceBeforeSwitchParen();
            spaceBeforeMatchParen = codeStyle.spaceBeforeMatchParen();
            spaceBeforeArrayDeclParen = codeStyle.spaceBeforeArrayDeclParen();

            spaceBeforeWhile = codeStyle.spaceBeforeWhile();
            spaceBeforeElse = codeStyle.spaceBeforeElse();
            spaceBeforeCatch = codeStyle.spaceBeforeCatch();
            spaceBeforeFinally = codeStyle.spaceBeforeFinally();

            spaceAroundScopeResolutionOp = codeStyle.spaceAroundScopeResolutionOps();
            spaceAroundObjectOp = codeStyle.spaceAroundObjectOps();
            spaceAroundNullsafeObjectOp = codeStyle.spaceAroundNullsafeObjectOps();
            spaceAroundDeclareEqual = codeStyle.spaceAroundDeclareEqual();
            spaceAroundUnionTypeSeparator = codeStyle.spaceAroundUnionTypeSeparator();
            spaceAroundIntersectionTypeSeparator = codeStyle.spaceAroundIntersectionTypeSeparator();
            spaceAroundStringConcatOp = codeStyle.spaceAroundStringConcatOps();
            spaceAroundUnaryOps = codeStyle.spaceAroundUnaryOps();
            spaceAroundBinaryOps = codeStyle.spaceAroundBinaryOps();
            spaceAroundTernaryOps = codeStyle.spaceAroundTernaryOps();
            spaceAroundCoalescingOps = codeStyle.spaceAroundCoalescingOps();
            spaceAroundAssignOps = codeStyle.spaceAroundAssignOps();
            spaceAroundKeyValueOps = codeStyle.spaceAroundKeyValueOps();

            spaceWithinArrayDeclParens = codeStyle.spaceWithinArrayDeclParens();
            spaceWithinAnonymousClassParens = codeStyle.spaceWithinAnonymousClassParens();
            spaceWithinMethodDeclParens = codeStyle.spaceWithinMethodDeclParens();
            spaceWithinMethodCallParens = codeStyle.spaceWithinMethodCallParens();
            spaceWithinIfParens = codeStyle.spaceWithinIfParens();
            spaceWithinForParens = codeStyle.spaceWithinForParens();
            spaceWithinWhileParens = codeStyle.spaceWithinWhileParens();
            spaceWithinSwitchParens = codeStyle.spaceWithinSwitchParens();
            spaceWithinMatchParens = codeStyle.spaceWithinMatchParens();
            spaceWithinCatchParens = codeStyle.spaceWithinCatchParens();
            spaceWithinArrayBrackets = codeStyle.spaceWithinArrayBrackets();
            spaceWithinAttributeBrackets = codeStyle.spaceWithinAttributeBrackets();
            spaceWithinAttributeDeclParens = codeStyle.spaceWithinAttributeDeclParens();
            spaceWithinTypeCastParens = codeStyle.spaceWithinTypeCastParens();

            spaceBeforeComma = codeStyle.spaceBeforeComma();
            spaceAfterComma = codeStyle.spaceAfterComma();
            spaceBeforeSemi = codeStyle.spaceBeforeSemi();
            spaceAfterSemi = codeStyle.spaceAfterSemi();
            spaceAfterTypeCast = codeStyle.spaceAfterTypeCast();
            spaceAfterShortTag = codeStyle.spaceAfterShortPHPTag();
            spaceBeforeClosePHPTag = codeStyle.spaceBeforeClosePHPTag();
            spaceBetweenOpenPHPTagAndNamespace = codeStyle.spaceBetweenOpenPHPTagAndNamespace();

            placeElseOnNewLine = codeStyle.placeElseOnNewLine();
            placeWhileOnNewLine = codeStyle.placeWhileOnNewLine();
            placeCatchOnNewLine = codeStyle.placeCatchOnNewLine();
            placeFinallyOnNewLine = codeStyle.placeFinallyOnNewLine();
            placeNewLineAfterModifiers = codeStyle.placeNewLineAfterModifiers();

            blankLinesBeforeNamespace = codeStyle.getBlankLinesBeforeNamespace();
            blankLinesAfterNamespace = codeStyle.getBlankLinesAfterNamespace();
            blankLinesBeforeUse = codeStyle.getBlankLinesBeforeUse();
            blankLinesBeforeUseTrait = codeStyle.getBlankLinesBeforeUseTrait();
            blankLinesAfterUseTrait = codeStyle.getBlankLinesAfterUseTrait();
            blankLinesAfterUse = codeStyle.getBlankLinesAfterUse();
            blankLinesBetweenUseTypes = codeStyle.getBlankLinesBetweenUseTypes();
            blankLinesBeforeClass = codeStyle.getBlankLinesBeforeClass();
            blankLinesBeforeClassEnd = codeStyle.getBlankLinesBeforeClassEnd();
            blankLinesAfterClass = codeStyle.getBlankLinesAfterClass();
            blankLinesAfterClassHeader = codeStyle.getBlankLinesAfterClassHeader();
            blankLinesBeforeFields = codeStyle.getBlankLinesBeforeFields();
            blankLinesBetweenFields = codeStyle.getBlankLinesBetweenFields();
            blankLinesEOF = codeStyle.getBlankLinesEOF();
            blankLinesGroupFields = codeStyle.getBlankLinesGroupFieldsWithoutDoc();
            blankLinesAfterFields = codeStyle.getBlankLinesAfterFields();
            blankLinesBeforeFunction = codeStyle.getBlankLinesBeforeFunction();
            blankLinesAfterFunction = codeStyle.getBlankLinesAfterFunction();
            blankLinesBeforeFunctionEnd = codeStyle.getBlankLinesBeforeFunctionEnd();
            blankLinesAfterOpenPHPTag = codeStyle.getBlankLinesAfterOpenPHPTag();
            blankLinesAfterOpenPHPTagInHTML = codeStyle.getBlankLinesAfterOpenPHPTagInHTML();
            blankLinesBeforeClosePHPTag = codeStyle.getBlankLinesBeforeClosePHPTag();
            blankLinesMaxPreserved = codeStyle.getBlankLinesMaxPreserved();

            wrapGroupUseList = codeStyle.wrapGroupUseList();
            wrapExtendsImplementsKeyword = codeStyle.wrapExtendsImplementsKeyword();
            wrapExtendsImplementsList = codeStyle.wrapExtendsImplementsList();
            wrapMethodParams = codeStyle.wrapMethodParams();
            wrapMethodParamsAfterLeftParen = codeStyle.wrapMethodParamsAfterLeftParen();
            wrapMethodParamsRightParen = codeStyle.wrapMethodParamsRightParen();
            wrapMethodParamsKeepParenAndBraceOnTheSameLine = codeStyle.wrapMethodParamsKeepParenAndBraceOnTheSameLine();
            wrapMethodCallArgs = codeStyle.wrapMethodCallArgs();
            wrapMethodCallArgsAfterLeftParen = codeStyle.wrapMethodCallArgsAfterLeftParen();
            wrapMethodCallArgsRightParen = codeStyle.wrapMethodCallArgsRightParen();
            wrapChainedMethodCalls = codeStyle.wrapChainedMethodCalls();
            wrapArrayInit = codeStyle.wrapArrayInit();
            wrapFor = codeStyle.wrapFor();
            wrapForAfterLeftParen = codeStyle.wrapForAfterLeftParen();
            wrapForRightParen = codeStyle.wrapForRightParen();
            wrapForStatement = codeStyle.wrapForStatement();
            wrapIfStatement = codeStyle.wrapIfStatement();
            wrapWhileStatement = codeStyle.wrapWhileStatement();
            wrapDoWhileStatement = codeStyle.wrapDoWhileStatement();
            wrapBinaryOps = codeStyle.wrapBinaryOps();
            wrapTernaryOps = codeStyle.wrapTernaryOps();
            wrapCoalescingOps = codeStyle.wrapCoalescingOps();
            wrapAssignOps = codeStyle.wrapAssignOps();
            wrapBlockBrace = codeStyle.wrapBlockBrace();
            wrapGroupUseBraces = codeStyle.wrapGroupUseBraces();
            wrapStatementsOnTheSameLine = codeStyle.wrapStatementsOnTheSameLine();
            wrapAfterBinOps = codeStyle.wrapAfterBinOps();
            wrapAfterAssignOps = codeStyle.wrapAfterAssignOps();

            alignMultilineMethodParams = codeStyle.alignMultilineMethodParams();
            alignMultilineCallArgs = codeStyle.alignMultilineCallArgs();
            alignMultilineImplements = codeStyle.alignMultilineImplements();
            alignMultilineParenthesized = codeStyle.alignMultilineParenthesized();
            alignMultilineBinaryOp = codeStyle.alignMultilineBinaryOp();
            alignMultilineTernaryOp = codeStyle.alignMultilineTernaryOp();
            alignMultilineAssignment = codeStyle.alignMultilineAssignment();
            alignMultilineFor = codeStyle.alignMultilineFor();
            alignMultilineArrayInit = codeStyle.alignMultilineArrayInit();
            groupMultilineArrayInit = codeStyle.groupMultilineArrayInit();
            groupMultilineMatchArmArrow = codeStyle.groupMultilineMatchArmArrow();
            groupMultilineAssignment = codeStyle.groupMultilineAssignment();
        }

    }

    /**
     *
     * @param chs
     * @return number of new lines in the inputunitTestPane != null ?
     * unitTestPane.getCaretPosition()
     */
    private int countOfNewLines(CharSequence chs) {
        int count = 0;
        if (chs != null) {
            for (int i = 0; i < chs.length(); i++) {
                if (chs.charAt(i) == '\n') { // NOI18N
                    count++;
                }
            }
        }
        return count;
    }

    public void reformat(final Context formatContext, ParserResult info) {
        final BaseDocument doc = (BaseDocument) formatContext.document();
        final PHPParseResult phpParseResult = ((PHPParseResult) info);
        final DocumentOptions docOptions = new DocumentOptions(doc);

        doc.runAtomic(new Runnable() {
            @Override
            public void run() {
                final AtomicLong start = new AtomicLong(System.currentTimeMillis());
                final boolean templateEdit = GsfUtilities.isCodeTemplateEditing(doc);
                JTextComponent lastFocusedComponent = templateEdit ? EditorRegistry.lastFocusedComponent() : null;
                final int caretOffset = lastFocusedComponent != null
                        ? lastFocusedComponent.getCaretPosition()
                        : unitTestCarretPosition == -1 ? 0 : unitTestCarretPosition;
                FormatVisitor fv = new FormatVisitor(doc, docOptions, caretOffset, formatContext.startOffset(), formatContext.endOffset());
                phpParseResult.getProgram().accept(fv);
                final List<FormatToken> formatTokens = fv.getFormatTokens();

                if (LOGGER.isLoggable(Level.FINE)) {
                    long end = System.currentTimeMillis();
                    LOGGER.log(Level.FINE, "Creating formating stream took: {0} ms", (end - start.get()));
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, 0);
                    if (ts == null) {
                        return;
                    }
                    LOGGER.log(Level.FINE, "Tokens in TS: {0}", ts.tokenCount());
                    LOGGER.log(Level.FINE, "Format tokens: {0}", formatTokens.size());
                }
                MutableTextInput mti = (MutableTextInput) doc.getProperty(MutableTextInput.class);
                try {
                    mti.tokenHierarchyControl().setActive(false);
                    start.set(System.currentTimeMillis());
                    int delta = 0;
                    // keeps the indentation of the php code. In a file, where is php
                    // mixed together with html it reflects also the "html" shift.
                    // It can be very different in html code.
                    int indent = docOptions.initialIndent;
                    // reflect only the php indentation itself. It's mainly used for
                    // finding position of open php tag in a html code.
                    int lastPHPIndent = 0;
                    boolean caretInTemplateSolved = false;
                    int htmlIndent = -1;
                    int index = 0;
                    int newLines;
                    int countSpaces;
                    int extraLines;
                    int column = 0;
                    int indentOfOpenTag = 0;
                    int methodCallParenBalance = 0; // GH-6714 for nested arguments
                    final Deque<Integer> lastBracedBlockIndent = new ArrayDeque<>();
                    final Deque<FormatToken.AnchorToken> lastAnchorTokenStack = new ArrayDeque<>(); // GH-6714 for nested arguments

                    FormatToken formatToken;
                    String newText = null;
                    String oldText;
                    int changeOffset = -1;
                    int deltaForLastMoveBeforeLineComment = 0;
                    FormatToken.AnchorToken lastAnchor = null;
                    boolean hasNewLineBeforeRightParen = false;
                    while (index < formatTokens.size()) {
                        formatToken = formatTokens.get(index);
                        oldText = null; //NOI18N
                        if (formatToken.isWhitespace()) {
                            newLines = -1;
                            countSpaces = 0;

                            boolean wasARule = false;
                            boolean indentLine = false;
                            boolean indentRule = false;
                            boolean afterSemi = false;
                            boolean wsBetweenBraces = false;
                            CodeStyle.BracePlacement lastBracePlacement = CodeStyle.BracePlacement.SAME_LINE;

                            changeOffset = formatToken.getOffset();

                            while (index < formatTokens.size() && (formatToken.isWhitespace()
                                    || formatToken.getId() == FormatToken.Kind.INDENT
                                    || formatToken.getId() == FormatToken.Kind.ANCHOR)) {
                                if (oldText == null && formatToken.getOldText() != null) {
                                    oldText = formatToken.getOldText();
                                }
                                if (formatToken.getId() != FormatToken.Kind.INDENT
                                        && formatToken.getId() != FormatToken.Kind.WHITESPACE_INDENT
                                        && formatToken.getId() != FormatToken.Kind.ANCHOR
                                        && formatToken.getId() != FormatToken.Kind.WHITESPACE) {
                                    wasARule = true;
                                }
                                switch (formatToken.getId()) {
                                    case WHITESPACE:
                                        break;
                                    case WHITESPACE_BEFORE_CLASS_LEFT_BRACE:
                                        indentRule = true;
                                        Whitespace ws = countWhiteSpaceBeforeLeftBrace(
                                                docOptions.classDeclBracePlacement,
                                                docOptions.spaceBeforeClassDeclLeftBrace,
                                                oldText,
                                                indent,
                                                peekLastBracedIndent(lastBracedBlockIndent));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_ANONYMOUS_CLASS_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.anonymousClassBracePlacement,
                                                docOptions.spaceBeforeClassDeclLeftBrace, // use the same option as class decl
                                                oldText,
                                                indent,
                                                peekLastBracedIndent(lastBracedBlockIndent));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_FUNCTION_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(
                                                docOptions.methodDeclBracePlacement,
                                                docOptions.spaceBeforeMethodDeclLeftBrace,
                                                oldText,
                                                indent,
                                                peekLastBracedIndent(lastBracedBlockIndent));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        // NETBEANS-3391
                                        if (hasNewLineBeforeRightParen
                                                && docOptions.wrapMethodParamsKeepParenAndBraceOnTheSameLine) {
                                            newLines = 0;
                                            countSpaces = docOptions.spaceBeforeMethodDeclLeftBrace ? 1 : 0;
                                        }
                                        hasNewLineBeforeRightParen = false;
                                        break;
                                    case WHITESPACE_BEFORE_IF_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(
                                                docOptions.ifBracePlacement,
                                                docOptions.spaceBeforeIfLeftBrace,
                                                oldText,
                                                indent,
                                                0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_ELSE_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.ifBracePlacement, docOptions.spaceBeforeElseLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.forBracePlacement, docOptions.spaceBeforeForLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.whileBracePlacement, docOptions.spaceBeforeWhileLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_DO_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.whileBracePlacement, docOptions.spaceBeforeDoLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_SWITCH_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.switchBracePlacement, docOptions.spaceBeforeSwitchLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_MATCH_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.matchBracePlacement, docOptions.spaceBeforeMatchLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_TRY_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.catchBracePlacement, docOptions.spaceBeforeTryLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.catchBracePlacement, docOptions.spaceBeforeCatchLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_FINALLY_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.catchBracePlacement, docOptions.spaceBeforeFinallyLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.useTraitBodyBracePlacement, docOptions.spaceBeforeUseTraitBodyLeftBrace, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_GROUP_USE_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.groupUseBracePlacement, false, oldText, indent, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_AFTER_GROUP_USE_LEFT_BRACE:
                                        indentRule = docOptions.wrapGroupUseBraces;
                                        if (docOptions.wrapGroupUseBraces) {
                                            newLines = 1;
                                            countSpaces = indent;
                                        }
                                        if (templateEdit) {
                                            if (oldText != null) {
                                                ws = countExistingWS(oldText);
                                                newLines = ws.lines;
                                                countSpaces = ws.spaces;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_OTHER_LEFT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeLeftBrace(docOptions.otherBracePlacement, docOptions.spaceBeforeTryLeftBrace, oldText, indent, 0, isAfterLineComment(formatTokens, index));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_AFTER_OTHER_LEFT_BRACE:
                                        indentRule = docOptions.wrapBlockBrace;
                                        int wrap = docOptions.wrapBlockBrace ? 1 : 0;
                                        newLines = countLinesAfter(formatTokens, index);
                                        newLines = wrap > newLines ? wrap : newLines;
                                        countSpaces = newLines > 0 ? indent : 1;
                                        break;
                                    case WHITESPACE_BEFORE_IF_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.ifBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        lastBracePlacement = docOptions.ifBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.forBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        lastBracePlacement = docOptions.forBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.whileBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        lastBracePlacement = docOptions.whileBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_SWITCH_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.switchBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        lastBracePlacement = docOptions.switchBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_MATCH_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.matchBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        lastBracePlacement = docOptions.matchBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.catchBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        lastBracePlacement = docOptions.catchBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_USE_TRAIT_BODY_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.useTraitBodyBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        lastBracePlacement = docOptions.useTraitBodyBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_GROUP_USE_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0
                                                ? true
                                                : docOptions.wrapGroupUseBraces;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.groupUseBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, popLastBracedIndent(lastBracedBlockIndent));
                                        indentLine = indentRule;
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 0;
                                        lastBracePlacement = docOptions.groupUseBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_OTHER_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(docOptions.otherBracePlacement, newLines, 0, indent, formatTokens, index - 1, oldText, 0);
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        lastBracePlacement = docOptions.otherBracePlacement;
                                        break;
                                    case WHITESPACE_BETWEEN_OPEN_CLOSE_BRACES:
                                        wsBetweenBraces = true;
                                        break;
                                    case WHITESPACE_BEFORE_CLASS:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeClass + 1 > newLines ? docOptions.blankLinesBeforeClass + 1 : newLines;
                                        countSpaces = indent;
                                        lastBracedBlockIndent.push(countLastBracedBlockIndent(indent, oldText));
                                        break;
                                    case WHITESPACE_AFTER_CLASS_LEFT_BRACE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterClassHeader + 1 > newLines ? docOptions.blankLinesAfterClassHeader + 1 : newLines;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_AFTER_ANONYMOUS_CLASS_LEFT_BRACE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterClassHeader + 1;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_AFTER_CLASS:
                                        indentRule = true;
                                        // If there is some another visible token after this one, add an extra line,
                                        // because that will be the line, where that another token will start.
                                        // So "docOptions.blankLinesAfterClass" is the number of real blank lines between elements
                                        // and then one extra, where the next token will start
                                        extraLines = isPenultimateTokenBeforeWhitespace(index, formatTokens) ? 0 : 1;
                                        newLines = docOptions.blankLinesAfterClass + extraLines > newLines ? docOptions.blankLinesAfterClass + extraLines : newLines;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_AFTER_ANONYMOUS_CLASS:
                                        indentRule = true;
                                        newLines = 0;
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_BEFORE_CLASS_RIGHT_BRACE:
                                        indentRule = true;
                                        ws = countWhiteSpaceBeforeRightBrace(
                                                docOptions.classDeclBracePlacement,
                                                docOptions.blankLinesBeforeClassEnd + 1, // GH-46111 ignore existing newLines to prioritize this option
                                                docOptions.blankLinesBeforeClassEnd,
                                                indent,
                                                formatTokens,
                                                index - 1,
                                                oldText,
                                                popLastBracedIndent(lastBracedBlockIndent));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        lastBracePlacement = docOptions.classDeclBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_ANONYMOUS_CLASS_RIGHT_BRACE:
                                        indentRule = true;
                                        if (docOptions.anonymousClassBracePlacement == CodeStyle.BracePlacement.PRESERVE_EXISTING) {
                                            ws = countWhiteSpaceForPreserveExistingBracePlacement(oldText, popLastBracedIndent(lastBracedBlockIndent));
                                        } else {
                                            int lines = docOptions.blankLinesBeforeClassEnd + 1;
                                            int spaces = docOptions.anonymousClassBracePlacement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? indent + docOptions.indentSize : indent;
                                            ws = new Whitespace(lines, spaces);
                                        }
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        lastBracePlacement = docOptions.anonymousClassBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_FUNCTION:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeFunction + 1 > newLines ? docOptions.blankLinesBeforeFunction + 1 : newLines;
                                        countSpaces = indent;
                                        lastBracedBlockIndent.push(countLastBracedBlockIndent(indent, oldText));
                                        break;
                                    case WHITESPACE_AFTER_FUNCTION:
                                        // If there is some another visible token after this one, add an extra line,
                                        // because that will be the line, where that another token will start.
                                        // So "docOptions.blankLinesAfterFunction" is the number of real blank lines between elements
                                        // and then one extra, where the next token will start
                                        extraLines = isPenultimateTokenBeforeWhitespace(index, formatTokens) ? 0 : 1;
                                        newLines = docOptions.blankLinesAfterFunction + extraLines > newLines ? docOptions.blankLinesAfterFunction + extraLines : newLines;
                                        break;
                                    case WHITESPACE_BEFORE_FUNCTION_RIGHT_BRACE:
                                        indentRule = oldText != null && countOfNewLines(oldText) > 0 ? true : docOptions.wrapBlockBrace;
                                        indentLine = indentRule;
                                        ws = countWhiteSpaceBeforeRightBrace(
                                                docOptions.methodDeclBracePlacement,
                                                newLines,
                                                docOptions.blankLinesBeforeFunctionEnd,
                                                indent,
                                                formatTokens,
                                                index - 1,
                                                oldText,
                                                popLastBracedIndent(lastBracedBlockIndent));
                                        newLines = ws.lines;
                                        countSpaces = indentRule ? ws.spaces : 1;
                                        lastBracePlacement = docOptions.methodDeclBracePlacement;
                                        break;
                                    case WHITESPACE_BEFORE_FIELDS:
                                        newLines = docOptions.blankLinesBeforeFields + 1 > newLines ? docOptions.blankLinesBeforeFields + 1 : newLines;
                                        indentRule = true;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_AFTER_FIELDS:
                                        newLines = docOptions.blankLinesAfterFields + 1 > newLines ? docOptions.blankLinesAfterFields + 1 : newLines;
                                        indentRule = true;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_BETWEEN_FIELDS:
                                        indentRule = true;
                                        if (docOptions.blankLinesGroupFields && !isBeforePHPDocOrAttribute(formatTokens, index)) {
                                            newLines = 1;
                                        } else {
                                            newLines = docOptions.blankLinesBetweenFields + 1 > newLines ? docOptions.blankLinesBetweenFields + 1 : newLines;
                                        }
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_BEFORE_NAMESPACE:
                                        indentRule = true;
                                        if (docOptions.blankLinesBeforeNamespace != 0 && docOptions.blankLinesBeforeNamespace + 1 > newLines) {
                                            newLines = docOptions.blankLinesBeforeNamespace + 1;
                                            countSpaces = indent;
                                        } else {
                                            if (newLines == 0) {
                                                if (docOptions.spaceBetweenOpenPHPTagAndNamespace) {
                                                    countSpaces = 1; // one space before OPEN_TAG and NS_DECLARATION - probably in one line  
                                                } else {
                                                    // for PSR-2
                                                    newLines = 1;
                                                    countSpaces = indent;
                                                }
                                            }
                                            countSpaces = Math.max(indent, countSpaces);
                                        }
                                        break;
                                    case WHITESPACE_AFTER_NAMESPACE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterNamespace + 1 > newLines ? docOptions.blankLinesAfterNamespace + 1 : newLines;
                                        break;
                                    case WHITESPACE_BEFORE_USE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeUse + 1 > newLines ? docOptions.blankLinesBeforeUse + 1 : newLines;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_BETWEEN_USE:
                                        indentRule = true;
                                        newLines = 1;
                                        countSpaces = indent;
                                        break;
                                    case WHITESPACE_BETWEEN_USE_TYPES:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBetweenUseTypes + 1 > newLines ? docOptions.blankLinesBetweenUseTypes + 1 : newLines;
                                        break;
                                    case WHITESPACE_AFTER_USE:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterUse + 1 > newLines ? docOptions.blankLinesAfterUse + 1 : newLines;
                                        break;
                                    case WHITESPACE_BEFORE_USES_PART:
                                        indentRule = true;
                                        if (formatTokens.get(index - 1).getId() == FormatToken.Kind.ANCHOR) {
                                            newLines = 0;
                                            countSpaces = 1;
                                        } else {
                                            newLines = 1;
                                            countSpaces = lastAnchor.getAnchorColumn();
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_USE_TRAIT:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesBeforeUseTrait + 1;
                                        break;
                                    case WHITESPACE_AFTER_USE_TRAIT:
                                        indentRule = true;
                                        newLines = docOptions.blankLinesAfterUseTrait + 1;
                                        break;
                                    case WHITESPACE_BEFORE_USE_TRAIT_PART:
                                        indentRule = true;
                                        if (formatTokens.get(index - 1).getId() == FormatToken.Kind.ANCHOR) {
                                            newLines = 0;
                                            countSpaces = 1;
                                        } else {
                                            newLines = 1;
                                            countSpaces = lastAnchor.getAnchorColumn();
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_EXTENDS_IMPLEMENTS:
                                        indentRule = true;
                                        switch (docOptions.wrapExtendsImplementsKeyword) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = docOptions.continualIndentSize;
                                                break;
                                            case WRAP_NEVER:
                                                newLines = 0;
                                                countSpaces = 1;
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.continualIndentSize;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                            default:
                                                assert false : docOptions.wrapExtendsImplementsKeyword;
                                        }
                                        break;
                                    case WHITESPACE_IN_INTERFACE_LIST:
                                        indentRule = true;
                                        switch (docOptions.wrapExtendsImplementsList) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = docOptions.alignMultilineImplements ? lastAnchor.getAnchorColumn() : docOptions.continualIndentSize;
                                                break;
                                            case WRAP_NEVER:
                                                newLines = 0;
                                                countSpaces = 1;
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countUnbreakableTextAfter(formatTokens, index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.continualIndentSize;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                            default:
                                                assert false : docOptions.wrapExtendsImplementsList;
                                        }
                                        break;
                                    case WHITESPACE_IN_PARAMETER_LIST:
                                        indentRule = true;
                                        switch (docOptions.wrapMethodParams) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = docOptions.alignMultilineMethodParams ? lastAnchor.getAnchorColumn() : indent;
                                                break;
                                            case WRAP_NEVER:
                                                // for keeping the same line
                                                int countOfNewLines = countOfNewLines(oldText);
                                                if (isAfterLineComment(formatTokens, index)
                                                        || (!docOptions.wrapNeverKeepLines && countOfNewLines > 0)) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineMethodParams ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                                }
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineMethodParams ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                            default:
                                                assert false : docOptions.wrapMethodParams;
                                        }
                                        break;
                                    case WHITESPACE_IN_ARGUMENT_LIST:
                                        indentRule = true;
                                        switch (docOptions.wrapMethodCallArgs) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = docOptions.alignMultilineCallArgs ? lastAnchor.getAnchorColumn() : indent;
                                                break;
                                            case WRAP_NEVER:
                                                // for keeping the same line
                                                int countOfNewLines = countOfNewLines(oldText);
                                                if (isAfterLineComment(formatTokens, index)
                                                        || (!docOptions.wrapNeverKeepLines && countOfNewLines > 0)) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineCallArgs ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                                }
                                                break;
                                            case WRAP_IF_LONG:
                                                if (isAfterLineComment(formatTokens, index)
                                                        || column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineCallArgs ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                            default:
                                                assert false : docOptions.wrapMethodCallArgs;
                                        }
                                        break;
                                    case WHITESPACE_IN_ARRAY_ELEMENT_LIST:
                                        switch (docOptions.wrapArrayInit) {
                                            case WRAP_ALWAYS:
                                                indentRule = true;
                                                newLines = 1;
                                                countSpaces = docOptions.alignMultilineArrayInit ? lastAnchor.getAnchorColumn() : indent;
                                                break;
                                            case WRAP_NEVER:
                                                if (isAfterLineComment(formatTokens, index)) {
                                                    indentRule = true;
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineArrayInit ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                                }
                                                break;
                                            case WRAP_IF_LONG:
                                                if (isAfterLineComment(formatTokens, index)
                                                        || column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                    indentRule = true;
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineArrayInit ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                                }
                                                break;
                                            default:
                                                assert false : docOptions.wrapArrayInit;
                                        }
                                        break;
                                    case WHITESPACE_IN_GROUP_USE_LIST:
                                        indentRule = true;
                                        if (formatToken.getOffset() <= formatContext.startOffset()) {
                                            // #259031 keep existing whitespaces
                                            // if template is inserted (using CC) or some characters are selected
                                            if (oldText != null) {
                                                ws = countExistingWS(oldText);
                                                newLines = ws.lines;
                                                countSpaces = ws.spaces;
                                            } else {
                                                newLines = 0;
                                                countSpaces = 0;
                                            }
                                        } else {
                                            switch (docOptions.wrapGroupUseList) {
                                                case WRAP_ALWAYS:
                                                    newLines = 1;
                                                    countSpaces = indent;
                                                    break;
                                                case WRAP_NEVER:
                                                    if (isAfterLineComment(formatTokens, index)) {
                                                        newLines = 1;
                                                        countSpaces = indent;
                                                    } else {
                                                        newLines = 0;
                                                        countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                                    }
                                                    break;
                                                case WRAP_IF_LONG:
                                                    if (isAfterLineComment(formatTokens, index)
                                                            || column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                        newLines = 1;
                                                        countSpaces = indent;
                                                    } else {
                                                        newLines = 0;
                                                        countSpaces = 1;
                                                    }
                                                    break;
                                                default:
                                                    assert false : docOptions.wrapGroupUseList;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_AROUND_SCOPE_RESOLUTION_OP:
                                        countSpaces = docOptions.spaceAroundScopeResolutionOp ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_OBJECT_OP:
                                        countSpaces = docOptions.spaceAroundObjectOp ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_NULLSAFE_OBJECT_OP:
                                        countSpaces = docOptions.spaceAroundNullsafeObjectOp ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_DECLARE_EQUAL:
                                        countSpaces = docOptions.spaceAroundDeclareEqual ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_UNION_TYPE_SEPARATOR:
                                        countSpaces = docOptions.spaceAroundUnionTypeSeparator ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_INTERSECTION_TYPE_SEPARATOR:
                                        countSpaces = docOptions.spaceAroundIntersectionTypeSeparator ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_CONCAT_OP:
                                        countSpaces = docOptions.spaceAroundStringConcatOp ? 1 : 0;
                                        break;
                                    case WHITESPACE_AFTER_KEYWORD:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_AROUND_UNARY_OP:
                                        countSpaces = docOptions.spaceAroundUnaryOps ? 1 : countSpaces;
                                        break;
                                    case WHITESPACE_AROUND_TEXTUAL_OP:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_BEFORE_BINARY_OP:
                                        if (docOptions.wrapAfterBinOps) {
                                            countSpaces = docOptions.spaceAroundBinaryOps ? 1 : 0;
                                        } else {
                                            indentRule = true;
                                            switch (docOptions.wrapBinaryOps) {
                                                case WRAP_ALWAYS:
                                                    newLines = 1;
                                                    countSpaces = indent;
                                                    break;
                                                case WRAP_NEVER:
                                                    if (isAfterLineComment(formatTokens, index)) {
                                                        indentRule = true;
                                                        newLines = 1;
                                                        countSpaces = indent;
                                                    } else {
                                                        newLines = 0;
                                                        countSpaces = docOptions.spaceAroundBinaryOps ? 1 : 0;
                                                    }
                                                    break;
                                                case WRAP_IF_LONG:
                                                    if (column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                        newLines = 1;
                                                        countSpaces = indent;
                                                    } else {
                                                        if (isAfterLineComment(formatTokens, index)) {
                                                            indentRule = true;
                                                            newLines = 1;
                                                            countSpaces = indent;
                                                        } else {
                                                            newLines = 0;
                                                            countSpaces = docOptions.spaceAroundBinaryOps ? 1 : 0;
                                                        }
                                                    }
                                                    break;
                                                default:
                                                    assert false : docOptions.wrapBinaryOps;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_AFTER_BINARY_OP:
                                        if (docOptions.wrapAfterBinOps) {
                                            indentRule = true;
                                            switch (docOptions.wrapBinaryOps) {
                                                case WRAP_ALWAYS:
                                                    newLines = 1;
                                                    countSpaces = indent;
                                                    break;
                                                case WRAP_NEVER:
                                                    newLines = 0;
                                                    countSpaces = docOptions.spaceAroundBinaryOps ? 1 : 0;
                                                    break;
                                                case WRAP_IF_LONG:
                                                    if (column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                        newLines = 1;
                                                        countSpaces = indent;
                                                    } else {
                                                        newLines = 0;
                                                        countSpaces = 1;
                                                    }
                                                    break;
                                                default:
                                                    assert false : docOptions.wrapBinaryOps;
                                            }
                                        } else {
                                            if (isAfterLineComment(formatTokens, index)) {
                                                indentRule = true;
                                                newLines = 1;
                                                countSpaces = indent;
                                            } else {
                                                newLines = 0;
                                                countSpaces = docOptions.spaceAroundBinaryOps ? 1 : 0;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_AROUND_TERNARY_OP:
                                        countSpaces = docOptions.spaceAroundTernaryOps ? 1 : 0;
                                        break;
                                    case WHITESPACE_AROUND_COALESCING_OP:
                                        countSpaces = docOptions.spaceAroundCoalescingOps ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_SHORT_TERNARY_OP:
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_BEFORE_ASSIGN_OP:
                                        indentRule = true;
                                        countSpaces = 0;
                                        boolean addSpaceBeforeAssign = true;
                                        if (index > 0 && docOptions.groupMultilineAssignment
                                                && formatTokens.get(index - 1).getId() == FormatToken.Kind.ASSIGNMENT_ANCHOR) {
                                            FormatToken.AssignmentAnchorToken aaToken = (FormatToken.AssignmentAnchorToken) formatTokens.get(index - 1);
                                            // space of options is added if the token is grouped and tab is not expanded
                                            countSpaces = new SpacesCounter(docOptions).count(aaToken);
                                            addSpaceBeforeAssign = addSpaceAroundAssignment(aaToken, docOptions);
                                        }
                                        if (addSpaceBeforeAssign) {
                                            countSpaces += (docOptions.spaceAroundAssignOps ? 1 : 0);
                                        }
                                        newLines = 0;
                                        if (!docOptions.wrapAfterAssignOps) {
                                            switch (docOptions.wrapAssignOps) {
                                                case WRAP_ALWAYS:
                                                    newLines = 1;
                                                    countSpaces = indent;
                                                    break;
                                                case WRAP_NEVER:
                                                    break;
                                                case WRAP_IF_LONG:
                                                    if (column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                        newLines = 1;
                                                        countSpaces = indent;
                                                    }
                                                    break;
                                                default:
                                                    assert false : docOptions.wrapAssignOps;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_AFTER_ASSIGN_OP:
                                        indentRule = true;
                                        countSpaces = 0;
                                        boolean addSpaceAfterAssign = true;
                                        if (index > 0 && docOptions.groupMultilineAssignment
                                                && formatTokens.get(index - 1).getId() == FormatToken.Kind.ASSIGNMENT_ANCHOR) {
                                            FormatToken.AssignmentAnchorToken aaToken = (FormatToken.AssignmentAnchorToken) formatTokens.get(index - 1);
                                            // space of options is added if the token is grouped and tab is not expanded
                                            countSpaces = new SpacesCounter(docOptions).count(aaToken);
                                            addSpaceAfterAssign = addSpaceAroundAssignment(aaToken, docOptions);
                                        }
                                        if (addSpaceAfterAssign) {
                                            countSpaces += (docOptions.spaceAroundAssignOps ? 1 : 0);
                                        }
                                        newLines = 0;
                                        if (docOptions.wrapAfterAssignOps) {
                                            switch (docOptions.wrapAssignOps) {
                                                case WRAP_ALWAYS:
                                                    newLines = 1;
                                                    countSpaces = indent;
                                                    break;
                                                case WRAP_NEVER:
                                                    break;
                                                case WRAP_IF_LONG:
                                                    if (column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                        newLines = 1;
                                                        countSpaces = indent;
                                                    }
                                                    break;
                                                default:
                                                    assert false : docOptions.wrapAssignOps;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_AROUND_KEY_VALUE_OP:
                                        countSpaces = 0;
                                        FormatToken lastToken = null;
                                        if (index > 0) {
                                            lastToken = formatTokens.get(index - 1);
                                        }
                                        boolean addSpaceAroundKeyValue = true;
                                        if (lastToken != null && lastToken.getId() == FormatToken.Kind.ASSIGNMENT_ANCHOR) {
                                            AssignmentAnchorToken anchorToken = (AssignmentAnchorToken) lastToken;
                                            if (anchorToken.getType() == AssignmentAnchorToken.Type.ARRAY && docOptions.groupMultilineArrayInit) {
                                                if (docOptions.wrapArrayInit == CodeStyle.WrapStyle.WRAP_ALWAYS || anchorToken.isMultilined()) {
                                                    // space of options is added if the token is grouped and tab is not expanded
                                                    countSpaces = new SpacesCounter(docOptions).count(anchorToken);
                                                    addSpaceAroundKeyValue = addSpaceAroundAssignment(anchorToken, docOptions);
                                                }
                                            } else if (anchorToken.getType() == AssignmentAnchorToken.Type.MATCH_ARM && docOptions.groupMultilineMatchArmArrow) {
                                                if (anchorToken.isMultilined()) {
                                                    // space of options is added if the token is grouped and tab is not expanded
                                                    countSpaces = new SpacesCounter(docOptions).count(anchorToken);
                                                    addSpaceAroundKeyValue = addSpaceAroundAssignment(anchorToken, docOptions);
                                                }
                                            }
                                        }
                                        if (addSpaceAroundKeyValue) {
                                            countSpaces += docOptions.spaceAroundKeyValueOps ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_ANONYMOUS_CLASS_PAREN:
                                        countSpaces = docOptions.spaceBeforeAnonymousClassParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_ANONYMOUS_FUNCTION_PAREN:
                                        countSpaces = docOptions.spaceBeforeAnonymousFunctionParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_ATTRIBUTE_DEC_PAREN:
                                        countSpaces = docOptions.spaceBeforeAttributeDeclParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_METHOD_DEC_PAREN:
                                        countSpaces = docOptions.spaceBeforeMethodDeclParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_METHOD_CALL_PAREN:
                                        countSpaces = docOptions.spaceBeforeMethodCallParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_IF_PAREN:
                                        countSpaces = docOptions.spaceBeforeIfParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_PAREN:
                                        countSpaces = docOptions.spaceBeforeForParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_PAREN:
                                        countSpaces = docOptions.spaceBeforeWhileParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH_PAREN:
                                        countSpaces = docOptions.spaceBeforeCatchParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_SWITCH_PAREN:
                                        countSpaces = docOptions.spaceBeforeSwitchParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_MATCH_PAREN:
                                        countSpaces = docOptions.spaceBeforeMatchParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_ARRAY_DECL_PAREN:
                                        countSpaces = docOptions.spaceBeforeArrayDeclParen ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_COMMA:
                                        countSpaces = docOptions.spaceBeforeComma ? 1 : 0;
                                        break;
                                    case WHITESPACE_AFTER_COMMA:
                                        // #262205 don't add spaces if existing spaces have new lines
                                        if (templateEdit
                                                && index + 1 < formatTokens.size()
                                                && formatTokens.get(index + 1).getId() == FormatToken.Kind.WHITESPACE_INDENT
                                                && countOfNewLines(formatTokens.get(index + 1).getOldText()) > 0) {
                                            countSpaces = 0;
                                        } else {
                                            countSpaces = docOptions.spaceAfterComma ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_SEMI:
                                        countSpaces = docOptions.spaceBeforeSemi ? 1 : 0;
                                        break;
                                    case WHITESPACE_AFTER_SEMI:
//                                        countSpaces = docOptions.spaceAfterSemi ? 1 : 0;
                                        afterSemi = true;
                                        break;
                                    case WHITESPACE_AFTER_MODIFIERS:
                                        if (docOptions.placeNewLineAfterModifiers || countOfNewLines(oldText) > 0) {
                                            indentRule = true;
                                            newLines = 1;
                                            countSpaces = indent + docOptions.continualIndentSize;
                                        } else {
                                            countSpaces = 1;
                                        }
                                        break;
                                    case WHITESPACE_AFTER_ARRAY_DECL_LEFT_PAREN:
                                        switch (docOptions.wrapArrayInit) {
                                            case WRAP_ALWAYS:
                                                indentRule = true;
                                                newLines = isEmptyArray(formatTokens, index) ? 0 : 1;
                                                countSpaces = docOptions.alignMultilineArrayInit ? lastAnchor.getAnchorColumn() : indent;
                                                break;
                                            case WRAP_NEVER:
                                                if (isAfterLineComment(formatTokens, index)) {
                                                    newLines = 1;
                                                    countSpaces = docOptions.alignMultilineArrayInit ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = countSpacesForArrayDeclParens(index, indent, formatTokens);
                                                }
                                                break;
                                            case WRAP_IF_LONG:
                                                if (isAfterLineComment(formatTokens, index)
                                                        || column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                    indentRule = true;
                                                    newLines = isEmptyArray(formatTokens, index) ? 0 : 1;
                                                    countSpaces = docOptions.alignMultilineArrayInit ? lastAnchor.getAnchorColumn() : indent;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = countSpacesForArrayDeclParens(index, indent, formatTokens);
                                                }
                                                break;
                                            default:
                                                assert false : docOptions.wrapArrayInit;
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_ARRAY_DECL_RIGHT_PAREN:
                                        countSpaces = countSpacesForArrayDeclParens(index, indent, formatTokens);
                                        break;
                                    case WHITESPACE_WITHIN_ANONYMOUS_CLASS_PARENS:
                                        int helpIndex = index - 1;
                                        while (helpIndex > 0
                                                && formatTokens.get(helpIndex).getId() != FormatToken.Kind.WHITESPACE_WITHIN_ANONYMOUS_CLASS_PARENS
                                                && (formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE)) {
                                            helpIndex--;
                                        }
                                        if (helpIndex > 0 && formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_WITHIN_ANONYMOUS_CLASS_PARENS) {
                                            countSpaces = 0;
                                        } else {
                                            countSpaces = docOptions.spaceWithinAnonymousClassParens ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_METHOD_DECL_PARENS:
                                        // NETBEANS-3391
                                        // reset the flag
                                        // e.g.
                                        // abstract public function abstractMethod(
                                        //     $param
                                        // ); // there is a newline before ")", however, there is no braces
                                        // public function method($param) {
                                        // }
                                        hasNewLineBeforeRightParen = false;
                                        helpIndex = index - 1;
                                        while (helpIndex > 0
                                                && formatTokens.get(helpIndex).getId() != FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS
                                                && (formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE
                                                || formatTokens.get(helpIndex).getId() == FormatToken.Kind.INDENT
                                                /*
                                                 * ||
                                                 * formatTokens.get(helpIndex).getId()
                                                 * == FormatToken.Kind.WHITESPACE_INDENT
                                                 */)) {
                                            helpIndex--;
                                        }
                                        boolean noParams = helpIndex > 0 && formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS;
                                        if (noParams) {
                                            countSpaces = 0;
                                        } else {
                                            countSpaces = docOptions.spaceWithinMethodDeclParens ? 1 : 0;
                                        }

                                        // NETBEANS-3391
                                        // Before:
                                        // function test($arg1,
                                        //     $arg2): string {
                                        //     return 'foo';
                                        // }
                                        // After:
                                        // function test(
                                        //     $arg1,
                                        //     $arg2
                                        // ): string {
                                        //     return 'foo';
                                        // }
                                        if (isLeftParen(formatTokens.get(index - 1))) {
                                            helpIndex = index + 1;
                                            while (helpIndex < formatTokens.size()
                                                    && formatTokens.get(helpIndex).getId() != FormatToken.Kind.WHITESPACE_WITHIN_METHOD_DECL_PARENS
                                                    && (formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE
                                                    || formatTokens.get(helpIndex).getId() == FormatToken.Kind.INDENT)) {
                                                helpIndex++;
                                            }
                                            if (docOptions.wrapMethodParamsAfterLeftParen) {
                                                if (hasNewLineWithinParensForward(index, formatTokens, formatToken.getId())) {
                                                    indentLine = true;
                                                    newLines = 1;
                                                }
                                            }
                                        } else {
                                            if (docOptions.wrapMethodParamsRightParen) {
                                                if (hasNewLineWithinParensBackward(index, formatTokens, formatToken.getId())) {
                                                    indentLine = true;
                                                    newLines = 1;
                                                }
                                            }
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_METHOD_CALL_PARENS:
                                        helpIndex = index - 1;
                                        while (helpIndex > 0
                                                && formatTokens.get(helpIndex).getId() != FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS
                                                && (formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE
                                                || formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_INDENT)) {
                                            helpIndex--;
                                        }
                                        if (index > 0 && formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS) {
                                            countSpaces = 0;
                                        } else {
                                            countSpaces = docOptions.spaceWithinMethodCallParens ? 1 : 0;
                                        }
                                        // NETBEANS-3391
                                        if (isLeftParen(formatTokens.get(index - 1))) {
                                            methodCallParenBalance++;
                                            if (hasNewLineWithinParensForward(index, formatTokens, formatToken.getId())
                                                    && docOptions.wrapMethodCallArgsAfterLeftParen) {
                                                indentLine = true;
                                                newLines = 1;
                                            }
                                        } else {
                                            methodCallParenBalance--;
                                            if (methodCallParenBalance > 0 && !lastAnchorTokenStack.isEmpty()) {
                                                lastAnchor = lastAnchorTokenStack.pop();
                                            }
                                            if (hasNewLineWithinParensBackward(index, formatTokens, formatToken.getId())
                                                    && docOptions.wrapMethodCallArgsRightParen) {
                                                indentLine = true;
                                                newLines = 1;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_IF_PARENS:
                                        countSpaces = docOptions.spaceWithinIfParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_FOR_PARENS:
                                        countSpaces = docOptions.spaceWithinForParens ? 1 : 0;
                                        // NETBEANS-3391
                                        if (isLeftParen(formatTokens.get(index - 1))) {
                                            if (hasNewLineWithinParensForward(index, formatTokens, formatToken.getId())
                                                    && docOptions.wrapForAfterLeftParen) {
                                                indentLine = true;
                                                newLines = 1;
                                            }
                                        } else {
                                            if (hasNewLineWithinParensBackward(index, formatTokens, formatToken.getId())
                                                    && docOptions.wrapForRightParen) {
                                                indentLine = true;
                                                newLines = 1;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_WHILE_PARENS:
                                        countSpaces = docOptions.spaceWithinWhileParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_SWITCH_PARENS:
                                        countSpaces = docOptions.spaceWithinSwitchParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_MATCH_PARENS:
                                        countSpaces = docOptions.spaceWithinMatchParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_CATCH_PARENS:
                                        countSpaces = docOptions.spaceWithinCatchParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS:
                                        helpIndex = index - 1;
                                        while (helpIndex > 0
                                                && formatTokens.get(helpIndex).getId() != FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS
                                                && formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE) {
                                            helpIndex--;
                                        }
                                        if (helpIndex > 0 && formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_WITHIN_ARRAY_BRACKETS_PARENS) {
                                            countSpaces = 0;
                                        } else {
                                            countSpaces = docOptions.spaceWithinArrayBrackets ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_ATTRIBUTE_BRACKETS:
                                        // countSpace 0
                                        // #[
                                        //     A(1)
                                        // ]
                                        // ^
                                        // class MyClass {}
                                        //
                                        // or
                                        // countSpace 1
                                        // #[A(1)] class MyClass{}
                                        helpIndex = index - 1;
                                        while (helpIndex > 0
                                                && (formatTokens.get(helpIndex).getId() == FormatToken.Kind.INDENT
                                                || formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE)) {
                                            helpIndex--;
                                        }
                                        if (index > 0 && formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                                            countSpaces = 0;
                                        } else {
                                            countSpaces = docOptions.spaceWithinAttributeBrackets ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_ATTRIBUTE_DECL_PARENS:
                                        // countSpace 0 if it's an empty parameter
                                        // #[A()]
                                        helpIndex = index - 1;
                                        while (helpIndex > 0
                                                && formatTokens.get(helpIndex).getId() != FormatToken.Kind.WHITESPACE_WITHIN_ATTRIBUTE_DECL_PARENS
                                                && (formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE
                                                || formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_INDENT)) {
                                            helpIndex--;
                                        }
                                        if (index > 0 && formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_WITHIN_ATTRIBUTE_DECL_PARENS) {
                                            countSpaces = 0;
                                        } else {
                                            countSpaces = docOptions.spaceWithinAttributeDeclParens ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_WITHIN_TYPE_CAST_PARENS:
                                        countSpaces = docOptions.spaceWithinTypeCastParens ? 1 : 0;
                                        break;
                                    case WHITESPACE_WITHIN_DNF_TYPE_PARENS:
                                        // change here if we add the option for it
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_WITHIN_DYNAMIC_NAME_BRACES:
                                        // change here if we add the option for it
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_AFTER_TYPE_CAST:
                                        countSpaces = docOptions.spaceAfterTypeCast ? 1 : 0;
                                        break;
                                    case WHITESPACE_BEFORE_FOR_STATEMENT:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(
                                                docOptions.wrapForStatement,
                                                true,
                                                column,
                                                countLengthOfNextSequence(formatTokens, index + 1),
                                                indent,
                                                isAfterLineComment(formatTokens, index));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE_STATEMENT:
                                        indentRule = true;
                                        if (!isBeforeEmptyStatement(formatTokens, index)) {
                                            ws = countWSBeforeAStatement(
                                                    docOptions.wrapWhileStatement,
                                                    true,
                                                    column,
                                                    countLengthOfNextSequence(formatTokens, index + 1),
                                                    indent,
                                                    isAfterLineComment(formatTokens, index));
                                            newLines = ws.lines;
                                            countSpaces = ws.spaces;
                                        } else {
                                            newLines = 0;
                                            countSpaces = docOptions.spaceBeforeSemi ? 1 : 0;
                                        }
                                        break;
                                    case WHITESPACE_BEFORE_DO_STATEMENT:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(
                                                docOptions.wrapDoWhileStatement,
                                                true,
                                                column,
                                                countLengthOfNextSequence(formatTokens, index + 1),
                                                indent,
                                                isAfterLineComment(formatTokens, index));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_IF_ELSE_STATEMENT:
                                        indentRule = true;
                                        if (isCloseAndOpenTagOnOneLine(formatTokens, index)) {
                                            newLines = 0;
                                            countSpaces = 1;
                                        } else {
                                            ws = countWSBeforeAStatement(
                                                    docOptions.wrapIfStatement,
                                                    true,
                                                    column,
                                                    countLengthOfNextSequence(formatTokens, index + 1),
                                                    indent,
                                                    isAfterLineComment(formatTokens, index));
                                            newLines = ws.lines;
                                            countSpaces = ws.spaces;
                                        }
                                        break;
                                    case WHITESPACE_IN_FOR:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(
                                                docOptions.wrapFor,
                                                true,
                                                column,
                                                countLengthOfNextSequence(formatTokens, index + 1),
                                                indent,
                                                isAfterLineComment(formatTokens, index));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        if (docOptions.wrapFor == CodeStyle.WrapStyle.WRAP_NEVER) {
                                            // for keeping the same line
                                            int countOfNewLines = countOfNewLines(oldText);
                                            if (isAfterLineComment(formatTokens, index)
                                                    || (!docOptions.wrapNeverKeepLines && countOfNewLines > 0)) {
                                                newLines = 1;
                                                countSpaces = indent;
                                            }
                                        }
                                        break;
                                    case WHITESPACE_IN_TERNARY_OP:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(
                                                docOptions.wrapTernaryOps,
                                                docOptions.spaceAroundTernaryOps,
                                                column,
                                                countLengthOfNextSequence(formatTokens, index + 1),
                                                indent,
                                                isAfterLineComment(formatTokens, index));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_IN_COALESCING_OP:
                                        indentRule = true;
                                        ws = countWSBeforeAStatement(
                                                docOptions.wrapCoalescingOps,
                                                docOptions.spaceAroundCoalescingOps,
                                                column,
                                                countLengthOfNextSequence(formatTokens, index + 1),
                                                indent,
                                                isAfterLineComment(formatTokens, index));
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_IN_CHAINED_METHOD_CALLS:
                                        indentRule = true;
                                        switch (docOptions.wrapChainedMethodCalls) {
                                            case WRAP_ALWAYS:
                                                newLines = 1;
                                                countSpaces = indent + docOptions.continualIndentSize;
                                                break;
                                            case WRAP_NEVER:
                                                newLines = 0;
                                                countSpaces = 0;
                                                break;
                                            case WRAP_IF_LONG:
                                                if (column + 1 + countLengthOfNextSequence(formatTokens, index + 1) > docOptions.margin) {
                                                    newLines = 1;
                                                    countSpaces = indent + docOptions.continualIndentSize;
                                                } else {
                                                    newLines = 0;
                                                    countSpaces = 1;
                                                }
                                                break;
                                            default:
                                                assert false : docOptions.wrapChainedMethodCalls;
                                        }
                                        break;
                                    case WHITESPACE_BETWEEN_LINE_COMMENTS:
                                        newLines = 1;
                                        break;
                                    case WHITESPACE_BEFORE_CATCH:
                                        indentRule = true;
                                        ws = countWSBeforeKeyword(docOptions.placeCatchOnNewLine, docOptions.spaceBeforeCatch, indent, formatTokens, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_FINALLY:
                                        indentRule = true;
                                        ws = countWSBeforeKeyword(docOptions.placeFinallyOnNewLine, docOptions.spaceBeforeFinally, indent, formatTokens, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_WHILE:
                                        indentRule = true;
                                        ws = countWSBeforeKeyword(docOptions.placeWhileOnNewLine, docOptions.spaceBeforeWhile, indent, formatTokens, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_ELSE:
                                        indentRule = true;
                                        ws = countWSBeforeKeyword(docOptions.placeElseOnNewLine, docOptions.spaceBeforeElse, indent, formatTokens, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_BEFORE_ELSE_WITHOUT_CURLY:
                                        indentRule = true;
                                        boolean placeElseOnNewLine;
                                        if (isPrecededByBlockedIf(index, formatTokens)) {
                                            placeElseOnNewLine = docOptions.placeElseOnNewLine;
                                        } else {
                                            placeElseOnNewLine = true;
                                        }
                                        ws = countWSBeforeKeyword(placeElseOnNewLine, docOptions.spaceBeforeElse, indent, formatTokens, index);
                                        newLines = ws.lines;
                                        countSpaces = ws.spaces;
                                        break;
                                    case WHITESPACE_INDENT:
                                        if (formatTokens.get(index - 1).getId() == FormatToken.Kind.WHITESPACE_AFTER_USE) {
                                            // GH-6980
                                            // namespace {
                                            //     use Vendor\Package\ExampleClass;
                                            //     $variable = 1; // add indent spaces here
                                            // }
                                            countSpaces = indent;
                                        }
                                        indentLine = true;
                                        break;
                                    case INDENT:
                                        int indentDelta = ((FormatToken.IndentToken) formatToken).getDelta();
                                        indent += indentDelta;
                                        lastPHPIndent += indentDelta;
                                        break;
                                    case ANCHOR:
                                        if (methodCallParenBalance > 0 && lastAnchor != null) {
                                            lastAnchorTokenStack.push(lastAnchor);
                                        }
                                        lastAnchor = (FormatToken.AnchorToken) formatToken;
                                        lastAnchor.setAnchorColumn(column + 1);
                                        break;
                                    case WHITESPACE_BEFORE_OPEN_PHP_TAG:
                                        // we rely on AbstractIndenter.lineIndents that comes from html formatter.
                                        // we have to be also sure that the html formatter is called before the php one.
                                        Map<Integer, Integer> suggestedLineIndents = (Map<Integer, Integer>) doc.getProperty("AbstractIndenter.lineIndents"); // NOI18N

                                        if (oldText == null) {
                                            try {
                                                int phpOpenTagOffset = formatToken.getOffset() + delta;
                                                int lineNumber = LineDocumentUtils.getLineIndex(doc, phpOpenTagOffset);
                                                Integer suggestedIndent = suggestedLineIndents != null
                                                        ? suggestedLineIndents.get(lineNumber)
                                                        : Integer.valueOf(0);
                                                if (suggestedIndent == null) {
                                                    // XXX this is a hack
                                                    //sometimes the html formatter doesn't catch the first line.
                                                    suggestedIndent = suggestedLineIndents.get(lineNumber + 1) != null
                                                            ? suggestedLineIndents.get(lineNumber + 1)
                                                            : Integer.valueOf(0);
                                                }

                                                int lineOffset = LineDocumentUtils.getLineStart(doc, phpOpenTagOffset);
                                                int firstNonWhiteCharacterOffset = LineDocumentUtils.getNextNonWhitespace(doc, lineOffset);
                                                if (firstNonWhiteCharacterOffset == phpOpenTagOffset) {
                                                    indentRule = true;
                                                    changeOffset = lineOffset - delta;
                                                    oldText = doc.getText(lineOffset, firstNonWhiteCharacterOffset - lineOffset);
                                                    htmlIndent = suggestedIndent.intValue();
                                                    // the indentation is composed from html inden + php indent
                                                    indent = htmlIndent + docOptions.initialIndent + lastPHPIndent;
                                                    // is it the first php open file in the file? Than don't add
                                                    // initial indent. There should be better
                                                    // recognition of this.
                                                    countSpaces = lastPHPIndent == 0 ? htmlIndent : indent;

                                                    // try to find, whether there is no indend tag after open tag
                                                    int indentIndex = index;
                                                    int helpSpaces = 0;
                                                    while (indentIndex < formatTokens.size()
                                                            && formatTokens.get(indentIndex).getId() != FormatToken.Kind.TEXT
                                                            && formatTokens.get(indentIndex).getId() != FormatToken.Kind.WHITESPACE_INDENT
                                                            && formatTokens.get(indentIndex).getId() != FormatToken.Kind.CLOSE_TAG) {
                                                        if (formatTokens.get(indentIndex).getId() == FormatToken.Kind.INDENT) {
                                                            helpSpaces += ((FormatToken.IndentToken) formatTokens.get(indentIndex)).getDelta();
                                                        }
                                                        indentIndex++;
                                                    }
                                                    if (indentIndex < formatTokens.size()
                                                            && formatTokens.get(indentIndex).getId() == FormatToken.Kind.TEXT) {
                                                        String text = formatTokens.get(indentIndex).getOldText();
                                                        if ("}".equals(text) || "endif".equals(text)
                                                                || "else".equals(text)
                                                                || "elseif".equals(text)
                                                                || "endfor".equals(text)
                                                                || "endforeach".equals(text)
                                                                || "endwhile".equals(text)
                                                                || "endswitch".equals(text)) {
                                                            countSpaces += helpSpaces;
                                                        }
                                                    }

                                                    indentOfOpenTag = countSpaces;
                                                }
                                            } catch (BadLocationException ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        }
                                        break;
                                    case WHITESPACE_AFTER_OPEN_PHP_TAG:
                                        indentRule = true;
                                        indent = Math.max(lastPHPIndent, indent);
                                        if (!isOpenAndCloseTagOnOneLine(formatTokens, index)) {
                                            if (((FormatToken.InitToken) formatTokens.get(0)).hasHTML()) {
                                                newLines = docOptions.blankLinesAfterOpenPHPTagInHTML + 1;
                                            } else {
                                                newLines = docOptions.blankLinesAfterOpenPHPTag;
                                                if (!isRightBeforeNamespaceDeclaration(formatTokens, index)) {
                                                    newLines++;
                                                } else if (newLines > 0) {
                                                    newLines++;
                                                }
                                            }
                                            suggestedLineIndents = (Map<Integer, Integer>) doc.getProperty("AbstractIndenter.lineIndents");
                                            if (suggestedLineIndents != null) {
                                                try {
                                                    int offset = formatToken.getOffset() + delta;
                                                    int lineNumber = LineDocumentUtils.getLineIndex(doc, offset) + 1;
                                                    Integer suggestedIndent = suggestedLineIndents.get(lineNumber);
                                                    if (suggestedIndent != null) {
                                                        htmlIndent = suggestedIndent.intValue();
                                                        indent = htmlIndent + docOptions.initialIndent + lastPHPIndent;
                                                        countSpaces = indent;
                                                    } else {
                                                        countSpaces = indent;
                                                    }
                                                } catch (BadLocationException ex) {
                                                    Exceptions.printStackTrace(ex);
                                                }
                                            } else {
                                                countSpaces = indent;
                                            }
                                            helpIndex = index + 1;
                                            while (helpIndex < formatTokens.size()
                                                    && formatTokens.get(helpIndex).isWhitespace()) {
                                                helpIndex++;
                                            }
                                            if (helpIndex < formatTokens.size() && formatTokens.get(helpIndex).getId() == FormatToken.Kind.INDENT) {
                                                countSpaces += ((FormatToken.IndentToken) formatTokens.get(helpIndex)).getDelta();
                                            }
                                        } else {
                                            newLines = 0;
                                            countSpaces = 1;
                                            if (index > 0) {
                                                FormatToken ft = formatTokens.get(index - 1);
                                                if (ft.getId() == FormatToken.Kind.OPEN_TAG
                                                        && ft.getOldText().length() < 4) {
                                                    countSpaces = docOptions.spaceAfterShortTag ? 1 : 0;
                                                }
                                            }

                                        }
                                        break;
                                    case WHITESPACE_BEFORE_CLOSE_PHP_TAG:
                                        suggestedLineIndents = (Map<Integer, Integer>) doc.getProperty("AbstractIndenter.lineIndents");
                                        indentRule = true;
                                        if (suggestedLineIndents != null) {
                                            try {
                                                int offset = formatToken.getOffset() + delta;
                                                int lineNumber = LineDocumentUtils.getLineIndex(doc, offset);
                                                Integer suggestedIndent = suggestedLineIndents.get(lineNumber);
                                                if (suggestedIndent != null) {
                                                    int lineOffset = LineDocumentUtils.getLineStart(doc, offset);
                                                    int firstNW = LineDocumentUtils.getNextNonWhitespace(doc, lineOffset);
                                                    if (firstNW == offset) {
                                                        countSpaces = lastPHPIndent == 0 ? htmlIndent : lastPHPIndent + htmlIndent + docOptions.initialIndent;
                                                        newLines = docOptions.blankLinesBeforeClosePHPTag + 1;
                                                    } else {
                                                        if (isAfterLineComment(formatTokens, index)) {
                                                            // there should be logic, which will remove whitespaces at the end of line comment in the case // comment ?>
                                                            countSpaces = 0;
                                                        } else {
                                                            countSpaces = docOptions.spaceBeforeClosePHPTag ? 1 : 0;
                                                        }
                                                        if (!isCloseAndOpenTagOnOneLine(formatTokens, index)) {
                                                            newLines = docOptions.blankLinesBeforeClosePHPTag + 1;
                                                            countSpaces = lastPHPIndent == 0 ? htmlIndent : lastPHPIndent + htmlIndent + docOptions.initialIndent;
                                                        } else {
                                                            newLines = 0;
                                                        }
                                                    }
                                                    indent = suggestedIndent;
                                                } else {
                                                    if (!isCloseAndOpenTagOnOneLine(formatTokens, index)) {
                                                        newLines = docOptions.blankLinesBeforeClosePHPTag + 1;
                                                        countSpaces = lastPHPIndent == 0 ? htmlIndent : indent;
                                                    } else {
                                                        newLines = 0;
                                                        countSpaces = docOptions.spaceBeforeClosePHPTag ? 1 : 0;
                                                    }
                                                }
                                            } catch (BadLocationException ex) {
                                                Exceptions.printStackTrace(ex);
                                            }
                                        } else {
                                            if (!isCloseAndOpenTagOnOneLine(formatTokens, index)) {
                                                newLines = Math.max(newLines, docOptions.blankLinesBeforeClosePHPTag + 1);
                                                countSpaces = indentOfOpenTag;
                                            } else {
                                                newLines = 0;
                                                countSpaces = docOptions.spaceBeforeClosePHPTag ? 1 : 0;
                                            }
                                        }

                                        break;
                                    case WHITESPACE_AFTER_CLOSE_PHP_TAG:
                                        break;
                                    case WHITESPACE_BEFORE_NAMED_ARGUMENT_SEPARATOR:
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_AFTER_NAMED_ARGUMENT_SEPARATOR:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_BEFORE_ENUM_BACKING_TYPE_SEPARATOR:
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_AFTER_ENUM_BACKING_TYPE_SEPARATOR:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_BEFORE_RETURN_TYPE_SEPARATOR:
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_AFTER_RETURN_TYPE_SEPARATOR:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_AFTER_NULLABLE_TYPE_PREFIX:
                                        countSpaces = 0;
                                        break;
                                    case WHITESPACE_BEFORE_MULTI_CATCH_SEPARATOR:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_AFTER_MULTI_CATCH_SEPARATOR:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_AFTER_TYPE:
                                        countSpaces = 1;
                                        break;
                                    case WHITESPACE_AFTER_ATTRIBUTE:
                                        if (index + 1 < formatTokens.size()) {
                                            // countSpace 0
                                            // #[A(1)]
                                            // class MyClass {}
                                            //
                                            // or
                                            // countSpace 1
                                            // #[A(1)] class MyClass{}
                                            countSpaces = formatTokens.get(index + 1).getId() == FormatToken.Kind.WHITESPACE_INDENT ? 0 : 1;
                                        }
                                        break;
                                    default:
                                    //no-op
                                }
                                index++; //index += moveIndex;
                                if (index < formatTokens.size()) {
                                    formatToken = formatTokens.get(index);
                                }
                            }



                            if (changeOffset > -1) {
                                boolean isBeforeLineComment = isBeforeLineComment(formatTokens, index - 1);
                                if (wasARule) {
                                    if ((!indentRule || newLines == -1) && indentLine) {
                                        boolean handlingSpecialCases = false;
                                        if (FormatToken.Kind.TEXT == formatToken.getId()
                                                && (")".equals(formatToken.getOldText()) || "]".equals(formatToken.getOldText()))) {
                                            // tryin find out and handling cases when )) folows.
                                            int hIndex = index + 1;
                                            int hindent = indent;
                                            if (hIndex < formatTokens.size()) {
                                                FormatToken token;
                                                int lastIndent = 0;
                                                boolean bracketsInLine = false;
                                                do {
                                                    token = formatTokens.get(hIndex);
                                                    if (token.getId() == FormatToken.Kind.INDENT) {
                                                        lastIndent = ((FormatToken.IndentToken) token).getDelta();
                                                        hindent += ((FormatToken.IndentToken) token).getDelta();
                                                    } else if (token.getId() == FormatToken.Kind.TEXT
                                                            && (")".equals(token.getOldText()) || "]".equals(token.getOldText()))) {
                                                        bracketsInLine = true;
                                                    } else if (token.getId() == FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS) {
                                                        // NETBEANS-3391
                                                        if (hasNewLineWithinParensBackward(hIndex, formatTokens, FormatToken.Kind.WHITESPACE_WITHIN_METHOD_CALL_PARENS)
                                                                && docOptions.wrapMethodCallArgsRightParen) {
                                                            break;
                                                        }
                                                    }

                                                    hIndex++;
                                                } while (hIndex < formatTokens.size()
                                                        && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                                                        && token.getId() != FormatToken.Kind.WHITESPACE
                                                        && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT
                                                        || token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_END
                                                        || (token.getId() == FormatToken.Kind.TEXT
                                                        && (")".equals(token.getOldText()) || "]".equals(token.getOldText())))));
                                                if (FormatToken.Kind.TEXT == token.getId() && ";".equals(token.getOldText())) {
                                                    countSpaces = hindent == 0 && bracketsInLine ? lastIndent * -1 : hindent;
                                                    handlingSpecialCases = true;
                                                }
                                            }

                                        }
                                        if (!handlingSpecialCases) {
                                            countSpaces = Math.max(countSpaces, indent);
                                        }
                                        newLines = Math.max(1, newLines);
                                    }
                                } else if (indentLine) {
                                    countSpaces = indent;
                                    newLines = oldText == null ? 1 : countOfNewLines(oldText);
                                    if (index > 1 && index < formatTokens.size()
                                            && formatTokens.get(index - 2).getId() == FormatToken.Kind.TEXT
                                            && formatTokens.get(index).getId() == FormatToken.Kind.TEXT
                                            && "(".equals(formatTokens.get(index - 2).getOldText())
                                            && ")".equals(formatTokens.get(index).getOldText())) {
                                        newLines = 0;
                                    } else if (index - 2 > -1) {
                                        newLines = getPreviousNonWhite(formatTokens, index - 2).getId() == FormatToken.Kind.DOC_COMMENT_END ? 1 : newLines;
                                    }
                                } else {
                                    boolean isBeginLine = isBeginLine(formatTokens, index - 1);

                                    if (isBeforeLineComment) {
                                        countSpaces = isBeginLine ? indent : oldText.length();
                                    } else {
                                        countSpaces = isBeginLine
                                                ? isBeforeLineComment ? 0 : Math.max(countSpaces, indent)
                                                : Math.max(countSpaces, 1);
                                    }
                                }
                                if (isBeforeLineComment && oldText != null && oldText.endsWith("\n")) {
                                    countSpaces = 0;
                                }
                                if (wsBetweenBraces && newLines > 1) {
                                    newLines = 1;
                                }
                                if (afterSemi) {
                                    if (oldText == null || countOfNewLines(oldText) == 0) {
                                        if (formatToken.getId() == FormatToken.Kind.TEXT) {
                                            if (docOptions.wrapStatementsOnTheSameLine) {
                                                if (docOptions.wrapBlockBrace || !"}".equals(formatToken.getOldText())) {
                                                    newLines = Math.max(1, newLines);
                                                    countSpaces = indent;
                                                }
                                            } else {
                                                if (!indentRule) {
                                                    countSpaces = docOptions.spaceAfterSemi ? 1 : 0;
                                                }
                                            }
                                        } else if (formatToken.getId() == FormatToken.Kind.LINE_COMMENT
                                                || formatToken.getId() == FormatToken.Kind.COMMENT_START) {
                                            if (oldText == null || oldText.length() == 0) {
                                                countSpaces = docOptions.spaceAfterSemi ? 1 : 0;
                                            } else {
                                                countSpaces = oldText.length();
                                            }
                                        }
                                    } else {
                                        if (!indentRule) {
                                            newLines = countOfNewLines(oldText);
                                            newLines = docOptions.blankLinesMaxPreserved + 1 < newLines ? docOptions.blankLinesMaxPreserved + 1 : newLines;
                                        }
                                    }
                                }
                                // NETBEANS-3391 keep ")" and "{" on the same line
                                // e.g.
                                // funciton test(
                                //    param,
                                // ): string {
                                if (isRightParen(formatToken) && newLines > 0) {
                                    hasNewLineBeforeRightParen = true;
                                }
                                newText = createWhitespace(docOptions, newLines, countSpaces);
                                if (wsBetweenBraces) {
                                    if (lastBracePlacement == CodeStyle.BracePlacement.PRESERVE_EXISTING) {
                                        newText = createWhitespace(docOptions, 1, indent + docOptions.indentSize) + newText;
                                    } else {
                                        newText = createWhitespace(docOptions, 1, indent + docOptions.indentSize)
                                                + createWhitespace(
                                                docOptions,
                                                1,
                                                lastBracePlacement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? indent + docOptions.indentSize : indent);
                                    }
                                }
                                int realOffset = changeOffset + delta;
                                if (templateEdit && !caretInTemplateSolved && oldText != null
                                        && formatContext.startOffset() - 1 <= realOffset
                                        && realOffset <= formatContext.endOffset() + 1) {

                                    int caretPosition = caretOffset + delta;
                                    if (caretPosition == formatContext.endOffset() && oldText.length() > 0 && newText.length() > 0
                                            && oldText.charAt(0) == ' ' && newText.charAt(0) != ' ' && 0 != countOfNewLines(oldText)) {
                                        newText = ' ' + newText;   // templates like public, return ...
                                        caretInTemplateSolved = true;
                                    }
                                }
                                if (formatToken.getId() == FormatToken.Kind.TEXT
                                        && "{".equals(formatToken.getOldText()) //NOI18N
                                        && newLines == 0
                                        && isAfterLineComment(formatTokens, index - 2)) {
                                    // there has to be moved '{' after ')'
                                    int hIndex = index - 2;
                                    while (hIndex > 0 && formatTokens.get(hIndex).getId() != FormatToken.Kind.TEXT) {
                                        hIndex--;
                                    }
                                    if (hIndex > 0 && formatTokens.get(hIndex).getId() == FormatToken.Kind.TEXT
                                            && (")".equals(formatTokens.get(hIndex).getOldText()) // NOI18N
                                            || "else".equals(formatTokens.get(hIndex).getOldText()) || "]".equals(formatToken.getOldText()))) { // NOI18N
                                        delta = replaceString(
                                                doc,
                                                formatTokens.get(hIndex).getOffset() + formatTokens.get(hIndex).getOldText().length() - (delta - deltaForLastMoveBeforeLineComment),
                                                hIndex + 1,
                                                "",
                                                newText + "{",
                                                delta,
                                                templateEdit); // NOI18N
                                        delta = replaceString(doc, changeOffset, index, oldText, "", delta, templateEdit);
                                        delta = replaceString(doc, formatToken.getOffset(), index, formatToken.getOldText(), "", delta, templateEdit);
                                        newText = null;
                                    }

                                }
                            }
                            index--;
                        } else {

                            switch (formatToken.getId()) {
                                case INDENT:
                                    indent += ((FormatToken.IndentToken) formatToken).getDelta();
                                    lastPHPIndent += ((FormatToken.IndentToken) formatToken).getDelta();
                                    break;
                                case COMMENT:
                                case DOC_COMMENT:
                                    oldText = formatToken.getOldText() != null ? formatToken.getOldText() : "";
                                    changeOffset = formatToken.getOffset();
                                    newText = formatComment(index, indent, oldText);
                                    if (newText.equals(oldText)) {
                                        newText = null;
                                    }
                                    break;
                                case ANCHOR:
                                    if (methodCallParenBalance > 0 && lastAnchor != null) {
                                        lastAnchorTokenStack.push(lastAnchor);
                                    }
                                    lastAnchor = (FormatToken.AnchorToken) formatToken;
                                    lastAnchor.setAnchorColumn(column);
                                    break;
                                case HTML:
                                    if (htmlIndent > -1) {
                                        oldText = formatToken.getOldText();
                                        int firstNW = 0;
                                        while (firstNW < oldText.length()
                                                && Character.isWhitespace(oldText.charAt(firstNW))) {
                                            firstNW++;
                                        }
                                        int lineOffset = formatToken.getOffset() + delta;
                                        try {
                                            // the first line of the html block
                                            int firstLine = LineDocumentUtils.getLineIndex(doc, lineOffset);

                                            boolean countInitialIndent = docOptions.initialIndent > 0 && lastPHPIndent > 0;

                                            int indexInST = 0;
                                            for (StringTokenizer st = new StringTokenizer(oldText, "\n", true); st.hasMoreTokens();) { //NOI18N
                                                String token = st.nextToken();
                                                int currentOffset = formatToken.getOffset() + delta + indexInST;
                                                indexInST += token.length();
                                                int currentLine = LineDocumentUtils.getLineIndex(doc, currentOffset);
                                                if (firstLine < currentLine && !token.equals("\n")) {  //NOI18N
                                                    int lineIndent = doc.getLength() + 1 >= currentOffset + 1 ? Utilities.getRowIndent(doc, currentOffset + 1) : 0;
                                                    int finalIndent = lastPHPIndent + lineIndent + (countInitialIndent ? docOptions.initialIndent : 0); // - lineHTMLIndent;
                                                    if (finalIndent == docOptions.initialIndent && finalIndent != 0) {
                                                        finalIndent = 0;
                                                    }
                                                    if (lineIndent < finalIndent) {
                                                        delta = replaceString(
                                                                doc,
                                                                currentOffset - delta,
                                                                index,
                                                                "",
                                                                createWhitespace(docOptions, 0, finalIndent - lineIndent),
                                                                delta,
                                                                false);
                                                    }
                                                }

                                            }

                                        } catch (BadLocationException ex) {
                                            Exceptions.printStackTrace(ex);
                                        }
                                        oldText = null;
                                        newText = null;
                                    }
                                    break;
                                case TEXT:
                                    if (")".equals(formatToken.getOldText()) // NOI18N
                                            || "else".equals(formatToken.getOldText()) || "]".equals(formatToken.getOldText())) {       // NOI18N
                                        // remember the delta for last paren or else keyword due to
                                        // possible moving { before line comment
                                        deltaForLastMoveBeforeLineComment = delta;
                                    }
                                    break;
                                default:
                                //no-op
                            }
                        }

                        delta = replaceString(doc, changeOffset, index, oldText, newText, delta, templateEdit);
                        // GH-6714 if text have TABs, get incorrect column
                        // so, use countOfSpaces() instead of newText.length()
                        if (newText == null) {
                            String formatTokenOldText = formatToken.getOldText() == null ? CodeUtils.EMPTY_STRING : formatToken.getOldText();
                            int formatTokenOldTextLength = countOfSpaces(formatTokenOldText, docOptions.tabSize);
                            int lines = countOfNewLines(formatTokenOldText);
                            if (lines > 0) {
                                int lastNewLine = formatTokenOldText.lastIndexOf(CodeUtils.NEW_LINE);
                                String substring = formatTokenOldText.substring(lastNewLine);
                                column = countOfSpaces(substring, docOptions.tabSize);
                            } else {
                                column += formatTokenOldTextLength;
                            }
                        } else {
                            int lines = countOfNewLines(newText);
                            if (lines > 0) {
                                column = countOfSpaces(newText, docOptions.tabSize) - lines;
                            } else {
                                column += countOfSpaces(newText, docOptions.tabSize);
                            }
                        }
                        newText = null;
                        index++;
                    }
                } finally {
                    if (docOptions.blankLinesEOF) {
                        resolveNoNewLineAtEOF(doc);
                    }
                    mti.tokenHierarchyControl().setActive(true);
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    long end = System.currentTimeMillis();
                    LOGGER.log(Level.FINE, "Applaying format stream took: {0} ms", (end - start.get())); // NOI18N
                }
            }

            private boolean isRightBeforeNamespaceDeclaration(List<FormatToken> formatTokens, int index) {
                boolean result = false;
                int i = index + 1;
                int formatTokensSize = formatTokens.size();
                while (formatTokensSize > i && formatTokens.get(i).isWhitespace()) {
                    if (formatTokens.get(i).getId() == FormatToken.Kind.WHITESPACE_BEFORE_NAMESPACE) {
                        result = true;
                        break;
                    }
                    i++;
                }
                return result;
            }

            private boolean isEmptyArray(List<FormatToken> formatTokens, int index) {
                boolean result = false;
                if (formatTokens.size() >= index + 2) {
                    FormatToken possibleParenToken = formatTokens.get(index + 1);
                    if (possibleParenToken.getId() == FormatToken.Kind.WHITESPACE) {
                        possibleParenToken = formatTokens.get(index + 2);
                    }
                    result = possibleParenToken.getId() == FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_RIGHT_PAREN;
                }
                return result;
            }

            private int countSpacesForArrayDeclParens(int index, int indent, List<FormatToken> formatTokens) {
                int countSpaces;
                int hIndex = index - 1;
                FormatToken token;
                do {
                    token = formatTokens.get(hIndex);
                    hIndex--;

                } while (token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                        && token.getId() != FormatToken.Kind.TEXT
                        && token.getId() != FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_RIGHT_PAREN
                        && token.getId() != FormatToken.Kind.WHITESPACE_AFTER_ARRAY_DECL_LEFT_PAREN
                        && hIndex > 0);
                if (token.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                    countSpaces = indent;
                } else if (token.getId() == FormatToken.Kind.WHITESPACE_BEFORE_ARRAY_DECL_RIGHT_PAREN
                        || token.getId() == FormatToken.Kind.WHITESPACE_AFTER_ARRAY_DECL_LEFT_PAREN) {
                    countSpaces = 0;
                } else {
                    countSpaces = docOptions.spaceWithinArrayDeclParens ? 1 : 0;
                }
                return countSpaces;
            }

            private int countLastBracedBlockIndent(int indent, CharSequence oldText) {
                int result = 0;
                int lastIndexOfNewLine = -1;
                if (oldText != null) {
                    lastIndexOfNewLine = oldText.toString().lastIndexOf('\n');
                }
                if (lastIndexOfNewLine != -1) {
                    result = indent - countOfSpaces(oldText.toString().substring(lastIndexOfNewLine + 1), docOptions.tabSize);
                }
                return result;
            }

            private Whitespace countWhiteSpaceBeforeLeftBrace(
                    CodeStyle.BracePlacement placement,
                    boolean spaceBefore,
                    CharSequence text,
                    int indent,
                    int lastBracedBlockIndent) {
                return countWhiteSpaceBeforeLeftBrace(placement, spaceBefore, text, indent, lastBracedBlockIndent, false);
            }

            private Whitespace countWhiteSpaceBeforeLeftBrace(
                    CodeStyle.BracePlacement placement,
                    boolean spaceBefore,
                    CharSequence text,
                    int indent,
                    int lastBracedBlockIndent,
                    boolean isAfterLineComment) {
                Whitespace result;
                int lines = isAfterLineComment ? 1 : 0;
                int spaces = 0;
                if (placement == CodeStyle.BracePlacement.PRESERVE_EXISTING) {
                    if (text == null) {
                        result = new Whitespace(lines, spaces);
                    } else {
                        result = countWhiteSpaceForPreserveExistingBracePlacement(text, text.toString().indexOf('\n') == -1 ? 0 : lastBracedBlockIndent);
                    }
                } else {
                    lines = (placement == CodeStyle.BracePlacement.SAME_LINE) ? lines : 1;
                    spaces = lines > 0
                            ? (placement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? indent + docOptions.indentSize : indent)
                            : spaceBefore ? 1 : 0;
                    result = new Whitespace(lines, spaces);
                }
                return result;
            }

            private Whitespace countWSBeforeAStatement(
                    CodeStyle.WrapStyle style,
                    boolean addSpaceIfNoLine,
                    int column,
                    int lengthOfNexSequence,
                    int currentIndent,
                    boolean isAfterLineComment) {
                int lines = 0;
                int spaces = 0;
                switch (style) {
                    case WRAP_ALWAYS:
                        lines = 1;
                        spaces = currentIndent;
                        break;
                    case WRAP_NEVER:
                        if (isAfterLineComment) {
                            lines = 1;
                            spaces = currentIndent;
                        } else {
                            lines = 0;
                            spaces = addSpaceIfNoLine ? 1 : 0;
                        }
                        break;
                    case WRAP_IF_LONG:
                        if (column + 1 + lengthOfNexSequence > docOptions.margin) {
                            lines = 1;
                            spaces = currentIndent + docOptions.indentSize;
                        } else {
                            if (isAfterLineComment) {
                                lines = 1;
                                spaces = currentIndent;
                            } else {
                                lines = 0;
                                spaces = addSpaceIfNoLine ? 1 : 0;
                            }
                        }
                        break;
                    default:
                        assert false : style;
                }
                return new Whitespace(lines, spaces);
            }

            private boolean isPenultimateTokenBeforeWhitespace(int index, List<FormatToken> formatTokens) {
                boolean result = false;
                int sizeOfTokens = formatTokens.size();
                if (sizeOfTokens > 0) {
                    int lastTokenIndex = formatTokens.size() - 1;
                    FormatToken lastToken = formatTokens.get(lastTokenIndex);
                    result = index + 1 == lastTokenIndex && lastToken.isWhitespace();
                }
                return result;
            }

            private Whitespace countWhiteSpaceBeforeRightBrace(
                    CodeStyle.BracePlacement placement,
                    int currentLine,
                    int addLine,
                    int indent,
                    List<FormatToken> formatTokens,
                    int currentIndex,
                    CharSequence oldText,
                    int lastBracedBlockIndent) {
                int lines;
                int spaces;
                Whitespace result;
                if (placement == CodeStyle.BracePlacement.PRESERVE_EXISTING) {
                    result = countWhiteSpaceForPreserveExistingBracePlacement(oldText, lastBracedBlockIndent);
                } else {
                    lines = addLines(currentLine, addLine);
                    // check whether the } is not before open php tag in html
                    int index = currentIndex;
                    while (index > 0 && (formatTokens.get(index).isWhitespace()
                            || formatTokens.get(index).getId() == FormatToken.Kind.INDENT)) {
                        index--;
                    }
                    if (lines == 0 && formatTokens.get(index).getId() == FormatToken.Kind.OPEN_TAG) {
                        spaces = 1;
                    } else {
                        spaces = placement == CodeStyle.BracePlacement.NEW_LINE_INDENTED ? indent + docOptions.indentSize : indent;
                    }
                    result = new Whitespace(lines, spaces);
                }
                return result;
            }

            private Whitespace countWhiteSpaceForPreserveExistingBracePlacement(CharSequence oldText, int lastIndentOfBracedBlock) {
                int lines = 0;
                int spaces = 0;
                if (oldText != null) {
                    lines = countOfNewLines(oldText);
                    spaces = countOfSpaces(oldText.toString(), docOptions.tabSize);
                    int lastIndexOfNewLine = oldText.toString().lastIndexOf('\n');
                    if (lastIndexOfNewLine != -1) {
                        spaces = countOfSpaces(oldText.toString().substring(lastIndexOfNewLine + 1), docOptions.tabSize);
                    }
                }
                return new Whitespace(lines, spaces + lastIndentOfBracedBlock);
            }

            private Whitespace countWSBeforeKeyword(boolean placeOnNewLine, boolean placeSpaceBefore, int currentIndent, List<FormatToken> formatTokens, int currentIndex) {
                int lines;
                int spaces;
                if (placeOnNewLine) {
                    lines = 1;
                    spaces = currentIndent;
                } else if (isAfterLineComment(formatTokens, currentIndex)) {
                    lines = 1;
                    spaces = currentIndent;
                } else {
                    lines = 0;
                    spaces = placeSpaceBefore ? 1 : 0;
                }
                return new Whitespace(lines, spaces);
            }

            private Whitespace countExistingWS(String whitespace) {
                int lines = countOfNewLines(whitespace);
                String ws = whitespace;
                if (lines > 0) {
                    int lastIndexOfNewLine = whitespace.lastIndexOf('\n');
                    ws = whitespace.substring(lastIndexOfNewLine + 1);
                }
                int spaces = countOfSpaces(ws, docOptions.tabSize);
                return new Whitespace(lines, spaces);
            }

            private boolean isPrecededByBlockedIf(int currentIndex, List<FormatToken> formatTokens) {
                FormatToken possibleClosingCurly = fetchLastTextToken(5, currentIndex, formatTokens);
                return possibleClosingCurly != null && "}".equals(possibleClosingCurly.getOldText()); //NOI18N
            }

            private FormatToken fetchLastTextToken(int limit, int index, List<FormatToken> formatTokens) {
                FormatToken result = null;
                assert formatTokens.size() >= index;
                for (int i = index - 1; limit > 0; i--, limit--) {
                    FormatToken previousToken = formatTokens.get(i);
                    if (previousToken != null && FormatToken.Kind.TEXT.equals(previousToken.getId())) {
                        result = previousToken;
                        break;
                    }
                }
                return result;
            }

            private int countLinesAfter(List<FormatToken> formatTokens, int currentIndex) {
                int lines = -1;

                while (currentIndex < formatTokens.size()
                        && formatTokens.get(currentIndex).isWhitespace()
                        && formatTokens.get(currentIndex).getId() != FormatToken.Kind.WHITESPACE_INDENT) {
                    currentIndex++;
                }

                if (formatTokens.get(currentIndex).getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                    lines = countOfNewLines(formatTokens.get(currentIndex).getOldText());
                }

                return lines;
            }

            private boolean isOpenAndCloseTagOnOneLine(List<FormatToken> formatTokens, int currentIndex) {
                boolean result;
                FormatToken token = formatTokens.get(currentIndex);
                do {
                    token = formatTokens.get(currentIndex);
                    currentIndex++;
                } while (currentIndex < formatTokens.size()
                        && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                        && token.getId() != FormatToken.Kind.CLOSE_TAG);
                if (currentIndex < formatTokens.size() && token.getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                    do {
                        token = formatTokens.get(currentIndex);
                        currentIndex++;
                    } while (currentIndex < formatTokens.size()
                            && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                            && token.getId() != FormatToken.Kind.CLOSE_TAG);
                }
                result = token.getId() == FormatToken.Kind.CLOSE_TAG;
                return result;
            }

            private boolean isCloseAndOpenTagOnOneLine(List<FormatToken> formatTokens, int currentIndex) {
                boolean result = false;
                int helpIndex = currentIndex;
                FormatToken token = formatTokens.get(currentIndex);
                do {
                    token = formatTokens.get(currentIndex);
                    currentIndex--;
                } while (currentIndex > 0
                        && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                        && token.getId() != FormatToken.Kind.OPEN_TAG);
                if ((currentIndex >= 0 && token.getId() == FormatToken.Kind.OPEN_TAG)
                        || (currentIndex >= 0 && token.getId() == FormatToken.Kind.WHITESPACE_INDENT
                        && formatTokens.get(currentIndex).getId() == FormatToken.Kind.WHITESPACE_AFTER_OPEN_PHP_TAG)) {
                    currentIndex = helpIndex;
                    do {
                        token = formatTokens.get(currentIndex);
                        currentIndex++;
                    } while (currentIndex < formatTokens.size()
                            && token.getId() != FormatToken.Kind.WHITESPACE_INDENT
                            && token.getId() != FormatToken.Kind.OPEN_TAG
                            && token.getId() != FormatToken.Kind.CLOSE_TAG);
                    result = token.getId() == FormatToken.Kind.CLOSE_TAG;
                }
                return result;
            }

            private int addLines(int currentCount, int addLines) {
                addLines = addLines + 1;
                if (addLines > 1) {
                    currentCount = addLines > currentCount ? addLines : currentCount;
                }
                return currentCount;
            }

            private int countUnbreakableTextAfter(List<FormatToken> formatTokens, int index) {
                int result = 0;
                FormatToken token;

                do {
                    token = formatTokens.get(index);
                    index++;
                } while (index < formatTokens.size()
                        && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT));
                index--;
                do {
                    token = formatTokens.get(index);
                    if (token.isWhitespace()) {
                        result += token.getOldText() == null ? 0 : 1;
                    } else {
                        result += token.getOldText() == null ? 0 : token.getOldText().length();
                    }
                    result++;  // space after the token
                    index++;
                } while (index < formatTokens.size() && !token.isBreakable());
                result--;
                return result;
            }

            private String formatComment(int index, int indent, String comment) {
                indent = Math.max(indent, 0);
                if (comment == null || comment.length() == 0) {
                    return "";
                }
                StringBuilder sb = new StringBuilder();
                boolean indentLine = false;
                boolean firstLine = true;  // is the first line of the comment?
                String indentString = createWhitespace(docOptions, 0, indent + 1);
                int indexFirstLine = 0;
                while (indexFirstLine < comment.length() && comment.charAt(indexFirstLine) == ' ') {
                    indexFirstLine++;
                }
                if (indexFirstLine < comment.length() && comment.charAt(indexFirstLine) == '\n') {
                    indentLine = true;
                    firstLine = false;
                }
                boolean lastAdded = false; // was the last part added to coment . does it have a non whitespace character?

                for (StringTokenizer st = new StringTokenizer(comment, "\n", true); st.hasMoreTokens();) { //NOI18N
                    String part = st.nextToken();
                    String trimPart = part.trim();
                    if (trimPart.length() > 0 && trimPart.charAt(0) == '*') {
                        sb.append(indentString);
                        part = part.substring(part.indexOf('*'));
                        if (part.length() > 1 && part.charAt(1) != ' ') {
                            sb.append("* "); //NOI18N
                            part = part.substring(1);
                        }
                    } else {
                        if (firstLine) {
                            if (part.charAt(0) != ' ') {
                                sb.append(' ');
                            }
                        } else {
                            if (trimPart.length() > 0) {
                                sb.append(indentString);
                                sb.append(' ');

                                part = trimPart;
                            }
                        }
                    }
                    if (trimPart.length() > 0 || firstLine || "\n".equals(part)) {
                        sb.append(part);
                        lastAdded = true;
                    } else {
                        lastAdded = false;
                    }
                    firstLine = false;
                }


                if (comment.charAt(comment.length() - 1) == '\n') {
                    sb.append(indentString);
                } else {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n' && !lastAdded) {
                        sb.append(indentString);
                    } else {
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
                            sb.setLength(sb.length() - 1); // remove the last new line
                        }
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                            sb.append(' ');
                        }
                        if (sb.length() == 0) {
                            sb.append(' ');
                        }
                    }
                }
                return sb.toString();
            }
            private int startOffset = -1;
            private int endOffset = -1;
            // prviousIndentDelta keeps information, when a template is inserted and
            // the code is not formatted according setted rules. Like initial indentation, etc.
            // Basically it contain difference between number of spaces in document and
            // the position if the document will be formatted according our rules.
            private int previousIndentDelta = 0;
            private String previousOldIndentText = "";
            private String previousNewIndentText = "";

            private int replaceString(BaseDocument document, int offset, int indexInFormatTokens, String oldText, String newText, int delta, boolean templateEdit) {
                if (oldText == null) {
                    oldText = "";
                }

                if (startOffset == -1) {
                    // set the range, where the formatting should be done
                    startOffset = formatContext.startOffset();
                    endOffset = formatContext.endOffset();
                }
                if (startOffset > 0 && (startOffset - oldText.length()) > offset
                        && newText != null && newText.indexOf('\n') > -1) {
                    // will be formatted new line that the first one has to be special
                    previousNewIndentText = newText;
                    previousOldIndentText = oldText;
                }
                if (newText != null && (!oldText.equals(newText)
                        || (startOffset > 0 && (startOffset - oldText.length()) == offset))) {
                    int realOffset = offset + delta;
                    if (startOffset > 0 && (startOffset - oldText.length()) == offset) {
                        // this should be a line with a place, where the template is inserted.
                        if (previousOldIndentText.length() == 0 && previousNewIndentText.length() == 0) {
                            // probably we are at the begining of file, so keep the current possition
                            previousOldIndentText = oldText;
                            previousNewIndentText = newText;
                        }
                        // find difference between the new text and old text of the previous formatting rule
                        int indexOldTextLine = previousOldIndentText.lastIndexOf('\n');
                        int indexNewTextLine = previousNewIndentText.lastIndexOf('\n');

                        previousNewIndentText = indexNewTextLine == -1 ? previousNewIndentText : previousNewIndentText.substring(indexNewTextLine + 1);
                        previousOldIndentText = indexOldTextLine == -1 ? previousOldIndentText : previousOldIndentText.substring(indexOldTextLine + 1);

                        previousIndentDelta = countOfSpaces(previousOldIndentText, docOptions.tabSize)
                                - countOfSpaces(previousNewIndentText, docOptions.tabSize);

                        // find the indent of the new text
                        indexNewTextLine = newText.lastIndexOf('\n');
                        String replaceNew = indexNewTextLine == -1 ? newText : newText.substring(indexNewTextLine + 1);
                        int replaceNewLength = countOfSpaces(replaceNew, docOptions.tabSize);

                        // if there was a difference on the previous line, apply the difference for the current line as well.
                        if (previousIndentDelta != 0 && indexNewTextLine > -1 && (replaceNewLength >= 0)) {
                            replaceNewLength += previousIndentDelta;
                            replaceNew = createWhitespace(docOptions, 0, Math.max(0, replaceNewLength));
                        }
                        indexOldTextLine = oldText.lastIndexOf('\n');
                        String replaceOld = indexOldTextLine == -1 ? oldText : oldText.substring(indexOldTextLine + 1);
                        int replaceOldLength = countOfSpaces(replaceOld, docOptions.tabSize);

                        if (replaceOldLength != replaceNewLength) {
                            delta = replaceSimpleString(document, realOffset + indexOldTextLine + 1, replaceOld, replaceNew, delta);
                            return delta;
                        }
                    }
                    if (startOffset <= realOffset
                            && ((realOffset < endOffset + delta) || (realOffset == endOffset + delta && !templateEdit))) {

                        if (!templateEdit || startOffset == 0) { // if is not in template, then replace simply or is not format selection
                            delta = replaceSimpleString(document, realOffset, oldText, newText, delta);
                        } else {
                            // the replacing has to be done line by line.
                            int indexOldTextLine = oldText.indexOf('\n');
                            int indexNewTextLine = newText.indexOf('\n');
                            int indexOldText = 0;
                            int indexNewText = 0;
                            String replaceOld;
                            String replaceNew;

                            if (indexOldTextLine == -1 && indexNewTextLine == -1) { // no new line in both)
                                delta = replaceSimpleString(document, realOffset, oldText, newText, delta);
                            } else {

                                do {
                                    indexOldTextLine = oldText.indexOf('\n', indexOldText); // NOI18N
                                    indexNewTextLine = newText.indexOf('\n', indexNewText); // NOI18N

                                    if (indexOldTextLine == -1) {
                                        indexOldTextLine = oldText.length();
                                    }
                                    if (indexNewTextLine == -1) {
                                        indexNewTextLine = newText.length();
                                    }
                                    replaceOld = indexOldText == indexOldTextLine && oldText.length() > 0 ? "\n" : oldText.substring(indexOldText, indexOldTextLine); // NOI18N
                                    replaceNew = indexNewText == indexNewTextLine ? "\n" : newText.substring(indexNewText, indexNewTextLine); // NOI18N
                                    if (previousIndentDelta != 0 && indexNewText != indexNewTextLine
                                            && indexNewText > 0
                                            && indexNewTextLine > -1 && (replaceNew.length()) > 0) {
                                        int newSpaces = countOfSpaces(replaceNew, docOptions.tabSize) + previousIndentDelta;
                                        replaceNew = createWhitespace(docOptions, 0, Math.max(0, newSpaces));
                                    }
                                    if (!replaceOld.equals(replaceNew)
                                            && ((indexOldText + replaceOld.length()) <= oldText.length()
                                            || indexNewText == indexNewTextLine)) {

                                        if (newText.trim().length() == 0) {
                                            delta = replaceSimpleString(document, realOffset + indexOldText,
                                                    replaceOld, replaceNew, delta);
                                        } else {
                                            // in template we can move only with whitespaces
                                            // if we will touch a parameter of the template
                                            // then the processing of the template is stopped.
                                            // see issue #197906
                                            int indexOldChar = 0;
                                            int indexNewChar = 0;
                                            while (indexNewChar < replaceNew.length() && indexOldChar < replaceOld.length()) {
                                                char newChar = replaceNew.charAt(indexNewChar);
                                                char oldChar = replaceOld.charAt(indexOldChar);
                                                if (newChar != oldChar) {
                                                    if (Character.isWhitespace(newChar)) {
                                                        delta = replaceSimpleString(document, realOffset + indexOldText + indexNewChar,
                                                                "", "" + newChar, delta);
                                                        indexNewChar++;
                                                    } else {
                                                        if (Character.isWhitespace(oldChar)) {
                                                            delta = replaceSimpleString(document, realOffset + indexOldText + indexNewChar,
                                                                    "" + oldChar, "", delta);
                                                            indexOldChar++;
                                                        }
                                                    }

                                                } else {
                                                    indexNewChar++;
                                                    indexOldChar++;
                                                }
                                            }

                                        }
                                    }
                                    indexOldText = indexOldTextLine + 1; //(indexOldText == indexOldTextLine ? 2 : 1);
                                    indexNewText = indexNewTextLine + 1; //(indexNewText == indexNewTextLine ? 2 : 1);
                                    realOffset = offset + delta;

                                } while (indexOldText < oldText.length()
                                        && indexNewText < newText.length());

                                if (indexOldText >= oldText.length()
                                        && indexNewText < newText.length()) {
                                    StringBuilder sb = new StringBuilder();
                                    boolean addNewLine;
                                    do {
                                        indexNewTextLine = newText.indexOf('\n', indexNewText); // NOI18N
                                        addNewLine = (indexNewTextLine != -1);
                                        if (!addNewLine) {
                                            indexNewTextLine = newText.length();
                                        }
                                        replaceNew = newText.substring(indexNewText, indexNewTextLine == -1 ? newText.length() : indexNewTextLine); // NOI18N
                                        int newSpaces = countOfSpaces(replaceNew, docOptions.tabSize);
                                        if (previousIndentDelta != 0 && indexNewText != indexNewTextLine
                                                && indexNewText > 0
                                                && indexNewTextLine > -1 && (newSpaces > 0)) {
                                            newSpaces = newSpaces + previousIndentDelta;
                                            replaceNew = createWhitespace(docOptions, 0, Math.max(0, newSpaces));
                                        }
                                        sb.append(replaceNew);
                                        if (addNewLine) {
                                            sb.append('\n');   //NOI18N
                                        }
                                        indexNewText = indexNewTextLine + 1;
                                    } while (indexNewText < newText.length());

                                    if (sb.length() > 0) {
                                        delta = replaceSimpleString(document, realOffset + oldText.length(),
                                                "", sb.toString(), delta);
                                    }
                                }
                            }
                        }
                    }
                }
                return delta;
            }

            private int replaceSimpleString(BaseDocument document, int realOffset, String oldText, String newText, int delta) {
                try {
                    int removeLength = 0;
                    if (oldText.length() > 0) {
                        removeLength = realOffset + oldText.length() < document.getLength()
                                ? oldText.length()
                                : document.getLength() - realOffset;
                    }
                    document.replace(realOffset, removeLength, newText, null);
                    delta = delta - oldText.length() + newText.length();
                } catch (BadLocationException ex) {
                    LOGGER.throwing(TokenFormatter.this.getClass().getName(), "replaceSimpleSring", ex); //NOI18N
                }
                return delta;
            }

            private int countLengthOfNextSequence(List<FormatToken> formatTokens, int index) {
                FormatToken token = formatTokens.get(index);
                int length = 0;
                if (token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_START) {
                    index++;
                    token = formatTokens.get(index);
                    int balance = 0;
                    while (index < formatTokens.size()
                            && !(token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_END
                            && balance == 0)) {
                        if (token.getId() == FormatToken.Kind.WHITESPACE) {
                            length += 1;
                        } else if (token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_START) {
                            balance++;
                        } else if (token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_END) {
                            balance--;
                        } else {
                            if (token.getOldText() != null) {
                                length += token.getOldText().length();
                            }
                        }
                        index++;
                        if (index < formatTokens.size()) {
                            token = formatTokens.get(index);
                        }
                    }
                }
                return length;
            }

            private boolean isBeforeEmptyStatement(List<FormatToken> formatTokens, int index) {
                FormatToken token = formatTokens.get(index);
                boolean value;
                index++;
                while (index < formatTokens.size() && (token.getOldText() == null
                        && token.getId() != FormatToken.Kind.WHITESPACE)) {
                    token = formatTokens.get(index);
                    index++;
                }
                value = index < formatTokens.size() && ";".equals(token.getOldText());
                return value;
            }

            private boolean hasNewLineWithinParensForward(int index, List<FormatToken> formatTokens, FormatToken.Kind tokenKind) {
                int helpIndex = index - 1;
                int balance = 0;
                boolean hasNewLine = false;
                if (helpIndex > 0 && isLeftParen(formatTokens.get(helpIndex))) {
                    helpIndex = index + 1;
                    balance++;
                    while (helpIndex < formatTokens.size()) {
                        switch (tokenKind) {
                            case WHITESPACE_WITHIN_METHOD_DECL_PARENS:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.HAS_NEWLINE_WITHIN_METHOD_PARAMS) {
                                    hasNewLine = true;
                                }
                                break;
                            case WHITESPACE_WITHIN_METHOD_CALL_PARENS:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.HAS_NEWLINE_WITHIN_METHOD_CALL_ARGS) {
                                    hasNewLine = true;
                                }
                                break;
                            case WHITESPACE_WITHIN_FOR_PARENS:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.HAS_NEWLINE_WITHIN_FOR) {
                                    hasNewLine = true;
                                }
                                break;
                            default:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                                    hasNewLine = true;
                                }
                                break;
                        }
                        if (hasNewLine) {
                            break;
                        }
                        if (isLeftParen(formatTokens.get(helpIndex))) {
                            balance++;
                        }
                        if (isRightParen(formatTokens.get(helpIndex))) {
                            balance--;
                            if (balance == 0) {
                                break;
                            }
                        }
                        helpIndex++;
                    }
                }
                return hasNewLine;
            }

            private boolean hasNewLineWithinParensBackward(int index, List<FormatToken> formatTokens, FormatToken.Kind tokenKind) {
                int helpIndex = index + 1;
                int balance = 0;
                boolean hasNewLine = false;
                if (helpIndex > 0 && isRightParen(formatTokens.get(helpIndex))) {
                    helpIndex = index - 1;
                    balance++;
                    while (helpIndex > 0) {
                        switch (tokenKind) {
                            case WHITESPACE_WITHIN_METHOD_DECL_PARENS:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.HAS_NEWLINE_WITHIN_METHOD_PARAMS) {
                                    hasNewLine = true;
                                }
                                break;
                            case WHITESPACE_WITHIN_METHOD_CALL_PARENS:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.HAS_NEWLINE_WITHIN_METHOD_CALL_ARGS) {
                                    hasNewLine = true;
                                }
                                break;
                            case WHITESPACE_WITHIN_FOR_PARENS:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.HAS_NEWLINE_WITHIN_FOR) {
                                    hasNewLine = true;
                                }
                                break;
                            default:
                                if (formatTokens.get(helpIndex).getId() == FormatToken.Kind.WHITESPACE_INDENT) {
                                    hasNewLine = true;
                                }
                                break;
                        }
                        if (hasNewLine) {
                            break;
                        }
                        if (isLeftParen(formatTokens.get(helpIndex))) {
                            balance--;
                            if (balance == 0) {
                                break;
                            }
                        }
                        if (isRightParen(formatTokens.get(helpIndex))) {
                            balance++;
                        }
                        helpIndex--;
                    }
                }
                return hasNewLine;
            }

            private boolean isLeftParen(FormatToken formatToken) {
                return formatToken.getId() == FormatToken.Kind.TEXT
                        && "(".equals(formatToken.getOldText()); // NOI18N
            }

            private boolean isRightParen(FormatToken formatToken) {
                return formatToken.getId() == FormatToken.Kind.TEXT
                        && ")".equals(formatToken.getOldText()); // NOI18N
            }
        });
    }

    private static class Whitespace {
        int lines;
        int spaces;

        public Whitespace(int lines, int spaces) {
            this.lines = lines;
            this.spaces = spaces;
        }

    }

    private static int peekLastBracedIndent(final Deque<Integer> lastBracedBlockIndent) {
        final Integer result = lastBracedBlockIndent.peekFirst();
        return result == null ? 0 : result;
    }

    private static int popLastBracedIndent(final Deque<Integer> lastBracedBlockIndent) {
        final Integer result = lastBracedBlockIndent.pollFirst();
        return result == null ? 0 : result;
    }

    /**
     *
     * @param tokens
     * @param index of the whitespace token
     * @return
     */
    private boolean isBeforeLineComment(List<FormatToken> tokens, int index) {
        FormatToken token = tokens.get(index);
        while (index < tokens.size() - 1 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT)) {
            token = tokens.get(++index);
        }
        return token.getId() == FormatToken.Kind.LINE_COMMENT;
    }

    private int countOfSpaces(String text, int tabSize) {
        int spaces = 0;
        int index = 0;
        while (index < text.length()) {
            if (text.charAt(index) == '\t') {
                spaces += tabSize;
            } else {
                spaces++;
            }
            index++;
        }
        return spaces;
    }

    private FormatToken getPreviousNonWhite(List<FormatToken> tokens, int index) {
        if (index < 0) {
            return null;
        }
        FormatToken token = tokens.get(index);
        while (index < tokens.size() - 1 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT)) {
            token = tokens.get(++index);
        }
        return token;
    }

    private boolean isAfterLineComment(List<FormatToken> tokens, int index) {
        FormatToken token = tokens.get(index);
        while (index > 0 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT
                || token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_END)) {
            token = tokens.get(--index);
        }
        return token.getId() == FormatToken.Kind.LINE_COMMENT;
    }

    private boolean isBeforePHPDocOrAttribute(List<FormatToken> tokens, int index) {
        int i = index;
        FormatToken token = tokens.get(i);
        while (i > 0 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT
                || token.getId() == FormatToken.Kind.UNBREAKABLE_SEQUENCE_END)) {
            i++;
            if (i < tokens.size()) {
                token = tokens.get(i);
            }
        }
        if (token.getId() == FormatToken.Kind.LINE_COMMENT) {
            // e.g.
            // // comment
            // #[A]
            while (0 < i && i < tokens.size()
                    && (token.getId() == FormatToken.Kind.LINE_COMMENT
                    || token.getId() == FormatToken.Kind.WHITESPACE_BETWEEN_LINE_COMMENTS
                    || token.getId() == FormatToken.Kind.WHITESPACE_INDENT)) {
                i++;
                if (i < tokens.size()) {
                    token = tokens.get(i);
                }
            }
        }
        return token.getId() == FormatToken.Kind.DOC_COMMENT_START
                || token.getId() == FormatToken.Kind.ATTRIBUTE_START;
    }

    private boolean isBeginLine(List<FormatToken> tokens, int index) {
        FormatToken token = tokens.get(index);
        while (index > 0 && (token.isWhitespace() || token.getId() == FormatToken.Kind.INDENT)
                && token.getId() != FormatToken.Kind.WHITESPACE_INDENT) {
            token = tokens.get(--index);
        }

        return token.getId() == FormatToken.Kind.WHITESPACE_INDENT || token.getId() == FormatToken.Kind.LINE_COMMENT;
    }

    private String createWhitespace(DocumentOptions docOptions, int lines, int spaces) {
        if (lines == 0 && spaces == 0) {
            return EMPTY_STRING;
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lines; i++) {
            sb.append('\n');
        }
        if (spaces > 0) {
            // should be called IndentUtils from editor api, when issue #192289 will be fixed
            sb.append(IndentUtils.cachedOrCreatedIndentString(spaces, docOptions.expandTabsToSpaces, docOptions.tabSize));
        }

        return sb.toString();
    }

    private void resolveNoNewLineAtEOF(BaseDocument document) {
        try {
            String lastChar = getLastChar(document);
            String lineEndings = getLineEndings(document);
            if (!lastChar.equals("\n") && !lastChar.equals("\r")) { // NOI18N
                document.insertString(document.getLength(), lineEndings, null);
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Cannot insert the newline to the EOF. document length: {0}, invalid offset: {1}",
                    new Object[]{document.getLength(), ex.offsetRequested()});
        }
    }

    private String getLastChar(BaseDocument document) throws BadLocationException {
        String lastChar = ""; // NOI18N
        if (document.getLength() > 0) {
            lastChar = document.getText(document.getLength() - 1, 1);
        }
        return lastChar;
    }

    private String getLineEndings(BaseDocument document) {
        String lineEndings = BaseDocument.LS_LF;
        Object lineSeparator = document.getProperty(BaseDocument.READ_LINE_SEPARATOR_PROP);
        if (lineSeparator instanceof String) {
            lineEndings = (String) lineSeparator;
        }
        return lineEndings;
    }

    private boolean addSpaceAroundAssignment(AssignmentAnchorToken token, DocumentOptions docOption) {
        return docOption.expandTabsToSpaces || !token.isInGroup();
    }

    private static final class SpacesCounter {
        private final DocumentOptions documentOptions;

        private SpacesCounter(final DocumentOptions documentOptions) {
            this.documentOptions = documentOptions;
        }

        public int count(final FormatToken.AssignmentAnchorToken token) {
            int spaces = 0;
            if (token.isInGroup()) {
                spaces = countSpacesForGroupedToken(token);
            }
            return spaces;
        }

        private int countSpacesForGroupedToken(final FormatToken.AssignmentAnchorToken token) {
            int spaces;
            if (documentOptions.expandTabsToSpaces) {
                spaces = token.getMaxLength() - token.getLength();
            } else {
                spaces = countSpacesWhenNotExpandingTabs(token);
            }
            return spaces;
        }

        private int countSpacesWhenNotExpandingTabs(final FormatToken.AssignmentAnchorToken token) {
            int spaces;
            int spaceAroundAssignment = getSpaceAroundAssignment(token);
            // consider also the space around assinment here
            // to avoid adding extra spaces before assignment
            final int maxLength = token.getMaxLength() + spaceAroundAssignment;
            final int tokenLength = token.getLength();
            // 1 tabSize will be reduced to tabWidthToComplete...
            int tabWidthToCompleteLengthToTab = documentOptions.tabSize - (tokenLength % documentOptions.tabSize);
            if (tabWidthToCompleteLengthToTab == documentOptions.tabSize) {
                // current item is on the Tab offset
                tabWidthToCompleteLengthToTab = 0;
            }
            boolean hasIncompleteTab = tabWidthToCompleteLengthToTab != 0;

            if (tokenLength == token.getMaxLength()) {
                spaces = spaceAroundAssignment;
            } else {
                spaces = maxLength - tabWidthToCompleteLengthToTab - tokenLength;
                // e.g. tab size: 4, spaceAroundKeyValueOps: true
                // match ($cond) {
                //     666666 => 6,
                //     22 => 2,
                // };
                //     666666 =>
                //     ^^^^^^^
                //     maxLength = 7
                //     22
                //       ^^
                //       tabWidthToCompleteLengthToTab = 2 (has incomplete tab, i.e. must add 4(tab size) instead of 2)
                //     ^^
                //     tokenLength = 2
                //     666666 =>
                //     22
                //         ^^^
                //         spaces = 3 = 7 - 2 - 2
                if (spaces < 0) {
                    // e.g. tab size: 4, spaceAroundKeyValueOps: true, spaces = -1
                    // in this case, add maxLength - tokenLength = 2
                    // match ($cond) {
                    //     22 => 1, // max length = 3
                    //     1 => 0, // tabWidthToCompleteLengthToTab = 3, token length = 1
                    // };
                    spaces = maxLength - tokenLength;
                } else {
                    if (hasIncompleteTab) {
                        spaces += documentOptions.tabSize;
                    }
                }
            }
            return spaces;
        }

        private int getSpaceAroundAssignment(AssignmentAnchorToken token) {
            int space = 0;
            switch (token.getType()) {
                case ARRAY: // no break
                case MATCH_ARM:
                    space = documentOptions.spaceAroundKeyValueOps ? 1 : 0;
                    break;
                case ASSIGNMENT:
                    space = documentOptions.spaceAroundAssignOps ? 1 : 0;
                    break;
                default:
                    assert false : "Unhandled AssignmentAnchorToken.Type: " + token.getType(); // NOI18N
            }
            return space;
        }
    }
}
