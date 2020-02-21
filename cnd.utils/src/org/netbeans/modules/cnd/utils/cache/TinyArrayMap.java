/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.utils.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 */
class TinyArrayMap<K, V> implements Map<K, V> {

    // key is even, followed by odd value
    private final Object[] keyValues;
    private int size;

    public TinyArrayMap(int capacity) {
        keyValues = new Object[capacity*2];
        this.size = 0;
    }

    TinyArrayMap(TinyTwoValuesMap<K, V> twoValues, int capacity) {
        assert capacity >= 2;
        keyValues = new Object[capacity*2];
        this.size = twoValues.size();
        int index = 0;
        this.keyValues[index++] = twoValues.getFirstKey();
        this.keyValues[index++] = twoValues.getFirstValue();
        this.keyValues[index++] = twoValues.getSecondKey();
        this.keyValues[index] = twoValues.getSecondValue();
    }

    // different order of params to simplify issue with ambiguity in derived classes constructors
    TinyArrayMap(TinyArrayMap<K, V> prev, int capacity) {
        assert prev.keyValues.length <= capacity*2;
        keyValues = new Object[capacity*2];
        System.arraycopy(prev.keyValues, 0, keyValues, 0, prev.keyValues.length);
        this.size = prev.size;
    }
    
    TinyArrayMap(int capacity, Map<K, V> map) {
        this.size = map.size();
        assert this.size <= capacity;
        keyValues = new Object[capacity*2];
        int index = 0;
        for (Entry<K, V> entry : map.entrySet()) {
            this.keyValues[index++] = entry.getKey();
            this.keyValues[index++] = entry.getValue();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object aKey) {
        assert aKey != null;
        int index = indexForKey(aKey);
        if (index >= 0) {
            return keyValues[index] != null;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object aValue) {
        assert aValue != null;
        for (int i = 1; i < keyValues.length; i++) {
            // value is odd
            Object val = keyValues[i++];
            if (val != null && val.equals(aValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object aKey) {
        assert aKey != null;
        int index = indexForKey(aKey);
        if (index >= 0) {
            @SuppressWarnings("unchecked")
            V val = (V) keyValues[index+1];
            return val;
        }
        return null;
    }

    @Override
    public V put(K aKey, V aValue) {
        assert aKey != null;
        int index = indexForKey(aKey);
        if (index >= 0) {
            Object key = keyValues[index];
            if (key == null) {
                assert (size+1)*2 <= keyValues.length;
                size++;
                keyValues[index] = aKey;
                assert keyValues[index+1] == null;
            }
            @SuppressWarnings("unchecked")
            V prev = (V) keyValues[index+1];
            keyValues[index+1] = aValue;
            return prev;
        }
        assert size*2 == keyValues.length : "trying to put " + size + " in " + keyValues.length;
        assert false : "this map can not contain more than " + size;
        return null;
    }

    /**
     * returns index where aKey is already exists or a free cell index where key can be put
     * @param aKey
     * @return 
     */
    private int indexForKey(Object aKey) {
        assert aKey != null;
        int freeCell = -1;
        for (int i = 0; i < keyValues.length; i+=2) {
            // key is even, value is odd
            Object key = keyValues[i];
            if (key == null) {
                freeCell = i;
                assert keyValues[i+1] == null;
            } else if (key.equals(aKey)) {
                return i;
            }
        }
        return freeCell;
    }
    @Override
    public V remove(Object aKey) {
        assert aKey != null;
        int index = indexForKey(aKey);
        if (index >= 0) {
            if (keyValues[index] != null) {
                size--;
                keyValues[index] = null;
                @SuppressWarnings("unchecked")
                V prev = (V) keyValues[index + 1];
                keyValues[index + 1] = null;
                return prev;
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> t) {
        for (Entry<? extends K, ? extends V> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.size = 0;
        for (int i = 0; i < keyValues.length; i++) {
            keyValues[i] = null;
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<K>(size);
        for (int i = 0; i < keyValues.length; i+=2) {
            @SuppressWarnings("unchecked")
            K key = (K) keyValues[i];
            if (key != null) {
                keys.add(key);
            }
        }
        return keys;
    }

    @Override
    public Collection<V> values() {
        List<V> values = new ArrayList<V>(size);
        for (int i = 0; i < keyValues.length; i += 2) {
            @SuppressWarnings("unchecked")
            K key = (K) keyValues[i];
            if (key != null) {
                @SuppressWarnings("unchecked")
                V val = (V)keyValues[i+1];
                values.add(val);
            }
        }
        return values;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (size == 0) {
            return Collections.<Map.Entry<K, V>>emptySet();
        } else {
            return new Set<Map.Entry<K, V>>() {
                @Override
                public int size() {
                    return size;
                }
                @Override
                public boolean isEmpty() {
                    return false;
                }
                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return new Iterator<Map.Entry<K, V>>(){
                        private byte index = 0;
                        private byte counter = 0;
                        @Override
                        public boolean hasNext() {
                            return counter < size;
                        }
                        @Override
                        public Map.Entry<K, V> next() {
                            if (counter < size) {
                                for (; index < keyValues.length; index+=2) {
                                    if (keyValues[index] != null) {
                                        break;
                                    }
                                }
                                assert index < keyValues.length-1;
                                final int entryIndex = index;
                                counter++;
                                index+=2;
                                return new Map.Entry<K, V>(){
                                    @Override
                                    @SuppressWarnings("unchecked")
                                    public K getKey() {
                                        return (K) keyValues[entryIndex];
                                    }
                                    @Override
                                    @SuppressWarnings("unchecked")
                                    public V getValue() {
                                        return (V) keyValues[entryIndex+1];
                                    }
                                    @Override
                                    public V setValue(V value) {
                                        @SuppressWarnings("unchecked")
                                        V res = (V) keyValues[entryIndex+1];
                                        keyValues[entryIndex+1] = value;
                                        return res;
                                    }
                                };
                            }
                            throw new NoSuchElementException();
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
                public boolean add(Map.Entry<K, V> o) {
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
                public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
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
}
