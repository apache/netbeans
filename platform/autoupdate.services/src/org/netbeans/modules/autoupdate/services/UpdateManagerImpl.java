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

package org.netbeans.modules.autoupdate.services;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek, Radek Matous
 */
public class UpdateManagerImpl extends Object {
    private static final UpdateManagerImpl INSTANCE = new UpdateManagerImpl();
    private static final UpdateManager.TYPE [] DEFAULT_TYPES = new UpdateManager.TYPE [] {  UpdateManager.TYPE.KIT_MODULE };
    
    private Reference<Cache> cacheReference = null;            
    
    // package-private for tests only
    
    public static UpdateManagerImpl getInstance() {
        return INSTANCE;
    }
    
    /** Creates a new instance of UpdateManagerImpl */
    private UpdateManagerImpl () {}

    public void clearCache () {
        synchronized(UpdateManagerImpl.Cache.class) {
            cacheReference = null;
            source2UpdateUnitProvider = null;
            Utilities.writeFirstClassModule(null);
        }
    }    
    
    /**
     * Flushes the cache, but does not flush the cache units themselves.
     * This prevents re-creation of UpdateUnit instances; old instances were
     * updated with info from the update step.
     */
    public void flushComputedInfo() {
        Cache c;
        
        synchronized(UpdateManagerImpl.Cache.class) {
            Reference<Cache> cr = getCacheReference();
            if (cr == null) {
                return;
            }
            c = cr.get();
            if (c == null) {
                return;
            }
        }
        c.clearMaps();
    }
    
    public static List<UpdateUnit> getUpdateUnits (UpdateProvider provider, UpdateManager.TYPE... types) {
        return filterUnitsByAskedTypes (UpdateUnitFactory.getDefault().getUpdateUnits (provider).values (), type2checkedList (types));
    }
    
    public List<UpdateUnit> getUpdateUnits (UpdateManager.TYPE... types) {                
        final Cache c = getCache();        
        return new ArrayList<UpdateUnit> (filterUnitsByAskedTypes (c.getUnits(), type2checkedList (types))) {
            Cache keepIt = c;
        };        
    }
        
    public Set<UpdateElement> getAvailableEagers () {
        final Cache c = getCache();        
        return new HashSet<UpdateElement> (c.getAvailableEagers()) {
            Cache keepIt = c;
        };        
    }
    

    public Set<UpdateElement> getInstalledEagers () {
        final Cache c = getCache();        
        return new HashSet<UpdateElement> (c.getInstalledEagers()) {
            Cache keepIt = c;
        };        
    }
    
    public Collection<ModuleInfo> getInstalledProviders (String token) {
        Collection<ModuleInfo> res;
        final Cache c = getCache ();
        if (token.startsWith("cnb.")) { // NOI18N
            UpdateUnit updateUnit = c.getUpdateUnit(token.substring(4));
            if (updateUnit != null && updateUnit.getInstalled() != null) {
                return Trampoline.API.impl(updateUnit.getInstalled()).getModuleInfos();
            }
        }
        Collection<ModuleInfo> providers = c.createMapToken2InstalledProviders ().get (token);
        if (providers == null || providers.isEmpty ()) {
            res = new HashSet<ModuleInfo> (0) {
                Cache keepIt = c;
            };
        } else {
            res = new HashSet<ModuleInfo> (providers) {
                Cache keepIt = c;
            };
        }
        return res;
    }
            
    public Collection<ModuleInfo> getAvailableProviders (String token) {
        Collection<ModuleInfo> res;
        final Cache c = getCache ();
        if (token.startsWith("cnb.")) { // NOI18N
            UpdateUnit updateUnit = c.getUpdateUnit(token.substring(4));
            if (updateUnit != null && ! updateUnit.getAvailableUpdates().isEmpty()) {
                return Trampoline.API.impl(updateUnit.getAvailableUpdates().get(0)).getModuleInfos();
            }
        }
        Collection<ModuleInfo> providers = c.createMapToken2AvailableProviders ().get (token);
        if (providers == null || providers.isEmpty ()) {
            res = new HashSet<ModuleInfo> (0) {
                Cache keepIt = c;
            };
        } else {
            res = new HashSet<ModuleInfo> (providers) {
                Cache keepIt = c;
            };
        }
        return res;
    }
            
    public TreeSet<UpdateElement> getInstalledKits(String cluster) {
        TreeSet<UpdateElement> res;
        final Cache c = getCache();
        TreeSet<UpdateElement> kits = c.createMapCluster2installedKits().get(cluster);
        if (kits == null || kits.isEmpty()) {
            res = new TreeSet<UpdateElement>() {
                Cache keepIt = c;
            };
        } else {
            res = new TreeSet<UpdateElement>(kits) {
                Cache keepIt = c;
            };
        }
        return res;
    }

    public UpdateUnit getUpdateUnit (String moduleCodeName) {
        if (moduleCodeName.indexOf('/') != -1) {
            int to = moduleCodeName.indexOf('/');
            moduleCodeName = moduleCodeName.substring(0, to);
        }        
        return getCache().getUpdateUnit(moduleCodeName);
    }
            
    public List<UpdateUnit> getUpdateUnits() {
        final Cache c = getCache();
        return new ArrayList<UpdateUnit> (c.getUnits()) {
            Cache keepIt = c; 
        };
    }
    
   private static List<UpdateUnit> filterUnitsByAskedTypes (Collection<UpdateUnit> units, List<UpdateManager.TYPE> types) {
        List<UpdateUnit> askedUnits = new ArrayList<UpdateUnit> ();

        //hotfix for #113193 - reevaluate and probably fix better
        List<UpdateManager.TYPE> tmpTypes =  new ArrayList<UpdateManager.TYPE>(types);
        if (tmpTypes.contains (UpdateManager.TYPE.MODULE) && !tmpTypes.contains (UpdateManager.TYPE.KIT_MODULE)) {
            tmpTypes.add (UpdateManager.TYPE.KIT_MODULE);
        }
        
        for (UpdateUnit unit : units) {
            UpdateUnitImpl impl = Trampoline.API.impl (unit);
            if (tmpTypes.contains (impl.getType ())) {
                askedUnits.add (unit);
            }
        }

        return askedUnits;
    } 
   
    private static List<UpdateManager.TYPE> type2checkedList (UpdateManager.TYPE... types) {
        List<UpdateManager.TYPE> l = Arrays.asList (types);
        if (types != null && types.length > 1) {
            if (l.contains (UpdateManager.TYPE.MODULE) && l.contains (UpdateManager.TYPE.KIT_MODULE)) {
                throw new IllegalArgumentException ("Cannot mix types MODULE and KIT_MODULE into once list.");
            }
        } else if (types == null || types.length == 0) {
            l = Arrays.asList (DEFAULT_TYPES);
        }
        return l;
    }

    private UpdateManagerImpl.Cache getCache() {
        Reference<UpdateManagerImpl.Cache> ref =  getCacheReference();
        UpdateManagerImpl.Cache retval = (ref != null) ? ref.get() : null;
        if (retval == null) {
            retval = new Cache();
            initCache(retval);
        }
        return retval;
    }        

    Reference<UpdateManagerImpl.Cache> getCacheReference() {        
        synchronized(UpdateManagerImpl.Cache.class) {        
            return cacheReference;
        }
    }    
    
    private void initCache(UpdateManagerImpl.Cache c) {
        synchronized(UpdateManagerImpl.Cache.class) {        
            cacheReference = new WeakReference<UpdateManagerImpl.Cache>(c);
        }        
    }
    
    private Map<String, UpdateUnitProvider> source2UpdateUnitProvider = null;

    public UpdateUnitProvider getUpdateUnitProvider(String source) {
        if (source2UpdateUnitProvider == null) {
            List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false);
            source2UpdateUnitProvider = new HashMap<String, UpdateUnitProvider>(providers.size());
            for (UpdateUnitProvider updateUnitProvider : providers) {
                source2UpdateUnitProvider.put(updateUnitProvider.getDisplayName(), updateUnitProvider);
            }
        }
        return source2UpdateUnitProvider.get(source);
    }
    
    private class Cache {
        private final Map<String, UpdateUnit> units;
        private Set<UpdateElement> availableEagers = null;
        private Set<UpdateElement> installedEagers = null;
        private Map<String, Collection<ModuleInfo>> token2installedProviders = null;
        private Map<String, Collection<ModuleInfo>> token2availableProviders = null;
        private Map<String, TreeSet<UpdateElement>> cluster2installedKits = null;

        Cache() {
            units = UpdateUnitFactory.getDefault ().getUpdateUnits ();
        }        
        
        synchronized void clearMaps() {
            availableEagers = null;
            installedEagers = null;
            token2installedProviders = null;
            token2availableProviders = null;
            cluster2installedKits = null;
        }
        
        public synchronized Set<UpdateElement> getAvailableEagers() {
            if (availableEagers == null) {
                createMaps ();
            }
            assert availableEagers != null : "availableEagers initialized";
            return availableEagers;
        }
        public synchronized Set<UpdateElement> getInstalledEagers() {
            if (installedEagers == null) {
                createMaps ();
            }            
            assert installedEagers != null : "installedEagers initialized";
            return installedEagers;
        }                        
        public synchronized Map<String, Collection<ModuleInfo>> createMapToken2InstalledProviders () {
            if (token2installedProviders == null) {
                createMaps ();
            }            
            assert token2installedProviders != null : "token2installedProviders initialized";
            return token2installedProviders;
        }                        
        public synchronized Map<String, Collection<ModuleInfo>> createMapToken2AvailableProviders () {
            if (token2availableProviders == null) {
                createMaps ();
            }
            assert token2availableProviders != null : "token2availableProviders initialized";
            return token2availableProviders;
        }                        
        public synchronized Map<String, TreeSet<UpdateElement>> createMapCluster2installedKits() {
            if (cluster2installedKits == null) {
                createMaps();
            }
            assert cluster2installedKits != null : "cluster2installedKits initialized";
            return cluster2installedKits;
        }
        public Collection<UpdateUnit> getUnits() {
            return units.values();
        }
        public UpdateUnit getUpdateUnit (String moduleCodeName) {
            return units.get(moduleCodeName);
        }
        
        private synchronized void createMaps () {
            availableEagers = new HashSet<UpdateElement> (getUnits ().size ());
            installedEagers = new HashSet<UpdateElement> (getUnits ().size ());
            token2installedProviders = new HashMap<String, Collection<ModuleInfo>> (11);
            token2availableProviders = new HashMap<String, Collection<ModuleInfo>> (11);    
            cluster2installedKits = new HashMap<String, TreeSet<UpdateElement>> ();
            DependencyAggregator.clearMaps();
            for (UpdateUnit unit : getUnits ()) {
                UpdateElement el;
                if ((el = unit.getInstalled ()) != null) {
                    UpdateElementImpl elImpl = Trampoline.API.impl(el);
                    if (elImpl.isEager()) {
                        installedEagers.add (el);
                    }
                    for (ModuleInfo mi : elImpl.getModuleInfos ()) {
                        for (Dependency dep : mi.getDependencies ()) {
                            DependencyAggregator dec = DependencyAggregator.getAggregator (dep);
                            dec.addDependee (mi);
                        }
                        String[] provs = mi.getProvides ();
                        if (provs == null || provs.length == 0) {
                            continue;
                        }
                        for (String token : provs) {
                            if (token2installedProviders.get (token) == null) {
                                token2installedProviders.put (token, new HashSet<ModuleInfo> ());
                            }
                            token2installedProviders.get (token).add (mi);
                        }
                    }
                    if (elImpl instanceof KitModuleUpdateElementImpl) {
                        String cluster = ((KitModuleUpdateElementImpl) elImpl).getInstallationCluster();
                        if (cluster != null) {
                            if (cluster2installedKits.get(cluster) == null) {
                                TreeSet<UpdateElement> s = new TreeSet<UpdateElement>(new Comparator<UpdateElement>() {
                                    @Override
                                    public int compare(UpdateElement ue1, UpdateElement ue2) {
                                        return ue1.getCodeName().compareTo(ue2.getCodeName());
                                    }
                                });
                                cluster2installedKits.put(cluster, s);
                            }
                            cluster2installedKits.get(cluster).add(el);
                        }
                    }
                }
                if (! unit.getAvailableUpdates ().isEmpty ()) {
                    el = unit.getAvailableUpdates ().get (0);
                    if (Trampoline.API.impl (el).isEager ()) {
                        availableEagers.add (el);
                    }
                    for (ModuleInfo mi : Trampoline.API.impl (el).getModuleInfos ()) {
                        for (Dependency dep : mi.getDependencies ()) {
                            DependencyAggregator dec = DependencyAggregator.getAggregator (dep);
                            dec.addDependee (mi);
                        }
                        String[] provs = mi.getProvides ();
                        if (provs == null || provs.length == 0) {
                            continue;
                        }
                        for (String token : provs) {
                            if (token2availableProviders.get (token) == null) {
                                token2availableProviders.put (token, new HashSet<ModuleInfo> ());
                            }
                            token2availableProviders.get (token).add (mi);
                        }
                    }
                }
            }
        }
    }
}
    
