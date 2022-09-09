/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.languages.antlr;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author lkishalmi
 */
public class DummyLexer implements Lexer<AntlrTokenId> {

    final LexerInput input;
    final TokenFactory<AntlrTokenId> factory;

    public DummyLexer(LexerRestartInfo<AntlrTokenId> info) {
        this.input = info.input();
        this.factory = info.tokenFactory();
    }


    @Override
    public Token<AntlrTokenId> nextToken() {
        int read = input.read();
        if (read == LexerInput.EOF) {
            return null;
        } else {
            while (input.read() != LexerInput.EOF);
            input.backup(1);
            return factory.createToken(AntlrTokenId.COMMENT);
        }
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
    }

}
