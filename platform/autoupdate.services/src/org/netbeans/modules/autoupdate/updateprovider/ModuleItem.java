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

package org.netbeans.modules.autoupdate.updateprovider;

import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.modules.autoupdate.services.*;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public class ModuleItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    private ModuleInfo info;
    private String author;
    private String downloadSize;
    private String homepage;
    private String category;
    private Date publishDate;
    private boolean isEager;
    private boolean isAutoload;
    private boolean isPreferedUpdate;
    private String moduleNotification = null;
    
    private String fragmentHost;
    
    private URL distribution;
    private Manifest manifest;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;
    
    protected ModuleItem () {}

    public ModuleItem (
            String codeName,
            String specificationVersion,
            URL distribution,
            String author,
            String publishDateString,
            String downloadSize,
            String homepage,
            String category,
            Manifest manifest, 
            Boolean isEager,
            Boolean isAutoload,
            Boolean needsRestart,
            Boolean isGlobal,
            Boolean isPrefered,
            String targetCluster,
            UpdateLicenseImpl licenseImpl) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.distribution = distribution;
        this.manifest = manifest;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, null, null);
        if (publishDateString != null && publishDateString.length () > 0) {
            try {
                this.publishDate = Utilities.parseDate(publishDateString);
            } catch (ParseException pe) {
                Logger.getLogger (ModuleItem.class.getName ()).log (Level.INFO, "Parsing \"" + publishDateString + "\" of " + codeName + " throws " + pe.getMessage (), pe);
            } catch (RuntimeException re) {
                Logger.getLogger (ModuleItem.class.getName ()).log (Level.INFO, "Parsing \"" + publishDateString + "\" of " + codeName + " throws " + re.getMessage (), re);
            }
        }
        this.licenseImpl = licenseImpl;
        this.author = author;
        this.downloadSize = downloadSize;
        this.homepage = homepage;
        this.category = category;
        this.isEager = isEager;
        this.isAutoload = isAutoload;
        this.isPreferedUpdate = isPrefered;
    }
    
    @Override
    public String getCodeName () {
        return codeName;
    }
    
    public String getSpecificationVersion () {
        return specificationVersion;
    }
    
    public URL getDistribution () {
        return this.distribution;
    }
    
    public String getAuthor () {
        return author;
    }
    
    public String getHomepage () {
        return homepage;
    }
    
    public int getDownloadSize () {
        int parseInt = 0;
        if (downloadSize == null || downloadSize.length() == 0) {
            return parseInt;
        }
        try {
            parseInt = Integer.parseInt (downloadSize);
        } catch (NumberFormatException ex) {
            Logger.getLogger(ModuleItem.class.getName()).log(Level.WARNING, "Module {0} has invalid value of downloadSize: {1}",
                    new Object[]{this.codeName, downloadSize});
        }
        return parseInt;
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    @Override
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        return this.licenseImpl;
    }
    
    public ModuleInfo getModuleInfo () {        
        if (info == null) {
            Module m = Utilities.toModule (codeName, specificationVersion == null ? null : new SpecificationVersion (specificationVersion));
            info = (m != null) ? m : new DummyModuleInfo (manifest.getMainAttributes ());
        }
        return info;
    }
    
    public String getAgreement() {
        return getUpdateLicenseImpl ().getAgreement();
    }

    @Override
    public String getCategory () {
        return category;
    }
    
    public String getDate () {
        return publishDate == null ? null : Utilities.formatDate(publishDate);
    }
    
    public boolean isAutoload () {
        return isAutoload;
    }

    public boolean isEager () {
        return isEager;
    }
    
    public boolean isPreferredUpdate() {
        return isPreferedUpdate;
    }
    
    public String getModuleNotification () {
        return moduleNotification;
    }
    
    void setModuleNotification (String notification) {
        this.moduleNotification = notification;
    }
    
    public void setFragmentHost(String fhost) {
        this.fragmentHost = fhost;
    }

    public String getFragmentHost() {
        return fragmentHost;
    }

    @Override
    public void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl) {
        this.licenseImpl = licenseImpl;
    }
        
    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        deployImpl.setNeedsRestart(needsRestart);
    }
}
