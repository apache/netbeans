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

package org.netbeans.modules.maven.jaxws;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class MavenJAXWSSupportImpl implements JAXWSLightSupportImpl {
    private final Project prj;
    private final List<JaxWsService> services = new LinkedList<JaxWsService>();
    /** Path for catalog file. */
    public static final String CATALOG_PATH = "src/jax-ws-catalog.xml"; //NOI18N

    private static final String SERVLET_CLASS_NAME =
            "com.sun.xml.ws.transport.http.servlet.WSServlet"; //NOI18N
    private static final String SERVLET_LISTENER =
            "com.sun.xml.ws.transport.http.servlet.WSServletContextListener"; //NOI18N
    /** Constructor.
     *
     * @param prj project
     */
    MavenJAXWSSupportImpl(Project prj) {
        this.prj = prj;
    }

    @Override
    public void addService(JaxWsService service) {
        services.add(service);

        if (service.isServiceProvider() && !WSUtils.isJsr109Supported(prj)) {
            boolean generateNonJsr109Stuff = WSUtils.needNonJsr109Artifacts(prj);
            if (generateNonJsr109Stuff) {
                // modify sun-jaxws.xml file
                Endpoint endpoint = null;
                try {
                    endpoint = addSunJaxWsEntries(service);
                } catch (IOException ex) {
                    Logger.getLogger(MavenJAXWSSupportImpl.class.getName()).log(Level.WARNING,
                            "Cannot modify sun-jaxws.xml file", ex); //NOI18N
                }
                if (endpoint != null) {
                    // modify web.xml file
                    try {
                        WSUtils.addServiceToDD(prj, service, endpoint);
                    } catch (IOException ex) {
                        Logger.getLogger(MavenJAXWSSupportImpl.class.getName()).log(Level.WARNING,
                                "Cannot add service elements to web.xml file", ex); //NOI18N
                    }
                }
            }
        }
    }

    private Endpoint addSunJaxWsEntries(JaxWsService service)
        throws IOException {

        FileObject ddFolder = getDeploymentDescriptorFolder();
        
        if (ddFolder == null) {
            File webAppFolder = FileUtilities.resolveFilePath(
                FileUtil.toFile(prj.getProjectDirectory()), "src/main/webapp"); //NOI18N
            if (webAppFolder.exists()) {
                FileObject webapp = FileUtil.toFileObject(webAppFolder);
                ddFolder = webapp.createFolder("WEB-INF"); //NOI18N
            }
        }
        
        if (ddFolder != null) {
            return WSUtils.addSunJaxWsEntry(ddFolder, service);
        } else {
            String mes = NbBundle.getMessage(MavenJAXWSSupportImpl.class, "MSG_CannotFindWEB-INF"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
            return null;
        }
    }

    @Override
    public List<JaxWsService> getServices() {
        return services;
    }

    @Override
    public void removeService(JaxWsService service) {
        String localWsdl = service.getLocalWsdl();
        if (localWsdl != null) {
            // remove auxiliary wsdl url property
            FileObject wsdlFolder = getWsdlFolder(false);
            if (wsdlFolder != null) {
                FileObject wsdlFo = wsdlFolder.getFileObject(localWsdl);
                if (wsdlFo != null) {
                    Preferences prefs = ProjectUtils.getPreferences(prj, MavenWebService.class, true);
                    if (prefs != null) {
                        prefs.remove(MavenWebService.CLIENT_PREFIX+service.getId());
                        prefs.remove(MavenWebService.SERVICE_PREFIX+service.getId());
                    }
                }
            }
        }
        if (service.isServiceProvider() && !WSUtils.isJsr109Supported(prj)) {

            // modify web.xml file
            try {
                WSUtils.removeServiceFromDD(prj, service);
            } catch (IOException ex) {
                Logger.getLogger(MavenJAXWSSupportImpl.class.getName()).log(Level.WARNING,
                        "Cannot remove services from web.xml", ex); //NOI18N
            }

            // modify sun-jaxws.xml file
            try {
                removeSunJaxWsEntries(service);
            } catch (IOException ex) {
                Logger.getLogger(MavenJAXWSSupportImpl.class.getName()).log(Level.WARNING,
                        "Cannot modify sun-jaxws.xml file", ex); //NOI18N
                }
        }
        services.remove(service);
    }

    private void removeSunJaxWsEntries(JaxWsService service) throws IOException {
        FileObject ddFolder = getDeploymentDescriptorFolder();
        if (ddFolder != null) {
            WSUtils.removeSunJaxWsEntry(ddFolder, service);
        }
    }

    @Override
    public FileObject getDeploymentDescriptorFolder() {
        File wsdlDir = FileUtilities.resolveFilePath(
            FileUtil.toFile(prj.getProjectDirectory()), "src/main/webapp/WEB-INF"); //NOI18N
        if (wsdlDir.exists()) {
            return FileUtil.toFileObject(wsdlDir);
        }
        return null;
    }

    @Override
    public FileObject getWsdlFolder(boolean createFolder) {
        File wsdlDir = FileUtilities.resolveFilePath(
                FileUtil.toFile(prj.getProjectDirectory()), "src/wsdl"); //NOI18N
        if (wsdlDir.exists()) {
            return FileUtil.toFileObject(wsdlDir);
        } else if (createFolder) {
            boolean created = wsdlDir.mkdirs();
            if (created) {
                return FileUtil.toFileObject(wsdlDir);
            }
        }
        return null;
    }

    @Override
    public FileObject getBindingsFolder(boolean createFolder) {
        File bindingsDir = FileUtilities.resolveFilePath(
                FileUtil.toFile(prj.getProjectDirectory()), "src/jaxws-bindings"); //NOI18N
        if (bindingsDir .exists()) {
            return FileUtil.toFileObject(bindingsDir);
        } else if (createFolder) {
            boolean created = bindingsDir.mkdirs();
            if (created) {
                return FileUtil.toFileObject(bindingsDir);
            }
        }
        return null;
    }

    @Override
    public URL getCatalog() {
        File catalogFile = FileUtilities.resolveFilePath(FileUtil.toFile(prj.getProjectDirectory()), CATALOG_PATH);
        try {
            return catalogFile.toURI().toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        J2eeModuleProvider provider = WSUtils.getModuleProvider(prj);
        if ( provider == null ){
            return null;
        }
        J2eeModule module = provider.getJ2eeModule();
        if (module==null){
            return null;
        }
        return module.getMetadataModel(WebservicesMetadata.class);
    }
    
}
