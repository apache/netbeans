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

package org.netbeans.modules.editor.lib2.highlighting;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.ReleasableHighlightsContainer;
import org.openide.util.WeakListeners;
import org.netbeans.spi.editor.highlighting.SplitOffsetHighlightsSequence;

/**
 * Compound highlights-layer container that does non-cached direct merging
 * of individual layers' highlights.
 * <br>
 * It's somewhat similar to a view building process in view hierarchy which also maintains
 * next-change-offset where a change in the particular layer occurs and needs to be processed.
 * <br>
 * {@link SplitOffsetHighlightsSequence} are supported and the highlights sequences returned by the container
 * are always instances of this interface.
 *
 * @author Miloslav Metelka
 */
public final class DirectMergeContainer implements HighlightsContainer, HighlightsChangeListener, ReleasableHighlightsContainer {
    
    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.DirectMergeContainer.level=FINE
    private static final Logger LOG = Logger.getLogger(DirectMergeContainer.class.getName());
    
    /**
     * Maximum number of empty highlights (returned from HighlightsSequence)
     * after which the particular layer will no longer be used for compound highlight sequence.
     * This is set to ensure that the whole code won't end up in an infinite loop.
     */
    static final int MAX_EMPTY_HIGHLIGHT_COUNT = 10000;

    private final HighlightsContainer[] layers;
    
    private final boolean covering;
    
    private final List<HighlightsChangeListener> listeners = new CopyOnWriteArrayList<HighlightsChangeListener>();
    
    private final List<Reference<HlSequence>> activeHlSeqs = new ArrayList<Reference<HlSequence>>();
    
    private HighlightsChangeEvent layerEvent;

    /**
     * Construct new direct merge container.
     *
     * @param layers highlight containers to be merged ordered by their importance (subsequent one is over previous one).
     * @param covering whether the highlights of the returned highlights sequence should cover the whole offset range or not.
     * @see CoveringHighlightsSequence
     */
    public DirectMergeContainer(HighlightsContainer[] layers, boolean covering) {
        this.layers = layers;
        this.covering = covering;
        for (int i = 0; i < layers.length; i++) {
            HighlightsContainer layer = layers[i];
            layer.addHighlightsChangeListener(WeakListeners.create(HighlightsChangeListener.class, this, layer));
        }
    }
    
    public HighlightsContainer[] getLayers() {
        return layers;
    }
    
    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        HlSequence hs = new HlSequence(layers, startOffset, endOffset, covering);
        synchronized (activeHlSeqs) {
            activeHlSeqs.add(new WeakReference<HlSequence>(hs));
        }
        return hs;
    }

    @Override
    public void addHighlightsChangeListener(HighlightsChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeHighlightsChangeListener(HighlightsChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void highlightChanged(HighlightsChangeEvent event) {
        layerEvent = event;
        try {
            if (!listeners.isEmpty()) {
                HighlightsChangeEvent thisEvt = new HighlightsChangeEvent(this, event.getStartOffset(), event.getEndOffset());
                for (HighlightsChangeListener l : listeners) {
                    l.highlightChanged(thisEvt);
                }
            }
            synchronized (activeHlSeqs) {
                for (Reference<HlSequence> hlSeqRef : activeHlSeqs) {
                    HlSequence seq = hlSeqRef.get();
                    if (seq != null) {
                        seq.notifyLayersChanged();
                    }
                }
                activeHlSeqs.clear();
            }
        } finally {
            layerEvent = null;
        }
    }
    
    /**
     * Get event from a contained layer which caused highlight change
     * (mainly for debugging purposes).
     * <br>
     * The information is only available during firing to change listeners registered by
     * {@link #addHighlightsChangeListener(org.netbeans.spi.editor.highlighting.HighlightsChangeListener)}.
     * 
     * @return event sent by a layer.
     */
    public HighlightsChangeEvent layerEvent() {
        return layerEvent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        int digitCount = ArrayUtilities.digitCount(layers.length);
        for (int i = 0; i < layers.length; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(layers[i]);
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public void released() {
        for (HighlightsContainer layer : layers) {
            if (layer instanceof ReleasableHighlightsContainer) {
                ((ReleasableHighlightsContainer) layer).released();
            }
        }
    }
    
    static final class HlSequence implements CoveringHighlightsSequence {

        /**
         * Wrappers around layers used to compute merged highlights.
         */
        private final Wrapper[] wrappers;
        
        private final boolean covering;
        
        private int topWrapperIndex;
        
        final int endOffset;
        
        private int mergedHighlightStartOffset;
        
        private int mergedHighlightStartSplitOffset;
        
        private int mergedHighlightEndOffset;
        
        private int mergedHighlightEndSplitOffset;
        
        AttributeSet mergedAttrs;
        
        volatile boolean finished; // Either no more highlights or layers were changed;
        
        public HlSequence(HighlightsContainer[] layers, int startOffset, int endOffset, boolean covering) {
            this.covering = covering;
            this.endOffset = endOffset;
            // Initially set an empty highlight (the values are undefined anyway)
            this.mergedHighlightStartOffset = startOffset;
            this.mergedHighlightEndOffset = startOffset;
            boolean log = LOG.isLoggable(Level.FINE);
            if (log) {
                LOG.fine(dumpId() + " NEW HlSequence for <" + startOffset + "," + endOffset + "> for layers:\n"); // NOI18N
            }
            wrappers = new Wrapper[layers.length];
            for (int i = 0; i < layers.length; i++) {
                HighlightsContainer container = layers[i];
                HighlightsSequence hlSequence = container.getHighlights(startOffset, endOffset);
                Wrapper wrapper = new Wrapper(this, container, hlSequence, endOffset);
                if (wrapper.init(startOffset)) { // For no-highlight wrapper do not include it at all in the array
                    if (log) {
                        LOG.fine("    " + dumpId() + " layer[" + topWrapperIndex + "]: " + container + '\n');
                    }
                    wrappers[topWrapperIndex++] = wrapper;
                } else { // Ignore layer - no highlights in the requested offset range
                    if (log) {
                        LOG.fine("    " + dumpId() + " Skipped layer (no highlights in the requested offset range) " + container + '\n');
                    }
                }
            }
            topWrapperIndex--;
            updateMergeVars(-1, startOffset, 0); // Update all layers to fetch correct values
        }
        
        @Override
        public boolean moveNext() {
            if (finished) {
                return false;
            }
            Wrapper topWrapper;
            int nextHighlightStartOffset = mergedHighlightEndOffset;
            int nextHighlightStartSplitOffset = mergedHighlightEndSplitOffset;
            while ((topWrapper = nextMerge(nextHighlightStartOffset, nextHighlightStartSplitOffset)) != null) {
                // If HS is running in covering mode then return everything including areas with null attrs
                if (covering || topWrapper.mAttrs != null) {
                    mergedHighlightStartOffset = nextHighlightStartOffset;
                    mergedHighlightStartSplitOffset = nextHighlightStartSplitOffset;
                    mergedHighlightEndOffset = topWrapper.mergedNextChangeOffset;
                    mergedHighlightEndSplitOffset = topWrapper.mergedNextChangeSplitOffset;
                    mergedAttrs = topWrapper.mAttrs;
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(dumpId() + ".moveNext: highlight <" + getStartOffset() + "_" + // NOI18N
                            getStartSplitOffset() + "," + getEndOffset() + "_" + getEndSplitOffset() +
                            "> attrs=" + getAttributes() + "\n"); // NOI18N

                    }
                    return true;
                }
                nextHighlightStartOffset = topWrapper.mergedNextChangeOffset;
                nextHighlightStartSplitOffset = topWrapper.mergedNextChangeSplitOffset;
            }
            finished = true;
            return false;
        }

        @Override
        public boolean isCovering() {
            return covering;
        }
        
        @Override
        public int getStartOffset() {
            return mergedHighlightStartOffset;
        }

        @Override
        public int getStartSplitOffset() {
            return mergedHighlightStartSplitOffset;
        }
        
        @Override
        public int getEndOffset() {
            return mergedHighlightEndOffset;
        }

        @Override
        public int getEndSplitOffset() {
            return mergedHighlightEndSplitOffset;
        }
        
        @Override
        public AttributeSet getAttributes() {
            return mergedAttrs;
        }

        void notifyLayersChanged() { // Notify that layers were changed => stop iteration
            finished = true;
        }

        /**
         * Do merge above the given offset.
         *
         * @param offset end of last merged highlight.
         * @param splitOffset end split offset of last merged highlight (accompanying the offset).
         * @return top wrapper containing info about the performed merge or null
         *  if there is zero wrappers.
         */
        Wrapper nextMerge(int offset, int splitOffset) {
            int i = topWrapperIndex;
            for (; i >= 0 && wrappers[i].isMergedNextChangeBelowOrAt(offset, splitOffset); i--) { }
            // i contains first layer which has mergedNextChangeOffset > offset (or same offset but lower split offset)
            return updateMergeVars(i, offset, splitOffset);
        }
        
        /**
         * Update merged vars of wrappers at (startIndex+1) and above.
         *
         * @param startIndex index of first wrapper which has mergedNextChangeOffset above given offset (and split offset)
         *  or -1 if all wrappers need to be updated.
         *  All wrappers above this index will have their mergedNextChangeOffset and mAttrs updated.
         * @param offset current offset at which to update.
         * @param splitOffset current split offset "within" the char at offset.
         * @return top wrapper (wrapper at topWrapperIndex).
         */
        Wrapper updateMergeVars(int startIndex, int offset, int splitOffset) {
            Wrapper wrapper = null;
            int nextChangeOffset;
            int nextChangeSplitOffset;
            AttributeSet lastAttrs;
            if (startIndex < 0) { // No valid layers
                nextChangeOffset = endOffset;
                nextChangeSplitOffset = 0;
                lastAttrs = null;
            } else {
                wrapper = wrappers[startIndex];
                nextChangeOffset = wrapper.mergedNextChangeOffset;
                nextChangeSplitOffset = wrapper.mergedNextChangeSplitOffset;
                lastAttrs = wrapper.mAttrs;
            }
            // Start with first wrapper that needs to be updated
            wrapperIteration:
            for (int i = startIndex + 1; i <= topWrapperIndex; i++) {
                wrapper = wrappers[i];
                if (wrapper.isNextChangeBelowOrAt(offset, splitOffset)) {
                    while (wrapper.updateCurrentState(offset, splitOffset)) { // Check if next highlight fetch is necessary
                        if (!wrapper.fetchNextHighlight()) { // Finished all highlights in sequence
                            removeWrapper(i); // Remove this wrapper (does topWrapperIndex--)
                            // Ensure that the wrapper returned from method is correct after removeWrapper()
                            // topWrapperIndex already decreased by removeWrapper()
                            i--; // Compensate wrapper removal in for(;;)
                            if (i == topWrapperIndex) {
                                // Since "wrapper" variable should return wrapper at current topWrapperIndex
                                // that in this particular case was just removed
                                // then assign current top wrapper explicitly.
                                wrapper = (i >= 0) ? wrappers[i] : null;
                                break wrapperIteration;
                            }
                            continue wrapperIteration;
                        }
                    }
                }
                if (wrapper.isNextChangeBelow(nextChangeOffset, nextChangeSplitOffset)) {
                    nextChangeOffset = wrapper.nextChangeOffset;
                    nextChangeSplitOffset = wrapper.nextChangeSplitOffset;
                }
                wrapper.mergedNextChangeOffset = nextChangeOffset;
                wrapper.mergedNextChangeSplitOffset = nextChangeSplitOffset;
                lastAttrs = (lastAttrs != null)
                        ? ((wrapper.currentAttrs != null)
                            ? AttributesUtilities.createComposite(wrapper.currentAttrs, lastAttrs) // first prior second
                            : lastAttrs)
                        : wrapper.currentAttrs;
                wrapper.mAttrs = lastAttrs;
            }
            return wrapper;
        }
        
        private void removeWrapper(int index) {
            System.arraycopy(wrappers, index + 1, wrappers, index, topWrapperIndex - index);
            wrappers[topWrapperIndex] = null;
            topWrapperIndex--;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("endO=").append(endOffset);
            if (finished) {
                sb.append("; FINISHED");
            } else {
                sb.append(" Merged <").append(mergedHighlightStartOffset).append('_'). // NOI18N
                        append(mergedHighlightStartSplitOffset).append(// NOI18N
                        ",").append(mergedHighlightEndOffset).append('_'). // NOI18N
                        append(mergedHighlightEndSplitOffset).append(">"); // NOI18N
            }
            sb.append('\n');
            int digitCount = ArrayUtilities.digitCount(topWrapperIndex + 1);
            for (int i = 0; i <= topWrapperIndex; i++) {
                sb.append("  ");
                ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
                sb.append(wrappers[i]);
                sb.append('\n');
            }
            return sb.toString();
        }
        
        String dumpId() {
            return "DMC$HS@" + Integer.toHexString(System.identityHashCode(this));
        }

    }


    static final class Wrapper {
        
        private final HlSequence parentSequence; // For logging purposes only

        /**
         * Layer over which layerSequence is constructed (for debugging purposes).
         */
        final HighlightsContainer layer;
        
        /**
         * Highlights sequence for layer corresponding to this wrapper.
         */
        final HighlightsSequence layerSequence;
        
        /**
         * Highlights sequence supporting coloring of characters inside tabs or newlines.
         */
        final SplitOffsetHighlightsSequence splitOffsetLayerSequence;
        
        /**
         * End offset of the region on which upper hlSequence operates so the highlights
         * returned by layerSequence should adhere to this limit too.
         */
        final int endOffset;
        
        /**
         * Start offset of the last fetched highlight.
         */
        int hlStartOffset;
        
        /**
         * Possible split offset accompanying hlStartOffset or zero for non-SplitOffsetHighlightsSequence.
         */
        int hlStartSplitOffset;
        
        /**
         * End offset of the last fetched highlight.
         */
        int hlEndOffset;
        
        /**
         * Possible split offset accompanying hlEndOffset or zero for non-SplitOffsetHighlightsSequence.
         */
        int hlEndSplitOffset;

        /**
         * Attributes of the last fetched highlight.
         */
        AttributeSet hlAttrs;
        
        /**
         * Offset where a change in highlighting for the current layer will occur.
         * If currently processed offset is below current highlight (fetched into hlStartOffset and hlEndOffset)
         * then the value is set to hlStartOffset.
         * For an offset inside current highlight the value will be set to hlEndOffset.
         * Offset above hlEndOffset will trigger a next highlight fetching.
         */
        int nextChangeOffset;
        
        /**
         * If splitOffsetLayerSequence != null then (similarly to nextChangeOffset)
         * this is set either to start split offset of the next highlight
         * or (if the offset and its split offset are inside current highlight) then
         * this variable is set to end split offset of the current highlight
         * or a next highlight will be fetched (if current offset and split offset are above the highlight).
         */
        int nextChangeSplitOffset;
        
        /**
         * Attributes for an offset: when before hlStartOffset it's null.
         * Otherwise it's hlAttrs.
         */
        AttributeSet currentAttrs;
        
        /**
         * Merged next change offset: minimum of nextChangeOffset from all
         * wrappers below this one in the wrappers array.
         */
        int mergedNextChangeOffset;
        
        /**
         * Merged next change split offset - possible split offset accompanying mergedNextChangeOffset or zero.
         */
        int mergedNextChangeSplitOffset;
        
        /**
         * Merged attributes: merge of currentAttrs from all
         * wrappers below this one in the wrappers array.
         */
        AttributeSet mAttrs;
        
        private int emptyHighlightCount;
        
        
        public Wrapper(HlSequence parent, HighlightsContainer layer, HighlightsSequence layerSequence, int endOffset) {
            this.parentSequence = parent;
            this.layer = layer;
            this.layerSequence = layerSequence;
            this.splitOffsetLayerSequence = (layerSequence instanceof SplitOffsetHighlightsSequence) ? (SplitOffsetHighlightsSequence) layerSequence : null;
            this.endOffset = endOffset;
        }
        
        boolean init(int startOffset) {
            do {
                if (!fetchNextHighlight()) {
                    return false;
                }
            } while (hlEndOffset < startOffset || hlEndOffset == startOffset && hlEndSplitOffset == 0); // Exclude any possible highlights ending below startOffset
            updateCurrentState(startOffset, 0);
            return true;
        }
        
        /**
         * Whether next change offset and split offset of this wrapper are below the given parameters.
         *
         * @param offset current offset.
         * @param splitOffset current split offset (accompanying the current offset).
         * @return true if next change offset and split offset of this wrapper are below the given parameters
         *  or false otherwise.
         */
        boolean isNextChangeBelow(int offset, int splitOffset) {
            return nextChangeOffset < offset || (nextChangeOffset == offset && nextChangeSplitOffset < splitOffset);
        }

        /**
         * Whether next change offset and split offset of this wrapper are below the given parameters
         * or right at them.
         *
         * @param offset current offset.
         * @param splitOffset current split offset (accompanying the current offset).
         * @return true if next change offset and split offset of this wrapper are below or right at the given parameters
         *  or false otherwise.
         */
        boolean isNextChangeBelowOrAt(int offset, int splitOffset) {
            return nextChangeOffset < offset || (nextChangeOffset == offset && nextChangeSplitOffset <= splitOffset);
        }

        /**
         * Whether merged next change offset and split offset of this wrapper are below the given parameters
         * or right at them.
         *
         * @param offset current offset.
         * @param splitOffset current split offset (accompanying the current offset).
         * @return true if next change offset and split offset of this wrapper are below or right at the given parameters
         *  or false otherwise.
         */
        boolean isMergedNextChangeBelowOrAt(int offset, int splitOffset) {
            return mergedNextChangeOffset < offset || (mergedNextChangeOffset == offset && mergedNextChangeSplitOffset <= splitOffset);
        }

        /**
         * Update currentAttrs and nextChangeOffset according to given offset.
         * @param offset offset to which to update
         * @param splitOffset split offset inside tab or newline character on the given offset.
         * @return true if the offset is >= hlEndOffset and so fetchNextHighlight() is necessary.
         */
        boolean updateCurrentState(int offset, int splitOffset) {
            if (offset < hlStartOffset) { // offset before current hl start
                currentAttrs = null;
                nextChangeOffset = hlStartOffset;
                nextChangeSplitOffset = hlStartSplitOffset;
                return false;
            } else if (offset == hlStartOffset) { // inside hl (assuming call after fetchNextHighlight())
                if (splitOffset < hlStartSplitOffset) {
                    currentAttrs = null;
                    nextChangeOffset = hlStartOffset;
                    nextChangeSplitOffset = hlStartSplitOffset;
                    return false;
                    
                } else { // Above (or at) highlight's start
                    if (offset < hlEndOffset || (offset == hlEndOffset && splitOffset < hlEndSplitOffset)) {
                        currentAttrs = hlAttrs;
                        nextChangeOffset = hlEndOffset;
                        nextChangeSplitOffset = hlEndSplitOffset;
                        return false;
                    } else {
                        return true; // Fetch next highlight
                    }
                } // else: fetch next highlight
            } else if (offset < hlEndOffset || (offset == hlEndOffset && splitOffset < hlEndSplitOffset)) {
                currentAttrs = hlAttrs;
                nextChangeOffset = hlEndOffset;
                nextChangeSplitOffset = hlEndSplitOffset;
                return false;
            } else { // Above hlEndOffset (or hlEndSplitOffset) => fetch next highlight
                return true; // Fetch next highlight
            }
        }
        
        /**
         * Fetch a next highlight for this wrapper.
         * @param offset
         * @return true if highlight fetched successfully or false if there are no more highlights.
         */
        boolean fetchNextHighlight() {
            while (layerSequence.moveNext()) { // Loop to allow for skip over empty highlights
                hlStartOffset = layerSequence.getStartOffset();
                if (hlStartOffset < hlEndOffset) { // Invalid layer: next highlight overlaps previous one
                    // To prevent infinite loops finish this HL
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(parentSequence.dumpId() + ".wrapper.fetchNextHighlight: Disabled an invalid highlighting layer: hlStartOffset=" + hlStartOffset + // NOI18N
                            " < previous hlEndOffset=" + hlEndOffset + " for layer=" + layer + '\n'); // NOI18N
                    }
                    return false;
                }
                hlEndOffset = layerSequence.getEndOffset();
                if (splitOffsetLayerSequence != null) {
                    hlStartSplitOffset = splitOffsetLayerSequence.getStartSplitOffset();
                    hlEndSplitOffset = splitOffsetLayerSequence.getEndSplitOffset();
                    // Do not perform extra checking of validity (non-overlapping with previous highlight
                    //  and validity of split offsets since it should not be crucial
                    //  for proper functioning of updateCurrentState() method.
                } // else hlStartSplitOffset and hlEndSplitOffset are always zero in the wrapper
                if (hlEndOffset <= hlStartOffset) {
                    if (hlEndOffset < hlStartOffset) { // Invalid highlight: end offset before start offset
                        // To prevent infinite loops finish this HL
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine(parentSequence.dumpId() + ".wrapper.fetchNextHighlight: Disabled an invalid highlighting layer: hlStartOffset=" + hlStartOffset + // NOI18N
                                " > hlEndOffset=" + hlEndOffset + " for layer=" + layer + "\n"); // NOI18N
                        }
                        return false;
                    }
                    if (hlEndSplitOffset <= hlStartSplitOffset) { // hlStartOffset == hlEndOffset
                        emptyHighlightCount++;
                        if (emptyHighlightCount >= MAX_EMPTY_HIGHLIGHT_COUNT) {
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine(parentSequence.dumpId() + ".wrapper.fetchNextHighlight: Disabled an invalid highlighting layer: too many empty highlights=" + // NOI18N
                                        + emptyHighlightCount + "\n"); // NOI18N
                            }
                            return false;
                        }
                        continue; // Fetch next highlight
                    }
                }
                if (hlEndOffset > endOffset) {
                    if (hlStartOffset >= endOffset) {
                        return false;
                    }
                    hlEndOffset = endOffset; // Limit the highlight to endOffset - it should still remain non-empty
                }
                hlAttrs = layerSequence.getAttributes();
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.fine("  " + parentSequence.dumpId() + " layer-highlight: <" + hlStartOffset + '_' + hlStartSplitOffset + // NOI18N
                            "," + hlEndOffset + '_' + hlEndSplitOffset + "> for " + layer + '\n'); // NOI18N
                }
                return true; // Valid highlight fetched
            }
            return false; // No more highlights
        }

        @Override
        public String toString() {
            return  "MergedChangeOffset(SplitOffset)=" + mergedNextChangeOffset + '(' + mergedNextChangeSplitOffset + // NOI18N
                    "), NextCO(SO)=" + nextChangeOffset + '(' + nextChangeSplitOffset + // NOI18N
                    "), HL:<" + hlStartOffset + '(' + hlStartSplitOffset + ")," + hlEndOffset + '(' + hlEndSplitOffset + ")>"; // NOI18N
        }

    }
    
}
