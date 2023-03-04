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
final class TestJoinTopLexer implements Lexer<TestJoinTopTokenId> {

    // Copy of LexerInput.EOF
    private static final int EOF = LexerInput.EOF;

    private final LexerInput input;
    
    private final TokenFactory<TestJoinTopTokenId> tokenFactory;
    
    TestJoinTopLexer(LexerRestartInfo<TestJoinTopTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // never set to non-null value in state()
    }

    public Object state() {
        return null; // always in default state after token recognition
    }

    public Token<TestJoinTopTokenId> nextToken() {
        int c = input.read();
        switch (c) {
            case '<':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinTopTokenId.TEXT);
                }
                while (true) {
                    switch ((c = input.read())) {
                        case '>':
                            return token(TestJoinTopTokenId.TAG);
                        case EOF:
                            return token(TestJoinTopTokenId.TEXT);
                    }
                }
                // break;

            case '{':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinTopTokenId.TEXT);
                }
                while (true) {
                    switch ((c = input.read())) {
                        case '}':
                            return token(TestJoinTopTokenId.BRACES);
                        case EOF:
                            return token(TestJoinTopTokenId.TEXT);
                    }
                }
                // break;

            case '"':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinTopTokenId.TEXT);
                }
                while (true) {
                    switch ((c = input.read())) {
                        case '"':
                            return token(TestJoinTopTokenId.BACKQUOTES);
                        case EOF:
                            return token(TestJoinTopTokenId.TEXT);
                    }
                }
                // break;

            case '%':
                if (input.readLength() > 1) {
                    input.backup(1);
                    return token(TestJoinTopTokenId.TEXT);
                }
                while (true) {
                    switch ((c = input.read())) {
                        case '%':
                            return token(TestJoinTopTokenId.PERCENTS);
                        case EOF:
                            // Return PERCENTS even in case there is no closing '%'
                            return token(TestJoinTopTokenId.PERCENTS);
                    }
                }
                // break;

            case EOF: // no more chars on the input
                return null; // the only legal situation when null can be returned

            default:
                while (true) {
                    switch ((c = input.read())) {
                        case '<':
                        case '{':
                        case '"':
                        case '%':
                        case EOF:
                            input.backup(1);
                            return token(TestJoinTopTokenId.TEXT);
                    }
                }
                // break;
        }
    }
        
    private Token<TestJoinTopTokenId> token(TestJoinTopTokenId id) {
        return tokenFactory.createToken(id);
    }
    
    public void release() {
    }

}
