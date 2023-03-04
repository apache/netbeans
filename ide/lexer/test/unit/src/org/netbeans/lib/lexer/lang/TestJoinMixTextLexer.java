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

package org.netbeans.lib.lexer.lang;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author mmetelka
 */
final class TestJoinMixTextLexer implements Lexer<TestJoinMixTextTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<TestJoinMixTextTokenId> tokenFactory;
    
    TestJoinMixTextLexer(LexerRestartInfo<TestJoinMixTextTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
    }

    public Object state() {
        return null;
    }

    public Token<TestJoinMixTextTokenId> nextToken() {
        boolean inWS = false;
        while (true) {
            int c = input.read();
            switch (c) {
                case ' ':
                case '\t':
                case '\n':
                    if (!inWS) {
                        inWS = true;
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TestJoinMixTextTokenId.WORD);
                        }
                    }
                    break;

                case EOF: // no more chars on the input
                    if (input.readLength() > 0) {
                        return inWS ?
                                token(TestJoinMixTextTokenId.WHITESPACE) :
                                token(TestJoinMixTextTokenId.WORD);
                    }
                    return null; // the only legal situation when null can be returned

                default: // Non-ws
                    if (inWS) {
                        inWS = false;
                        if (input.readLength() > 1) {
                            input.backup(1);
                            return token(TestJoinMixTextTokenId.WHITESPACE);
                        }
                    }
                    break;
            }
        }
    }
    
    private Token<TestJoinMixTextTokenId> token(TestJoinMixTextTokenId id) {
        return tokenFactory.createToken(id);
    }
    
    public void release() {
    }

}
