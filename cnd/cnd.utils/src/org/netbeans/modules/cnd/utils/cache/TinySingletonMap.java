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

package org.netbeans.modules.cnd.utils.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * map for one entry set (only two fields to reduce memory) - 16 bytes on 32bit system.
 */
final class TinySingletonMap<K, V> implements Map<K, V>, TinyMaps.CompactMap<K, V> {

    private K key;
    private V value;

    public TinySingletonMap() {
    }

    public TinySingletonMap(K key, V value) {
        assert key != null;
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
    
    @Override
    public int size() {
        if (key == null) {
            return 0;
        }
        return 1;
    }

    @Override
    public boolean isEmpty() {
        if (key == null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean containsKey(Object aKey) {
        if (key == null) {
            return false;
        }
        return key.equals(aKey);
    }

    @Override
    public boolean containsValue(Object aValue) {
        return value != null && value.equals(aValue);
    }

    @Override
    public V get(Object aKey) {
        if (key != null && key.equals(aKey)) {
            return value;
        }
        return null;
    }

    @Override
    public V put(K aKey, V aValue) {
        assert aKey != null;
        if (key == null) {
            key = aKey;
            value = aValue;
            return null;
        } else if (key.equals(aKey)) {
            V out = value;
            value = aValue;
            return out;
        }
        // only one element is supported in this map
        throw new IllegalStateException();
    }

    @Override
    public V remove(Object aKey) {
        if (key == null) {
            return null;
        }
        if (key.equals(aKey)) {
            V res = value;
            key = null;
            value = null;
            return res;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    @Override
    public void clear() {
        key = null;
        value = null;
    }

    @Override
    public Set<K> keySet() {
        if (key == null) {
            return Collections.<K>emptySet();
        } else {
            return Collections.<K>singleton(key);
        }
    }

    @Override
    public Collection<V> values() {
        if (key == null) {
            return Collections.<V>emptyList();
        } else {
            return Collections.<V>singletonList(value);
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (key == null) {
            return Collections.<Entry<K, V>>emptySet();
        } else {
            return new Set<Entry<K, V>>() {
                @Override
                public int size() {
                    return 1;
                }
                @Override
                public boolean isEmpty() {
                    return false;
                }
                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return new Iterator<Entry<K, V>>(){
                        private boolean last = false;
                        @Override
                        public boolean hasNext() {
                            return !last;
                        }
                        @Override
                        public Entry<K, V> next() {
                            if (!last) {
                                last = true;
                                return new Entry<K, V>(){
                                    @Override
                                    public K getKey() {
                                        return key;
                                    }
                                    @Override
                                    public V getValue() {
                                        return value;
                                    }
                                    @Override
                                    public V setValue(V value) {
                                        V res = TinySingletonMap.this.value;
                                        TinySingletonMap.this.value = value;
                                        return res;
                                    }
                                };
                            }
                            return null;
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                        }
                    };
                }
                @Override
                public boolean contains(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public Object[] toArray() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public <T> T[] toArray(T[] a) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean add(Entry<K, V> o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean containsAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean addAll(Collection<? extends Entry<K, V>> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean retainAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public boolean removeAll(Collection<?> c) {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
                @Override
                public void clear() {
                    throw new UnsupportedOperationException("Not supported yet."); // NOI18N
                }
            };
        }
    }

    @Override
    public Map<K, V> expandForNextKeyIfNeeded(K newElem) {
        if (key == null || key.equals(newElem)) {
            return this;
        }
        return new TinyTwoValuesMap<K, V>(this);
    }
}
