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
package org.netbeans.modules.groovy.editor.language;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.openide.util.Exceptions;


/**
 * Formatting and indentation for Groovy.
 *
 * @todo bodies of case statements (in switch) are not indented
 * @todo formats javadoc
 *
 * @author Tor Norbye
 * @author Martin Adamek
 * @author Gopalakrishnan Sankaran
 */
public class GroovyFormatter implements Formatter {

    @Override
    public boolean needsParserResult() {
        return false;
    }

    @Override
    public void reindent(Context context) {
        reindent(context, null, true);
    }

    @Override
    public void reformat(Context context, ParserResult compilationInfo) {
        reindent(context, compilationInfo, false);
    }

    @Override
    public int indentSize() {
        return IndentUtils.indentLevelSize(null);
    }

    @Override
    public int hangingIndentSize() {
        return CodeStylePreferences.get((Document) null).getPreferences().getInt("continuationIndentSize", 4);
    }

    /** Compute the initial balance of brackets at the given offset. */
    private int getFormatStableStart(Document doc, int offset) {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);
        if (ts == null) {
            return 0;
        }

        ts.move(offset);

        if (!ts.movePrevious()) {
            return 0;
        }

        // Look backwards to find a suitable context - a class, module or method definition
        // which we will assume is properly indented and balanced
        do {
            Token<GroovyTokenId> token = ts.token();
            TokenId id = token.id();

            if (id == GroovyTokenId.LITERAL_class) {
                return ts.offset();
            }
        } while (ts.movePrevious());

        return ts.offset();
    }

    private int getTokenBalanceDelta(TokenId id, Token<GroovyTokenId> token,
            LineDocument doc, TokenSequence<GroovyTokenId> ts, boolean includeKeywords) {
        if (id == GroovyTokenId.IDENTIFIER) {
            // In some cases, the [ shows up as an identifier, for example in this expression:
            //  for k, v in sort{|a1, a2| a1[0].id2name <=> a2[0].id2name}
            if (token.length() == 1) {
                char c = token.text().charAt(0);
                if (c == '[') {
                    return 1;
                } else if (c == ']') {
                    // I've seen "]" come instead of a RBRACKET too - for example in RHTML:
                    // <%if session[:user]%>
                    return -1;
                }
            }
        } else if (id == GroovyTokenId.LPAREN || id == GroovyTokenId.LBRACKET || id == GroovyTokenId.LBRACE) {
            return 1;
        } else if (id == GroovyTokenId.RPAREN || id == GroovyTokenId.RBRACKET || id == GroovyTokenId.RBRACE) {
            return -1;
        } else if (includeKeywords) {
            if (LexUtilities.isBeginToken(id, doc, ts)) {
                return 1;
            } else if (id == GroovyTokenId.RBRACE) {
                return -1;
            }
        }

        return 0;
    }

    // TODO RHTML - there can be many discontiguous sections, I've gotta process all of them on the given line
    private int getTokenBalance(LineDocument doc, int begin, int end, boolean includeKeywords) {
        int balance = 0;

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, begin);
        if (ts == null) {
            return 0;
        }

        ts.move(begin);

        if (!ts.moveNext()) {
            return 0;
        }

        do {
            Token<GroovyTokenId> token = ts.token();
            TokenId id = token.id();

            balance += getTokenBalanceDelta(id, token, doc, ts, includeKeywords);
        } while (ts.moveNext() && (ts.offset() < end));

        return balance;
    }

    // This method will indent lines beginning with * by 1 space
    private boolean isJavaDocComment(LineDocument doc, int offset, int endOfLine) throws BadLocationException {
        int pos = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
        if (pos != -1) {
            Token<GroovyTokenId> token = LexUtilities.getToken(doc, pos);
            if (token != null) {
                TokenId id = token.id();
                if (id == GroovyTokenId.BLOCK_COMMENT) {
                    String text = doc.getText(offset, endOfLine - offset);
                    if (text.trim().startsWith("*")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isInLiteral(LineDocument doc, int offset) throws BadLocationException {
        // TODO: Handle arrays better
        // %w(January February March April May June July
        //    August September October November December)
        // I should indent to the same level

        // Can't reformat these at the moment because reindenting a line
        // that is a continued string array causes incremental lexing errors
        // (which further screw up formatting)
        int pos = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
        //int pos = offset;

        if (pos != -1) {
            // I can't look at the first position on the line, since
            // for a string array that is indented, the indentation portion
            // is recorded as a blank identifier
            Token<GroovyTokenId> token = LexUtilities.getToken(doc, pos);

            if (token != null) {
                TokenId id = token.id();
                // If we're in a string literal (or regexp or documentation) leave
                // indentation alone!
                if (id == GroovyTokenId.STRING_LITERAL || id == GroovyTokenId.REGEXP_LITERAL) {
                    // No indentation for literal strings in Groovy, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }
            } else {
                // No Groovy token -- leave the formatting alone!
                return true;
            }
        } else {
            // Empty line inside a string, documentation etc. literal?
            Token<GroovyTokenId> token = LexUtilities.getToken(doc, offset);

            if (token != null) {
                TokenId id = token.id();
                // If we're in a string literal (or regexp or documentation) leave
                // indentation alone!
                if (id == GroovyTokenId.STRING_LITERAL || id == GroovyTokenId.REGEXP_LITERAL) {
                    // No indentation for literal strings in Groovy, since they can
                    // contain newlines. Leave it as is.
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isEndIndent(LineDocument doc, int offset) throws BadLocationException {
        int lineBegin = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);

        if (lineBegin != -1) {
            Token<GroovyTokenId> token = LexUtilities.getToken(doc, lineBegin);

            if (token == null) {
                return false;
            }

            TokenId id = token.id();

            // If the line starts with an end-marker, such as "end", "}", "]", etc.,
            // find the corresponding opening marker, and indent the line to the same
            // offset as the beginning of that line.
            return (LexUtilities.isIndentToken(id) && !LexUtilities.isBeginToken(id, doc, offset)) ||
                id == GroovyTokenId.RBRACE || id == GroovyTokenId.RBRACKET || id == GroovyTokenId.RPAREN;
        }

        return false;
    }

    private boolean isLineContinued(LineDocument doc, int offset, int bracketBalance) throws BadLocationException {
        offset = LineDocumentUtils.getLineLastNonWhitespace(doc, offset);
        if (offset == -1) {
            return false;
        }

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts == null) {
            return false;
        }
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<GroovyTokenId> token = ts.token();

        if (token != null) {
            TokenId id = token.id();

            // http://www.netbeans.org/issues/show_bug.cgi?id=115279
            boolean isContinuationOperator = id == GroovyTokenId.DOT;

            if (ts.offset() == offset && token.length() > 1 && token.text().toString().startsWith("\\")) {
                // Continued lines have different token types
                isContinuationOperator = true;
            }

            if (token.length() == 1 && id == GroovyTokenId.IDENTIFIER && token.text().toString().equals(",")) {
                // If there's a comma it's a continuation operator, but inside arrays, hashes or parentheses
                // parameter lists we should not treat it as such since we'd "double indent" the items, and
                // NOT the first item (where there's no comma, e.g. you'd have
                //  foo(
                //    firstarg,
                //      secondarg,  # indented both by ( and hanging indent ,
                //      thirdarg)
                if (bracketBalance == 0) {
                    isContinuationOperator = true;
                }
            }

            if (isContinuationOperator) {
                // Make sure it's not a case like this:
                //    alias eql? ==
                // or
                //    def ==
                token = LexUtilities.getToken(doc, LineDocumentUtils.getLineFirstNonWhitespace(doc, offset));
                if (token != null) {
                    id = token.id();
                    if (id == GroovyTokenId.LBRACE) {
                        return false;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private void reindent(final Context context, ParserResult info, final boolean indentOnly) {
        Document document = context.document();
        final int endOffset = Math.min(context.endOffset(), document.getLength());

        final LineDocument doc = (LineDocument) document;

        final int startOffset = LineDocumentUtils.getLineStart(doc, context.startOffset());
        final int lineStart = startOffset;
        int initialOffset = 0;
        int initialIndent = 0;
        if (startOffset > 0) {
            int prevOffset = LineDocumentUtils.getLineStart(doc, startOffset - 1);
            initialOffset = getFormatStableStart(doc, prevOffset);
            initialIndent = GsfUtilities.getLineIndent(doc, initialOffset);
        }

        // Build up a set of offsets and indents for lines where I know I need
        // to adjust the offset. I will then go back over the document and adjust
        // lines that are different from the intended indent. By doing piecemeal
        // replacements in the document rather than replacing the whole thing,
        // a lot of things will work better: breakpoints and other line annotations
        // will be left in place, semantic coloring info will not be temporarily
        // damaged, and the caret will stay roughly where it belongs.
        final List<Integer> offsets = new ArrayList<Integer>();
        final List<Integer> indents = new ArrayList<Integer>();

        // When we're formatting sections, include whitespace on empty lines; this
        // is used during live code template insertions for example. However, when
        // wholesale formatting a whole document, leave these lines alone.
        boolean indentEmptyLines = (startOffset != 0 || endOffset != doc.getLength());

        boolean includeEnd = endOffset == doc.getLength() || indentOnly;

        // TODO - remove initialbalance etc.
        computeIndents(doc, initialIndent, initialOffset, endOffset, info, offsets, indents, indentEmptyLines, includeEnd, indentOnly);

        AtomicLockDocument bdoc = LineDocumentUtils.as(doc, AtomicLockDocument.class);
        bdoc.runAtomic(new Runnable() {
            @Override
            public void run() {
                try {
                    // Iterate in reverse order such that offsets are not affected by our edits
                    assert indents.size() == offsets.size();
                    for (int i = indents.size() - 1; i >= 0; i--) {
                        int indent = indents.get(i);
                        int lineBegin = offsets.get(i);

                        if (lineBegin < lineStart) {
                            // We're now outside the region that the user wanted reformatting;
                            // these offsets were computed to get the correct continuation context etc.
                            // for the formatter
                            break;
                        }

                        if (lineBegin == lineStart && i > 0) {
                            // Look at the previous line, and see how it's indented
                            // in the buffer.  If it differs from the computed position,
                            // offset my computed position (thus, I'm only going to adjust
                            // the new line position relative to the existing editing.
                            // This avoids the situation where you're inserting a newline
                            // in the middle of "incorrectly" indented code (e.g. different
                            // size than the IDE is using) and the newline position ending
                            // up "out of sync"
                            int prevOffset = offsets.get(i - 1);
                            int prevIndent = indents.get(i - 1);
                            int actualPrevIndent = GsfUtilities.getLineIndent(doc, prevOffset);
                            if (actualPrevIndent != prevIndent) {
                                // For blank lines, indentation may be 0, so don't adjust in that case
                                if (!(LineDocumentUtils.isLineEmpty(doc, prevOffset) || LineDocumentUtils.isLineWhitespace(doc, prevOffset))) {
                                    indent = actualPrevIndent + (indent - prevIndent);
                                    if (indent < 0) {
                                        indent = 0;
                                    }
                                }
                            }
                        }

                        // Adjust the indent at the given line (specified by offset) to the given indent
                        int currentIndent = GsfUtilities.getLineIndent(doc, lineBegin);

                        if (currentIndent != indent) {
                            context.modifyIndent(lineBegin, indent);
                        }
                    }
                } catch (BadLocationException ble) {
                    Exceptions.printStackTrace(ble);
                }
            }
        });
    }

    private void computeIndents(LineDocument doc, int initialIndent, int startOffset, int endOffset, ParserResult info,
            List<Integer> offsets,
            List<Integer> indents,
            boolean indentEmptyLines, boolean includeEnd, boolean indentOnly
        ) {
        // PENDING:
        // The reformatting APIs in NetBeans should be lexer based. They are still
        // based on the old TokenID apis. Once we get a lexer version, convert this over.
        // I just need -something- in place until that is provided.

        try {
            // Algorithm:
            // Iterate over the range.
            // Accumulate a token balance ( {,(,[, and keywords like class, case, etc. increases the balance,
            //      },),] and "end" decreases it
            // If the line starts with an end marker, indent the line to the level AFTER the token
            // else indent the line to the level BEFORE the token (the level being the balance * indentationSize)
            // Compute the initial balance and indentation level and use that as a "base".
            // If the previous line is not "done" (ends with a comma or a binary operator like "+" etc.
            // add a "hanging indent" modifier.
            // At the end of the day, we're recording a set of line offsets and indents.
            // This can be used either to reformat the buffer, or indent a new line.

            // State:
            int offset = LineDocumentUtils.getLineStart(doc, startOffset); // The line's offset
            int end = endOffset;

            int indentSize = IndentUtils.indentLevelSize(doc);
            int hangingIndentSize = hangingIndentSize();

            // Pending - apply comment formatting too?


            // Build up a set of offsets and indents for lines where I know I need
            // to adjust the offset. I will then go back over the document and adjust
            // lines that are different from the intended indent. By doing piecemeal
            // replacements in the document rather than replacing the whole thing,
            // a lot of things will work better: breakpoints and other line annotations
            // will be left in place, semantic coloring info will not be temporarily
            // damaged, and the caret will stay roughly where it belongs.

            // The token balance at the offset
            int balance = 0;
            // The bracket balance at the offset ( parens, bracket, brace )
            int bracketBalance = 0;
            boolean continued = false;

            while ((!includeEnd && offset < end) || (includeEnd && offset <= end)) {
                int indent; // The indentation to be used for the current line
                int hangingIndent = continued ? (hangingIndentSize) : 0;

                if (isInLiteral(doc, offset)) {
                    // Skip this line - leave formatting as it is prior to reformatting
                    indent = GsfUtilities.getLineIndent(doc, offset);

                } else if (isEndIndent(doc, offset)) {
                    indent = (balance - 1) * indentSize + hangingIndent + initialIndent;
                } else {
                    indent = balance * indentSize + hangingIndent + initialIndent;
                }

                int endOfLine = LineDocumentUtils.getLineEnd(doc, offset) + 1;

                if (isJavaDocComment(doc, offset, endOfLine)) {
                    indent++;
                }

                if (indent < 0) {
                    indent = 0;
                }

                int lineBegin = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);

                // Insert whitespace on empty lines too -- needed for abbreviations expansion
                if (lineBegin != -1 || indentEmptyLines) {
                    // Don't do a hanging indent if we're already indenting beyond the parent level?

                    indents.add(Integer.valueOf(indent));
                    offsets.add(Integer.valueOf(offset));
                }



                if (lineBegin != -1) {
                    balance += getTokenBalance(doc, lineBegin, endOfLine, true);
                    bracketBalance += getTokenBalance(doc, lineBegin, endOfLine, false);
                    continued = isLineContinued(doc, offset, bracketBalance);
                }

                offset = endOfLine;
            }
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
    }
}
