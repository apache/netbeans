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

package org.netbeans.spi.project.support;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectapi.MetaLookupMerger;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

class DelegatingLookupImpl extends ProxyLookup implements LookupListener, ChangeListener {

    private static final Logger LOG = Logger.getLogger(DelegatingLookupImpl.class.getName());

    private final Lookup baseLookup;
    private final String pathDescriptor;
    private final Controller unmergedController;
    private final ProxyLookup unmergedLookup;
    private final Map<LookupMerger<?>,Object> mergerResults = new HashMap<LookupMerger<?>,Object>();
    private final Lookup.Result<LookupProvider> providerResult;
    private final LookupListener providerListener;
    private List<LookupProvider> old = Collections.emptyList();
    private List<Lookup> currentLookups;
    private final ChangeListener metaMergerListener;
    @SuppressWarnings("rawtypes") private Lookup.Result<LookupMerger> mergers;
    private final Lookup.Result<MetaLookupMerger> metaMergers;
    private Reference<LookupListener> listenerRef;
    //#68623: the proxy lookup fires changes only if someone listens on a particular template:
    private final List<Lookup.Result<?>> results = new ArrayList<Lookup.Result<?>>();

    @SuppressWarnings("LeakingThisInConstructor")
    DelegatingLookupImpl(Lookup base, Lookup providerLookup, String pathDescriptor) {
        assert base != null;
        this.unmergedController = new ProxyLookup.Controller();
        this.unmergedLookup = new ProxyLookup(this.unmergedController);
        baseLookup = base;
        this.pathDescriptor = pathDescriptor;
        providerResult = providerLookup.lookupResult(LookupProvider.class);
        metaMergers = providerLookup.lookupResult(MetaLookupMerger.class);
        metaMergerListener = WeakListeners.change(this, null);
        assert isAllJustLookupProviders(providerLookup) : "Layer content at " + pathDescriptor + " contains other than LookupProvider instances! See messages.log file for more details.";
        doDelegate();
        providerListener = new LookupListener() {
            @Override public void resultChanged(LookupEvent ev) {
                // XXX this may need to be run asynchronously; deadlock-prone
                doDelegate();
            }
        };
        providerResult.addLookupListener(WeakListeners.create(LookupListener.class, providerListener, providerResult));
        metaMergers.addLookupListener(WeakListeners.create(LookupListener.class, providerListener, metaMergers));
    }

    @Override public void resultChanged(LookupEvent ev) {
        doDelegate();
    }

    @Override protected void beforeLookup(Lookup.Template<?> template) {
        for (MetaLookupMerger metaMerger : metaMergers.allInstances()) {
            metaMerger.probing(template.getType()); // might fire ChangeEvent, see below
        }
    }

    @Override public void stateChanged(ChangeEvent e) {
        // A metamerger loaded its class and is now ready for service.
        doDelegate();
    }

    private void doDelegate() {
        class NotifyLater implements Executor {
            List<Runnable> pending = new ArrayList<>();

            @Override
            public void execute(Runnable command) {
                pending.add(command);
            }

            public void notifyCollectedEvents() {
                List<Runnable> tmp = pending;
                pending = null;
                for (Runnable r : tmp) {
                    r.run();
                }
            }
        }
        NotifyLater notifyLater = new NotifyLater();

        synchronized (results) {
            for (Lookup.Result<?> r : results) {
                r.removeLookupListener(this);
            }
            results.clear();
            Collection<? extends LookupProvider> providers = providerResult.allInstances();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(
                    Level.FINE,
                    "New providers count: {0} for: {1}",  //NOI18N
                     new Object[] {
                         providers.size(),
                         System.identityHashCode(this)
                     });
            }
            List<Lookup> newLookups = new ArrayList<Lookup>();
            for (LookupProvider elem : providers) {
                if (old.contains(elem)) {
                    int index = old.indexOf(elem);
                    newLookups.add(currentLookups.get(index));
                } else {
                    Lookup newone = elem.createAdditionalLookup(baseLookup);
                    assert newone != null;
                    newLookups.add(newone);
                }
            }
            old = new ArrayList<LookupProvider>(providers);
            currentLookups = newLookups;
            newLookups.add(baseLookup);
            unmergedController.setLookups(notifyLater, newLookups.toArray(new Lookup[0]));
            List<Class<?>> filteredClasses = new ArrayList<Class<?>>();
            List<Object> mergedInstances = new ArrayList<Object>();
            LookupListener l = listenerRef != null ? listenerRef.get() : null;
            if (l != null) {
                mergers.removeLookupListener(l);
            }
            mergers = unmergedLookup.lookupResult(LookupMerger.class); // XXX maybe do this just in ctor
            l = WeakListeners.create(LookupListener.class, this, mergers);
            listenerRef = new WeakReference<LookupListener>(l);
            mergers.addLookupListener(l);
            @SuppressWarnings("rawtypes")
            Collection<LookupMerger> allMergers = new ArrayList<LookupMerger>(mergers.allInstances());
            for (MetaLookupMerger metaMerger : metaMergers.allInstances()) {
                LookupMerger<?> merger = metaMerger.merger();
                if (merger != null) {
                    allMergers.add(merger);
                }
                metaMerger.removeChangeListener(metaMergerListener);
                metaMerger.addChangeListener(metaMergerListener);
            }
            for (LookupMerger<?> lm : allMergers) {
                Class<?> c = lm.getMergeableClass();
                if (filteredClasses.contains(c)) {
                    LOG.log(Level.WARNING, "Two LookupMerger instances for {0} among {1} in {2}. Only first one will be used", new Object[] {c, allMergers, pathDescriptor});
                    continue;
                }
                filteredClasses.add(c);
                Object merge = mergerResults.get(lm);
                if (merge == null) {
                    merge = lm.merge(unmergedLookup);
                    mergerResults.put(lm, merge);
                }
                mergedInstances.add(merge);
                Lookup.Result<?> result = unmergedLookup.lookupResult(c);
                result.addLookupListener(this);
                results.add(result);
            }
            Lookup filtered = Lookups.exclude(unmergedLookup, filteredClasses.toArray(new Class<?>[0]));
            Lookup fixed = Lookups.fixed(mergedInstances.toArray(new Object[0]));
            setLookups(notifyLater, fixed, filtered);
        }
        notifyLater.notifyCollectedEvents();
    }

    final boolean holdsLock() {
        return Thread.holdsLock(results);
    }

    //just for assertion evaluation.
    private boolean isAllJustLookupProviders(Lookup lkp) {
        for (Lookup.Item<?> item : lkp.lookupResult(Object.class).allItems()) {
            Class<?> clzz = item.getType();
            if (!LookupProvider.class.isAssignableFrom(clzz) && !MetaLookupMerger.class.isAssignableFrom(clzz)) {
                LOG.log(Level.WARNING, "{0} from {1} is not a LookupProvider", new Object[] {clzz.getName(), item.getId()});
            }
        }
        return true; // always just print warnings
    }

}
