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
import java.util.function.Function;
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
public abstract class AnstractHCLLexer extends AbstractAntlrLexerBridge<HCLHereDocAdaptor, HCLTokenId> {

    private static final Logger LOG = Logger.getLogger(AnstractHCLLexer.class.getName());

    public AnstractHCLLexer(LexerRestartInfo<HCLTokenId> info, Function<CharStream, HCLHereDocAdaptor> lexerCreator) {
        super(info, lexerCreator);
        lexer.removeErrorListeners();
        lexer.addErrorListener(HCL_ERROR_LISTENER);
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

    private static class LexerState extends AbstractAntlrLexerBridge.LexerState<HCLHereDocAdaptor> {
        final String currentHereDocVar;
        final LinkedList<String> hereDocStack = new LinkedList<>();

        LexerState(HCLHereDocAdaptor lexer) {
            super(lexer);

            this.currentHereDocVar = lexer.currentHereDocVar;
            this.hereDocStack.addAll(lexer.hereDocStack);
        }

        @Override
        public void restore(HCLHereDocAdaptor lexer) {
            super.restore(lexer);

            lexer.currentHereDocVar = currentHereDocVar;
            lexer.hereDocStack.addAll(hereDocStack);
        }
    }
    
}