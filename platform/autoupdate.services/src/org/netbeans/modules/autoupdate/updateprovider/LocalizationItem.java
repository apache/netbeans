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

import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.netbeans.modules.autoupdate.services.*;
import java.net.URL;
import java.util.Locale;

/**
 *
 * @author Jiri Rechtacek
 */
public class LocalizationItem extends UpdateItemImpl {
    
    private String codeName;
    private String specificationVersion;
    
    private Locale locale;
    private String branding;
    private String moduleSpecificationVersion;
    private String localizedName;
    private String localizedDescription;
    private URL distribution;
    private String category;

    private UpdateItemDeploymentImpl deployImpl;
    private UpdateLicenseImpl licenseImpl;

    public LocalizationItem (
            String codeName,
            String specificationVersion,
            URL distribution,
            Locale locale,
            String branding,
            String moduleSpecificationVersion,
            String localizedName,
            String localizedDescription,
            String category,
            Boolean needsRestart,
            Boolean isGlobal,
            String targetCluster,
            UpdateLicenseImpl licenseImpl) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.distribution = distribution;
        this.locale = locale;
        this.branding = branding;
        this.moduleSpecificationVersion = moduleSpecificationVersion;
        this.localizedName = localizedName;
        this.localizedDescription = localizedDescription;
        this.deployImpl = new UpdateItemDeploymentImpl (needsRestart, isGlobal, targetCluster, null, null);
        this.licenseImpl = licenseImpl;
        this.category = category;
    }
    
    public String getCodeName () {
        return this.codeName;
    }
    
    public String getSpecificationVersion () {
        return this.specificationVersion;
    }
    
    public URL getDistribution () {
        return this.distribution;
    }
    
    public Locale getLocale () {
        return this.locale;
    }
    
    public String getBranding () {
        return this.branding;
    }
    
    public String getMasterModuleSpecificationVersion () {
        return this.moduleSpecificationVersion;
    }
    
    public String getLocalizedModuleName () {
        return this.localizedName;
    }
    
    public String getLocalizedModuleDescription () {
        return this.localizedDescription;
    }
    
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        return this.deployImpl;
    }
    
    public UpdateLicenseImpl getUpdateLicenseImpl () {
        return this.licenseImpl;
    }

    public String getAgreement() {
        return getUpdateLicenseImpl ().getAgreement();
    }

    public String getCategory () {
        return category;
    }

    @Override
    public void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl) {
        this.licenseImpl = licenseImpl;
    }

    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        deployImpl.needsRestart();
    }
}
