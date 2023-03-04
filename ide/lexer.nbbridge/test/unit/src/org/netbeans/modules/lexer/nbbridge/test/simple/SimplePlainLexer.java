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

package org.netbeans.modules.lexer.nbbridge.test.simple;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Simple implementation a lexer.
 *
 * @author mmetelka
 */
final class SimplePlainLexer implements Lexer<SimplePlainTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private static final int INIT = 0;
    private static final int IN_WORD = 1;
    private static final int IN_WHITESPACE = 2;
    
    
    private LexerInput input;
    
    private TokenFactory<SimplePlainTokenId> tokenFactory;
    
    private int state = INIT;
    
    SimplePlainLexer(LexerRestartInfo<SimplePlainTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null);
    }
    
    public Token<SimplePlainTokenId> nextToken() {
        while (true) {
            int ch = input.read();
            switch (state) {
                case INIT:
                    if (ch == EOF) {
                        return null;
                    } else if (Character.isWhitespace((char)ch)) { // start of whitespace
                        state = IN_WHITESPACE;
                    } else { // start of word
                        state = IN_WORD;
                    }
                    break;

                case IN_WORD:
                    while (true) {
                        if (ch == EOF || Character.isWhitespace((char)ch)) {
                            if (ch != EOF) { // no backup of EOF
                                input.backup(1);
                            }
                            state = INIT;
                            return tokenFactory.createToken(SimplePlainTokenId.WORD);
                        }
                        ch = input.read();
                    }
                    // break;

                case IN_WHITESPACE:
                    while (true) {
                        if (ch == EOF || !Character.isWhitespace((char)ch)) {
                            if (ch != EOF) { // no backup of EOF
                                input.backup(1);
                            }
                            state = INIT;
                            return tokenFactory.createToken(SimplePlainTokenId.WHITESPACE);
                        }
                        ch = input.read();
                    }
                    // break;

                default:
                    throw new IllegalStateException();
            }
        }
    }
    
    public Object state() {
        return null; // always in default state after token recognition
    }
    
    public void release() {
    }

}
