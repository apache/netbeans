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
package org.netbeans.modules.websvc.design.javamodel;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.design.configuration.WSConfiguration;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProvider;
import org.netbeans.modules.websvc.design.configuration.WSConfigurationProviderRegistry;
import org.openide.execution.ExecutorTask;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;


/**
 * Ant based Project service instance ( info based on Service class which 
 * is model for jax-ws.xml ).
 * @author ads
 *
 */
class ConfigProjectService implements ProjectService {
    
    private static final String BUILD_IMPL_XML_PATH = "nbproject/build-impl.xml"; // NOI18N

    ConfigProjectService( JAXWSSupport support, Service service, 
            DataObject dataObject ) 
    {
        this.support = support;
        this.service = service;
        this.dataObject = dataObject;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.design.javamodel.ProjectService#cleanup()
     */
    @Override
    public void cleanup() throws java.io.IOException {
        if (getService() == null || support == null) {
            return;
        }
        String serviceName = getService().getName();
        if (serviceName != null) {
            FileObject localWsdlFolder = support.getLocalWsdlFolderForService(
                    serviceName, false);
            if (localWsdlFolder != null) {
                // removing local wsdl and xml artifacts
                FileLock lock = null;
                FileObject clientArtifactsFolder = localWsdlFolder.getParent();
                try {
                    lock = clientArtifactsFolder.lock();
                    clientArtifactsFolder.delete(lock);
                }
                finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
                // removing wsdl and xml artifacts from WEB-INF/wsdl
                FileObject wsdlFolder = support.getWsdlFolder(false);
                if (wsdlFolder != null) {
                    FileObject serviceWsdlFolder = wsdlFolder
                            .getFileObject(serviceName);
                    if (serviceWsdlFolder != null) {
                        try {
                            lock = serviceWsdlFolder.lock();
                            serviceWsdlFolder.delete(lock);
                        }
                        finally {
                            if (lock != null) {
                                lock.releaseLock();
                            }
                        }
                    }
                }
                // cleaning java artifacts
                FileObject buildImplFo = getProject().getProjectDirectory()
                        .getFileObject(BUILD_IMPL_XML_PATH);
                try {
                    ExecutorTask wsimportTask = ActionUtils.runTarget(
                            buildImplFo,
                            new String[] { "wsimport-service-clean-"
                                    + serviceName }, null); // NOI18N
                    wsimportTask.waitFinished();
                }
                catch (java.io.IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
                catch (IllegalArgumentException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
            }

            // removing service from jax-ws.xml
            support.removeService(serviceName);

            // remove non JSR109 entries
            Boolean isJsr109 = getProject().getLookup().lookup(JaxWsModel.class)
                    .getJsr109();
            if (isJsr109 != null && !isJsr109) {
                if (getService().getWsdlUrl() != null) {
                    // if coming from wsdl
                    serviceName = getService().getServiceName();
                }
                support.removeNonJsr109Entries(serviceName);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.design.javamodel.ProjectService#getConfigurations()
     */
    @Override
    public Collection<WSConfiguration> getConfigurations() {
        if ( getService()== null){
            return Collections.emptyList();
        }
        Collection<WSConfiguration> configurations = new LinkedList<WSConfiguration>();
        for(WSConfigurationProvider provider : getConfigProviders()){
            WSConfiguration config = provider.getWSConfiguration(
                    getService(), dataObject.getPrimaryFile());
            if ( config != null ){
                configurations.add( config );
            }
        }
        return configurations;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.design.javamodel.ProjectService#getWsdlUrl()
     */
    @Override
    public String getWsdlUrl() {
        Service s = getService();
        return s == null ? null : s.getWsdlUrl();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.design.javamodel.ProjectService#getImplementationClass()
     */
    @Override
    public String getImplementationClass() {
        Service s = getService();
        return s == null ? null : s.getImplementationClass();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.websvc.design.javamodel.ProjectService#getLocalWsdlFile()
     */
    @Override
    public String getLocalWsdlFile() {
        Service s = getService();
        return s == null ? null : s.getLocalWsdlFile();
    }
    
    Service getService(){
        if ( service == null ){
            Project project = getProject();
            if(project==null) {
                return null;
            }
            JaxWsModel model = project.getLookup().lookup(JaxWsModel.class);
            ClassPath classPath = ClassPath.getClassPath(dataObject.getPrimaryFile(),
                    ClassPath.SOURCE);
            if (classPath == null) {
                return null;
            }
            String implClass = classPath.getResourceName(dataObject.getPrimaryFile(), '.', false);
            service = model.findServiceByImplementationClass(implClass);
            return service;
        }
        return service;
    }
    
    private Project getProject(){
        return FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
    }
    
    private Set<WSConfigurationProvider> getConfigProviders(){
        return WSConfigurationProviderRegistry.getDefault().getWSConfigurationProviders();
    }
    
    private final JAXWSSupport support;
    private Service service;
    private final DataObject dataObject;

}
