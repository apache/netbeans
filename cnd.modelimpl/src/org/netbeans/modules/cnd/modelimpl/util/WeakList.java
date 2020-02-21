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
package org.netbeans.modules.cnd.modelimpl.util;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * A list that keeps weak references to its elements
 */
public class WeakList<T> implements Iterable<T> {

    private final List<WeakReference<T>> list = new ArrayList<>();

    /**
     * Adds a weak reference to the given element to this list
     */
    public synchronized void add(T element) {
        list.add(new WeakReference<>(element));
    }

    /**
     * Adds all weak references frim the given iterator to this list
     */
    public synchronized void addAll(Iterator<T> elements) {
        while (elements.hasNext()) {
            list.add(new WeakReference<>(elements.next()));
        }
    }

    /*
     * Removes all references to the given element from this list
     */
    public synchronized void remove(T element) {
        for (Iterator<WeakReference<T>> it = list.iterator(); it.hasNext();) {
            WeakReference<T> ref = it.next();
            if (ref.get() == element) {
                it.remove();
            }
        }
    }

    /** Removes all elements */
    public synchronized void clear() {
        list.clear();
    }

    /** 
     * Returns an iterator of non-null references.
     * NB: it iterates over a snapshot made at the moment of the call
     */
    @Override
    public synchronized Iterator<T> iterator() {
        List<T> result = new ArrayList<>();
        addTo(result);
        return result.iterator();
    }

    public synchronized Collection<T> join(Collection<? extends T> collection) {
        List<T> result = new ArrayList<>(collection.size() + list.size());
        result.addAll(collection);
        addTo(result);
        return result;
    }

    private void addTo(Collection<T> collection) {
        for (Iterator<WeakReference<T>> it = list.iterator(); it.hasNext();) {
            WeakReference<T> ref = it.next();
            T element = ref.get();
            if (element != null) {
                collection.add(element);
            }
        }
    }
}
