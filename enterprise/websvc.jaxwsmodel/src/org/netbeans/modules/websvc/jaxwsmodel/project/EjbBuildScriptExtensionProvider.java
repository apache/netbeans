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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=JaxWsBuildScriptExtensionProvider.class, projectType="org-netbeans-modules-j2ee-ejbjarproject")
public class EjbBuildScriptExtensionProvider implements JaxWsBuildScriptExtensionProvider {
    static String JAX_WS_STYLESHEET_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jaxws-ejb.xsl"; //NOI18N
    private Project project;

    /** Creates a new instance of EjbBuildScriptExtensionProvider */
    public EjbBuildScriptExtensionProvider(Project project) {
        this.project = project;
    }

    @Override
    public void addJaxWsExtension(AntBuildExtender ext) throws IOException {
        TransformerUtils.transformClients(project.getProjectDirectory(), JAX_WS_STYLESHEET_RESOURCE, true);
        FileObject jaxws_build = project.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        assert jaxws_build!=null;
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension == null) {
            extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
            int clientsLength = 0;
            int fromWsdlServicesLength = 0;
            JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                clientsLength = jaxWsModel.getClients().length;
                for (Service service : jaxWsModel.getServices()) {
                    if (service.getWsdlUrl() != null) {
                        fromWsdlServicesLength++;
                    }
                }
            }
            //adding dependencies
            if (clientsLength > 0) {
                extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
            }
            if (fromWsdlServicesLength > 0) {
                extension.addDependency("-pre-pre-compile", "wsimport-service-generate"); //NOI18N
            }
        }
    }

    @Override
    public void removeJaxWsExtension(final AntBuildExtender ext) throws IOException {
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);
        if (extension != null) {
            ProjectManager.mutex().writeAccess(new Runnable() {
                @Override
                public void run() {
                    ext.removeExtension(JAXWS_EXTENSION);
                }
            });
            ProjectManager.getDefault().saveProject(project);
        }
        FileObject jaxws_build = project.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        if (jaxws_build != null) {
            FileLock fileLock = jaxws_build.lock();
            if (fileLock!=null) {
                try {
                    jaxws_build.delete(fileLock);
                } finally {
                    fileLock.releaseLock();
                }
            }
        }
    }

    @Override
    public void handleJaxWsModelChanges(JaxWsModel model) throws IOException {
        AntBuildExtender ext = project.getLookup().lookup(AntBuildExtender.class);
        if (ext != null) {
            int clientsLength = 0;
            int servicesLength = 0;
            int fromWsdlServicesLength = 0;
            JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                clientsLength = jaxWsModel.getClients().length;
                servicesLength = jaxWsModel.getServices().length;
                for (Service service : jaxWsModel.getServices()) {
                    if (service.getWsdlUrl() != null) {
                        fromWsdlServicesLength++;
                    }
                }
            }
            if (clientsLength + servicesLength == 0) {
                // remove nbproject/jaxws-build.xml
                // remove the jaxws extension
                removeJaxWsExtension(ext);
            } else {
                // re-generate nbproject/jaxws-build.xml
                // add jaxws extension, if needed
                changeJaxWsExtension(ext, servicesLength, fromWsdlServicesLength, clientsLength);
            }
            ProjectManager.getDefault().saveProject(project);
        }
    }

    private void changeJaxWsExtension(
            AntBuildExtender ext,
            int servicesLength,
            int fromWsdlServicesLength,
            int clientsLength) throws IOException {

        TransformerUtils.transformClients(project.getProjectDirectory(), JAX_WS_STYLESHEET_RESOURCE, true);
        FileObject jaxws_build = project.getProjectDirectory().getFileObject(TransformerUtils.JAXWS_BUILD_XML_PATH);
        assert jaxws_build != null;
        AntBuildExtender.Extension extension = ext.getExtension(JAXWS_EXTENSION);

        boolean extensionCreated = false;

        if (extension == null) {
            extension = ext.addExtension(JAXWS_EXTENSION, jaxws_build);
            extensionCreated = true;
        }

        // adding/removing dependencies
        if (clientsLength > 0) {
            extension.addDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
        } else if (!extensionCreated) {
            extension.removeDependency("-pre-pre-compile", "wsimport-client-generate"); //NOI18N
        }
        if (fromWsdlServicesLength > 0) {
            extension.addDependency("-pre-pre-compile", "wsimport-service-generate"); //NOI18N
        } else if (!extensionCreated) {
            extension.removeDependency("-pre-pre-compile", "wsimport-service-generate"); //NOI18N
        }
    }
}
