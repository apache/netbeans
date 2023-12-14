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
package org.netbeans.modules.rust.cargo.language;

import java.util.BitSet;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import static org.antlr.v4.runtime.Token.EOF;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.rust.cargo.language.antlr4.TOMLLexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

/**
 *
 * @author lkishalmi
 */
public final class CargoTOMLLanguageLexer extends AbstractAntlrLexerBridge<TOMLLexer, CargoTOMLTokenID> {

    private static final Logger LOG = Logger.getLogger(CargoTOMLLanguageLexer.class.getName());

    public CargoTOMLLanguageLexer(LexerRestartInfo<CargoTOMLTokenID> info) {
        super(info, TOMLLexer::new);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ANTLRErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> rcgnzr, Object o, int i, int i1, String string, RecognitionException re) {
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
        });
    }

    @Override
    protected Token<CargoTOMLTokenID> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        switch (antlrToken.getType()) {
            case EOF:
                return null;

            case TOMLLexer.BASIC_STRING:
            case TOMLLexer.LITERAL_STRING:
            case TOMLLexer.ML_BASIC_STRING:
            case TOMLLexer.ML_LITERAL_STRING:
                return token(CargoTOMLTokenID.STRING);

            case TOMLLexer.COMMENT:
                return token(CargoTOMLTokenID.COMMENT);

            case TOMLLexer.LOCAL_DATE:
            case TOMLLexer.LOCAL_DATE_TIME:
            case TOMLLexer.LOCAL_TIME:
            case TOMLLexer.OFFSET_DATE_TIME:
                return token(CargoTOMLTokenID.DATE);

            case TOMLLexer.R_BRACE:
            case TOMLLexer.R_BRACKET:
            case TOMLLexer.L_BRACE:
            case TOMLLexer.L_BRACKET:
            case TOMLLexer.DOUBLE_L_BRACKET:
            case TOMLLexer.DOUBLE_R_BRACKET:
            case TOMLLexer.EQUALS:
                return token(CargoTOMLTokenID.OPERATOR);

            case TOMLLexer.COMMA:
            case TOMLLexer.DOT:
                return token(CargoTOMLTokenID.SEPARATOR);

            case TOMLLexer.NAN:
            case TOMLLexer.FLOAT:
            case TOMLLexer.DEC_INT:
            case TOMLLexer.HEX_INT:
            case TOMLLexer.OCT_INT:
            case TOMLLexer.BIN_INT:
            case TOMLLexer.INF:
                return token(CargoTOMLTokenID.NUMBER);

            case TOMLLexer.BOOLEAN:
                return token(CargoTOMLTokenID.BOOLEAN);

            case TOMLLexer.UNQUOTED_KEY:
                return token(CargoTOMLTokenID.KEY);

            case TOMLLexer.WS:
            case TOMLLexer.NL:
            case TOMLLexer.ARRAY_WS:
            case TOMLLexer.INLINE_TABLE_WS:
            case TOMLLexer.VALUE_WS:
                return token(CargoTOMLTokenID.WHITESPACE);

            case TOMLLexer.TOML_ERROR:
                return token(CargoTOMLTokenID.ERROR);

            default:
                return token(CargoTOMLTokenID.ERROR);
        }
    }

    @Override
    public Object state() {
        return new LexerState(lexer);
    }

    private static class LexerState extends AbstractAntlrLexerBridge.LexerState<TOMLLexer> {

        LexerState(TOMLLexer lexer) {
            super(lexer);
        }

        @Override
        public void restore(TOMLLexer lexer) {
            super.restore(lexer);
        }
    }
}
