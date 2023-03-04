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
/*
 * PlatformUtil.java
 *
 * Created on April 18, 2006, 2:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.maven.jaxws;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;


/**
 *
 * @author mkuchtiak
 */
public class WSStackUtils {

    private Project project;
    private J2eePlatform j2eePlatform;
    
    /**
     * this string constant is usd as ServerInstanceID in case maven
     * project has no server appointed.
     */
    public static final String DEVNULL = "DEV-NULL"; //NOI18N

    /** Creates a new instance of WSStackUtils */
    public WSStackUtils(Project project) {
        this.project = project;
        this.j2eePlatform = getJ2eePlatform(project);
    }

    private J2eePlatform getJ2eePlatform(Project project){
        J2eeModuleProvider provider = project.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null){
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && !serverInstanceID.equals(DEVNULL)) {
                try {
                    return Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Failed to find J2eePlatform", ex);
                }
            }
        }
        return null;
    }

     public boolean isWsitSupported() {
        if (j2eePlatform != null) {
            WSStack<JaxWs> wsStack = JaxWsStackSupport.getJaxWsStack(j2eePlatform);
            return wsStack != null && wsStack.isFeatureSupported(JaxWs.Feature.WSIT);
        }
        return false;
     }

     public boolean isJsr109Supported() {
        if(j2eePlatform != null){
            WSStack<JaxWs> wsStack = JaxWsStackSupport.getJaxWsStack(j2eePlatform);
            return wsStack != null && wsStack.isFeatureSupported(JaxWs.Feature.JSR109);
        } else {
            // by default return true if j2eePlatform (server) isn't specified
            return true;
        }
    }

    public boolean isJsr109OldSupported() {
        if(j2eePlatform != null && getServerType(project) == ServerType.GLASSFISH) {
            return true;
//            WSStack wsStack = getWsStack(WSStack.STACK_JAX_RPC);
//            return wsStack != null && wsStack.getSupportedTools().contains(WSStack.TOOL_WSCOMPILE);
        }
        return false;
    }

//    public boolean hasJAXWSLibrary() {
//        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
//        FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N
//        return wsimportFO != null;
//    }

    public static ServerType getServerType(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null || 
            j2eeModuleProvider.getServerInstanceID() == null ||
            WSStackUtils.DEVNULL.equals(j2eeModuleProvider.getServerInstanceID())) {
            return ServerType.NOT_SPECIFIED;
        }
        String serverId = j2eeModuleProvider.getServerID();
        if (serverId.startsWith("Tomcat")) return ServerType.TOMCAT; //NOI18N
        else if (serverId.equals("J2EE")) return ServerType.GLASSFISH; //NOI18N
        else if (serverId.startsWith("gfv3")) return ServerType.GLASSFISH_V3; //NOI18N
        else if (serverId.equals("GlassFish")) return ServerType.GLASSFISH; //NOI18N
        else if (serverId.equals("APPSERVER")) return ServerType.GLASSFISH; //NOI18N
        else if (serverId.equals("JavaEE")) return ServerType.GLASSFISH; //NOI18N
        else if (serverId.startsWith("JBoss")) return ServerType.JBOSS; //NOI18N
        else if (serverId.startsWith("WebLogic")) return ServerType.WEBLOGIC; //NOI18N
        else if (serverId.startsWith("WebSphere")) return ServerType.WEBSPHERE; //NOI18N
        else return ServerType.UNKNOWN;
    }

    public ServerType getServerType() {
        return getServerType(project);
    }

    public <T> WSStack<T> getWsStack(Class<T> stackDescriptor) {
        if (j2eePlatform != null) {
            return WSStack.findWSStack(j2eePlatform.getLookup(), stackDescriptor);
        }
        return null;
    }
}


