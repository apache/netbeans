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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.modules.autoupdate.updateprovider.MessageDigestValue;
import org.netbeans.modules.autoupdate.updateprovider.InstallInfo;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class UpdateElementImpl extends Object {
    private UpdateUnit unit;
    private UpdateElement element;
    private List<MessageDigestValue> messageDigests = new ArrayList<>();
    private boolean catalogTrusted = false;

    public UpdateElementImpl (UpdateItemImpl item, String providerName) {
        if(item.getMessageDigests() != null) {
            messageDigests.addAll(item.getMessageDigests());
        }
        this.catalogTrusted = item.isCatalogTrusted();
    }
    
    public UpdateUnit getUpdateUnit () {
        return unit;
    }
    
    public void setUpdateUnit (UpdateUnit unit) {
        assert unit != null : "UpdateUnit cannot for " + this + " cannot be null.";
        this.unit = unit;
    }
    
    public UpdateElement getUpdateElement () {
        return element;
    }
    
    public void setUpdateElement (UpdateElement element) {
        assert element != null : "UpdateElement cannot for " + this + " cannot be null.";
        this.element = element;
    }
    
    public abstract String getCodeName ();
    
    public abstract String getDisplayName ();
    
    public abstract SpecificationVersion getSpecificationVersion ();
    
    public abstract String getDescription ();
    
    public abstract String getNotification();
    
    public abstract String getAuthor ();
    
    public abstract String getHomepage ();
    
    public abstract int getDownloadSize ();
    
    public abstract String getSource ();
    
    public abstract String getDate ();
    
    public abstract String getCategory ();
    
    public abstract boolean isEnabled ();

    public abstract String getLicence ();
    public abstract String getLicenseId();
    
    public abstract UpdateManager.TYPE getType ();
    
    public abstract boolean isAutoload ();
    public abstract boolean isEager ();
    public abstract boolean isFixed ();
    public abstract boolean isPreferredUpdate();
    
   // XXX: try to rid of this
    public abstract List<ModuleInfo> getModuleInfos ();
    public List<ModuleInfo> getModuleInfos(boolean recursive) {
        return getModuleInfos();
    }
    
    // XXX: try to rid of this
    public abstract InstallInfo getInstallInfo ();

    public List<MessageDigestValue> getMessageDigests() {
        return messageDigests;
    }

    public void setMessageDigests(List<MessageDigestValue> messageDigests) {
        this.messageDigests = messageDigests;
    }

    public boolean isCatalogTrusted() {
        return catalogTrusted;
    }

    public void setCatalogTrusted(boolean catalogTrusted) {
        this.catalogTrusted = catalogTrusted;
    }

}
