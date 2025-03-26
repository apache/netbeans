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
package org.openide.util.lookup;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;

import java.util.*;
import org.openide.util.LookupEvent;


/** Allows exclusion of certain instances from lookup.
 *
 * @author Jaroslav Tulach
 */
final class ExcludingLookup extends org.openide.util.Lookup {
    /** the other lookup that we delegate to */
    private final Lookup delegate;

    /** classes to exclude (Class[]) or just one class (Class) */
    private final Object classes;
    
    /** results */
    private WeakResult<?> results;
    
    /**
     * Creates new Result object with supplied instances parameter.
     * @param classes to be used to return from the lookup
     */
    ExcludingLookup(Lookup delegate, Class[] classes) {
        this.delegate = delegate;

        for (Class c : classes) {
            if (c == null) {
                throw new NullPointerException();
            }
        }
        if (classes.length == 1) {
            this.classes = classes[0];
        } else {
            this.classes = classes;
        }
    }

    @Override
    public String toString() {
        return "ExcludingLookup: " + delegate + " excludes: " + Arrays.asList(classes()); // NOI18N
    }

    public <T> Result<T> lookup(Template<T> template) {
        if (template == null) {
            throw new NullPointerException();
        }

        if (areSubclassesOfThisClassAlwaysExcluded(template.getType())) {
            // empty result
            return Lookup.EMPTY.lookup(template);
        }
        
        R<T> ret = null;
        for (;;) { // at most twice
            synchronized (this) {
                WeakResult<?> r = results;
                WeakResult<?> prev = null;
                while (r != null) {
                    R<?> res = r.result.get();
                    if (res == null) {
                        if (prev != null) {
                            prev.next = r.next;
                        } else {
                            results = r.next;
                        }
                    } else {
                        if (template.equals(res.from)) {
                            @SuppressWarnings("unchecked")
                            Result<T> old = (Result<T>) res;
                            return old;
                        }
                    }
                    prev = r;
                    r = r.next;
                }
                if (ret != null) {
                    ret.weak.next = results;
                    results = ret.weak;
                    return ret;
                }                
            }
            ret = new R<T>(template, delegate.lookup(template));
        }

    }

    public <T> T lookup(Class<T> clazz) {
        if (areSubclassesOfThisClassAlwaysExcluded(clazz)) {
            return null;
        }

        T res = delegate.lookup(clazz);

        if (isObjectAccessible(clazz, res, 0)) {
            return res;
        } else {
            return null;
        }
    }

    @Override
    public <T> Lookup.Item<T> lookupItem(Lookup.Template<T> template) {
        if (areSubclassesOfThisClassAlwaysExcluded(template.getType())) {
            return null;
        }

        Lookup.Item<T> retValue = delegate.lookupItem(template);

        if (isObjectAccessible(template.getType(), retValue, 2)) {
            return retValue;
        } else {
            return null;
        }
    }

    /** @return true if the instance of class c shall never be returned from this lookup
     */
    private boolean areSubclassesOfThisClassAlwaysExcluded(Class<?> c) {
        Class<?>[] arr = classes();

        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isAssignableFrom(c)) {
                return true;
            }
        }

        return false;
    }

    /** Returns the array of classes this lookup filters.
     */
    final Class<?>[] classes() {
        if (classes instanceof Class[]) {
            return (Class[]) classes;
        } else {
            return new Class[] { (Class) classes };
        }
    }

    /** Does a check whether two classes are accessible (in the super/sub class)
     * releation ship without walking thru any of the classes mentioned in the
     * barrier.
     */
    private static boolean isAccessible(Class<?>[] barriers, Class<?> from, Class<?> to) {
        if ((to == null) || !from.isAssignableFrom(to)) {
            // no way to reach each other by walking up
            return false;
        }

        for (int i = 0; i < barriers.length; i++) {
            if (to == barriers[i]) {
                return false;
            }
        }

        if (from == to) {
            return true;
        }

        //
        // depth first search
        //
        if (isAccessible(barriers, from, to.getSuperclass())) {
            return true;
        }

        Class[] interfaces = to.getInterfaces();

        for (int i = 0; i < interfaces.length; i++) {
            if (isAccessible(barriers, from, interfaces[i])) {
                return true;
            }
        }

        return false;
    }

    /** based on type decides whether the class accepts or not anObject
     * @param from the base type of the query
     * @param to depending on value of type either Object, Class or Item
     * @param type 0,1,2 for Object, Class or Item
     * @return true if we can access the to from from by walking around the bariers
     */
    private final boolean isObjectAccessible(Class from, Object to, int type) {
        if (to == null) {
            return false;
        }

        return isObjectAccessible(classes(), from, to, type);
    }

    /** based on type decides whether the class accepts or not anObject
     * @param barriers classes to avoid when testing reachability
     * @param from the base type of the query
     * @param to depending on value of type either Object, Class or Item
     * @param type 0,1,2 for Object, Class or Item
     * @return true if we can access the to from from by walking around the bariers
     */
    static final boolean isObjectAccessible(Class[] barriers, Class from, Object to, int type) {
        if (to == null) {
            return false;
        }

        switch (type) {
        case 0:
            return isAccessible(barriers, from, to.getClass());

        case 1:
            return isAccessible(barriers, from, (Class) to);

        case 2: {
            Item item = (Item) to;

            return isAccessible(barriers, from, item.getType());
        }

        default:
            throw new IllegalStateException("Type: " + type);
        }
    }

    /** Filters collection accroding to set of given filters.
     */
    final <E, T extends Collection<E>> T filter(
        Class<?>[] arr, Class<?> from, T c, int type, T prototype
    ) {
        T ret = null;


// optimistic strategy expecting we will not need to filter
TWICE: 
        for (;;) {
            Iterator<E> it = c.iterator();
BIG: 
            while (it.hasNext()) {
                E res = it.next();

                if (!isObjectAccessible(arr, from, res, type)) {
                    if (ret == null) {
                        // we need to restart the scanning again 
                        // as there is an active filter
                        ret = prototype;
                        continue TWICE;
                    }

                    continue BIG;
                }

                if (ret != null) {
                    // if we are running the second round from TWICE
                    ret.add(res);
                }
            }

            // ok, processed
            break TWICE;
        }

        return (ret != null) ? ret : c;
    }

    /** Delegating result that filters unwanted items and instances.
     */
    private final class R<T> extends WaitableResult<T> implements LookupListener {
        private Result<T> result;
        private WeakResult<T> weak;
        private Object listeners;
        private Template from;

        R(Template from, Result<T> delegate) {
            this.from = from;
            this.result = delegate;
            this.weak = new WeakResult<T>(this, delegate);
        }

        protected void beforeLookup(Template t) {
            if (result instanceof WaitableResult) {
                ((WaitableResult) result).beforeLookup(t);
            }
        }

        public void addLookupListener(LookupListener l) {
            boolean add;

            synchronized (this) {
                listeners = AbstractLookup.modifyListenerList(true, l, listeners);
                add = listeners != null;
            }

            if (add) {
                result.addLookupListener(weak);
            }
        }

        public void removeLookupListener(LookupListener l) {
            boolean remove;

            synchronized (this) {
                listeners = AbstractLookup.modifyListenerList(false, l, listeners);
                remove = listeners == null;
            }

            if (remove) {
                result.removeLookupListener(weak);
            }
        }

        @Override
        public Collection<? extends T> allInstances() {
            return openCol(result.allInstances(), 0);
        }
        @Override
        protected Collection<? extends T> allInstances(boolean ignore) {
            return allInstances();
        }

        private <S> Collection<S> openCol(Collection<S> c, int type) {
            return filter(classes(), from.getType(), c, type, new ArrayList<S>(c.size()));
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            return filter(classes(), from.getType(), result.allClasses(), 1, new HashSet<Class<? extends T>>());
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            return openCol(result.allItems(), 2);
        }
        @Override
        public Collection<? extends Item<T>> allItems(boolean ignore) {
            return allItems();
        }

        public void resultChanged(org.openide.util.LookupEvent ev) {
            if (ev.getSource() == result) {
                collectFires(null);
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            LookupListener[] arr;

            synchronized (this) {
                if (listeners == null) {
                    return;
                }

                if (listeners instanceof LookupListener) {
                    arr = new LookupListener[] { (LookupListener) listeners };
                } else {
                    ArrayList<?> l = (ArrayList<?>) listeners;
                    arr = l.toArray(new LookupListener[0]);
                }
            }

            final LookupListener[] ll = arr;
            final org.openide.util.LookupEvent newev = new org.openide.util.LookupEvent(this);
            AbstractLookup.notifyListeners(ll, newev, evAndListeners);
        }
    } // end of R
    
    private final class WeakResult<T> extends WaitableResult<T> implements LookupListener {
        private final Lookup.Result source;
        private final Reference<R<T>> result;
        /** @GuardedBy(ExcludingLookup.this) */
        private WeakResult<?> next;
        
        public WeakResult(R<T> r, Lookup.Result<T> s) {
            this.result = new WeakReference<R<T>>(r);
            this.source = s;
        }
        
        protected void beforeLookup(Lookup.Template t) {
            R r = (R)result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                source.removeLookupListener(this);
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            R<T> r = result.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            } else {
                source.removeLookupListener(this);
            }
        }

        public void addLookupListener(LookupListener l) {
            assert false;
        }

        public void removeLookupListener(LookupListener l) {
            assert false;
        }

        public Collection<T> allInstances() {
            assert false;
            return null;
        }
        protected Collection<T> allInstances(boolean ignore) {
            assert false;
            return null;
        }

        public void resultChanged(LookupEvent ev) {
            R r = (R)result.get();
            if (r != null) {
                r.resultChanged(ev);
            } else {
                source.removeLookupListener(this);
            }
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            assert false;
            return null;
        }
        @Override
        protected Collection<? extends Item<T>> allItems(boolean callBeforeLookup) {
            return allItems();
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            assert false;
            return null;
        }

    } // end of WeakResult
}
