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
package org.netbeans.modules.editor.lib2.document;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.ArrayUtilities;

/**
 * Container for {@link Mark} for the document.
 * The structure is array with a gap together with an offset gap
 * for the abstract storage of the document text.
 * <br/>
 * {@link #isBackwardBias()} determines whether regular (or backward-bias) marks
 * are handled by the mark vector.
 * <br/>
 * Certain methods require an explicit outer synchronization done by {@link EditorDocumentContent}.
 *
 * @author Miloslav Metelka
 * @since 1.46
 */
final class MarkVector {
    
    // -J-Dorg.netbeans.modules.editor.lib2.document.MarkVector.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorDocumentContent.class.getName());
    /**
     * Default size of the offset gap. Since 
     */
    private static final int INITIAL_OFFSET_GAP_SIZE = (Integer.MAX_VALUE >> 2);

    /** Sorted array of marks. Contains a gap to speed up adding of new marks. */
    private Mark[] markArray;
    
    /** Whether this vector manages marks with backward bias. */
    private final boolean backwardBiasMarks;
    
    /** Starting index of the gap in the markArray. */
    private int gapStart;

    /** Length of the gap in the markArray. */
    private int gapLength;

    /** Starting offset of the offset gap */
    private int offsetGapStart;

    /** Length of the offset gap */
    private int offsetGapLength;
    
    private final Object lock;

    /**
     * Number of mark instances that are still in the markArray but 
     * that are no longer valid (their position objects were disposed).
     */
    private int disposedMarkCount;

    /**
     * Position at offset == 0 that is first in array and never moves.
     * <br/>
     * Hard reference to it here ensures that its mark will never be GCed.
     */
    private static final EditorPosition zeroPos = new EditorPosition();
    private static final Mark zeroMark = new Mark(null, 0, zeroPos);
    
    MarkVector(Object lock, boolean backwardBias) {
        this.lock = lock;
        this.backwardBiasMarks = backwardBias;
        // Create mark array with two free slots
        markArray = new Mark[gapLength = 2]; // gapStart == 0
        offsetGapStart = 1; // Above offset == 0
        offsetGapLength = INITIAL_OFFSET_GAP_SIZE;
        
    }
    
    /**
     * Whether this mark vector serves regular marks or backward-bias marks.
     * 
     * @return true for BB marks or false otherwise.
     */
    boolean isBackwardBiasMarks() {
        return backwardBiasMarks;
    }

    /**
     * Create position (or get an existing one) for the given offset.
     * Needs explicit synchronization.
     *
     * @param offset 
     */
    public EditorPosition position(int offset) {
        if (offset == 0) { // Both regular and backward-bias marks use zeroPos
            return zeroPos;
        }
        EditorPosition pos;
        // Find existing position by binary-search or create a new position
        // For better consistency (see EDC javadoc) the last mark
        // (in a series of marks with same offset) is returned.
        int low = 0;
        int markCount = markCount();
        int high = markCount - 1;
        int rawOffset = rawOffset(offset);
        if (!backwardBiasMarks) { // Regular marks
            while (low <= high) {
                int mid = (low + high) >>> 1;
                // Search for index that follows marks with "rawOffset"
                if (getMark(mid).rawOffset() <= rawOffset) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            // "low" points to mark insertion point and high == low - 1
            Mark mark;
            if (high >= 0 && (mark = getMark(high)).rawOffset() == rawOffset && (pos = mark.get()) != null) {
                return pos; // Reuse
            }

        } else { // Backward bias marks => return first mark with the index
            while (low <= high) {
                int mid = (low + high) >>> 1;
                // Search for index that points to first mark with "rawOffset"
                if (getMark(mid).rawOffset() < rawOffset) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            // "low" >= 0 points to mark with rawOffset or a higher offset
            Mark mark;
            if (low < markCount && (mark = getMark(low)).rawOffset() == rawOffset && (pos = mark.get()) != null) {
                return pos; // Reuse
            }
        }
        return createPosition(low, offset);
    }
    
    private EditorPosition createPosition(int index, int offset) {
        if (index != gapStart) {
            moveGap(index);
        }
        if (gapLength == 0) {
            // Although gapLength == 0 so single arraycopy() could be used
            // the gap should be present at gapStart index for insertion of the new mark.
            reallocate(Math.max(4, markArray.length >>> 1));
        }
        if (offset >= offsetGapStart) {
            offset += offsetGapLength;
        }
        EditorPosition pos = new EditorPosition();
        markArray[gapStart++] = new Mark(this, offset, pos);
        gapLength--;
        return pos;
    }
    
    /**
     * Update mark vector by insertion. Needs explicit synchronization.
     *
     * @param offset offset >= 0
     * @param length length > 0 
     * @param markUpdates possible mark updates in case this is redo of a removal.
     * @return position at offset or null (if offsetPos was false).
     */
    void insertUpdate(int offset, int length, MarkUpdate[] markUpdates) {
        // According to AbstractDocument's implementation positions at offset=0 should stay
        // so treat them like backward-bias marks
        boolean backwardBiasHandling = isBackwardBiasMarks() || offset == 0;
        int index; // index in mark array to which the offset gap gets moved
        if (!backwardBiasHandling) { // Regular marks
            index = findFirstIndex(offset);
            if (offsetGapStart != offset) {
                moveOffsetGap(offset, index);
            }

        } else { // Backward-bias marks or marks at offset == 0
            int newGapOffset = offset + 1;
            // Find first index with offset+1 or higher so offsetGapStart will be at offset+1
            index = findFirstIndex(newGapOffset);
            if (offsetGapStart != newGapOffset) {
                moveOffsetGap(newGapOffset, index);
            }
        }
        // Shift the offset gap right to the end of insertion (to end-of-insertion+1 for backward-bias marks).
        // This is necessary since marks from markUpdates assume it and so they update to below-gap offsets.
        offsetGapStart += length;
        offsetGapLength -= length; // Shrink the offset gap to process insertion

        // If markUpdates != null (retain original offsets) then marks could become "unsorted":
        // 1. Position pos = doc.createPosition(2);
        // 2. doc.remove(2, 1); // ensures "marking" of pos to return to offset==2
        // 3. doc.insert(context, 2, "x"); // pos moves to offset==3
        // 4. Position pos2 = doc.createPosition(2); // creates new position at offset==2
        // 5. undoManager.undo(); // both pos and pos2 at offset==2; pos2 before pos
        // 6. undoManager.undo(); // pos2 at offset==3; pos2 returned to offset==2 but pos2 before pos!
        //
        // For remove(offset, n) the markUpdates content is created by taking a consecutive array of marks
        // with <offset, offset+n) and resetting their offset to "offset" and remembering their
        // original offsets in MarkUpdate[].
        // Mark must be resorted so that marks with offset are the first followed by marks with the updated offset.
        // Some marks in markUpdates array might already be removed from markArray since their position
        // was GCed so this must be taken into consideration too.
        if (markUpdates != null) { // Possibly restore marks' original offsets
            int activeMarkUpdatesCount = 0;
            for (int i = 0; i < markUpdates.length; i++) {
                MarkUpdate update = markUpdates[i];
                if (update.mark.isActive()) {
                    update.restoreRawOffset();
                    markUpdates[activeMarkUpdatesCount++] = update; // Filter markUpdates to valid ones
                }
            }
            // markUpdates array is ordered appropriately however the marks in markArray may require sorting.
            if (activeMarkUpdatesCount > 0) {
                int foundRestoredMarkCount = 0; // Number of active marks found during scan
                if (!backwardBiasHandling) {
                    int rawOffset = offsetGapStart + offsetGapLength;
                    for (int i = index;; i++) { // Scan upwards; index points at first mark with 'offset'
                        if (getMark(i).rawOffset() != rawOffset) { // Found updated mark
                            foundRestoredMarkCount++;
                            if (foundRestoredMarkCount == activeMarkUpdatesCount) { // Found them all
                                if (i >= index + activeMarkUpdatesCount) { // Only if any extra marks at offset are present
                                    // Go back and copy all marks with 'offset' into continuous area
                                    int tgtI = i;
                                    i--;
                                    do {
                                        Mark mark = getMark(i);
                                        if (mark.rawOffset() == rawOffset) {
                                            setMark(tgtI--, mark);
                                        }
                                        i--;
                                    } while (i >= index);
                                }
                                // Copy the updated (sorted) marks to begining (starting at index)
                                for (int j = activeMarkUpdatesCount - 1; j >= 0; j--) {
                                    setMark(index + j, markUpdates[j].mark);
                                }
                                break;
                            }
                        }
                    }
                } else { // backward-bias marks or marks at offset == 0
                    // "index" corresponds to offset+1 (or offset+1+length)
                    // Locate last updated mark and swap group to end
                    for (int i = index - 1;; i--) { // The updated mark must be there
                        if (getMark(i).rawOffset() != offset) {
                            foundRestoredMarkCount++;
                            if (foundRestoredMarkCount == activeMarkUpdatesCount) {
                                if (i < index - activeMarkUpdatesCount) { // Only if not already ordered properly
                                    // Must move initial marks backward right beyond those from filtered markUpdates
                                    int tgtI = i;
                                    i++;
                                    do {
                                        Mark mark = getMark(i);
                                        if (mark.rawOffset() == offset) {
                                            setMark(tgtI++, mark);
                                        }
                                        i++;
                                    } while (i < index);
                                }
                                i = index - activeMarkUpdatesCount;
                                for (int j = activeMarkUpdatesCount - 1; j >= 0; j--) {
                                    setMark(i + j, markUpdates[j].mark);
                                }
                                break;
                            }
                        }
                    }
                }

                // The following alternative algorithm relies on assumption that the marks
                // (that just had their offset corrected) had the same offset after the removal
                // and thus they also retained their physical order with no "intruder" mark inserted in between them.
                // The algorithm above does no rely on that.
                // Since the EditorDocumentContentTest.testWholeDocRemove() was failing a variant above was created.
                // However the test failure was caused by something else so the algorithm is still candidate
                // since it's more efficient than the one above.
//                if (!backwardBiasHandling) {
//                    Mark firstUpdatedMark = markUpdates[0].mark;
//                    for (int i = index;; i++) { // The updated mark must be there
//                        if (getMark(i) == firstUpdatedMark) {
//                            if (i > index) {
//                                // Must move initial marks forward right beyond those from filtered markUpdates
//                                // Cannot use System.arraycopy() due to gap-storage
//                                for (int j = i - index - 1; j >= 0; j--) { // Copy backward
//                                    setMark(index + activeMarkUpdatesCount + j, getMark(index + j));
//                                }
//                                for (int j = activeMarkUpdatesCount - 1; j >= 0; j--) {
//                                    setMark(index + j, markUpdates[j].mark);
//                                }
//                            }
//                            break;
//                        }
//                    }
//                } else { // backward-bias marks or marks at offset == 0
//                    // For backward bias the active mark updates will be above the non-updated marks.
//                    // "index" corresponds to offset+1 (or offset+1+length)
//                    Mark lastUpdatedMark = markUpdates[activeMarkUpdatesCount - 1].mark;
//                    for (int i = index - 1;; i--) { // The updated mark must be there
//                        if (getMark(i) == lastUpdatedMark) {
//                            i++; // First mark to move
//                            if (i < index) {
//                                // Must move initial marks backward right beyond those from filtered markUpdates
//                                int count = index - i;
//                                for (int j = 0; j < count; j++) {
//                                    setMark(i - activeMarkUpdatesCount + j, getMark(i + j));
//                                }
//                                i = index - activeMarkUpdatesCount;
//                                for (int j = activeMarkUpdatesCount - 1; j >= 0; j--) {
//                                    setMark(i + j, markUpdates[j].mark);
//                                }
//                            }
//                            break;
//                        }
//                    }
//                }
            }
        }

    }

    /**
     * Update mark vector by removal. Needs explicit synchronization.
     * 
     * @param offset offset >= 0.
     * @param length length >= 0.
     * @return mark updates or null if none needed.
     */
    MarkUpdate[] removeUpdate(int offset, int length) {
        int index; // Index of first mark to restore its offset upon undo of removal
        int endIndex; // End index of marks to restore their offset upon undo of removal
        int biasOffset = offset;
        boolean backwardBiasHandling = isBackwardBiasMarks() || offset == 0;
        if (backwardBiasHandling) {
            biasOffset++;
        }
        int newGapOffset = biasOffset + length;
        endIndex = findFirstIndex(newGapOffset);
        // Move offset gap to endIndex to have natural offsets in RemoveMarkUpdate
        if (newGapOffset != offsetGapStart) {
            moveOffsetGap(newGapOffset, endIndex);
        }
        
        offsetGapStart -= length;
        offsetGapLength += length;
        // Move all positions inside the removed area to its begining.
        // First scan which marks need to be moved to create array of proper size.
        // Affected mark count is small for typical removals so linear iteration
        // is faster than another binary search.
        for (index = endIndex - 1; index >= 0; index--) {
            Mark mark = getMark(index);
            if (mark.rawOffset() < biasOffset) { // btw offset-gap is at (biasOffset+length).
                break;
            }
        }
        index++;

        // Remember original marks' offsets for possible undo
        int updateCount = endIndex - index;
        MarkUpdate[] updates;
        if (updateCount > 0) {
            updates = new MarkUpdate[updateCount];
            // Regular marks:
            // Offset gap was moved to (offset+length) i.e. to offset (after actual removal)
            // so all the marks "inside" removed area need to be at 'offset' but above offset gap.
            // Backward-bias marks:
            // Offset gap will be at offset + 1 so all marks will be below gap.
            int newRawOffset = offset;
            if (!backwardBiasHandling) {
                newRawOffset += offsetGapLength;
            }
            for (int i = updateCount - 1; i >= 0; i--) {
                Mark mark = getMark(index + i);
                updates[i] = new MarkUpdate(mark);
                // Fix the mark (it's below gap)
                mark.rawOffset = newRawOffset;
            }
        } else {
            updates = null;
        }
        return updates;
    }
    
    /**
     * Move offset gap so that its start is at particular offset and a corresponding (pre-computed) index.
     *
     * @param newOffsetGapStart new gap start offset.
     * @param index pre-computed index corresponding to new gap start offset.
     *  When offset &gt;= newOffsetGapStart it's above gap.
     */
    private void moveOffsetGap(int newOffsetGapStart, int index) {
        int rawIndex = rawIndex(index);
        int markArrayLength = markArray.length;
        int origOffsetGapStart = offsetGapStart;
        offsetGapStart = newOffsetGapStart;

        if (rawIndex == markArrayLength || markArray[rawIndex].rawOffset() > origOffsetGapStart) {
            // Mark at rawIndex is above the gap
            // Go down and move marks below gap
            if (rawIndex >= gapStart) {
                int gapEnd = gapStart + gapLength;
                while (--rawIndex >= gapEnd) {
                    Mark mark = markArray[rawIndex];
                    if (mark.rawOffset() > origOffsetGapStart) {
                        mark.rawOffset -= offsetGapLength;
                    } else {
                        return;
                    }
                }
                rawIndex = gapStart; // Continue on gapStart-1
            }
            while (--rawIndex >= 0) {
                Mark mark = markArray[rawIndex];
                if (mark.rawOffset() > origOffsetGapStart) {
                    mark.rawOffset -= offsetGapLength;
                } else {
                    return;
                }
            }
            
        } else { // go up to check and move the marks to be beyond the offset gap
            if (rawIndex < gapStart) {
                while (rawIndex < gapStart) {
                    Mark mark = markArray[rawIndex++];
                    if (mark.rawOffset() <= origOffsetGapStart) {
                        mark.rawOffset += offsetGapLength;
                    } else {
                        return;
                    }
                }
                rawIndex += gapLength;
            }
            while (rawIndex < markArrayLength) {
                Mark mark = markArray[rawIndex++];
                if (mark.rawOffset() <= origOffsetGapStart) {
                    mark.rawOffset += offsetGapLength;
                } else {
                    return;
                }
            }
        }
    }

    private void moveGap(int index) {
        // No need to clear the stale space in mark array after arraycopy() since removeDisposedMarks() will do it
        if (index <= gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(markArray, index, markArray, gapStart + gapLength - moveSize, moveSize);
        } else { // above gap
            int moveSize = index - gapStart;
            System.arraycopy(markArray, gapStart + gapLength, markArray, gapStart, moveSize);
        }
        gapStart = index;
    }

    /**
     * Compact array of marks. Needs explicit synchronization.
     */
    void compact() {
        if (gapLength > 4) { // Possibly leave 4 free mark slots for little expansion
            reallocate(4);
        }
    }

    private void reallocate(int newGapLength) {
        int gapEnd = gapStart + gapLength;
        int aboveGapLength = markArray.length - gapEnd;
        int newLength = gapStart + aboveGapLength + newGapLength;
        Mark[] newMarkArray = new Mark[newLength];
        System.arraycopy(markArray, 0, newMarkArray, 0, gapStart);
        System.arraycopy(markArray, gapEnd, newMarkArray, newLength - aboveGapLength, aboveGapLength);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("MarkVector.reallocate() from markArray.length=" + markArray.length + " to newLength=" + newLength + "\n");
        }
        // gapStart is same
        gapLength = newGapLength;
        markArray = newMarkArray;
    }

    /**
     * Find an index of the first mark with the given offset (if there would be multiple marks with the same offset).
     *
     * @param offset &gt;=0
     * @return index &gt;=0 where a first mark with the given offset (or a higher offset) resides.
     */
    private int findFirstIndex(int offset) {
        // Find existing position or create a new position
        int low = 0;
        int markCount = markCount();
        int high = markCount - 1;
        int rawOffset = rawOffset(offset);
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (getMark(mid).rawOffset() < rawOffset) { // Search for first with "rawOffset"
                low = mid + 1;
            } else { // markRawOffset >= rawOffset
                high = mid - 1;
            }
        }
        return low; // "low" points to first mark with the given offset
    }

    int offset(int rawOffset) {
        return (rawOffset < offsetGapStart) ? rawOffset : (rawOffset - offsetGapLength);
    }
    
    int rawOffset(int offset) {
        return (offset < offsetGapStart) ? offset : offset + offsetGapLength;
    }

    private int rawIndex(int index) {
        return (index < gapStart)
                ? index
                : index + gapLength;
    }

    private int markCount() {
        return markArray.length - gapLength;
    }

    private Mark getMark(int index) {
        return markArray[rawIndex(index)];
    }
    
    private void setMark(int index, Mark mark) {
        markArray[rawIndex(index)] = mark;
    }

    void notifyMarkDisposed() {
        synchronized (lock) {
            disposedMarkCount++;
            if (disposedMarkCount > Math.max(5, markCount() >> 3)) {
                removeDisposedMarks();
            }
        }
    }

    private void removeDisposedMarks() {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("MarkVector.removeDisposedMarks() disposedMarkCount=" + disposedMarkCount + "\n");
        }
        int rawIndex = 0;
        int validIndex = 0;
        int gapEnd = gapStart + gapLength;
        // Only retain marks with valid position
        while (rawIndex < gapStart) {
            Mark mark = markArray[rawIndex];
            if (mark.get() != null) { // valid mark
                if (rawIndex != validIndex) {
                    markArray[validIndex] = mark;
                }
                validIndex++;
            } else {
                mark.clearMarkVector();
            }
            rawIndex++;
        }
        gapStart = validIndex;
        // Go back from end till gap end
        rawIndex = markArray.length;
        int topValidIndex = rawIndex; // validIndex points to first valid mark above gap
        while (--rawIndex >= gapEnd) {
            Mark mark = markArray[rawIndex];
            if (mark.get() != null) { // valid mark
                if (rawIndex != --topValidIndex) {
                    markArray[topValidIndex] = mark;
                }
            } else {
                mark.clearMarkVector();
            }
        }
        int newGapLength = topValidIndex - gapStart;
        gapLength = newGapLength;
        // Clear the area between valid indices (also because moveGap() does not clear the stale areas)
        while (validIndex < topValidIndex) {
            markArray[validIndex++] = null;
        }
        // Set disposedMarkCount to zero. Since the "markVector" field was cleared in the marks
        // removed from markArray and so those marks pending in the queue
        // will no longer notify markVector.notifyMarkDisposed().
        disposedMarkCount = 0;
    }
    
    String consistencyError(int maxOffset) {
        int markCount = markCount();
        int lastOffset = 0;
        for (int i = 0; i < markCount; i++) {
            Mark mark = getMark(i);
            int offset = mark.getOffset();
            int rawOffset = mark.rawOffset();
            String err = null;
            if (offset < lastOffset) {
                err = "offset=" + offset + " < lastOffset=" + lastOffset; // NOI18N
            } else if (rawOffset < 0) {
                err = "rawOffset=" + rawOffset + " < 0";
            } else if (offset > maxOffset) {
                err = "offset=" + offset + " > maxOffset=" + maxOffset; // NOI18N
            } else if (offset < offsetGapStart && rawOffset >= offsetGapStart) {
                err = "offset=" + offset + " but rawOffset=" + rawOffset + // NOI18N
                        " >= offsetGapStart=" + offsetGapStart; // NOI18N
            } else if (offset >= offsetGapStart && rawOffset < offsetGapStart + offsetGapLength) {
                err = "offset=" + offset + " but rawOffset=" + rawOffset + // NOI18N
                        " < offsetGapStart=" + offsetGapStart + " + offsetGapLength=" + offsetGapLength; // NOI18N
            }
            if (err != null) {
                return (isBackwardBiasMarks() ? "BB-" : "") + "markArray[" + i + "]: " + err; // NOI18N
            }
            lastOffset = offset;
        }
        if (zeroPos.getOffset() != 0) {
            return "zeroPos.getOffset()=" + zeroPos.getOffset() + " != 0"; // NOI18N
        }
        return null;
    }

    /** Get info about this mark vector. */
    @Override
    public String toString() {
        return (isBackwardBiasMarks() ? "BB:" : "") + "markCount=" + markCount() + // NOI18N
                ", gap:" + CharContent.gapToString(markArray.length, gapStart, gapLength) + // NOI18N
                ", OGap:<0," + offsetGapStart + ")" + offsetGapLength + // NOI18N
                '<' + (offsetGapStart+offsetGapLength) + ",...>"; // NOI18N
    }
    
    public String toStringDetail(Mark accentMark) {
        StringBuilder sb = new StringBuilder(200);
        sb.append(toString()).append(", IHC=").append(System.identityHashCode(this)).append('\n');
        int markCount = markCount();
        int digitCount = ArrayUtilities.digitCount(markCount);
        for (int i = 0; i < markCount; i++) {
            Mark mark = getMark(i);
            sb.append((mark == accentMark) ? "**" : "  ");
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(mark.toStringDetail()).append('\n');
        }
        return sb.toString();
    }
    
    static StringBuilder markUpdatesToString(StringBuilder sb, MarkUpdate[] markUpdates, int length) {
        int digitCount = ArrayUtilities.digitCount(length);
        for (int i = 0; i < length; i++) {
            sb.append("    ");
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(markUpdates[i]).append('\n'); // NOI18N
        }
        return sb;
    }

    /**
     * Class used for holding the offset to which the mark
     * will be restored in case it was inside the area 
     * that was removed.
     */
    static final class MarkUpdate {

        final Mark mark;

        /**
         * Original offset that should be used for restoring of the mark's offset upon undo.
         */
        final int origRawOffset;
        
        MarkUpdate(Mark mark) {
            this.mark = mark;
            this.origRawOffset = mark.rawOffset();
        }
        
        void restoreRawOffset() {
            mark.rawOffset = origRawOffset;
        }

        @Override
        public String toString() {
            return mark.toStringDetail() + " <= OrigRaw:" + origRawOffset; // NOI18N
        }

    }

}
