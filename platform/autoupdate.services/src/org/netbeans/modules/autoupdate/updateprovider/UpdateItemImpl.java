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

package org.netbeans.modules.autoupdate.updateprovider;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.autoupdate.services.UpdateLicenseImpl;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jiri Rechtacek
 */
public abstract class UpdateItemImpl {
    private UpdateItem originalUpdateItem;
    private List<MessageDigestValue> messageDigests = new ArrayList<>();
    private boolean catalogTrusted = false;

    /** Creates a new instance of UpdateItemImpl */
    UpdateItemImpl () {
    }
    
    public void setUpdateItem (UpdateItem item) {
        originalUpdateItem = item;
    }
    
    public UpdateItem getUpdateItem () {
        return originalUpdateItem;
    }
    
    public abstract String getCodeName ();
    
    /**
     * @return agreement or null
     */
    public abstract UpdateLicenseImpl getUpdateLicenseImpl ();
    
    public abstract void setUpdateLicenseImpl (UpdateLicenseImpl licenseImpl);
    
    /** 
     * @return category or null
     */
    public abstract String getCategory ();
    
    public abstract void setNeedsRestart(Boolean needsRestart);
    
    public boolean isFragment(){
        return getFragmentHost() != null;
    }
    
    public String getFragmentHost() {
        return null;
    }

    public List<MessageDigestValue> getMessageDigests() {
        return messageDigests;
    }

    public void setMessageDigests(List<MessageDigestValue> messageDigests) {
        this.messageDigests = new ArrayList<>(messageDigests);
    }

    public boolean isCatalogTrusted() {
        return catalogTrusted;
    }

    public void setCatalogTrusted(boolean catalogTrusted) {
        this.catalogTrusted = catalogTrusted;
    }
}
