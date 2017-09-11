/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.autoupdate.services;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.modules.autoupdate.updateprovider.ArtificialFeaturesProvider;
import org.netbeans.modules.autoupdate.updateprovider.FeatureItem;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.openide.modules.Dependency;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class FeatureUpdateElementImpl extends UpdateElementImpl {
    private String codeName;
    private String displayName;
    private SpecificationVersion specVersion;
    private String description;
    private String homepage;
    private String category;
    private InstallInfo installInfo;
    private static final Logger LOG = Logger.getLogger (FeatureUpdateElementImpl.class.getName ());
    private Set<ModuleUpdateElementImpl> moduleElementsImpl;
    private Set<FeatureUpdateElementImpl> featureElementsImpl;
    private UpdateManager.TYPE type;
    
    public FeatureUpdateElementImpl (FeatureItem item, String providerName,
            Set<ModuleUpdateElementImpl> moduleElementsImpl,
            Set<FeatureUpdateElementImpl> featureElementsImpl,
            UpdateManager.TYPE type) {
        super (item, providerName);
        this.type = type;
        this.moduleElementsImpl = moduleElementsImpl;
        this.featureElementsImpl = featureElementsImpl;
        codeName = item.getCodeName ();
        String itemSpec = item.getSpecificationVersion ();
        if (itemSpec == null) {
            LOG.log (Level.INFO, codeName + " has no specificationVersion.");
        } else {
            specVersion = new SpecificationVersion (itemSpec);
        }
        installInfo = new InstallInfo (item);
        displayName = item.getDisplayName ();
        description = item.getDescription ();
        category = item.getCategory ();
        if (category == null) {
            category = NbBundle.getMessage (UpdateElementImpl.class, "UpdateElementImpl_Feature_CategoryName");
        }
    }
    
    @Override
    public String getCodeName () {
        return codeName;
    }
    
    @Override
    public String getDisplayName () {
        return displayName;
    }
    
    @Override
    public SpecificationVersion getSpecificationVersion () {
        if (specVersion == null) {
            specVersion = new SpecificationVersion (ArtificialFeaturesProvider.createVersion (getModuleInfos ()));
        }
        return specVersion;
    }
    
    @Override
    public String getDescription () {
        return description;
    }
    
    @Override
    public String getNotification() {
        return null;
    }
    
    @Override
    public String getAuthor () {
        String res = "";
        Set<String> authors = new HashSet<String> ();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (impl.getAuthor () != null) {
                if (authors.add (impl.getAuthor ())) {
                    res += res.length () == 0 ? impl.getAuthor () : ", " + impl.getAuthor (); // NOI18N
                }
            }
        }
        return res;
    }
    
    @Override
    public String getHomepage () {
        return homepage;
    }
    
    @Override
    public int getDownloadSize () {
        int res = 0;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (! impl.getUpdateUnit ().getAvailableUpdates ().isEmpty ()) {
                res += impl.getUpdateUnit ().getAvailableUpdates ().get (0).getDownloadSize ();
            }
        }
        return res;
    }
    
    @Override
    public String getSource () {
        String res = "";
        Set<String> sources = new HashSet<String> ();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (sources.add (impl.getSource ())) {
                res += res.length () == 0 ? impl.getSource () : ", " + impl.getSource (); // NOI18N
            }
        }
        return res;
    }
    
    @Override
    public String getCategory () {
        if (isAutoload () || isFixed ()) {
            category = UpdateUnitFactory.LIBRARIES_CATEGORY;
        } else if (isEager ()) {
            category = UpdateUnitFactory.BRIDGES_CATEGORY;
        } else if (category == null || category.length () == 0) {
            category = UpdateUnitFactory.UNSORTED_CATEGORY;
        }
        return category;
    }
    
    @Override
    public String getDate () {
        String res = null;
        Date date = null;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            String sd = impl.getDate ();
            if (sd != null) {
                try {
                    Date d = Utilities.parseDate(sd);
                    date = date == null ? d : new Date (Math.max (date.getTime (), d.getTime ()));
                } catch (ParseException pe) {
                    assert false : pe + " cannot happened.";
                }
            }
        }
        if (date != null) {
            res = Utilities.formatDate(date);
        }
        return res;
    }
    
    @Override
    public String getLicenseId() {
        String res = "";
        Set<String> ids = new HashSet<String>();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements()) {
            if (!impl.getUpdateUnit().getAvailableUpdates().isEmpty()) {
                String id = impl.getUpdateUnit().getAvailableUpdates().get(0).getLicenseId();
                if (ids.add(id)) {
                    res += res.length() == 0 ? id : "," + id; // NOI18N
                }
            }
        }
        return res;
    }
    
    @Override
    public String getLicence () {
        String res = "";
        Set<String> licenses = new HashSet<String> ();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            if (! impl.getUpdateUnit ().getAvailableUpdates ().isEmpty ()) {
                String lic = impl.getUpdateUnit ().getAvailableUpdates ().get (0).getLicence ();
                if (licenses.add (lic)) {
                    res += res.length () == 0 ? lic : "<br>" + lic; // NOI18N
                }
            }
        }
        return res;
    }

    @Override
    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    @Override
    public List<ModuleInfo> getModuleInfos () {
        return getModuleInfos(false);
    }
    
    @Override
    public List<ModuleInfo> getModuleInfos(boolean recursive) {
        List<ModuleInfo> infos = new ArrayList<ModuleInfo>();
        for (ModuleUpdateElementImpl impl : getContainedModuleElements()) {
            if (! infos.contains(impl.getModuleInfo())) {
                infos.add(impl.getModuleInfo());
            }
        }
        if (recursive) {
            for (FeatureUpdateElementImpl featureImpl : getDependingFeatures()) {
                for (ModuleUpdateElementImpl modImpl : featureImpl.getContainedModuleElements()) {
                    if (! infos.contains(modImpl.getModuleInfo())) {
                        infos.add(modImpl.getModuleInfo());
                    }
                }
            }
        }
        return infos;
    }
    
    public Set<ModuleUpdateElementImpl> getContainedModuleElements () {
        assert moduleElementsImpl != null : "FeatureUpdateElementImpl contains modules " + moduleElementsImpl;
        return moduleElementsImpl;
    }
    
    public Set<FeatureUpdateElementImpl> getDependingFeatures() {
        assert featureElementsImpl != null : "FeatureUpdateElementImpl contains features " + featureElementsImpl;
        return featureElementsImpl;
    }

    @Override
    public UpdateManager.TYPE getType () {
        return type;
    }

    @Override
    public boolean isEnabled () {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res &= impl.isEnabled ();
        }
        for (FeatureUpdateElementImpl featureImpl : getDependingFeatures()) {
            res &= featureImpl.isEnabled();
        }
        return res;
    }
    
    @Override
    public boolean isAutoload () {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res &= impl.isAutoload ();
        }
        for (FeatureUpdateElementImpl featureImpl : getDependingFeatures()) {
            res &= featureImpl.isAutoload();
        }
        return res;
    }

    @Override
    public boolean isEager () {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res &= impl.isEager ();
        }
        for (FeatureUpdateElementImpl featureImpl : getDependingFeatures()) {
            res &= featureImpl.isEager();
        }
        return res;
    }
    
    @Override
    public boolean isFixed () {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements ()) {
            res &= impl.isFixed ();
        }
        for (FeatureUpdateElementImpl featureImpl : getDependingFeatures()) {
            res &= featureImpl.isFixed();
        }
        return res;
    }
    
    @Override
    public boolean isPreferredUpdate() {
        boolean res = true;
        for (ModuleUpdateElementImpl impl : getContainedModuleElements()) {
            res &= impl.isPreferredUpdate();
        }
        for (FeatureUpdateElementImpl featureImpl : getDependingFeatures()) {
            res &= featureImpl.isPreferredUpdate();
        }
        return res;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FeatureUpdateElementImpl other = (FeatureUpdateElementImpl) obj;

        if (this.specVersion != other.specVersion &&
            (this.specVersion == null ||
             !this.specVersion.equals(other.specVersion))) {
            return false;
        }
        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 61 * hash + (this.codeName != null ? this.codeName.hashCode()
                                                  : 0);
        hash = 61 * hash +
               (this.specVersion != null ? this.specVersion.hashCode()
                                         : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return "FeatureUpdateElementImpl[" + this.codeName + "/" + this.specVersion + "]";
    }

    public static class Agent extends FeatureUpdateElementImpl {
        
        private Set<ModuleUpdateElementImpl> moduleElementsImpl;
        private Set<FeatureUpdateElementImpl> featureElementsImpl;
        private FeatureItem featureItem;
        
        public Agent (FeatureItem item, String providerName, UpdateManager.TYPE type) {
            super (item, providerName, null, null, type);
            this.featureItem = item;
        }
        
        @Override
        public Set<ModuleUpdateElementImpl> getContainedModuleElements () {
            initializeAgent();
            assert moduleElementsImpl != null : "FeatureUpdateElementImpl contains modules " + moduleElementsImpl;
            return moduleElementsImpl;
        }
        
        @Override
        public Set<FeatureUpdateElementImpl> getDependingFeatures() {
            initializeAgent();
            assert featureElementsImpl != null : "FeatureUpdateElementImpl depends on features " + featureElementsImpl;
            return featureElementsImpl;
        }
        
        private void initializeAgent() {
            synchronized(this) {
                if (featureElementsImpl == null || moduleElementsImpl == null) {
                    Set<FeatureUpdateElementImpl> depFeatures = new HashSet<FeatureUpdateElementImpl>();
                    moduleElementsImpl = processContainedModules (featureItem.getModuleCodeNames (), null, depFeatures);
                    featureElementsImpl = depFeatures;
                }
            }
        }

        private Set<ModuleUpdateElementImpl> processContainedModules (
            Set<String> dependenciesToModulesOrFeatures,
            UpdateUnitProvider provider,
            Set<FeatureUpdateElementImpl> depFeatures
        ) {
            Set<ModuleUpdateElementImpl> res = new HashSet<ModuleUpdateElementImpl> ();
            assert dependenciesToModulesOrFeatures != null : "Invalid Feature " + this + " with null modules.";
            if (dependenciesToModulesOrFeatures == null) {
                dependenciesToModulesOrFeatures = new HashSet<String>();
            } else {
                dependenciesToModulesOrFeatures = new HashSet<String>(dependenciesToModulesOrFeatures);
            }
            Set<Dependency> deps = new HashSet<Dependency> ();
            for (String depSpec : dependenciesToModulesOrFeatures) {
                deps.addAll (Dependency.create (Dependency.TYPE_MODULE, depSpec));
            }
            List<UpdateUnit> moduleUnits = provider == null ?
                UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE) :
                provider.getUpdateUnits (UpdateManager.TYPE.MODULE);
            for (UpdateUnit unit : moduleUnits) {
                for (Iterator<Dependency> it = deps.iterator(); it.hasNext();) {
                    Dependency dep = it.next();
                    assert Dependency.TYPE_MODULE == dep.getType () : "Only Dependency.TYPE_MODULE supported, but " + dep;
                    String name = dep.getName ();
                    // trim release impl.
                    if (name.indexOf ('/') != -1) {
                        int to = name.indexOf ('/');
                        name = name.substring (0, to);
                    }
                    if (unit.getCodeName ().equals (name)) {
                        UpdateElement el = getMatchedUpdateElement (unit, dep);
                        if (el != null) {
                            assert Trampoline.API.impl (el) instanceof ModuleUpdateElementImpl : "Impl of " + el + " is instanceof ModuleUpdateElementImpl.";
                            ModuleUpdateElementImpl impl = (ModuleUpdateElementImpl) Trampoline.API.impl (el);
                            res.add (impl);
                            dependenciesToModulesOrFeatures.remove(name);
                            it.remove();
                        } else {
                            LOG.log (Level.INFO, getUpdateUnit () + " requires a module " + name + " what is not present.");
                        }
                    }
                }
            }
            if (!dependenciesToModulesOrFeatures.isEmpty()) {
                List<UpdateUnit> features = provider == null
                        ? UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE)
                        : provider.getUpdateUnits(UpdateManager.TYPE.FEATURE);
                for (UpdateUnit feat : features) {
                    for (Iterator<Dependency> it = deps.iterator(); it.hasNext();) {
                        Dependency dep = it.next();
                        final String name = dep.getName();
                        if (name.equals(feat.getCodeName())) {
                            UpdateElement el = getMatchedUpdateElement(feat, dep);
                            if (el != null) {
                                assert Trampoline.API.impl(el) instanceof FeatureUpdateElementImpl : "Impl of " + el + " is instanceof FeatureUpdateElementImpl.";
                                FeatureUpdateElementImpl impl = (FeatureUpdateElementImpl) Trampoline.API.impl(el);
                                if (depFeatures.add(impl)) {
                                    //res.addAll(impl.getContainedModuleElements());
                                }
                                dependenciesToModulesOrFeatures.remove(name);
                                it.remove();
                            } else {
                                LOG.log(Level.INFO, getUpdateUnit() + " requires a feature " + name + " what is not present.");
                            }
                        }
                    }
                }
            }
            for (Iterator<String> it = dependenciesToModulesOrFeatures.iterator(); it.hasNext();) {
                String depSpec = it.next();
                for (Dependency dep : Dependency.create (Dependency.TYPE_MODULE, depSpec)) {
                    String cnb = dep.getName ();
                    // trim release impl.
                    if (cnb.indexOf ('/') != -1) {
                        int to = cnb.indexOf ('/');
                        cnb = cnb.substring (0, to);
                    }
                    Module m = Utilities.toModule(cnb, null);
                    if (m != null && ! m.getProblems().isEmpty()) {
                        dependenciesToModulesOrFeatures.remove(depSpec);
                    }
                }
            }
            if (!dependenciesToModulesOrFeatures.isEmpty()) {
                LOG.log(Level.INFO, this.featureItem + " depends on non-existing " + dependenciesToModulesOrFeatures);
            }
            return res;
        }

        private static UpdateElement getMatchedUpdateElement (UpdateUnit unit, Dependency dep) {
            // find installed
            if (match (unit.getInstalled (), dep)) {
                return unit.getInstalled ();
            } else {
                // find available updates
                if (! unit.getAvailableUpdates ().isEmpty ()) {
                    if (match (unit.getAvailableUpdates ().get (0), dep)) {
                        return unit.getAvailableUpdates ().get (0);
                    }
                }
            }
            return null;
        }

        private static boolean match (UpdateElement el, Dependency dep) {
            if (el == null) {
                return false;
            }
            UpdateElementImpl impl = Trampoline.API.impl(el);
            if (impl instanceof FeatureUpdateElementImpl) {
                if (dep.getVersion() == null) {
                    return true;
                }
                SpecificationVersion v = new SpecificationVersion(dep.getVersion());
                return v.compareTo(impl.getSpecificationVersion()) >= 0;
            }
            return DependencyChecker.checkDependencyModuleAllowEqual (dep, Utilities.takeModuleInfo (el));
        }

   }

}
