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
package org.netbeans.modules.rust.grammar;

import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.rust.grammar.antlr4.RustLexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

/**
 *
 */
public class RustLanguageLexer extends AbstractAntlrLexerBridge<RustLexer, RustTokenID> {

    private static final Logger LOG = Logger.getLogger(RustLanguageLexer.class.getName());

    /**
     * A BaseErrorListener that listens for errors in the lexer. We avoid
     * throuing exceptions, since AbstractAntlrLexerBridge doesn't know how to
     * handle them and this may interfere with the EDT. We enable logginf of
     * errors instead, for debugging and enhancement purposes.
     */
    private static final class RustLanguageLexerErrorListener extends BaseErrorListener {

        private static final Level LEVEL = Level.FINE;

        private static String formatMessage(String kind, Recognizer<?, ?> recognizer, Object o, int line, int charPositionInLine, String message, RecognitionException ex) {
            return String.format("%s @%3d:%-3d %s", kind, line, charPositionInLine, message);
        }

        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            if (LOG.isLoggable(LEVEL)) {
                String errorMessage = formatMessage("RUST: Syntax error: ", recognizer, offendingSymbol, line, charPositionInLine, msg, e);
                LOG.log(LEVEL, errorMessage);
            }
        }

        @Override
        public void reportAmbiguity(Parser parser, DFA dfa, int line, int charPositionInLine, boolean bln, BitSet bitset, ATNConfigSet atncs) {
            if (LOG.isLoggable(LEVEL)) {
                String errorMessage = formatMessage("RUST: Ambiguity: ", null, null, line, charPositionInLine, "Ambiguity error", null);
                LOG.log(LEVEL, errorMessage);
            }
        }

        @Override
        public void reportAttemptingFullContext(Parser parser, DFA dfa,
                int line, int charPositionInLine, BitSet bitset,
                ATNConfigSet atncs
        ) {
            if (LOG.isLoggable(LEVEL)) {
                String errorMessage = formatMessage("RUST: AttemptingFullContext: ", null, null, line, charPositionInLine, "Ambiguity error", null);
                LOG.log(LEVEL, errorMessage);
            }
        }

        @Override
        public void reportContextSensitivity(Parser parser, DFA dfa,
                int line, int charPositionInLine, int line2, ATNConfigSet atncs
        ) {
            if (LOG.isLoggable(LEVEL)) {
                String errorMessage = formatMessage("RUST: ContextSensitivity", null, null, line, charPositionInLine, "Ambiguity error", null);
                LOG.log(LEVEL, errorMessage);
            }
        }

    }

    private static RustLexer createLexer(CharStream input) {
        RustLexer lexer = new RustLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new RustLanguageLexerErrorListener());
        return lexer;
    }

    public RustLanguageLexer(LexerRestartInfo<RustTokenID> info) {
        super(info, RustLanguageLexer::createLexer);
    }

    @Override
    protected Token<RustTokenID> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        return token(RustTokenID.from(antlrToken));
    }

    @Override
    public Object state() {
        return new LexerState(lexer);
    }

    private static final class LexerState extends AbstractAntlrLexerBridge.LexerState<RustLexer> {

        final Integer lt1;
        final Integer lt2;

        LexerState(RustLexer lexer) {
            super(lexer);

            this.lt1 = lexer.lt1;
            this.lt2 = lexer.lt2;
        }

        @Override
        public void restore(RustLexer lexer) {
            super.restore(lexer);

            lexer.lt1 = lt1;
            lexer.lt2 = lt2;
        }
    }
}
