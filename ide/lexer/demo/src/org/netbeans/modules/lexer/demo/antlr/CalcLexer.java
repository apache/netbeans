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

package org.netbeans.modules.lexer.demo.antlr;

import antlr.LexerSharedInputState;
import antlr.CharStreamException;
import antlr.TokenStreamException;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.AbstractLexer;
import org.netbeans.spi.lexer.antlr.AntlrToken;
import org.netbeans.spi.lexer.util.IntegerCache;
import org.netbeans.spi.lexer.util.LexerInputReader;

/**
 * Wrapper for antlr generated {@link antlr.CharScanner}.
 * <BR>Please read <A href="http://lexer.netbeans.org/doc/antlr.html">
 * to get additional information related to this source.
 *
 * <P>Most of the tokens
 * returned from the scanner are just accepted and passed on 
 * but e.g. error tokens are created by assembling one or more scanner tokens into one
 * extended error token. That's done because it's nicer to produce
 * just one error token than multiple successive error tokens.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class CalcLexer extends AbstractLexer {
    
    private static final CalcLanguage language = CalcLanguage.get();
    
    private CalcScanner scanner;
    
    private LexerInput lexerInput;
    
    public CalcLexer() {
        this.scanner = new CalcScanner((LexerSharedInputState)null);
    }
    
    /*
     * Default implementation returns null already.
     *
    protected Object getLexerState() {
        return null;
    }
     */

    public void restart(LexerInput input, Object state) {
        super.restart(input, state);

        this.lexerInput = input;

        // Assign a new input state to the scanner for the given lexer inputs
        LexerSharedInputState inputState = null;
        if (lexerInput != null) {
            inputState = new LexerSharedInputState(new LexerInputReader(lexerInput));
        }
        scanner.setInputState(inputState);
        if (inputState != null) {
            scanner.resetText();
        }
        
        // state argument ignored - should always be null
    }

    protected final LexerInput getLexerInput() { // this method is necessary for AbstractLexer
        return lexerInput;
    }

    protected final Language getLanguage() { // this method is necessary for AbstractLexer
        return language;
    }

    /**
     * Fetch next token from underlying antlr scanner.
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
            antlr.Token antlrToken = scanner.nextToken();
            if (antlrToken != null) {
                int intId = antlrToken.getType();
                if (intId == CalcScannerTokenTypes.EOF) {
                    return false;
                }
                tokenData.setTokenIntId(antlrToken.getType());
                
                int len;
                if (antlrToken instanceof AntlrToken) {
                    len = ((AntlrToken)antlrToken).getLength();
                } else {
                    String text = antlrToken.getText();
                    len = text.length();
                }

                tokenData.setTokenLength(len);
                
            } else { // antlrToken is null
                return false;  // no more tokens from scanner
            }
                
        } catch (TokenStreamException e) {
            /* Input that could not be recognized by antlr.
             * According to the Calc grammar this should
             * only occur if there are incomplete
             * multi-line-comment
             * at the end of the input
             * or a generic error caused by characters
             * not conforming to the grammar.
             */
            boolean useScannerTextTokenLength = true;
            
            // check for incomplete token - use the state variable
            int incompleteIntId;
            int state = scanner.getState();
            switch (state) {
                case 0:
                    incompleteIntId = CalcLanguage.ERROR_INT;
                    break;
                    
                case CalcScannerTokenTypes.INCOMPLETE_ML_COMMENT:
                    // the following construction in Calc.g causes to get here
                    //
                    // ML_COMMENT  : INCOMPLETE_ML_COMMENT { state = CalcScannerTokenTypes.INCOMPLETE_ML_COMMENT; }
                    // (  { LA(2) != '/' }? '*'
                    //      | ~('*')
                    // )*
                    // "*/" { state = 0; }
                    // ;
                    //
                    incompleteIntId = state;

                    // The scanner would not include
                    // the last char when adding non-star character to the end of input
                    // ending by "/**"
                    // when useScannerTextTokenLength is left to be true
                    // Therefore lexerInput-based tokenLength is used instead.
                    useScannerTextTokenLength = false;
                    break;
                    
                default:
                    throw new IllegalStateException(); // unhandled case

            }
            scanner.resetState();

            tokenData.setTokenIntId(incompleteIntId);
            
            int scannerTextTokenLength = scanner.getText().length();
            int tokenLength = useScannerTextTokenLength
                    ? scannerTextTokenLength
                    : tokenData.getDefaultTokenLength();
                    
            // Sync scanner with lexerInput if necessary
            if (scannerTextTokenLength > tokenLength) { // Should not happen
                throw new IllegalStateException("Internal lexer error"); // NOI18N
            }
            while (scannerTextTokenLength < tokenLength) {
                scannerConsumeChar();
                scannerTextTokenLength++;
            }

            // Make sure that token contains at least one char
            tokenLength = increaseTokenLengthIfEmpty(tokenLength);
            tokenData.setTokenLength(tokenLength);

            scanner.resetText();
        }
        
        return true;
    }
    
    private int increaseTokenLengthIfEmpty(int tokenLength) {
        if (tokenLength == 0) { // single char unaccepted by scanner
            scannerConsumeChar();
            tokenLength++;
        }
        return tokenLength;
    }
    
    private void scannerConsumeChar() {
        try {
            scanner.consume();
        } catch (CharStreamException e) {
            throw new IllegalStateException();
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
         * Now possibly update the tokenIntId for tokens
         * that do not have direct counterparts in the language
         * and start extended tokens for errors
         * and multi-line-comments.
         */
        int tokenIntId = tokenData.getTokenIntId();
        switch (tokenIntId) { // check for types that start extended token
            case CalcLanguage.ERROR_INT:
                // All errors are attempted to be concatenated together
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
            case CalcLanguage.ERROR_INT:
                if (!fetchedTokenExists
                    || tokenIntId != CalcLanguage.ERROR_INT
                ) {
                    /* The fetched token is not the error token
                     * or there are no more tokens on the input.
                     * Finish the extended token and exclude
                     * the current token from it.
                     */
                    tokenData.finishExtendedToken(false);
                }
                break;

            default: // there should be no other extended tokens supported
                throw new IllegalStateException("Unsupported extended token");

        }
        
    }
    
    public String toString() {
        String scannerText = scanner.getText();
        return super.toString() + ", scannerText=\"" + scannerText
            + "\";length=" + scannerText.length();
    }

}
