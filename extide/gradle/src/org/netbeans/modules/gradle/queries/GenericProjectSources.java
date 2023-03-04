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
package org.netbeans.modules.gradle.queries;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = Sources.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class GenericProjectSources implements Sources {
    private final Project project;

    public GenericProjectSources(Project project) {
        this.project = project;
    }
    @Override
    public SourceGroup[] getSourceGroups(String type) {
        return Sources.TYPE_GENERIC.equals(type)? new SourceGroup[]{new ProjectSourceGroup()} : new SourceGroup[0];
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    private final class ProjectSourceGroup implements SourceGroup {

        public ProjectSourceGroup() {
        }

        @Override
        public FileObject getRootFolder() {
            return project.getProjectDirectory();
        }

        @Override
        public String getName() {
            return ProjectUtils.getInformation(project).getName();
        }

        @Override
        public String getDisplayName() {
            return ProjectUtils.getInformation(project).getDisplayName();
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override
        public boolean contains(FileObject file) {
            FileObject rootFolder = getRootFolder();
            if (file != rootFolder && !FileUtil.isParentOf(rootFolder, file)) {
                return false;
            }
            if (file.isFolder() && file != rootFolder && ProjectManager.getDefault().isProject(file)) {
                // #67450: avoid actually loading the nested project.
                return false;
            }
            return FileOwnerQuery.getOwner(file) == project;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public String toString() {
            return "ProjectSourceGroup: " + getDisplayName();
        }
    }

}
