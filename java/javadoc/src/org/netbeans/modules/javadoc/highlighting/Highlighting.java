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

package org.netbeans.modules.javadoc.highlighting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.java.lexer.JavadocTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 * @author Jan Becicka
 */
public class Highlighting extends AbstractHighlightsContainer implements TokenHierarchyListener {

    private static final Logger LOG = Logger.getLogger(Highlighting.class.getName());
    private static final String WS = " \t\n"; // NOI18N
    private static final String JAPANESE_PERIOD = "\u3002"; // 。 NOI18N
    private static final List<String> PERIODS = Arrays.asList(
            JAPANESE_PERIOD
    );

    public static final String LAYER_ID = "org.netbeans.modules.javadoc.highlighting"; //NOI18N

    private final AttributeSet fontColor;

    private final Document document;
    private TokenHierarchy<? extends Document> hierarchy = null;
    private final AtomicLong version = new AtomicLong();

    /** Creates a new instance of Highlighting */
    public Highlighting(Document doc) {
        AttributeSet firstLineFontColor = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class).getTokenFontColors("javadoc-first-sentence"); //NOI18N
        AttributeSet commentFontColor = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class).getTokenFontColors("comment"); //NOI18N
        if(firstLineFontColor != null && commentFontColor != null) {
            Collection<Object> attrs = new LinkedList<>();
            for (Enumeration<?> e = firstLineFontColor.getAttributeNames(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                Object value = firstLineFontColor.getAttribute(key);

                if (!commentFontColor.containsAttribute(key, value)) {
                    attrs.add(key);
                    attrs.add(value);
                }
            }
            fontColor = AttributesUtilities.createImmutable(attrs.toArray());
        } else {
            fontColor = AttributesUtilities.createImmutable();
            LOG.warning("FontColorSettings for javadoc-first-sentence or comment are not available."); //NOI18N
        }
        this.document = doc;
        hierarchy = TokenHierarchy.get(document);
        if (hierarchy != null) {
            hierarchy.addTokenHierarchyListener(WeakListeners.create(TokenHierarchyListener.class, this, hierarchy));
        }
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized(this) {
            if (hierarchy.isActive()) {
                return new HSImpl(version.get(), hierarchy, startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }

    // ----------------------------------------------------------------------
    //  TokenHierarchyListener implementation
    // ----------------------------------------------------------------------

    @Override
    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        TokenChange<?> tc = evt.tokenChange();
        int affectedArea [] = null;

        TokenSequence<? extends TokenId> seq = tc.currentTokenSequence();
        if (seq.language().equals(JavadocTokenId.language())) {
            // Change inside javadoc
            int [] firstSentence = findFirstSentence(seq);
            if (firstSentence != null) {
                if (tc.offset() <= firstSentence[1]) {
                    // Change before the end of the first sentence
                    affectedArea = firstSentence;
                }
            } else {
                // XXX: need the embedding token (i.e. JavaTokenId.JAVADOC_COMMENT*)
                // and fire a change in its whole area
                affectedArea = new int [] { tc.offset(), evt.affectedEndOffset() };
            }
        } else {
            // The change may or may not involve javadoc, so reset everyting.
            // It would be more efficient to traverse the changed area and
            // find out whether it really involves javadoc or not.
            affectedArea = new int [] { tc.offset(), evt.affectedEndOffset() };
        }

        if (affectedArea != null) {
            version.incrementAndGet();
            fireHighlightsChange(affectedArea[0], affectedArea[1]);
        }
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private int [] findFirstSentence(TokenSequence<? extends TokenId> seq) {
        seq.moveStart();
        if (seq.moveNext()) {
            int start = seq.offset();
            do {
                String period = null;
                int indexOfPeriod = -1;
                for (String p : PERIODS) {
                    int index = TokenUtilities.indexOf(seq.token().text(), p);
                    if (index != -1) {
                        if (indexOfPeriod == -1 || index < indexOfPeriod) {
                            indexOfPeriod = index;
                            period = p;
                        }
                    }
                }

                if (seq.token().id() == JavadocTokenId.DOT) {
                    if (seq.moveNext()) {
                        if (isWhiteSpace(seq.token())) {
                            return new int [] { start, seq.offset()};
                        }
                        seq.movePrevious();
                     }
                } else if (period != null && indexOfPeriod != -1) {
                    // NETBEANS-791
                    int offset = indexOfPeriod + 1;
                    while (offset < seq.token().length()
                            && isPeriod(seq.token().text().subSequence(offset, offset + 1))) {
                        // e.g. 。。。
                        offset++;
                    }
                    return new int[]{start, seq.offset() + offset};
                } else if (seq.token().id() == JavadocTokenId.TAG) {
                    if (seq.movePrevious()) {
                        if (!seq.token().text().toString().trim().endsWith("{")) {
                            //not an inline tag
                            return new int [] { start, seq.offset()};
                        }
                    }
                    seq.moveNext();
                }
            } while (seq.moveNext());
        }
        return null;
    }

    private static boolean isWhiteSpace(Token<? extends TokenId> token) {
        if (token == null || token.id() != JavadocTokenId.OTHER_TEXT) {
            return false;
        }
        return WS.indexOf(token.text().charAt(0)) >= 0;
    }

    private static boolean isPeriod(CharSequence cs) {
        return PERIODS.stream().anyMatch(period -> TokenUtilities.equals(cs, period));
    }

    private final class HSImpl implements HighlightsSequence {

        private final long version;
        private final TokenHierarchy<? extends Document> scanner;
        private List<TokenSequence<? extends TokenId>> sequences;
        private final int startOffset;
        private final int endOffset;

        private List<Integer> lines = null;
        private int linesIdx = -1;

        public HSImpl(long version, TokenHierarchy<? extends Document> scanner, int startOffset, int endOffset) {
            this.version = version;
            this.scanner = scanner;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.sequences = null;
        }

        @Override
        public boolean moveNext() {
            synchronized (Highlighting.this) {
                checkVersion();

                if (sequences == null) {
                    // initialize
                    TokenSequence<?> tokenSequence = scanner.tokenSequence();
                    if (tokenSequence==null) {
                        //#199027
                        //inactive hierarchy, no next
                        return false;
                    }
                    TokenSequence<?> seq = tokenSequence.subSequence(startOffset, endOffset);
                    sequences = new ArrayList<>();
                    sequences.add(seq);
                }

                if (lines != null) {
                    if (linesIdx + 2 < lines.size()) {
                        linesIdx += 2;
                        return true;
                    }

                    lines = null;
                    linesIdx = -1;
                }

                while (!sequences.isEmpty()) {
                    TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);

                    if (seq.language().equals(JavadocTokenId.language())) {
                        int [] firstSentence = findFirstSentence(seq);
                        sequences.remove(sequences.size() - 1);

                        if (firstSentence != null) {
                            lines = splitByLines(firstSentence[0], firstSentence[1]);
                            if (lines != null) {
                                linesIdx = 0;
                                return true;
                            }
                        }
                    } else {
                        boolean hasNextToken;

                        while (true == (hasNextToken = seq.moveNext())) {
                            TokenSequence<?> embeddedSeq = seq.embedded();
                            if (embeddedSeq != null) {
                                sequences.add(sequences.size(), embeddedSeq);
                                break;
                            }
                        }

                        if (!hasNextToken) {
                            sequences.remove(sequences.size() - 1);
                        }
                    }
                }

                return false;
            }
        }

        @Override
        public int getStartOffset() {
            synchronized (Highlighting.this) {
                checkVersion();

                if (sequences == null) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                }

                if (lines != null) {
                    return lines.get(linesIdx);
                } else {
                    throw new NoSuchElementException();
                }
            }
        }

        @Override
        public int getEndOffset() {
            synchronized (Highlighting.this) {
                checkVersion();

                if (sequences == null) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                }

                if (lines != null) {
                    return lines.get(linesIdx + 1);
                } else {
                    throw new NoSuchElementException();
                }
            }
        }

        @Override
        public AttributeSet getAttributes() {
            synchronized (Highlighting.this) {
                checkVersion();

                if (sequences == null) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                }

                if (lines != null) {
                    return fontColor;
                } else {
                    throw new NoSuchElementException();
                }
            }
        }

        private void checkVersion() {
            if (this.version != Highlighting.this.version.get()) {
                throw new ConcurrentModificationException();
            }
        }

        private List<Integer> splitByLines(int sentenceStart, int sentenceEnd) {
            ArrayList<Integer> lines = new ArrayList<>();
            int offset = sentenceStart;

            try {
                while (offset < sentenceEnd) {
                    Element lineElement = document.getDefaultRootElement().getElement(
                        document.getDefaultRootElement().getElementIndex(offset));

                    int rowStart = offset == sentenceStart ? offset : lineElement.getStartOffset();
                    int rowEnd = lineElement.getEndOffset();

                    String line = document.getText(rowStart, rowEnd - rowStart);
                    int idx = 0;
                    while (idx < line.length() &&
                        (line.charAt(idx) == ' ' ||
                        line.charAt(idx) == '\t' ||
                        line.charAt(idx) == '*'  ||
                        line.charAt(idx) == '/'))
                    {
                        if (line.charAt(idx) == '/') {
                            if (line.length() > idx + 2 &&
                                line.charAt(idx + 1) == '/' &&
                                line.charAt(idx + 2) == '/') {
                                idx += 3;
                                continue;
                            }
                            break;
                        }
                        idx++;
                    }

                    if (rowStart + idx < rowEnd) {
                        lines.add(rowStart + idx);
                        lines.add(Math.min(rowEnd, sentenceEnd));
                    }

                    offset = rowEnd + 1;
                }
            } catch (BadLocationException e) {
                LOG.log(Level.WARNING, "Can't determine javadoc first sentence", e);
            }

            return lines.isEmpty() ? null : lines;
        }
    } // End of HSImpl class
}
