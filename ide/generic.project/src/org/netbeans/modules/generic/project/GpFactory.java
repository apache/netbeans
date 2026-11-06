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

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * The {@link ProjectFactory} for generic projects: recognizes a
 * directory by the marker file or the local-project recognition
 * attribute and instantiates {@link GenericProject}.
 */
@ServiceProvider(service=ProjectFactory.class)
public final class GpFactory implements ProjectFactory {

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
        if (project instanceof GenericProject gp && gp.isDeleted()) {
            // the project was explicitly deleted: never
            // resurrect the marker or the attribute
            return;
        }
        FileObject dir = project.getProjectDirectory();
        if (GenericProject.isAttributeProject(dir)) {
            // local project: recognition lives in the FileObject
            // attribute, never (re)create the marker file
            dir.setAttribute(GenericProject.MARKER_ATTRIBUTE, Boolean.TRUE);
        } else {
            GenericProject.createMarker(dir);
        }
    }
}
