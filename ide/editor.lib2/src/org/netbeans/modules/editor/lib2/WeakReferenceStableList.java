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
package org.netbeans.modules.editor.lib2;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * List holding non-null items weakly with automatic collection of GCed items.
 * <br>
 * Add and remove operations do not affect a list previously obtained by {@link #getList() }
 * so it may be used in a similar way like ListenerList holding all listener instances
 * by a weak reference.
 *
 * @author Miloslav Metelka
 */
public final class WeakReferenceStableList<E> {
    
    /**
     * Maximum empty reference count before array reallocation.
     */
    private static final int EMPTY_REF_COUNT_THRESHOLD = 3;
    
    private ImmutableList<E> list;
    
    private int emptyRefCount;

    public WeakReferenceStableList() {
        list = new ImmutableList<E>(newArray(0));
    }
    
    /**
     * Get list of weakly-held items.
     *
     * @return list of weakly-held items. When getting items from the list
     *  some of them may be null. Its <code>size()</code> method includes those null values
     *  so when iterating by index there should be an explicit check for null value.
     *  When using an iterator the null values are skipped.
     *
     */
    public synchronized List<E> getList() {
        return list;
    }
  
    /**
     * Add a non-null item.
     * @param e non-null item. Null items are not supported.
     */
    public synchronized void add(E e) {
        assert (e != null) : "This list is not designed to hold null elements"; // NOI18N
        WeakRef<E>[] array = list.array;
        WeakRef<E>[] newArray = newArray(array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = new WeakRef<E>(e, this);
        assignList(newArray);
    }

    public synchronized void addAll(Collection<E> c) {
        WeakRef<E>[] array = list.array;
        WeakRef<E>[] newArray = newArray(array.length + c.size());
        System.arraycopy(array, 0, newArray, 0, array.length);
        int i = array.length;
        for (E e : c) {
            assert (e != null) : "This list is not designed to hold null elements"; // NOI18N
            newArray[i++] = new WeakRef<E>(e, this);
        }
        assignList(newArray);
    }

    /**
     * Remove given non-null entry.
     * @param e non-null entry. Null entry is supported but may remove things that were GCed
     *  instead of true null items.
     * @return true if element was removed successfully.
     */
    public synchronized boolean remove(E e) {
        assert (e != null) : "This list is not designed to hold null elements"; // NOI18N
        WeakRef<E>[] array = list.array;
        for (int i = 0; i < array.length; i++) {
            if (e.equals(array[i].get())) {
                removeImpl(i);
                return true;
            }
        }
        return false;
    }
    
    private void removeImpl(int index) {
        WeakRef<E>[] array = list.array;
        WeakRef<E>[] newArray = newArray(array.length - 1);
        System.arraycopy(array, 0, newArray, 0, index);
        System.arraycopy(array, index + 1, newArray, index, array.length - index - 1);
        assignList(newArray);
    }
    
    private void assignList(WeakRef<E>[] array) {
        list = new ImmutableList<E>(array);
    }
    
    private WeakRef<E>[] newArray(int size) {
        @SuppressWarnings("unchecked")
        WeakRef<E>[] array = new WeakRef[size];
        return array;
    }
    
    synchronized void incrementEmptyRefCount() {
        if (++emptyRefCount > EMPTY_REF_COUNT_THRESHOLD) {
            WeakRef<E>[] array = list.array;
            WeakRef<E>[] newArray = newArray(array.length - emptyRefCount);
            int j = 0;
            for (int i = 0; i < array.length; i++) {
                WeakRef<E> ref = array[i];
                if (ref != null && ref.get() != null) {
                    newArray[j++] = ref;
                }
            }
            assignList(newArray);
            emptyRefCount = 0;
        }
    }

    @Override
    public synchronized String toString() {
        return list.toString();
    }

    private static final class ImmutableList<E> extends AbstractList<E> {

        WeakRef<E>[] array;
        
        public ImmutableList(WeakRef<E>[] array) {
            super();
            this.array = array;
        }
        
        @Override
        public E get(int index) {
            WeakRef<E> eRef = array[index];
            return (eRef != null) ? eRef.get() : null;
        }
        
        @Override
        public int size() {
            return array.length;
        }

        @Override
        public Iterator<E> iterator() {
            return new Itr();
        }
        
        private final class Itr implements Iterator<E> {
            
            int index;
            
            E next;
            
            Itr() {
                updateNext();
            }
            
            @Override
            public boolean hasNext() {
                return (next != null);
            }

            @Override
            public E next() {
                E e = next;
                if (e == null) {
                    throw new NoSuchElementException();
                }
                updateNext();
                return e;
            }
            
            private void updateNext() {
                int size = size();
                while (index < size) {
                    next = get(index++);
                    if (next != null) {
                        return;
                    }
                }
                next = null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet."); // NOI18N
            }

        }
        

    }
    
    private static final class WeakRef<E> extends WeakReference<E> implements Runnable {
        
        private final WeakReferenceStableList list;

        public WeakRef(E e, WeakReferenceStableList list) {
            super(e, org.openide.util.Utilities.activeReferenceQueue());
            this.list = list;
        }
        

        @Override
        public void run() {
            list.incrementEmptyRefCount();
        }
        
    }

}
