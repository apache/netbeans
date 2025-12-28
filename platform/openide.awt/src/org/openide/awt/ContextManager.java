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

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Item;
import org.openide.util.Lookup.Provider;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/** Listener on a global context.
 */
class ContextManager extends Object {
    private static final Logger LOG = GeneralAction.LOG;
    
    private static final Map<LookupRef, Reference<ContextManager>> CACHE = new HashMap<LookupRef, Reference<ContextManager>>();
    private static final Map<LookupRef, Reference<ContextManager>> SURVIVE = new HashMap<LookupRef, Reference<ContextManager>>();

    private Map<Class,LSet> listeners;
    private Lookup lookup;
    private LSet<Lookup.Provider> selectionAll;
    
    private ContextManager(Lookup lookup) {
        this.listeners = new HashMap<Class,LSet>();
        this.lookup = lookup;
    }
    
    public static ContextManager findManager(Lookup context, boolean survive) {
        synchronized (CACHE) {
            Map<LookupRef, Reference<ContextManager>> map = survive ? SURVIVE : CACHE;
            LookupRef lr = new LookupRef(context);
            Reference<ContextManager> ref = map.get(lr);
            ContextManager g = ref == null ? null : ref.get();
            if (g == null) {
                g = survive ? new SurviveManager(context) : new ContextManager(context);
                ref = new GMReference(g, lr, survive);
                map.put(lr, ref);
            }
            return g;
        }
    }
    
    static void clearCache(LookupRef lr, GMReference ref, boolean survive) {
        synchronized (CACHE) {
            Map<LookupRef, Reference<ContextManager>> map = survive ? SURVIVE : CACHE;
            if (map.get(lr) == ref) {
                map.remove(lr);
            }
        }
    }
    
    public <T> void registerListener(Class<T> type, ContextAction<T> a) {
        synchronized (CACHE) {
            LSet<T> existing = findLSet(type);
            if (existing == null) {
                Lookup.Result<T> result = createResult(lookup.lookupResult(type));
                existing = new LSet<T>(result, type);
                listeners.put(type, existing);
            }
            existing.add(a);
            // TBD: a.updateState(new ActionMap(), actionMap.get());
            
            if (a.selectMode == ContextSelection.ALL) {
                initSelectionAll();
                selectionAll.add(a);
            }
        }
    }

    public <T> void unregisterListener(Class<T> type, ContextAction<T> a) {
        synchronized (CACHE) {
            LSet<T> existing = findLSet(type);
            if (existing != null) {
                existing.remove(a);
                if (existing.isEmpty()) {
                    listeners.remove(type);
                    existing.cleanup();
                }
            }
            if (a.selectMode == ContextSelection.ALL && selectionAll != null) {
                selectionAll.remove(a);
                if (selectionAll.isEmpty() && !isSurvive()) {
                    selectionAll = null;
                }
            }
        }
    }
    
    /** Does not survive focus change */
    public boolean isSurvive() {
        return false;
    }

    /** Checks whether a type is enabled.
     */
    public <T> boolean isEnabled(Class<T> type, ContextSelection selectMode, ContextAction.Performer<? super T> enabler) {
        Lookup.Result<T> result = findResult(type);
        
        boolean e = isEnabledOnData(result, type, selectMode);
        if (enabler != null) {
            if (e) {
                List<? extends T> all = listFromResult(result);
                e = enabler.enabled(all, new LkpAE(all, type));
            }
        }
        
        return e;
    }
    
    /** Checks whether a type is enabled.
     */
    public <T> boolean runEnabled(Class<T> type, ContextSelection selectMode,  BiFunction<List<? extends T>, Lookup.Provider, Boolean> callback) {
        Lookup.Result<T> result = findResult(type);
        
        boolean e = isEnabledOnData(result, type, selectMode);
        if (e) {
            List<? extends T> all = listFromResult(result);
            e = callback.apply(all, new LkpAE(all, type));
        }
        
        return e;
    }

    private <T> boolean isEnabledOnData(Lookup.Result<T> result, Class<T> type, ContextSelection selectMode) {
        boolean res = isEnabledOnDataImpl(result, type, selectMode);
        LOG.log(Level.FINE, "isEnabledOnData(result, {0}, {1}) = {2}", new Object[]{type, selectMode, res});
        return res;
    }
    
    private <T> boolean isEnabledOnDataImpl(Lookup.Result<T> result, Class<T> type, ContextSelection selectMode) {
        switch (selectMode) {
            case EXACTLY_ONE:
                Collection<Lookup.Item<T>> instances = new HashSet<Lookup.Item<T>>(result.allItems());
                return instances.size() == 1;
            case ANY:
                return !result.allItems().isEmpty();
            case EACH: {
                if (result.allItems().isEmpty()) {
                    return false;
                }
                Lookup.Result<Lookup.Provider> items = lookup.lookupResult(Lookup.Provider.class);
                if (result.allItems().size() != items.allItems().size()) {
                    return false;
                }
                Lookup.Template<T> template = new Lookup.Template<T>(type);
                for (Lookup.Provider prov : items.allInstances()) {
                    if (prov.getLookup().lookupItem(template) == null) {
                        return false;
                    }
                }
                return true;
            }
            case ALL: {
                if (result.allItems().isEmpty()) {
                    return false;
                }
                Lookup.Result<Lookup.Provider> items = lookup.lookupResult(Lookup.Provider.class);
                if (result.allItems().size() < items.allItems().size()) {
                    return false;
                }
                Lookup.Template<T> template = new Lookup.Template<T>(type);
                for (Lookup.Provider prov : items.allInstances()) {
                    if (prov.getLookup().lookupItem(template) == null) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    // package private for tests only
    @SuppressWarnings("unchecked")
    <T> LSet<T> findLSet(Class<T> type) {
        synchronized (CACHE) {
            return listeners.get(type);
        }
    }
    private <T> Lookup.Result<T> findResult(final Class<T> type) {
        LSet<T> lset = findLSet(type);
        Lookup.Result<T> result;
        if (lset != null) {
            result = lset.result;
        } else {
            result = lookup.lookupResult(type);
        }
        return result;
    }
    
    protected <T> Lookup.Result<T> createResult(Lookup.Result<T> res) {
        return res;
    }
    
    private final class LkpAE<T> implements Lookup.Provider {
        final List<? extends T> all;
        final Class<T> type;
        public LkpAE(List<? extends T> all, Class<T> type) {
            this.all = all;
            this.type = type;
        }
        
        private Lookup lookup;
        public Lookup getLookup() {
            if (lookup == null) {
                lookup = new ProxyLookup(
                    Lookups.fixed(all.toArray()),
                    Lookups.exclude(ContextManager.this.lookup, type)
                );
            }
            return lookup;
        }
    }
    
    public <T> void actionPerformed(final ActionEvent e, ContextAction.Performer<? super T> perf, final Class<T> type, ContextSelection selectMode) {
        Lookup.Result<T> result = findResult(type);
        final List<? extends T> all = listFromResult(result);

        perf.actionPerformed(e, Collections.unmodifiableList(all), new LkpAE(all, type));
    }

    private <T> List<? extends T> listFromResult(Lookup.Result<T> result) {
        Collection<? extends T> col = result.allInstances();
        Collection<T> tmp = new LinkedHashSet<T>(col);
        if (tmp.size() != col.size()) {
            Collection<T> nt = new ArrayList<T>(tmp.size());
            nt.addAll(tmp);
            col = nt;
        }
        List<? extends T> all;
        if (col instanceof List) {
            all = (List<? extends T>)col;
        } else {
            ArrayList<T> arr = new ArrayList<T>();
            arr.addAll(col);
            all = arr;
        }
        return all;
    }

    private Lookup.Result<Lookup.Provider> initSelectionAll() {
        assert Thread.holdsLock(CACHE);
        if (selectionAll == null) {
            Lookup.Result<Lookup.Provider> result = lookup.lookupResult(Lookup.Provider.class);
            selectionAll = new LSet<Lookup.Provider>(result, Lookup.Provider.class);
        }
        return selectionAll.result;
    }

    
    private static final class GMReference extends WeakReference<ContextManager> 
    implements Runnable {
        private LookupRef context;
        private boolean survive;
        
        public GMReference(ContextManager m, LookupRef context, boolean survive) {
            super(m, Utilities.activeReferenceQueue());
            this.context = context;
            this.survive = survive;
        }
        
        public void run() {
            clearCache(context, this, survive);
        }
    } // end of GMReference

    /** Manager with special behaviour.
     */
    private static final class SurviveManager extends ContextManager {
        private SurviveManager(Lookup context) {
            super(context);
        }
        
        @Override
        public boolean isSurvive() {
            return true;
        }

        @Override
        protected <T> Result<T> createResult(Result<T> res) {
            return new NeverEmptyResult<T>(res, super.initSelectionAll());
        }
    }
    
    private static final class NeverEmptyResult<T> extends Lookup.Result<T> 
    implements LookupListener {
        private final Lookup.Result<T> delegate;
        private final Lookup.Result<Provider> nodes;
        private final Collection<LookupListener> listeners;
        private Collection<? extends Item<T>> allItems;
        private Collection<? extends T> allInstances;
        private Set<Class<? extends T>> allClasses;

        public NeverEmptyResult(Result<T> delegate, Result<Provider> nodes) {
            this.delegate = delegate;
            this.nodes = nodes;
            this.listeners = new CopyOnWriteArrayList<LookupListener>();
            // add weak listeners so this can be GCed when listeners are empty
            this.delegate.addLookupListener(WeakListeners.create(LookupListener.class, this, this.delegate));
            this.nodes.addLookupListener(WeakListeners.create(LookupListener.class, this, this.nodes));
            initValues();
        }

        @Override
        public void addLookupListener(LookupListener l) {
            listeners.add(l);
        }

        @Override
        public void removeLookupListener(LookupListener l) {
            listeners.remove(l);
        }

        @Override
        public Collection<? extends Item<T>> allItems() {
            Collection<? extends Item<T>> res = delegate.allItems();
            synchronized (this) {
                if (!res.isEmpty()) {
                    allItems = res;
                }
                return allItems;
            }
        }

        @Override
        public Collection<? extends T> allInstances() {
            Collection<? extends T> res = delegate.allInstances();
            synchronized (this) {
                if (!res.isEmpty()) {
                    allInstances = res;
                }
                return allInstances;
            }
        }

        @Override
        public Set<Class<? extends T>> allClasses() {
            Set<Class<? extends T>> res = delegate.allClasses();
            synchronized (this) {
                if (!res.isEmpty()) {
                    allClasses = res;
                }
                return allClasses;
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            if (ev.getSource() == nodes) {
                Collection<? extends Item<Provider>> arr = nodes.allItems();
                if (arr.size() == 1 && arr.iterator().next().getInstance() == null) {
                    return;
                }
                initValues();
                return;
            }
            final LookupEvent mev = new LookupEvent(this);
            for (LookupListener ll : listeners) {
                ll.resultChanged(mev);
            }
        }

        private synchronized void initValues() {
            allItems = Collections.emptyList();
            allInstances = Collections.emptyList();
            allClasses = Collections.emptySet();
        }

    } // end of NeverEmptyResult
    
    /** Special set, that is weakly holding its actions, but also
     * listens on changes in lookup.
     */
    static final class LSet<T> implements Set<ContextAction>, LookupListener, Runnable {
        final Lookup.Result<T> result;
        private final Set<ContextAction> delegate = Collections.newSetFromMap(new WeakHashMap<>());
        
        public LSet(Lookup.Result<T> context, Class<T> type) {
            this.result = context;
            this.result.addLookupListener(this);
            // activate listener
            this.result.allItems();
        }

        @Override
        public boolean add(ContextAction e) {
            assert e != null;
            return delegate.add(e);
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            Mutex.EVENT.readAccess(this);
        }
        
        @Override
        public void run() {
            ContextAction[] arr;
            synchronized (CACHE) {
                arr = toArray(new ContextAction[0]);
            }
            long now = 0; 
            assert (now = System.currentTimeMillis()) >= 0;
            for (ContextAction a : arr) {
                if (a != null) {
                    a.updateState();
                }
            }
            long took = 0;
            assert (took = System.currentTimeMillis() - now) >= 0;
            if (took > 2000) {
                LOG.log(Level.WARNING, "Updating state of {1} actions took {0} ms. here is the action list:", new Object[] { took, arr.length });
                for (ContextAction a : arr) {
                    LOG.log(Level.INFO, "  {0}", a);
                }
            }
        }

        private void cleanup() {
            this.result.removeLookupListener(this);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public Iterator<ContextAction> iterator() {
            return delegate.iterator();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends ContextAction> c) {
            return delegate.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }
    }

    static class LookupRef extends WeakReference<Lookup> {
        private final int hashCode;

        public LookupRef(Lookup referent) {
            super(referent);
            hashCode = System.identityHashCode(referent);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof LookupRef) {
                LookupRef lr = (LookupRef)obj;
                return get() == lr.get();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }
}

