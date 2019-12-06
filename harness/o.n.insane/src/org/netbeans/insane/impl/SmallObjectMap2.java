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

package org.netbeans.insane.impl;

import java.util.*;

import org.netbeans.insane.scanner.*;


/** This is a special kind of IdentityHashSet.
 * It hashes the objects according to their system hash code
 * and in case of colision keeps wrappers to provide unique IDs.
 * This implementation does even provide flat numeric IDs
 */
class SmallObjectMap2 implements ObjectMap {
    // the primary table keeping all the known objects
    // hashed using their system hashcodes, with circular-linear shift
    // in case of bucket collision.
    private Object[] table = new Object[128*1024];
    private int size;

    // this map keeps reference to all objects with system hash collision
    private Map<Object,Integer> wrappers = new IdentityHashMap<Object,Integer>();

    int idCounter;

    int maxDisplace;

    SmallObjectMap2() {}

    public boolean isKnown(Object o) {
        int bucket = System.identityHashCode(o) % table.length;

        while (table[bucket] != null) {
            if (table[bucket] == o) return true;

            bucket = (bucket + 1) % table.length;
        }

        return false;
    }


    public String getID(Object o) {
        // find whether it is known and wrapped
        Integer wid = wrappers.get(o);
        if (wid != null) return getWrappedId(o, wid.intValue());

        // ... or at least known
        if (isKnown(o)) return getNormalId(o);

        // unknown object
        if (putObject(o)) { //wrapped
            return getWrappedId(o, wrappers.get(o).intValue());
        } else {
             return getNormalId(o);
        }
    }

    private boolean usedId(int id) {
        if (id > 0 && id < idCounter) return true;

        int bucket = id % table.length;

        while (table[bucket] != null) {
            if (System.identityHashCode(table[bucket]) == id) return true;

            bucket = (bucket + 1) % table.length;
        }

        return false;
    }

    private static String getWrappedId(Object o, int i) {
        return Integer.toHexString(i);
    }

    private static String getNormalId(Object o) {
        return Integer.toHexString(System.identityHashCode(o));
    }

    private int nextFreeId() {
        while (usedId(++idCounter));
        return idCounter;
    }

    // knows it is not there.
    // returns true iff wraps
    private boolean putObject(Object o) {
        if (5*size/4 > table.length) rehash(3*table.length/2);

        size++;
        int sysID = System.identityHashCode(o);
        int bucket = sysID % table.length;
        boolean wrap = usedId(sysID);

        int temp = 0;
        // find an empty slot, look for friends with the same ID
        while (table[bucket] != null) {
//                if (System.identityHashCode(table[bucket]) == sysID) wrap = true;
            temp++;
            bucket = (bucket + 1) % table.length;
        }
        if (temp > maxDisplace) maxDisplace = temp;

        // fill the slot
        table[bucket] = o;

        // add the wrapping info
        if (wrap) wrappers.put(o, new Integer(nextFreeId()));
        return wrap;
    }

    private void rehash(int newSize) {
        Object[] newTable = new Object[newSize];
        for (int i=0; i<table.length; i++) {
            Object act = table[i];
            if (act != null) {
                int bucket = System.identityHashCode(act) % newTable.length;
                int temp=0;
                while (newTable[bucket] != null) { // find an empty slot
                    temp++;
                    bucket = (bucket + 1) % newTable.length;
                }
                if (temp > maxDisplace) maxDisplace = temp;

                newTable[bucket] = act;
            }
        }

        table = newTable;
    }
}

