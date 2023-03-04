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

package org.netbeans.modules.html.editor.xhtml;

import java.awt.Color;
import java.util.NoSuchElementException;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 * Implementation of Highlighting SPI creating coloured background
 * for expression language content in xhtml files
 *
 * @author Vita Stejskal, mfukala@netbeans.org
 */
public class XhtmlElHighlighting extends AbstractHighlightsContainer implements TokenHierarchyListener {

    private final Document document;
    private final AttributeSet expressionLanguageBackground;
    private TokenHierarchy<? extends Document> hierarchy = null;
    private long version = 0;

    XhtmlElHighlighting(Document document) {
        this.document = document;
        
        // load the background color for the embedding token
        AttributeSet elAttribs = null;
        String mimeType = (String) document.getProperty("mimeType"); //NOI18N
        FontColorSettings fcs = MimeLookup.getLookup(mimeType).lookup(FontColorSettings.class);
        if (fcs != null) {
            Color elBC = getColoring(fcs, XhtmlElTokenId.EL.primaryCategory());
            if (elBC != null) {
                elAttribs = AttributesUtilities.createImmutable(
                    StyleConstants.Background, elBC, 
                    ATTR_EXTENDS_EOL, Boolean.TRUE);
            }
        }
        expressionLanguageBackground = elAttribs;
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (expressionLanguageBackground != null) {
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
        private final TokenHierarchy<?> scanner;
        private final int startOffset;
        private final int endOffset;

        private TokenSequence<?> sequence = null;
        private int sectionStart = -1;
        private int sectionEnd = -1;
        private boolean finished = false;
        private AttributeSet attributeSet;
        
        private Highlights(long version, TokenHierarchy<?> scanner, int startOffset, int endOffset) {
            this.version = version;
            this.scanner = scanner;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public boolean moveNext() {
            synchronized (XhtmlElHighlighting.this) {
                if (checkVersion()) {
                    if (sequence == null) {
                        if(!scanner.isActive()) {
                            return false; //token hierarchy inactive already
                        }
                        sequence = scanner.tokenSequence();
                        sequence.move(startOffset);
                    }

                    while (sequence.moveNext() && sequence.offset() < endOffset) {
                        if (expressionLanguageBackground != null && sequence.token().id() == XhtmlElTokenId.EL) {
                            sectionStart = sequence.offset();
                            sectionEnd = sequence.offset() + sequence.token().length();
                            attributeSet = expressionLanguageBackground;
                            
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

        
        public int getStartOffset() {
            synchronized (XhtmlElHighlighting.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert sequence != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return Math.max(sectionStart, startOffset);
                }
            }
        }

        public int getEndOffset() {
            synchronized (XhtmlElHighlighting.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert sequence != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return Math.min(sectionEnd, endOffset);
                }
            }
        }

        public AttributeSet getAttributes() {
            synchronized (XhtmlElHighlighting.this) {
                if (finished) {
                    throw new NoSuchElementException();
                } else {
                    assert sequence != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                    return attributeSet;
                }
            }
        }
        
        private boolean checkVersion() {
            return this.version == XhtmlElHighlighting.this.version;
        }
        
   } // End of Highlights class
    
    public static final class Factory implements HighlightsLayerFactory {
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[]{ HighlightsLayer.create(
                "el-embedded-xhtml-highlighting-layer", //NOI18N
                ZOrder.BOTTOM_RACK.forPosition(150),  //we need to have lower priority than the default syntax from options - 0
                true, 
                new XhtmlElHighlighting(context.getDocument())
            )};
        }
    } // End of Factory class
}
