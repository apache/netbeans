/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
