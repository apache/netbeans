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
package org.netbeans.modules.rust.grammar.antlr4;

import java.util.BitSet;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

/**
 *
 * @author antonio
 */
public final class RustANTLRTestErrorListener implements ANTLRErrorListener {

    private static final Logger LOG = Logger.getLogger(RustANTLRTestErrorListener.class.getName());

    private static String formatMessage(String kind, Recognizer<?, ?> recognizer, Object o, int line, int charPositionInLine, String message, RecognitionException ex) {
       return String.format("%s @%3d:%-3d %s", kind, line, charPositionInLine, message);
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int line, int charPositionInLine, String message, RecognitionException re) {
        String errorMessage = formatMessage("Syntax error: ", recognizer, o, line, charPositionInLine, message, re);
        throw new IllegalStateException(errorMessage);
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int line, int charPositionInLine, boolean bln, BitSet bitset, ATNConfigSet atncs) {
        String errorMessage = formatMessage("Ambiguity: ", null, null, line, charPositionInLine, "Ambiguity error", null);
        System.err.println(errorMessage);
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int line, int charPositionInLine, BitSet bitset, ATNConfigSet atncs) {
        String errorMessage = formatMessage("AttemptingFullContext: ", null, null, line, charPositionInLine, "Ambiguity error", null);
        System.err.println(errorMessage);
    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int line, int charPositionInLine, int line2, ATNConfigSet atncs) {
        String errorMessage = formatMessage("ContextSensitivity", null, null, line, charPositionInLine, "Ambiguity error", null);
    }

}
