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
package org.netbeans.modules.javaee.wildfly.config;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.netbeans.modules.javaee.wildfly.config.ds.DatasourceSupport;
import org.netbeans.modules.javaee.wildfly.config.mdb.MessageDestinationSupport;
import org.netbeans.modules.javaee.wildfly.ide.ui.WildflyPluginUtils;
import org.openide.loaders.DataObject;

/**
 * Base for JBoss DeploymentConfiguration implementations.
 *
 * @author Pavel Buzek, Libor Kotouc
 */
public abstract class WildflyDeploymentConfiguration
        implements DatasourceConfiguration, MessageDestinationConfiguration, EjbResourceConfiguration {

    // TODO move to a more appropriate class as soon as E-mail resource API is introduced
    protected static final String MAIL_SERVICE_JNDI_NAME_JB4 = "java:Mail"; // NOI18N

    //JSR-88 deployable object - initialized when instance is constructed
    protected final J2eeModule j2eeModule;

    //cached data object for the server-specific configuration file (initialized by the subclasses)
    protected DataObject deploymentDescriptorDO;

    protected final WildflyPluginUtils.Version version;

    //the directory with resources - supplied by the configuration support in the construction time
    private File resourceDir;

    //support for data sources
    private DatasourceSupport dsSupport;

    //support for message destination resources
    private MessageDestinationSupport destSupport;

    protected boolean isWildFly;

    /**
     * Creates a new instance of JBDeploymentConfiguration
     */
    public WildflyDeploymentConfiguration(J2eeModule j2eeModule, WildflyPluginUtils.Version version, boolean isWildFly) {
        this.j2eeModule = j2eeModule;
        this.version = version;
        this.resourceDir = j2eeModule.getResourceDirectory();
        this.isWildFly = isWildFly;
    }

// -------------------------------------- ModuleConfiguration  -----------------------------------------
    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }

    public boolean isWildfly() {
        return isWildFly;
    }

    @Override
    public boolean supportsCreateDatasource() {
        return false;
    }

    @Override
    public boolean supportsCreateMessageDestination() {
        return isWildfly();
    }

// -------------------------------------- DatasourceConfiguration  -----------------------------------------
    private DatasourceSupport getDatasourceSupport() {
        if (dsSupport == null) {
            dsSupport = new DatasourceSupport(resourceDir);
        }
        return dsSupport;
    }

    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return getDatasourceSupport().getDatasources();
    }

    @Override
    public Datasource createDatasource(String jndiName, String url,
            String username, String password, String driver)
            throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {

        return getDatasourceSupport().createDatasource(jndiName, url, username, password, driver);
    }

    @Override
    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {
    }

    @Override
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {
    }

    @Override
    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        return null;
    }

    @Override
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        return null;
    }

// -------------------------------------- MessageDestinationConfiguration  -----------------------------------------
    private MessageDestinationSupport getMessageDestinationsSupport() throws IOException {
        if (destSupport == null) {
            String configFile = "module-destinations";
            if (this.j2eeModule != null && this.j2eeModule.getArchive() != null) {
                configFile = this.j2eeModule.getArchive().getName();
            }
            if (this.version.compareTo(WildflyPluginUtils.WILDFLY_10_0_0) < 0) {
                destSupport = new org.netbeans.modules.javaee.wildfly.config.mdb.MessageDestinationSupportImpl(resourceDir, configFile);
            } else {
                destSupport = new org.netbeans.modules.javaee.wildfly.config.mdb.wf10.MessageDestinationSupportImpl(resourceDir, configFile);
            }
        }
        return destSupport;
    }

    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        try {
            return getMessageDestinationsSupport().getMessageDestinations();
        } catch (IOException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }

    @Override
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type)
            throws UnsupportedOperationException, ConfigurationException {
        try {
            return getMessageDestinationsSupport().createMessageDestination(name, type);
        } catch (IOException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }

    @Override
    public void bindMdbToMessageDestination(String mdbName, String name,
            MessageDestination.Type type) throws ConfigurationException {
    }

    @Override
    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        return null;
    }

    @Override
    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
    }

    @Override
    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {
    }

// -------------------------------------- EjbResourceConfiguration  -----------------------------------------
    @Override
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        return null;
    }

    @Override
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {
    }

    @Override
    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {
    }

}
