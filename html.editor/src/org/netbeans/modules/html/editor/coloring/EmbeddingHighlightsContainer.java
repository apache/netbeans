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

package org.netbeans.modules.html.editor.coloring;

import java.awt.Color;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * Implementation of Highlighting SPI creating coloured background
 * for embedded CSS and JavaScript.
 *
 * @author Marek Fukala
 */
public class EmbeddingHighlightsContainer extends AbstractHighlightsContainer implements TokenHierarchyListener {

    private static final Logger LOG = Logger.getLogger(EmbeddingHighlightsContainer.class.getName());
    
    private static final String CSS_BACKGROUND_TOKEN_NAME = "css-embedded"; //NOI18N
    private static final String JAVASCRIPT_BACKGROUND_TOKEN_NAME = "javascript-embedded"; //NOI18N
    private static final String HTML_MIME_TYPE = "text/html"; //NOI18N
    private static final String CSS_MIME_TYPE = "text/css"; //NOI18N
    private static final String CSS_INLINED_MIME_TYPE = "text/css-inlined"; //NOI18N
    private static final String JAVASCRIPT_MIME_TYPE = "text/javascript"; //NOI18N

    private AttributeSet cssBackground;
    private AttributeSet javascriptBackground;
    private final Document document;
    private TokenHierarchy<? extends Document> hierarchy = null;
    private long version = 0;
    
    private Result<FontColorSettings> lookupResult;
    private LookupListener lookupListener;

    EmbeddingHighlightsContainer(Document document) {
        this.document = document;

        lookupResult = MimeLookup.getLookup(HTML_MIME_TYPE).lookupResult(FontColorSettings.class);
        lookupResult.addLookupListener(WeakListeners.create(LookupListener.class,
                lookupListener = new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                refreshColorings();
            }
        }, lookupResult));
        refreshColorings();
    }
    
    private void refreshColorings() {
        Collection<? extends FontColorSettings> allInstances = lookupResult.allInstances();
        assert allInstances.size() > 0;
        
        FontColorSettings fcs = allInstances.iterator().next();
        Color cssBC = null;
        Color jsBC = null;
        if (fcs != null) {
            cssBC = getColoring(fcs, CSS_BACKGROUND_TOKEN_NAME);
            jsBC = getColoring(fcs, JAVASCRIPT_BACKGROUND_TOKEN_NAME);
        }

        cssBackground = cssBC == null ? SimpleAttributeSet.EMPTY : AttributesUtilities.createImmutable(
            StyleConstants.Background, cssBC, 
            ATTR_EXTENDS_EOL, Boolean.TRUE);

        javascriptBackground = jsBC == null ? SimpleAttributeSet.EMPTY : AttributesUtilities.createImmutable(
            StyleConstants.Background, jsBC, 
            ATTR_EXTENDS_EOL, Boolean.TRUE);        
    }

    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (javascriptBackground != null || cssBackground != null) {
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
//        return new Highlights(document, startOffset, endOffset);
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
                    if (mimeType.equals(HTML_MIME_TYPE)) {
                        //html is the top level one
                        tokenSequenceList = scanner.tokenSequenceList(topLevelLanguagePath, startOffsetBoundary, endOffsetBoundary);
                    } else {
                        //html is embedded in some other language
                        LanguagePath htmlPath = LanguagePath.get(topLevelLanguagePath, HTMLTokenId.language());
                        tokenSequenceList = scanner.tokenSequenceList(htmlPath, startOffsetBoundary, endOffsetBoundary);
                    }
                } else {
                    LOG.log(Level.WARNING, "Language " + mimeType + " obtained from the document mimeType property cannot be found!"); //NOI18N
                }
            }

            if (tokenSequenceList != null) {
                for (TokenSequence tokenSequence : tokenSequenceList) {
                    assert tokenSequence.language().mimeType().equals(HTML_MIME_TYPE);
                    tokenSequence.move(realEndOffset);
                    while (tokenSequence.moveNext() && tokenSequence.offset() < endOffsetBoundary) {
                        TokenSequence eTokenSequence = tokenSequence.embedded();

                        if (eTokenSequence == null || !eTokenSequence.moveNext()) {
                            continue;
                        }
                        String embeddedMimeType = eTokenSequence.language().mimeType();
                        if (CSS_MIME_TYPE.equals(embeddedMimeType) || (CSS_INLINED_MIME_TYPE).equals(embeddedMimeType) || (JAVASCRIPT_MIME_TYPE).equals(embeddedMimeType)) {
                            try {
                                eTokenSequence.move(realEndOffset);
                                if(eTokenSequence.moveNext()) {
                                    startOffset = eTokenSequence.offset();
                                    do {
                                        endOffset = eTokenSequence.offset() + eTokenSequence.token().length();
                                    } while (eTokenSequence.moveNext());
                                    realEndOffset = endOffset > realEndOffset ? endOffset : realEndOffset + 1;
                                    int startLO = Utilities.getLineOffset((BaseDocument) document, startOffset);
                                    int endLO = Utilities.getLineOffset((BaseDocument) document, endOffset);
                                    if (startLO != endLO) {
                                        //not just one line block - test boundaries
                                        if ((Utilities.getFirstNonWhiteBwd((BaseDocument) document, Utilities.getRowEnd((BaseDocument) document, startOffset)) + 1) == startOffset) {
                                            //just <script-style> tag on the first line -> move start to next line
                                            startOffset = Utilities.getRowStartFromLineOffset((BaseDocument) document, startLO + 1);
                                        }
                                        if (Utilities.getFirstNonWhiteFwd((BaseDocument) document, Utilities.getRowStartFromLineOffset((BaseDocument) document, endLO)) == endOffset) {
                                            //just </script-style> tag on the last line -> move block end to previous line end
                                            endOffset = Utilities.getRowStartFromLineOffset((BaseDocument) document, endLO);
                                        }
                                    }

                                    attributeSet = embeddedMimeType.equals(JAVASCRIPT_MIME_TYPE) ? javascriptBackground : cssBackground;
                                    if (attributeSet != null) {
                                        return true;
                                    }
                                }
                            } catch (BadLocationException ex) {
                                LOG.log(Level.INFO, "An error occured when creating coloured background for CSS and JavaScript.", ex); //NOI18N
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
            return this.version == EmbeddingHighlightsContainer.this.version;
        }
    }
}
