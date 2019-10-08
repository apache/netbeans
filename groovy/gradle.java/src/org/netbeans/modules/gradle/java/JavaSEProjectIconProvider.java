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
package org.netbeans.modules.gradle.java;

import java.awt.Image;
import java.util.Set;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.ProjectIconProvider;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.ImageUtilities;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = ProjectIconProvider.class, projectTypes = {
    @ProjectType(id = NbGradleProject.GRADLE_PROJECT_TYPE, position = 1000)
})
public class JavaSEProjectIconProvider implements ProjectIconProvider {

    @StaticResource
    private static final String GRADLE_JAVASE_ICON = "org/netbeans/modules/gradle/java/resources/javaseProjectIcon.png"; //NOI18

    @StaticResource
    private static final String APPLICATION_BADGE = "org/netbeans/modules/gradle/java/resources/application-badge.png"; //NOI18

    final Project project;

    public JavaSEProjectIconProvider(Project project) {
        this.project = project;
    }

    @Override
    public Image getIcon() {
        Image ret = null;
        Set<String> plugins = GradleBaseProject.get(project).getPlugins();
        if (plugins.contains("java")) {                 //NOI18N
            ret = ImageUtilities.loadImage(GRADLE_JAVASE_ICON);
            if (plugins.contains("application")) {      //NOI18N
               Image badge = ImageUtilities.loadImage(APPLICATION_BADGE);
               ret = ImageUtilities.mergeImages(ret, badge, 8, 8);
            }
        }
        return  ret;
    }

    @Override
    public boolean isGradleBadgeRequested() {
        return false;
    }

}
