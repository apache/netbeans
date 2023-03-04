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

package org.netbeans.modules.lexer.demo.javacc;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.AbstractLexer;
import org.netbeans.spi.lexer.javacc.LexerInputCharStream;
import org.netbeans.spi.lexer.javacc.TokenMgrError;
import org.netbeans.spi.lexer.util.IntegerCache;

/**
 * Wrapper around javacc's generated token manager.
 * <BR>Please read <A href="http://lexer.netbeans.org/doc/javacc.html">
 * to get additional information related to this source.
 * 
 * <P>Most of the tokens
 * returned from the token manager are just accepted and passed on 
 * but several token types are created by assembling several tokens into one
 * extended token.
 * <br>For example block comment is assembled by first recognizing initial
 * slash-star as a tokenmanager's token and then recognizing the rest of the comment.
 * <BR>Error tokens are recognized as single characters by tokenmanagers
 * and assembled together by CalcLexer so that they form just one extended
 * token rather than many single-char successive error tokens.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class CalcLexer extends AbstractLexer {

    private static final CalcLanguage language = CalcLanguage.get();
    
    private static final Integer[] integerStates = IntegerCache.getTable(CalcLanguage.MAX_STATE);
    
    private CalcTokenManager tokenManager;
    
    private LexerInputCharStream charStream;

    private LexerInput lexerInput;
    
    public CalcLexer() {
        this.charStream = new LexerInputCharStream();
        this.tokenManager = new CalcTokenManager(charStream);
    }
    
    protected Object getLexerState() {
        int s = tokenManager.curLexState;
        // Default state is returned as null others like Integer instances
        return (s == tokenManager.defaultLexState)
            ? null
            : integerStates[s];
            
        /* BTW in this particular case (with this Calc grammar
         * and this CalcLexer) there could be just
         *     return null;
         * Although there are some extra tokenmanager's states
         * (e.g. when tokenmanager recognizes slash-star
         * in the block-comment and goes into non-default internal state)
         * all those subtokens (after which
         * the tokenmanager goes into non-deafult state)
         * are immediately merged with the subtoken(s)
         * that follow them and after these subtokens the tokenmanager
         * is always in default state again. As the lexer framework
         * only asks the lexer for its state at token boundaries
         * (not on tokenmanager's subtoken boundaries) it would be fine
         * to return null.
         */
    }

    public void restart(LexerInput input, Object state) {
        super.restart(input, state);

        this.lexerInput = input;
        /* It's necessary to update the lexerInput
         * in the charStream that the tokenManager uses.
         * The LexerInputCharStream is a wrapper
         * around lexerInput to look like CharStream.
         */
        charStream.setLexerInput(lexerInput);

        // Reinit the tokenManager so that it acts like a fresh instance
        tokenManager.ReInit(charStream,
            (state != null) // see getLexerState() for info about which states can be returned
                ? ((Integer)state).intValue() // non-default state
                : tokenManager.defaultLexState // default state
        );
    }
    
    protected final LexerInput getLexerInput() { // this method is necessary for AbstractLexer
        return lexerInput;
    }
    
    protected final Language getLanguage() { // this method is necessary for AbstractLexer
        return language;
    }


    /**
     * Fetch next token from underlying javacc tokenmanager.
     * <BR>The intId of the token that was found can be set
     * into the given tokenData parameter
     * by <CODE>TokenData.setTokenIntId()</CODE> in case there was
     * a valid token found.
     * <P>Token length of the fetched token can be set into tokenData
     * by <CODE>TokenData.setTokenLength()</CODE>.
     * If the token intId or length is not assigned in <CODE>fetchToken()</CODE>
     * it must be assigned later during either
     * {@link #ordinaryToken(OrdinaryTokenData)}
     * or {@link #extendedToken(ExtendedTokenData)} depending
     * which of these two gets called.
     * @param tokenData mutable info about the token being fetched.
     * @return true if a valid token was found or false
     *  if there are no more tokens on the input (in which case a call
     *  to <CODE>TokenData.setTokenIntId()</CODE> is not necessary).
     */
    protected boolean fetchToken(TokenData tokenData) {
        try {
            // Get javacc token from tokenmanager
            org.netbeans.spi.lexer.javacc.Token javaccToken = tokenManager.getNextToken();
            if (javaccToken != null) {
                int tokenKind = javaccToken.kind;
                tokenData.setTokenIntId(tokenKind);
                tokenData.setTokenLength(tokenData.getDefaultTokenLength());
                return (tokenKind != CalcConstants.EOF); // EOF presents no characters
                
            } else { // javaccToken is null
                return false;  // no more tokens from tokenManager
            }
                
        } catch (TokenMgrError e) {
            if (e.getErrorCode() == TokenMgrError.LEXICAL_ERROR) {
                if (tokenData.inExtendedToken()) {
                    switch (tokenData.getExtendedTokenIntId()) {
                        case CalcConstants.INCOMPLETE_ML_COMMENT:
                            // This should only happen at the end of input
                            tokenData.setTokenIntId(CalcConstants.EOF);
                            // Lookahead will be non-zero (for no chars the lexical
                            // error would not be thrown)
                            tokenData.setTokenLength(tokenData.getTextLookahead());
                            return true; // there are chars -> valid token exists
                            
                    }
                }
                
                // Fallback for other ERRORS
//                System.out.println("msg=" + e.getMessage());
                throw new IllegalStateException("Internal lexer error");
                
            } else { // non-lexical type of error
                throw e;
            }
        }
    }


    /**
     * Called after a token was successfully fetched
     * by {@link #fetchToken(TokenData)} to possibly
     * start an extended token mode
     * by {@link OrdinaryTokenData#startExtendedToken()}
     * <P>When extended token mode is started
     * the {@link #extendedToken(ExtendedTokenData, boolean)}
     * is called after each future {@link #fetchToken(TokenData) instead
     * of <CODE>ordinaryToken()</CODE> (that would be called
     * in non-extended mode by default).
     * @param tokenData mutable info holding information
     *  about previously fetched token.
     * @see OrdinaryTokenData
     */
    protected void ordinaryToken(OrdinaryTokenData tokenData) {

        /*
         * Start extended tokens for errors
         * and multi-line-comments.
         */
        int tokenIntId = tokenData.getTokenIntId();
        switch (tokenIntId) { // check for types that start extended token
            case CalcConstants.ERROR:
                /* All errors which are recognized as single chars
                 * by tokenManager will be concatenated together.
                 */
                tokenData.startExtendedToken();
                break;

            case CalcConstants.INCOMPLETE_ML_COMMENT: // "/*" was found by tokenManager
                /* Multi-line-comment token is recognized by first matching "/*"
                 * by tokenManager. TokenManager then goes into an extra state
                 * in which it recognizes all the chars up to star-slash including.
                 * The recognized token forms the rest of the multi-line-comment
                 * token then. Both tokens from tokenManager are concatenated
                 * into a single extended token and returned from nextToken()
                 * implementation in <CODE>AbstractLexer</CODE>.
                 * Here the extended token is started. The rest of the matching
                 * is in extendedToken().
                 * Here it's possible that the tokenManager throws
                 * lexical error if it finds end-of-input before
                 * matching the closing star-slash.
                 */
                tokenData.startExtendedToken();
                break;
        }
        
    }
    
    /**
     * Called in extended token mode after a token was successfully fetched
     * by {@link #fetchToken(TokenData)} to possibly update
     * the extended token identification or finish
     * the extended token being put together.
     *
     * <P>Please note that the <CODE>extendedToken()</CODE> is not called
     * after extended token mode gets started
     * by <CODE>OrdinaryTokenData.startExtendedToken()</CODE>
     * in <CODE>ordinaryToken()</CODE> until another <CODE>fetchToken()</CODE>
     * is done. The sequence is:<pre>
     *   fetchToken()
     *   ordinaryToken() -> possibly startExtendedToken()
     *   fetchToken()
     *   extendedToken()
     *   fetchToken()
     *   extendedToken()
     *   fetchToken()
     *   extendedToken() -> possibly finishExtendedToken(true)
     *   fetchToken()
     *   ordinaryToken()
     *   fetchToken()
     *   ordinaryToken()
     *   ...
     * </pre>
     *
     * @param tokenData mutable compound info about the token
     *  that was previously fetched and about the extended token
     *  that is being put together.
     * @param fetchedTokenExists true if the last fetched token
     *  was valid i.e. the <CODE>fetchToken()</CODE> returned true.
     *  False if there are no more tokens to fetch from the input.
     *  <BR>If the parameter is false then this method
     *  must mandatorily finish the extended token 
     *  by calling <CODE>finishExtendedToken()</CODE>.
     * @see ExtendedTokenData
     */
    protected void extendedToken(ExtendedTokenData tokenData,
    boolean fetchedTokenExists) {
        
        int extendedTokenIntId = tokenData.getExtendedTokenIntId();
        int tokenIntId = tokenData.getTokenIntId(); // fetched token id

        switch (extendedTokenIntId) {
            case CalcConstants.ERROR:
                if (!fetchedTokenExists
                    || tokenIntId != CalcConstants.ERROR
                ) {
                    /* The fetched token is not the error token
                     * or there are no more tokens on the input.
                     * Finish the extended token and exclude
                     * the current token from it.
                     */
                    tokenData.finishExtendedToken(false);
                }
                break;

            case CalcConstants.INCOMPLETE_ML_COMMENT:
                /* Three possibilities exist:
                 * 1) fetchedTokenExists == true && tokenIntId == CalcConstants.ML_COMMENT
                 *    Lexer recognized end of the multi-line comment token
                 *    and returned CalcConstants.ML_COMMENT.
                 *
                 *    In this case we change the extended token
                 *    to be CalcConstants.ML_COMMENT.
                 *
                 * 2) fetchedTokenExists == true && tokenIntId == CalcConstants.EOF
                 *    There was some additional text after "/*" but EOF was reached
                 *    before matching the closing star-slash and therefore
                 *    the token manager has thrown a lexical error wchich was catched in 
                 *    the fetchToken() and reported as an artificial CalcConstants.EOF token.
                 *    
                 *    In this case we leave the extended token
                 *    to be CalcConstants.INCOMPLETE_ML_COMMENT.
                 *
                 * 3) fetchedTokenExists == false
                 *    There was just "/*" and no more characters after it (EOF was reached).
                 *
                 *    In this case we leave the extended token
                 *    to be CalcConstants.INCOMPLETE_ML_COMMENT.
                 */

                if (fetchedTokenExists && tokenIntId == CalcConstants.ML_COMMENT) { // Token exists
                    tokenData.updateExtendedTokenIntId(tokenIntId);
                }
                tokenData.finishExtendedToken(fetchedTokenExists);
                break;

            default: // there should be no other extended tokens supported
                throw new IllegalStateException("Unsupported extended token");

        }
        
    }


}
