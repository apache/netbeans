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
package org.netbeans.modules.cloud.oracle;

import org.netbeans.modules.cloud.oracle.items.OCIItem;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.GenerateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.requests.CreateAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.GenerateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Tenancy;
import com.oracle.bmc.identity.requests.GetTenancyRequest;
import com.oracle.bmc.identity.responses.GetTenancyResponse;
import com.oracle.bmc.model.BmcException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.netbeans.modules.cloud.oracle.items.OCID;

/**
 * Manages access to Oracle Cloud Resources
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "LBL_HomeRegion=Home Region: {0}"
})
public final class OCIManager {

    private ConfigFileReader.ConfigFile configFile;
    private ConfigFileAuthenticationDetailsProvider provider;
    private ConfigFileAuthenticationDetailsProvider configProvider;

    private static OCIManager instance;

    private OCIManager() {
        init();
    }

    private void init() {
        try {
            configFile = ConfigFileReader.parseDefault();
            provider = new ConfigFileAuthenticationDetailsProvider(configFile);
            configProvider = new ConfigFileAuthenticationDetailsProvider(configFile);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static synchronized OCIManager getDefault() {
        if (instance == null) {
            instance = new OCIManager();
        }
        return instance;
    }

    /**
     * Retrieves information about Tenancy configured in ~/.oci
     *
     * @return Optional {@code OCIItem} describing the Tenancy. If
     * Optional.empty() OCI configuration was not found
     */
    public Optional<TenancyItem> getTenancy() {
        if (provider == null) {
            return Optional.empty();
        }
        try (Identity identityClient = new IdentityClient(provider)) {
            identityClient.setRegion(configProvider.getRegion());
            GetTenancyRequest gtr = GetTenancyRequest.builder().tenancyId(provider.getTenantId()).build();
            GetTenancyResponse response = identityClient.getTenancy(gtr);
            Tenancy tenancy = response.getTenancy();
            TenancyItem item = new TenancyItem(OCID.of(tenancy.getId(), "Tenancy"), tenancy.getName());
            item.setDescription(Bundle.LBL_HomeRegion(tenancy.getHomeRegionKey()));
            return Optional.of(item);
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
        }
        return Optional.empty();
    }

    public ConfigFileAuthenticationDetailsProvider getConfigProvider() {
        return configProvider;
    }

    /**
     * Creates a new Autonomous Oracle Database.
     *
     * @param compartmentId Id of Compartment where the Database will be created
     * @param dbName Name of Database
     * @param password Password of ADMIN user
     * @return true if DB was created
     */
    public Optional<String> createAutonomousDatabase(String compartmentId, String dbName, char[] password) {
        try (DatabaseClient client = new DatabaseClient(configProvider)) {
            CreateAutonomousDatabaseBase createAutonomousDatabaseBase = CreateAutonomousDatabaseDetails.builder()
                    .compartmentId(compartmentId)
                    .dbName(dbName)
                    .adminPassword(new String(password))
                    .cpuCoreCount(1)
                    .dataStorageSizeInTBs(1)
                    .build();

            CreateAutonomousDatabaseRequest createAutonomousDatabaseRequest = CreateAutonomousDatabaseRequest.builder()
                    .createAutonomousDatabaseDetails(createAutonomousDatabaseBase).build();

            try {
                CreateAutonomousDatabaseResponse response = client.createAutonomousDatabase(createAutonomousDatabaseRequest);
            } catch (BmcException e) {
                return Optional.of(e.getMessage());
            }
            return Optional.empty();
        }
    }

    /**
     * Creates and downloads a wallet for the specified Autonomous Database.
     *
     * @param dbInstance Database identification
     * @param password The password to encrypt the keys inside the wallet. The
     * password must be at least 8 characters long and must include at least 1
     * letter and either 1 numeric character or 1 special character.
     * @param parentPath Path where Database Wallet Directory will be created.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public Path downloadWallet(OCIItem dbInstance, String password, String parentPath) throws FileNotFoundException, IOException {
        try (DatabaseClient client = new DatabaseClient(configProvider)) {
            GenerateAutonomousDatabaseWalletDetails details
                    = GenerateAutonomousDatabaseWalletDetails.builder().password(password).build();
            GenerateAutonomousDatabaseWalletRequest generateAutonomousDatabaseWalletRequest
                    = GenerateAutonomousDatabaseWalletRequest.builder()
                            .autonomousDatabaseId(dbInstance.getKey().getValue())
                            .generateAutonomousDatabaseWalletDetails(details)
                            .build();
            GenerateAutonomousDatabaseWalletResponse response
                    = client.generateAutonomousDatabaseWallet(generateAutonomousDatabaseWalletRequest);

            Path walletPath = null;
            int i = 1;
            do {
                if (walletPath == null) {
                    walletPath = Paths.get(parentPath, dbInstance.getName());
                } else {
                    walletPath = Paths.get(parentPath, dbInstance.getName() + "_" + i++); //NOI18N
                }
            } while (Files.exists(walletPath));

            Files.createDirectory(walletPath);
            ZipInputStream zin = new ZipInputStream(response.getInputStream());
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                Path entryPath = walletPath.resolve(entry.getName());
                Files.copy(zin, entryPath);
            }
            return walletPath;
        }
    }
}
