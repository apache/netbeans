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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.cache.SubProjectDiskCache;
import java.io.File;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleSettings;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Laszlo Kishalmi
 */
@ServiceProvider(service = ProjectFactory.class, position = 0)
public final class NbGradleProjectFactory implements ProjectFactory2 {

    @Override
    public ProjectManager.Result isProject2(FileObject dir) {
        if (!isProject(dir)) {
            return null;
        }
        // project display name can be only safely determined if the project is loaded
        return isProject(dir) ? new ProjectManager.Result(
                null, NbGradleProject.GRADLE_PROJECT_TYPE, NbGradleProject.getIcon()) : null;
    }

    @Override
    public boolean isProject(FileObject dir) {
        return isProjectCheck(dir, GradleSettings.getDefault().isPreferMaven());
    }

    static boolean isProjectCheck(FileObject dir, final boolean preferMaven) {
        if (dir == null || FileUtil.toFile(dir) == null) {
            return false;
        }
        FileObject pom = dir.getFileObject("pom.xml"); //NOI18N
        if (pom != null && pom.isData()) {
            if (preferMaven) {
                return false;
            }
            final FileObject parent = dir.getParent();
            if (parent != null && parent.getFileObject("pom.xml") != null) { // NOI18N
                return isProjectCheck(parent, preferMaven);
            }
        }
        File suspect = FileUtil.toFile(dir);
        GradleFiles files = new GradleFiles(suspect);
        if (files.isRootProject() || files.isBuildSrcProject()) return true;
        
        if ((files.getSettingsScript() != null) && !files.isBuildSrcProject()) {
            SubProjectDiskCache spCache = SubProjectDiskCache.get(files.getRootDir());
            SubProjectDiskCache.SubProjectInfo data = spCache.loadData();
            if (data != null) {
                // Use the cached sub-project data, even if it's invalid,
                // it may have better results, than the heuristics
                return data.getProjectPath(suspect) != null;
            } else {
                // No cached info available, use heuristics.
                return files.isProject();
            }
        } else {
            return false;
        }
    }

    @Override
    public Project loadProject(FileObject dir, ProjectState ps) throws IOException {
        if (!isProject(dir)) {
            return null;
        }
        NbGradleProjectImpl prj = new NbGradleProjectImpl(dir, ps);
        prj.getGradleProject();
        return prj;
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {
    }

}
