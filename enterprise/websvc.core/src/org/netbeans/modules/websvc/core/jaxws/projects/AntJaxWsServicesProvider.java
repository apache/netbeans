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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.project.api.WebService;
import org.netbeans.modules.websvc.project.spi.WebServiceDataProvider;
import org.netbeans.modules.websvc.project.spi.WebServiceFactory;
import org.netbeans.spi.project.ProjectServiceProvider;

/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=WebServiceDataProvider.class, projectType={
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-j2ee-clientproject",
    "org-netbeans-modules-java-j2seproject"
})
public class AntJaxWsServicesProvider implements WebServiceDataProvider {
    private Project prj;

    /** Constructor.
     *
     * @param prj project
     * @param jaxWsSupport JAXWSLightSupport
     */
    public AntJaxWsServicesProvider(Project prj) {
        this.prj = prj;
    }

    public List<WebService> getServiceProviders() {
        List<WebService> webServices = new ArrayList<WebService>();
        JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            Service[] services = jaxWsModel.getServices();
            for (Service s : services) {
                webServices.add(WebServiceFactory.createWebService(new AntJAXWSService(jaxWsModel, s, prj)));
            }
        }
        return webServices;
    }

    public List<WebService> getServiceConsumers() {
        List<WebService> webServices = new ArrayList<WebService>();
        JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            Client[] clients = jaxWsModel.getClients();
            for (Client c : clients) {
                webServices.add(WebServiceFactory.createWebService(new AntJAXWSClient(jaxWsModel, c, prj)));
            }
        }
        return webServices;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            jaxWsModel.addPropertyChangeListener(pcl);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        JaxWsModel jaxWsModel = prj.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null) {
            jaxWsModel.removePropertyChangeListener(pcl);
        }
    }

}
