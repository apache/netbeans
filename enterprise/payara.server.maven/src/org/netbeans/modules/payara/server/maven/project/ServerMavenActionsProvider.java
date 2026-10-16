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

import static org.netbeans.modules.payara.server.maven.plugin.Constants.*;
import java.io.InputStream;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import static org.netbeans.api.project.ProjectUtils.getPreferences;
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
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
@ProjectServiceProvider(
        service = MavenActionsProvider.class,
        projectType = MAVEN_WAR_PROJECT_TYPE
)
public class ServerMavenActionsProvider implements MavenActionsProvider {

    @StaticResource
    static final String ACTION_MAPPINGS =
            "org/netbeans/modules/payara/server/maven/project/resources/action-mapping.xml";

    final AbstractMavenActionsProvider actionsProvider = new AbstractMavenActionsProvider() {
        @Override
        protected InputStream getActionDefinitionStream() {
            return ServerMavenActionsProvider.class
                    .getClassLoader()
                    .getResourceAsStream(ACTION_MAPPINGS);
        }

        @Override
        public boolean isActionEnable(String action, Project project, Lookup lookup) {
            NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
            if (nbMavenProject == null || !WAR_PACKAGING.equals(nbMavenProject.getPackagingType())) {
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
            return ServerMavenApplication.getInstance(project) != null;
        }
    };

    @Override
    public RunConfig createConfigForDefaultAction(String actionName, Project project, Lookup lookup) {
        if (ServerMavenApplication.getInstance(project) == null) {
            return null;
        }
        RunConfig rc = actionsProvider.createConfigForDefaultAction(actionName, project, lookup);
        if (rc == null) {
            return null;
        }
        Preferences pref = getPreferences(project, ServerMavenApplication.class, true);
        setIfNotEmpty(rc, MVNPROP_SERVER_VERSION, pref.get(PREF_SERVER_VERSION, ""));
        setIfNotEmpty(rc, MVNPROP_SERVER_PATH,    pref.get(PREF_SERVER_PATH,    ""));
        setIfNotEmpty(rc, MVNPROP_DOMAIN_NAME,    pref.get(PREF_DOMAIN_NAME,    ""));
        setIfNotEmpty(rc, MVNPROP_CONTEXT_ROOT,   pref.get(PREF_CONTEXT_ROOT,   ""));
        setIfNotEmpty(rc, MVNPROP_INSTANCE_NAME,  pref.get(PREF_INSTANCE_NAME,  ""));
        if (pref.getBoolean(PREF_REMOTE, false)) {
            rc.setProperty(MVNPROP_REMOTE, Boolean.TRUE.toString());
            setIfNotEmpty(rc, MVNPROP_HOST_NAME,  pref.get(PREF_HOST_NAME,  ""));
            setIfNotEmpty(rc, MVNPROP_ADMIN_PORT, pref.get(PREF_ADMIN_PORT, ""));
            setIfNotEmpty(rc, MVNPROP_ADMIN_USER, pref.get(PREF_ADMIN_USER, ""));
        }
        if (pref.getBoolean(PREF_EXPLODED,     true)) rc.setProperty(MVNPROP_EXPLODED,     Boolean.TRUE.toString());
        if (pref.getBoolean(PREF_HOT_DEPLOY,   false)) rc.setProperty(MVNPROP_HOT_DEPLOY,   Boolean.TRUE.toString());
        if (pref.getBoolean(PREF_AUTO_DEPLOY,  true)) rc.setProperty(MVNPROP_AUTO_DEPLOY,  Boolean.TRUE.toString());
        if (pref.getBoolean(PREF_LIVE_RELOAD,  true)) rc.setProperty(MVNPROP_LIVE_RELOAD,  Boolean.TRUE.toString());
        if (pref.getBoolean(PREF_KEEP_STATE,   true)) rc.setProperty(MVNPROP_KEEP_STATE,   Boolean.TRUE.toString());
        if (pref.getBoolean(PREF_TRIM_LOG,     true)) rc.setProperty(MVNPROP_TRIM_LOG,     Boolean.TRUE.toString());
        if (pref.getBoolean(PREF_DAEMON,       false)) rc.setProperty(MVNPROP_DAEMON,       Boolean.TRUE.toString());
        if (!pref.getBoolean(PREF_IGNORE_TEST, true))  rc.setProperty(MVNPROP_IGNORE_TEST,  Boolean.FALSE.toString());
        if (pref.getBoolean(PREF_AI_AGENT, false)) {
            rc.setProperty(MVNPROP_AI_AGENT, Boolean.TRUE.toString());
            String apiKey = pref.get(PREF_AI_API_KEY, "");
            if (apiKey.isEmpty()) {
                apiKey = System.getenv(ENV_AI_API_KEY);
            }
            setIfNotEmpty(rc, MVNPROP_AI_API_KEY, apiKey != null ? apiKey : "");
            setIfNotEmpty(rc, MVNPROP_AI_PROVIDER,     pref.get(PREF_AI_PROVIDER, ""));
            setIfNotEmpty(rc, MVNPROP_AI_PROVIDER_LOC, pref.get(PREF_AI_PROVIDER_LOC, ""));
            setIfNotEmpty(rc, MVNPROP_AI_MODEL,        pref.get(PREF_AI_MODEL, ""));
        }
        return rc;
    }

    @Override
    public NetbeansActionMapping getMappingForAction(String actionName, Project project) {
        if (ServerMavenApplication.getInstance(project) != null) {
            return actionsProvider.getMappingForAction(actionName, project);
        }
        return null;
    }

    @Override
    public boolean isActionEnable(String action, Project project, Lookup lookup) {
        if (ServerMavenApplication.getInstance(project) != null) {
            return actionsProvider.isActionEnable(action, project, lookup);
        }
        return false;
    }

    @Override
    public Set<String> getSupportedDefaultActions() {
        return actionsProvider.getSupportedDefaultActions();
    }

    private static void setIfNotEmpty(org.netbeans.modules.maven.api.execute.RunConfig rc,
            String key, String value) {
        if (value != null && !value.isEmpty()) {
            rc.setProperty(key, value);
        }
    }
}
