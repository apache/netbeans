/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee.execution;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.ExecutionContext;
import org.netbeans.modules.maven.api.execute.ExecutionResultChecker;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.OneTimeDeployment;
import org.netbeans.modules.maven.j2ee.ui.SelectAppServerPanel;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import static org.netbeans.modules.maven.j2ee.execution.Bundle.*;


@ProjectServiceProvider(
    service = {
        ExecutionResultChecker.class,
        PrerequisitesChecker.class
    }, projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR
    }
)
public class ExecutionChecker implements ExecutionResultChecker, PrerequisitesChecker {

    public static final String CLIENTURLPART = "netbeans.deploy.clientUrlPart"; //NOI18N
    public static final String DEV_NULL = "DEV-NULL"; //NOI18N


    private final Project project;
    

    public ExecutionChecker(Project prj) {
        project = prj;
    }


    @Override
    public void executionResult(RunConfig config, ExecutionContext res, int resultCode) {
        boolean depl = Boolean.parseBoolean(config.getProperties().get(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY));
        if (depl && resultCode == 0) {
            DeploymentHelper.perform(config, res);
        }
    }

    @Override
    public boolean checkRunConfig(RunConfig config) {
        if (!isSupported(config)) { // #234767
            return false;
        }

        boolean depl = Boolean.parseBoolean(config.getProperties().get(MavenJavaEEConstants.ACTION_PROPERTY_DEPLOY));
        if (depl) {
            J2eeModuleProvider provider = config.getProject().getLookup().lookup(J2eeModuleProvider.class);
            if (provider != null) {
                return SelectAppServerPanel.showServerSelectionDialog(project, provider, config);
            }
        }
        return true;
    }

    @NbBundle.Messages({
        "MSG_Server_No_Profiling=<html>The target server does not support profiling.<br><b>Choose a different server</b> in project properties.</html>",
        "MSG_Server_No_Debugging=<html>The target server does not support debugging.<br><b>Choose a different server</b> in project properties.</html>"
    })
    private boolean isSupported(RunConfig config) {
        String serverInstanceID = getServerInstanceID();
        if (serverInstanceID == null || serverInstanceID.equals(DEV_NULL)) {
            return true;
        }

        boolean debugmode = DeploymentHelper.isDebugMode(config);
        boolean profilemode = DeploymentHelper.isProfileMode(config);

        ServerInstance serverInstance = Deployment.getDefault().getServerInstance(serverInstanceID);
        try {
            if (serverInstance != null) {
                if (debugmode && !serverInstance.isDebuggingSupported()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(MSG_Server_No_Debugging(), NotifyDescriptor.WARNING_MESSAGE));
                    return false;
                }
                if (profilemode && !serverInstance.isProfilingSupported()) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(MSG_Server_No_Profiling(), NotifyDescriptor.WARNING_MESSAGE));
                    return false;
                }
            }
        } catch (InstanceRemovedException ex) {
            // If the instance was removed in the meantime, server is not set correctly
            return true;
        }
        return true;
    }

    private String getServerInstanceID() {
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        String serverInstanceID = null;

        // First check if the one-time deployment server is set
        OneTimeDeployment oneTimeDeployment = project.getLookup().lookup(OneTimeDeployment.class);
        if (oneTimeDeployment != null) {
            serverInstanceID = oneTimeDeployment.getServerInstanceId();
        }

        if (serverInstanceID == null && moduleProvider != null) {
            serverInstanceID = moduleProvider.getServerInstanceID();
        }
        return serverInstanceID;
    }
}
