/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
