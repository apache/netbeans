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

package org.netbeans.modules.maven.j2ee.web;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.j2ee.J2eeMavenSourcesImpl;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Janicek
 */
public final class WebProjectUtils {
    
    private WebProjectUtils() {
    }

    /**
     * Returns {@link FileObject} corresponding to the document base of the given
     * {@link Project} or {@code null} if nothing was found. 
     * 
     * @param project for which we want to get document base
     * @return document base of the given project or null if nothing was found
     */
    @CheckForNull
    public static FileObject getDocumentBase(Project project) {
        Sources srcs = ProjectUtils.getSources(project);
        SourceGroup[] grp = srcs.getSourceGroups(J2eeMavenSourcesImpl.TYPE_DOC_ROOT);
        
        if (grp.length > 0) {
            return grp[0].getRootFolder();
        }
        return null;
    }

    /**
     * Simplifies usage of {@link PluginPropertyUtils} for Web based project's.
     * <p>
     * Use this method in case if you want to check for configuration property in maven-war-plugin
     * with <i>war</i> goal.
     * </p>
     *
     * @param project where we want to evaluate given property
     * @param property the name of the plugin parameter to look for
     * @return value of the property
     */
    public static String getPluginProperty(@NonNull Project project, @NonNull String property) {
        return PluginPropertyUtils.getPluginProperty(
                project,
                Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR,
                property,
                "war", // NOI18N
                null);
    }

    /**
     * Simplifies usage of {@link PluginPropertyUtils} for Web based project's.
     * <p>
     * Use this method in case if you want to check for configuration property in maven-war-plugin with
     * <i>war</i> goal and when you need to use specific {@link PluginPropertyUtils.ConfigurationBuilder}.
     * </p>
     *
     * @param <T> type of our {@link PluginPropertyUtils.ConfigurationBuilder}
     * @param project where we want to evaluate given property
     * @param property the name of the plugin parameter to look for
     * @return value of the property
     */
    public static <T> T getPluginProperty(@NonNull Project project, @NonNull PluginPropertyUtils.ConfigurationBuilder<T> config) {
        return PluginPropertyUtils.getPluginPropertyBuildable(
                project,
                Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_WAR,
                "war", // NOI18N
                config);
    }
}
