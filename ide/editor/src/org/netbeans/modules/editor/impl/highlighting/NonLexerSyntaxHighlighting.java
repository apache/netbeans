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

package org.netbeans.modules.editor.impl.highlighting;

import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class NonLexerSyntaxHighlighting extends AbstractHighlightsContainer implements DocumentListener {
    
    private static final Logger LOG = Logger.getLogger(NonLexerSyntaxHighlighting.class.getName());
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.oldlibbridge.NonLexerSyntaxHighlighting"; //NOI18N
    
    private final Document document;
    private long version = 0;

    private final MimePath mimePath;
    private final WeakHashMap<TokenID, AttributeSet> attribsCache = new WeakHashMap<TokenID, AttributeSet>();
    
    /** Creates a new instance of NonLexerSytaxHighlighting */
    public NonLexerSyntaxHighlighting(Document document, String mimeType) {
        this.mimePath = MimePath.parse(mimeType);
        
        this.document = document;
        this.document.addDocumentListener(WeakListeners.document(this, document));
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (document instanceof BaseDocument) {
                return new HSImpl(version, (BaseDocument) document, startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }
    
    // ----------------------------------------------------------------------
    //  DocumentListener implementation
    // ----------------------------------------------------------------------
    
    public void insertUpdate(DocumentEvent e) {
        documentChanged(e.getOffset(), e.getLength());
    }

    public void removeUpdate(DocumentEvent e) {
        documentChanged(e.getOffset(), e.getLength());
    }

    public void changedUpdate(DocumentEvent e) {
        documentChanged(e.getOffset(), e.getLength());
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private AttributeSet findAttribs(TokenItem tokenItem) {
        synchronized (this) {
            AttributeSet attribs = attribsCache.get(tokenItem.getTokenID());

            if (attribs == null) {
                FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
                if (fcs != null) {
                    attribs = findFontAndColors(fcs, tokenItem);
                    if (attribs == null) {
                        attribs = SimpleAttributeSet.EMPTY;
                    }
                    
                    attribsCache.put(tokenItem.getTokenID(), attribs);
                } else {
                    LOG.warning("Can't find FCS for mime path: '" + mimePath.getPath() + "'"); //NOI18N
                }
            }

            return attribs == null ? SimpleAttributeSet.EMPTY : attribs;
        }
    }

    private static AttributeSet findFontAndColors(FontColorSettings fcs, TokenItem tokenItem) {
        AttributeSet attribs = null;
        TokenContextPath tokenContextPath = tokenItem.getTokenContextPath();
        
        // First try the token's name
        {
            String name = tokenContextPath.getFullTokenName(tokenItem.getTokenID());
            if (name != null) {
                attribs = fcs.getTokenFontColors(name);
            }
        }

        // Then try the category
        if (attribs == null) {
            TokenCategory category = tokenItem.getTokenID().getCategory();
            if (category != null) {
                String categoryName = tokenContextPath.getFullTokenName(category);
                if (categoryName != null) {
                    attribs = fcs.getTokenFontColors(categoryName);
                }
            }
        }

        return attribs;
    }

    private void documentChanged(int offset, int lenght) {
        synchronized (this) {
            version++;
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Document changed: changeStart = " + offset + ", changeEnd = " + (offset + lenght)); //NOI18N
        }
        
        if (offset < 0 || offset > document.getLength()) {
            offset = 0;
        }
        
        if (lenght <= 0 || offset + lenght > document.getLength()) {
            lenght = document.getLength() - offset;
        }
        
        fireHighlightsChange(offset, offset + lenght);
    }

    private final class HSImpl implements HighlightsSequence {
        
        private final long version;
        private final BaseDocument baseDocument;
        private final int startOffset;
        private final int endOffset;

        private boolean init = false;
        private TokenItem tokenItem;
        
        public HSImpl(long version, BaseDocument baseDocument, int startOffset, int endOffset) {
            this.version = version;
            this.baseDocument = baseDocument;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public boolean moveNext() {
            if (!init) {
                init = true;

                try {
                    @SuppressWarnings("deprecation")
                    SyntaxSupport syntax = baseDocument.getSyntaxSupport();

                    if (syntax instanceof ExtSyntaxSupport) {
                        ExtSyntaxSupport ess = (ExtSyntaxSupport) syntax;
                        tokenItem = ess.getTokenChain(startOffset, endOffset);
                    } else {
                        LOG.log(Level.WARNING, "Token sequence not an ExtSyntaxSupport: document " + baseDocument + " (lexer.nbbridge module missing?)"); //NOI18N
                        tokenItem = null;
                    }
                } catch (BadLocationException e) {
                    LOG.log(Level.WARNING, "Can't get token sequence: document " + baseDocument + //NOI18N
                        ", startOffset = " + startOffset + ", endOffset = " + endOffset, e); //NOI18N
                    tokenItem = null;
                }
                
                while(null != tokenItem) {
                    if (tokenItem.getOffset() + tokenItem.getImage().length() > startOffset) {
                        break;
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Skipping tokenId: " + tokenItem.getTokenID() + //NOI18N
                            ", tokenStart = " + tokenItem.getOffset() + //NOI18N
                            ", tokenEnd = " + (tokenItem.getOffset() + tokenItem.getImage().length()) + //NOI18N
                            ", startOffset = " + startOffset + //NOI18N
                            ", endOffset = " + endOffset //NOI18N
                        );
                    }
                    
                    tokenItem = tokenItem.getNext();
                }
            } else if (tokenItem != null) {
                tokenItem = tokenItem.getNext();
            }
            
            if (tokenItem != null && tokenItem.getOffset() > endOffset) {
                tokenItem = null;
            }
            
            if (LOG.isLoggable(Level.FINE)) {
                if (tokenItem != null) {
                    LOG.fine("Next tokenId: " + tokenItem.getTokenID() + //NOI18N
                        ", tokenStart = " + tokenItem.getOffset() + //NOI18N
                        ", tokenEnd = " + (tokenItem.getOffset() + tokenItem.getImage().length()) + //NOI18N
                        ", startOffset = " + startOffset + //NOI18N
                        ", endOffset = " + endOffset //NOI18N
                    );
                } else {
                    LOG.fine("Next tokenId: null"); //NOI18N
                }
            }
            
            return tokenItem != null;
        }

        public int getStartOffset() {
            synchronized (NonLexerSyntaxHighlighting.this) {
                checkVersion();
                
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (tokenItem == null) {
                    throw new NoSuchElementException();
                }

                return Math.max(tokenItem.getOffset(), startOffset);
            }
        }

        public int getEndOffset() {
            synchronized (NonLexerSyntaxHighlighting.this) {
                checkVersion();
                
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (tokenItem == null) {
                    throw new NoSuchElementException();
                }

                return Math.min(tokenItem.getOffset() + tokenItem.getImage().length(), endOffset);
            }
        }

        public AttributeSet getAttributes() {
            synchronized (NonLexerSyntaxHighlighting.this) {
                checkVersion();
                
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (tokenItem == null) {
                    throw new NoSuchElementException();
                }

                return findAttribs(tokenItem);
            }
        }
        
        private void checkVersion() {
            if (this.version != NonLexerSyntaxHighlighting.this.version) {
                throw new ConcurrentModificationException();
            }
        }
    } // End of HSImpl class
    
}
