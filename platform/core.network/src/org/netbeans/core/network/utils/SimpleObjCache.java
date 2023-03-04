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
package org.netbeans.core.network.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple cache with a max capacity. Encapsulates {@link java.util.Map Map}.
 *
 * <p>
 * Eviction is based on usage. Every time a value is retrieved from the cache
 * its 'usage timestamp' will change. When the cache is full, the value with the
 * oldest usage timestamp will be evicted.
 *
 * <p>
 * This class is thread-safe and is very efficient for read operations
 * ({@code get}). The write operation ({@code put}) will - if the cache is full
 * - have performance which degrades linearly with the size of the cache as all
 * elements will have to be inspected to find the eviction candidate. For this
 * reason, the class is best suited for smaller caches (say less than 1000
 * elements).
 *
 *
 * @author lbruun
 * 
 * @param <K> key 
 * @param <V> value
 */
public class SimpleObjCache<K,V> {

    private final int maxSize;
    private final ConcurrentHashMap<K, ValueWrapper> map;
    
    /**
     * Constructs a new cache with {@code maxSize} capacity. When the 
     * cache is full, the value used/created the furthest in the past will
     * be evicted to make room for a new element.
     * 
     * @param maxSize capacity - any value larger than zero
     */
    public SimpleObjCache(int maxSize) {
        
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Cache max size cannot be zero or less");
        }
        this.maxSize = maxSize;
        this.map = new ConcurrentHashMap<>();
    }
    
    /**
     * Puts a value into the cache mapped to the specified key.
     * 
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with key, or null if there 
     *         was no mapping for the key
     */
     public V put(K key, V value) {
        evictIfFull();
        ValueWrapper existingValue = map.put(key, new ValueWrapper(value));
        if (existingValue != null) {
            return existingValue.value;
        } else {
            return null;
        }
    }
    
    /**
     * Returns the value to which the specified key is mapped, or {@code null}
     * if this cache contains no mapping for the key.
     * 
     * @param key the key whose associated value is to be returned
     * @return  the value to which the specified key is mapped, or {@code null}
     *          if this map contains no mapping for the key
     */
    public V get(K key) {
        ValueWrapper wrapper = map.get(key);
        if (wrapper != null) {
            wrapper.lastUsed.set(System.currentTimeMillis());
            return wrapper.value;
        }
        return null;
    }
    
    /**
     * Gets the current number of elements in the cache.
     * @return 
     */
    public int getCacheSize() {
        return map.size();
    }
    
    /**
     * Removes all elements from the cache. There is little reason to use
     * this method as the cache will do its own house keeping.
     */
    public void clear() {
        map.clear();
    }
    
    private synchronized void evictIfFull() {
        if (map.size() >= maxSize) {
            K toBeEvicted = findEvictionCandidate();
            if (toBeEvicted != null) {
                map.remove(toBeEvicted);
            }
        }
    }
    
    private K findEvictionCandidate() {
        // Finds minimum of all timestamps of all elements in the cache.
        
        Optional<Map.Entry<K, ValueWrapper>> minEntry = map.entrySet().stream()
                .min(Comparator.comparingLong( e -> e.getValue().lastUsed.get()));
        
        // Did we actually find something to delete?  
        // Theoretically this should always be so, but we are cautious
        if (minEntry.isPresent()) {  
            return minEntry.get().getKey();
        } else {
            return null;
        }
    }
    
    private class ValueWrapper {
        private final AtomicLong lastUsed = new AtomicLong(System.currentTimeMillis());
        private final V value;

        public ValueWrapper(V value) {
            this.value = value;
        }
    }
}
