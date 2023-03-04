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

package org.netbeans.lib.lexer.test.state;

import org.netbeans.api.lexer.InputAttributes;
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
final class StateLexer implements Lexer<StateTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    static final Object AFTER_A = "after_a";
    static final Object AFTER_B = "after_b";
    
    static final Integer AFTER_A_INT = 1;
    static final Integer AFTER_B_INT = 2;

    private boolean useIntStates;
    
    private Object state;
    
    private LexerInput input;

    private TokenFactory<StateTokenId> tokenFactory;
    
    private LexerRestartInfo<StateTokenId> info;
    
    private InputAttributes inputAttributes;
    
    StateLexer(LexerRestartInfo<StateTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        this.state = info.state();
        this.info = info;

        this.useIntStates = Boolean.TRUE.equals(info.getAttributeValue("states"));
        Object expectedRestartState = info.getAttributeValue("restartState");
        if (expectedRestartState != null && !expectedRestartState.equals(state)) {
            throw new IllegalStateException("Expected restart state " + expectedRestartState + ", but real is " + state);
        }
    }

    public Object state() {
        return state;
    }

    public Token<StateTokenId> nextToken() {
        boolean returnNullToken = Boolean.TRUE.equals(info.getAttributeValue("returnNullToken"));
        while (true) {
            int c = input.read();
            if (returnNullToken) // Test early return of null token
                return null;
            switch (c) {
                case 'a':
                    state = useIntStates ? AFTER_A_INT : AFTER_A;
                    return token(StateTokenId.A);

                case 'b':
                    while (input.read() == 'b') {}
                    input.backup(1);
                    state = useIntStates ? AFTER_B_INT : AFTER_B;
                    return token(StateTokenId.BMULTI);

                case EOF: // no more chars on the input
                    return null; // the only legal situation when null can be returned

                default:
                    // Invalid char
                    state = null;
                    return token(StateTokenId.ERROR);
            }
        }
    }
        
    private Token<StateTokenId> token(StateTokenId id) {
        return tokenFactory.createToken(id);
    }
    
    public void release() {
        InputAttributes attrs = info.inputAttributes();
        if (attrs != null) {
            attrs.setValue(StateTokenId.language(), "lexerRelease", Boolean.TRUE, false);
        }
    }

}
