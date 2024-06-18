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
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.netbeans.api.server.properties.InstanceProperties;
import org.netbeans.api.server.properties.InstancePropertiesManager;
import org.netbeans.modules.cloud.oracle.items.TenancyItem;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Manages access to Oracle Cloud Resources
 *
 * @author Jan Horvath
 */
public final class OCIManager {
    private static final Logger LOG = Logger.getLogger(OCIManager.class.getName());
    
    public static final String PROP_ACTIVE_PROFILE = "activeProfile"; // NOI18N
    public static final String PROP_CONNECTED_PROFILES = "connectedProfiles"; // NOI18N
    
    /**
     * Maps all created profiles.
     */
    private static final Map<OCIConfig, OCIProfile> profiles = new HashMap<>();
    
    // @GuardedBy(this)
    /**
     * Implicit profiles inferred at runtime. Unitialized initially, will auto-populate
     * to the default config on first reference, if OCIManager_Autoload_DefaultConfig=true.
     */
    private Map<String, List<OCIProfile>> implicitProfiles = new HashMap<>();
    
    // @GuardedBy(this)
    /**
     * Not read, but used to keep the listern from GC.
     */
    private FileChangeListener defaultProfileListener;
    
    /**
     * Configuration for the OCI manager
     */
    public static final class OCIConfig {
        
        /**
         * Configuration file location, or {@code null} for the default location.
         */
        private final Path configPath;
        
        /**
         * The profile ID, or {@code null} for the default profile.
         */
        private final String profile;
        
        private long timestamp;

        public OCIConfig(Path configFile, String profile) {
            this.configPath = configFile;
            this.profile = profile;
        }

        public Path getConfigFile() {
            return configPath;
        }

        public String getProfile() {
            return profile;
        }

        @Override
        public int hashCode() {
            int hash = 7;
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
            final OCIConfig other = (OCIConfig) obj;
            if (!Objects.equals(this.configPath, other.configPath)) {
                return false;
            }
            return Objects.equals(this.profile, other.profile);
        }
    }
    
    private static Path defaultConfigPath;
    
    static Path getDefaultConfigPath() {
        if (defaultConfigPath != null) {
            return defaultConfigPath;
        }
        String filename = System.getenv(ConfigFileReader.OCI_CONFIG_FILE_PATH_ENV_VAR_NAME);
        if (filename == null) {
            filename = ConfigFileReader.DEFAULT_FILE_PATH;
        }
        // the BMC library uses / as filename separator, assuming the env variable does the same
        String replacedFile = filename.replace("~", new File(System.getProperty("user.home")).getAbsolutePath());
        try {
            try {
                defaultConfigPath = Paths.get(new URI("file://" + replacedFile));
            } catch (URISyntaxException ex) {
                LOG.log(Level.WARNING, "Unable to parse default config path {0} as a file:// URI", replacedFile);
                defaultConfigPath = Paths.get(replacedFile);
            }
        } catch (IllegalArgumentException ex) {
            // illegal path - what to do ?!? use a fallback
            LOG.log(Level.WARNING, "Unable to parse default config path {0} even as a path", replacedFile);
            defaultConfigPath = Paths.get(System.getProperty("user.home"), ".oci", "config"); // NOI18N
        }
        return defaultConfigPath;
    }
    
    private PropertyChangeSupport listeners;
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new PropertyChangeSupport(this);
            }
            listeners.addPropertyChangeListener(listener);
        }
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        synchronized (this) {
            if (listeners == null) {
                return;
            }
            listeners.removePropertyChangeListener(listener);
        }
    }

    /**
     * Checks that the manager is still valid. Note that even invalid managers may succeed in 
     * performing tasks like creating database or downloading a wallet, provided that different
     * parts of OCI config changed.
     * @return manager is valid
     */
    public boolean isValid() {
        return getActiveProfile().isValid();
    }
    
    /**
     * Creates (or obtains) a manager instance for the default config and a specific
     * profile. Use {@code null} profile to select the default profile. The method
     * returns {@code null} if the requested profile does not exist.
     * 
     * @param profile profile ID or {@code null} for the default profile.
     * @return manager instance.
     */
    public static synchronized OCIProfile forProfile(String profile) {
        return forConfig(null, profile);
    }
    
    
    /**
     * Lists profiles from the given config file. Profiles are listed in the order of appearance,
     * with only a minimal information.
     * 
     * @param configPath path to the config file, or {@code null} for the default config.
     * @return 
     */
    public List<OCIProfile> listProfiles(Path configPath) throws IOException {
        Path p = configPath != null ? configPath : getDefaultConfigPath();
        
        return Files.readAllLines(p).stream().filter(s -> s.startsWith("[")).map(s -> {
            String n = s.substring(1).trim();
            if (n.endsWith("]")) {
                n = n.substring(0, n.length() - 1).trim();
            }
            return n;
        }).map(n -> forConfig(configPath, n)).collect(Collectors.toList());
    }
    
    public static synchronized OCIProfile forConfig(Path configPath, String profile) {
        OCIProfile prof;
        if (configPath == null) {
            configPath = getDefaultConfigPath();
        }
        if (profile == null) {
            profile = OCIProfile.DEFAULT_ID;
        }
        OCIConfig c = new OCIConfig(configPath, profile);
        synchronized (profiles) {
            prof = profiles.get(c);
            if (prof != null && prof.isValid()) {
                return prof;
            }
        }
        OCIProfile newProf = new OCIProfile(getDefaultConfigPath(), c.getProfile());
        synchronized (profiles) {
            OCIProfile check;
            if (prof == null) {
                check = profiles.putIfAbsent(c, newProf);
                return check != null ? check : newProf;
            } else {
                check = profiles.get(c);
                if (check == null || !check.isValid()) {
                    profiles.put(c, newProf);
                }
                return newProf;
            }
        }
    }
    
    private List<OCIProfile> list() {
        return Collections.emptyList();
    }
    
    private static OCIManager INSTANCE = new OCIManager();
    
    private OCIProfile activeProfile;
    
    private static final String KEY_CONFIG_PATH = "configPath";
    private static final String KEY_PROFILE_ID = "profile";
    
    public void setImplicitProfiles(String key, List<OCIProfile> profiles) {
        
        List<OCIProfile> current  = getConnectedProfiles();
        
        boolean changes = false;
        OCIProfile active;
        synchronized (this) {
             current = new ArrayList<>(implicitProfiles.getOrDefault(key, Collections.emptyList()));
             for (Iterator<OCIProfile> it = current.iterator(); it.hasNext(); ) {
                 OCIProfile p = it.next();
                 if (!p.isValid()) {
                     it.remove();
                     changes = true;
                 }
             }
             changes |= !(current.size() == profiles.size() && current.containsAll(profiles));
             if (!changes) {
                 return;
             }
             current.addAll(profiles);
             implicitProfiles.put(key, profiles);
             active = activeProfile;
        }
        listeners.firePropertyChange(PROP_CONNECTED_PROFILES, null, null);
        synchronized (this) {
            if ((active != null || profiles.isEmpty()) && !current.contains(active)) {
                return;
            }
            activeProfile = null;
        }
        listeners.firePropertyChange(PROP_ACTIVE_PROFILE, null, null);
    }
    
    static boolean loadDefaultConfigProfiles() {
        return Boolean.valueOf(NbBundle.getMessage(OCIManager.class, "OCIManager_Autoload_DefaultConfig"));
    }
    
    private Map<String, List<OCIProfile>> initImplicitProfiles() {
        if (!loadDefaultConfigProfiles()) {
            return implicitProfiles;
        }
        Path path = getDefaultConfigPath();
        String s = path.toString();
        synchronized (this) {
            if (implicitProfiles.get(s) != null) {
                return implicitProfiles;
            }
            try {
                implicitProfiles.put(s, listProfiles(null));
            } catch (IOException ex) {
                // TODO: report inability to load profiles.
                Exceptions.printStackTrace(ex);
            }
            FileUtil.addFileChangeListener(defaultProfileListener = new FileChangeAdapter() {
                @Override
                public void fileDeleted(FileEvent fe) {
                    refresh();
                }

                @Override
                public void fileChanged(FileEvent fe) {
                    refresh();
                }

                @Override
                public void fileDataCreated(FileEvent fe) {
                    refresh();
                }
                
                private void refresh() {
                    try {
                        setImplicitProfiles(s, listProfiles(null));
                    } catch (IOException ex) {
                        // TODO: display some lightweight unobtrusive message, or status line item
                        Exceptions.printStackTrace(ex);
                    }
                }
                
            }, path.toFile());
        }
        return implicitProfiles;
    }
    
    public void addConnectedProfile(OCIProfile profile) {
        if (!profile.getTenancy().isPresent()) {
            throw new IllegalArgumentException("Broken profiles are not supported.");
        }
        List<OCIProfile> current = getConnectedProfiles();
        // PENDING: maybe add hashcode + equals to a profile.
        if (current.contains(profile)) {
            return;
        }
        boolean fireActive = current.isEmpty();
        InstanceProperties p = InstancePropertiesManager.getInstance().createProperties("cloud.oracle.com.ociprofiles");
        if (!getDefaultConfigPath().equals(profile.getConfigPath())) {
            p.putString(KEY_CONFIG_PATH, profile.getConfigPath().toString());
        }
        // always store ID, at least something has to be stored for the properties to materialize.
        p.putString(KEY_PROFILE_ID, profile.getId());
        listeners.firePropertyChange(PROP_CONNECTED_PROFILES, null, null);
        synchronized (this) {
            if (activeProfile != null && activeProfile != profile) {
                return;
            }
        }
        listeners.firePropertyChange(PROP_ACTIVE_PROFILE, null, null);
    }
    
    public boolean isConfiguredProfile(OCIProfile profile) {
        return findProfileProperties(profile) != null;
    }
    
    private InstanceProperties findProfileProperties(OCIProfile profile) {
        List<InstanceProperties> props = InstancePropertiesManager.getInstance().getProperties("cloud.oracle.com.ociprofiles");
        for (InstanceProperties p : props) {
            String cfgPath = p.getString(KEY_CONFIG_PATH, null);
            String profName = p.getString(KEY_PROFILE_ID, null);
            if (cfgPath == null) {
                cfgPath = getDefaultConfigPath().toString();
            }
            if (!profile.getConfigPath().toString().equals(cfgPath)) {
                continue;
            }
            if (profName == null) {
                profName = OCIProfile.DEFAULT_ID;
            }
            if (profName.equals(profile.getId())) {
                return p;
            }
        }
        return null;
    }
    
    public void removeConnectedProfile(OCIProfile profile) {
        String profName = profile.getId();
        InstanceProperties p = findProfileProperties(profile);
        if (p == null) {
            return;
        }
        OCIProfile resetToProfile = null;

        synchronized (this) {
            OCIConfig cfg = new OCIConfig(profile.getConfigPath(), OCIProfile.DEFAULT_ID.equals(profile.getId()) ? null : profile.getId());
            if (profiles.remove(cfg) == null) {
                return;
            }
            if (profile == getActiveProfile()) {
                OCIProfile def = forConfig(defaultConfigPath, profName);
                if (profiles.containsValue(def) || profiles.isEmpty()) {
                    resetToProfile = def;
                } else {
                    resetToProfile = profiles.values().iterator().next();
                }
            }
        }
        p.remove();
        if (resetToProfile != null) {
            setActiveProfile(resetToProfile);
        }
        listeners.firePropertyChange("connectedProfiles", null, null);
    }
    
    /**
     * Returns OCI profiles connected to the IDE
     * @return list of OCI profiles.
     */
    public List<OCIProfile> getConnectedProfiles() {
        Set<OCIProfile> toReturn = new LinkedHashSet<>();
        
        Path defConfigPath = getDefaultConfigPath();
        List<InstanceProperties> props = InstancePropertiesManager.getInstance().getProperties("cloud.oracle.com.ociprofiles");
        for (InstanceProperties p : props) {
            String cfgPath = p.getString(KEY_CONFIG_PATH, null);
            String profName = p.getString(KEY_PROFILE_ID, null);
            if (OCIProfile.DEFAULT_ID.equals(profName)) {
                profName = null;
            }
            if (cfgPath != null) {
                try {
                    Path check = Paths.get(cfgPath);
                    if (defConfigPath.equals(check)) {
                        cfgPath = null;
                    }
                } catch (IllegalArgumentException ex) {
                    // TODO: handle malformed fs path
                }
            }
            toReturn.add(forConfig(defConfigPath, profName));
        }
        
        initImplicitProfiles().values().stream().flatMap(l -> l.stream()).forEach(toReturn::add);
        
        return new ArrayList<>(toReturn);
    }
    
    /**
     * Returns the active profile. 
     * @return 
     */
    public OCIProfile getActiveProfile() {
        OCIProfile p = Lookup.getDefault().lookup(OCIProfile.class);
        if (p != null) {
            return p;
        }
        OCIProfile active;
        synchronized (this) {
            active = activeProfile;
        }
        if (activeProfile == null) {
            Preferences prefs = NbPreferences.forModule(OCIManager.class);
            String confPath = prefs.get("activeProfilePath", null);
            String id = null;
            
            Path path = null;
            if (confPath != null) {
                try {
                    path = Paths.get(confPath);
                } catch (IllegalArgumentException ex) {
                    LOG.log(Level.WARNING, "Invalid active OCI profile path: {0}", confPath);
                }
            }
            id = prefs.get("activeProfileId", null);
            p = forConfig(path, id);
            synchronized (this) {
                if (activeProfile == null) {
                    activeProfile = p;
                } else {
                    p = activeProfile;
                }
            }
        }
        return activeProfile;
    }
    
    private String nonDefaultProfilePath(Path p) {
        if (getDefaultConfigPath().equals(p)) {
            return null;
        } else {
            return p.toString();
        }
    }
    
    private String nonDefaultProfileId(String id) {
        return OCIProfile.DEFAULT_ID.equals(id) ? null : id;
    }
    
    public void setActiveProfile(OCIProfile profile) {
        if (profile == null) {
            return;
        }
        OCIProfile oldProfile;
        synchronized (this) {
            if (profile == null) {
                if (activeProfile == null) {
                    return;
                }
                profile = forProfile(null);
            }
            if (profile == activeProfile) {
                return;
            }
            oldProfile = activeProfile;
            this.activeProfile = profile;
            if (listeners == null) {
                return;
            }
        }
        if (oldProfile == null) {
            oldProfile = forProfile(null);
        }
        Preferences prefs = NbPreferences.forModule(OCIManager.class);
        if (getDefaultConfigPath().equals(profile.getConfigPath())) {
            prefs.remove("activeProfilePath");
        } else {
            prefs.put("activeProfilePath", profile.getConfigPath().toString());
        }
        if (OCIProfile.DEFAULT_ID.equals(profile.getId())) {
            prefs.remove("activeProfileId");
        } else {
            prefs.put("activeProfileId", profile.getId());
        }
        try {
            prefs.flush();
        } catch (BackingStoreException ex) {
            // TODO perhaps log only
            Exceptions.printStackTrace(ex);
        }
        listeners.firePropertyChange(PROP_ACTIVE_PROFILE, oldProfile, profile);
    }
    
    public static synchronized OCIManager getDefault() {
        return INSTANCE;
    }

    /**
     * Retrieves information about Tenancy configured in ~/.oci
     *
     * @return Optional {@code OCIItem} describing the Tenancy. If
     * Optional.empty() OCI configuration was not found
     */
    public Optional<TenancyItem> getTenancy() {
        return getActiveProfile().getTenancy();
    }
    
    
    /**
     * Returns a factory that provides a {@link BasicAuthenticationDetailsProvider} that can
     * initialize 
     * @return 
     */
    public OCISessionInitiator getActiveSession() {
        return getActiveProfile();
    }

    /**
     * @return
     * @deprecated use either {@link #getActiveProfile()} or {@link #getActiveSession()}.
     */
    @Deprecated
    public ConfigFileAuthenticationDetailsProvider getConfigProvider() {
        return getActiveProfile().getConfigProvider();
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
        return getActiveProfile().createAutonomousDatabase(compartmentId, dbName, password);
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
        return getActiveProfile().downloadWallet(dbInstance, password, parentPath);
    }
    
    @FunctionalInterface
    public interface OCIOperation<V,E extends Exception> {
        /**
         * Performs the project operation, returning a value. The method may throw one
         * checked exception.
         * 
         * @return the operation's result
         * @throws E on failure.
         */
        public V call() throws E;
    }

    private static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }
    
    /**
     * Allows to execute OCI calls using a specific profile. During the callback, or tasks initiated
     * by the callback, the {@link #getActiveProfile()} will return the profile `p' passed as a parameter.
     * @param <V>
     * @param <E>
     * @param p
     * @param toExecute
     * @return 
     */
    public static <V, E extends Exception> V usingSession(OCISessionInitiator p, OCIOperation<V, E> toExecute) throws E{
        Lookup localDefLookup = new ProxyLookup(Lookups.singleton(p), Lookup.getDefault());
        Object[] res = new Object[1];
        Exception[] t = new Exception[1];
        if (OCIManager.getDefault().getActiveSession() != p) {
            Lookups.executeWith(localDefLookup, () -> {
                try {
                    res[0] = toExecute.call();
                } catch (Error | RuntimeException td) {
                    throw td;
                } catch (Exception ex) {
                    t[0] = ex;
                }
            });
        } else {
            return toExecute.call();
        }
        if (t[0] != null) {
            sneakyThrow(t[0]);
            // never reached
            return null;
        } else {
            return (V)res[0];
        }
    }
}
