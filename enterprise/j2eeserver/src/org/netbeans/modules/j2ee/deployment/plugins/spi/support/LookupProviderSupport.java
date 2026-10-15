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

package org.netbeans.modules.j2ee.deployment.plugins.spi.support;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.deployment.plugins.spi.LookupProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Factory for lookup capable of merging content from registered 
 * {@link org.netbeans.spi.project.LookupProvider} instances.
 * @author phejl, mkleint
 * @since 1.50
 */
public final class LookupProviderSupport {
    
    private LookupProviderSupport() {
    }
    
    /**
     * Creates a platform lookup instance that combines the content from multiple sources.
     * A convenience factory method for implementors of
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}.
     * 
     * @param baseLookup initial, base content of the project lookup created by the plugin
     * @param folderPath the path in the System Filesystem that is used as root for lookup composition, as for {@link Lookups#forPath}.
     *        The content of the folder is assumed to be {@link LookupProvider} instances.
     * @return a lookup to be used in platform
     */ 
    public static Lookup createCompositeLookup(Lookup baseLookup, String folderPath) {
        return new DelegatingLookupImpl(baseLookup, folderPath);
    }
    
    static class DelegatingLookupImpl extends ProxyLookup implements LookupListener {
        private Lookup baseLookup;
        private Lookup.Result<LookupProvider> providerResult;
        private LookupListener providerListener;
        private List<LookupProvider> old = Collections.emptyList();
        private List<Lookup> currentLookups;

        //#68623: the proxy lookup fires changes only if someone listens on a particular template:
        private List<Lookup.Result<?>> results = new ArrayList<Lookup.Result<?>>();
        
        public DelegatingLookupImpl(Lookup base, String path) {
            this(base, Lookups.forPath(path), path);
        }
        
        public DelegatingLookupImpl(Lookup base, Lookup providerLookup, String path) {
            super();
            assert base != null;
            baseLookup = base;
            providerResult = providerLookup.lookup(new Lookup.Template<LookupProvider>(LookupProvider.class));
            assert isAllJustLookupProviders(providerLookup) : 
                "Layer content at " + path + " contains other than LookupProvider instances! See messages.log file for more details."; //NOI18N
            doDelegate(providerResult.allInstances());
            providerListener = new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    doDelegate(providerResult.allInstances());
                }
            };
            providerResult.addLookupListener(
                WeakListeners.create(LookupListener.class, providerListener, providerResult));
        }
        
        //just for assertion evaluation.
        private boolean isAllJustLookupProviders(Lookup lkp) {
            Lookup.Result<Object> res = lkp.lookupResult(Object.class);
            Set<Class<?>> set = res.allClasses();
            for (Class clzz : set) {
                if (!LookupProvider.class.isAssignableFrom(clzz)) {
                    Logger.getLogger(LookupProviderSupport.class.getName()).warning("" + clzz.getName() + " is not instance of LookupProvider."); //NOI18N
                    return false;
                }
            }
            return true;
        }
        
        
        public void resultChanged(LookupEvent ev) {
            doDelegate(providerResult.allInstances());
        }
        
        
        private synchronized void doDelegate(Collection<? extends LookupProvider> providers) {
            //unregister listeners from the old results:
            for (Lookup.Result<?> r : results) {
                r.removeLookupListener(this);
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
            Lookup lkp = new ProxyLookup(newLookups.toArray(new Lookup[0]));
            
            setLookups(lkp);
        }
    }
    
}
