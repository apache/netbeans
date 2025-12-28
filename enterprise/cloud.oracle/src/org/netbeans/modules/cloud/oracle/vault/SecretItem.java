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
package org.netbeans.modules.cloud.oracle.vault;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import org.netbeans.modules.cloud.oracle.adm.URLProvider;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
public class SecretItem extends OCIItem implements URLProvider {
    
    private String lifecycleState;
    private Date deletionTime;
    private String vaultId;

    public SecretItem(OCID id, String compartmentId, String name, String lifecycleState, Date deletionTime, String vaultId, String tenancyId, String regionCode) {
        super(id, compartmentId, name, tenancyId, regionCode);
        this.lifecycleState = lifecycleState;
        this.deletionTime = deletionTime;
        this.vaultId = vaultId;
    }

    public SecretItem() { 
        super();
        this.lifecycleState = null;
        this.deletionTime = null;
    }
    
    @Override
    public int maxInProject() {
        return Integer.MAX_VALUE;
    }
    
    public Date getDeletionTime() {
        return this.deletionTime;
    }

    public String getVaultId() {
        return vaultId;
    }
    
    void setDeletionTime(Date deletionTime) {
        this.deletionTime = deletionTime;
    }
    
    public String getLifecycleState() {
        return this.lifecycleState;
    }
    
    void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }
    
    @Override
    public URL getURL() {
        if (getKey().getValue() != null && getRegion() != null) {
            try {
                URI uri = new URI(String.format("https://cloud.oracle.com/security/kms/vaults/%s/secrets/%s?region=%s", 
                        getVaultId(), getKey().getValue(), getRegion()));
                return uri.toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        } 
        return null;
    }
    
}
