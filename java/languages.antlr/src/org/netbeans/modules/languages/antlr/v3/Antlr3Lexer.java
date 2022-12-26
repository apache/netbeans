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
package org.netbeans.modules.languages.antlr.v3;

import org.antlr.parser.antlr3.ANTLRv3Lexer;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerRestartInfo;

import static org.antlr.parser.antlr3.ANTLRv3Lexer.*;
import org.netbeans.modules.languages.antlr.AbstractAntlrLexer;
import org.netbeans.modules.languages.antlr.AntlrTokenId;
import static org.netbeans.modules.languages.antlr.AntlrTokenId.*;
import org.netbeans.modules.languages.antlr.LexerInputCharStream;

/**
 *
 * @author lkishalmi
 */
public final class Antlr3Lexer extends AbstractAntlrLexer<ANTLRv3Lexer> {


    public Antlr3Lexer(LexerRestartInfo<AntlrTokenId> info) {
        super(info, new ANTLRv3Lexer(new LexerInputCharStream(info.input())));
    }

    private org.antlr.v4.runtime.Token preFetchedToken = null;

    @Override
    public Token<AntlrTokenId> nextToken() {
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
                return token(AntlrTokenId.TOKEN);
            case RULE_REF:
                return token(AntlrTokenId.RULE);

            case DOC_COMMENT:
            case ML_COMMENT:
            case SL_COMMENT:
                return token(AntlrTokenId.COMMENT);

            case INT:
                return token(NUMBER);

            case CHAR_LITERAL:
            case STRING_LITERAL:
            case DOUBLE_QUOTE_STRING_LITERAL:
            case DOUBLE_ANGLE_STRING_LITERAL:
                return token(STRING);

            case BEGIN_ARGUMENT:
            case BEGIN_ACTION:
            case END_ARGUMENT:
            case END_ACTION:
                return token(PUNCTUATION);


            case OPTIONS:
            case TOKENS:
            case CATCH:
            case FINALLY:
            case FRAGMENT:
            case GRAMMAR:
            case LEXER:
            case PARSER:
            case PRIVATE:
            case PROTECTED:
            case PUBLIC:
            case RETURNS:
            case SCOPE:
            case THROWS:
            case TREE:
                return token(KEYWORD);

            case AT:
            case COLON:
            case COLONCOLON:
            case COMMA:
            case DOT:
            case EQUAL:
            case LBRACE:
            case RBRACE:
            case SEMI:
            case SEMPREDOP:
            case PEQ:
            case REWRITE:
                return token(PUNCTUATION);

            case LBRACK:
            case LPAREN:
            case OR:
            case PLUS:
            case QM:
            case RBRACK:
            case RPAREN:
            case STAR:
            case DOLLAR:
            case NOT:
            case LEXER_CHAR_SET:
            case RANGE:
                return token(REGEXP_CHARS);

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
    }

    @Override
    public Object state() {
        return new State(lexer);
    }

    public static class State extends AbstractAntlrLexer.LexerState<ANTLRv3Lexer> {
        final int currentRuleType;

        public State(ANTLRv3Lexer lexer) {
            super(lexer);
            this.currentRuleType = lexer.getCurrentRuleType();
        }

        @Override
        public void restore(ANTLRv3Lexer lexer) {
            super.restore(lexer);
            lexer.setCurrentRuleType(currentRuleType);
        }

    }
}
