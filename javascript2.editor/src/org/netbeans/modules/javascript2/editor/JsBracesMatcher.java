/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.List;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.lexer.api.JsTokenId;
import org.netbeans.modules.javascript2.lexer.api.LexUtilities;
import org.netbeans.spi.editor.bracesmatching.BraceContext;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Hejl
 */
// major portion copied from java
public class JsBracesMatcher implements BracesMatcher {

    private static final char [] PAIRS = new char [] { '(', ')', '[', ']', '{', '}' }; //NOI18N

    private static final JsTokenId [] PAIR_TOKEN_IDS = new JsTokenId [] {
        JsTokenId.BRACKET_LEFT_PAREN, JsTokenId.BRACKET_RIGHT_PAREN,
        JsTokenId.BRACKET_LEFT_BRACKET, JsTokenId.BRACKET_RIGHT_BRACKET,
        JsTokenId.BRACKET_LEFT_CURLY, JsTokenId.BRACKET_RIGHT_CURLY
    };

    private final MatcherContext context;

    private final Language<JsTokenId> language;

    private int originOffset;
    private char originChar;
    private char matchingChar;
    private boolean backward;
    private List<TokenSequence<?>> sequences;
    private boolean templateExp;

    public JsBracesMatcher(MatcherContext context, Language<JsTokenId> language) {
        this.context = context;
        this.language = language;
    }

    @Override
    public int [] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            templateExp = false;
            int endOffset = -1;

            TokenSequence<? extends JsTokenId> testSeq = LexUtilities.getJsPositionedSequence(
                    context.getDocument(), context.getSearchOffset());
            if (testSeq != null) {
                if (testSeq.token().id() != JsTokenId.TEMPLATE_EXP_BEGIN && context.isSearchingBackward()) {
                    if (!testSeq.movePrevious() && context.getSearchOffset() - 1 >= context.getLimitOffset()) {
                        testSeq = LexUtilities.getJsPositionedSequence(
                                context.getDocument(), context.getSearchOffset() - 1);
                    }
                }
            }
            if (testSeq != null && testSeq.token().id() == JsTokenId.TEMPLATE_EXP_BEGIN) {
                originOffset = testSeq.offset();
                endOffset = originOffset + testSeq.token().length();
                originChar = '{'; // NOI18N
                matchingChar = '}'; // NOI18N
                backward = false;
            } else {
                int[] origin = BracesMatcherSupport.findChar(
                        context.getDocument(),
                        context.getSearchOffset(),
                        context.getLimitOffset(),
                        PAIRS
                );
                if (origin != null) {
                    originOffset = origin[0];
                    endOffset = originOffset + 1;
                    originChar = PAIRS[origin[1]];
                    matchingChar = PAIRS[origin[1] + origin[2]];
                    backward = origin[2] < 0;
                }
            }

            if (endOffset > 0) {
                TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
                // to get it work, there should not be checked previous ts. it can be different. see issue #250521
                sequences = getEmbeddedTokenSequences(th, originOffset, false, language);

                if (!sequences.isEmpty()) {
                    // Check special tokens
                    TokenSequence<?> seq = sequences.get(sequences.size() - 1);
                    seq.move(originOffset);
                    if (seq.moveNext()) {
                        if (seq.token().id() == JsTokenId.BLOCK_COMMENT
                                || seq.token().id() == JsTokenId.DOC_COMMENT
                                || seq.token().id() == JsTokenId.LINE_COMMENT
                                // remove once we have a lagueage
                                || seq.token().id() == JsTokenId.REGEXP
                                || seq.token().id() == JsTokenId.STRING) {
                            return null;
                        }
                        if (seq.token().id() == JsTokenId.TEMPLATE_EXP_BEGIN
                                || seq.token().id() == JsTokenId.TEMPLATE_EXP_END) {
                            templateExp = true;
                        }
                    }
                }

                return new int [] { originOffset, endOffset };
            } else {
                return null;
            }
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    @Override
    public int [] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            if (sequences != null && !sequences.isEmpty()) {
                TokenSequence<?> seq = sequences.get(sequences.size() - 1);

                TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
                List<TokenSequence<?>> list;
                if (backward) {
                    list = th.tokenSequenceList(seq.languagePath(), 0, originOffset);
                } else {
                    int offset = originOffset + 1;
                    if (templateExp) {
                        offset++;
                    }
                    list = th.tokenSequenceList(seq.languagePath(), offset, context.getDocument().getLength());
                }

                JsTokenId originId = getTokenId(originChar);
                JsTokenId lookingForId = getTokenId(matchingChar);
                if (templateExp) {
                    if (originChar == '}') { // NOI18N
                        originId = JsTokenId.TEMPLATE_EXP_END;
                        lookingForId = JsTokenId.TEMPLATE_EXP_BEGIN;
                    } else {
                        originId = JsTokenId.TEMPLATE_EXP_BEGIN;
                        lookingForId = JsTokenId.TEMPLATE_EXP_END;
                    }
                }
                int counter = 0;

                for(TokenSequenceIterator tsi = new TokenSequenceIterator(list, backward); tsi.hasMore(); ) {
                    TokenSequence<?> sq = tsi.getSequence();

                    if (originId == sq.token().id()) {
                        counter++;
                    } else if (lookingForId == sq.token().id()) {
                        if (counter == 0) {
                            return new int [] { sq.offset(), sq.offset() + sq.token().length() };
                        } else {
                            counter--;
                        }
                    }
                }
            }

            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public static List<TokenSequence<?>> getEmbeddedTokenSequences(
        TokenHierarchy<?> th, int offset, boolean backwardBias, Language<?> language) {
        List<TokenSequence<?>> sequences = th.embeddedTokenSequences(offset, backwardBias);

        for (int i = sequences.size() - 1; i >= 0; i--) {
            TokenSequence<?> seq = sequences.get(i);
            if (seq.language() == language) {
                break;
            } else {
                sequences.remove(i);
            }
        }

        return sequences;
    }

    private JsTokenId getTokenId(char ch) {
        for(int i = 0; i < PAIRS.length; i++) {
            if (PAIRS[i] == ch) {
                return PAIR_TOKEN_IDS[i];
            }
        }
        return null;
    }



    @MimeRegistration(mimeType = JsTokenId.JAVASCRIPT_MIME_TYPE, service = BracesMatcherFactory.class, position=0)
    public static class JsBracesMatcherFactory implements BracesMatcherFactory {

        @Override
        public BracesMatcher createMatcher(MatcherContext context) {
            return new JsBracesMatcher(context, JsTokenId.javascriptLanguage());
        }

    }

    @MimeRegistration(mimeType = JsTokenId.JSON_MIME_TYPE, service = BracesMatcherFactory.class, position=0)
    public static class JsonBracesMatcherFactory implements BracesMatcherFactory {

        @Override
        public BracesMatcher createMatcher(MatcherContext context) {
            return new JsBracesMatcher(context, JsTokenId.jsonLanguage());
        }

    }
}
