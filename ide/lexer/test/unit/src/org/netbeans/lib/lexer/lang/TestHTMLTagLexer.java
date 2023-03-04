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
 * Lexical analyzer for javadoc language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class TestHTMLTagLexer implements Lexer<TestHTMLTagTokenId> {

    private static final int EOF = LexerInput.EOF;

    private LexerInput input;

    private TokenFactory<TestHTMLTagTokenId> tokenFactory;
    
    public TestHTMLTagLexer(LexerRestartInfo<TestHTMLTagTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }
    
    public Object state() {
        return null;
    }
    
    public Token<TestHTMLTagTokenId> nextToken() {
        int ch = input.read();
        switch (ch) {
            case '<':
                return token(TestHTMLTagTokenId.LT);
            case '>':
                return token(TestHTMLTagTokenId.GT);
            case EOF:
                return null;
            default:
                while (true) {
                    switch (input.read()) {
                        case '>':
                        case EOF:
                            input.backup(1);
                            return token(TestHTMLTagTokenId.TEXT);
                    }
                }
        }
    }

    private Token<TestHTMLTagTokenId> token(TestHTMLTagTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

}
