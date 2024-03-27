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
package org.netbeans.modules.languages.toml;

import java.util.logging.Logger;
import net.vieiro.toml.antlr4.TOMLAntlrLexer;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerRestartInfo;

import static org.netbeans.modules.languages.toml.TomlTokenId.*;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

/**
 *
 * @author lkishalmi
 */
public final class TomlLexer extends AbstractAntlrLexerBridge<TOMLAntlrLexer, TomlTokenId> {

    private static final Logger LOG = Logger.getLogger(TomlLexer.class.getName());

    public TomlLexer(LexerRestartInfo<TomlTokenId> info) {
        super(info, TOMLAntlrLexer::new);
    }

    @Override
    protected Token<TomlTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        switch (antlrToken.getType()) {
            case TOMLAntlrLexer.EOF:
                return null;

            // Strings
            case TOMLAntlrLexer.BASIC_STRING:
            case TOMLAntlrLexer.ML_BASIC_STRING:
            case TOMLAntlrLexer.LITERAL_STRING:
            case TOMLAntlrLexer.ML_LITERAL_STRING:
                return token(STRING_QUOTE);

            // Booleans
            case TOMLAntlrLexer.BOOLEAN:
                return token(BOOLEAN);

            // Numbers
            case TOMLAntlrLexer.DEC_INT:
            case TOMLAntlrLexer.BIN_INT:
            case TOMLAntlrLexer.OCT_INT:
            case TOMLAntlrLexer.HEX_INT:
            case TOMLAntlrLexer.FLOAT:
            case TOMLAntlrLexer.INF:
            case TOMLAntlrLexer.NAN:
                return token(NUMBER);

            // Dates
            case TOMLAntlrLexer.LOCAL_DATE:
            case TOMLAntlrLexer.LOCAL_DATE_TIME:
            case TOMLAntlrLexer.LOCAL_TIME:
            case TOMLAntlrLexer.OFFSET_DATE_TIME:
                return token(DATE);

            // Comments
            case TOMLAntlrLexer.COMMENT:
                return token(COMMENT);

            // Punctuation
            case TOMLAntlrLexer.COMMA:
            case TOMLAntlrLexer.EQUALS:
            case TOMLAntlrLexer.L_BRACE:
            case TOMLAntlrLexer.L_BRACKET:
            case TOMLAntlrLexer.R_BRACE:
            case TOMLAntlrLexer.R_BRACKET:
            case TOMLAntlrLexer.DOT:
                return token(DOT);

            case TOMLAntlrLexer.DOUBLE_L_BRACKET:
            case TOMLAntlrLexer.DOUBLE_R_BRACKET:
                return token(TABLE_MARK);

            // Whitespace, NL
            case TOMLAntlrLexer.WS:
            case TOMLAntlrLexer.NL:
                return token(WHITESPACE);

            // Keys
            case TOMLAntlrLexer.UNQUOTED_KEY:
                return token(KEY);

            // Invalid values
            case TOMLAntlrLexer.INVALID_VALUE:
                return token(ERROR);

            default:
                LOG.info(String.format("Unexpected token type: %s", TOMLAntlrLexer.VOCABULARY.getSymbolicName(antlrToken.getType())));
                return token(ERROR);
        }
    }

}
