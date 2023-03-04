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

package org.netbeans.modules.java.hints.declarative.test;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author lahvac
 */
class TestLexer implements Lexer<TestTokenId> {

    private final LexerInput input;
    private final TokenFactory<TestTokenId> factory;

    public TestLexer(LexerRestartInfo<TestTokenId> info) {
        this.input = info.input();
        this.factory = info.tokenFactory();
    }

    @Override
    public Token<TestTokenId> nextToken() {
        if (input.read() == LexerInput.EOF) {
            return null;
        }
        
        input.read();

        if (input.readText().toString().startsWith("%%")) {
            readUntil("\n");
            
            return factory.createToken(TestTokenId.METADATA);
        }

        if (readUntil("\n%%")) {
            input.backup(2);
        }

        return factory.createToken(TestTokenId.JAVA_CODE);
    }

    private boolean readUntil(String condition) {
        int read;

        while ((read = input.read()) != LexerInput.EOF && !input.readText().toString().endsWith(condition))
            ;

        return read != LexerInput.EOF;
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {}

}
