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

import java.util.Collection;
import org.netbeans.lib.editor.util.GapList;

/**
 * The <code>OffsetGapList</code> is an extension of the gap list storing elements
 * containing an integer offset.
 * 
 * <p>In order to perform well the implementation of this list stores its elements
 * in a sequential order sorted by their offset. This fact affects behavior of some
 * methods you might be familiar with from the <code>List</code> interface.
 * 
 * <p>The list updates offsets of elements that it contains according to changes
 * notified by calling <code>defaultInsertUpdate</code> and <code>defaultRemoveUpdate</code>
 * methods. Usually this list is used for storing offsets in a <code>java.swing.text.Document</code>
 * and once an element with an offset is stored in the list the element's offset
 * is automatically updated when those two methods are called. It is the responsibility
 * of a user of this list to call the methods.
 * 
 * <p>Updating offsets of many elements could be a time consuming task. In order
 * to minimize the overhead the list maintains so called <i>offset gap</i>. The
 * offset gap is a gap at the position of the last insert with the size of 
 * <code>Integer.MAX_VALUE / 2</code>. When an element is added to the list its
 * offset gets adjusted and becomes so called <i>raw offset</i>. The relation
 * between offsets and raw offsets is following:
 * 
 * <ul>
 * <li>offset &lt; offsetGapStart : rawOffset = offset</li>
 * <li>offset &gt; offsetGapStart : rawOffset = offset + offsetGapLength
 * </ul>
 * 
 * The equivalent formulas give a recipe for computing an offset from rawOffset:
 * 
 * <ul>
 * <li>rawOffset &lt; offsetGapStart : offset = rawOffset</li>
 * <li>rawOffset &gt; offsetGapStart : offset = rawOffset - offsetGapLength
 * </ul>
 * 
 * When characteres are inserted at the position of the gap start
 * the gap simply gets shrinked and no offset have to be recomputed. Also, when
 * characters are removed just in front of the gap start the gap gets extended
 * and again no offset have to be recomputed. Offsets only have to be recomputed
 * when an insertion/removal happens not at the begginig of the gap. In that case
 * the gap has to be moved and offsets of any element, which is moved from 'behind'
 * the gap to a positoin in front of the gap, have to be recalculated. This algorithm
 * minimizes the number of recalculations needed for updating offsets when editing
 * a document under normal circumstances.
 *
 * <p>The maximum gap size is +1GB (<code>Integer.MAX_VALUE / 2</code>), which 
 * should be enough for most of the cases. The gap size can't be bigger, because
 * raw offsets of elements behind the gap (i.e. with offset > offsetGapStart)
 * would exceed the maximum value of <code>Integer</code> and became negative
 * (i.e. less the zero). If that happend the offsets comparision would get broken.
 * 
 * <p>This class also supports negative offsets up to a limit of -2GB.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class AbstractOffsetGapList<E> extends GapList<E> {
    
    private int offsetGapStart; // 28 bytes (24-super + 4)

    private int offsetGapLength = Integer.MAX_VALUE / 2; // 32 bytes

    private final boolean fixedZeroOffset;
    
    /**
     * Creates new <code>AbstractOffsetGapList</code> with <code>fixedZeroOffset = false</code>.
     */
    public AbstractOffsetGapList() {
        this(false);
    }

    /**
     * Creates new <code>AbstractOffsetGapList</code>.
     * 
     * @param fixedZeroOffset If <code>true</code> the offset 0 will
     *   always stay 0, otherwise it will move as any other offset
     *   when inserting in front of it (ie. at offsets less then or equal to 0).
     *   If <code>true</code> the behavior is the same as handling position 0
     *   in swing <code>Document</code>s.
     */
    public AbstractOffsetGapList(boolean fixedZeroOffset) {
        this.fixedZeroOffset = fixedZeroOffset;
    }
    
    /**
     * Adds an <code>element</code> to this list. The <code>element</code> is
     * going to be added to at the position, which is appropriate for the
     * <code>element</code>'s offset.
     * 
     * @param  element    The element to add.
     * @return Always <code>true</code>.
     */
    public final boolean add(E element) {
        int originalOffset = attachElement(element);
        setElementRawOffset(element, offset2raw(originalOffset));
        int index = findOffsetIndex(originalOffset);
        super.add(index, element);
        return true;
    }

    /**
     * Adds an <code>element</code> to this list at the given position. If the
     * <code>index</code> is not an appropriate position for the <code>element</code>
     * to be added at this method will throw an exception.
     * 
     * @param  element    The element to add.
     * @throws IndexOutOfBoundException If the <code>index</code> is not greater
     *         or equal to zero and less or equal to the size of the list.
     * @throws IllegalStateException If adding the <code>element</code> at the
     *         the <code>index</code> position would break sorting of the list.
     */
    public final void add(int index, E element) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException("index = " + index + " size = " + size()); //NOI18N
        }
        
        int originalOffset = attachElement(element);
        setElementRawOffset(element, offset2raw(originalOffset));

        boolean ok = true;
        
        // check offset of the element at (index - 1)
        if (index > 0 && elementOffset(index - 1) > originalOffset) {
            System.out.println("[" + (index - 1) + "] = " + elementOffset(index - 1) + " > {" + index + "} = "+ originalOffset);
            ok = false;
        }

        // check offset of the element at index
        if (index < size() && elementOffset(index) < originalOffset) {
            System.out.println("[" + index + "] = " + elementOffset(index) + " < {" + index + "} = "+ originalOffset);
            ok = false;
        }
        
        if (ok) {
            // the index is valid for the element
            super.add(index, element);
        } else {
            // can't insert the element at this index, the list must remain sorted
            detachElement(element);
            throw new IllegalStateException("Can't insert element at index: " + index); //NOI18N
        }
    }

    /**
     * Adds all elements from a collection. Calling this method is equivalent to
     * calling <code>add(E)</code> method for each element in the collection <code>c<code>.
     * 
     * @return Always <code>true</code>.
     */
    public final boolean addAll(Collection<? extends E> c) {
        for (E e : c) {
            add(e);
        }
        return true;
    }

    /**
     * This operation is not supported by this list.
     * 
     * @throws UnsupportedOperationException 
     */
    public final boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException("This is an illegal operation on OffsetGapList."); //NOI18N
    }

    /**
     * This operation is not supported by this list.
     * 
     * @throws UnsupportedOperationException
     */
    public final boolean addArray(int index, Object[] elements) {
        throw new UnsupportedOperationException("This is an illegal operation on OffsetGapList."); //NOI18N
    }

    /**
     * This operation is not supported by this list.
     * 
     * @throws UnsupportedOperationException
     */
    public final boolean addArray(int index, Object[] elements, int off, int len) {
        throw new UnsupportedOperationException("This is an illegal operation on OffsetGapList."); //NOI18N
    }

    /**
     * Finds the position of the <code>element</code> in this list.
     * 
     * @return The index of the <code>element</code> or -1 if the element can't
     *         be find.
     */
    public final int indexOf(Object o) {
        E element = getAttachedElement(o);
        if (element != null) {
            int offset = elementOffset(element);
            int idx = findElementIndex(offset);
            if (idx >= 0) {
                for (int i = idx; i < size() && elementOffset(i) == offset; i++) {
                    if (element == get(i)) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * The same as the <code>indexOf(E)</code> method. The <code>OffsetGapList</code>
     * does not allow adding one element twice.
     * 
     * @return The index of the <code>element</code> or -1 if the element can't
     *         be find.
     */
    public final int lastIndexOf(Object element) {
        return indexOf(element);
    }

    /**
     * Replaces an element of this list at the given position with a new element.
     * If the new <code>element</code> can't be placed at the <code>index</code>
     * position this method will throw an exception.
     * 
     * @param  index      The position to add the element at.
     * @param  element    The element to add.
     * 
     * @return The old element, which was originally stored at the <code>index</code>
     *         position.
     */
    @Override
    public final E set(int index, E element) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("index = " + index + " size = " + size()); //NOI18N
        }
        
        int originalOffset = attachElement(element);
        setElementRawOffset(element, offset2raw(originalOffset));
        
        // check offset of the element at (index - 1)
        if (index > 0 && elementOffset(index - 1) > originalOffset) {
            // can't insert the element at this index, the list must remain sorted
            String log = "[" + (index - 1) + "] = " + elementOffset(index - 1) + " > {" + index + "} = "+ originalOffset;
            detachElement(element);
            throw new IllegalStateException("Can't insert element at index: " + index + log); //NOI18N
        }

        // check offset of the element at (index + 1)
        if (index + 1 < size() && elementOffset(index + 1) < originalOffset) {
            // can't insert the element at this index, the list must remain sorted
            String log = "[" + (index + 1) + "] = " + elementOffset(index + 1) + " < {" + index + "} = "+ originalOffset;
            detachElement(element);
            throw new IllegalStateException("Can't insert element at index: " + index + log); //NOI18N
        }
        
        // the index is valid for the element
        E oldElement = super.set(index, element);
        detachElement(oldElement);
        return oldElement;
    }

    /**
     * This operation is not supported by this list.
     * 
     * @throws UnsupportedOperationException
     */
    public final void swap(int index1, int index2) {
        throw new UnsupportedOperationException("This is an illegal operation on OffsetGapList."); //NOI18N
    }
    
    /**
     * Gets the raw offset of the given element.
     *
     * @param elem The element to get the raw offset for.
     * @return The raw offset of the element.
     */
    protected abstract int elementRawOffset(E elem);

    /**
     * Sets the raw offset of the given element currently stored in the list.
     *
     * @param elem element currently stored in the list.
     * @param rawOffset raw offset to be stored in the given element.
     */
    protected abstract void setElementRawOffset(E elem, int rawOffset);

    /**
     * Gets called when an element is being added to the list. This is the first
     * method that will be called when an element is added to the list. The method
     * should return the original offset of the element. This offset will be
     * used for calculating element's raw offset and setting it by calling
     * the <code>setElementRawOffset</code>. Since that time the elemnt should
     * only keep its raw offset and use the <code>raw2offset</code> method for
     * translating it to the real offset again.
     * 
     * @param elem The element being added to the list.
     * @return The original offset of the element (i.e. offset at the time when
     *         the element is added to this list).
     */
    protected abstract int attachElement(E elem);

    /**
     * Gets called when an element is removed from this list. After this method
     * is called the element should never try to use <code>raw2offset</code> to
     * get its real offset. The element is removed from the list and its raw offset
     * is no longer updated by the list and therefore can't be used for computing
     * the real offset.
     * 
     * @param elem The element to detach from the list.
     */
    protected abstract void detachElement(E elem);
    
    /**
     * Recognizes an object as an element of this list. If the object passed in
     * can be recognized as an element attached to this list the method should
     * return its type-casted version. If the object is not of a right type or
     * does not belong to this list the method should return <code>null</code>.
     * 
     * @param o The object to recognize.
     * @return The type-casted version of the object (i.e. an element of this list)
     *         or <code>null</code> if the element does not belong to the list.
     */
    protected abstract E getAttachedElement(Object o);
    
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
            moveOffsetGap(offset, findOffsetIndex(offset));
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
        int index = findOffsetIndex(offset);
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
            int rawOffset = elementRawOffset(elem);
            if (rawOffset < removeAreaEndRawOffset) {
                if (!fixedZeroOffset || rawOffset != 0) {
                    setElementRawOffset(elem, removeAreaEndRawOffset);
                }
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
     *  that has an offset that is greater than or equal to the given offset parameter.
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
                    if (!fixedZeroOffset || rawOffset != 0) {
                        setElementRawOffset(elem, rawOffset + offsetGapLength);
                    }
                } else {
                    break;
                }
            }

        } else {  // check items below index
            for (int i = index - 1; i >= 0; i--) {
                E elem = get(i);
                int rawOffset = elementRawOffset(elem);
                if (rawOffset >= offsetGapStart) {
                    if (!fixedZeroOffset || rawOffset != 0) {
                        setElementRawOffset(elem, rawOffset - offsetGapLength);
                    }
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
    protected final int findOffsetIndex(int offset) {
        int index = findElementIndex(offset);
        return index < 0 ? -index - 1 : index;
    }

    /**
     * Finds an index of the first element at the given offset in the list
     * by using binary search.
     *
     * @param  offset  The offset of the element to find.
     * @param  lowIdx  The lowest index to look at.
     * @param  highIdx The highest index to look at.
     * 
     * @return index of the element with the given <code>offset</code>,
     *         if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which an
     *	       element with the <code>offset</code> would be inserted into the list:
     *         the index of the first
     *	       element with its offset greater than the <code>offset</code> prameter,
     *         or <tt>list.size()</tt>, if all
     *	       elements in the list have offsets less than the specified <code>offset</code>.
     *         Note that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the element with the given <code>offset</code> is found.
     */
    public final int findElementIndex(int offset, int lowIdx, int highIdx) {
        if (lowIdx < 0 || highIdx > size() - 1) {
            throw new IndexOutOfBoundsException("lowIdx = " + lowIdx + ", highIdx = " + highIdx + ", size = " + size());
        }
        
        int low = lowIdx;
        int high = highIdx;

        while (low <= high) {
            int index = (low + high) >> 1; // mid in the binary search
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
                return index;
            }
        }
        
        return -(low + 1);
    }

    public final int findElementIndex(int offset) {
        return findElementIndex(offset, 0, size() - 1);
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
     * <br>
     * This method should be called after the element is physically removed
     * from the list and it's desired that it retains its natural offset
     * (not possibly shifted by the offset gap length).
     * <br>
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
        if (fixedZeroOffset && rawOffset == 0) {
            return 0;
        } else {
            return (rawOffset < offsetGapStart)
                ? rawOffset
                : rawOffset - offsetGapLength;
        }
    }
    
    /**
     * Translate regular offset to raw offset.
     *
     * @param offset regular offset.
     * @return raw offset that can be used in elements.
     */
    protected final int offset2raw(int offset) {
        if (fixedZeroOffset && offset == 0) {
            return 0;
        } else {
            return (offset < offsetGapStart)
                ? offset
                : offset + offsetGapLength;
        }
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
