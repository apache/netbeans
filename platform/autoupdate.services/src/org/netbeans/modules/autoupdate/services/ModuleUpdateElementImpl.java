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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateManager.TYPE;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public class ModuleUpdateElementImpl extends UpdateElementImpl {
    private String codeName;
    private String displayName;
    private SpecificationVersion specVersion;
    private String description;
    private String source;
    private String author;
    private String homepage;
    private int downloadSize;
    private String category;
    private String rawCategory;
    private InstallInfo installInfo;
    private static final Logger log = Logger.getLogger (ModuleUpdateElementImpl.class.getName ());
    private ModuleInfo moduleInfo;
    private ModuleItem item;
    private String providerName;
    private String date;
    private boolean isEager;
    private boolean isAutoload;
    private boolean isPreferredUpdate;
    private String installationCluster;
    
    public ModuleUpdateElementImpl (ModuleItem item, String providerName) {
        super (item, providerName);
        this.moduleInfo = item.getModuleInfo ();
        this.item = item;
        this.providerName = providerName;
        codeName = item.getCodeName ();
        specVersion = item.getSpecificationVersion () == null ? null : new SpecificationVersion (item.getSpecificationVersion ());
        installInfo = new InstallInfo (item);
        author = item.getAuthor ();
        downloadSize = item.getDownloadSize ();
        homepage = item.getHomepage ();
        date = item.getDate ();
        isEager = item.isEager ();
        isAutoload = item.isAutoload ();
        this.isPreferredUpdate = item.isPreferredUpdate();
    }
    
    @Override
    public String getCodeName () {
        return codeName;
    }
    
    @Override
    public String getDisplayName () {
        if (displayName == null) {
            String dn = moduleInfo.getDisplayName ();
            assert dn != null : "Module " + codeName + " doesn't provider display name. Value of \"OpenIDE-Module-Name\" cannot be null.";
            if (dn == null) {
                log.log (Level.WARNING, "Module " + codeName + " doesn't provider display name. Value of \"OpenIDE-Module-Name\" cannot be null.");
            }
            displayName = dn == null ? codeName : dn;
        }
        return displayName;
    }
    
    @Override
    public SpecificationVersion getSpecificationVersion () {
        return specVersion;
    }
    
    @Override
    public String getDescription () {
        if (description == null) {
            description = (String) moduleInfo.getLocalizedAttribute ("OpenIDE-Module-Long-Description");
        }
        return description;
    }
    
    @Override
    public String getNotification() {
        String notification = item.getModuleNotification ();
        if (notification != null) {
            notification = notification.trim();
        }
        return notification;
    }
    
    @Override
    public String getAuthor () {
        return author;
    }
    
    @Override
    public String getHomepage () {
        return homepage;
    }
    
    @Override
    public int getDownloadSize () {
        return downloadSize;
    }
    
    @Override
    public String getSource () {
        if (source == null) {
            source = item instanceof InstalledModuleItem ? ((InstalledModuleItem) item).getSource () : providerName;
            if (source == null) {
                source = Utilities.getProductVersion ();
            }
        }
        return source;
    }
    
    @Override
    public String getDate () {
        return date;
    }
    
    public String getRawCategory() {
        if (rawCategory == null) {
            rawCategory = item.getCategory ();
            if (rawCategory == null) {
                rawCategory = (String) moduleInfo.getLocalizedAttribute ("OpenIDE-Module-Display-Category");
            }
            if (rawCategory == null) {
                rawCategory = "";
            }
        }
        return rawCategory;
    }
    
    @Override
    public String getCategory () {
        if (category == null) {
            category = getRawCategory();
            if (isAutoload () || isFixed ()) {
                category = UpdateUnitFactory.LIBRARIES_CATEGORY;
            } else if (category.isEmpty() && isEager ()) {
                category = UpdateUnitFactory.BRIDGES_CATEGORY;
            } else if (category.isEmpty()) {
                category = UpdateUnitFactory.UNSORTED_CATEGORY;
            }
        }
        return category;
    }
    
    @Override
    public String getLicenseId() {
        if (item instanceof InstalledModuleItem) {
            // find the same item from UC
            UpdateUnitImpl impl = Trampoline.API.impl(getUpdateUnit());
            UpdateElement elWithSameVersion = impl.findUpdateSameAsInstalled();
            if (elWithSameVersion != null) {
                return elWithSameVersion.getLicenseId();
            }
        } else {
            assert item.getUpdateLicenseImpl() != null : item + " has UpdateLicenseImpl.";
            if (item.getUpdateLicenseImpl() != null) {
                return item.getUpdateLicenseImpl().getName();
            }
        }
        return null;
    }
    
    @Override
    public String getLicence () {
        return item.getAgreement ();
    }

    @Override
    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    @Override
    public List<ModuleInfo> getModuleInfos () {
        return Collections.singletonList (getModuleInfo ());
    }
    
    public ModuleInfo getModuleInfo () {
        assert moduleInfo != null : "Each ModuleUpdateElementImpl has ModuleInfo, but " + this;
        
        // find really module info if present
        ModuleInfo info = Utilities.toModule (this.moduleInfo);
        if (info != null) {
            this.moduleInfo = info;
        } else {
            this.moduleInfo = item.getModuleInfo ();
        }
        
        return this.moduleInfo;
    }
    
    @Override
    public TYPE getType () {
        return UpdateManager.TYPE.MODULE;
    }

    @Override
    public boolean isEnabled () {
        return getModuleInfo ().isEnabled ();
    }            
    
    @Override
    public boolean isAutoload () {
        return isAutoload;
    }

    @Override
    public boolean isEager () {
        return isEager;
    }
    
    @Override
    public boolean isPreferredUpdate() {
        return isPreferredUpdate;
    }
    
    @Override
    public boolean isFixed () {
        return Utilities.toModule(getCodeName (), null) == null ? false : Utilities.toModule(getCodeName (), null).isFixed ();
    }
    
    public String getInstallationCluster() {
        if (! (item instanceof InstalledModuleItem)) {
            return null;
        }
        if (installationCluster == null) {
            installationCluster = findInstallationCluster();
        }
        return installationCluster;
    }
    
    private String findInstallationCluster() {
        Module m = Utilities.toModule(this.moduleInfo);
        if (m == null) {
            return null;
        }

        File jarFile = m.getJarFile();
        String res = null;

        if (jarFile != null) {
            for (File cluster : UpdateTracking.clusters(true)) {
                cluster = FileUtil.normalizeFile(cluster);
                if (isParentOf(cluster, jarFile)) {
                    res = cluster.getName();
                    break;
                }
            }
        } else if (UpdateTracking.getPlatformDir() != null) {
            return UpdateTracking.getPlatformDir().getName();
        }
        return res;
    }

    private static boolean isParentOf(File parent, File child) {
        File tmp = child.getParentFile();
        while (tmp != null && !parent.equals(tmp)) {
            tmp = tmp.getParentFile();
        }
        return tmp != null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModuleUpdateElementImpl other = (ModuleUpdateElementImpl) obj;

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
    public String toString () {
        return "Impl[" + getUpdateElement () + "]"; // NOI18N
    }
    
}
