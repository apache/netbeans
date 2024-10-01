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
package org.netbeans.modules.cloud.oracle.assets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.cloud.oracle.bucket.BucketItem;
import org.netbeans.modules.cloud.oracle.developer.MetricsNamespaceItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;

/**
 *
 * @author Jan Horvath
 */
public final class PropertiesGenerator {

    private static final String DEFAULT = "DEFAULT";
    final Map<String, String> application = new HashMap<>();
    final Map<String, String> bootstrap = new HashMap<>();
    final Map<String, String> secrets = new HashMap<>();
    final List<String> allPropertiesNames = Arrays.asList(
            "micronaut.object-storage.oracle-cloud.default.bucket",
            "micronaut.object-storage.oracle-cloud.default.namespace",
            "datasources.default.dialect",
            "datasources.default.ocid",
            "datasources.default.walletPassword",
            "datasources.default.username",
            "datasources.default.password",
            "datasources.default.url",
            "oci.config.instance-principal.enabled",
            "oci.vault.config.enabled",
            "oci.vault.vaults[0].ocid",
            "oci.vault.vaults[0].compartment-ocid",
            "micronaut.metrics.enabled",
            "micronaut.metrics.binders.files.enabled",
            "micronaut.metrics.binders.jdbc.enabled",
            "micronaut.metrics.binders.jvm.enabled",
            "micronaut.metrics.binders.logback.enabled",
            "micronaut.metrics.binders.processor.enabled",
            "micronaut.metrics.binders.uptime.enabled",
            "micronaut.metrics.binders.web.enabled",
            "micronaut.metrics.export.oraclecloud.enabled",
            "micronaut.metrics.export.oraclecloud.namespace",
            "micronaut.metrics.export.oraclecloud.compartmentId"
    );

    public PropertiesGenerator(boolean local) {
        Collection<OCIItem> items = CloudAssets.getDefault().getItems();
        VaultItem vault = null;
        DatabaseConnection conn = null;
        for (OCIItem item : items) {
            if ("Vault".equals(item.getKey().getPath())) {
                vault = (VaultItem) item;
            }
        }
        for (OCIItem item : items) {
            String refName = CloudAssets.getDefault().getReferenceName(item);
            if (refName == null) {
                refName = DEFAULT;
            }
            String nameLowercase = refName.toLowerCase();
            String refNameUppercase = refName.toUpperCase();
            switch (item.getKey().getPath()) {
                case "Bucket": //NOI18N
                    application.put("micronaut.object-storage.oracle-cloud." + nameLowercase + ".bucket", item.getKey().getValue()); //NOI18N
                    application.put("micronaut.object-storage.oracle-cloud." + nameLowercase + ".namespace", ((BucketItem) item).getNamespace()); //NOI18N
                    break;
                case "Databases": //NOI18N
                    DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
                    for (int i = 0; i < connections.length; i++) {
                        if (item.getKey().getValue().equals(
                                connections[i].getConnectionProperties().get("OCID"))) { //NOI18N
                            conn = connections[i];
                            break;
                        }
                    }
                    if (conn != null) {
                        
                        if (vault != null) {
                            application.put("datasources." + nameLowercase + ".dialect", "ORACLE"); // NOI18N
                            application.put("datasources." + nameLowercase + ".ocid", "${DATASOURCES_" + refNameUppercase + "_OCID}"); // NOI18N
                            application.put("datasources." + nameLowercase + ".walletPassword", "${DATASOURCES_" + refNameUppercase + "_WALLET_PASSWORD}"); // NOI18N
                            application.put("datasources." + nameLowercase + ".username", "${DATASOURCES_" + refNameUppercase + "_USERNAME}"); // NOI18N
                            application.put("datasources." + nameLowercase + ".password", "${DATASOURCES_" + refNameUppercase + "_PASSWORD}"); // NOI18N
                            putSecret(refNameUppercase, "Username", conn.getUser()); //NOI18N
                            putSecret(refNameUppercase, "Password", conn.getPassword()); //NOI18N
                            putSecret(refNameUppercase, "OCID", (String) conn.getConnectionProperties().get("OCID")); //NOI18N
                            putSecret(refNameUppercase, "CompartmentOCID", (String) conn.getConnectionProperties().get("CompartmentOCID")); //NOI18N
                            putSecret(refNameUppercase, "wallet_Password", UUID.randomUUID().toString()); //NOI18N
                        } else {
                            if (local) {
                                application.put("datasources." + nameLowercase + ".url", conn.getDatabaseURL()); //NOI18N
                            }
                            application.put("datasources." + nameLowercase + ".username", conn.getUser()); //NOI18N
                            application.put("datasources." + nameLowercase + ".password", conn.getPassword()); //NOI18N
                            application.put("datasources." + nameLowercase + ".walletPassword", UUID.randomUUID().toString()); //NOI18N
                            application.put("datasources." + nameLowercase + ".driverClassName", conn.getDriverClass()); //NOI18N
                            String ocid = (String) conn.getConnectionProperties().get("OCID"); //NOI18N
                            if (ocid != null && !ocid.isEmpty()) {
                                application.put("datasources." + nameLowercase + ".ocid", ocid); //NOI18N
                            }
                        }
                    }
                    break;
                case "MetricsNamespace": //NOI18N                    
                    application.put("micronaut.metrics.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.binders.files.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.binders.jdbc.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.binders.jvm.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.binders.logback.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.binders.processor.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.binders.uptime.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.binders.web.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.export.oraclecloud.enabled", "true"); //NOI18N
                    application.put("micronaut.metrics.export.oraclecloud.namespace", ((MetricsNamespaceItem) item).getName()); //NOI18N
                    application.put("micronaut.metrics.export.oraclecloud.compartmentId", ((MetricsNamespaceItem) item).getCompartmentId()); //NOI18N

            }
        }
        if (!local) {
            bootstrap.put("oci.config.instance-principal.enabled", "true"); // NOI18N
        }
        if (vault != null) {
            bootstrap.put("oci.vault.config.enabled", "true"); // NOI18N
            bootstrap.put("oci.vault.vaults[0].ocid", vault.getKey().getValue()); //NOI18N
            bootstrap.put("oci.vault.vaults[0].compartment-ocid", vault.getCompartmentId()); //NOI18N
        }
    }

    private void putSecret(String datasourceName, String name, String value) {
        String secretName = "DATASOURCES_" + datasourceName + "_" + name; //NOI18N
        secrets.put(secretName, value);
    }

    /**
     * Returns all potentially used property names. This can be useful for cleaning up unused values, such as those in a Config Map.
     * 
     * @return {@link Collection} 
     */
    public Collection<String> getAllPropertiesNames() {
        return allPropertiesNames;
    }
    
    public synchronized Map<String, String> getApplication() {
        return Collections.unmodifiableMap(application);
    }

    public synchronized Map<String, String> getBootstrap() {
        return Collections.unmodifiableMap(bootstrap);
    }

    public synchronized Map<String, String> getVaultSecrets() {
        return Collections.unmodifiableMap(secrets);
    }

}
