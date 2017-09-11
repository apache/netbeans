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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.editor.lib2.highlighting;

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal, Miloslav Metelka
 */
public final class CompoundHighlightsContainer extends AbstractHighlightsContainer implements MultiLayerContainer {

    private static final Logger LOG = Logger.getLogger(CompoundHighlightsContainer.class.getName());
    
    private static final int MIN_CACHE_SIZE = 128;
    
    private Document doc;
    private HighlightsContainer[] layers;
    private boolean[] blacklisted;
    private long version = 0;

    private final Object LOCK = new String("CompoundHighlightsContainer.LOCK"); //NOI18N
    private final LayerListener listener = new LayerListener(this);

    private OffsetsBag cache;
    private boolean cacheObsolete;
    private CacheBoundaries cacheBoundaries;

    private final boolean assertions;
    
    public CompoundHighlightsContainer() {
        this(null, null);
    }
    
    public CompoundHighlightsContainer(Document doc, HighlightsContainer[] layers) {
        setLayers(doc, layers);
        boolean a = false;
        assert a = true;
        this.assertions = a;
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
    public @Override HighlightsSequence getHighlights(int startOffset, int endOffset) {
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

            int [] update = null;

            int lowest = -1;
            int highest = -1;

            if (cacheObsolete) {
                cacheObsolete = false;
                discardCache();
            } else {
                lowest = cacheBoundaries.getLowerBoundary();
                highest = cacheBoundaries.getUpperBoundary();

                if (lowest == -1 || highest == -1) {
                    // not sure what is cached -> reset the cache
                    discardCache();
                } else {
                    if (endOffset <= highest && startOffset < lowest) {
                        // below the cached area, but close enough
                        update = new int [] { expandBelow(startOffset, lowest), lowest };
                    } else if (startOffset >= lowest && endOffset > highest) {
                        // above the cached area, but close enough
                        update = new int [] { highest, expandAbove(highest, endOffset) };
                    } else if (startOffset < lowest && endOffset > highest) {
                        // extends the cached area on both sides
                        update = new int [] { expandBelow(startOffset, lowest), lowest, highest, expandAbove(highest, endOffset) };
                    } else if (startOffset >= lowest && endOffset <= highest) {
                        // inside the cached area
                    } else {
                        // completely off the area and too far
                        discardCache();
                    }
                }
            }

            retry:
            for (;;) {
                OffsetsBag bag = cache;
                if (bag == null) {
                    bag = new OffsetsBag(doc, true);
                    cache = bag;
                    lowest = highest = -1;
                    update = new int [] { expandBelow(startOffset, endOffset), expandAbove(startOffset, endOffset) };
                }

                if (update != null) {
                    // check the update boundaries in order to prevent errors such as #172884
                    for (int i = 0; i < update.length / 2; i++) {
                        if (update[2 * i] > doc.getLength()) {
                            if (assertions && LOG.isLoggable(Level.WARNING)) {
                                String msg = "Inconsistent cache update boundaries:" //NOI18N
                                            + " startOffset=" + startOffset + ", endOffset=" + endOffset //NOI18N
                                            + ", lowest=" + lowest + ", highest=" + highest //NOI18N
                                            + ", doc.length=" + doc.getLength() //NOI18N
                                            + ", update[]=" + update; //NOI18N
                                LOG.log(Level.WARNING, null, new Throwable(msg));
                            }
                            update = new int [] { 0, Integer.MAX_VALUE };
                            break;
                        }
                    }

                    for (int i = 0; i < update.length / 2; i++) {
                        if (update[2 * i + 1] >= doc.getLength()) {
                            update[2 * i + 1] = Integer.MAX_VALUE;
                        }

                        if (!updateCache(update[2 * i], update[2 * i + 1], bag)) {
                            discardCache();
                            continue retry;
                        }

                        if (update[2 * i + 1] == Integer.MAX_VALUE) {
                            break;
                        }
                    }

                    if (lowest == -1 || highest == -1) {
                        cacheBoundaries.setBoundaries(update[0], update[update.length - 1]);
                    } else {
                        cacheBoundaries.setBoundaries(Math.min(lowest, update[0]), Math.max(highest, update[update.length - 1]));
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        int lower = cacheBoundaries.getLowerBoundary();
                        int upper = cacheBoundaries.getUpperBoundary();
                        LOG.fine("Cache boundaries: " + //NOI18N
                            "<" + (lower == -1 ? "-" : lower) + //NOI18N
                            ", " + (upper == -1 ? "-" : upper) + "> " + //NOI18N
                            "when asked for <" + startOffset + ", " + endOffset + ">"); //NOI18N
                    }
                }

                return new Seq(version, bag.getHighlights(startOffset, endOffset));
            }
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
            this.cacheObsolete = true;
            this.cacheBoundaries = doc == null ? null : new CacheBoundaries(doc);
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

//    public void resetCache() {
//        layerChanged(null, 0, Integer.MAX_VALUE);
//    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------
    
    private void layerChanged(HighlightsContainer layer, final int changeStartOffset, final int changeEndOffset) {
        Document docForEvents = null;

        synchronized (LOCK) {
            // XXX: Perhaps we could do something more efficient.
            LOG.log(Level.FINE, "Cache obsoleted by changes in {0}", layer); //NOI18N
            cacheObsolete = true;
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

    private boolean updateCache(final int startOffset, final int endOffset, OffsetsBag bag) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Updating cache: <" + startOffset + ", " + endOffset + ">"); //NOI18N
        }
        
        for (int i = 0; i < layers.length; i++) {
            if (blacklisted[i]) {
                continue;
            }

            try {
                CheckedHighlightsSequence checked = new CheckedHighlightsSequence(
                    layers[i].getHighlights(startOffset, endOffset),
                    startOffset,
                    endOffset
                );
                if (LOG.isLoggable(Level.FINE)) {
                    checked.setContainerDebugId("CHC.Layer[" + i + "]=" + layers[i]); //NOI18N
                }
                bag.addAllHighlights(checked);
                if (bag != cache) {
                    // #185171: layers[i] perfomed an operation, which reset the cache.
                    // Let's start over again.
                    return false;
                }
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(dumpLayerHighlights(layers[i], startOffset, endOffset));
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                blacklisted[i] = true;
                LOG.log(Level.WARNING, "The layer failed to supply highlights: " + layers[i], t); //NOI18N
            }
        }

        // Successfully went through all the layers without resetting the cache.
        return true;
    }
    
    private void increaseVersion() {
        version++;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CHC@" + Integer.toHexString(System.identityHashCode(this)) + //NOI18N
                ", OB@" + (cache == null ? "null" : Integer.toHexString(System.identityHashCode(cache))) + //NOI18N
                ", doc@" + Integer.toHexString(System.identityHashCode(doc)) + " version=" + version); //NOI18N
        }
    }
    
    private void discardCache() {
        if (cache != null) {
            cache.discard();
        }
        cache = null;
    }
    
    private static int expandBelow(int startOffset, int endOffset) {
        if (startOffset == 0 || endOffset == Integer.MAX_VALUE) {
            return startOffset;
        } else {
            int expandBy = Math.max((endOffset - startOffset) >> 2, MIN_CACHE_SIZE);
            return Math.max(startOffset - expandBy, 0);
        }
    }
    
    private static int expandAbove(int startOffset, int endOffset) {
        if (endOffset == Integer.MAX_VALUE) {
            return endOffset;
        } else {
            int expandBy = Math.max((endOffset - startOffset) >> 2, MIN_CACHE_SIZE);
            return endOffset + expandBy;
        }
    }

    private static String dumpLayerHighlights(HighlightsContainer layer, int startOffset, int endOffset) {
        StringBuilder sb = new StringBuilder();

        sb.append("Highlights in ").append(layer).append(": {\n"); //NOI18N
        
        for(HighlightsSequence seq = layer.getHighlights(startOffset, endOffset); seq.moveNext(); ) {
            sb.append("  "); //NOI18N
            dumpHighlight(seq, sb);
            sb.append("\n"); //NOI18N
        }
        
        sb.append("} End of Highlights in ").append(layer); //NOI18N
        sb.append("\n"); //NOI18N
        
        return sb.toString();
    }

    /* package */ static StringBuilder dumpHighlight(HighlightsSequence seq, StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        sb.append("<"); //NOI18N
        sb.append(seq.getStartOffset());
        sb.append(", "); //NOI18N
        sb.append(seq.getEndOffset());
        sb.append(", "); //NOI18N
        sb.append(seq.getAttributes().getAttribute(StyleConstants.NameAttribute));
        sb.append(">"); //NOI18N
        return sb;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        for (int i = 0; i < layers.length; i++) {
            sb.append('[').append(i).append("]: ").append(layers[i]);
            sb.append('\n');
        }
        return sb.toString();
    }
    
    private static final class LayerListener implements HighlightsChangeListener {
        
        private WeakReference<CompoundHighlightsContainer> ref;
        
        public LayerListener(CompoundHighlightsContainer container) {
            ref = new WeakReference<CompoundHighlightsContainer>(container);
        }
        
        public @Override void highlightChanged(HighlightsChangeEvent event) {
            CompoundHighlightsContainer container = ref.get();
            if (container != null) {
                container.layerChanged(
                    (HighlightsContainer)event.getSource(), 
                    event.getStartOffset(), 
                    event.getEndOffset());
            }
        }
    } // End of Listener class

    private final class Seq implements HighlightsSequenceEx {
        
        private HighlightsSequence seq;
        private long version;
        
        private int startOffset = -1;
        private int endOffset = -1;
        private AttributeSet attibutes = null;
        
        public Seq(long version, HighlightsSequence seq) {
            this.version = version;
            this.seq = seq;
        }
        
        public @Override boolean moveNext() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                if (checkVersion()) {
                    if (seq.moveNext()) {
                        startOffset = seq.getStartOffset();
                        endOffset = seq.getEndOffset();
                        attibutes = seq.getAttributes();
                        return true;
                    }
                }
                
                return false;
            }
        }

        public @Override int getStartOffset() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                assert startOffset != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return startOffset;
            }
        }

        public @Override int getEndOffset() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                assert endOffset != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return endOffset;
            }
        }

        public @Override AttributeSet getAttributes() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                assert attibutes != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                return attibutes;
            }
        }

        public @Override boolean isStale() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                return !checkVersion();
            }
        }

        // There can be concurrent modifications from different threads operating under
        // document's read lock. See IZ#106069.
        private boolean checkVersion() {
            return this.version == CompoundHighlightsContainer.this.version;
        }
    } // End of Seq class

    private static final class CacheBoundaries implements DocumentListener {

        private final OffsetGapList<OffsetGapList.Offset> boundaries;
        private final Document doc;

        @SuppressWarnings("LeakingThisInConstructor")
        public CacheBoundaries(Document doc) {
            this.boundaries = new OffsetGapList<OffsetGapList.Offset>(false);
            this.doc = doc;
            this.doc.addDocumentListener(WeakListeners.document(this, this.doc));
        }

        public int getLowerBoundary() {
            if (boundaries.size() == 2) {
                OffsetGapList.Offset lower = boundaries.get(0);
                int lowerOffset = lower.getOffset();
                return lowerOffset >= doc.getLength() ? -1 : lowerOffset;
            } else {
                return -1;
            }
        }

        public int getUpperBoundary() {
            if (boundaries.size() == 2) {
                OffsetGapList.Offset higher = boundaries.get(1);
                int higherOffset = higher.getOffset();
                return higherOffset >= doc.getLength() ? Integer.MAX_VALUE : higherOffset;
            } else {
                return -1;
            }
        }

        public void setBoundaries(int lowerOffset, int higherOffset) {
            boundaries.clear();
            boundaries.add(new OffsetGapList.Offset(lowerOffset));
            boundaries.add(new OffsetGapList.Offset(Math.min(higherOffset, doc.getLength() + 1)));
        }

        public @Override void insertUpdate(DocumentEvent e) {
            boundaries.defaultInsertUpdate(e.getOffset(), e.getLength());
        }

        public @Override void removeUpdate(DocumentEvent e) {
            boundaries.defaultRemoveUpdate(e.getOffset(), e.getLength());
        }

        public @Override void changedUpdate(DocumentEvent e) {
            // ignored
        }

    } // End of CacheBoundaries class
}
