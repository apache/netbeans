/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.netbeans.modules.j2ee.deployment.plugins.spi;

import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.WizardDescriptor;

/**
 * Factory for optional deployment functionality that a plugin can provide.
 * Plugins need to register an instance of this class in module layer in folder
 * <code>J2EE/DeploymentPlugins/{plugin_name}</code>.
 *
 * @author  Pavel Buzek
 */
public abstract class OptionalDeploymentManagerFactory {

    /**
     * Create StartServer for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */ 
    public abstract StartServer getStartServer (DeploymentManager dm);
    
    /** 
     * Create IncrementalDeployment for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public abstract IncrementalDeployment getIncrementalDeployment (DeploymentManager dm);
    
    /** 
     * Create FindJSPServlet for given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public abstract FindJSPServlet getFindJSPServlet (DeploymentManager dm);
    
    /** 
     * Create TargetModuleIDResolver for the given DeploymentManager.
     * The instance returned by this method will be cached by the j2eeserver.
     */
    public TargetModuleIDResolver getTargetModuleIDResolver(DeploymentManager dm) {
        return null;
    }
    
    /** 
     * Create the wizard iterator to be used in the Add Server Instance wizard
     */
    public WizardDescriptor.InstantiatingIterator getAddInstanceIterator() {
        return null;
    }
    

    /**
     * Returns <code>true</code> if the common UI (like the wizard in common
     * add dialog) should be handled by insfrastructure of the j2eeserver.
     *
     * @return <code>true</code> if the common UI is required by the plugin
     * @since 1.38.0
     */
    public boolean isCommonUIRequired() {
        return true;
    }
    
    /**
     * Creates an Ant deployment provider for the specified deployment manager.
     *
     * @param dm deployment manager.
     * @return an instance of the AntDeploymentProvider if Ant deployment
     *         is supported for the specified deployment manager, null otherwise.
     * @since 1.18
     */
    public AntDeploymentProvider getAntDeploymentProvider(DeploymentManager dm) {
        return null;
    }
    
    /**
     * Creates a <code>DatasourceManager</code> for the given deployment manager
     * or <code>null</code> if data source management is not supported
     *
     * @param dm the deployment manager
     *
     * @return a data source manager or <code>null</code> if data source management
     *         is not supported
     *
     * @since 1.15
     */
    public DatasourceManager getDatasourceManager(DeploymentManager dm) {
        return null;
    }
    
    /**
     * Creates a JDBC driver deployer for the specified deployment manager.
     * 
     * @param dm deployment manager.
     * 
     * @return JDBC driver deployer for the specified deployment manager or null
     *         if JDBC driver deployment is not supported.
     * 
     * @since 1.24
     */
    public JDBCDriverDeployer getJDBCDriverDeployer(DeploymentManager dm) {
        return null;
    }

    /**
     * Creates a <code>MessageDestinationDeployment</code> for the given deployment manager
     * or <code>null</code> if message destination deployment is not supported
     *
     * @param dm the deployment manager
     *
     * @return a message destination deployment or <code>null</code> 
     *          if message destination deployment is not supported
     *
     * @since 1.25
     */
    public MessageDestinationDeployment getMessageDestinationDeployment(DeploymentManager dm) {
        return null;
    }

    /**
     * Creates a <code>ServerInstanceDescriptor</code> for the given deployment manager
     * or <code>null</code> if descriptor is not supported.
     *
     * @param dm the deployment manager
     * @return instance descriptor or <code>null</code> if descriptor is not supported
     * @since 1.46
     */
    public ServerInstanceDescriptor getServerInstanceDescriptor(DeploymentManager dm) {
        return null;
    }

    /**
     * Allows a plugin to perform post initialization action. When this method
     * invoked infrastructure is initialized so it can register/query instances.
     *
     * @since 1.51
     */
    public void finishServerInitialization() throws ServerInitializationException {
    }

    /**
     * Return the manager handling the server libraries. May return
     * <code>null</code> if the functionality is not supported by the plugin.
     *
     * @param dm the deployment manager
     * @return the manager handling the server libraries
     * @since 1.68
     * @see ServerLibraryManager
     */
    @CheckForNull
    public ServerLibraryManager getServerLibraryManager(DeploymentManager dm) {
        return null;
    }
}
