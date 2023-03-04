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

package org.netbeans.modules.versioning.util;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Collection if utility method for working with {@code Collection}s and arrays.
 *
 * @author Marian Petras
 * @since 1.9.1
 */
public final class CollectionUtils {

    private CollectionUtils() {}

    /**
     * Finds index of the first occurence of an item in the given array.
     * The items are compared using operator {@code ==}, method {@code equals()}
     * is not used. It is legal to search for {@code null}.
     *
     * @param  itemToFind  object to search for
     * @param  array  array of objects to be searched
     * @return  index of the first occurence of the given item in the array,
     *          or {@code -1} if not found
     * @exception  java.lang.IllegalArgumentException
     *             if the given array is {@code null}
     * @since 1.9.1
     */
    public static <T,U extends T> int findInArray(T[] array, U itemToFind) {
        if (array == null) {
            throw new IllegalArgumentException("null array");           //NOI18N
        }

        for (int i = 0; i < array.length; i++) {
            if (array[i] == itemToFind) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Appends an item to an array.
     *
     * @param  array  array to append an item to
     * @param  itemToAppend  item to be appended (may be {@code null})
     * @return  new array containing the same items, in the same order
     *          as the passed array, just one item longer, the given item
     *          being the last item
     * @exception  java.lang.NullPointerException
     *             if the passed array was {@code null}
     * @since 1.9.1
     */
    public static <T> T[] appendItem(T[] array, T itemToAppend) {
        if (array == null) {
            throw new IllegalArgumentException("null array");           //NOI18N
        }

        T[] result = makeArray(array, array.length + 1);
        if (array.length != 0) {
            System.arraycopy(array, 0, result, 0, array.length);
        }
        result[array.length] = itemToAppend;
        return result;
    }

    /**
     * Removes a first occurence of the given item from an array, if present.
     * The items are compared using operator {@code ==}, method {@code equals()}
     * is not used. It is legal to ask for removal of item {@code null}.
     *
     * @param  array  array to remove an item from
     * @param  index  item to be removed
     * @return  the passed array if the given item has not been found in the
     *          array, or a new array containing the same items as in the passed
     *          array, in the same order, just with the given item missing
     */
    public static <T,U extends T> T[] removeItem(T[] array, U itemToRemove) {
        if (array == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        int index = findInArray(array, itemToRemove);
        return (index == -1) ? array : removeItem(array, index);
    }

    /**
     * Removes a given item from an array.
     * @param  array  array to remove an item from
     * @param  index  index to the item to be removed
     * @return  new array containing the same items as in the passed array,
     *          in the same order, just with the given item missing
     */
    public static <T> T[] removeItem(T[] array, int index) {
        if (array == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        if (index < 0) {
            throw new IllegalArgumentException(
                    "negative index: " + index);                        //NOI18N
        }
        if (index >= array.length) {
            throw new IllegalArgumentException(
                    "index out of bounds (array length: " + array.length//NOI18N
                    + ", index: " + index + ')');                       //NOI18N
        }

        T[] result = makeArray(array, array.length - 1);
        if (index != 0) {
            System.arraycopy(array, 0, result, 0, index);
        }
        if (index != array.length - 1) {
            System.arraycopy(array, index + 1, result, index,
                             array.length - index - 1);
        }
        return result;
    }

    /**
     * Returns array containing set of unique elements from the given array.
     * I.e. the returned array will not contain any pair of non-null
     * items <em>a</em>, <em>b</em> such that {@code a.equals(b)}.
     * If the passed array contains one ore more {@code null} elements,
     * the returned array will contain a single {@code null} element.
     * <p>
     * The returned array may be the same instance as the passed array
     * if the passed array did not contain any duplicate items.
     * <p>
     * This method is only suitable for small arrays because it takes time
     * O(<em>n</em>^2) where <em>n</em> is the number of elements in the array.
     *
     * @param  array  array to remove trailing duplicates elements from
     * @return  array being a copy of the passed array with duplicate items
     *          missing
     * @exception  java.lang.NullPointerException
     *             if the passed array was {@code null}
     * @since 1.9.1
     */
    public static <T> T[] removeDuplicates(T[] array) {
        if (array == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        if (array.length < 2) {
            return array;
        }

        boolean nullItemFound = false;

        int mainTopBound = array.length - 1;
        int i;

        for (i = 0; i < mainTopBound; i++) {
            T item = array[i];
            boolean isDuplicate = false;
            if (item == null) {
                nullItemFound = true;
                for (int j = i + 1; j < array.length; j++) {
                    if (array[j] == null) {
                        isDuplicate = true;
                        break;
                    }
                }
            } else {
                for (int j = i + 1; j < array.length; j++) {
                    if (equal(item, array[j])) {
                        isDuplicate = true;
                        array[j] = null;    //clear item known to be a duplicate
                    }
                }
            }
            if (isDuplicate) {
                break;
            }
        }

        if (i == mainTopBound) {
            return array;       //there were no duplicates in the array
        }

        T[] proResult = (T[]) Array.newInstance(
                                    array.getClass().getComponentType(),
                                    array.length - 1);
        int count = i;      //number of unique items known so far
        if (count != 0) {
            System.arraycopy(array, 0, proResult, 0, count);
        }

        for (i = i + 1; i < mainTopBound; i++) {
            T item = array[i];
            if (item == null) {
                if (!nullItemFound) {
                    nullItemFound = true;
                    proResult[count++] = null;  //store the first null-item
                }
                /*
                 * Skip all null-items. Each null-item is either the first
                 * null-item in the array and has been already stored to the
                 * result, or it is a cleared duplicate item and we do not want
                 * to compare it with other items.
                 */
                continue;
            }

            boolean isDuplicate = false;
            for (int j = i + 1; j < array.length; j++) {
                if (equal(item, array[j])) {
                    isDuplicate = true;
                    array[j] = null;        //clear item known to be a duplicate
                }
            }
            if (isDuplicate) {
                continue;
            }
            proResult[count++] = array[i];
        }
        proResult[count++] = array[mainTopBound];
        return shortenArray(proResult, count);
    }

    /**
     * Returns array containing all non-{@code null} elements from the given
     * array, in the same order.
     * <p>
     * The returned array can be the same instance as the passed array
     * if the passed array did not contain any {@code null} items.
     *
     * @param  array  array to remove {@code null} elements from
     * @return  the passed array, if it did not contain any {@code null} items,
     *          or a new array being a copy of the passed array with
     *          {@code null} items missing
     * @exception  java.lang.NullPointerException
     *             if the passed array was {@code null}
     * @since 1.9.1
     */
    public static <T> T[] removeNulls(T[] array) {
        if (array == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        int i = 0;
        for (i = 0; i < array.length; i++) {
            if (array[i] == null) {
                break;
            }
        }
        /* 'i' now contains number of non-null items */

        if (i == array.length) {        //there were no null-items
            return array;
        }


        T[] proResult = (T[]) Array.newInstance(
                                    array.getClass().getComponentType(),
                                    array.length - 1);
        int count = i;      //number of non-null items
        if (count != 0) {
            System.arraycopy(array, 0, proResult, 0, count);
        }

        for (i = i + 1; i < array.length; i++) {
            if (array[i] != null) {
                proResult[count++] = array[i];
            }
        }
        return shortenArray(proResult, count);
    }

    /**
     * Returns array containing the same elements and in the same order
     * as the array passed as an argument, except that all trailing {@code null}
     * elements (if any) are missing.
     *
     * @param  array  array to remove trailing {@code null} elements from
     * @return  the passed array, if it did not contain any trailing
     *          {@code null} elements, or a new array being a copy of the
     *          passed array with trailing {@code null} elements missing
     * @since 1.9.1
     */
    public static <T> T[] stripTrailingNulls(T[] array) {
        if (array == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        /* count trailing nulls -> compute size of the resulting array */
        int resultSize = array.length;
        while ((resultSize > 0) && (array[resultSize - 1] == null)) {
            resultSize--;
        }

        return shortenArray(array, resultSize);
    }

    /**
     * Shortens the given array to the requested length by stripping last
     * few items.
     *
     * @param  array  array to be shortened
     * @param  requestedLength  requested length of the array
     * @return  the passed array if it was already of the requested length,
     *          or a new array containing the given number of items
     *          from the passed array, in the same order
     * @exception  java.lang.NullPointerException
     *             if the passed array was {@code null} or if the requested
     *             length was out of range (less than 0 or greater than
     *             the length of the passed array)
     * @since 1.9.1
     */
    public static <T> T[] shortenArray(T[] array, int requestedLength) {
        if (array == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        if (requestedLength < 0) {
            throw new IllegalArgumentException(
                "negative requested length (" + requestedLength + ')'); //NOI18N
        }
        if (requestedLength > array.length) {
            throw new IllegalArgumentException(
                    "requested length (" + requestedLength              //NOI18N
                    + ") is greater than the current length ("          //NOI18N
                    + array.length + ')');
        }
        if (requestedLength == array.length) {
            return array;
        }

        T[] result = makeArray(array, requestedLength);
        if (requestedLength != 0) {
            System.arraycopy(array, 0, result, 0, requestedLength);
        }
        return result;
    }

    /**
     * Creates a copy of the given array.
     *
     * @param  source  array to be copied
     * @return  new array containing the same items and in the same order
     *          as the source array
     * @exception  java.lang.IllegalArgumentException
     *             if the source array is {@code null}
     * @since 1.9.1
     */
    public static <T> T[] copyArray(T[] source) {
        if (source == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        T[] result = makeArray(source, source.length);
        System.arraycopy(source, 0, result, 0, source.length);
        return result;
    }

    /**
     * Makes a new array of the same type as the array passed as an argument.
     * 
     * @param  typeTemplate  array to get the type of elements from
     * @param  length  requested length of the new array
     * @return  new array of the type of the given array
     * @exception  java.lang.IllegalArgumentException
     *             if the passed array is {@code null}
     *             or if the requested length is negative
     * @since 1.9.1
     */
    public static <T> T[] makeArray(T[] typeTemplate, int length) {
        if (typeTemplate == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        if (length < 0) {
            throw new IllegalArgumentException("negative length");      //NOI18N
        }

        return (T[]) java.lang.reflect.Array.newInstance(
                                    typeTemplate.getClass().getComponentType(),
                                    length);
    }

    /**
     * Compares whether the two arrays contain the same elements (in any order).
     * {@code null} arrays are legal and are treated like empty arrays.
     * Elements of the array are compared using identity check, method
     * {@code equals()} is not used.
     * Order of elements in the arrays is not taken into account.
     * {@code null} elements in arrays are legal.
     *
     * @param  a  first array to be compared with the other array
     * @param  b  second array to be compared with the other array
     * @return  {@code true} if the two arrays contain exactly the same object
     *          references as the other array, ignoring order of the elements
     */
    public static boolean containSameObjects(Object[] a, Object[] b) {
        int lengthA = (a != null) ? a.length : 0;
        int lengthB = (b != null) ? b.length : 0;
        if (lengthA != lengthB) {
            return false;
        }

        final int length = lengthA;
        if (length == 0) {
            return true;
        }
        if (length == 1) {
            return a[0] == b[0];
        }
        if (length == 2) {
            return    (a[0] == b[0]) && (a[1] == b[1])
                   || (a[0] == b[1]) && (a[1] == b[0]);
        }

        return compareArrays(a, b, length,
                             new IdentityHashMap<Object,Counter>(length));
    }

    /**
     * Compares whether the two arrays contain equal elements (in any order).
     * Elements of the array are compared using method {@code equals()}.
     * {@code null} arrays are legal and are treated like empty arrays.
     * <p>
     * It is legal to have two or more
     * {@linkplain java.lang.Object#equals equal} elements in a single array.
     * In such a case, the other array must contain exactly the same number
     * of objects equal to those from the first array, otherwise this method
     * will return {@code false}. Order of elements in the arrays is not taken
     * into account.
     * <p>
     * {@code null} elements in arrays are legal. Two {@code null} elements
     * are considered equal to each other.
     *
     * @param  a  first array to be compared with the other array,
     *            or {@code null}
     * @param  b  second array to be compared with the other array,
     *            or {@code null}
     * @return  {@code true} if the two given arrays are equal,
     *          {@code false} otherwise
     */
    public static boolean containEqualObjects(Object[] a, Object[] b) {
        int lengthA = (a != null) ? a.length : 0;
        int lengthB = (b != null) ? b.length : 0;
        if (lengthA != lengthB) {
            return false;
        }

        final int length = lengthA;
        if (length == 0) {
            return true;
        }
        if (length == 1) {
            return equal(a[0], b[0]);
        }
        if (length == 2) {
            return    equal(a[0], b[0]) && equal(a[1], b[1])
                   || equal(a[0], b[1]) && equal(a[1], b[0]);
        }

        return compareArrays(a, b, length,
                             new HashMap<Object,Counter>((length * 4 + 2) / 3,
                                                         0.75f));
    }

    /**
     * Compares content of given arrays using the given map.
     * The passed map defines how similar a pair of elements must be in order
     * to be considered equivalent. E.e. HashMap considers elements
     * {@code a}, {@code b} equivalent if {@code a.equals(b)}, while
     * IdentityHashMap requires that {@code a == b}.
     *
     * @param  a  first array
     * @param  b  second array
     * @param  length  number of elements in each of the arrays
     * @param  comparatorMap  map to be used for comparison
     * @return  {@code true} if the arrays are found to have equal/same content,
     *          {@code false} otherwise
     */
    private static boolean compareArrays(Object[] a, Object[] b,
                                         final int length,
                                         Map<Object,Counter> comparatorMap) {
        /*
         * At first, store elements of the first array to the map, together
         * with number of occurences of equal elements.
         */
        Counter newCounter = new Counter();
        Counter previousCounter;
        for (int i = 0; i < length; i++) {
            previousCounter = comparatorMap.put(a[i], newCounter);
            newCounter = (previousCounter != null)
                         ? previousCounter.passIncrementedValueTo(newCounter)
                         : new Counter();
        }

        /* Then, verify that the second array produces the same data: */
        for (int i = 0; i < length; i++) {
            Counter counter = comparatorMap.get(b[i]);
            if (counter == null) {
                return false;           //element from 'b' not found in 'a'
            }
            if (counter.dec().isNegative()) {
                return false;   //more equivalents of 'b[i]' in 'b' than in 'a'
            }
        }

        return true;
    }

    static class Counter {
        private int number;
        Counter() {
            reset();
        }
        Counter reset() {
            number = 1;
            return this;
        }
        Counter passIncrementedValueTo(Counter counter) {
            counter.number = this.number + 1;
            reset();
            return this;
        }
        Counter dec() {
            number--;
            return this;
        }
        boolean isNegative() {
            return number < 0;
        }
    }

    public static <A,B> A[] retainAll(A[] a, B[] b) {
        if (a.length == 0) {
            return a;
        }
        if (b.length == 0) {
            return makeArray(a, 0);
        }
        Collection<A> setA = new HashSet<A>(Arrays.asList(a));
        Collection<B> setB = new HashSet<B>(Arrays.asList(b));
        setA.retainAll(setB);
        return setA.isEmpty() ? makeArray(a, 0)
                              : setA.toArray(makeArray(a, setA.size()));
    }

    public static <A,B> A[] retainAllByIdentity(A[] a, B[] b) {
        if (a.length == 0) {
            return a;
        }
        if (b.length == 0) {
            return makeArray(a, 0);
        }
        Collection<A> setA = new IdentityHashSet<A>(a);
        Collection<B> setB = new IdentityHashSet<B>(b);
        setA.retainAll(setB);
        return setA.isEmpty() ? makeArray(a, 0)
                              : setA.toArray(makeArray(a, setA.size()));
    }

    static final class IdentityComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if (o1 == null) {
                return (o2 == null) ? 0 : 1;
            }
            return (o2 != null)
                   ? System.identityHashCode(o2) - System.identityHashCode(o1)
                   : -1;
        }
    }

    static boolean equal(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    /**
     * Returns an unmodifiable {@code List} of the given items.
     * It is not guaranteed that each invocation will return a new instance.
     *
     * @param  items  items to be included in the resulting {@code List}
     * @exception  java.lang.IllegalArgumentException
     *             if the argument is {@code null}
     * @since 1.9.1
     */
    public static <T> List<T> unmodifiableList(T... items) {
        if (items == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }
        if (items.length == 0) {
            return Collections.emptyList();
        }
        if (items.length == 1) {
            return Collections.singletonList(items[0]);
        }

        List<T> result = new ArrayList<T>(items.length);
        for (int i = 0; i < items.length; i++) {
            result.add(items[i]);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns an unmodifiable {@code Set} of the given items.
     * It is not guaranteed that each invocation will return a new instance.
     *
     * @param  items  items to be included in the resulting {@code Set}
     * @exception  java.lang.IllegalArgumentException
     *             if the argument is {@code null}
     * @since 1.9.1
     */
    public static <T> Set<T> unmodifiableSet(T... items) {
        if (items == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        items = removeDuplicates(items);
        if (items.length == 0) {
            return Collections.emptySet();
        }
        if (items.length == 1) {
            return Collections.singleton(items[0]);
        }
        return new UnmodifiableArraySet<T>(items);
    }


    static class UnmodifiableArraySet<E> extends AbstractSet<E> {

        private final E[] content;

        /**
         * @param  items  array of unique items
         */
        public UnmodifiableArraySet(E[] items) {
            if (items == null) {
                throw new IllegalArgumentException("null");             //NOI18N
            }
            this.content = items;
        }

        @Override
        public boolean contains(Object o) {
            if (isEmpty()) {
                return false;
            }

            for (E element : content) {
                if (equal(element, o)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            if (c.isEmpty()) {
                return true;
            }
            if (isEmpty()) {
                return false;
            }
            if (c instanceof UnmodifiableArraySet) {
                UnmodifiableArraySet m = (UnmodifiableArraySet) c;
                if (m.size() > this.size()) {
                    return false;
                }
            }
            if (c.getClass() == HashSet.class) {
                HashSet m = (HashSet) c;
                if (m.size() > this.size()) {
                    return false;
                }
            }
            return super.containsAll(c);
        }

        @Override
        public Iterator<E> iterator() {
            return (content.length == 0)
                   ? Collections.<E>emptySet().iterator()
                   : new ArrayListIterator(content);
        }

        @Override
        public int size() {
            return content.length;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            if (a == null) {
                throw new NullPointerException();
            }

            if (a.length < content.length) {
                a = makeArray(a, content.length);
            }
            System.arraycopy(content, 0, a, 0, content.length);

            if (a.length > content.length) {
                a[content.length] = null;
            }

            return a;
        }

        @Override
        public Object[] toArray() {
            Object[] result = new Object[content.length];
            if (result.length != 0) {
                System.arraycopy(content, 0, result, 0, content.length);
            }
            return result;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * Simple {@code ListIterator} for iteration across a given array.
     * It does not support the optional operations that would modify data,
     * i.e. {@code add()}, {@code remove()} and {@code set()}.
     */
    static class ArrayListIterator<T> implements ListIterator<T> {

        private final T[] array;
        private int index = 0;

        public ArrayListIterator(T[] array) {
            if (array == null) {
                throw new IllegalArgumentException("null");             //NOI18N
            }

            this.array = array;
        }

        public boolean hasNext() {
            return index < array.length;
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return array[index++];
        }

        public boolean hasPrevious() {
            return index > 0;
        }

        public T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            return array[--index];
        }

        public int nextIndex() {
            return index;
        }

        public int previousIndex() {
            return index - 1;
        }

        public void add(T o) {
            throw new UnsupportedOperationException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(T o) {
            throw new UnsupportedOperationException();
        }

    }

}
