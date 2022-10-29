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
package org.netbeans.modules.languages.antlr;

import org.antlr.v4.runtime.misc.IntegerList;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 *
 * @author lkishalmi
 */
public abstract class AbstractAntlrLexer<T extends org.antlr.v4.runtime.Lexer> implements Lexer<AntlrTokenId> {

    private final TokenFactory<AntlrTokenId> tokenFactory;
    protected final T lexer;
    private final LexerInputCharStream input;

    public AbstractAntlrLexer(LexerRestartInfo<AntlrTokenId> info, T lexer) {
        this.tokenFactory = info.tokenFactory();
        this.lexer = lexer;
        this.input = (LexerInputCharStream) lexer.getInputStream();
        if (info.state() != null) {
            ((LexerState) info.state()).restore(lexer);
        }
        input.markToken();
    }


    @Override
    public void release() {
    }

    protected final Token<AntlrTokenId> token(AntlrTokenId id) {
        input.markToken();
        return tokenFactory.createToken(id);
    }

    public static class LexerState<T extends org.antlr.v4.runtime.Lexer> {
        final int state;
        final int mode;
        final IntegerList modes;

        public LexerState(T lexer) {
            this.state= lexer.getState();

            this.mode = lexer._mode;
            this.modes = new IntegerList(lexer._modeStack);
        }

        public void restore(T lexer) {
            lexer.setState(state);
            lexer._modeStack.addAll(modes);
            lexer._mode = mode;
        }

        @Override
        public String toString() {
            return String.valueOf(state);
        }

    }
}
