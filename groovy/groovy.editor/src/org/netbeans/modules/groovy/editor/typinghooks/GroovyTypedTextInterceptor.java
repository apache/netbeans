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

import java.util.EnumSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 *
 * @author Martin Janicek
 */
public class GroovyTypedTextInterceptor implements TypedTextInterceptor {

    @MimeRegistration(mimeType = GroovyTokenId.GROOVY_MIME_TYPE, service = TypedTextInterceptor.Factory.class)
    public static class GroovyTypedTextInterceptorFactory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(MimePath mimePath) {
            return new GroovyTypedTextInterceptor();
        }
    }

    /** Tokens which indicate that we're within a literal string */
    private static final Set<GroovyTokenId> STRING_TOKENS = EnumSet.of(
        GroovyTokenId.STRING_LITERAL
    );

    private static final Set<GroovyTokenId> COMMENT_TOKENS = EnumSet.of(
        GroovyTokenId.BLOCK_COMMENT,
        GroovyTokenId.LINE_COMMENT
    );
    
    /** Tokens which indicate that we're within a regexp string */
    private static final Set<GroovyTokenId> REGEXP_TOKENS = EnumSet.of(
        GroovyTokenId.REGEXP_LITERAL,
        GroovyTokenId.REGEXP_SYMBOL
    );

    /** True iff we're processing bracket matching AFTER the key has been inserted rather than before  */
    private boolean isAfter;

    /* The indentation to revert to when previousAdjustmentOffset is set and the token changed */
    private int previousAdjustmentIndent;

    /**
     * When != -1, this indicates that we previously adjusted the indentation of the
     * line to the given offset, and if it turns out that the user changes that token,
     * we revert to the original indentation
     */
    private int previousAdjustmentOffset = -1;


    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        isAfter = true;
        BaseDocument doc = (BaseDocument) context.getDocument();
        doc.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {

            }
        });

        int dotPos = context.getOffset();
        Caret caret = context.getComponent().getCaret();
        char ch = context.getText().charAt(0);

        // See if our automatic adjustment of indentation when typing (for example) "end" was
        // premature - if you were typing a longer word beginning with one of my adjustment
        // prefixes, such as "endian", then put the indentation back.
        if (previousAdjustmentOffset != -1) {
            if (dotPos == previousAdjustmentOffset) {
                // Revert indentation iff the character at the insert position does
                // not start a new token (e.g. the previous token that we reindented
                // was not complete)
                TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, dotPos);

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
        case '}':
        case '{':
        case ')':
        case ']':
        case '(':
        case '[': {
            Token<GroovyTokenId> token = LexUtilities.getToken(doc, dotPos);
            if (token == null) {
                return;
            }
            TokenId id = token.id();

            if (((id == GroovyTokenId.IDENTIFIER) && (token.length() == 1)) ||
                    (id == GroovyTokenId.LBRACKET) || (id == GroovyTokenId.RBRACKET) ||
                    (id == GroovyTokenId.LBRACE) || (id == GroovyTokenId.RBRACE) ||
                    (id == GroovyTokenId.LPAREN) || (id == GroovyTokenId.RPAREN)) {
                if (TypingHooksUtil.isMatchingBracketsEnabled(doc)) {
                    if (ch == ']') {
                        skipClosingBracket(doc, caret, ch, GroovyTokenId.RBRACKET);
                    } else if (ch == ')') {
                        skipClosingBracket(doc, caret, ch, GroovyTokenId.RPAREN);
                    } else if (ch == '}') {
                        skipClosingBracket(doc, caret, ch, GroovyTokenId.RBRACE);
                    // the curly is not completed intentionally see #189443
                    // java and php don't do that as well
                    } else if ((ch == '[') || (ch == '(')) {
                        completeOpeningBracket(doc, dotPos, caret, ch);
                    }
                }
            }

            // Reindent blocks (won't do anything if } is not at the beginning of a line
            if (ch == '}') {
                reindent(doc, dotPos, GroovyTokenId.RBRACE, caret);
            } else if (ch == ']') {
                reindent(doc, dotPos, GroovyTokenId.RBRACKET, caret);
            }
        }
        break;
            
        case ';':
            moveSemicolon(doc, dotPos, caret);
            break;

        case '/':
            if (!TypingHooksUtil.isMatchingBracketsEnabled(doc)) {
                break;
            }

            // Bracket matching for regular expressions has to be done AFTER the
            // character is inserted into the document such that I can use the lexer
            // to determine whether it's a division (e.g. x/y) or a regular expression (/foo/)
            TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, dotPos);
            if (ts != null) {
                Token token = ts.token();
                TokenId id = token.id();

                if (id == GroovyTokenId.LINE_COMMENT) {
                    // Did you just type "//" - make sure this didn't turn into "///"
                    // where typing the first "/" inserted "//" and the second "/" appended
                    // another "/" to make "///"
                    if (dotPos == ts.offset()+1 && dotPos+1 < doc.getLength() && doc.getText(dotPos+1,1).charAt(0) == '/') {
                        doc.remove(dotPos, 1);
                        caret.setDot(dotPos+1);
                        break;
                    }
                }
            }
            break;
            
        default:
            break;
        }
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        isAfter = false;
        JTextComponent target = context.getComponent();
        Caret caret = target.getCaret();
        int caretOffset = context.getOffset();
        char ch = context.getText().charAt(0);
        BaseDocument doc = (BaseDocument) context.getDocument();

        if (target.getSelectionStart() != -1) {
            if (GsfUtilities.isCodeTemplateEditing(doc)) {
                int start = target.getSelectionStart();
                int end = target.getSelectionEnd();
                if (start < end) {
                    target.setSelectionStart(start);
                    target.setSelectionEnd(start);
                    caretOffset = start;
                    caret.setDot(caretOffset);
                    doc.remove(start, end-start);
                }
                // Fall through to do normal insert matching work
            } else if (ch == '"' || ch == '\'' || ch == '(' || ch == '{' || ch == '[') {
                // Bracket the selection
                String selection = target.getSelectedText();
                if (selection != null && selection.length() > 0) {
                    char firstChar = selection.charAt(0);
                    if (firstChar != ch) {
                        int start = target.getSelectionStart();
                        int end = target.getSelectionEnd();
                        TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(
                                doc, start);
                        if (ts != null
                                && ts.token().id() != GroovyTokenId.LINE_COMMENT
                                && ts.token().id() != GroovyTokenId.BLOCK_COMMENT // not inside comments
                                && ts.token().id() != GroovyTokenId.STRING_LITERAL) { // not inside strings!
                            int lastChar = selection.charAt(selection.length()-1);
                            // Replace the surround-with chars?
                            if (selection.length() > 1 &&
                                    ((firstChar == '"' || firstChar == '\'' || firstChar == '(' ||
                                    firstChar == '{' || firstChar == '[' || firstChar == '/') &&
                                    lastChar == matching(firstChar))) {
                                doc.remove(end-1, 1);
                                doc.insertString(end-1, ""+matching(ch), null);
                                doc.remove(start, 1);
                                doc.insertString(start, ""+ch, null);
                                target.getCaret().setDot(end);
                            } else {
                                // No, insert around
                                doc.remove(start,end-start);
                                doc.insertString(start, ch + selection + matching(ch), null);
                                target.getCaret().setDot(start+selection.length()+2);
                            }

                            return true;
                        }
                    }
                }
            }
        }

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<GroovyTokenId> token = ts.token();
        GroovyTokenId id = token.id();

        if (ch == '*' && id == GroovyTokenId.LINE_COMMENT && caretOffset == ts.offset()+1) {
            // Just typed "*" inside a "//" -- the user has typed "/", which automatched to
            // "//" and now they're typing "*" (e.g. to type "/*", but ended up with "/*/".
            // Remove the auto-matched /.
            doc.remove(caretOffset, 1);
            return false; // false: continue to insert the "*"
        }

        // "/" is handled AFTER the character has been inserted since we need the lexer's help        
        if ( ( ch == '\"' || ch == '\'' ) && TypingHooksUtil.isMatchingBracketsEnabled(doc)) {
            if (completeQuote(doc, caretOffset, caret, ch, STRING_TOKENS, GroovyTokenId.STRING_LITERAL)) {
                caret.setDot(caretOffset + 1);

                return true;
            }
        }

        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
    }

    @Override
    public void cancelled(Context context) {
    }

    private void reindent(BaseDocument doc, int offset, TokenId id, Caret caret)
        throws BadLocationException {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, offset);

        if (ts != null) {
            ts.move(offset);

            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }

            Token<GroovyTokenId> token = ts.token();

            if ((token.id() == id)) {
                final int rowFirstNonWhite = LineDocumentUtils.getLineFirstNonWhitespace(doc, offset);
                // Ensure that this token is at the beginning of the line
                if (ts.offset() > rowFirstNonWhite) {
                    return;
                }

                OffsetRange begin = OffsetRange.NONE;

                if (id == GroovyTokenId.RBRACE) {
                    begin = LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACE, GroovyTokenId.RBRACE);
                } else if (id == GroovyTokenId.RBRACKET) {
                    begin = LexUtilities.findBwd(doc, ts, GroovyTokenId.LBRACKET, GroovyTokenId.RBRACKET);
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
    private void completeOpeningBracket(BaseDocument doc, int dotPos, Caret caret, char bracket)
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
    private boolean completeQuote(BaseDocument doc, int dotPos, Caret caret, char bracket,
        Set<GroovyTokenId> stringTokens, TokenId beginToken) throws BadLocationException {
        if (isEscapeSequence(doc, dotPos)) { // \" or \' typed

            return false;
        }

        // Examine token at the caret offset
        if (doc.getLength() < dotPos) {
            return false;
        }

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, dotPos);

        if (ts == null) {
            return false;
        }

        ts.move(dotPos);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return false;
        }

        Token<GroovyTokenId> token = ts.token();
        Token<GroovyTokenId> previousToken = null;

        if (ts.movePrevious()) {
            previousToken = ts.token();
        }

        int lastNonWhite = LineDocumentUtils.getLineLastNonWhitespace(doc, dotPos);

        // eol - true if the caret is at the end of line (ignoring whitespaces)
        boolean eol = lastNonWhite < dotPos;

        if ((token.id() == GroovyTokenId.BLOCK_COMMENT)
                || (token.id() == GroovyTokenId.LINE_COMMENT)
                || (previousToken != null && previousToken.id() == GroovyTokenId.LINE_COMMENT && token.id() == GroovyTokenId.EOL)) {
            return false;
        } else if ((token.id() == GroovyTokenId.WHITESPACE || token.id() == GroovyTokenId.NLS) && eol && ((dotPos - 1) > 0)) {
            // check if the caret is at the very end of the line comment
            token = LexUtilities.getToken(doc, dotPos - 1);

            if (token.id() == GroovyTokenId.LINE_COMMENT) {
                return false;
            }
        }

        boolean completablePosition = isQuoteCompletablePosition(doc, dotPos);

        boolean insideString = false;
        GroovyTokenId id = token.id();

        for (TokenId currId : stringTokens) {
            if (id == currId) {
                insideString = true;
                break;
            }
        }

        // See issue #236428 - when closing string, lexer returns IDENTIFIER instead of STRING_LITERAL
        if (id == GroovyTokenId.IDENTIFIER) {
            String text = token.text().toString();
            if (text.startsWith("\"") || text.startsWith("\'")) {
                insideString = true;
            }
        }

        if (id == GroovyTokenId.ERROR && (previousToken != null) && (previousToken.id() == beginToken)) {
            insideString = true;
        }

        if (id == GroovyTokenId.EOL && previousToken != null) {
            if (previousToken.id() == beginToken) {
                insideString = true;
            } else if (previousToken.id() == GroovyTokenId.ERROR) {
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
            if ((token.id() == GroovyTokenId.WHITESPACE) && eol) {
                if ((dotPos - 1) > 0) {
                    token = LexUtilities.getToken(doc, dotPos - 1);
                    // XXX TODO use language embedding to handle this
                    insideString = (token.id() == GroovyTokenId.STRING_LITERAL);
                }
            }
        }

        if (insideString) {
            if (eol) {
                return false; // do not complete
            } else {
                //#69524
                char chr = doc.getChars(dotPos, 1)[0];

                if (chr == bracket) {
                    if (!isAfter) {
                        doc.insertString(dotPos, "" + bracket, null); //NOI18N
                    } else {
                        if (!(dotPos < doc.getLength()-1 && doc.getText(dotPos+1,1).charAt(0) == bracket)) {
                            return true;
                        }
                    }

                    doc.remove(dotPos, 1);

                    return true;
                }
            }
        }

        if ((completablePosition && !insideString) || eol) {
            doc.insertString(dotPos, "" + bracket + (isAfter ? "" : matching(bracket)), null); //NOI18N

            return true;
        }

        return false;
    }

    /**
     * Checks whether dotPos is a position at which bracket and quote
     * completion is performed. Brackets and quotes are not completed
     * everywhere but just at suitable places .
     * @param doc the document
     * @param dotPos position to be tested
     */
    private boolean isCompletablePosition(BaseDocument doc, int dotPos)
        throws BadLocationException {
        if (dotPos == doc.getLength()) { // there's no other character to test

            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = doc.getChars(dotPos, 1)[0];

            return ((chr == ')') || (chr == ',') || (chr == '\"') || (chr == '\'') || (chr == ' ') ||
            (chr == ']') || (chr == '}') || (chr == '\n') || (chr == '\t') || (chr == ';'));
        }
    }

    private boolean isQuoteCompletablePosition(BaseDocument doc, int dotPos)
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

            char chr = doc.getChars(firstNonWhiteFwd, 1)[0];

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
     * @param bracket the bracket character ']' or ')'
     */
    private void skipClosingBracket(BaseDocument doc, Caret caret, char bracket, TokenId bracketId)
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
    private boolean isSkipClosingBracket(BaseDocument doc, int caretOffset, TokenId bracketId)
        throws BadLocationException {
        // First check whether the caret is not after the last char in the document
        // because no bracket would follow then so it could not be skipped.
        if (caretOffset == doc.getLength()) {
            return false; // no skip in this case
        }

        boolean skipClosingBracket = false; // by default do not remove

        TokenSequence<GroovyTokenId> ts = LexUtilities.getGroovyTokenSequence(doc, caretOffset);

        if (ts == null) {
            return false;
        }

        // XXX BEGIN TOR MODIFICATIONS
        //ts.move(caretOffset+1);
        ts.move(caretOffset);

        if (!ts.moveNext()) {
            return false;
        }

        Token<GroovyTokenId> token = ts.token();

        // Check whether character follows the bracket is the same bracket
        if ((token != null) && (token.id() == bracketId)) {
            int bracketIntId = bracketId.ordinal();
            int leftBracketIntId =
                (bracketIntId == GroovyTokenId.RPAREN.ordinal()) ? GroovyTokenId.LPAREN.ordinal()
                                                               : GroovyTokenId.LBRACKET.ordinal();

            // Skip all the brackets of the same type that follow the last one
            ts.moveNext();

            Token<GroovyTokenId> nextToken = ts.token();
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
            Token<GroovyTokenId> lastRBracket = token;
            if (!endOfJs) {
                // move on the las bracket || parent
                ts.movePrevious();
            }
            token = ts.token();

            boolean finished = false;

            while (!finished && (token != null)) {
                int tokenIntId = token.id().ordinal();

                if ((token.id() == GroovyTokenId.LPAREN) || (token.id() == GroovyTokenId.LBRACKET)) {
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
                } else if ((token.id() == GroovyTokenId.RPAREN) ||
                        (token.id() == GroovyTokenId.RBRACKET)) {
                    if (tokenIntId == bracketIntId) {
                        bracketBalance--;
                    }
                } else if (token.id() == GroovyTokenId.LBRACE) {
                    braceBalance++;

                    if (braceBalance > 0) { // stop on extra left brace
                        finished = true;
                    }
                } else if (token.id() == GroovyTokenId.RBRACE) {
                    braceBalance--;
                }

                if (!ts.movePrevious()) {
                    break;
                }

                token = ts.token();
            }

            if (bracketBalance != 0
                    || (bracketId ==  GroovyTokenId.RBRACE && braceBalance < 0)) { // not found matching bracket
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
                TokenHierarchy<BaseDocument> th = TokenHierarchy.get(doc);

                int ofs = lastRBracket.offset(th);

                ts.move(ofs);
                ts.moveNext();
                token = ts.token();
                finished = false;

                while (!finished && (token != null)) {
                    if ((token.id() == GroovyTokenId.LPAREN) || (token.id() == GroovyTokenId.LBRACKET)) {
                        if (token.id().ordinal() == leftBracketIntId) {
                            bracketBalance++;
                        }
                    } else if ((token.id() == GroovyTokenId.RPAREN) ||
                            (token.id() == GroovyTokenId.RBRACKET)) {
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
                    } else if (token.id() == GroovyTokenId.LBRACE) {
                        braceBalance++;
                    } else if (token.id() == GroovyTokenId.RBRACE) {
                        braceBalance--;
                    }

                    if (!ts.movePrevious()) {
                        break;
                    }

                    token = ts.token();
                }

                skipClosingBracket = ((braceBalance == 0) && (bracketId == GroovyTokenId.RBRACE))
                        || ((bracketBalance > 0) && (bracketId == GroovyTokenId.RBRACKET || bracketId == GroovyTokenId.RPAREN));
            }
        }

        return skipClosingBracket;
    }

    // XXX TODO Use embedded string sequence here and see if it
    // really is escaped. I know where those are!
    // TODO Adjust for JavaScript
    private static boolean isEscapeSequence(BaseDocument doc, int dotPos)
        throws BadLocationException {
        if (dotPos <= 0) {
            return false;
        }

        char previousChar = doc.getChars(dotPos - 1, 1)[0];

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
            
        case '/':
            return '/';

        case '{':
            return '}';

        case '}':
            return '{';

        default:
            return bracket;
        }
    }
    
    /**
     * Called to add semicolon after bracket for some conditions
     *
     * @param context
     * @return relative caretOffset change
     * @throws BadLocationException
     */
    private static boolean moveSemicolon(BaseDocument doc, int dotPos, Caret caret) throws BadLocationException {
        TokenSequence<GroovyTokenId> ts = LexUtilities.getPositionedSequence(doc, dotPos);
        if (ts == null || isStringOrComment(ts.token().id())) {
            return false;
        }
        int lastParenPos = dotPos;
        int index = ts.index();
        // Move beyond semicolon
        while (ts.moveNext() && !(ts.token().id() == GroovyTokenId.NLS)) {
            switch (ts.token().id()) {
                case RPAREN:
                    lastParenPos = ts.offset();
                    break;
                case WHITESPACE:
                    break;
                default:
                    return false;
            }
        }
        // Restore javaTS position
        ts.moveIndex(index);
        ts.moveNext();
        if (isForLoopOrTryWithResourcesSemicolon(ts) || posWithinAnyQuote(doc, dotPos, ts) || (lastParenPos == dotPos && !ts.token().id().equals(GroovyTokenId.RPAREN))) {
            return false;
        }
        
        doc.remove(dotPos, 1);
        doc.insertString(lastParenPos, ";", null); // NOI18N
        caret.setDot(lastParenPos + 1);
        return true;
    }
    
    private static boolean isStringOrComment(GroovyTokenId tokenID) {
        return COMMENT_TOKENS.contains(tokenID) || STRING_TOKENS.contains(tokenID);
    }
    
    private static boolean isForLoopOrTryWithResourcesSemicolon(TokenSequence<GroovyTokenId> ts) {
        int parenDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        int tsOrigIndex = ts.index();
        try {
            while (ts.movePrevious()) {
                switch (ts.token().id()) {
                    case LPAREN:
                        if (parenDepth == 0) { // could be a 'for (' or 'try ('
                            while (ts.movePrevious()) {
                                switch (ts.token().id()) {
                                    case WHITESPACE:
                                    case BLOCK_COMMENT:
                                    case LINE_COMMENT:
                                        break; // skip
                                    case LITERAL_for:
                                    case LITERAL_try:
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                            return false;
                        } else { // non-zero depth
                            parenDepth--;
                        }
                        break;

                    case RPAREN:
                        parenDepth++;
                        break;

                    case LBRACE:
                        if (braceDepth == 0) { // unclosed left brace
                            return false;
                        }
                        braceDepth--;
                        break;

                    case RBRACE:
                        braceDepth++;
                        break;

                    case SEMI:
                        if (semicolonFound) { // one semicolon already found
                            return false;
                        }
                        semicolonFound = true;
                        break;
                }
            }
        } finally {
            // Restore orig TS's location
            ts.moveIndex(tsOrigIndex);
            ts.moveNext();
        }
        return false;
    }
    
    private static boolean posWithinAnyQuote(BaseDocument doc, int offset, TokenSequence<GroovyTokenId> ts) throws BadLocationException {
        if (ts.token().id() == GroovyTokenId.STRING_LITERAL || ts.token().id() == GroovyTokenId.STRING_CH) {
            char chr = doc.getText(offset, 1).charAt(0);
            return (offset - ts.offset() == 1 || (chr != '"' && chr != '\''));
        }
        return false;
    }
}
