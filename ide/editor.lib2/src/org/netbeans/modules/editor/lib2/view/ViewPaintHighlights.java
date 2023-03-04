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
package org.netbeans.modules.editor.lib2.view;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.editor.lib2.highlighting.CompoundAttributes;
import org.netbeans.modules.editor.lib2.highlighting.HighlightItem;
import org.netbeans.modules.editor.lib2.highlighting.HighlightsList;
import org.netbeans.spi.editor.highlighting.SplitOffsetHighlightsSequence;

/**
 * Special highlights sequence used for painting of individual views.
 * <br>
 * It merges together highlights contained in views (as attributes) together
 * with extra painting highlights (from highlighting layers that do not change metrics).
 * <br>
 * It "covers" even non-highlighted areas by returning null from {@link #getAttributes()}.
 * <br>
 * The instance can only be used by a single thread.
 *
 * @author mmetelka
 */
class ViewPaintHighlights implements SplitOffsetHighlightsSequence {
    
    // -J-Dorg.netbeans.modules.editor.lib2.view.ViewPaintHighlights.level=FINE
    private static final Logger LOG = Logger.getLogger(ViewPaintHighlights.class.getName());

    private static final HighlightItem[] EMPTY = new HighlightItem[0];

    /** All paint highlights in the area being painted. */
    private final HighlightsList paintHighlights;
    
    /** Current index in paint highlights. */
    private int phIndex;
    
    /** Current paint highlight (the one pointed by phIndex) start offset. */
    private int phStartOffset;
    private int phStartSplitOffset;
    
    /** Current paint highlight (the one pointed by phIndex) end offset. */
    private int phEndOffset;
    private int phEndSplitOffset;
    
    /** Current paint highlight (the one pointed by phIndex) attributes (or null). */
    private AttributeSet phAttrs;
    
    private int viewEndOffset;
    
    /** Items of view's compound attributes. */
    private HighlightItem[] cahItems;
    
    /** Index of current compoundAttrs highlight. It's -1 for regular attrs or no attrs. */
    private int cahIndex;
    
    /** End offset of current compoundAttrs highlight. */
    private int cahEndOffset;
    private int cahEndSplitOffset;
    
    /** Attributes (or null) of current compoundAttrs highlight. */
    private AttributeSet cahAttrs;
    
    private int offsetDiff;
    
    /** Start offset of highlight currently provided by this highlights sequence. */
    private int hiStartOffset;
    private int hiStartSplitOffset;
    
    /** End offset of highlight currently provided by this highlights sequence. */
    private int hiEndOffset;
    private int hiEndSplitOffset;
    
    /** Attributes (or null) of highlight currently provided by this highlights sequence. */
    private AttributeSet hiAttrs;

    ViewPaintHighlights(HighlightsList paintHighlights) {
        this.paintHighlights = paintHighlights;
        updatePH(0);
    }
    
    /**
     * Reset paint highlights for the given view.
     *
     * @param view child view for which the highlights are obtained.
     * @param shift shift inside the view where the computed highlights should start.
     */
    void reset(EditorView view, int shift) {
        assert (shift >= 0) : "shift=" + shift + " < 0"; // NOI18N
        int viewStartOffset = view.getStartOffset();
        int viewLength = view.getLength();
        assert (shift < viewLength) : "shift=" + shift + " >= viewLength=" + viewLength; // NOI18N
        viewEndOffset = viewStartOffset + viewLength;
        AttributeSet attrs = view.getAttributes();
        int startOffset = viewStartOffset + shift;
        if (ViewUtils.isCompoundAttributes(attrs)) {
            CompoundAttributes cAttrs = (CompoundAttributes) attrs;
            offsetDiff = viewStartOffset - cAttrs.startOffset();
            cahItems = cAttrs.highlightItems();
            if (shift == 0) {
                cahIndex = 0;
            } else {
                int cahOffset = startOffset - offsetDiff; // Orig offset inside cAttrs
                cahIndex = findCAHIndex(cahOffset); // Search in orig.offsets
            }
            if (cahIndex >= cahItems.length) {
                throw new IllegalStateException("offsetDiff=" + offsetDiff + // NOI18N
                        ", view=" + view + ", shift=" + shift + ", viewStartOffset+shift=" + startOffset + // NOI18N
                        "\ncAttrs:\n" + cAttrs + // NOI18N
                        "\n" + this + "docView:\n" + // NOI18N
                        ((DocumentView) view.getParent().getParent()).toStringDetail());
            }
            HighlightItem cahItem = cahItems[cahIndex];
            cahEndOffset = cahItem.getEndOffset() + offsetDiff;
            cahEndSplitOffset = cahItem.getEndSplitOffset();
//            assert (startOffset < cahEndOffset) : "startOffset=" + startOffset + // NOI18N
//                    " >= cahEndOffset=" + cahEndOffset + "\n" + this; // NOI18N
            cahAttrs = cahItem.getAttributes();

        } else { // Either regular or no attrs
            // offsetDiff will not be used
            cahItems = EMPTY;
            cahIndex = -1;
            cahEndOffset = viewEndOffset;
            cahEndSplitOffset = 0;
            if (attrs == null) {
                cahAttrs = null;
            } else { // regular attrs
                cahAttrs = attrs;
            }
        }
        // Update paint highlight if necessary
        if (startOffset < phStartOffset || phStartSplitOffset != 0) { // Must go back
            updatePH(findPHIndex(startOffset));
        } else if (startOffset > phEndOffset || startOffset == phEndOffset && phEndSplitOffset == 0) { // Must fetch further
            // Should be able to fetch since it should not fetch beyond requested area size
//            if (startOffset >= paintHighlights.endOffset()) {
//                throw new IllegalStateException("startOffset=" + startOffset + // NOI18N
//                        " >= paintHighlights.endOffset()=" + paintHighlights.endOffset() + // NOI18N
//                        "\n" + this + "docView:\n" + // NOI18N
//                        ((DocumentView)view.getParent().getParent()).toStringDetail());
//            }
            fetchNextPH();
            if (startOffset > phEndOffset || startOffset == phEndOffset && phEndSplitOffset == 0) { // If still unsuccessful do bin-search positioning
                updatePH(findPHIndex(startOffset));
            }
        } // Within current PH
        hiStartOffset = hiEndOffset = startOffset;
        hiStartSplitOffset = hiEndSplitOffset = 0;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("ViewPaintHighlights.reset: view=" + view.getDumpId() + // NOI18N
                    ", startOffset=" + startOffset + " attrs=" + attrs + "\n" + // NOI18N
                    "    cahEndOffset=" + cahEndOffset + '_' + cahEndSplitOffset + // NOI18N
                    ", phStartOffset=" + phStartOffset + '_' + phStartSplitOffset + // NOI18N
                    ", phEndOffset=" + phEndOffset + '_' + phEndSplitOffset + "\n"  // NOI18N
            );
        }
    }
    
    @Override
    public boolean moveNext() {
        if (hiEndOffset >= viewEndOffset) { // Last highlight's end is beyond view's end -> stop
            return false;
        }
        if (hiEndOffset > phEndOffset || (hiEndOffset == phEndOffset && hiEndSplitOffset >= phEndSplitOffset)) {
            fetchNextPH();
        }
        if (hiEndOffset > cahEndOffset || (hiEndOffset == cahEndOffset && hiEndSplitOffset >= cahEndSplitOffset)) {
            // Fetch next CAH
            cahIndex++;
            if (cahIndex >= cahItems.length) {
                hiStartOffset = hiEndOffset = viewEndOffset; // Mark finished
                return false;
            }
            HighlightItem hItem = cahItems[cahIndex];
            cahEndOffset = hItem.getEndOffset() + offsetDiff;
            cahEndSplitOffset = hItem.getEndSplitOffset();
            cahAttrs = hItem.getAttributes();
        }
        // There will certainly be a next highlight
        hiStartOffset = hiEndOffset;
        hiStartSplitOffset = hiEndSplitOffset;
        // Decide whether paint highlight ends lower than compound attrs' one
        if (phEndOffset < cahEndOffset || (phEndOffset == cahEndOffset && phEndSplitOffset <= cahEndSplitOffset)) {
            if (phEndOffset >= viewEndOffset) {
                hiEndOffset = viewEndOffset;
                hiEndSplitOffset = 0;
            } else {
                hiEndOffset = phEndOffset;
                hiEndSplitOffset = phEndSplitOffset;
            }
        } else {
            hiEndOffset = cahEndOffset;
            hiEndSplitOffset = cahEndSplitOffset;
        }
        // Merge (possibly null) attrs (ph over cah)
        hiAttrs = cahAttrs;
        if (phAttrs != null) {
            hiAttrs = (hiAttrs != null) ? AttributesUtilities.createComposite(phAttrs, hiAttrs) : phAttrs;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("ViewPaintHighlights.moveNext-highlight: <" + getStartOffset() + "_" + // NOI18N
                        getStartSplitOffset() + "," + getEndOffset() + "_" + getEndSplitOffset() + "> attrs=" + getAttributes() + "\n"); // NOI18N
        }
        return true;
    }

    @Override
    public int getStartOffset() {
        return hiStartOffset;
    }

    @Override
    public int getStartSplitOffset() {
        return hiStartSplitOffset;
    }

    @Override
    public int getEndOffset() {
        return hiEndOffset;
    }

    @Override
    public int getEndSplitOffset() {
        return hiEndSplitOffset;
    }

    @Override
    public AttributeSet getAttributes() {
        return hiAttrs;
    }

    /**
     * Find index of cAttrs' item "containing" the cahOffset.
     * @param cahOffset offset in original offset space of cAttrs.
     * @return index of hItem.
     */
    private int findCAHIndex(int cahOffset) {
        int low = 0;
        int high = cahItems.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            int hEndOffset = cahItems[mid].getEndOffset();
            if (hEndOffset < cahOffset) {
                low = mid + 1;
            } else if (hEndOffset > cahOffset) {
                high = mid - 1;
            } else { // hEndOffset == offset
                low = mid + 1;
                break;
            }
        }
        return low;
    }

    private void updatePH(int index) {
        phIndex = index;
        if (phIndex > 0) {
            HighlightItem prevPHItem = paintHighlights.get(phIndex - 1);
            phStartOffset = prevPHItem.getEndOffset();
            phStartSplitOffset = prevPHItem.getEndSplitOffset();
        } else {
            phStartOffset = paintHighlights.startOffset();
            phStartSplitOffset = 0;
        }
        HighlightItem phItem = paintHighlights.get(phIndex);
        phEndOffset = phItem.getEndOffset();
        phEndSplitOffset = phItem.getEndSplitOffset();
        phAttrs = phItem.getAttributes();
    }

    private void fetchNextPH() {
        phIndex++;
        if (phIndex >= paintHighlights.size()) {
            throw new IllegalStateException("phIndex=" + phIndex + // NOI18N
                    " >= paintHighlights.size()=" + paintHighlights.size() + // NOI18N
                    "\n" + this); // NOI18N
        }
        phStartOffset = phEndOffset;
        phStartSplitOffset = phEndSplitOffset;
        HighlightItem phItem = paintHighlights.get(phIndex);
        phEndOffset = phItem.getEndOffset();
        phEndSplitOffset = phItem.getEndSplitOffset();
        phAttrs = phItem.getAttributes();
    }
    
    private int findPHIndex(int offset) {
        int low = 0;
        int high = paintHighlights.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1; // mid in the binary search
            HighlightItem phItem = paintHighlights.get(mid);
            int hEndOffset = phItem.getEndOffset();
            if (hEndOffset < offset) {
                low = mid + 1;
            } else if (hEndOffset > offset) {
                high = mid - 1;
            } else { // hEndOffset == offset
                // Search for the first if there would be several with same offset bu different splitOfset
                if (phItem.getEndSplitOffset() != 0) {
                    while (mid > 0) {
                        mid--;
                        phItem = paintHighlights.get(mid);
                        if (phItem.getEndOffset() == offset) {
                            if (phItem.getEndSplitOffset() == 0) { // First one
                                break;
                            }
                        } else { // Use next one
                            mid++;
                            break;
                        }
                    }
                }
                low = mid + 1; // Comparing item's end offset to the given offset so use next item
                break;
            }
        }
        return low;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append("ViewPaintHighlights: ph[").append(phIndex). // NOI18N
                append("]<").append(phStartOffset). // NOI18N
                append(",").append(phEndOffset).append('_').append(phEndSplitOffset). // NOI18N
                append("> attrs=").append(phAttrs).append('\n');
        sb.append("cah[").append(cahIndex).append("]#").append(cahItems.length);
        sb.append(" <?,").append(cahEndOffset).append("> attrs=").append(cahAttrs);
        sb.append(", viewEndOffset=").append(viewEndOffset).
                append(", offsetDiff=").append(offsetDiff);
        sb.append("\npaintHighlights:").append(paintHighlights);
        return sb.toString();
    }
    
}
