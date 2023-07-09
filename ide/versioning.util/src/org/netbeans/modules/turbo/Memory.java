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
package org.netbeans.modules.turbo;

import java.util.*;

/**
 * Keeps entity key => attribute,value pairs without actually
 * holding key's reference. It supports several size strategies:
 * <ul>
 *   <li>minimal entry count (for caches with short lived keys),
 *   <li>maximum entry count (for caches with long lived keys) and
 *   <li>driven by key lifetime
 * </ul>
 * <p>
 * It's synchronized for safety because at least
 * <code>TrackingRef.run</code> comes from private OpenAPI
 * thread.
 *
 * @author Petr Kuzel
 */
final class Memory {

    /** Defines minimum entries count limit. */
    private final int minimumSize;

    /** Defines maximum entries count limit. */
    private final int maximumSize;

    /** Limited size Map&lt;key, Map>. It defines minimal cache size. */
    private final Map minimalMap;

    /** Unbound Map&lt;key, Map&lt;attributeName, attributeValue>>. Key identifies live entity. */
    private final  Map liveEntitiesMap = new WeakHashMap(3571);

    /** Special value, known null that does not invalidate but caches. */
    public static final Object NULL = new Object();

    /** Keep reference to last isPrepared result. */
    public static final ThreadLocal<Entry> prepared = new ThreadLocal<>();

    private final Statistics statistics;

    private static final Random random = new Random(0);

    /**
     * Creates memory map with given strategy.
     * @param minSize minimum size
     * @param maxSize maximum size or <code>-1</code> for unbound size
     *        defined by key instance lifetime
     */
    public Memory(Statistics statistics, int minSize, int maxSize) {
        if (maxSize != -1) {
            if (maxSize < minSize || maxSize <1) {
                throw new IllegalArgumentException();
            }
        }
        minimumSize = minSize;
        maximumSize = maxSize;
        minimalMap = new LRU(minimumSize);
        this.statistics = statistics;
    }

    /**
     * Makes best efford to store file object attributes obtainable by next {@link #get}.
     * @param value updated value, <code>null</code> for removing memory entry
     * or <code>Memory.NULL</code> for storing <code>null</code> value (it's known that
     * value does not exist) later returned by {@link #get}.
     */
    public synchronized void put(Object key, String name, Object value) {

        // find values map

        Map attributes = null;
        attributes = (Map) liveEntitiesMap.get(key);
        if (attributes == null) {
            attributes = (Map) minimalMap.get(key);
            if (attributes == null) {
                attributes = new HashMap(5);
            }
        }

        // update it

        if (value != null) {
            attributes.put(name, normalizeValue(value));
        } else {
            attributes.remove(name);
        }
        putLive(key, attributes);
        minimalMap.put(key, attributes);
        Entry entry = prepared.get();
        if (entry != null) {
            if (key.equals(entry.key) && name.equals(entry.name)) {
                if (value != null) {
                    entry.value = normalizeValue(value);
                } else {
                    prepared.set(null);
                }
            }
        }
    }

    private void putLive(Object key, Map attributes) {

        // enforce maximumSize strategy by randomly removing
        // several (7) entries on reaching the max            
        if (maximumSize != -1 && liveEntitiesMap.size() >= maximumSize) {                                    
            Set keySet = liveEntitiesMap.keySet();            
            List l = new ArrayList(liveEntitiesMap.keySet());            
            
            // liveEntitiesMap is a weakhashmap so l.size() should be the real size                                               
            if(l.size() == maximumSize) {   
                int limit = Math.min(7, maximumSize/10);
                for (int i = 0; i<limit; i++) {
                    int index = random.nextInt(maximumSize);
                    Object removed = l.get(index);                           
                    if (keySet.remove(removed)) {                        
                        statistics.keyRemoved(removed);             
                    }                          
                }
            }    
            
        }

        liveEntitiesMap.put(key, attributes);
        statistics.keyAdded(key);
    }
    
    private static Object normalizeValue(Object value) {
        if (value == NULL) return null;
        return value;
    }

    /**
     * Looks for cached file atribute of given name.
     * Return stored attributes or <code>null</code>.
     */
    public synchronized Object get(Object key, String name) {

        Map attributes = (Map) liveEntitiesMap.get(key);
        if (attributes != null) {
            return attributes.get(name);
        }

        // try the minimal map
        attributes = (Map) minimalMap.get(key);
        if (attributes != null) {
            putLive(key, attributes);
            return attributes.get(name);
        }

        // have not been promised by existsEntry but eliminated by GC?
        Entry entry = prepared.get();
        if (entry != null) {
            if (key.equals(entry.key) && name.equals(entry.name)) {
                prepared.set(null);  // here ends our promised contract
                return entry.value;
            }
        }

        return null;
    }

    /**
     * Determines if given attribute has a cache entry.
     * Note that the entry can contain info that attribute
     * does not exist!
     */
    public synchronized boolean existsEntry(Object key, String name) {
        Map attributes = (Map) liveEntitiesMap.get(key);
        if (attributes == null) {
            attributes = (Map) minimalMap.get(key);
        }

        // keep promised value in tread local to survive paralell GC
        boolean isPrepared = attributes != null && attributes.containsKey(name);
        if (isPrepared) {
            Entry entry = prepared.get();
            if (entry == null) {
                entry = new Entry();
            }
            entry.key = key;
            entry.name = name;
            entry.value = attributes.get(name);
            prepared.set(entry);
        } else {
            statistics.computeRemoved(liveEntitiesMap.keySet());
            prepared.set(null);
        }
        return isPrepared;
    }

    public synchronized Object getMonitoredKey(Object key) {
        Set keySet = liveEntitiesMap.keySet();
        if (keySet.contains(key)) {
            Iterator it = keySet.iterator();
            while (it.hasNext()) {
                Object next = it.next();
                if (key.equals(next)) {
                    return next;
                }
            }
        }
        return null;
    }

    /** Single entry structure. */
    private class Entry {
        private Object key;
        private String name;
        private Object value;
    }

    /** Limited size LRU map implementation. */
    private static final class LRU extends LinkedHashMap {

        private final int maxSize;

        public LRU(int maxSize) {
            super(maxSize *2, 0.5f, true);
            this.maxSize = maxSize;
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxSize;
        }
    }

}
