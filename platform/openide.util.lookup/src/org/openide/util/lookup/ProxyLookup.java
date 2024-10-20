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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/** Implementation of lookup that can delegate to others.
 *
 * @author  Jaroslav Tulach
 * @since 1.9
 */
public class ProxyLookup extends Lookup {
    /** data representing the state of the lookup */
    private ImmutableInternalData data;

    /** Create a proxy to some other lookups.
     * @param lookups the initial delegates
     */
    public ProxyLookup(Lookup... lookups) {
        data = ImmutableInternalData.EMPTY.setLookupsNoFire(lookups, true);
    }
    /**
     * Create a {@code ProxyLookup} whose contents can be set dynamically 
     * subclassing. The passed
     * {@link Controller} can be later be used to call
     * {@link Controller#setLookups} which then 
     * {@link ProxyLookup#setLookups changes} the lookups this {@code ProxyLookup} 
     * delegates to. The passed controller may
     * only be used for <i>one</i> ProxyLookup.
     *
     * @param controller A {@link Controller} which can be used to set the lookups
     * @throws IllegalStateException if the passed controller has already
     * been attached to another ProxyLookup 
     * @since 8.43
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public ProxyLookup(Controller controller) {
        this();
        controller.setProxyLookup(this);
    }

    /**
     * Create a lookup initially proxying to no others.
     * Permits serializable subclasses.
     * @since 3.27
     */
    protected ProxyLookup() {
        data = ImmutableInternalData.EMPTY;
    }

    @Override
    public synchronized String toString() {
        return "ProxyLookup(class=" + getClass() + ")->" + Arrays.asList(getData().getLookups(false)); // NOI18N
    }

    /** Getter for the delegates.
    * @return the array of lookups we delegate to
    * @since 1.19
    */
    protected final Lookup[] getLookups() {
        synchronized (ProxyLookup.this) {
            return getData().getLookups(true);
        }
    }

    private Set<Lookup> identityHashSet(Collection<Lookup> current) {
        Map<Lookup,Void> map = new IdentityHashMap<Lookup, Void>();
        for (Lookup lookup : current) {
            map.put(lookup, null);
        }
        return map.keySet();
    }

    /**
     * A controller which allows the set of lookups being proxied to be
     * set dynamically for those who create the instance of
     * {@link ProxyLookup}.
     *
     * @since 8.43
     */
    public static final class Controller {

        private ProxyLookup consumer;

        /**
         * Creates a new controller to be attached to a {@link ProxyLookup}.
         * @since 8.43
         */
        public Controller() {
        }

        /**
         * Set the lookups on the {@link ProxyLookup} this controller controls.
         * If called before a {@link ProxyLookup} has been attached to this
         * controller, an IllegalStateException will be thrown.
         *
         * @param notifyIn an executor to notify changes in
         * @param lookups an array of Lookups to be proxied
         * @throws IllegalStateException if called before this instance
         * has been passed to the constructor of (exactly one) {@link ProxyLookup}
         * @since 8.43
         */
        public void setLookups(Executor notifyIn, Lookup... lookups) {
            if (consumer == null) {
                throw new IllegalStateException("Cannot use Controller until "
                        + "a ProxyLookup has been created with it.");
            }
            consumer.setLookups(notifyIn, lookups);
        }

        /**
         * Set the lookups on the {@link ProxyLookup} this controller controls.
         * If called before a {@link ProxyLookup} has been attached to this
         * controller, an IllegalStateException will be thrown.
         *
         * @param lookups An array of Lookups to be proxied
         * @throws IllegalStateException if called before this instance
         * has been passed to the constructor of (exactly one) {@link ProxyLookup}
         * @since 8.43
         */
        public void setLookups(Lookup... lookups) {
            if (consumer == null) {
                throw new IllegalStateException("Cannot use Controller until "
                        + "a ProxyLookup has been created with it.");
            }
            setLookups(null, lookups);
        }

        void setProxyLookup(ProxyLookup lkp) {
            if (consumer != null) {
                throw new IllegalStateException("Controller cannot be used "
                        + "with more than one ProxyLookup.");
            }
            consumer = lkp;
        }
    }

    /**
     * Changes the delegates.
     *
     * @param lookups the new lookups to delegate to
     * @since 1.19 protected
     */
    protected final void setLookups(Lookup... lookups) {
        setLookups(null, lookups);
    }

    /**
     * Changes the delegates immediatelly, notifies the listeners in provided
     * executor, potentially later.
     *
     * @param lookups the new lookups to delegate to
     * @param notifyIn executor to deliver the notification to listeners or null
     * @since 7.16
     */
    protected final void setLookups(Executor notifyIn, Lookup... lookups) {
        Collection<Reference<R>> arr;
        Set<Lookup> newL;
        Set<Lookup> current;
        Lookup[] old;

        Map<Result<?>,LookupListener> toRemove = new IdentityHashMap<>();
        Map<Result<?>,LookupListener> toAdd = new IdentityHashMap<>();

        ImmutableInternalData orig;
        synchronized (ProxyLookup.this) {
            orig = getData();
            ImmutableInternalData newData = getData().setLookupsNoFire(lookups, false);
            if (newData == getData()) {
                return;
            }
            arr = setData(newData, lookups, toAdd, toRemove);
        }

        // better to do this later than in synchronized block
        for (Map.Entry<Result<?>, LookupListener> e : toRemove.entrySet()) {
            e.getKey().removeLookupListener(e.getValue());
        }
        for (Map.Entry<Result<?>, LookupListener> e : toAdd.entrySet()) {
            e.getKey().addLookupListener(e.getValue());
        }


        // this cannot be done from the synchronized block
        final List<Object> evAndListeners = new ArrayList<>();
        for (Reference<R> ref : arr) {
            R<?> r = ref.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            }
        }

        class Notify implements Runnable {
            public void run() {
                Iterator<?> it = evAndListeners.iterator();
                while (it.hasNext()) {
                    LookupEvent ev = (LookupEvent)it.next();
                    LookupListener l = (LookupListener)it.next();
                    try {
                        l.resultChanged(ev);
                    } catch (RuntimeException x) {
                        Logger.getLogger(ProxyLookup.class.getName()).log(Level.WARNING, null, x);
                    }
                }
            }
        }
        Notify n = new Notify();
        if (notifyIn == null) {
            n.run();
        } else {
            notifyIn.execute(n);
        }
    }

    /** Notifies subclasses that a query is about to be processed.
     * Subclasses can update its state before the actual processing
     * begins. It is allowed to call <code>setLookups</code> method
     * to change/update the set of objects the proxy delegates to.
     *
     * @param template the template of the query
     * @since 1.31
     */
    protected void beforeLookup(Template<?> template) {
    }

    // mostly for testing purposes
    void beforeLookup(boolean call, Template<?> template) {
        if (call) {
            beforeLookup(template);
        }
    }

    public final <T> T lookup(Class<T> clazz) {
        beforeLookup(new Template<T>(clazz));

        Lookup[] tmpLkps;
        synchronized (ProxyLookup.this) {
            tmpLkps = getData().getLookups(false);
        }

        for (int i = 0; i < tmpLkps.length; i++) {
            T o = tmpLkps[i].lookup(clazz);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    @Override
    public final <T> Item<T> lookupItem(Template<T> template) {
        beforeLookup(template);

        Lookup[] tmpLkps;
        synchronized (ProxyLookup.this) {
            tmpLkps = getData().getLookups(false);
        }

        for (int i = 0; i < tmpLkps.length; i++) {
            Item<T> o = tmpLkps[i].lookupItem(template);

            if (o != null) {
                return o;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> R<T> convertResult(R r) {
        return (R<T>)r;
    }

    public final <T> Result<T> lookup(Lookup.Template<T> template) {
        synchronized (ProxyLookup.this) {
            ImmutableInternalData[] res = { null };
            R<T> newR = getData().findResult(this, res, template);
            setData(res[0], getData().getLookups(false), null, null);
            return newR;
        }
    }

    /** Unregisters a template from the has map.
     */
    private final void unregisterTemplate(Template<?> template) {
        synchronized (ProxyLookup.this) {
            ImmutableInternalData id = getData();
            if (id == null) {
                return;
            }
            setData(id.removeTemplate(this, template), getData().getLookups(false), null, null);
        }
    }

    private ImmutableInternalData getData() {
        assert Thread.holdsLock(this);
        return data;
    }

    private Collection<Reference<R>> setData(
        ImmutableInternalData newData, Lookup[] current,
        Map<Result<?>,LookupListener> toAdd, Map<Result<?>,LookupListener> toRemove
    ) {
        assert Thread.holdsLock(ProxyLookup.this);
        assert newData != null;

        ImmutableInternalData previous = this.getData();

        if (previous == newData) {
            return Collections.emptyList();
        }

        if (newData.isEmpty()) {
            this.setData(newData);
            // no affected results => exit
            return Collections.emptyList();
        }

        Collection<Reference<R>> arr = newData.references();

        Set<Lookup> removed = identityHashSet(previous.getLookupsList());
        Set<Lookup> currentSet = identityHashSet(Arrays.asList(current));
        Set<Lookup> newL = identityHashSet(currentSet);
        removed.removeAll(currentSet); // current contains just those lookups that have disappeared
        newL.removeAll(previous.getLookupsList()); // really new lookups

        for (Reference<R> ref : arr) {
            R<?> r = ref.get();
            if (r != null) {
                r.lookupChange(newData, current, previous, newL, removed, toAdd, toRemove);
                if (this.getData() != previous) {
                    // the data were changed by an re-entrant call
                    // skip any other change processing, as it is not needed
                    // anymore
                }
            }
        }
                for (Reference<R> ref : arr) {
            R<?> r = ref.get();
            if (r != null) {
                r.data = newData;
            }
        }
        this.setData(newData);
        return arr;
    }

    private void setData(ImmutableInternalData data) {
        this.data = data;
    }

    /** Result of a lookup request. Allows access to single object
     * that was found (not too useful) and also to all objects found
     * (more useful).
     */
    private static final class R<T> extends WaitableResult<T> {
        /** weak listener & result */
        private final WeakResult<T> weakL;

        /** list of listeners added */
        private LookupListenerList listeners;

        /** collection of Objects */
        private Collection[] cache;


        /** associated lookup */
        private ImmutableInternalData data;

        /** Constructor.
         */
        public R(ProxyLookup proxy, Lookup.Template<T> t) {
            this.weakL = new WeakResult<T>(proxy, this, t);
        }

        private ProxyLookup proxy() {
            return weakL.result.proxy;
        }

        @SuppressWarnings("unchecked")
        private Result<T>[] newResults(int len) {
            return new Result[len];
        }

        @Override
        protected void finalize() {
            weakL.result.run();
        }

        /** initializes the results
         */
        private Result<T>[] initResults() {
            BIG_LOOP: for (;;) {
                Lookup[] myLkps;
                ImmutableInternalData current;
                synchronized (proxy()) {
                    if (weakL.getResults() != null) {
                        return weakL.getResults();
                    }
                    myLkps = data.getLookups(false);
                    current = data;
                }

                Result<T>[] arr = newResults(myLkps.length);

                for (int i = 0; i < arr.length; i++) {
                    arr[i] = myLkps[i].lookup(template());
                }

                synchronized (proxy()) {
                    if (current != data) {
                        continue;
                    }

                    Lookup[] currentLkps = data.getLookups(false);
                    if (currentLkps.length != myLkps.length) {
                        continue BIG_LOOP;
                    }
                    for (int i = 0; i < currentLkps.length; i++) {
                        if (currentLkps[i] != myLkps[i]) {
                            continue BIG_LOOP;
                        }
                    }

                    // some other thread might compute the result mean while.
                    // if not finish the computation yourself
                    if (weakL.getResults() != null) {
                        return weakL.getResults();
                    }

                    weakL.setResults(arr);
                }
                for (int i = 0; i < arr.length; i++) {
                    arr[i].addLookupListener(weakL);
                }
                return arr;
            }
        }

        /** Called when there is a change in the list of proxied lookups.
         * @param current array of current lookups
         * @param oldData
         * @param added set of added lookups
         * @param removed set of removed lookups
         */
        final void lookupChange(
            ImmutableInternalData newData, Lookup[] current, ImmutableInternalData oldData,
            Set<Lookup> added, Set<Lookup> removed,
            Map<Result<?>,LookupListener> toAdd, Map<Result<?>,LookupListener> toRemove
        ) {
            if (weakL.getResults() == null) {
                // not computed yet, do not need to do anything
                return;
            }

            Lookup[] old = oldData.getLookups(false);

            // map (Lookup, Lookup.Result)
            Map<Lookup, Result<T>> map = new IdentityHashMap<>(old.length * 2);

            for (int i = 0; i < old.length; i++) {
                if (removed.contains(old[i])) {
                    // removed lookup
                    if (toRemove != null) {
                        toRemove.put(weakL.getResults()[i], weakL);
                    }
                } else {
                    // remember the association
                    map.put(old[i], weakL.getResults()[i]);
                }
            }

            Lookup.Result<T>[] arr = newResults(current.length);

            for (int i = 0; i < current.length; i++) {
                if (added.contains(current[i])) {
                    // new lookup
                    arr[i] = current[i].lookup(template());
                    if (toAdd != null) {
                        toAdd.put(arr[i], weakL);
                    }
                } else {
                    // old lookup
                    arr[i] = map.get(current[i]);

                    if (arr[i] == null) {
                        // assert
                        throw new IllegalStateException();
                    }
                }
            }

            // remember the new results
            weakL.setResults(arr);
        }

        /** Just delegates.
         */
        public void addLookupListener(LookupListener l) {
            synchronized (proxy()) {
                if (listeners == null) {
                    listeners = new LookupListenerList();
                }
            }

            listeners.add(l);
            initResults();
        }

        /** Just delegates.
         */
        public void removeLookupListener(LookupListener l) {
            LookupListenerList listenersLocal;
            synchronized (proxy()) {
                listenersLocal = listeners;
            }
            if (listenersLocal != null) {
                listenersLocal.remove(l);
            }
        }

        /** Access to all instances in the result.
         * @return collection of all instances
         */
        @Override
        public java.util.Collection<T> allInstances() {
            return allInstances(true);
        }
        @SuppressWarnings("unchecked")
        protected java.util.Collection<T> allInstances(boolean callBeforeLookup) {
            return computeResult(0, callBeforeLookup);
        }

        /** Classes of all results. Set of the most concreate classes
         * that are registered in the system.
         * @return set of Class objects
         */
        @SuppressWarnings("unchecked")
        @Override
        public java.util.Set<Class<? extends T>> allClasses() {
            return (java.util.Set<Class<? extends T>>) computeResult(1, true);
        }

        /** All registered items. The collection of all pairs of
         * ii and their classes.
         * @return collection of Lookup.Item
         */
        @Override
        public java.util.Collection<? extends Item<T>> allItems() {
            return allItems(true);
        }
        @SuppressWarnings("unchecked")
        @Override
        public java.util.Collection<? extends Item<T>> allItems(boolean callBeforeLookup) {
            return computeResult(2, callBeforeLookup);
        }

        /** Computes results from proxied lookups.
         * @param indexToCache 0 = allInstances, 1 = allClasses, 2 = allItems
         * @return the collection or set of the objects
         */
        private Collection computeResult(int indexToCache, boolean callBeforeLookup) {
            Collection cachedResult = null;
            synchronized (proxy()) {
                Collection[] cc = getCache();
                if (cc != null && cc != R.NO_CACHE) {
                    cachedResult = cc[indexToCache];
                }
            }
            // if caches exist, wait for finished
            Lookup.Result<T>[] arr = myBeforeLookup(callBeforeLookup, cachedResult != null);
            // use caches, if they exist
            Collection[] cc;
            synchronized (proxy()) {
                cc = getCache();
                if (cc != null && cc != R.NO_CACHE) {
                    cachedResult = cc[indexToCache];
                }
            }
            if (cachedResult != null) {
                return cachedResult;
            }
            if (indexToCache == 1) {
                return new LazySet(this, cc, indexToCache, callBeforeLookup, arr);
            }
            return new LazyList(this, cc, indexToCache, callBeforeLookup, arr);
        }

        /** When the result changes, fire the event.
         */
        public void resultChanged(LookupEvent ev) {
            collectFires(null);
        }

        private static ThreadLocal<R<?>> IN = new ThreadLocal<>();
        protected void collectFires(Collection<Object> evAndListeners) {
            R<?> prev = IN.get();
            if (this == prev) {
//                Thread.dumpStack();
                return;
            }
            try {
                IN.set(this);
                collImpl(evAndListeners);
            } finally {
                IN.set(prev);
            }
        }

        private void collImpl(Collection<Object> evAndListeners) {
            boolean modified = true;

            final Object[] ll;
            try {
                // clear cached instances
                Collection oldItems;
                Collection oldInstances;
                synchronized (proxy()) {
                    final Collection[] cc = getCache();
                    if (cc == NO_CACHE) {
                        return;
                    }

                    oldInstances = cc == null ? null : cc[0];
                    oldItems = cc == null ? null : cc[2];


                    if (listeners == null || listeners.getListenerCount() == 0) {
                        // clear the cache
                        setCache(new Collection[3]);
                        return;
                    }
                    ll = listeners.getListenerList();
                    assert ll != null;


                    // ignore events if they arrive as a result of call to allItems
                    // or allInstances, bellow...
                    setCache(NO_CACHE);
                }

                if (oldItems != null) {
                    Collection<? extends Item<T>> newItems = allItems(false);
                    if (newItems != null && newItems.size() == oldItems.size()) {
                        if (oldItems.equals(newItems)) {
                            modified = false;
                        }
                    }
                } else {
                    if (oldInstances != null) {
                        Collection newInstances = allInstances(false);
                        if (newInstances != null && newInstances.size() == oldInstances.size()) {
                            if (oldInstances.equals(newInstances)) {
                                modified = false;
                            }
                        }
                    } else {
                        Collection<? extends Item<T>> newItems = allItems(false);
                        if (newItems.isEmpty()) {
                            modified = false;
                        }
                        synchronized (proxy()) {
                            if (getCache() == NO_CACHE) {
                                // we have to initialize the cache
                                // to show that the result has been initialized
                                setCache(new Collection[3]);
                            }
                        }
                    }
                }
            } finally {
                synchronized (proxy()) {
                    if (getCache() == NO_CACHE) {
                        setCache(null);
                    }
                }
            }

            if (modified) {
                LookupEvent ev = new LookupEvent(this);
                AbstractLookup.notifyListeners(ll, ev, evAndListeners);
            }
        }

        /** Implementation of my before lookup.
         * @return results to work on.
         */
        private Lookup.Result<T>[] myBeforeLookup(
            boolean callBeforeLookup, boolean callBeforeOnWait
        ) {
            Template<T> template = template();

            proxy().beforeLookup(callBeforeLookup, template);

            Lookup.Result<T>[] arr = initResults();

            if (callBeforeOnWait) {
                // invoke update on the results
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] instanceof WaitableResult) {
                        WaitableResult w = (WaitableResult) arr[i];
                        w.beforeLookup(template);
                    }
                }
            }

            return arr;
        }

        /** Used by proxy results to synchronize before lookup.
         */
        @Override
        protected void beforeLookup(Lookup.Template t) {
            if (t.getType() == template().getType()) {
                myBeforeLookup(true, true);
            }
        }

        private Collection[] getCache() {
            return cache;
        }

        private void setCache(Collection[] cache) {
            assert Thread.holdsLock(proxy());
            this.cache = cache;
        }
        private static final Collection[] NO_CACHE = new Collection[0];

        private Template<T> template() {
            return weakL.result.template;
        }

        private void updateResultCache(Object[] oldCC, int indexToCache, Result[] arr, Collection<Object> ret) {
            synchronized (proxy()) {
                Collection[] cc = getCache();
                if (cc != oldCC) {
                    // don't change the cache when it is based on
                    // outdated results
                    return;
                }

                if (cc == null || cc == R.NO_CACHE) {
                    // initialize the cache to indicate this result is in use
                    setCache(cc = new Collection[3]);
                }

                if (arr == weakL.getResults()) {
                    // updates the results, if the results have not been
                    // changed during the computation of allInstances
                    cc[indexToCache] = ret;
                }
            }

        }
    }
    private static final class WeakRef<T> extends WeakReference<R> implements Runnable {
        final WeakResult<T> result;
        final ProxyLookup proxy;
        final Template<T> template;

        public WeakRef(R r, WeakResult<T> result, ProxyLookup proxy, Template<T> template) {
            super(r);
            this.result = result;
            this.template = template;
            this.proxy = proxy;
        }

        public void run() {
            result.removeListeners();
            proxy.unregisterTemplate(template);
        }
    }


    private static final class WeakResult<T> extends WaitableResult<T> implements LookupListener, Runnable {
        /** all results */
        private Lookup.Result<T>[] results;
        private final WeakRef<T> result;

        public WeakResult(ProxyLookup proxy, R r, Template<T> t) {
            this.result = new WeakRef<T>(r, this, proxy, t);
        }

        final void removeListeners() {
            Lookup.Result<T>[] arr = this.getResults();
            if (arr == null) {
                return;
            }

            for(int i = 0; i < arr.length; i++) {
                arr[i].removeLookupListener(this);
            }
        }

        protected void beforeLookup(Lookup.Template t) {
            R r = result.get();
            if (r != null) {
                r.beforeLookup(t);
            } else {
                removeListeners();
            }
        }

        protected void collectFires(Collection<Object> evAndListeners) {
            R<?> r = result.get();
            if (r != null) {
                r.collectFires(evAndListeners);
            } else {
                removeListeners();
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

        public void resultChanged(LookupEvent ev) {
            R r = result.get();
            if (r != null) {
                r.resultChanged(ev);
            } else {
                removeListeners();
            }
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            assert false;
            return null;
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            assert false;
            return null;
        }

        public void run() {
            removeListeners();
        }

        private Lookup.Result<T>[] getResults() {
            return results;
        }

        private void setResults(Lookup.Result<T>[] results) {
            this.results = results;
        }

        @Override
        protected Collection<? extends Object> allInstances(boolean callBeforeLookup) {
            return allInstances();
        }
        @Override
        protected Collection<? extends Item<T>> allItems(boolean callBeforeLookup) {
            return allItems();
        }
    } // end of WeakResult

    abstract static class ImmutableInternalData extends Object {
        static final ImmutableInternalData EMPTY = new EmptyInternalData();
        static final Lookup[] EMPTY_ARR = new Lookup[0];


        protected ImmutableInternalData() {
        }

        public static ImmutableInternalData create(Object lkp, Map<Template, Reference<R>> results) {
            if (results.size() == 0 && lkp == EMPTY_ARR) {
                return EMPTY;
            }
            if (results.size() == 1) {
                Entry<Template,Reference<R>> e = results.entrySet().iterator().next();
                return new SingleInternalData(lkp, e.getKey(), e.getValue());
            }

            return new RealInternalData(lkp, results);
        }

        protected abstract boolean isEmpty();
        protected abstract Map<Template, Reference<R>> getResults();
        protected abstract Object getRawLookups();

        final Collection<Reference<R>> references() {
            return getResults().values();
        }

        final <T> ImmutableInternalData removeTemplate(ProxyLookup proxy, Template<T> template) {
            if (getResults().containsKey(template)) {
                HashMap<Template,Reference<R>> c = new HashMap<Template, Reference<ProxyLookup.R>>(getResults());
                Reference<R> ref = c.remove(template);
                if (ref != null && ref.get() != null) {
                    // seems like there is a reference to a result for this template
                    // thta is still alive
                    return this;
                }
                return create(getRawLookups(), c);
            } else {
                return this;
            }
        }

        <T> R<T> findResult(ProxyLookup proxy, ImmutableInternalData[] newData, Template<T> template) {
            assert Thread.holdsLock(proxy);

            Map<Template,Reference<R>> map = getResults();

            Reference<R> ref = map.get(template);
            R r = (ref == null) ? null : ref.get();

            if (r != null) {
                newData[0] = this;
                return convertResult(r);
            }

            HashMap<Template, Reference<R>> res = new HashMap<Template, Reference<R>>(map);
            R<T> newR = new R<T>(proxy, template);
            res.put(template, new java.lang.ref.SoftReference<R>(newR));
            newR.data = newData[0] = create(getRawLookups(), res);
            return newR;
        }
        final ImmutableInternalData setLookupsNoFire(Lookup[] lookups, boolean skipCheck) {
            Object l;

            if (!skipCheck) {
                Lookup[] previous = getLookups(false);
                if (previous == lookups) {
                    return this;
                }

                if (previous.length == lookups.length) {
                    int same = 0;
                    for (int i = 0; i < previous.length; i++) {
                        if (lookups[i] != previous[i]) {
                            break;
                        }
                        same++;
                    }
                    if (same == previous.length) {
                        return this;
                    }
                }
            }

            if (lookups.length == 1) {
                l = lookups[0];
                assert l != null : "Cannot assign null delegate";
            } else {
                if (lookups.length == 0) {
                    l = EMPTY_ARR;
                } else {
                    l = lookups.clone();
                }
            }

            if (isEmpty() && l == EMPTY_ARR) {
                return this;
            }

            return create(l, getResults());
        }
        final Lookup[] getLookups(boolean clone) {
            Object l = this.getRawLookups();
            if (l instanceof Lookup) {
                return new Lookup[] { (Lookup)l };
            } else {
                Lookup[] arr = (Lookup[])l;
                if (clone) {
                    arr = arr.clone();
                }
                return arr;
            }
        }
        final List<Lookup> getLookupsList() {
            return Arrays.asList(getLookups(false));
        }

    } // end of ImmutableInternalData

    private static final class SingleInternalData extends ImmutableInternalData {
        /** lookups to delegate to (either Lookup or array of Lookups) */
        private final Object lookups;
        private final Template template;
        private final Reference<ProxyLookup.R> result;

        public SingleInternalData(Object lookups, Template<?> template, Reference<ProxyLookup.R> result) {
            this.lookups = lookups;
            this.template = template;
            this.result = result;
        }

        protected final boolean isEmpty() {
            return false;
        }

        protected Map<Template, Reference<R>> getResults() {
            return Collections.singletonMap(template, result);
        }

        protected Object getRawLookups() {
            return lookups;
        }
    }
    private static final class RealInternalData extends ImmutableInternalData {
        /** lookups to delegate to (either Lookup or array of Lookups) */
        private final Object lookups;

        /** map of templates to currently active results */
        private final Map<Template,Reference<R>> results;

        public RealInternalData(Object lookups, Map<Template, Reference<ProxyLookup.R>> results) {
            this.results = results;
            this.lookups = lookups;
        }

        @Override
        protected final boolean isEmpty() {
            return false;
        }

        @Override
        protected Map<Template, Reference<R>> getResults() {
            boolean needsStrict = false;
            assert needsStrict = true;
            return needsStrict && !isUnmodifiable(results) ? unmodifiableMap(results) : results;
        }

        @Override
        protected Object getRawLookups() {
            return lookups;
        }

        private static Class<?> unmodifiableClass;
        private static boolean isUnmodifiable(Map<?,?> map) {
            return map.getClass() == unmodifiableClass;
        }
        private static <K,V> Map<K,V> unmodifiableMap(Map<K,V> map) {
            Map<K,V> res = Collections.unmodifiableMap(map);
            if (unmodifiableClass == null) {
                unmodifiableClass = res.getClass();
            }
            return res;
        }
    }

    private static final class EmptyInternalData extends ImmutableInternalData {
        EmptyInternalData() {
        }

        protected final boolean isEmpty() {
            return true;
        }

        protected Map<Template, Reference<R>> getResults() {
            return Collections.emptyMap();
        }

        @Override
        protected Object getRawLookups() {
            return EMPTY_ARR;
        }
    } // end of EmptyInternalData

    private static class LazyCollection implements Collection {

        private R result;
        private final Object[] cc;
        private final int indexToCache;
        private final boolean callBeforeLookup;
        private final Lookup.Result<?>[] arr;
        /** GuardedBy("this") */
        private final Collection[] computed;
        /** GuardedBy("this") */
        private Collection delegate;

        public LazyCollection(R result, Object[] cc, int indexToCache, boolean callBeforeLookup, Lookup.Result[] arr) {
            this.result = result;
            this.indexToCache = indexToCache;
            this.cc = cc;
            this.callBeforeLookup = callBeforeLookup;
            this.arr = arr;
            this.computed = new Collection[arr.length];
        }

        final Collection delegate() {
            return delegate(true);
        }
        final Collection delegate(boolean computeIt) {
            Collection dlgt = null;
            for (;;) {
                synchronized (this) {
                    if (dlgt != null && delegate == null) {
                        delegate = dlgt;
                        result = null;
                    }
                    if (delegate != null) {
                        return delegate;
                    }
                    if (!computeIt) {
                        return null;
                    }
                }
                dlgt = computeDelegate(null);
            }
        }

        private Collection computeDelegate(int[] firstNonEmpty) {
            // initialize the collection to hold result
            Collection<Object> compute = null;
            Collection<Object> ret = null;

            if (firstNonEmpty == null || firstNonEmpty[0] == 0) {
                if (indexToCache == 1) {
                    HashSet<Object> s = new HashSet<Object>();
                    compute = s;
                    ret = Collections.unmodifiableSet(s);
                } else {
                    List<Object> l = new ArrayList<Object>(arr.length * 2);
                    compute = l;
                    ret = Collections.unmodifiableList(l);
                }
            }

            // fill the collection
            int i = firstNonEmpty == null ? 0 : firstNonEmpty[0];
            while (i < arr.length) {
                Collection one;
                synchronized (this) {
                    one = getComputed()[i];
                }
                if (one == null) {
                    if (firstNonEmpty != null && callBeforeLookup && arr[i] instanceof WaitableResult) {
                        WaitableResult<?> wr = (WaitableResult<?>) arr[i];
                        wr.beforeLookup(result.template());
                    }
                    one = computeSingleResult(i);
                    assert one != null;
                }
                boolean addAll = false;
                synchronized (this) {
                    if (getComputed()[i] == null) {
                        getComputed()[i] = one;
                    }
                    i++;
                    if (firstNonEmpty != null) {
                        firstNonEmpty[0] = i;
                        if (!one.isEmpty()) {
                            ret = one;
                            break;
                        }
                    } else {
                        addAll = true;
                    }
                }
                if (addAll) {
                    compute.addAll(one);
                }
            }
            if (i == arr.length && compute != null) {
                R r = result;
                if (r != null) {
                    r.updateResultCache(cc, indexToCache, arr, ret);
                }
                result = null;
            }
            return ret;
        }

        @Override
        public int size() {
            return delegate().size();
        }

        @Override
        public boolean isEmpty() {
            return delegate().isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate().contains(o);
        }

        @Override
        public Iterator iterator() {
            Collection c = delegate(false);
            return c != null ? c.iterator() : lazyIterator();
        }

        @Override
        public Object[] toArray() {
            return delegate().toArray();
        }

        @Override
        public Object[] toArray(Object[] a) {
            return delegate().toArray(a);
        }

        @Override
        public String toString() {
            return delegate().toString();
        }

        @Override
        public int hashCode() {
            return delegate().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return delegate().equals(obj);
        }

        @Override
        public boolean add(Object e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsAll(Collection c) {
            return delegate().containsAll(c);
        }

        @Override
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        private Iterator lazyIterator() {
            return new Iterator() {
                private Iterator current;
                private int[] indx = { 0 };
                @Override
                public boolean hasNext() {
                    for (;;) {
                        if (current != null && current.hasNext()) {
                            return true;
                        }
                        if (indx[0] == arr.length) {
                            return false;
                        }
                        // increments indx[0] at least by one
                        final Collection newIt = computeDelegate(indx);
                        if (newIt != null) {
                            current = newIt.iterator();
                        } else {
                            assert indx[0] == arr.length;
                            current = null;
                        }
                    }
                }

                @Override
                public Object next() {
                    if (hasNext()) {
                        return current.next();
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        private Collection computeSingleResult(int i) {
            Collection one = null;
            switch (indexToCache) {
                case 0:
                    if (!callBeforeLookup && arr[i] instanceof WaitableResult<?>) {
                        WaitableResult<?> wr = (WaitableResult<?>) arr[i];
                        one = wr.allInstances(callBeforeLookup);
                    } else {
                        one = arr[i].allInstances();
                    }
                    break;
                case 1:
                    one = arr[i].allClasses();
                    break;
                case 2:
                    if (!callBeforeLookup && arr[i] instanceof WaitableResult<?>) {
                        WaitableResult<?> wr = (WaitableResult<?>) arr[i];
                        one = wr.allItems(callBeforeLookup);
                    } else {
                        one = arr[i].allItems();
                    }
                    break;
                default:
                    assert false : "Wrong index: " + indexToCache;
            }
            return one;
        }

        private Collection[] getComputed() {
            assert Thread.holdsLock(this);
            return computed;
        }
    } // end of LazyCollection

    private static final class LazyList extends LazyCollection implements List {

        public LazyList(R data, Object[] cc, int indexToCache, boolean callBeforeLookup, Lookup.Result[] arr) {
            super(data, cc, indexToCache, callBeforeLookup, arr);
        }

        final List delegateList() {
            return (List) delegate();
        }

        @Override
        public Object get(int index) {
            return delegateList().get(index);
        }

        @Override
        public List subList(int fromIndex, int toIndex) {
            return delegateList().subList(fromIndex, toIndex);
        }

        @Override
        public int indexOf(Object o) {
            return delegateList().indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return delegateList().lastIndexOf(o);
        }

        @Override
        public ListIterator listIterator() {
            return delegateList().listIterator();
        }

        @Override
        public ListIterator listIterator(int index) {
            return delegateList().listIterator(index);
        }

        @Override
        public boolean addAll(int index, Collection c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object set(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, Object element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }
    } // end of LazyList

    private static final class LazySet extends LazyCollection implements Set {

        public LazySet(R data, Object[] cc, int indexToCache, boolean callBeforeLookup, Lookup.Result[] arr) {
            super(data, cc, indexToCache, callBeforeLookup, arr);
        }
    } // end of LazySet
}
