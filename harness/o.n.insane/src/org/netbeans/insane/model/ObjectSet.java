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

package org.netbeans.insane.model;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** A set of objects with reflexive map behavior, using possibly
 * an external hash implementation. Tries to minimize memory consumption.
 *
 * @author Nenik
 */
class ObjectSet {

    private Hash hash;
    private Object[] table;
    private float loadFactor = 0.75f;
    private int limit;
    private int size;

    public ObjectSet() {
        this (new Hash() {
            public boolean equals(Object o1, Object o2) {
                return o1.equals(o2);
            }

            public int hashCodeFor(Object o) {
                return o.hashCode();
            }
        });
    }
    
    /** Creates a new instance of ObjectSet */
    public ObjectSet(Hash hash) {
        this.hash = hash;
        table = new Object[11];
        limit = (int)(table.length * loadFactor);
    }

    public interface Hash {
        public int hashCodeFor(Object o);
        public boolean equals(Object o1, Object o2);
    }
    
    
    public Object get(Object key) {
        int bucket = (hash.hashCodeFor(key) & 0x7FFFFFFF)  % table.length;
        
        while (table[bucket] != null) {
            if (hash.equals(key, table[bucket])) return table[bucket];
            bucket = (bucket + 1) % table.length;
        }
        
        return null; // XXX
    }
    
    /* Always replaces exiting equals object */
    public void put(Object key) {
//System.err.println("put: size=" + size + ", limit=" + limit + ", len=" + table.length);
        if ((size+1) > limit) rehash(table.length*2);

        size++;
        int bucket = (hash.hashCodeFor(key) & 0x7FFFFFFF) % table.length;
            
        while (table[bucket] != null && !hash.equals(key, table[bucket])) {
            bucket = (bucket + 1) % table.length;
        }
        
        table[bucket] = key;
    }
    
    public boolean contains(Object key) {
        return get(key) != null;
    }
    
    public Iterator iterator() {
        return new Iterator() {
            int ptr = 0;
            Object next;
            public boolean hasNext() {
                while (next == null && ptr < table.length) {
                    next = table[ptr++];
                }
                return next != null;
            }
            
            public Object next() {
                if (!hasNext()) throw new NoSuchElementException();
                Object ret = next;
                next = null;
                return ret;
            }
            
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    private void rehash(int newSize) {
        Object[] newTable = new Object[newSize];
        for (int i=0; i<table.length; i++) {
            Object act = table[i];
            if (act != null) {
                int bucket = (hash.hashCodeFor(act) & 0x7FFFFFFF) % newTable.length;
                while (newTable[bucket] != null) { // find an empty slot
                    bucket = (bucket + 1) % newTable.length;
                }
                
                newTable[bucket] = act;
            }
        }
            
        table = newTable;
        limit = (int)(table.length * loadFactor);
    }
}
