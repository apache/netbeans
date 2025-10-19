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

package org.netbeans.modules.groovy.editor.typinghooks;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;

/**
 *
 * @author Martin Janicek
 */
public class GroovyTypedBreakInterceptor implements TypedBreakInterceptor {

    @MimeRegistration(mimeType = GroovyTokenId.GROOVY_MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    public static class GroovyTypedBreakInterceptorFactory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new GroovyTypedBreakInterceptor();
        }
    }

    /**
     * When true, continue comments if you press return in a line comment (that does not
     * also have code on the same line
     */
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("groovy.cont.comment"); // NOI18N

    
    @Override
    public void insert(MutableContext context) throws BadLocationException {
        BaseDocument doc = (BaseDocument) context.getDocument();
        TokenHierarchy<Document> tokenHierarchy = TokenHierarchy.get(context.getDocument());
        int offset = context.getCaretOffset();
        boolean insertMatching = TypingHooksUtil.isMatchingBracketsEnabled(doc);

        int lineBegin = LineDocumentUtils.getLineStartOffset(doc, offset);
        int lineEnd = LineDocumentUtils.getLineEndOffset(doc, offset);

        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return;
        }

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(tokenHierarchy, offset);

        if (ts == null) {
            return;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }

        Token<GroovyTokenId> token = ts.token();
        GroovyTokenId id = token.id();

        // Insert a missing }
        boolean insertRightBrace = isAddRightBrace(doc, offset);

        if (id != GroovyTokenId.ERROR && id != GroovyTokenId.BLOCK_COMMENT && insertMatching && insertRightBrace) {
            int indent = GsfUtilities.getLineIndent(doc, offset);

            int afterLastNonWhite = Utilities.getRowLastNonWhite(doc, offset);

            // We've either encountered a further indented line, or a line that doesn't
            // look like the end we're after, so insert a matching end.
            StringBuilder sb = new StringBuilder();
            int carretOffset = 0;
            int curlyOffset = getUnbalancedCurlyOffset(doc, offset);
            if (offset > afterLastNonWhite) {
                
                sb.append("\n"); // XXX On Windows, do \r\n?
                sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                carretOffset = sb.length();
                sb.append("\n"); // NOI18N
                if (curlyOffset >= 0) {
                    sb.append(IndentUtils.createIndentString(doc, GsfUtilities.getLineIndent(doc, curlyOffset)));
                } else {
                    sb.append(IndentUtils.createIndentString(doc, indent));
                }
                sb.append("}"); // NOI18N
            } else {
                boolean insert[] = {true};
                int end = getRowOrBlockEnd(doc, offset, insert);
                if (insert[0]) {
                    // I'm inserting a newline in the middle of a sentence, such as the scenario in #118656
                    // I should insert the end AFTER the text on the line
                    String restOfLine = doc.getText(offset,
                            Math.min(end, LineDocumentUtils.getLineEndOffset(doc, afterLastNonWhite)) - offset);
                    sb.append("\n"); // XXX On Windows, do \r\n?
                    sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                    // right brace must be included into the correct context - issue #219683
                    carretOffset = sb.length();

                    sb.append(restOfLine); // NOI18N
                    sb.append("\n"); // NOI18N
                    if (curlyOffset >= 0) {
                        sb.append(IndentUtils.createIndentString(doc, GsfUtilities.getLineIndent(doc, curlyOffset)));
                    } else {
                        sb.append(IndentUtils.createIndentString(doc, indent));
                    }
                    sb.append("}"); // NOI18N
                    doc.remove(offset, restOfLine.length());
                }
                
            }

            if (sb.length() > 0) {
                context.setText(sb.toString(), 0, carretOffset);
            }
            return;
        }

        if (id == GroovyTokenId.ERROR) {
            // See if it's a block comment opener
            String text = token.text().toString();
            if (text.startsWith("/*") && ts.offset() == Utilities.getRowFirstNonWhite(doc, offset)) {
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" * "); // NOI18N
                int carretOffset = sb.length();
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" */"); // NOI18N

                context.setText(sb.toString(), 0, carretOffset);
                return;
            }
        }

        if (id == GroovyTokenId.STRING_LITERAL || id == GroovyTokenId.STRING_CH && offset < ts.offset()+ts.token().length()) {
            String tokenText = token.text().toString();
            if (id == GroovyTokenId.STRING_LITERAL 
                    && (tokenText.startsWith("\"\"\"") || tokenText.startsWith("'''"))) {
                return;
            }
            String str = (id != GroovyTokenId.STRING_LITERAL || offset > ts.offset()) ? "\\n\\\n"  : "\\\n";
            context.setText(str, -1, str.length());
            return;
        }



        if (id == GroovyTokenId.REGEXP_LITERAL && offset < ts.offset()+ts.token().length()) {
            // Instead of splitting a string "foobar" into "foo"+"bar", just insert a \ instead!
            //int indent = GsfUtilities.getLineIndent(doc, offset);
            //doc.insertString(offset, "/ + /", null);
            //caret.setDot(offset+3);
            //return offset + 5 + indent;
            String str = (id != GroovyTokenId.REGEXP_LITERAL || offset > ts.offset()) ? "\\n\\\n"  : "\\\n";
            context.setText(str, -1, str.length());
            return;
        }

        // Special case: since I do hash completion, if you try to type
        //     y = Thread.start {
        //         code here
        //     }
        // you end up with
        //     y = Thread.start {|}
        // If you hit newline at this point, you end up with
        //     y = Thread.start {
        //     |}
        // which is not as helpful as it would be if we were not doing hash-matching
        // (in that case we'd notice the brace imbalance, and insert the closing
        // brace on the line below the insert position, and indent properly.
        // Catch this scenario and handle it properly.
        if ((id == GroovyTokenId.RBRACE || id == GroovyTokenId.RBRACKET) && offset > 0) {
            Token<GroovyTokenId> prevToken = LexUtilities.getToken(doc, offset - 1);
            if (prevToken != null) {
                GroovyTokenId prevTokenId = prevToken.id();
                if (id == GroovyTokenId.RBRACE && prevTokenId == GroovyTokenId.LBRACE ||
                        id == GroovyTokenId.RBRACKET && prevTokenId == GroovyTokenId.LBRACKET) {
                    int indent = GsfUtilities.getLineIndent(doc, offset);
                    StringBuilder sb = new StringBuilder();
                    // XXX On Windows, do \r\n?
                    sb.append("\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                    int carretOffset = sb.length();
                    sb.append("\n"); // NOI18N
                    sb.append(IndentUtils.createIndentString(doc, indent));

                    // should we reindent it automatically ?
                    context.setText(sb.toString(), 0, carretOffset);
                    return;
                }
            }
        }

        if (id == GroovyTokenId.WHITESPACE || id == GroovyTokenId.NLS) {
            // Pressing newline in the whitespace before a comment
            // should be identical to pressing newline with the caret
            // at the beginning of the comment
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            if (begin != -1 && offset < begin) {
                ts.move(begin);
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id == GroovyTokenId.LINE_COMMENT) {
                        offset = begin;
                    }
                }
            }
        }

        if ((id == GroovyTokenId.BLOCK_COMMENT)
                && offset > ts.offset() && offset < ts.offset()+ts.token().length()) {
            // Continue *'s
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            int end = LineDocumentUtils.getLineEndOffset(doc, offset)+1;
            if (begin == -1) {
                begin = end;
            }
            String line = doc.getText(begin, end-begin);
            boolean isBlockStart = line.startsWith("/*") || (begin != -1 && begin < ts.offset());
            if (isBlockStart || line.startsWith("*")) {
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder("\n");
                if (isBlockStart) {
                    indent++;
                }
                int carretPosition;
                sb.append(IndentUtils.createIndentString(doc, indent));
                if (isBlockStart) {
                    // First comment should be propertly indented
                    sb.append("* "); //NOI18N
                    carretPosition = sb.length();
                } else {
                    // Copy existing indentation inside the block
                    sb.append("*"); //NOI18N
                    int afterStar = isBlockStart ? begin+2 : begin+1;
                    line = doc.getText(afterStar, LineDocumentUtils.getLineEndOffset(doc, afterStar)-afterStar);
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c == ' ' || c == '\t') { //NOI18N
                            sb.append(c);
                        } else {
                            break;
                        }
                    }
                    carretPosition = sb.length();
                }

                if (offset == begin && offset > 0) {
                    context.setText(sb.toString(), -1, sb.length());
                    return;
                }
                context.setText(sb.toString(), -1, carretPosition);
                return;
            }
        }

        boolean isComment = id == GroovyTokenId.LINE_COMMENT;
        if (id == GroovyTokenId.EOL) {
            if (ts.movePrevious() && ts.token().id() == GroovyTokenId.LINE_COMMENT) {
                //ts.moveNext();
                isComment = true;
            }
        }

        if (isComment) {
            // Only do this if the line only contains comments OR if there is content to the right on this line,
            // or if the next line is a comment!

            boolean continueComment = false;
            int begin = Utilities.getRowFirstNonWhite(doc, offset);

            // We should only continue comments if the previous line had a comment
            // (and a comment from the beginning, not a trailing comment)
            boolean previousLineWasComment = false;
            boolean nextLineIsComment = false;
            int rowStart = LineDocumentUtils.getLineStartOffset(doc, offset);
            if (rowStart > 0) {
                int prevBegin = Utilities.getRowFirstNonWhite(doc, rowStart - 1);
                if (prevBegin != -1) {
                    Token<GroovyTokenId> firstToken = LexUtilities.getToken(doc, prevBegin);
                    if (firstToken != null && firstToken.id() == GroovyTokenId.LINE_COMMENT) {
                        previousLineWasComment = true;
                    }
                }
            }
            int rowEnd = LineDocumentUtils.getLineEndOffset(doc, offset);
            if (rowEnd < doc.getLength()) {
                int nextBegin = Utilities.getRowFirstNonWhite(doc, rowEnd + 1);
                if (nextBegin != -1) {
                    Token<GroovyTokenId> firstToken = LexUtilities.getToken(doc, nextBegin);
                    if (firstToken != null && firstToken.id() == GroovyTokenId.LINE_COMMENT) {
                        nextLineIsComment = true;
                    }
                }
            }

            // See if we have more input on this comment line (to the right
            // of the inserted newline); if so it's a "split" operation on
            // the comment
            if (previousLineWasComment || nextLineIsComment
                    || (offset > ts.offset() && offset < ts.offset() + ts.token().length())) {
                if (ts.offset() + token.length() > offset + 1) {
                    // See if the remaining text is just whitespace
                    String trailing = doc.getText(offset, LineDocumentUtils.getLineEndOffset(doc, offset) - offset);
                    if (trailing.trim().length() != 0) {
                        continueComment = true;
                    }
                } else if (CONTINUE_COMMENTS) {
                    // See if the "continue comments" options is turned on, and this is a line that
                    // contains only a comment (after leading whitespace)
                    Token<GroovyTokenId> firstToken = LexUtilities.getToken(doc, begin);
                    if (firstToken.id() == GroovyTokenId.LINE_COMMENT) {
                        continueComment = true;
                    }
                }
                if (!continueComment) {
                    // See if the next line is a comment; if so we want to continue
                    // comments editing the middle of the comment
                    int nextLine = LineDocumentUtils.getLineEndOffset(doc, offset) + 1;
                    if (nextLine < doc.getLength()) {
                        int nextLineFirst = Utilities.getRowFirstNonWhite(doc, nextLine);
                        if (nextLineFirst != -1) {
                            Token<GroovyTokenId> firstToken = LexUtilities.getToken(doc, nextLineFirst);
                            if (firstToken != null && firstToken.id() == GroovyTokenId.LINE_COMMENT) {
                                continueComment = true;
                            }
                        }
                    }
                }
            }

            if (continueComment) {
                // Line comments should continue
                int indent = GsfUtilities.getLineIndent(doc, offset);
                StringBuilder sb = new StringBuilder();
                if (offset != begin || offset <= 0) {
                    sb.append("\n");
                }
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append("//"); // NOI18N
                // Copy existing indentation
                int afterSlash = begin + 2;
                String line = doc.getText(afterSlash, LineDocumentUtils.getLineEndOffset(doc, afterSlash) - afterSlash);
                for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (c == ' ' || c == '\t') {
                        sb.append(c);
                    } else {
                        break;
                    }
                }

                if (offset == begin && offset > 0) {
                    int caretPosition = sb.length();
                    sb.append("\n");
                    context.setText(sb.toString(), -1, caretPosition);
                    return;
                }
                context.setText(sb.toString(), -1, sb.length());
                return;
            }
        }
        
        // Just indent the line properly
        int indentSize = getNextLineIndentation(doc, offset);
        if (indentSize > 0) {
            StringBuilder sb = new StringBuilder("\n"); // NOI18N
            sb.append(IndentUtils.createIndentString(doc, indentSize));
            context.setText(sb.toString(), -1, sb.length());
        }
    }

    @Override
    public void afterInsert(Context context) throws BadLocationException {
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void cancelled(Context context) {
    }

    /**
     * Computes the indentation of the next line (after the line break).
     * 
     * @param doc document
     * @param offset current offset
     * @return indentation size
     * @throws BadLocationException 
     */
    private int getNextLineIndentation(BaseDocument doc, int offset) throws BadLocationException {
        int indent = GsfUtilities.getLineIndent(doc, offset);
        int currentOffset = offset;
        while (currentOffset > 0) {
            if (!Utilities.isRowEmpty(doc, currentOffset) && !Utilities.isRowWhite(doc, currentOffset)
                    && !LexUtilities.isCommentOnlyLine(doc, currentOffset)) {
                indent = GsfUtilities.getLineIndent(doc, currentOffset);
                int parenBalance = LexUtilities.getLineBalance(doc, currentOffset,
                        GroovyTokenId.LPAREN, GroovyTokenId.RPAREN);
                if (parenBalance < 0) {
                    break;
                }
                int curlyBalance = LexUtilities.getLineBalance(doc, currentOffset,
                        GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);
                if (curlyBalance > 0) {
                    indent += IndentUtils.indentLevelSize(doc);
                }
                return indent;
            }
            currentOffset = LineDocumentUtils.getLineStartOffset(doc, currentOffset) - 1;
        }

        return indent;
    }

    /**
     * From Java.
     *
     * Resolve whether pairing right curly should be added automatically
     * at the caret position or not.
     * <br>
     * There must be only whitespace or line comment or block comment
     * between the caret position
     * and the left brace and the left brace must be on the same line
     * where the caret is located.
     * <br>
     * The caret must not be "contained" in the opened block comment token.
     *
     * @param doc document in which to operate.
     * @param caretOffset offset of the caret.
     * @return true if a right brace '}' should be added
     *  or false if not.
     */
    private boolean isAddRightBrace(BaseDocument doc, int caretOffset) throws BadLocationException {
        if (LexUtilities.getTokenBalance(doc, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE, caretOffset) <= 0) {
            return false;
        }
        int caretRowStartOffset = LineDocumentUtils.getLineStartOffset(doc, caretOffset);
        TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, caretOffset);
        if (ts == null) {
            return false;
        }
        boolean first = true;
        do {
            if (ts.offset() < caretRowStartOffset) {
                return false;
            }
            GroovyTokenId id = ts.token().id();
            switch (id) {
                case WHITESPACE:
                case LINE_COMMENT:
                    break;
                case BLOCK_COMMENT:
                    if (first && caretOffset > ts.offset() && caretOffset < ts.offset() + ts.token().length()) {
                        // Caret contained within block comment -> do not add anything
                        return false;
                    }
                    break; // Skip
                case LBRACE:
                    return true;
            }
            first = false;
        } while (ts.movePrevious());
        return false;
    }

    /**
     * From Java.
     * 
     * Returns position of the first unpaired closing paren/brace/bracket from the caretOffset
     * till the end of caret row. If there is no such element, position after the last non-white
     * character on the caret row is returned.
     */
    private int getRowOrBlockEnd(BaseDocument doc, int caretOffset, boolean[] insert) throws BadLocationException {
        int rowEnd = org.netbeans.editor.Utilities.getRowLastNonWhite(doc, caretOffset);
        if (rowEnd == -1 || caretOffset >= rowEnd) {
            return caretOffset;
        }
        rowEnd += 1;
        int parenBalance = 0;
        int braceBalance = 0;
        int bracketBalance = 0;
        TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, caretOffset);
        if (ts == null) {
            return caretOffset;
        }
        while (ts.offset() < rowEnd) {
            GroovyTokenId id = ts.token().id();
            switch (id) {
                case SEMI:
                    return ts.offset() + 1;
                case COMMA:
                    return ts.offset();
                case LPAREN:
                    parenBalance++;
                    break;
                case RPAREN:
                    if (parenBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
                case LBRACE:
                    braceBalance++;
                    break;
                case RBRACE:
                    if (braceBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
                case LBRACKET:
                    bracketBalance++;
                    break;
                case RBRACKET:
                    if (bracketBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
            }
            if (!ts.moveNext()) {
                // this might happen in embedded case - line is not at the end
                // but there are no more tokens - for example <script>function foo() {</script>
                if ((caretOffset - ts.offset()) == 1
                        && (bracketBalance == 1 || parenBalance == 1 || braceBalance == 1)) {
                    return caretOffset;
                }
                break;
            }
        }

        insert[0] = false;
        return rowEnd;
    }

    private int getUnbalancedCurlyOffset(BaseDocument doc, int offset) throws BadLocationException {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, offset);
        if (ts == null) {
            return -1;
        }

        int balance = 0;
        while (ts.movePrevious()) {
            Token t = ts.token();

            if (t.id() == GroovyTokenId.RBRACE) {
                balance++;
            } else if (t.id() == GroovyTokenId.LBRACE) {
                balance--;
                if (balance < 0) {
                    return ts.offset();
                }
            }
        }
        return -1;
    }
}
