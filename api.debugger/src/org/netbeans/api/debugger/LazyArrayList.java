/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.debugger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Lazy implementation of ArrayList
 *
 * @author Martin Entlicher
 */
class LazyArrayList<T> extends ArrayList<T> {

    private final Map<Integer, LazyEntry<T>> lazyEntries = new HashMap<Integer, LazyEntry<T>>();

    @Override
    public Object[] toArray() {
        Object[] arr = super.toArray();
        for (int i = 0; i < arr.length; i++) {
            LazyEntry<T> le = lazyEntries.remove(i);
            if (le != null) {
                arr[i] = le.get();
                set(i, (T) arr[i]);
            }
        }
        return arr;
    }

    @Override
    public <TT> TT[] toArray(TT[] a) {
        TT[] arr = super.toArray(a);
        for (int i = 0; i < arr.length; i++) {
            LazyEntry<T> le = lazyEntries.remove(i);
            if (le != null) {
                T e = le.get();
                arr[i] = (TT) e;
                set(i, e);
            }
        }
        return arr;
    }

    private Object[] toOriginalArray() {
        return super.toArray();
    }

    public boolean add(LazyEntry<T> e) {
        lazyEntries.put(size(), e);
        return add((T) null);
    }

    @Override
    public boolean remove(Object o) {
        if (o instanceof LazyEntry) {
            for (Integer i : lazyEntries.keySet()) {
                if (o.equals(lazyEntries.get(i))) {
                    super.remove(i);
                    lazyEntries.remove(i);
                    shiftLazyEntries(i, -1);
                    return true;
                }
            }
            return false;
        } else {
            return super.remove(o);
        }
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (c instanceof LazyArrayList) {
            LazyArrayList lazyArrayList = (LazyArrayList) c;
            int s = size();
            Map<Integer, LazyEntry> newLazyEntries = lazyArrayList.lazyEntries;
            for (Map.Entry<Integer, LazyEntry> e : newLazyEntries.entrySet()) {
                lazyEntries.put((e.getKey() + s), e.getValue());
            }
            return super.addAll((Collection<? extends T>) Arrays.asList(lazyArrayList.toOriginalArray()));
        } else {
            return super.addAll(c);
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void clear() {
        lazyEntries.clear();
        super.clear();
    }

    @Override
    public T get(int index) {
        LazyEntry<T> le = lazyEntries.remove(index);
        if (le != null) {
            T e = le.get();
            set(index, e);
            return e;
        } else {
            return super.get(index);
        }
    }
    
    protected T getRaw(int index) {
        return super.get(index);
    }

    /** Gets the instance or LazyEntry */
    public Object getEntry(int index) {
        LazyEntry<T> le = lazyEntries.get(index);
        if (le != null) {
            return le;
        } else {
            return super.get(index);
        }
    }

    @Override
    public void add(int index, T element) {
        shiftLazyEntries(index, 1);
        super.add(index, element);
    }

    @Override
    public T remove(int index) {
        LazyEntry<T> le = lazyEntries.remove(index);
        if (le != null) {
            T e = le.get();
            super.remove(index);
            shiftLazyEntries(index, -1);
            return e;
        } else {
            return super.remove(index);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator {

        private int cursor = 0;
        private int expectedModCount = modCount;

        public boolean hasNext() {
            return cursor < size();
        }

        public Object next() {
            if (cursor >= size())
                throw new NoSuchElementException();
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            return get(cursor++);
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private void shiftLazyEntries(int from, final int by) {
        // Set<Integer> indexes = new TreeSet(lazyEntries.keySet());
        // if (by > 0) indexes = indexes.descendingSet(); - Since JDK 6.
        TreeSet<Integer> indexes = new TreeSet(new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                int i1 = o1;
                int i2 = o2;
                return (int) (Math.signum(i2 - i1)*Math.signum(by));
            }
        });
        //System.err.println("\nSHIFTING "+lazyEntries+"\nby "+by);
        indexes.addAll(lazyEntries.keySet());
        for (Integer i : indexes) {
            if (i >= from) {
                lazyEntries.put((i + by), lazyEntries.remove(i));
            }
        }
        //System.err.println(" entries = "+lazyEntries+"\n");
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object o) {
	if (o == this)
	    return true;
	if (!(o instanceof LazyArrayList))
	    return false;

        LazyArrayList l = (LazyArrayList) o;
        if (size() != l.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            Object o1 = getEntry(i);
            Object o2 = l.getEntry(i);
	    if (!(o1==null ? o2==null : o1.equals(o2)))
		return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int hashCode = 1;
        for (int i = 0; i < size(); i++) {
	    Object obj = getEntry(i);
	    hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
	}
	return hashCode;
    }

    protected abstract static class LazyEntry<T> {
        protected abstract T get();
    }

}
