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
 * @author Miloslav Metelka
 */
final class TestJoinMixTagLexer implements Lexer<TestJoinMixTagTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<TestJoinMixTagTokenId> tokenFactory;
    
    TestJoinMixTagLexer(LexerRestartInfo<TestJoinMixTagTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // never set to non-null value in state()
    }

    public Object state() {
        return null; // always in default state after token recognition
    }

    public Token<TestJoinMixTagTokenId> nextToken() {
        int c = input.read();
        switch (c) {
            case '<':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinMixTagTokenId.TEXT);
                }
                while (true) {
                    switch ((c = input.read())) {
                        case '>':
                            return (input.readLength() == 2)
                                    ? token(TestJoinMixTagTokenId.TEXT) // Empty "<>" is TEXT
                                    : token(TestJoinMixTagTokenId.TAG);
                        case EOF:
                            return token(TestJoinMixTagTokenId.TEXT);
                    }
                }
                // break;

            case EOF: // no more chars on the input
                return null; // the only legal situation when null can be returned

            default:
                while (true) {
                    switch ((c = input.read())) {
                        case '<':
                        case EOF:
                            input.backup(1);
                            return token(TestJoinMixTagTokenId.TEXT);
                    }
                }
                // break;
        }
    }
        
    private Token<TestJoinMixTagTokenId> token(TestJoinMixTagTokenId id) {
        return tokenFactory.createToken(id);
    }
    
    public void release() {
    }

}
