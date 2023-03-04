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
package org.netbeans.modules.php.twig.editor.completion;

import java.util.Arrays;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.twig.editor.lexer.TwigBlockTokenId;
import org.netbeans.modules.php.twig.editor.lexer.TwigLexerUtils;
import org.netbeans.modules.php.twig.editor.lexer.TwigVariableTokenId;
import org.netbeans.modules.php.twig.editor.parsing.TwigParserResult;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class TwigCompletionContextFinder {

    public static enum CompletionContext {
        VARIABLE,
        BLOCK,
        FILTER,
        NONE,
        ALL;
    }

    private static enum ValuedTokenId {
        FILTER_PUNCTUATION_TOKEN_BLOCK(TwigBlockTokenId.T_TWIG_PUNCTUATION, "|"), //NOI18N
        FILTER_PUNCTUATION_TOKEN_VAR_IABLE(TwigVariableTokenId.T_TWIG_PUNCTUATION, "|"), //NOI18N
        FILTER_TAG_TOKEN(TwigBlockTokenId.T_TWIG_TAG, "filter"); //NOI18N

        private final TokenId id;
        private final String value;

        private ValuedTokenId(TokenId id, String value) {
            this.id = id;
            this.value = value;
        }

        public TokenId getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

    }

    private static final List<Object[]> FILTER_TOKEN_CHAINS = Arrays.asList(
            new Object[]{ValuedTokenId.FILTER_PUNCTUATION_TOKEN_BLOCK},
            new Object[]{ValuedTokenId.FILTER_PUNCTUATION_TOKEN_BLOCK, TwigBlockTokenId.T_TWIG_NAME},
            new Object[]{ValuedTokenId.FILTER_PUNCTUATION_TOKEN_VAR_IABLE},
            new Object[]{ValuedTokenId.FILTER_PUNCTUATION_TOKEN_VAR_IABLE, TwigVariableTokenId.T_TWIG_NAME},
            new Object[]{ValuedTokenId.FILTER_TAG_TOKEN},
            new Object[]{ValuedTokenId.FILTER_TAG_TOKEN, TwigBlockTokenId.T_TWIG_WHITESPACE},
            new Object[]{ValuedTokenId.FILTER_TAG_TOKEN, TwigBlockTokenId.T_TWIG_WHITESPACE, TwigBlockTokenId.T_TWIG_NAME}
    );

    private TwigCompletionContextFinder() {
    }

    public static CompletionContext find(final TwigParserResult parserResult, final int offset) {
        assert parserResult != null;
        CompletionContext result = CompletionContext.NONE;
        TokenSequence<? extends TokenId> tokenSequence = TwigLexerUtils.getTwigMarkupTokenSequence(parserResult.getSnapshot(), offset);
        if (tokenSequence != null) {
            tokenSequence.move(offset);
            if (!tokenSequence.moveNext()) {
                tokenSequence.movePrevious();
            }
            result = findContext(tokenSequence);
        }
        return result;
    }

    private static CompletionContext findContext(TokenSequence<? extends TokenId> tokenSequence) {
        CompletionContext result = CompletionContext.ALL;
        do {
            Token<? extends TokenId> token = tokenSequence.token();
            if (token == null) {
                break;
            }
            TokenId tokenId = token.id();
            if (TwigBlockTokenId.T_TWIG_OTHER.equals(tokenId) || TwigVariableTokenId.T_TWIG_OTHER.equals(tokenId)) {
                result = CompletionContext.NONE;
                break;
            } else if (acceptTokenChains(tokenSequence, FILTER_TOKEN_CHAINS, true)) {
                result = CompletionContext.FILTER;
                break;
            } else if (tokenId instanceof TwigBlockTokenId) {
                result = CompletionContext.BLOCK;
                break;
            } else if (tokenId instanceof TwigVariableTokenId) {
                result = CompletionContext.VARIABLE;
                break;
            }
        } while (tokenSequence.movePrevious());
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
            if (tokenId instanceof TwigBlockTokenId || tokenId instanceof TwigVariableTokenId) {
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

}
