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

package org.netbeans.lib.editor.view;

/**
 * Implementation of {@link ObjectArray} that
 * contains a gap which helps to speed up inserts/removals
 * close to the gap.
 * <P><strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access an instance of this class concurrently, and at
 * least one of the threads inserts/removes items, the whole access <i>must</i> be
 * synchronized externally.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

class GapObjectArray {

    private static final Object[] EMPTY_ARRAY = new Object[0];

    /**
     * Array holding the elements with the gap starting at the <CODE>gapStart</CODE>
     * being <CODE>gapLength</CODE> long.
     */
    private Object[] array;
    
    /** Starting index of the gap in the array. */
    private int gapStart;
    
    /** Length of the gap */
    private int gapLength;
    
    public GapObjectArray() {
        this.array = EMPTY_ARRAY;
    }
    
    /**
     * Construct new gap array of objects.
     * @param array use this array as an initial array for processing.
     *  The array will be modified by subsequent changes. If the array
     *  contents should be preserved the array must be cloned first
     *  before processing.
     * @param length length of the valid part of the array that contains
     *  the objects to be managed. The area must start at the index 0.
     */
    public GapObjectArray(Object[] array, int length) {
        this.array = array;
        this.gapStart = length;
        this.gapLength = array.length - length;
    }
    
    /**
     * Get total number of items in this object array.
     */
    public int getItemCount() {
        return array.length - gapLength;
    }
    
    /**
     * Get item at the given index.
     * @param index &gt=0 and &lt{@link #getItemCount()} index from which
     *  the item should be returned.
     * @return item at the given index.
     */
    public Object getItem(int index) {
        return array[(index < gapStart) ? index : (index + gapLength)];
    }

    public void copyItems(int srcStartIndex, int srcEndIndex,
    Object[] dest, int destIndex) {

        rangeCheck(srcStartIndex, srcEndIndex - srcStartIndex);

        if (srcEndIndex < gapStart) { // fully below gap
            System.arraycopy(array, srcStartIndex,
                dest, destIndex, srcEndIndex - srcStartIndex);

        } else { // above gap or spans the gap
            if (srcStartIndex >= gapStart) { // fully above gap
                System.arraycopy(array, srcStartIndex + gapLength, dest, destIndex,
                    srcEndIndex - srcStartIndex);

            } else { // spans gap
                int beforeGap = gapStart - srcStartIndex;
                System.arraycopy(array, srcStartIndex, dest, destIndex, beforeGap);
                System.arraycopy(array, gapStart + gapLength, dest, destIndex + beforeGap,
                    srcEndIndex - srcStartIndex - beforeGap);
            }
        }
    }
    
    public final int getGapStart() { // to implement GapStart interface
        return gapStart;
    }

    public void replace(int index, int removeCount, Object[] newItems) {
        remove(index, removeCount);
        insertAll(index, newItems);
    }
    
    public void insertItem(int index, Object item) {
        indexCheck(index);
        
        if (gapLength == 0) {
            enlargeGap(1);
        }
        if (index != gapStart) {
            moveGap(index);
        }
        array[gapStart++] = item;
        gapLength--;
    }

    public void insertAll(int index, Object[] items) {
        insertAll(index, items, 0, items.length);
    }
    
    public void insertAll(int index, Object[] items, int off, int len) {
        indexCheck(index);
        
        if (items.length == 0) {
            return;
        }

        int extraLength = len - gapLength;
        if (extraLength > 0) {
            enlargeGap(extraLength);
        }
        if (index != gapStart) {
            moveGap(index);
        }
        System.arraycopy(items, off, array, gapStart, len);
        gapStart += len;
        gapLength -= len;
    }
    
    public void ensureCapacity(int minCapacity) {
        if (minCapacity < 0) {
            throw new IllegalArgumentException("minCapacity=" + minCapacity + " < 0"); // NOI18N
        }
        
        int capacity = array.length;
        if (capacity == 0) { // use exactly what requested if empty now
            setCapacity(minCapacity);
        } else if (minCapacity > capacity) { // some items already exist
            enlargeGap(minCapacity - capacity);
        }
    }
    
    public void remove(int index, int count) {
        remove(index, count, null);
    }
    
    public void remove(int index, int count, RemoveUpdater removeUpdater) {
        rangeCheck(index, count);

        if (count == 0) {
            return;
        }

        if (index >= gapStart) { // completely over gap
            if (index > gapStart) {
                moveGap(index);
            }

            // Allow GC of removed items
            index += gapLength; // begining of abandoned area
            for (int endIndex = index + count; index < endIndex; index++) {
                if (removeUpdater != null) {
                    removeUpdater.removeUpdate(array[index]);
                }
                array[index] = null;
            }

        } else { // completely below gap or spans the gap
            int endIndex = index + count;
            if (endIndex <= gapStart) {
                if (endIndex < gapStart) {
                    moveGap(endIndex);
                }
                gapStart = index;

            } else { // spans gap: gapStart > index but gapStart - index < count
                // Allow GC of removed items
                for (int clearIndex = index; clearIndex < gapStart; clearIndex++) {
                    if (removeUpdater != null) {
                        removeUpdater.removeUpdate(array[clearIndex]);
                    }
                    array[clearIndex] = null;
                }
                
                index = gapStart + gapLength; // part above the gap
                gapStart = endIndex - count; // original value of index
                endIndex += gapLength;

            }

            // Allow GC of removed items
            while (index < endIndex) {
                if (removeUpdater != null) {
                    removeUpdater.removeUpdate(array[index]);
                }
                array[index++] = null;
            }
                
        }

        gapLength += count;
    }

    protected void unoptimizedRemove(int index, int count, RemoveUpdater removeUpdater) {
        rangeCheck(index, count);
        
        int endIndex = index + count;
        if (gapStart != endIndex) {
            moveGap(endIndex);
        }

        // Null the cleared items to allow possible GC of those objects
        for (int i = endIndex - 1; i >= index; i--) {
            if (removeUpdater != null) {
                removeUpdater.removeUpdate(array[i]);
            }
            array[i] = null;
        }

        gapStart = index;
    }

    public void compact() {
        setCapacity(getItemCount());
    }
    
    protected void movedAboveGapUpdate(Object[] array, int index, int count) {
    }
    
    protected void movedBelowGapUpdate(Object[] array, int index, int count) {
    }
    
    private void moveGap(int index) {
        if (index <= gapStart) { // move gap down
            int moveSize = gapStart - index;
            System.arraycopy(array, index, array,
                gapStart + gapLength - moveSize, moveSize);
            clearEmpty(index, Math.min(moveSize, gapLength));
            gapStart = index;
            movedAboveGapUpdate(array, gapStart + gapLength, moveSize);

        } else { // above gap
            int gapEnd = gapStart + gapLength;
            int moveSize = index - gapStart;
            System.arraycopy(array, gapEnd, array, gapStart, moveSize);
            if (index < gapEnd) {
                clearEmpty(gapEnd, moveSize);
            } else {
                clearEmpty(index, gapLength);
            }
            movedBelowGapUpdate(array, gapStart, moveSize);
            gapStart += moveSize;
        }
    }
    
    private void clearEmpty(int index, int length) {
        while (--length >= 0) {
            array[index++] = null; // allow GC
        }
    }
    
    private void enlargeGap(int extraLength) {
        int newLength = Math.max(4,
            Math.max(array.length * 2, array.length + extraLength));
        setCapacity(newLength);
    }
    
    private void setCapacity(int newCapacity) {
        int gapEnd = gapStart + gapLength;
        int afterGapLength = (array.length - gapEnd);
        int newGapEnd = newCapacity - afterGapLength;
        Object[] newArray = new Object[newCapacity];
        if (newCapacity < gapStart + afterGapLength) { // to small capacity
            throw new IllegalArgumentException("newCapacity=" + newCapacity // NOI18N
                + " < itemCount=" + (gapStart + afterGapLength)); // NOI18N
        }

        System.arraycopy(array, 0, newArray, 0, gapStart);
        System.arraycopy(array, gapEnd, newArray, newGapEnd, afterGapLength);
        array = newArray;
        gapLength = newGapEnd - gapStart;
    }

    private void rangeCheck(int index, int count) {
        if (index < 0 || count < 0 || index + count > getItemCount()) {
            throw new IndexOutOfBoundsException("index=" + index // NOI18N
                + ", count=" + count + ", getItemCount()=" + getItemCount()); // NOI18N
        }
    } 

    private void indexCheck(int index) {
        if (index > getItemCount()) {
            throw new IndexOutOfBoundsException("index=" + index // NOI18N
                + ", getItemCount()=" + getItemCount()); // NOI18N
        }
    }

    /**
     * Internal consistency check.
     */
    void check() {
        if (gapStart < 0 || gapLength < 0
            || gapStart + gapLength > array.length
        ) {
            throw new IllegalStateException();
        }
        
        // Check whether the whole gap contains only nulls
        for (int i = gapStart + gapLength - 1; i >= gapStart; i--) {
            if (array[i] != null) {
                throw new IllegalStateException();
            }
        }
    }
    
    public String toStringDetail() {
        return "gapStart=" + gapStart + ", gapLength=" + gapLength // NOI18N
            + ", array.length=" + array.length; // NOI18N
    }
        
    /**
     * Updater of the removed items after the given item was removed
     * from the array.
     */   
    public interface RemoveUpdater {
        
        /**
         * Update the item after it was removed
         * from the array.
         */
        public void removeUpdate(Object removedItem);
        
    }

}
