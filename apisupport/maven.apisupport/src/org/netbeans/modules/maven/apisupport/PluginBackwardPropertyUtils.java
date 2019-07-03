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

package org.netbeans.modules.maven.apisupport;

import org.apache.maven.project.MavenProject;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;

public class PluginBackwardPropertyUtils {

    static String[] getPluginPropertyList(@NonNull Project prj, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        String[] propertielist = PluginPropertyUtils.getPluginPropertyList(prj, MavenNbModuleImpl.GROUPID_APACHE,
                MavenNbModuleImpl.NBM_PLUGIN, //NOI18N
                multiproperty, singleproperty, goal); //NOI18N
        if (propertielist == null) {
            propertielist = PluginPropertyUtils.getPluginPropertyList(prj, MavenNbModuleImpl.GROUPID_MOJO,
                    MavenNbModuleImpl.NBM_PLUGIN, //NOI18N
                    multiproperty, singleproperty, goal); //NOI18N
        }
        return propertielist;

    }

    static String getPluginProperty(@NonNull Project prj, @NonNull String parameter, @NullAllowed String goal, @NullAllowed String expressionProperty) {
        String propertielist = PluginPropertyUtils.getPluginProperty(prj, MavenNbModuleImpl.GROUPID_APACHE,
                MavenNbModuleImpl.NBM_PLUGIN, //NOI18N
                parameter, goal, expressionProperty); //NOI18N
        if (propertielist == null) {
            propertielist = PluginPropertyUtils.getPluginProperty(prj, MavenNbModuleImpl.GROUPID_MOJO,
                    MavenNbModuleImpl.NBM_PLUGIN, //NOI18N
                    parameter, goal, expressionProperty); //NOI18N
        }
        return propertielist;
    }

    static <T> T getPluginPropertyBuildable(@NonNull Project prj,
            @NullAllowed String goal, @NonNull PluginPropertyUtils.ConfigurationBuilder<T> builder) {
        T pluginPropertyBuildable = PluginPropertyUtils.getPluginPropertyBuildable(prj, MavenNbModuleImpl.GROUPID_APACHE,
                MavenNbModuleImpl.NBM_PLUGIN, goal, builder);
        if (pluginPropertyBuildable == null) {
            pluginPropertyBuildable = PluginPropertyUtils.getPluginPropertyBuildable(prj, MavenNbModuleImpl.GROUPID_MOJO,
                    MavenNbModuleImpl.NBM_PLUGIN, goal, builder);
        }
        return pluginPropertyBuildable;
    }

    static Plugin findPluginFromBuild(Build bld) {
        Plugin plg = bld.findPluginById(MavenNbModuleImpl.GROUPID_APACHE, MavenNbModuleImpl.NBM_PLUGIN);
        if (plg == null) {
            plg = bld.findPluginById(MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN);
        }
        return plg;
    }

    static Plugin findPluginFromPluginManagement(PluginManagement pm) {
        Plugin plg = pm.findPluginById(MavenNbModuleImpl.GROUPID_APACHE, MavenNbModuleImpl.NBM_PLUGIN);
        if (plg == null) {
            plg = pm.findPluginById(MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN);
        }
        return plg;
    }

    static String getPluginVersion(MavenProject prj) {
        String version = PluginPropertyUtils.getPluginVersion(prj, MavenNbModuleImpl.GROUPID_APACHE, MavenNbModuleImpl.NBM_PLUGIN);
        if (version == null) {
            version = PluginPropertyUtils.getPluginVersion(prj, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN);
        }
        return version;
    }

}
