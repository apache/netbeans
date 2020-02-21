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
package org.netbeans.modules.cnd.repository.disk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.modules.cnd.repository.api.RepositoryExceptions;

/**
 * The cache that maps K to V and stores the most frequently used pairs.
 *
 * The class does NOT perform any synchronization! It's a caller responsibility
 * to syncronize access to this class (Because as a rule, caller has to
 * synchronize in any case - for example, when it tries getting cache element,
 * gets null, then creates new value and puts it into cache)
 *
 */
public class RepositoryCacheMap<K, V> {

    private final TreeMap<K, RepositoryCacheValue<V>> keyToValue;
    private final TreeMap<RepositoryCacheValue<V>, K> valueToKey;
    private final int capacity;
    private static final int DEFAULT_CAPACITY = 20;
    private static AtomicInteger currentBornStamp = new AtomicInteger(0);
    private static final boolean ASSERTIONS = Boolean.getBoolean("cnd.repository.cache.map.assert");

    static private final class RepositoryCacheValue<V> implements Comparable<RepositoryCacheValue<V>> {

        public V value;
        public AtomicInteger frequency;
        public AtomicBoolean newBorn;
        public final int bornStamp;

        RepositoryCacheValue(final V value) {
            frequency = new AtomicInteger(1);
            newBorn = new AtomicBoolean(true);
            bornStamp = currentBornStamp.incrementAndGet();
            this.value = value;
        }

        private int compareAdults(final RepositoryCacheValue<V> elemToCompare) {
            int ownValue = frequency.intValue();
            int objValue = elemToCompare.frequency.intValue();

            if (ownValue < objValue) {
                return -1;
            } else if (ownValue == objValue) {
                ownValue = bornStamp;
                objValue = elemToCompare.bornStamp;

                if (ownValue < objValue) {
                    return -1;
                } else if (ownValue > objValue) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        }

        private int compareNewBorns(final RepositoryCacheValue<V> elemToCompare) {
            final int ownValue = bornStamp;
            final int objValue = elemToCompare.bornStamp;

            if (ownValue < objValue) {
                return -1;
            } else if (ownValue > objValue) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public int compareTo(final RepositoryCacheValue<V> elemToCompare) {
            final boolean ownChildhood = newBorn.get();
            final boolean objChildhood = elemToCompare.newBorn.get();

            if (ownChildhood && objChildhood) {
                return compareNewBorns(elemToCompare);
            } else if (ownChildhood && !objChildhood) {
                return 1;
            } else if (!ownChildhood && objChildhood) {
                return -1;
            } else {
                return compareAdults(elemToCompare);
            }
        }

        @Override
        public String toString() {
            return "RepositoryCacheValue {" + value + ", frq=" + frequency + ", nwBrn=" + newBorn + ", stmp=" + bornStamp + '}'; // NOI18N
        }
    }

    /**
     * Creates a new instance of RepositoryCacheMap
     */
    public RepositoryCacheMap(final int capacity) {
        keyToValue = new TreeMap<K, RepositoryCacheValue<V>>();
        valueToKey = new TreeMap<RepositoryCacheValue<V>, K>();
        this.capacity = (capacity > 0) ? capacity : DEFAULT_CAPACITY;
    }

    public V get(final K key) {
        RepositoryCacheValue<V> value = keyToValue.get(key);
        if (value != null) {
            valueToKey.remove(value);
            value.frequency.incrementAndGet();
            value.newBorn.set(false);
            valueToKey.put(value, key);
            return value.value;
        }
        return null;
    }

    public V put(K key, V value) {
        RepositoryCacheValue<V> entry = new RepositoryCacheValue<V>(value);
        try {
            softAssert(!(keyToValue.size() < valueToKey.size()), "valueToKeyStorage contains more elements than keyToValueStorage key=", key); //NOI18N
            softAssert(!(keyToValue.size() > valueToKey.size()), "keyToValueStorage contains more elements than valueToKeyStorage", null); //NOI18N

            if (keyToValue.size() < capacity) {
                RepositoryCacheValue<V> oldValue = keyToValue.put(key, entry);
                softAssert(oldValue == null, "Value replacement in RepositoryCacheMap key=", key); //NOI18N
                valueToKey.put(entry, key);
            } else {
                RepositoryCacheValue<V> minValue = valueToKey.firstKey();
                K minKey = valueToKey.get(minValue);

                keyToValue.remove(minKey);
                valueToKey.remove(minValue);

                RepositoryCacheValue<V> oldValue = keyToValue.put(key, entry);
                softAssert(oldValue == null, "Value replacement in RepositoryCacheMap key=", key); //NOI18N
                valueToKey.put(entry, key);

                return minValue.value;
            }
        } catch (NoSuchElementException e) {
            RepositoryExceptions.throwException(this, e);
        }
        return null;
    }

    public V remove(K key) {
        RepositoryCacheValue<V> entry = keyToValue.remove(key);
        if (entry != null) {
            valueToKey.remove(entry);
            return entry.value;
        }
        return null;
    }

    public Collection<V> remove(Filter<V> filter) {
        Collection<V> retSet = new ArrayList<V>(size());
        List<RepositoryCacheValue<V>> entriesToRemove = new ArrayList<RepositoryCacheValue<V>>(DEFAULT_CAPACITY);
        for (RepositoryCacheValue<V> entry : valueToKey.keySet()) {
            if (filter.accept(entry.value)) {
                retSet.add(entry.value);
                entriesToRemove.add(entry);
            }
        }
        for (RepositoryCacheValue<V> entry : entriesToRemove) {
            K removedKey = valueToKey.get(entry);
            valueToKey.remove(entry);
            keyToValue.remove(removedKey);
        }
        return retSet;
    }

    public int size() {
        return keyToValue.size();
    }

    public Collection<V> values() {
        Collection<V> retSet = new ArrayList<V>(size());
        for (RepositoryCacheValue<V> entry : valueToKey.keySet()) {
            retSet.add(entry.value);
        }
        return retSet;
    }

    public Collection<K> keys() {
        Collection<K> retSet = new ArrayList<K>(size());
        for (K key : keyToValue.keySet()) {
            retSet.add(key);
        }
        return retSet;
    }

    private void softAssert(boolean condition, String message, K key) {
        if (ASSERTIONS && !condition) {
            Exception ex = new Exception();
            StackTraceElement[] trace = ex.getStackTrace();
            if (key == null) {
                System.err.println(message);
            } else {
                System.err.println(message+key);
            }
            for (int i = 1; i < trace.length; i++) {
                System.err.println("\tat " + trace[i]); //NOI18N
            }
        }
    }

    public static interface Filter<V> {

        boolean accept(V value);
    }
}
