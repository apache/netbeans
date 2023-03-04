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

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/** Lookup Provider for WS Support
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-java-j2seproject")
public class J2SEWSSupportLookupProvider extends ProjectOpenedHook {
    private Project project;

    /** Creates a new instance of JaxWSLookupProvider */
    public J2SEWSSupportLookupProvider(Project project) {
        this.project = project;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("DE_MIGHT_IGNORE")
    @Override
    protected void projectOpened() {
        if(WebServicesClientSupport.isBroken(project)) {
            WebServicesClientSupport.showBrokenAlert(project);
        }
        FileObject jaxWsFo = WSUtils.findJaxWsFileObject(project);
        try {
            if (jaxWsFo != null && WSUtils.hasClients(jaxWsFo)) {
                final JAXWSClientSupport jaxWsClientSupport = project.getLookup().lookup(JAXWSClientSupport.class);
                if (jaxWsClientSupport != null) {
                    FileObject wsdlFolder = null;
                    try {
                        wsdlFolder = jaxWsClientSupport.getWsdlFolder(false);
                    } catch (IOException ex) {}
                    if (wsdlFolder == null || wsdlFolder.getParent().getFileObject("jax-ws-catalog.xml") == null) { //NOI18N
                        RequestProcessor.getDefault().post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JaxWsCatalogPanel.generateJaxWsCatalog(project, jaxWsClientSupport);
                                } catch (IOException ex) {
                                    Logger.getLogger(JaxWsCatalogPanel.class.getName()).log(Level.WARNING, "Cannot create jax-ws-catalog.xml", ex);
                                }
                            }
                        });
                    }
                }
            }
        } catch (IOException ex) {
             Logger.getLogger(JaxWsCatalogPanel.class.getName()).log(Level.WARNING, "Cannot read nbproject/jax-ws.xml file", ex);
        }
    }

    @Override
    protected void projectClosed() {
    }

    @ProjectServiceProvider(service=JAXWSClientSupport.class, projectType="org-netbeans-modules-java-j2seproject")
    public static JAXWSClientSupport createJAXWSClientSupport(Project project) {
        JAXWSClientSupportImpl j2seJAXWSClientSupport = new J2SEProjectJAXWSClientSupport(project);
        return JAXWSClientSupportFactory.createJAXWSClientSupport(j2seJAXWSClientSupport);
    }

    @ProjectServiceProvider(service=WebServicesClientSupport.class, projectType="org-netbeans-modules-java-j2seproject")
    public static WebServicesClientSupport createWebServicesClientSupport(Project project) {
        WebServicesClientSupportImpl jaxrpcClientSupport = new J2SEProjectJaxRpcClientSupport(project);
        return WebServicesClientSupportFactory.createWebServicesClientSupport(jaxrpcClientSupport);
    }
}
