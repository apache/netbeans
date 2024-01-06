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

package org.netbeans.modules.glassfish.common;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.tooling.GlassFishStatus;
import org.netbeans.modules.glassfish.tooling.admin.CommandSetProperty;
import org.netbeans.modules.glassfish.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.common.parser.DomainXMLChangeListener;
import org.netbeans.modules.glassfish.common.utils.ServerUtils;
import org.netbeans.modules.glassfish.common.utils.Util;
import org.netbeans.modules.glassfish.spi.CommandFactory;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.RegisteredDDCatalog;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;

/**
 * GlassFish server instances provider.
 * <p/>
 * Handles all registered GlassFish server instances. Implemented as singleton
 * because NetBeans GUI components require singleton implementing
 * {@link ServerInstanceProvider} interface.
 * <p/>
 * @author Peter Williams, Vince Kraemer, Tomas Kraus
 */
public final class GlassfishInstanceProvider implements ServerInstanceProvider, LookupListener {

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(GlassfishInstanceProvider.class);

    public static final String GLASSFISH_AUTOREGISTERED_INSTANCE = "glassfish_autoregistered_instance";

    private static final String AUTOINSTANCECOPIED = "autoinstance-copied"; // NOI18N

    private static volatile GlassfishInstanceProvider glassFishProvider;

    public static final String EE6_DEPLOYER_FRAGMENT = "deployer:gfv3ee6"; // NOI18N
    public static final String EE7_DEPLOYER_FRAGMENT = "deployer:gfv4ee7"; // NOI18N
    public static final String EE8_DEPLOYER_FRAGMENT = "deployer:gfv5ee8"; // NOI18N
    public static final String JAKARTAEE8_DEPLOYER_FRAGMENT = "deployer:gfv510ee8";
    public static final String JAKARTAEE9_DEPLOYER_FRAGMENT = "deployer:gfv6ee9";
    public static final String JAKARTAEE91_DEPLOYER_FRAGMENT = "deployer:gfv610ee9";
    public static final String JAKARTAEE10_DEPLOYER_FRAGMENT = "deployer:gfv700ee10";
    public static final String JAKARTAEE11_DEPLOYER_FRAGMENT = "deployer:gfv800ee11";
    public static final String EE6WC_DEPLOYER_FRAGMENT = "deployer:gfv3ee6wc"; // NOI18N
    public static final String PRELUDE_DEPLOYER_FRAGMENT = "deployer:gfv3"; // NOI18N
    private static String EE6_INSTANCES_PATH = "/GlassFishEE6/Instances"; // NOI18N
    private static String EE7_INSTANCES_PATH = "/GlassFishEE7/Instances"; // NOI18N
    private static String EE8_INSTANCES_PATH = "/GlassFishEE8/Instances"; // NOI18N
    private static String JAKARTAEE8_INSTANCES_PATH = "/GlassFishJakartaEE8/Instances"; // NOI18N
    private static String JAKARTAEE9_INSTANCES_PATH = "/GlassFishJakartaEE9/Instances"; // NOI18N
    private static String JAKARTAEE91_INSTANCES_PATH = "/GlassFishJakartaEE91/Instances"; // NOI18N
    private static String JAKARTAEE10_INSTANCES_PATH = "/GlassFishJakartaEE10/Instances"; // NOI18N
    private static String JAKARTAEE11_INSTANCES_PATH = "/GlassFishJakartaEE11/Instances"; // NOI18N
    private static String EE6WC_INSTANCES_PATH = "/GlassFishEE6WC/Instances"; // NOI18N

    public static String PRELUDE_DEFAULT_NAME = "GlassFish_v3_Prelude"; //NOI18N
    public static String EE6WC_DEFAULT_NAME = "GlassFish_Server_3.1"; // NOI18N

    // GlassFish Tooling SDK configuration should be done before any server
    // instance is created and used.
    static {
        GlassFishSettings.toolingLibraryconfig();
    }

    public static GlassfishInstanceProvider getProvider() {
        if (glassFishProvider != null) {
            return glassFishProvider;
        }
        else {
            boolean runInit = false;
            synchronized(GlassfishInstanceProvider.class) {
                if (glassFishProvider == null) {
                    runInit = true;
                    glassFishProvider = new GlassfishInstanceProvider(
                            new String[]{EE6_DEPLOYER_FRAGMENT, EE6WC_DEPLOYER_FRAGMENT, 
                                    EE7_DEPLOYER_FRAGMENT, EE8_DEPLOYER_FRAGMENT, 
                                    JAKARTAEE8_DEPLOYER_FRAGMENT, JAKARTAEE9_DEPLOYER_FRAGMENT,
                                    JAKARTAEE91_DEPLOYER_FRAGMENT, JAKARTAEE10_DEPLOYER_FRAGMENT,
                                    JAKARTAEE11_DEPLOYER_FRAGMENT},
                            new String[]{EE6_INSTANCES_PATH, EE6WC_INSTANCES_PATH, 
                                    EE7_INSTANCES_PATH, EE8_INSTANCES_PATH, 
                                    JAKARTAEE8_INSTANCES_PATH, JAKARTAEE9_INSTANCES_PATH,
                                    JAKARTAEE91_INSTANCES_PATH, JAKARTAEE10_INSTANCES_PATH,
                                    JAKARTAEE11_INSTANCES_PATH},
                            null,
                            true,
                            new String[]{"--nopassword"}, // NOI18N
                            new CommandFactory()  {

                        @Override
                        public CommandSetProperty getSetPropertyCommand(
                                String property, String value) {
                            return new CommandSetProperty(property,
                                    value, "DEFAULT={0}={1}");
                        }

                    });
                }
            }
            if (runInit) {
                glassFishProvider.init();
            }
            return glassFishProvider;
        }
    }

    public static final Set<String> activeRegistrationSet = Collections.synchronizedSet(new HashSet<String>());

    private final Map<String, GlassfishInstance> instanceMap =
            Collections.synchronizedMap(new HashMap<String, GlassfishInstance>());
    private static final Set<String> activeDisplayNames = Collections.synchronizedSet(new HashSet<String>());
    private final ChangeSupport support = new ChangeSupport(this);

    private final String[] instancesDirNames;
    private final String displayName;
    private final String[] uriFragments;
    private final boolean needsJdk6;
    private final List<String> noPasswordOptions;
    private final CommandFactory cf;
    private final Lookup.Result<RegisteredDDCatalog> lookupResult = Lookups.forPath(Util.GF_LOOKUP_PATH).lookupResult(RegisteredDDCatalog.class);

    @SuppressWarnings("LeakingThisInConstructor")
    private GlassfishInstanceProvider(
            String[] uriFragments,
            String[] instancesDirNames,
            String displayName,
            boolean needsJdk6,
            String[] noPasswordOptionsArray,
            CommandFactory cf
            ) {
        this.instancesDirNames = instancesDirNames;
        this.displayName = displayName;
        this.uriFragments = uriFragments;
        this.needsJdk6 = needsJdk6;
        this.noPasswordOptions = new ArrayList<>();
        if (null != noPasswordOptionsArray) {
            noPasswordOptions.addAll(Arrays.asList(noPasswordOptionsArray));
        }
        this.cf = cf;
        lookupResult.allInstances();

        lookupResult.addLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        LOGGER.log(Level.FINE, "***** resultChanged fired ********  {0}", hashCode()); // NOI18N
        RegisteredDDCatalog catalog = getDDCatalog();
        if (null != catalog) {
            catalog.registerEE6RunTimeDDCatalog(this);
        }
        refreshCatalogFromFirstInstance(this, getDDCatalog());
    }

    /**
     * Check providers initialization status.
     * <p>
     * @return <code>true</code> when at least one of the providers
     *         is initialized or <code>false</code> otherwise.
     */
    public static synchronized boolean initialized() {
        return glassFishProvider != null;
    }

    private static RegisteredDDCatalog getDDCatalog() {
        return Lookups.forPath(Util.GF_LOOKUP_PATH).lookup(RegisteredDDCatalog.class);
    }

    private static void refreshCatalogFromFirstInstance(GlassfishInstanceProvider gip, RegisteredDDCatalog catalog) {
        GlassfishInstance firstInstance = gip.getFirstServerInstance();
        if (null != firstInstance) {
            catalog.refreshRunTimeDDCatalog(gip, firstInstance.getGlassfishRoot());
        }
    }

    /**
     * Get API representation of GlassFish server instance matching
     * provided internal server URI.
     * <p/>
     * @param uri Internal server URI used as key to find
     *            {@link ServerInstance}.
     * @return {@link ServerInstance} matching given URI.
     */
    public static ServerInstance getInstanceByUri(String uri) {
        return getProvider().getInstance(uri);
    }

    /**
     * Get {@link GlassfishInstance} matching provided internal
     * server URI.
     * <p/>
     * @param uri Internal server URI used as key to find
     *            {@link GlassfishInstance}.
     * @return {@link GlassfishInstance} matching provided internal server URI
     *         or <code>null</code> when no matching object was found.
     */
    public static GlassfishInstance getGlassFishInstanceByUri(String uri) {
        return getProvider().getGlassfishInstance(uri);
    }

    private GlassfishInstance getFirstServerInstance() {
        if (!instanceMap.isEmpty()) {
            return instanceMap.values().iterator().next();
        }
        return null;
    }

    /**
     * Retrieve {@link GlassfishInstance} matching provided
     * internal server URI.
     * <p/>
     * @param uri Internal server URI used as key to find
     *            {@link GlassfishInstance}.
     * @return {@link GlassfishInstance} matching provided internal server URI
     *         or <code>null</code> when no matching object was found.
     */
    public GlassfishInstance getGlassfishInstance(String uri) {
        synchronized(instanceMap) {
            return instanceMap.get(uri);
        }
    }

    /**
     * Add GlassFish server instance into this provider.
     * <p/>
     * @param si GlassFish server instance to be added.
     */
    public void addServerInstance(GlassfishInstance si) {
        synchronized(instanceMap) {
            try {
                instanceMap.put(si.getDeployerUri(), si);
                activeDisplayNames.add(si.getDisplayName());
                if (instanceMap.size() == 1) { // only need to do if this first of this type
                    RegisteredDDCatalog catalog = getDDCatalog();
                    if (null != catalog) {
                        catalog.refreshRunTimeDDCatalog(this, si.getGlassfishRoot());
                    }
                }
                GlassfishInstance.writeInstanceToFile(si);
            } catch(IOException ex) {
                LOGGER.log(Level.INFO,
                        "Could not store GlassFish server attributes", ex);
            }
        }
        if (!si.isRemote()) {
            DomainXMLChangeListener.registerListener(si);
        }
        support.fireChange();
    }

    /**
     * Remove GlassFish server instance from this provider.
     * <p/>
     * @param si GlassFish server instance to be removed.
     */
    public boolean removeServerInstance(GlassfishInstance si) {
        boolean result = false;
        synchronized(instanceMap) {
            if(instanceMap.remove(si.getDeployerUri()) != null) {
                result = true;
                removeInstanceFromFile(si.getDeployerUri());
                activeDisplayNames.remove(si.getDisplayName());
                // If this was the last of its type, need to remove the
                // resolver catalog contents
                if (instanceMap.isEmpty()) {
                    RegisteredDDCatalog catalog = getDDCatalog();
                    if (null != catalog) {
                        catalog.refreshRunTimeDDCatalog(this, null);
                    }
                }
            }
        }
        GlassFishStatus.remove(si);
        if (result) {
            ConfigBuilderProvider.destroyBuilder(si);
            if (!si.isRemote()) {
                DomainXMLChangeListener.unregisterListener(si);
            }
            support.fireChange();
        }
        return result;
    }

    public Lookup getLookupFor(ServerInstance instance) {
        synchronized (instanceMap) {
            for (GlassfishInstance gfInstance : instanceMap.values()) {
                if (gfInstance.getCommonInstance().equals(instance)) {
                    return gfInstance.getLookup();
                }
            }
            return null;
        }
    }

    public ServerInstanceImplementation getInternalInstance(String uri) {
        return instanceMap.get(uri);
    }

    public <T> T getInstanceByCapability(String uri, Class <T> serverFacadeClass) {
        T result = null;
        GlassfishInstance instance = instanceMap.get(uri);
        if(instance != null) {
            result = instance.getLookup().lookup(serverFacadeClass);
        }
        return result;
    }

    public <T> List<T> getInstancesByCapability(Class<T> serverFacadeClass) {
        List<T> result = new ArrayList<>();
        synchronized (instanceMap) {
            for (GlassfishInstance instance : instanceMap.values()) {
                T serverFacade = instance.getLookup().lookup(serverFacadeClass);
                if(serverFacade != null) {
                    result.add(serverFacade);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // ServerInstanceProvider interface implementation
    // ------------------------------------------------------------------------
    @Override
    public List<ServerInstance> getInstances() {
        List<ServerInstance> result = new  ArrayList<>();
        synchronized (instanceMap) {
            for (GlassfishInstance instance : instanceMap.values()) {
                ServerInstance si = instance.getCommonInstance();
                if (null != si) {
                    result.add(si);
                } else {
                    String message = "invalid commonInstance for " + instance.getDeployerUri(); // NOI18N
                    LOGGER.log(Level.WARNING, message);   // NOI18N
                    if (null != instance.getDeployerUri())
                        instanceMap.remove(instance.getDeployerUri());
                }
            }
        }
        return result;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    // Additional interesting API's
    public boolean hasServer(String uri) {
        return getInstance(uri) != null;
    }

    public ServerInstance getInstance(String uri) {
        ServerInstance rv = null;
        GlassfishInstance instance = instanceMap.get(uri);
        if (null != instance) {
            rv = instance.getCommonInstance();
            if (null == rv) {
                String message = "invalid commonInstance for " + instance.getDeployerUri(); // NOI18N
                LOGGER.log(Level.WARNING, message);
                if (null != instance.getDeployerUri())
                    instanceMap.remove(instance.getDeployerUri());
            }
        }
        return rv;
    }

    String getInstancesDirFirstName() {
        return instancesDirNames[0];
    }

    // ------------------------------------------------------------------------
    // Internal use only.  Used by Installer.close() to quickly identify and
    // shutdown any instances we started during this IDE session.
    // ------------------------------------------------------------------------
    Collection<GlassfishInstance> getInternalInstances() {
        return instanceMap.values();
    }

    boolean requiresJdk6OrHigher() {
        return needsJdk6;
    }

    private void init() {
        synchronized (instanceMap) {
            try {
                loadServerInstances();
            } catch (RuntimeException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
            RegisteredDDCatalog catalog = getDDCatalog();
            if (null != catalog) {
                    catalog.registerEE6RunTimeDDCatalog(this);
                refreshCatalogFromFirstInstance(this, catalog);
            }
        }
        for (GlassfishInstance gi : instanceMap.values()) {
            GlassfishInstance.updateModuleSupport(gi);
        }
    }

    // ------------------------------------------------------------------------
    // Persistence for server instances.
    // ------------------------------------------------------------------------
    private void loadServerInstances() {
        List<FileObject> installedInstances = new LinkedList<>();
        for (int j = 0; j < instancesDirNames.length; j++) {
            FileObject dir
                    = ServerUtils.getRepositoryDir(instancesDirNames[j], false);
            if (dir != null) {
                FileObject[] instanceFOs = dir.getChildren();
                if (instanceFOs != null && instanceFOs.length > 0) {
                    for (int i = 0; i < instanceFOs.length; i++) {
                        try {
                            if (instanceFOs[i].getName().startsWith(GLASSFISH_AUTOREGISTERED_INSTANCE)) {
                                installedInstances.add(instanceFOs[i]);
                                continue;
                            }
                            GlassfishInstance si = GlassfishInstance
                                    .readInstanceFromFile(instanceFOs[i], false);
                            if (si != null) {
                                activeDisplayNames.add(si.getDisplayName());
                            } else {
                                LOGGER.log(Level.FINER,
                                        "Unable to create glassfish instance for {0}", // NOI18N
                                        instanceFOs[i].getPath());
                            }
                        } catch (IOException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                        }
                    }
                }
            }
        }
        if (!installedInstances.isEmpty()
                && null == NbPreferences.forModule(this.getClass())
                .get(AUTOINSTANCECOPIED, null)) {
            try {
                for (FileObject installedInstance : installedInstances) {
                    GlassfishInstance igi = GlassfishInstance.
                            readInstanceFromFile(installedInstance, true);
                    activeDisplayNames.add(igi.getDisplayName());
                }
                try {
                    NbPreferences.forModule(this.getClass())
                            .put(AUTOINSTANCECOPIED, "true"); // NOI18N
                    NbPreferences.forModule(this.getClass()).flush();
                } catch (BackingStoreException ex) {
                    LOGGER.log(Level.INFO,
                            "auto-registered instance may reappear", ex); // NOI18N
                }
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

    private void removeInstanceFromFile(String url) {
        FileObject instanceFO = getInstanceFileObject(url);
        if(instanceFO != null && instanceFO.isValid()) {
            try {
                instanceFO.delete();
            } catch(IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

    private FileObject getInstanceFileObject(String url) {
        for (String instancesDirName : instancesDirNames) {
            FileObject dir = ServerUtils.getRepositoryDir(
                    instancesDirName, false);
            if(dir != null) {
                FileObject[] installedServers = dir.getChildren();
                for(int i = 0; i < installedServers.length; i++) {
                    String val = ServerUtils.getStringAttribute(
                            installedServers[i], GlassfishModule.URL_ATTR);
                    if(val != null && val.equals(url) &&
                            !installedServers[i].getName().startsWith(GLASSFISH_AUTOREGISTERED_INSTANCE)) {
                        return installedServers[i];
                    }
                }
            }
        }
        return null;
    }

    String[] getNoPasswordCreatDomainCommand(String startScript, String jarLocation,
            String domainDir, String portBase, String uname, String domain) {
            List<String> retVal = new ArrayList<>();
        retVal.addAll(Arrays.asList(new String[] {startScript,
                    "-client",  // NOI18N
                    "-jar",  // NOI18N
                    jarLocation,
                    "create-domain", //NOI18N
                    "--user", //NOI18N
                    uname,
                    "--domaindir", //NOI18N
                    domainDir}));
        if (null != portBase) {
            retVal.add("--portbase"); //NOI18N
            retVal.add(portBase);
        }
        if (noPasswordOptions.size() > 0) {
            retVal.addAll(noPasswordOptions);
        }
        retVal.add(domain);
        return retVal.toArray(String[]::new);
    }

    public CommandFactory getCommandFactory() {
       return cf;
    }

}
