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
package org.netbeans.modules.languages.hcl;

import java.text.Normalizer;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.languages.hcl.grammar.HCLLexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

import static org.netbeans.modules.languages.hcl.HCLTokenId.*;
import static org.netbeans.modules.languages.hcl.grammar.HCLLexer.*;


/**
 *
 * @author lkishalmi
 */
public final class NbHCLLexer extends AbstractAntlrLexerBridge<HCLLexer, HCLTokenId> {

    private static final Logger LOG = Logger.getLogger(NbHCLLexer.class.getName());

    public NbHCLLexer(LexerRestartInfo<HCLTokenId> info) {
        super(info, NbHCLLexer::createLexer);
    }
    
    @Override
    protected Token<HCLTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        switch (antlrToken.getType()) {
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                return token(COMMENT);

            case BOOL_LIT:
                return token(BOOLEAN);

            case NUMERIC_LIT:
                return token(NUMBER);

            case HCLLexer.IDENTIFIER:
                return token(HCLTokenId.IDENTIFIER);

            case FOR:
            case IF:
            case IN:
                return token(KEYWORD);
                
            case LBRACE:
            case RBRACE:
            case LBRACK:
            case RBRACK:
            case LPAREN:
            case RPAREN:
            case COMMA:
            case DOT:
            case INTERPOLATION_START:
            case INTERPOLATION_END:
            case RARROW:
            case TEMPLATE_START:
            case TEMPLATE_END:
                return token(SEPARATOR);

            case AND:
            case COLON:
            case ELLIPSIS:
            case EQUAL:
            case EQUALS:
            case GT:
            case GTE:
            case LT:
            case LTE:
            case NOT:
            case NOT_EQUALS:
            case OR:
            case PERCENT:
            case PLUS:
            case QUESTION:
                return token(OPERATOR);

            case QUOTE:
                return token(STRING);
            case HEREDOC_START:
            case HEREDOC_END:
                return token(HEREDOC_GUARD);
            case HEREDOC_CONTENT:
                return groupToken(HEREDOC, HEREDOC_CONTENT);

            case STRING_CONTENT:
                return groupToken(STRING, STRING_CONTENT);
                
            case INTERPOLATION_CONTENT:
                return groupToken(INTERPOLATION, INTERPOLATION_CONTENT);
                
            case TEMPLATE_CONTENT:
                return groupToken(ERROR, TEMPLATE_CONTENT);
            case WS:
            case NL:
                return token(WHITESPACE);

            default:
                return token(ERROR);
        }
    }

    @Override
    public Object state() {
        return new LexerState(lexer);
    }

    private final static ANTLRErrorListener HCL_ERROR_LISTENER = new ANTLRErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingObject, int line, int charPositionInLine, String msg, RecognitionException e) {
            LOG.log(Level.SEVERE, msg);
            throw new ParseCancellationException(e);
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean bln, BitSet bitset, ATNConfigSet atncs) {
        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitset, ATNConfigSet atncs) {
        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atncs) {
        }
    };

    private static HCLLexer createLexer(CharStream input) {
        HCLLexer lexer = new HCLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(HCL_ERROR_LISTENER);
        return lexer;
    }

    private static class LexerState extends AbstractAntlrLexerBridge.LexerState<HCLLexer> {
        final String currentHereDocVar;
        final LinkedList<String> hereDocStack = new LinkedList<>();

        LexerState(HCLLexer lexer) {
            super(lexer);

            this.currentHereDocVar = lexer.currentHereDocVar;
            this.hereDocStack.addAll(lexer.hereDocStack);
        }

        @Override
        public void restore(HCLLexer lexer) {
            super.restore(lexer);

            lexer.currentHereDocVar = currentHereDocVar;
            lexer.hereDocStack.addAll(hereDocStack);
        }
    }
    
}