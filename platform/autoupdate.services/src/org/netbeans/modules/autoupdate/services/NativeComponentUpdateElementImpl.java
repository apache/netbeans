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
import java.util.List;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.NativeComponentItem;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public class NativeComponentUpdateElementImpl extends UpdateElementImpl {
    private String codeName;
    private String displayName;
    private SpecificationVersion specVersion;
    private String description;
    private String source;
    private String author;
    private String homepage;
    private int downloadSize;
    private String category;
    private InstallInfo installInfo;
    private NativeComponentItem nativeItem;

    public NativeComponentUpdateElementImpl (NativeComponentItem item, String providerName) {
        super (item, providerName);
        codeName = item.getCodeName ();
        specVersion = item.getSpecificationVersion () == null ? null : new SpecificationVersion (item.getSpecificationVersion ());
        source = providerName;
        installInfo = new InstallInfo (item);
        displayName = item.getDisplayName ();
        description = item.getDescription ();
        downloadSize = item.getDownloadSize ();
        this.nativeItem = item;
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
        return source;
    }
    
    @Override
    public String getCategory () {
        if (category == null) {
            category = UpdateUnitFactory.UNSORTED_CATEGORY;
        }
        return category;
    }
    
    @Override
    public String getDate () {
        return null;
    }
    
    @Override
    public String getLicenseId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String getLicence () {
        return nativeItem.getAgreement ();
    }

    @Override
    public InstallInfo getInstallInfo () {
        return installInfo;
    }
    
    @Override
    public List<ModuleInfo> getModuleInfos () {
        return Collections.emptyList ();
    }

    public NativeComponentItem getNativeItem () {
        return nativeItem;
    }

    @Override
    public UpdateManager.TYPE getType () {
        return UpdateManager.TYPE.CUSTOM_HANDLED_COMPONENT;
    }

    @Override
    public boolean isEnabled () {
        // XXX: how to detect if NativeComponent is enabled or not
        return true;
    }            
    
    @Override
    public boolean isAutoload () {
        return false;
    }

    @Override
    public boolean isEager () {
        return false;
    }
    
    @Override
    public boolean isFixed () {
        return false;
    }
    
    @Override
    public boolean isPreferredUpdate() {
        return false;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final NativeComponentUpdateElementImpl other = (NativeComponentUpdateElementImpl) obj;

        if (this.specVersion != other.specVersion &&
            (this.specVersion == null ||
             !this.specVersion.equals(other.specVersion)))
            return false;
        if (this.codeName != other.codeName &&
            (this.codeName == null || !this.codeName.equals(other.codeName)))
            return false;
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


}
