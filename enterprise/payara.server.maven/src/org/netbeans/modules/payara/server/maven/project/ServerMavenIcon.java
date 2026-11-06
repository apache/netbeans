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
package org.netbeans.modules.payara.server.maven.project;

import static org.netbeans.modules.payara.server.maven.plugin.Constants.PROJECT_ICON;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.RELOAD_ICON;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.RESTART_ICON;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.j2ee.ui.EEIcons.WarIcon;
import org.netbeans.modules.maven.spi.nodes.SpecialIcon;
import static org.openide.util.ImageUtilities.loadImageIcon;

/**
 * Provides the Payara Server Maven project icon.
 * <p>
 * Not registered globally — added to the project lookup dynamically by
 * {@link ServerMavenApplicationContent} only when the project is confirmed to
 * use the payara-server-maven-plugin. This ensures it never appears in plain
 * WAR or Micro project lookups.
 *
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
public class ServerMavenIcon extends WarIcon {

    private volatile Project project;

    public void setProject(Project project) {
        this.project = project;
    }

    @Override
    public Icon getIcon() {
        ServerMavenApplication application = ServerMavenApplication.getInstance(project);
        String icon = PROJECT_ICON;
        if (application == null) {
            return super.getIcon();
        } else if (application.isLoading()) {
            icon = RELOAD_ICON;
        } else if (application.isBuilding() && application.getBuildActionType() != null) {
            icon = application.getBuildActionType().getIcon();
        } else if (application.isRunning()) {
            if (application.getRunningInstanceCount() > 1) {
                icon = RESTART_ICON;
            } else if (application.getRunActionType() != null) {
                icon = application.getRunActionType().getIcon();
            }
        }
        return loadImageIcon(icon, true);
    }
}
