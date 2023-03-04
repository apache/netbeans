/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.EditorDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.javascript2.editor.doc.JsDocumentationCompleter;
import org.netbeans.modules.javascript2.json.api.JsonOptionsQuery;
import org.netbeans.modules.javascript2.lexer.api.JsDocumentationTokenId;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedBreakInterceptor;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
public class JsTypedBreakInterceptor implements TypedBreakInterceptor {

    /**
     * When true, continue comments if you press return in a line comment
     * (that does not also have code on the same line).
     */
    static final boolean CONTINUE_COMMENTS = Boolean.getBoolean("js.cont.comment"); // NOI18N

    // unit testing
    static boolean completeDocumentation = true;

    private static final Logger LOGGER = Logger.getLogger(JsTypedBreakInterceptor.class.getName());

    private final Language<JsTokenId> language;

    private final Predicate<Document> comments;

    private final boolean multiLineLiterals;

    private CommentGenerator commentGenerator = null;

    public JsTypedBreakInterceptor(Language<JsTokenId> language, boolean comments, boolean multiLineLiterals) {
        this(language, (doc) -> comments, multiLineLiterals);
    }

    public JsTypedBreakInterceptor(Language<JsTokenId> language, Predicate<Document> comments, boolean multiLineLiterals) {
        this.language = language;
        this.comments = comments;
        this.multiLineLiterals = multiLineLiterals;
    }

    private boolean isInsertMatchingEnabled() {
        EditorOptions options = EditorOptions.get(language.mimeType());
        if (options != null) {
            return options.getMatchBrackets();
        }

        return true;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        BaseDocument doc = (BaseDocument) context.getDocument();
        TokenHierarchy<BaseDocument> tokenHierarchy = TokenHierarchy.get(doc);
        int offset = context.getCaretOffset();

        int lineBegin = Utilities.getRowStart(doc, offset);
        int lineEnd = Utilities.getRowEnd(doc, offset);

        if (lineBegin == offset && lineEnd == offset) {
            // Pressed return on a blank newline - do nothing
            return;
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                tokenHierarchy, offset, language);

        if (ts == null) {
            return;
        }

        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }

        Token<? extends JsTokenId> token = ts.token();
        JsTokenId id = token.id();

        // Insert a missing }
        if (!id.isError() && isInsertMatchingEnabled() && !isDocToken(id) && isAddRightBrace(doc, offset)) {
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
                    sb.append(IndentUtils.createIndentString(doc, getCurlyIndent(doc, curlyOffset)));
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
                            Math.min(end, Utilities.getRowEnd(doc, afterLastNonWhite)) - offset);
                    sb.append("\n"); // XXX On Windows, do \r\n?
                    sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
                    // right brace must be included into the correct context - issue #219683
                    carretOffset = sb.length();

                    sb.append(restOfLine); // NOI18N
                    sb.append("\n"); // NOI18N
                    if (curlyOffset >= 0) {
                        sb.append(IndentUtils.createIndentString(doc, getCurlyIndent(doc, curlyOffset)));
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

        if (id.isError()) {
            // See if it's a block comment opener
            String text = token.text().toString();
            if (comments.test(doc) && text.startsWith("/*") ) {
                int indent = GsfUtilities.getLineIndent(doc, ts.offset());
                StringBuilder sb = new StringBuilder();
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" * "); // NOI18N
                int carretOffset = sb.length();
                sb.append("\n"); // NOI18N
                sb.append(IndentUtils.createIndentString(doc, indent));
                sb.append(" */"); // NOI18N

                if (text.startsWith("/**")) {
                    // setup comment generator
                    commentGenerator = new CommentGenerator(offset + carretOffset, indent + 1);
                }
                context.setText(sb.toString(), 0, carretOffset);
                return;
            }
        }

        if (multiLineLiterals) {
            if (id == JsTokenId.STRING ||
                    (id == JsTokenId.STRING_END) && offset < ts.offset()+ts.token().length()) {
                // Instead of splitting a string "foobar" into "foo"+"bar", just insert a \ instead!
                //int indent = GsfUtilities.getLineIndent(doc, offset);
                //int delimiterOffset = id == JsTokenId.STRING_END ? ts.offset() : ts.offset()-1;
                //char delimiter = doc.getText(delimiterOffset,1).charAt(0);
                //doc.insertString(offset, delimiter + " + " + delimiter, null);
                //caret.setDot(offset+3);
                //return offset + 5 + indent;
                String str = "\\\n";    //NOI18N
                if (id != JsTokenId.STRING || offset > ts.offset()) {
                    str = "\\n\\\n";    //NOI18N
                    if (offset - ts.offset() < ts.token().length()) {
                        String text = ts.token().text().toString();
                        text = text.substring(0, offset - ts.offset());
                        if(text.endsWith("\\n\\")) {    //NOI18N
                            str = "\n\\n\\";    //NOI18N
                        }
                    }
                }
                context.setText(str, -1, str.length());
                return;
            }
            if (id == JsTokenId.TEMPLATE ||
                    (id == JsTokenId.TEMPLATE_END) && offset < ts.offset()+ts.token().length()) {
                // Instead of indenting it to the previous line as below just insert a newline and finish!
                String str = "\n"; //NOI18N
                context.setText(str, -1, str.length());
                return;
            }



            if (id == JsTokenId.REGEXP ||
                    (id == JsTokenId.REGEXP_END) && offset < ts.offset()+ts.token().length()) {
                // Instead of splitting a string "foobar" into "foo"+"bar", just insert a \ instead!
                //int indent = GsfUtilities.getLineIndent(doc, offset);
                //doc.insertString(offset, "/ + /", null);
                //caret.setDot(offset+3);
                //return offset + 5 + indent;
                String str = (id != JsTokenId.REGEXP || offset > ts.offset()) ? "\\n\\\n"  : "\\\n";
                context.setText(str, -1, str.length());
                return;
            }
        } else {
            final int indent = GsfUtilities.getLineIndent(doc, offset);
            final StringBuilder sb = new StringBuilder();
            sb.append("\n"); // NOI18N
            sb.append(IndentUtils.createIndentString(doc, indent + IndentUtils.indentLevelSize(doc)));
            final int carretOffset = sb.length();
            context.setText(sb.toString(), 0, carretOffset);
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
        if ((id == JsTokenId.BRACKET_RIGHT_CURLY || id == JsTokenId.BRACKET_RIGHT_BRACKET) && offset > 0) {
            Token<? extends JsTokenId> prevToken = LexUtilities.getToken(doc, offset - 1, language);
            if (prevToken != null) {
                JsTokenId prevTokenId = prevToken.id();
                if (id == JsTokenId.BRACKET_RIGHT_CURLY && prevTokenId == JsTokenId.BRACKET_LEFT_CURLY ||
                        id == JsTokenId.BRACKET_RIGHT_BRACKET && prevTokenId == JsTokenId.BRACKET_LEFT_BRACKET) {
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

        if (!comments.test(doc)) {
            return;
        }
        if (id == JsTokenId.WHITESPACE) {
            // Pressing newline in the whitespace before a comment
            // should be identical to pressing newline with the caret
            // at the beginning of the comment
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            if (begin != -1 && offset < begin) {
                ts.move(begin);
                if (ts.moveNext()) {
                    id = ts.token().id();
                    if (id == JsTokenId.LINE_COMMENT) {
                        offset = begin;
                    }
                }
            }
        }

        if ((id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.DOC_COMMENT)
                && offset > ts.offset() && offset < ts.offset()+ts.token().length()) {
            // Continue *'s
            int begin = Utilities.getRowFirstNonWhite(doc, offset);
            int end = Utilities.getRowEnd(doc, offset)+1;
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

                    TokenSequence<? extends JsDocumentationTokenId> jsDocTS =
                            LexUtilities.getJsDocumentationTokenSequence(tokenHierarchy, offset);
                    if (jsDocTS != null) {
                        if (!hasCommentEnd(jsDocTS)) {
                            // setup comment generator
                            commentGenerator = new CommentGenerator(offset + carretPosition, indent);
                            // append end of the comment
                            sb.append("\n").append(IndentUtils.createIndentString(doc, indent)).append("*/"); //NOI18N
                        }
                    }
                } else {
                    // Copy existing indentation inside the block
                    sb.append("*"); //NOI18N
                    int afterStar = isBlockStart ? begin+2 : begin+1;
                    line = doc.getText(afterStar, Utilities.getRowEnd(doc, afterStar)-afterStar);
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

        boolean isComment = id == JsTokenId.LINE_COMMENT;
        if (id == JsTokenId.EOL) {
            if (ts.movePrevious() && ts.token().id() == JsTokenId.LINE_COMMENT) {
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
            int rowStart = Utilities.getRowStart(doc, offset);
            if (rowStart > 0) {
                int prevBegin = Utilities.getRowFirstNonWhite(doc, rowStart - 1);
                if (prevBegin != -1) {
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(
                            doc, prevBegin, language);
                    if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
                        previousLineWasComment = true;
                    }
                }
            }
            int rowEnd = Utilities.getRowEnd(doc, offset);
            if (rowEnd < doc.getLength()) {
                int nextBegin = Utilities.getRowFirstNonWhite(doc, rowEnd + 1);
                if (nextBegin != -1) {
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(
                            doc, nextBegin, language);
                    if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
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
                    String trailing = doc.getText(offset, Utilities.getRowEnd(doc, offset) - offset);
                    if (trailing.trim().length() != 0) {
                        continueComment = true;
                    }
                } else if (CONTINUE_COMMENTS) {
                    // See if the "continue comments" options is turned on, and this is a line that
                    // contains only a comment (after leading whitespace)
                    Token<? extends JsTokenId> firstToken = LexUtilities.getToken(
                            doc, begin, language);
                    if (firstToken.id() == JsTokenId.LINE_COMMENT) {
                        continueComment = true;
                    }
                }
                if (!continueComment) {
                    // See if the next line is a comment; if so we want to continue
                    // comments editing the middle of the comment
                    int nextLine = Utilities.getRowEnd(doc, offset) + 1;
                    if (nextLine < doc.getLength()) {
                        int nextLineFirst = Utilities.getRowFirstNonWhite(doc, nextLine);
                        if (nextLineFirst != -1) {
                            Token<? extends JsTokenId> firstToken = LexUtilities.getToken(
                                    doc, nextLineFirst, language);
                            if (firstToken != null && firstToken.id() == JsTokenId.LINE_COMMENT) {
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
                String line = doc.getText(afterSlash, Utilities.getRowEnd(doc, afterSlash) - afterSlash);
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
        if (completeDocumentation && commentGenerator != null) {
            JsDocumentationCompleter.generateCompleteComment(
                    (BaseDocument) context.getDocument(),
                    commentGenerator.getOffset(),
                    commentGenerator.getIndent());
            commentGenerator = null;
        }
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
                    && !isCommentOnlyLine(doc, currentOffset, language)) {
                indent = GsfUtilities.getLineIndent(doc, currentOffset);
                int parenBalance = getLineBalance(doc, currentOffset,
                        JsTokenId.BRACKET_LEFT_PAREN, JsTokenId.BRACKET_RIGHT_PAREN);
                if (parenBalance < 0) {
                    break;
                }
                int curlyBalance = getLineBalance(doc, currentOffset,
                        JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
                if (curlyBalance > 0) {
                    indent += IndentUtils.indentLevelSize(doc);
                }
                return indent;
            }
            currentOffset = Utilities.getRowStart(doc, currentOffset) - 1;
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
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(doc, caretOffset, language);
        if (ts == null) {
            return false;
        }

        // get balance from start index of the tokenSequence
        ts.moveIndex(0);
        if (!ts.moveNext()) {
            return false;
        }

        int balance = 0;
        boolean balancedAfter = false;

        do {
            Token t = ts.token();

            if (t.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                balance++;
            } else if (t.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                balance--;
            }
        } while (ts.offset() < caretOffset && ts.moveNext());

        for (TokenSequenceIterator tsi = new TokenSequenceIterator(TokenHierarchy.get(doc).tokenSequenceList(ts.languagePath(), caretOffset, doc.getLength()), false); tsi.hasMore();) {
            TokenSequence<?> sq = tsi.getSequence();
            Token t = sq.token();

            if (t.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                balance++;
            } else if (t.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                balance--;
            }
            if (balance == 0
                    && (t.id() == JsTokenId.BRACKET_LEFT_CURLY || t.id() == JsTokenId.BRACKET_RIGHT_CURLY)) {
                balancedAfter = true;
                break;
            }
        }

        if (balance < 0) {
            return false;
        }

        int caretRowStartOffset = org.netbeans.editor.Utilities.getRowStart(doc, caretOffset);
        ts = LexUtilities.getPositionedSequence(doc, caretOffset, language);
        if (ts == null) {
            return false;
        }
        if (ts.offset() == caretOffset && !ts.movePrevious()) {
            return false;
        }
        boolean first = true;
        do {
            if (ts.offset() < caretRowStartOffset) {
                return false;
            }
            JsTokenId id = ts.token().id();
            switch (id) {
                case WHITESPACE:
                case LINE_COMMENT:
                    break;
                case BLOCK_COMMENT:
                case DOC_COMMENT:
                    if (first && caretOffset > ts.offset() && caretOffset < ts.offset() + ts.token().length()) {
                        // Caret contained within block comment -> do not add anything
                        return false;
                    }
                    break; // Skip
                case BRACKET_LEFT_CURLY:
                    return !balancedAfter;
                default:
                    return false;
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
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(
                doc, caretOffset, language);
        if (ts == null) {
            return caretOffset;
        }
        while (ts.offset() < rowEnd) {
            JsTokenId id = ts.token().id();
            switch (id) {
                case OPERATOR_SEMICOLON:
                    return ts.offset() + 1;
                case OPERATOR_COMMA:
                    return ts.offset();
                case BRACKET_LEFT_PAREN:
                    parenBalance++;
                    break;
                case BRACKET_RIGHT_PAREN:
                    if (parenBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
                case BRACKET_LEFT_CURLY:
                    braceBalance++;
                    break;
                case BRACKET_RIGHT_CURLY:
                    if (braceBalance-- == 0) {
                        return ts.offset();
                    }
                    break;
                case BRACKET_LEFT_BRACKET:
                    bracketBalance++;
                    break;
                case BRACKET_RIGHT_BRACKET:
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
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(
                doc, offset, language);
        if (ts == null) {
            return -1;
        }

        int balance = 0;
        while (ts.movePrevious()) {
            Token t = ts.token();

            if (t.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                balance++;
            } else if (t.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                balance--;
                if (balance < 0) {
                    return ts.offset();
                }
            }
        }
        return -1;
    }

    private int getCurlyIndent(BaseDocument doc, int offset) {
        try {
            int lineStart = Utilities.getRowStart(doc, offset, 0);
            TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                    doc, lineStart, language);

            int prevLineStart = -1;
            if (ts != null) {
                do {
                    ts.move(lineStart);
                    if (!ts.moveNext()) {
                        if (prevLineStart >= 0) {
                            return IndentUtils.lineIndent(doc, lineStart);
                        } else {
                            return GsfUtilities.getLineIndent(doc, offset);
                        }
                    }

                    Token<? extends JsTokenId> token = LexUtilities.findNextNonWsNonComment(ts);
                    Token<? extends JsTokenId> nextToken = LexUtilities.findNextNonWsNonComment(ts);
                    if (!LexUtilities.isBinaryOperator(token.id(), nextToken.id())) {
                        ts.move(lineStart);
                        if (!ts.movePrevious()) {
                            return IndentUtils.lineIndent(doc, lineStart);
                        }
                        nextToken = token;
                        token = LexUtilities.findPreviousNonWsNonComment(ts);
                        if (!LexUtilities.isBinaryOperator(token.id(), nextToken.id())) {
                            return IndentUtils.lineIndent(doc, lineStart);
                        }
                    }
                    prevLineStart = lineStart;
                    lineStart = Utilities.getRowStart(doc, lineStart, -1);
                } while (lineStart > 0);

                if (lineStart <= 0) {
                    return IndentUtils.lineIndent(doc, lineStart);
                }
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return GsfUtilities.getLineIndent(doc, offset);
    }

    private boolean isDocToken(JsTokenId id) {
        return id == JsTokenId.BLOCK_COMMENT || id == JsTokenId.DOC_COMMENT;
    }

    private static boolean hasCommentEnd(TokenSequence ts) {
        while (ts.moveNext()) {
            Token<JsDocumentationTokenId> token = ts.token();
            if (token.id() == JsDocumentationTokenId.COMMENT_END) {
                return true;
            } else if (CharSequenceUtilities.endsWith(token.text(), "/")) { //NOI18N
                if (ts.moveNext()) {
                    Token<JsDocumentationTokenId> nextToken = ts.token();
                    if (CharSequenceUtilities.textEquals(nextToken.text(), "/")) { //NOI18N
                        ts.movePrevious();
                        continue;
                    } else if (nextToken.id() == JsDocumentationTokenId.ASTERISK) {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Return true iff the line for the given offset is a JavaScript comment line.
     * This will return false for lines that contain comments (even when the
     * offset is within the comment portion) but also contain code.
     */
    private static boolean isCommentOnlyLine(BaseDocument doc, int offset, Language<JsTokenId> language)
            throws BadLocationException {

        int begin = Utilities.getRowFirstNonWhite(doc, offset);

        if (begin == -1) {
            return false; // whitespace only
        }

        Token<? extends JsTokenId> token = LexUtilities.getToken(doc, begin, language);
        if (token != null) {
            return token.id() == JsTokenId.LINE_COMMENT;
        }

        return false;
    }

    /** Compute the balance of begin/end tokens on the line */
    private static int getLineBalance(BaseDocument doc, int offset, TokenId up, TokenId down) {
        try {
            int begin = Utilities.getRowStart(doc, offset);
            int end = Utilities.getRowEnd(doc, offset);

            TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(doc, begin);
            if (ts == null) {
                return 0;
            }

            ts.move(begin);

            if (!ts.moveNext()) {
                return 0;
            }

            int balance = 0;

            do {
                Token<? extends JsTokenId> token = ts.token();
                TokenId id = token.id();

                if (id == up) {
                    balance++;
                } else if (id == down) {
                    balance--;
                }
            } while (ts.moveNext() && (ts.offset() <= end));

            return balance;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);

            return 0;
        }
    }

    @MimeRegistrations({
        @MimeRegistration(mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE, service = TypedBreakInterceptor.Factory.class),
        @MimeRegistration(mimeType = JsDocumentationTokenId.MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    })
    public static class JsFactory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new JsTypedBreakInterceptor(JsTokenId.javascriptLanguage(), true, true);
        }

    }

    @MimeRegistration(mimeType = JsTokenId.JSON_MIME_TYPE, service = TypedBreakInterceptor.Factory.class)
    public static class JsonFactory implements TypedBreakInterceptor.Factory {

        @Override
        public TypedBreakInterceptor createTypedBreakInterceptor(MimePath mimePath) {
            return new JsTypedBreakInterceptor(
                    JsTokenId.jsonLanguage(),
                    (doc) -> {
                        return Optional.ofNullable(EditorDocumentUtils.getFileObject(doc))
                                .map((fo) -> JsonOptionsQuery.getOptions(fo).isCommentSupported())
                                .orElse(false);
                    },
                    false);
        }

    }

    private static class CommentGenerator {

        private final int offset;
        private final int indent;

        public CommentGenerator(int offset, int indent) {
            this.offset = offset;
            this.indent = indent;
        }

        public int getIndent() {
            return indent;
        }

        public int getOffset() {
            return offset;
        }

    }
}
