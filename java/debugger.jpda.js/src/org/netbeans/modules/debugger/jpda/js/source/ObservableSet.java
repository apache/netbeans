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

package org.netbeans.modules.debugger.jpda.js.source;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Synchronized implementation of a set, which fires property change events.
 * 
 * @author Martin Entlicher
 */
public class ObservableSet<E> implements Set<E> {

    public static final String PROP_ELM_ADDED = "elementAdded";                 // NOI18N
    public static final String PROP_ELM_REMOVED = "elementRemoved";             // NOI18N
    public static final String PROP_CLEARED = "cleared";                        // NOI18N
    
    private final PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    
    private Set<E> set = Collections.emptySet();
    
    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return set.iterator();
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean add(E e) {
        if (set == Collections.emptySet()) {
            set = Collections.synchronizedSet(new HashSet<E>());
        }
        boolean added = set.add(e);
        if (added) {
            pchs.firePropertyChange(PROP_ELM_ADDED, null, e);
        }
        return added;
    }

    @Override
    public boolean remove(Object o) {
        boolean removed = set.remove(o);
        if (removed) {
            pchs.firePropertyChange(PROP_ELM_REMOVED, o, null);
        }
        return removed;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clear() {
        set.clear();
        pchs.firePropertyChange(PROP_CLEARED, null, null);
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pchs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pchs.removePropertyChangeListener(l);
    }
    
}
