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

package org.netbeans.modules.autoupdate.services;

import org.netbeans.modules.autoupdate.updateprovider.NativeComponentItem;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.updateprovider.LocalizationItem;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.modules.autoupdate.updateprovider.BackupUpdateProvider;
import org.netbeans.modules.autoupdate.updateprovider.InstalledUpdateProvider;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitFactory {
    
    /** Creates a new instance of UpdateItemFactory */
    private UpdateUnitFactory () {}
    
    private static final UpdateUnitFactory INSTANCE = new UpdateUnitFactory ();
    private final Logger log = Logger.getLogger (this.getClass ().getName ());
    private static final DateFormat FMT = new SimpleDateFormat ("mm:ss:SS"); // NOI18N
    private static long runTime = -1;
    private Set<String> scheduledForRestartUE = null;
    private Set<String> scheduledForRestartUU = null;
    
    public static final String UNSORTED_CATEGORY = NbBundle.getMessage (UpdateUnitFactory.class, "UpdateUnitFactory_Unsorted_Category");
    public static final String LIBRARIES_CATEGORY = NbBundle.getMessage (UpdateUnitFactory.class, "UpdateUnitFactory_Libraries_Category");
    public static final String BRIDGES_CATEGORY = NbBundle.getMessage (UpdateUnitFactory.class, "UpdateUnitFactory_Bridges_Category");
    
    public static UpdateUnitFactory getDefault () {
        return INSTANCE;
    }
    
    public Map<String, UpdateUnit> getUpdateUnits () {
        //TODO: this call should be forced not to be called from AWT
        //assert !SwingUtilities.isEventDispatchThread();
        resetRunTime ("Measuring of UpdateUnitFactory.getUpdateUnits()"); // NOI18N
        List<UpdateUnitProvider> updates = UpdateUnitProviderImpl.getUpdateUnitProviders (true);
        
//        // loop installed modules
//        SortedSet<String> unmarked = new TreeSet<String> ();
//        SortedSet<String> markedTrue = new TreeSet<String> ();
//        SortedSet<String> markedFalse = new TreeSet<String> ();
//        for (ModuleInfo moduleInfo : InstalledModuleProvider.getInstalledModules ().values ()) {
//            Object f = moduleInfo.getAttribute (ATTR_VISIBLE);
//            if (f == null) {
//                unmarked.add (moduleInfo.getCodeName ());
//            } else if (Boolean.parseBoolean (f.toString ())) {
//                markedTrue.add (moduleInfo.getCodeName ());
//            } else {
//                markedFalse.add (moduleInfo.getCodeName ());
//            }
//        }
//        System.out.println("###### SIZE OF UNMARKED MODULES ? " + unmarked.size ());
//        System.out.println("###### SIZE OF VISIBLE MODULES ? " + markedTrue.size ());
//        System.out.println("###### SIZE OF HIDDEN MODULES ? " + markedFalse.size ());
        
        try {
            InstalledModuleProvider.getDefault().getUpdateItems();
        } catch (Exception x) {
            x.printStackTrace();
        }
        reportRunTime ("Get all installed modules.");
        
        // append installed units
        Map<String, UpdateUnit> mappedImpl = appendUpdateItems (
                new HashMap<String, UpdateUnit> (),
                InstalledModuleProvider.getDefault());
        reportRunTime ("Append installed units.");

        for (UpdateUnitProvider up : updates) {
            UpdateUnitProviderImpl impl = Trampoline.API.impl (up);

            // append units from provider
            mappedImpl = appendUpdateItems (mappedImpl, impl.getUpdateProvider ());
            reportRunTime ("AppendUpdateItems for " + impl.getUpdateProvider ().getDisplayName ());
        }

        return mappedImpl;
    }
    
    public Map<String, UpdateUnit> getUpdateUnits (UpdateProvider provider) {
        //TODO: this call should be forced not to be called from AWT
        //assert !SwingUtilities.isEventDispatchThread();
        resetRunTime ("Measuring UpdateUnitFactory.getUpdateUnits (" + provider.getDisplayName () + ")"); // NOI18N

        // append units from provider
        Map<String, UpdateUnit> temp = appendUpdateItems (new HashMap<String, UpdateUnit> (), provider);
        reportRunTime ("Get appendUpdateItems for " + provider.getDisplayName ());
        
        Map<String, UpdateUnit> retval = new HashMap<String, UpdateUnit>();
        for (UpdateUnit unit : temp.values ()) {
            retval.put (unit.getCodeName (), mergeInstalledUpdateUnit (unit));
        }
        reportRunTime ("Get filltering by " + provider.getDisplayName ());
        
        return temp;
    }
    
    Map<String, UpdateUnit> appendUpdateItems (Map<String, UpdateUnit> originalUnits, UpdateProvider provider) {
        assert originalUnits != null : "Map of original UnitImpl cannot be null";

        boolean trusted = UpdateUnitProviderImpl.loadTrusted(provider);

        Map<String, UpdateItem> items;
        try {
            items = provider.getUpdateItems ();
        } catch (IOException ioe) {
            log.log (Level.INFO, "Cannot read UpdateItem from UpdateProvider " + provider, ioe);
            return originalUnits;
        }
        
        assert items != null : "UpdateProvider[" + provider.getName () + "] should return non-null items.";
        
        // append updates
        for (UpdateItem ui : items.values()) {
            UpdateElement updateEl = null;
            try {
                UpdateItemImpl itemImpl = Trampoline.SPI.impl(ui); // create UpdateItemImpl

                boolean isKitModule = false;
                if (itemImpl instanceof ModuleItem) {
                    ModuleInfo mi = ((ModuleItem) itemImpl).getModuleInfo ();
                    assert mi != null : "ModuleInfo must be found for " + itemImpl;
                    isKitModule = Utilities.isKitModule (mi);
                }
                if (itemImpl instanceof InstalledModuleItem) {
                    if (isKitModule) {
                        KitModuleUpdateElementImpl impl = new KitModuleUpdateElementImpl ((InstalledModuleItem) itemImpl, null);
                        updateEl = Trampoline.API.createUpdateElement (impl);
                    } else {
                        ModuleUpdateElementImpl impl = new ModuleUpdateElementImpl ((InstalledModuleItem) itemImpl, null);
                        updateEl = Trampoline.API.createUpdateElement (impl);
                    }
                } else if (itemImpl instanceof ModuleItem) {
                    if (isKitModule) {
                        KitModuleUpdateElementImpl impl = new KitModuleUpdateElementImpl ((ModuleItem) itemImpl, provider.getDisplayName ());
                        updateEl = Trampoline.API.createUpdateElement (impl);
                    } else {
                        ModuleUpdateElementImpl impl = new ModuleUpdateElementImpl ((ModuleItem) itemImpl, provider.getDisplayName ());
                        updateEl = Trampoline.API.createUpdateElement (impl);
                    }
                } else if (itemImpl instanceof LocalizationItem) {
                    updateEl = Trampoline.API.createUpdateElement (new LocalizationUpdateElementImpl ((LocalizationItem) itemImpl, provider.getDisplayName ()));
                } else if (itemImpl instanceof NativeComponentItem) {
                    updateEl = Trampoline.API.createUpdateElement (new NativeComponentUpdateElementImpl ((NativeComponentItem) itemImpl, provider.getDisplayName ()));
                } else if (itemImpl instanceof FeatureItem) {
                    FeatureUpdateElementImpl impl = new FeatureUpdateElementImpl.Agent (
                            (FeatureItem) itemImpl,
                            provider.getDisplayName (),
                            UpdateManager.TYPE.FEATURE);
                    updateEl = Trampoline.API.createUpdateElement (impl);
                } else {
                    assert false : "Unknown type of UpdateElement " + updateEl;
                }

            } catch (IllegalArgumentException iae) {
                log.log (Level.INFO, iae.getLocalizedMessage (), iae);
            }

            // add element to map
            if (updateEl != null) {
                Trampoline.API.impl(updateEl).setCatalogTrusted(trusted);
                addElement (originalUnits, updateEl, provider);
            }
        }
        
        return originalUnits;
    }
    
    private void addElement (Map<String, UpdateUnit> impls, UpdateElement element, UpdateProvider provider) {
        // find if corresponding element exists
        UpdateUnit unit = impls.get (element.getCodeName ());
        
        // XXX: it's should be moved in UI what should filter all elements w/ broken dependencies
        // #101515: Plugin Manager must filter updates by platform dependency
        UpdateElementImpl elImpl = Trampoline.API.impl (element);
        if (elImpl instanceof ModuleUpdateElementImpl && elImpl.getModuleInfos () != null && elImpl.getModuleInfos ().size() == 1) {
            for (Dependency d : elImpl.getModuleInfos ().get (0).getDependencies ()) {
                if (Dependency.TYPE_REQUIRES == d.getType ()) {
                    //log.log (Level.FINEST, "Dependency: NAME: " + d.getName () + ", TYPE: " + d.getType () + ": " + d.toString ());
                    if (d.getName ().startsWith ("org.openide.modules.os")) { // NOI18N
                        // Filter OS specific dependencies
                        boolean passed = false;
                        for (ModuleInfo info : InstalledModuleProvider.getInstalledModules ().values ()) {
                            if (Arrays.asList (info.getProvides ()).contains (d.getName ())) {
                                log.log (Level.FINEST, element + " which requires OS " + d + " succeed.");
                                passed = true;
                                break;
                            }
                        }
                        if (! passed) {
                            log.log (Level.FINE, element + " which requires OS " + d + " fails.");
                            return ;
                        }
                    } else if (d.getName ().startsWith ("org.openide.modules.arch")) { // NOI18N
                        // Filter architecture specific dependencies
                        boolean passed = false;
                        for (ModuleInfo info : InstalledModuleProvider.getInstalledModules ().values ()) {
                            if (Arrays.asList (info.getProvides ()).contains (d.getName ())) {
                                log.log (Level.FINEST, element + " which requires architecture " + d + " succeed.");
                                passed = true;
                                break;
                            }
                        }
                        if (! passed) {
                            log.log (Level.FINE, element + " which requires architecture " + d + " fails.");
                            return ;
                        }
                    }
                }
            }
        }
        
        UpdateUnitImpl unitImpl = null;
        
        if (unit == null) {
            switch (elImpl.getType ()) {
            case MODULE :
                unitImpl = new ModuleUpdateUnitImpl (element.getCodeName ());
                break;
            case KIT_MODULE :
                unitImpl = new KitModuleUpdateUnitImpl (element.getCodeName ());
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                unitImpl = new FeatureUpdateUnitImpl (element.getCodeName (), elImpl.getType ());
                break;
            case CUSTOM_HANDLED_COMPONENT :
                unitImpl = new NativeComponentUpdateUnitImpl (element.getCodeName ());
                break;
            case LOCALIZATION :
                unitImpl = new LocalizationUpdateUnitImpl (element.getCodeName ());
                break;
            default:
                assert false : "Unsupported for type " + elImpl.getType ();
            }
            unit = Trampoline.API.createUpdateUnit (unitImpl);
            impls.put (unit.getCodeName (), unit);
        } else {
            unitImpl = Trampoline.API.impl (unit);
        }
        
        if (provider == InstalledUpdateProvider.getDefault()) {
            if (unitImpl.getInstalled () == null) {
                unitImpl.setInstalled (element);
            }
        } else if (provider instanceof BackupUpdateProvider) {
            unitImpl.setBackup (element);
        } else {
            // suppose common UpdateProvider
            unitImpl.addUpdate (element);
        }
        
        // set UpdateUnit into element
        elImpl.setUpdateUnit (unit);

    }
    
    private UpdateUnit mergeInstalledUpdateUnit (UpdateUnit uu) {
        UpdateUnit fromCache = UpdateManagerImpl.getInstance ().getUpdateUnit (uu.getCodeName ());
        if (fromCache != null && fromCache.getInstalled () != null) {
            UpdateUnitImpl impl = Trampoline.API.impl (uu);
            impl.setInstalled (fromCache.getInstalled ());
        }
        return uu;
    }
    
    private void resetRunTime (String msg) {
        if (log.isLoggable (Level.FINE)) {
            if (msg != null) {
                log.log (Level.FINE, "|=== " + msg + " ===|"); // NOI18N
            }
        runTime = System.currentTimeMillis ();
        }
    }
    
    private void reportRunTime (String msg) {
        if (log.isLoggable (Level.FINE)) {
            if (msg != null) {
                log.log (Level.FINE, msg + " === " + FMT.format (new Date (System.currentTimeMillis () - runTime))); // NOI18N
            }
            resetRunTime (null);
        }
    }
    
    public void scheduleForRestart (UpdateElement el) {
        if (scheduledForRestartUE == null) {
            scheduledForRestartUE = new HashSet<String> ();
            scheduledForRestartUU = new HashSet<String> ();
        }
        scheduledForRestartUE.add (el.getCodeName () + "_" + el.getSpecificationVersion ()); // NOI18N
        scheduledForRestartUU.add (el.getCodeName ());
    }
    
    public boolean isScheduledForRestart (UpdateElement el) {
        return scheduledForRestartUE != null && scheduledForRestartUE.contains (el.getCodeName () + "_" + el.getSpecificationVersion ()); // NOI18N
    }
    
    public boolean isScheduledForRestart (UpdateUnit u) {
        return scheduledForRestartUU != null && scheduledForRestartUU.contains (u.getCodeName ());
    }
    
}
