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
package org.netbeans.modules.rust.project;

import java.io.IOException;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for Rust projects.
 */
@ServiceProvider(service = ProjectFactory.class)
public final class RustProjectFactory implements ProjectFactory2 {

    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (isProject2(projectDirectory) == null) {
            return null;
        }
        FileObject cargotoml = projectDirectory.getFileObject("Cargo.toml");
        CargoTOML cargo = null;
        try {
            cargo = CargoTOML.fromFileObject(cargotoml);
        } catch (IOException ex) {
            return null;
        }
        return new RustProject(projectDirectory, cargo, state);
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
        ((RustProject) project).save();
    }

    @Override
    public ProjectManager.Result isProject2(FileObject projectDirectory) {
        if (projectDirectory == null) {
            return null;
        }
        FileObject cargoToml = projectDirectory.getFileObject("Cargo.toml");
        if (cargoToml == null) {
            return null;
        }
        return new ProjectManager.Result("Rust project", RustProjectAPI.RUST_PROJECT_KEY, new ImageIcon(ImageUtilities.loadImage(RustProjectAPI.ICON))); // NOI18N0:w
    }

    @Override
    public boolean isProject(FileObject projectDirectory) {
        if (isProject2(projectDirectory) != null) {
            FileObject cargotoml = projectDirectory.getFileObject("Cargo.toml");
            return cargotoml.isData();
        }
        return false;
    }

}
