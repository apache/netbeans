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
package org.netbeans.modules.javascript2.editor;

import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.csl.api.EditorOptions;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Petr Hejl
 */
public class JsTypedTextInterceptor implements TypedTextInterceptor {

    /** Tokens which indicate that we're within a regexp string */
    // XXX What about JsTokenId.REGEXP_BEGIN?
    private static final TokenId[] REGEXP_TOKENS = { JsTokenId.REGEXP, JsTokenId.REGEXP_END };

    /** Tokens which indicate that we're within a literal string */
    private static final TokenId[] STRING_TOKENS = { JsTokenId.STRING, JsTokenId.STRING_END };

    /** Tokens which indicate that we're within a template string */
    private static final TokenId[] TEMPLATE_TOKENS = { JsTokenId.TEMPLATE, JsTokenId.TEMPLATE_END };

    private final Language<JsTokenId> language;

    private final boolean singleQuote;

    /** When != -1, this indicates that we previously adjusted the indentation of the
     * line to the given offset, and if it turns out that the user changes that token,
     * we revert to the original indentation
     */
    private int previousAdjustmentOffset = -1;

    /**
     * The indentation to revert to when previousAdjustmentOffset is set and the token
     * changed
     */
    private int previousAdjustmentIndent;

    public JsTypedTextInterceptor(Language<JsTokenId> language, boolean singleQuote) {
        this.language = language;
        this.singleQuote = singleQuote;
    }

    private boolean isInsertMatchingEnabled() {
        EditorOptions options = EditorOptions.get(language.mimeType());
        if (options != null) {
            return options.getMatchBrackets();
        }

        return true;
    }

    private boolean isSmartQuotingEnabled() {
        return OptionsUtils.forLanguage(language).autoCompletionSmartQuotes();
    }

    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        final Document doc = context.getDocument();
        final LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (ld == null) {
            return;
        }
        final AtomicLockDocument ald = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
        final AtomicReference<BadLocationException> ex = new AtomicReference<>();
        ald.runAtomicAsUser(() -> {
            int dotPos = context.getOffset();
            Caret caret = context.getComponent().getCaret();
            char ch = context.getText().charAt(0);

            try {
                // See if our automatic adjustment of indentation when typing (for example) "end" was
                // premature - if you were typing a longer word beginning with one of my adjustment
                // prefixes, such as "endian", then put the indentation back.
                if (previousAdjustmentOffset != -1) {
                    if (dotPos == previousAdjustmentOffset) {
                        // Revert indentation iff the character at the insert position does
                        // not start a new token (e.g. the previous token that we reindented
                        // was not complete)
                        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                                doc, dotPos, language);

                        if (ts != null) {
                            ts.move(dotPos);

                            if (ts.moveNext() && (ts.offset() < dotPos)) {
                                GsfUtilities.setLineIndentation(doc, dotPos, previousAdjustmentIndent);
                            }
                        }
                    }

                    previousAdjustmentOffset = -1;
                }

                switch (ch) {
                    case '{':
                    case '(':
                    case '[':
                        if (!isInsertMatchingEnabled()) {
                            break;
                        }
                    case '}':
                    case ')':
                    case ']':
                        Token<? extends JsTokenId> token = LexUtilities.getToken(doc, dotPos, language);
                        if (token == null) {
                            return;
                        }
                        TokenId id = token.id();

                        if (((token.id() == JsTokenId.IDENTIFIER || token.id() == JsTokenId.PRIVATE_IDENTIFIER) && (token.length() == 1))
                                || (id == JsTokenId.BRACKET_LEFT_BRACKET) || (id == JsTokenId.BRACKET_RIGHT_BRACKET)
                                || (id == JsTokenId.BRACKET_LEFT_CURLY) || (id == JsTokenId.BRACKET_RIGHT_CURLY)
                                || (id == JsTokenId.BRACKET_LEFT_PAREN) || (id == JsTokenId.BRACKET_RIGHT_PAREN)) {
                            if (ch == ']') {
                                skipClosingBracket(doc, caret, JsTokenId.BRACKET_RIGHT_BRACKET);
                            } else if (ch == ')') {
                                skipClosingBracket(doc, caret, JsTokenId.BRACKET_RIGHT_PAREN);
                            } else if (ch == '}') {
                                skipClosingBracket(doc, caret, JsTokenId.BRACKET_RIGHT_CURLY);
                                // the curly is not completed intentionally see #189443
                                // java and php don't do that as well
                            } else if ((ch == '[') || (ch == '(')) {
                                completeOpeningBracket(doc, dotPos, caret, ch);
                            }
                        }

                        // Reindent blocks (won't do anything if } is not at the beginning of a line
                        if (ch == '}') {
                            reindent(ld, dotPos, JsTokenId.BRACKET_RIGHT_CURLY, caret);
                        } else if (ch == ']') {
                            reindent(ld, dotPos, JsTokenId.BRACKET_RIGHT_BRACKET, caret);
                        }
                        break;
                    default:
                        break;
                }
            } catch (BadLocationException blex) {
                ex.set(blex);
            }
        });
        BadLocationException blex = ex.get();
        if (blex != null) {
            throw blex;
        }
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
        int caretOffset = context.getOffset();
        char ch = context.getText().charAt(0);
        BaseDocument doc = (BaseDocument) context.getDocument();
        String selection = context.getReplacedText();
        boolean isTemplate = GsfUtilities.isCodeTemplateEditing(doc);

        if (selection != null && selection.length() > 0) {
            if (!isTemplate && (((ch == '"' || ch == '\'' || ch == '`') && isSmartQuotingEnabled())
                    || ((ch == '(' || ch == '{' || ch == '[')) && isInsertMatchingEnabled())) {
                    // Bracket the selection
                    char firstChar = selection.charAt(0);
                    if (firstChar != ch) {
                        TokenSequence<? extends JsTokenId> ts = LexUtilities.getPositionedSequence(
                                doc, caretOffset, language);
                        if (ts != null
                                && ts.token().id() != JsTokenId.LINE_COMMENT
                                && ts.token().id() != JsTokenId.DOC_COMMENT
                                && ts.token().id() != JsTokenId.BLOCK_COMMENT // not inside comments
                                && ts.token().id() != JsTokenId.STRING) { // not inside strings!
                            int lastChar = selection.charAt(selection.length()-1);
                            // Replace the surround-with chars?
                            if (selection.length() > 1 &&
                                    ((firstChar == '"' || firstChar == '\'' || firstChar == '`' || firstChar == '(' ||
                                    firstChar == '{' || firstChar == '[' || firstChar == '/') &&
                                    lastChar == matching(firstChar))) {
                                String innerText = selection.substring(1, selection.length() - 1);
                                String text = Character.toString(ch) + innerText + Character.toString(matching(ch));
                                context.setText(text, text.length());
                            } else {
                                // No, insert around
                                String text = ch + selection + matching(ch);
                                context.setText(text, text.length());
                            }

                            return;
                        }
                    }
            }
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                doc, caretOffset, language);

        if (ts == null) {
            return;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }

        Token<? extends JsTokenId> token = ts.token();
        JsTokenId id = token.id();
        TokenId[] stringTokens = null;
        TokenId beginTokenId = null;

        // "/" is handled AFTER the character has been inserted since we need the lexer's help
        if (ch == '\"' || (ch == '\'' && singleQuote)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = JsTokenId.STRING_BEGIN;
        } else if (ch == '`' && singleQuote) {
            stringTokens = TEMPLATE_TOKENS;
            beginTokenId = JsTokenId.TEMPLATE_BEGIN;
        } else if (id.isError()) {
            ts.movePrevious();

            TokenId prevId = ts.token().id();

            if (isCompletableStringBoundary(ts.token(), singleQuote, false)) {
                stringTokens = STRING_TOKENS;
                beginTokenId = prevId;
            } else if (isCompletableTemplateBoundary(ts.token(), singleQuote, false)) {
                stringTokens = TEMPLATE_TOKENS;
                beginTokenId = prevId;
            } else if (prevId == JsTokenId.REGEXP_BEGIN) {
                stringTokens = REGEXP_TOKENS;
                beginTokenId = JsTokenId.REGEXP_BEGIN;
            }
        } else if (isCompletableStringBoundary(token, singleQuote, false) &&
                (caretOffset == (ts.offset() + 1))) {
            if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                stringTokens = STRING_TOKENS;
                beginTokenId = id;
            }
        } else if (isCompletableTemplateBoundary(token, singleQuote, false) &&
                (caretOffset == (ts.offset() + 1))) {
            if (!Character.isLetter(ch)) { // %q, %x, etc. Only %[], %!!, %<space> etc. is allowed
                stringTokens = TEMPLATE_TOKENS;
                beginTokenId = id;
            }
        } else if ((isCompletableStringBoundary(token, singleQuote, false) && (caretOffset == (ts.offset() + 2))) ||
                isCompletableStringBoundary(token, singleQuote, true)) {
            stringTokens = STRING_TOKENS;
            beginTokenId = JsTokenId.STRING_BEGIN;
        } else if ((isCompletableTemplateBoundary(token, singleQuote, false) && (caretOffset == (ts.offset() + 2))) ||
                isCompletableTemplateBoundary(token, singleQuote, true)) {
            stringTokens = TEMPLATE_TOKENS;
            beginTokenId = JsTokenId.TEMPLATE_BEGIN;
        } else if (((id == JsTokenId.REGEXP_BEGIN) && (caretOffset == (ts.offset() + 2))) ||
                (id == JsTokenId.REGEXP_END)) {
            stringTokens = REGEXP_TOKENS;
            beginTokenId = JsTokenId.REGEXP_BEGIN;
        }

        if (stringTokens != null && isSmartQuotingEnabled()) {
            completeQuote(context, ch, stringTokens, beginTokenId, isTemplate);
        }
    }

    @Override
    public void cancelled(Context context) {
    }

    private void reindent(LineDocument doc, int offset, TokenId id, Caret caret)
        throws BadLocationException {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                doc, offset, language);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<? extends JsTokenId> token = ts.token();

            if ((token.id() == id)) {
                final int rowFirstNonWhite = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > rowFirstNonWhite) {
//                    if (RubyUtils.isRhtmlDocument(doc)) {
//                        // Allow "<%[whitespace]*" to preceed
//                        String s = doc.getText(rowFirstNonWhite, ts.offset()-rowFirstNonWhite);
//                        if (!s.matches("<%\\s*")) {
//                            return;
//                        }
//                    } else {
                        return;
//                    }
                }

                OffsetRange begin = OffsetRange.NONE;

                if (id == JsTokenId.BRACKET_RIGHT_CURLY) {
                    begin = LexUtilities.findBwd(doc, ts, JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY);
                } else if (id == JsTokenId.BRACKET_RIGHT_BRACKET) {
                    begin = LexUtilities.findBwd(doc, ts, JsTokenId.BRACKET_LEFT_BRACKET, JsTokenId.BRACKET_RIGHT_BRACKET);
                }

                if (begin != OffsetRange.NONE) {
                    int beginOffset = begin.getStart();
                    int indent = GsfUtilities.getLineIndent(doc, beginOffset);
                    previousAdjustmentIndent = GsfUtilities.getLineIndent(doc, offset);
                    GsfUtilities.setLineIndentation(doc, offset, indent);
                    previousAdjustmentOffset = caret.getDot();
                }
            }
        }
    }

    /**
     * Check for various conditions and possibly add a pairing bracket
     * to the already inserted.
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the bracket that was inserted
     */
    private void completeOpeningBracket(Document doc, int dotPos, Caret caret, char bracket)
        throws BadLocationException {
        if (isCompletablePosition(doc, dotPos + 1)) {
            String matchingBracket = "" + matching(bracket);
            doc.insertString(dotPos + 1, matchingBracket, null);
            caret.setDot(dotPos + 1);
        }
    }

    /**
     * Check for conditions and possibly complete an already inserted
     * quote .
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param bracket the character that was inserted
     */
    private void completeQuote(MutableContext context, char bracket,
            TokenId[] stringTokens, TokenId beginToken, boolean isTemplate) throws BadLocationException {
        if (isTemplate) {
            if (bracket == '"' || bracket == '\'' || bracket == '`' || bracket == '(' || bracket == '{' || bracket == '[') {
                String text = context.getText() + matching(bracket);
                context.setText(text, text.length() - 1);
            }
            return;
        }
        int dotPos = context.getOffset();
        Document doc = context.getDocument();
        LineDocument ld = LineDocumentUtils.as(doc, LineDocument.class);
        if (isEscapeSequence(doc, dotPos)) { // \" or \' typed
            return;
        }

        // Examine token at the caret offset
        if (doc.getLength() < dotPos) {
            return;
        }

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                doc, dotPos, language);

        if (ts == null || ld == null) {
            return;
        }

        ts.move(dotPos);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return;
        }

        Token<? extends JsTokenId> token = ts.token();
        Token<? extends JsTokenId> previousToken = null;

        if (ts.movePrevious()) {
            previousToken = ts.token();
        }

        int lastNonWhite = LineDocumentUtils.getLineLastNonWhitespace(ld, dotPos);

        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;

        if ((token.id() == JsTokenId.BLOCK_COMMENT)
                || (token.id() == JsTokenId.DOC_COMMENT)
                || (token.id() == JsTokenId.LINE_COMMENT)
                || (previousToken != null && previousToken.id() == JsTokenId.LINE_COMMENT && token.id() == JsTokenId.EOL)) {
            return;
        } else if ((token.id() == JsTokenId.WHITESPACE) && eol && ((dotPos - 1) > 0)) {
            // check if the caret is at the very end of the line comment
            token = LexUtilities.getToken(doc, dotPos - 1, language);

            if (token.id() == JsTokenId.LINE_COMMENT) {
                return;
            }
        }

        boolean completablePosition = isQuoteCompletablePosition(ld, dotPos);

        boolean insideString = false;
        JsTokenId id = token.id();

        for (TokenId currId : stringTokens) {
            if (id == currId) {
                insideString = true;
                break;
            }
        }

        if (id.isError() && (previousToken != null)
                && (previousToken.id() == beginToken)) {
            insideString = true;
        }

        if (id == JsTokenId.EOL && previousToken != null) {
            if (previousToken.id() == beginToken) {
                insideString = true;
            } else if (previousToken.id().isError()) {
                if (ts.movePrevious()) {
                    if (ts.token().id() == beginToken) {
                        insideString = true;
                    }
                }
            }
        }

        if (!insideString) {
            // check if the caret is at the very end of the line and there
            // is an unterminated string literal
            if ((token.id() == JsTokenId.WHITESPACE) && eol) {
                if ((dotPos - 1) > 0) {
                    token = LexUtilities.getToken(doc, dotPos - 1, language);
                    // XXX TODO use language embedding to handle this
                    insideString = (token.id() == JsTokenId.STRING || token.id() == JsTokenId.TEMPLATE);
                }
            }
        }

        if (insideString) {
            if (eol) {
                return; // do not complete
            } else {
                //#69524
                char chr = DocumentUtilities.getText(doc, dotPos, 1).charAt(0);

                if (chr == bracket) {
                    context.setText(Character.toString(bracket), 1);
                    doc.remove(dotPos, 1);

                    return;
                }
            }
        }

        if ((completablePosition && !insideString) || eol) {
            String text = Character.toString(bracket) + matching(bracket);
            context.setText(text, text.length() - 1);
        }
    }

    /**
     * Checks whether dotPos is a position at which bracket and quote
     * completion is performed. Brackets and quotes are not completed
     * everywhere but just at suitable places .
     * @param doc the document
     * @param dotPos position to be tested
     */
    private boolean isCompletablePosition(Document doc, int dotPos)
        throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test

            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = DocumentUtilities.getText(doc, dotPos, 1).charAt(0);
            return ((chr == ')') || (chr == ',') || (chr == '\"') || (chr == '\'') || (chr == '`') || (chr == ' ') ||
            (chr == ']') || (chr == '}') || (chr == '\n') || (chr == '\t') || (chr == ';'));
        }
    }

    private boolean isQuoteCompletablePosition(LineDocument doc, int dotPos)
        throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test

            return true;
        } else {
            // test that we are in front of ) , " or ' ... etc.
            int eol = LineDocumentUtils.getLineEndOffset(doc, dotPos);

            if ((dotPos == eol) || (eol == -1)) {
                return false;
            }

            int firstNonWhiteFwd = LineDocumentUtils.getNextNonWhitespace(doc, dotPos, eol);

            if (firstNonWhiteFwd == -1) {
                return false;
            }

            char chr = DocumentUtilities.getText(doc, firstNonWhiteFwd, 1).charAt(0);

            return ((chr == ')') || (chr == ',') || (chr == '+') || (chr == '}') || (chr == ';') ||
               (chr == ']'));
        }
    }

    /**
     * A hook to be called after closing bracket ) or ] was inserted into
     * the document. The method checks if the bracket should stay there
     * or be removed and some exisitng bracket just skipped.
     *
     * @param doc the document
     * @param dotPos position of the inserted bracket
     * @param caret caret
     * @param bracketId TokenId of the the bracket character ']' or ')'
     */
    private void skipClosingBracket(Document doc, Caret caret, TokenId bracketId)
        throws BadLocationException {
        int caretOffset = caret.getDot();

        if (isSkipClosingBracket(doc, caretOffset, bracketId)) {
            doc.remove(caretOffset - 1, 1);
            caret.setDot(caretOffset); // skip closing bracket
        }
    }

    /**
     * Check whether the typed bracket should stay in the document
     * or be removed.
     * <br>
     * This method is called by <code>skipClosingBracket()</code>.
     *
     * @param doc document into which typing was done.
     * @param caretOffset
     */
    private boolean isSkipClosingBracket(Document doc, int caretOffset, TokenId bracketId)
        throws BadLocationException {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }

        boolean skipClosingBracket = false; // by default do not remove

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                doc, caretOffset, language);

        if (ts == null) {
            return false;
        }

        // XXX BEGIN TOR MODIFICATIONS
        //ts.move(caretOffset+1);
        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<? extends JsTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (token.id() == bracketId)) {
            int bracketIntId = bracketId.ordinal();
            int leftBracketIntId =
                (bracketIntId == JsTokenId.BRACKET_RIGHT_PAREN.ordinal()) ? JsTokenId.BRACKET_LEFT_PAREN.ordinal()
                                                               : JsTokenId.BRACKET_LEFT_BRACKET.ordinal();

            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<? extends JsTokenId> nextToken = ts.token();
            boolean endOfJs = false;
            while ((nextToken != null) && (nextToken.id() == bracketId)) {
                token = nextToken;

                if (!ts.moveNext()) {
                    endOfJs = true;
                    break;
                }

                nextToken = ts.token();
            }

            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = 0; // balance of the brackets or parenthesis
            Token<? extends JsTokenId> lastRBracket = token;
            if (!endOfJs) {
                // move on the las bracket || parent
                ts.movePrevious();
            }
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                int tokenIntId = token.id().ordinal();

                if ((token.id() == JsTokenId.BRACKET_LEFT_PAREN) || (token.id() == JsTokenId.BRACKET_LEFT_BRACKET)) {
                    if (tokenIntId == leftBracketIntId) {
                        bracketBalance++;

                        if (bracketBalance == 0) {
                            if (braceBalance != 0) {
                                // Here the bracket is matched but it is located
                                // inside an unclosed brace block
                                // e.g. ... ->( } a()|)
                                // which is in fact illegal but it's a question
                                // of what's best to do in this case.
                                // We chose to leave the typed bracket
                                // by setting bracketBalance to 1.
                                // It can be revised in the future.
                                bracketBalance = 1;
                            }

                            finished = true;
                        }
                    }
                } else if ((token.id() == JsTokenId.BRACKET_RIGHT_PAREN) ||
                        (token.id() == JsTokenId.BRACKET_RIGHT_BRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance--;
                    }
                } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                    braceBalance--;
                }

                if (!ts.movePrevious()) {
                    break;
                }

                token = ts.token();
            }

            if (bracketBalance != 0
                    || (bracketId ==  JsTokenId.BRACKET_RIGHT_CURLY && braceBalance < 0)) { // not found matching bracket
                                       // Remove the typed bracket as it's unmatched
                skipClosingBracket = true;
            } else { // the bracket is matched
                     // Now check whether the bracket would be matched
                     // when the closing bracket would be removed
                     // i.e. starting from the original lastRBracket token
                     // and search for the same bracket to the right in the text
                     // The search would stop on an extra right brace if found
                braceBalance = 0;
                bracketBalance = 0;

                //token = lastRBracket.getNext();
                TokenHierarchy<Document> th = TokenHierarchy.get(doc);

                int ofs = lastRBracket.offset(th);

                ts.move(ofs);
                ts.moveNext();
                token = ts.token();
                finished = false;

                while (!finished && (token != null)) {
                    if ((token.id() == JsTokenId.BRACKET_LEFT_PAREN) || (token.id() == JsTokenId.BRACKET_LEFT_BRACKET)) {
                        if (token.id().ordinal() == leftBracketIntId) {
                            bracketBalance++;
                        }
                    } else if ((token.id() == JsTokenId.BRACKET_RIGHT_PAREN) ||
                            (token.id() == JsTokenId.BRACKET_RIGHT_BRACKET)) {
                        if (token.id().ordinal() == bracketIntId) {
                            bracketBalance--;

                            if (bracketBalance == 0) {
                                if (braceBalance != 0) {
                                    // Here the bracket is matched but it is located
                                    // inside an unclosed brace block
                                    // which is in fact illegal but it's a question
                                    // of what's best to do in this case.
                                    // We chose to leave the typed bracket
                                    // by setting bracketBalance to -1.
                                    // It can be revised in the future.
                                    bracketBalance = -1;
                                }

                                finished = true;
                            }
                        }
                    } else if (token.id() == JsTokenId.BRACKET_LEFT_CURLY) {
                        braceBalance++;
                    } else if (token.id() == JsTokenId.BRACKET_RIGHT_CURLY) {
                        braceBalance--;
                    }

                    if (!ts.movePrevious()) {
                        break;
                    }

                    token = ts.token();
                }

                skipClosingBracket = ((braceBalance == 0) && (bracketId == JsTokenId.BRACKET_RIGHT_CURLY))
                        || ((bracketBalance > 0) && (bracketId == JsTokenId.BRACKET_RIGHT_BRACKET || bracketId == JsTokenId.BRACKET_RIGHT_PAREN));
            }
        }

        return skipClosingBracket;
    }

    // XXX TODO Use embedded string sequence here and see if it
    // really is escaped. I know where those are!
    // TODO Adjust for JavaScript
    private static boolean isEscapeSequence(Document doc, int dotPos)
        throws BadLocationException {
        if (dotPos <= 0) {
            return false;
        }

        char previousChar = DocumentUtilities.getText(doc, dotPos - 1, 1).charAt(0);

        return previousChar == '\\';
    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private static char matching(char bracket) {
        switch (bracket) {
        case '(':
            return ')';

        case '[':
            return ']';

        case '\"':
            return '\"'; // NOI18N

        case '\'':
            return '\'';

        case '`':
            return '`';

        case '{':
            return '}';

        case '}':
            return '{';

        default:
            return bracket;
        }
    }

    private static boolean isCompletableStringBoundary(Token<? extends JsTokenId> token,
            boolean singleQuote, boolean end) {
        if ((!end && token.id() == JsTokenId.STRING_BEGIN)
                || (end && token.id() == JsTokenId.STRING_END)) {
            if (singleQuote || "\"".equals(token.text().toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCompletableTemplateBoundary(Token<? extends JsTokenId> token,
            boolean singleQuote, boolean end) {
        if ((!end && token.id() == JsTokenId.TEMPLATE_BEGIN)
                || (end && token.id() == JsTokenId.TEMPLATE_END)) {
            return singleQuote;
        }
        return false;
    }

    @MimeRegistration(mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    public static class JsFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath mimePath) {
            return new JsTypedTextInterceptor(JsTokenId.javascriptLanguage(), true);
        }

    }

    @MimeRegistration(mimeType = JsTokenId.JSON_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    public static class JsonFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath mimePath) {
            return new JsTypedTextInterceptor(JsTokenId.jsonLanguage(), false);
        }

    }
}
