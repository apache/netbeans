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


import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.jaxws.nodes.JaxWsClientNode;
import org.netbeans.modules.websvc.project.api.ServiceDescriptor;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.api.WebService.Type;
import org.netbeans.modules.websvc.project.spi.WebServiceImplementation;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class AntJAXWSClient implements WebServiceImplementation {
 
    private Client client;
    private Project prj;
    private JaxWsModel jaxWsModel;

    /** Constructor.
     *
     * @param service JaxWsService
     * @param prj project
     */
    public AntJAXWSClient(JaxWsModel jaxWsModel, Client client, Project prj) {
        this.jaxWsModel = jaxWsModel;
        this.client = client;
        this.prj = prj;
    }

    public String getIdentifier() {
         return client.getWsdlUrl();
    }

    public boolean isServiceProvider() {
        return false;
    }

    public Type getServiceType() {
        return WebService.Type.SOAP;
    }

    public ServiceDescriptor getServiceDescriptor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Node createNode() {
        return new JaxWsClientNode(jaxWsModel, client, prj.getProjectDirectory());
    }

}
