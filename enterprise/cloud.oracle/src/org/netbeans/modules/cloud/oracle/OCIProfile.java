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

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
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
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.cloud.oracle.items.OCID;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Represents an OCI profile. A profile has a user, tenancy and region
 * assigned.
 */
public final class OCIProfile implements OCISessionInitiator {

    /**
     * ID of the default profile.
     */
    public static final String DEFAULT_ID = "DEFAULT"; // NOI18N
    /**
     * Profile ID.
     */
    private final String id;
    /**
     * Path to the profile's config file.
     */
    private final Path configPath;
    /**
     * Timestamp of the config file at the time the profile has been initialized.
     */
    private long fileStamp;
    private ConfigFileAuthenticationDetailsProvider configProvider;
    private IOException initError;
    private Tenancy tenancyOpt;
    private static final Logger LOG = Logger.getLogger(OCIProfile.class.getName());

    OCIProfile(Path configPath, String id) {
        this(configPath, id, true);
        init();
    }
    
    OCIProfile(Path configPath, String id, boolean internal) {
        if (id == null) {
            id = DEFAULT_ID;
        }
        if (configPath == null) {
            configPath = OCIManager.getDefaultConfigPath();
        }
        this.id = id;
        this.configPath = configPath;
    }
    
    /**
     * Determines if the profile is defined by the default configuration file.
     * @return true, if the profile comes from the default OCI configuration, false otherwise.
     */
    public boolean isDefaultConfig() {
        return configPath.equals(OCIManager.getDefaultConfigPath());
    }

    private void init() {
        try {
            if (!Files.exists(configPath)) {
                return;
            }
            long stamp = Files.getLastModifiedTime(configPath).toMillis();
            String stringPath = configPath.toAbsolutePath().toString();
            ConfigFileReader.ConfigFile configFile = id == null ? ConfigFileReader.parse(stringPath) : ConfigFileReader.parse(stringPath, id);
            configProvider = new ConfigFileAuthenticationDetailsProvider(configFile);
            fileStamp = stamp;
        } catch (IOException ex) {
            LOG.log(Level.INFO, "init()", ex);
            initError = ex;
        } catch (Throwable ex) {
            LOG.log(Level.INFO, "init()", ex);
            initError = new IOException(ex);
        }
    }

    public Path getConfigPath() {
        return configPath;
    }

    public String getId() {
        return id;
    }

    public boolean isValid() {
        return configProvider != null && fileStamp == configPath.toFile().lastModified(); // avoid IOE
    }

    @Override
    public BasicAuthenticationDetailsProvider getAuthenticationProvider() {
        return configProvider;
    }
    
    @Override
    public Region getRegion() {
        return configProvider.getRegion();
    }

    public Tenancy getTenancyData() {
        if (configProvider == null) {
            return null;
        }
        synchronized (this) {
            if (tenancyOpt != null || initError != null) {
                return tenancyOpt;
            }
        }
        try (final Identity identityClient = new IdentityClient(configProvider)) {
            identityClient.setRegion(configProvider.getRegion());
            GetTenancyRequest gtr = GetTenancyRequest.builder().tenancyId(configProvider.getTenantId()).build();
            GetTenancyResponse response = identityClient.getTenancy(gtr);
            Tenancy tenancy = response.getTenancy();
            synchronized (this) {
                return tenancyOpt = tenancy;
            }
        } catch (Throwable t) {
            LOG.log(Level.INFO, "getTenancyData()", t);            
            initError = new IOException(t);
        }
        return null;
    }
    
    /**
     * Retrieves information about Tenancy configured in ~/.oci
     *
     * @return Optional {@code OCIItem} describing the Tenancy. If
     * Optional.empty() OCI configuration was not found
     */
    @NbBundle.Messages({
        "LBL_HomeRegion=Region: {0}"
    })
    public Optional<TenancyItem> getTenancy() {
        if (configProvider == null) {
            return Optional.empty();
        }
        synchronized (this) {
            if (initError != null) {
                return Optional.empty();
            } else if (tenancyOpt != null) {
                return Optional.of(createTenancyItem(tenancyOpt));
            }
            Tenancy t = getTenancyData();
            return t == null ? Optional.empty() : Optional.of(createTenancyItem(t));
        }
    }
    
    /**
     * Creates and initializes a new client. The client is authenticated by {@link #getAuthenticationProvider()} and
     * its region is set from {@link #getRegion()}. Throws IllegalArgumentException when the client cannot be configured.
     * 
     * @param <T> client type
     * @param clientClass client class
     * @return client instance.
     */
    public <T> T newClient(Class<T> clientClass) {
        try {
            T client = clientClass.getConstructor(BasicAuthenticationDetailsProvider.class).newInstance(configProvider);
            Method setRegion = clientClass.getMethod("setRegion", Region.class);
            setRegion.invoke(client, configProvider.getRegion());
            return client;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Could not initialize client: " + clientClass);
        }
    }
    
    private TenancyItem createTenancyItem(Tenancy tenancy) {
        TenancyItem item = new TenancyItem(OCID.of(tenancy.getId(), "Tenancy"), tenancy.getName());
        item.setDescription(Bundle.LBL_HomeRegion(tenancy.getHomeRegionKey()));
        return item;
    }
    
    private final Lookup lkp = Lookups.fixed(this);

    @Override
    public Lookup getLookup() {
        return lkp;
    }

    public ConfigFileAuthenticationDetailsProvider getConfigProvider() {
        return configProvider;
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
        if (configProvider == null) {
            return null;
        }
        try (final DatabaseClient client = new DatabaseClient(configProvider)) {
            GenerateAutonomousDatabaseWalletDetails details = GenerateAutonomousDatabaseWalletDetails.builder().password(password).build();
            GenerateAutonomousDatabaseWalletRequest generateAutonomousDatabaseWalletRequest = GenerateAutonomousDatabaseWalletRequest.builder().autonomousDatabaseId(dbInstance.getKey().getValue()).generateAutonomousDatabaseWalletDetails(details).build();
            GenerateAutonomousDatabaseWalletResponse response = client.generateAutonomousDatabaseWallet(generateAutonomousDatabaseWalletRequest);
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

    /**
     * Creates a new Autonomous Oracle Database.
     *
     * @param compartmentId Id of Compartment where the Database will be created
     * @param dbName Name of Database
     * @param password Password of ADMIN user
     * @return true if DB was created
     */
    public Optional<String> createAutonomousDatabase(String compartmentId, String dbName, char[] password) {
        if (configProvider == null) {
            return Optional.empty();
        }
        try (final DatabaseClient client = new DatabaseClient(configProvider)) {
            CreateAutonomousDatabaseBase createAutonomousDatabaseBase = CreateAutonomousDatabaseDetails.builder().compartmentId(compartmentId).dbName(dbName).adminPassword(new String(password)).cpuCoreCount(1).dataStorageSizeInTBs(1).build();
            CreateAutonomousDatabaseRequest createAutonomousDatabaseRequest = CreateAutonomousDatabaseRequest.builder().createAutonomousDatabaseDetails(createAutonomousDatabaseBase).build();
            try {
                CreateAutonomousDatabaseResponse response = client.createAutonomousDatabase(createAutonomousDatabaseRequest);
            } catch (BmcException e) {
                return Optional.of(e.getMessage());
            }
            return Optional.empty();
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.id);
        hash = 19 * hash + Objects.hashCode(this.configPath);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OCIProfile other = (OCIProfile) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.configPath, other.configPath);
    }
    
    
}
