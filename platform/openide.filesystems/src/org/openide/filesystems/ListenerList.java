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

package org.openide.filesystems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class that holds a list of listeners of some type.
 * Replacement of  EventListListener, that solves performance issue #20715
 * @author  rm111737
 */
class ListenerList<T> {
    private final List<T> listenerList;
    private List<T> copy = null;

    ListenerList() {
        listenerList = new ArrayList<T>();
    }

    /**
     * Adds the listener .
     **/
    public synchronized boolean add(T listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        copy = null;

        return listenerList.add(listener);
    }

    /**
     * Removes the listener .
     **/
    public synchronized boolean remove(T listener) {
        copy = null;

        return listenerList.remove(listener);
    }

    /**
     * Passes back the event listener list
     */
    public synchronized List<T> getAllListeners() {
        if (listenerList.isEmpty()) {
            return Collections.emptyList();
        }
        if (copy == null) {
            copy = new ArrayList<T>(listenerList);
        }
        return copy;
    }
    
    public synchronized boolean hasListeners() {
        return !listenerList.isEmpty();
    }

    static <T> List<T> allListeners(ListenerList<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.getAllListeners();
    }
}
