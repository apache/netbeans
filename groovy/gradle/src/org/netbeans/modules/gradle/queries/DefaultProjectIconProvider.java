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

package org.netbeans.modules.gradle.queries;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.ProjectIconProvider;
import java.awt.Image;
import java.util.Set;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ProjectIconProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class DefaultProjectIconProvider implements ProjectIconProvider {

    @StaticResource
    private static final String GRADLE_ICON = "org/netbeans/modules/gradle/resources/gradle.png"; //NOI18

    @StaticResource
    private static final String GRADLE_JAVASE_ICON = "org/netbeans/modules/gradle/resources/javaseProjectIcon.png"; //NOI18

    @StaticResource
    private static final String GRADLE_WEB_ICON = "org/netbeans/modules/gradle/resources/webProjectIcon.png"; //NOI18
    
    @StaticResource
    private static final String APPLICATION_BADGE = "org/netbeans/modules/gradle/resources/application-badge.png"; //NOI18

    final Project project;

    public DefaultProjectIconProvider(Project project) {
        this.project = project;
    }
    
    @Override
    public Image getIcon() {
        Set<String> plugins = GradleBaseProject.get(project).getPlugins();
        String iconResource = plugins.contains("java") ? GRADLE_JAVASE_ICON: GRADLE_ICON;
        iconResource = plugins.contains("war") ? GRADLE_WEB_ICON : iconResource;
        Image ret = ImageUtilities.loadImage(iconResource);
        
        if (plugins.contains("application")) {
            Image badge = ImageUtilities.loadImage(APPLICATION_BADGE);
            ret = ImageUtilities.mergeImages(ret, badge, 8, 8);
        }
        return ret;
    }

    @Override
    public boolean isGradleBadgeRequested() {
        return false;
    }

    
}
