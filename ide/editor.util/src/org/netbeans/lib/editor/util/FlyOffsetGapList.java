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

package org.netbeans.lib.editor.util;

/**
 * A variant of {@link OffsetGapList} additionally supporting
 * flyweight (non-modifiable) elements.
 * <br>
 * Flyweight elements may not be modified during update operations
 * when offset gap is moved and the raw offset must be updated
 * or when the flyweight element is added or removed from the list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class FlyOffsetGapList<E> extends GapList<E> {

    private int offsetGapStart; // 28 bytes (24-super + 4)

    private int offsetGapLength = Integer.MAX_VALUE / 2; // 32 bytes

    public FlyOffsetGapList() {
        this(10);
    }

    public FlyOffsetGapList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Get the raw offset of the given element currently stored in the list.
     *
     * @param elem element currently stored in the list.
     * @return raw offset of the element. It needs to be preprocessed
     * by {@link #raw2RelOffset(int)} to become the real offset.
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
     * Check whether the given element is flyweight and therefore skipped
     * during update operations.
     *
     * @return true if the given element is flyweight or false otherwise.
     */
    protected abstract boolean isElementFlyweight(E elem);
    
    /**
     * Get length of an element (assuming it has a length when it has
     * an offset).
     * <br>
     * As there can be flyweight elements it's assumed that the flyweight
     * elements occupy certain offset area.
     * <br>
     * This method is primarily used for measuring of flyweight elements
     * (except consistency check) but subclasses may extend its use to any element.
     */
    protected abstract int elementLength(E elem);

    /**
     * Return base starting offset to which all the tokens contained in this list
     * are related. The absolute token's offset is a sum of this start offset plus
     * token's offset.
     * <br>
     * There may be just flyweight element(s) at the begining of the list
     * so the start offset gives the necessary basing.
     * <br>
     * By default it's zero.
     */
    protected int startOffset() {
        return 0;
    }
    
    /**
     * Get the offset of an element stored in the list at the given index.
     * <br>
     * The raw offset stored in the element is preprocessed to become a real offset.
     * <br>
     * Flyweight elements are supported and they are asked for occupied length
     * by {@link #elementLength(Object)} and its sum
     * plus the first preceding non-flyweight element's offset is returned.
     * <br>
     * If there is no preceding non-flyweight element a zero offset
     * is used as a base.
     *
     * @param index of the element in the list.
     * @return offset of the element. It will include {@link #startOffset()}.
     * @throws IndexOutOfBoundsException if index >= size() or lower than zero
     */
    protected final int elementOffset(int index) {
        E elem = get(index);
        int offset;
        if (isElementFlyweight(elem)) {
            offset = 0;
            while (--index >= 0) {
                elem = get(index);
                offset += elementLength(elem);
                if (!isElementFlyweight(elem)) {
                    // non-flyweight element
                    offset += raw2RelOffset(elementRawOffset(elem));
                    break;
                }
            }
            
        } else { // non-flyweight
            offset = raw2RelOffset(elementRawOffset(elem));
        }
        return startOffset() + offset;
    }
    
    /**
     * Get the offset of an element stored in the list at the given index
     * like {@link #elementOffset(int)} does or get end offset of the last element
     * if {@link #size()} is passed as index parameter.
     *
     * @param indexOrSize index of the element in the list.
     *  Index equal to <code>size()</code> can be used to get end offset
     *  of the last element.
     * @return offset of the element. It will include {@link #startOffset()}.
     * @throws IndexOutOfBoundsException if index > size() or lower than zero
     */
    protected final int elementOrEndOffset(int indexOrSize) {
        E elem;
        int offset;
        // Fail for index > size() and for == size() use end of the last element
        if (indexOrSize == size() || isElementFlyweight(elem = get(indexOrSize))) {
            offset = 0;
            while (--indexOrSize >= 0) {
                elem = get(indexOrSize);
                offset += elementLength(elem);
                if (!isElementFlyweight(elem)) {
                    // non-flyweight element
                    offset += raw2RelOffset(elementRawOffset(elem));
                    break;
                }
            }
            
        } else { // non-flyweight
            offset = raw2RelOffset(elementRawOffset(elem));
        }
        return startOffset() + offset;
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
     * @param offset offset at which the insertion occurred. It should not include {@link #startOffset()}.
     * @param length length of the inserted area.
     */
    public void defaultInsertUpdate(int offset, int length) {
        assert (length >= 0);
        if (offset != offsetGapStart()) {
            moveOffsetGap(offset, findElementIndex(offset));
        }
        updateOffsetGapLength(-length);
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
     * @param offset offset at which the removal occurred. It should not include {@link #startOffset()}.
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
            if (!isElementFlyweight(elem)) {
                if (elementRawOffset(elem) < removeAreaEndRawOffset) {
                    setElementRawOffset(elem, removeAreaEndRawOffset);
                } else { // all subsequent offsets are higher
                    break;
                }
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
     *  should be assigned. It should not include {@link #startOffset()}.
     * @param index index of the first element at the given offset in the list.
     *  <br>
     *  It may be computed by {@link #findElementIndex(int)}.
     */
    protected final void moveOffsetGap(int offset, int index) {
        if (offset < offsetGapStart) { // need to check items above index
            int bound = size();
            for (int i = index; i < bound; i++) {
                E elem = get(i);
                if (!isElementFlyweight(elem)) {
                    int rawOffset = elementRawOffset(elem);
                    if (rawOffset < offsetGapStart) {
                        setElementRawOffset(elem, rawOffset + offsetGapLength);
                    } else {
                        break;
                    }
                }
            }

        } else {  // check items below index
            for (int i = index - 1; i >= 0; i--) {
                E elem = get(i);
                if (!isElementFlyweight(elem)) {
                    int rawOffset = elementRawOffset(elem);
                    if (rawOffset >= offsetGapStart) {
                        setElementRawOffset(elem, rawOffset - offsetGapLength);
                    } else {
                        break;
                    }
                }
            }
        }
        offsetGapStart = offset;
    }

    protected final int offsetGapStart() {
        return offsetGapStart;
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
    protected final void updateOffsetGapStart(int offsetDelta) {
        offsetGapStart += offsetDelta;
    }

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
     * @param offset offset of the element to be found. It should not include {@link #startOffset()}.
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
     * <br>
     * This method should be called before (or after) the element is physically added
     * to the list. If the element is added below the offset gap
     * then calling of this method is not necessary.
     */
    protected void updateElementOffsetAdd(E elem) {
        if (!isElementFlyweight(elem)) {
            int offset = elementRawOffset(elem); // not raw yet
            setElementRawOffset(elem, offset2Raw(offset));
        }
    }

    /**
     * This method updates element's offset (shifts it below offset gap if necessary)
     * before (or after) the element gets removed from the list.
     * <br>
     * This method should be called after the element is physically removed
     * from the list and it's desired that it retains its natural offset
     * (not possibly shifted by the offset gap length).
     * <br>
     * If the element was located below the offset gap prior removal
     * then calling of this method is not necessary.
     */
    protected void updateElementOffsetRemove(E elem) {
        if (!isElementFlyweight(elem)) {
            int rawOffset = raw2RelOffset(elementRawOffset(elem));
            rawOffset += startOffset();
            setElementRawOffset(elem, rawOffset);
        }
    }
    
    /**
     * Translate raw offset into real offset.
     *
     * @param rawOffset raw offset stored in an element.
     * @return real offset that the element is supposed to have.
     */
    private int raw2RelOffset(int rawOffset) {
        return (rawOffset < offsetGapStart)
            ? rawOffset
            : rawOffset - offsetGapLength;
    }
    
    /**
     * Convert the given offset into raw form suitable for storing in this list.
     *
     * @param offset >=0 absolute offset that includes {@link #startOffset()}.
     * @return corresponding raw offset.
     */
    protected final int offset2Raw(int offset) {
        offset -= startOffset();
        if (offset >= offsetGapStart) {
            offset += offsetGapLength;
        }
        return offset;
    }
    
    /**
     * Check consistency of this list.
     *
     * @param checkElementLength whether {@link #elementLength(Object)}
     *  should be called to check lengths of the elements and verify
     *  that the offsets are in concert with the offsets.
     */
    protected void consistencyCheck(boolean checkElementLength) {
        super.consistencyCheck();

        if (offsetGapLength < 0) {
            consistencyError("offsetGapLength < 0"); // NOI18N
        }

        int lastRawOffset = Integer.MIN_VALUE;
        int lastOffset = Integer.MIN_VALUE;
        int lastEndOffset = lastOffset;
        int size = size();
        for (int i = 0; i < size; i++) {
            E elem = get(i);
            if (!isElementFlyweight(elem)) {
                int rawOffset = elementRawOffset(elem);
                int offset = raw2RelOffset(rawOffset);
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
                if (checkElementLength) { // Use the element's length
                    int length = elementLength(elem);
                    if (i == 0) {
                        lastEndOffset = offset;
                    }
                    if (offset != lastEndOffset) {
                        consistencyError("Offset=" + offset // NOI18N
                            + " differs from lastEndOffset=" + lastEndOffset // NOI18N
                            + " at index=" + i // NOI18N
                        );
                    }
                    lastEndOffset += length;
                }
                lastRawOffset = rawOffset;
                lastOffset = offset;

            } else { // Flyweight element
                if (checkElementLength) {
                    if (i == 0) {
                        // Assume zero as the base offset when flyweight element is first
                        lastEndOffset = 0;
                    }
                    int length = elementLength(elem);
                    lastEndOffset += length;
                }
            }
        }
    }
    
    protected String dumpInternals() {
        return super.dumpInternals() + ", offGap(s=" + offsetGapStart
        + ", l=" + offsetGapLength + ")";
    }

}
