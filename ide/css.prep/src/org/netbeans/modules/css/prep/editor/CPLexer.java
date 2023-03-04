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
package org.netbeans.modules.css.prep.editor;

import org.netbeans.modules.css.prep.editor.CPTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * @author mfukala@netbeans.org
 */
public class CPLexer implements Lexer<CPTokenId> {

    private LexerInput input;
    private TokenFactory<CPTokenId> tokenFactory;

    @Override
    public Object state() {
        return null; //stateless
    }

    public CPLexer(LexerRestartInfo<CPTokenId> info) {
        input = info.input();
        tokenFactory = info.tokenFactory();
    }

    @Override
    public Token<CPTokenId> nextToken() {
        //just read whole input
        while(input.read() != LexerInput.EOF) {};
        //and create one big token
        return input.readLength() > 0 
                ? tokenFactory.createToken(CPTokenId.CSS) 
                : null;
    }

    @Override
    public void release() {
        //no-op
    }

}
