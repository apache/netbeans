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
package org.netbeans.modules.cnd.repository;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * An in-memory cache for storing repository objects
 *
 */
public final class RepositoryCache {

    private static final boolean WEAK_REF = false && CndTraceFlags.WEAK_REFS_HOLDERS;
    // Use soft references only when we request an object
    private static final boolean SOFT_REFS_ON_GET = true;
    private static final boolean STATISTIC = false;
    private static final int DEFAULT_SLICE_CAPACITY;
    private static final int SLICE_SIZE;
    private static final int SLICE_MASK;

    static {
        int nrProc = CndUtils.getConcurrencyLevel();
        if (nrProc <= 4) {
            SLICE_SIZE = 32;
            SLICE_MASK = SLICE_SIZE - 1;
            DEFAULT_SLICE_CAPACITY = 512;
        } else {
            SLICE_SIZE = 128;
            SLICE_MASK = SLICE_SIZE - 1;
            DEFAULT_SLICE_CAPACITY = 128;
        }
    }

    private interface CacheValue {

        Key getKey();
    }

    private static class SoftValue<T> extends SoftReference<T> implements CacheValue {

        private final Key key;

        private SoftValue(T k, Key key, ReferenceQueue<T> q) {
            super(k, q);
            this.key = key;
        }

        @Override
        public Key getKey() {
            return key;
        }
    }

    public static class Pair<T1, T2> {

        public final T1 first;
        public final T2 second;

        public Pair(T1 first, T2 second) {
            this.first = first;
            this.second = second;
        }
    };

    private static class WeakValue<T> extends WeakReference<T> implements CacheValue {

        private final Key key;

        private WeakValue(T k, Key key, ReferenceQueue<T> q) {
            super(k, q);
            this.key = key;
        }

        @Override
        public Key getKey() {
            return key;
        }
    }

    private static final class Slice {

        private final Map<Key, Object> storage = new HashMap<Key, Object>(DEFAULT_SLICE_CAPACITY);
        private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
        private final Lock w = cacheLock.writeLock();
        private final Lock r = cacheLock.readLock();
    }

    private static final class SlicedMap {

        private final Slice slices[] = new Slice[SLICE_SIZE];

        private SlicedMap() {
            for (int i = 0; i < SLICE_SIZE; i++) {
                slices[i] = new Slice();
            }
        }

        private Slice getSilce(Key key) {
            int i = key.hashCode() & SLICE_MASK;
            return slices[i];
        }

        private Slice getSilce(int i) {
            return slices[i];
        }
    }
    private final SlicedMap cache = new SlicedMap();
    private final Lock refQueueLock;
    private final ReferenceQueue<Persistent> refQueue;
    // Cache statistics
    private int readCnt = 0;
    private int readHitCnt = 0;
    private int hangs = 0;
    private int puts = 0;
    private int putIfAbs = 0;
    private int switchToSoft = 0;
    private int weakRefs = 0;

    public RepositoryCache() {
        refQueueLock = new ReentrantLock();
        refQueue = new ReferenceQueue<Persistent>();
    }

    public void hang(Key key, Persistent obj) {
        if (STATISTIC) {
            hangs++;
        }
        Slice s = cache.getSilce(key);
        s.w.lock();
        try {
            s.storage.put(key, obj);
        } finally {
            s.w.unlock();
        }
    }

    public void put(Key key, Persistent obj) {
        if (STATISTIC) {
            puts++;
        }
        Slice s = cache.getSilce(key);
        Reference<Persistent> value;
        if (SOFT_REFS_ON_GET || (WEAK_REF && key.getBehavior() != Key.Behavior.LargeAndMutable)) {
            value = new WeakValue<Persistent>(obj, key, refQueue);
            if (STATISTIC) {
                weakRefs++;
            }
        } else {
            value = new SoftValue<Persistent>(obj, key, refQueue);
        }
        s.w.lock();
        try {
            s.storage.put(key, value);
        } finally {
            s.w.unlock();
        }
    }

    /**
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with the specified key, or
     * <tt>null</tt> if there was no mapping for the key.
     */
    public Persistent putIfAbsent(Key key, Persistent obj) {
        if (STATISTIC) {
            putIfAbs++;
        }
        Persistent prevPersistent = null;
        Slice s = cache.getSilce(key);
        s.w.lock();
        try {
            // do not override existed value if any
            Object old = s.storage.get(key);
            if (old instanceof RemovedValue) {
                prevPersistent = null;
            } else if (old instanceof Reference) {
                prevPersistent = (Persistent) ((Reference) old).get();
            } else if (old instanceof Persistent) {
                prevPersistent = (Persistent) old;
            } else if (old != null) {
                System.err.println("unexpected value " + old + " for key " + key);
            }
            if (prevPersistent == null) {
                Reference<Persistent> value;
                if (WEAK_REF && key.getBehavior() != Key.Behavior.LargeAndMutable) {
                    value = new WeakValue<Persistent>(obj, key, refQueue);
                    if (STATISTIC) {
                        weakRefs++;
                    }
                } else {
                    value = new SoftValue<Persistent>(obj, key, refQueue);
                }
                // no previous value
                // put new item into storage
                s.storage.put(key, value);
            }
        } finally {
            s.w.unlock();
        }
        processQueue();
        return prevPersistent;
    }

    public Persistent get(Key key) {
        if (STATISTIC) {
            readCnt++;
        }
        Slice s = cache.getSilce(key);
        Object value;
        s.r.lock();
        try {
            value = s.storage.get(key);
        } finally {
            s.r.unlock();
        }
        if (value instanceof RemovedValue) {
            return ((RemovedValue) value).value;
        } else if (value instanceof Persistent) {
            if (STATISTIC) {
                readHitCnt++;
            }
            return (Persistent) value;
        } else if (value instanceof Reference) {
            Persistent result = (Persistent) ((Reference) value).get();
            if (SOFT_REFS_ON_GET && result != null && value instanceof WeakReference) {
                // switch to soft reference
                s.w.lock();
                try {
                    // check that there were no modifications
                    Object freshValue = s.storage.get(key);
                    if (freshValue == value) {
                        value = new SoftValue<Persistent>(result, key, refQueue);
                        s.storage.put(key, value);
                        if (STATISTIC) {
                            switchToSoft++;
                        }
                    }
                } finally {
                    s.w.unlock();
                }
            }
            if (STATISTIC && result != null) {
                readHitCnt++;
            }
            return result;
        }
        return null;
    }

    public void remove(Key key) {
        markRemoved(key);
    }

    void removePhysically(Key key) {
        Slice s = cache.getSilce(key);
        s.w.lock();
        try {
            Object prev = s.storage.get(key);
            if (prev instanceof RemovedValue) {
                s.storage.remove(key);
            }
        } finally {
            s.w.unlock();
        }
    }

    void markRemoved(Key key) {
        Slice s = cache.getSilce(key);
        s.w.lock();
        try {
            Persistent old = get(key);
            s.storage.put(key, new RemovedValue(old));
            // do not assert for now
            //            Object old = s.storage.put(key, REMOVED);
            //            assert old != null : " no value for removed key " + key;
        } finally {
            s.w.unlock();
        }
    }
    
    public void clearSoftRefs() {
        //cleanWriteHungObjects(null, false);
        processQueue();
        Set<Key> keys;
        for (int i = 0; i < SLICE_SIZE; i++) {
            Slice s = cache.getSilce(i);
            s.r.lock();
            try {
                keys = new HashSet<Key>(s.storage.keySet());
            } finally {
                s.r.unlock();
            }
            for (Key key : keys) {
                Object value;
                s.w.lock();
                try {
                    value = s.storage.get(key);
                    if (value != null && !(value instanceof Persistent)) {
                        s.storage.remove(key);
                    }
                } finally {
                    s.w.unlock();
                }
            }
        }
    }

    private void processQueue() {
        if (refQueueLock.tryLock()) {
            try {
                CacheValue sv;
                while ((sv = (CacheValue) refQueue.poll()) != null) {
                    Object value;
                    final Key key = sv.getKey();
                    if (key != null) {
                        Slice s = cache.getSilce(key);
                        s.w.lock();
                        try {
                            value = s.storage.get(key);
                            // check if the object has already been added by another thread
                            // it is more efficient than blocking puts from the disk
                            if ((value != null) && (value instanceof Reference) && (((Reference) value).get() == null)) {
                                Object removed = s.storage.remove(key);
                                assert (value == removed);
                            }
                        } finally {
                            s.w.unlock();
                        }
                    }
                }
            } finally {
                refQueueLock.unlock();
            }
        }
    }

    public Collection<Pair<Key, Persistent>> clearHungObjects(/*Filter<Key> filter*/) {
        processQueue();
        Collection<Pair<Key, Persistent>> result = new ArrayList<Pair<Key, Persistent>>();
        Set<Key> keys;
        for (int i = 0; i < SLICE_SIZE; i++) {
            Slice s = cache.getSilce(i);
            s.r.lock();
            try {
                keys = new HashSet<Key>(s.storage.keySet());
            } finally {
                s.r.unlock();
            }
            for (Key key : keys) {
                Object value;
                s.r.lock();
                try {
                    value = s.storage.get(key);
                } finally {
                    s.r.unlock();
                }
                if (value instanceof Persistent) {
                    result.add(new Pair<Key, Persistent>(key, (Persistent) value));
                    s.w.lock();
                    try {
                        s.storage.remove(key);
                    } finally {
                        s.w.unlock();
                    }
                }
            }
        }
        return result;
    }

    private void printStatistics(String name) {
        int hitPercentage = (readCnt == 0) ? 0 : readHitCnt * 100 / readCnt;
        System.out.printf("\n\nMemory cache statistics %s: %d reads,  %d hits (%d%%)\n\n", // NOI18N
                name, readCnt, readHitCnt, hitPercentage);
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("UL") // bug in find bugs!
    public void debugPrintDistribution() {
        Map<String, Integer> stat = new TreeMap<String, Integer>();
        Map<String, Integer> statSoft = new TreeMap<String, Integer>();
        int fullSize = 0;
        int nullSize = 0;
        for (final Slice s : cache.slices) {
            s.r.lock();
            try {
                fullSize += s.storage.size();
                for (Map.Entry<Key, Object> entry : s.storage.entrySet()) {
                    Key key = entry.getKey();
                    Object value = entry.getValue();
                    boolean isSoft = false;
                    if ((value != null) && (value instanceof Reference)) {
                        isSoft = true;
                        value = ((Reference) value).get();
                    }
                    String res = key.getClass().getName();
                    if (value == null) {
                        if (isSoft) {
                            res += "-soft null"; // NOI18N
                        } else {
                            res += "-null"; // NOI18N
                        }
                        nullSize++;
                    } else {
                        if (isSoft) {
                            res += "-soft " + value.getClass().getName(); // NOI18N
                        } else {
                            res += "-" + value.getClass().getName(); // NOI18N
                        }
                    }
                    Integer i = isSoft ? statSoft.get(res) : stat.get(res);
                    if (i == null) {
                        i = Integer.valueOf(1);
                    } else {
                        i = Integer.valueOf(i.intValue() + 1);
                    }
                    if (isSoft) {
                        statSoft.put(res, i);
                    } else {
                        stat.put(res, i);
                    }
                }
            } finally {
                s.r.unlock();
            }
        }
        System.err.println("\tMemCache of size " + fullSize + " with null " + nullSize + " objects");
        System.err.println("\tSoft memory cache");
        for (Map.Entry<String, Integer> entry : statSoft.entrySet()) {
            System.err.println("\t" + entry.getKey() + "=" + entry.getValue());
        }
        System.err.println("\tHard memory cache");
        for (Map.Entry<String, Integer> entry : stat.entrySet()) {
            System.err.println("\t" + entry.getKey() + "=" + entry.getValue());
        }
    }

    private static final class RemovedValue {

        private final Persistent value;

        public RemovedValue(Persistent value) {
            this.value = value == null ? RepositoryImpl.REMOVED_OBJECT : value;
        }

        @Override
        public String toString() {
            return "RemovedValue{" + "value=" + value + '}'; // NOI18N
        }
    };
}
