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
import org.netbeans.modules.cloud.oracle.items.DatabaseItem;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.database.DatabaseClient;
import com.oracle.bmc.database.model.AutonomousDatabaseSummary;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseBase;
import com.oracle.bmc.database.model.CreateAutonomousDatabaseDetails;
import com.oracle.bmc.database.model.DatabaseConnectionStringProfile;
import com.oracle.bmc.database.model.GenerateAutonomousDatabaseWalletDetails;
import com.oracle.bmc.database.requests.CreateAutonomousDatabaseRequest;
import com.oracle.bmc.database.requests.GenerateAutonomousDatabaseWalletRequest;
import com.oracle.bmc.database.requests.ListAutonomousDatabasesRequest;
import com.oracle.bmc.database.responses.CreateAutonomousDatabaseResponse;
import com.oracle.bmc.database.responses.GenerateAutonomousDatabaseWalletResponse;
import com.oracle.bmc.identity.Identity;
import com.oracle.bmc.identity.IdentityClient;
import com.oracle.bmc.identity.model.Tenancy;
import com.oracle.bmc.identity.requests.GetTenancyRequest;
import com.oracle.bmc.identity.requests.ListCompartmentsRequest;
import com.oracle.bmc.identity.responses.GetTenancyResponse;
import com.oracle.bmc.identity.responses.ListCompartmentsResponse;
import com.oracle.bmc.model.BmcException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.cloud.oracle.items.CompartmentItem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Manages access to Oracle Cloud Resources
 * 
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "LBL_HomeRegion=Home Region: {0}",
    "LBL_WorkloadType=Workload Type: {0}\n",
    "LBL_DatabaseVersion=Database version: {0}\n",
    "LBL_Storage=Storage: {0}TB"
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
     * @return Optional {@code OCIItem} describing the Tenancy. If Optional.empty() OCI configuration was not found
     */
    public Optional<OCIItem> getTenancy() {
        if (provider == null) {
            return Optional.empty();
        }
        try {
            Identity identityClient = new IdentityClient(provider);
            identityClient.setRegion(configProvider.getRegion());

            GetTenancyRequest gtr = GetTenancyRequest.builder().tenancyId(provider.getTenantId()).build();
            GetTenancyResponse response = identityClient.getTenancy(gtr);
            Tenancy tenancy = response.getTenancy();
            OCIItem item = new OCIItem(tenancy.getId(), tenancy.getName());
            item.setDescription(Bundle.LBL_HomeRegion(tenancy.getHomeRegionKey()));
            return Optional.of(item);
        } catch (Throwable t) {
            Exceptions.printStackTrace(t);
        }
        return Optional.empty();
    }
    
    /**
     * Retrieves list of Compartments in the Tenancy.
     * 
     * @param tenancyId OCID of the Tenancy
     * @return List of {@code OCIItem} describing tenancy Compartments
     */
    public List<CompartmentItem> getCompartments(String tenancyId) {
        Identity identityClient = new IdentityClient(provider);
        identityClient.setRegion(configProvider.getRegion());

        List<CompartmentItem> compartments = new ArrayList<>();

        String nextPageToken = null;
        do {
            ListCompartmentsResponse response
                    = identityClient.listCompartments(
                            ListCompartmentsRequest.builder()
                                    .limit(30)
                                    .compartmentId(tenancyId)
                                    .accessLevel(ListCompartmentsRequest.AccessLevel.Accessible)
                                    .compartmentIdInSubtree(Boolean.TRUE)
                                    .page(nextPageToken)
                                    .build());
            response.getItems().stream()
                    .map(c -> new CompartmentItem(c.getId(), c.getName()))
                    .collect(Collectors.toCollection(() -> compartments));
            nextPageToken = response.getOpcNextPage();
        } while (nextPageToken != null);
        return compartments;
    }

    /**
     * Retrieves list of Databases belonging to a given Compartment.
     * 
     * @param compartmentId OCID of the Compartment
     * @return List of {@code OCIItem} describing databases in a given Compartment
     */
    public List<DatabaseItem> getDatabases(String compartmentId) {
        DatabaseClient client = new DatabaseClient(configProvider);
        ListAutonomousDatabasesRequest listAutonomousDatabasesRequest = ListAutonomousDatabasesRequest.builder()
                .compartmentId(compartmentId)
                .limit(88)
                .build();

        return client.listAutonomousDatabases(listAutonomousDatabasesRequest)
                .getItems()
                .stream()
                .map(d -> {
                    DatabaseItem item = new DatabaseItem(d.getId(), d.getDbName(), d.getServiceConsoleUrl(), getConnectionName(d));
                    StringBuilder sb = new StringBuilder();
                    sb.append(Bundle.LBL_WorkloadType(d.getDbWorkload().getValue()));
                    sb.append(Bundle.LBL_DatabaseVersion(d.getDbVersion()));
                    sb.append(Bundle.LBL_Storage(d.getDataStorageSizeInTBs()));
                    item.setDescription(sb.toString());
                    return item;
                })
                .collect(Collectors.toList());
    }
    
    private String getConnectionName(AutonomousDatabaseSummary summary) {
        List<DatabaseConnectionStringProfile> profiles = summary.getConnectionStrings().getProfiles();
        if (profiles != null) {
            for (DatabaseConnectionStringProfile profile : profiles) {
                if (profile.getDisplayName().contains("high")) { //NOI18N
                    return profile.getDisplayName();
                }
            }
        }
        return null;
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
        DatabaseClient client = new DatabaseClient(configProvider);
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

    /**
     * Creates and downloads a wallet for the specified Autonomous Database.
     * 
     * @param dbInstance Database identification
     * @param password The password to encrypt the keys inside the wallet. 
     *      The password must be at least 8 characters long and must include 
     *      at least 1 letter and either 1 numeric character or 1 special character.
     * @param parentPath Path where Database Wallet Directory will be created.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public Path downloadWallet(OCIItem dbInstance, String password, String parentPath) throws FileNotFoundException, IOException {
        DatabaseClient client = new DatabaseClient(configProvider);

        GenerateAutonomousDatabaseWalletDetails details
                = GenerateAutonomousDatabaseWalletDetails.builder().password(password).build();
        GenerateAutonomousDatabaseWalletRequest generateAutonomousDatabaseWalletRequest
                = GenerateAutonomousDatabaseWalletRequest.builder()
                        .autonomousDatabaseId(dbInstance.getId())
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
