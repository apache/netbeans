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
package org.netbeans.modules.glassfish.javaee;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper;
import org.netbeans.modules.glassfish.javaee.db.Hk2DatasourceManager;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.util.NbBundle;

/**
 * Java EE server configuration API support for GlassFish servers before 3.1.
 * Covers GlassFish servers before 3.1. Old {@code sun-resources.xml} files are used.
 * <p/>
 * @author Ludovic Champenois, Peter Williams, Tomas Kraus
 */
public class Hk2Configuration extends GlassfishConfiguration implements DeploymentConfiguration {

    /**
     * Creates an instance of Java EE server configuration API support
     * for GlassFish servers before 3.1.
     * <p/>
     * @param module  Java EE module (project).
     * @param version GlassFish server version.
     * @throws ConfigurationException when there is a problem with Java EE server
     *         configuration initialization.
     */
    public Hk2Configuration(
            final J2eeModule module, final GlassFishVersion version
    ) throws ConfigurationException {
        super(module, version);
    }

    /**
     * Creates an instance of Java EE server configuration API support
     * for GlassFish servers before 3.1 with existing {@link J2eeModuleHelper} instance.
     * <p/>
     * @param module       Java EE module (project).
     * @param moduleHelper Already existing {@link J2eeModuleHelper} instance.
     * @param version      GlassFish server version.
     * @throws ConfigurationException when there is a problem with Java EE server
     *         configuration initialization.
     */
    public Hk2Configuration(
            final J2eeModule module, final J2eeModuleHelper jmh,
            final GlassFishVersion version
    ) throws ConfigurationException {
        super(module, jmh, version);
    }

    @Deprecated
    public Hk2Configuration(DeployableObject dObj)  {
        throw new IllegalArgumentException("deprecated constructor called");
    }

    // ------------------------------------------------------------------------
    // DatasourceConfiguration support
    // ------------------------------------------------------------------------
    @Override
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return Hk2DatasourceManager.getDatasources(module, version);
    }

    @Override
    public boolean supportsCreateDatasource() {
        return true;
    }

    @Override
    public Datasource createDatasource(
            final String jndiName, final String url, final String username,
            final String password, final String driver
    ) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        return Hk2DatasourceManager.createDataSource(
                jndiName, url, username, password, driver, module, version);
    }

    // ------------------------------------------------------------------------
    // MessageDestinationConfiguration support
    // ------------------------------------------------------------------------
    @Override
    public Set<MessageDestination> getMessageDestinations()
            throws ConfigurationException {
        return Hk2MessageDestinationManager.getMessageDestinations(module.getResourceDirectory(),"sun-resources");
    }

    @Override
    public boolean supportsCreateMessageDestination() {
        return true;
    }

    @Override
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) throws UnsupportedOperationException, org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException {
        File resourceDir = module.getResourceDirectory();
        if (resourceDir == null) {
            Logger.getLogger("glassfish-javaee").log(Level.WARNING, "Null Resource Folder."); // NOI18N
            throw new ConfigurationException(NbBundle.getMessage(
                    ModuleConfigurationImpl.class, "ERR_NoJMSResource", name, type)); // NOI18N
        }
        return Hk2MessageDestinationManager.createMessageDestination(name, type, resourceDir,"sun-resources");
    }

    // ------------------------------------------------------------------------
    // Implementation (or lack thereof) of JSR-88 DeploymentConfiguration interface
    // Here to make the deployment manager class happy.
    // ------------------------------------------------------------------------
    @Override
    public DeployableObject getDeployableObject() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot ddbeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeDConfigBean(DConfigBeanRoot dconfigBeanRoot) throws BeanNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DConfigBeanRoot restoreDConfigBean(InputStream is, DDBeanRoot ddbeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveDConfigBean(OutputStream os, DConfigBeanRoot dconfigBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void restore(InputStream is) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void save(OutputStream os) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
