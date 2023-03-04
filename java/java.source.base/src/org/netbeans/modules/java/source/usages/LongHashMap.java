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

package org.netbeans.modules.java.source.usages;

import java.util.*;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Todo: Copied from parsing API, also in cnd - should be part of API?
 */
public class LongHashMap<K> //extends AbstractMap<K>
//implements Map<K>, Cloneable, Serializable
{

    public static final long NO_VALUE = Long.MIN_VALUE;
    /**
     * The default initial capacity - MUST be a power of two.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;
    /**
     * The load factor used when none specified in constructor.
     **/
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * The table, resized as necessary. Length MUST Always be a power of two.
     */
    transient Entry<K>[] table;
    /**
     * The number of key-value mappings contained in this identity hash map.
     */
    transient int size;
    /**
     * The next size value at which to resize (capacity * load factor).
     * @serial
     */
    int threshold;
    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;
    /**
     * The number of times this LongHashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the LongHashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the LongHashMap fail-fast.  (See ConcurrentModificationException).
     */
    transient volatile int modCount;

    /**
     * Constructs an empty <tt>LongHashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param initialCapacity The initial capacity.
     * @param loadFactor      The load factor.
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive.
     */
    public LongHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + // NOI18N
                    initialCapacity);
        }
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + // NOI18N
                    loadFactor);
        }

        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }

        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
        @SuppressWarnings("unchecked")
        Entry<K>[] ar = new Entry[capacity];
        table = ar;
        init();
    }

    /**
     * Constructs an empty <tt>LongHashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public LongHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>LongHashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    public LongHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
        @SuppressWarnings("unchecked")
        Entry<K>[] ar = new Entry[DEFAULT_INITIAL_CAPACITY];
        table = ar;
        init();
    }

    /**
     * Creates a new {@link LongHashMap} as a copy of given map.
     * @param m the {@link LongHashMap} to copy.
     */
    public LongHashMap(@NonNull final LongHashMap<K> m) {
        this(m.size(),DEFAULT_LOAD_FACTOR);
        for (LongHashMap.Entry<K> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    // internal utilities
    /**
     * Initialization hook for subclasses. This method is called
     * in all constructors and pseudo-constructors (clone, readObject)
     * after LongHashMap has been initialized but before any entries have
     * been inserted.  (In the absence of this method, readObject would
     * require explicit knowledge of subclasses.)
     */
    void init() {
    }
    /**
     * Value representing null keys inside tables.
     */
    static final Object NULL_KEY = new Object();

    /**
     * Returns internal representation for key. Use NULL_KEY if key is null.
     */
    static <T> Object maskNull(T key) {
        return key == null ? NULL_KEY : key;
    }

    /**
     * Returns key represented by specified internal representation.
     */
    static <T> T unmaskNull(T key) {
        return (key == NULL_KEY ? null : key);
    }

    /** 
     * Check for equality of non-null reference x and possibly-null y. 
     */
    static boolean eq(Object x, Object y) {
        return x == y || x.equals(y);
    }

    /**
     * Returns index for hash code h. 
     */
    static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map.
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped in this identity
     * hash map, or <tt>null</tt> if the map contains no mapping for this key.
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate
     * that the map contains no mapping for the key; it is also possible that
     * the map explicitly maps the key to <tt>null</tt>. The
     * <tt>containsKey</tt> method may be used to distinguish these two cases.
     *
     * @param   key the key whose associated value is to be returned.
     * @return  the value to which this map maps the specified key, or
     *          <tt>null</tt> if the map contains no mapping for this key.
     * @see #put(Object, Object)
     */
    public long get(Object key) {
        if (key == null) {
            return getForNullKey();
        }
        int hash = key.hashCode();
        for (Entry<K> e = table[indexFor(hash, table.length)];
                e != null;
                e = e.next) {
            Object k;
            if (e.key.hashCode() == hash && ((k = e.key) == key || key.equals(k))) {
                return e.value;
            }
        }
        return NO_VALUE;
    }

    private long getForNullKey() {
        int hash = NULL_KEY.hashCode();
        int i = indexFor(hash, table.length);
        Entry<K> e = table[i];
        while (true) {
            if (e == null) {
                return NO_VALUE;
            }
            if (e.key == NULL_KEY) {
                return e.value;
            }
            e = e.next;
        }
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
        Object k = maskNull(key);
        int hash = k.hashCode();
        int i = indexFor(hash, table.length);
        Entry e = table[i];
        while (e != null) {
            if (e.key.hashCode() == hash && eq(k, e.key)) {
                return true;
            }
            e = e.next;
        }
        return false;
    }

    /**
     * Returns the entry associated with the specified key in the
     * LongHashMap.  Returns null if the LongHashMap contains no mapping
     * for this key.
     */
    public Entry<K> getEntry(Object key) {
        Object k = maskNull(key);
        int hash = k.hashCode();
        int i = indexFor(hash, table.length);
        Entry<K> e = table[i];
        while (e != null && !(e.key.hashCode() == hash && eq(k, e.key))) {
            e = e.next;
        }
        return e;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     * 	       if there was no mapping for key.  A <tt>null</tt> return can
     * 	       also indicate that the LongHashMap previously associated
     * 	       <tt>null</tt> with the specified key.
     */
    public long put(K key, long value) {
        if (key == null) {
            return putForNullKey(value);
        }
        int hash = key.hashCode();
        int i = indexFor(hash, table.length);
        for (Entry<K> e = table[i]; e != null; e = e.next) {
            Object k;
            if (e.key.hashCode() == hash && ((k = e.key) == key || key.equals(k))) {
                long oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(key, value, i);
        return NO_VALUE;
    }

    private long putForNullKey(long value) {
        int hash = NULL_KEY.hashCode();
        int i = indexFor(hash, table.length);

        for (Entry<K> e = table[i]; e != null; e = e.next) {
            if (e.key == NULL_KEY) {
                long oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        @SuppressWarnings("unchecked")
        K nullKey = (K) NULL_KEY;
        addEntry(nullKey, value, i);
        return NO_VALUE;
    }

    /**
     * This method is used instead of put by constructors and
     * pseudoconstructors (clone, readObject).  It does not resize the table,
     * check for comodification, etc.  It calls createEntry rather than
     * addEntry.
     */
//    private void putForCreate(K key, long value) {
//        K k = maskNull(key);
//        int hash = hash(k.hashCode());
//        int i = indexFor(hash, table.length);
//
//        /**
//         * Look for preexisting entry for key.  This will never happen for
//         * clone or deserialize.  It will only happen for construction if the
//         * input Map is a sorted map whose ordering is inconsistent w/ equals.
//         */
//        for (Entry<K> e = table[i]; e != null; e = e.next) {
//            if (e.hash == hash && eq(k, e.key)) {
//                e.value = value;
//                return;
//            }
//        }
//
//        createEntry(hash, k, value, i);
//    }
    /**
     * Rehashes the contents of this map into a new array with a
     * larger capacity.  This method is called automatically when the
     * number of keys in this map reaches its threshold.
     *
     * If current capacity is MAXIMUM_CAPACITY, this method does not
     * resize the map, but sets threshold to Integer.MAX_VALUE.
     * This has the effect of preventing future calls.
     *
     * @param newCapacity the new capacity, MUST be a power of two;
     *        must be greater than current capacity unless current
     *        capacity is MAXIMUM_CAPACITY (in which case value
     *        is irrelevant).
     */
    void resize(int newCapacity) {
        Entry<K>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        @SuppressWarnings("unchecked")
        Entry<K>[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int) (newCapacity * loadFactor);
    }

    /** 
     * Transfer all entries from current table to newTable.
     */
    void transfer(Entry<K>[] newTable) {
        Entry<K>[] src = table;
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            Entry<K> e = src[j];
            if (e != null) {
                src[j] = null;
                do {
                    Entry<K> next = e.next;
                    int i = indexFor(e.key.hashCode(), newCapacity);
                    e.next = newTable[i];
                    newTable[i] = e;
                    e = next;
                } while (e != null);
            }
        }
    }

    public long remove(Object key) {
        Entry<K> e = removeEntryForKey(key);
        return (e == null ? NO_VALUE : e.value);
    }

    /**
     * Removes and returns the entry associated with the specified key
     * in the LongHashMap.  Returns null if the LongHashMap contains no mapping
     * for this key.
     */
    Entry<K> removeEntryForKey(Object key) {
        Object k = maskNull(key);
        int hash = k.hashCode();
        int i = indexFor(hash, table.length);
        Entry<K> prev = table[i];
        Entry<K> e = prev;

        while (e != null) {
            Entry<K> next = e.next;
            if (e.key.hashCode() == hash && eq(k, e.key)) {
                modCount++;
                size--;
                if (prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Special version of remove for EntrySet.
     */
    Entry<K> removeMapping(Object o) {
        if (!(o instanceof Map.Entry)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        LongHashMap.Entry<K> entry = (LongHashMap.Entry<K>) o;
        Object k = maskNull(entry.getKey());
        int hash = k.hashCode();
        int i = indexFor(hash, table.length);
        Entry<K> prev = table[i];
        Entry<K> e = prev;

        while (e != null) {
            Entry<K> next = e.next;
            if (e.key.hashCode() == hash && e.equals(entry)) {
                modCount++;
                size--;
                if (prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }
                e.recordRemoval(this);
                return e;
            }
            prev = e;
            e = next;
        }

        return e;
    }

    /**
     * Removes all mappings from this map.
     */
    public void clear() {
        modCount++;
        Entry[] tab = table;
        for (int i = 0; i < tab.length; i++) {
            tab[i] = null;
        }
        size = 0;
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested.
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value.
     */
    public boolean containsValue(Object value) {
        if (value == null) {
            return containsNullValue();
        }

        Entry[] tab = table;
        for (int i = 0; i < tab.length; i++) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Special-case code for containsValue with null argument
     **/
    private boolean containsNullValue() {
        Entry[] tab = table;
        for (int i = 0; i < tab.length; i++) {
            for (Entry e = tab[i]; e != null; e = e.next) {
                if (e.value == NO_VALUE) {
                    return true;
                }
            }
        }
        return false;
    }

    public static class Entry<K> /*implements Map.Entry<K>*/ {

        final K key;
        long value;
        Entry<K> next;

        /**
         * Create new entry.
         */
        Entry(K k, long v, Entry<K> n) {
            value = v;
            next = n;
            key = k;
        }

        public K getKey() {
            return LongHashMap.<K>unmaskNull(key);
        }

        public long getValue() {
            return value;
        }

        public long setValue(long newValue) {
            long oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (int) (value ^ (value >>> 32));
        }

        @Override
        public String toString() {
            return getKey() + "=" + getValue(); // NOI18N
        }

        /**
         * This method is invoked whenever the value in an entry is
         * overwritten by an invocation of put(k,v) for a key k that's already
         * in the LongHashMap.
         */
        void recordAccess(LongHashMap<K> m) {
        }

        /**
         * This method is invoked whenever the entry is
         * removed from the table.
         */
        void recordRemoval(LongHashMap<K> m) {
        }
    }

    /**
     * Add a new entry with the specified key, value and hash code to
     * the specified bucket.  It is the responsibility of this 
     * method to resize the table if appropriate.
     *
     * Subclass overrides this to alter the behavior of put method.
     */
    void addEntry(K key, long value, int bucketIndex) {
        Entry<K> e = table[bucketIndex];
        table[bucketIndex] = new Entry<K>(key, value, e);
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
    }

    private abstract class HashIterator<E> implements Iterator<E> {

        Entry<K> next;	// next entry to return
        int expectedModCount;	// For fast-fail 
        int index;		// current slot 
        Entry<K> current;	// current entry

        HashIterator() {
            expectedModCount = modCount;
            Entry<K>[] t = table;
            int i = t.length;
            Entry<K> n = null;
            if (size != 0) { // advance to first entry
                while (i > 0 && (n = t[--i]) == null) {
                }
            }
            next = n;
            index = i;
        }

        public boolean hasNext() {
            return next != null;
        }

        Entry<K> nextEntry() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            Entry<K> e = next;
            if (e == null) {
                throw new NoSuchElementException();
            }

            Entry<K> n = e.next;
            Entry<K>[] t = table;
            int i = index;
            while (n == null && i > 0) {
                n = t[--i];
            }
            index = i;
            next = n;
            return current = e;
        }

        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            Object k = current.key;
            current = null;
            LongHashMap.this.removeEntryForKey(k);
            expectedModCount = modCount;
        }
    }

    private class KeyIterator extends HashIterator<K> {

        public K next() {
            return nextEntry().getKey();
        }
    }

    private class EntryIterator extends HashIterator<LongHashMap.Entry<K>> {

        public LongHashMap.Entry<K> next() {
            return nextEntry();
        }
    }

    // Subclass overrides these to alter behavior of views' iterator() method
    Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }

    Iterator<LongHashMap.Entry<K>> newEntryIterator() {
        return new EntryIterator();
    }
    // Views
    private transient Set<LongHashMap.Entry<K>> entrySet = null;
    /**
     * Each of these fields are initialized to contain an instance of the
     * appropriate view the first time this view is requested.  The views are
     * stateless, so there's no reason to create more than one of each.
     */
    transient volatile Set<K> keySet = null;

    /**
     * Returns a set view of the keys contained in this map.  The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa.  The set supports element removal, which removes the
     * corresponding mapping from this map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt>, and
     * <tt>clear</tt> operations.  It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this map.
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null ? ks : (keySet = new KeySet()));
    }

    private class KeySet extends AbstractSet<K> {

        public Iterator<K> iterator() {
            return newKeyIterator();
        }

        public int size() {
            return size;
        }

        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return LongHashMap.this.removeEntryForKey(o) != null;
        }

        @Override
        public void clear() {
            LongHashMap.this.clear();
        }
    }

    /**
     * Returns a collection view of the mappings contained in this map.  Each
     * element in the returned collection is a <tt>Map.Entry</tt>.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  The collection supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
     * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a collection view of the mappings contained in this map.
     * @see Map.Entry
     */
    public Set<LongHashMap.Entry<K>> entrySet() {
        Set<LongHashMap.Entry<K>> es = entrySet;
        return (es != null ? es : (entrySet = new EntrySet()));
    }

    private class EntrySet extends AbstractSet<LongHashMap.Entry<K>> {

        public Iterator<LongHashMap.Entry<K>> iterator() {
            return newEntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            LongHashMap.Entry<K> e = (LongHashMap.Entry<K>) o;
            Entry<K> candidate = getEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        @Override
        public boolean remove(Object o) {
            return removeMapping(o) != null;
        }

        public int size() {
            return size;
        }

        @Override
        public void clear() {
            LongHashMap.this.clear();
        }
    }


    // These methods are used when serializing HashSets
    int capacity() {
        return table.length;
    }

    float loadFactor() {
        return loadFactor;
    }
}
