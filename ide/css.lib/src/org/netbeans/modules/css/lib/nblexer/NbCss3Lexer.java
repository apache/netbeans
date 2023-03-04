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
package org.netbeans.modules.css.lib.nblexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.css.lib.Css3Lexer;
import org.netbeans.modules.css.lib.ExtCss3Lexer;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * antlr css3 lexer
 *
 * @author Marek Fukala
 * @version 1.00
 */
public class NbCss3Lexer implements Lexer<CssTokenId> {

    private final TokenFactory<CssTokenId> tokenFactory;
    private Css3Lexer antlrLexer;

    @Override
    public Object state() {
        return null; //stateless
    }

    public NbCss3Lexer(LexerRestartInfo<CssTokenId> info) {
        tokenFactory = info.tokenFactory();
        antlrLexer = new ExtCss3Lexer(new CaseInsensitiveNbLexerCHS(info));
    }

    @Override
    public Token<CssTokenId> nextToken() {
        org.antlr.runtime.Token token = antlrLexer.nextToken();
        int tokenTypeCode = token.getType();
        CssTokenId tokenId = CssTokenId.forTokenTypeCode(tokenTypeCode);
        
        if(tokenId == CssTokenId.EOF) {
            return null; //end of input
        } else {
            return tokenFactory.createToken(tokenId);
        }
    }

    @Override
    public void release() {
        antlrLexer = null;
    }
}
