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
package org.netbeans.modules.php.blade.editor.cache;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author bogdan
 */
public final class QueryCache<K, V> {

    public static final Long DEFAULT_CACHE_TIMEOUT = 60000L;

    private Map<K, CacheValue<V>> cacheMap;
    private Long cacheTimeout;
    
    //private static final Map<QuerySupport, QueryCache> QUERY_SUPPORT_INDEX = new WeakHashMap<>();

    public QueryCache() {
        this(DEFAULT_CACHE_TIMEOUT);
    }

    public QueryCache(Long cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
        this.clear();
    }

    public void clean() {
        for (K key : this.getExpiredKeys()) {
            this.remove(key);
        }
    }

    public boolean containsKey(K key) {
        return this.cacheMap.containsKey(key);
    }

    protected Set<K> getExpiredKeys() {
        return this.cacheMap.keySet().stream()
                .filter(this::isExpired)
                .collect(Collectors.toSet());
    }

    protected boolean isExpired(K key) {
        LocalDateTime expirationDateTime = this.cacheMap.get(key).getCreatedAt().plus(this.cacheTimeout, ChronoUnit.MILLIS);
        return LocalDateTime.now().isAfter(expirationDateTime);
    }

    public void clear() {
        this.cacheMap = new HashMap<>();
    }

    public Optional<V> get(K key) {
        this.clean();
        return Optional.ofNullable(this.cacheMap.get(key)).map(CacheValue::getValue);
    }

    public void put(K key, V value) {
        this.cacheMap.put(key, this.createCacheValue(value));
    }

    protected CacheValue<V> createCacheValue(V value) {
        LocalDateTime now = LocalDateTime.now();
        return new CacheValue<V>() {
            @Override
            public V getValue() {
                return value;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return now;
            }
        };
    }

    public void remove(K key) {
        this.cacheMap.remove(key);
    }

    protected interface CacheValue<V> {

        V getValue();

        LocalDateTime getCreatedAt();
    }

}
