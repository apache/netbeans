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

package org.netbeans.modules.websvc.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxRpc;
import org.netbeans.modules.javaee.specs.support.api.JaxRpcStackSupport;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;

/**
 *
 * @author radko, mkuchtiak
 */
public class ProjectInfo {
    
    private Project project;
    private int projectType;
    
    public static final int JSE_PROJECT_TYPE = 0;
    public static final int WEB_PROJECT_TYPE = 1;
    public static final int EJB_PROJECT_TYPE = 2;
    public static final int CAR_PROJECT_TYPE = 3;
    
    private boolean jsr109Supported = false;
    private boolean jsr109oldSupported = false;
    private boolean wsgenSupported = false;
    private boolean wsimportSupported = false;
    private ServerType serverType;
    
    /** Creates a new instance of ProjectInfo */
    
    public ProjectInfo(Project project) {
        this.project=project;
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            if (serverInstanceId != null) {
                try {
                    J2eePlatform j2eePlatform = Deployment.getDefault().getServerInstance(serverInstanceId).getJ2eePlatform();
                    if (j2eePlatform != null) {
                        WSStack<JaxWs> wsStack = JaxWsStackSupport.getJaxWsStack(j2eePlatform);
                        if (wsStack != null) {
                            jsr109Supported = wsStack.isFeatureSupported(JaxWs.Feature.JSR109);
                            serverType = WSStackUtils.getServerType(project);
                            wsgenSupported = true;
                            wsimportSupported = true;
                        }
                        WSStack<JaxRpc> jaxRpcStack = JaxRpcStackSupport.getJaxWsStack(j2eePlatform);
                        if (jaxRpcStack != null) {
                            jsr109oldSupported = jaxRpcStack.isFeatureSupported(JaxRpc.Feature.JSR109);
                        }
                    }
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.FINE, "Failed to find J2eePlatform", ex);
                }
            }

            J2eeModule j2eeModule = j2eeModuleProvider.getJ2eeModule();
            J2eeModule.Type moduleType = j2eeModule.getType();
            if (J2eeModule.Type.EJB.equals(moduleType)) {
                projectType = EJB_PROJECT_TYPE;
            } else if (J2eeModule.Type.WAR.equals(moduleType)) {
                projectType = WEB_PROJECT_TYPE;
            } else if (J2eeModule.Type.CAR.equals(moduleType)) {
                projectType = CAR_PROJECT_TYPE;
            } else {
                projectType = JSE_PROJECT_TYPE;
            }
        } else {
            projectType = JSE_PROJECT_TYPE;
        }
    }
    
    public int getProjectType() {
        return projectType;
    }
    
    public Project getProject() {
        return project;
    }
    
    public boolean isJsr109Supported() {
        return jsr109Supported;
    }
    
    public boolean isJsr109oldSupported() {
        return jsr109oldSupported;
    }
    
    public boolean isWsgenSupported() {
        return wsgenSupported;
    }
    
    public boolean isWsimportSupported() {
        return wsimportSupported;
    }
    
    public ServerType getServerType() {
        return serverType;
    }
}
