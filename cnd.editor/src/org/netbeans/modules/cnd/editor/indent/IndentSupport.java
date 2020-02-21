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

package org.netbeans.modules.cnd.editor.indent;

import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.editor.api.CodeStyle;

/**
 *
 */
public class IndentSupport {
    protected CodeStyle codeStyle;
    protected TokenSequence<TokenId> ts;

    /** Find the token either by token-id or token-text or both.
    * @param startToken token from which to start searching. For backward
    *  search this token is excluded from the search.
    * @param limitToken the token where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    *  For forward search this token is not considered to be part of search,
    *  but for backward search it is.
    * @param tokenID token-id to be searched. If null the token-id
    *  of the tokens inspected will be ignored.
    * @param tokenImage text of the token to find. If null the text
    *  of the tokens inspected will be ignored.
    * @param backward true for searching in backward direction or false
    *  to serach in forward direction.
    * @return return the matching token or null if nothing was found
    */
    protected TokenItem findToken(TokenItem startToken, TokenItem limitToken, CppTokenId tokenID, boolean backward) {

        if (backward) { // go to the previous token for the backward search
            if (startToken != null && startToken.equals(limitToken)) { // empty search
                return null;
            }

            startToken = getPreviousToken(startToken);

            if (limitToken != null) {
                limitToken = limitToken.getPrevious();
            }
        }

        while (startToken != null && startToken.equals(limitToken)) {
            if (startToken.getTokenID() == tokenID) {
                return startToken;
            }

            startToken = backward ? startToken.getPrevious() : startToken.getNext();
        }

        return null;
    }

    /** This method can be used to find a matching brace token. Both
    * the token-id and token-text are used for comparison of the starting token.
    * @param startToken token from which to start. It cannot be null.
    *  For backward search this token is ignored and the previous one is used.
    * @param limitToken the token where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    *  For forward search this token is not considered to be part of search,
    *  but for backward search it is.
    * @param matchTokenID matching token-id for the start token.
    * @param matchTokenImage matching token-text for the start token.
    * @param backward true for searching in backward direction or false
    *  to serach in forward direction.
    */
    protected TokenItem findMatchingToken(TokenItem startToken, TokenItem limitToken, CppTokenId matchTokenID, boolean backward) {

        int depth = 0;
        TokenId startTokenID = startToken.getTokenID();

        // Start to search from the adjacent item
        TokenItem token = backward ? startToken.getPrevious() : startToken.getNext();

        while (token != null && !token.equals(limitToken)) {
            if (token.getTokenID() == matchTokenID) {
                if (depth-- == 0) {
                    return token;
                }

            } else if (token.getTokenID() == startTokenID) {
                depth++;
            }

            token = backward ? token.getPrevious() : token.getNext();
        }

        return null;
    }

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

    private boolean isComment(TokenItem token) {
        TokenId tokenID = token.getTokenID();
        if(tokenID instanceof CppTokenId) {
            switch ((CppTokenId)tokenID) {
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                case DOXYGEN_COMMENT:
                case DOXYGEN_LINE_COMMENT:
                    return true;
            }
        }
        return false;
    }

    private boolean isImportant(TokenItem token) {
        return !isComment(token) && !isWhitespace(token);
    }

    /** Decide whether the character at the given offset in the given token
     * is whitespace.
     */
    private boolean isWhitespace(TokenItem token) {
        return CppTokenId.WHITESPACE_CATEGORY.equals(token.getTokenID().primaryCategory());
    }


    /** Search for any of the image tokens from the given array
    * and return if the token matches any item from the array.
    * The index of the item from the array that matched
    * can be found by calling <tt>getIndex()</tt> method.
    * It is suitable mainly for the image-token-ids.
    *
    * @param startToken token from which to start. For backward search
    *  this token is excluded from the search.
    * @param limitToken the token where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    *  For forward search this token is not considered to be part of search,
    *  but for backward search it is.
    * @param tokenIDArray array of the token-ids for which to search.
    * @param tokenContextPath context path that the found token must have.
    *  It can be null.
    * @param backward true for searching in backward direction or false
    *  to serach in forward direction.
    */
    protected TokenItem findAnyToken(TokenItem startToken, TokenItem limitToken, CppTokenId[] tokenIDArray, boolean backward) {

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
            for (int i = 0; i < tokenIDArray.length; i++) {
                if (startToken.getTokenID() == tokenIDArray[i]){
                    return startToken;
                }
            }

            startToken = backward ? startToken.getPrevious() : startToken.getNext();
        }

        return null;
    }

    /** Find the start of the statement.
     * @param token token from which to start. It searches
     *  backward using <code>findStatement()</code> so it ignores
     *  the given token.
     * @return the statement start token (outer statement start for nested
     *  statements).
     *  It returns the same token if there is '{' before
     *  the given token.
     */
    protected TokenItem findStatementStart(TokenItem token) {
        return findStatementStart(token, false);
    }

    protected TokenItem findStatementStart(TokenItem token, boolean outermost) {
        TokenItem t = findStatement(token);
        if (t != null) {
            TokenId tokenID = t.getTokenID();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case SEMICOLON: // ';' found
                        TokenItem scss = findStatement(t);
                        if (scss == null) {
                            return token;
                        }
                        TokenId scssTokenID = scss.getTokenID();
                        if(scssTokenID instanceof CppTokenId) {
                            switch ((CppTokenId)scssTokenID) {
                                case LBRACE: // '{' then ';'
                                case RBRACE: // '}' then ';'
                                case COLON: // ':' then ';'
                                case CASE: // 'case' then ';'
                                case DEFAULT:
                                case SEMICOLON: // ';' then ';'
                                    return t; // return ';'

                                case PRIVATE:
                                case PROTECTED:
                                case PUBLIC:

                                case DO:
                                case FOR:
                                case IF:
                                case WHILE:
                                    return findStatementStart(t, outermost);

                                case ELSE: // 'else' then ';'
                                    // Find the corresponding 'if'
                                    TokenItem ifss = findIf(scss);
                                    if (ifss != null) { // 'if' ... 'else' then ';'
                                        return findStatementStart(ifss, outermost);
                                    } else { // no valid starting 'if'
                                        return scss; // return 'else'
                                    }

                                default: // something usual then ';'
                                    TokenItem bscss = findStatement(scss);
                                    if (bscss != null) {
                                        TokenId bscssTokenID = bscss.getTokenID();
                                        if(bscssTokenID instanceof CppTokenId) {
                                            switch ((CppTokenId)bscssTokenID) {
                                                case SEMICOLON: // ';' then stmt ending with ';'
                                                case LBRACE:
                                                case RBRACE:
                                                case COLON:
                                                    return scss; //

                                                case DO:
                                                case FOR:
                                                case IF:
                                                case WHILE:
                                                    return findStatementStart(bscss, outermost);

                                                case ELSE:
                                                    // Find the corresponding 'if'
                                                    ifss = findIf(bscss);
                                                    if (ifss != null) { // 'if' ... 'else' ... ';'
                                                        return findStatementStart(ifss, outermost);
                                                    } else { // no valid starting 'if'
                                                        return bscss; // return 'else'
                                                    }
                                            }
                                        }
                                    }
                            }
                        } // semicolon servicing end
                        return scss;

                    case LBRACE: // '{' found
                        return token; // return original token

                    case RBRACE: // '}' found
                        TokenItem lb = findMatchingToken(t, null, CppTokenId.LBRACE, true);
                        if (lb != null) { // valid matching left-brace
                            // Find a stmt-start of the '{'
                            TokenItem lbss = findStatement(lb);
                            if (lbss != null) {
                                TokenId lbssTokenID = lbss.getTokenID();
                                if(lbssTokenID instanceof CppTokenId) {
                                    switch ((CppTokenId)lbssTokenID) {
                                        case ELSE: // 'else {'
                                            // Find the corresponding 'if'
                                            TokenItem ifss = findIf(lbss);
                                            if (ifss != null) { // valid 'if'
                                                return findStatementStart(ifss, outermost);
                                            } else {
                                                return lbss; // return 'else'
                                            }

                                        case CATCH: // 'catch (...) {'
                                            // I'm not sure what to do if this isn't C++...
                                            // Find the corresponding 'try'
                                            TokenItem tryss = findTry(lbss);
                                            if (tryss != null) { // valid 'try'
                                                return findStatementStart(tryss, outermost);
                                            } else {
                                                return lbss; // return 'catch'
                                            }

                                        case DO:
                                        case FOR:
                                        case IF:
                                        case WHILE:
                                            return findStatementStart(lbss, outermost);
                                    }
                                }
                                // I copied the next if from JavaFormatSupport. But I'm not 100% certain it
                                // applies...
                                if (lbss.getTokenID() == CppTokenId.LBRACE) {
                                    return t; // return right brace
                                }
                                return lbss;
                            }
                        }
                        return t; // return right brace

                    case COLON:
                    case CASE:
                    case DEFAULT:
                        return token;

                    case ELSE:
                        // Find the corresponding 'if'
                        TokenItem ifss = findIf(t);
                        if (ifss != null) {
                            if (!outermost) {
                                return ifss;
                            } else {
                                return findStatementStart(ifss, outermost);
                            }
                        }
                        return t;

                    case DO:
                    case FOR:
                    case IF:
                    case WHILE:
                        if (!outermost) {
                            return t;
                        } else {
                            return findStatementStart(t, outermost);
                        }

                    case IDENTIFIER:
                        return t;
                    default:
                        return t;
                }
            }
        }
        return token; // return original token
    }

    /** Find the starting token of the statement before
     * the given position and also return all the command
     * delimiters. It searches in the backward direction
     * for all the delimiters and statement starts and
     * return all the tokens that are either command starts
     * or delimiters. As the first step it uses
     * <code>getPreviousToken()</code> so it ignores the initial token.
     * @param token token before which the statement-start
     *  and delimiter is being searched.
     * @return token that is start of the given statement
     *  or command delimiter.
     *  If the start of the statement is not found, null is returned.
     */
    protected TokenItem findStatement(TokenItem token) {
        TokenItem lit = null; // last important token
        boolean firstColon = true;
        TokenItem t = getPreviousToken(token);
        int paren = 0;
        int bracket = 0;

        while (t != null) {
            TokenId tTokenID = t.getTokenID();
            if(tTokenID instanceof CppTokenId) {
                switch ((CppTokenId)tTokenID) {
                    case SEMICOLON:
                        if (!isForLoopSemicolon(t)) {
                            return (lit != null) ? lit : t;
                        }
                        break;
                    
                    case LPAREN:
                        paren--;
                        break;
                    case RPAREN:
                        paren++;
                        break;
                    case LBRACKET:
                        bracket--;
                        if (bracket == 0 && paren == 0) {// && wasRbracketAndLparen) {
                            TokenItem prev = findImportantToken(t, null, true);
                            if (prev != null && (prev.getTokenID() instanceof CppTokenId)) {
                                switch ((CppTokenId)prev.getTokenID()) {
                                    case IDENTIFIER:
                                    case LBRACKET:
                                    case RBRACKET:
                                        break;
                                    default:
                                        return t;
                                }
                            }
                        }
                        break;
                    case RBRACKET:
                        bracket++;
                        break;

                    case LBRACE:
                    case ELSE:
                        return (lit != null) ? lit : t;

                    case RBRACE:
                        // Check whether this is an array initialization block
                        if (!isArrayInitializationBraceBlock(t, null)) {
                            return (lit != null) ? lit : t;
                        } else { // skip the array initialization block
                            t = findMatchingToken(t, null, CppTokenId.LBRACE, true);
                        }
                        break;

                    case COLON:
                        TokenItem tt = findAnyToken(t, null, new CppTokenId[] {CppTokenId.CASE, CppTokenId.DEFAULT,
                        CppTokenId.PUBLIC, CppTokenId.PRIVATE, CppTokenId.PROTECTED,
                        CppTokenId.LBRACE, CppTokenId.RBRACE, CppTokenId.SEMICOLON,
                        CppTokenId.QUESTION}, true);
                        if (tt != null) {
                            TokenId ttTokenID = tt.getTokenID();
                            if(ttTokenID instanceof CppTokenId) {
                                switch ((CppTokenId)ttTokenID) {
                                    case PUBLIC:
                                    case PRIVATE:
                                    case PROTECTED:
                                    case CASE:
                                    case DEFAULT:
                                        return (lit != null) ? lit : t;
                                }
                            }
                        }
                        TokenItem prev = findImportantToken(t, null, true);
                        if (prev != null && prev.getTokenID() == CppTokenId.RPAREN) {
                            t = prev;
                            break;
                        }
                        if (lit != null && firstColon && tt == null){
                            return lit;
                        }
                        firstColon = false;
                        break;

                    case DO:
                    case SWITCH:
                    case CASE:
                    case DEFAULT:
                        return t;

                    case FOR:
                    case IF:
                    case WHILE:
                        /* Try to find the statement after ( ... )
                         * If it exists, then the first important
                         * token after it is the stmt start. Otherwise
                         * it's this token.
                         */
                        if (lit != null && lit.getTokenID() == CppTokenId.LPAREN) {
                            // Find matching right paren in fwd dir
                            TokenItem mt = findMatchingToken(lit, token,
                                    CppTokenId.RPAREN, false);
                            if (mt != null){
                                mt = mt.getNext();
                                if (mt != null) {
                                    mt = findImportantToken(mt, token, false);
                                    if (mt != null) {
                                        return mt;
                                    }
                                }
                            }
                        }
                        // No further stmt found, return this one
                        return t;
                }
            }
            // Remember last important token (preprocessor token are not important (?) (4922370))
            if (isImportant(t)) {
                lit = t;
            }
            t = t.getPrevious();
        }
        return lit;
    }


    /**
     * Find the 'if' when the 'else' is provided.
     * @param elseToken the token with the 'else' command
     *  for which the 'if' is being searched.
     * @return corresponding 'if' token or null if there's
     *  no corresponding 'if' statement.
     */
    protected TokenItem findIf(TokenItem elseToken) {
        assert(elseToken != null && elseToken.getTokenID() == CppTokenId.ELSE);

        int braceDepth = 0; // depth of the braces
        int elseDepth = 0; // depth of multiple else stmts
        while (true) {
            elseToken = findStatement(elseToken);
            if (elseToken == null) {
                return null;
            }

            TokenId elseTokenID = elseToken.getTokenID();
            if(elseTokenID instanceof CppTokenId) {
                switch ((CppTokenId)elseTokenID) {
                    case LBRACE:
                        if (--braceDepth < 0) {
                            return null; // no corresponding right brace
                        }
                        break;

                    case RBRACE:
                        braceDepth++;
                        break;

                    case ELSE:
                        if (braceDepth == 0) {
                            elseDepth++;
                        }
                        break;

                    case SEMICOLON:
                    case COLON:
                    case DO:
                    case CASE:
                    case DEFAULT:
                    case FOR:
                    case WHILE:
                        break;

                    case IF:
                        if (braceDepth == 0) {
                            if (elseDepth-- == 0) {
                                return elseToken; // successful search
                            }
                        }
                        break;
                }
            }
        }
    }


    /** Find the 'switch' when the 'case' is provided.
     * @param caseToken the token with the 'case' command
     *  for which the 'switch' is being searched.
     * @return corresponding 'switch' token or null if there's
     *  no corresponding 'switch' statement.
     */
    protected TokenItem findSwitch(TokenItem caseToken) {
        assert (caseToken != null && (caseToken.getTokenID() == CppTokenId.CASE || caseToken.getTokenID() == CppTokenId.DEFAULT));

        int braceDepth = 1; // depth of the braces - need one more left
        while (true) {
            caseToken = findStatement(caseToken);
            if (caseToken == null) {
                return null;
            }

            TokenId caseTokenID = caseToken.getTokenID();
            if(caseTokenID instanceof CppTokenId) {
                switch ((CppTokenId)caseTokenID) {
                    case LBRACE:
                        if (--braceDepth < 0) {
                            return null; // no corresponding right brace
                        }
                        break;

                    case RBRACE:
                        braceDepth++;
                        break;

                    case SWITCH:
                    case DEFAULT:
                        if (braceDepth == 0) {
                            return caseToken;
                        }
                        break;
                }
            }
        }
    }

    /** Find the 'class' or 'struct'
     * @param visibilityToken the token.
     */
    protected TokenItem findClassifier(TokenItem visibilityToken) {
        int braceDepth = 1; // depth of the braces - need one more left
        TokenItem previous = visibilityToken;
        while (true) {
            TokenId previousID = previous.getTokenID();
            switch ((CppTokenId)previousID) {
                case LBRACE:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    if (braceDepth == 0){
                        TokenItem classifierToken = findStatementStart(previous);
                        if (classifierToken != null) {
                            if (classifierToken.getTokenID() == CppTokenId.CLASS ||
                                classifierToken.getTokenID() == CppTokenId.STRUCT ||
                                classifierToken.getTokenID() == CppTokenId.UNION) {
                                TokenItem next = previous;
                                while(true) {
                                    TokenId nextID = next.getTokenID();
                                    switch ((CppTokenId)nextID) {
                                        case LPAREN:
                                            return null;
                                        case LBRACE:
                                            return classifierToken;
                                    }
                                    next = next.getNext();
                                }
                            }
                        }
                        if (classifierToken == null) {
                            return null;
                        }
                    }
                    break;

                case RBRACE:
                    braceDepth++;
                    break;
            }
            previous = previous.getPrevious();
            if (previous == null) {
                return null;
            }
        }
    }


    /** Find the 'try' when the 'catch' is provided.
     * @param catchToken the token with the 'catch' command
     *  for which the 'try' is being searched.
     * @return corresponding 'try' token or null if there's
     *  no corresponding 'try' statement.
     */
    protected TokenItem findTry(TokenItem catchToken) {
        assert(catchToken != null && catchToken.getTokenID() == CppTokenId.CATCH);

        int braceDepth = 0; // depth of the braces
        while (true) {
            catchToken = findStatement(catchToken);
            if (catchToken == null) {
                return null;
            }

            TokenId catchTokenID = catchToken.getTokenID();
            if(catchTokenID instanceof CppTokenId) {
                switch ((CppTokenId)catchTokenID) {
                    case LBRACE:
                        if (--braceDepth < 0) {
                            return null; // no corresponding right brace
                        }
                        break;

                    case RBRACE:
                        braceDepth++;
                        break;

                    case TRY:
                        if (braceDepth == 0) {
                            return catchToken;
                        }
                        break;
                }
            }
        }
    }

    protected boolean isFieldComma(TokenItem token) {
        while (token != null && token.getTokenID() == CppTokenId.COMMA) {
            TokenItem itm = findStatementStart(token);
            if (itm == token) {
                break;
            }
            token = itm;
        }
        if (token != null) {
            TokenItem itm = findImportantToken(token, null, true);
            if (itm != null && itm.getTokenID() == CppTokenId.LBRACE) {
                TokenItem startItem = findStatementStart(itm);
                if (startItem != null) {
                    if (startItem.getTokenID() == CppTokenId.CLASS ||
                        startItem.getTokenID() == CppTokenId.STRUCT ||
                        startItem.getTokenID() == CppTokenId.UNION) {
                        TokenItem next = startItem;
                        while(true) {
                            TokenId nextID = next.getTokenID();
                            switch ((CppTokenId)nextID) {
                                case LPAREN:
                                    return false;
                                case LBRACE:
                                    return true;
                            }
                            next = next.getNext();
                        }
                    }
                }
            } else if (itm != null && itm.getTokenID() == CppTokenId.SEMICOLON) {
                return findClassifier(itm) != null;
            } else if (itm != null && itm.getTokenID() == CppTokenId.RBRACE) {
                return findClassifier(itm) != null;
            }
        }
        return false;
    }

    protected boolean isEnumComma(TokenItem token) {
        while (token != null && token.getTokenID() == CppTokenId.COMMA) {
            TokenItem itm = findStatementStart(token);
            if (itm == token) {
                break;
            }
            token = itm;
        }
        if (token != null &&
            (token.getTokenID() == CppTokenId.IDENTIFIER || token.getTokenID() == CppTokenId.DOT)) {
            TokenItem itm = findImportantToken(token, null, true);
            if (itm != null && itm.getTokenID() == CppTokenId.LBRACE) {
                TokenItem startItem = findStatementStart(itm);
                if (startItem != null) {
                    if (startItem.getTokenID() == CppTokenId.ENUM ||
                        findToken(startItem, itm, CppTokenId.ENUM, false) != null ||
                        findToken(startItem, itm, CppTokenId.EQ, false) != null) {
                        return true;
                    }
                    startItem = findStatementStart(startItem);
                    if (startItem != null) {
                        if (startItem.getTokenID() == CppTokenId.ENUM ||
                            findToken(startItem, itm, CppTokenId.ENUM, false) != null ||
                            findToken(startItem, itm, CppTokenId.EQ, false) != null) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given semicolon is inside the for() statement
     *
     * @param token token to check. It must be a semicolon
     * @return true if the given semicolon is inside the for() statement, or false otherwise
     */
    protected boolean isForLoopSemicolon(TokenItem token) {
        assert (token != null && token.getTokenID() == CppTokenId.SEMICOLON);

        int parDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        token = token.getPrevious(); // ignore this semicolon
        while (token != null) {
            TokenId tokenID = token.getTokenID();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case LPAREN:
                        if (parDepth == 0) { // could be a 'for ('
                            TokenItem tp = findImportantToken(token, null, true);
                            if (tp != null && tp.getTokenID() == CppTokenId.FOR) {
                                return true;
                            }
                            return false;
                        } else { // non-zero depth
                            parDepth--;
                        }
                        break;
                    case RPAREN:
                        parDepth++;
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
                    case SEMICOLON:
                        if (semicolonFound) { // one semicolon already found
                            return false;
                        }
                        semicolonFound = true;
                        break;
                }
            }
            token = token.getPrevious();
        }

        return false;
    }

    protected boolean isStatement(TokenItem t){
        boolean likeDeclaration = false;
        boolean findLParen = false;
        boolean findQuestion = false;
        int identifiers = 0;
        while (t != null) {
            TokenId tTokenID = t.getTokenID();
            if(tTokenID instanceof CppTokenId) {
                switch ((CppTokenId)tTokenID) {
                    case EQ:
                    case LTLT:
                    case GTGT:
                    case PLUSEQ:
                    case MINUSEQ:
                    case LTLTEQ:
                    case GTGTEQ:
                    case AMPEQ:
                    case BAREQ:
                    case STAREQ:
                    case SLASHEQ:
                    case CARETEQ:
                    case PERCENTEQ:
                    case DELETE:
                    case RETURN:
                    case BREAK:
                    case CASE:
                    case CATCH:
                    case CONTINUE:
                    case DEFAULT:
                    case DO:
                    case ELSE:
                    case FOR:
                    case GOTO:
                    case IF:
                    case SIZEOF:
                    case TYPEID:
                    case TYPEOF:
                    case __HAS_TRIVIAL_CONSTRUCTOR:
                    case __HAS_NOTHROW_ASSIGN:
                    case __HAS_NOTHROW_COPY:
                    case __HAS_NOTHROW_CONSTRUCTOR:
                    case __HAS_TRIVIAL_ASSIGN:
                    case __HAS_TRIVIAL_COPY:
                    case __HAS_TRIVIAL_DESTRUCTOR:
                    case __IS_ABSTRACT:
                    case __IS_EMPTY:
                    case __IS_LITERAL_TYPE:
                    case __IS_POLYMORPHIC:
                    case __IS_STANDARD_LAYOUT:
                    case __IS_TRIVIAL:
                    case __IS_UNION:
                    case __UNDERLYING_TYPE:
                    case __IS_CLASS:
                    case __IS_BASE_OF:
                    case __IS_POD:                    
                    case SWITCH:
                    case THIS:
                    case THROW:
                    case TRY:
                    case USING:
                    case WHILE:
                        return true;
                    case SEMICOLON:
                        if (likeDeclaration) {
                            return false;
                        }
                        return true;
                    case FRIEND:
                    case EXPLICIT:
                    case EXTERN:
                    case CLASS:
                    case STATIC:
                    case OPERATOR:
                    case PRIVATE:
                    case PROTECTED:
                    case PUBLIC:
                    case NAMESPACE:
                    case TEMPLATE:
                    case UNION:
                    case ENUM:
                    case VIRTUAL:
                    case INLINE:
                    case _INLINE:
                    case __INLINE:
                    case __INLINE__:
                    case LBRACE:
                        return false;
                    case COLON:
                        if (!findQuestion) {
                            return false;
                        }
                        break;
                    case QUESTION:
                        findQuestion = true;
                        break;
                    case SCOPE:
                        if (!findLParen && identifiers == 1){
                            likeDeclaration = true;
                        }
                        break;
                    case RPAREN:
                        break;
                    case LPAREN:
                        if (!findLParen && identifiers > 1){
                            likeDeclaration = true;
                        }
                        findLParen = true;
                        break;
                    case ASM:
                    case AUTO:
                    case BOOL:
                    case CHAR:
                    case DOUBLE:
                    case EXPORT:
                    case FLOAT:
                    case INT:
                    case LONG:
                    case MUTABLE:
                    case REGISTER:
                    case SHORT:
                    case SIGNED:
                    case STRUCT:
                    case TYPEDEF:
                    case TYPENAME:
                    case UNSIGNED:
                    case VOID:
                    case WCHAR_T:
                    case VOLATILE:
                    case CONST:
                        if (!findLParen) {
                            return false;
                        }
                        break;
                    case IDENTIFIER:
                        identifiers++;
                        break;
                }
            }
            t = t.getNext();
        }
        return true;
    }

    protected TokenItem findClassifierStart(TokenItem token) {
        while (true) {
            token = findStatement(token);
            if (token == null) {
                return null;
            }
            TokenId tokenID = token.getTokenID();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case LBRACE:
                    case RBRACE:
                    case SEMICOLON:
                        return null;
                    case TEMPLATE:
                        return findStatementStart(token);
                }
            }
        }
    }

    protected TokenItem getVisibility(TokenItem token) {
        TokenItem t = token;
        if (t != null){
            t = token.getPrevious();
        }
        while (t != null) {
            TokenId tTokenID = t.getTokenID();
            if(tTokenID instanceof CppTokenId) {
                switch ((CppTokenId)tTokenID) {
                    case SEMICOLON:
                    case LBRACE:
                    case RBRACE:
                        return null;
                    case PRIVATE:
                    case PROTECTED:
                    case PUBLIC:
                        return t;
                }
            }
            t = t.getPrevious();
        }
        return null;
    }

    /**
     * Check whether there are left parenthesis before the given token
     * until the limit token.
     *
     * @param token non-null token from which to start searching back.
     * @param limitToken limit token when reached the search will stop
     *  with returning false.
     * @return true if there is LPAREN token before the given token
     *  (while respecting paren nesting).
     */
    protected TokenItem getLeftParen(TokenItem token, TokenItem limitToken) {
        int depth = 0;
        token = token.getPrevious();

        while (token != null && !token.equals(limitToken)) {
            TokenId tokenID = token.getTokenID();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case LPAREN:
                        if (--depth < 0) {
                            return token;
                        }
                        break;
                    case RPAREN:
                        depth++;
                        break;
                }
            }
            token = token.getPrevious();
        }
        return null;
    }

    /**
     * Check whether the given token is located in array initialization block.
     *
     * @param token non-null token from which to start searching back.
     * @param limitToken limit token when reached the search will stop
     *  with returning false.
     * @return true if the token is located inside the brace block of array
     *  initialization.
     */
    protected boolean isArrayInitializationBraceBlock(TokenItem token, TokenItem limitToken) {
        int depth = 0;
        token = token.getPrevious();

        while (token != null && !token.equals(limitToken)) {
            TokenId tokenID = token.getTokenID();
            if(tokenID instanceof CppTokenId) {
                switch ((CppTokenId)tokenID) {
                    case RBRACE:
                        depth++;
                        break;

                    case LBRACE:
                        depth--;
                        if (depth < 0) {
                            TokenItem prev = findImportantToken(token, limitToken, true);
                            // Array initialization left brace should be preceded
                            // by either '=' or ']' i.e.
                            // either "String array = { "a", "b", ... }"
                            // or     "String array = new String[] { "a", "b", ... }"
                            return (prev != null && (CppTokenId.RBRACKET.equals(prev.getTokenID()) || CppTokenId.EQ.equals(prev.getTokenID())));
                        }
                        break;

                    // Array initialization block should not contain statements or ';'
                    case DO:
                    case FOR:
                    case IF:
                    case WHILE:
                    case SEMICOLON:
                        if (depth == 0) {
                            return false;
                        }
                }
            }
            token = token.getPrevious();
        }
        return false;
    }

    protected CodeStyle getCodeStyle(){
        return this.codeStyle;
    }

    protected boolean expandTabs() {
        return codeStyle.expandTabToSpaces();
    }

    protected int getTabSize() {
        return codeStyle.getTabSize();
    }

    protected int getRightIndent(){
        int i = getShiftWidth();
        if (isHalfIndentNewlineBeforeBrace()){
            return i/2;
        }
        return i;
    }

    protected int getRightIndentSwitch(){
        int i = getShiftWidth();
        if (isHalfIndentNewlineBeforeBrace()){
            return i/2;
        }
        return i;
    }

    protected int getRightIndentDeclaration(){
        int i = getShiftWidth();
        if (isHalfIndentNewlineBeforeBraceDeclaration()){
            return i/2;
        }
        return i;
    }

    protected int getRightIndentLambda(){
        int i = getShiftWidth();
        if (isHalfIndentNewlineBeforeBraceLambda()){
            return i/2;
        }
        return i;
    }

    protected boolean indentCasesFromSwitch() {
        return getCodeStyle().indentCasesFromSwitch();
    }

    protected boolean indentNamespace() {
        return getCodeStyle().indentNamespace();
    }

    protected boolean isHalfIndentNewlineBeforeBrace() {
        return getCodeStyle().getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED;
    }

    protected boolean isFullIndentNewlineBeforeBrace() {
        return getCodeStyle().getFormatNewlineBeforeBrace() == CodeStyle.BracePlacement.NEW_LINE_FULL_INDENTED;
    }

    protected boolean isHalfIndentNewlineBeforeBraceSwitch() {
        return getCodeStyle().getFormatNewLineBeforeBraceSwitch() == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED;
    }

    protected boolean isFullIndentNewlineBeforeBraceSwitch() {
        return getCodeStyle().getFormatNewLineBeforeBraceSwitch() == CodeStyle.BracePlacement.NEW_LINE_FULL_INDENTED;
    }

    protected boolean isHalfIndentNewlineBeforeBraceLambda() {
        return getCodeStyle().getFormatNewlineBeforeBraceLambda() == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED;
    }

    protected boolean isFullIndentNewlineBeforeBraceLambda() {
        return getCodeStyle().getFormatNewlineBeforeBraceLambda() == CodeStyle.BracePlacement.NEW_LINE_FULL_INDENTED;
    }

    protected boolean isHalfIndentNewlineBeforeBraceDeclaration() {
        return getCodeStyle().getFormatNewlineBeforeBraceDeclaration() == CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED;
    }

    protected boolean isFullIndentNewlineBeforeBraceDeclaration() {
        return getCodeStyle().getFormatNewlineBeforeBraceDeclaration() == CodeStyle.BracePlacement.NEW_LINE_FULL_INDENTED;
    }

    protected boolean isHalfIndentVisibility() {
        return getCodeStyle().indentVisibility() == CodeStyle.VisibilityIndent.HALF_INDENT;
    }

    protected boolean getFormatLeadingStarInComment() {
        return getCodeStyle().getFormatLeadingStarInComment();
    }

    protected int getFormatStatementContinuationIndent() {
        return getCodeStyle().getFormatStatementContinuationIndent();
    }

    protected int getShiftWidth() {
        return getCodeStyle().indentSize();
    }

    protected boolean alignMultilineCallArgs() {
        return getCodeStyle().alignMultilineCallArgs();
    }

    protected boolean alignMultilineIf() {
        return getCodeStyle().alignMultilineIfCondition();
    }

    protected boolean alignMultilineWhile() {
        return getCodeStyle().alignMultilineWhileCondition();
    }

    protected boolean alignMultilineFor() {
        return getCodeStyle().alignMultilineFor();
    }

    protected boolean alignMultilineMethodParams() {
        return getCodeStyle().alignMultilineMethodParams();
    }

    /** Get the previous token or last token if the argument is null. */
    protected TokenItem getPreviousToken(TokenItem token) {
        if (token == null) {
            ts.moveEnd();
            while (ts.movePrevious()){
                if (ts.token().id() != CppTokenId.PREPROCESSOR_DIRECTIVE) {
                    return new TokenItem(ts, true);
                }
            }
            return null;
        }
        return token.getPrevious();
    }

    protected int go(TokenItem t) {
        TokenSequence<TokenId> tokenSeq = t.getTokenSequence();
        int aIndex = tokenSeq.index();
        tokenSeq.moveIndex(t.index());
        tokenSeq.moveNext();
        return aIndex;
    }
    
    protected int getTokenColumnAfterBrace(TokenItem t){
        int column = getTokenColumn(t);
        TokenSequence<TokenId> tokenSeq = t.getTokenSequence();
        int aIndex = go(t);
        try {
            while(tokenSeq.moveNext()){
                TokenId tokenID = tokenSeq.token().id();
                if(tokenID instanceof CppTokenId) {
                    switch ((CppTokenId)tokenID) {
                        case NEW_LINE:
                        case PREPROCESSOR_DIRECTIVE:
                             return column;
                        case DOXYGEN_COMMENT:
                        case BLOCK_COMMENT:
                        {
                            String text = tokenSeq.token().text().toString();
                            int i = text.lastIndexOf('\n');
                            if (i < 0){
                                column += text.length();
                            }
                            column += text.length()-i+1;
                            return column;
                        }
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
                        case LBRACE:
                            column += tokenSeq.token().length();
                            break;
                        default:
                            return column;
                    }
                }
            }
            return column;
        } finally {
            tokenSeq.moveIndex(aIndex);
            tokenSeq.moveNext();
        }
    }

    protected int getTokenIndent(TokenItem t){
        TokenSequence<TokenId> tokenSeq = t.getTokenSequence();
        int aIndex = go(t);
        try {
            int column = 0;
            while(tokenSeq.movePrevious()){
                TokenId tokenID = tokenSeq.token().id();
                if(tokenID instanceof CppTokenId) {
                    switch ((CppTokenId)tokenID) {
                        case NEW_LINE:
                        case PREPROCESSOR_DIRECTIVE:
                             return column;
                        case DOXYGEN_COMMENT:
                        case BLOCK_COMMENT:
                        {
                            String text = tokenSeq.token().text().toString();
                            int i = text.lastIndexOf('\n');
                            if (i < 0){
                                column = 0;
                                break;
                            }
                            column += text.length()-i+1;
                            return column;
                        }
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
                            column = 0;
                            break;
                    }
                }
            }
            return column;
        } finally {
            tokenSeq.moveIndex(aIndex);
            tokenSeq.moveNext();
        }
    }

    protected int getTokenColumn(TokenItem t){
        TokenSequence<TokenId> tokenSeq = t.getTokenSequence();
        int aIndex = go(t);
        try {
            int column = 0;
            while(tokenSeq.movePrevious()){
                TokenId tokenID = tokenSeq.token().id();
                if(tokenID instanceof CppTokenId) {
                    switch ((CppTokenId)tokenID) {
                        case NEW_LINE:
                        case PREPROCESSOR_DIRECTIVE:
                             return column;
                        case DOXYGEN_COMMENT:
                        case BLOCK_COMMENT:
                        {
                            String text = tokenSeq.token().text().toString();
                            int i = text.lastIndexOf('\n');
                            if (i < 0){
                                column += text.length();
                                break;
                            }
                            column += text.length()-i+1;
                            return column;
                        }
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
            }
            return column;
        } finally {
            tokenSeq.moveIndex(aIndex);
            tokenSeq.moveNext();
        }
    }
}
