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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.cloud.oracle.bucket.BucketItem;
import org.netbeans.modules.cloud.oracle.items.OCIItem;

/**
 *
 * @author Jan Horvath
 */
public final class PropertiesGenerator {

    private PropertiesGenerator() {
    }

    public static Map<String, String> getProperties(String environment) {
        Map<String, String> result = new HashMap<>();
        for (OCIItem item : CloudAssets.getDefault().getItems()) {
            switch (item.getKey().getPath()) {
                case "Bucket": //NOI18N
                    result.put("micronaut.object-storage.oracle-cloud.default.bucket", item.getKey().getValue()); //NOI18N
                    result.put("micronaut.object-storage.oracle-cloud.default.namespace", ((BucketItem) item).getNamespace()); //NOI18N
                    break;
                case "Databases": //NOI18N
                    DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
                    DatabaseConnection conn = null;
                    for (int i = 0; i < connections.length; i++) {
                        if (item.getKey().getValue().equals(
                                connections[i].getConnectionProperties().get("OCID"))) { //NOI18N
                            conn = connections[i];
                            break;
                        }
                    }
                    if (conn != null) {
                        result.put("datasources.default.url", conn.getDatabaseURL()); //NOI18N
                        result.put("datasources.default.username", conn.getUser()); //NOI18N
                        result.put("datasources.default.password", conn.getPassword()); //NOI18N
                        result.put("datasources.default.driverClassName", conn.getDriverClass()); //NOI18N
                        String ocid = (String) conn.getConnectionProperties().get("OCID"); //NOI18N
                        if (ocid != null && !ocid.isEmpty()) {
                            result.put("datasources.default.ocid", ocid); //NOI18N
                        }
                    }
                    break;
                case "Vault": 
                    result.put("oci.vault.vaults[0].ocid", item.getKey().getValue()); //NOI18N
                    result.put("oci.vault.vaults[0].compartment-ocid", item.getCompartmentId()); //NOI18N
            }
        }
        return result;
    }

}
