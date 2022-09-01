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

import java.lang.reflect.Field;
import org.antlr.v4.runtime.misc.IntegerStack;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

import static org.tomlj.internal.TomlLexer.*;
import static org.netbeans.modules.languages.toml.TomlTokenId.*;

/**
 *
 * @author lkishalmi
 */
public final class TomlLexer implements Lexer<TomlTokenId> {

    private final TokenFactory<TomlTokenId> tokenFactory;
    private final org.tomlj.internal.TomlLexer lexer;
    private final LexerInputCharStream input;

    public TomlLexer(LexerRestartInfo<TomlTokenId> info) {
        this.tokenFactory = info.tokenFactory();
        this.input = new LexerInputCharStream(info.input());
        this.lexer = new org.tomlj.internal.TomlLexer(input);
        if (info.state() != null) {
            ((LexerState) info.state()).restore(lexer);
        }
    }

    @Override
    public Token<TomlTokenId> nextToken() {
        org.antlr.v4.runtime.Token nextToken = lexer.nextToken();
        if (nextToken.getType() == EOF) {
            return null;
        }
        switch (nextToken.getType()) {
            case TripleQuotationMark:
            case TripleApostrophe:
                return token(ML_STRING_START);

            case StringChar:
            case QuotationMark:
            case Apostrophe:
                return token(STRING);

            case MLBasicStringEnd:
            case MLLiteralStringEnd:
                return token(ML_STRING_END);

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
            case DateComma:
                return token(DATE);
            default:
                return token(ERROR);
        }
    }

    @Override
    public Object state() {
        return new LexerState(lexer);
    }

    @Override
    public void release() {
    }

    private Token<TomlTokenId> token(TomlTokenId id) {
        input.markToken();
        return tokenFactory.createToken(id);
    }

    private static class LexerState {
        private static Field ARRAY_DEPTH;
        private static Field ARRAY_DEPTH_STACK;

        final int state;
        final int mode;
        final IntegerStack modes;

        final int arrayDepth;
        final IntegerStack arrayDepthStack;

        static {
            try {
                // Hack accessing private state parts of TomlLexer
                // See: https://github.com/tomlj/tomlj/pull/42
                ARRAY_DEPTH = org.tomlj.internal.TomlLexer.class.getDeclaredField("arrayDepth");
                ARRAY_DEPTH.setAccessible(true);
                ARRAY_DEPTH_STACK = org.tomlj.internal.TomlLexer.class.getDeclaredField("arrayDepthStack");
                ARRAY_DEPTH_STACK.setAccessible(true);
            } catch (ReflectiveOperationException ex) {
            }
        }

        LexerState(org.tomlj.internal.TomlLexer lexer) {
            this.state= lexer.getState();

            this.mode = lexer._mode;
            this.modes = new IntegerStack(lexer._modeStack);

            try {
                this.arrayDepth = ARRAY_DEPTH.getInt(lexer);
                this.arrayDepthStack = new IntegerStack((IntegerStack)ARRAY_DEPTH_STACK.get(lexer));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        public void restore(org.tomlj.internal.TomlLexer lexer) {
            lexer.setState(state);
            lexer._modeStack.addAll(modes);
            lexer._mode = mode;

            try {
                ARRAY_DEPTH.setInt(lexer, arrayDepth);
                ((IntegerStack) ARRAY_DEPTH_STACK.get(lexer)).addAll(arrayDepthStack);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public String toString() {
            return String.valueOf(state);
        }

    }
}
