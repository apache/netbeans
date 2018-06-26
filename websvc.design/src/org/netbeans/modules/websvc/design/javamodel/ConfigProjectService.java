/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            if (isJsr109 != null && !isJsr109.booleanValue()) {
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
