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

package org.netbeans.editor.ext.java;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.editor.TokenID;
import java.util.Collections;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.util.Lookup;

/**
* Java indentation services are located here
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaFormatSupport extends ExtFormatSupport {

    private TokenContextPath tokenContextPath;

    public JavaFormatSupport(FormatWriter formatWriter) {
        this(formatWriter, JavaTokenContext.contextPath);
    }

    public JavaFormatSupport(FormatWriter formatWriter, TokenContextPath tokenContextPath) {
        super(formatWriter);
        this.tokenContextPath = tokenContextPath;
    }

    public TokenContextPath getTokenContextPath() {
        return tokenContextPath;
    }

    public boolean isComment(TokenItem token, int offset) {
        TokenID tokenID = token.getTokenID();
        return (token.getTokenContextPath() == tokenContextPath
                && (tokenID == JavaTokenContext.LINE_COMMENT
                    || tokenID == JavaTokenContext.BLOCK_COMMENT));
    }

    public boolean isMultiLineComment(TokenItem token) {
        return (token.getTokenID() == JavaTokenContext.BLOCK_COMMENT);
    }

    public boolean isMultiLineComment(FormatTokenPosition pos) {
        TokenItem token = pos.getToken();
        return (token == null) ? false : isMultiLineComment(token);
    }

    /** Check whether the given token is multi-line comment
     * that starts with slash and two stars.
     */
    public boolean isJavaDocComment(TokenItem token) {
        return isMultiLineComment(token)
            && token.getImage().startsWith("/**");
    }

    public TokenID getWhitespaceTokenID() {
        return JavaTokenContext.WHITESPACE;
    }

    public TokenContextPath getWhitespaceTokenContextPath() {
        return tokenContextPath;
    }

    public boolean canModifyWhitespace(TokenItem inToken) {
        if (inToken.getTokenContextPath() == JavaTokenContext.contextPath) {
            switch (inToken.getTokenID().getNumericID()) {
                case JavaTokenContext.BLOCK_COMMENT_ID:
                case JavaTokenContext.WHITESPACE_ID:
                    return true;
            }
        }

        return false;
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
     *  If the start of the statement is not found, null is retrurned.
     */
    public TokenItem findStatement(TokenItem token) {
        TokenItem lit = null; // last important token
        TokenItem t = getPreviousToken(token);

        while (t != null) {
            if (t.getTokenContextPath() == tokenContextPath) {

                switch (t.getTokenID().getNumericID()) {
                    case JavaTokenContext.SEMICOLON_ID:
                        if (!isForLoopSemicolon(t)) {
                            return (lit != null) ? lit : t;
                        }
                        break;

                    case JavaTokenContext.LBRACE_ID:
                    case JavaTokenContext.ELSE_ID:
                        return (lit != null) ? lit : t;
                        
                    case JavaTokenContext.RBRACE_ID:
                        // Check whether this is an array initialization block
                        if (!isArrayInitializationBraceBlock(t, null)) {
                            return (lit != null) ? lit : t;
                        } else { // skip the array initialization block
                            t = findMatchingToken(t, null, JavaTokenContext.LBRACE, true);
                        }
                        break;

                    case JavaTokenContext.COLON_ID:
                        TokenItem tt = findAnyToken(t, null, new TokenID[] {JavaTokenContext.CASE, JavaTokenContext.DEFAULT, JavaTokenContext.FOR, JavaTokenContext.QUESTION, JavaTokenContext.ASSERT}, t.getTokenContextPath(), true);
                        if (tt != null) {
                            switch (tt.getTokenID().getNumericID()) {
                                case JavaTokenContext.CASE_ID:
                                case JavaTokenContext.DEFAULT_ID:
                                case JavaTokenContext.FOR_ID:
                                    return (lit != null) ? lit : t;
                            }
                        }
                        break;

                    case JavaTokenContext.DO_ID:
                    case JavaTokenContext.SWITCH_ID:
                    case JavaTokenContext.CASE_ID:
                    case JavaTokenContext.DEFAULT_ID:
                        return t;

                    case JavaTokenContext.FOR_ID:
                    case JavaTokenContext.IF_ID:
                    case JavaTokenContext.WHILE_ID:
                        /* Try to find the statement after ( ... )
                         * If it exists, then the first important
                         * token after it is the stmt start. Otherwise
                         * it's this token.
                         */
                        if (lit != null && lit.getTokenID() == JavaTokenContext.LPAREN) {
                            // Find matching right paren in fwd dir
                            TokenItem mt = findMatchingToken(lit, token,
                                    JavaTokenContext.RPAREN, false);
                            if (mt != null && mt.getNext() != null) {
                                mt = findImportantToken(mt.getNext(), token, false);
                                if (mt != null) {
                                    return mt;
                                }
                            }
                        }

                        // No further stmt found, return this one
                        return t;

                }

                // Remember last important token
                if (isImportant(t, 0)) {
                    lit = t;
                }

            }

            t = t.getPrevious();
        }

        return lit;
    }


    /** Find the 'if' when the 'else' is provided.
     * @param elseToken the token with the 'else' command
     *  for which the 'if' is being searched.
     * @return corresponding 'if' token or null if there's
     *  no corresponding 'if' statement.
     */
    public TokenItem findIf(TokenItem elseToken) {
        if (elseToken == null || !tokenEquals(elseToken,
                    JavaTokenContext.ELSE, tokenContextPath)
        ) {
            throw new IllegalArgumentException("Only accept 'else'."); // NOI18N
        }

        int braceDepth = 0; // depth of the braces
        int elseDepth = 0; // depth of multiple else stmts
        while (true) {
            elseToken = findStatement(elseToken);
            if (elseToken == null) {
                return null;
            }
            
            switch (elseToken.getTokenID().getNumericID()) {
                case JavaTokenContext.LBRACE_ID:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    break;

                case JavaTokenContext.RBRACE_ID:
                    braceDepth++;
                    break;

                case JavaTokenContext.ELSE_ID:
                    if (braceDepth == 0) {
                        elseDepth++;
                    }
                    break;

                case JavaTokenContext.SEMICOLON_ID:
                case JavaTokenContext.COLON_ID:
                case JavaTokenContext.DO_ID:
                case JavaTokenContext.CASE_ID:
                case JavaTokenContext.DEFAULT_ID:
                case JavaTokenContext.FOR_ID:
                case JavaTokenContext.WHILE_ID:
                    break;

                case JavaTokenContext.IF_ID:
                    if (braceDepth == 0) {
                        if (elseDepth-- == 0) {
                            return elseToken; // successful search
                        }
                    }
                    break;
            }
        }
    }


    /** Find the 'switch' when the 'case' is provided.
     * @param caseToken the token with the 'case' command
     *  for which the 'switch' is being searched.
     * @return corresponding 'switch' token or null if there's
     *  no corresponding 'switch' statement.
     */
    public TokenItem findSwitch(TokenItem caseToken) {
        if (caseToken == null || 
             (!tokenEquals(caseToken, JavaTokenContext.CASE,
               tokenContextPath)
               && !tokenEquals(caseToken, JavaTokenContext.DEFAULT,
                    tokenContextPath))
        ) {
            throw new IllegalArgumentException("Only accept 'case' or 'default'."); // NOI18N
        }

        int braceDepth = 1; // depth of the braces - need one more left
        while (true) {
            caseToken = findStatement(caseToken);
            if (caseToken == null) {
                return null;
            }
            
            switch (caseToken.getTokenID().getNumericID()) {
                case JavaTokenContext.LBRACE_ID:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    break;

                case JavaTokenContext.RBRACE_ID:
                    braceDepth++;
                    break;

                case JavaTokenContext.SWITCH_ID:
                case JavaTokenContext.DEFAULT_ID:
                    if (braceDepth == 0) {
                        return caseToken;
                    }
                    break;
            }
        }
    }

    /** Find the 'try' when the 'catch' is provided.
     * @param catchToken the token with the 'catch' command
     *  for which the 'try' is being searched.
     * @return corresponding 'try' token or null if there's
     *  no corresponding 'try' statement.
     */
    public TokenItem findTry(TokenItem catchToken) {
        if (catchToken == null || 
             (!tokenEquals(catchToken, JavaTokenContext.CATCH,
               tokenContextPath))
        ) {
            throw new IllegalArgumentException("Only accept 'catch'."); // NOI18N
        }

        int braceDepth = 0; // depth of the braces
        while (true) {
            catchToken = findStatement(catchToken);
            if (catchToken == null) {
                return null;
            }
            
            switch (catchToken.getTokenID().getNumericID()) {
                case JavaTokenContext.LBRACE_ID:
                    if (--braceDepth < 0) {
                        return null; // no corresponding right brace
                    }
                    break;

                case JavaTokenContext.RBRACE_ID:
                    braceDepth++;
                    break;

                case JavaTokenContext.TRY_ID:
                    if (braceDepth == 0) {
                        return catchToken;
                    }
                    break;
            }
        }
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
    public TokenItem findStatementStart(TokenItem token) {
        return findStatementStart(token, true);
    }
    
    public TokenItem findStatementStart(TokenItem token, boolean outermost) {
        TokenItem t = findStatement(token);
        if (t != null) {
            switch (t.getTokenID().getNumericID()) {
                case JavaTokenContext.SEMICOLON_ID: // ';' found
                    TokenItem scss = findStatement(t);
                    
                    // fix for issue 14274
                    if (scss == null)
                        return token;
                    
                    switch (scss.getTokenID().getNumericID()) {
                        case JavaTokenContext.LBRACE_ID: // '{' then ';'
                        case JavaTokenContext.RBRACE_ID: // '}' then ';'
                        case JavaTokenContext.COLON_ID: // ':' then ';'
                        case JavaTokenContext.CASE_ID: // 'case' then ';'
                        case JavaTokenContext.DEFAULT_ID:
                        case JavaTokenContext.SEMICOLON_ID: // ';' then ';'
                            return t; // return ';'

                        case JavaTokenContext.DO_ID:
                        case JavaTokenContext.FOR_ID:
                        case JavaTokenContext.IF_ID:
                        case JavaTokenContext.WHILE_ID:
                        case JavaTokenContext.SYNCHRONIZED_ID:
                            return findStatementStart(t, outermost);

                        case JavaTokenContext.ELSE_ID: // 'else' then ';'
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
                                switch (bscss.getTokenID().getNumericID()) {
                                    case JavaTokenContext.SEMICOLON_ID: // ';' then stmt ending with ';'
                                    case JavaTokenContext.LBRACE_ID:
                                    case JavaTokenContext.RBRACE_ID:
                                    case JavaTokenContext.COLON_ID:
                                        return scss; // 

                                    case JavaTokenContext.DO_ID:
                                    case JavaTokenContext.FOR_ID:
                                    case JavaTokenContext.IF_ID:
                                    case JavaTokenContext.WHILE_ID:
                                    case JavaTokenContext.SYNCHRONIZED_ID:
                                        return findStatementStart(bscss, outermost);

                                    case JavaTokenContext.ELSE_ID:
                                        // Find the corresponding 'if'
                                        ifss = findIf(bscss);
                                        if (ifss != null) { // 'if' ... 'else' ... ';'
                                            return findStatementStart(ifss, outermost);

                                        } else { // no valid starting 'if'
                                            return bscss; // return 'else'
                                        }
                                }
                            }

                            return scss;
                    } // semicolon servicing end

                case JavaTokenContext.LBRACE_ID: // '{' found
                    return token; // return original token

                case JavaTokenContext.RBRACE_ID: // '}' found
                    TokenItem lb = findMatchingToken(t, null,
                            JavaTokenContext.LBRACE, true);
                    if (lb != null) { // valid matching left-brace
                        // Find a stmt-start of the '{'
                        TokenItem lbss = findStatement(lb);
                        if (lbss != null) {
                            switch (lbss.getTokenID().getNumericID()) {
                                case JavaTokenContext.ELSE_ID: // 'else {'
                                    // Find the corresponding 'if'
                                    TokenItem ifss = findIf(lbss);
                                    if (ifss != null) { // valid 'if'
                                        return findStatementStart(ifss, outermost);
                                    } else {
                                        return lbss; // return 'else'
                                    }

                                case JavaTokenContext.CATCH_ID: // 'catch (...) {'
                                    // Find the corresponding 'try'
                                    TokenItem tryss = findTry(lbss);
                                    if (tryss != null) { // valid 'try'
                                        return findStatementStart(tryss, outermost);
                                    } else {
                                        return lbss; // return 'catch'
                                    }
                                    
                                case JavaTokenContext.DO_ID:
                                case JavaTokenContext.FOR_ID:
                                case JavaTokenContext.IF_ID:
                                case JavaTokenContext.WHILE_ID:
                                case JavaTokenContext.SYNCHRONIZED_ID:
                                    return findStatementStart(lbss, outermost);

                            }
                            
                            // another hack to prevent problem described in issue 17033
                            if (lbss.getTokenID().getNumericID() == JavaTokenContext.LBRACE_ID) {
                                return t; // return right brace
                            }
                            
                            return lbss;
                        }

                    }
                    return t; // return right brace

                case JavaTokenContext.COLON_ID:
                case JavaTokenContext.CASE_ID:
                case JavaTokenContext.DEFAULT_ID:
                    return token;

                case JavaTokenContext.ELSE_ID:
                    // Find the corresponding 'if'
                    TokenItem ifss = findIf(t);
                    return (ifss != null) ? findStatementStart(ifss, outermost) : t;

                case JavaTokenContext.DO_ID:
                case JavaTokenContext.FOR_ID:
                case JavaTokenContext.IF_ID:
                case JavaTokenContext.WHILE_ID:
                case JavaTokenContext.SYNCHRONIZED_ID:
                    if (!outermost) {
                        return t;
                    } else {
                        return findStatementStart(t, outermost);
                    }
                    
                case JavaTokenContext.IDENTIFIER_ID:
                    return t;
                default:
                    return t;
            }
        }

        return token; // return original token
    }

    /** Get the indentation for the given token.
     * It first searches whether there's an non-whitespace and a non-leftbrace
     * character on the line with the token and if so,
     * it takes indent of the non-ws char instead.
     * @param token token for which the indent is being searched.
     *  The token itself is ignored and the previous token
     *  is used as a base for the search.
     * @param forceFirstNonWhitespace set true to ignore leftbrace and search 
     * directly for first non-whitespace
     */
    public int getTokenIndent(TokenItem token, boolean forceFirstNonWhitespace) {
        FormatTokenPosition tp = getPosition(token, 0);
        // this is fix for bugs: 7980 and 9111
        // see the findLineFirstNonWhitespaceAndNonLeftBrace definition
        // for more info about the fix
        FormatTokenPosition fnw;
        if (forceFirstNonWhitespace)
            fnw = findLineFirstNonWhitespace(tp);
        else
            fnw = findLineFirstNonWhitespaceAndNonLeftBrace(tp);
        
        if (fnw != null) { // valid first non-whitespace
            tp = fnw;
        }
        return getVisualColumnOffset(tp);
    }

    public int getTokenIndent(TokenItem token) {
        return getTokenIndent(token, false);
    }   
    
    /** Find the indentation for the first token on the line.
     * The given token is also examined in some cases.
     */
    public int findIndent(TokenItem token) {
        int indent = -1; // assign invalid indent

        // First check the given token
        if (token != null) {
            switch (token.getTokenID().getNumericID()) {
                case JavaTokenContext.ELSE_ID:
                    TokenItem ifss = findIf(token);
                    if (ifss != null) {
                        indent = getTokenIndent(ifss);
                    }
                    break;

                case JavaTokenContext.LBRACE_ID:
                    TokenItem stmt = findStatement(token);
                    if (stmt == null) {
                        indent = 0;

                    } else {
                        switch (stmt.getTokenID().getNumericID()) {
                            case JavaTokenContext.DO_ID:
                            case JavaTokenContext.FOR_ID:
                            case JavaTokenContext.IF_ID:
                            case JavaTokenContext.WHILE_ID:
                            case JavaTokenContext.ELSE_ID:
                                indent = getTokenIndent(stmt);
                                break;
                                
                            case JavaTokenContext.LBRACE_ID:
                                indent = getTokenIndent(stmt) + getShiftWidth();
                                break;
                                
                            default:
                                stmt = findStatementStart(token);
                                if (stmt == null) {
                                    indent = 0;

                                } else if (stmt == token) { 
                                    stmt = findStatement(token); // search for delimiter
                                    indent = (stmt != null) ? indent = getTokenIndent(stmt) : 0;

                                } else { // valid statement
                                    indent = getTokenIndent(stmt);
                                    switch (stmt.getTokenID().getNumericID()) {
                                        case JavaTokenContext.LBRACE_ID:
                                            indent += getShiftWidth();
                                            break;
                                    }
                                }
                        }
                    }
                    break;

                case JavaTokenContext.RBRACE_ID:
                    TokenItem rbmt = findMatchingToken(token, null,
                                JavaTokenContext.LBRACE, true);
                    if (rbmt != null) { // valid matching left-brace
                        TokenItem t = findStatement(rbmt);
                        boolean forceFirstNonWhitespace = false;
                        if (t == null) {
                            t = rbmt; // will get indent of the matching brace

                        } else {
                            switch (t.getTokenID().getNumericID()) {
                                case JavaTokenContext.SEMICOLON_ID:
                                case JavaTokenContext.LBRACE_ID:
                                case JavaTokenContext.RBRACE_ID:
                                {
                                    t = rbmt;
                                    forceFirstNonWhitespace = true;
                                }
                            }
                        }
                        // the right brace must be indented to the first 
                        // non-whitespace char - forceFirstNonWhitespace=true
                        indent = getTokenIndent(t, forceFirstNonWhitespace);

                    } else { // no matching left brace
                        indent = getTokenIndent(token); // leave as is
                    }
                    break;

                case JavaTokenContext.CASE_ID:
                case JavaTokenContext.DEFAULT_ID:
                    TokenItem swss = findSwitch(token);
                    if (swss != null) {
                        indent = getFormatOptionBoolean("indentCasesFromSwitch", true) ? (getTokenIndent(swss) + getShiftWidth()) : getTokenIndent(swss);
                    }
                    break;

            }
        }

        // If indent not found, search back for the first important token
        if (indent < 0) { // if not yet resolved
            TokenItem t = findImportantToken(token, null, true);
            if (t != null) { // valid important token
                if (t.getTokenContextPath() != tokenContextPath) {
                    // For non-java tokens such as jsp return indent
                    // of last non-java line
                    return getTokenIndent(t);
                }
                
                switch (t.getTokenID().getNumericID()) {
                    case JavaTokenContext.SEMICOLON_ID: // semicolon found
                        TokenItem tt = findStatementStart(token);
                        indent = getTokenIndent(tt);
                            
                        break;

                    case JavaTokenContext.LBRACE_ID:
                        TokenItem lbss = findStatementStart(t, false);
                        if (lbss == null) {
                            lbss = t;
                        }
                        indent = getTokenIndent(lbss) + getShiftWidth();
                        break;

                    case JavaTokenContext.RBRACE_ID:
                        if (true) {
                            TokenItem t3 = findStatementStart(token);
                            indent = getTokenIndent(t3);
                            break;
                        }

                        /** Check whether the following situation occurs:
                         *  if (t1)
                         *    if (t2) {
                         *      ...
                         *    }
                         * 
                         *  In this case the indentation must be shifted
                         *  one level back.
                         */
                        TokenItem rbmt = findMatchingToken(t, null,
                                JavaTokenContext.LBRACE, true);
                        if (rbmt != null) { // valid matching left-brace
                            // Check whether there's a indent stmt
                            TokenItem t6 = findStatement(rbmt);
                            if (t6 != null) {
                                switch (t6.getTokenID().getNumericID()) {
                                    case JavaTokenContext.ELSE_ID:
                                        /* Check the following situation:
                                         * if (t1)
                                         *   if (t2)
                                         *     c1();
                                         *   else {
                                         *     c2();
                                         *   }
                                         */

                                        // Find the corresponding 'if'
                                        t6 = findIf(t6);
                                        if (t6 != null) { // valid 'if'
                                            TokenItem t7 = findStatement(t6);
                                            if (t7 != null) {
                                                switch (t7.getTokenID().getNumericID()) {
                                                    case JavaTokenContext.DO_ID:
                                                    case JavaTokenContext.FOR_ID:
                                                    case JavaTokenContext.IF_ID:
                                                    case JavaTokenContext.WHILE_ID:
                                                        indent = getTokenIndent(t7);
                                                        break;

                                                    case JavaTokenContext.ELSE_ID:
                                                        indent = getTokenIndent(findStatementStart(t6));
                                                }
                                            }
                                        }
                                        break;

                                    case JavaTokenContext.DO_ID:
                                    case JavaTokenContext.FOR_ID:
                                    case JavaTokenContext.IF_ID:
                                    case JavaTokenContext.WHILE_ID:
                                        /* Check the following:
                                         * if (t1)
                                         *   if (t2) {
                                         *     c1();
                                         *   }
                                         */
                                        TokenItem t7 = findStatement(t6);
                                        if (t7 != null) {
                                            switch (t7.getTokenID().getNumericID()) {
                                                case JavaTokenContext.DO_ID:
                                                case JavaTokenContext.FOR_ID:
                                                case JavaTokenContext.IF_ID:
                                                case JavaTokenContext.WHILE_ID:
                                                    indent = getTokenIndent(t7);
                                                    break;

                                                case JavaTokenContext.ELSE_ID:
                                                    indent = getTokenIndent(findStatementStart(t6));

                                            }
                                        }
                                        break;

                                    case JavaTokenContext.LBRACE_ID: // '{' ... '{'
                                        indent = getTokenIndent(rbmt);
                                        break;

                                }

                            }

                            if (indent < 0) {
                                indent = getTokenIndent(t); // indent of original rbrace
                            }

                        } else { // no matching left-brace
                            indent = getTokenIndent(t); // return indent of '}'
                        }
                        break;

                    case JavaTokenContext.COLON_ID:
                        TokenItem ttt = findAnyToken(t, null, new TokenID[] {JavaTokenContext.CASE, JavaTokenContext.DEFAULT, JavaTokenContext.FOR, JavaTokenContext.QUESTION, JavaTokenContext.ASSERT}, t.getTokenContextPath(), true);
                        if (ttt != null && ttt.getTokenID().getNumericID() == JavaTokenContext.QUESTION_ID) {
                            indent = getTokenIndent(ttt) + getShiftWidth();
                        } else {
                            // Indent of line with ':' plus one indent level
                            indent = getTokenIndent(t) + getShiftWidth();
                        }
                        break;

                    case JavaTokenContext.QUESTION_ID:
                    case JavaTokenContext.DO_ID:
                    case JavaTokenContext.ELSE_ID:
                        indent = getTokenIndent(t) + getShiftWidth();
                        break;

                    case JavaTokenContext.RPAREN_ID:
                        // Try to find the matching left paren
                        TokenItem rpmt = findMatchingToken(t, null, JavaTokenContext.LPAREN, true);
                        if (rpmt != null) {
                            rpmt = findImportantToken(rpmt, null, true);
                            // Check whether there are the indent changing kwds
                            if (rpmt != null && rpmt.getTokenContextPath() == tokenContextPath) {
                                switch (rpmt.getTokenID().getNumericID()) {
                                    case JavaTokenContext.FOR_ID:
                                    case JavaTokenContext.IF_ID:
                                    case JavaTokenContext.WHILE_ID:
                                        // Indent one level
                                        indent = getTokenIndent(rpmt) + getShiftWidth();
                                        break;
                                }
                            }
                        }
                        if (indent < 0) {
                            indent = computeStatementIndent(t);
                        }
                        break;
                        
                    case JavaTokenContext.COMMA_ID:
                        if (isEnumComma(t)) {
                            indent = getTokenIndent(t);
                            break;
                        } // else continue to default

                    default: {
                        indent = computeStatementIndent(t);
                        break;
                    }
                }

                if (indent < 0) { // no indent found yet
                    indent = getTokenIndent(t);
                }
            }
        }
        
        if (indent < 0) { // no important token found
            indent = 0;
        }

        return indent;
    }

    private int computeStatementIndent(final TokenItem t) {
        int indent;
        // Find stmt start and add continuation indent
        TokenItem stmtStart = findStatementStart(t);
        indent = getTokenIndent(stmtStart);
        int tindent = getTokenIndent(t);
        if (tindent > indent)
            return tindent;
        
        if (stmtStart != null) {
            // Check whether there is a comma on the previous line end
            // and if so then also check whether the present
            // statement is inside array initialization statement
            // and not inside parents and if so then do not indent
            // statement continuation
            if (t != null && tokenEquals(t, JavaTokenContext.COMMA, tokenContextPath)) {
                if (isArrayInitializationBraceBlock(t, null) && !isInsideParens(t, stmtStart)) {
                    // Eliminate the later effect of statement continuation shifting
                    indent -= getFormatStatementContinuationIndent();
                }
            }
            // Check whether there is an annotation '@' on the previous line begining
            // and if so then do not add the continuation indent
            if (t != null) {
                FormatTokenPosition pos = findLineFirstNonWhitespace(getPosition(t, 0));
                if (pos != null) {
                    TokenItem maybeAnno = pos.getToken();
                    if (maybeAnno != null && maybeAnno.getTokenID() == JavaTokenContext.ANNOTATION) {
                        indent -= getFormatStatementContinuationIndent();
                    }
                }
            }
            indent += getFormatStatementContinuationIndent();
        }
        return indent;
    }

    public FormatTokenPosition indentLine(FormatTokenPosition pos) {
        int indent = 0; // Desired indent

        // Get the first non-whitespace position on the line
        FormatTokenPosition firstNWS = findLineFirstNonWhitespace(pos);
        if (firstNWS != null) { // some non-WS on the line
            if (isComment(firstNWS)) { // comment is first on the line
                if (isMultiLineComment(firstNWS) && firstNWS.getOffset() != 0) {

                    // Indent the inner lines of the multi-line comment by one
                    indent = getLineIndent(getPosition(firstNWS.getToken(), 0), true) + 1;

                    // If the line is inside multi-line comment and doesn't contain '*'
                    if (!isIndentOnly()) {
                        if (getChar(firstNWS) != '*') {
                            if (isJavaDocComment(firstNWS.getToken())) {
                                if (getFormatLeadingStarInComment()) {
                                    // For java-doc it should be OK to add the star
                                    insertString(firstNWS, "* "); // NOI18N
                                }

                            } else {
                                // For non-java-doc not because it can be commented code
                                indent = getLineIndent(pos, true);
                            }
                        } else {
                            if (isJavaDocComment(firstNWS.getToken())) {
                                if (!getFormatLeadingStarInComment()) {
                                    // For java-doc it should be OK to remove the star
                                    int len = -1;
                                    if (firstNWS.getOffset() + 1 < firstNWS.getToken().getImage().length()) {
                                        FormatTokenPosition nextCharPos = getPosition(firstNWS.getToken(), firstNWS.getOffset() + 1);
                                        char nextChar = getChar(nextCharPos);
                                        if (nextChar != '/') {
                                            len = getChar(nextCharPos) == ' ' ? 2 : 1;
                                        }
                                    } else {
                                        len = 1;
                                    }
                                    
                                    if (len != -1) {
                                        remove(firstNWS, len);
                                    }
                                }
                            }
                        }

                    } else { // in indent mode (not formatting)
                        if (getChar(firstNWS) != '*') { // e.g. not for '*/'
                            if (isJavaDocComment(firstNWS.getToken())) {
                                if (getFormatLeadingStarInComment()) {
                                    insertString(firstNWS, "* "); // NOI18N
                                    setIndentShift(2);
                                }
                            }
                        }
                    }

                } else if (!isMultiLineComment(firstNWS)) { // line-comment
                    indent = findIndent(firstNWS.getToken());
                } else { // multi-line comment
                    if (isJavaDocComment(firstNWS.getToken()))
                    {
                        indent = findIndent(firstNWS.getToken());
                    }
                    else
                    {
                        // check whether the multiline comment isn't finished on the same line (see issue 12821)
                        if (firstNWS.getToken().getImage().indexOf('\n') == -1)
                            indent = findIndent(firstNWS.getToken());
                        else
                        indent = getLineIndent(firstNWS, true);
                    }
                }

            } else { // first non-WS char is not comment
                indent = findIndent(firstNWS.getToken());
            }

        } else { // whole line is WS
            // Can be empty line inside multi-line comment
            TokenItem token = pos.getToken();
            if (token == null) {
                token = findLineStart(pos).getToken();
                if (token == null) { // empty line
                    token = getLastToken();
                }
            }

            if (token != null && isMultiLineComment(token)) {
                if (getFormatLeadingStarInComment()
                    && (isIndentOnly() || isJavaDocComment(token))
                ) {
                    // Insert initial '* '
                    insertString(pos, "* "); // NOI18N
                    setIndentShift(2);
                }

                // Indent the multi-comment by one more space
                indent = getVisualColumnOffset(getPosition(token, 0)) + 1;

            } else { // non-multi-line comment
                indent = findIndent(pos.getToken());
            }
        }

        // For indent-only always indent
        return changeLineIndent(pos, indent);
    }

    public String getIndentString(int indent) {
        return IndentUtils.createIndentString(getFormatWriter().getDocument(), indent);
    }
    
    /** Check whether the given semicolon is inside
     * the for() statement.
     * @param token token to check. It must be a semicolon.
     * @return true if the given semicolon is inside
     *  the for() statement, or false otherwise.
     */
    public boolean isForLoopSemicolon(TokenItem token) {
        if (token == null || !tokenEquals(token,
                    JavaTokenContext.SEMICOLON, tokenContextPath)
        ) {
            throw new IllegalArgumentException("Only accept ';'."); // NOI18N
        }

        int parDepth = 0; // parenthesis depth
        int braceDepth = 0; // brace depth
        boolean semicolonFound = false; // next semicolon
        token = token.getPrevious(); // ignore this semicolon
        while (token != null) {
            if (tokenEquals(token, JavaTokenContext.LPAREN, tokenContextPath)) {
                if (parDepth == 0) { // could be a 'for ('
                    FormatTokenPosition tp = getPosition(token, 0);
                    tp = findImportant(tp, null, false, true);
                    if (tp != null && tokenEquals(tp.getToken(),
                            JavaTokenContext.FOR, tokenContextPath)
                    ) {
                        return true;
                    }
                    return false;

                } else { // non-zero depth
                    parDepth--;
                }

            } else if (tokenEquals(token, JavaTokenContext.RPAREN, tokenContextPath)) {
                parDepth++;

            } else if (tokenEquals(token, JavaTokenContext.LBRACE, tokenContextPath)) {
                if (braceDepth == 0) { // unclosed left brace
                    return false;
                }
                braceDepth--;

            } else if (tokenEquals(token, JavaTokenContext.RBRACE, tokenContextPath)) {
                braceDepth++;

            } else if (tokenEquals(token, JavaTokenContext.SEMICOLON, tokenContextPath)) {
                if (semicolonFound) { // one semicolon already found
                    return false;
                }
                semicolonFound = true;
            }

            token = token.getPrevious();
        }

        return false;
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
    private boolean isInsideParens(TokenItem token, TokenItem limitToken) {
        int depth = 0;
        token = token.getPrevious();

        while (token != null && token != limitToken) {
            if (tokenEquals(token, JavaTokenContext.LPAREN, tokenContextPath)) {
                if (--depth < 0) {
                    return true;
                }

            } else if (tokenEquals(token, JavaTokenContext.RPAREN, tokenContextPath)) {
                depth++;
            }
            token = token.getPrevious();
        }
        return false;
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
    private boolean isArrayInitializationBraceBlock(TokenItem token, TokenItem limitToken) {
        int depth = 0;
        token = token.getPrevious();

        while (token != null && token != limitToken && token.getTokenContextPath() == tokenContextPath) {
            switch (token.getTokenID().getNumericID()) {
                case JavaTokenContext.RBRACE_ID:
                    depth++;
                    break;

                case JavaTokenContext.LBRACE_ID:
                    depth--;
                    if (depth < 0) {
                        TokenItem prev = findImportantToken(token, limitToken, true);
                        // Array initialization left brace should be preceded
                        // by either '=' or ']' i.e.
                        // either "String array = { "a", "b", ... }"
                        // or     "String array = new String[] { "a", "b", ... }"
                        return (prev != null && prev.getTokenContextPath() == tokenContextPath
                                && (JavaTokenContext.RBRACKET.equals(prev.getTokenID())
                                 || JavaTokenContext.EQ.equals(prev.getTokenID())));
                    }
                    break;

                // Array initialization block should not contain statements or ';'
                case JavaTokenContext.DO_ID:
                case JavaTokenContext.FOR_ID:
                case JavaTokenContext.IF_ID:
                case JavaTokenContext.WHILE_ID:
                case JavaTokenContext.SEMICOLON_ID:
                    if (depth == 0) {
                        return false;
                    }
            }                    
            token = token.getPrevious();
        }
        return false;
    }

    public boolean isEnumComma(TokenItem token) {
        while (token != null && tokenEquals(token, JavaTokenContext.COMMA, tokenContextPath)) {
            TokenItem itm = findStatementStart(token);
            if (itm == token)
                break;
            token = itm;
        }
        if (token != null && tokenEquals(token, JavaTokenContext.IDENTIFIER, tokenContextPath)) {
            TokenItem itm = findImportantToken(token, null, true);
            if (itm != null && tokenEquals(itm, JavaTokenContext.LBRACE, tokenContextPath)) {
                TokenItem startItem = findStatementStart(itm);
                if (startItem != null && findToken(startItem, itm, JavaTokenContext.ENUM, tokenContextPath, null, false) != null)
                    return true;
            }
        }
        return false;
    }

    public boolean getFormatSpaceBeforeParenthesis() {
        return getFormatOptionBoolean("spaceBeforeMethodDeclParen", false); //NOI18N
    }

    public boolean getFormatSpaceAfterComma() {
        return getFormatOptionBoolean("spaceAfterComma", true); //NOI18N
    }

    public boolean getFormatNewlineBeforeBrace() {
        Preferences p = getFormatOptions();
        String s = p == null ? null : p.get("methodDeclBracePlacement", null); //NOI18N
        if (s != null && s.equals("NEW_LINE")) { //NOI18N
            return true;
        } else {
            return false;
        }
    }

    public boolean getFormatLeadingSpaceInComment() {
        // XXX: add this to FmtOptions
        return false;
    }

    public boolean getFormatLeadingStarInComment() {
        return getFormatOptionBoolean("addLeadingStarInComment", true); //NOI18N
    }

    private int getFormatStatementContinuationIndent() {
        return getFormatOptionInt("continuationIndentSize", 8); //NOI18N
    }
    
    private boolean getFormatOptionBoolean(String optionName, boolean def) {
        Preferences p = getFormatOptions();
        if (p == null) {
            return def;
        } else {
            return p.getBoolean(optionName, def);
        }
    }
    
    private int getFormatOptionInt(String optionName, int def) {
        Preferences p = getFormatOptions();
        if (p == null) {
            return def;
        } else {
            return p.getInt(optionName, def);
        }
    }

    private Preferences getFormatOptions() {
        Lookup l = MimeLookup.getLookup("text/x-java"); //NOI18N
        return (Preferences)l.lookup(Preferences.class);
    }

    /*   this is fix for bugs: 7980 and 9111. if user enters
     *        {   foo();
     *   and press enter at the end of the line, she wants
     *   to be indented just under "f" in "foo();" and not under the "{" 
     *   as it happens now. and this is what findLineFirstNonWhitespaceAndNonLeftBrace checks
     */    
    public FormatTokenPosition findLineFirstNonWhitespaceAndNonLeftBrace(FormatTokenPosition pos) {
        // first call the findLineFirstNonWhitespace
        FormatTokenPosition ftp = super.findLineFirstNonWhitespace(pos);
        if (ftp == null) { // no line start, no WS
            return null;
        }

        // now checks if the first non-whitespace char is "{"
        // if it is, find the next non-whitespace char
        if (!ftp.getToken().getImage().startsWith("{")) // NOI18N
            return ftp;

        // if the left brace is closed on the same line - "{ foo(); }"
        // it must be ignored. otherwise next statement is incorrectly indented 
        // under the "f" and not under the "{" as expected
        FormatTokenPosition eolp = findNextEOL(ftp);
        TokenItem rbmt = findMatchingToken(ftp.getToken(), 
            eolp != null ? eolp.getToken() : null, JavaTokenContext.RBRACE, false);
        if (rbmt != null)
            return ftp;
        
        FormatTokenPosition ftp_next = getNextPosition(ftp);
        if (ftp_next == null)
            return ftp;
        
        FormatTokenPosition ftp2 = findImportant(ftp_next, null, true, false);
        if (ftp2 != null)
            return ftp2;
        else
            return ftp;
    }

}
