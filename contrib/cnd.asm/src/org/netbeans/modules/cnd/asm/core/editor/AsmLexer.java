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


package org.netbeans.modules.cnd.asm.core.editor;

import org.netbeans.api.lexer.Token;

import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

import org.netbeans.modules.cnd.asm.model.AsmSyntax;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmBaseTokenId;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmHighlightLexer;
import org.netbeans.modules.cnd.asm.model.lang.syntax.AsmTokenId;

public class AsmLexer implements Lexer<AsmTokenId> {
    
    private AsmHighlightLexer lexer;
    private final LexerRestartInfo info;
    private final AsmSyntax syntax;
    private final LexerInput input;
    
    private final TokenFactory<AsmTokenId> tokenFactory;

    public AsmLexer(LexerRestartInfo<AsmTokenId> info, AsmSyntax syntax) {
        this.syntax = syntax;
        this.info = info;
        
        tokenFactory = info.tokenFactory();
        input = info.input();
    }
    
    public Token<AsmTokenId> nextToken() {        
        if (lexer == null) {
            lexer = syntax.createHighlightLexer(new LexerInputReader(input), 
                                                info.state());
        }
                
        AsmTokenId tokId = lexer.nextToken();
        int length = lexer.getLastLength();
                        
        if (tokId == AsmBaseTokenId.ASM_EOF) {                        
            return null;
        }
        
        if (length == 0) {
            return tokenFactory.createToken(AsmBaseTokenId.ASM_EMPTY, 
                                     input.readLength());
        }
               
        return tokenFactory.createToken(tokId, length);
    }

    public Object state() {
        if (lexer == null) {
            return null;            
        }
        return lexer.getState();
    }

    public void release() {
        
    }

}
