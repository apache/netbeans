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

package org.netbeans.modules.editor.lib2.highlighting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;

/**
 *
 * @author Vita Stejskal, Miloslav Metelka
 */
public final class ProxyHighlightsContainer extends AbstractHighlightsContainer implements MultiLayerContainer {

    private static final Logger LOG = Logger.getLogger(ProxyHighlightsContainer.class.getName());
    
    private Document doc;
    private HighlightsContainer[] layers;
    private boolean[] blacklisted;
    private long version = 0;

    private final String LOCK = new String("ProxyHighlightsContainer.LOCK"); //NOI18N
    private final LayerListener listener = new LayerListener(this);

    public ProxyHighlightsContainer() {
        this(null, null);
    }
    
    public ProxyHighlightsContainer(Document doc, HighlightsContainer[] layers) {
        setLayers(doc, layers);
    }
    
    /**
     * Gets the list of <code>Highlight</code>s from this layer in the specified
     * area. The highlights are obtained as a merge of the highlights from all the
     * delegate layers. The following rules must hold true for the parameters
     * passed in:
     * 
     * <ul>
     * <li>0 <= <code>startOffset</code> <= <code>endOffset</code></li>
     * <li>0 <= <code>endOffset</code> <= <code>document.getLength() - 1<code></li>
     * <li>Optionally, <code>endOffset</code> can be equal to Integer.MAX_VALUE
     * in which case all available highlights will be returned.</li>
     * </ul>
     *
     * @param startOffset    The beginning of the area.
     * @param endOffset      The end of the area.
     *
     * @return The <code>Highlight</code>s in the area between <code>startOffset</code>
     * and <code>endOffset</code>.
     */
    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        assert 0 <= startOffset : "startOffset must be greater than or equal to zero"; //NOI18N
        assert 0 <= endOffset : "endOffset must be greater than or equal to zero"; //NOI18N
        assert startOffset <= endOffset : "startOffset must be less than or equal to endOffset; " + //NOI18N
            "startOffset = " + startOffset + " endOffset = " + endOffset; //NOI18N
        
        synchronized (LOCK) {
            if (doc == null || layers == null || layers.length == 0 ||
                startOffset < 0 || endOffset < 0 || startOffset >= endOffset || startOffset > doc.getLength()
            ) {
                return HighlightsSequence.EMPTY;
            }

            if (endOffset >= doc.getLength()) {
                endOffset = Integer.MAX_VALUE;
            }

            List<HighlightsSequence> seq = new ArrayList<HighlightsSequence>(layers.length);

            for(int i = layers.length - 1; i >= 0; i--) {
                if (blacklisted[i]) {
                    continue;
                }
                
                try {
                    CheckedHighlightsSequence checked = new CheckedHighlightsSequence(
                        layers[i].getHighlights(startOffset, endOffset),
                        startOffset,
                        endOffset);
                    if (LOG.isLoggable(Level.FINE)) {
                        checked.setContainerDebugId("PHC.Layer[" + i + "]=" + layers[i]); //NOI18N
                    }
                    seq.add(checked);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    blacklisted[i] = true;
                    LOG.log(Level.WARNING, "The layer failed to supply highlights: " + layers[i], t); //NOI18N
                }
            }

            return new ProxySeq(version, seq, startOffset, endOffset);
        }
    }

    /**
     * Gets the delegate layers.
     *
     * @return The layers, which this proxy layer delegates to.
     */
    public HighlightsContainer[] getLayers() {
        synchronized (LOCK) {
            return layers;
        }
    }
    
    /**
     * Sets the delegate layers. The layers are merged in the same order in which
     * they appear in the array passed into this method. That means that the first
     * layer in the array is the less important (i.e. the bottom of the z-order) and
     * the last layer in the array is the most visible one (i.e. the top of the z-order).
     *
     * <p>If you want the layers to be merged according to their real z-order sort
     * the array first by using <code>ZOrder.sort()</code>.
     *
     * @param layers    The new delegate layers. Can be <code>null</code>.
     * @see org.netbeans.api.editor.view.ZOrder#sort(HighlightLayer [])
     */
    @Override
    public void setLayers(Document doc, HighlightsContainer[] layers) {
        Document docForEvents = null;

        synchronized (LOCK) {
            if (doc == null) {
                assert layers == null : "If doc is null the layers must be null too."; //NOI18N
            }

            docForEvents = doc != null ? doc : this.doc;

            // Remove the listener from the current layers
            if (this.layers != null) {
                for (int i = 0; i < this.layers.length; i++) {
                    this.layers[i].removeHighlightsChangeListener(listener);
                }
            }

            this.doc = doc;
            this.layers = layers;
            this.blacklisted = layers == null ? null : new boolean [layers.length];
            increaseVersion();

            // Add the listener to the new layers
            if (this.layers != null) {
                for (int i = 0; i < this.layers.length; i++) {
                    this.layers[i].addHighlightsChangeListener(listener);
                }
            }
        }
        
        if (docForEvents != null) {
            docForEvents.render(new Runnable() {
                public @Override void run() {
                    fireHighlightsChange(0, Integer.MAX_VALUE);
                }
            });
        }
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------
    
    private void layerChanged(HighlightsContainer layer, final int changeStartOffset, final int changeEndOffset) {
        Document docForEvents = null;

        synchronized (LOCK) {
            LOG.log(Level.FINE, "Container's layer changed: {0}", layer); //NOI18N
            increaseVersion();
            docForEvents = doc;
        }
        
        // Fire an event
        if (docForEvents != null) {
            docForEvents.render(new Runnable() {
                public @Override void run() {
                    fireHighlightsChange(changeStartOffset, changeEndOffset);
                }
            });
        }
    }

    private void increaseVersion() {
        version++;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("PHC@" + Integer.toHexString(System.identityHashCode(this)) + //NOI18N
                ", doc@" + Integer.toHexString(System.identityHashCode(doc)) + " version=" + version); //NOI18N
        }
    }

    private static final class LayerListener implements HighlightsChangeListener {
        
        private WeakReference<ProxyHighlightsContainer> ref;
        
        public LayerListener(ProxyHighlightsContainer container) {
            ref = new WeakReference<ProxyHighlightsContainer>(container);
        }
        
        public @Override void highlightChanged(HighlightsChangeEvent event) {
            ProxyHighlightsContainer container = ref.get();
            if (container != null) {
                container.layerChanged(
                    (HighlightsContainer)event.getSource(), 
                    event.getStartOffset(), 
                    event.getEndOffset());
            }
        }
    } // End of Listener class

    private final class ProxySeq implements HighlightsSequenceEx {
        
        private final Sequence2Marks [] marks;
        private int index1 = -2;
        private int index2 = -2;
        private AttributeSet compositeAttributes = null;
        private long version;
        
        public ProxySeq(long version, List<HighlightsSequence> seq, int startOffset, int endOffset) {
            this.version = version;
            
            // Initialize marks
            marks = new Sequence2Marks [seq.size()];
            for (int i = 0; i < seq.size(); i++) {
                marks[i] = new Sequence2Marks(seq.get(i), startOffset, endOffset);
            }
        }

        public @Override boolean moveNext() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                if (checkVersion()) {
                    if (index1 == -2 && index2 == -2) {
                        for(Sequence2Marks m : marks) {
                            m.moveNext();
                        }
                        index2 = findLowest();
                    }

                    do {
                        // Move to the next mark
                        index1 = index2;
                        if (index2 != -1) {
                            marks[index2].moveNext();
                            index2 = findLowest();
                        }

                        if (index1 == -1 || index2 == -1) {
                            break;
                        }

                        compositeAttributes = findAttributes();

                    } while (compositeAttributes == null);

                    return index1 != -1 && index2 != -1;
                } else {
                    index1 = index2 = -1;
                    return false;
                }
            }
        }

        public @Override int getStartOffset() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                if (index1 == -2 && index2 == -2) {
                    throw new IllegalStateException("Uninitialized sequence, call moveNext() first."); //NOI18N
                } else if (index1 == -1 || index2 == -1) {
                    throw new NoSuchElementException();
                }

                return marks[index1].getPreviousMarkOffset();
            }
        }

        public @Override int getEndOffset() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                if (index1 == -2 && index2 == -2) {
                    throw new IllegalStateException("Uninitialized sequence, call moveNext() first."); //NOI18N
                } else if (index1 == -1 || index2 == -1) {
                    throw new NoSuchElementException();
                }

                return marks[index2].getMarkOffset();
            }
        }

        public @Override AttributeSet getAttributes() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                if (index1 == -2 && index2 == -2) {
                    throw new IllegalStateException("Uninitialized sequence, call moveNext() first."); //NOI18N
                } else if (index1 == -1 || index2 == -1) {
                    throw new NoSuchElementException();
                }

                return compositeAttributes;
            }
        }

        @Override
        public boolean isStale() {
            synchronized (ProxyHighlightsContainer.this.LOCK) {
                return !checkVersion();
            }
        }

        private int findLowest() {
            int lowest = Integer.MAX_VALUE;
            int idx = -1;
            
            for(int i = 0; i < marks.length; i++) {
                if (marks[i].isFinished()) {
                    continue;
                }
                
                int offset = marks[i].getMarkOffset();
                if (offset < lowest) {
                    lowest = offset;
                    idx = i;
                }
            }
            
            return idx;
        }

        private AttributeSet findAttributes() {
            ArrayList<AttributeSet> list = new ArrayList<AttributeSet>();

            for(int i = 0; i < marks.length; i++) {
                if (marks[i].getPreviousMarkAttributes() != null) {
                    list.add(marks[i].getPreviousMarkAttributes());
                }
            }

            if (!list.isEmpty()) {
                return AttributesUtilities.createComposite(list.toArray(new AttributeSet[0]));
            } else {
                return null;
            }
        }
        
        private boolean checkVersion() {
            return this.version == ProxyHighlightsContainer.this.version;
        }
    } // End of ProxySeq class
    
    /* package */ static final class Sequence2Marks {
        
        private HighlightsSequence seq;
        private int startOffset;
        private int endOffset;
        
        private boolean hasNext = false;
        private boolean useStartOffset = true;
        private boolean finished = true;

        private int lastEndOffset = -1;
        
        private int previousMarkOffset = -1;
        private AttributeSet previousMarkAttributes = null;
        
        public Sequence2Marks(HighlightsSequence seq, int startOffset, int endOffset) {
            this.seq = seq;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        public boolean isFinished() {
            return finished;
        }
        
        public boolean moveNext() {
            if (!useStartOffset || hasNext) {
                previousMarkOffset = getMarkOffset();
                previousMarkAttributes = getMarkAttributes();
            }
            
            if (useStartOffset) {
                // Move to the next highlighted area
                while(true == (hasNext = seq.moveNext())) {
                    if (seq.getEndOffset() > startOffset) {
                        break;
                    }
                }
                
                if (hasNext && seq.getStartOffset() > endOffset) {
                    hasNext = false;
                }
                
                if (hasNext) {
                    if (lastEndOffset != -1 && lastEndOffset < seq.getStartOffset()) {
                        useStartOffset = false;
                    } else {
                        lastEndOffset = seq.getEndOffset();
                    }
                } else {
                    if (lastEndOffset != -1) {
                        useStartOffset = false;
                    }
                }
            } else {
                if (hasNext) {
                    lastEndOffset = seq.getEndOffset();
                }
                useStartOffset = true;
            }
            
            finished = useStartOffset && !hasNext;
            return !finished;
        }
        
        public int getMarkOffset() {
            if (finished) {
                throw new NoSuchElementException();
            }
            
            return useStartOffset ? 
                Math.max(startOffset, seq.getStartOffset()) : 
                Math.min(endOffset, lastEndOffset);
        }
        
        public AttributeSet getMarkAttributes() {
            if (finished) {
                throw new NoSuchElementException();
            }
            
            return useStartOffset ? seq.getAttributes() : null;
        }
        
        public int getPreviousMarkOffset() {
            return previousMarkOffset;
        }
        
        public AttributeSet getPreviousMarkAttributes() {
            return previousMarkAttributes;
        }
    } // End of Sequence2Marks class
}
