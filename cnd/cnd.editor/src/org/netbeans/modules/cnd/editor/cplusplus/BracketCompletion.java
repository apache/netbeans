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
package org.netbeans.modules.cnd.editor.cplusplus;

import java.util.EnumSet;
import java.util.Set;
import java.util.Stack;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndAbstractTokenProcessor;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

/**
 * This static class groups the whole aspect of bracket
 * completion. It is defined to clearly separate the functionality
 * and keep actions clean.
 * The methods of the class are called from different actions as
 * KeyTyped, DeletePreviousChar.
 */
public class BracketCompletion {

    /**
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     * @param doc the document where the change occurred
     * @param dotPos position of the character insertion
     * @param caret caret
     * @param ch the character that was inserted
     * @throws BadLocationException if dotPos is not correct
     */
    static void charInserted(BaseDocument doc,
            int dotPos,
            Caret caret,
            char ch, boolean blockCommentStart) throws BadLocationException {
        if (!completionSettingEnabled(doc)) {
            return;
        }
        TokenItem<TokenId> tokenAtDot = CndTokenUtilities.getToken(doc, dotPos, true);
        if (tokenAtDot == null) {
            return;
        }
        if (ch == '\"' || ch == '\'') {
            completeQuote(doc, dotPos, caret, ch);
        } else if (ch == ';') {
            moveSemicolon(doc, dotPos, caret);
        } else if (ch == '.') {
            if (dotPos > 0) {
                tokenAtDot = CndTokenUtilities.getToken(doc, dotPos - 1, true);
                if (tokenAtDot.id() == CppTokenId.THIS) {
                    doc.remove(dotPos, 1);
                    doc.insertString(dotPos, "->", null);// NOI18N
                    caret.setDot(dotPos + 2);
                }
            }
        } else if (ch == '*' && blockCommentStart) {
            // complete /* with /*|*/
            if (tokenAtDot.id() == CppTokenId.BLOCK_COMMENT) {
                doc.insertString(dotPos + 1, "*/", null);// NOI18N
                caret.setDot(dotPos + 1);
            }
        }
    }

    private static void moveSemicolon(BaseDocument doc, int dotPos, Caret caret) throws BadLocationException {
        int eolPos = Utilities.getRowEnd(doc, dotPos);
        TokenSequence<TokenId> cppTokenSequence = CndLexerUtilities.getCppTokenSequence(doc, dotPos, true, false);
        if (cppTokenSequence == null) {
            return;
        }
        int lastParenPos = dotPos;
        while (cppTokenSequence.moveNext() && cppTokenSequence.offset() < eolPos) {
            Token<TokenId> token = cppTokenSequence.token();
            if (token.id() == CppTokenId.RPAREN || token.id() == CppTokenId.RBRACKET) {
                lastParenPos = cppTokenSequence.offset();
            } else if (!CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) {
                return;
            }
        }
        if (posWithinAnyQuote(doc, dotPos) || isForLoopSemicolon(doc, dotPos)) {
            return;
        }
        // may be check offsets?
//        if (lastParenPos != dotPos) {
        doc.remove(dotPos, 1);
        doc.insertString(lastParenPos, ";", null); // NOI18N
        caret.setDot(lastParenPos + 1);
//        }
    }

    private static boolean isForLoopSemicolon(BaseDocument doc, int dotPos) {
        TokenSequence<TokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, dotPos, true, false);
        if (ts == null || ts.token().id() != CppTokenId.SEMICOLON) {
            return false;
        }
        int parDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        while (ts.movePrevious()) {
            Token<TokenId> token = ts.token();
            if (token.id() == CppTokenId.LPAREN) {
                if (parDepth == 0) { // could be a 'for ('
                    while (ts.movePrevious()) {
                        token = ts.token();
                        String category = token.id().primaryCategory();
                        if (!CppTokenId.WHITESPACE_CATEGORY.equals(category) && !CppTokenId.COMMENT_CATEGORY.equals(category)) {
                            break;
                        }
                    }
                    if (token.id() == CppTokenId.FOR) {
                        return true;
                    }
                    return false;
                } else { // non-zero depth
                    parDepth--;
                }
            } else if (token.id() == CppTokenId.RPAREN) {
                parDepth++;
            } else if (token.id() == CppTokenId.LBRACE) {
                if (braceDepth == 0) { // unclosed left brace
                    return false;
                }
                braceDepth--;
            } else if (token.id() == CppTokenId.RBRACE) {
                braceDepth++;
            } else if (token.id() == CppTokenId.SEMICOLON) {
                if (semicolonFound) { // one semicolon already found
                    return false;
                }
                semicolonFound = true;
            }
        }
        return false;
    }

    /**
     * Hook called after a character *ch* was backspace-deleted from
     * *doc*. The function possibly removes bracket or quote pair if
     * appropriate.
     * @param doc the document
     * @param dotPos position of the change
     * @param caret caret
     * @param ch the character that was deleted
     */
    static void charBackspaced(BaseDocument doc,
            int dotPos,
            char ch) throws BadLocationException {
        if (completionSettingEnabled(doc)) {
            if (doc.getLength() == 0) {
                return;
            }
            if (ch == '(' || ch == '[') {
                TokenItem<TokenId> token = CndTokenUtilities.getToken(doc, dotPos, true);
                if (token == null) {
                    return;
                }
                if ((token.id() == CppTokenId.RBRACKET && tokenBalance(doc, CppTokenId.LBRACKET, CppTokenId.RBRACKET, dotPos) != 0)
                    || (token.id() == CppTokenId.RPAREN && tokenBalance(doc, CppTokenId.LPAREN, CppTokenId.RPAREN, dotPos) != 0)) {
                    doc.remove(dotPos, 1);
                }
            } else if (ch == '\"') {
                char match[] = doc.getChars(dotPos, 1);
                if (match != null && match[0] == '\"') {
                    doc.remove(dotPos, 1);
                }
            } else if (ch == '\'') {
                char match[] = doc.getChars(dotPos, 1);
                if (match != null && match[0] == '\'') {
                    doc.remove(dotPos, 1);
                }
            } else if (ch == '<') {
                char match[] = doc.getChars(dotPos, 1);
                if (match != null && match[0] == '>' && dotPos > 0) {
                    TokenItem<TokenId> token = CndTokenUtilities.getFirstNonWhiteBwd(doc, dotPos - 1);
                    TokenId id = token.id();
                    if(id instanceof CppTokenId) {
                        switch ((CppTokenId)id) {
                            case PREPROCESSOR_INCLUDE:
                            case PREPROCESSOR_INCLUDE_NEXT:
                                doc.remove(dotPos, 1);
                        }
                    }
                }
            }
        }
    }

    private static TokenSequence<TokenId> cppTokenSequence(Document doc, int offset, boolean backwardBias) {
        return CndLexerUtilities.getCppTokenSequence(doc, offset, true, backwardBias);
    }

    /**
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
    static boolean isAddRightBrace(BaseDocument doc, int caretOffset) throws BadLocationException {
        if (!completionSettingEnabled(doc)) {
            return false;
        }
        if (tokenBalance(doc, CppTokenId.LBRACE, CppTokenId.RBRACE, caretOffset) <= 0) {
            return false;
        }
        int caretRowStartOffset = Utilities.getRowStart(doc, caretOffset);
        TokenSequence<TokenId> ts = cppTokenSequence(doc, caretOffset, true);
        if (ts == null) {
            return false;
        }
        boolean first = true;
        do {
            if (ts.offset() < caretRowStartOffset) {
                return false;
            }
            TokenId id = ts.token().id();
            if(id instanceof CppTokenId) {
                switch ((CppTokenId)id) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                        break;
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                        if (first && caretOffset > ts.offset() && caretOffset < ts.offset() + ts.token().length()) {
                            // Caret contained within block comment -> do not add anything
                            return false;
                        }
                        break; // Skip
                    case LBRACE:
                        return true;
                }
            }
            first = false;
        } while (ts.movePrevious());
        return false;
    }

    /**
     * Returns position of the first unpaired closing paren/brace/bracket from the caretOffset
     * till the end of caret row. If there is no such element, position after the last non-white
     * character on the caret row is returned.
     */
    @SuppressWarnings("unchecked")
    static int getRowOrBlockEnd(BaseDocument doc, int caretOffset) throws BadLocationException {
        int rowEnd = Utilities.getRowLastNonWhite(doc, caretOffset);
        if (rowEnd == -1 || caretOffset >= rowEnd) {
            return caretOffset;
        }
        rowEnd += 1;
        int parenBalance = 0;
        int braceBalance = 0;
        int bracketBalance = 0;

        TokenSequence<TokenId> cppTokenSequence = cppTokenSequence(doc, caretOffset, false);
        if (cppTokenSequence == null) {
            return caretOffset;
        }
        while (cppTokenSequence.moveNext() && cppTokenSequence.offset() < rowEnd) {
            TokenId id = cppTokenSequence.token().id();
            if(id instanceof CppTokenId) {
                switch ((CppTokenId)id) {
                    case LPAREN:
                        parenBalance++;
                        break;
                    case RPAREN:
                        if (parenBalance-- == 0) {
                            return cppTokenSequence.offset();
                        }
                        break;
                    case LBRACE:
                        braceBalance++;
                        break;
                    case RBRACE:
                        if (braceBalance-- == 0) {
                            return cppTokenSequence.offset();
                        }
                        break;
                    case LBRACKET:
                        bracketBalance++;
                        break;
                    case RBRACKET:
                        if (bracketBalance-- == 0) {
                            return cppTokenSequence.offset();
                        }
                        break;
                }
            }
        }
        return rowEnd;
    }

    /**
     * Counts the number of braces starting at dotPos to the end of the
     * document. Every occurence of { increses the count by 1, every
     * occurrence of } decreses the count by 1. The result is returned.
     * @return The number of { - number of } (>0 more { than } ,<0 more } than {)
     */
    /**
     * The same as braceBalance but generalized to any pair of matching
     * tokens.
     * @param open the token that increses the count
     * @param close the token that decreses the count
     */
    private static int tokenBalance(BaseDocument doc, CppTokenId open, CppTokenId close, int caretOffset)
            throws BadLocationException {
        BalanceTokenProcessor tp = new BalanceTokenProcessor(open, close);
        CndTokenUtilities.processTokens(tp, doc, 0, doc.getLength());
        return tp.getBalance();
    }

    private static final Set<? extends TokenId> STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET = EnumSet.of(CppTokenId.LBRACE, CppTokenId.RBRACE, CppTokenId.SEMICOLON);
    /**
     * Check whether the typed bracket should stay in the document
     * or be removed.
     * <br>
     * This method is called by <code>skipClosingBracket()</code>.
     *
     * @param doc document into which typing was done.
     * @param caretOffset
     */
    private static boolean isSkipClosingBracket(TypedTextInterceptor.MutableContext context, TokenSequence<TokenId> cppTS, CppTokenId bracketId) throws BadLocationException {
        if (context.getOffset() == context.getDocument().getLength()) {
            return false;
        }

        boolean skipClosingBracket = false;
        TokenId id = cppTS.token().id();
        if (id == CppTokenId.PREPROCESSOR_SYS_INCLUDE && bracketId == CppTokenId.GT) {
            char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);
            return chr == '>';
        }
        if (id == bracketId) {
            CppTokenId leftBracketId = matching(bracketId);
            // Skip all the brackets of the same type that follow the last one
            do {
                if (STOP_TOKENS_FOR_SKIP_CLOSING_BRACKET.contains(cppTS.token().id())
                        || (cppTS.token().id() == CppTokenId.WHITESPACE && cppTS.token().text().toString().contains("\n"))) {  // NOI18N
                    while (cppTS.token().id() != bracketId) {
                        boolean isPrevious = cppTS.movePrevious();
                        if (!isPrevious) {
                            break;
                        }
                    }
                    break;
                }
            } while (cppTS.moveNext());

            // token var points to the last bracket in a group of two or more right brackets
            // Attempt to find the left matching bracket for it
            // Search would stop on an extra opening left brace if found
            int braceBalance = 0; // balance of '{' and '}'
            int bracketBalance = -1; // balance of the brackets or parenthesis
            int numOfSemi = 0;
            boolean finished = false;
            while (!finished && cppTS.movePrevious()) {
                id = cppTS.token().id();
                switch ((CppTokenId) id) {
                    case LPAREN:
                    case LBRACKET:
                        if (id == leftBracketId) {
                            bracketBalance++;
                            if (bracketBalance == 1) {
                                if (braceBalance != 0) {
                                    // Here the bracket is matched but it is located
                                    // inside an unclosed brace block
                                    // e.g. ... ->( } a()|)
                                    // which is in fact illegal but it's a question
                                    // of what's best to do in this case.
                                    // We chose to leave the typed bracket
                                    // by setting bracketBalance to 1.
                                    // It can be revised in the future.
                                    bracketBalance = 2;
                                }
                                finished = cppTS.offset() < context.getOffset();
                            }
                        }
                        break;

                    case RPAREN:
                    case RBRACKET:
                        if (id == bracketId) {
                            bracketBalance--;
                        }
                        break;
                    case LBRACE:
                        braceBalance++;
                        if (braceBalance > 0) { // stop on extra left brace
                            finished = true;
                        }
                        break;

                    case RBRACE:
                        braceBalance--;
                        break;

                    case SEMICOLON:
                        numOfSemi++;
                        break;
                }
            }

            if (bracketBalance == 1 && numOfSemi < 2) {
                finished = false;
                while (!finished && cppTS.movePrevious()) {
                    switch ((CppTokenId) cppTS.token().id()) {
                        case WHITESPACE:
                        case LINE_COMMENT:
                        case BLOCK_COMMENT:
                        case DOXYGEN_COMMENT:
                        case DOXYGEN_LINE_COMMENT:
                            break;
                        case FOR:
                            bracketBalance--;
                        default:
                            finished = true;
                            break;
                    }
                }
            }

            skipClosingBracket = bracketBalance != 1;
        }
        return skipClosingBracket;

    }
    private static final Set<? extends TokenId> STRING_AND_COMMENT_TOKENS = EnumSet.of(CppTokenId.STRING_LITERAL, CppTokenId.LINE_COMMENT, CppTokenId.DOXYGEN_COMMENT, CppTokenId.DOXYGEN_LINE_COMMENT, CppTokenId.BLOCK_COMMENT);

    private static boolean isStringOrComment(TokenId tokenId) {
        return STRING_AND_COMMENT_TOKENS.contains(tokenId);
    }

    private static boolean isEscapeSequence(BaseDocument doc, int dotPos) throws BadLocationException {
        if (dotPos <= 0) {
            return false;
        }
        char previousChar = doc.getChars(dotPos - 1, 1)[0];
        return previousChar == '\\';
    }

    /**
     * Check for conditions and possibly complete an already inserted
     * quote .
     * @param doc the document
     * @param dotPos position of the opening bracket (already in the doc)
     * @param caret caret
     * @param theBracket the character that was inserted
     */
    private static void completeQuote(BaseDocument doc, int dotPos, Caret caret,
            char theBracket)
            throws BadLocationException {
        if (isEscapeSequence(doc, dotPos)) {
            return;
        }
        CppTokenId[] tokenIds = theBracket == '\"' ?
                  new CppTokenId[]{CppTokenId.STRING_LITERAL, CppTokenId.RAW_STRING_LITERAL, CppTokenId.PREPROCESSOR_USER_INCLUDE}
                : new CppTokenId[]{CppTokenId.CHAR_LITERAL};
        if ((posWithinQuotes(doc, dotPos + 1, theBracket, tokenIds) && isCompletablePosition(doc, dotPos + 1)) &&
                (isUnclosedStringAtLineEnd(doc, dotPos + 1, tokenIds) &&
                ((doc.getLength() == dotPos + 1) ||
                (doc.getLength() != dotPos + 1 && doc.getChars(dotPos + 1, 1)[0] != theBracket)))) {
            doc.insertString(dotPos + 1, "" + theBracket, null);
            caret.setDot(dotPos + 1);
        } else {
            char[] charss = doc.getChars(dotPos + 1, 1);
            // System.out.println("NOT Within string, " + new String(charss));
            if (charss != null && charss[0] == theBracket) {
                doc.remove(dotPos + 1, 1);
            }
        }
    }

    /**
     * Checks whether dotPos is a position at which bracket and quote
     * completion is performed. Brackets and quotes are not completed
     * everywhere but just at suitable places .
     * @param doc the document
     * @param dotPos position to be tested
     */
    private static boolean isCompletablePosition(Document doc, int dotPos)
            throws BadLocationException {
        if (dotPos == doc.getLength()) {// there's no other character to test
            return true;
        } else {
            // test that we are in front of ) , " or '
            char chr = doc.getText(dotPos, 1).charAt(0);
            return (chr == ')' ||
                    chr == ',' ||
                    chr == '\"' ||
                    chr == '\'' ||
                    chr == ' ' ||
                    chr == '-' ||
                    chr == '+' ||
                    chr == '|' ||
                    chr == '&' ||
                    chr == ']' ||
                    chr == '}' ||
                    chr == '\n' ||
                    chr == '\t' ||
                    chr == ';');
        }
    }

    /**
     * Returns true if bracket completion is enabled in options.
     */
    static boolean completionSettingEnabled(Document doc) {
        Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(doc)).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.COMPLETION_PAIR_CHARACTERS, true);
    }

    /**
     * Returns for an opening bracket or quote the appropriate closing
     * character.
     */
    private static char matching(char theBracket) {
        switch (theBracket) {
            case '(':
                return ')';
            case '[':
                return ']';
            case '\"':
                return '\"'; // NOI18N
            case '\'':
                return '\'';
            case '<':
                return '>';

            default:
                return ' ';
        }
    }

    /**
     * posWithinString(doc, pos) iff position *pos* is within a string
     * literal in document doc.
     * @param doc the document
     * @param dotPos position to be tested
     */
    static boolean posWithinNonRawString(BaseDocument doc, int dotPos) {
        return posWithinQuotes(doc, dotPos, '\"', new CppTokenId[]{CppTokenId.STRING_LITERAL});
    }

    /**
     * Generalized posWithingString to any token and delimiting
     * character. It works for tokens are delimited by *quote* and
     * extend up to the other *quote* or whitespace in case of an
     * incomplete token.
     * @param doc the document
     * @param dotPos position to be tested
     */
    static boolean posWithinQuotes(BaseDocument doc, int dotPos, char quote, CppTokenId[] tokenIDs) {
        TokenSequence<TokenId> cppTS = cppTokenSequence(doc, dotPos, true);
        if (cppTS != null && matchIDs(cppTS.token().id(), tokenIDs)) {
            return (dotPos - cppTS.offset() == 1 || DocumentUtilities.getText(doc).charAt(dotPos - 1) != quote);
        }
        return false;
    }

    static boolean posWithinAnyQuote(BaseDocument doc, int dotPos) {
        TokenSequence<TokenId> cppTS = cppTokenSequence(doc, dotPos - 1, false);
        if (cppTS != null) {
            TokenId id = cppTS.token().id();
            if(id instanceof CppTokenId) {
                switch ((CppTokenId)id) {
                    case RAW_STRING_LITERAL:
                    case STRING_LITERAL:
                    case CHAR_LITERAL:
                    case PREPROCESSOR_USER_INCLUDE:
                    case PREPROCESSOR_SYS_INCLUDE:
                    {
                        char ch = DocumentUtilities.getText(doc).charAt(dotPos - 1);
                        return (dotPos - cppTS.offset() == 1 || (ch != '"' && ch != '\''));
                    }
                }
            }
        }
        return false;
    }

    static boolean isUnclosedStringAtLineEnd(BaseDocument doc, int dotPos, CppTokenId[] tokenIDs) {
        int lastNonWhiteOffset;
        try {
            lastNonWhiteOffset = Utilities.getRowLastNonWhite(doc, dotPos);
        } catch (BadLocationException e) {
            return false;
        }
        TokenSequence<TokenId> cppTS = cppTokenSequence(doc, lastNonWhiteOffset, false);
        if (cppTS != null) {
            return matchIDs(cppTS.token().id(), tokenIDs);
        }
        return false;
    }

    static boolean matchIDs(TokenID toCheck, TokenID[] checkWith) {
        for (int i = checkWith.length - 1; i >= 0; i--) {
            if (toCheck == checkWith[i]) {
                return true;
            }
        }
        return false;
    }

    static boolean matchIDs(TokenId toCheck, CppTokenId[] checkWith) {
        for (int i = checkWith.length - 1; i >= 0; i--) {
            if (toCheck == checkWith[i]) {
                return true;
            }
        }
        return false;
    }

    static boolean matchIDs(Token<CppTokenId> toCheck, Token<CppTokenId>[] checkWith) {
        for (int i = checkWith.length - 1; i >= 0; i--) {
            if (toCheck == checkWith[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for various conditions and possibly add a pairing bracket.
     *
     * @param context
     * @throws BadLocationException
     */
    static void completeOpeningBracket(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        TokenSequence<TokenId> cppTS = cppTokenSequence(context.getDocument(), context.getOffset(), false);
        if (isStringOrComment(cppTS.token().id())) {
            return;
        }
        char insChr = context.getText().charAt(0);
        char chr = context.getDocument().getText(context.getOffset(), 1).charAt(0);
        if (insChr == '<') {
            if (chr == '\n') {
                if (((Language<?>)cppTS.language()) == ((Language<?>)CppTokenId.languagePreproc())) {
                    // autocomple if system includes directive
                    cppTS.moveStart();
                    if (cppTS.moveNext() && cppTS.moveNext()) {
                        TokenId id = cppTS.token().id();
                        if (id == CppTokenId.PREPROCESSOR_INCLUDE || id == CppTokenId.PREPROCESSOR_INCLUDE_NEXT) {
                            context.setText("" + insChr + matching(insChr) , 1);  // NOI18N
                        }
                    }
                }
            }
            return;
        }
        if (chr == '\n') {
            if (cppTS.movePrevious()) {
                if (cppTS.token().id() == CppTokenId.LINE_COMMENT) {
                    // typed char belongs to line comment
                    return;
                }
            }
        }
        if (isCompletablePosition(context.getDocument(), context.getOffset())) {
            context.setText("" + insChr + matching(insChr) , 1);  // NOI18N
        }
    }

    static int skipClosingBracket(TypedTextInterceptor.MutableContext context) throws BadLocationException {
        TokenSequence<TokenId> cppTS = cppTokenSequence(context.getDocument(), context.getOffset(), false);
        if (cppTS == null || isStringOrComment(cppTS.token().id())) {
            return -1;
        }
        CppTokenId rightBracketId = bracketCharToId(context.getText().charAt(0));
        if (isSkipClosingBracket(context, cppTS, rightBracketId)) {
            context.setText("", 0);  // NOI18N
            return context.getOffset() + 1;
        }
        return -1;
    }

    private static CppTokenId bracketCharToId(char bracket) {
        switch (bracket) {
            case '(':
                return CppTokenId.LPAREN;
            case ')':
                return CppTokenId.RPAREN;
            case '[':
                return CppTokenId.LBRACKET;
            case ']':
                return CppTokenId.RBRACKET;
            case '{':
                return CppTokenId.LBRACE;
            case '}':
                return CppTokenId.RBRACE;
            case '<':
                return CppTokenId.LT;
            case '>':
                return CppTokenId.GT;
            default:
                throw new IllegalArgumentException("Not a bracket char '" + bracket + '\'');  // NOI18N
        }
    }

    private static CppTokenId matching(CppTokenId id) {
        switch (id) {
            case LPAREN:
                return CppTokenId.RPAREN;
            case LBRACKET:
                return CppTokenId.RBRACKET;
            case RPAREN:
                return CppTokenId.LPAREN;
            case RBRACKET:
                return CppTokenId.LBRACKET;
            case LT:
                return CppTokenId.GT;
            case GT:
                return CppTokenId.LT;
            default:
                return null;
        }
    }

    /**
     * A token processor used to find out the length of a token.
     */
    static class MyTokenProcessor implements TokenProcessor {

        private TokenID tokenID;
        private int tokenStart = -1;

        @Override
        public boolean token(TokenID tokenID, TokenContextPath tcp,
                int tokBuffOffset, int tokLength) {
            this.tokenStart = tokenBuffer2DocumentOffset(tokBuffOffset);
            this.tokenID = tokenID;
            return false;
        }

        @Override
        public int eot(int offset) {
            return 0;
        }

        @Override
        public void nextBuffer(char[] buffer, int offset, int len, int startPos, int preScan, boolean lastBuffer) {
            this.bufferStartPos = startPos - offset;
        }
        private int bufferStartPos = 0;

        private int tokenBuffer2DocumentOffset(int offs) {
            return offs + bufferStartPos;
        }
    }

    /**
     * Token processor for finding of balance of brackets and braces.
     */
    private static class BalanceTokenProcessor extends CndAbstractTokenProcessor<Token<TokenId>> {

        private final CppTokenId leftTokenID;
        private final CppTokenId rightTokenID;
        private final Stack<Integer> stack = new Stack<Integer>();
        private int balance;
        private boolean isDefine;

        BalanceTokenProcessor(CppTokenId leftTokenID, CppTokenId rightTokenID) {
            this.leftTokenID = leftTokenID;
            this.rightTokenID = rightTokenID;
        }

        @Override
        public boolean token(Token<TokenId> token, int tokenOffset) {
            if (token.id() == CppTokenId.PREPROCESSOR_DIRECTIVE) {
                return true;
            }
            TokenId id = token.id();
            if(id instanceof CppTokenId) {
                switch ((CppTokenId)id) {
                    case NEW_LINE:
                        isDefine = false;
                        break;
                    case PREPROCESSOR_DEFINE:
                        isDefine = true;
                        break;
                    case PREPROCESSOR_IF:
                    case PREPROCESSOR_IFDEF:
                    case PREPROCESSOR_IFNDEF:
                        stack.push(balance);
                        break;
                    case PREPROCESSOR_ELSE:
                    case PREPROCESSOR_ELIF:
                        if (!stack.empty()) {
                            balance = stack.peek();
                        }
                        break;
                    case PREPROCESSOR_ENDIF:
                        if (!stack.empty()) {
                            stack.pop();
                        }
                        break;
                    default:
                        if (!isDefine) {
                            if (token.id() == leftTokenID) {
                                balance++;
                            } else if (token.id() == rightTokenID) {
                                balance--;
                            }
                        }
                }
            }
            return false;
        }

        private int getBalance() {
            return balance;
        }
    }
}
