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

package org.netbeans.modules.generic.project;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 * A generic project. Any directory containing the {@link #MARKER_FILE} is
 * recognized as a generic project. The logical view of the project simply shows
 * all files contained in the project directory.
 */
public final class GenericProject implements Project {

    public static final String MARKER_FILE = ".nbgp"; // NOI18N

    @StaticResource
    public static final String ICON = "org/netbeans/modules/generic/project/gp.svg"; // NOI18N

    private final FileObject projectDirectory;
    private final Lookup lookup;

    private GenericProject(FileObject projectDirectory) {
        this.projectDirectory = projectDirectory;
        this.lookup = Lookups.fixed(
                new GenericProjectLogicalViewProvider(this),
                new GenericProjectInfo(this),
                new SubprojectProviderImpl(this),
                this
        );
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    static boolean isProject(FileObject projectDirectory) {
        return projectDirectory != null
            && projectDirectory.getFileObject(MARKER_FILE) != null;
    }

    @ServiceProvider(service=ProjectFactory.class)
    public static final class GenericProjectFactory implements ProjectFactory {

        @Override
        public boolean isProject(FileObject projectDirectory) {
            return GenericProject.isProject(projectDirectory);
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (GenericProject.isProject(projectDirectory)) {
                return new GenericProject(projectDirectory);
            }
            return null;
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
            FileObject dir = project.getProjectDirectory();
            if (dir.getFileObject(MARKER_FILE) == null) {
                FileObject marker = dir.createData(MARKER_FILE);
                marker.setAttribute("displayName", dir.getNameExt());
            }
        }

    }

    private record GenericProjectInfo(GenericProject prj) implements ProjectInformation {

        @Override
        public String getName() {
            return prj.getProjectDirectory().getNameExt();
        }

        @Override
        public String getDisplayName() {
            return prj.getProjectDirectory().getNameExt();
        }

        @Override
        public Icon getIcon() {
            Icon icon = ImageUtilities.loadIcon(ICON);
            return icon != null ? icon : UIManager.getIcon("Nb.Explorer.Folder.icon"); // NOI18N
        }

        @Override
        public Project getProject() {
            return prj;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

    }

}
