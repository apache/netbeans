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

package org.netbeans.modules.j2ee.weblogic9.config;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination.Type;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;

/**
 *
 * @author Petr Hejl
 */
public class WLDeploymentConfiguration implements DatasourceConfiguration, MessageDestinationConfiguration {

    private final WLDatasourceSupport datasourceSupport;
    
    private final WLMessageDestinationSupport messageSupport;

    public WLDeploymentConfiguration(J2eeModule module, Version version) {
        this.datasourceSupport = new WLDatasourceSupport(module.getResourceDirectory());
        this.messageSupport = new WLMessageDestinationSupport(module.getResourceDirectory(), version);
    }

    @Override
    public void bindMdbToMessageDestination(String mdbName, String name, Type type) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType, String referenceName, String connectionFactoryName, String destName, Type type) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MessageDestination createMessageDestination(String name, Type type) throws UnsupportedOperationException, ConfigurationException {
        // api does not provide module and jndi name so we use the same
        return messageSupport.createMessageDestination(name, name, type);
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        return new HashSet<MessageDestination>(messageSupport.getMessageDestinations());
    }

    @Override
    public boolean supportsCreateMessageDestination() {
        return false;
    }

    @Override
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
        throw new UnsupportedOperationException("bindDatasourceReference");
    }

    @Override
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, String referenceName, String jndiName) throws ConfigurationException {
        throw new UnsupportedOperationException("bindDatasourceReferenceForEjb");
    }

    @Override
    public Datasource createDatasource(String jndiName, String url, String username, String password, String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        return datasourceSupport.createDatasource(jndiName, url, username, password, driver);
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        throw new UnsupportedOperationException("findDatasourceJndiName");
    }

    @Override
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        throw new UnsupportedOperationException("findDatasourceJndiNameForEjb");
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return new HashSet<Datasource>(datasourceSupport.getDatasources());
    }

    @Override
    public boolean supportsCreateDatasource() {
        return true;
    }

}
