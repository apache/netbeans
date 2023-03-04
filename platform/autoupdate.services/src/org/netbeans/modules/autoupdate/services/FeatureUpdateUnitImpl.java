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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.updateprovider.ArtificialFeaturesProvider;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import org.openide.util.NbBundle;


public class FeatureUpdateUnitImpl extends UpdateUnitImpl {
    private static final Logger LOG = Logger.getLogger (FeatureUpdateUnitImpl.class.getName());
    private UpdateElement installedElement = null;
    private UpdateElement updateElement = null;
    private boolean initialized = false;
    private UpdateManager.TYPE type;

    public FeatureUpdateUnitImpl (String codename, UpdateManager.TYPE type) {
        super (codename);
        this.type = type;
    }

    @Override
    public UpdateElement getInstalled () {
        synchronized(this) {
            initializeFeature ();                
        }
        return installedElement;
    }
    
    @Override
    public List<UpdateElement> getAvailableUpdates () {
         synchronized(this) {
            initializeFeature ();            
         }
        
        if (updateElement == null) {
            return Collections.emptyList ();
        }
        
        String id = updateElement.getCodeName ();
        LOG.log (Level.FINE, "UpdateElement " + id + "[" +
                (installedElement == null ? "<not installed>" : installedElement.getSpecificationVersion ()) + "] has update " +
                id + "[" + updateElement.getSpecificationVersion () + "]");
        
        return Collections.singletonList (updateElement);
    }

    @Override
    public TYPE getType () {
        return type;
    }
    
    private void initializeFeature () {
        if(initialized) {
            return;
        }
        List<UpdateElement> featureElements = getUpdates ();
        
        installedElement = null;
        updateElement = null;
        
        UpdateElement installedFeatureElement = null;
        FeatureUpdateElementImpl featureImpl = null;
        
        Set<ModuleUpdateElementImpl> installedModules = new HashSet<ModuleUpdateElementImpl> ();
        Set<FeatureUpdateElementImpl> installedFeatures = new HashSet<FeatureUpdateElementImpl> ();
        
        Set<ModuleUpdateElementImpl> availableModules = new HashSet<ModuleUpdateElementImpl> ();
        Set<FeatureUpdateElementImpl> availableFeatures = new HashSet<FeatureUpdateElementImpl> ();
        
        Set<ModuleUpdateElementImpl> missingModules = new HashSet<ModuleUpdateElementImpl> ();
        Set<FeatureUpdateElementImpl> missingFeatures = new HashSet<FeatureUpdateElementImpl> ();

        assert featureElements != null : "FeatureUpdateUnitImpl " + getCodeName () + " contains some available elements.";
        for (UpdateElement el : featureElements) {
            
            featureImpl = (FeatureUpdateElementImpl) Trampoline.API.impl (el);
            boolean installed = false;
            
            for (ModuleUpdateElementImpl moduleImpl : featureImpl.getContainedModuleElements ()) {
                installed |= moduleImpl.getUpdateUnit ().getInstalled () != null;
                UpdateElement iue = moduleImpl.getUpdateUnit ().getInstalled ();
                UpdateElementImpl iuei = iue == null ? null : Trampoline.API.impl (iue);
                assert iuei == null || iuei instanceof ModuleUpdateElementImpl : "Impl of " + iue + " is instanceof ModuleUpdateElementImpl";
                if (iue != null) {
                    installedModules.add ((ModuleUpdateElementImpl) iuei);
                } else {
                    LOG.log (Level.FINER, this.getCodeName () + " misses required module " + moduleImpl.getUpdateElement ());
                    missingModules.add (moduleImpl);
                }
                if (! moduleImpl.getUpdateUnit ().getAvailableUpdates ().isEmpty ()) {
                    UpdateElement aue = moduleImpl.getUpdateUnit ().getAvailableUpdates ().get (0);
                    UpdateElementImpl auei = Trampoline.API.impl (aue);
                    assert auei instanceof ModuleUpdateElementImpl : "Impl of " + aue + " is instanceof ModuleUpdateElementImpl";
                    availableModules.add ((ModuleUpdateElementImpl) auei);
                    LOG.log (Level.FINER, this + " has a update of module " + moduleImpl.getUpdateElement () + " to " + auei.getUpdateElement ());
                }
            }
            
            for (FeatureUpdateElementImpl dependingFeatureImpl : featureImpl.getDependingFeatures()) {
                installed |= dependingFeatureImpl.getUpdateUnit().getInstalled() != null;
                UpdateElement iue = dependingFeatureImpl.getUpdateUnit().getInstalled();
                UpdateElementImpl iuei = iue == null ? null : Trampoline.API.impl(iue);
                assert iuei == null || iuei instanceof FeatureUpdateElementImpl : "Impl of " + iue + " is instanceof FeatureUpdateElementImpl";
                if (iuei != null) {
                    installedFeatures.add((FeatureUpdateElementImpl) iuei);
                } else {
                    LOG.log(Level.FINER, this.getCodeName() + " misses required module " + featureImpl.getUpdateElement());
                    missingFeatures.add(featureImpl);
                }
                if (! dependingFeatureImpl.getUpdateUnit().getAvailableUpdates().isEmpty()) {
                    UpdateElement aue = dependingFeatureImpl.getUpdateUnit().getAvailableUpdates().get(0);
                    UpdateElementImpl auei = Trampoline.API.impl(aue);
                    assert auei instanceof FeatureUpdateElementImpl : "Impl of " + aue + " is instanceof FeatureUpdateElementImpl";
                    availableFeatures.add((FeatureUpdateElementImpl) auei);
                    LOG.log(Level.FINER, this + " has a update of feature " + dependingFeatureImpl.getUpdateElement() + " to " + auei.getUpdateElement());
                }
            }
            
            if (installed) {
                installedFeatureElement = el;
            }
        }
        
        boolean isStandalone = UpdateManager.TYPE.STANDALONE_MODULE == getType ();
        
        // if some element is whole installed
        if (installedFeatureElement != null) {
            // create new one element contains all installed modules
            FeatureItem item = ArtificialFeaturesProvider.createFeatureItem (
                    getCodeName (),
                    installedModules,
                    installedFeatures,
                    featureImpl,
                    isStandalone ? null : presentAddionallyDescription (installedModules, presentMissingModules (missingModules)));
            FeatureUpdateElementImpl featureElementImpl = new FeatureUpdateElementImpl (
                    item,
                    installedFeatureElement.getSource (),
                    installedModules,
                    installedFeatures,
                    ((FeatureUpdateElementImpl) Trampoline.API.impl (installedFeatureElement)).getMissingElements(),
                    featureImpl.getType ());
            installedElement = Trampoline.API.createUpdateElement (featureElementImpl);
            featureElementImpl.setUpdateUnit (installedFeatureElement.getUpdateUnit ());
        }
        
        // add also new update element
        if (! featureElements.isEmpty ()) {
            if (! availableModules.isEmpty () || ! availableFeatures.isEmpty()) {
                // add available modules or features to missing
                missingModules.addAll (availableModules);
                FeatureItem item = ArtificialFeaturesProvider.createFeatureItem (
                        getCodeName (),
                        availableModules,
                        availableFeatures,
                        featureImpl,
                        isStandalone ? null : presentAddionallyDescription (presentUpdatableModules (/* XXX */missingModules), installedModules));
                FeatureUpdateElementImpl featureElementImpl = new FeatureUpdateElementImpl (
                        item,
                        featureElements.get (0).getSource (),
                        availableModules,
                        availableFeatures,
                        null,
                        featureImpl.getType ());
                updateElement = Trampoline.API.createUpdateElement (featureElementImpl);
                featureElementImpl.setUpdateUnit (featureElements.get (0).getUpdateUnit ());
                addUpdate (updateElement);
            }
        }
        
        initialized = true;
    }

    @Override
    public void setInstalled (UpdateElement installed) {
        assert false : "Invalid calling setInstalled (" + installed + ") on FeatureUpdateUnitImpl.";
    }

    @Override
    public void setAsUninstalled () {
        synchronized (this) {
            initialized = false;
        }
    }
    
    @Override
    public void updateInstalled (UpdateElement installed) {
        synchronized (this) {
            initialized = false;
        }
    }
    
    private static String getDisplayNames (Set<ModuleUpdateElementImpl> moduleImpls) {
        assert moduleImpls != null && ! moduleImpls.isEmpty () : "Some ModuleUpdateElementImpl must found to take its display names.";
        String res = "";
        for (ModuleUpdateElementImpl moduleImpl : moduleImpls) {
            res += (res.length () == 0 ? "" : ", ") + moduleImpl.getDisplayName ();
        }
        return res;
    }
    
    private static String presentMissingModules (Set<ModuleUpdateElementImpl> missingModuleImpls) {
        if (missingModuleImpls.isEmpty ()) {
            return "";
        }
        
        boolean once = missingModuleImpls.size () == 1;
        String res;
        if (once) {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_MissingModule", getDisplayNames (missingModuleImpls));
        } else {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_MissingModules", getDisplayNames (missingModuleImpls));
        }
        
        return res;
    }

    private static String presentUpdatableModules (Set<ModuleUpdateElementImpl> updatebleModuleImpls) {
        if (updatebleModuleImpls.isEmpty ()) {
            return "";
        }
        
        boolean once = updatebleModuleImpls.size () == 1;
        String res;
        if (once) {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_UpdatableModule", getDisplayNames (updatebleModuleImpls));
        } else {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_UpdatableModules", getDisplayNames (updatebleModuleImpls));
        }
        
        return res;
    }
    
    private static String presentIncludedModules (Set<ModuleUpdateElementImpl> includedModuleImpls) {
        if (includedModuleImpls.isEmpty ()) {
            return "";
        }
        
        boolean once = includedModuleImpls.size () == 1;
        String res;
        if (once) {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_ContainedModule", getDisplayNames (includedModuleImpls));
        } else {
            res = NbBundle.getMessage (FeatureUpdateUnitImpl.class, "FeatureUpdateUnitImpl_ContainedModules", getDisplayNames (includedModuleImpls));
        }
        
        return res;
    }
    
    private static String presentAddionallyDescription (Set<ModuleUpdateElementImpl> included, String more) {
        String add = presentIncludedModules (included) + more;
        return add.length () > 0 ? add : null;
    }

    private static String presentAddionallyDescription (String more, Set<ModuleUpdateElementImpl> included) {
        String add = more + presentIncludedModules (included);
        return add.length () > 0 ? add : null;
    }

    @Override
    public boolean isPending () {
        return UpdateUnitFactory.getDefault().isScheduledForRestart (getUpdateUnit ());
    }

    @Override
    public UpdateUnit getVisibleAncestor() {
        return this.getUpdateUnit();
    }
}
