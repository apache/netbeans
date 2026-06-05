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
package org.netbeans.modules.languages.env.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;
import org.netbeans.modules.languages.env.grammar.antlr4.coloring.EnvAntlrColoringLexer;
import static org.netbeans.modules.languages.env.grammar.antlr4.coloring.EnvAntlrColoringLexer.*;

public class EnvLexer extends AbstractAntlrLexerBridge<EnvAntlrColoringLexer, EnvTokenId> {
    public EnvLexer(LexerRestartInfo<EnvTokenId> info) {
        super(info, EnvAntlrColoringLexer::new);
    }

    @Override
    public Object state() {
        return new State(lexer);
    }

    @Override
    protected Token<EnvTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        return switch (antlrToken.getType()) {
            case COMMENT -> groupToken(EnvTokenId.COMMENT, COMMENT);
            case KEY -> token(EnvTokenId.KEY);
            case KEYWORD -> token(EnvTokenId.KEYWORD);  
            case STRING -> groupToken(EnvTokenId.STRING, STRING);
            case VALUE -> groupToken(EnvTokenId.VALUE, VALUE);    
            case OPERATOR, ASSIGN_OPERATOR -> token(EnvTokenId.OPERATOR);
            case CURLY_OPEN, CURLY_CLOSE -> token(EnvTokenId.INTERPOLATION_DELIMITATOR);
            case INTERPOLATION_OPERATOR -> token(EnvTokenId.INTERPOLATION_OPERATOR);
            case DELIMITATOR -> token(EnvTokenId.DELIMITATOR);
            case DOLLAR -> token(EnvTokenId.DOLLAR);    
            case WS -> groupToken(EnvTokenId.WS, WS);    
            case NL -> groupToken(EnvTokenId.WS, NL);
            default -> groupToken(EnvTokenId.ERROR, ERROR);
        };
    }

    private static class State extends AbstractAntlrLexerBridge.LexerState<EnvAntlrColoringLexer> {
        final boolean interpolationKeyAdded;

        public State(EnvAntlrColoringLexer lexer) {
            super(lexer);
            this.interpolationKeyAdded = lexer.keyTokenAdded();
        }

        @Override
        public void restore(EnvAntlrColoringLexer lexer) {
            super.restore(lexer);
            lexer.setInterpolationKeyAddedState(interpolationKeyAdded);
        }
    }
}
