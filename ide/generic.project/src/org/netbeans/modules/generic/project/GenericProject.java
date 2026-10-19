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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
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
                new GenericSubprojectProvider(this),
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

    /**
     * Creates the marker file in the given directory if it does not
     * exist yet, returning the marker.
     */
    static FileObject createMarker(FileObject dir) throws IOException {
        FileObject marker = dir.getFileObject(MARKER_FILE);
        if (marker == null) {
            marker = dir.createData(MARKER_FILE);
            marker.setAttribute("displayName", dir.getNameExt()); // NOI18N
        }
        return marker;
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
            createMarker(project.getProjectDirectory());
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

    /**
     * Exposes the projects contained directly in the project directory
     * as subprojects. Adds "Open Required Projects" and "Open Selected Project" actions.
     */
    private static final class GenericSubprojectProvider implements SubprojectProvider {

        private final GenericProject prj;
        private final ChangeSupport changeSupport = new ChangeSupport(this);

        GenericSubprojectProvider(GenericProject prj) {
            this.prj = prj;
        }

        @Override
        public Set<? extends Project> getSubprojects() {
            Set<Project> subprojects = new LinkedHashSet<>();
            FileObject[] children = prj.getProjectDirectory().getChildren();
            if (children != null) {
                for (FileObject child : children) {
                    Project owner = FileOwnerQuery.getOwner(child);
                    if (owner != null && !owner.equals(prj)) {
                        subprojects.add(owner);
                    }
                }
            }
            return Collections.unmodifiableSet(subprojects);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            changeSupport.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            changeSupport.removeChangeListener(listener);
        }

    }

}
