/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
