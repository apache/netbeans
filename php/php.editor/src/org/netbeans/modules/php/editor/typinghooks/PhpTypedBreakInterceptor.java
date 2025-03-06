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
package org.netbeans.modules.php.editor.typinghooks;

import java.util.Arrays;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.php.editor.lexer.PHPDocCommentTokenId;
import org.netbeans.modules.php.editor.lexer.utils.LexerUtils;
import org.netbeans.modules.php.editor.options.OptionsUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpTypedBreakInterceptor implements TypedBreakInterceptor {

    // XXX: this should made it to options and be supported in java for example
    /**
     * When true, continue comments if you press return in a line comment (that
     * does not also have code on the same line).
     */
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("php.cont.comment"); // NOI18N
    // for unit tests
    private static Boolean INSERT_ASTERISK_TO_PHP_COMMENT = null;

    private PhpDocBodyGenerator phpDocBodyGenerator = PhpDocBodyGenerator.NONE;

    static void setInsertAsteriskToPHPComment(Boolean insert) {
        PhpTypedBreakInterceptor.INSERT_ASTERISK_TO_PHP_COMMENT = insert;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.getDocument();
        int offset = context.getCaretOffset();
        boolean insertMatching = TypingHooksUtils.isInsertMatchingEnabled();
        int lineBegin = LineDocumentUtils.getLineStart(doc, offset);
        int lineEnd = LineDocumentUtils.getLineEnd(doc, offset);
        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return;
        }
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }
        Token<? extends PHPTokenId> token = ts.token();
        TokenId id = token.id();
        int tokenOffsetOnCaret = ts.offset();
        // Insert an end statement? Insert a } marker?
        int[] startOfContext = new int[1];
        PHPTokenId completeIn = insertMatching ? findContextForEnd(ts, offset, startOfContext) : null;
        boolean insert = completeIn != null && isEndMissing(doc, offset, completeIn);
        if (insert) {
            boolean addSemicolon = canBeAddedSemicolonAfterCloseBrace(completeIn, ts);
            int indent = IndentUtils.lineIndent(doc, IndentUtils.lineStartOffset(doc, startOfContext[0]));
            int afterLastNonWhite = LineDocumentUtils.getLineLastNonWhitespace(doc, offset);
            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            StringBuilder sb = new StringBuilder("\n");
            if (offset > afterLastNonWhite || id == PHPTokenId.PHP_CLOSETAG
                    || (offset < afterLastNonWhite && "?>".equals(doc.getText(afterLastNonWhite - 1, 2)))) {
                // don't put php close tag iside. see #167816
                sb.append("\n"); //NOI18N
                sb.append(createIndentString(doc, offset, indent));
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(offset, LineDocumentUtils.getLineEnd(doc, afterLastNonWhite) - offset);
                sb.append(restOfLine);
                sb.append("\n"); //NOI18N
                sb.append(createIndentString(doc, offset, indent));
                doc.remove(offset, restOfLine.length());
            }
            if (id == PHPTokenId.PHP_CLOSETAG && offset > tokenOffsetOnCaret) {
                token = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_OPENTAG));
                String begin = token != null ? token.text().toString() : "<?php"; //NOI18N
                sb.append(begin);
                sb.append(" "); // NOI18N
            }
            if (completeIn == PHPTokenId.PHP_CURLY_OPEN
                    || completeIn == PHPTokenId.PHP_CLASS
                    || completeIn == PHPTokenId.PHP_FUNCTION
                    || completeIn == PHPTokenId.PHP_USE) {
                sb.append("}"); // NOI18N
                if (addSemicolon) {
                    sb.append(";"); // NOI18N
                }
            } else if (completeIn == PHPTokenId.PHP_TRY) {
                sb.append("} catch (Exception $ex) {\n\n").append(createIndentString(doc, offset, indent)).append("}"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_IF || completeIn == PHPTokenId.PHP_ELSE || completeIn == PHPTokenId.PHP_ELSEIF) {
                sb.append("endif;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_FOR) {
                sb.append("endfor;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_FOREACH) {
                sb.append("endforeach;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_WHILE) {
                sb.append("endwhile;"); // NOI18N
            } else if (completeIn == PHPTokenId.PHP_SWITCH) {
                sb.append("endswitch;"); // NOI18N
            }
            if (id == PHPTokenId.PHP_CLOSETAG && offset > tokenOffsetOnCaret) {
                sb.append(" ?>");  //NOI18N
            }

            if (id == PHPTokenId.PHP_CLOSETAG) {
                // place the close tag on the new line.
                sb.append("\n"); //NOI18N
            }
            context.setText(sb.toString(), 0, 1);
            return;
        }
        if ((id == PHPTokenId.PHP_CURLY_CLOSE || LexUtilities.textEquals(token.text(), ']') || LexUtilities.textEquals(token.text(), ')'))) {
            int indent = GsfUtilities.getLineIndent((Document) doc, offset);
            StringBuilder sb = new StringBuilder("\n");
            // the new line will not be added, if we are in middle of array declaration
            if ((LexUtilities.textEquals(token.text(), ')') || LexUtilities.textEquals(token.text(), ']')) && ts.movePrevious()) {
                Token<? extends PHPTokenId> helpToken = LexUtilities.findPrevious(ts, Arrays.asList(
                        PHPTokenId.WHITESPACE,
                        PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_LINE_COMMENT));
                if (helpToken.id() == PHPTokenId.PHP_TOKEN
                        && (helpToken.text().charAt(0) == ',' || helpToken.text().charAt(0) == '(' || helpToken.text().charAt(0) == '[') && ts.movePrevious()) {
                    // e.g.
                    // $array = [^];
                    // $array = array(^);
                    // function something(^){};
                    if (helpToken.text().charAt(0) == '[') {
                        sb.append("\n"); // NOI18N
                    } else if (helpToken.text().charAt(0) == '(') {
                        sb.append("\n"); // NOI18N
                    } else {
                        helpToken = LexUtilities.findPrevious(ts, Arrays.asList(
                                PHPTokenId.WHITESPACE,
                                PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, PHPTokenId.PHPDOC_COMMENT_START,
                                PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                                PHPTokenId.PHP_LINE_COMMENT));
                        if (helpToken.id() == PHPTokenId.PHP_ARRAY || (helpToken.id() == PHPTokenId.PHP_TOKEN && helpToken.text().charAt(0) == '[')) { //NOI18N
                            sb.append("\n"); // NOI18N
                        }
                    }
                } else if (helpToken.id() == PHPTokenId.PHP_ATTRIBUTE) {
                    sb.append("\n"); // NOI18N
                }
                sb.append(IndentUtils.createIndentString(doc, indent));
            } else {
                sb.append("\n"); // NOI18N
                int curlyOpenOffset = findCurlyOpenOffset(ts);
                if (curlyOpenOffset > 0) {
                    sb.append(IndentUtils.createIndentString(doc, GsfUtilities.getLineIndent((Document) doc, curlyOpenOffset)));
                }
            }
            context.setText(sb.toString(), 0, sb.lastIndexOf("\n") != 0 ? sb.lastIndexOf("\n") : sb.toString().length());
            return;
        }
        // Support continual line comments
        if (id == PHPTokenId.WHITESPACE) {
            // Pressing newline in the whitespace before a comment
            // should be identical to pressing newline with the caret
            // at the beginning of the comment
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            if (begin != -1 && offset < begin) {
                ts.move(begin);
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id == PHPTokenId.PHP_LINE_COMMENT
                            || id == PHPTokenId.PHPDOC_COMMENT_START
                            || id == PHPTokenId.PHP_COMMENT_START) {
                        offset = begin;
                    }
                }
            }
        }
        if (id == PHPTokenId.PHP_LINE_COMMENT) {
            // Only do this if the line only contains comments OR if there is content to the right on this line,
            // or if the next line is a comment!
            boolean continueComment = false;
            int begin = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
            // We should only continue comments if the previous line had a comment
            // (and a comment from the beginning, not a trailing comment)
            boolean previousLineWasComment = false;
            int rowStart = LineDocumentUtils.getLineStart(doc, offset);
            if (rowStart > 0) {
                int prevBegin = LineDocumentUtils.getLineFirstNonWhitespace(doc, rowStart - 1);
                if (prevBegin != -1) {
                    Token<? extends PHPTokenId> firstToken = LexUtilities.getToken(doc, prevBegin);
                    if (firstToken != null && firstToken.id() == PHPTokenId.PHP_LINE_COMMENT) {
                        previousLineWasComment = true;
                    }
                }
            }
            // See if we have more input on this comment line (to the right
            // of the inserted newline); if so it's a "split" operation on
            // the comment
            if (previousLineWasComment || offset > begin) {
                if (ts.offset() + token.length() > offset + 1) {
                    // See if the remaining text is just whitespace
                    String trailing = doc.getText(offset, LineDocumentUtils.getLineEnd(doc, offset) - offset);
                    if (trailing.trim().length() != 0 && !trailing.startsWith("//")) { //NOI18N
                        continueComment = true;
                    }
                } else if (CONTINUE_COMMENTS) {
                    // See if the "continue comments" options is turned on, and this is a line that
                    // contains only a comment (after leading whitespace)
                    Token<? extends PHPTokenId> firstToken = LexUtilities.getToken(doc, begin);
                    if (firstToken != null && firstToken.id() == PHPTokenId.PHP_LINE_COMMENT) {
                        continueComment = true;
                    }
                }
                if (!continueComment) {
                    // See if the next line is a comment; if so we want to continue
                    // comments editing the middle of the comment
                    int nextLine = LineDocumentUtils.getLineEnd(doc, offset) + 1;
                    if (nextLine < doc.getLength()) {
                        int nextLineFirst = Utilities.getRowFirstNonWhite(doc, nextLine);
                        if (nextLineFirst != -1) {
                            Token<? extends PHPTokenId> firstToken = LexUtilities.getToken(doc, nextLineFirst);
                            if (firstToken != null && firstToken.id() == PHPTokenId.PHP_LINE_COMMENT) {
                                continueComment = true;
                            }
                        }
                    }
                }
            }
            if (continueComment) {
                // Line comments should continue
                int indent = GsfUtilities.getLineIndent((Document) doc, offset);
                StringBuilder sb = new StringBuilder("\n");
                sb.append(IndentUtils.createIndentString(doc, indent));
                String commentDelimiter = "//"; //NOI18N
                boolean moved = true;
                while (moved && ts.token() != null && ts.token().id() == PHPTokenId.PHP_LINE_COMMENT && !isLineCommentDelimiter(ts.token())) {
                    moved = ts.movePrevious();
                }
                if (isLineCommentDelimiter(ts.token())) {
                    commentDelimiter = ts.token().text().toString();
                }
                sb.append(commentDelimiter);
                // Copy existing indentation
                int afterHash = begin + commentDelimiter.length();
                String line = doc.getText(afterHash, LineDocumentUtils.getLineEnd(doc, afterHash) - afterHash);
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == ' ' || c == '\t') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }
                context.setText(sb.toString(), 0, sb.length());
                return;
            }
        }
        if (id == PHPTokenId.PHPDOC_COMMENT || (id == PHPTokenId.PHPDOC_COMMENT_START && offset > ts.offset()) || id == PHPTokenId.PHPDOC_COMMENT_END) {
            final Object[] ret = beforeBreakInComments(doc, ts, offset, PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END, context);
            boolean isEmptyComment = (Boolean) ret[1];
            if (isEmptyComment) {
                final int indent = GsfUtilities.getLineIndent((Document) doc, ts.offset());
                phpDocBodyGenerator = new PhpDocBodyGeneratorImpl((Integer) ret[0], indent);
            }
            return;
        }
        if (id == PHPTokenId.PHP_COMMENT || id == PHPTokenId.PHP_COMMENT_START || id == PHPTokenId.PHP_COMMENT_END) {
            if (!(id == PHPTokenId.PHP_COMMENT_START && offset == ts.offset())) {
                beforeBreakInComments(doc, ts, offset, PHPTokenId.PHP_COMMENT_START, PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, context);
                return;
            }
        }
        if (OptionsUtils.autoStringConcatination() && concatPossibleStringToken(ts, offset, tokenOffsetOnCaret)) {
            char stringDelimiter = extractStringDelimiter(ts);
            String concatString = stringDelimiter + "\n . " + stringDelimiter; //NOI18N
            context.setText(concatString, 1, concatString.length(), 2, concatString.length() - 1);
        }
    }

    private static String createIndentString(BaseDocument doc, int offset, int previousIndent) {
        return IndentUtils.createIndentString(doc, org.netbeans.modules.php.editor.indent.IndentUtils.countIndent(doc, offset, previousIndent)); //NOI18N
    }

    private static boolean isLineCommentDelimiter(Token<? extends PHPTokenId> token) {
        return token != null && token.id() == PHPTokenId.PHP_LINE_COMMENT
                && (TokenUtilities.textEquals(token.text(), "//") || TokenUtilities.textEquals(token.text(), "#")); // NOI18N
    }

    private static boolean canBeAddedSemicolonAfterCloseBrace(PHPTokenId completeIn, TokenSequence<? extends PHPTokenId> ts) {
        return canBeAddedSemicolonAfterGroupUseCloseBrace(completeIn, ts)
                || canBeAddedSemicolonAfterMatchExpressionCloseBrace(ts);
    }

    private static boolean canBeAddedSemicolonAfterGroupUseCloseBrace(PHPTokenId completeIn, TokenSequence<? extends PHPTokenId> ts) {
        return completeIn == PHPTokenId.PHP_USE
                && isGroupUseCurlyOpen(ts);
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
        phpDocBodyGenerator.generate((BaseDocument) context.getDocument());
        phpDocBodyGenerator = PhpDocBodyGenerator.NONE;
    }

    private PHPTokenId findContextForEnd(TokenSequence<? extends PHPTokenId> ts, int offset, int[] startOfContext) {
        if (ts == null) {
            return null;
        }
        if (ts.offset() != offset) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return null;
            }
        }
        PHPTokenId result = null;
        PHPTokenId previousToken = null;
        if (ts.movePrevious()) {
            previousToken = ts.token().id();
            ts.moveNext();
        }
        if (previousToken == PHPTokenId.PHPDOC_COMMENT_START || previousToken == PHPTokenId.PHP_COMMENT_START) {
            return null;
        }
        boolean foundQuestionMark = false;
        // at fist there should be find a bracket  '{' or column ':'
        Token<? extends PHPTokenId> bracketColumnToken = LexUtilities.findPrevious(ts,
                List.of(PHPTokenId.PHP_COMMENT, PHPTokenId.PHP_COMMENT_END, PHPTokenId.PHP_COMMENT_START,
                PHPTokenId.PHPDOC_COMMENT_START, PHPTokenId.PHPDOC_COMMENT, PHPTokenId.PHPDOC_COMMENT_END,
                PHPTokenId.PHP_LINE_COMMENT, PHPTokenId.WHITESPACE, PHPTokenId.PHP_CLOSETAG));
        if (bracketColumnToken != null
                && (bracketColumnToken.id() == PHPTokenId.PHP_CURLY_OPEN
                || (bracketColumnToken.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(ts.token().text(), ":")))) { // NOI18N
            startOfContext[0] = ts.offset();
            // we are interested only in adding end for { or alternative syntax :
            List<PHPTokenId> lookFor = List.of(PHPTokenId.PHP_CURLY_CLOSE, //PHPTokenId.PHP_SEMICOLON,
                    PHPTokenId.PHP_CURLY_OPEN, // class C { public int $prop {
                    PHPTokenId.PHP_CLASS, PHPTokenId.PHP_FUNCTION, PHPTokenId.PHP_USE,
                    PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF,
                    PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_TRY,
                    PHPTokenId.PHP_DO, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_TOKEN,
                    PHPTokenId.PHP_SWITCH, PHPTokenId.PHP_CASE, PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_DEFAULT,
                    PHPTokenId.PHP_MATCH,
                    PHPTokenId.PHP_PUBLIC, PHPTokenId.PHP_PROTECTED, PHPTokenId.PHP_PRIVATE, // CCP
                    PHPTokenId.PHP_PUBLIC_SET, PHPTokenId.PHP_PROTECTED_SET, PHPTokenId.PHP_PRIVATE_SET
            );
            ts.movePrevious(); // consume "{" or ":"
            Token<? extends PHPTokenId> keyToken = LexUtilities.findPreviousToken(ts, lookFor);
            while (keyToken.id() == PHPTokenId.PHP_TOKEN) {
                if (TokenUtilities.textEquals(keyToken.text(), "?")) { // NOI18N
                    // nullable type prefix or ternary operator
                    foundQuestionMark = true;
                }
                ts.movePrevious();
                keyToken = LexUtilities.findPreviousToken(ts, lookFor);
            }
            if (keyToken.id() == PHPTokenId.PHP_CASE || keyToken.id() == PHPTokenId.PHP_DEFAULT) {
                return null;
            }
            if (bracketColumnToken.id() == PHPTokenId.PHP_CURLY_OPEN) {
                if (keyToken.id() == PHPTokenId.PHP_CLASS || keyToken.id() == PHPTokenId.PHP_TRY) {
                    result = keyToken.id();
                } else if (keyToken.id() == PHPTokenId.PHP_FUNCTION) {
                    // try to find : use function Foo\Bar
                    Token<? extends PHPTokenId> useToken = findPreviousKeyToken(ts, PHPTokenId.PHP_USE);
                    if (useToken != null) {
                        keyToken = useToken;
                    }
                    result = keyToken.id();
                } else if (keyToken.id() == PHPTokenId.PHP_USE) {
                    // try to find : function() use ($a, $b)
                    Token<? extends PHPTokenId> functionToken = findPreviousKeyToken(ts, PHPTokenId.PHP_FUNCTION);
                    if (functionToken != null) {
                        keyToken = functionToken;
                    }
                    result = keyToken.id();
                } else {
                    result = PHPTokenId.PHP_CURLY_OPEN;
                }
            } else {
                if (bracketColumnToken.id() == PHPTokenId.PHP_TOKEN && TokenUtilities.textEquals(bracketColumnToken.text(), ":")) { // NOI18N
                    if (keyToken.id() != PHPTokenId.PHP_OPENTAG
                            && keyToken.id() != PHPTokenId.PHP_CLASS
                            && keyToken.id() != PHPTokenId.PHP_FUNCTION) {
                        result = keyToken.id();
                    }
                }
            }
            if (keyToken.id() != PHPTokenId.PHP_CURLY_CLOSE
                    && keyToken.id() != PHPTokenId.PHP_CURLY_OPEN
                    && keyToken.id() != PHPTokenId.PHP_SEMICOLON) {
                startOfContext[0] = ts.offset();
            }
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        // #270233 also see #196596
        if (foundQuestionMark && result != PHPTokenId.PHP_FUNCTION) {
            return null;
        }
        return result;
    }

    private boolean isEndMissing(BaseDocument doc, int offset, PHPTokenId startTokenId) throws BadLocationException {
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return false;
        }
        ts.move(0);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }
        Token<? extends PHPTokenId> token;
        int curlyBalance = 0;
        boolean curlyProcessed = false;
        if (startTokenId == PHPTokenId.PHP_CURLY_OPEN || startTokenId == PHPTokenId.PHP_FUNCTION
                || startTokenId == PHPTokenId.PHP_CLASS || startTokenId == PHPTokenId.PHP_TRY
                || startTokenId == PHPTokenId.PHP_USE) {
            boolean unfinishedComment = false;
            do {
                token = ts.token();
                if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                    curlyBalance--;
                    curlyProcessed = true;
                } else if (LexerUtils.hasCurlyOpen(token)) {
                    curlyBalance++;
                    curlyProcessed = true;
                } else if (token.id() == PHPTokenId.PHP_COMMENT_START || token.id() == PHPTokenId.PHPDOC_COMMENT_START) {
                    unfinishedComment = true;
                } else if (token.id() == PHPTokenId.PHP_COMMENT_END || token.id() == PHPTokenId.PHPDOC_COMMENT_END) {
                    unfinishedComment = false;
                }
                if (curlyBalance == 0 && curlyProcessed && ts.offset() > offset) {
                    break;
                }
            } while (ts.moveNext());
            if (unfinishedComment) {
                curlyBalance--;
            }
        } else {
            // complete alternative syntax.
            PHPTokenId endTokenId = null;
            if (startTokenId == PHPTokenId.PHP_FOR) {
                endTokenId = PHPTokenId.PHP_ENDFOR;
            } else if (startTokenId == PHPTokenId.PHP_FOREACH) {
                endTokenId = PHPTokenId.PHP_ENDFOREACH;
            } else if (startTokenId == PHPTokenId.PHP_WHILE) {
                endTokenId = PHPTokenId.PHP_ENDWHILE;
            } else if (startTokenId == PHPTokenId.PHP_SWITCH) {
                endTokenId = PHPTokenId.PHP_ENDSWITCH;
            } else if (startTokenId == PHPTokenId.PHP_IF) {
                endTokenId = PHPTokenId.PHP_ENDIF;
            } else if (startTokenId == PHPTokenId.PHP_ELSE || startTokenId == PHPTokenId.PHP_ELSEIF) {
                startTokenId = PHPTokenId.PHP_IF;
                endTokenId = PHPTokenId.PHP_ENDIF;
            }
            ts.move(0);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return false;
            }
            int balance = 0;
            boolean checkAlternativeSyntax = false;
            do {
                token = ts.token();
                if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                    curlyBalance--;
                } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                    curlyBalance++;
                    checkAlternativeSyntax = false;
                } else if (token.id() == startTokenId) {
                    checkAlternativeSyntax = true;
                } else if (token.id() == PHPTokenId.PHP_TOKEN
                        && TokenUtilities.textEquals(token.text(), ":") // NOI18N
                        && checkAlternativeSyntax) {
                    balance++;
                    checkAlternativeSyntax = false;
                } else if (token.id() == endTokenId) {
                    balance--;
                }
            } while (ts.moveNext() && curlyBalance > -1 && balance > -1);
            return balance > 0;

        }
        return curlyBalance > 0;
    }

    private boolean concatPossibleStringToken(TokenSequence<? extends PHPTokenId> ts, int offset, int tokenOffsetOnCaret) {
        assert ts != null;
        boolean concat = false;
        if (!isPartOfHereOrNowDoc(ts)) {
            Token<? extends PHPTokenId> token = ts.token();
            if (token != null) {
                concat = concatCurrentStringToken(ts, offset, tokenOffsetOnCaret);
                PHPTokenId id = token.id();
                if (!concat && id != PHPTokenId.PHP_SEMICOLON && ts.movePrevious()) {
                    concat = concatCurrentStringToken(ts, offset, tokenOffsetOnCaret);
                }
            }
        }
        return concat;
    }

    private int findCurlyOpenOffset(TokenSequence<? extends PHPTokenId> ts) {
        int originalOffset = ts.offset();
        int balance = 0;
        int curlyOpenOffset = -1;
        while (ts.movePrevious()) {
            Token<? extends PHPTokenId> token = ts.token();
            if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                balance++;
            } else if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                if (balance == 0) {
                    curlyOpenOffset = ts.offset();
                    break;
                }
                balance--;
            } else if (LexerUtils.isDollarCurlyOpen(token)) {
                balance--;
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return curlyOpenOffset;
    }

    private static boolean concatCurrentStringToken(TokenSequence<? extends PHPTokenId> ts, int offset, int tokenOffsetOnCaret) {
        assert ts != null;
        boolean concat = false;
        Token<? extends PHPTokenId> token = ts.token();
        if (token != null) {
            PHPTokenId id = token.id();
            if (TypingHooksUtils.isStringToken(token) && !isMultiline(token)) {
                concat = offset != tokenOffsetOnCaret || (id == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE && token.length() != 1);
                if (token.length() == 1) {
                    int original = ts.offset();
                    while (ts.moveNext()) {
                        Token<? extends PHPTokenId> followingToken = ts.token();
                        if (followingToken == null) {
                            break;
                        }
                        PHPTokenId followingTokenId = followingToken.id();
                        if (followingTokenId == PHPTokenId.WHITESPACE) {
                            continue;
                        }
                        concat = !TypingHooksUtils.isStringToken(followingToken) && followingTokenId != PHPTokenId.PHP_CLOSETAG;
                        break;
                    }
                    ts.move(original);
                    ts.moveNext();
                }
            }
        }
        return concat;
    }

    private static boolean isMultiline(Token<? extends PHPTokenId> token) {
        assert token != null;
        return TokenUtilities.indexOf(token.text(), '\n') != -1; //NOI18N
    }

    private static boolean isPartOfHereOrNowDoc(TokenSequence<? extends PHPTokenId> ts) {
        boolean result = false;
        int originalOffset = ts.offset();
        Token<? extends PHPTokenId> token = ts.token();
        if (token != null && TypingHooksUtils.isStringToken(token)) {
            while (ts.movePrevious()) {
                token = ts.token();
                if (token != null) {
                    if (!TypingHooksUtils.isStringToken(token)) {
                        PHPTokenId tokenId = token.id();
                        if (tokenId == PHPTokenId.PHP_HEREDOC_TAG_START || tokenId == PHPTokenId.PHP_NOWDOC_TAG_START) {
                            result = true;
                            break;
                        } else if (tokenId == PHPTokenId.PHP_HEREDOC_TAG_END || tokenId == PHPTokenId.PHP_NOWDOC_TAG_END) {
                            break;
                        }
                    }
                }
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private static char extractStringDelimiter(TokenSequence<? extends PHPTokenId> ts) {
        assert ts != null;
        Token<? extends PHPTokenId> token = ts.token();
        PHPTokenId id = token.id();
        if (TypingHooksUtils.isStringToken(token)) {
            return (id == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE ? '"' : token.text().charAt(0));
        } else if (ts.movePrevious()) {
            token = ts.token();
            id = token.id();
            ts.moveNext();
            if (TypingHooksUtils.isStringToken(token)) {
                return (id == PHPTokenId.PHP_ENCAPSED_AND_WHITESPACE ? '"' : token.text().charAt(0));
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static Object[] beforeBreakInComments(
            BaseDocument doc,
            TokenSequence<? extends PHPTokenId> ts,
            int offset,
            PHPTokenId commentStart,
            PHPTokenId commentBody,
            PHPTokenId commentEnd,
            MutableContext context) throws BadLocationException {
        PHPTokenId id = ts.token().id();
        boolean insertAsterisk = insertAsterisk(commentStart);
        if (id == commentBody || id == commentStart) {
            int insertOffset;
            if (id == commentStart) {
                insertOffset = ts.offset() + ts.token().length();
            } else {
                insertOffset = offset;
            }
            // hofix for #174165
            if (insertOffset > doc.getLength()) {
                insertOffset = doc.getLength();
            }
            int indent = GsfUtilities.getLineIndent(doc, ts.offset());
            int afterLastNonWhite = LineDocumentUtils.getLineLastNonWhitespace(doc, insertOffset);
            // find comment end
            boolean addClosingTag = !isClosedComment(DocumentUtilities.getText(doc), insertOffset);
            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            int newCaretOffset;
            StringBuilder sb = new StringBuilder("\n");
            if (offset > afterLastNonWhite) {
                sb.append(org.netbeans.modules.editor.indent.api.IndentUtils.createIndentString(doc, indent));
                if (insertAsterisk) {
                    sb.append(" * "); // NOI18N
                }
                newCaretOffset = sb.length();
            } else {
                // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                // I should insert the end AFTER the text on the line
                String restOfLine = doc.getText(insertOffset, LineDocumentUtils.getLineEnd(doc, afterLastNonWhite) - insertOffset);
                sb.append(org.netbeans.modules.editor.indent.api.IndentUtils.createIndentString(doc, indent));
                if (insertAsterisk) {
                    sb.append(" * "); // NOI18N
                }
                newCaretOffset = sb.length();
                sb.append(restOfLine);
                doc.remove(insertOffset, restOfLine.length());
            }
            if (addClosingTag) {
                // add the closing tag
                sb.append("\n");
                sb.append(org.netbeans.modules.editor.indent.api.IndentUtils.createIndentString(doc, indent));
                if (insertAsterisk) {
                    sb.append(" */"); // NOI18N
                } else {
                    sb.append("*/"); // NOI18N
                }
            }
            context.setText(sb.toString(), 0, newCaretOffset);
            return new Object[]{insertOffset + newCaretOffset, addClosingTag};
        }
        if (id == commentEnd) {
            int insertOffset = ts.offset();
            // find comment start
            if (ts.movePrevious()) {
                assert ts.token().id() == commentBody
                        || ts.token().id() == commentStart : "PHP_COMMENT_END should not be preceeded by " + ts.token().id().name(); //NOI18N
            } else {
                assert false : "PHP_COMMENT_END without PHP_COMMENT or PHP_COMMENT_START"; //NOI18N
            }
            int indent = GsfUtilities.getLineIndent(doc, ts.offset());
            int beforeFirstNonWhite = Utilities.getRowFirstNonWhite(doc, insertOffset);
            int rowStart = LineDocumentUtils.getLineStart(doc, insertOffset);
            StringBuilder sb = new StringBuilder("\n"); // NOI18N
            int newCaretOffset = 1;
            int newCaretOffset2 = insertOffset;
            if (beforeFirstNonWhite >= insertOffset) {
                // only whitespace in front of */
                sb.append(org.netbeans.modules.editor.indent.api.IndentUtils.createIndentString(doc, indent));
                if (insertAsterisk) {
                    sb.append(" * "); // NOI18N
                }
                newCaretOffset = sb.length();
                newCaretOffset2 = rowStart + newCaretOffset;
                sb.append(org.netbeans.modules.editor.indent.api.IndentUtils.createIndentString(doc, indent));
                sb.append(" "); //NOI18N
                doc.remove(rowStart, insertOffset - rowStart);
            } else {
                sb.append(org.netbeans.modules.editor.indent.api.IndentUtils.createIndentString(doc, indent));
                sb.append(" "); //NOI18N
            }
            context.setText(sb.toString(), 0, newCaretOffset);
            return new Object[]{newCaretOffset2, false};
        }
        return new Object[]{-1, false};
    }

    private static boolean insertAsterisk(PHPTokenId commentStart) {
        return commentStart == PHPTokenId.PHPDOC_COMMENT_START
                || (commentStart == PHPTokenId.PHP_COMMENT_START && insertAsteriskToPHPComment());
    }

    private static boolean insertAsteriskToPHPComment() {
        if (INSERT_ASTERISK_TO_PHP_COMMENT != null) {
            return INSERT_ASTERISK_TO_PHP_COMMENT;
        }
        return OptionsUtils.autoCompletionCommentAsterisk();
    }

    // XXX: stolen from JavaKit.JavaInsertBreakAction, we should extend it to support heredoc
    private static boolean isClosedComment(CharSequence txt, int pos) {
        int length = txt.length();
        int quotation = 0;
        int simpleQuotation = 0;
        for (int i = pos; i < length; i++) {
            char c = txt.charAt(i);
            if (c == '*' && i < length - 1 && txt.charAt(i + 1) == '/') {
                if (quotation == 0 && simpleQuotation == 0 && i < length - 2) {
                    return true;
                }
                // guess it is not just part of some text constant
                boolean isClosed = true;
                for (int j = i + 2; j < length; j++) {
                    char cc = txt.charAt(j);
                    if (cc == '\n') {
                        break;
                    } else if (cc == '"' && j < length - 1 && txt.charAt(j + 1) != '\'') {
                        isClosed = false;
                        break;
                    } else if (cc == '\'' && j < length - 1) {
                        isClosed = false;
                        break;
                    }
                }
                if (isClosed) {
                    return true;
                }
            } else if (c == '/' && i < length - 1 && txt.charAt(i + 1) == '*') {
                // start of another comment block
                return false;
            } else if (c == '\n') {
                quotation = 0;
                simpleQuotation = 0;
            } else if (c == '"' && i < length - 1 && txt.charAt(i + 1) != '\'') {
                quotation = ++quotation % 2;
            } else if (c == '\'' && i < length - 1) {
                simpleQuotation = ++simpleQuotation % 2;
            }
        }
        return false;
    }

    /**
     * Determine if an "end" or "}" is missing following the caret offset. The
     * logic used is to check the text on the current line for block initiators
     * (e.g. "def", "for", "{" etc.) and then see if a corresponding close is
     * found after the same indentation level.
     *
     * @param doc The document to be checked
     * @param offset The offset of the current line
     * @param skipJunk If false, only consider the current line (of the offset)
     * as the possible "block opener"; if true, look backwards across empty
     * lines and comment lines as well.
     * @param insertEndResult Null, or a boolean 1-element array whose first
     * element will be set to true iff this method determines that "end" should
     * be inserted
     * @param insertRBraceResult Null, or a boolean 1-element array whose first
     * element will be set to true iff this method determines that "}" should be
     * inserted
     * @param startOffsetResult Null, or an integer 1-element array whose first
     * element will be set to the starting offset of the opening block.
     * @param indentResult Null, or an integer 1-element array whose first
     * element will be set to the indentation level "end" or "}" should be
     * indented to when inserted.
     * @return true if something is missing; insertEndResult, insertRBraceResult
     * and identResult will provide the more specific return values in their
     * first elements.
     */
    static boolean isEndMissing(BaseDocument doc, int offset, boolean skipJunk,
            boolean[] insertEndResult, boolean[] insertRBraceResult, int[] startOffsetResult,
            int[] indentResult, PHPTokenId insertingEnd) throws BadLocationException {
        if (startOffsetResult != null) {
            startOffsetResult[0] = Utilities.getRowFirstNonWhite(doc, offset);
        }
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return false;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }
        Token<? extends PHPTokenId> token = ts.token();
        int balance = 1;
        boolean endOfFile = false;
        if (insertingEnd == PHPTokenId.PHP_CURLY_CLOSE) {
            while ((token.id() == PHPTokenId.PHP_CURLY_OPEN
                    || token.id() == PHPTokenId.PHP_CURLY_CLOSE
                    || token.id() == PHPTokenId.WHITESPACE)
                    && !endOfFile) {
                if (token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                    balance++;
                } else if (token.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                    balance--;
                }
                if (ts.moveNext()) {
                    token = ts.token();
                } else {
                    endOfFile = true;
                }
            }
            if (endOfFile) {
                if (balance == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Find the specified previous token until ";" or "}".
     *
     * @param ts The token sequence
     * @param id The token id for finding
     * @return The specified token, ";" or "}" if found them, otherwise
     * {@code null}
     */
    @CheckForNull
    private static Token<? extends PHPTokenId> findPreviousKeyToken(TokenSequence<? extends PHPTokenId> ts, PHPTokenId id) {
        Token<? extends PHPTokenId> result = null;
        int originalOffset = ts.offset();
        List<PHPTokenId> lookFor = Arrays.asList(PHPTokenId.PHP_SEMICOLON, PHPTokenId.PHP_CURLY_CLOSE, id);
        Token<? extends PHPTokenId> previousToken = LexUtilities.findPreviousToken(ts, lookFor);
        if (previousToken != null
                && previousToken.id() == id) {
            result = previousToken;
        } else {
            ts.move(originalOffset);
            ts.moveNext();
        }
        return result;
    }

    private static boolean canBeAddedSemicolonAfterMatchExpressionCloseBrace(TokenSequence<? extends PHPTokenId> ts) {
        boolean result = false;
        int originalOffset = ts.offset();
        int curlyBalance = 0;
        while (ts.movePrevious()) {
            PHPTokenId tokenId = ts.token().id();
            if (tokenId == PHPTokenId.PHP_SEMICOLON) {
                break;
            }
            if (tokenId == PHPTokenId.PHP_MATCH && ts.movePrevious()) {
                // match (true) { true => function(){^}
                if (curlyBalance != 1) {
                    break;
                }
                List<PHPTokenId> ignores = Arrays.asList(
                        PHPTokenId.PHPDOC_COMMENT_START,
                        PHPTokenId.PHPDOC_COMMENT,
                        PHPTokenId.PHPDOC_COMMENT_END,
                        PHPTokenId.PHP_COMMENT_START,
                        PHPTokenId.PHP_COMMENT,
                        PHPTokenId.PHP_COMMENT_END,
                        PHPTokenId.PHP_LINE_COMMENT,
                        PHPTokenId.WHITESPACE
                );
                Token<? extends PHPTokenId> token = LexUtilities.findPrevious(ts, ignores);
                if (token.id() == PHPTokenId.PHP_RETURN
                        || token.id() == PHPTokenId.PHP_ECHO) {
                    // return match() {} or echo match() {}
                    result = true;
                } else if (token.id() == PHPTokenId.PHP_OPERATOR
                        && TokenUtilities.textEquals(token.text(), "=") // NOI18N
                        && ts.movePrevious()) {
                    // $variable = match() {}
                    token = LexUtilities.findPrevious(ts, ignores);
                    if (token.id() == PHPTokenId.PHP_VARIABLE && ts.movePrevious()) {
                        token = LexUtilities.findPrevious(ts, ignores);
                        if (token.id() == PHPTokenId.PHP_SEMICOLON
                                || token.id() == PHPTokenId.PHP_CURLY_OPEN
                                || token.id() == PHPTokenId.PHP_CURLY_CLOSE
                                || token.id() == PHPTokenId.PHP_OPENTAG) {
                            result = true;
                        }
                    }
                }
                break;
            } else if (tokenId == PHPTokenId.PHP_CURLY_OPEN) {
                curlyBalance++;
            } else if (tokenId == PHPTokenId.PHP_CURLY_CLOSE) {
                curlyBalance--;
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private static boolean isGroupUseCurlyOpen(TokenSequence<? extends PHPTokenId> ts) {
        boolean result = false;
        int originalOffset = ts.offset();
        while (ts.movePrevious()) {
            PHPTokenId tokenId = ts.token().id();
            if (tokenId != PHPTokenId.WHITESPACE
                    && tokenId != PHPTokenId.PHP_CURLY_OPEN
                    && !isComment(tokenId)) {
                if (tokenId == PHPTokenId.PHP_NS_SEPARATOR) {
                    result = true;
                }
                break;
            }
        }
        ts.move(originalOffset);
        ts.moveNext();
        return result;
    }

    private static boolean isComment(PHPTokenId tokenId) {
        return tokenId == PHPTokenId.PHP_COMMENT_START
                || tokenId == PHPTokenId.PHP_COMMENT
                || tokenId == PHPTokenId.PHP_COMMENT_END
                || tokenId == PHPTokenId.PHPDOC_COMMENT_START
                || tokenId == PHPTokenId.PHPDOC_COMMENT
                || tokenId == PHPTokenId.PHPDOC_COMMENT_END
                || tokenId == PHPTokenId.PHP_LINE_COMMENT;
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void cancelled(Context context) {
    }

    private interface PhpDocBodyGenerator {
        PhpDocBodyGenerator NONE = new PhpDocBodyGenerator() {

            @Override
            public void generate(BaseDocument baseDocument) {
            }
        };

        void generate(BaseDocument baseDocument);
    }

    private static final class PhpDocBodyGeneratorImpl implements PhpDocBodyGenerator {
        private final int offset;
        private final int indent;

        public PhpDocBodyGeneratorImpl(int offset, int indent) {
            this.offset = offset;
            this.indent = indent;
        }

        @Override
        public void generate(BaseDocument baseDocument) {
            PhpCommentGenerator.generateDocTags(baseDocument, offset, indent);
        }
    }

    @MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    public static class PhpFactory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new PhpTypedBreakInterceptor();
        }
    }

    @MimeRegistration(mimeType = PHPDocCommentTokenId.MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    public static class PhpDocFactory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new PhpTypedBreakInterceptor();
        }
    }

}
