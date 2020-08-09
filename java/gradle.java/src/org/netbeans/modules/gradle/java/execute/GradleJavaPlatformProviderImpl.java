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
package org.netbeans.modules.gradle.java.execute;

import java.io.File;
import java.io.FileNotFoundException;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.execute.GradleJavaPlatformProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Pair;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = GradleJavaPlatformProvider.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public final class GradleJavaPlatformProviderImpl implements GradleJavaPlatformProvider {

    final Project project;

    public GradleJavaPlatformProviderImpl(Project project) {
        this.project = project;
    }

    
    @Override
    public File getJavaHome() throws FileNotFoundException {
        Pair<String, JavaPlatform> platform = JavaRunUtils.getActivePlatform(project);
        if (platform.second() == null || !platform.second().isValid() || platform.second().getInstallFolders().isEmpty()) {
            throw new FileNotFoundException(platform.first());
        }
        File javaHome = FileUtil.toFile(platform.second().getInstallFolders().iterator().next());
        return javaHome;
    }

}
