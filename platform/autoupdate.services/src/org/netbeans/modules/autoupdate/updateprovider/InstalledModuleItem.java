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

import org.netbeans.Module;
import org.netbeans.modules.autoupdate.services.UpdateItemDeploymentImpl;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.openide.modules.ModuleInfo;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstalledModuleItem extends ModuleItem {
    
    private String codeName;
    private String specificationVersion;
    private ModuleInfo info;
    private String author;
    private String source;
    private String installCluster;
    private String installDate;
    
    public InstalledModuleItem (
            String codeName,
            String specificationVersion,
            ModuleInfo info,
            String author,
            String installCluster,
            String installTime) {
        this.codeName = codeName;
        this.specificationVersion = specificationVersion;
        this.info = info;
        this.author = author;
        this.installCluster = installCluster;
        this.installDate = installTime;
    }
    
    @Override
    public final String getCodeName () {
        return codeName;
    }
    
    @Override
    public final String getSpecificationVersion () {
        return specificationVersion;
    }
    
    public String getSource () {
        if (source == null) {
            source = Utilities.readSourceFromUpdateTracking (info);
        }
        // fallback to product version
        if (source == null) {
            source = Utilities.getProductVersion ();
        }
        return source;
    }
    
    @Override
    public String getAuthor () {
        return author;
    }
    
    @Override
    public ModuleInfo getModuleInfo () {        
        return info;
    }
    
    @Override
    public String getAgreement () {
        assert false : "Don't call getAgreement() on InstalledModuleItem " + info;
        return null;
    }

    @Override
    public int getDownloadSize () {
        return 0;
    }
    
    @Override
    public String getDate () {
        return installDate;
    }
    
    @Override
    public UpdateItemDeploymentImpl getUpdateItemDeploymentImpl () {
        assert false : "Don't call getUpdateItemDeploymentImpl () on InstalledModuleItem.";
        return null;
    }
    
    @Override
    public boolean isAutoload () {
        return getModule () != null && getModule ().isAutoload ();
    }

    @Override
    public boolean isEager () {
        return getModule () != null && getModule ().isEager ();
    }
    
    private Module getModule () {
        if (info instanceof Module) {
            return (Module)info;
        }
        return null;
    }
    
    @Override
    public void setNeedsRestart(Boolean needsRestart) {
        // do nothing
    } 
   
    @Override
    public String getFragmentHost() {
        Object o = info.getAttribute("OpenIDE-Module-Fragment-Host"); // NOI18N
        return o instanceof String ? (String)o : null;
    }
}
