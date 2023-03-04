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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.debugger.jpda.util.WeakCacheMap.KeyedValue;

/**
 * A weak cache of objects which reference the key.
 * The key is not stored at all, we use the hash code instead. The values must
 * be able to provide the key for verification, as multiple keys can provide
 * identical has codes. <p>
 * Neither key nor value can be <code>null</code> and keys must not change their
 * hash codes over time. <p>
 * This map is not synchronized.
 * 
 * @author Martin Entlicher
 */
public final class WeakCacheMap<K, V extends KeyedValue<K>> extends AbstractMap<K, V> {
    
    private final Map<Integer, List<Reference<V>>> cache = new HashMap<>();

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        for (List<Reference<V>> values : cache.values()) {
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (value.equals(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        Integer hash = key.hashCode();
        List<Reference<V>> values = cache.get(hash);
        if (values != null) {
            V retv = null;
            List<Reference<V>> staledValues = null;
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (v == null) {
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                } else if (retv == null && key.equals(v.getKey())) {
                    retv = v;
                }
            }
            if (staledValues != null) {
                values.removeAll(staledValues);
            }
            return retv;
        } else {
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        Integer hash = key.hashCode();
        List<Reference<V>> values = cache.get(hash);
        V existingv = null;
        if (values == null) {
            values = new LinkedList<>();
            cache.put(hash, values);
        } else {
            List<Reference<V>> staledValues = null;
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (v == null) {
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                } else if (existingv == null && key.equals(v.getKey())) {
                    existingv = v;
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                }
            }
            if (staledValues != null) {
                values.removeAll(staledValues);
            }
        }
        values.add(new WeakReference<>(value));
        return existingv;
    }

    @Override
    public V remove(Object key) {
        Integer hash = key.hashCode();
        List<Reference<V>> values = cache.get(hash);
        if (values != null) {
            V retv = null;
            List<Reference<V>> staledValues = null;
            for (Reference<V> rv : values) {
                V v = rv.get();
                if (v == null || retv == null && key.equals(v.getKey())) {
                    if (staledValues == null) {
                        staledValues = new LinkedList<>();
                    }
                    staledValues.add(rv);
                }
            }
            if (staledValues != null) {
                values.removeAll(staledValues);
            }
            return retv;
        } else {
            return null;
        }
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static interface KeyedValue<K> {
        K getKey();
    }
    
}
