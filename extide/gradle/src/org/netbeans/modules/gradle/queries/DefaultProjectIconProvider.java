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

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.ProjectIconProvider;
import java.awt.Image;
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

    final Project project;

    public DefaultProjectIconProvider(Project project) {
        this.project = project;
    }
    
    @Override
    public Image getIcon() {
        return  ImageUtilities.loadImage(GRADLE_ICON);
    }

    @Override
    public boolean isGradleBadgeRequested() {
        return false;
    }

    
}
