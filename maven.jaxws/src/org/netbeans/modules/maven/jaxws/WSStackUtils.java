/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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


