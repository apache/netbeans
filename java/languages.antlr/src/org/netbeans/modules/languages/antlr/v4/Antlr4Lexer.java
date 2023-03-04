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
package org.netbeans.modules.languages.antlr.v4;

import org.antlr.parser.antlr4.ANTLRv4Lexer;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerRestartInfo;

import static org.antlr.parser.antlr4.ANTLRv4Lexer.*;
import org.netbeans.modules.languages.antlr.AntlrTokenId;
import static org.netbeans.modules.languages.antlr.AntlrTokenId.*;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

/**
 *
 * @author lkishalmi
 */
public final class Antlr4Lexer extends AbstractAntlrLexerBridge<ANTLRv4Lexer, AntlrTokenId> {


    public Antlr4Lexer(LexerRestartInfo<AntlrTokenId> info) {
        super(info, ANTLRv4Lexer::new);
    }

    @Override
    public Object state() {
        return new State(lexer);
    }

    @Override
    protected Token<AntlrTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        switch (antlrToken.getType()) {
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

            case LEXER_CHAR_SET:
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
            case LT:
            case GT:
            case BEGIN_ACTION:
            case END_ACTION:
            case AT:
            case PLUS_ASSIGN:
            case POUND:
            case LBRACE:
            case RBRACE:
            case RARROW:
            case ASSIGN:
            case COMMA:
            case SEMI:
                return token(PUNCTUATION);

            case LPAREN:
            case RPAREN:
            case QUESTION:
            case STAR:
            case PLUS:
            case OR:
            case DOLLAR:
            case RANGE:
            case DOT:
            case NOT:
                return token(REGEXP_CHARS);

            case WS:
                return token(WHITESPACE);

            case ACTION_CONTENT:
                return groupToken(ACTION, ACTION_CONTENT);

            default:
                return token(ERROR);
        }
    }

    private static class State extends AbstractAntlrLexerBridge.LexerState<ANTLRv4Lexer> {
        final int currentRuleType;

        public State(ANTLRv4Lexer lexer) {
            super(lexer);
            this.currentRuleType = lexer.getCurrentRuleType();
        }

        @Override
        public void restore(ANTLRv4Lexer lexer) {
            super.restore(lexer);
            lexer.setCurrentRuleType(currentRuleType);
        }

    }
}
