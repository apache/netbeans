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
        Set<Integer> indexes = new TreeSet<>(new Comparator<Integer>() {
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
