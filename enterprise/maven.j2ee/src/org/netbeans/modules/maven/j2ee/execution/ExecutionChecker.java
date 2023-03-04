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
