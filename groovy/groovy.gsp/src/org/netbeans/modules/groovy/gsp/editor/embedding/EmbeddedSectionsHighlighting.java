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

package org.netbeans.modules.groovy.gsp.editor.embedding;

import java.awt.Color;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.groovy.gsp.lexer.GspLexerLanguage;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 * Implementation of Highlighting SPI creating coloured background
 * for embedded java sections.
 *
 * @author Marek Fukala
 */
public class EmbeddedSectionsHighlighting extends AbstractHighlightsContainer implements TokenHierarchyListener {

    private static final Logger LOG = Logger.getLogger(EmbeddedSectionsHighlighting.class.getName());
    
    private final Document document;
    private final AttributeSet groovyBackground;
    private TokenHierarchy<? extends Document> hierarchy = null;
    private long version = 0;

    EmbeddedSectionsHighlighting(Document document) {
        this.document = document;
        
        // load the background color for the embedding token
        AttributeSet attribs = null;
        String mimeType = (String) document.getProperty("mimeType"); //NOI18N
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        if (fcs != null) {
            Color jsBC = getColoring(fcs, GspTokenId.GSTRING_CONTENT.primaryCategory());
            if (jsBC != null) {
                 attribs = AttributesUtilities.createImmutable(
                    StyleConstants.Background, jsBC, 
                    ATTR_EXTENDS_EOL, Boolean.TRUE);
            }
        }
        groovyBackground = attribs;
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (groovyBackground != null) {
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

    @Override
    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        synchronized (this) {
            version++;
        }
        
        fireHighlightsChange(evt.affectedStartOffset(), evt.affectedEndOffset());
    }
    
    private static Color getColoring(FontColorSettings fcs, String tokenName) {
        AttributeSet as = fcs.getTokenFontColors(tokenName);
        if (as != null) {
            return (Color) as.getAttribute(StyleConstants.Background); //NOI18N
        }
        return null;
    }
    
    private static boolean isWhitespace(Document document, int startOffset, int endOffset) throws BadLocationException {
        CharSequence chars = DocumentUtilities.getText(document, startOffset, endOffset - startOffset);
        for(int i = 0; i < chars.length(); i++) {
            if (!Character.isWhitespace(chars.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    private class Highlights implements HighlightsSequence {

        private final long version;
        private final TokenHierarchy<?> scanner;
        private final int startOffset;
        private final int endOffset;

        private TokenSequence<GspTokenId> sequence = null;
        private int sectionStart = -1;
        private int sectionEnd = -1;
        private boolean finished = false;
        
        private Highlights(long version, TokenHierarchy<?> scanner, int startOffset, int endOffset) {
            this.version = version;
            this.scanner = scanner;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        @Override
        public boolean moveNext() {
            synchronized (EmbeddedSectionsHighlighting.this) {
                if (checkVersion()) {
                    if (sequence == null) {
                        sequence = scanner.tokenSequence(GspLexerLanguage.getLanguage());

                        if (sequence == null) {
                            sectionStart = -1;
                            sectionEnd = -1;
                            finished = true;

                            return false;
                        }

                        sequence.move(startOffset);
                    }

                    int delimiterSize = 0;
                    while (sequence.moveNext() && sequence.offset() < endOffset) {
                        if (sequence.token().id().isDelimiter()) {
                            // opening delimiters can have different lenght
                            delimiterSize = sequence.token().length();
                        } else {
                            sectionStart = sequence.offset();
                            sectionEnd = sequence.offset() + sequence.token().length();

                            try {
                                int docLen = document.getLength();
                                int startLine = LineDocumentUtils.getLineIndex((BaseDocument) document, Math.min(sectionStart, docLen));
                                int endLine = LineDocumentUtils.getLineIndex((BaseDocument) document, Math.min(sectionEnd, docLen));

                                if (startLine != endLine) {
                                    // multiline scriplet section
                                    // adjust the sections start to the beginning of the firts line
                                    int firstLineStartOffset = Utilities.getRowStartFromLineOffset((BaseDocument) document, startLine);
                                    if (firstLineStartOffset < sectionStart - delimiterSize && 
                                        isWhitespace(document, firstLineStartOffset, sectionStart - delimiterSize)) // always preceeded by the delimiter
                                    {
                                        sectionStart = firstLineStartOffset;
                                    }

                                    // adjust the sections end to the end of the last line
                                    int lines = Utilities.getRowCount((BaseDocument) document);
                                    int lastLineEndOffset;
                                    if (endLine + 1 < lines) {
                                        lastLineEndOffset = Utilities.getRowStartFromLineOffset((BaseDocument) document, endLine + 1);
                                    } else {
                                        lastLineEndOffset = document.getLength() + 1;
                                    }
                                    
                                    if (sectionEnd + 2 >= lastLineEndOffset || // unclosed section
                                        isWhitespace(document, sectionEnd + 2, lastLineEndOffset)) // always succeeded by '%>' hence +2
                                    {
                                        sectionEnd = lastLineEndOffset;
                                    }
                                }
                            } catch (BadLocationException ble) {
                                LOG.log(Level.WARNING, null, ble);
                            }
                            
                            return true;
                        }
                    }
                }
                
                sectionStart = -1;
                sectionEnd = -1;
                finished = true;

                return false;
            }
        }

        
        @Override
        public int getStartOffset() {
            synchronized (EmbeddedSectionsHighlighting.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert sequence != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return Math.max(sectionStart, startOffset);
                }
            }
        }

        @Override
        public int getEndOffset() {
            synchronized (EmbeddedSectionsHighlighting.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert sequence != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return Math.min(sectionEnd, endOffset);
                }
            }
        }

        @Override
        public AttributeSet getAttributes() {
            synchronized (EmbeddedSectionsHighlighting.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert sequence != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return groovyBackground;
                }
            }
        }
        
        private boolean checkVersion() {
            return this.version == EmbeddedSectionsHighlighting.this.version;
        }
   }
    
    public static final class Factory implements HighlightsLayerFactory {
        @Override
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[]{ HighlightsLayer.create(
                "gsp-embedded-groovy-scriplets-highlighting-layer", //NOI18N
                ZOrder.SYNTAX_RACK.forPosition(110), 
                true, 
                new EmbeddedSectionsHighlighting(context.getDocument())
            )};
        }
    }
}
