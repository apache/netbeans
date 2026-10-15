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
package org.netbeans.modules.j2ee.sun.share.configbean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.BeanNotFoundException;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.glassfish.eecommon.api.config.GlassfishConfiguration;
import org.netbeans.modules.glassfish.eecommon.api.config.J2eeModuleHelper;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.common.api.DatasourceAlreadyExistsException;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.sun.api.CmpMappingProvider;
import org.netbeans.modules.j2ee.sun.api.ResourceConfiguratorInterface;
import org.netbeans.modules.j2ee.sun.api.SunDeploymentManagerInterface;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceConfigurator;
import org.netbeans.modules.j2ee.sun.dd.api.DDProvider;
import org.netbeans.modules.j2ee.sun.dd.api.RootInterface;
import org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * Manages the deployment plan I/O and access for initializing DConfigBeans
 * <p/>
 * @author Vince Kraemer, Peter Williams, Tomas Kraus
 */ 
public class SunONEDeploymentConfiguration
extends GlassfishConfiguration implements DeploymentConfiguration {

    /**
     * inject cmp bean & field update support into descriptor listener factories
     */
    static {
        CmpListenerSupport.enableCmpListenerSupport();
    }

    /** Value to hold the module name used by the IDE to define the deployable object
     *  this is a jsr88 extension for directory deployment: we need to find a good
     *  dir name to put the bits that will be deployed.
     */
    private String deploymentModuleName = "_default_"; // NOI18N

    private static final RequestProcessor resourceProcessor = new RequestProcessor("sun-resource-ref"); // NOI18N
    private SunDeploymentManagerInterface sdmi;
    

    /**
     * Create an instance of SunONEDeploymentConfiguration for GF V2
     * and earlier servers.
     *
     * @param module  J2eeModule instance for the project represented by this config.
     * @param mySdmi  Sun deployment manager.
     * @param version GlassFish server version.
     *
     * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
     */
    public SunONEDeploymentConfiguration(
            final J2eeModule module, final SunDeploymentManagerInterface mySdmi,
            final GlassFishVersion version
    ) throws ConfigurationException {
        super(module, version);
        this.sdmi = mySdmi;
    }

    /**
     * Create an instance of SunONEDeploymentConfiguration for Webserver.
     *
     * @param module J2eeModule instance for the project represented by this config.
     * @param webServerDDName short name for web server sun dd
     * @param version GlassFish server version.
     *
     * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
     */
    public SunONEDeploymentConfiguration(
            final J2eeModule module, final String webServerDDName,
            final GlassFishVersion version
    ) throws ConfigurationException {
        super(module, J2eeModuleHelper.getWsModuleHelper(webServerDDName), version);
    }

    /**
      * Create an instance of SunONEDeploymentConfiguration for GF V2
      * and earlier servers.
      *
      * @param module  J2eeModule instance for the project represented by this config.
      * @param mySdmi  Sun deployment manager.
      * @deprecated Use constructor with {@link GlassFishVersion}.
      * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
      */
    @Deprecated
    public SunONEDeploymentConfiguration(
            final J2eeModule module, final SunDeploymentManagerInterface mySdmi
    ) throws ConfigurationException {
        super(module, null);
        this.sdmi = mySdmi;
    }

    /**
     * Create an instance of SunONEDeploymentConfiguration for Webserver.
     *
     * @param module J2eeModule instance for the project represented by this config.
     * @param webServerDDName short name for web server sun dd
     * @deprecated Use constructor with {@link GlassFishVersion}.
     * @throws org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException
     */
    @Deprecated
    public SunONEDeploymentConfiguration(
            final J2eeModule module, final String webServerDDName
    ) throws ConfigurationException {
        super(module, J2eeModuleHelper.getWsModuleHelper(webServerDDName), null);
    }

    /** Deprecated form used for JSR-88.  Only exists to keep legacy parts of
     *  j2eeserver module happy.
     *
     * @param dObj JSR-88 deployable object for this JavaEE module.
     * @deprecated
     */
    @Deprecated
    public SunONEDeploymentConfiguration(javax.enterprise.deploy.model.DeployableObject dObj) {
        assert false : "used dead constructor";
    }


    private void postResourceError(String resourceMsg) {
        // Unable to create JDBC data source for CMP.
        // JNDI name of CMP resource field not set.
        String folderMsg;
        String projectName = getProjectName(primarySunDD);
        if (projectName != null) {
            folderMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoResourceFolderForProject", projectName); // NOI18N
        } else {
            folderMsg = NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoResourceFolderUnknown"); // NOI18N
        }

        final String text = folderMsg + " " + resourceMsg;
        resourceProcessor.post(new Runnable() {

            @Override
            public void run() {
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(text, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        });
    }

    private ResourceConfiguratorInterface getResourceConfigurator() {
        return sdmi.getResourceConfigurator(); // rci;
    }

    private String getProjectName(File file) {
        String result = null;
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            Project project = FileOwnerQuery.getOwner(fo);
            if (project != null) {
                ProjectInformation info = ProjectUtils.getInformation(project);
                if (info != null) {
                    result = info.getName();
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------------
    // CMP related automatic descriptor updating support.
    // ------------------------------------------------------------------------
    void removeMappingForCmp(String beanName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.removeMappingForCmp(sunCmpMappings, beanName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    void removeMappingForCmpField(String beanName, String fieldName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.removeMappingForCmpField(sunCmpMappings, beanName, fieldName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    void renameMappingForCmp(String oldBeanName, String newBeanName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.renameMappingForCmp(sunCmpMappings, oldBeanName, newBeanName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    void renameMappingForCmpField(String beanName, String oldFieldName, String newFieldName) {
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, false);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;
                    CmpMappingProvider mapper = getSunCmpMapper();

                    if (mapper.renameMappingForCmpField(sunCmpMappings, beanName, oldFieldName, newFieldName)) {
                        sunCmpMappings.write(sunCmpDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            handleEventRelatedIOException(ex);
        } catch (Exception ex) {
            handleEventRelatedException(ex);
        }
    }

    /* Get the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public String getDeploymentModuleName() {
        return deploymentModuleName;
    }

    /* Set the deploymentModuleName value which is usually passed in by an IDE
     * to define a good value for a directory name used for dir deploy actions.
     **/
    public void setDeploymentModuleName(String s) {
        deploymentModuleName = s;
    }

    // ------------------------------------------------------------------------
    // Implementation of abstract portion of MessageDestinationConfiguration
    // ------------------------------------------------------------------------
    @Override
    public boolean supportsCreateDatasource() {
        return true;
    }

    @Override
    public Set<Datasource> getDatasources() {
        Set<Datasource> datasources = null;
        File resourceDir = module.getResourceDirectory();
        if (resourceDir != null && resourceDir.exists()) {
            datasources = ResourceConfigurator.getResourcesFromFile(resourceDir);
        }
        if(datasources == null) {
            datasources = Collections.EMPTY_SET;
        }
        return datasources;
    }

    @Override
    public Datasource createDatasource(final String jndiName, final String url, final String username, final String password, final String driver) throws UnsupportedOperationException, ConfigurationException, DatasourceAlreadyExistsException {
        Datasource ds = null;
        File resourceDir = module.getResourceDirectory();
        if (resourceDir == null) {
            // Unable to create JDBC data source for resource ref.
            postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
            throw new ConfigurationException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoRefJdbcDataSource", jndiName)); // NOI18N
        }

        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if (rci != null) {
            ds = rci.createDataSource(jndiName, url, username, password, driver, resourceDir,"sun-resources");
        }
        return ds;
    }

    // ------------------------------------------------------------------------
    // Implementation of abstract portion of MessageDestinationConfiguration
    // ------------------------------------------------------------------------
    @Override
    public Set<MessageDestination> getMessageDestinations() throws ConfigurationException {
        Set<MessageDestination> destinations = null;
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        File resourceDir = module.getResourceDirectory();
        if (rci != null && resourceDir != null && resourceDir.exists()) {
            destinations = rci.getMessageDestinations(resourceDir);
        }
        if(destinations == null) {
            destinations = Collections.EMPTY_SET;
        }
        return destinations;
    }

    @Override
    public boolean supportsCreateMessageDestination() {
        return true;
    }

    @Override
    public MessageDestination createMessageDestination(String name, MessageDestination.Type type) throws UnsupportedOperationException, ConfigurationException {
        MessageDestination jmsResource = null;
        File resourceDir = module.getResourceDirectory();
        if (resourceDir == null) {
            // Unable to create reqested JMS Resource
            postResourceError(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", name)); // NOI18N
            throw new ConfigurationException(NbBundle.getMessage(SunONEDeploymentConfiguration.class, "ERR_NoJMSResource", name)); // NOI18N
        }
        ResourceConfiguratorInterface rci = getResourceConfigurator();
        if (rci != null) {
            if (!rci.isJMSResourceDefined(name, resourceDir)) {
                jmsResource = rci.createJMSResource(name, type, name, resourceDir, "sun-resouces");
            }
        }
        return jmsResource;
    }

    // ------------------------------------------------------------------------
    // Implementation of MappingConfiguration
    // ------------------------------------------------------------------------
    public void setCMPResource(String ejbName, String jndiName) throws ConfigurationException {
        // validation
        if (Utils.strEmpty(ejbName) || Utils.strEmpty(jndiName)) {
            return;
        }

        try {
            FileObject primarySunDDFO = getSunDD(primarySunDD, true);
            if (primarySunDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(primarySunDDFO);
                if (sunDDRoot instanceof SunEjbJar) {
                    SunEjbJar sunEjbJar = (SunEjbJar) sunDDRoot;
                    EnterpriseBeans eb = sunEjbJar.getEnterpriseBeans();
                    if (eb == null) {
                        eb = sunEjbJar.newEnterpriseBeans();
                        sunEjbJar.setEnterpriseBeans(eb);
                    }

                    CmpResource cmpResource = eb.getCmpResource();
                    if(cmpResource == null) {
                        cmpResource = eb.newCmpResource();
                        eb.setCmpResource(cmpResource);
                    }

                    String oldJndiName = cmpResource.getJndiName();
                    if(!Utils.strEquivalent(oldJndiName, jndiName)) {
                        if(Utils.notEmpty(oldJndiName)) {
                            // !PW FIXME changing existing jndi name, should we notify user?
                        }

                        cmpResource.setJndiName(jndiName);

                        // if changes, save file.
                        sunEjbJar.write(primarySunDDFO);
                    }
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionBindingResourceRef", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    public void setMappingInfo(OriginalCMPMapping[] mappings) throws ConfigurationException {
        if(!J2eeModule.Type.EJB.equals(module.getType())) {
            return; // wrong module type.
        }
        
        try {
            FileObject sunCmpDDFO = getSunDD(secondarySunDD, true);
            if (sunCmpDDFO != null) {
                RootInterface sunDDRoot = DDProvider.getDefault().getDDRoot(sunCmpDDFO);
                if (sunDDRoot instanceof SunCmpMappings) {
                    SunCmpMappings sunCmpMappings = (SunCmpMappings) sunDDRoot;

                    try {
                       CmpMappingProvider mapper = getSunCmpMapper();
                       mapper.mapCmpBeans(sunCmpDDFO, mappings, sunCmpMappings);
                   } catch(Exception ex) {
                       ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                   }                
                   
                    // if changes, save file.
                    sunCmpMappings.write(sunCmpDDFO);
                }
            }
        } catch (IOException ex) {
            // This is a legitimate exception that could occur, such as a problem
            // writing the changed descriptor to disk.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionMapCmpBeans", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        } catch (Exception ex) {
            // This would probably be a runtime exception due to a bug, but we
            // must trap it here so it doesn't cause trouble upstream.
            // We handle it the same as above for now.
            String message = NbBundle.getMessage(SunONEDeploymentConfiguration.class,
                    "ERR_ExceptionMapCmpBeans", ex.getClass().getSimpleName()); // NOI18N
            throw new ConfigurationException(message, ex);
        }
    }

    private CmpMappingProvider getSunCmpMapper() {
       return sdmi.getSunCmpMapper(); // mapper;
    }

    // ------------------------------------------------------------------------
    // Implementation (or lack thereof) of JSR-88 DeploymentConfiguration
    // ------------------------------------------------------------------------

    @Override
    public DConfigBeanRoot getDConfigBeanRoot(DDBeanRoot dDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    @Override
    public javax.enterprise.deploy.model.DeployableObject getDeployableObject() {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    @Override
    public void removeDConfigBean(DConfigBeanRoot dConfigBeanRoot) throws BeanNotFoundException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    @Override
    public void restore(InputStream inputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    @Override
    public DConfigBeanRoot restoreDConfigBean(InputStream inputStream, DDBeanRoot dDBeanRoot) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    @Override
    public void save(OutputStream outputStream) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

    @Override
    public void saveDConfigBean(OutputStream outputStream, DConfigBeanRoot rootBean) throws javax.enterprise.deploy.spi.exceptions.ConfigurationException {
        throw new UnsupportedOperationException("JSR-88 Configuration is no longer supported.");
    }

}
