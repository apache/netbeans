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
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.rust.project.cargotoml.CargoTOML;
import org.netbeans.modules.rust.project.templates.privileged.RustPrivilegedTemplates;
import org.netbeans.modules.rust.project.ui.RustProjectLogicalViewProvider;
import org.netbeans.modules.rust.project.ui.customizer.RustProjectCustomizerProvider2;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A Rust project.
 *
 * @see
 * <a href="https://netbeans.apache.org/tutorials/nbm-projecttype.html">Project
 * Type Tutorial</a>
 * @see
 * <a href="https://bits.netbeans.org/dev/javadoc/org-netbeans-modules-projectapi/index.html?org/netbeans/spi/project/ProjectFactory.html">Project
 * API</a>
 * @see
 * <a href="https://bits.netbeans.org/dev/javadoc/org-netbeans-modules-projectapi/org/netbeans/api/project/Project.html#getLookup--">Project
 * Lookup Details</a>
 */
public final class RustProject implements Project {

    static final Logger LOG = Logger.getLogger(RustProject.class.getName());

    @StaticResource
    public static final String ICON = "org/netbeans/modules/rust/project/resources/rust-logo-2.png";

    /**
     * Used to register stuff in the "layer.xml" file.
     * For instance, under "/Projects/RUST_PROJECT_KEY/Customizer", for example.
     */
    public static final String RUST_PROJECT_KEY = "org-netbeans-modules-rust-project";

    private final FileObject projectDirectory;
    private final InstanceContent instanceContent;
    private final Lookup lookup;
    private final ProjectState state;
    private final RustProjectInformation information;
    private final CargoTOML cargotoml;

    public RustProject(FileObject projectDirectory, ProjectState state) throws IOException {
        this.projectDirectory = projectDirectory;
        this.instanceContent = new InstanceContent();
        this.state = state;
        FileObject cargotmolFile = projectDirectory.getFileObject("Cargo.toml"); // NOI18N
        this.cargotoml = new CargoTOML(cargotmolFile);
        this.information = new RustProjectInformation(this);
        // Prepare lookup
        instanceContent.add(information);
        instanceContent.add(cargotoml);
        instanceContent.add(new RustProjectLogicalViewProvider(this));
        instanceContent.add(new RustSources(this));
        instanceContent.add(new RustProjectActionProvider(this));
        instanceContent.add(new RustPrivilegedTemplates());
        instanceContent.add(this);
        instanceContent.add(new RustProjectCustomizerProvider2(this));
        this.lookup = new AbstractLookup(instanceContent);
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    public CargoTOML getCargoTOML() {
        return cargotoml;
    }

    public void save() throws IOException {
        // TODO: save project, including changes to Cargo.toml 
    }

}
