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
import org.netbeans.modules.cloud.oracle.adm.URLProvider;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
public class VaultItem extends OCIItem implements URLProvider {
    private String managementEndpoint;

    public VaultItem(OCID id, String compartment, String name, String managementEndpoint, String tenancyId, String regionCode) {
        super(id, compartment, name, tenancyId, regionCode);
        this.managementEndpoint = managementEndpoint;
    }

    public VaultItem() {
        super();
    }

    public String getManagementEndpoint() {
        return managementEndpoint;
    }
    
    @Override
    public int maxInProject() {
        return 1;
    }
    
    @Override
    public URL getURL() {
        if (getKey().getValue() != null && getRegion() != null) {
            try {
                URI uri = new URI(String.format("https://cloud.oracle.com/security/kms/vaults/%s?region=%s", getKey().getValue(), getRegion()));
                return uri.toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        } 
        return null;
    }
    
}
