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
package org.netbeans.modules.websvc.wsitconf.projects;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.TypeElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;

import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitconf.wizard.STSWizardCreator;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;



/**
 * @author ads
 *
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType={
    "org-netbeans-modules-web-project"
})
public class WsitPojectOpenedHook extends ProjectOpenedHook {
    
    private static Logger LOG = Logger.getLogger( WsitPojectOpenedHook.class.getCanonicalName()); 
    
    public WsitPojectOpenedHook( Project project ){
        this.project = project;
    }

    @Override
    protected void projectOpened() {
        JAXWSSupport support = JAXWSSupport.getJAXWSSupport(
                project.getProjectDirectory());
        
        FileObject ddFolder = support.getDeploymentDescriptorFolder();
        if ( ddFolder == null ){
            return;
        }
        FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");  // NOI18N
        if ( sunjaxwsFile == null ){
            return;
        }
        try {
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(
                    sunjaxwsFile);
            final Endpoint[] endpointsCollection = endpoints.getEndpoints();
            final MetadataModel<WebservicesMetadata> wsModel = support
                    .getWebservicesMetadataModel();
            wsModel.runReadActionWhenReady(new MetadataModelAction<WebservicesMetadata, Void>()
            {

                @Override
                public Void run( final WebservicesMetadata metadata ) {
                    for (Endpoint endpoint : endpointsCollection) {
                        String implementation = endpoint.getImplementation();
                        FileObject fileObject = getFileObjectFromClassName(implementation);
                        if (fileObject != null) {
                            Object isStsWs = fileObject
                                    .getAttribute(STSWizardCreator.STS_WEBSERVICE);
                            if (isStsWs != null
                                    && isStsWs.toString().equals("true"))// NOI18N
                            {
                                addDescriptorsCleaner(fileObject,
                                        implementation,
                                        endpoint.getEndpointName());
                            }
                        }
                    }
                    return null;
                }

            });
        }
        catch (IOException e ){
            LOG.log(Level.WARNING , null , e);
        }
    }
    
    private void addDescriptorsCleaner( FileObject fileObject , final String fqn , 
            final String endpointName )
    {
        fileObject.addFileChangeListener( new FileChangeAdapter(){
           /* (non-Javadoc)
            * @see org.openide.filesystems.FileChangeAdapter#fileDeleted(org.openide.filesystems.FileEvent)
            */
            @Override
            public void fileDeleted( FileEvent fe ) {
                try {
                    JAXWSSupport support = JAXWSSupport.getJAXWSSupport(
                         project.getProjectDirectory());
                    support.removeNonJsr109Entries(Util.MEX_NAME);
                    support.removeNonJsr109Entries(fqn);
                    support.removeNonJsr109Entries(endpointName);
                }
                catch(IOException e ){
                    LOG.log(Level.WARNING , null , e);
                }
            } 
        });
    }
    
    public FileObject getFileObjectFromClassName(String fqn) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups == null || sourceGroups.length == 0) {
            return null;
        }
        ClasspathInfo cpInfo = ClasspathInfo.create(sourceGroups[0].getRootFolder());
        ClassIndex ci = cpInfo.getClassIndex();
        int beginIndex = fqn.lastIndexOf('.')+1;
        String simple = fqn.substring(beginIndex);
        Set<ElementHandle<TypeElement>> handles = ci.getDeclaredTypes(
                simple, ClassIndex.NameKind.SIMPLE_NAME, 
                Collections.singleton(ClassIndex.SearchScope.SOURCE));
        for (ElementHandle<TypeElement> handle : handles) {
            if (fqn.equals(handle.getQualifiedName())) {
                return SourceUtils.getFile(handle, cpInfo);
            }
        }
        return null;
    }
    
    @Override
    protected void projectClosed() {
    }
        
    private Project project;

}
