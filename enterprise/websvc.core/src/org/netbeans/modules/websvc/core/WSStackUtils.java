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

package org.netbeans.modules.websvc.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.javaee.specs.support.api.JaxRpc;
import org.netbeans.modules.javaee.specs.support.api.JaxRpcStackSupport;
import org.netbeans.modules.javaee.specs.support.api.JaxWs;
import org.netbeans.modules.javaee.specs.support.api.JaxWsStackSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.wsstack.api.WSStack;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WSStackUtils {
    Project project;
    J2eePlatform j2eePlatform;
    
    /** Creates a new instance of WSStackUtils */
    public WSStackUtils(Project project) {
        this.project = project;
        this.j2eePlatform = getJ2eePlatform(project);
    }

    private J2eePlatform getJ2eePlatform(Project project){
        J2eeModuleProvider provider = (J2eeModuleProvider) project.getLookup().lookup(J2eeModuleProvider.class);
        if(provider != null){
            String serverInstanceID = provider.getServerInstanceID();
            if(serverInstanceID != null && serverInstanceID.length() > 0) {
                try {
                    return Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
                } catch (InstanceRemovedException ex) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "Failed to find J2eePlatform");
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
        }
        return false;
    }
    
    public boolean isJsr109OldSupported() {
        if (j2eePlatform != null) {
            WSStack<JaxRpc> wsStack = JaxRpcStackSupport.getJaxWsStack(j2eePlatform);
            return wsStack != null && wsStack.isFeatureSupported(JaxRpc.Feature.JSR109);
        }
        return false;
    }
    
    public boolean hasJAXWSLibrary() {
        SourceGroup[] sgs = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
        if ( classPath == null ){
            return false;
        }
        FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N
        return wsimportFO != null;
    }
    
    public static ServerType getServerType(Project project) {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider == null || j2eeModuleProvider.getServerInstanceID() == null) {
            return ServerType.NOT_SPECIFIED;
        }
        String serverId = j2eeModuleProvider.getServerID();
        if (serverId != null) {
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
        } else {
            return ServerType.NOT_SPECIFIED;
        }
        
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
    
    boolean isWebModule() {
        J2eeModuleProvider j2eeModuleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            return J2eeModule.Type.WAR.equals(j2eeModuleProvider.getJ2eeModule().getType());
        }
        return false;
    }

    public ErrorMessage getErrorMessage(WizardType wizardType) {
        switch (wizardType) {
            case WS:
                return getWSErrorMessage();
            case WS_FROM_WSDL:
                return getWSErrorMessage();
            case WS_CLIENT:
                break;
            default:
        }
        return null;
    }

    private ErrorMessage getWSErrorMessage() {
        ServerType serverType = getServerType();
        if (ProjectUtil.isJavaEE5orHigher(project)) {
            if (ServerType.GLASSFISH_V3 == serverType && !isWsitSupported()) {
                return new ErrorMessage(ErrorType.INFO,
                        NbBundle.getMessage(WSStackUtils.class, "MSG_NoMetroInstalled"), false);
            }
        } else {
            boolean noJsr109InWeb = isWebModule() && !isJsr109Supported() && !isJsr109OldSupported(); // e.g. Tomcat
            boolean jBoss = (ServerType.JBOSS == getServerType());
            if (!noJsr109InWeb && !jBoss && WebServicesSupport.getWebServicesSupport(project.getProjectDirectory()) == null) {
                return new ErrorMessage(ErrorType.ERROR,
                        NbBundle.getMessage(WSStackUtils.class, "MSG_NoJaxrpcPluginFound"));
            }
        }
        return null;
    }

    public static enum WizardType {
        WS,
        WS_FROM_WSDL,
        WS_CLIENT;
    }

    public static enum ErrorType {
        ERROR,
        INFO,
        WARNING;
    }

    public static class ErrorMessage {
        private ErrorType type;
        private String text;
        private boolean serious;

        public ErrorMessage(ErrorType type, String text) {
            this(type, text, true);
        }

        public ErrorMessage(ErrorType type, String text, boolean serious) {
            this.type = type;
            this.text = text;
            this.serious = serious;
        }

        public String getText() {
            return text;
        }

        public ErrorType getType() {
            return type;
        }

        public String getWizardMessageProperty() {
            switch (type) {
                case ERROR:
                    return WizardDescriptor.PROP_ERROR_MESSAGE;
                case INFO:
                    return WizardDescriptor.PROP_INFO_MESSAGE;
                case WARNING:
                    return WizardDescriptor.PROP_WARNING_MESSAGE;
                default:
                    return WizardDescriptor.PROP_ERROR_MESSAGE;
            }
        }

        public boolean isSerious() {
            return serious;
        }

    }

}


