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

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.modules.editor.lib2.highlighting.OffsetGapList;
import org.netbeans.spi.editor.highlighting.*;
import org.openide.util.Utilities;

/**
 * A bag of highlighted areas specified by their offsets in a document.
 *
 * <p>The highlighted areas (highlights) are determined by their starting and ending
 * offsets in a document and the set of attributes that should be used for rendering
 * that area. The highlights can be arbitrarily added to and remove from this bag.
 * 
 * <p>The <code>OffsetsBag</code> is designed to work over a single
 * document, which is passed in to the constructor. All offsets
 * accepted by various methods in this class must refer to positions within
 * this document. Therefore any offsets passed in to the methods in this class
 * have to be equal to or greater than zero and less than or equal to the document
 * size.
 *
 * <p>The <code>OffsetsBag</code> can operate in two modes depending on a
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
 * then constitute a new highlight, which attributes will be a composition of
 * the attributes of both the new and existing highlight. Should there be attributes
 * with the same name the attribute values from the newly added highlight will take
 * precedence.
 *
 * @author Vita Stejskal
 */
public final class OffsetsBag extends AbstractHighlightsContainer {

    private static final Logger LOG = Logger.getLogger(OffsetsBag.class.getName());

    private Document document;
    private final OffsetGapList<Mark> marks;
    private final boolean mergeHighlights;
    private long version = 0;
    private DocL docListener;
    private int lastAddIndex; // Index where last add to marks list was done
    private int lastMoveNextIndex; // Index where last moveNext() with idx=-1 was done
    private StackTraceElement [] discardCaller = null;
    private String discardThreadId = null;
    
    /**
     * Creates a new instance of <code>OffsetsBag</code>, which trims highlights
     * as they are added. It calls the {@link #OffsetsBag(Document, boolean)} constructor
     * passing <code>false</code> as a parameter.
     * 
     * @param document           The document that should be highlighted.
     */
    public OffsetsBag(Document document) {
        this(document, false);
    }
    
    /**
     * Creates a new instance of <code>OffsetsBag</code>.
     *
     * @param document           The document that should be highlighted.
     * @param mergeHighlights    Determines whether highlights should be merged
     *                           or trimmed.
     */
    public OffsetsBag(Document document, boolean mergeHighlights) {
        this.document = document;
        this.mergeHighlights = mergeHighlights;
        this.marks = new OffsetGapList<Mark>(true); // do not move 0 offset, #102955
        this.docListener = new DocL(this);
        this.document.addDocumentListener(docListener);
    }

    /**
     * Discards this <code>OffsetsBag</code>. This method should be called when
     * a client stops using the bag. After calling this method no other methods
     * should be called. The bag is effectively empty and it is not possible to
     * modify it.
     */
    public void discard() {
        synchronized (marks) {
            if (document != null) {
                document.removeDocumentListener(docListener);
                
                marks.clear();
                version++;
                docListener = null;
                document = null;

                boolean ae = false;
                assert ae = true;
                if (ae) {
                    Thread t = Thread.currentThread();
                    discardCaller = t.getStackTrace();
                    discardThreadId = t.getName() + ":" + t.getId(); //NOI18N
                }
            }
        }
    }
    
    /**
     * Adds a highlight to this bag. The highlight is specified by its staring
     * and ending offset and by its attributes. Adding a highlight that overlaps
     * with one or more existing highlights can have a different result depending
     * on the value of the <code>mergingHighlights</code> parameter used for
     * constructing this bag.
     *
     * @param startOffset    The beginning of the highlighted area.
     * @param endOffset      The end of the highlighted area.
     * @param attributes     The attributes to use for highlighting.
     */
    public void addHighlight(int startOffset, int endOffset, AttributeSet attributes) {
        int [] offsets;
        
        synchronized (marks) {
            offsets = addHighlightImpl(startOffset, endOffset, attributes);
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
     * @param bag    The bag of highlights that will be atomically
     *               added to this bag.
     */
    public void addAllHighlights(HighlightsSequence bag) {
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
     * Resets this bag to use the new set of highlights. This method drops
     * all the existing highlights in this bag and adds all highlights from
     * the sequence passed in as a parameter. The changes are made atomically.
     * The sequence passed in has to be acting on the same <code>Document</code>
     * as this bag.
     *
     * @param seq    New highlights to add.
     */
    public void setHighlights(HighlightsSequence seq) {
        if (seq instanceof Seq) {
            setHighlights(((Seq) seq).getBag());
            return;
        }
        
        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;
        
        synchronized (marks) {
            int [] clearedArea = clearImpl();
            int [] populatedArea = addAllHighlightsImpl(seq);
        
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
     * Resets this bag to use the new set of highlights. This method drops
     * all the existing highlights in this bag and adds all highlights from
     * the bag passed in as a parameter. The changes are made atomically. Both
     * bags have to be acting on the same <code>Document</code>.
     *
     * @param bag    New highlights to add.
     */
    public void setHighlights(OffsetsBag bag) {
        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;

        synchronized (marks) {
            assert document != null : "Can't modify discarded bag."; //NOI18N
            
            int [] clearedArea = clearImpl();
            int [] populatedArea = null;
            
            OffsetGapList<OffsetsBag.Mark> newMarks = bag.getMarks();

            synchronized (newMarks) {
                for(OffsetsBag.Mark mark : newMarks) {
                    marks.add(marks.size(), new OffsetsBag.Mark(mark.getOffset(), mark.getAttributes()));
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
     * Removes highlights in a specific area of the document. All existing highlights
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
     * @param startOffset    The beginning of the area to clear.
     * @param endOffset      The end of the area to clear.
     * @param clip           Whether to clip the partially overlapping highlights.
     */
    public void removeHighlights(int startOffset, int endOffset, boolean clip) {
        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;

        // Ignore empty areas when clipping
        if (startOffset == endOffset && clip) {
            return;
        } else {
            assert startOffset <= endOffset : "Start offset must be less than or equal to the end offset. startOffset = " + startOffset + ", endOffset = " + endOffset; //NOI18N
        }
        
        synchronized (marks) {
            assert document != null : "Can't modify discarded bag."; //NOI18N
            
            if (marks.isEmpty()) {
                return;
            }
            
            int startIdx = indexBeforeOffset(startOffset);
            int endIdx = (startOffset == endOffset) // true for MarkOccurrencesHighlighter
                    ? startIdx
                    : indexBeforeOffset(endOffset, startIdx < 0 ? 0 : startIdx, marks.size() - 1);
            
//            System.out.println("removeHighlights(" + startOffset + ", " + endOffset + ", " + clip + ") : startIdx = " + startIdx + ", endIdx = " + endIdx);
            
            if (clip) {
                if (startIdx == endIdx) {
                    if (startIdx != -1 && marks.get(startIdx).getAttributes() != null) {
                        AttributeSet original = marks.get(startIdx).getAttributes();
                        
                        if (marks.get(startIdx).getOffset() == startOffset) {
                            marks.set(startIdx, new Mark(endOffset, original));
                        } else {
                            marks.add(startIdx + 1, new Mark(startOffset, null));
                            marks.add(startIdx + 2, new Mark(endOffset, original));
                        }
                        
                        changeStart = startOffset;
                        changeEnd = endOffset;
                    }
                    
                    // make sure nothing gets removed
                    startIdx = Integer.MAX_VALUE;
                    endIdx = Integer.MIN_VALUE;
                } else {
                    assert endIdx != -1 : "Invalid range: startIdx = " + startIdx + " endIdx = " + endIdx;

                    if (marks.get(endIdx).getAttributes() != null) {
                        marks.set(endIdx, new Mark(endOffset, marks.get(endIdx).getAttributes()));
                        changeEnd = endOffset;
                        endIdx--;
                    }
                    
                    if (startIdx != -1 && marks.get(startIdx).getAttributes() != null) {
                        if (startIdx + 1 < endIdx) {
                            marks.set(++startIdx, new Mark(startOffset, null));
                        } else {
                            if (marks.get(startIdx).getOffset() < startOffset) {
                                if (startIdx + 1 == endIdx) {
                                    marks.set(++startIdx, new Mark(startOffset, null));
                                } else {
                                    marks.add(++startIdx, new Mark(startOffset, null));
                                }
                            } else {
                                if (startIdx == 0 || marks.get(startIdx - 1).getAttributes() == null) {
                                    startIdx--;
                                } else {
                                    marks.set(startIdx, new Mark(startOffset, null));
                                }
                            }
                        }
                        changeStart = startOffset;
                    }
                    startIdx++;
                }
            } else {
                if (startIdx == -1 || marks.get(startIdx).getAttributes() == null) {
                    startIdx++;
                } else if (startIdx > 0 && marks.get(startIdx - 1).getAttributes() != null) {
                    marks.get(startIdx).setAttributes(null);
                    startIdx++;
                }
                
                if (endIdx != -1 && marks.get(endIdx).getAttributes() != null) {
                    if (marks.get(endIdx).getOffset() < endOffset) {
                        if (endIdx + 1 >= marks.size() || marks.get(endIdx + 1).getAttributes() == null) {
                            endIdx++;
                        }
                    } else {
                        endIdx--;
                    }
                }
            }
            
            if (startIdx <= endIdx) {
                if (changeStart == Integer.MAX_VALUE) {
                    changeStart = marks.get(startIdx).getOffset();
                }
                if (changeEnd == Integer.MIN_VALUE) {
                    if (endIdx >= marks.size()) { // Logging for #117403
                        if (LOG.isLoggable(Level.INFO)) {
                            String logMsg = "Too high endIdx=" + endIdx + // NOI18N
                                    ", marks.size()=" + marks.size() + // NOI18N
                                    ", startIdx=" + startIdx + // NOI18N
                                    ", startOffset=" + startOffset + ", endOffset=" + endOffset + // NOI18N
                                    ", changeStart=" + changeStart + // NOI18N
                                    ", document.getLength()=" + document.getLength() + // NOI18N
                                    ", lastMark=" + marks.get(marks.size() - 1); // NOI18N
                            LOG.log(Level.INFO, logMsg, new Exception());
                        }
                        endIdx = marks.size() - 1; // Fix the index so that exc. does not occur
                    }
                    changeEnd = marks.get(endIdx).getOffset();
                }
                marks.remove(startIdx, endIdx - startIdx + 1);
            }
            
            if (changeStart <= changeEnd) {
                version++;
            }
        }
        
        if (changeStart <= changeEnd) {
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
    @Override
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        if (LOG.isLoggable(Level.FINE) && !(startOffset < endOffset)) {
            LOG.fine("startOffset must be less than endOffset: startOffset = " + //NOI18N
                startOffset + " endOffset = " + endOffset); //NOI18N
        }
        
        synchronized (marks) {
            if (document != null) {
                return new Seq(version, startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }

    /**
     * Removes all highlights previously added to this bag.
     */
    public void clear() {
        int [] clearedArea;
        
        synchronized (marks) {
            assert document != null : "Can't modify discarded bag."; //NOI18N
            
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
    
    /* package */ OffsetGapList<Mark> getMarks() {
        return marks;
    }

    /* package */ Document getDocument() {
        return document;
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private int [] addHighlightImpl(int startOffset, int endOffset, AttributeSet attributes) {
        if (startOffset == endOffset) {
            return null;
        } else {
            assert document != null : "Can't modify discarded bag. Called on " + discardThreadId + " by " + printStackTrace(discardCaller); //NOI18N
            assert startOffset < endOffset : "Start offset must be before the end offset. startOffset = " + startOffset + ", endOffset = " + endOffset; //NOI18N
            assert attributes != null : "Highlight attributes must not be null."; //NOI18N
            if (document == null || startOffset >= endOffset || attributes == null) {
                return null;
            }
        }

        if (mergeHighlights) {
            merge(startOffset, endOffset, attributes);
        } else {
            trim(startOffset, endOffset, attributes);
        }

        return new int [] { startOffset, endOffset };
    }
    
    // [CACHE] Uncomment the following to debug the cache
//    private static int lastAddIndexUsed;
//    private static int lastAddIndexMissed;
//    private static int lastAddIndexFwd1;
//    private static int lastMoveNextIndexUsed;
//    private static int lastMoveNextIndexMissed;
//    private static int lastMoveNextIndexFwd1;
//    private static int binSearchCount;

    private int findAddIndex(int offset) {
        int lastIdx = lastAddIndex;
        boolean hit = false;
        int marksSize = marks.size();
        if (lastIdx < marksSize) { // Otherwise the index would be obsolete
            if (lastIdx == -1) {
                hit = (marksSize == 0 || marks.get(0).getOffset() > offset);
            } else if (lastIdx >= 0) { // Since there's "lastAddIndex--" the "lastIdx <= -2" is possible
                int markOffset = marks.get(lastIdx).getOffset();
                if (offset == markOffset) { // Verify that idx is first with this offset
                    hit = (lastIdx == 0 || marks.get(lastIdx - 1).getOffset() < offset);
                } else if (offset > markOffset) {
                    if (lastIdx >= marksSize - 1 || offset < marks.get(lastIdx + 1).getOffset()) {
                        hit = true;
                    } else { // offset >= markOffset
                        // Statistically it appears that rather often lastIdx == real-index - 1.
                        // so check for this case proactively
                        if (lastIdx >= marksSize - 2 || offset < marks.get(lastIdx + 2).getOffset()) {
                            lastIdx++; // Use the one index higher
                            hit = true;
                        }
                    }
                }
            }
        }
        int idx = hit ? lastIdx : indexBeforeOffset(offset);

        // [CACHE] Uncomment the following to debug the cache
//        if (hit) {
//            lastAddIndexUsed++;
//            if (lastIdx == lastAddIndex + 1)
//                lastAddIndexFwd1++;
//            // Verify that the hit result is correct
//            idx = indexBeforeOffset(offset);
//            if (lastIdx != idx) {
//                throw new IllegalStateException("lastIdx=" + lastIdx + " != idx=" + idx);
//            }
//        } else { // not hit
//            lastAddIndexMissed++;
//            // Info about cache miss
//            StringBuilder sb = new StringBuilder("AAA o=" + offset + ", idx=" + idx +
//                    ", last=" + lastIdx + ", marksSize=" + marksSize);
//            if (lastIdx > 0 && lastIdx <= marksSize) {
//                    sb.append(", bLast.o=" + marks.get(lastIdx - 1).getOffset());
//            }
//            if (lastIdx >= 0 && lastIdx < marksSize) {
//                    sb.append(", atLast.o=" + marks.get(lastIdx).getOffset());
//            }
//            System.err.println(sb.toString());
//        }
//        if ((lastAddIndexUsed + lastAddIndexMissed) % 1000 == 0) {
//            System.err.println("CACHE: hit=" + lastAddIndexUsed + ", miss=" + lastAddIndexMissed +
//                    ", Fwd1=" + lastAddIndexFwd1 +
//                    ", mvNxtHit=" + lastMoveNextIndexUsed + ", mvNxtMiss=" + lastMoveNextIndexMissed +
//                    ", binSearches=" + binSearchCount);
//        }

        lastAddIndex = idx;
        return idx;
    }
    
    private int findMoveNextIndex(int offset) {
        int lastIdx = lastMoveNextIndex;
        boolean hit = false;
        int marksSize = marks.size();
        if (lastIdx < marksSize) { // Otherwise the index would be obsolete
            if (lastIdx == -1) {
                hit = (marksSize == 0 || marks.get(0).getOffset() > offset);
            } else { // idx != -1
                int markOffset = marks.get(lastIdx).getOffset();
                if (offset == markOffset) { // Verify that idx is first with this offset
                    hit = (lastIdx == 0 || marks.get(lastIdx - 1).getOffset() < offset);
                } else if (offset > markOffset) {
                    if (lastIdx >= marksSize - 1 || offset < marks.get(lastIdx + 1).getOffset()) {
                        hit = true;
                    } else { // offset >= markOffset
                        // Statistically it appears that rather often lastIdx == real-index - 1.
                        // so check for this case proactively
                        if (lastIdx >= marksSize - 2 || offset < marks.get(lastIdx + 2).getOffset()) {
                            lastIdx++; // Use the one index higher
                            hit = true;
                        }
                    }
                }
            }
        }
        int idx = hit ? lastIdx : indexBeforeOffset(offset);

        // [CACHE] Uncomment the following to debug the cache
//        if (hit) {
//            lastMoveNextIndexUsed++;
//            if (lastIdx == lastMoveNextIndex + 1)
//                lastMoveNextIndexFwd1++;
//            // Verify that the hit result is correct
//            idx = indexBeforeOffset(offset);
//            if (lastIdx != idx) {
//                throw new IllegalStateException("lastIdx=" + lastIdx + " != idx=" + idx);
//            }
//        } else { // not hit
//            lastMoveNextIndexMissed++;
//            StringBuilder sb = new StringBuilder("III o=" + offset + ", idx=" + idx +
//                    ", last=" + lastIdx + ", marksSize=" + marksSize);
//            if (lastIdx > 0 && lastIdx <= marksSize) {
//                    sb.append(", bLast.o=" + marks.get(lastIdx - 1).getOffset());
//            }
//            if (lastIdx >= 0 && lastIdx < marksSize) {
//                    sb.append(", atLast.o=" + marks.get(lastIdx).getOffset());
//            }
//            System.err.println(sb.toString());
//        }

        lastMoveNextIndex = idx;
        return idx;
    }
    
    private void merge(int startOffset, int endOffset, AttributeSet attributes) {
        AttributeSet lastKnownAttributes = null;
        int startIdx = findAddIndex(startOffset);
        if (startIdx < 0) {
            startIdx = 0;
            marks.add(startIdx, new Mark(startOffset, attributes));
            lastAddIndex++; // Increase the index after addition
        } else {
            Mark mark = marks.get(startIdx);
            AttributeSet markAttribs = mark.getAttributes();
            AttributeSet newAttribs = markAttribs == null ? attributes : AttributesUtilities.createComposite(attributes, markAttribs);
            lastKnownAttributes = mark.getAttributes();

            if (mark.getOffset() == startOffset) {
                mark.setAttributes(newAttribs);
            } else {
                startIdx++;
                marks.add(startIdx, new Mark(startOffset, newAttribs));
                lastAddIndex++; // Increase the index after addition
            }
        }

        for(int idx = startIdx + 1; ; idx++) {
            if (idx < marks.size()) {
                Mark mark = marks.get(idx);

                if (mark.getOffset() < endOffset) {
                    lastKnownAttributes = mark.getAttributes();
                    mark.setAttributes(lastKnownAttributes == null ? 
                        attributes : 
                        AttributesUtilities.createComposite(attributes, lastKnownAttributes));
                } else {
                    if (mark.getOffset() > endOffset) {
                        marks.add(idx, new Mark(endOffset, lastKnownAttributes));
                        lastAddIndex++; // Increase the index after addition
                    }
                    break;
                }
            } else {
                marks.add(idx, new Mark(endOffset, lastKnownAttributes));
                lastAddIndex++; // Increase the index after addition
                break;
            }
        }
    }

    private void trim(int startOffset, int endOffset, AttributeSet attributes) {
        int startIdx = findAddIndex(startOffset);
        int endIdx = indexBeforeOffset(endOffset, startIdx < 0 ? 0 : startIdx, marks.size() - 1);
        
//        System.out.println("trim(" + startOffset + ", " + endOffset + ") : startIdx = " + startIdx + ", endIdx = " + endIdx);
        
        if (startIdx == endIdx) {
            AttributeSet original = null;
            if (startIdx != -1 && marks.get(startIdx).getAttributes() != null) {
                original = marks.get(startIdx).getAttributes();
            }
            
            if (startIdx != -1 && marks.get(startIdx).getOffset() == startOffset) {
                marks.get(startIdx).setAttributes(attributes);
            } else {
                startIdx++;
                marks.add(startIdx, new Mark(startOffset, attributes));
                lastAddIndex++; // Increase the index after addition
            }
            
            startIdx++;
            marks.add(startIdx, new Mark(endOffset, original));
            lastAddIndex++; // Increase the index after addition
        } else {
            assert endIdx != -1 : "Invalid range: startIdx = " + startIdx + " endIdx = " + endIdx; //NOI81N

            marks.set(endIdx, new Mark(endOffset, marks.get(endIdx).getAttributes()));
            endIdx--;

            if (startIdx != -1 && marks.get(startIdx).getOffset() == startOffset) {
                marks.get(startIdx).setAttributes(attributes);
            } else if (startIdx + 1 <= endIdx) {
                marks.set(++startIdx, new Mark(startOffset, attributes));
            } else {
                marks.add(++startIdx, new Mark(startOffset, attributes));
                lastAddIndex++; // Increase the index after addition
            }
            startIdx++;

            if (startIdx <= endIdx) {
                marks.remove(startIdx, endIdx - startIdx + 1);
                lastAddIndex--; // Decrease the index after removal
            }
        }
    }
    
    private int [] addAllHighlightsImpl(HighlightsSequence sequence) {
        int changeStart = Integer.MAX_VALUE;
        int changeEnd = Integer.MIN_VALUE;

        for ( ; sequence.moveNext(); ) {
            addHighlightImpl(sequence.getStartOffset(), sequence.getEndOffset(), sequence.getAttributes());

            if (changeStart == Integer.MAX_VALUE) {
                changeStart = sequence.getStartOffset();
            }
            changeEnd = sequence.getEndOffset();
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

            return new int [] { changeStart, changeEnd };
        } else {
            return null;
        }
    }

    private int indexBeforeOffset(int offset, int low, int high) {
        // [CACHE] Uncomment the following to debug the cache
//        binSearchCount++;

        int idx = marks.findElementIndex(offset, low, high);
        if (idx < 0) {
            idx = -idx - 2; // the insertion point: <0, size()>
        }
        return idx;
    }
    
    private int indexBeforeOffset(int offset) {
        return indexBeforeOffset(offset, 0, marks.size() - 1);
    }

    private static String printStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement e : stackTrace) {
            sb.append(e);
            sb.append('\n'); //NOI18N
        }
        return sb.toString();
    }

    /* package */ static final class Mark extends OffsetGapList.Offset {
        private AttributeSet attribs;
        
        public Mark(int offset, AttributeSet attribs) {
            super(offset);
            this.attribs = attribs;
        }
        
        public AttributeSet getAttributes() {
            return attribs;
        }
        
        public void setAttributes(AttributeSet attribs) {
            this.attribs = attribs;
        }

        @Override
        public String toString() {
            return "offset=" + getOffset() + ", attribs=" + attribs; // NOI81N
        }
        
    } // End of Mark class

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

        @Override
        public boolean moveNext() {
            synchronized (OffsetsBag.this.marks) {
                if (checkVersion()) {
                    if (idx == -1) {
                        idx = findMoveNextIndex(startOffset);
                        if (idx == -1 && marks.size() > 0) {
                            idx = 0;
                        }
                    } else {
                        idx++;
                    }

                    int []  offsets = new int [2];
                    while (isIndexValid(idx, offsets)) {
                        if (marks.get(idx).getAttributes() != null) {
                            highlightStart = Math.max(offsets[0], startOffset);
                            highlightEnd = Math.min(offsets[1], endOffset);
                            highlightAttributes = marks.get(idx).getAttributes();
                            return true;
                        }

                        // Skip any empty areas
                        idx++;
                    }
                }
                
                return false;
            }
        }

        @Override
        public int getStartOffset() {
            synchronized (OffsetsBag.this.marks) {
                assert idx != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return highlightStart;
            }
        }

        @Override
        public int getEndOffset() {
            synchronized (OffsetsBag.this.marks) {
                assert idx != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return highlightEnd;
            }
        }

        @Override
        public AttributeSet getAttributes() {
            synchronized (OffsetsBag.this.marks) {
                assert idx != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return highlightAttributes;
            }
        }
        
        private boolean isIndexValid(int idx, int [] offsets) {
            return  idx >= 0 && 
                    idx + 1 < marks.size() && 
                    (offsets[0] = marks.get(idx).getOffset()) < endOffset &&
                    (offsets[1] = marks.get(idx + 1).getOffset()) > startOffset;
        }
        
        private OffsetsBag getBag() {
            return OffsetsBag.this;
        }
        
        private boolean checkVersion() {
            return OffsetsBag.this.version == version;
        }
    } // End of Seq class
    
    private static final class DocL extends WeakReference<OffsetsBag> implements DocumentListener, Runnable {
        
        private Document document;
        
        public DocL(OffsetsBag bag) {
            super(bag, Utilities.activeReferenceQueue());
            this.document = bag.getDocument();
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            OffsetsBag bag = get();
            if (bag != null) {
                synchronized (bag.marks) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("OffsetsBag@" + Integer.toHexString(System.identityHashCode(this)) + //NOI18N
                            " insertUpdate: doc=" + Integer.toHexString(System.identityHashCode(document)) + //NOI18N
                            ", offset=" + e.getOffset() + ", insertLength=" + e.getLength() + //NOI18N
                            ", docLength=" + document.getLength()); //NOI18N
                    }
                    bag.marks.defaultInsertUpdate(e.getOffset(), e.getLength());
                }
            } else {
                run();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            OffsetsBag bag = get();
            if (bag != null) {
                synchronized (bag.marks) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("OffsetsBag@" + Integer.toHexString(System.identityHashCode(this)) + //NOI18N
                            " removeUpdate: doc=" + Integer.toHexString(System.identityHashCode(document)) + //NOI18N
                            ", offset=" + e.getOffset() + ", removedLength=" + e.getLength() + //NOI18N
                            ", docLength=" + document.getLength()); //NOI18N
                    }
                    bag.marks.defaultRemoveUpdate(e.getOffset(), e.getLength());
                }
            } else {
                run();
            }
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // not interested
        }

        @Override
        public void run() {
            Document d = document;
            if (d != null) {
                d.removeDocumentListener(this);
                document = null;
            }
        }
    } // End of DocL class
}
