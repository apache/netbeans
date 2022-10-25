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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.common.api.ValidationException;
import org.netbeans.modules.j2ee.deployment.config.*;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.j2ee.deployment.impl.DefaultSourceMap;
import org.netbeans.modules.j2ee.deployment.impl.Server;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerTarget;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.modules.j2ee.deployment.plugins.spi.VerifierSupport;
import org.openide.filesystems.FileObject;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.impl.projects.J2eeModuleProviderAccessor;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;

/** This object must be implemented by J2EE module support and an instance 
 * added into project lookup.
 * 
 * @author  Pavel Buzek
 */
public abstract class J2eeModuleProvider {
    
    private static final Logger LOGGER = Logger.getLogger(J2eeModuleProvider.class.getName());

    static {
        J2eeModuleProviderAccessor.setDefault(new J2eeModuleProviderAccessor() {

            @Override
            public ConfigSupportImpl getConfigSupportImpl(J2eeModuleProvider impl) {
                return impl.getConfigSupportImpl();
            }
        });
    }

    private final Object configSupportImplLock = new Object();

    /* GuardedBy(configSupportImplLock) */
    private ConfigSupportImpl configSupportImpl;
    private final CopyOnWriteArrayList<ConfigurationFilesListener> listeners = new CopyOnWriteArrayList<ConfigurationFilesListener>();
    private ConfigFilesListener configFilesListener = null;
    
    public J2eeModuleProvider () {
        super();
    }
    
    public abstract J2eeModule getJ2eeModule ();
    
    public abstract ModuleChangeReporter getModuleChangeReporter ();

    /**
     * Return the class reporting any possible change in resources intended to
     * be deployed on server.
     *
     * @return class reporting changes in server resources
     * @since 1.63
     */
    @CheckForNull
    public ResourceChangeReporter getResourceChangeReporter() {
        return null;
    }
    
    public final ConfigSupport getConfigSupport () {
        ConfigSupportImpl confSupp;
        synchronized (configSupportImplLock) {
            confSupp = configSupportImpl;
        }
        if (confSupp == null) {
            confSupp = new ConfigSupportImpl(this);
            synchronized (configSupportImplLock) {
                configSupportImpl = confSupp;
            }
        }
	return confSupp;
    }
    
    // Do not remove this method! It is a helper for the Maven support project to 
    // workaround the issue #109507. Please keep in mind that this is a hack, so
    // keep it private! No one else should use it.
    private void resetConfigSupport() {
        synchronized (configSupportImplLock) {
            configSupportImpl = null;
        }
    }
    
    /**
     * Return server debug info.
     * Note: if server is not running and needs to be up for retrieving debug info, 
     * this call will return null.  This call is also used by UI so it should not 
     * try to ping or start the server.
     */
    public final ServerDebugInfo getServerDebugInfo () {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID());
        if (si == null) {
            return null;
        }
        StartServer ss = si.getStartServer();
        if (ss == null) {
            return null;
        }
        // AS8.1 needs to have server running to get accurate debug info, and also need a non-null target 
        // But getting targets from AS8.1 require start server which would hang UI, so avoid start server
        // Note: for debug info after deploy, server should already start.
        if (!si.isRunningLastCheck() && ss.needsStartForTargetList()) {
            if (ss.isAlsoTargetServer(null)) {
                return ss.getDebugInfo(null);
            } else {
                return null;
            }
        }

        Target target = null;
        ServerTarget[] sts = si.getTargets();
        for (int i = 0; i < sts.length; i++) {
            if (si.getStartServer().isAlsoTargetServer(sts[i].getTarget())) {
                target = sts[i].getTarget();
            }
        }
        if (target == null && sts.length > 0) {
            target = sts[0].getTarget();
        }
        return si.getStartServer().getDebugInfo(target);
    }

    /**
     * Gets the data sources deployed on the target server instance.
     *
     * @return set of data sources
     * 
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     * 
     * @since 1.15 
     */
    public Set<Datasource> getServerDatasources() throws ConfigurationException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        Set<Datasource> deployedDS = Collections.<Datasource>emptySet();
        if (si != null) {
            deployedDS = si.getDatasources();
        }
        else {
            Logger.getLogger("global").log(Level.WARNING, "The server data sources cannot be retrieved because the server instance cannot be found.");
        }
        return deployedDS;
    }
    
    /**
     * Gets the data sources saved in the module.
     *
     * @return set of data sources
     * 
     * @throws ConfigurationException reports problems in retrieving data source
     *         definitions.
     * @since 1.15
     * @deprecated use {@link ConfigSupport#getDatasources()}
     *             on {@link #getConfigSupport()} result
     */
    @Deprecated
    public Set<Datasource> getModuleDatasources() throws ConfigurationException {
        Set<Datasource> projectDS = getConfigSupport().getDatasources();
        return projectDS;
    }

    /**
     * Tests whether data source creation is supported.
     *
     * @return true if data source creation is supported, false otherwise.
     *
     * @since 1.15 
     */
    public boolean isDatasourceCreationSupported() {
        return getConfigSupport().isDatasourceCreationSupported();
    }
    
    
    /**
     * Creates and saves data source in the module if it does not exist yet on the target server or in the module.
     * Data source is considered to be existing when JNDI name of the found data source and the one
     * just created equal.
     *
     * @param jndiName name of data source
     * @param url database URL
     * @param username database user
     * @param password user's password
     * @param driver fully qualified name of database driver class
     * @return created data source
     * @exception DatasourceAlreadyExistsException if conflicting data source is found
     *
     * @since 1.15 
     */
    public final Datasource createDatasource(String jndiName, String  url, String username, String password, String driver) 
    throws DatasourceAlreadyExistsException, ConfigurationException {

        //check whether the ds is not already on the server
        Set<Datasource> deployedDS = getServerDatasources();
        if (deployedDS != null) {
            for (Iterator<Datasource> it = deployedDS.iterator(); it.hasNext();) {
                Datasource ds = (Datasource) it.next();
                if (jndiName.equals(ds.getJndiName())) // ds with the same JNDI name already exists on the server, do not create new one
                    throw new DatasourceAlreadyExistsException(ds);
            }
        }
        
        Datasource ds = null;
        try {
            //btw, ds existence in a project is verified directly in the deployment configuration
            ds = getConfigSupport().createDatasource(jndiName, url, username, password, driver);
        } catch (UnsupportedOperationException oue) {
            Logger.getLogger("global").log(Level.INFO, null, oue);
        }
        
        return ds;
    }
    
    /**
     * Deploys data sources saved in the module.
     *
     * @exception ConfigurationException if there is some problem with data source configuration
     * @exception DatasourceAlreadyExistsException if module data source(s) are conflicting
     * with data source(s) already deployed on the server
     *
     * @since 1.15
     * @deprecated Nobody should use this method. Being an API is a mistake.
     */
    @Deprecated
    public void deployDatasources() throws ConfigurationException, DatasourceAlreadyExistsException {
        ServerInstance si = ServerRegistry.getInstance ().getServerInstance (getServerInstanceID ());
        if (si != null) {
            Set<Datasource> moduleDS = getModuleDatasources();
            si.deployDatasources(moduleDS);
        }
        else {
            Logger.getLogger("global").log(Level.WARNING,
                                           "The data sources cannot be deployed because the server instance cannot be found.");
        }
    }
    
    
    /**
     * Configuration support to allow development module code to access well-known 
     * configuration propeties, such as web context root, cmp mapping info...
     * The setters and getters work with server specific data on the server returned by
     * {@link #getServerID} method.
     */
    // FIXME replace this with final class - this is not deigned to be implemented by anybody
    public static interface ConfigSupport {
        /**
         * Create an initial fresh configuration for the current module.  Do nothing if configuration already exists.
         * @return true if there is no existing configuration, false if there is exsisting configuration.
         */
        public boolean createInitialConfiguration();
        /**
         * Ensure configuration is ready to respond to any editing to the module.
         * @return true if the configuration is ready, else false.
         */
        public boolean ensureConfigurationReady();

        /**
         * Set web module context root.
         * 
         * @param contextRoot web module context root. 
         * @throws ConfigurationException reports errors in setting the web context
         *         root.
         */
        public void setWebContextRoot(String contextRoot) throws ConfigurationException;
        
        /**
         * Get web module context root.
         * 
         * @return web module context root.
         * 
         * @throws ConfigurationException reports errors in setting the web context
         *         root.
         */
        public String getWebContextRoot() throws ConfigurationException;
        
        /**
         * Return a list of file names for current server specific deployment 
         * descriptor used in this module.
         */
        public String [] getDeploymentConfigurationFileNames();
        /**
         * Return relative path within the archive or distribution content for the
         * given server specific deployment descriptor file.
         * @param deploymentConfigurationFileName server specific descriptor file name
         * @return relative path inside distribution content.
         */
        public String getContentRelativePath(String deploymentConfigurationFileName);
        /**
         * Push the CMP and CMR mapping info to the server configuraion.
         * This call is typically used by CMP mapping wizard.
         * 
         * @throws ConfigurationException reports errors in setting the CMP mapping.
         */
        public void setCMPMappingInfo(OriginalCMPMapping[] mappings) throws ConfigurationException;
        
        /**
         * Sets the resource for the specified CMP bean. Some containers may not 
         * support fine-grained per bean resource definition, in which case global 
         * EJB module CMP resource is set.
         *
         * @param ejbName   name of the CMP bean.
         * @param jndiName  the JNDI name of the resource.
         * 
         * @throws ConfigurationException reports errors in setting the CMP resource.
         * @throws NullPointerException if any of the parameters is <code>null</code>.
         * 
         * @since 1.30
         */
        void setCMPResource(String ejbName, String jndiName) throws ConfigurationException;
        
        /**
         * Tests whether data source creation is supported.
         *
         * @return true if data source creation is supported, false otherwise.
         *
         * @since 1.15 
         */
        public boolean isDatasourceCreationSupported();
                
        /**
         * Gets the data sources saved in the module.
         *
         * @return set of data sources
         *
         * @throws ConfigurationException reports errors in retrieving the data sources.
         * 
         * @since 1.15 
         * 
         */
        public Set<Datasource> getDatasources() throws ConfigurationException;
        
        /**
         * Creates and saves data source in the module if it does not exist yet in the module.
         * Data source is considered to be existing when JNDI name of the found data source and the one
         * just created equal.
         *
         * @param jndiName name of data source
         * @param url database URL
         * @param username database user
         * @param password user's password
         * @param driver fully qualified name of database driver class
         * 
         * @return created data source
         * 
         * @throws UnsupportedOperationException if operation is not supported
         * @throws DatasourceAlreadyExistsException if conflicting data source is found
         * @throws ConfigurationException reports errors in creating the data source.
         *
         * @since 1.15 
         */
        public Datasource createDatasource(String jndiName, String  url, String username, String password, String driver)
        throws UnsupportedOperationException, DatasourceAlreadyExistsException, ConfigurationException;
        
        /**
         * Binds the data source reference name with the corresponding data source which is
         * identified by the given JNDI name.
         * 
         * @param referenceName name used to identify the data source
         * @param jndiName JNDI name of the data source
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException;

        /**
         * Binds the data source reference name with the corresponding data source which is
         * identified by the given JNDI name. The reference is used within the scope of the EJB.
         * 
         * @param ejbName EJB name
         * @param ejbType EJB type - the possible values are 
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
         * @param referenceName name used to identify the data source
         * @param jndiName JNDI name of the data source

         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
         * 
         * @since 1.25
         */
        public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, 
                String referenceName, String jndiName) throws ConfigurationException;
        
        /**
         * Finds JNDI name of data source which is mapped to the given reference name of a data source
         * 
         * @param referenceName reference name of data source
         * @return JNDI name which is mapped to the given JNDI name
         * 
         * @throws NullPointerException if reference name is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public String findDatasourceJndiName(String referenceName) throws ConfigurationException;
        
        /**
         * Finds JNDI name of data source which is mapped to the given reference name in the scope of the EJB.
         * 
         * @param ejbName EJB name
         * @param referenceName reference name of data source
         * @return data source if it exists, null otherwise
         *
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException;
        
        /**
         * Finds data source with the given JNDI name.
         * 
         * @param jndiName JNDI name of a data source
         * @return data source if it exists, null otherwise
         *
         * @throws NullPointerException if JNDI name is null
         * @throws ConfigurationException if there is some problem with data source configuration
         * 
         * @since 1.25
         */
        public Datasource findDatasource(String jndiName) throws ConfigurationException;

        /**
         * Configure the library (dependency) the enterprise module needs in order
         * to work properly.
         * <p>
         * Once library is configured it should be present in the result
         * of the {@link #getRequiredLibraries()} call.
         *
         * @param library the library the enterprise module needs in order to work
         *             properly
         * @throws ConfigurationException if there was a problem writing
         *             configuration
         * @since 1.68
         */
        public void configureLibrary(@NonNull ServerLibraryDependency library) throws ConfigurationException;

        /**
         * Returns the server library dependencies the enterprise module needs
         * to work properly.
         *
         * @return the server library dependencies
         * @throws ConfigurationException if there was a problem reading
         *             configuration
         * @since 1.68
         */
        @NonNull
        public Set<ServerLibraryDependency> getLibraries() throws ConfigurationException;

        public void addLibraryChangeListener(@NonNull ChangeListener listener);

        public void removeLibraryChangeListener(@NonNull ChangeListener listener);

        public boolean isDescriptorRequired();
        
        /**
         * Adds a listener which is notified whenever there is deploy on save
         * performed for the provider associated with this support.
         * 
         * @param listener listener to add
         * @since 1.97
         */
        public void addDeployOnSaveListener( DeployOnSaveListener listener );

        /**
         * Removes the listener listening for deploy on save.
         * 
         * @param listener listener to remove
         * @see #addDeployOnSaveListener(DeployOnSaveListener)
         * @since 1.97
         */
        public void removeDeployOnSaveListener( DeployOnSaveListener listener );

        /**
         * Retrieves message destinations stored in the module.
         * 
         * @return set of message destinations
         * 
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public Set<MessageDestination> getMessageDestinations() throws ConfigurationException;

        /**
         * Retrieves message destinations configured on the target server instance.
         *
         * @return set of message destinations
         * 
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25 
         */
        public Set<MessageDestination> getServerMessageDestinations() throws ConfigurationException;
        
        /**
         * Tests whether a message destination creation is supported.
         *
         * @return true if message destination creation is supported, false otherwise.
         *
         * @since 1.25
         */
        public boolean supportsCreateMessageDestination();

        /**
         * Creates and saves a message destination in the module if it does not exist in the module yet.
         * Message destinations are considered to be equal if their JNDI names are equal.
         *
         * @param name name of the message destination
         * @param type message destination type
         * @return created message destination
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws UnsupportedOperationException if this opearation is not supported
         * @throws ConfigurationException if there is some problem with message destination configuration
         *
         * @since 1.25 
         */
        public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
        throws UnsupportedOperationException, ConfigurationException;
        
        /**
         * Binds the message destination name with message-driven bean.
         * 
         * @param mdbName MDB name
         * @param name name of the message destination
         * @param type message destination type
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public void bindMdbToMessageDestination(String mdbName, String name, MessageDestination.Type type) throws ConfigurationException;

        /**
         * Finds name of message destination which the given MDB listens to
         * 
         * @param mdbName MDB name
         * @return message destination name
         * 
         * @throws NullPointerException if MDB name is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public String findMessageDestinationName(String mdbName) throws ConfigurationException;

        /**
         * Finds message destination with the given name.
         * 
         * @param name message destination name
         * @return message destination if it exists, null otherwise
         *
         * @throws NullPointerException if name is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public MessageDestination findMessageDestination(String name) throws ConfigurationException;

        /**
         * Binds the message destination reference name with the corresponding message destination which is
         * identified by the given name.
         * 
         * @param referenceName reference name used to identify the message destination
         * @param connectionFactoryName connection factory name
         * @param destName name of the message destination
         * @param type message destination type
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * 
         * @since 1.25
         */
        public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
                String destName, MessageDestination.Type type) throws ConfigurationException;

        /**
         * Binds the message destination reference name with the corresponding message destination which is
         * identified by the given name. The reference is used within the EJB scope.
         * 
         * @param ejbName EJB name
         * @param ejbType EJB type - the possible values are 
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
         * @param referenceName reference name used to identify the message destination
         * @param connectionFactoryName connection factory name
         * @param destName name of the message destination
         * @param type message destination type
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with message destination configuration
         * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
         * 
         * @since 1.25
         */
        public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
                String referenceName, String connectionFactoryName,
                String destName, MessageDestination.Type type) throws ConfigurationException;


        /**
         * Returns a JNDI name for the given EJB or <code>null</code> if the EJB has 
         * no JNDI name assigned.
         *
         * @param  ejbName EJB name
         * 
         * @return JNDI name bound to the EJB or <code>null</code> if the EJB has no 
         *         JNDI name assigned.
         * 
         * @throws ConfigurationException if there is some problem with EJB configuration.
         * 
         * @since 1.33
         */
         public String findJndiNameForEjb(String ejbName) throws ConfigurationException;

        /**
         * Binds EJB reference name with EJB name.
         * 
         * @param referenceName name used to identify the EJB
         * @param jndiName JNDI name of the referenced EJB
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with EJB configuration
         * 
         * @since 1.26
         */
        public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException;

        /**
         * Binds EJB reference name with EJB name within the EJB scope.
         * 
         * @param ejbName EJB name
         * @param ejbType EJB type - the possible values are 
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.SESSION,
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.ENTITY and
         *        org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans.MESSAGE_DRIVEN
         * @param referenceName name used to identify the referenced EJB
         * @param jndiName JNDI name of the referenced EJB
         * 
         * @throws NullPointerException if any of parameters is null
         * @throws ConfigurationException if there is some problem with EJB configuration
         * @throws IllegalArgumentException if ejbType doesn't have one of allowed values
         * 
         * @since 1.26
         */
        public void bindEjbReferenceForEjb(String ejbName, String ejbType,
                String referenceName, String jndiName) throws ConfigurationException;

        /**
         * The listener interface to listen for deploy on save operations.
         *
         * @since 1.97
         */
        public static interface DeployOnSaveListener {

            /**
             * Invoked when a deploy on save operation has been successfully
             * performed by the infrastructure.
             *
             * @param artifacts artifacts affected by the deploy
             */
            public void deployed(Iterable<ArtifactListener.Artifact> artifacts);

        }


    }

    /**
     *  Returns list of root directories for source files including configuration files.
     *  Examples: file objects for src/java, src/conf.  
     *  Note: 
     *  If there is a standard configuration root, it should be the first one in
     *  the returned list.
     */
    public FileObject[] getSourceRoots() {
        return new FileObject[0];
    }
    
    /**
     * Return destination path-to-source file mappings.
     * Default returns config file mapping with straight mapping from the configuration
     * directory to distribution directory.
     */
    public SourceFileMap getSourceFileMap() {
        return new DefaultSourceMap(this);
    }
    
    /**
     * Set ID of the server instance that will be used for deployment.
     * 
     * @param severInstanceID server instance ID.
     * @since 1.6
     */
    public abstract void setServerInstanceID(String severInstanceID);
    
    /** 
     * Id of server instance for deployment or null if the module has no server
     * instance set.
     *
     * @return Id of server instance for deployment or <code>null</code> if the module has no server
     *         instance set.
     */
    public abstract String getServerInstanceID();
    
    /**
     * 
     * @return
     * @since 1.48
     */
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return null;
    }

    /**
     * @since 1.56
     */

    public DeployOnSaveClassInterceptor getDeployOnSaveClassInterceptor() {
        return null;
    }

    /**
     * @since org.netbeans.modules.j2eeserver/4 1.70
     */
    public File[] getRequiredLibraries() {
        return new File[] {};
    }
    
    /**
     * Allow J2EE module provider to indicate to server deployment infrastructure
     * that only Compile on Save should be performed and not full Deploy on Save.
     * 
     * @since org.netbeans.modules.j2eeserver/4 1.73
     */
    public boolean isOnlyCompileOnSaveEnabled() {
        return false;
    }
    
    /**
     * Return InstanceProperties of the server instance
     **/
    public InstanceProperties getInstanceProperties() {
        String serverInstanceID = getServerInstanceID();
        if (serverInstanceID == null) {
            return null;
        }
        InstanceProperties props = InstanceProperties.getInstanceProperties(serverInstanceID);

        boolean asserts = false;
        assert asserts = true;

        if (asserts && props != null) {
            return new WarningInstanceProperties(props);
        }
        return props;
    }

    /** 
     * This method is used to determin type of target server. The return value 
     * must correspond to the value returned from {@link getServerInstanceID}.
     *
     * @return the target server type or null if the module has no target server
     *         type set.
     */
    public abstract String getServerID();
    
    /**
     * Return name to be used in deployment of the module.
     */
    public String getDeploymentName() {
        return getConfigSupportImpl().getDeploymentName();
    }

    /**
     * Returns true if the current target platform provide verifier support for this module.
     */
    public boolean hasVerifierSupport() {
        String serverId = getServerID();
        if (serverId != null) {
            Server server = ServerRegistry.getInstance().getServer(serverId);
            if (server != null) {
                return server.canVerify(getJ2eeModule().getType());
            }
        }
        return false;
    }
    
    /**
     * Invoke verifier from current platform on the provided target file.
     * @param target File to run verifier against.
     * @param logger output stream to write verification resutl to.
     */
    public void verify(FileObject target, OutputStream logger) throws ValidationException {
        VerifierSupport verifier = ServerRegistry.getInstance().getServer(getServerID()).getVerifierSupport();
        if (verifier == null) {
            throw new ValidationException ("Verification not supported by the selected server");
        }
        Object jsrType = J2eeModuleAccessor.getDefault().getJsrModuleType(getJ2eeModule().getType());
        if (!verifier.supportsModuleType(jsrType)) {
            throw new ValidationException ("Verification not supported for module type " + jsrType);
        }
        ServerRegistry.getInstance().getServer(getServerID()).getVerifierSupport().verify(target, logger);
    }

    // TODO project should handle this
    protected final void fireServerChange (String oldServerID, String newServerID) {
        Server oldServer = ServerRegistry.getInstance().getServer(oldServerID);
        Server newServer = ServerRegistry.getInstance().getServer(newServerID);

        // corresponds to the "resolve missing server" or "new project"
        if (oldServer == null && newServer != null) {
            ConfigSupportImpl oldConSupp;
            synchronized (configSupportImplLock) {
                oldConSupp = configSupportImpl;
                configSupportImpl = null;
            }

            if (oldConSupp != null) {
                /**
                 * Only if we are resolving the missing server we create the
                 * configuration. In fact this shouldn't hurt anything if we
                 * did it always, but some plugins print some annoying messages.
                 * However oldConSupp not null condition could be fragile.
                 */
                getConfigSupportImpl().ensureConfigurationReady();
                oldConSupp.dispose();
            }
            return;
        }

        // corresponds to switching from one server to another, both existing
        if (oldServer != null && newServer != null && !newServer.equals(oldServer)) {

            if (J2eeModule.Type.WAR.equals(getJ2eeModule().getType())) {
                String oldCtxPath = getConfigSupportImpl().getWebContextRoot();
                ConfigSupportImpl oldConSupp;
                synchronized (configSupportImplLock) {
                    oldConSupp = configSupportImpl;
                    configSupportImpl = null;
                }
                getConfigSupportImpl().ensureConfigurationReady();

                if (oldCtxPath == null || oldCtxPath.equals("")) { //NOI18N
                    oldCtxPath = getDeploymentName();
                    if (null != oldCtxPath) {
                        char c [] = oldCtxPath.replace(' ', '_').toCharArray();
                        for (int i = 0; i < c.length; i++) {
                            if (!Character.UnicodeBlock.BASIC_LATIN.equals(Character.UnicodeBlock.of(c[i])) ||
                                    !Character.isLetterOrDigit(c[i])) {
                                c[i] = '_';
                            }
                        }
                        oldCtxPath = "/" + new String (c); //NOI18N
                    } else {
                        LOGGER.log(Level.WARNING, "null deploymentName for "+
                                getConfigSupportImpl().toString());
                    }
                }
                getConfigSupportImpl().setWebContextRoot(oldCtxPath);

                if (oldConSupp != null) {
                    oldConSupp.dispose();
                }
            } else {
                ConfigSupportImpl oldConSupp;
                synchronized (configSupportImplLock) {
                    oldConSupp = configSupportImpl;
                    configSupportImpl = null;
                }
                getConfigSupportImpl().ensureConfigurationReady();
                if (oldConSupp != null) {
                    oldConSupp.dispose();
                }
            }
        }
    }

    /**
     * Returns all configuration files known to this J2EE Module.
     */
    public final FileObject[] getConfigurationFiles() {
        return getConfigurationFiles(false);
    }

    public final FileObject[] getConfigurationFiles(boolean refresh) {
        if (refresh) {
            configFilesListener.stopListening();
            configFilesListener = null;
        }
        addCFL();
        return ConfigSupportImpl.getConfigurationFiles(this);
    }
    
    public final void addConfigurationFilesListener(ConfigurationFilesListener l) {
        listeners.add(l);
    }
    public final void removeConfigurationFilesListener(ConfigurationFilesListener l) {
        listeners.remove(l);
    }
    
    /**
     * Register an instance listener that will listen to server instances changes.
     *
     * @param l listener which should be added.
     *
     * @since 1.6
     */
    public final void addInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().addInstanceListener(l);
    }

    /**
     * Remove an instance listener which has been registered previously.
     *
     * @param l listener which should be removed.
     *
     * @since 1.6
     */
    public final void removeInstanceListener(InstanceListener l) {
        ServerRegistry.getInstance ().removeInstanceListener(l);
    }
    
    private void addCFL() {
        //already listen
        if (configFilesListener != null)
            return;
        configFilesListener = new ConfigFilesListener(this, listeners);
    }
        
    private ConfigSupportImpl getConfigSupportImpl() {
        return (ConfigSupportImpl) getConfigSupport();
    }

    /**
     * @since 1.48
     */
    public static interface DeployOnSaveSupport {
        
        public void addArtifactListener(ArtifactListener listner);
        
        public void removeArtifactListener(ArtifactListener listener);

        /**
         * @since 1.54
         */
        public boolean containsIdeArtifacts();
    }

    /**
     * @since 1.56
     */
    public static interface DeployOnSaveClassInterceptor {

        public ArtifactListener.Artifact convert(ArtifactListener.Artifact original);

    }
    
    private static class WarningInstanceProperties extends InstanceProperties {

        private final InstanceProperties delegate;

        public WarningInstanceProperties(InstanceProperties delegate) {
            this.delegate = delegate;
        }

        public String getProperty(String propname) throws IllegalStateException {
            LOGGER.log(Level.WARNING, "Accessing instance property through "
                    + J2eeModuleProvider.class.getName() + " is pointing to a missing API or a bad design of the module");
            return delegate.getProperty(propname);
        }

        public void setProperty(String propname, String value) throws IllegalStateException {
            LOGGER.log(Level.WARNING, "Accessing instance property through "
                    + J2eeModuleProvider.class.getName() + " is pointing to a missing API or a bad design of the module");
            delegate.setProperty(propname, value);
        }

        public void setProperties(Properties props) throws IllegalStateException {
            LOGGER.log(Level.WARNING, "Accessing instance property through "
                    + J2eeModuleProvider.class.getName() + " is pointing to a missing API or a bad design of the module");
            delegate.setProperties(props);
        }

        public void refreshServerInstance() {
            delegate.refreshServerInstance();
        }

        public Enumeration propertyNames() throws IllegalStateException {
            return delegate.propertyNames();
        }

        public DeploymentManager getDeploymentManager() {
            return delegate.getDeploymentManager();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            delegate.addPropertyChangeListener(listener);
        }
    }
}
