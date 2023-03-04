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

package org.netbeans.modules.j2ee.jboss4.config;

import java.io.File;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.DatasourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.EjbResourceConfiguration;
import org.netbeans.modules.j2ee.deployment.plugins.spi.config.MessageDestinationConfiguration;
import org.netbeans.modules.j2ee.jboss4.config.ds.DatasourceSupport;
import org.netbeans.modules.j2ee.jboss4.config.mdb.MessageDestinationSupport;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils;
import org.openide.loaders.DataObject;

/** 
 * Base for JBoss DeploymentConfiguration implementations.
 *
 * @author  Pavel Buzek, Libor Kotouc
 */
public abstract class JBDeploymentConfiguration 
        implements DatasourceConfiguration, MessageDestinationConfiguration, EjbResourceConfiguration {

    // TODO move to a more appropriate class as soon as E-mail resource API is introduced
    protected static final String MAIL_SERVICE_JNDI_NAME_JB4 = "java:Mail"; // NOI18N

    //JSR-88 deployable object - initialized when instance is constructed
    protected final J2eeModule j2eeModule;
    
    //cached data object for the server-specific configuration file (initialized by the subclasses)
    protected DataObject deploymentDescriptorDO;

    private final JBPluginUtils.Version version;

    //the directory with resources - supplied by the configuration support in the construction time
    private File resourceDir;

     //support for data sources
    private DatasourceSupport dsSupport;

    //support for message destination resources
    private MessageDestinationSupport destSupport;
    
    /** Creates a new instance of JBDeploymentConfiguration */
    public JBDeploymentConfiguration (J2eeModule j2eeModule, JBPluginUtils.Version version) {
        this.j2eeModule = j2eeModule;
        this.version = version;
        this.resourceDir = j2eeModule.getResourceDirectory();
    }
            
// -------------------------------------- ModuleConfiguration  -----------------------------------------
    
    public J2eeModule getJ2eeModule() {
        return j2eeModule;
    }

    public boolean isAs7() {
        return version != null && JBPluginUtils.JBOSS_7_0_0.compareTo(version) <= 0;
    }
    
// -------------------------------------- DatasourceConfiguration  -----------------------------------------

    private DatasourceSupport getDatasourceSupport() {
        if (dsSupport == null) {
            dsSupport = new DatasourceSupport(resourceDir);
        }
        return dsSupport;
    }
   
    public Set<Datasource> getDatasources() throws ConfigurationException {
        return getDatasourceSupport().getDatasources();
    }

    public Datasource createDatasource(String jndiName, String url,
            String username, String password, String driver) 
            throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        
        return getDatasourceSupport().createDatasource(jndiName, url, username, password, driver);
    }

    public void bindDatasourceReference(String referenceName, String jndiName) throws ConfigurationException {}
    
    public void bindDatasourceReferenceForEjb(String ejbName, String ejbType, 
            String referenceName, String jndiName) throws ConfigurationException {}

    public String findDatasourceJndiName(String referenceName) throws ConfigurationException {
        return null;
    }
    
    public String findDatasourceJndiNameForEjb(String ejbName, String referenceName) throws ConfigurationException {
        return null;
    }
    
// -------------------------------------- MessageDestinationConfiguration  -----------------------------------------

    private MessageDestinationSupport getMessageDestinationsSupport() {
        if (destSupport == null) {
            destSupport = new MessageDestinationSupport(resourceDir);
        }
        return destSupport;
    }
   
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        return getMessageDestinationsSupport().getMessageDestinations();
    }

    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) 
    throws UnsupportedOperationException, ConfigurationException {
        return getMessageDestinationsSupport().createMessageDestination(name, type);
    }
    
    public void bindMdbToMessageDestination(String mdbName, String name, 
            MessageDestination.Type type) throws ConfigurationException {}

    public String findMessageDestinationName(String mdbName) throws ConfigurationException {
        return null;
    }

    public void bindMessageDestinationReference(String referenceName, String connectionFactoryName, 
            String destName, MessageDestination.Type type) throws ConfigurationException {}

    public void bindMessageDestinationReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String connectionFactoryName,
            String destName, MessageDestination.Type type) throws ConfigurationException {}
    
// -------------------------------------- EjbResourceConfiguration  -----------------------------------------
    
    public String findJndiNameForEjb(String ejbName) throws ConfigurationException {
        return null;
    }
    
    public void bindEjbReference(String referenceName, String jndiName) throws ConfigurationException {}

    public void bindEjbReferenceForEjb(String ejbName, String ejbType,
            String referenceName, String jndiName) throws ConfigurationException {}
    
}
