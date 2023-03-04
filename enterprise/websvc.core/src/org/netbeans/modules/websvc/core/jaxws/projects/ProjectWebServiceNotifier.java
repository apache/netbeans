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
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.WebServiceNotifier;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;


/**
 *
 * @author mkuchtiak
 */
@ProjectServiceProvider(service=WebServiceNotifier.class, projectType={
    "org-netbeans-modules-web-project",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-j2ee-clientproject"
})
public class ProjectWebServiceNotifier implements WebServiceNotifier {
    private static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance"; //NOI18N
    
    private Project proj;
    public ProjectWebServiceNotifier(Project proj) {
        this.proj=proj;
    }

    /** Notifies that web service was added */
    public void serviceAdded(String serviceName, String implementationClass) {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(proj.getProjectDirectory());
        if (jaxWsSupport!=null) jaxWsSupport.addService(serviceName, implementationClass, isJsr109Supported());
    }

    /** Notifies that web service was removed */
    public void serviceRemoved(String serviceName) {
        JAXWSSupport jaxWsSupport = JAXWSSupport.getJAXWSSupport(proj.getProjectDirectory());
        if (jaxWsSupport!=null) jaxWsSupport.serviceFromJavaRemoved(serviceName);
    }

    private boolean isJsr109Supported() {
        JaxWsModel jaxWsModel = proj.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null && jaxWsModel.getJsr109() != null) {
            return jaxWsModel.getJsr109();
        }

        EditableProperties projectProperties = null;
        try {
            projectProperties = WSUtils.getEditableProperties(proj, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        } catch (IOException ex) {
            
        }
        boolean jsr109Supported = false;

        if (projectProperties!=null) {
            String serverInstance = projectProperties.getProperty(J2EE_SERVER_INSTANCE);
            if (serverInstance != null) {
                try {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getServerInstance(serverInstance).getJ2eePlatform();
                    WSStack<JaxWs> wsStack = JaxWsStackSupport.getJaxWsStack(j2eePlatform);
                    if (wsStack != null) {
                        jsr109Supported = wsStack.isFeatureSupported(JaxWs.Feature.JSR109);
                    }
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Failed to find J2eePlatform", ex);
                }
            }
        }
        
        if (!jsr109Supported) {
            if (jaxWsModel != null) {
                jsr109Supported = JaxWsUtils.askForSunJaxWsConfig(jaxWsModel);
            } else {
                jsr109Supported = true;
            }
        }

        return jsr109Supported;
    }

}
