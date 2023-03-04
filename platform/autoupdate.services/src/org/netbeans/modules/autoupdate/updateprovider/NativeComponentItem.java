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

import org.netbeans.modules.autoupdate.services.*;
import java.util.Set;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.CustomUninstaller;

/**
 *
 * @author Jiri Rechtacek
 */
public class NativeComponentItem extends UpdateItemImpl {
    
    private boolean isInstalled;
    private String codeName;
    private String specificationVersion;
    private Set<String> dependencies;
    private String displayName;
    private String description;
    private String downloadSize;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;
    
    public NativeComponentItem (
            boolean isInstalled,
            String codeName,
            String specificationVersion,
            String downloadSize,
            Set<String> dependencies,
            String displayName,
            String description,
            Boolean needsRestart,
            Boolean isGlobal,
            String targetCluster,
            CustomInstaller installer,
            CustomUninstaller uninstaller,
            UpdateLicenseImpl license) {
        this.isInstalled = isInstalled;
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.dependencies = dependencies;
        this.displayName = displayName;
        this.description = description;
        this.licenseImpl = license;
        this.downloadSize = downloadSize;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, installer, uninstaller);
    }
    
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public String getDisplayName () {
        return this.displayName;
    }
    
    public String getDescription () {
        return this.description;
    }
    
    public Set<String> getDependenciesToModules () {
        return this.dependencies;
    }
    
    public int getDownloadSize () {
        return isInstalled ? 0 : Integer.parseInt (downloadSize);
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        assert false : "Invalid call getUpdateLicenseImpl() on NativeComponentItem.";
        return this.licenseImpl;
    }
    
    public String getAgreement () {
        return null;
        //return licenseImpl.getAgreement ();
    }

    public String getCategory () {
        throw new UnsupportedOperationException ("Not supported yet.");
    }

    @Override
    public void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl) {
        this.licenseImpl = licenseImpl;
    }

    public boolean isInstalled () {
        return isInstalled;
    }
    
    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        deployImpl.setNeedsRestart(needsRestart);
    }
}
