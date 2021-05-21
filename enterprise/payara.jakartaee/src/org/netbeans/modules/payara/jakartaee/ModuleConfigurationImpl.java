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

package org.netbeans.modules.payara.jakartaee;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.payara.jakartaee.db.ResourcesHelper;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ContextRootConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.ModuleConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DeploymentPlanConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Implementation of ModuleConfiguration.
 * <p/>
 * Primarily serves to delegate directly to the specified
 * DeploymentConfiguration instance, as that is in shared code
 * and has appropriate access and this instance is not.
 */
public class ModuleConfigurationImpl implements
        ModuleConfiguration,
        ContextRootConfiguration,
        DeploymentPlanConfiguration,
        DatasourceConfiguration,
        MessageDestinationConfiguration,
        EjbResourceConfiguration
{
    
    private Hk2Configuration config;
    private J2eeModule module;
    private Lookup lookup;
    
    private static final Map<J2eeModule,ModuleConfigurationImpl> configs = 
            new HashMap<J2eeModule,ModuleConfigurationImpl>();

    @SuppressWarnings("LeakingThisInConstructor")
    ModuleConfigurationImpl(J2eeModule module, Hk2Configuration config, Hk2DeploymentManager hk2Dm) throws ConfigurationException {
        synchronized (configs) {
            this.module = module;
            this.config = config;
            ResourcesHelper.addSampleDatasource(module, hk2Dm);
            configs.put(module, this);
        }
    }
    
    static public ModuleConfigurationImpl get(J2eeModule j2eemodule) {
        synchronized (configs) {
            return configs.get(j2eemodule);
        }
    }

    // ------------------------------------------------------------------------
    // J2EE Server API implementations
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Implementation of ModuleConfiguration
    // ------------------------------------------------------------------------
    @Override
    public synchronized Lookup getLookup() {
        if (null == lookup) {
            lookup = Lookups.fixed(this);
        }
        return lookup;
    }

    @Override
    public J2eeModule getJ2eeModule() {
        return module;
    }

    @Override
    public void dispose() {
        config.dispose();
    }

    // ------------------------------------------------------------------------
    // Implementation of ContextRootConfiguration
    // ------------------------------------------------------------------------
    @Override
    public String getContextRoot() throws ConfigurationException {
        return config.getContextRoot();
    }

    @Override
    public void setContextRoot(String contextRoot) throws ConfigurationException {
        config.setContextRoot(contextRoot);
    }

    // ------------------------------------------------------------------------
    // Implementation of DeploymentPlanConfiguration
    // ------------------------------------------------------------------------
    @Override
    public void save(OutputStream outputStream) throws ConfigurationException {
        config.saveConfiguration(outputStream);
    }

    // ------------------------------------------------------------------------
    // Implementation of DatasourceConfiguration
    // ------------------------------------------------------------------------
    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return config.getDatasources();
    }

    @Override
    public boolean supportsCreateDatasource() {
        return config.supportsCreateDatasource();
    }

    @Override
    public Datasource createDatasource(String jndiName, String url, String username, String password, String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        return config.createDatasource(jndiName, url, username, password, driver);
    }

    @Override
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        config.bindDatasourceReference(referenceName, jndiName);
    }

    @Override
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws ConfigurationException {
        config.bindDatasourceReferenceForEjb(ejbName, ejbType, referenceName, jndiName);
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        return config.findDatasourceJndiName(referenceName);
    }

    @Override
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        return config.findDatasourceJndiNameForEjb(ejbName, referenceName);
    }

    // ------------------------------------------------------------------------
    // Implementation of EjbResourceConfiguration
    // ------------------------------------------------------------------------
    @Override
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        return config.findJndiNameForEjb(ejbName);
    }

    @Override
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {
        config.bindEjbReference(referenceName, jndiName);
    }

    @Override
    public void bindEjbReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws ConfigurationException {
        config.bindEjbReferenceForEjb(ejbName, ejbType, referenceName, jndiName);
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        return config.getMessageDestinations();
 }

    @Override
    public boolean supportsCreateMessageDestination() {
        return config.supportsCreateMessageDestination();
    }

    @Override
    public MessageDestination createMessageDestination(String name, Type type) throws UnsupportedOperationException, ConfigurationException {
        return config.createMessageDestination(name, type);
    }

    @Override
    public void bindMdbToMessageDestination(String mdbName, String name, Type type) throws ConfigurationException {
        config.bindMdbToMessageDestination(mdbName, name, type);
    }

    @Override
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        return config.findMessageDestinationName(mdbName);
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        config.bindMessageDestinationReference(referenceName, connectionFactoryName, destName, type);
    }

    @Override
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType, String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        config.bindMessageDestinationReferenceForEjb(ejbName, ejbType, referenceName, connectionFactoryName, destName, type);
    }

 }

