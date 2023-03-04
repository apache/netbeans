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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class GuardedBlocksHighlighting extends AbstractHighlightsContainer implements PropertyChangeListener, DocumentListener {
    
    private static final Logger LOG = Logger.getLogger(GuardedBlocksHighlighting.class.getName());
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.oldlibbridge.GuardedBlocksHighlighting"; //NOI18N
    
    static final AttributeSet EXTENDS_EOL_ATTR_SET =
            AttributesUtilities.createImmutable(ATTR_EXTENDS_EOL, Boolean.TRUE);

    private final Document document;
    private final MarkBlockChain guardedBlocksChain;
    private final MimePath mimePath;

    private AttributeSet attribs = null;
    
    /** Creates a new instance of NonLexerSytaxHighlighting */
    public GuardedBlocksHighlighting(Document document, String mimeType) {
        this.document = document;
        if (document instanceof GuardedDocument) {
            this.guardedBlocksChain = ((GuardedDocument) document).getGuardedBlockChain();
            this.guardedBlocksChain.addPropertyChangeListener(WeakListeners.propertyChange(this, this.guardedBlocksChain));
            this.document.addDocumentListener(WeakListeners.create(DocumentListener.class, this, this.document));
        } else {
            this.guardedBlocksChain = null;
        }
        this.mimePath = MimePath.parse(mimeType);
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (guardedBlocksChain != null) {
                return new HSImpl(guardedBlocksChain.getChain(), startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }
    
    // ----------------------------------------------------------------------
    //  PropertyChangeListener implementation
    // ----------------------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || evt.getPropertyName().equals(MarkBlockChain.PROP_BLOCKS_CHANGED)) {
            int start = evt.getOldValue() == null ? -1 : ((Integer) evt.getOldValue()).intValue();
            int end = evt.getNewValue() == null ? -1 : ((Integer) evt.getNewValue()).intValue();
            
            if (start < 0 || start >= document.getLength()) {
                start = 0;
            }
            
            if (end <= start || end > document.getLength()) {
                end = Integer.MAX_VALUE;
            }
            
            fireHighlightsChange(start, end);
        }
    }

    // ----------------------------------------------------------------------
    //  DocumentListener implementation
    // ----------------------------------------------------------------------

    public void changedUpdate(DocumentEvent e) {
        // ignore
    }

    public void insertUpdate(DocumentEvent e) {
        int changeStart = e.getOffset();
        int changeEnd = e.getOffset() + e.getLength();

        if (isAffectedByChange(changeStart, changeEnd)) {
            fireHighlightsChange(changeStart, changeEnd);
        }
    }

    public void removeUpdate(DocumentEvent e) {
        int changeStart = e.getOffset();
        int changeEnd = e.getOffset() + e.getLength();

        if (isAffectedByChange(changeStart, changeEnd)) {
            fireHighlightsChange(changeStart, changeEnd);
        }
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private boolean isAffectedByChange(int startOffset, int endOffset) {
        for(MarkBlock b = guardedBlocksChain.getChain(); b != null; b = b.getNext()) {
            int c = b.compare(startOffset, endOffset);
            if ((c & MarkBlock.OVERLAP) != 0 || (c & MarkBlock.CONTINUE) != 0) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("<" + startOffset + ", " + endOffset + "> collides with guarded block <" //NOI18N
                        + b.getStartOffset() + ", " + b.getEndOffset() + ">"); //NOI18N
                }
                return true;
            } else {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("<" + startOffset + ", " + endOffset + "> is outside of guarded block <" //NOI18N
                        + b.getStartOffset() + ", " + b.getEndOffset() + ">"); //NOI18N
                }
            }
        }
        return false;
    }
    
    private final class HSImpl implements HighlightsSequence {
        
        private final int startOffset;
        private final int endOffset;

        private boolean init = false;
        private MarkBlock block;

        public HSImpl(MarkBlock block, int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.block = block;
        }
        
        public boolean moveNext() {
            if (!init) {
                init = true;

                while(null != block) {
                    if (block.getEndOffset() > startOffset) {
                        break;
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Skipping block: " + block + //NOI18N
                            ", blockStart = " + block.getStartOffset() + //NOI18N
                            ", blockEnd = " + block.getEndOffset() + //NOI18N
                            ", startOffset = " + startOffset + //NOI18N
                            ", endOffset = " + endOffset //NOI18N
                        );
                    }
                    
                    block = block.getNext();
                }
            } else if (block != null) {
                block = block.getNext();
            }
            
            if (block != null && block.getStartOffset() > endOffset) {
                block = null;
            }
            
            if (LOG.isLoggable(Level.FINE)) {
                if (block != null) {
                    LOG.fine("Next block: " + block + //NOI18N
                        ", blockStart = " + block.getStartOffset() + //NOI18N
                        ", blockEnd = " + block.getEndOffset() + //NOI18N
                        ", startOffset = " + startOffset + //NOI18N
                        ", endOffset = " + endOffset //NOI18N
                    );
                } else {
                    LOG.fine("Next block: null"); //NOI18N
                }
            }
            
            return block != null;
        }

        public int getStartOffset() {
            synchronized (GuardedBlocksHighlighting.this) {
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                return Math.max(block.getStartOffset(), startOffset);
            }
        }

        public int getEndOffset() {
            synchronized (GuardedBlocksHighlighting.this) {
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                return Math.min(block.getEndOffset(), endOffset);
            }
        }

        public AttributeSet getAttributes() {
            synchronized (GuardedBlocksHighlighting.this) {
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                if (attribs == null) {
                    FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
                    if (fcs != null) {
                        attribs = fcs.getFontColors(FontColorNames.GUARDED_COLORING);
                    }

                    if (attribs == null) {
                        attribs = SimpleAttributeSet.EMPTY;
                    } else {
                        attribs = AttributesUtilities.createImmutable(
                            attribs, 
                            EXTENDS_EOL_ATTR_SET
                        );
                    }
                }
                
                return attribs;
            }
        }
    } // End of HSImpl class

}
