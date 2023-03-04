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
package org.netbeans.modules.fish.payara.micro.project;

import static org.netbeans.modules.fish.payara.micro.plugin.Constants.DEBUG_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.MAVEN_WAR_PROJECT_TYPE;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROFILE_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.RUN_SINGLE_ACTION;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.WAR_PACKAGING;
import java.io.InputStream;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import static org.netbeans.api.project.ProjectUtils.getPreferences;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.HOT_DEPLOY;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.VERSION;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.execute.model.NetbeansActionMapping;
import org.netbeans.modules.maven.spi.actions.AbstractMavenActionsProvider;
import org.netbeans.modules.maven.spi.actions.MavenActionsProvider;
import static org.netbeans.spi.project.ActionProvider.COMMAND_DEBUG;
import static org.netbeans.spi.project.ActionProvider.COMMAND_PROFILE;
import static org.netbeans.spi.project.ActionProvider.COMMAND_RUN;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
@ProjectServiceProvider(
        service = MavenActionsProvider.class,
        projectType = MAVEN_WAR_PROJECT_TYPE
)
public class MicroActionsProvider implements MavenActionsProvider {

    @StaticResource
    private static final String ACTION_MAPPINGS = "org/netbeans/modules/fish/payara/micro/project/resources/action-mapping.xml";
        
    private final AbstractMavenActionsProvider actionsProvider = new AbstractMavenActionsProvider() {
        @Override
        protected InputStream getActionDefinitionStream() {
            return MicroActionsProvider.class
                    .getClassLoader()
                    .getResourceAsStream(ACTION_MAPPINGS);
        }

        @Override
        public boolean isActionEnable(String action, Project project, Lookup lookup) {
            NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
            final String packagingType = nbMavenProject.getPackagingType();
            if (!WAR_PACKAGING.equals(packagingType)) {
                return false;
            }
            switch (action) {
                case COMMAND_RUN:
                case COMMAND_DEBUG:
                case COMMAND_PROFILE:
                case RUN_SINGLE_ACTION:
                case DEBUG_SINGLE_ACTION:
                case PROFILE_SINGLE_ACTION:
                    break;
                default:
                    return false;
            }
            return MicroApplication.getInstance(project) != null;
        }

    };

    @Override
    public RunConfig createConfigForDefaultAction(String actionName, Project project, Lookup lookup) {
        MicroApplication microApplication = MicroApplication.getInstance(project);
        if (microApplication != null) {
            Preferences pref = getPreferences(project, MicroApplication.class, true);
            String microVersionText = pref.get(VERSION, "");
            Boolean hotDeploy = pref.getBoolean(HOT_DEPLOY, false);
            RunConfig config = actionsProvider.createConfigForDefaultAction(actionName, project, lookup);
            if (!microVersionText.isEmpty()) {
                config.setProperty("version.payara", microVersionText);
            }
            if(hotDeploy) {
                config.setProperty("hotDeploy", Boolean.TRUE.toString());
            }
            return config;
        }
        return null;
    }

    @Override
    public NetbeansActionMapping getMappingForAction(String actionName, Project project) {
        MicroApplication microApplication = MicroApplication.getInstance(project);
        if (microApplication != null) {
            return actionsProvider.getMappingForAction(actionName, project);
        }
        return null;
    }

    @Override
    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        MicroApplication microApplication = MicroApplication.getInstance(project);
        if (microApplication != null) {
            return actionsProvider.isActionEnable(action, project, lookup);
        }
        return false;
    }

    @Override
    public Set<String> getSupportedDefaultActions() {
        return actionsProvider.getSupportedDefaultActions();
    }

}