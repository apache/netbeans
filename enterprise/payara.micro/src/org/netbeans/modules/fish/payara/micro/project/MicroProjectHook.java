/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.fish.payara.micro.project;

import static org.netbeans.modules.fish.payara.micro.plugin.Constants.MAVEN_WAR_PROJECT_TYPE;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.spi.nodes.SpecialIcon;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
@ProjectServiceProvider(
        service = ProjectOpenedHook.class,
        projectType = MAVEN_WAR_PROJECT_TYPE
)
public class MicroProjectHook extends ProjectOpenedHook {

    private final Project project;

    public MicroProjectHook(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        if (MicroApplication.getInstance(project) != null) {
            addDeployOnSaveManager(project);
            updateMicroIcon();
        }
    }

    @Override
    public void projectClosed() {
        if (MicroApplication.getInstance(project) != null) {
            removeDeployOnSaveManager(project);
        }
    }

    private void updateMicroIcon() {
        SpecialIcon specialIcon = project.getLookup().lookup(SpecialIcon.class);
        MicroIcon microIcon;
        if (specialIcon instanceof MicroIcon) {
            microIcon = (MicroIcon) specialIcon;
        } else {
            return;
        }
        microIcon.setProject(project);
    }

    private void addDeployOnSaveManager(Project project) {
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (moduleProvider != null) {
            DeployOnSaveManager.getDefault().startListening(project, moduleProvider);
        }
    }

    private void removeDeployOnSaveManager(Project project) {
        J2eeModuleProvider moduleProvider = project.getLookup().lookup(J2eeModuleProvider.class);
        if (moduleProvider != null) {
            DeployOnSaveManager.getDefault().stopListening(project, moduleProvider);
        }
    }
}
