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
package org.netbeans.modules.php.twig.editor.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public final class TwigLexerUtils {

    private TwigLexerUtils() {
    }

    public static TokenSequence<? extends TokenId> getTwigMarkupTokenSequence(final Snapshot snapshot, final int offset) {
        TokenSequence<? extends TwigBlockTokenId> twigBlockTokenSequence = getTokenSequence(snapshot.getTokenHierarchy(), offset, TwigBlockTokenId.language());
        return twigBlockTokenSequence == null ? getTokenSequence(snapshot.getTokenHierarchy(), offset, TwigVariableTokenId.language()) : twigBlockTokenSequence;
    }

    public static TokenSequence<? extends TokenId> getTwigMarkupTokenSequence(final Document document, final int offset) {
        TokenSequence<? extends TwigBlockTokenId> twigBlockTokenSequence = getTwigBlockTokenSequence(document, offset);
        return twigBlockTokenSequence == null ? getTwigVariableTokenSequence(document, offset) : twigBlockTokenSequence;
    }

    public static TokenSequence<? extends TwigBlockTokenId> getTwigBlockTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, TwigBlockTokenId.language());
    }

    public static TokenSequence<? extends TwigVariableTokenId> getTwigVariableTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, TwigVariableTokenId.language());
    }

    public static TokenSequence<? extends TwigTopTokenId> getTwigTokenSequence(final Snapshot snapshot, final int offset) {
        return getTokenSequence(snapshot.getTokenHierarchy(), offset, TwigTopTokenId.language());
    }

    public static TokenSequence<? extends TwigTopTokenId> getTwigTokenSequence(final Document document, final int offset) {
        TokenHierarchy<Document> th = TokenHierarchy.get(document);
        return getTokenSequence(th, offset, TwigTopTokenId.language());
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

    public static List<OffsetRange> findForwardMatching(TokenSequence<? extends TwigTopTokenId> topTs, TwigTokenText start, TwigTokenText end) {
        return findForwardMatching(topTs, start, end, Collections.<TwigTokenText>emptyList());
    }

    public static List<OffsetRange> findForwardMatching(
            TokenSequence<? extends TwigTopTokenId> topTs,
            TwigTokenText start,
            TwigTokenText end,
            List<TwigTokenText> middle) {
        List<OffsetRange> result = new ArrayList<>();
        topTs.moveNext();
        int originalOffset = topTs.offset();
        int balance = 1;
        while (topTs.moveNext()) {
            Token<? extends TwigTopTokenId> token = topTs.token();
            if (token != null && (token.id() == TwigTopTokenId.T_TWIG_BLOCK || token.id() == TwigTopTokenId.T_TWIG_VAR)) {
                TokenSequence<TwigBlockTokenId> markupTs = topTs.embedded(TwigBlockTokenId.language());
                if (markupTs != null) {
                    markupTs.moveNext();
                    while (markupTs.moveNext()) {
                        Token<? extends TwigBlockTokenId> markupToken = markupTs.token();
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

    private static boolean matchesToken(List<TwigTokenText> middle, Token<? extends TwigBlockTokenId> markupToken) {
        boolean result = false;
        for (TwigTokenText twigTokenText : middle) {
            if (twigTokenText.matches(markupToken)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public static List<OffsetRange> findBackwardMatching(
            TokenSequence<? extends TwigTopTokenId> topTs,
            TwigTokenText start,
            TwigTokenText end,
            List<TwigTokenText> middle) {
        List<OffsetRange> result = new ArrayList<>();
        topTs.movePrevious();
        int originalOffset = topTs.offset();
        int balance = 1;
        while (topTs.movePrevious()) {
            Token<? extends TwigTopTokenId> token = topTs.token();
            if (token != null && (token.id() == TwigTopTokenId.T_TWIG_BLOCK || token.id() == TwigTopTokenId.T_TWIG_VAR)) {
                TokenSequence<TwigBlockTokenId> markupTs = topTs.embedded(TwigBlockTokenId.language());
                if (markupTs != null) {
                    markupTs.moveEnd();
                    while (markupTs.movePrevious()) {
                        Token<? extends TwigBlockTokenId> markupToken = markupTs.token();
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

    public static List<OffsetRange> findBackwardMatching(TokenSequence<? extends TwigTopTokenId> topTs, TwigTokenText start, TwigTokenText end) {
        return findBackwardMatching(topTs, start, end, Collections.<TwigTokenText>emptyList());
    }

    public static boolean textEquals(CharSequence text1, char... text2) {
        int len = text1.length();
        if (len == text2.length) {
            for (int i = len - 1; i >= 0; i--) {
                if (text1.charAt(i) != text2[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean textStartWith(CharSequence text1, char text2) {
        int len = text1.length();
        if (len > 0) {
            return text1.charAt(0) == text2;
        }
        return false;
    }

    public static int getTokenBalance(BaseDocument doc, char open, char close, int offset) throws BadLocationException {
        TokenSequence<? extends TokenId> ts = TwigLexerUtils.getTwigMarkupTokenSequence(doc, offset);
        if (ts == null) {
            return 0;
        }

        int balance = 0;
        ts.move(offset);
        // check next tokens
        while (ts.moveNext()) {
            TokenId id = ts.token().id();
            if (id == TwigVariableTokenId.T_TWIG_PUNCTUATION || id == TwigBlockTokenId.T_TWIG_PUNCTUATION) {
                if (TwigLexerUtils.textEquals(ts.token().text(), close)) {
                    balance--;
                } else if (TwigLexerUtils.textEquals(ts.token().text(), open)) {
                    break;
                }
            }
        }

        // check previous tokens
        ts.move(offset);
        while (ts.movePrevious()) {
            TokenId id = ts.token().id();
            if (id == TwigVariableTokenId.T_TWIG_PUNCTUATION || id == TwigBlockTokenId.T_TWIG_PUNCTUATION) {
                if (TwigLexerUtils.textEquals(ts.token().text(), close)) {
                    balance--;
                } else if (TwigLexerUtils.textEquals(ts.token().text(), open)) {
                    balance++;
                }
                if (balance > 0) {
                    break;
                }
            }
        }
        return balance;
    }

    public interface TwigTokenText {
        TwigTokenText NONE = new TwigTokenText() {

            @Override
            public boolean matches(Token<? extends TwigBlockTokenId> token) {
                return false;
            }
        };

        boolean matches(Token<? extends TwigBlockTokenId> token);
    }

    public static final class TwigTokenTextImpl implements TwigTokenText {
        private final TwigBlockTokenId tokenId;
        private final String tokenText;

        public static TwigTokenText create(TwigBlockTokenId tokenId, String tokenText) {
            return new TwigTokenTextImpl(tokenId, tokenText);
        }

        private TwigTokenTextImpl(TwigBlockTokenId tokenId, String tokenText) {
            this.tokenId = tokenId;
            this.tokenText = tokenText;
        }

        @Override
        public boolean matches(Token<? extends TwigBlockTokenId> token) {
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
            final TwigTokenTextImpl other = (TwigTokenTextImpl) obj;
            if (this.tokenId != other.tokenId) {
                return false;
            }
            return Objects.equals(this.tokenText, other.tokenText);
        }

        @Override
        public String toString() {
            return "TwigTokenText{" + "tokenId=" + tokenId + ", tokenText=" + tokenText + '}';
        }

    }

}
