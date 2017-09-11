/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
