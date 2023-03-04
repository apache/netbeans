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

package org.netbeans.spi.editor.highlighting.support;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.lib.editor.util.GapList;
import org.netbeans.spi.editor.highlighting.*;

/**
 * A bag of highlighted areas specified by their document <code>Position</code>s.
 *
 * <p>The highlighted areas (highlights) are determined by their starting and ending
 * positions in a document and the set of attributes that should be used for rendering
 * that area. The highlights can be arbitrarily added to and remove from the bag.
 * 
 * <p>The <code>PositionsBag</code> is designed to work over a single
 * document, which is passed in to the constructor. All the <code>Position</code>s
 * accepted by various methods in this class must refer to positions within
 * this document. Since there is no way how this could be checked it is up to
 * the users of this class to make sure that <code>Position</code>s they pass in
 * to the bag are from the same <code>Document</code>, which they used for creating
 * the bag.
 *
 * <p>The <code>PositionsBag</code> can operate in two modes depending on a
 * value passed in its construtor. The mode determines how the bag will treat
 * newly added highlights that overlap with existing highlights in the bag.
 * 
 * <p><b>Trimming mode</b>:
 * In the trimming mode the bag will <i>trim</i> any existing highlights that
 * would overlap with a newly added highlight. In this mode the newly
 * added highlights always replace the existing highlights if they overlap.
 * 
 * <p><b>Merging mode</b>:
 * In the merging mode the bag will <i>merge</i> attributes of the overlapping highlights.
 * The area where the new highlight overlaps with some existing highlights will
 * then constitute a new highlight and its attributes will be a composition of
 * the attributes of both the new and existing highlight. Should there be attributes
 * with the same name the attribute values from the newly added highlight will take
 * precedence.
 *
 * @author Vita Stejskal
 */
public final class PositionsBag extends AbstractHighlightsContainer {

    private static final Logger LOG = Logger.getLogger(PositionsBag.class.getName());
    
    private final Document document;
    private final boolean mergeHighlights;

    private final GapList<Position> marks = new GapList<Position>();
    private final GapList<AttributeSet> attributes = new GapList<AttributeSet>();
    private long version = 0;
    
    /**
     * Creates a new instance of <code>PositionsBag</code>, which trims highlights
     * as they are added. It calls the {@link #PositionsBag(Document, boolean)} constructore
     * passing <code>false</code> as a parameter.
     * 
     * @param document           The document that should be highlighted.
     */
    public PositionsBag(Document document) {
        this(document, false);
    }
    
    /**
     * Creates a new instance of <code>PositionsBag</code>.
     *
     * @param document           The document that should be highlighted.
     * @param mergeHighlights    Determines whether highlights should be merged
     *                           or trimmed.
     */
    public PositionsBag(Document document, boolean mergeHighlights) {
        this.document = document;
        this.mergeHighlights = mergeHighlights;
    }
    
    /**
     * Adds a highlight to this bag. The highlight is specified by its staring
     * and ending <code>Position</code> and by its attributes. Adding a highlight that overlaps
     * with one or more existing highlights can have a different result depending
     * on the value of the <code>mergingHighlights</code> parameter used for
     * constructing this bag.
     *
     * @param startPosition    The beginning of the highlighted area.
     * @param endPosition      The end of the highlighted area.
     * @param attributes       The attributes to use for highlighting.
     */
    public void addHighlight(Position startPosition, Position endPosition, AttributeSet attributes) {
        int [] offsets;
        
        synchronized (marks) {
            offsets = addHighlightImpl(startPosition, endPosition, attributes);
            if (offsets != null) {
                version++;
            }
        }
        
        if (offsets != null) {
            fireHighlightsChange(offsets[0], offsets[1]);
        }
    }
    
    /**
     * Adds all highlights from the bag passed in. This method is equivalent
     * to calling <code>addHighlight</code> for all the highlights in the
     * <code>bag</code> except that the changes are done atomically.
     *
     * @param bag    The highlights that will be atomically added to this bag.
     */
    public void addAllHighlights(PositionsBag bag) {
        int [] offsets;
        
        synchronized (marks) {
            offsets = addAllHighlightsImpl(bag);
            if (offsets != null) {
                version++;
            }
        }
        
        if (offsets != null) {
            fireHighlightsChange(offsets[0], offsets[1]);
        }
    }

    /**
     * Resets this sequence to use the new set of highlights. This method drops
     * all the existing highlights in this bag and adds all highlights from
     * the <code>bag</code> passed in as a parameter. The changes are done atomically.
     *
     * @param bag    The new highlights.
     */
    public void setHighlights(PositionsBag bag) {
        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;

        synchronized (marks) {
            int [] clearedArea = clearImpl();
            int [] populatedArea = null;
            
            GapList<Position> newMarks = bag.getMarks();
            GapList<AttributeSet> newAttributes = bag.getAttributes();

            synchronized (newMarks) {
                for(Position mark : newMarks) {
                    marks.add(marks.size(), mark);
                }

                for(AttributeSet attrib : newAttributes) {
                    attributes.add(attributes.size(), attrib);
                }

                if (marks.size() > 0) {
                    populatedArea = new int [] { 
                        marks.get(0).getOffset(), 
                        marks.get(marks.size() - 1).getOffset() 
                    };
                }
            }
            
            if (clearedArea != null) {
                changeStart = clearedArea[0];
                changeEnd = clearedArea[1];
            }

            if (populatedArea != null) {
                if (changeStart == Integer.MAX_VALUE || changeStart > populatedArea[0]) {
                    changeStart = populatedArea[0];
                }
                if (changeEnd == Integer.MIN_VALUE || changeEnd < populatedArea[1]) {
                    changeEnd = populatedArea[1];
                }
            }
            
            if (changeStart < changeEnd) {
                version++;
            }
        }
        
        if (changeStart < changeEnd) {
            fireHighlightsChange(changeStart, changeEnd);
        }
    }
    
    /**
     * Removes highlights in the specific area. All existing highlights
     * that are positioned within the area specified by the <code>startOffset</code> and
     * <code>endOffset</code> parameters are removed from this bag. The highlights
     * that only partialy overlap with the area are treated according to the value
     * of the <code>clip</code> parameter.
     * 
     * <ul>
     * <li>If <code>clip == true</code> : the overlapping highlights will remain
     * in this sequence but will be clipped so that they do not overlap anymore
     * <li>If <code>clip == false</code> : the overlapping highlights will be
     * removed from this sequence
     * </ul>
     *
     * @param startPosition    The beginning of the area to clear.
     * @param endPosition      The end of the area to clear.
     * @param clip             Whether to clip the partially overlapping highlights.
     */
    public void removeHighlights(Position startPosition, Position endPosition, boolean clip) {
        if (!clip) {
            removeHighlights(startPosition.getOffset(), endPosition.getOffset());
            return;
        }

        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;

        // Ignore empty areas, we are clipping
        if (startPosition.getOffset() == endPosition.getOffset()) {
            return;
        } else {
            assert startPosition.getOffset() < endPosition.getOffset() : 
                "Start position must be less than the end position"; //NOI18N
        }
        
        synchronized (marks) {
            if (marks.isEmpty()) {
                return;
            }

            int startIdx = indexBeforeOffset(startPosition.getOffset());
            int endIdx = indexBeforeOffset(endPosition.getOffset(), startIdx < 0 ? 0 : startIdx, marks.size() - 1);

//            System.out.println("removeHighlights(" + startOffset + ", " + endOffset + ", " + clip + ") : startIdx = " + startIdx + ", endIdx = " + endIdx);

            if (startIdx == endIdx) {
                if (startIdx != -1 && attributes.get(startIdx) != null) {
                    AttributeSet original = attributes.get(startIdx);

                    if (marks.get(startIdx).getOffset() == startPosition.getOffset()) {
                        marks.set(startIdx, endPosition);
                        attributes.set(startIdx, original);
                    } else {
                        marks.add(startIdx + 1, startPosition);
                        attributes.add(startIdx + 1, null);
                        marks.add(startIdx + 2, endPosition);
                        attributes.add(startIdx + 2, original);
                    }

                    changeStart = startPosition.getOffset();
                    changeEnd = endPosition.getOffset();
                }

                // make sure nothing gets removed
                startIdx = Integer.MAX_VALUE;
                endIdx = Integer.MIN_VALUE;
            } else {
                assert endIdx != -1 : "Invalid range: startIdx = " + startIdx + " endIdx = " + endIdx;

                if (attributes.get(endIdx) != null) {
                    marks.set(endIdx, endPosition);
                    changeEnd = endPosition.getOffset();
                    endIdx--;
                }

                if (startIdx != -1 && attributes.get(startIdx) != null) {
                    if (startIdx + 1 < endIdx) {
                        startIdx++;
                        marks.set(startIdx, startPosition);
                        attributes.set(startIdx, null);
                    } else {
                        if (marks.get(startIdx).getOffset() < startPosition.getOffset()) {
                            if (startIdx + 1 == endIdx) {
                                startIdx++;
                                marks.set(startIdx, startPosition);
                                attributes.set(startIdx, null);
                            } else {
                                startIdx++;
                                marks.add(startIdx, startPosition);
                                attributes.add(startIdx, null);
                            }
                        } else {
                            if (startIdx == 0 || attributes.get(startIdx - 1) == null) {
                                startIdx--;
                            } else {
                                marks.set(startIdx, startPosition);
                                attributes.set(startIdx, null);
                            }
                        }
                    }
                    changeStart = startPosition.getOffset();
                }
                startIdx++;
            }

            if (startIdx <= endIdx) {
                if (changeStart == Integer.MAX_VALUE) {
                    changeStart = marks.get(startIdx).getOffset();
                }
                if (changeEnd == Integer.MIN_VALUE) {
                    changeEnd = marks.get(endIdx).getOffset();
                }
                marks.remove(startIdx, endIdx - startIdx + 1);
                attributes.remove(startIdx, endIdx - startIdx + 1);
            }
            
            if (changeStart < changeEnd) {
                version++;
            }
        }

        if (changeStart < changeEnd) {
            fireHighlightsChange(changeStart, changeEnd);
        }
    }

    /**
     * Removes highlights in the specific area. All existing highlights
     * that are positioned within the area specified by the <code>startOffset</code> and
     * <code>endOffset</code> parameters are removed from this sequence. The highlights
     * that only partialy overlap with the area will be removed as well.
     *
     * @param startOffset    The beginning of the area to clear.
     * @param endOffset      The end of the area to clear.
     */
    public void removeHighlights(int startOffset, int endOffset) {
        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;

        // We are not clipping, allow removal of empty areas
        assert startOffset <= endOffset : "Start position must be less than or equal to the end position"; //NOI18N
        
        synchronized (marks) {
            if (marks.isEmpty()) {
                return;
            }
            
            int startIdx = indexBeforeOffset(startOffset);
            int endIdx = indexBeforeOffset(endOffset, startIdx < 0 ? 0 : startIdx, marks.size() - 1);
            
//            System.out.println("removeHighlights(" + startOffset + ", " + endOffset + ", " + clip + ") : startIdx = " + startIdx + ", endIdx = " + endIdx);
            
            if (startIdx == -1 || attributes.get(startIdx) == null) {
                startIdx++;
            } else if (startIdx > 0 && attributes.get(startIdx - 1) != null) {
                attributes.set(startIdx, null);
                startIdx++;
            }

            if (endIdx != -1 && attributes.get(endIdx) != null) {
                if (marks.get(endIdx).getOffset() < endOffset) {
                    if (endIdx + 1 >= attributes.size() || attributes.get(endIdx + 1) == null) {
                        endIdx++;
                    }
                } else {
                    endIdx--;
                }
            }
            
            if (startIdx <= endIdx) {
                if (changeStart == Integer.MAX_VALUE) {
                    changeStart = marks.get(startIdx).getOffset();
                }
                if (changeEnd == Integer.MIN_VALUE) {
                    changeEnd = marks.get(endIdx).getOffset();
                }
                marks.remove(startIdx, endIdx - startIdx + 1);
                attributes.remove(startIdx, endIdx - startIdx + 1);
            }
            
            if (changeStart < changeEnd) {
                version++;
            }
        }
        
        if (changeStart < changeEnd) {
            fireHighlightsChange(changeStart, changeEnd);
        }
    }

    /**
     * Gets highlights from an area of a document. The <code>HighlightsSequence</code> is
     * computed using all the highlights present in this bag between the
     * <code>startOffset</code> and <code>endOffset</code>.
     *
     * @param startOffset  The beginning of the area.
     * @param endOffset    The end of the area.
     *
     * @return The <code>HighlightsSequence</code> which iterates through the
     *         highlights in the given area of this bag.
     */
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (LOG.isLoggable(Level.FINE) && !(startOffset < endOffset)) {
            LOG.fine("startOffset must be less than endOffset: startOffset = " + //NOI18N
                startOffset + " endOffset = " + endOffset); //NOI18N
        }
        
        synchronized (marks) {
            return new Seq(version, startOffset, endOffset);
        }
    }

    /**
     * Removes all highlights previously added to this bag.
     */
    public void clear() {
        int [] clearedArea;
        
        synchronized (marks) {
            clearedArea = clearImpl();
            if (clearedArea != null) {
                version++;
            }
        }
        
        if (clearedArea != null) {
            fireHighlightsChange(clearedArea[0], clearedArea[1]);
        }
    }
    
    // ----------------------------------------------------------------------
    //  Package private API
    // ----------------------------------------------------------------------
    
    /* package */ GapList<Position> getMarks() {
        return marks;
    }

    /* package */ GapList<AttributeSet> getAttributes() {
        return attributes;
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private int [] addHighlightImpl(Position startPosition, Position endPosition, AttributeSet attributes) {
        if (startPosition.getOffset() == endPosition.getOffset()) {
            return null;
        } else {
            assert startPosition.getOffset() < endPosition.getOffset() : 
                "Start position must be before the end position."; //NOI18N
            assert attributes != null : "Highlight attributes must not be null."; //NOI18N
        }

        if (mergeHighlights) {
            merge(startPosition, endPosition, attributes);
        } else {
            trim(startPosition, endPosition, attributes);
        }

        return new int [] { startPosition.getOffset(), endPosition.getOffset() };
    }
    
    private void merge(Position startPosition, Position endPosition, AttributeSet attrSet) {
        AttributeSet lastKnownAttributes = null;
        int startIdx = indexBeforeOffset(startPosition.getOffset());
        if (startIdx < 0) {
            startIdx = 0;
            marks.add(startIdx, startPosition);
            attributes.add(startIdx, attrSet);
        } else {
            Position mark = marks.get(startIdx);
            AttributeSet markAttribs = attributes.get(startIdx);
            AttributeSet newAttribs = markAttribs == null ? attrSet : AttributesUtilities.createComposite(attrSet, markAttribs);
            lastKnownAttributes = attributes.get(startIdx);

            if (mark.getOffset() == startPosition.getOffset()) {
                attributes.set(startIdx, newAttribs);
            } else {
                startIdx++;
                marks.add(startIdx, startPosition);
                attributes.add(startIdx, newAttribs);
            }
        }

        for(int idx = startIdx + 1; ; idx++) {
            if (idx < marks.size()) {
                Position mark = marks.get(idx);

                if (mark.getOffset() < endPosition.getOffset()) {
                    lastKnownAttributes = attributes.get(idx);
                    attributes.set(idx, lastKnownAttributes == null ? 
                        attrSet : 
                        AttributesUtilities.createComposite(attrSet, lastKnownAttributes));
                } else {
                    if (mark.getOffset() > endPosition.getOffset()) {
                        marks.add(idx, endPosition);
                        attributes.add(idx, lastKnownAttributes);
                    }
                    break;
                }
            } else {
                marks.add(idx, endPosition);
                attributes.add(idx, lastKnownAttributes);
                break;
            }
        }
    }

    private void trim(Position startPosition, Position endPosition, AttributeSet attrSet) {
        int startIdx = indexBeforeOffset(startPosition.getOffset());
        int endIdx = indexBeforeOffset(endPosition.getOffset(), startIdx < 0 ? 0 : startIdx, marks.size() - 1);
        
//        System.out.println("trim(" + startOffset + ", " + endOffset + ") : startIdx = " + startIdx + ", endIdx = " + endIdx);
        
        if (startIdx == endIdx) {
            AttributeSet original = null;
            if (startIdx != -1 && attributes.get(startIdx) != null) {
                original = attributes.get(startIdx);
            }
            
            if (startIdx != -1 && marks.get(startIdx).getOffset() == startPosition.getOffset()) {
                attributes.set(startIdx, attrSet);
            } else {
                startIdx++;
                marks.add(startIdx, startPosition);
                attributes.add(startIdx, attrSet);
            }
            
            startIdx++;
            marks.add(startIdx, endPosition);
            attributes.add(startIdx, original);
        } else {
            assert endIdx != -1 : "Invalid range: startIdx = " + startIdx + " endIdx = " + endIdx;

            marks.set(endIdx, endPosition);
            attributes.set(endIdx, attributes.get(endIdx));
            endIdx--;

            if (startIdx != -1 && marks.get(startIdx).getOffset() == startPosition.getOffset()) {
                attributes.set(startIdx, attrSet);
            } else if (startIdx + 1 <= endIdx) {
                startIdx++;
                marks.set(startIdx, startPosition);
                attributes.set(startIdx, attrSet);
            } else {
                startIdx++;
                marks.add(startIdx, startPosition);
                attributes.add(startIdx, attrSet);
            }
            startIdx++;
            
            if (startIdx <= endIdx) {
                marks.remove(startIdx, endIdx - startIdx + 1);
                attributes.remove(startIdx, endIdx - startIdx + 1);
            }
        }
    }
    
    private int [] addAllHighlightsImpl(PositionsBag bag) {
        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;

        GapList<Position> newMarks = bag.getMarks();
        GapList<AttributeSet> newAttributes = bag.getAttributes();
        
        for (int i = 0 ; i + 1 < newMarks.size(); i++) {
            Position mark1 = newMarks.get(i);
            Position mark2 = newMarks.get(i + 1);
            AttributeSet attrSet = newAttributes.get(i);
            
            if (attrSet == null) {
                // skip empty highlight
                continue;
            }
            
            addHighlightImpl(mark1, mark2, attrSet);

            if (changeStart == Integer.MAX_VALUE) {
                changeStart = mark1.getOffset();
            }
            changeEnd = mark2.getOffset();
        }

        if (changeStart != Integer.MAX_VALUE && changeEnd != Integer.MIN_VALUE) {
            return new int [] { changeStart, changeEnd };
        } else {
            return null;
        }
    }
    
    private int [] clearImpl() {
        if (!marks.isEmpty()) {
            int changeStart = marks.get(0).getOffset();
            int changeEnd = marks.get(marks.size() - 1).getOffset();

            marks.clear();
            attributes.clear();

            return new int [] { changeStart, changeEnd };
        } else {
            return null;
        }
    }

    private int indexBeforeOffset(int offset, int low, int high) {
        int idx = findElementIndex(offset, low, high);
        if (idx < 0) {
            idx = -idx - 1; // the insertion point: <0, size()>
            return idx - 1;
        } else {
            return idx;
        }
    }
    
    private int indexBeforeOffset(int offset) {
        return indexBeforeOffset(offset, 0, marks.size() - 1);
    }

    private int findElementIndex(int offset, int lowIdx, int highIdx) {
        if (lowIdx < 0 || highIdx > marks.size() - 1) {
            throw new IndexOutOfBoundsException("lowIdx = " + lowIdx + ", highIdx = " + highIdx + ", size = " + marks.size());
        }
        
        int low = lowIdx;
        int high = highIdx;

        while (low <= high) {
            int index = (low + high) >> 1; // mid in the binary search
            int elemOffset = marks.get(index).getOffset();

            if (elemOffset < offset) {
                low = index + 1;

            } else if (elemOffset > offset) {
                high = index - 1;

            } else { // exact offset found at index
                while (index > 0) {
                    index--;
                    if (marks.get(index).getOffset() < offset) {
                        index++;
                        break;
                    }
                }
                return index;
            }
        }
        
        return -(low + 1);
    }
    
    private final class Seq implements HighlightsSequence {

        private long version;
        private int startOffset;
        private int endOffset;

        private int highlightStart;
        private int highlightEnd;
        private AttributeSet highlightAttributes;
        
        private int idx = -1;

        public Seq(long version, int startOffset, int endOffset) {
            this.version = version;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }

        public boolean moveNext() {
            synchronized (PositionsBag.this.marks) {
                if (checkVersion()) {
                    if (idx == -1) {
                        idx = indexBeforeOffset(startOffset);
                        if (idx == -1 && marks.size() > 0) {
                            idx = 0;
                        }
                    } else {
                        idx++;
                    }

                    while (isIndexValid(idx)) {
                        if (attributes.get(idx) != null) {
                            highlightStart = Math.max(marks.get(idx).getOffset(), startOffset);
                            highlightEnd = Math.min(marks.get(idx + 1).getOffset(), endOffset);
                            highlightAttributes = attributes.get(idx);
                            return true;
                        }

                        // Skip any empty areas
                        idx++;
                    }
                }
                
                return false;
            }
        }

        public int getStartOffset() {
            synchronized (PositionsBag.this.marks) {
                assert idx != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return highlightStart;
            }
        }

        public int getEndOffset() {
            synchronized (PositionsBag.this.marks) {
                assert idx != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return highlightEnd;
            }
        }

        public AttributeSet getAttributes() {
            synchronized (PositionsBag.this.marks) {
                assert idx != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return highlightAttributes;
            }
        }
        
        private boolean isIndexValid(int idx) {
            return  idx >= 0 && 
                    idx + 1 < marks.size() && 
                    marks.get(idx).getOffset() < endOffset &&
                    marks.get(idx + 1).getOffset() > startOffset;
        }
        
        private boolean checkVersion() {
            return PositionsBag.this.version == version;
        }
    } // End of Seq class
}
