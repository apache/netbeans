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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import static org.netbeans.modules.autoupdate.services.Utilities.findRequiredUpdateElements;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Radek Matous, Jiri Rechtacek
 */
abstract class OperationValidator {
    private static final OperationValidator FOR_INSTALL = new InstallValidator();
    private static final OperationValidator FOR_INTERNAL_UPDATE = new InternalUpdateValidator();
    private static final OperationValidator FOR_UNINSTALL = new UninstallValidator();
    private static final OperationValidator FOR_UPDATE = new UpdateValidator();
    private static final OperationValidator FOR_ENABLE = new EnableValidator();
    private static final OperationValidator FOR_DISABLE = new DisableValidator();
    private static final OperationValidator FOR_CUSTOM_INSTALL = new CustomInstallValidator();
    private static final OperationValidator FOR_CUSTOM_UNINSTALL = new CustomUninstallValidator();
    private static final Logger LOGGER = Logger.getLogger (OperationValidator.class.getName ());
    
    /** Creates a new instance of OperationValidator */
    private OperationValidator() {}
    
    public static boolean isValidOperation(OperationContainerImpl.OperationType type, UpdateUnit updateUnit, UpdateElement updateElement) {
        if (updateUnit.isPending ()) {
            return false;
        }
        boolean isValid = false;
        switch(type){
        case INSTALL:
            isValid = FOR_INSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case INTERNAL_UPDATE:
            isValid = FOR_INTERNAL_UPDATE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case DIRECT_UNINSTALL:
        case UNINSTALL:
            isValid = FOR_UNINSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case UPDATE:
            isValid = FOR_UPDATE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case ENABLE:
            isValid = FOR_ENABLE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case DIRECT_DISABLE:
        case DISABLE:
            isValid = FOR_DISABLE.isValidOperationImpl(updateUnit, updateElement);
            break;
        case CUSTOM_INSTALL:
            isValid = FOR_CUSTOM_INSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        case CUSTOM_UNINSTALL:
            isValid = FOR_CUSTOM_UNINSTALL.isValidOperationImpl(updateUnit, updateElement);
            break;
        default:
            assert false;
        }
        return isValid;
    }
    
    public static List<UpdateElement> getRequiredElements (OperationContainerImpl.OperationType type,
            UpdateElement updateElement,
            List<ModuleInfo> moduleInfos,
            Collection<String> brokenDependencies,
            Collection<UpdateElement> recommendedElements) {
        List<UpdateElement> retval = Collections.emptyList ();
        switch(type){
        case INSTALL:
            retval = FOR_INSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        case DIRECT_UNINSTALL:
        case UNINSTALL:
            retval = FOR_UNINSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        case UPDATE:
            retval = FOR_UPDATE.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        case ENABLE:
            retval = FOR_ENABLE.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        case DIRECT_DISABLE:
        case DISABLE:
            retval = FOR_DISABLE.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        case CUSTOM_INSTALL:
            retval = FOR_CUSTOM_INSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        case CUSTOM_UNINSTALL:
            retval = FOR_CUSTOM_UNINSTALL.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        case INTERNAL_UPDATE:
            retval = FOR_INTERNAL_UPDATE.getRequiredElementsImpl(updateElement, moduleInfos, brokenDependencies, recommendedElements);
            break;
        default:
            assert false;
        }
        if (LOGGER.isLoggable (Level.FINE)) {
            LOGGER.log (Level.FINE, "== do getRequiredElements for " + type + " of " + updateElement + " ==");
            for (UpdateElement el : retval) {
                LOGGER.log (Level.FINE, "--> " + el);
            }
            LOGGER.log (Level.FINE, "== done. ==");
        }
        return retval;
    }
    
    public static Set<String> getBrokenDependencies (OperationContainerImpl.OperationType type,
            UpdateElement updateElement,
            List<ModuleInfo> moduleInfos) {
            Set<String> broken = new HashSet<String> ();
            switch (type) {
            case ENABLE :
                broken = Utilities.getBrokenDependenciesInInstalledModules (updateElement);
                break;
            case INSTALL :
            case UPDATE :
            case INTERNAL_UPDATE:
                Set<UpdateElement> recommeded = new HashSet<UpdateElement>();
                getRequiredElements (type, updateElement, moduleInfos, broken, recommeded);
                if (! recommeded.isEmpty() && ! broken.isEmpty()) {
                    broken = new HashSet<String> ();
                    getRequiredElements(type, updateElement, moduleInfos, broken, recommeded);
                }
                break;
            case UNINSTALL :
            case DIRECT_UNINSTALL :
            case CUSTOM_UNINSTALL :
            case DISABLE :
            case DIRECT_DISABLE :
            case CUSTOM_INSTALL:
                broken = Utilities.getBrokenDependencies (updateElement, moduleInfos);
                break;
            default:
                assert false : "Unknown type of operation " + type;
            }
            return broken;
    }
    
    abstract boolean isValidOperationImpl(UpdateUnit updateUnit, UpdateElement uElement);
    abstract List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement,
            List<ModuleInfo> moduleInfos,
            Collection<String> brokenDependencies,
            Collection<UpdateElement> recommendedElements);
    
    private static class InternalUpdateValidator extends UpdateValidator {
        @Override
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return uElement.equals(unit.getInstalled()) || containsElement (uElement, unit);
        }
    }
    private static class InstallValidator extends OperationValidator {
        @Override
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() == null && containsElement (uElement, unit);
        }
        
        @Override
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos,
                                                    Collection<String> brokenDependencies, Collection<UpdateElement> recommendedElements) {
            Set<Dependency> brokenDeps = new HashSet<Dependency> ();
            List<UpdateElement> res = new LinkedList<UpdateElement> (Utilities.findRequiredUpdateElements (uElement, moduleInfos, brokenDeps, false, recommendedElements));
            if (brokenDependencies != null) {
                for (Dependency dep : brokenDeps) {
                    brokenDependencies.add (dep.toString ());
                }
            }
            return res;
        }
    }
    
    private static Map<Module, Set<Module>> module2depending = new HashMap<Module, Set<Module>> ();
    private static Map<Module, Set<Module>> module2required = new HashMap<Module, Set<Module>> ();
    
    public static void clearMaps () {
        module2depending = new HashMap<Module, Set<Module>> ();
        module2required = new HashMap<Module, Set<Module>> ();
    }
    
    private static class UninstallValidator extends OperationValidator {
        
        @Override
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case KIT_MODULE :
            case MODULE :
                Module m =  Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = ModuleEnableDisableDeleteHelper.getInstance ().canDelete (m);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module module = Utilities.toModule (info);
                    res |= ModuleEnableDisableDeleteHelper.getInstance ().canDelete (module);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                LOGGER.log (Level.INFO, "CUSTOM_HANDLED_COMPONENT doesn't support custom uninstaller yet."); // XXX
                res = false;
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        @Override
        List<UpdateElement> getRequiredElementsImpl  (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies, Collection<UpdateElement> recommendedElements) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (m == null) {
                    continue;
                }
                if (! Utilities.isEssentialModule (m)) {
                    modules.add (m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            Set<UpdateElement> retval = new HashSet<UpdateElement>();
            if (mm != null) {
                Set<Module> toUninstall = findRequiredModulesForDeactivate (modules, mm);
                toUninstall.removeAll (modules);
                for (Module module : toUninstall) {
                    if (Utilities.isEssentialModule (module)) {
                        LOGGER.log (Level.WARNING, "Essential module cannot be planned for uninstall but " + module);
                        continue;
                    } else if (! ModuleEnableDisableDeleteHelper.getInstance ().canDelete (module)) {
                        LOGGER.log (Level.WARNING, "The module " + module + " cannot be planned for uninstall because is read-only.");
                        continue;
                    }
                    // !!! e.g. applemodule can be found in the list for uninstall but has UpdateUnit nowhere else MacXOS
                    UpdateUnit unit = Utilities.toUpdateUnit (module);
                    if (unit != null) {
                        retval.add (unit.getInstalled ());
                    }
                }
            }
            return new ArrayList<UpdateElement> (retval);
        }
        
    }
    
    private static class UpdateValidator extends OperationValidator {
        @Override
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled() != null && containsElement (uElement, unit);
        }
        
        @Override
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos,
                                                    Collection<String> brokenDependencies, Collection<UpdateElement> recommendedElements) {
            Set<Dependency> brokenDeps = new HashSet<Dependency> ();
            List<UpdateElement> res = new LinkedList<UpdateElement> (Utilities.findRequiredUpdateElements (uElement, moduleInfos, brokenDeps, true, recommendedElements));
            if (brokenDependencies != null) {
                for (Dependency dep : brokenDeps) {
                    brokenDependencies.add (dep.toString ());
                }
            }
            return res;
        }
    }
    
    private static boolean containsElement (UpdateElement el, UpdateUnit unit) {
        return unit.getAvailableUpdates ().contains (el);
    }
    
    private static class EnableValidator extends OperationValidator {
        @Override
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled () != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case KIT_MODULE :
            case MODULE :
                Module module = Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canEnable (module);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canEnable (m);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                res = false;
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        private List<Module> getModulesToEnable(ModuleManager mm, final Set<Module> modules) {
            List<Module> toEnable = new ArrayList<Module>();
            boolean stateChanged = true;
            while (stateChanged) {
                stateChanged = false;
                try {
                    toEnable = mm.simulateEnable(modules);
                } catch (IllegalArgumentException e) {
                    //#160500
                    LOGGER.log(Level.INFO, "Cannot enable all modules " + modules, e);
                    Set<Module> tempModules = new LinkedHashSet<Module>(modules);
                    for (Module module : tempModules) {
                        if (!Utilities.canEnable(module)) {
                            modules.remove(module);
                            stateChanged = true;
                        }
                    }
                    assert stateChanged : "Can`t enable modules " + modules;
                }
            }
            return toEnable;
        }

        @Override
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies, Collection<UpdateElement> recommendedElements) {
            UpdateElementImpl uElementImpl = Trampoline.API.impl(uElement);
            List<ModuleInfo> expandedModuleInfos = new ArrayList<>(moduleInfos);
            expandedModuleInfos.addAll(uElementImpl.getModuleInfos(true));
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : expandedModuleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (Utilities.canEnable (m)) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            List<UpdateElement> retval = new ArrayList<UpdateElement>();
            Set<Dependency> brokenDeps = new HashSet<Dependency>();
            if (mm != null) {
                List<Module> toEnable = getModulesToEnable(mm, modules);
                for (Module module : toEnable) {
                    if (!modules.contains(module) && Utilities.canEnable (module)) {
                        if (Utilities.toUpdateUnit(module).getInstalled() != null) {
                            retval.add(Utilities.toUpdateUnit(module).getInstalled());
                        }
                    }
                }
            }
            if (uElementImpl.getUpdateUnit() != null &&
                uElementImpl.getType() == UpdateManager.TYPE.FEATURE) {
                FeatureUpdateElementImpl ufi = (FeatureUpdateElementImpl)uElementImpl;
                for (ModuleUpdateElementImpl module : ufi.getContainedModuleElements ()) {
                    Set<UpdateElement> els = findRequiredUpdateElements (module.getUpdateElement (), moduleInfos, brokenDeps, true, 
                           recommendedElements);                
                    if (module.getUpdateUnit().getInstalled() == null) {
                        retval.add(module.getUpdateElement());
                        retval.addAll (els);
                    }
                }
                addMissingElements(ufi, brokenDependencies);
                for (UpdateElement avail : uElementImpl.getUpdateUnit().getAvailableUpdates()) {
                    UpdateElementImpl availImpl = Trampoline.API.impl (avail);
                    if (availImpl.getType() == UpdateManager.TYPE.FEATURE) {
                        // process container modules, which are known, but not installed
                        // and are part of the feature
                        FeatureUpdateElementImpl feature = (FeatureUpdateElementImpl)availImpl;
                        for (ModuleUpdateElementImpl module : feature.getContainedModuleElements ()) {
                            if (module.getUpdateUnit().getInstalled() == null) {
                                retval.add(module.getUpdateElement());
                                retval.addAll (
                                        findRequiredUpdateElements (module.getUpdateElement (), moduleInfos, brokenDeps, true, 
                                   recommendedElements));                
                            }
                        }
                        // process contained modules, which are NOT known into broken deps:
                        addMissingElements(feature, brokenDependencies);
                    }
                }
                for (FeatureUpdateElementImpl fei : ufi.getDependingFeatures()) {
                    retval.addAll(getRequiredElementsImpl(fei.getUpdateElement(), fei.getModuleInfos(), 
                            brokenDependencies, recommendedElements));
                }
                // do IGNORE non-module dependencies from the feature: there may be platform- or java-specific
                // modules that have "broken" requirements.
                for (Dependency d : brokenDeps) {
                    if (d.getType() == Dependency.TYPE_MODULE) {
                        brokenDependencies.add(d.toString());
                    }
                }
            }
            return retval;
        }
    }
    
    private static void addMissingElements(FeatureUpdateElementImpl feature, Collection<String> broken) {
        for (String s : feature.getMissingElements()) {
            broken.add("module " + s); // NOI18N
        }
    }
    
    private static class DisableValidator extends OperationValidator {
        @Override
        boolean isValidOperationImpl(UpdateUnit unit, UpdateElement uElement) {
            return unit.getInstalled () != null && isValidOperationImpl (Trampoline.API.impl (uElement));
        }
        
        private boolean isValidOperationImpl (UpdateElementImpl impl) {
            boolean res = false;
            switch (impl.getType ()) {
            case KIT_MODULE :
            case MODULE :
                Module module = Utilities.toModule (((ModuleUpdateElementImpl) impl).getModuleInfo ());
                res = Utilities.canDisable (module);
                break;
            case STANDALONE_MODULE :
            case FEATURE :
                for (ModuleInfo info : ((FeatureUpdateElementImpl) impl).getModuleInfos ()) {
                    Module m =  Utilities.toModule (info);
                    res |= Utilities.canDisable (m);
                }
                break;
            case CUSTOM_HANDLED_COMPONENT :
                res = false;
                break;
            default:
                assert false : "Not supported for impl " + impl;
            }
            return res;
        }
        
        @Override
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies, Collection<UpdateElement> recommendedElements) {
            ModuleManager mm = null;
            final Set<Module> modules = new LinkedHashSet<Module>();
            for (ModuleInfo moduleInfo : moduleInfos) {
                Module m = Utilities.toModule (moduleInfo);
                if (Utilities.canDisable (m)) {
                    modules.add(m);
                }
                if (mm == null) {
                    mm = m.getManager();
                }
            }
            
            if (mm == null) {
                LOGGER.log (Level.WARNING, "No modules can be disabled when disabling UpdateElement " + uElement);
                return Collections.emptyList ();
            } 
            
            Set<Module> requestedToDisable = findRequiredModulesForDeactivate (modules, mm);

            List<Module> toDisable = mm.simulateDisable (modules);
            boolean wasAdded = requestedToDisable.addAll (toDisable);
            
            // XXX why sometimes happens that no all module for deactivated found?
            // assert ! wasAdded : "The requestedToDisable cannot be enlarged by " + toDisable;
            if (LOGGER.isLoggable (Level.FINE) && wasAdded) {
                toDisable.removeAll (filterCandidatesToDeactivate (modules, requestedToDisable, mm));
                LOGGER.log (Level.FINE, "requestedToDisable was enlarged by " + toDisable);
            }
            
            Set<UpdateElement> retval = new HashSet<UpdateElement> ();
            for (Module module : requestedToDisable) {
                if (! modules.contains (module) && Utilities.canDisable (module)) {
                    // !!! e.g. applemodule can be found in the list for uninstall but has UpdateUnit nowhere else MacXOS
                    UpdateUnit unit = Utilities.toUpdateUnit (module);
                    if (unit != null) {
                        retval.add (unit.getInstalled ());
                    }
                }
            }
            
            return new ArrayList<UpdateElement> (retval);
        }
    }
    
    private static class CustomInstallValidator extends OperationValidator {
        @Override
        boolean isValidOperationImpl (UpdateUnit unit, UpdateElement uElement) {
            boolean res = false;
            UpdateElementImpl impl = Trampoline.API.impl (uElement);
            assert impl != null;
            if (impl instanceof NativeComponentUpdateElementImpl) {
                NativeComponentUpdateElementImpl ni = (NativeComponentUpdateElementImpl) impl;
                if (ni.getInstallInfo ().getCustomInstaller () != null) {
                    res = containsElement (uElement, unit);
                }
            }
            return res;
        }

        @Override
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies, Collection<UpdateElement> recommendedElements) {
            LOGGER.log (Level.INFO, "CustomInstallValidator doesn't care about required elements."); // XXX
            return Collections.emptyList ();
        }
    }

    private static class CustomUninstallValidator extends OperationValidator {
        @Override
        boolean isValidOperationImpl (UpdateUnit unit, UpdateElement uElement) {
            boolean res = false;
            UpdateElementImpl impl = Trampoline.API.impl (uElement);
            assert impl != null;
            if (impl instanceof NativeComponentUpdateElementImpl) {
                NativeComponentUpdateElementImpl ni = (NativeComponentUpdateElementImpl) impl;
                res = ni.getNativeItem ().getUpdateItemDeploymentImpl ().getCustomUninstaller () != null;
            }
            return res;
        }

        @Override
        List<UpdateElement> getRequiredElementsImpl (UpdateElement uElement, List<ModuleInfo> moduleInfos, Collection<String> brokenDependencies, Collection<UpdateElement> recommendedElements) {
            LOGGER.log (Level.INFO, "CustomUninstallValidator doesn't care about required elements."); // XXX
            return Collections.emptyList ();
            //return new LinkedList<UpdateElement> (Utilities.findRequiredUpdateElements (uElement, moduleInfos));
        }
    }

    private static Set<Module> findRequiredModulesForDeactivate (final Set<Module> requestedToDeactivate, ModuleManager mm) {
        // go up and find kits which depending on requestedToDeactivate modules
        Set<Module> extendReqToDeactivate = new HashSet<Module> (requestedToDeactivate);
        boolean inscreasing = true;
        while (inscreasing) {
            inscreasing = false;
            Set<Module> tmp = new HashSet<Module> (extendReqToDeactivate);
            for (Module dep : tmp) {
                Set<Module> deps = Utilities.findDependingModules (dep, mm, module2depending);
                inscreasing |= extendReqToDeactivate.addAll (deps);
            }
        }

        // go down and find all modules (except for other kits) which can be deactivated
        Set<Module> moreToDeactivate = new HashSet<Module> (extendReqToDeactivate);
        inscreasing = true;
        while (inscreasing) {
            inscreasing = false;
            Set<Module> tmp = new HashSet<Module> (moreToDeactivate);
            for (Module req : tmp) {
                if ((! Utilities.isKitModule (req) && ! Utilities.isEssentialModule (req)) || extendReqToDeactivate.contains (req)) {
                    Set<Module> reqs = Utilities.findRequiredModules (req, mm, module2required);
                    inscreasing |= moreToDeactivate.addAll (reqs);
                }
            }
        }

        return filterCandidatesToDeactivate (extendReqToDeactivate, moreToDeactivate, mm);
    }
    
    private static Set<Module> filterCandidatesToDeactivate (final Collection<Module> requested, final Collection<Module> candidates, ModuleManager mm) {
        // go down and find all modules (except other kits) which can be deactivated
        Set<Module> result = new HashSet<Module> ();
        Set<Module> compactSet = new HashSet<Module> (candidates);
        
        // create collection of all installed eagers
        Set<Module> installedEagers = new HashSet<Module> ();
        for (UpdateElement eagerEl : UpdateManagerImpl.getInstance ().getInstalledEagers ()) {
            // take a module
            UpdateElementImpl impl = Trampoline.API.impl (eagerEl);
            if(impl instanceof ModuleUpdateElementImpl) {
                ModuleInfo mi = ((ModuleUpdateElementImpl) impl).getModuleInfo ();
                installedEagers.add (Utilities.toModule (mi));
            } else if(impl instanceof FeatureUpdateElementImpl) {
                List <ModuleInfo> infos = ((FeatureUpdateElementImpl) impl).getModuleInfos();
                for(ModuleInfo mi : infos) {
                    installedEagers.add (Utilities.toModule (mi));
                }
            } else {
                assert false : eagerEl + " is instanceof neither ModuleUpdateElementImpl nor FeatureUpdateElementImpl";
            }
            
        }
        // add installedEagers into affected modules to don't break uninstall of candidates
        compactSet.addAll (installedEagers);
        
        Set<Module> mustRemain = new HashSet<Module> ();
        Set<Module> affectedEagers = new HashSet<Module> ();
        for (Module depM : candidates) {
            if ((Utilities.isKitModule (depM) || Utilities.isEssentialModule (depM)) && ! requested.contains (depM)) {
                if (LOGGER.isLoggable (Level.FINE)) {
                    LOGGER.log(Level.FINE, "The module " + depM.getCodeNameBase() +
                        " is KIT_MODULE and won't be deactivated now not even " + Utilities.findRequiredModules(depM, mm, module2required));
                }
                mustRemain.add (depM);
            } else if (mustRemain.contains (depM)) {
                LOGGER.log (Level.FINE, "The module " + depM.getCodeNameBase () + " was investigated already and won't be deactivated now.");
            } else {
                Set<Module> depends = Utilities.findDependingModules (depM, mm, module2depending);
                if (! compactSet.containsAll (depends)) {
                    mustRemain.add (depM);
                    Set<Module> otherRequiredModules = Utilities.findRequiredModules(depM, mm, module2required);
                    mustRemain.addAll (otherRequiredModules);
                    LOGGER.log (Level.FINE, "The module " + depM.getCodeNameBase () + " is shared and cannot be deactivated now.");
                    if (LOGGER.isLoggable (Level.FINER)) {
                        Set<Module> outsideModules = new HashSet<Module> (depends);
                        outsideModules.removeAll (compactSet);
                        LOGGER.log (Level.FINER, "On " + depM.getCodeNameBase () + " depending modules outside of set now deactivating modules: " + outsideModules);
                        LOGGER.log (Level.FINER, "With " + depM.getCodeNameBase () + " must remain also these required modules: " + otherRequiredModules);
                    }
                } else {
                    result.add (depM);
                    Collection<Module> reducedDepends = new HashSet<Module> (depends);
                    reducedDepends.retainAll (installedEagers);
                    if (! reducedDepends.isEmpty ()) {
                        affectedEagers.addAll (reducedDepends);
                    }
                }
            }
        }
        result.removeAll (installedEagers);

        // add only affected eagers again
        LOGGER.log(Level.FINE, "Possible affected eagers are " + affectedEagers);

        result.removeAll(findDeepRequired(mustRemain, mm));

        // once again check the eagers
        Set<Module> notAffectedEagers = new HashSet<Module>();
        for (Module eager : affectedEagers) {
            Set<Module> requiredByEager = Utilities.findRequiredModules(eager, mm, module2depending);
            if (! requiredByEager.removeAll(result)) {
                notAffectedEagers.add(eager);
            }
        }
        affectedEagers.removeAll(notAffectedEagers);
        result.addAll(affectedEagers);
        LOGGER.log(Level.FINE, "Real affected eagers are " + affectedEagers);

        return result;
    }
    
    private static Set<Module> findDeepRequired (Set<Module> orig, ModuleManager mm) {
        Set<Module> more = new HashSet<Module> (orig);
        boolean inscreasing = true;
        while (inscreasing) {
            Set<Module> tmp = new HashSet<Module> (more);
            inscreasing = false;
            for (Module req : tmp) {
                Set<Module> reqs = Utilities.findRequiredModules (req, mm, module2required);
                inscreasing |= more.addAll (reqs);
            }
        }
        return more;
    }
    
}
