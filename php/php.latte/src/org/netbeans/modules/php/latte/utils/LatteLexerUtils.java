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
package org.netbeans.modules.php.latte.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.php.latte.lexer.LatteMarkupTokenId;
import org.netbeans.modules.php.latte.lexer.LatteTopTokenId;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class LatteLexerUtils {

    private LatteLexerUtils() {
    }

    public static LanguagePath fetchLanguagePath(TokenHierarchy<?> tokenHierarchy, Language<?> language) {
        LanguagePath result = null;
        for (LanguagePath languagePath : tokenHierarchy.languagePaths()) {
            if (languagePath.endsWith(LanguagePath.get(language))) {
                result = languagePath;
                break;
            }
        }
        return result;
    }

    public static TokenSequence<? extends LatteMarkupTokenId> getLatteMarkupTokenSequence(final Snapshot snapshot, final int offset) {
        return getTokenSequence(snapshot.getTokenHierarchy(), offset, LatteMarkupTokenId.language());
    }

    public static TokenSequence<? extends LatteMarkupTokenId> getLatteMarkupTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, LatteMarkupTokenId.language());
    }

    public static TokenSequence<? extends LatteTopTokenId> getLatteTopTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, LatteTopTokenId.language());
    }

    public static <L> TokenSequence<? extends L> getTokenSequence(final TokenHierarchy<?> th, final int offset, final Language<? extends L> language) {
        TokenSequence<? extends L> ts = th.tokenSequence(language);
        if (ts == null) {
            List<TokenSequence<?>> list = th.embeddedTokenSequences(offset, true);
            for (TokenSequence t : list) {
                if (t.language() == language) {
                    ts = t;
                    break;
                }
            }
            if (ts == null) {
                list = th.embeddedTokenSequences(offset, false);
                for (TokenSequence t : list) {
                    if (t.language() == language) {
                        ts = t;
                        break;
                    }
                }
            }
        }
        return ts;
    }

    public static List<OffsetRange> findForwardMatching(TokenSequence<? extends LatteTopTokenId> topTs, LatteTokenText start, LatteTokenText end) {
        return findForwardMatching(topTs, start, end, Collections.<LatteTokenText>emptyList());
    }

    public static List<OffsetRange> findForwardMatching(
            TokenSequence<? extends LatteTopTokenId> topTs,
            LatteTokenText start,
            LatteTokenText end,
            List<LatteTokenText> middle) {
        List<OffsetRange> result = new ArrayList<>();
        topTs.moveNext();
        int originalOffset = topTs.offset();
        int balance = 1;
        while (topTs.moveNext()) {
            Token<? extends LatteTopTokenId> token = topTs.token();
            if (token != null && (token.id() == LatteTopTokenId.T_LATTE)) {
                TokenSequence<LatteMarkupTokenId> markupTs = topTs.embedded(LatteMarkupTokenId.language());
                if (markupTs != null) {
                    while (markupTs.moveNext()) {
                        Token<? extends LatteMarkupTokenId> markupToken = markupTs.token();
                        if (start.matches(markupToken)) {
                            balance++;
                        } else if (end.matches(markupToken)) {
                            balance--;
                            if (balance == 0) {
                                result.add(new OffsetRange(markupTs.offset(), markupTs.offset() + markupToken.length()));
                                break;
                            }
                        } else if (matchesToken(middle, markupToken)) {
                            if (balance == 1) {
                                result.add(new OffsetRange(markupTs.offset(), markupTs.offset() + markupToken.length()));
                                break;
                            }
                        }
                    }
                    if (balance == 0) {
                        break;
                    }
                }
            }
        }
        topTs.move(originalOffset);
        return result;
    }

    public static List<OffsetRange> findBackwardMatching(TokenSequence<? extends LatteTopTokenId> topTs, LatteTokenText start, LatteTokenText end) {
        return findBackwardMatching(topTs, start, end, Collections.<LatteTokenText>emptyList());
    }

    public static List<OffsetRange> findBackwardMatching(
            TokenSequence<? extends LatteTopTokenId> topTs,
            LatteTokenText start,
            LatteTokenText end,
            List<LatteTokenText> middle) {
        List<OffsetRange> result = new ArrayList<>();
        topTs.movePrevious();
        int originalOffset = topTs.offset();
        int balance = 1;
        while (topTs.movePrevious()) {
            Token<? extends LatteTopTokenId> token = topTs.token();
            if (token != null && (token.id() == LatteTopTokenId.T_LATTE)) {
                TokenSequence<LatteMarkupTokenId> markupTs = topTs.embedded(LatteMarkupTokenId.language());
                if (markupTs != null) {
                    markupTs.moveEnd();
                    while (markupTs.movePrevious()) {
                        Token<? extends LatteMarkupTokenId> markupToken = markupTs.token();
                        if (start.matches(markupToken)) {
                            balance++;
                        } else if (end.matches(markupToken)) {
                            balance--;
                            if (balance == 0) {
                                result.add(new OffsetRange(markupTs.offset(), markupTs.offset() + markupToken.length()));
                                break;
                            }
                        } else if (matchesToken(middle, markupToken)) {
                            if (balance == 1) {
                                result.add(new OffsetRange(markupTs.offset(), markupTs.offset() + markupToken.length()));
                                break;
                            }
                        }
                    }
                    if (balance == 0) {
                        break;
                    }
                }
            }
        }
        topTs.move(originalOffset);
        return result;
    }

    private static boolean matchesToken(List<LatteTokenText> middle, Token<? extends LatteMarkupTokenId> markupToken) {
        boolean result = false;
        for (LatteTokenText twigTokenText : middle) {
            if (twigTokenText.matches(markupToken)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public interface LatteTokenText {
        LatteTokenText NONE = new LatteTokenText() {

            @Override
            public boolean matches(Token<? extends LatteMarkupTokenId> token) {
                return false;
            }
        };

        boolean matches(Token<? extends LatteMarkupTokenId> token);
    }

    public static final class LatteTokenTextImpl implements LatteTokenText {
        private final LatteMarkupTokenId tokenId;
        private final String tokenText;

        public static LatteTokenText create(LatteMarkupTokenId tokenId, String tokenText) {
            return new LatteTokenTextImpl(tokenId, tokenText);
        }

        private LatteTokenTextImpl(LatteMarkupTokenId tokenId, String tokenText) {
            this.tokenId = tokenId;
            this.tokenText = tokenText;
        }

        @Override
        public boolean matches(Token<? extends LatteMarkupTokenId> token) {
            return token != null && token.id() == tokenId && tokenText.equals(token.text().toString());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + Objects.hashCode(this.tokenId);
            hash = 71 * hash + Objects.hashCode(this.tokenText);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LatteTokenTextImpl other = (LatteTokenTextImpl) obj;
            if (this.tokenId != other.tokenId) {
                return false;
            }
            return Objects.equals(this.tokenText, other.tokenText);
        }

        @Override
        public String toString() {
            return "LatteTokenText{" + "tokenId=" + tokenId + ", tokenText=" + tokenText + '}'; //NOI18N
        }

    }

}
