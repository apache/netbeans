/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.utils.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.openide.util.CharSequences;

/**
 * Base class for Maps 
 */
public abstract class MapSnapshot<V> implements Iterable<Entry<CharSequence, V>>{
    /**
     * Single empty map instance to share between snapshots
     */
    protected static final Map<CharSequence, Object> EMPTY = Collections.emptyMap();
    
    /**
     * optimize by memory.
     * one of:
     * 1) EMPTY map instance - for empty state
     * 2) Map<CharSequence, V> - if at least one pair is in
     * 3) frozen array has Holder with sorted array: [name1, name2, ..., value1, value1, ...]
     *    keys followed by corresponding value objects. Array is sorted to be comparable by equals
     */
    protected Object storage;

    private final MapSnapshot<V> parent;
    
    protected MapSnapshot(MapSnapshot<V> parent) {
        storage = EMPTY;
        assert (parent == null || parent.parent == null || !parent.parent.isEmpty()) : "how grand father could be empty " + parent;
        // optimization to prevent chaining of empty snapshots
        while (parent != null && parent.isEmpty()) {
            parent = parent.parent;
        }
        this.parent = parent;
        if (this.parent != null) {
            this.parent.freeze();
        }
    }
    
    protected MapSnapshot<V> getParent() {
        return parent;
    }

    public void put(CharSequence key, V value) {
        assert !(storage instanceof Holder) : "frozen snap can not be modified";
        if (storage == EMPTY) {
            storage = TinyMaps.createMap(1);
        } else if (storage instanceof Map<?,?>) {
            @SuppressWarnings("unchecked")
            Map<CharSequence, V> map = (Map<CharSequence, V>)storage;
            // expand map if needed based on expected next key
            storage = TinyMaps.expandForNextKey(map, key);
        }
        assert storage instanceof Map<?,?> : "unexpected class " + storage.getClass();
        @SuppressWarnings("unchecked")
        Map<CharSequence, V> map = (Map<CharSequence, V>)storage;
        map.put(key, value);
    }

    public final V get(CharSequence key) {
        assert CharSequences.isCompact(key) : "string can't be here " + key;
        MapSnapshot currentSnap = this;
        while (currentSnap != null) {
            Object value = currentSnap.getImpl(key);
            if (value != null) {
                return (V)value;
            }
            currentSnap = currentSnap.parent;
        }
        return null;
    }
    
    protected V getImpl(CharSequence key) {
        assert storage instanceof Map<?,?> : "unexpected to have get from frozen" + storage.getClass();
        @SuppressWarnings("unchecked")
        V map = ((Map<CharSequence, V>)storage).get(key);
        return map;
    }
    
    @Override
    public String toString() {
        Map<CharSequence, V> tmpMap = getAll();
        StringBuilder retValue = new StringBuilder();
        retValue.append("VALUES (sorted ").append(tmpMap.size()).append("):\n"); // NOI18N
        List<CharSequence> macrosSorted = new ArrayList<CharSequence>(tmpMap.keySet());
        Collections.sort(macrosSorted, CharSequences.comparator());
        for (CharSequence key : macrosSorted) {
            Object macro = tmpMap.get(key);
            assert(macro != null);
            retValue.append(macro);
            retValue.append("'\n"); // NOI18N
        }
        return retValue.toString();
    }
    
    public Map<CharSequence, V> getAll() {
        LinkedList<MapSnapshot> stack = new LinkedList<MapSnapshot>();
        MapSnapshot<V> snap = this;
        int i = 0;
        while (snap != null) {
            i += snap.size();
            stack.add(snap);
            snap = snap.parent;
        }
        Map<CharSequence, V> out = new HashMap<CharSequence, V>(i);
        while(!stack.isEmpty()) {
            snap = stack.removeLast();
            for (Object object : snap) {
                Entry<CharSequence, V> entry = (Entry<CharSequence, V>) object;
                if (isRemoved(entry.getValue())) {
                    out.remove(entry.getKey());
                } else {
                    out.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return out;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    protected int size() {
        if (storage == EMPTY) {
            return 0;
        } else if (storage instanceof Map<?, ?>) {
            return ((Map<?,?>)storage).size();
        } else if (storage instanceof Holder) {
            return ((Holder)storage).arr.length / 2;
        } else {
            return 1;
        }
    }

    @SuppressWarnings("unchecked")
    private void freeze() {
        if (storage instanceof Map<?,?>) {
            if (storage != EMPTY) {
                Object[] arr = compact((Map<CharSequence,Object>)storage);
                storage = cacheHolder(new Holder(arr));
            }
        }
    }
    
    protected Holder cacheHolder(Holder holder) {
        // do nothing by default
        return holder;
    }

    private static Object[] compact(Map<CharSequence, Object> map) {
        assert map != EMPTY;
        int size = map.size();
        assert size > 0;
        Object[] out = new Object[size*2];
        int index = 0;
        // prepare entries for sorting
        @SuppressWarnings("unchecked")
        Map.Entry<CharSequence, Object>[] entries = new Map.Entry[size];
        for (Entry<CharSequence, Object> entry : map.entrySet()) {
            entries[index++] = entry;
        }
        index = 0;
        Arrays.sort(entries, ENTRY_COMPARATOR);
        // compact output array based on sorted collection to be comparable for equality
        for (Map.Entry<CharSequence, Object> entry : entries) {
            // first half are macro names
            out[index]=entry.getKey();
            // second half are macros
            out[index+size]=entry.getValue();
            index++;
        }
        return out;
    }
    
    /**
     * Default implementation - no removes, only overrides
     */
    protected boolean isRemoved(V value) {
        return false;
    };
    
    public static final class Holder implements Iterable {

        // array have to be sorted, otherwise equals can not work
        public final Object[] arr;
        private final int hashCode;

        public Holder(Object[] arr) {
            this.arr = arr;
            this.hashCode = Arrays.hashCode(this.arr);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Holder other = (Holder) obj;
            if (hashCode != other.hashCode) {
                return false;
            }
            // macro names are at the beginning, it speeds up comparision
            if (!Arrays.equals(this.arr, other.arr)) {
                return false;
            }
            return true;
        }
        
        public int size() {
            return arr.length / 2;
        }

        @Override
        public Iterator iterator() {
            return new Iterator() {
                private int pos = 0;
                private final int offset = arr.length/2;
                
                @Override
                public boolean hasNext() {
                    return pos < offset;
                }

                @Override
                public Object next() {
                    Entry<CharSequence, Object> res = 
                            new Entry<CharSequence, Object> () {
                                int ePos = pos;
                                @Override
                                public CharSequence getKey() {
                                    return (CharSequence)arr[ePos];
                                }

                                @Override
                                public Object getValue() {
                                    return arr[ePos + offset];
                                }

                                @Override
                                public Object setValue(Object value) {
                                    throw new UnsupportedOperationException("Not supported."); //NOI18N
                                }
                            };
                    pos++;
                    return res;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported."); //NOI18N
                }
            };
        }
    }

    public static final Comparator<Entry<CharSequence, Object>> ENTRY_COMPARATOR = new EntryComparatorImpl();
    private static class EntryComparatorImpl implements Comparator<Entry<CharSequence, Object>> {
        private final Comparator<CharSequence> charSeqComparator = CharSequences.comparator();
        public EntryComparatorImpl() {
        }

        @Override
        public int compare(Entry<CharSequence, Object> o1, Entry<CharSequence, Object> o2) {
            return charSeqComparator.compare(o1.getKey(), o2.getKey());
        }
    }

    @Override
    public Iterator<Entry<CharSequence, V>> iterator() {
        if (storage == EMPTY) {
            return Collections.emptyIterator();
        } else if (storage instanceof Map<?,?>) {
            return ((Map<CharSequence,V>)storage).entrySet().iterator();
        } else if (storage instanceof Holder) {
            return ((Holder)storage).iterator();
        }
        return Collections.emptyIterator();
    }
    
    protected static class SingleItemIterator<V> implements Iterator<Entry<CharSequence, V>> {
        private Entry entry;

        public SingleItemIterator(final CharSequence key, final V value) {
            this.entry = new Entry() {
                @Override
                public Object getKey() {
                    return key;
                }

                @Override
                public Object getValue() {
                    return value;
                }

                @Override
                public Object setValue(Object value) {
                    throw new UnsupportedOperationException("Not supported."); //NOI18N
                }
            };
        }
                
        @Override
        public boolean hasNext() {
            return entry != null;
        }

        @Override
        public Entry next() {
            Entry res = entry;
            entry = null;
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }
    }
}
