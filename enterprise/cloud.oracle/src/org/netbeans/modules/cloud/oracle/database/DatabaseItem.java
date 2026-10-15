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
package org.netbeans.modules.cloud.oracle.database;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.cloud.oracle.adm.URLProvider;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.vault.SensitiveData;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Horvath
 */
public class DatabaseItem extends OCIItem implements SensitiveData, URLProvider {
    private static final String DEFAULT_REF_NAME = "DEFAULT";
    private final String serviceUrl;
    private final String connectionName;

    public DatabaseItem(OCID id, String compartmentId, String name, String serviceUrl, String connectionName, String tenancyId, String regionCode) {
        super(id, compartmentId, name, tenancyId, regionCode);
        this.serviceUrl = serviceUrl;
        this.connectionName = connectionName;
    }

    public DatabaseItem() {
        super();
        serviceUrl = null;
        connectionName = null;
    }
    
    public String getServiceUrl() {
        return serviceUrl;
    }

    public String getConnectionName() {
        return connectionName;
    }

    @Override
    public int maxInProject() {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public Map<String, String> getSecrets() {
        Map<String, String> secrets = new HashMap<>();
        DatabaseConnection conn = this.getCorrespondingConnection();
        if (conn == null) {
            return secrets;
        }
        
        String refName = getFormattedRefName();

        secrets.put("DATASOURCES_" + refName + "_USERNAME", conn.getUser()); //NOI18N
        secrets.put("DATASOURCES_" + refName + "_PASSWORD", conn.getPassword()); //NOI18N
        secrets.put("DATASOURCES_" + refName + "_WALLET_PASSWORD", UUID.randomUUID().toString()); //NOI18N
        secrets.put("DATASOURCES_" + refName + "_COMPARTMENTOCID", (String) conn.getConnectionProperties().get("CompartmentOCID")); //NOI18N
        String ocid = (String) conn.getConnectionProperties().get("OCID"); //NOI18N
        if (ocid != null && !ocid.isEmpty()) {
            secrets.put("DATASOURCES_" + refName + "_OCID", ocid); //NOI18N
        }
        return secrets;
    }
    
    private String getFormattedRefName() {
        String refName = CloudAssets.getDefault().getReferenceName(this);
        if (refName == null) {
            return DEFAULT_REF_NAME;
        }
        return refName.toUpperCase();
    }
    
    /**
     * Corresponding database connection if exist, or null
     * 
     * @return DatabaseConnection or null
     */
    public DatabaseConnection getCorrespondingConnection() {
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        for (int i = 0; i < connections.length; i++) {
            if (this.getKey().getValue().equals(
                    connections[i].getConnectionProperties().get("OCID"))) { //NOI18N
                return connections[i];
            }
        }
        return null;
    }

    @Override
    public URL getURL() {
        if (getKey().getValue() != null && getRegion() != null) {
            try {
                URI uri = new URI(String.format("https://cloud.oracle.com/db/adbs/%s?region=%s", getKey().getValue(), getRegion()));
                return uri.toURL();
            } catch (MalformedURLException | URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        } 
        return null;
    }
}
