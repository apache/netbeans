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

package org.netbeans.modules.cnd.lexer;

import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.DoxygenTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Lexical analyzer for doxygen language.
 * based on JavadocLexer
 * 
 * @version 1.00
 */

public class DoxygenLexer implements Lexer<DoxygenTokenId> {

    private static final int EOF = LexerInput.EOF;

    // states
    private static final int INIT = 0;
    private static final int OTHER = 1;

    private LexerInput input;
    private int state = INIT;

    private TokenFactory<DoxygenTokenId> tokenFactory;
    
    public DoxygenLexer(LexerRestartInfo<DoxygenTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        fromState((Integer)info.state());
    }
    
    @Override
    public Object state() {
        return state == INIT ? null : Integer.valueOf(state);
    }

    private void fromState(Integer state) {
        if (state == null) {
            this.state = INIT;
        } else {
            this.state = state.intValue();
        }
    }
    
    private final static String DOXYGEN_CONTROL_SYMBOLS = "@\\<.#"; // NOI18N
    
    public Token<DoxygenTokenId> nextToken() {
        int ch = input.read();
        
        if (ch == EOF) {
            return null;
        }
        
        int oldState = state;
        state = OTHER;
        if (oldState == INIT && ch == '<') {
            return token(DoxygenTokenId.POINTER_MARK);
        }

        if (CndLexerUtilities.isCppIdentifierStart(ch)) {
            //TODO: EOF
            while (CndLexerUtilities.isCppIdentifierPart(input.read())) {}
            
            input.backup(1);
            return token(DoxygenTokenId.IDENT);
        }
        
        if (DOXYGEN_CONTROL_SYMBOLS.indexOf(ch) == (-1)) {
            //TODO: EOF
            ch = input.read();
            
            while (!CndLexerUtilities.isCppIdentifierStart(ch) && DOXYGEN_CONTROL_SYMBOLS.indexOf(ch) == (-1) && ch != EOF) {
                ch = input.read();
            }
            
            if (ch != EOF) {
                input.backup(1);
            }
            return token(DoxygenTokenId.OTHER_TEXT);
        }
        
        switch (ch) {
            case '@':
            case '\\':
            {
                boolean first = true;
                while (true) {
                    ch = input.read();
                    boolean wasFirst = first;
                    first = false;
                    if ((wasFirst && !CndLexerUtilities.isCppIdentifierStart(ch)) || (!CndLexerUtilities.isCppIdentifierPart(ch))) {
                        input.backup(1);
                        if (input.readLength() > 1) {
                            return tokenFactory.createToken(DoxygenTokenId.TAG, input.readLength());
                        } else {
                            // no identifier after control symbol => control symbol is not tag-start
                            return tokenFactory.createToken(DoxygenTokenId.OTHER_TEXT, input.readLength());
                        }
                    }
                }
            }
            case '<':
                while (true) {
                    ch = input.read();
                    if (ch == '>' || ch == EOF) {
                        return token(DoxygenTokenId.HTML_TAG);
                    }
                }
            case '.':
                return token(DoxygenTokenId.DOT);
            case '#':
                return token(DoxygenTokenId.HASH);
        } // end of switch (ch)
        
        assert false;
        
        return null;
    }

    private Token<DoxygenTokenId> token(DoxygenTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }

}
