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

package org.netbeans.lib.editor.util;

/**
 * Extension of the gap list which is expected to store elements
 * that have an integer offset stored in them.
 * <br>
 * The elements must be stored in the list in an ascending order.
 * <br>
 * To efficiently manage offsets that need to change upon
 * inserts and removes into an underlying storage (e.g. a swing document)
 * the list maintains an offset gap that gets moved
 * and shrinked/extended according to inserts/removals.
 *
 * <p>
 * The physical raw offset stored in the element needs to be preprocessed
 * to get the real offset value.
 * <br>
 * In short the raw offset is either the actual offset in case
 * the offset gap is above it (greater or equal to <code>offsetGapStart</code> value)
 * or, it's the actual offset plus the offset gap length otherwise.
 *
 * <p>
 * Offsets up to +1GB (<code>Integer.MAX_VALUE / 2</code>)
 * can be handled by this class which should be sufficient
 * for most uses.
 * <br>
 * It's not +2GB as then the offsets shifted above the offset gap
 * would overflow and be below zero which would break
 * the comparisons whether the offset is below offset gap start.
 * <br>
 * Negative offsets are supported as well with a limit of -2GB.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class OffsetGapList<E> extends GapList<E> {
    
    private int offsetGapStart; // 28 bytes (24-super + 4)

    private int offsetGapLength = Integer.MAX_VALUE / 2; // 32 bytes

    public OffsetGapList() {
        this(10);
    }

    public OffsetGapList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Get the raw offset of the given element currently stored in the list.
     *
     * @param elem element currently stored in the list.
     * @return raw offset of the element. It needs to be preprocessed
     * by {@link #raw2Offset(int)} to become the real offset.
     */
    protected abstract int elementRawOffset(E elem);

    /**
     * Set the raw offset of the given element currently stored in the list.
     *
     * @param elem element currently stored in the list.
     * @param rawOffset raw offset to be stored in the given element.
     */
    protected abstract void setElementRawOffset(E elem, int rawOffset);
    
    /**
     * Get the offset of the element stored in the list.
     * <br>
     * The raw offset stored in the element is preprocessed to become a real offset.
     *
     * @param elem element stored in the list.
     * @return offset of the element.
     */
    protected int elementOffset(E elem) {
        return raw2Offset(elementRawOffset(elem));
    }
    
    /**
     * Get the offset of the element stored in the list at the given index.
     * <br>
     * The raw offset stored in the element is preprocessed to become a real offset.
     *
     * @param index of the element in the list.
     * @return offset of the element.
     * @throws IndexOutOfBoundsException if index >= size() or lower than zero
     */
    protected int elementOffset(int index) {
        return elementOffset(get(index));
    }
    
    /**
     * Inform the list that there was an insert done into an underlying storage
     * (e.g. a swing document) which should move up offsets of the elements that have
     * their offset greater or equal to the insertion offset.
     *
     * <p>
     * Subclasses can build their own way of updating
     * and they are not required to use this method.
     *
     * @param offset offset at which the insertion occurred.
     * @param length length of the inserted area.
     */
    public void defaultInsertUpdate(int offset, int length) {
        assert (length >= 0);
        if (offset != offsetGapStart()) {
            moveOffsetGap(offset, findElementIndex(offset));
        }
        updateOffsetGapLength(-length); // Shrink the offset gap to simulate insertion
        // Optimize for subsequent insert by moving gap start to the end
        // of the just performed insertion.
        // This way the subsequent insert will not need to call moveOffsetGap() at all.
        // It's less optimal for insert-remove pairs (e.g. overwrite mode)
        // but they should be less frequent.
        updateOffsetGapStart(length);
    }

    /**
     * Inform the list that there was a removal done into an underlying storage
     * (e.g. a swing document) which should move down offsets of the elements
     * that have their offsets greater than the removal offset.
     * <br>
     * The offsets inside the removal area will be moved to its begining.
     * <p>
     * Subclasses can build their own way of updating
     * and they are not required to use this method.
     *
     * @param offset offset at which the removal occurred.
     * @param length length of the removed area.
     */
    public void defaultRemoveUpdate(int offset, int length) {
        assert (length >= 0);
        int index = findElementIndex(offset);
        if (offset != offsetGapStart()) {
            moveOffsetGap(offset, index);
        }
        int size = size();
        int removeAreaEndRawOffset = offset + offsetGapLength + length;
        // Move all elements inside the removed area to its end
        // so that after update of the offset gap length they appear
        // at the begining of the removal offset area
        while (index < size) {
            E elem = get(index++);
            if (elementRawOffset(elem) < removeAreaEndRawOffset) {
                setElementRawOffset(elem, removeAreaEndRawOffset);
            } else { // all subsequent offsets are higher
                break;
            }
        }
        updateOffsetGapLength(+length);
    }
    
    /**
     * Move the offset gap so that it's on the requested offset.
     * <br>
     * This method can be used when the index of the first element
     * at the given offset was precomputed already.
     *
     * <p>
     * <b>Note:</b> Improper use of this may logically damage
     * offsets of the elements contained in the list.
     *
     * @param offset offset to which the <code>offsetGapStart</code>
     *  should be assigned.
     * @param index index of the first element in the list
     *  that has an offset that is greater or equal that the given offset parameter.
     *  <br>
     *  It may be computed by {@link #findElementIndex(int)}.
     */
    protected final void moveOffsetGap(int offset, int index) {
        if (offset < offsetGapStart) { // need to check items above index
            int bound = size();
            for (int i = index; i < bound; i++) {
                E elem = get(i);
                int rawOffset = elementRawOffset(elem);
                if (rawOffset < offsetGapStart) {
                    setElementRawOffset(elem, rawOffset + offsetGapLength);
                } else {
                    break;
                }
            }

        } else {  // check items below index
            for (int i = index - 1; i >= 0; i--) {
                E elem = get(i);
                int rawOffset = elementRawOffset(elem);
                if (rawOffset >= offsetGapStart) {
                    setElementRawOffset(elem, rawOffset - offsetGapLength);
                } else {
                    break;
                }
            }
        }
        offsetGapStart = offset;
    }

    /**
     * Obtain the start of the offset gap.
     *
     * @return start of the offset gap.
     */
    protected final int offsetGapStart() {
        return offsetGapStart;
    }

    /**
     * Update the offset gap start by the given delta.
     * <br>
     * This may be needed e.g. after insertion/removal was done
     * in the document.
     *
     * <p>
     * <b>Note:</b> Improper use of this may logically damage
     * offsets of the elements contained in the list.
     */ 
    protected final void updateOffsetGapStart(int offsetDelta) {
        offsetGapStart += offsetDelta;
    }

    /**
     * Obtain the length of the offset gap.
     *
     * @return length of the offset gap.
     */
    protected final int offsetGapLength() {
        return offsetGapLength;
    }

    /**
     * Update the offset gap length by the given delta.
     * <br>
     * This may be needed e.g. after insertion/removal was done
     * in the document.
     *
     * <p>
     * <b>Note:</b> Improper use of this may logically damage
     * offsets of the elements contained in the list.
     */ 
    protected final void updateOffsetGapLength(int offsetGapLengthDelta) {
        offsetGapLength += offsetGapLengthDelta;
        assert (offsetGapLength >= 0); // prevent overflow to negative numbers
    }

    /**
     * Find an index of the first element at the given offset in the list
     * by using binary search.
     *
     * @param offset offset of the element
     * @return index of the element. If there is no element with that
     *  index then the index of the next element (with the greater offset)
     *  (or size of the list) will be returned.
     *  <br>
     *  If there are multiple items with the same offset then the first one of them
     *  will be returned.
     */
    protected final int findElementIndex(int offset) {
        int low = 0;
        int high = size() - 1;

        while (low <= high) {
            int index = (low + high) / 2; // mid in the binary search
            int elemOffset = elementOffset(index);

            if (elemOffset < offset) {
                low = index + 1;

            } else if (elemOffset > offset) {
                high = index - 1;

            } else { // exact offset found at index
                while (index > 0) {
                    index--;
                    if (elementOffset(index) < offset) {
                        index++;
                        break;
                    }
                }
                low = index;
                break;
            }
        }
        return low;
    }

    /**
     * This method updates element's offset (shifts it above offset gap if necessary)
     * before adding the element to the list.
     * <bt/>
     * This method should be called before (or after) the element is physically added
     * to the list. If the element is added below the offset gap
     * then calling of this method is not necessary.
     */
    protected void updateElementOffsetAdd(E elem) {
        int rawOffset = elementRawOffset(elem);
        if (rawOffset >= offsetGapStart) {
            setElementRawOffset(elem, rawOffset + offsetGapLength);
        }
    }

    /**
     * This method updates element's offset (shifts it below offset gap if necessary)
     * before (or after) the element gets removed from the list.
     * <br/>
     * This method should be called after the element is physically removed
     * from the list and it's desired that it retains its natural offset
     * (not possibly shifted by the offset gap length).
     * <br/>
     * If the element was located below the offset gap prior removal
     * then calling of this method is not necessary.
     */
    protected void updateElementOffsetRemove(E elem) {
        int rawOffset = elementRawOffset(elem);
        if (rawOffset >= offsetGapStart) {
            setElementRawOffset(elem, rawOffset - offsetGapLength);
        }
    }
    
    /**
     * Translate raw offset into real offset.
     *
     * @param rawOffset raw offset stored in an element.
     * @return real offset that the element is supposed to have.
     */
    protected final int raw2Offset(int rawOffset) {
        return (rawOffset < offsetGapStart)
            ? rawOffset
            : rawOffset - offsetGapLength;
    }
    
    /**
     * Translate regular offset to raw offset.
     *
     * @param offset regular offset.
     * @return raw offset that can be used in elements.
     */
    protected final int offset2raw(int offset) {
        return (offset < offsetGapStart)
            ? offset
            : offset + offsetGapLength;
    }
    
    protected void consistencyCheck() {
        super.consistencyCheck();

        if (offsetGapLength < 0) {
            consistencyError("offsetGapLength < 0"); // NOI18N
        }

        int lastRawOffset = Integer.MIN_VALUE;
        int lastOffset = Integer.MIN_VALUE;
        int size = size();
        for (int i = 0; i < size; i++) {
            E elem = get(i);
            int rawOffset = elementRawOffset(elem);
            int offset = raw2Offset(rawOffset);
            if (rawOffset < lastRawOffset) {
                consistencyError("Invalid rawOffset=" // NOI18N
                    + rawOffset + " >= lastRawOffset=" + lastRawOffset // NOI18N
                    + " at index=" + i // NOI18N
                );
            }
            if (offset < lastOffset) {
                consistencyError("Invalid offset=" // NOI18N
                    + offset + " >= lastOffset=" + lastOffset // NOI18N
                    + " at index=" + i // NOI18N
                );
            }
            lastRawOffset = rawOffset;
            lastOffset = offset;
        }
    }
    
    protected String dumpInternals() {
        return super.dumpInternals() + ", offGap(s=" + offsetGapStart
        + ", l=" + offsetGapLength + ")";
    }

}
