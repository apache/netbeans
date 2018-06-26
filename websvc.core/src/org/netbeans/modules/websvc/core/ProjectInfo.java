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
