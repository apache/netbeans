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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.wsitconf.util.UndoManagerHolder;
import org.netbeans.modules.websvc.wsitconf.projects.MavenWsitProvider;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Grebac
 */
public class MavenWSITModelSupport {
    
    private static final Logger logger = Logger.getLogger(MavenWSITModelSupport.class.getName());
    
    /** Creates a new instance of MavenWSITModelSupport */
    public MavenWSITModelSupport() { }
    
    public static WSDLModel getModel(Node node, Project project, JAXWSLightSupport jaxWsSupport, JaxWsService jaxService, UndoManagerHolder umHolder, boolean create, Collection<FileObject> createdFiles) throws MalformedURLException, Exception {

        WSDLModel model = null;
        boolean isClient = !jaxService.isServiceProvider();

        if (isClient) {
            model = getModelForClient(project, jaxWsSupport, jaxService, create, createdFiles);
        } else {  //it is a service
            FileObject implClass = node.getLookup().lookup(FileObject.class);
            try {
                String wsdlUrl = jaxService.getLocalWsdl();
                if (wsdlUrl == null) { // WS from Java
                    if ((implClass == null) || (!implClass.isValid() || implClass.isVirtual())) {
                        logger.log(Level.INFO, "Implementation class is null or not valid, or just virtual: " + implClass + ", service: " + jaxService);
                        return null;
                    }
                    return WSITModelSupport.getModelForServiceFromJava(implClass, project, create, createdFiles);
                } else {
                    if (project == null) return null;
                    return getModelForServiceFromWsdl(jaxWsSupport, jaxService);
                }
            } catch (Exception e) {
                logger.log(Level.INFO, null, e);
            }
        }

        if ((model != null) && (umHolder != null) && (umHolder.getUndoManager() == null)) {
            UndoManager undoManager = new UndoManager();
            model.addUndoableEditListener(undoManager);  //maybe use WeakListener instead
            umHolder.setUndoManager(undoManager);
        }
        return model;
    }

    /* Retrieves WSDL model for a WS client - always has a wsdl
     */
    public static WSDLModel getModelForClient(Project p, JAXWSLightSupport jaxWsSupport, JaxWsService jaxWsService, boolean create, Collection<FileObject> createdFiles) throws IOException {
        URI uri = null;
        try {
            uri = jaxWsSupport.getWsdlFolder(false).getFileObject(jaxWsService.getLocalWsdl()).getURL().toURI();
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (uri == null) return null;
        return WSITModelSupport.getModelForClient(p, uri, create, createdFiles);
    }
    
    /* Retrieves WSDL model for a WS from Java - if config file exists, reuses that one, otherwise generates new one
     */
    public static WSDLModel getServiceModelForClient(JAXWSLightSupport supp, JaxWsService client) throws IOException, Exception {
        FileObject originalWsdlFolder = supp.getWsdlFolder(false);
        FileObject originalWsdlFO = originalWsdlFolder.getFileObject(client.getLocalWsdl());

        if ((originalWsdlFO != null) && (originalWsdlFO.isValid())) {
            return WSITModelSupport.getModelFromFO(originalWsdlFO, true);
        }
        return null;
    }
    
    private static WSDLModel getModelForServiceFromWsdl(JAXWSLightSupport supp, JaxWsService service) throws IOException, Exception {
        String wsdlLocation = service.getLocalWsdl();
        FileObject wsdlFO = supp.getWsdlFolder(false).getFileObject(wsdlLocation);
        return WSITModelSupport.getModelFromFO(wsdlFO, true);
    }

    public static boolean isMavenProject(Project p) {
        WsitProvider provider = p.getLookup().lookup(WsitProvider.class);
        if (provider instanceof MavenWsitProvider) {
            return true;
        }
        return false;
    }
    
}
