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

package org.netbeans.modules.gradle;

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
        return isProject(dir) ? new ProjectManager.Result(NbGradleProject.getIcon()) : null;
    }

    @Override
    public boolean isProject(FileObject dir) {
        boolean ret = FileUtil.toFile(dir) != null;
        if (ret) {
            FileObject pom = dir.getFileObject("pom.xml"); //NOI18N
            if ((pom != null) && pom.isData() && GradleSettings.getDefault().isPreferMaven()) {
                ret = false;
            } else {
                GradleFiles files = new GradleFiles(FileUtil.toFile(dir));
                ret = files.isProject();
            }
        }

        return ret;
    }

    @Override
    public Project loadProject(FileObject dir, ProjectState ps) throws IOException {
        return isProject(dir) ? new NbGradleProjectImpl(dir, ps) : null;
    }

    @Override
    public void saveProject(Project prjct) throws IOException, ClassCastException {
    }

}
