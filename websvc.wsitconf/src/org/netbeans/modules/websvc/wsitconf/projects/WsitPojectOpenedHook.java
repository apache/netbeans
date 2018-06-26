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
