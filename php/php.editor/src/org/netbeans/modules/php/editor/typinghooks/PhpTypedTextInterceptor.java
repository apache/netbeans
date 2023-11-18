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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.editor.indent.IndentUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PhpTypedTextInterceptor implements TypedTextInterceptor {
    private static final Logger LOGGER = Logger.getLogger(PhpTypedTextInterceptor.class.getName());
    /**
     * When != -1, this indicates that we previously adjusted the indentation of
     * the line to the given offset, and if it turns out that the user changes
     * that token, we revert to the original indentation
     */
    private int previousAdjustmentOffset = -1;
    /**
     * True iff we're processing bracket matching AFTER the key has been
     * inserted rather than before
     */
    private boolean isAfter;
    /**
     * The indentation to revert to when previousAdjustmentOffset is set and the
     * token changed
     */
    private int previousAdjustmentIndent;
    private boolean codeTemplateEditing;
    private boolean bracketCompleted;

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        isAfter = false;
        codeTemplateEditing = false;
        bracketCompleted = false;
        Document document = context.getDocument();
        BaseDocument doc = (BaseDocument) document;
        int caretOffset = context.getOffset();
        char ch = context.getText().charAt(0);
        if (doNotAutoComplete(ch) || caretOffset == 0) {
            return;
        }
        String selection = context.getReplacedText();
        if (selection != null && selection.length() > 0) {
            codeTemplateEditing = GsfUtilities.isCodeTemplateEditing(doc);
            if (!codeTemplateEditing && (ch == '"' || ch == '\'' || ch == '(' || ch == '{' || ch == '[')) {
                char firstChar = selection.charAt(0);
                if (firstChar != ch) {
                    TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPositionedSequence(doc, caretOffset);
                    if (ts != null && (!TypingHooksUtils.isStringToken(ts.token()) || firstChar == '\"' || firstChar == '\'')) {
                        int lastChar = selection.charAt(selection.length() - 1);
                        // Replace the surround-with chars?
                        if (selection.length() > 1
                                && ((firstChar == '"' || firstChar == '\'' || firstChar == '('
                                || firstChar == '{' || firstChar == '[')
                                && lastChar == matching(firstChar))) {
                            String innerText = selection.substring(1, selection.length() - 1);
                            String text = Character.toString(ch) + innerText + Character.toString(matching(ch));
                            context.setText(text, text.length());
                            bracketCompleted = true;
                        } else if (selection.length() == 1 && (firstChar == '"' || firstChar == '\'')) {
                            if (ts.token().id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING) {
                                String original = ts.token().text().toString();
                                if (original.length() > 1) {
                                    String text = ch + original.substring(1, original.length() - 1) + ch;
                                    doc.remove(ts.offset(), text.length());
                                    context.setText(text, text.length() - 1);
                                } else {
                                    return;
                                }
                            }
                        } else {
                            String text = ch + selection + matching(ch);
                            context.setText(text, text.length());
                            bracketCompleted = true;
                        }
                        return;
                    }
                }
            }
        }

        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);
        if (ts == null) {
            return;
        }
        ts.move(caretOffset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }
        Token<? extends PHPTokenId> token = ts.token();
        TokenId id = token.id();
        if (id == PHPTokenId.PHP_LINE_COMMENT && selection != null && selection.length() > 0) {
            if (ch == '*' || ch == '+' || ch == '_') {
                if (selection.charAt(0) != ch && selection.indexOf(' ') == -1) {
                    String text = ch + selection + matching(ch);
                    context.setText(text, text.length());
                    return;
                }
            }
        }
        if (ch == '"' || ch == '\'') {
            completeQuote(context);
        }
    }

    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        final BaseDocument doc = (BaseDocument) context.getDocument();
        doc.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {
                try {
                    afterInsertUnderWriteLock(context);
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.FINE, null, ex);
                }
            }
        });
    }

    private void afterInsertUnderWriteLock(Context context) throws BadLocationException {
        isAfter = true;
        JTextComponent target = context.getComponent();
        Caret caret = target.getCaret();
        BaseDocument doc = (BaseDocument) context.getDocument();
        int dotPos = context.getOffset();
        char ch = context.getText().charAt(0);
        // See if our automatic adjustment of indentation when typing (for example) "end" was
        // premature - if you were typing a longer word beginning with one of my adjustment
        // prefixes, such as "endian", then put the indentation back.
        if (previousAdjustmentOffset != -1) {
            if (dotPos == previousAdjustmentOffset) {
                // Revert indentation iff the character at the insert position does
                // not start a new token (e.g. the previous token that we reindented
                // was not complete)
                TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, dotPos);
                if (ts != null) {
                    ts.move(dotPos);
                    if (ts.moveNext() && (ts.offset() < dotPos)) {
                        GsfUtilities.setLineIndentation((Document) doc, dotPos, previousAdjustmentIndent);
                    }
                }
            }
            previousAdjustmentOffset = -1;
        }
        switch (ch) {
            case '}':
            case '{':
            case ')':
            case ']':
            case '(':
            case '[':
            case '\t':
            case ' ':
            case ':':
                if (!TypingHooksUtils.isInsertMatchingEnabled() && ch != '{' && ch != '}') {
                    return;
                }
                Token<? extends PHPTokenId> token = LexUtilities.getToken(doc, dotPos);
                if (token == null) {
                    return;
                }
                TokenId id = token.id();

                if (((id == PHPTokenId.PHP_VARIABLE) && (token.length() == 1))
                        || (LexUtilities.textEquals(token.text(), '[')) || (LexUtilities.textEquals(token.text(), ']'))
                        || (LexUtilities.textEquals(token.text(), '(')) || (LexUtilities.textEquals(token.text(), ')'))
                        || id == PHPTokenId.PHP_ATTRIBUTE /* #[ */) {
                    if (ch == ']' || ch == ')') {
                        skipClosingBracket(doc, caret, ch);
                    } else if ((ch == '[') || (ch == '(')) {
                        completeOpeningBracket(doc, dotPos, caret, ch);
                    }
                } else if (id == PHPTokenId.PHP_CASTING && ch == ')') {
                    skipClosingBracket(doc, caret, ch);
                }

                // Reindent blocks (won't do anything if } is not at the beginning of a line
                if (ch == '}') {
                    reindent(doc, dotPos, PHPTokenId.PHP_CURLY_CLOSE, caret);
                } else if (ch == '{') {
                    reindent(doc, dotPos, PHPTokenId.PHP_CURLY_OPEN, caret);
                } else if (ch == '\t' || ch == ' ') {
                    reindent(doc, dotPos, PHPTokenId.WHITESPACE, caret);
                } else if (ch == ':') {
                    reindent(doc, dotPos, PHPTokenId.PHP_TOKEN, caret);
                }
                break;
            default:
                //no-op
        }
    }

    @Override
    public void cancelled(Context context) {
    }

    private boolean doNotAutoComplete(final char ch) {
        return (!TypingHooksUtils.isInsertMatchingEnabled() && isBracket(ch)) || (isQuote(ch) && !OptionsUtils.autoCompletionSmartQuotes());
    }

    private boolean isBracket(final char ch) {
        return isOpeningBracket(ch) || isClosingBracket(ch);
    }

    private boolean isOpeningBracket(final char ch) {
        return ch == '(' || ch == '{' || ch == '[';
    }

    private boolean isClosingBracket(final char ch) {
        return ch == ')' || ch == '}' || ch == ']';
    }

    private boolean isQuote(final char ch) {
        return ch == '"' || ch == '\'';
    }

    private boolean isQuote(final Token<? extends PHPTokenId> token) {
        return isQuote(token.text().charAt(0));
    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private char matching(char bracket) {
        switch (bracket) {
            case '(':
                return ')';
            case '/':
                return '/';
            case '[':
                return ']';
            case '\"':
                return '\"'; // NOI18N
            case '\'':
                return '\'';
            case '{':
                return '}';
            case '}':
                return '{';
            default:
                return bracket;
        }
    }

    /**
     * Check for conditions and possibly complete an already inserted quote .
     */
    private void completeQuote(MutableContext context) throws BadLocationException {
        int dotPos = context.getOffset();
        BaseDocument doc = (BaseDocument) context.getDocument();
        char bracket = context.getText().charAt(0);
        if (codeTemplateEditing) {
            String text = context.getText() + bracket;
            context.setText(text, text.length() - 1);
            return;
        }
        // No chars completion when escaping, eg \" or \' typed
        if (isEscapeSequence(doc, dotPos)) {
            return;
        }
        // Find the token sequence and look at what token is under the caret
        Object[] result = findPhpSectionBoundaries(doc, dotPos, true);
        if (result == null) {
            // not in PHP section
            return;
        }

        @SuppressWarnings("unchecked")
        TokenSequence<? extends PHPTokenId> ts = (TokenSequence<? extends PHPTokenId>) result[0];
        int sectionEnd = (Integer) result[2];
        boolean onlyWhitespaceFollows = (Boolean) result[4];
        Token<? extends PHPTokenId> token = ts.token();
        if (token == null) { // Issue #151886
            return;
        }
        Token<? extends PHPTokenId> previousToken = ts.movePrevious() ? ts.token() : null;
        // Check if we are inside a comment
        if (token.id() == PHPTokenId.PHP_COMMENT
                || token.id() == PHPTokenId.PHP_LINE_COMMENT
                || token.id() == PHPTokenId.PHPDOC_COMMENT
                || token.id() == PHPTokenId.T_INLINE_HTML // #132981
                ) {
            return;
        }
        // Check if we are inside a string
        boolean insideString = TypingHooksUtils.isStringToken(token)/* || context.getText().startsWith("\"") || context.getText().startsWith("'")*/; //NOI18N
        if (!insideString) {
            if (onlyWhitespaceFollows && previousToken != null && TypingHooksUtils.isStringToken(previousToken)) {
                // The same as for the line comment above. We could be at the EOL
                // of a string literal, token is the EOL whitespace,
                // but the previous token is PHP string
                insideString = true;
            }
        }
        if (insideString) {
            if (!onlyWhitespaceFollows) {
                //#69524
                char chr = doc.getChars(dotPos, 1)[0];
                if (chr == bracket) {
                    if (!isAfter) {
                        String text = "" + bracket;
                        context.setText(text, text.length());
                    }
                    doc.remove(dotPos, 1);
                }
            }
        } else {
            boolean insert = onlyWhitespaceFollows;
            if (!insert) {
                int firstNonWhiteFwd = LineDocumentUtils.getNextNonWhitespace(doc, dotPos, sectionEnd);
                if (firstNonWhiteFwd != -1) {
                    char chr = doc.getChars(firstNonWhiteFwd, 1)[0];
                    insert = (chr == ')' || chr == ',' || chr == '+' || chr == '}' || //NOI18N
                            chr == ';' || chr == ']' || chr == '.') && !TypingHooksUtils.isStringToken(previousToken) && !isQuote(token); //NOI18N
                }
            }
            if (insert) {
                String text = "" + bracket + (isAfter ? "" : matching(bracket));
                context.setText(text, isAfter ? text.length() : text.length() - 1);
            }
        }
    }

    private boolean isEscapeSequence(BaseDocument doc, int dotPos) throws BadLocationException {
        if (dotPos <= 0) {
            return false;
        }
        char previousChar = doc.getChars(dotPos - 1, 1)[0];
        return previousChar == '\\';
    }

    private static Object[] findPhpSectionBoundaries(BaseDocument doc, int offset, boolean currentLineOnly) {
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts == null) {
            return null;
        }
        ts.move(offset);
        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        // determine the row boundaries
        int lowest = 0;
        int highest = doc.getLength();
        if (currentLineOnly) {
            lowest = doc.getParagraphElement(offset).getStartOffset();
            highest = Math.max(doc.getParagraphElement(offset).getEndOffset() - 1, lowest);
        }
        // find the section end
        int sectionEnd = highest;
        boolean onlyWhitespaceFollows = true;
        do {
            if (highest < ts.offset()) {
                break;
            }

            if (ts.token().id() == PHPTokenId.PHP_CLOSETAG) {
                sectionEnd = ts.offset();
                break;
            } else if (ts.token().id() != PHPTokenId.WHITESPACE) {
                onlyWhitespaceFollows = false;
            }
        } while (ts.moveNext());
        // find the section start
        int sectionStart = lowest;
        boolean onlyWhitespacePreceeds = true;
        while (ts.movePrevious()) {
            if (lowest > ts.offset()) {
                break;
            }
            if (ts.token().id() == PHPTokenId.PHP_OPENTAG) {
                sectionStart = ts.offset();
                break;
            } else if (ts.token().id() != PHPTokenId.WHITESPACE) {
                onlyWhitespacePreceeds = false;
            }
        }
        // re-position the sequence
        ts.move(offset);
        if (!ts.moveNext()) {
            assert ts.movePrevious();
        }
        assert sectionStart != -1 && sectionEnd != -1 : "sectionStart=" + sectionStart + ", sectionEnd=" + sectionEnd; //NOI18N
        return new Object[]{ts, sectionStart, sectionEnd, onlyWhitespacePreceeds, onlyWhitespaceFollows};
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret) throws BadLocationException {
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);
        if (ts != null) {
            ts.move(offset);
            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }
            Token<? extends PHPTokenId> token = ts.token();
            if ((token.id() == id)) {
                final int rowFirstNonWhite = Utilities.getRowFirstNonWhite(doc, offset);
                if (id == PHPTokenId.PHP_CURLY_OPEN && ts.offset() == rowFirstNonWhite
                        && ts.movePrevious()) {
                    // The curly is at the first nonwhite char at the line.
                    // Do we need to indent the { according previous line?
                    int previousExprestion = LexUtilities.findStartTokenOfExpression(ts);
                    int previousIndent = Utilities.getRowIndent(doc, previousExprestion);
                    int currentIndent = Utilities.getRowIndent(doc, offset);
                    int newIndent = IndentUtils.countIndent(doc, offset, previousIndent);
                    if (newIndent != currentIndent) {
                        GsfUtilities.setLineIndentation((Document) doc, offset, Math.max(newIndent, 0));
                    }
                } else if (id == PHPTokenId.WHITESPACE || (id == PHPTokenId.PHP_TOKEN && token.text().charAt(0) == ':')) { // ":" handles "default:"
                    Token<? extends PHPTokenId> previousToken = null;
                    if (id == PHPTokenId.WHITESPACE) {
                        previousToken = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_CASE, PHPTokenId.PHP_TOKEN));
                    } else {
                        if (ts.movePrevious()) { // ":"
                            previousToken = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_DEFAULT, PHPTokenId.PHP_TOKEN));
                        }
                    }
                    if (ts.offset() >= rowFirstNonWhite
                            && previousToken != null
                            && (previousToken.id() == PHPTokenId.PHP_CASE
                            || previousToken.id() == PHPTokenId.PHP_DEFAULT)) {
                        // previous "case" or "default" on one line with typed char
                        previousToken = LexUtilities.findPreviousToken(ts, Arrays.asList(PHPTokenId.PHP_SWITCH));
                        if (previousToken != null && previousToken.id() == PHPTokenId.PHP_SWITCH) {
                            Token<? extends PHPTokenId> firstCaseInSwitch = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_CASE));
                            if (firstCaseInSwitch != null && firstCaseInSwitch.id() == PHPTokenId.PHP_CASE) {
                                int indentOfFirstCase = GsfUtilities.getLineIndent((Document) doc, ts.offset());
                                GsfUtilities.setLineIndentation((Document) doc, offset, indentOfFirstCase);
                            }
                        }
                    }
                } else if (id == PHPTokenId.PHP_CURLY_CLOSE) {
                    OffsetRange begin = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_CURLY_OPEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                    if (begin != OffsetRange.NONE) {
                        int beginOffset = begin.getStart();
                        int indent = GsfUtilities.getLineIndent((Document) doc, beginOffset);
                        previousAdjustmentIndent = GsfUtilities.getLineIndent((Document) doc, offset);
                        GsfUtilities.setLineIndentation((Document) doc, offset, indent);
                        previousAdjustmentOffset = caret.getDot();
                    }
                }
            }
        }
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into the
     * document. The method checks if the bracket should stay there or be
     * removed and some exisitng bracket just skipped.
     *
     * @param doc the document
     * @param dotPos position of the inserted bracket
     * @param caret caret
     * @param bracket the bracket character ']' or ')'
     */
    private void skipClosingBracket(BaseDocument doc, Caret caret, char bracket) throws BadLocationException {
        int caretOffset = caret.getDot();
        if (isSkipClosingBracket(doc, caretOffset, bracket)) {
            doc.remove(caretOffset - 1, 1);
            caret.setDot(caretOffset); // skip closing bracket
        }
    }

    /**
     * Check whether the typed bracket should stay in the document or be
     * removed. <br> This method is called by
     * <code>skipClosingBracket()</code>.
     *
     * @param doc document into which typing was done.
     * @param caretOffset
     */
    private boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, char bracket) throws BadLocationException {
        if (caretOffset == doc.getLength()) {
            return false;
        }
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, caretOffset);
        if (ts == null) {
            return false;
        }
        ts.move(caretOffset);
        if (!ts.moveNext()) {
            return false;
        }
        Token<? extends PHPTokenId> token = ts.token();
        boolean skipClosingBracket = false;
        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (LexUtilities.textEquals(token.text(), bracket))) {
            char leftBracket = bracket == ')' ? '(' : (bracket == ']' ? '[' : '{');
            int bracketBalanceWithNewBracket = 0;
            ts.moveStart();
            if (!ts.moveNext()) {
                return false;
            }
            token = ts.token();
            while (token != null) {
                // GH-6706 we can get "[" as PHP_ENCAPSED_AND_WHITESPACE token e.g. $x = "[$y example]"
                if (token.id() == PHPTokenId.PHP_TOKEN) {
                    if ((LexUtilities.textEquals(token.text(), '(')) || (LexUtilities.textEquals(token.text(), '['))) {
                        if (LexUtilities.textEquals(token.text(), leftBracket)) {
                            bracketBalanceWithNewBracket++;
                        }
                    } else if ((LexUtilities.textEquals(token.text(), ')')) || (LexUtilities.textEquals(token.text(), ']'))) {
                        if (LexUtilities.textEquals(token.text(), bracket)) {
                            bracketBalanceWithNewBracket--;
                        }
                    }
                }
                if (!ts.moveNext()) {
                    break;
                }
                token = ts.token();
            }
            skipClosingBracket = bracketBalanceWithNewBracket != 0;
        }

        return skipClosingBracket;
    }

    /**
     * Check for various conditions and possibly add a pairing bracket to the
     * already inserted.
     *
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the bracket that was inserted
     */
    private void completeOpeningBracket(BaseDocument doc, int dotPos, Caret caret, char bracket) throws BadLocationException {
        if (!bracketCompleted && isCompletablePosition(doc, dotPos + 1)) {
            String matchingBracket = "" + matching(bracket);
            doc.insertString(dotPos + 1, matchingBracket, null);
            caret.setDot(dotPos + 1);
        }
    }

    /**
     * Checks whether dotPos is a position at which bracket and quote completion
     * is performed. Brackets and quotes are not completed everywhere but just
     * at suitable places .
     *
     * @param doc the document
     * @param dotPos position to be tested
     */
    private boolean isCompletablePosition(BaseDocument doc, int dotPos) throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test
            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = doc.getChars(dotPos, 1)[0];
            return ((chr == ')') || (chr == ',') || (chr == '\"') || (chr == '\'') || (chr == ' ')
                    || (chr == ']') || (chr == '}') || (chr == '\n') || (chr == '\t') || (chr == ';'));
        }
    }

    @MimeRegistration(mimeType = FileUtils.PHP_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    public static class Factory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new PhpTypedTextInterceptor();
        }
    }

}
