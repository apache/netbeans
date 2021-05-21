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

package org.netbeans.modules.payara.common;

import java.io.IOException;
import java.util.*;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.payara.tooling.PayaraStatus;
import org.netbeans.modules.payara.tooling.admin.CommandSetProperty;
import org.netbeans.modules.payara.tooling.server.config.ConfigBuilderProvider;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.payara.common.parser.DomainXMLChangeListener;
import org.netbeans.modules.payara.common.utils.ServerUtils;
import org.netbeans.modules.payara.common.utils.Util;
import org.netbeans.modules.payara.spi.CommandFactory;
import org.netbeans.modules.payara.spi.RegisteredDDCatalog;
import org.netbeans.spi.server.ServerInstanceImplementation;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.payara.spi.PayaraModule;

/**
 * Payara server instances provider.
 * <p/>
 * Handles all registered Payara server instances. Implemented as singleton
 * because NetBeans GUI components require singleton implementing                                                         
 * {@link ServerInstanceProvider} interface.
 * <p/>
 * @author Peter Williams, Vince Kraemer, Tomas Kraus
 */
public final class PayaraInstanceProvider implements ServerInstanceProvider, LookupListener {

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(PayaraInstanceProvider.class);

    public static final String PAYARA_AUTOREGISTERED_INSTANCE = "payara_autoregistered_instance";

    private static final String AUTOINSTANCECOPIED = "autoinstance-copied"; // NOI18N

    private volatile static PayaraInstanceProvider payaraProvider;

    public static final String EE6_DEPLOYER_FRAGMENT = "deployer:pfv3ee6"; // NOI18N
    public static final String EE6WC_DEPLOYER_FRAGMENT = "deployer:pfv3ee6wc"; // NOI18N
    private static final String EE6_INSTANCES_PATH = "/PayaraEE6/Instances"; // NOI18N
    private static final String EE6WC_INSTANCES_PATH = "/PayaraEE6WC/Instances"; // NOI18N

    // Payara Tooling SDK configuration should be done before any server
    // instance is created and used.
    static {
        PayaraSettings.toolingLibraryconfig();
    }

    public static PayaraInstanceProvider getProvider() {
        if (payaraProvider != null) {
            return payaraProvider;
        }
        else {
            boolean runInit = false;
            synchronized(PayaraInstanceProvider.class) {
                if (payaraProvider == null) {
                    runInit = true;
                    payaraProvider = new PayaraInstanceProvider(
                            new String[]{EE6_DEPLOYER_FRAGMENT, EE6WC_DEPLOYER_FRAGMENT},
                            new String[]{EE6_INSTANCES_PATH, EE6WC_INSTANCES_PATH},
                            null,
                            true, 
                            new String[]{"--nopassword"}, // NOI18N
                            (property, value) -> new CommandSetProperty(property, value, "DEFAULT={0}={1}"));
                }
            }
            if (runInit) {
                payaraProvider.init();                
            }
            return payaraProvider;
        }
    }

    public static final Set<String> activeRegistrationSet
            = synchronizedSet(new HashSet<>());
    
    private final Map<String, PayaraInstance> instanceMap
            = synchronizedMap(new HashMap<>());
    
    private static final Set<String> activeDisplayNames
            = synchronizedSet(new HashSet<>());
    
    private final ChangeSupport support = new ChangeSupport(this);

    final private String[] instancesDirNames;
    final private String displayName;
    final private String[] uriFragments;
    final private boolean needsJdk6;
    final private List<String> noPasswordOptions;
    final private CommandFactory cf;
    final private Lookup.Result<RegisteredDDCatalog> lookupResult = Lookups.forPath(Util.PF_LOOKUP_PATH).lookupResult(RegisteredDDCatalog.class);
    
    @SuppressWarnings("LeakingThisInConstructor")
    private PayaraInstanceProvider(
            String[] uriFragments, 
            String[] instancesDirNames,
            String displayName, 
            boolean needsJdk6,
            String[] noPasswordOptionsArray, 
            CommandFactory cf) {
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
            catalog.registerRunTimeDDCatalog(this);
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
        return payaraProvider != null;
    }

    private static RegisteredDDCatalog getDDCatalog() {
        return Lookups.forPath(Util.PF_LOOKUP_PATH).lookup(RegisteredDDCatalog.class);
    }

    private static void refreshCatalogFromFirstInstance(PayaraInstanceProvider pip, RegisteredDDCatalog catalog) {
        PayaraInstance firstInstance = pip.getFirstServerInstance();
        if (null != firstInstance) {
            catalog.refreshRunTimeDDCatalog(pip, firstInstance.getPayaraRoot());
        }
    }

    /**
     * Get API representation of Payara server instance matching
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
     * Get {@link PayaraInstance} matching provided internal
     * server URI.
     * <p/>
     * @param uri Internal server URI used as key to find
     *            {@link PayaraInstance}.
     * @return {@link PayaraInstance} matching provided internal server URI
     *         or <code>null</code> when no matching object was found.
     */
    public static PayaraInstance getPayaraInstanceByUri(String uri) {
        return getProvider().getPayaraInstance(uri);
    }

    private PayaraInstance getFirstServerInstance() {
        if (!instanceMap.isEmpty()) {
            return instanceMap.values().iterator().next();
        }
        return null;
    }

    /**
     * Retrieve {@link PayaraInstance} matching provided
     * internal server URI.
     * <p/>
     * @param uri Internal server URI used as key to find
     *            {@link PayaraInstance}.
     * @return {@link PayaraInstance} matching provided internal server URI
     *         or <code>null</code> when no matching object was found.
     */
    public PayaraInstance getPayaraInstance(String uri) {
        synchronized(instanceMap) {
            return instanceMap.get(uri);
        }
    }

    /**
     * Add Payara server instance into this provider.
     * <p/>
     * @param si Payara server instance to be added.
     */
    public void addServerInstance(PayaraInstance si) {
        synchronized(instanceMap) {
            try {
                instanceMap.put(si.getDeployerUri(), si);
                activeDisplayNames.add(si.getDisplayName());
                if (instanceMap.size() == 1) { // only need to do if this first of this type
                    RegisteredDDCatalog catalog = getDDCatalog();
                    if (null != catalog) {
                        catalog.refreshRunTimeDDCatalog(this, si.getPayaraRoot());
                    }
                }
                PayaraInstance.writeInstanceToFile(si);
            } catch(IOException ex) {
                LOGGER.log(Level.INFO,
                        "Could not store Payara server attributes", ex);
            }
        }
        if (!si.isRemote()) {
            DomainXMLChangeListener.registerListener(si);
        }
        support.fireChange();
    }

    /**
     * Remove Payara server instance from this provider.
     * <p/>
     * @param si Payara server instance to be removed.
     */
    public boolean removeServerInstance(PayaraInstance si) {
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
        PayaraStatus.remove(si);
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
            for (PayaraInstance gfInstance : instanceMap.values()) {
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
        PayaraInstance instance = instanceMap.get(uri);
        if(instance != null) {
            result = instance.getLookup().lookup(serverFacadeClass);
        }
        return result;
    }
    
    public <T> List<T> getInstancesByCapability(Class<T> serverFacadeClass) {
        List<T> result = new ArrayList<>();
        synchronized (instanceMap) {
            for (PayaraInstance instance : instanceMap.values()) {
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
            for (PayaraInstance instance : instanceMap.values()) {
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
        PayaraInstance instance = instanceMap.get(uri);
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
    Collection<PayaraInstance> getInternalInstances() {
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
                    catalog.registerRunTimeDDCatalog(this);
                refreshCatalogFromFirstInstance(this, catalog);
            }
        }
        for (PayaraInstance gi : instanceMap.values()) {
            PayaraInstance.updateModuleSupport(gi);
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
                            if (instanceFOs[i].getName().startsWith(PAYARA_AUTOREGISTERED_INSTANCE)) {
                                installedInstances.add(instanceFOs[i]);
                                continue;
                            }
                            PayaraInstance si = PayaraInstance
                                    .readInstanceFromFile(instanceFOs[i], false);
                            if (si != null) {
                                activeDisplayNames.add(si.getDisplayName());
                            } else {
                                LOGGER.log(Level.FINER,
                                        "Unable to create payara instance for {0}", // NOI18N
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
                    PayaraInstance igi = PayaraInstance.
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
                    String val = ServerUtils.getStringAttribute(installedServers[i], PayaraModule.URL_ATTR);
                    if(val != null && val.equals(url) &&
                            !installedServers[i].getName().startsWith(PAYARA_AUTOREGISTERED_INSTANCE)) {
                        return installedServers[i];
                    }
                }
            }
        }
        return null;
    }

    List<String> getNoPasswordOptions() {
        return noPasswordOptions;
    }

    public CommandFactory getCommandFactory() {
       return cf;
    }

}
