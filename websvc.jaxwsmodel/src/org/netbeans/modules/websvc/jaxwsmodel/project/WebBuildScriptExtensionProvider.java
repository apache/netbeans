/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
@ProjectServiceProvider(service=JaxWsBuildScriptExtensionProvider.class, projectType="org-netbeans-modules-web-project")
public class WebBuildScriptExtensionProvider implements JaxWsBuildScriptExtensionProvider {
    static String JAX_WS_STYLESHEET_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jaxws-web.xsl"; //NOI18N
    private Project project;

    /** Creates a new instance of EjbBuildScriptExtensionProvider */
    public WebBuildScriptExtensionProvider(Project project) {
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
