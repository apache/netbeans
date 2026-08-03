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
package org.netbeans.modules.payara.server.maven.plugin;

import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ActionProvider;

/**
 * @author Gaurav Gupta <gaurav.gupta@azul.com>
 */
public interface Constants {

    String WAR_PACKAGING = "war";

    String MAVEN_WAR_PROJECT_TYPE = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR;

    String PAYARA_SERVER_MAVEN_PLUGIN = "fish.payara.maven.plugins:payara-server-maven-plugin";

    String PLUGIN_GROUP_ID    = "fish.payara.maven.plugins";
    String PLUGIN_ARTIFACT_ID = "payara-server-maven-plugin";

    String RUN_SINGLE_ACTION     = ActionProvider.COMMAND_RUN_SINGLE + ".deploy";
    String DEBUG_SINGLE_ACTION   = ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy";
    String PROFILE_SINGLE_ACTION = ActionProvider.COMMAND_PROFILE_SINGLE + ".deploy";

    // ── Preference keys (stored per-project via ProjectUtils.getPreferences) ──
    String PREF_SERVER_VERSION  = "payaraServerVersion";
    String PREF_SERVER_PATH     = "payaraServerPath";
    String PREF_DOMAIN_NAME     = "domainName";
    String PREF_CONTEXT_ROOT    = "contextRoot";
    String PREF_EXPLODED        = "exploded";
    String PREF_REMOTE          = "remote";
    String PREF_HOST_NAME       = "hostName";
    String PREF_ADMIN_PORT      = "adminPort";
    String PREF_ADMIN_USER      = "adminUser";
    String PREF_INSTANCE_NAME   = "instanceName";
    String PREF_HOT_DEPLOY      = "hotDeploy";
    String PREF_AUTO_DEPLOY     = "autoDeploy";
    String PREF_LIVE_RELOAD     = "liveReload";
    String PREF_KEEP_STATE      = "keepState";
    String PREF_TRIM_LOG        = "trimLog";
    String PREF_DAEMON          = "daemon";
    String PREF_IGNORE_TEST     = "ignoreTestChanges";

    // ── Maven property names (passed as -D arguments to the plugin) ──────────
    String MVNPROP_SERVER_VERSION  = "payara.server.version";
    String MVNPROP_SERVER_PATH     = "payara.server.path";
    String MVNPROP_DOMAIN_NAME     = "payara.domain.name";
    String MVNPROP_CONTEXT_ROOT    = "payara.context.root";
    String MVNPROP_EXPLODED        = "payara.exploded";
    String MVNPROP_REMOTE          = "payara.remote";
    String MVNPROP_HOST_NAME       = "payara.host.name";
    String MVNPROP_ADMIN_PORT      = "payara.admin.port";
    String MVNPROP_ADMIN_USER      = "payara.admin.user";
    String MVNPROP_INSTANCE_NAME   = "payara.instance.name";
    String MVNPROP_HOT_DEPLOY      = "payara.hot.deploy";
    String MVNPROP_AUTO_DEPLOY     = "payara.auto.deploy";
    String MVNPROP_LIVE_RELOAD     = "payara.live.reload";
    String MVNPROP_KEEP_STATE      = "payara.keep.state";
    String MVNPROP_TRIM_LOG        = "payara.trim.log";
    String MVNPROP_DAEMON          = "payara.daemon";
    String MVNPROP_IGNORE_TEST     = "payara.ignore.test.changes";
    String PREF_AI_AGENT            = "aiAgent";
    String PREF_AI_API_KEY          = "aiApiKey";
    String PREF_AI_PROVIDER         = "aiProvider";
    String PREF_AI_PROVIDER_LOC     = "aiProviderLocation";
    String PREF_AI_MODEL            = "aiModel";

    String ENV_AI_API_KEY            = "PAYARA_AI_API_KEY";

    String MVNPROP_AI_AGENT         = "payara.ai.agent";
    String MVNPROP_AI_API_KEY       = "payara.ai.api.key";
    String MVNPROP_AI_PROVIDER      = "payara.ai.provider";
    String MVNPROP_AI_PROVIDER_LOC  = "payara.ai.provider.location";
    String MVNPROP_AI_MODEL         = "payara.ai.model";

    @StaticResource
    String PROJECT_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven.png";

    @StaticResource
    String CLEAN_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-clean.png";

    @StaticResource
    String BUILD_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-build.png";

    @StaticResource
    String REBUILD_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-clean-build.png";

    @StaticResource
    String START_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-start.png";

    @StaticResource
    String RESTART_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-restart.png";

    @StaticResource
    String RELOAD_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-reload.png";

    @StaticResource
    String DEBUG_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-debug.png";

    @StaticResource
    String PROFILE_ICON = "org/netbeans/modules/payara/server/maven/project/resources/payara-server-maven-profile.png";
}
