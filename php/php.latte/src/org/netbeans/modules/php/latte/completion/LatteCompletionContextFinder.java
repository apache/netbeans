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
package org.netbeans.modules.php.latte.completion;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;
import org.netbeans.modules.php.latte.parser.LatteParserResult;
import org.netbeans.modules.php.latte.utils.LatteLexerUtils;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class LatteCompletionContextFinder {
    private static final List<Object[]> FILTER_TOKEN_CHAINS = Arrays.asList(
            new Object[]{ValuedTokenId.HELPER_TOKEN},
            new Object[]{ValuedTokenId.HELPER_TOKEN, LatteMarkupTokenId.T_SYMBOL}
    );
    private static final List<Object[]> ITERATOR_ITEMS_TOKEN_CHAINS = Arrays.asList(
            new Object[]{ValuedTokenId.ITERATOR_TOKEN, ValuedTokenId.OBJECT_ACCESS_TOKEN},
            new Object[]{ValuedTokenId.ITERATOR_TOKEN, ValuedTokenId.OBJECT_ACCESS_TOKEN, LatteMarkupTokenId.T_SYMBOL}
    );
    private static final List<Object[]> VARIABLE_TOKEN_CHAINS = Arrays.asList(
            new Object[]{ValuedTokenId.VARIABLE_TOKEN},
            new Object[]{LatteMarkupTokenId.T_VARIABLE}
    );
    private static final List<Object[]> END_MACRO_TOKEN_CHAINS = Arrays.asList(
            new Object[]{LatteMarkupTokenId.T_MACRO_END},
            new Object[]{LatteMarkupTokenId.T_MACRO_END, LatteMarkupTokenId.T_SYMBOL}
    );
    private static final List<Object[]> CONTROL_MACRO_TOKEN_CHAINS = Arrays.asList(
            new Object[]{LatteMarkupTokenId.T_MACRO_START},
            new Object[]{LatteMarkupTokenId.T_MACRO_START, LatteMarkupTokenId.T_WHITESPACE},
            new Object[]{LatteMarkupTokenId.T_MACRO_START, LatteMarkupTokenId.T_WHITESPACE, LatteMarkupTokenId.T_SYMBOL}
    );

    private LatteCompletionContextFinder() {
    }

    public static LatteCompletionContext find(LatteParserResult parserResult, int caretOffset) {
        LatteCompletionContext result = LatteCompletionContext.NONE;
        TokenSequence<? extends LatteMarkupTokenId> ts = LatteLexerUtils.getLatteMarkupTokenSequence(parserResult.getSnapshot(), caretOffset);
        if (ts != null) {
            ts.move(caretOffset);
            if (ts.moveNext() || ts.movePrevious()) {
                result = findContext(ts);
            }
        } else {
            TokenSequence<? extends LatteTopTokenId> tts = LatteLexerUtils.getTokenSequence(
                    parserResult.getSnapshot().getTokenHierarchy(),
                    caretOffset,
                    LatteTopTokenId.language());
            if (tts != null) {
                result = LatteCompletionContext.EMPTY_DELIMITERS;
            }
        }
        return result;
    }

    private static LatteCompletionContext findContext(TokenSequence<? extends LatteMarkupTokenId> ts) {
        LatteCompletionContext result = LatteCompletionContext.ALL;
        do {
            Token<? extends LatteMarkupTokenId> token = ts.token();
            if (token == null) {
                break;
            }
            LatteMarkupTokenId tokenId = token.id();
            if (acceptTokenChains(ts, FILTER_TOKEN_CHAINS, false)) {
                result = LatteCompletionContext.HELPER;
                break;
            } else if (acceptTokenChains(ts, ITERATOR_ITEMS_TOKEN_CHAINS, false)) {
                result = LatteCompletionContext.ITERATOR_ITEM;
                break;
            } else if (acceptTokenChains(ts, VARIABLE_TOKEN_CHAINS, false)) {
                result = LatteCompletionContext.VARIABLE;
                break;
            } else if (acceptTokenChains(ts, END_MACRO_TOKEN_CHAINS, false)) {
                result = LatteCompletionContext.END_MACRO;
                break;
            } else if (acceptTokenChains(ts, CONTROL_MACRO_TOKEN_CHAINS, false)) {
                result = LatteCompletionContext.CONTROL_MACRO;
                break;
            } else if (LatteMarkupTokenId.T_SYMBOL.equals(tokenId) || LatteMarkupTokenId.T_MACRO_START.equals(tokenId)) {
                result = LatteCompletionContext.MACRO;
                break;
            }
        } while (ts.movePrevious());
        return result;
    }

    private static boolean acceptTokenChains(final TokenSequence tokenSequence, final List<Object[]> tokenIdChains, final boolean movePrevious) {
        boolean result = false;
        for (Object[] tokenIDChain : tokenIdChains) {
            if (acceptTokenChain(tokenSequence, tokenIDChain, movePrevious)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private static boolean acceptTokenChain(final TokenSequence tokenSequence, final Object[] tokenIdChain, final boolean movePrevious) {
        int originalPosition = tokenSequence.offset();
        boolean accept = true;
        boolean moreTokens = movePrevious ? tokenSequence.movePrevious() : true;
        for (int i = tokenIdChain.length - 1; i >= 0; i--) {
            Object tokenId = tokenIdChain[i];
            if (!moreTokens) {
                accept = false;
                break;
            }
            if (tokenId instanceof LatteMarkupTokenId) {
                if (tokenSequence.token().id() == tokenId) {
                    moreTokens = tokenSequence.movePrevious();
                } else {
                    accept = false;
                    break;
                }
            } else if (tokenId instanceof ValuedTokenId) {
                ValuedTokenId valuedToken = (ValuedTokenId) tokenId;
                Token token = tokenSequence.token();
                if (token != null && valuedToken.getId().equals(token.id()) && token.text().equals(valuedToken.getValue())) {
                    moreTokens = tokenSequence.movePrevious();
                } else {
                    accept = false;
                    break;
                }
            } else {
                assert false : "Unsupported token type: " + tokenId.getClass().getName();
            }
        }
        tokenSequence.move(originalPosition);
        tokenSequence.moveNext();
        return accept;
    }

    private static enum ValuedTokenId {
        HELPER_TOKEN(LatteMarkupTokenId.T_CHAR, "|"), //NOI18N
        ITERATOR_TOKEN(LatteMarkupTokenId.T_VARIABLE, "$iterator"), //NOI18N
        OBJECT_ACCESS_TOKEN(LatteMarkupTokenId.T_CHAR, "->"), //NOI18N
        VARIABLE_TOKEN(LatteMarkupTokenId.T_CHAR, "$"); //NOI18N

        private final LatteMarkupTokenId id;
        private final String value;

        private ValuedTokenId(LatteMarkupTokenId id, String value) {
            this.id = id;
            this.value = value;
        }

        public LatteMarkupTokenId getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

    }

}
