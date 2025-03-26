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

import java.util.Arrays;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.util.Exceptions;

/**
 * Extracted from Tomasz Slota's PHPNewLineIndenter.
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class IndentationCounter {
    private static final Collection<PHPTokenId> CONTROL_STATEMENT_TOKENS = Arrays.asList(
            PHPTokenId.PHP_DO, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_FOR,
            PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE);
    private Collection<ScopeDelimiter> scopeDelimiters;
    private final BaseDocument doc;
    private final int indentSize;
    private final int continuationSize;
    private final int itemsArrayDeclararionSize;

    public IndentationCounter(BaseDocument doc) {
        this.doc = doc;
        indentSize = CodeStyle.get(doc).getIndentSize();
        continuationSize = CodeStyle.get(doc).getContinuationIndentSize();
        itemsArrayDeclararionSize = CodeStyle.get(doc).getItemsInArrayDeclarationIndentSize();
        int initialIndentSize = CodeStyle.get(doc).getInitialIndent();
        scopeDelimiters = Arrays.asList(
                new ScopeDelimiter(PHPTokenId.PHP_SEMICOLON, 0),
                new ScopeDelimiter(PHPTokenId.PHP_OPENTAG, initialIndentSize),
                new ScopeDelimiter(PHPTokenId.PHP_CURLY_CLOSE, 0),
                new ScopeDelimiter(PHPTokenId.PHP_CURLY_OPEN, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_CASE, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_IF, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_ELSE, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_ELSEIF, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_WHILE, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_DO, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_FOR, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_FOREACH, indentSize),
                new ScopeDelimiter(PHPTokenId.PHP_DEFAULT, indentSize));
    }

    public Indentation count(int caretOffset) {
        Indentation result = Indentation.NONE;
        doc.readLock();
        try {
            result = countUnderReadLock(caretOffset);
        } finally {
            doc.readUnlock();
        }
        return result;
    }

    private Indentation countUnderReadLock(int caretOffset) {
        int newIndent = 0;
        try {
            boolean insideString = false;
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);
            int caretLineStart = LineDocumentUtils.getLineStart(doc, LineDocumentUtils.getLineStart(doc, caretOffset) - 1);
            if (ts != null) {
                ts.move(caretOffset);
                ts.moveNext();

                boolean indentStartComment = false;

                boolean movePrevious = false;
                if (ts.token() == null) {
                    return Indentation.NONE;
                }
                if (ts.token().id() == PHPTokenId.PHP_OPENTAG) {
                    int neOffset = LineDocumentUtils.getPreviousNonWhitespace(doc, caretOffset - 1);
                    Indentation result = Indentation.NONE;
                    if (neOffset != -1) {
                        result = new IndentationImpl(Utilities.getRowIndent(doc, neOffset) + indentSize);
                    }
                    return result;
                }

                if (isAttributeCloseBracket(ts)) {
                    // e.g.
                    // #[A(1, "param")]
                    //                 ^ Enter here
                    int attributeIndent = Utilities.getRowIndent(doc, caretLineStart);
                    return new IndentationImpl(attributeIndent < 0 ? 0 : attributeIndent);
                }

                if (ts.token().id() == PHPTokenId.WHITESPACE && ts.moveNext()) {
                    movePrevious = true;
                }

                // #268621
                if (ts.token().id() == PHPTokenId.PHP_CURLY_OPEN) {
                    newIndent = Utilities.getRowIndent(doc, caretLineStart);
                    if (newIndent < 0) {
                        int caretStart = caretOffset - 1;
                        int caretLineEnd = LineDocumentUtils.getLineEnd(doc, LineDocumentUtils.getLineEnd(doc, caretOffset) - 1);
                        int curlyOffset = ts.offset() - 1;
                        if (caretLineEnd == caretStart) {
                            newIndent = caretStart - caretLineStart;
                        } else if (caretLineEnd < curlyOffset) {
                            // -1 : in this case, caretLineEnd is the top of the next line
                            newIndent = caretLineEnd - 1 - caretLineStart;
                        } else {
                            newIndent = curlyOffset - caretLineStart;
                        }
                        if (newIndent < 0) {
                            newIndent = 0;
                        }
                    }
                    return new IndentationImpl(newIndent);
                }

                if (ts.token().id() == PHPTokenId.PHP_COMMENT
                        || ts.token().id() == PHPTokenId.PHP_LINE_COMMENT
                        || ts.token().id() == PHPTokenId.PHP_COMMENT_START
                        || ts.token().id() == PHPTokenId.PHP_COMMENT_END) {

                    if (ts.token().id() == PHPTokenId.PHP_COMMENT_START && ts.offset() >= caretOffset) {
                        indentStartComment = true;
                    } else {
                        if (!movePrevious) {
                            // don't indent comment - issue #173979
                            return Indentation.NONE;
                        } else {
                            if (ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
                                ts.movePrevious();
                                CharSequence whitespace = ts.token().text();
                                if (ts.movePrevious() && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT) {
                                    int index = 0;
                                    while (index < whitespace.length() && whitespace.charAt(index) != '\n') {
                                        index++;
                                    }
                                    if (index == whitespace.length()) {
                                        // don't indent if the line commnet continue
                                        // the last new line belongs to the line comment
                                        return Indentation.NONE;
                                    }
                                }
                                ts.moveNext();
                                movePrevious = false;
                            }
                        }
                    }
                }
                if (movePrevious) {
                    ts.movePrevious();
                }
                if ((ts.token().id() == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE || ts.token().id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING) && caretOffset > ts.offset()) {

                    int stringLineStart = LineDocumentUtils.getLineStart(doc, ts.offset());

                    if (stringLineStart >= caretLineStart) {
                        // string starts on the same line:
                        // current line indent + continuation size
                        newIndent = Utilities.getRowIndent(doc, stringLineStart) + indentSize;
                    } else {
                        // string starts before:
                        // repeat indent from the previous line
                        newIndent = Utilities.getRowIndent(doc, caretLineStart);
                    }

                    insideString = true;
                }

                int bracketBalance = 0;
                int squaredBalance = 0;
                PHPTokenId previousTokenId = ts.token().id();
                while (!insideString && ts.movePrevious()) {
                    Token token = ts.token();
                    ScopeDelimiter delimiter = getScopeDelimiter(token);
                    int anchor = ts.offset();
                    int shiftAtAncor = 0;

                    if (delimiter != null) {
                        if (delimiter.tokenId == PHPTokenId.PHP_SEMICOLON) {
                            int casePosition = breakProceededByCase(ts); // is after break in case statement?
                            if (casePosition > -1) {
                                newIndent = Utilities.getRowIndent(doc, anchor);
                                if (LineDocumentUtils.getLineStart(doc, casePosition) != caretLineStart) {
                                    // check that case is not on the same line, where enter was pressed
                                    newIndent -= indentSize;
                                }
                                break;
                            }

                            CodeB4BreakData codeB4BreakData = processCodeBeforeBreak(ts, indentStartComment);
                            anchor = codeB4BreakData.expressionStartOffset;
                            shiftAtAncor = codeB4BreakData.indentDelta;

                            if (codeB4BreakData.processedByControlStmt) {
                                newIndent = Utilities.getRowIndent(doc, anchor) - indentSize;
                            } else {
                                newIndent = Utilities.getRowIndent(doc, anchor) + delimiter.indentDelta + shiftAtAncor;
                            }
                            break;
                        } else if (delimiter.tokenId == PHPTokenId.PHP_CURLY_OPEN && ts.movePrevious()) {
                            int startExpression;
                            if (isInMatchExpression(ts.offset(), ts)) {
                                startExpression = findMatchExpressionStart(ts);
                            } else {
                                startExpression = LexUtilities.findStartTokenOfExpression(ts);
                            }
                            newIndent = Utilities.getRowIndent(doc, startExpression) + indentSize;
                            break;
                        }
                        if (anchor >= 0) {
                            newIndent = Utilities.getRowIndent(doc, anchor) + delimiter.indentDelta + shiftAtAncor;
                        }
                        break;
                    } else {
                        if (ts.token().id() == PHPTokenId.PHP_TOKEN
                                || ts.token().id() == PHPTokenId.PHP_ATTRIBUTE
                                || (ts.token().id() == PHPTokenId.PHP_OPERATOR && TokenUtilities.textEquals("=", ts.token().text()))) { // NOI18N
                            char ch = ts.token().text().charAt(0);
                            boolean continualIndent = false;
                            boolean indent = false;
                            switch (ch) {
                                case ')':
                                    bracketBalance++;
                                    break;
                                case '(':
                                    if (bracketBalance == 0) {
                                        continualIndent = true;
                                    }
                                    bracketBalance--;
                                    break;
                                case ']':
                                    squaredBalance++;
                                    break;
                                case '[':
                                    if (squaredBalance == 0) {
                                        continualIndent = true;
                                    }
                                    squaredBalance--;
                                    break;
                                case ',':
                                    continualIndent = true;
                                    break;
                                case '.':
                                    continualIndent = true;
                                    break;
                                case ':':
                                    if (isInTernaryOperatorStatement(ts)) {
                                        continualIndent = true;
                                    } else {
                                        indent = true;
                                    }
                                    break;
                                case '=':
                                    continualIndent = true;
                                    break;
                                default:
                                    //no-op
                            }
                            if (ts.token().id() == PHPTokenId.PHP_ATTRIBUTE) { // #[
                                if (squaredBalance == 0) {
                                    indent = true;
                                }
                                squaredBalance--;
                            }
                            if (continualIndent || indent) {
                                ts.move(caretOffset);
                                ts.movePrevious();
                                int startExpression = LexUtilities.findStartTokenOfExpression(ts);
                                if (startExpression != -1) {
                                    if (continualIndent) {
                                        int offsetArrayDeclaration = offsetArrayDeclaration(startExpression, ts);
                                        if (offsetArrayDeclaration > -1) {
                                            newIndent = Utilities.getRowIndent(doc, offsetArrayDeclaration) + itemsArrayDeclararionSize;
                                        } else if (inGroupUse(startExpression, ts)) {
                                            newIndent = Utilities.getRowIndent(doc, startExpression);
                                        } else if (isInMatchExpression(startExpression, ts)
                                                && isFirstCommaAfterDoubleArrow(startExpression, caretOffset, ts)) {
                                            newIndent = Utilities.getRowIndent(doc, startExpression);
                                        } else if (isInAttributeExpression(caretOffset, ts)) {
                                            newIndent = Utilities.getRowIndent(doc, startExpression) + indentSize;
                                        } else {
                                            newIndent = Utilities.getRowIndent(doc, startExpression) + continuationSize;
                                        }
                                    }
                                    if (indent) {
                                        newIndent = Utilities.getRowIndent(doc, startExpression) + indentSize;
                                    }
                                }
                                break;
                            }
                        } else if ((previousTokenId == PHPTokenId.PHP_OBJECT_OPERATOR
                                || previousTokenId == PHPTokenId.PHP_NULLSAFE_OBJECT_OPERATOR
                                || ts.token().id() == PHPTokenId.PHP_OBJECT_OPERATOR
                                || ts.token().id() == PHPTokenId.PHP_NULLSAFE_OBJECT_OPERATOR
                                || ts.token().id() == PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM) && bracketBalance <= 0) {
                            int startExpression = LexUtilities.findStartTokenOfExpression(ts);
                            if (startExpression != -1) {
                                int rememberOffset = ts.offset();
                                ts.move(startExpression);
                                ts.moveNext();
                                if (ts.token().id() != PHPTokenId.PHP_IF
                                        && ts.token().id() != PHPTokenId.PHP_WHILE
                                        && ts.token().id() != PHPTokenId.PHP_FOR
                                        && ts.token().id() != PHPTokenId.PHP_FOREACH) {
                                    newIndent = Utilities.getRowIndent(doc, startExpression) + continuationSize;
                                    break;
                                } else {
                                    ts.move(rememberOffset);
                                    ts.moveNext();
                                }

                            }
                        } else if (ts.token().id() == PHPTokenId.PHP_PUBLIC || ts.token().id() == PHPTokenId.PHP_PROTECTED
                                || ts.token().id() == PHPTokenId.PHP_PRIVATE || (ts.token().id() == PHPTokenId.PHP_VARIABLE && bracketBalance <= 0)) {
                            int startExpression = LexUtilities.findStartTokenOfExpression(ts);
                            if (startExpression != -1) {
                                newIndent = Utilities.getRowIndent(doc, startExpression) + continuationSize;
                                break;
                            }
                        }
                    }
                    previousTokenId = ts.token().id();
                }

                if (newIndent < 0) {
                    newIndent = 0;
                }
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return new IndentationImpl(newIndent);
    }

    private static boolean isInTernaryOperatorStatement(TokenSequence<? extends PHPTokenId> ts) {
        boolean result = false;
        int originalOffset = ts.offset();
        ts.movePrevious();
        Token<? extends PHPTokenId> previousToken = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN));
        if (previousToken != null && previousToken.id() == PHPTokenId.PHP_TOKEN && previousToken.text().charAt(0) == '?') {
            result = true;
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private CodeB4BreakData processCodeBeforeBreak(TokenSequence ts, boolean indentComment) {
        CodeB4BreakData retunValue = new CodeB4BreakData();
        int origOffset = ts.offset();
        Token token = ts.token();

        if (token.id() == PHPTokenId.PHP_SEMICOLON && ts.movePrevious()) {
            retunValue.expressionStartOffset = LexUtilities.findStartTokenOfExpression(ts);
            boolean hasColon = false;
            ts.move(retunValue.expressionStartOffset);
            ts.moveNext();
            // case ENUM_CASE;
            // case Expression:
            if (ts.token().id() == PHPTokenId.PHP_CASE) {
                while (ts.moveNext() && ts.offset() < origOffset) {
                    TokenId id = ts.token().id();
                    if (ts.token().id().equals(PHPTokenId.PHP_TOKEN)
                            && TokenUtilities.textEquals(ts.token().text(), ":")) { // NOI18N
                        hasColon = true;
                        break;
                    }
                }
                ts.move(retunValue.expressionStartOffset);
                ts.moveNext();
            }
            retunValue.indentDelta = (ts.token().id() == PHPTokenId.PHP_CASE && hasColon) || ts.token().id() == PHPTokenId.PHP_DEFAULT
                    ? indentSize : 0;
            retunValue.processedByControlStmt = false;
            ts.move(origOffset);
            ts.moveNext();
            return retunValue;
        }
        while (ts.movePrevious()) {
            token = ts.token();
            ScopeDelimiter delimiter = getScopeDelimiter(token);
            if (delimiter != null) {
                retunValue.expressionStartOffset = ts.offset();
                retunValue.indentDelta = delimiter.indentDelta;
                if (CONTROL_STATEMENT_TOKENS.contains(delimiter.tokenId)) {
                    retunValue.indentDelta = 0;
                }
                break;
            } else {
                if (indentComment && token.id() == PHPTokenId.WHITESPACE
                        && TokenUtilities.indexOf(token.text(), '\n') != -1
                        && ts.moveNext()) {
                    retunValue.expressionStartOffset = ts.offset();
                    retunValue.indentDelta = 0;
                    break;
                }
            }
        }

        if (token.id() == PHPTokenId.PHP_OPENTAG && ts.moveNext()) {
            // we are at the begining of the php blog
            LexUtilities.findNext(ts, Arrays.asList(
                    PHPTokenId.WHITESPACE,
                    PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                    PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                    PHPTokenId.PHP_LINE_COMMENT));
            retunValue.expressionStartOffset = ts.offset();
            retunValue.indentDelta = 0;
        }
        ts.move(origOffset);
        ts.moveNext();
        return retunValue;
    }

    /**
     * Returns of set of the array declaration, where is the exexpression.
     *
     * @param startExpression
     * @param ts
     * @return
     */
    private static int offsetArrayDeclaration(int startExpression, TokenSequence ts) {
        int result = -1;
        int origOffset = ts.offset();
        Token token;
        int balance = 0;
        int squaredBalance = 0;
        do {
            token = ts.token();
            if (token.id() == PHPTokenId.PHP_TOKEN) {
                switch (token.text().charAt(0)) {
                    case ')':
                        balance--;
                        break;
                    case '(':
                        balance++;
                        break;
                    case ']':
                        squaredBalance--;
                        break;
                    case '[':
                        squaredBalance++;
                        break;
                    default:
                        //no-op
                }
            }
        } while (ts.offset() > startExpression
                && !(token.id() == PHPTokenId.PHP_ARRAY && balance == 1)
                && !(token.id() == PHPTokenId.PHP_TOKEN && squaredBalance == 1)
                && ts.movePrevious());

        if ((token.id() == PHPTokenId.PHP_ARRAY && balance == 1)
                || (token.id() == PHPTokenId.PHP_TOKEN && squaredBalance == 1)) {
            result = ts.offset();
        }
        ts.move(origOffset);
        ts.moveNext();
        return result;
    }

    private boolean inGroupUse(int startExpression, TokenSequence ts) {
        boolean result = false;
        int origOffset = ts.offset();
        ts.move(startExpression);
        // move to start expression
        if (ts.moveNext()
                && ts.movePrevious()) {
            // try to find '{', namespace and then 'use' (possibly with 'const' or 'function')
            boolean openCurlyFound = false;
            boolean namespaceFound = false;
            for (;;) {
                TokenId tokenId = ts.token().id();
                if (tokenId == PHPTokenId.PHP_USE) {
                    result = openCurlyFound && namespaceFound;
                    break;
                } else if (tokenId == PHPTokenId.PHP_CURLY_OPEN) {
                    if (openCurlyFound) {
                        break;
                    }
                    openCurlyFound = true;
                } else if (tokenId == PHPTokenId.PHP_NS_SEPARATOR) {
                    namespaceFound = true;
                } else if (tokenId != PHPTokenId.WHITESPACE
                        && tokenId != PHPTokenId.PHP_STRING
                        && tokenId != PHPTokenId.PHP_CONST
                        && tokenId != PHPTokenId.PHP_FUNCTION) {
                    break;
                }
                if (!ts.movePrevious()) {
                    break;
                }
            }
        }
        ts.move(origOffset);
        ts.moveNext();
        return result;
    }

    private boolean isInMatchExpression(int startExpression, TokenSequence ts) {
        boolean result = false;
        int originalOffset = ts.offset();
        ts.move(startExpression);
        if (ts.moveNext() && ts.movePrevious()) {
            while (ts.movePrevious()) {
                TokenId tokenId = ts.token().id();
                if (tokenId == PHPTokenId.PHP_SEMICOLON) {
                    break;
                }
                if (tokenId == PHPTokenId.PHP_MATCH) {
                    result = true;
                    break;
                }
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private boolean isInAttributeExpression(int caretOffset, TokenSequence ts) {
        boolean result = false;
        int originalOffset = ts.offset();
        ts.move(caretOffset);
        if (ts.moveNext()) {
            if (ts.token().id() == PHPTokenId.PHP_ATTRIBUTE
                    && ts.offset() + ts.token().length() <= caretOffset) {
                result = true;
            }
        }
        // check brakets balance for param list
        // e.g.
        // function foo(#[A(1)] $a, #[A(2)] $b, #[A(3)] $c) {}
        //                                          ^ Enter here
        // #[A("foo")]
        // function foo(#[A(1)] $a, #[A(2)] $b, #[A(3)] $c) {}
        //                         ^ Enter here
        int bracketBalance = 0;
        int parenBlance = 0;
        while (!result && ts.movePrevious()) {
            TokenId tokenId = ts.token().id();
            if (isCloseBracket(ts.token())) {
                bracketBalance--;
            } else if (isOpenBracket(ts.token())) {
                bracketBalance++;
            } else if (isCloseParen(ts.token())) {
                parenBlance--;
            } else if (isOpenParen(ts.token())) {
                parenBlance++;
            }
            if (tokenId == PHPTokenId.PHP_SEMICOLON
                    || tokenId == PHPTokenId.PHP_CURLY_CLOSE) {
                break;
            }
            if (tokenId == PHPTokenId.PHP_ATTRIBUTE) {
                bracketBalance++;
                if (bracketBalance != 0 && parenBlance == 0) {
                    result = true;
                }
                break;
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private int findMatchExpressionStart(TokenSequence<? extends PHPTokenId> ts) {
        int originalOffset = ts.offset();
        Token<? extends PHPTokenId> matchToken = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_MATCH));
        assert matchToken != null;
        int startExpression = ts.offset();
        ts.move(originalOffset);
        ts.moveNext();
        return startExpression;
    }

    private boolean isFirstCommaAfterDoubleArrow(int startExpression, int caretOffset, TokenSequence ts) {
        boolean result = false;
        int originalOffset = ts.offset();
        ts.move(caretOffset);
        int parenBalance = 0; // ()
        int bracketBalance = 0; // []
        int curlyBalance = 0; // {}
        int commaCount = 0;
        if (ts.moveNext() && ts.movePrevious()) {
            for (;;) {
                if (ts.offset() < startExpression) {
                    break;
                }
                TokenId tokenId = ts.token().id();
                if (tokenId == PHPTokenId.PHP_SEMICOLON) {
                    break;
                }
                if (tokenId == PHPTokenId.PHP_TOKEN) {
                    char c = ts.token().text().charAt(0);
                    switch (c) {
                        case '(':
                            parenBalance++;
                            break;
                        case ')':
                            parenBalance--;
                            break;
                        case '[':
                            bracketBalance++;
                            break;
                        case ']':
                            bracketBalance--;
                            break;
                        case ',':
                            if (parenBalance == 0
                                    && bracketBalance == 0
                                    && curlyBalance == 0) {
                                commaCount++;
                            }
                            break;
                        default:
                            break;
                    }
                } else if (tokenId == PHPTokenId.PHP_CURLY_OPEN) {
                    curlyBalance++;
                } else if (tokenId == PHPTokenId.PHP_CURLY_CLOSE) {
                    curlyBalance--;
                } else if (isDoubleArrowOperator(ts.token())) {
                    result = parenBalance == 0
                            && bracketBalance == 0
                            && curlyBalance == 0
                            && commaCount == 1;
                    break;
                }
                if (!ts.movePrevious()) {
                    break;
                }
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private static boolean isDoubleArrowOperator(Token<PHPTokenId> token) {
        return token.id() == PHPTokenId.PHP_OPERATOR
                && TokenUtilities.textEquals("=>", token.text()); // NOI18N
    }

    /**
     *
     * @param ts
     * @return -1 if is not by case or offset of the case keyword
     */
    private int breakProceededByCase(TokenSequence<? extends PHPTokenId> ts) {
        int retunValue = -1;
        int origOffset = ts.offset();

        if (ts.movePrevious()) {
            if (semicolonProceededByBreak(ts)) {
                while (ts.movePrevious()) {
                    PHPTokenId tid = ts.token().id();

                    if (tid == PHPTokenId.PHP_CASE) {
                        retunValue = ts.offset();
                        break;
                    } else if (CONTROL_STATEMENT_TOKENS.contains(tid)) {
                        break;
                    }
                }
            }
        }

        ts.move(origOffset);
        ts.moveNext();

        return retunValue;
    }

    private boolean semicolonProceededByBreak(TokenSequence ts) {
        boolean retunValue = false;

        if (ts.token().id() == PHPTokenId.PHP_BREAK) {
            retunValue = true;
        } else if (ts.token().id() == PHPTokenId.PHP_NUMBER) {
            int origOffset = ts.offset();

            if (ts.movePrevious()) {
                if (ts.token().id() == PHPTokenId.WHITESPACE) {
                    if (ts.movePrevious()) {
                        if (ts.token().id() == PHPTokenId.PHP_BREAK) {
                            retunValue = true;
                        }
                    }
                }
            }

            ts.move(origOffset);
            ts.moveNext();
        }

        return retunValue;
    }

    private ScopeDelimiter getScopeDelimiter(Token token) {
        // TODO: more efficient impl
        for (ScopeDelimiter scopeDelimiter : scopeDelimiters) {
            if (scopeDelimiter.matches(token)) {
                return scopeDelimiter;
            }
        }
        return null;
    }

    /**
     * Check whether the token on the caret is an attribute close bracket for
     * class, method/function, or feilds.
     *
     * @param ts the token sequence
     * @return {@code true} if the token is an attribute close bracket,
     * otherwise {@code false}
     */
    private boolean isAttributeCloseBracket(TokenSequence ts) {
        int originalOffset = ts.offset();
        boolean result = false;
        Token findPrevious = LexUtilities.findPrevious(ts, Arrays.asList(PHPTokenId.WHITESPACE));
        if (findPrevious != null && isCloseBracket(findPrevious)) {
            int balance = -1;
            while (ts.movePrevious()) {
                if (isOpenBracket(ts.token())) {
                    balance++;
                } else if (isCloseBracket(ts.token())) {
                    balance--;
                } else if (ts.token().id() == PHPTokenId.PHP_ATTRIBUTE) {
                    balance++;
                    if (balance == 0) {
                        result = true;
                    }
                    break;
                }
                if (balance == 0) {
                    break;
                }
            }
        }
        if (result) {
            // check for non-whitespace tokens before an attribute of parameters, anonymous function/class
            // not `ts.offset() - 1` but `ts.offset()` to avoid getting the start position of the previous line
            int lineStart = LineDocumentUtils.getLineStart(doc, LineDocumentUtils.getLineStart(doc, ts.offset()));
            while (ts.movePrevious() && ts.offset() >= lineStart) {
                if (ts.token().id() != PHPTokenId.WHITESPACE) {
                    result = false;
                    break;
                }
            }
        }

        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private static boolean isOpenBracket(Token token) {
        return token.id() == PHPTokenId.PHP_TOKEN
                && TokenUtilities.textEquals(token.text(), "["); // NOI18N
    }

    private static boolean isCloseBracket(Token token) {
        return token.id() == PHPTokenId.PHP_TOKEN
                && TokenUtilities.textEquals(token.text(), "]"); // NOI18N
    }

    private static boolean isOpenParen(Token token) {
        return token.id() == PHPTokenId.PHP_TOKEN
                && TokenUtilities.textEquals(token.text(), "("); // NOI18N
    }

    private static boolean isCloseParen(Token token) {
        return token.id() == PHPTokenId.PHP_TOKEN
                && TokenUtilities.textEquals(token.text(), ")"); // NOI18N
    }

    //~ Inner classes
    private static class CodeB4BreakData {
        int expressionStartOffset;
        boolean processedByControlStmt;
        int indentDelta;
    }

    private static class ScopeDelimiter {
        private PHPTokenId tokenId;
        private String tokenContent;
        private int indentDelta;

        public ScopeDelimiter(PHPTokenId tokenId, int indentDelta) {
            this(tokenId, null, indentDelta);
        }

        public ScopeDelimiter(PHPTokenId tokenId, String tokenContent, int indentDelta) {
            this.tokenId = tokenId;
            this.tokenContent = tokenContent;
            this.indentDelta = indentDelta;
        }

        public boolean matches(Token token) {
            if (tokenId != token.id()) {
                return false;
            }
            if (tokenContent != null
                    && TokenUtilities.textEquals(token.text(), tokenContent)) {
                return false;
            }
            return true;
        }
    }

    public interface Indentation {

        Indentation NONE = new Indentation() {

            @Override
            public int getIndentation() {
                return 0;
            }

            @Override
            public void modify(Context context) {
            }

        };

        int getIndentation();
        void modify(Context context);

    }

    private static final class IndentationImpl implements Indentation {
        private final int indentation;

        public IndentationImpl(int indentation) {
            this.indentation = indentation;
        }

        @Override
        public int getIndentation() {
            return indentation;
        }

        @Override
        public void modify(final Context context) {
            assert  context != null;
            context.document().render(new Runnable() {

                @Override
                public void run() {
                    modifyUnderWriteLock(context);
                }
            });
        }

        private void modifyUnderWriteLock(Context context) {
            try {
                context.modifyIndent(LineDocumentUtils.getLineStart((BaseDocument) context.document(), context.caretOffset()), indentation);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

}
