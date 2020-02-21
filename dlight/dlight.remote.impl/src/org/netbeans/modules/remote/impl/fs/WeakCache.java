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

package org.netbeans.modules.remote.impl.fs;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A cache that holds soft references to its values and
 * removes entries when values are collected
 */
public class WeakCache<K, V> {

    private final ConcurrentMap<K, Ref<K, V>> map;
    private final ReferenceQueue<V> referenceQueue;
    private final Lock lock;
    //private final Object replacementLock = new Object();
            
    private static class Ref<K, V> extends SoftReference<V> {
        private final K key;
        private Ref(K key, V value, ReferenceQueue<V> referenceQueue) {
            super(value, referenceQueue);
            this.key = key;
        }
        @Override
	public boolean equals(Object o) {
	    if (!(o instanceof Ref)) {
		return false;
            }
	    Ref ref = (Ref)o;
	    return equalsImpl(key, ref.key) && (get() == ref.get());
	}
        
        @Override
	public int hashCode() {
            V value = get();
	    return (key   == null ? 0 :   key.hashCode()) ^
		   (value == null ? 0 : value.hashCode());
	}
        
        private static boolean equalsImpl(Object o1, Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }        
    }

    public WeakCache() {
        map = new ConcurrentHashMap<>();
        referenceQueue = new ReferenceQueue<>();
        lock = new ReentrantLock();
    }

    public Collection<V> values() {
        Collection<V> result = new ArrayList<>(map.size());
        for (Ref<K, V> ref : map.values()) {
            V value = ref.get();
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }
    
    public void remove(K key, V expected) {
        Ref<K, V> expectedRef = new Ref<>(key, expected, referenceQueue);
        map.remove(key, expectedRef);
    }
    
//    public V safePut(K key, V value, V expected) {
//        final Ref<K, V> newRef = new Ref<K, V>(key, value, referenceQueue);
//        Ref<K, V> expectedRef = new Ref<K, V>(key, expected, referenceQueue);
//        if (map.replace(key, expectedRef, newRef)) {
//            return value;
//        } else {
//            Ref<K, V> curRef = map.get(key);
//            V result = curRef == null ? null : curRef.get();
//            if (result != null) {
//                return result;
//            }
//            
//            // ref was removed of unhold => try to replace it once more
//            expectedRef = new Ref<K, V>(key, null, referenceQueue);
//            if (map.replace(key, expectedRef, newRef)) {
//                return value;
//            }
//        }
//        return null;
//    }
    
//    public V putIfAbsent(K key, V value) {
//        synchronized (replacementLock) {
//            Ref<K, V> oldRef = map.get(key);
//            if (oldRef != null) {
//                V oldValue = oldRef.get();
//                if (oldValue != null) {
//                    return oldValue;
//                }
//            }            
//            Ref<K, V> newRef = new Ref<K, V>(key, value, referenceQueue);
//            map.put(key, newRef);
//            return value;
//        }
////        Ref<K, V> newRef = new Ref<K, V>(key, value, referenceQueue);
////        Ref<K, V> oldRef;
////        for (int i = 0; i < 5; i++) { // in fact no more than twice
////            oldRef = map.putIfAbsent(key, newRef);
////            if (oldRef == null) {
////                return value;
////            } else {
////                V oldValue = oldRef.get();
////                if (oldValue == null) {
////                    map.remove(key, oldRef); // and retry
////                } else {
////                    return oldValue;
////                }
////            }                   
////        }
////        return null;
//    }

    public void put(K key, V value) {
        Ref<K, V> ref = new Ref<>(key, value, referenceQueue);
        map.put(key, ref);
    }

    public V get(K key) {
        Ref<K, V> ref = map.get(key);
        V result = (ref == null) ? null : ref.get();
        return result;
    }

    public V remove(K key) {
        Ref<K, V> removed = map.remove(key);
        return (removed == null) ? null : removed.get();
    }

    public int size() {
        return map.size();
    }

    public void tryCleaningDeadEntries() {
        if (lock.tryLock()) {
            try {
                Ref<K, V> ref;
                while ( (ref = (Ref<K, V>) referenceQueue.poll()) != null) {
                    if (ref.key != null) {
                        Ref<K, V> expectedRef = new Ref<>(ref.key, null, referenceQueue);
                        map.remove(ref.key, expectedRef);
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }
}
