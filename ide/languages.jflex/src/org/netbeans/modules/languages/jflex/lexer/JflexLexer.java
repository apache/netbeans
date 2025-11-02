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
package org.netbeans.modules.languages.jflex.lexer;

import org.netbeans.api.lexer.Token;
import org.netbeans.modules.languages.jflex.grammar.antlr4.coloring.JflexAntlrColoringLexer;
import static org.netbeans.modules.languages.jflex.grammar.antlr4.coloring.JflexAntlrColoringLexer.*;
import org.netbeans.spi.lexer.*;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

public class JflexLexer extends AbstractAntlrLexerBridge<JflexAntlrColoringLexer, JflexTokenId> {

    public JflexLexer(LexerRestartInfo<JflexTokenId> info) {
        super(info, JflexAntlrColoringLexer::new);
    }

    @Override
    public Object state() {
        return new State(lexer);
    }

    @Override
    protected Token<JflexTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        return switch (antlrToken.getType()) {
            case COMMENT -> token(JflexTokenId.COMMENT);
            case OPTIONS_SEPARATOR -> token(JflexTokenId.OPTIONS_SEPARATOR);
            case OPTION -> token(JflexTokenId.OPTION);    
            case MACRO -> token(JflexTokenId.MACRO);    
            case LEXICAL_STATE -> token(JflexTokenId.LEXICAL_STATE);
            case KEYWORD -> token(JflexTokenId.KEYWORD);  
            case DIRECTIVE_VALUE -> token(JflexTokenId.DIRECTIVE_VALUE);
            case STRING -> token(JflexTokenId.STRING);
            case LEXICAL_REGEX -> groupToken(JflexTokenId.REGEX, LEXICAL_REGEX);
            case QUANTIFIER -> token(JflexTokenId.QUANTIFIER);
            case NUMBER -> token(JflexTokenId.NUMBER);
            case OPERATOR -> token(JflexTokenId.OPERATOR);
            case PUNCTUATION -> token(JflexTokenId.PUNCTUATION);    
            case CODE -> groupToken(JflexTokenId.CODE, CODE);    
            case WS -> groupToken(JflexTokenId.WHITESPACE, WS);
            default -> groupToken(JflexTokenId.ERROR, ERROR);
        };
    }

    private static class State extends AbstractAntlrLexerBridge.LexerState<JflexAntlrColoringLexer> {

        final boolean ruleDefined;
        final boolean inRuleList;
        final int sqbracketBalance;
        final int parenBalance;
        final int curlyBalance;
        final boolean inMacroAssign;

        public State(JflexAntlrColoringLexer lexer) {
            super(lexer);
            this.ruleDefined = lexer.isRuleDefined();
            this.inRuleList = lexer.isInRuleList();
            this.inMacroAssign = lexer.isInMacroAssign();

            //hack for bracket in rule opening lexer restart
            if (lexer.getSQBracketBalance() == 1 && (this.ruleDefined || this.inMacroAssign)) {
                this.sqbracketBalance = 0;
            } else {
                this.sqbracketBalance = lexer.getSQBracketBalance();
            }

            //hack for paren rule opening lexer restart
            if (lexer.getParenthesisBalance() == 1 && (this.ruleDefined || this.inMacroAssign)) {
                this.parenBalance = 0;
            } else {
                this.parenBalance = lexer.getParenthesisBalance();
            }
            this.curlyBalance = lexer.getCurlyBracketBalance();
            
        }

        @Override
        public void restore(JflexAntlrColoringLexer lexer) {
            super.restore(lexer);
            lexer.setRuleDefined(ruleDefined);
            lexer.setInRuleList(inRuleList);
            lexer.setSQBracketBalance(sqbracketBalance);
            lexer.setParenthesisBalance(parenBalance);
            lexer.setCurlyBracketBalance(curlyBalance);
            lexer.setInMacroAssign(inMacroAssign);
        }
    }

}
