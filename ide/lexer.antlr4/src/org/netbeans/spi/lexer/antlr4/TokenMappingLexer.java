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
package org.netbeans.spi.lexer.antlr4;

import java.util.function.Function;
import java.util.function.IntFunction;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.IntegerList;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.antlr.v4.runtime.Lexer;
import static org.antlr.v4.runtime.Token.EOF;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 * Simple wrapper class lets an ANTLR4 Lexer to be added into NetBeans by simply
 * mapping the token type of an ANTLR4 lexer to a NetBeans TokenId.
 *
 * @author Laszlo Kishalmi
 *
 * @param <T> NetBeans recognized TokenId type.
 */
public final class TokenMappingLexer<T extends TokenId> implements org.netbeans.spi.lexer.Lexer<T> {

    private final LexerRestartInfo<T> info;
    private Lexer lexer;
    private final IntFunction<T> tokenMapper;
    private final Function<CharStream, Lexer> lexerCreator;

    public TokenMappingLexer(LexerRestartInfo<T> info, Function<CharStream, Lexer> lexerCreator, IntFunction<T> tokenMapper) {
        this.info = info;
        this.tokenMapper = tokenMapper;
        this.lexerCreator = lexerCreator;
    }


    private void initLexer() {
        if (lexer == null) {
            lexer = lexerCreator.apply(new LexerInputCharStream(info.input()));
            if (info.state() != null) {
                ((LexerState) info.state()).restore(lexer);
            }
        }
    }

    @Override
    public Token<T> nextToken() {
        initLexer();

        org.antlr.v4.runtime.Token nextToken = lexer.nextToken();
        return nextToken.getType() != EOF ? token(tokenMapper.apply(nextToken.getType())) : null;
    }

    @Override
    public final Object state() {
        return new LexerState(lexer);
    }

    @Override
    public final void release() {
        lexer = null;
    }

    private Token<T> token(T id) {
        return info.tokenFactory().createToken(id);
    }

    private static class LexerState {
        final int mode;
        final int state;
        final IntegerList modes;

        LexerState(Lexer lexer) {
            this.mode = lexer._mode;
            this.modes = new IntegerList(lexer._modeStack);
            this.state = lexer.getState();
        }

        public void restore(Lexer lexer) {
            lexer._modeStack.addAll(modes);
            lexer._mode = mode;
            lexer.setState(state);
        }

        @Override
        public String toString() {
            return String.valueOf(state);
        }
    }
}
