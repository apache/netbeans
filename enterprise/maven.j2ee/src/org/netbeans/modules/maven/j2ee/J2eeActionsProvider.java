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

package org.netbeans.modules.maven.j2ee;

import java.io.InputStream;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 * j2ee specific defaults for project running and debugging..
 * @author mkleint
 */
@ProjectServiceProvider(
    service =
        MavenActionsProvider.class,
    projectType = {
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EAR,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
        "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT
    }
)
public class J2eeActionsProvider extends AbstractMavenActionsProvider {

    private static final String ACT_RUN = ActionProvider.COMMAND_RUN_SINGLE + ".deploy";
    private static final String ACT_DEBUG = ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy";
    private static final String ACT_PROFILE = ActionProvider.COMMAND_PROFILE_SINGLE + ".deploy";
    @StaticResource private static final String MAPPINGS = "org/netbeans/modules/maven/j2ee/webActionMappings.xml";

    @Override
    protected InputStream getActionDefinitionStream() {
        return J2eeActionsProvider.class.getClassLoader().getResourceAsStream(MAPPINGS);
    }

    @Override
    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        final String packagingType = project.getLookup().lookup(NbMavenProject.class).getPackagingType();
        if ("app-client".equals(packagingType)) {
            return false;
        }
        switch (action) {
            case ACT_DEBUG:
            case ACT_RUN:
            case ACT_PROFILE:
                //only enable for doc root fileobjects..
                return true;
            case ActionProvider.COMMAND_RUN:
            case ActionProvider.COMMAND_DEBUG:
            case ActionProvider.COMMAND_PROFILE:
                //performance, don't read the xml file to figure enablement..
                return true;
            default:
                return false;
        }
    }
}
