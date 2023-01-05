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

import org.antlr.v4.runtime.misc.IntegerList;
import org.netbeans.api.lexer.Token;
import org.antlr.v4.runtime.Lexer;
import static org.antlr.v4.runtime.Recognizer.EOF;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 * Implementations of this class shall take an ANTLRv4 Lexer and map ANTLR tokens
 * to NetBeans Lexer tokens.
 *
 * @author Laszlo Kishalmi
 * @param <L> The ANTLR Lexer type
 * @param <T> The NetBeans TokenId type
 */
public abstract class AbstractAntlrLexerBridge<L extends Lexer, T extends TokenId> implements org.netbeans.spi.lexer.Lexer<T> {

    private final TokenFactory<T> tokenFactory;
    protected final L lexer;
    protected final LexerInputCharStream input;

    /**
     * Constructor for the lexer bridge, usually used as:
     * <pre>{@code
     * public SomeLexer(LexerRestartInfo<SomeTokenId> info) {
     *     super(info, new SomeANTLRLexer(new LexerInputCharStream(info.input())));
     * }
     * }
     * </pre>
     * @param info  The lexer restart info
     * @param lexer The ANTLR generated Lexer
     */
    @SuppressWarnings("unchecked")
    public AbstractAntlrLexerBridge(LexerRestartInfo<T> info, L lexer) {
        this.tokenFactory = info.tokenFactory();
        this.lexer = lexer;
        this.input = (LexerInputCharStream) lexer.getInputStream();
        if (info.state() != null) {
            ((LexerState<L>) info.state()).restore(lexer);
        }
        input.markToken();
    }


    private org.antlr.v4.runtime.Token preFetchedToken = null;

    @Override
    public final Token<T> nextToken() {
        org.antlr.v4.runtime.Token nextToken;
        if (preFetchedToken != null) {
            nextToken = preFetchedToken;
            input.seek(preFetchedToken.getStopIndex() + 1);
            preFetchedToken = null;
        } else {
            nextToken = nextRealToken();
        }
        return nextToken.getType() != EOF ? mapToken(nextToken.getType()) : null;
    }

    /**
     * Implementations shall provide a suitable mapping between ANTLR lexer
     * token types and NetBeans lexer tokens. The mapping is usually many to one,
     * could be implemented as:
     * <pre>{@code
     * switch (antlrTokenType) {
     *      case DOC_COMMENT:
     *      case BLOCK_COMMENT:
     *      case LINE_COMMENT:
     *          return token(SomeTokenId.COMMENT);
*
     *      case STRING_CHAR:
     *          return groupToken(SomeTokenId.STRING);
     *      default:
     *          return token(SomeTokenId.ERROR);
     *  }
     * }</pre>
     * @param antlrTokenType the token type from the ANTLR Lexer
     *
     * @return a NetBeans lexer token.
     */
    protected abstract Token<T> mapToken(int antlrTokenType);

    @Override
    /**
     * This method can be overridden, if some resource cleanup is needed when
     * the lexer is no longer in use. Usually not required for ANTLR lexers.
     */
    public void release() {
    }

    @Override
    public Object state() {
        return new LexerState<>(lexer);
    }

    /**
     * Some grammars provide big chunks of the same token. It is recommended to
     * group those tokens together in one Token for the editor for performance
     * reasons.
     *
     * @param id the NetBeans TokenId of the returned token.
     * @param antlrTokenType the ANTLR token type to be collected till found.
     *
     * @return one lexer token, that represents several ANTLR tokens.
     */
    protected final Token<T> groupToken(T id, int antlrTokenType) {
        preFetchedToken = nextRealToken();
        while (preFetchedToken.getType() == antlrTokenType) {
            preFetchedToken = nextRealToken();
        }
        input.seek(preFetchedToken.getStartIndex());
        return token(id);
    }
    
    protected final Token<T> token(T id) {
        input.markToken();
        return tokenFactory.createToken(id);
    }

    private org.antlr.v4.runtime.Token nextRealToken() {
        int index = input.index();
        org.antlr.v4.runtime.Token ret = lexer.nextToken();
        // Filtering out pseudo tokens
        while ((ret.getType() != EOF) && (index == input.index())) {
            ret = lexer.nextToken();
        }
        return ret;
    }

    /**
     * This class can save and restore the internal state of a basic ANTLR Lexer.
     * Some grammars has additional state information, for those this class
     * needs to be extended, and the {@link #state()} method has to be
     * overridden.
     *
     * @param <L> The ANTLR Lexer, that state is to be kept.
     */
    public static class LexerState<L extends Lexer> {
        final int state;
        final int mode;
        final IntegerList modes;

        public LexerState(L lexer) {
            this.state= lexer.getState();

            this.mode = lexer._mode;
            this.modes = new IntegerList(lexer._modeStack);
        }

        public void restore(L lexer) {
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
