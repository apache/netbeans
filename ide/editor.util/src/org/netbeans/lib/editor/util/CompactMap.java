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

import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Map implementation that allows to define the class that implements
 * the <code>Map.Entry</code>.
 * <br>
 * The present implementation does not allow <code>null</code> to be used
 * as a key in the map. The client may use NULL_KEY masking on its own side
 * if it wants to use null keys (see <code>java.util.HashMap</code> impl).
 * <br>
 * The load factor is fixed to <code>1.0</code> but if the approximate map size
 * is known there is a constructor that allows to pass the initial capacity.
 * <br>
 * There are no additional attempts to generally improve hashing for poor hashing functions
 * like those in <code>HashMap.hash()</code>.
 * <br>
 * The iterators produced by this map are not fail-fast - they will continue iteration
 * and their behavior is generally undefined after the modification.
 * The caller should ensure that there will be no pending iterators during modification
 * of this map.
 * <br>
 * When iterating through entries in a bucket the <code>Object.equals()</code>
 * is used for comparison.
 * <br>
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class CompactMap<K,V> implements Map<K,V> {
    
    // Empty array would fail with present impl
    private MapEntry<K,V>[] table;
    
    private int size;

    public CompactMap() {
        table = allocateTableArray(1);
    }

    public CompactMap(int initialCapacity) {
        // Find a power of 2 >= initialCapacity
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }
        table = allocateTableArray(capacity);
    }
    
    public V get(Object key) {
        MapEntry<K,V> e = findEntry(key);
        return (e != null) ? e.getValue() : null;
    }
    
    /**
     * Get an entry from a bucket that corresponds to the given hash code.
     * <br>
     * This may be useful if the hash code can be computed without creating
     * an actual object being stored in the map. The target object should
     * provide some method for comparing itself to the passed arguments that represent
     * the contained data.
     * 
     * @param hashCode computed hash code.
     * @return first entry in the particular bucket or null if there are no entries
     *  for the given hash code. The caller may need to iterate through the linked
     *  list of the entries to find the right entry.
     */
    public MapEntry<K,V> getFirstEntry(int hashCode) {
        return table[hashCode & (table.length - 1)];
    }

    public boolean containsKey(Object key) {
        MapEntry<K,V> e = findEntry(key);
        return (e != null);
    }
    
    public boolean containsValue(Object value) {
        for (int i = table.length - 1; i >= 0 ; i--) {
            for (MapEntry<K,V> e = table[i]; e != null; e = e.nextMapEntry()) {
                if ((value == null && e.getValue() == null)
                    || (value != null && value.equals(e.getValue()))
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Put the given entry into the map.
     * <br>
     * The given entry should only be added to one compact map instance.
     * <br>
     * Adding a single entry into multiple compact maps will break
     * internal consistency of all the intended maps!
     * <br>
     * If there will be an existing entry with a key that equals to the key
     * in the entry parameter then the original entry will be replaced
     * by the given entry.
     */
    public MapEntry<K,V> putEntry(MapEntry<K,V> entry) {
        Object key = entry.getKey();
        int hash = key.hashCode();
        int tableIndex = hash & (table.length - 1);
        entry.setKeyHashCode(hash);
        MapEntry<K,V> e = table[tableIndex];
        MapEntry<K,V> prevEntry = null;
        while (e != null) {
            if (e == entry) { // Entry already added => do nothing
                return entry;
            }
            if (hash == e.keyHashCode() && (key == e.getKey() || key.equals(e.getKey()))) {
                // Found the entry -> replace it
                if (prevEntry == null) {
                    table[tableIndex] = entry;
                } else {
                    prevEntry.setNextMapEntry(entry);
                }
                entry.setNextMapEntry(e.nextMapEntry());
                e.setNextMapEntry(null);
                return e;
            }
            prevEntry = e;
            e = e.nextMapEntry();
        }
        
        // Not found in present table => add the entry
        addEntry(entry, tableIndex);
        return null; // nothing replaced
    }

    public V put(K key, V value) {
        int hash = key.hashCode();
        int tableIndex = hash & (table.length - 1);
        MapEntry<K,V> e = table[tableIndex];
        while (e != null) {
            if (hash == e.keyHashCode() && (key == e.getKey() || key.equals(e.getKey()))) {
                // Found the entry
                V oldValue = e.getValue();
                e.setValue(value);
                return oldValue;
            }
            e = e.nextMapEntry();
        }
        
        // Not found in present table => add the entry
        e = new DefaultMapEntry<K,V>(key);
        e.setValue(value);
        e.setKeyHashCode(hash);
        addEntry(e, tableIndex);
        return null;
    }

    public void putAll(Map<? extends K,? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public V remove(Object key) {
        MapEntry<K,V> e = removeEntryForKey(key);
        return (e != null) ? e.getValue() : null;
    }
    
    /**
     * Remove the given entry from the map.
     * <br>
     * This method will search for the entry instance (not its key)
     * so the given entry must be physically used in the map
     * otherwise this method will not do anything.
     */
    public MapEntry<K,V> removeEntry(MapEntry<K,V> entry) {
        int hash = entry.keyHashCode();
        int tableIndex = hash & (table.length - 1);
        MapEntry<K,V> e = table[tableIndex];
        MapEntry<K,V> prev = null;
        while (e != null) {
            if (e == entry) {
                if (prev == null) {
                    table[tableIndex] = e.nextMapEntry();
                } else {
                    prev.setNextMapEntry(e.nextMapEntry());
                }
                entry.setNextMapEntry(null);
                size--;
                return entry;
            }
            prev = entry;
            entry = entry.nextMapEntry();
        }
        return null;
    }

    public void clear() {
        // Retain present table array
        for (int i = table.length - 1; i >= 0; i--) {
            MapEntry<K,V> e = table[i];
            table[i] = null;
            // Unbind entries
            while (e != null) {
                MapEntry<K,V> next = e.nextMapEntry();
                e.setNextMapEntry(null);
                e = next;
            }
        }
        size = 0;
    }

    public final int size() {
        return size;
    }

    public boolean isEmpty() {
        return (size() == 0);
    }

    public Set<Entry<K,V>> entrySet() {
        return new EntrySet();
    }
    
    public Collection<V> values() {
        throw new IllegalStateException("Not yet implemented");
    }

    public Set<K> keySet() {
        throw new IllegalStateException("Not yet implemented");
    }

    private MapEntry<K,V> findEntry(Object key) {
        int hash = key.hashCode();
        int tableIndex = hash & (table.length - 1);
        MapEntry<K,V> e = table[tableIndex];
        while (e != null) {
            if (hash == e.keyHashCode() && (key == e.getKey() || key.equals(e.getKey()))) {
                return e;
            }
            e = e.nextMapEntry();
        }
        return null;
    }
    
    private void addEntry(MapEntry<K,V> entry, int tableIndex) {
        entry.setNextMapEntry(table[tableIndex]);
        table[tableIndex] = entry;
        size++;
        if (size > table.length) { // Fill factor is 1.0
            MapEntry<K,V>[] newTable = allocateTableArray(Math.max(table.length << 1, 4));
            for (int i = table.length - 1; i >= 0; i--) {
                entry = table[i];
                while (entry != null) {
                    MapEntry<K,V> next = entry.nextMapEntry();
                    int newIndex = entry.keyHashCode() & (newTable.length - 1);
                    entry.setNextMapEntry(newTable[newIndex]);
                    newTable[newIndex] = entry;
                    entry = next;
                }
            }
            table = newTable;
        }
    }
    
    private MapEntry<K,V> removeEntryForKey(Object key) {
        int hash = key.hashCode();
        int tableIndex = hash & (table.length - 1);
        MapEntry<K,V> e = table[tableIndex];
        MapEntry<K,V> prev = null;
        while (e != null) {
            if (hash == e.keyHashCode() && (key == e.getKey() || key.equals(e.getKey()))) {
                if (prev == null) {
                    table[tableIndex] = e.nextMapEntry();
                } else {
                    prev.setNextMapEntry(e.nextMapEntry());
                }
                e.setNextMapEntry(null);
                size--;
                return e;
            }
            prev = e;
            e = e.nextMapEntry();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private MapEntry<K,V>[] allocateTableArray(int capacity) {
        return new MapEntry[capacity];
    }
    
    @Override
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("{");

	Iterator i = entrySet().iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
	    Map.Entry e = (Map.Entry)i.next();
	    Object key = e.getKey();
            Object value = e.getValue();
	    if (key == this)
		buf.append("(this Map)");
	    else
		buf.append(key);
	    buf.append("=");
	    if (value == this)
		buf.append("(this Map)");
	    else
		buf.append(value);
            hasNext = i.hasNext();
            if (hasNext)
                buf.append(", ");
        }

	buf.append("}");
	return buf.toString();
    }

    /**
     * Abstract implementation of the map entry.
     * <br>
     * This is suitable for the cases when e.g. the value is a primitive
     * data type.
     */
    public abstract static class MapEntry<K,V> implements Map.Entry<K,V> {
        
        private MapEntry<K,V> nextMapEntry; // 12 bytes (8-Object + 4)
        
        private int keyHashCode; // 16 bytes
        
        public abstract K getKey();
        
        public abstract V getValue();
        
        public abstract V setValue(V value);
        
        /**
         * Used by {@link #hashCode()} to return the real hashCode for the value
         * held by this map entry.
         * <br>
         * <code>getValue().hashCode</code> cannot be used in general because
         * <code>getValue()</code> may return <code>this</code> e.g. in case
         * the value is represented by primitive data type. The method should
         * use that real value for the hash code computation.
         */
        protected abstract int valueHashCode();
        
        /**
         * Used by {@link #equals(Object)} to check whether the value
         * held by this map entry equals to the the given value.
         * <br>
         * <code>getValue().equals()</code> cannot be used in general because
         * <code>getValue()</code> may return <code>this</code> e.g. in case
         * the value is represented by primitive data type. The method should
         * use that real value for the operation of this method.
         * 
         * @param value2 value to be compared with value stored in this entry.
         *  The argument may be null.
         */
        protected abstract boolean valueEquals(Object value2);
        
        
        /**
         * Get next map entry linked to this one.
         * <br>
         * This method may be useful after using {@link #getFirstEntry(int)}.
         */
        public final MapEntry<K,V> nextMapEntry() {
            return nextMapEntry;
        }
        
        final void setNextMapEntry(MapEntry<K,V> next) {
            this.nextMapEntry = next;
        }
        
        /**
         * Get stored hash code of the key contained in this entry.
         * <br>
         * This method may be useful after using {@link #getFirstEntry(int)}
         * to quickly exclude entries which hash code differs from the requested one.
         */
        public final int keyHashCode() {
            return keyHashCode;
        }
        
        final void setKeyHashCode(int keyHashCode) {
            this.keyHashCode = keyHashCode;
        }
    
        /**
         * Implementation that adheres to {@link java.util.Map.Entry#hashCode()} contract.
         */
        @Override
        public final int hashCode() {
            // keyHashCode() cannot be used always as the entry is possibly not yet contained in a map
            int keyHash = (keyHashCode != 0) ? keyHashCode : getKey().hashCode();
            return keyHash ^ valueHashCode();
        }
        
        /**
         * Implementation that adheres to {@link java.util.Map.Entry#equals(Object)} contract.
         */
        @Override
        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry e = (Map.Entry)o;
                K key = getKey();
                Object key2 = e.getKey();
                // Note: update needed if this map would allow for null keys
                if (key == key2 || key.equals(key2)) {
                    return valueEquals(e.getValue());
                }
            }
            return false;
        }
        
    }
    
    /**
     * Default implementation of the map entry similar to e.g. the <code>HashMap</code>.
     * <br>
     * It may be extended as well if the should be an additional information stored
     * in the map entry.
     */
    public static class DefaultMapEntry<K,V> extends MapEntry<K,V> {
        
        private K key; // 20 bytes
        
        private V value; // 24 bytes
        
        public DefaultMapEntry(K key) {
            this.key = key;
        }
        
        public final K getKey() {
            return key;
        }
        
        public final V getValue() {
            return value;
        }
        
        public final V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
        protected final int valueHashCode() {
            return (value != null) ? value.hashCode() : 0;
        }
        
        protected final boolean valueEquals(Object value2) {
            return (value == value2 || (value != null && value.equals(value2)));
        }
        
        @Override
        public String toString() {
            return "key=" + getKey() + ", value=" + getValue(); // NOI18N
        }
        
    }

    private final class EntrySet extends AbstractSet<Entry<K,V>> {

        public Iterator<Entry<K,V>> iterator() {
            return new EntryIterator();
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            @SuppressWarnings("unchecked")
            Map.Entry<K,V> e = (Map.Entry<K,V>)o;
            MapEntry<K,V> candidate = findEntry(e.getKey());
            return candidate != null && candidate.equals(e);
        }

        @Override
        public boolean remove(Object o) {
            @SuppressWarnings("unchecked")
            MapEntry<K,V> e = (MapEntry<K,V>)o;
            return removeEntry(e) != null;
        }

        public int size() {
            return CompactMap.this.size();
        }

        public void clear() {
            CompactMap.this.clear();
        }

    }
    
    private abstract class HashIterator {

        MapEntry<K,V> next;       // next entry to return
        int index;                   // current slot
        MapEntry<K,V> current;    // current entry
        
        HashIterator() {
            MapEntry<K,V>[] t = table;
            int i = t.length;
            MapEntry<K,V> n = null;
            if (size != 0) { // advance to first entry
                while (i > 0 && (n = t[--i]) == null)
                    ;
            }
            next = n;
            index = i;
        }
        
        public boolean hasNext() {
            return next != null;
        }
        
        MapEntry<K,V> nextEntry() {
            MapEntry<K,V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            
            MapEntry<K,V> n = e.nextMapEntry();
            MapEntry<K,V>[] t = table;
            int i = index;
            while (n == null && i > 0)
                n = t[--i];
            index = i;
            next = n;
            return current = e;
        }
        
        public void remove() {
            if (current == null)
                throw new IllegalStateException();
            Object k = current.getKey();
            current = null;
            removeEntryForKey(k);
        }
        
    }
    
    private final class ValueIterator extends HashIterator implements Iterator<V> {

        public V next() {
            return nextEntry().getValue();
        }
    }
    
    private final class KeyIterator extends HashIterator implements Iterator<K> {

        public K next() {
            return nextEntry().getKey();
        }

    }
    
    private final class EntryIterator extends HashIterator implements Iterator<Entry<K,V>> {

        public Entry<K,V> next() {
            return nextEntry();
        }

    }

}
