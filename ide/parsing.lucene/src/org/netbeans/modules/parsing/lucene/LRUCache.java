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

package org.netbeans.modules.parsing.lucene;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.api.annotations.common.NonNull;

/**
 *
 * @author Tomas Zezula
 */
final class LRUCache<K,V extends Evictable> {

    private final LinkedHashMap<K, V> cache;
    private final ReadWriteLock lock;

    public LRUCache (final EvictionPolicy<? super K,? super V> policy) {
        this.lock = new ReentrantReadWriteLock();
        this.cache = new LinkedHashMap<K, V>(10,0.75f,true) {
            @Override
            protected boolean removeEldestEntry(Entry<K, V> eldest) {
                final boolean evict = policy.shouldEvict(this.size(), eldest.getKey(), eldest.getValue());
                if (evict) {
                    eldest.getValue().evicted();
                }
                return evict;
            }
        };
    }

    public void put (final K key, final V evictable) {
        assert key != null;
        assert evictable != null;
        this.lock.writeLock().lock();
        try {
            this.cache.put(key, evictable);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public V get(final K key) {
        assert key != null;
        this.lock.readLock().lock();
        try {
            return this.cache.get(key);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public V remove (final K key) {
        assert key != null;
        this.lock.writeLock().lock();
        try {
            return this.cache.remove(key);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @NonNull
    public Collection<? extends V> clear() {
        final Collection<V> res = new ArrayDeque<>();
        this.lock.writeLock().lock();
        try {
            for (Iterator<Entry<K, V>> it = this.cache.entrySet().iterator(); it.hasNext();) {
                Map.Entry<K,V> e = it.next();
                res.add(e.getValue());
                it.remove();
            }
        } finally {
            this.lock.writeLock().unlock();
        }
        return res;
    }

    @Override
    public String toString () {
        this.lock.readLock().lock();
        try {
            return this.cache.toString();
        } finally {
            this.lock.readLock().unlock();
        }
    }

}
