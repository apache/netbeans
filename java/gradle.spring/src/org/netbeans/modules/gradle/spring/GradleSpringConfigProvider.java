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

package org.netbeans.modules.gradle.spring;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import static org.netbeans.modules.gradle.java.api.GradleJavaSourceSet.MAIN_SOURCESET_NAME;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.spring.api.beans.SpringConstants;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileLocationProvider;
import org.netbeans.modules.spring.spi.beans.SpringConfigFileProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = {SpringConfigFileProvider.class, SpringConfigFileLocationProvider.class}, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GradleSpringConfigProvider implements SpringConfigFileProvider, SpringConfigFileLocationProvider {

    private final Project project;

    public GradleSpringConfigProvider(Project project) {
        this.project = project;
    }

    @Override
    public Set<File> getConfigFiles() {
        Set<File> ret = new HashSet<>();
        GradleJavaSourceSet ss = GradleJavaProject.get(project).getSourceSets().get(MAIN_SOURCESET_NAME); //NoI18N
        if (ss != null) {
            for (File resourcesDir : ss.getResourcesDirs()) {
                FileObject dir = FileUtil.toFileObject(resourcesDir);
                if (dir != null) {
                    collectConfigs(dir, false, ret);
                    dir = dir.getFileObject("spring"); //NOI18N
                    if (dir != null) {
                        collectConfigs(dir, true, ret);
                    }
                }
            }
        }
        return Collections.unmodifiableSet(ret);
    }

    @Override
    public FileObject getLocation() {
        FileObject ret = null;
        GradleJavaSourceSet ss = GradleJavaProject.get(project).getSourceSets().get(MAIN_SOURCESET_NAME); //NoI18N
        if (ss != null) {
            for (File resourcesDir : ss.getResourcesDirs()) {
                ret = FileUtil.toFileObject(resourcesDir);
                if (ret != null) {
                    break;
                }
            }
        }
        return ret;
    }
    private void collectConfigs(FileObject dir, boolean recursive, final Set<File> configFiles) {
        for (FileObject fo : dir.getChildren()) {
            if (SpringConstants.CONFIG_MIME_TYPE.equals(fo.getMIMEType())) {
                configFiles.add(FileUtil.toFile(fo));
            }
            if (recursive && fo.isFolder()) {
                collectConfigs(fo, recursive, configFiles);
            }
        }

    }
}
