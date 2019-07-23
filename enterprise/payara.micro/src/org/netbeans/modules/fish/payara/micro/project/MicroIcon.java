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
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROJECT_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RELOAD_ICON;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RESTART_ICON;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.j2ee.ui.EEIcons.WarIcon;
import org.netbeans.modules.maven.spi.nodes.SpecialIcon;
import org.netbeans.spi.project.ProjectServiceProvider;
import static org.openide.util.ImageUtilities.loadImageIcon;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
@ProjectServiceProvider(
        service = SpecialIcon.class,
        projectType = MAVEN_WAR_PROJECT_TYPE
)
public class MicroIcon extends WarIcon {

    private Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public Icon getIcon() {
        MicroApplication microApplication = MicroApplication.getInstance(project);
        String icon = PROJECT_ICON;
        if (microApplication == null) {
            return super.getIcon();
        } else if (microApplication.isLoading()) {
            icon = RELOAD_ICON;
        } else if (microApplication.isBuilding() && microApplication.getBuildActionType() != null) {
            icon = microApplication.getBuildActionType().getIcon();
        } else if (microApplication.isRunning()) {
            if (microApplication.getRunningInstanceCount() > 1) {
                icon = RESTART_ICON;
            } else if (microApplication.getRunActionType() != null) {
                icon = microApplication.getRunActionType().getIcon();
            }
        }       
        return loadImageIcon(icon, true);
    }

}
