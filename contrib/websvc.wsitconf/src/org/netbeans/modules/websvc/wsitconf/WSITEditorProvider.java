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
package org.netbeans.modules.websvc.wsitconf;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Grebac
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.websvc.spi.wseditor.WSEditorProvider.class)
public class WSITEditorProvider implements WSEditorProvider {
    
    /**
     * Creates a new instance of WSITEditorProvider
     */
    public WSITEditorProvider () {}

    public WSEditor createWSEditor(Lookup nodeLookup) {
        FileObject srcRoot = nodeLookup.lookup(FileObject.class);
        if (srcRoot != null) {
            Project prj = FileOwnerQuery.getOwner(srcRoot);
            JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                return new WSITEditor(jaxWsModel);
            } else {
                JaxWsService service = nodeLookup.lookup(JaxWsService.class);
                if (service != null) {
                    JAXWSLightSupport jaxWsSupport = nodeLookup.lookup(JAXWSLightSupport.class);
                    if (jaxWsSupport != null) {
                        return new MavenWSITEditor(jaxWsSupport, service, prj);
                    } else {
                        jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(srcRoot);
                        if (jaxWsSupport != null) {
                            return new MavenWSITEditor(jaxWsSupport, service, prj);
                        }
                    }
                }
            }
        } else {
            JaxWsService service = nodeLookup.lookup(JaxWsService.class);
            JAXWSLightSupport jaxWsSupport = nodeLookup.lookup(JAXWSLightSupport.class);
            if ((service != null) && (jaxWsSupport != null)) {
                FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
                if (wsdlFolder != null) {
                    Project prj = FileOwnerQuery.getOwner(wsdlFolder);
                    return new MavenWSITEditor(jaxWsSupport, service, prj);
                }
            }
        }
        return null;
    }

    public boolean enable(Node node) {
        Client client = node.getLookup().lookup(Client.class);
        if (client != null) {
            return true;
        }
        Service service = node.getLookup().lookup(Service.class);
        if (service != null) {
            return true;
        }
        JaxWsService jaxService = node.getLookup().lookup(JaxWsService.class);
        if (jaxService != null) {
            return true;
        }
        return false;
    }
}
