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
package org.netbeans.modules.css.lib;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.*;
import org.netbeans.modules.css.lib.api.ProblemDescription;
import org.netbeans.modules.css.lib.api.ProblemDescription.Type;
import org.openide.util.NbBundle;
import static org.netbeans.modules.css.lib.Bundle.*;

/**
 * Note: Funny aspect of the ANTLR lexer is that it doesn't create any kind of
 * error tokens. So if there's a character in the input which cannot be properly
 * made a part of a token it is simply skipped. The result is that the sequence
 * of tokens is not continuous and there might be "holes".
 *
 * @author marekfukala
 */
public class ExtCss3Lexer extends Css3Lexer {

    private List<ProblemDescription> problems = new ArrayList<>();
    static boolean isLessSource_unit_tests = false;
    static boolean isScssSource_unit_tests = false;
    
    private boolean isLessSource = isLessSource_unit_tests;
    private boolean isScssSource = isScssSource_unit_tests;
    
    public ExtCss3Lexer(CharStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public ExtCss3Lexer(CharStream input) {
        super(input);
    }
    
    public ExtCss3Lexer(CharSequence charSequence, String mimeType) {
        this(charSequence);
        if(mimeType != null) {
            this.isLessSource = mimeType.equals("text/less");
            this.isScssSource = mimeType.equals("text/scss");
        }
    }

    /**
     * Preferred constructor.
     * 
     * Make the Css3Lexer case insensitive by default - the characters passed to
     * the lexer are converted to upper case. The token images are in the
     * original case though.
     */
    public ExtCss3Lexer(CharSequence charSequence) {

        this(new ANTLRStringStream(charSequence.toString()) {
            @Override
            public int LA(int i) {
                if (i == 0) {
                    return 0; // undefined
                }
                if (i < 0) {
                    i++; // e.g., translate LA(-1) to use offset 0
                }

                if ((p + i - 1) >= n) {

                    return CharStream.EOF;
                }
                return Character.toUpperCase(data[p + i - 1]);
            }
        });

    }

    @Override
    //overridden since we need to produce error tokens for unrecognized input,
    //by default such content is only skipped and the resulting token sequence
    //contains "holes".
    //
    //this way of solving the proble seems to be the official one:
    //http://www.antlr.org/wiki/pages/viewpage.action?pageId=5341230
    public Token nextToken() {
        while (true) {
            state.token = null;
            state.channel = Token.DEFAULT_CHANNEL;
            state.tokenStartCharIndex = input.index();
            state.tokenStartCharPositionInLine = input.getCharPositionInLine();
            state.tokenStartLine = input.getLine();
            state.text = null;
            if (input.LA(1) == CharStream.EOF) {
                Token eof = new CommonToken(input, Token.EOF,
                        Token.DEFAULT_CHANNEL,
                        input.index(), input.index());
                eof.setLine(getLine());
                eof.setCharPositionInLine(getCharPositionInLine());
                return eof;
            }
            try {
                mTokens();
                if (state.token == null) {
                    emit();
                } else if (state.token == Token.SKIP_TOKEN) {
                    continue;
                }
                return state.token;
            } catch (RecognitionException re) {
                reportError(re);
                if (re instanceof NoViableAltException) {
                    recover(re);
                }
                // create token that holds mismatched char
                Token t = new CommonToken(input, Token.INVALID_TOKEN_TYPE,
                        Token.DEFAULT_CHANNEL,
                        state.tokenStartCharIndex,
                        getCharIndex() - 1);
                t.setLine(state.tokenStartLine);
                t.setCharPositionInLine(state.tokenStartCharPositionInLine);
                emit(t);
                return state.token;
            }
        }
    }

    @Override
    @NbBundle.Messages({
            "# {0} - the unexpected character",
            "MSG_Error_Unexpected_Char=Unexpected character(s) {0} found"
    })
    public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
        StringBuilder b = new StringBuilder();
        b.append(getErrorHeader(e));
        b.append(' ');
        if (e instanceof NoViableAltException) {
            //lexing error - unexpected character in the char stream
            char unexpectedChar = (char) input.LA(1);
            b.append(MSG_Error_Unexpected_Char(unexpectedChar));
            ProblemDescription pp = new ProblemDescription(e.input.index(), e.input.index() + 1, b.toString(), ProblemDescription.Keys.LEXING.name(), Type.ERROR);
            problems.add(pp);
        } else {
            b.append(getErrorHeader(e));
            b.append(getErrorMessage(e, tokenNames));
        }

    }

    public List<ProblemDescription> getProblems() {
        return problems;
    }
    
    @Override
    protected boolean isLessSource() {
        return isLessSource;
    }

    @Override
    protected boolean isScssSource() {
        return isScssSource;
    }
}
