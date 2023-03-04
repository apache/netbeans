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

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;

/**
 * List implementation that stores items in an array
 * with a gap.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class GapList<E> extends AbstractList<E>
implements List<E>, RandomAccess, Cloneable, java.io.Serializable {

    /**
     * The array buffer into which the elements are stored.
     * <br>
     * The elements are stored in the whole array except
     * the indexes starting at <code>gapStart</code>
     * till <code>gapStart + gapLength - 1</code>.
     */
    private transient E[] elementData; // 16 bytes (12-super(modCount) + 4)
    
    /**
     * The start of the gap in the elementData array.
     */
    private int gapStart; // 20 bytes
    
    /**
     * Length of the gap in the elementData array starting at gapStart.
     */
    private int gapLength; // 24 bytes
    
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param   initialCapacity   the initial capacity of the list.
     * @exception IllegalArgumentException if the specified initial capacity
     *            is negative
     */
    public GapList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " // NOI18N
                + initialCapacity);
        }
        this.elementData = allocateElementsArray(initialCapacity);
        this.gapLength = initialCapacity;
    }
    
    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public GapList() {
        this(10);
    }
    
    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.  The <tt>GapList</tt> instance has an initial capacity of
     * 110% the size of the specified collection.
     *
     * @param c the collection whose elements are to be placed into this list.
     * @throws NullPointerException if the specified collection is null.
     */
    public GapList(Collection<? extends E> c) {
        int size = c.size();
        // Allow 10% room for growth
        int capacity = (int)Math.min((size*110L)/100,Integer.MAX_VALUE);
        @SuppressWarnings("unchecked")
        E[] data = (E[])c.toArray(new Object[capacity]);
        elementData = data;
        this.gapStart = size;
        this.gapLength = elementData.length - size;
    }
    
    private GapList(E[] data, int gapStart, int gapLength) {
        this.elementData = data;
        this.gapStart = gapStart;
        this.gapLength = gapLength;
    }
    
    /**
     * Trims the capacity of this <tt>GapList</tt> instance to be the
     * list's current size.  An application can use this operation to minimize
     * the storage of an <tt>GapList</tt> instance.
     */
    public void trimToSize() {
        modCount++;
        if (gapLength > 0) {
            int newLength = elementData.length - gapLength;
            E[] newElementData = allocateElementsArray(newLength);
            copyAllData(newElementData);
            elementData = newElementData;
            // Leave gapStart as is
            gapLength = 0;
        }
    }
    
    /**
     * Increases the capacity of this <tt>GapList</tt> instance, if
     * necessary, to ensure  that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity.
     */
    public void ensureCapacity(int minCapacity) {
        modCount++; // expected to always increment modCount (same in ArrayList)
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            int gapEnd = gapStart + gapLength;
            int afterGapLength = (oldCapacity - gapEnd);
            int newGapEnd = newCapacity - afterGapLength;
            E[] newElementData = allocateElementsArray(newCapacity);
            System.arraycopy(elementData, 0, newElementData, 0, gapStart);
            System.arraycopy(elementData, gapEnd, newElementData, newGapEnd, afterGapLength);
            elementData = newElementData;
            gapLength = newGapEnd - gapStart;
        }
    }
    
    /**
     * Returns the number of elements in this list.
     *
     * @return  the number of elements in this list.
     */
    public int size() {
        return elementData.length - gapLength;
    }
    
    /**
     * Tests if this list has no elements.
     *
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
        return (elementData.length == gapLength);
    }
    
    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param elem element whose presence in this List is to be tested.
     * @return  <code>true</code> if the specified element is present;
     *		<code>false</code> otherwise.
     */
    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }
    
    /**
     * Searches for the first occurence of the given argument, testing
     * for equality using the <tt>equals</tt> method.
     *
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     *          list; returns <tt>-1</tt> if the object is not found.
     * @see     Object#equals(Object)
     */
    public int indexOf(Object elem) {
        if (elem == null) {
            int i = 0;
            while (i < gapStart) {
                if (elementData[i] == null) {
                    return i;
                }
                i++;
            }
            i += gapLength;
            int elementDataLength = elementData.length;
            while (i < elementDataLength) {
                if (elementData[i] == null) {
                    return i - gapLength;
                }
                i++;
            }
            
        } else { // elem not null
            int i = 0;
            while (i < gapStart) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
                i++;
            }
            i += gapLength;
            int elementDataLength = elementData.length;
            while (i < elementDataLength) {
                if (elem.equals(elementData[i])) {
                    return i - gapLength;
                }
                i++;
            }
        }
        
        return -1;
    }
    
    /**
     * Returns the index of the last occurrence of the specified object in
     * this list.
     *
     * @param   elem   the desired element.
     * @return  the index of the last occurrence of the specified object in
     *          this list; returns -1 if the object is not found.
     */
    public int lastIndexOf(Object elem) {
        if (elem == null) {
            int i = elementData.length - 1;
            int gapEnd = gapStart + gapLength;
            while (i >= gapEnd) {
                if (elementData[i] == null) {
                    return i - gapLength;
                }
                i--;
            }
            i -= gapLength;
            while (i >= 0) {
                if (elementData[i] == null) {
                    return i;
                }
                i--;
            }
            
        } else { // elem not null
            int i = elementData.length - 1;
            int gapEnd = gapStart + gapLength;
            while (i >= gapEnd) {
                if (elem.equals(elementData[i])) {
                    return i - gapLength;
                }
                i--;
            }
            i -= gapLength;
            while (i >= 0) {
                if (elem.equals(elementData[i])) {
                    return i;
                }
                i--;
            }
        }
        
        return -1;
    }
    
    /**
     * Returns a shallow copy of this <tt>GapList</tt> instance.  (The
     * elements themselves are not copied.)
     *
     * @return  a clone of this <tt>GapList</tt> instance.
     */
    public Object clone() {
        try {
            @SuppressWarnings("unchecked")
            GapList<E> clonedList = (GapList<E>)super.clone();
            int size = size();
            E[] clonedElementData = allocateElementsArray(size);
            copyAllData(clonedElementData);
            clonedList.elementData = clonedElementData;
            // Will retain gapStart - would have to call moved*() otherwise
            clonedList.gapStart = size;
            clonedList.resetModCount();
            return clonedList;

        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
    
    /**
     * Create shallow copy of this gap list.
     * @return copy of this gap list with zero extra capacity.
     * @since 1.63
     */
    public GapList<E> copy() {
        int size = size();
        E[] data = allocateElementsArray(size);
        copyAllData(data);
        return new GapList<E>(data, size, 0);
    }
    
    /**
     * @deprecated use {@link #copyElements(int, int, Object[], int)} which performs the same operation
     */
    @Deprecated
    public void copyItems(int startIndex, int endIndex,
    Object[] dest, int destIndex) {
        copyElements(startIndex, endIndex, dest, destIndex);
    }

    /**
     * Copy elements of this list between the given index range to the given object array.
     *
     * @param startIndex start index of the region of this list to be copied.
     * @param endIndex end index of the region of this list to be copied.
     * @param dest collection to the end of which the items should be copied.
     */
    public void copyElements(int startIndex, int endIndex,
    Object[] dest, int destIndex) {
        
        if (startIndex < 0 || endIndex < startIndex || endIndex > size()) {
            throw new IndexOutOfBoundsException("startIndex=" + startIndex // NOI18N
            + ", endIndex=" + endIndex + ", size()=" + size()); // NOI18N
        }
        
        if (endIndex < gapStart) { // fully below gap
            System.arraycopy(elementData, startIndex,
            dest, destIndex, endIndex - startIndex);
            
        } else { // above gap or spans the gap
            if (startIndex >= gapStart) { // fully above gap
                System.arraycopy(elementData, startIndex + gapLength, dest, destIndex,
                endIndex - startIndex);
                
            } else { // spans gap
                int beforeGap = gapStart - startIndex;
                System.arraycopy(elementData, startIndex, dest, destIndex, beforeGap);
                System.arraycopy(elementData, gapStart + gapLength, dest, destIndex + beforeGap,
                endIndex - startIndex - beforeGap);
            }
        }
    }
    
    /**
     * Copy elements of this list between the given index range
     * to the end of the given collection.
     *
     * @param startIndex start index of the region of this list to be copied.
     * @param endIndex end index of the region of this list to be copied.
     * @param dest collection to the end of which the items should be copied.
     */
    public void copyElements(int startIndex, int endIndex, Collection<E> dest) {
        
        if (startIndex < 0 || endIndex < startIndex || endIndex > size()) {
            throw new IndexOutOfBoundsException("startIndex=" + startIndex // NOI18N
            + ", endIndex=" + endIndex + ", size()=" + size()); // NOI18N
        }
        
        if (endIndex < gapStart) { // fully below gap
            while (startIndex < endIndex) {
                dest.add(elementData[startIndex++]);
            }
            
        } else { // above gap or spans the gap
            if (startIndex >= gapStart) { // fully above gap
                startIndex += gapLength;
                endIndex += gapLength;
                while (startIndex < endIndex) {
                    dest.add(elementData[startIndex++]);
                }
                
            } else { // spans gap
                while (startIndex < gapStart) {
                    dest.add(elementData[startIndex++]);
                }
                startIndex += gapLength;
                endIndex += gapLength;
                while (startIndex < endIndex) {
                    dest.add(elementData[startIndex++]);
                }
            }
        }
    }
    
    /**
     * Returns an array containing all of the elements in this list
     * in the correct order.
     *
     * @return an array containing all of the elements in this list
     * 	       in the correct order.
     */
    public Object[] toArray() {
        int size = size();
        Object[] result = new Object[size];
        copyAllData(result);
        return result;
    }
    
    /**
     * Returns an array containing all of the elements in this list in the
     * correct order; the runtime type of the returned array is that of the
     * specified array.  If the list fits in the specified array, it is
     * returned therein.  Otherwise, a new array is allocated with the runtime
     * type of the specified array and the size of this list.<p>
     *
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the collection is set to
     * <tt>null</tt>.  This is useful in determining the length of the list
     * <i>only</i> if the caller knows that the list does not contain any
     * <tt>null</tt> elements.
     *
     * @param a the array into which the elements of the list are to
     *		be stored, if it is big enough; otherwise, a new array of the
     * 		same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list.
     * @throws ArrayStoreException if the runtime type of a is not a supertype
     *         of the runtime type of every element in this list.
     */
    public <T> T[] toArray(T[] a) {
        int size = size();
        if (a.length < size) {
            @SuppressWarnings("unchecked")
            T[] tmp = (T[])java.lang.reflect.Array.newInstance(
                a.getClass().getComponentType(), size);
            a = tmp;
        }
        copyAllData(a);
        if (a.length > size)
            a[size] = null;
        
        return a;
    }
    
    // Positional Access Operations
    
    /**
     * Returns the element at the specified position in this list.
     *
     * @param  index index of element to return.
     * @return the element at the specified position in this list.
     * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public E get(int index) {
        // rangeCheck(index) not necessary - would fail with AIOOBE anyway
        return elementData[(index < gapStart) ? index : (index + gapLength)];
    }
    
    /**
     * Replaces the element at the specified position in this list with
     * the specified element.
     *
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     * @throws    IndexOutOfBoundsException if index out of range
     *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
     */
    public E set(int index, E element) {
        // rangeCheck(index) not necessary - would fail with AIOOBE anyway
        if (index >= gapStart) {
            index += gapLength;
        }
        E oldValue = elementData[index];
        elementData[index] = element;
        return oldValue;
    }
    
    /**
     * Swap elements at the given indexes.
     */
    public void swap(int index1, int index2) {
        // rangeCheck(index) not necessary - would fail with AIOOBE anyway
        // rangeCheck(byIndex) not necessary - would fail with AIOOBE anyway
        if (index1 >= gapStart) {
            index1 += gapLength;
        }
        if (index2 >= gapStart) {
            index2 += gapLength;
        }
        E tmpValue = elementData[index1];
        elementData[index1] = elementData[index2];
        elementData[index2] = tmpValue;
    }
    
    /**
     * Appends the specified element to the end of this list.
     *
     * @param element non-null element to be appended to this list.
     * @return <tt>true</tt> (as per the general contract of Collection.add).
     */
    public boolean add(E element) {
        int size = size();
        ensureCapacity(size + 1); // Increments modCount
        addImpl(size, element);
        return true;
    }
    
    /**
     * Inserts the specified element at the specified position in this
     * list. Shifts the element currently at that position (if any) and
     * any subsequent elements to the right (adds one to their indices).
     *
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     * @throws    IndexOutOfBoundsException if index is out of range
     *		  <tt>(index &lt; 0 || index &gt; size())</tt>.
     */
    public void add(int index, E element) {
        int size = size();
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size); // NOI18N
        }
        ensureCapacity(size + 1); // Increments modCount
        addImpl(index, element);
    }
    
    private void addImpl(int index, E element) {
        moveGap(index);
        elementData[gapStart++] = element;
        gapLength--;
    }
    
    /**
     * Appends all of the elements in the specified Collection to the end of
     * this list, in the order that they are returned by the
     * specified Collection's Iterator.  The behavior of this operation is
     * undefined if the specified Collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified Collection is this list, and this
     * list is nonempty.)
     *
     * @param c the elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    NullPointerException if the specified collection is null.
     */
    public boolean addAll(Collection<? extends E> c) {
        return addAll(size(), c);
    }
    
    /**
     * Appends elements from the specified collection to the end of
     * this list, in the order that they are returned by the
     * specified collection's iterator.  The behavior of this operation is
     * undefined if the specified Collection is modified while the operation
     * is in progress.  (This implies that the behavior of this call is
     * undefined if the specified Collection is this list, and this
     * list is nonempty.)
     *
     * @param c collection containing the elements to be inserted into this list.
     * @param off offset in the collection pointing to first element to copy.
     * @param len number of elements to copy from the collection.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    NullPointerException if the specified Collection is null.
     * @since 1.64
     */
    public boolean addAll(Collection<? extends E> c, int off, int len) {
        return addArray(size(), c.toArray(), off, len);
    }

    /**
     * Inserts all of the elements in the specified Collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified Collection's iterator.
     *
     * @param index index at which to insert first element
     *		    from the specified collection.
     * @param c elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     * @throws    NullPointerException if the specified Collection is null.
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        return addArray(index, c.toArray());
    }

    /**
     * Inserts elements in the specified Collection into this
     * list, starting at the specified position.  Shifts the element
     * currently at that position (if any) and any subsequent elements to
     * the right (increases their indices).  The new elements will appear
     * in the list in the order that they are returned by the
     * specified Collection's iterator.
     *
     * @param index index at which to insert first element
     *		    from the specified collection.
     * @param c collection containing the elements to be inserted into this list.
     * @param off offset in the collection pointing to first element to copy.
     * @param len number of elements to copy from the collection.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     *		  &lt; 0 || index &gt; size())</tt>.
     * @throws    NullPointerException if the specified Collection is null.
     * @since 1.64
     */
    public boolean addAll(int index, Collection<? extends E> c, int off, int len) {
        return addArray(index, c.toArray(), off, len);
    }

    /**
     * Inserts all elements of the given array at the end of this list.
     *
     * @param elements array of elements to insert.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @since 1.64
     */
    public boolean addArray(Object[] elements) {
        return addArray(size(), elements, 0, elements.length);
    }

    /**
     * Inserts all elements from the given array into this list, starting
     * at the given index.
     *
     * @param index index at which to insert first element from the array.
     * @param elements array of elements to insert.
     * @return <tt>true</tt> if this list changed as a result of the call.
     */
    public boolean addArray(int index, Object[] elements) {
        return addArray(index, elements, 0, elements.length);
    }

    /**
     * Inserts elements from the given array into this list, starting
     * at the given index.
     *
     * @param index index at which to insert first element.
     * @param elements array of elements from which to insert elements.
     * @param off offset in the elements pointing to first element to copy.
     * @param len number of elements to copy from the elements array.
     * @return <tt>true</tt> if this list changed as a result of the call.
     */
    public boolean addArray(int index, Object[] elements, int off, int len) {
        int size = size();
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: " + index + ", Size: " + size); // NOI18N
        }
        
        ensureCapacity(size + len);  // Increments modCount
        
        moveGap(index); // after that (index == gapStart)
        System.arraycopy(elements, off, elementData, index, len);
        gapStart += len;
        gapLength -= len;

        return (len != 0);
    }
    
    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns.
     */
    public void clear() {
        removeRange(0, size());
    }
    
    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).
     *
     * @param index the index of the element to removed.
     * @return the element that was removed from the list.
     * @throws    IndexOutOfBoundsException if index out of range <tt>(index
     * 		  &lt; 0 || index &gt;= size())</tt>.
     */
    public E remove(int index) {
        int size = size();
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException(
                "remove(): Index: " + index + ", Size: " + size); // NOI18N
        }

        modCount++;
        moveGap(index + 1); // if previous were adds() - this should be no-op
        E oldValue = elementData[index];
        elementData[index] = null;
        gapStart--;
        gapLength++;
        
        return oldValue;
    }
    
    /**
     * Removes elements at the given index.
     *
     * @param index index of the first element to be removed.
     * @param count number of elements to remove.
     */
    public void remove(int index, int count) {
        int toIndex = index + count;
        if (index < 0 || toIndex < index || toIndex > size()) {
            throw new IndexOutOfBoundsException("index=" + index // NOI18N
            + ", count=" + count + ", size()=" + size()); // NOI18N
        }
        removeRange(index, toIndex);
    }
    
    /**
     * Removes from this List all of the elements whose index is between
     * fromIndex, inclusive and toIndex, exclusive.  Shifts any succeeding
     * elements to the left (reduces their index).
     * This call shortens the list by <tt>(toIndex - fromIndex)</tt> elements.
     * (If <tt>toIndex==fromIndex</tt>, this operation has no effect.)
     *
     * @param fromIndex index of first element to be removed.
     * @param toIndex index after last element to be removed.
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        if (fromIndex == toIndex) {
            return;
        }
        
        int removeCount = toIndex - fromIndex;
        if (fromIndex >= gapStart) { // completely over gap
            // Move gap to the start of the removed area
            // (this should be the minimum necessary count of elements moved)
            moveGap(fromIndex);
            
            // Allow GC of removed items
            fromIndex += gapLength; // begining of abandoned area
            toIndex += gapLength;
            while (fromIndex < toIndex) {
                elementData[fromIndex] = null;
                fromIndex++;
            }
            
        } else { // completely below gap or spans the gap
            if (toIndex <= gapStart) {
                // Move gap to the end of the removed area
                // (this should be the minimum necessary count of elements moved)
                moveGap(toIndex);
                gapStart = fromIndex;
                
            } else { // spans gap: gapStart > fromIndex but gapStart - fromIndex < removeCount
                // Allow GC of removed items
                for (int clearIndex = fromIndex; clearIndex < gapStart; clearIndex++) {
                    elementData[clearIndex] = null;
                }
                
                fromIndex = gapStart + gapLength; // part above the gap
                gapStart = toIndex - removeCount; // original value of fromIndex
                toIndex += gapLength;
            }
            
            // Allow GC of removed items
            while (fromIndex < toIndex) {
                elementData[fromIndex++] = null;
            }
            
        }
        
        gapLength += removeCount;
    }
    
    private void moveGap(int index) {
        if (index == gapStart) {
            return; // do nothing
        }

        if (gapLength > 0) {
            if (index < gapStart) { // move gap down
                int moveSize = gapStart - index;
                System.arraycopy(elementData, index, elementData,
                    gapStart + gapLength - moveSize, moveSize);
                clearEmpty(index, Math.min(moveSize, gapLength));

            } else { // above gap
                int gapEnd = gapStart + gapLength;
                int moveSize = index - gapStart;
                System.arraycopy(elementData, gapEnd, elementData, gapStart, moveSize);
                if (index < gapEnd) {
                    clearEmpty(gapEnd, moveSize);
                } else {
                    clearEmpty(index, gapLength);
                }
            }
        }
        gapStart = index;
    }
    
    private void copyAllData(Object[] toArray) {
        if (gapLength != 0) {
            int gapEnd = gapStart + gapLength;
            System.arraycopy(elementData, 0, toArray, 0, gapStart);
            System.arraycopy(elementData, gapEnd, toArray, gapStart,
                elementData.length - gapEnd);
        } else { // no gap => single copy of everything
            System.arraycopy(elementData, 0, toArray, 0, elementData.length);
        }
    }
    
    private void clearEmpty(int index, int length) {
        while (--length >= 0) {
            elementData[index++] = null; // allow GC
        }
    }
    
    private void resetModCount() {
        modCount = 0;
    }
    
    /**
     * Save the state of the <tt>GapList</tt> instance to a stream (that
     * is, serialize it).
     *
     * @serialData The length of the array backing the <tt>GapList</tt>
     *             instance is emitted (int), followed by all of its elements
     *             (each an <tt>Object</tt>) in the proper order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
    throws java.io.IOException{
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();
        
        // Write out array length
        s.writeInt(elementData.length);
        
        // Write out all elements in the proper order.
        int i = 0;
        while (i < gapStart) {
            s.writeObject(elementData[i]);
            i++;
        }
        i += gapLength;
        int elementDataLength = elementData.length;
        while (i < elementDataLength) {
            s.writeObject(elementData[i]);
            i++;
        }
    }
    
    /**
     * Reconstitute the <tt>GapList</tt> instance from a stream (that is,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
    throws java.io.IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();
        
        // Read in array length and allocate array
        int arrayLength = s.readInt();
        elementData = allocateElementsArray(arrayLength);
        
        // Read in all elements in the proper order.
        int i = 0;
        while (i < gapStart) {
            @SuppressWarnings("unchecked")
            E e = (E)s.readObject();
            elementData[i] = e;
            i++;
        }
        i += gapLength;
        int elementDataLength = elementData.length;
        while (i < elementDataLength) {
            @SuppressWarnings("unchecked")
            E e = (E)s.readObject();
            elementData[i] = e;
            i++;
        }
    }
    
    /**
     * Internal consistency check.
     */
    protected void consistencyCheck() {
        if (gapStart < 0 || gapLength < 0
            || gapStart + gapLength > elementData.length
        ) {
            consistencyError("Inconsistent gap"); // NOI18N
        }
        
        // Check whether the whole gap contains only nulls
        for (int i = gapStart + gapLength - 1; i >= gapStart; i--) {
            if (elementData[i] != null) {
                consistencyError("Non-null value at raw-index i"); // NOI18N
            }
        }
    }
    
    protected final void consistencyError(String s) {
        throw new IllegalStateException(s + ": " + dumpDetails()); // NOI18N
    }
    
    protected String dumpDetails() {
        return dumpInternals() + "; DATA:\n" + toString(); // NOI18N
    }
    
    protected String dumpInternals() {
        return "elems: " + size() + '(' + elementData.length // NOI18N
            + "), gap(s=" + gapStart + ", l=" + gapLength + ')';// NOI18N
    }
    
    @SuppressWarnings("unchecked")
    private E[] allocateElementsArray(int capacity) {
        return (E[])new Object[capacity];
    }
    
    public String toString() {
        return dumpElements(this);
    }

    public static String dumpElements(java.util.List l) {
        StringBuffer sb = new StringBuffer();
        int size = l.size();
        int digitCount = String.valueOf(size - 1).length();
        for (int i = 0; i < size; i++) {
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            sb.append(l.get(i));
            sb.append("\n"); // NOI18N
        }
        return sb.toString();
    }
    
}
