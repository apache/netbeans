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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class implements the {@code Set} interface with a hash table, using
 * reference-equality in place of object-equality when comparing elements.
 * In other words, in an {@code IdentityHashSet}, two elements
 * {@code e1} and {@code e2} are considered equal if and only if
 * {@code (e1 == e2)}.  (In normal {@code Map} implementations (like
 * {@code HashMap}) two elements {@code e1} and {@code e2} are considered equal
 * if and only if {@code (e1 == null ? e2 == null : e1.equals(e2))}.)
 *
 * <p><b>This class is <i>not</i> a general-purpose {@code Map} implementation!
 * While this class implements the {@code Map} interface, it intentionally
 * violates {@code Map's} general contract, which mandates the use of
 * the {@code equals} method when comparing objects.  This class is designed
 * for use only in the rare cases wherein reference-equality semantics are
 * required.</b>
 *
 * @author  Doug Lea and Josh Bloch
 * @author  Marian Petras
 * @since  1.9.1
 */
public class IdentityHashSet<E> extends AbstractSet<E> {

    /*
     * The implementation is based on implementation if IdentityHashMap
     * by Doug Lea and Josh Bloch.
     */

    /**
     * The initial capacity used by the no-args constructor.
     * MUST be a power of two.  The value 32 corresponds to the
     * (specified) expected maximum size of 21, given a load factor
     * of 2/3.
     */
    private static final int DEFAULT_CAPACITY = 32;

    /**
     * The minimum capacity, used if a lower value is implicitly specified
     * by either of the constructors with arguments.  The value 4 corresponds
     * to an expected maximum size of 2, given a load factor of 2/3.
     * MUST be a power of two.
     */
    private static final int MINIMUM_CAPACITY = 4;

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two &lt;= 1&lt;&lt;30.
     */
    private static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The table, resized as necessary. Length MUST always be a power of two.
     */
    private transient E[] table;

    /**
     * The number of elements contained in this identity hash set.
     */
    private int size;

    /**
     * The number of modifications, to support fast-fail iterators
     */
    private transient volatile int modCount;

    /**
     * The next size value at which to resize (capacity * load factor).
     */
    private transient int threshold;

    /**
     * Constructs a new, empty identity hash set with a default expected
     * maximum size (21).
     */
    public IdentityHashSet() {
        init(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a new, empty set with the specified expected maximum size.
     * Putting more than the expected number of elements into the map
     * may cause the internal data structure to grow, which may be
     * somewhat time-consuming.
     *
     * @param  expectedMaxSize  the expected maximum size of the set
     * @throws  IllegalArgumentException if <tt>expectedMaxSize</tt> is negative
     */
    public IdentityHashSet(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException("expectedMaxSize is negative: "
                                               + expectedMaxSize);
        init(capacity(expectedMaxSize));
    }

    public IdentityHashSet(Collection<? extends E> toAdd) {
        // Allow for a bit of growth
        this((int) ((1 + toAdd.size()) * 1.1));
        addAll(toAdd);
    }

    public IdentityHashSet(E[] toAdd) {
        // Allow for a bit of growth
        this((int) ((1 + toAdd.length) * 1.1));
        addAll(toAdd);
    }

    /**
     * Returns the appropriate capacity for the specified expected maximum
     * size.  Returns the smallest power of two between MINIMUM_CAPACITY
     * and MAXIMUM_CAPACITY, inclusive, that is greater than
     * (3 * expectedMaxSize)/2, if such a number exists.  Otherwise
     * returns MAXIMUM_CAPACITY.  If (3 * expectedMaxSize)/2 is negative, it
     * is assumed that overflow has occurred, and MAXIMUM_CAPACITY is returned.
     */
    private int capacity(int expectedMaxSize) {
        // Compute min capacity for expectedMaxSize given a load factor of 2/3
        int minCapacity = (3 * expectedMaxSize + 1) / 2;    // + 1 ... round up

        // Compute the appropriate capacity
        int result;
        if ((minCapacity > MAXIMUM_CAPACITY) || (minCapacity < 0)) {
            result = MAXIMUM_CAPACITY;
        } else {
            result = MINIMUM_CAPACITY;
            while (result < minCapacity) {
                result <<= 1;
            }
        }
        return result;
    }

    private void init(int initCapacity) {
        threshold = (initCapacity * 2) / 3;
        table = (E[]) new Object[initCapacity];
    }

    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        Object[] tab = table;
        int len = tab.length;
        int i = hash(o, len);
        while (true) {
            Object item = tab[i];
            if (item == o) {
                return true;
            }
            if (item == null) {
                return false;
            }
            i = nextElementIndex(i, len);
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c == this) {
            return true;
        } else if (c instanceof IdentityHashSet) {
            IdentityHashSet m = (IdentityHashSet) c;
            if (m.size() > size) {
                return false;
            }
            Object[] tab = m.table;
            for (int i = 0; i < tab.length; i++) {
                Object o = tab[i];
                if ((o != null) && !contains(o)) {
                    return false;
                }
            }
            return true;
        } else if (c instanceof Set) {
            Set m = (Set) c;
            if (m.size() > size) {
                return false;
            }
        }
        return super.containsAll(c);
    }

    @Override
    public boolean add(E o) {
        E[] tab = table;
        int len = tab.length;
        int i = hash(o, len);

        E item;
        while ((item = tab[i]) != null) {
            if (item == o) {
                return false;
            }
            i = nextElementIndex(i, len);
        }

        modCount++;
        tab[i] = o;
        if (++size >= threshold) {
            resize(len);                    // len = 2 * current capacity
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        int n = c.size();
        if (n == 0) {
            return false;
        }

        if (n > threshold) {        // conservatively pre-expand
            resize(capacity(n));
        }

        boolean changed = false;
        for (E item : c) {
            changed |= add(item);
        }
        return changed;
    }

    public boolean addAll(E[] c) {
        int n = c.length;
        if (n == 0) {
            return false;
        }

        if (n > threshold) {        // conservatively pre-expand
            resize(capacity(n));
        }

        boolean changed = false;
        for (E item : c) {
            changed |= add(item);
        }
        return changed;
    }

    @Override
    public boolean remove(Object o) {
        E[] tab = table;
        int len = tab.length;
        int i = hash(o, len);

        while (true) {
            Object item = tab[i];
            if (item == o) {
                modCount++;
                size--;
                tab[i] = null;
                closeDeletion(i);
                return true;
            }
            if (item == null) {
                return false;
            }
            i = nextElementIndex(i, len);
        }
    }

    /**
     * Rehash all possibly-colliding entries following a
     * deletion. This preserves the linear-probe
     * collision properties required by get, put, etc.
     *
     * @param  d  the index of a newly empty deleted slot
     */
    private void closeDeletion(int d) {
        // Adapted from Knuth Section 6.4 Algorithm R
        E[] tab = table;
        int len = tab.length;

        // Look for items to swap into newly vacated slot
        // starting at index immediately following deletion,
        // and continuing until a null slot is seen, indicating
        // the end of a run of possibly-colliding keys.
        E item;
        for (int i = nextElementIndex(d, len); (item = tab[i]) != null;
             i = nextElementIndex(i, len) ) {
            // The following test triggers if the item at slot i (which
            // hashes to be at slot r) should take the spot vacated by d.
            // If so, we swap it in, and then continue with d now at the
            // newly vacated i.  This process will terminate when we hit
            // the null slot at the end of this run.
            // The test is messy because we are using a circular table.
            int r = hash(item, len);
            if ((i < r && (r <= d || d <= i)) || (r <= d && d <= i)) {
                tab[d] = item;
                tab[i] = null;
                d = i;
            }
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if (isEmpty()) {
            return false;
        } else if (c == this) {
            clear();
            return true;
        } else if (c.isEmpty()) {
            return false;
        } else if (c instanceof IdentityHashSet) {
            IdentityHashSet m = (IdentityHashSet) c;
            boolean changed = false;
            Object[] tab = m.table;
            for (int i = 0; i < tab.length; i++) {
                Object o = tab[i];
                if (o != null) {
                    changed |= remove(o);
                }
            }
            return changed;
        } else {
            return super.removeAll(c);
        }
    }

    @Override
    public void clear() {
        if (isEmpty()) {
            return;
        }

        modCount++;
        E[] tab = table;
        for (int i = 0; i < tab.length; i++) {
            tab[i] = null;
        }
        size = 0;
    }

    public Iterator<E> iterator() {
        return new IdentityHashSetIterator();
    }

    private class IdentityHashSetIterator implements Iterator<E> {
        int index = (size != 0 ? 0 : table.length); // current slot.
        int expectedModCount = modCount; // to support fast-fail
        int lastReturnedIndex = -1;      // to allow remove()
        boolean indexValid; // To avoid unnecessary next computation
	E[] traversalTable = table; // reference to main table or copy

        public boolean hasNext() {
            E[] tab = traversalTable;
            if (!indexValid) {
                int i;
                for (i = index; i < tab.length; i++) {
                    if (tab[i] != null) {
                        break;
                    }
                }
                index = i;
                indexValid = true;
            }
            return (index < tab.length);
        }

        protected int nextIndex() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            indexValid = false;
            lastReturnedIndex = index;
            index++;
            return lastReturnedIndex;
        }

        public E next() {
            nextIndex();
            return traversalTable[lastReturnedIndex];
        }

        public void remove() {
            if (lastReturnedIndex == -1) {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }

            expectedModCount = ++modCount;
            int deletedSlot = lastReturnedIndex;
            lastReturnedIndex = -1;
            size--;
            // back up index to revisit new contents after deletion
            index = deletedSlot;
            indexValid = false;

            // Removal code proceeds as in closeDeletion except that
            // it must catch the rare case where an element already
            // seen is swapped into a vacant slot that will be later
            // traversed by this iterator. We cannot allow future
            // next() calls to return it again.  The likelihood of
            // this occurring under 2/3 load factor is very slim, but
            // when it does happen, we must make a copy of the rest of
            // the table to use for the rest of the traversal. Since
            // this can only happen when we are near the end of the table,
            // even in these rare cases, this is not very expensive in
            // time or space.

            E[] tab = traversalTable;
            int len = tab.length;

            int d = deletedSlot;
            E element = tab[d];
            tab[d] = null;        // vacate the slot

            // If traversing a copy, remove in real table.
            // We can skip gap-closure on copy.
            if (tab != IdentityHashSet.this.table) {
                IdentityHashSet.this.remove(element);
                expectedModCount = modCount;
                return;
            }

            E item;
            for (int i = nextElementIndex(d, len); (item = tab[i]) != null;
                 i = nextElementIndex(i, len)) {
                int r = hash(item, len);
                // See closeDeletion for explanation of this conditional
                if ((i < r && (r <= d || d <= i)) ||
                    (r <= d && d <= i)) {

                    // If we are about to swap an already-seen element
                    // into a slot that may later be returned by next(),
                    // then clone the rest of table for use in future
                    // next() calls. It is OK that our copy will have
                    // a gap in the "wrong" place, since it will never
                    // be used for searching anyway.

                    if (i < deletedSlot && d >= deletedSlot &&
                        traversalTable == IdentityHashSet.this.table) {
                        int remaining = len - deletedSlot;
                        E[] newTable = (E[]) new Object[remaining];
                        System.arraycopy(tab, deletedSlot,
                                         newTable, 0, remaining);
                        traversalTable = newTable;
                        index = 0;
                    }

                    tab[d] = item;
                    tab[i] = null;
                    d = i;
                }
            }
        }
    }

    @Override
    protected Object clone() {
        try {
            IdentityHashSet<E> t = (IdentityHashSet<E>) super.clone();
            t.table = (E[]) table.clone();
            return t;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof IdentityHashSet) {
            IdentityHashSet m = (IdentityHashSet) other;
            if (m.size() != size) {
                return false;
            }

            Object[] tab = m.table;
            for (int i = 0; i < tab.length; i++) {
                Object o = tab[i];
                if ((o != null) && !contains(o)) {
                    return false;
                }
            }
            return true;
        } else if (other instanceof Set) {
            Set m = (Set) other;
            if (m.size() != size) {
                return false;
            }

            return containsAll(m);
        } else {
            return false;                   // other is not a Set
        }
    }

    @Override
    public int hashCode() {
	int h = 0;
        for (E item : this) {
            h += item.hashCode();
        }
	return h;
    }

    /**
     * Resize the table to hold given capacity.
     *
     * @param  newCapacity  the new capacity, must be a power of two
     */
    private void resize(int newCapacity) {
        // assert (newCapacity & -newCapacity) == newCapacity; // power of 2
        int newLength = newCapacity;

	E[] oldTable = table;
        int oldLength = oldTable.length;
        if (oldLength == MAXIMUM_CAPACITY) { // can't expand any further
            if (threshold == (MAXIMUM_CAPACITY - 1)) {
                throw new IllegalStateException("Capacity exhausted."); //NOI18N
            }
            threshold = MAXIMUM_CAPACITY - 1;  // Gigantic set!
            return;
        }
        if (oldLength >= newLength) {
            return;
        }

	E[] newTable = (E[]) new Object[newLength];
        threshold = newLength * 2 / 3;

        for (int j = 0; j < oldLength; j++) {
            E element = oldTable[j];
            if (element != null) {
                oldTable[j] = null;
                int i = hash(element, newLength);
                while (newTable[i] != null) {
                    i = nextElementIndex(i, newLength);
                }
                newTable[i] = element;
            }
        }
        table = newTable;
    }

    /**
     * Return index for Object x.
     */
    private static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        // Multiply by -127, and left-shift to use least bit as part of hash
        return ((h << 1) - (h << 8)) & (length - 1);
    }

    /**
     * Circularly traverse table of size len.
     **/
    private static int nextElementIndex(int i, int len) {
        return (i < len ? i + 1 : 0);
    }

}
