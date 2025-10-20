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

package org.netbeans.modules.php.smarty.editor.coloring;

import java.awt.Color;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.php.smarty.editor.TplDataLoader;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 * Implementation of Highlighting SPI creating coloured background
 * for SMARTY templates.
 *
 * @author Martin Fousek
 * created according to EmbeddingHighlightsContainer
 */
public class EmbeddingHighlightsContainer extends AbstractHighlightsContainer implements TokenHierarchyListener {

    private static final Logger LOG = Logger.getLogger(EmbeddingHighlightsContainer.class.getName());

    private static final String TPL_BACKGROUND_TOKEN_NAME = "smarty"; //NOI18N
    private static final String TPL_INNER_MIME_TYPE = "text/x-tpl-inner"; //NOI18N

    private final AttributeSet tplBackground;
    private final Document document;
    private TokenHierarchy<? extends Document> hierarchy = null;
    private long version = 0;

    EmbeddingHighlightsContainer(Document document) {
        this.document = document;

        //try load the background from tpl settings
        FontColorSettings fcs = MimeLookup.getLookup(TplDataLoader.MIME_TYPE).lookup(FontColorSettings.class);
        Color tplBG = null;
        if (fcs != null) {
            tplBG = getColoring(fcs, TPL_BACKGROUND_TOKEN_NAME);
        }

        tplBackground = tplBG == null ? SimpleAttributeSet.EMPTY : AttributesUtilities.createImmutable(
            StyleConstants.Background, tplBG, ATTR_EXTENDS_EOL, Boolean.TRUE);
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (tplBackground != null) {
                if (hierarchy == null) {
                    hierarchy = TokenHierarchy.get(document);
                    if (hierarchy != null) {
                        hierarchy.addTokenHierarchyListener(WeakListeners.create(TokenHierarchyListener.class, this, hierarchy));
                    }
                }

                if (hierarchy != null) {
                    return new Highlights(version, hierarchy, startOffset, endOffset);
                }
            }
            return HighlightsSequence.EMPTY;
        }
    }

    // ----------------------------------------------------------------------
    //  TokenHierarchyListener implementation
    // ----------------------------------------------------------------------

    @Override
    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        synchronized (this) {
            version++;
        }

        fireHighlightsChange(evt.affectedStartOffset(), evt.affectedEndOffset());
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private static Color getColoring(FontColorSettings fcs, String tokenName) {
        AttributeSet as = fcs.getTokenFontColors(tokenName);
        if (as != null) {
            return (Color) as.getAttribute(StyleConstants.Background); //NOI18N
        }
        return null;
    }

    private class Highlights implements HighlightsSequence {

        private final long version;
        private final TokenHierarchy<? extends Document> scanner;
        private final int startOffsetBoundary;
        private final int endOffsetBoundary;

        private List<TokenSequence<?>> tokenSequenceList = null;
        private int startOffset;
        private int endOffset;
        private int realEndOffset;
        private AttributeSet attributeSet;
        private boolean finished = false;

        private Highlights(long version, TokenHierarchy<? extends Document> scanner, int startOffset, int endOffset) {
            this.version = version;
            this.scanner = scanner;
            this.startOffsetBoundary = startOffset;
            this.endOffsetBoundary = endOffset;
        }

        private boolean _moveNext() {
            if (tokenSequenceList == null) {
                // initialize
                this.startOffset = startOffsetBoundary;
                this.endOffset = startOffsetBoundary;
                this.realEndOffset = startOffsetBoundary;

                String mimeType = (String) document.getProperty ("mimeType"); //NOI18N
                Language<?> language = Language.find(mimeType);
                if (language != null) {
                    //get html token sequence list
                    LanguagePath topLevelLanguagePath = LanguagePath.get(language);
                    tokenSequenceList = scanner.tokenSequenceList(topLevelLanguagePath, startOffsetBoundary, endOffsetBoundary);
                } else {
                    LOG.log(Level.WARNING, "Language " + mimeType + " obtained from the document mimeType property cannot be found!"); //NOI18N
                }
            }

            if (tokenSequenceList != null) {
                for (TokenSequence tokenSequence : tokenSequenceList) {
                    assert tokenSequence.language().mimeType().equals(TplDataLoader.MIME_TYPE);
                    tokenSequence.move(realEndOffset);
                    while (tokenSequence.moveNext() && tokenSequence.offset() < endOffsetBoundary) {
                        TokenSequence eTokenSequence = tokenSequence.embedded();

                        if (eTokenSequence == null || !eTokenSequence.moveNext()) {
                            continue;
                        }
                        String embeddedMimeType = eTokenSequence.language().mimeType();
                        if (TPL_INNER_MIME_TYPE.equals(embeddedMimeType)) {
                            try {
                                startOffset = eTokenSequence.offset();
                                do {
                                    endOffset = eTokenSequence.offset() + eTokenSequence.token().length();
                                } while (eTokenSequence.moveNext());

                                realEndOffset = endOffset > realEndOffset ? endOffset : realEndOffset + 1;
                                int startLO = Utilities.getLineOffset((BaseDocument) document, startOffset);
                                int endLO = Utilities.getLineOffset((BaseDocument) document, endOffset);
                                if (startLO != endLO) {
                                    //not just one line block - test boundaries
                                    if ((LineDocumentUtils.getPreviousNonWhitespace((BaseDocument) document, LineDocumentUtils.getLineEndOffset((BaseDocument) document, startOffset)) + 1) == startOffset) {
                                        //just <script-style> tag on the first line -> move start to next line
                                        startOffset = Utilities.getRowStartFromLineOffset((BaseDocument) document, startLO + 1);
                                    }
                                    if (LineDocumentUtils.getNextNonWhitespace((BaseDocument) document, Utilities.getRowStartFromLineOffset((BaseDocument) document, endLO)) == endOffset) {
                                        //just </script-style> tag on the last line -> move block end to previous line end
                                        endOffset = Utilities.getRowStartFromLineOffset((BaseDocument) document, endLO);
                                    }
                                }

                                attributeSet = tplBackground;
                                if (attributeSet != null) {
                                    return true;
                                }
                            } catch (BadLocationException ex) {
                                LOG.log(Level.INFO, "An error occured when creating coloured background for TPL.", ex); //NOI18N
                            }
                        }
                    }
                }
            }

            return false;
        }

        @Override
        public boolean moveNext() {
            synchronized (EmbeddingHighlightsContainer.this) {
                if (checkVersion()) {
                    if (_moveNext()) {
                        return true;
                    }
                }
            }

            finished = true;
            return false;
        }

        @Override
        public int getStartOffset() {
            synchronized (EmbeddingHighlightsContainer.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert tokenSequenceList != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return startOffset;
                }
            }
        }

        @Override
        public int getEndOffset() {
            synchronized (EmbeddingHighlightsContainer.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert tokenSequenceList != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return endOffset;
                }
            }
        }

        @Override
        public AttributeSet getAttributes() {
            synchronized (EmbeddingHighlightsContainer.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert tokenSequenceList != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return attributeSet;
                }
            }
        }

        private boolean checkVersion() {
            synchronized(EmbeddingHighlightsContainer.this) {
                return this.version == EmbeddingHighlightsContainer.this.version;
            }
        }
    }
}
