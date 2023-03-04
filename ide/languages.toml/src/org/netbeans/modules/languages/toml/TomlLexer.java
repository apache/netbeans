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
package org.netbeans.modules.languages.toml;

import org.antlr.v4.runtime.misc.IntegerStack;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.LexerRestartInfo;

import static org.tomlj.internal.TomlLexer.*;
import static org.netbeans.modules.languages.toml.TomlTokenId.*;
import org.netbeans.spi.lexer.antlr4.AbstractAntlrLexerBridge;

/**
 *
 * @author lkishalmi
 */
public final class TomlLexer extends AbstractAntlrLexerBridge<org.tomlj.internal.TomlLexer, TomlTokenId> {

    public TomlLexer(LexerRestartInfo<TomlTokenId> info) {
        super(info, org.tomlj.internal.TomlLexer::new);
    }

    @Override
    protected Token<TomlTokenId> mapToken(org.antlr.v4.runtime.Token antlrToken) {
        switch (antlrToken.getType()) {
            case EOF:
                return null;

            case StringChar:
                return groupToken(STRING, StringChar);

            case TripleQuotationMark:
            case TripleApostrophe:
            case QuotationMark:
            case Apostrophe:
                return token(STRING_QUOTE);

            case Comma:
            case ArrayStart:
            case ArrayEnd:
            case InlineTableStart:
            case InlineTableEnd:

            case Dot:
                return token(DOT);

            case Equals:
                return token(EQUALS);

            case TableKeyStart:
            case TableKeyEnd:
            case ArrayTableKeyStart:
            case ArrayTableKeyEnd:
                return token(TABLE_MARK);
            case UnquotedKey:
                return token(KEY);
            case Comment:
                return token(COMMENT);
            case WS:
            case NewLine:
                return token(TomlTokenId.WHITESPACE);
            case Error:
                return token(ERROR);

            case DecimalInteger:
            case HexInteger:
            case OctalInteger:
            case BinaryInteger:
            case FloatingPoint:
            case FloatingPointInf:
            case FloatingPointNaN:
                return token(NUMBER);

            case TrueBoolean:
            case FalseBoolean:
                return token(BOOLEAN);

            case EscapeSequence:
                return token(ESCAPE_SEQUENCE);

            case Dash:
            case Plus:
            case Colon:
            case Z:
            case TimeDelimiter:
            case DateDigits:
                return token(DATE);
            default:
                return token(ERROR);
        }
    }

    @Override
    public Object state() {
        return new LexerState(lexer);
    }

    private static class LexerState extends AbstractAntlrLexerBridge.LexerState<org.tomlj.internal.TomlLexer> {
        final int arrayDepth;
        final IntegerStack arrayDepthStack;

        LexerState(org.tomlj.internal.TomlLexer lexer) {
            super(lexer);

            this.arrayDepth = lexer.arrayDepth;
            this.arrayDepthStack = new IntegerStack(lexer.arrayDepthStack);
        }

        @Override
        public void restore(org.tomlj.internal.TomlLexer lexer) {
            super.restore(lexer);

            lexer.arrayDepth = arrayDepth;
            lexer.arrayDepthStack.addAll(arrayDepthStack);
        }
    }
}
