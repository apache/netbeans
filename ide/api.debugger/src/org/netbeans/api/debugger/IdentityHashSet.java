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

package org.netbeans.api.debugger;

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Delete when someone decides to fix http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4479578
 *
 * @author Martin Entlicher
 */
class IdentityHashSet<T> extends AbstractSet<T> {

    final Map<T, Object> map;

    IdentityHashSet() {
        map = new IdentityHashMap<T, Object>();
    }

    IdentityHashSet(int size) {
        map = new IdentityHashMap<T, Object>(size);
    }

    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return map.keySet().toArray(a);
    }

    @Override
    public boolean add(T o) {
        return map.put(o, map) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == map;
    }

    @Override
    public void clear() {
        map.clear();
    }

}
