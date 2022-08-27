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

import org.antlr.v4.runtime.misc.IntegerList;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

import static org.antlr.parser.antlr4.ANTLRv4Lexer.*;
import org.antlr.v4.runtime.CharStream;
import static org.netbeans.modules.languages.antlr.AntlrTokenId.*;

/**
 *
 * @author lkishalmi
 */
public final class AntlrLexer implements Lexer<AntlrTokenId> {

    private final TokenFactory<AntlrTokenId> tokenFactory;
    private org.antlr.parser.antlr4.ANTLRv4Lexer lexer;

    public AntlrLexer(LexerRestartInfo<AntlrTokenId> info) {
        this.tokenFactory = info.tokenFactory();
        this.lexer = new org.antlr.parser.antlr4.ANTLRv4Lexer(new LexerInputCharStream(info.input()));
        if (info.state() != null) {
            ((LexerState) info.state()).restore(lexer);
        }
    }

    private org.antlr.v4.runtime.Token preFetchedToken = null;

    @Override
    public Token<AntlrTokenId> nextToken() {
        try {
            org.antlr.v4.runtime.Token nextToken;
            if (preFetchedToken != null) {
                nextToken = preFetchedToken;
                lexer.getInputStream().seek(preFetchedToken.getStopIndex() + 1);
                preFetchedToken = null;
            } else {
                nextToken = lexer.nextToken();
            }
            if (nextToken.getType() == EOF) {
                return null;
            }
            switch (nextToken.getType()) {
                case TOKEN_REF:
                    return token(TOKEN);
                case RULE_REF:
                    return token(RULE);

                case DOC_COMMENT:
                case BLOCK_COMMENT:
                case LINE_COMMENT:
                    return token(AntlrTokenId.COMMENT);

                case INT:
                    return token(NUMBER);

                case STRING_LITERAL:
                case UNTERMINATED_STRING_LITERAL:
                    return token(STRING);

                case OPTIONS:
                case TOKENS:
                case CHANNELS:
                case IMPORT:
                case FRAGMENT:
                case LEXER:
                case PARSER:
                case GRAMMAR:
                case PROTECTED:
                case PUBLIC:
                case PRIVATE:
                case RETURNS:
                case LOCALS:
                case THROWS:
                case CATCH:
                case FINALLY:
                case MODE:
                    return token(KEYWORD);

                case COLON:
                case COLONCOLON:
                case COMMA:
                case SEMI:
                case LPAREN:
                case RPAREN:
                case LBRACE:
                case RBRACE:
                case RARROW:
                case LT:
                case GT:
                case ASSIGN:
                case QUESTION:
                case STAR:
                case PLUS_ASSIGN:
                case PLUS:
                case OR:
                case DOLLAR:
                case RANGE:
                case DOT:
                case AT:
                case POUND:
                case NOT:
                case BEGIN_ACTION:
                case END_ACTION:
                    return token(PUNCTUATION);

                case WS:
                    return token(WHITESPACE);

                case ACTION_CONTENT:
                    preFetchedToken = lexer.nextToken();
                    while (preFetchedToken.getType() == ACTION_CONTENT) {
                        preFetchedToken = lexer.nextToken();
                    }
                    lexer.getInputStream().seek(preFetchedToken.getStartIndex());
                    return token(ACTION);
                
                default:
                    return token(ERROR);
            }
        } catch (IndexOutOfBoundsException ex) {
            return token(ERROR);
        }
    }

    @Override
    public Object state() {
        return new LexerState(lexer);
    }

    @Override
    public void release() {
    }

    private Token<AntlrTokenId> token(AntlrTokenId id) {
        return tokenFactory.createToken(id);
    }

    private static class LexerState {
        final int state;
        final int mode;
        final IntegerList modes;

        LexerState(org.antlr.v4.runtime.Lexer lexer) {
            this.state= lexer.getState();

            this.mode = lexer._mode;
            this.modes = new IntegerList(lexer._modeStack);
        }

        public void restore(org.antlr.v4.runtime.Lexer lexer) {
            lexer.setState(state);
            lexer._modeStack.addAll(modes);
            lexer._mode = mode;
        }

        @Override
        public String toString() {
            return String.valueOf(state);
        }

    }
}
