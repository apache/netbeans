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

package org.netbeans.modules.settings.convertors;

import java.beans.*;
import java.util.*;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.MainLookup;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;

/** Manager providing ModuleInfo of all modules presented in the system.
 * @author  Jan Pokorsky
 */
final class ModuleInfoManager {
    private static final ModuleInfoManager mim = new ModuleInfoManager();

    /** all modules <code bas name, ModuleInfo> */
    private HashMap<String, ModuleInfo> modules = null;
    /** lookup query to find out all modules */
    private Lookup.Result<ModuleInfo> modulesResult = null;
    /** <ModuleInfo, PCL> */
    private HashMap<ModuleInfo, PCL> mapOfListeners;
    /** Creates a new instance of ModuleInfoManager */
    private ModuleInfoManager() {
    }
    
    public static final ModuleInfoManager getDefault() {
        return mim;
    }
    
    /** find module info.
     * @param codeBaseName module code base name (without revision)
     * @return module info or null
     */
    public ModuleInfo getModule(String codeBaseName) {
        Collection<? extends ModuleInfo> l = null;
        if (modules == null) {
            l = getModulesResult().allInstances();
        }
        synchronized (this) {
            if (modules == null) fillModules(l);
            return (ModuleInfo) modules.get(codeBaseName);
        }
    }

    private Lookup.Result<ModuleInfo> getModulesResult() {
        synchronized (this) {
            if (modulesResult == null) {
                Lookup lookup = getModuleLookup();
                modulesResult = lookup.
                    lookup(new Lookup.Template<ModuleInfo>(ModuleInfo.class));
                modulesResult.addLookupListener(new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        Collection<? extends ModuleInfo> l = getModulesResult().allInstances();
                        XMLSettingsSupport.err.fine("Modules changed: " + l); // NOI18N
                        List reloaded;
                        synchronized (this) {
                            fillModules(l);
                            reloaded = replaceReloadedModules();
                            XMLSettingsSupport.err.fine("Reloaded modules: " + reloaded); // NOI18N
                        }
                        notifyReloads(reloaded);
                    }
                });
            }
            return modulesResult;
        }
    }

    private static Lookup getModuleLookup() {
        Lookup l = Lookup.getDefault();
        if (l instanceof MainLookup) {
            l = Main.getModuleSystem().getManager().getModuleLookup();
        }
        return l;
    }

    /** notify registered listeners about reloaded modules
     * @param l a list of PCLs of reloaded modules
     */
    private void notifyReloads(List l) {
        Iterator it = l.iterator();
        while (it.hasNext()) {
            PCL lsnr = (PCL) it.next();
            lsnr.notifyReload();
        }
    }

    /** recompute accessible modules.
     * @param l a collection of module infos
     */
    private void fillModules(Collection<? extends ModuleInfo> l) {
        HashMap<String, ModuleInfo> m = new HashMap<String, ModuleInfo>((l.size() << 2) / 3 + 1);
        for (ModuleInfo mi: l) {
            m.put(mi.getCodeNameBase(), mi);
        }
        modules = m;
    }
    
    /** replace old MIs of reloaded modules with new ones
     * @return the list of PCLs of reloaded modules
     */
    private List<PCL> replaceReloadedModules() {
        if (mapOfListeners == null) return Collections.emptyList();
        
        Iterator<ModuleInfo> it = new ArrayList<ModuleInfo>(mapOfListeners.keySet()).iterator();
        List<PCL> reloaded = new ArrayList<PCL>();
        
        while (it.hasNext()) {
            ModuleInfo mi = it.next();
            ModuleInfo miNew = modules.get(mi.getCodeNameBase());
            if (mi != miNew && miNew != null) {
                PCL lsnr = mapOfListeners.remove(mi);
                lsnr.setModuleInfo(miNew);
                reloaded.add(lsnr);
                mapOfListeners.put(miNew, lsnr);
            }
        }
        
        return reloaded;
    }
    
    /** register listener to be notified about changes of mi
     * @param sdc convertor
     * @param mi ModuleInfo for which the listener will be registered
     */
    public synchronized void registerPropertyChangeListener(SerialDataConvertor sdc, ModuleInfo mi) {
        if (mapOfListeners == null) {
            mapOfListeners = new HashMap<ModuleInfo,PCL>(modules.size());
        }
        
        PCL lsnr = mapOfListeners.get(mi);
        if (lsnr == null) {
            lsnr = new PCL(mi);
            mapOfListeners.put(mi, lsnr);
        }
        PropertyChangeListener pcl = org.openide.util.WeakListeners.propertyChange(sdc, lsnr);
        lsnr.addPropertyChangeListener(sdc, pcl);
    }
    
    /** unregister listener
     * @param sdc convertor
     * @param mi ModuleInfo
     * @see #registerPropertyChangeListener
     */
    public synchronized void unregisterPropertyChangeListener(SerialDataConvertor sdc, ModuleInfo mi) {
        if (mapOfListeners == null) return;
        PCL lsnr = (PCL) mapOfListeners.get(mi);
        if (lsnr != null) {
            lsnr.removePropertyChangeListener(sdc);
            // do not try to discard lsnr to allow to track reloading of a module
        }
    }
    
    /** find out if a module was reloaded (disable+enabled)
     * @param mi ModuleInfo of the queried module
     * @return reload status
     */
    public synchronized boolean isReloaded(ModuleInfo mi) {
        if (mapOfListeners == null) return false;
        PCL lsnr = (PCL) mapOfListeners.get(mi);
        return lsnr != null && lsnr.isReloaded();
    }
    
    /** find out if a module was reloaded (disable+enabled)
     * @param codeBaseName ModuleInfo's code base name of the queried module
     * @return reload status
     */
    public synchronized boolean isReloaded(String codeBaseName) {
        if (mapOfListeners == null) return false;
        return isReloaded(getModule(codeBaseName));
    }
    
    /** ModuleInfo status provider shared by registered listeners
     * @see #registerPropertyChangeListener
     */
    private static final class PCL implements PropertyChangeListener {
        /** a flag to be set to true when a module has been disabled */
        private boolean aModuleHasBeenChanged = false;
        private boolean wasModuleEnabled;
        private ModuleInfo mi;
        private PropertyChangeSupport changeSupport;
        /** map of registered listeners <SerialDataConvertor, PropertyChangeListener> */
        private Map<SerialDataConvertor, PropertyChangeListener> origs;
        
        public PCL(ModuleInfo mi) {
            this.mi = mi;
            wasModuleEnabled = mi.isEnabled();
            mi.addPropertyChangeListener(this);
        }
        
        /** replace an old module info with a new one */
        void setModuleInfo(ModuleInfo mi) {
            this.mi.removePropertyChangeListener(this);
            aModuleHasBeenChanged = true;
            this.mi = mi;
            mi.addPropertyChangeListener(this);
        }
        
        /** notify listeners about a module reload */
        void notifyReload() {
            firePropertyChange();
        }
        
        boolean isReloaded() {
            return aModuleHasBeenChanged;
        }
        
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if(ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {

                boolean change;

                if (!Boolean.TRUE.equals (evt.getNewValue ())) {
                    // a module has been disabled, use full checks
                    aModuleHasBeenChanged = true;

                    // if wasModuleEnabled was true, we changed state
                    change = wasModuleEnabled;
                } else {
                    // a module was enabled, if wasModuleEnabled was false
                    // we changed state
                    change = !wasModuleEnabled;
                }

                // update wasModuleEnabled to current state of the module
                wasModuleEnabled = mi.isEnabled();

                if (change) {
                    //instanceCookieChanged(null);
                    firePropertyChange();
                }
            }
        }

        /** adds listener per convertor */
        public void addPropertyChangeListener(SerialDataConvertor sdc, PropertyChangeListener listener) {
            synchronized (this) {
                if (changeSupport == null) {
                    changeSupport = new PropertyChangeSupport(this);
                    origs = new WeakHashMap<SerialDataConvertor, PropertyChangeListener>();
                }
                
                PropertyChangeListener old = (PropertyChangeListener) origs.get(sdc);
                if (old != null) return;
                origs.put(sdc, listener);
            }
            changeSupport.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            if (changeSupport != null) {
                changeSupport.removePropertyChangeListener(listener);
            }
        }
        
        /** unregister listener registered per convertor */
        public void removePropertyChangeListener(SerialDataConvertor sdc) {
            synchronized (this) {
                if (origs == null) return;
                
                PropertyChangeListener pcl = (PropertyChangeListener) origs.remove(sdc);
                if (pcl != null) {
                    removePropertyChangeListener(pcl);
                }
            }
        }
        
        private void firePropertyChange() {
            if (changeSupport != null) {
                changeSupport.firePropertyChange(ModuleInfo.PROP_ENABLED, null, null);
            }
        }
    }
    
}
