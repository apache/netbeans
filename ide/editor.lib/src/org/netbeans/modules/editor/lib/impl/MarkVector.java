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

package org.netbeans.modules.editor.lib.impl;

import java.util.List;
import javax.swing.text.Position;

/**
 * Container for {@link MultiMark} for the document.
 * The structure is array with a gap together with an offset gap
 * for the abstract storage of the document text.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class MarkVector {

    /** Empty array of marks used initially as markArray. */
    private static final MultiMark[] EMPTY = new MultiMark[0];

    /** Default size of the offset gap. */
    private static final int INITIAL_OFFSET_GAP_SIZE = (Integer.MAX_VALUE >> 1);
    
    /** Array of the marks with the gap. */
    private MultiMark[] markArray;
    
    /** Starting index of the gap in the markArray. */
    private int gapStart;
    
    /** Length of the gap in the markArray.
     * The length of the gap must always be &gt;0.
     */
    private int gapLength;
    
    /** Starting offset of the offset gap */
    private int offsetGapStart;
    
    /** Length of the offset gap */
    private int offsetGapLength;
    
    /** Number of marks that are still in the markArray but 
     * that are no longer valid.
     */
    private int disposedMarkCount;
    
    public MarkVector() {
        markArray = EMPTY;
        offsetGapLength = INITIAL_OFFSET_GAP_SIZE;
    }
    
    /** Create mark with the given bias. The mark is not automatically
     * inserted into the vector.
     * @param offset offset of the mark.
     * @param bias bias of the mark.
     */
    public MultiMark createBiasMark(int offset, Position.Bias bias) {
        return new MultiMark(null, this, offset, bias);
    }
    
    /** Create mark with the given bias. The mark is not automatically
     * inserted into the vector.
     * @param pos position that will delegate to this mark.
     * @param offset offset of the mark.
     * @param bias bias of the mark.
     */
    public MultiMark createBiasMark(BasePosition pos, int offset, Position.Bias bias) {
        return new MultiMark(pos, this, offset, bias);
    }
    
    /** Create swing-compatible mark. The mark is not automatically
     * inserted into the vector.
     * @param offset offset of the mark.
     */
    public MultiMark createMark(int offset) {
        return new MultiMark(null, this, offset);
    }
    
    /** Create swing-compatible mark. The mark is not automatically
     * inserted into the vector.
     * @param pos position that will delegate to this mark.
     * @param offset offset of the mark.
     */
    public MultiMark createMark(BasePosition pos, int offset) {
        return new MultiMark(pos, this, offset);
    }
    
    /** @return total count of marks in the vector.
     */
    public synchronized int getMarkCount() {
        return markArray.length - gapLength;
    }
    
    public synchronized MultiMark getMark(int index) {
        return markArray[getRawIndex(index)];
    }
    
    public synchronized int getMarkOffsetInternal(int index) {
        return getOffset(getMark(index).rawOffset);
    }

    /** Insert mark previously created by <CODE>createBiasMark()</CODE>
     * or <CODE>createMark()</CODE>.
     * @param mark mark to insert. The mark must have the valid
     *  offset and flags filled in.
     */
    public synchronized MultiMark insert(MultiMark mark) {
        int flags = mark.flags;
        if ((flags & MultiMark.VALID) != 0) { // trying to re-insert valid mark
            throw new IllegalStateException();
        }

        int offset = mark.rawOffset;
        int index = findInsertIndex(offset);
        if (gapLength == 0) {
            enlargeGap(1);
        }
        if (index != gapStart) {
            moveGap(index);
        }

        if (offset > offsetGapStart
            || (offset == offsetGapStart
                && ((flags & MultiMark.BACKWARD_BIAS) == 0))
        ) { // above offset gap
            mark.rawOffset += offsetGapLength;
        }

        markArray[gapStart++] = mark;
        gapLength--;
        mark.flags |= MultiMark.VALID;

        return mark;
    }
    
    /** Insert list of marks at once. When inserting
     * a large number of sorted marks (at least partially) this method offers
     * better performance.
     * @param markList list of MultiMarks to insert.
     */
    synchronized void insertList(List markList) {
        int lastOffset = Integer.MAX_VALUE;
        boolean lastBackwardBias = true;
        int upperOffset = 0;
        boolean upperBackwardBias = false;
        int markCount = getMarkCount();
        int insertMarkCount = markList.size();
        
        if (gapLength < insertMarkCount) {
            enlargeGap(insertMarkCount); // enough space for all the marks
        }
        
        for (int i = 0; i < insertMarkCount; i++ ) {
            MultiMark mark = (MultiMark)markList.get(i);
            int flags = mark.flags;
            if ((flags & MultiMark.VALID) != 0) { // trying to re-insert valid mark
                throw new IllegalStateException();
            }
            boolean backwardBias = ((flags & MultiMark.BACKWARD_BIAS) != 0);

            int offset = mark.rawOffset;
            if ((offset < lastOffset)
                || (offset == lastOffset && backwardBias && !lastBackwardBias)
                || (offset > upperOffset)
                || (offset == upperOffset && !backwardBias && upperBackwardBias)
            ) {
                // Find the index and inspect previous/next marks
                int index = findInsertIndex(offset);
                if (index != gapStart) {
                    moveGap(index);
                }
                
                if (index < markCount) {
                    MultiMark m = markArray[getRawIndex(index)];
                    upperOffset = getOffset(m.rawOffset);
                    upperBackwardBias = ((m.flags & MultiMark.BACKWARD_BIAS) != 0);

                } else { // was last mark
                    upperOffset = Integer.MAX_VALUE;
                    upperBackwardBias = false;
                }
            }
                
            if (offset > offsetGapStart
                || (offset == offsetGapStart
                    && ((flags & MultiMark.BACKWARD_BIAS) == 0))
            ) { // above offset gap
                mark.rawOffset += offsetGapLength;
            }
            
            markArray[gapStart++] = mark;
            gapLength--;
            mark.flags |= MultiMark.VALID;

            lastOffset = offset;
            lastBackwardBias = backwardBias;
            markCount++;
        }
    }
    
    synchronized void notifyMarkDisposed() {
        disposedMarkCount++;
        
        if (disposedMarkCount > Math.max(5, getMarkCount() / 10)) {
            removeDisposedMarks();
        }
    }

    public synchronized void compact() {
        if (gapLength > 0) {
            int newLength = markArray.length - gapLength;
            MultiMark[] newMarkArray = new MultiMark[newLength];
            int gapEnd = gapStart + gapLength;
            System.arraycopy(markArray, 0, newMarkArray, 0, gapStart);
            System.arraycopy(markArray, gapEnd, newMarkArray, gapStart, 
                markArray.length - gapEnd);
            markArray = newMarkArray;
            gapStart = markArray.length;
            gapLength = 0;
        }
    }

    /** Document was modified. This means that the ficitonal
     * offset gap must be updated by the modifiaction. The offset gap can
     * possibly be moved and the marks in the moved area must
     * be updated accordingly.
     * @param offset offset of the modification
     * @param length length of added/removed data. If the length is positive
     *  then the insert occured. If the length is negative then
     *  the removal has occured.
     * @param undo undo information to be processed. It can be null
     *  if there is no undo information for this update operation.
     * @return non-null undo record or null in case an insert was done
     *  or there are no marks to be undone.
     */
    public synchronized Undo update(int offset, int length, Undo undo) {
        if (length < 0) { // removal occured - temporarily increase offset var
            offset -= length; // move offset after end of removal (length < 0)
        }

        // First move the gap if necessary
        int offsetGapIndex = findInsertIndex(offset);

        moveOffsetGap(offsetGapIndex, offset);
        offsetGapStart += length;
        offsetGapLength -= length;

        if (length >= 0) { // insert performed
            if (undo != null) { // in undo or redo
                /* It's necessary to restore the offsets
                 * of the marks contained in the linked list of undo items.
                 * The 'logicalNext' will allow to find whether
                 * there were some marks added or not
                 * since the time the items being undone were created.
                 * The 'next' determines the original
                 * order of the marks.
                 * It's possible that some marks were already
                 * disposed so these will be ignored
                 * (they are possibly no longer in the array).
                 */
                
                // Process fwd bias marks first
                UndoItem dirFirstItem = undo.fbItem;
                int fbUndoMarkCount = 0;

                /* Eliminate the items (by clearing the mark in them)
                 * of marks that were removed from the vector.
                 */
                while (dirFirstItem != null) {
                    if ((dirFirstItem.mark.flags & MultiMark.REMOVED) == 0) { // valid
                        fbUndoMarkCount++;
                        UndoItem item = dirFirstItem.logicalNext;
                        // Eliminate marks removed from vector
                        while (item != null) {
                            if ((item.mark.flags & MultiMark.REMOVED) == 0) { // valid
                                fbUndoMarkCount++;
                            } else { // mark removed from vector
                                item.mark = null;
                            }

                            item = item.logicalNext;
                        }
                        break;

                    } else { // mark removed from vector
                        dirFirstItem.mark = null;
                    }

                    dirFirstItem = dirFirstItem.logicalNext;
                }
                
                if (dirFirstItem != null) { // some fwd marks to undo
                    // Find the dirFirstItem mark's index
                    MultiMark firstItemMark = dirFirstItem.mark;
                    int index = offsetGapIndex;
                    
                    while (index < markArray.length && markArray[getRawIndex(index)] != firstItemMark) {
                        index++;
                    }
                    
                    while (--index >= offsetGapIndex) {
                        markArray[getRawIndex(index + fbUndoMarkCount)]
                             = markArray[getRawIndex(index)];
                    }
                }
                
                dirFirstItem = undo.bbItem;
                int bbUndoMarkCount = 0;
                
                /* Eliminate the items (by clearing the mark in them)
                 * of marks that were removed from the vector.
                 */
                while (dirFirstItem != null) {
                    if ((dirFirstItem.mark.flags & MultiMark.REMOVED) == 0) { // valid
                        bbUndoMarkCount++;
                        UndoItem item = dirFirstItem.logicalNext;
                        // Eliminate marks removed from vector
                        while (item != null) {
                            if ((item.mark.flags & MultiMark.REMOVED) == 0) { // valid
                                bbUndoMarkCount++;
                            } else { // mark removed from vector
                                item.mark = null;
                            }
                            item = item.logicalNext;
                        }
                        break;
                        
                    } else { // mark removed from vector
                        dirFirstItem.mark = null;
                    }
                        
                    dirFirstItem = dirFirstItem.logicalNext;
                }

                if (dirFirstItem != null) { // some marks to undo
                    // Find the dirFirstItem's mark
                    MultiMark firstItemMark = dirFirstItem.mark;
                    int index = offsetGapIndex - 1;
                    
                    while (index >= 0 && markArray[getRawIndex(index)] != firstItemMark) {
                        index--;
                    }
                    index++;
                    
                    while (index < offsetGapIndex) {
                        markArray[getRawIndex(index - bbUndoMarkCount)]
                             = markArray[getRawIndex(index)];
                        index++;
                    }
                }


                UndoItem origItem = undo.firstItem;
                /* Rewrite the current bwd and fwd bias marks
                 * by the original ones in the right order
                 */
                offsetGapIndex -= bbUndoMarkCount;
                while (origItem != null) {
                    MultiMark mark = origItem.mark;
                    if (mark != null) {
                        // Undo the offset
                        mark.rawOffset = origItem.undoOffset;
                        markArray[getRawIndex(offsetGapIndex++)] = mark;
                    }

                    origItem = origItem.next;
                }
                
                /* Special case offset == 0 requires handling
                 * of compatible marks.
                 */
                if (offset == 0) {
                    ZeroUndoItem zeroItem = undo.zeroItem;
                    while (zeroItem != null) {
                        MultiMark mark = zeroItem.mark;
                        if ((mark.flags & MultiMark.REMOVED) == 0) {
                            mark.flags &= ~MultiMark.ZERO;
                        }
                        zeroItem = zeroItem.next;
                    }
                }
            }
            
            undo = null;
            
        } else { // remove performed
            /* Marks with backward bias within the whole removed area
             * will be moved to the offset (before offset gap).
             * It is necessary that all the backwardBias marks
             * within the removed area must be placed before all
             * the forwardBias marks within the removed area
             * in order for the structure to work properly.
             * Additionally the marks with the forward bias are placed
             * after current offset gap.
             * The algorithm goes through the marks in the backward direction.
             * It uses the last-backward-bias-mark-index that
             * is -1 unless the first backward-bias-mark is found
             * then it stores its index. If the forward-bias mark
             * is then found it must be exchanged with the currently
             * last backward-bias mark (if upperBBMIndex != -1) and so on
             * for every forward mark found.
             * In order to perform undo later the algorithm generates
             * the linked list of undo items. As the algorithm goes from the end
             * back (each item has next field) the list of items will
             * finally be ordered ascendingly by offset.
             */
            offset += length; // offset back to original value (length < 0)
            UndoItem item = null; // current item in the undo list
            UndoItem fbItem = null; // current forward bias undo item
            UndoItem bbItem = null; // current backward bias undo item
            UndoItem upperBBItem = null; // upper backward bias undo item
            int upperBBMIndex = -1; // index of last backward bias mark
            int offsetAboveGap = offset + offsetGapLength; // use current (updated) gap size
            ZeroUndoItem zeroItem = null;
            
            if (offset == 0) {
                int offsetGapIndexCopy = offsetGapIndex;
                int markCount = getMarkCount();
                while (offsetGapIndexCopy < markCount) {
                    MultiMark mark = markArray[getRawIndex(offsetGapIndexCopy++)];
                    if (mark.rawOffset == offsetAboveGap) {
                        if ((mark.flags & (MultiMark.COMPATIBLE | MultiMark.ZERO))
                            == MultiMark.COMPATIBLE
                        ) {
                            mark.flags |= MultiMark.ZERO;
                            zeroItem = new ZeroUndoItem(mark, zeroItem);
                        }

                    } else { // higher offset
                        break;
                    }
                }
            }
                
            
            while (offsetGapIndex > 0) {
                MultiMark mark = markArray[getRawIndex(--offsetGapIndex)];
                int markOffset = mark.rawOffset; // under offset gap -> real offset
                boolean backwardBias = ((mark.flags & MultiMark.BACKWARD_BIAS) != 0);
                if (markOffset < offset // (all the marks below gap)
                    || (mark.rawOffset == offset && backwardBias)
                ) { // stop at < offset or == offset and backward bias
                    break;
                }
                
                item = new UndoItem(mark, markOffset, item);

                if (backwardBias) {
                    if (bbItem != null) {
                        bbItem.logicalNext = item;
                    } else { // bbItem is null
                        upperBBItem = item;
                        upperBBMIndex = offsetGapIndex;
                    }
                    bbItem = item;

                    // Move mark to offset - will be before offset gap
                    mark.rawOffset = offset;

                } else { // forward bias
                    item.logicalNext = fbItem;
                    fbItem = item;

                    // Mark will be positioned over the offset gap
                    mark.rawOffset = offsetAboveGap;

                    if (upperBBMIndex >= 0) { // backward bias mark(s) exist
                        // exchange this fb mark with upper bb mark
                        int upperBBMRawIndex = getRawIndex(upperBBMIndex--);
                        markArray[getRawIndex(offsetGapIndex)]
                            = markArray[upperBBMRawIndex];

                        markArray[upperBBMRawIndex] = mark;

                        UndoItem upperNext = upperBBItem.logicalNext;
                        if (upperNext != null) {
                            bbItem.logicalNext = upperBBItem;
                            bbItem = upperBBItem;
                            upperBBItem.logicalNext = null;
                            upperBBItem = upperNext;
                        }
                    }
                }
            }
            
            /* It's necessary to handle compatible marks
             * that haven't the ZERO flag set yet
             * and generate ZeroUndoItem for each of them.
             */
            if (offset == 0 && item != null) {
                UndoItem i = item;
                while (i != null) {
                    MultiMark mark = i.mark;
                    if ((mark.flags & (MultiMark.COMPATIBLE | MultiMark.ZERO))
                        == MultiMark.COMPATIBLE
                    ) {
                        mark.flags |= MultiMark.ZERO;
                        zeroItem = new ZeroUndoItem(mark, zeroItem);
                    }
                    i = i.next;
                }
            }
                
            undo = (item != null || zeroItem != null)
                ? new Undo(item, fbItem, upperBBItem, zeroItem)
                : null;

        }
        
        return undo;
    }
    
    private void removeDisposedMarks() {
        int rawIndex = 0;
        int validInd = -1;
        int gapEnd = gapStart + gapLength;

        while (rawIndex < gapStart) {
            MultiMark mark = markArray[rawIndex];
            if ((mark.flags & MultiMark.VALID) != 0) { // valid mark
                if (rawIndex != ++validInd) {
                    markArray[validInd] = mark;
                }

            } else { // mark not valid
                mark.flags |= MultiMark.REMOVED;
            }
            rawIndex++;
        }
        gapStart = validInd + 1;
        
        rawIndex = markArray.length;
        validInd = rawIndex;
        while (--rawIndex >= gapEnd) {
            MultiMark mark = markArray[rawIndex];
            if ((mark.flags & MultiMark.VALID) != 0) { // valid mark
                if (rawIndex != --validInd) {
                    markArray[validInd] = mark;
                }

            } else { // mark not valid
                mark.flags |= MultiMark.REMOVED;
            }
        }
        gapLength = validInd - gapStart;
        
        disposedMarkCount = 0;
    }

    synchronized int getOffset(int rawOffset) {
        /* rawOffset == offsetGapStart for backward bias marks
         * forward bias marks go typically over the end of the offset gap.
         * The offsetGapLength must always stay to be >0.
         */
        return (rawOffset <= offsetGapStart)
            ? rawOffset
            : (rawOffset - offsetGapLength);
    }

    private int getRawIndex(int index) {
        return (index < gapStart)
            ? index
            : index + gapLength;
    }
    
    /** Find the index at which it's valid to perform an insert of the new mark.
     * @param offset offset of the mark
     * @return index &gt;= 0 and &lt;=<CODE>getMarkCount()</CODE>
     *  in the markArray where the insert of the mark with the given
     *  offset can be done.
     *  <BR>If there are marks with the same offset as the given one
     *  then there are first all the marks with the backward bias
     *  and then all the marks with the forward bias. The method
     *  will return the index of the first mark with forward bias.
     */
    private int findInsertIndex(int offset) {
        int low = 0;
        int high = getMarkCount() - 1;
        
        while (low <= high) {
            int index = (low + high) / 2; // mid in the binary search
            MultiMark mark = markArray[getRawIndex(index)];
            int markOffset = getOffset(mark.rawOffset);

            if (markOffset < offset) {
                low = index + 1;
            
            } else if (markOffset > offset) {
                high = index - 1;
            
            } else { // exact offset found - use bias for positioning
                if ((mark.flags & MultiMark.BACKWARD_BIAS) != 0) { // bwd bias
                    low = index + 1;
                } else { // fwd bias
                    high = index - 1;
                }
            }
        }

        return low;
    }
        
    private void moveGap(int index) {
        if (index <= gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(markArray, index, markArray,
                gapStart + gapLength - moveSize, moveSize);
            gapStart = index;

        } else { // above gap
            int moveSize = index - gapStart;
            System.arraycopy(markArray, gapStart + gapLength, markArray, gapStart, moveSize);
            gapStart += moveSize;
        }
    }
    
    private void moveOffsetGap(int index, int newOffsetGapStart) {
        int rawIndex = getRawIndex(index);
        int markArrayLength = markArray.length;
        int offset = offsetGapStart;
        offsetGapStart = newOffsetGapStart;
        int length = offsetGapLength;
        
        if (rawIndex == markArrayLength
            || markArray[rawIndex].rawOffset > offset
        ) { // go down to check and fix the marks are below the offset
            
            int bound = (rawIndex < gapStart)
                ? 0
                : (gapStart + gapLength);

            boolean done = false;
            while (!done) {
                while (--rawIndex >= bound) {
                    MultiMark mark = markArray[rawIndex];
                    if (mark.rawOffset > offset) {
                        mark.rawOffset -= length;
                    } else {
                        done = true;
                        break;
                    }
                }

                if (bound > 0) { // shift the bound
                    bound = 0;
                    rawIndex = gapStart;

                } else { // all marks processed
                    done = true;
                }
            }
            
        } else { // go up to check and fix the marks are above the offset
            
            int bound = (rawIndex < gapStart)
                ? gapStart
                : markArrayLength;

            boolean done = false;
            while (!done) {
                while (rawIndex < bound) {
                    MultiMark mark = markArray[rawIndex];
                    if (mark.rawOffset <= offset) {
                        mark.rawOffset += length;
                    } else {
                        done = true;
                        break;
                    }
                    rawIndex++;
                }

                if (bound < markArrayLength) { // shift the bound
                    bound = markArrayLength;
                    rawIndex += gapLength;

                } else { // all marks processed
                    done = true;
                }
            }
            
        }
    }

    private void enlargeGap(int extraLength) {
        int newLength = Math.max(8, markArray.length * 3 / 2 + extraLength);
        int gapEnd = gapStart + gapLength;
        int afterGapLength = (markArray.length - gapEnd);
        int newGapEnd = newLength - afterGapLength;
        MultiMark[] newMarkArray = new MultiMark[newLength];
        System.arraycopy(markArray, 0, newMarkArray, 0, gapStart);
        System.arraycopy(markArray, gapEnd, newMarkArray, newGapEnd, afterGapLength);
        markArray = newMarkArray;
        gapLength = newGapEnd - gapStart;
    }
// XXX: not used    
//    /** @return the array of objects describing
//     * the state of this object for testing and debug purposes.
//     * They are returned in the following order:<pre>
//     *   markArray
//     *   gapStart
//     *   gapLength
//     *   offsetGapStart
//     *   offsetGapLength
//     * <pre>
//     */
//    private Object[] toObjects() {
//        return new Object[] {
//            markArray.clone(),
//            Integer.valueOf(gapStart),
//            Integer.valueOf(gapLength),
//            Integer.valueOf(offsetGapStart),
//            Integer.valueOf(offsetGapLength)
//        };
//    }

    /** Get info about this mark vector. */
    public @Override String toString() {
        return "markCount=" + getMarkCount() // NOI18N
            + ", gapStart=" + gapStart // NOI18N
            + ", gapLength=" + gapLength // NOI18N
            + ", offsetGapStart=" + offsetGapStart // NOI18N
            + ", offsetGapLength=" + offsetGapLength; // NOI18N
    }
    
    /** Undo record holding the info about undo items.
     */
    public static final class Undo {
        
        Undo(UndoItem firstItem, UndoItem fbItem, UndoItem bbItem,
        ZeroUndoItem zeroItem) {

            this.firstItem = firstItem;
            this.fbItem = fbItem;
            this.bbItem = bbItem;
            this.zeroItem = zeroItem;
        }
        
        UndoItem firstItem;
        
        UndoItem fbItem;
        
        UndoItem bbItem;
        
        ZeroUndoItem zeroItem;

    }

    /** Class used for holding the offset to which the mark
     * will be restored in case it was inside the area 
     * that was removed.
     */
    static final class UndoItem {
        
        UndoItem(MultiMark mark, int undoOffset, UndoItem next) {
            this.mark = mark;
            this.undoOffset = undoOffset;
            this.next = next;
        }
        
        MultiMark mark;
        
        int undoOffset;
        
        UndoItem next;
        
        UndoItem logicalNext;

    }
    
    static final class ZeroUndoItem {
        
        ZeroUndoItem(MultiMark mark, ZeroUndoItem next) {
            this.mark = mark;
            this.next = next;
        }
        
        final MultiMark mark;
        
        final ZeroUndoItem next;

    }

}
