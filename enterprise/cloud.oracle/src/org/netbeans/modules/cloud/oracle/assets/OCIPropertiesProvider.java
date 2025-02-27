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


import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.spi.lsp.CommandProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Horvath
 */
@ServiceProvider(service = CommandProvider.class)
public class OCIPropertiesProvider implements CommandProvider {
    private static final String  GET_DB_CONNECTION = "nbls.db.connection"; //NOI18N

    private final TempFileGenerator configFileGenerator;

    public OCIPropertiesProvider() {
        this.configFileGenerator = new TempFileGenerator("oci-", ".properties"); // NOI18N
    }

    @Override
    public CompletableFuture<Object> runCommand(String command, List<Object> arguments) {
        Map<String, String> result = new HashMap<> ();
        CompletableFuture ret = new CompletableFuture();
        Properties dbProps = new Properties();
        DatabaseConnection conn = ConnectionManager.getDefault().getPreferredConnection(true);

        PropertiesGenerator propGen = new PropertiesGenerator(true);
        dbProps.putAll(propGen.getApplication());
        dbProps.putAll(propGen.getBootstrap());
        // If cloud assets are empty, try to get properties for a preferred DB connection
        if (conn != null && dbProps.isEmpty()) {
            dbProps.put("datasources.default.url", conn.getDatabaseURL()); //NOI18N
            dbProps.put("datasources.default.username", conn.getUser()); //NOI18N
            dbProps.put("datasources.default.password", conn.getPassword()); //NOI18N
            dbProps.put("datasources.default.driverClassName", conn.getDriverClass()); //NOI18N
            String ocid = (String) conn.getConnectionProperties().get("OCID"); //NOI18N
            if (ocid != null && !ocid.isEmpty()) {
                dbProps.put("datasources.default.ocid", ocid); //NOI18N
            }
        }
        if (!dbProps.isEmpty()) {
            try {
                Path temp = configFileGenerator.writePropertiesFile(dbProps);
                result.put("MICRONAUT_CONFIG_FILES", temp.toAbsolutePath().toString()); // NOI18N
            } catch (IOException ex) {
                ret.completeExceptionally(ex);
                return ret;
            }
        }
        ret.complete(result);
        return ret;
    }

    @Override
    public Set<String> getCommands() {
        return Collections.singleton(GET_DB_CONNECTION);
    }
}
