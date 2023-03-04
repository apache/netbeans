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
package org.netbeans.modules.web.clientproject.createprojectapi;

import java.awt.EventQueue;
import java.io.IOException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.netbeans.modules.web.clientproject.ui.customizer.ClientSideProjectProperties;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.openide.util.Parameters;

/**
 * Creates a Web Client Side project from scratch according to some initial configuration.
 *
 * @since 1.37
 * @author Martin Janicek
 */
public final class ClientSideProjectGenerator {

    private ClientSideProjectGenerator() {
    }

    /**
     * Creates a new empty Web Client Side project according to the given {@link CreateProjectProperties}.
     * <p>
     * <b>Warning:</b> This method should not be called in the UI thread.
     * @param properties used for project setup
     * @return project
     * @throws IOException in case something went wrong
     *
     * @since 1.37
     */
    @NonNull
    public static Project createProject(@NonNull CreateProjectProperties properties) throws IOException {
        Parameters.notNull("properties", properties); // NOI18N
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Cannot run in UI thread");
        }

        CommonProjectHelper h = ClientSideProjectUtilities.setupProject(properties.getProjectDir(), properties.getProjectName());

        Project project = FileOwnerQuery.getOwner(h.getProjectDirectory());
        assert project != null;

        ClientSideProject clientSideProject = project.getLookup().lookup(ClientSideProject.class);
        if (clientSideProject == null) {
            throw new IllegalStateException("HTML5 project needed but found " + project.getClass().getName()); //NOI18N
        }

        ClientSideProjectUtilities.initializeProject(clientSideProject,
                    properties.getSourceFolder(),
                    properties.getSiteRootFolder(),
                    properties.getTestFolder(),
                    properties.getTestSeleniumFolder());
        // js testing provider
        String jsTestingProvider = properties.getJsTestingProvider();
        if (jsTestingProvider != null) {
            ClientSideProjectUtilities.setJsTestingProvider(project, jsTestingProvider);
        }
        // platform provider
        String platformProvider = properties.getPlatformProvider();
        if (platformProvider != null) {
            ClientSideProjectUtilities.setPlatformProvider(project, platformProvider);
        }
        // autoconfigured?
        ClientSideProjectProperties projectProperties = new ClientSideProjectProperties(clientSideProject);
        boolean autoconfigured = properties.isAutoconfigured();
        if (autoconfigured) {
            projectProperties.setAutoconfigured(true);
        }
        String startFile = properties.getStartFile();
        if (startFile != null) {
            projectProperties.setStartFile(startFile);
        }
        String projectUrl = properties.getProjectUrl();
        if (projectUrl != null) {
            projectProperties.setProjectUrl(projectUrl);
        }
        projectProperties.save();
        // usage logging
        String siteRoot = properties.getSiteRootFolder();
        ClientSideProjectUtilities.logUsageProjectCreate(autoconfigured, null, siteRoot == null ? null : !siteRoot.startsWith("../"), // NOI18N
                siteRoot == null, platformProvider, autoconfigured);
        return project;
    }

}
