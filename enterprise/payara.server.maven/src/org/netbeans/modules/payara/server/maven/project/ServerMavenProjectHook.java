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

import org.netbeans.api.project.Project;
import static org.netbeans.modules.payara.server.maven.plugin.Constants.MAVEN_WAR_PROJECT_TYPE;
import org.netbeans.modules.maven.j2ee.ProjectHookImpl;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
@ProjectServiceProvider(
        service = ProjectOpenedHook.class,
        projectType = MAVEN_WAR_PROJECT_TYPE
)
public class ServerMavenProjectHook extends ProjectHookImpl {

    private final Project project;

    public ServerMavenProjectHook(Project project) {
        super(project);
        this.project = project;
    }

    @Override
    public void projectOpened() {
        // ServerMavenApplicationContent.getApplication() handles detection
        // and dynamically registers ServerMavenIcon when Maven model loads.
        ServerMavenApplication.getInstance(project);
    }

    @Override
    public void projectClosed() {
        ServerMavenApplication app = ServerMavenApplication.getInstance(project);
        if (app != null) {
            app.stopAllRunningTasks();
        }
    }
}
