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
package org.netbeans.modules.fish.payara.micro.plugin;

import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.project.ActionProvider;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public interface Constants {

    J2eeModule.Type PROJECT_TYPE = J2eeModule.Type.WAR;

    String PROP_GROUP_ID = "groupId";
    String PROP_ARTIFACT_ID = "artifactId";
    String PROP_VERSION = "version";
    String PROP_PACKAGE = "package";

    String PROP_JAVA_EE_VERSION = "javaeeVersion";
    String PROP_PAYARA_MICRO_VERSION = "payaraMicroVersion";
    String PROP_AUTO_BIND_HTTP = "autoBindHttp";
    String PROP_CONTEXT_ROOT = "contextRoot";

    String VERSION = "version";

    String WAR_PACKAGING = "war";

    String MAVEN_WAR_PROJECT_TYPE = "org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR;

    String PAYARA_MICRO_MAVEN_PLUGIN = "fish.payara.maven.plugins:payara-micro-maven-plugin";

    String RELOAD_FILE = ".reload";
    String COMMAND_EXPLODE = "explode";

    String RESOURCES_GOAL = "resources:resources";
    String COMPILE_GOAL = "compiler:compile";
    String EXPLODED_GOAL = "war:exploded";
    String WAR_GOAL = "war:war";
    String STOP_GOAL = "payara-micro:stop";
    String START_GOAL = "payara-micro:start";

    String COMPILE_EXPLODE_ACTION = "micro-complie-explode";
    String EXPLODE_ACTION = "micro-explode";
    String STOP_ACTION = "micro-stop";
    String RUN_ACTION = ActionProvider.COMMAND_RUN;
    String DEBUG_ACTION = ActionProvider.COMMAND_DEBUG;
    String PROFILE_ACTION = ActionProvider.COMMAND_PROFILE;
    String RUN_SINGLE_ACTION = ActionProvider.COMMAND_RUN_SINGLE + ".deploy";
    String DEBUG_SINGLE_ACTION = ActionProvider.COMMAND_DEBUG_SINGLE + ".deploy";
    String PROFILE_SINGLE_ACTION = ActionProvider.COMMAND_PROFILE_SINGLE + ".deploy";

    String ARCHETYPE_GROUP_ID = "fish.payara.maven.archetypes";
    String ARCHETYPE_ARTIFACT_ID = "payara-micro-maven-archetype";
    String ARCHETYPE_REPOSITORY = "https://oss.sonatype.org/content/repositories/snapshots";

    @StaticResource
    String PROJECT_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro.png";

    @StaticResource
    String CLEAN_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-clean.png";

    @StaticResource
    String BUILD_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-build.png";

    @StaticResource
    String REBUILD_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-clean-build.png";

    @StaticResource
    String START_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-start.png";

    @StaticResource
    String RESTART_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-restart.png";

    @StaticResource
    String RELOAD_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-reload.png";

    @StaticResource
    String DEBUG_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-debug.png";

    @StaticResource
    String PROFILE_ICON = "org/netbeans/modules/fish/payara/micro/project/resources/payara-micro-profile.png";

    @StaticResource
    String POM_TEMPLATE = "org/netbeans/modules/fish/payara/micro/plugin/resources/pom.xml.ftl";

}
