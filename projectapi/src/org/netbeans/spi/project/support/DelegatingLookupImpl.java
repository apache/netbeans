/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
    private final UnmergedLookup unmergedLookup = new UnmergedLookup();
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
            unmergedLookup._setLookups(newLookups.toArray(new Lookup[newLookups.size()]));
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
            Lookup filtered = Lookups.exclude(unmergedLookup, filteredClasses.toArray(new Class<?>[filteredClasses.size()]));
            Lookup fixed = Lookups.fixed(mergedInstances.toArray(new Object[mergedInstances.size()]));
            setLookups(fixed, filtered);
        }
    }

    private static class UnmergedLookup extends ProxyLookup {
        void _setLookups(Lookup... lookups) {
            setLookups(lookups);
        }
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
