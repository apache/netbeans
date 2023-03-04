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
package org.netbeans.modules.debugger.jpda.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openide.util.BaseUtilities;

/**
 * A weak hash map, that automatically release entries as soon as they are freed by GC.
 * 
 * @author Martin Entlicher
 */
// TODO: Make it a public API. There's another copy of this class in debugger.jpda.projects module.
public final class WeakHashMapActive<K,V> extends AbstractMap<K,V> {
    
    private final ReferenceQueue<Object> queue;
    private final Map<KeyReference<K>, V> map;
    
    public WeakHashMapActive() {
        super();
        map = new HashMap<>();
        queue = BaseUtilities.activeReferenceQueue();
    }
    
    @Override
    public V put(K key, V value) {
        KeyReference<K> rk = new KeyReference<>(key, queue);
        synchronized (map) {
            return map.put(rk, value);
        }
    }

    @Override
    public V get(Object key) {
        KeyReference<Object> rk = new KeyReference<>(key, null);
        synchronized (map) {
            return map.get(rk);
        }
    }

    @Override
    public V remove(Object key) {
        KeyReference<Object> rk = new KeyReference<>(key, null);
        synchronized (map) {
            return map.remove(rk);
        }
    }
    
    @Override
    public void clear() {
        synchronized (map) {
            map.clear();
        }
    }

    @Override
    public int size() {
        synchronized (map) {
            return map.size();
        }
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    private class KeyReference<K> extends WeakReference<K> implements Runnable {
        
        private final int hash;
        
        KeyReference(K r, ReferenceQueue<? super K> queue) {
            super(r, queue);
            hash = r.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyReference)) {
                return false;
            }
            KeyReference kr = (KeyReference) obj;
            K k1 = get();
            Object k2 = kr.get();
            if (k1 == null && k2 == null) {
                return hash == kr.hash;
            }
            return (k1 == k2 || (k1 != null && k1.equals(k2)));
        }

        @Override
        public void run() {
            // Collected
            synchronized (map) {
                map.remove(this);
            }
        }
        
    }
    
}
