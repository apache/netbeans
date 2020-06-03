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
package org.netbeans.modules.cnd.editor.fortran.indent;

import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;

/**
 *
 */
public class FortranIndentSupport {

    protected FortranCodeStyle codeStyle;
    protected TokenSequence<FortranTokenId> ts;

    /** Find the first non-whitespace and non-comment token in the given
     * direction. This is similair to <tt>findImportant()</tt>
     * but it operates over the tokens.
     * @param startToken token from which to start searching. For backward
     *  search this token is excluded from the search.
     * @param limitToken the token where the search will be broken
     *  reporting that nothing was found. It can be null to search
     *  till the end or begining of the chain (depending on direction).
     *  For forward search this token is not considered to be part of search,
     *  but for backward search it is.
     * @param backward true for searching in backward direction or false
     *  to serach in forward direction.
     * @return return the matching token or null if nothing was found
     */
    protected TokenItem findImportantToken(TokenItem startToken, TokenItem limitToken, boolean backward) {

        if (backward) { // go to the previous token for the backward search
            if (startToken != null && startToken.equals(limitToken)) { // empty search
                return null;
            }

            startToken = getPreviousToken(startToken);

            if (limitToken != null) {
                limitToken = limitToken.getPrevious();
            }
        }

        while (startToken != null && !startToken.equals(limitToken)) {
            if (isImportant(startToken)) {
                return startToken;
            }

            startToken = backward ? startToken.getPrevious() : startToken.getNext();
        }

        return null;
    }

    /** Get the previous token or last token if the argument is null. */
    protected TokenItem getPreviousToken(TokenItem token) {
        if (token == null) {
            ts.moveEnd();
            while (ts.movePrevious()){
                if (ts.token().id() != PREPROCESSOR_DIRECTIVE) {
                    return new TokenItem(ts);
                }
            }
            return null;
        }
        return token.getPrevious();
    }

    private boolean isImportant(TokenItem token) {
        return !isComment(token) && !isWhitespace(token);
    }

    public boolean isComment(TokenItem token) {
        return token.getTokenID() == LINE_COMMENT_FIXED || token.getTokenID() == LINE_COMMENT_FREE;
    }

    private boolean isWhitespace(TokenItem token) {
        return WHITESPACE_CATEGORY.equals(token.getTokenID().primaryCategory());
    }

    /** Determine if the given token is a "Free Format" Fortran comment.
     */
    public boolean isFreeFormatComment(TokenItem token) {
        return token != null && token.getTokenID() == LINE_COMMENT_FREE;
    }

    /** Determine if the given token is a "Fixed Format" Fortran comment.
     */
    public boolean isFixedFormatComment(TokenItem token) {
        return token != null && token.getTokenID() == LINE_COMMENT_FIXED;
    }

    public boolean isPreprocessor(TokenItem token) {
        return token != null && token.getTokenID() == PREPROCESSOR_DIRECTIVE;
    }

    public boolean isFixedFormatLabel(TokenItem token) {
        if (token != null && token.getTokenID() == NUM_LITERAL_INT && !getFreeFormat()) {
            if ((getTokenColumn(token) + token.getImage().length()) <= 5) {
                return true;
            }
        }
        return false;
    }

    public boolean isFixedFormatLineContinuation(TokenItem token) {
        return token != null && (token.getTokenID() == OP_PLUS || token.getTokenID() == OP_MINUS) &&
                !getFreeFormat() && getTokenColumn(token) == 5;
    }

    public FortranTokenId getWhitespaceTokenID() {
        return WHITESPACE;
    }

    public boolean canModifyWhitespace(TokenItem inToken) {
        switch (inToken.getTokenID()) {
            case WHITESPACE:
                return true;
        }
        return false;
    }

    /** Find the starting token in the line of code, given a particular token
     * @param token the starting point token
     * @return token the token at the start of the line of code
     */
    public TokenItem findLineStartToken(TokenItem token) {
        if (token != null) {
            token = findLineStart(token);
            while (true){
                if (token.getTokenID() == WHITESPACE) {
                    TokenItem t = token.getNext();
                    if (t == null) {
                        return token;
                    }
                    token = t;
                } else {
                    return token;
                }
            }
        }
        return token;
    }

    /** Get the starting position of the line. It searches for the new-line
     * character in backward direction and returns the position
     * of the character following
     * the new-line character or the first character of the first token
     * in the chain.
     * @param pos any token-position on the line.
     */
    public TokenItem findLineStart(TokenItem pos) {
        TokenItem token = pos.getPrevious();
        if (token == null){
            return pos;
        }
        while (true) {
            switch (token.getTokenID()) {
                case LINE_COMMENT_FIXED:
                case LINE_COMMENT_FREE:
                case NEW_LINE:
                    return token.getNext();
            }
            TokenItem t = token.getPrevious();
            if (t == null) {
                return token;
            }
            token = t;
        }
    }

    /** Determine if this if statement is a single line if statement or
     *  if it is a multiline if statement. A multiline if statement will
     *  always end with the "then" keyword.
     * @param startToken the starting token for this line of code
     * @return true if this is a multiline if statement.
     */
    public boolean isIfThenStatement(TokenItem startToken) {
        boolean find = false;
        while(startToken != null){
            switch(startToken.getTokenID()){
                case NEW_LINE:
                    return find;
                case KW_THEN:
                    find = true;
                    break;
                case WHITESPACE:
                    break;
                default:
                    find = false;
            }
            startToken = startToken.getNext();
        }
        return false;
    }

    /** Find the matching token for the supplied token. This will always
     *  do a backward search.
     * @param token - the token that ends the block of code, ie,
     *  "endselect", "end", "enddo", etc.
     * @param matchTokenID - the token numeric ID that you are trying to match,
     * ie, KW_SELECT_ID if you are trying to match the "select" token
     * @param matchEndKeywordID - the token numeric ID of an "end..." token,
     * ie, KW_ENDSELECT_ID if you are trying to match the "endselect" token
     * @return corresponding token that begins the block
     */
    public TokenItem findMatchingToken(TokenItem token, FortranTokenId matchTokenID, FortranTokenId matchEndKeywordID) {
        int depth = 0; // depth of multiple "end select" stmts
        TokenItem startToken;
        while (true) {
            TokenItem impToken = findImportantToken(token, null, true);
            startToken = token = findLineStartToken(impToken);
            if (token == null) {
                return null;
            }
            if (isFixedFormatLabel(startToken)) {
                // in fixed format: labels are not treated as start tokens
                //   line cont.can be ignored because a starting matchToken
                //   will not be found on a continuated line.
                do {
                    startToken = startToken.getNext();
                } while (startToken.getTokenID() == WHITESPACE);
            }
            FortranTokenId tokenID = startToken.getTokenID();
            if (tokenID == KW_END) {

                // is this "end" token is really an "end..." token then
                TokenItem tokenAfterEnd = startToken.getNext();
                while (tokenAfterEnd.getTokenID() == WHITESPACE) {
                    tokenAfterEnd = tokenAfterEnd.getNext();
                    if (tokenAfterEnd == null) {
                        return null;
                    }
                }

                if (tokenAfterEnd.getTokenID() == matchTokenID) {
                    depth++;
                }

            } else if (tokenID == matchEndKeywordID) {
                depth++;

            } else if (tokenID == matchTokenID) {
                if (matchTokenID == KW_IF && matchEndKeywordID == KW_ENDIF) {
                    // there must be a 'THEN' on this line to be a valid 'IF' match
                    TokenItem nextToken = startToken;
                    do {
                        nextToken = nextToken.getNext();
                        if (nextToken == null) {
                            return null;
                        }
                        if (nextToken.getImage().indexOf('\n') > -1) {
                            // break, unless the next line is a continuation
                            TokenItem t = nextToken.getNext();
                            if (!isFixedFormatLineContinuation(findLineStartToken(t))) {
                                break;
                            }
                        }
                    } while (nextToken.getTokenID() != KW_THEN);
                    if (nextToken.getImage().indexOf('\n') > -1) {
                        continue;
                    }
                } else if (matchTokenID == KW_TYPE && matchEndKeywordID == KW_ENDTYPE) {
                    TokenItem next = findImportantToken(startToken.getNext(), null, false);
                    if (next != null && next.getTokenID() == LPAREN) {
                        continue;
                    }
                }
                if (depth-- == 0) {
                    return token; // successful search
                }
            }
        }// end while
    }// end findMatchingToken()

    /** Get the indentation for the given token.
     * @param token token for which the indent is being searched.
     *  The token itself is ignored and the previous token
     *  is used as a base for the search.
     */
    public int getTokenIndent(TokenItem token) {

        TokenItem tp = token;
        TokenItem fnw = findLineFirstNonWhitespace(tp);

        if (fnw != null) { // valid first non-whitespace
            TokenItem t = fnw;
            if (isFixedFormatLabel(t) || isFixedFormatLineContinuation(t)) {
                do {
                    t = t.getNext();
                } while (t != null && t.getTokenID() == getWhitespaceTokenID());
                fnw = (t == null || t.getImage().length() > 0) ? null : t;
            }
            if (fnw != null) {
                tp = fnw;
            }
        }
        return getTokenColumn(tp);
    }

    /** Return the first non-whitespace character on the line
     * or null if there is no non-WS char on the line.
     */
    public TokenItem findLineFirstNonWhitespace(TokenItem pos) {
        TokenItem token = findLineStartToken(pos);
        if (token == null) { // no line start, no WS
            return null;
        }
        return moveToFirstLineImportantToken(token);
    }

    protected TokenItem moveToFirstLineImportantToken(TokenItem token){
        TokenItem t = token;
        while(true) {
            if (t == null) {
                return token;
            }
            switch (t.getTokenID()){
                case NEW_LINE:
                    return token;
                case WHITESPACE:
                    break;
                default:
                    return t;
            }
            token = t;
            t = token.getNext();
        }
    }


    /** Determines how many characters the token (after a fixed token)
     * needs to be indented.
     * The indentation is hence done with spaces NOT tabs.
     **/
    public int findInlineSpacing(TokenItem token) {
        // fill if short fixed format Label
        int additionalIndent = 0;
        TokenItem startToken = findLineStartToken(token);
        if (isFixedFormatLabel(startToken)) {
            additionalIndent = 4 - token.getImage().length();
            startToken = startToken.getNext();
        }

        // Search backwards ...
        TokenItem indentToken = findImportantToken(token, null, true);
        startToken = findLineStartToken(indentToken);
        if (startToken == null) {
            return 6;
        }
        // in fixed format: line cont. and preprocessors are not treated as important tokens
        while (isFixedFormatLineContinuation(startToken) || isPreprocessor(startToken) || startToken.getTokenID() == KW_ENTRY) {
            indentToken = findImportantToken(startToken, null, true);
            startToken = findLineStartToken(indentToken);
            if (startToken == null) {
                return 6;
            }
        }

        // ignore whitespace && fixed format labels
        while (isFixedFormatLabel(startToken) || startToken.getTokenID() == WHITESPACE) {
            startToken = startToken.getNext();
            if (startToken == null) {
                return 6;
            }
        }

        // check for END Tokens
        while (isFixedFormatLineContinuation(token) || isFixedFormatLabel(token) || token.getTokenID() == WHITESPACE) {
            token = token.getNext();
            if (token == null) {
                return 6;
            }
        }
        if (token.getTokenID() == KW_SUBROUTINE || token.getTokenID() == KW_ENTRY || token.getTokenID() == KW_FUNCTION) {
            return 6;
        }

        // although this is cheap and [PENDING] improvement
        // it was the quickest way without some re-engineering of this class :(
        if ((token.getImage().length() > 2 && token.getImage().substring(0, 3).equalsIgnoreCase("end")) //NOI18N
                || token.getTokenID() == KW_ELSE || token.getTokenID() == KW_ELSEIF) {
            additionalIndent -= getShiftWidth();
        }

        return Math.max(6, getTokenColumn(startToken) + additionalIndent);
    }

    protected boolean getFreeFormat() {
        return codeStyle.getFormatFortran() == CndLexerUtilities.FortranFormat.FREE;
    }

    protected int getTabSize() {
        return codeStyle.getTabSize();
    }

    protected int getShiftWidth() {
        return codeStyle.indentSize();
    }

    protected int go(TokenItem t) {
        TokenSequence<FortranTokenId> tokenSeq = t.getTokenSequence();
        int aIndex = tokenSeq.index();
        tokenSeq.moveIndex(t.index());
        tokenSeq.moveNext();
        return aIndex;
    }

    protected int getTokenColumn(TokenItem t){
        TokenSequence<FortranTokenId> tokenSeq = t.getTokenSequence();
        int aIndex = go(t);
        try {
            int column = 0;
            while(tokenSeq.movePrevious()){
                switch (tokenSeq.token().id()) {
                    case NEW_LINE:
                    case PREPROCESSOR_DIRECTIVE:
                    case LINE_COMMENT_FIXED:
                    case LINE_COMMENT_FREE:
                         return column;
                    case WHITESPACE:
                    {
                        String text = tokenSeq.token().text().toString();
                        for(int i = 0; i < text.length(); i++){
                            char c = text.charAt(i);
                            if (c == '\t'){
                                column = (column/getTabSize()+1)* getTabSize();
                            } else {
                                column += 1;
                            }
                        }
                        break;
                    }
                    default:
                        column += tokenSeq.token().length();
                        break;
                }
            }
            return column;
        } finally {
            tokenSeq.moveIndex(aIndex);
            tokenSeq.moveNext();
        }
    }
}
