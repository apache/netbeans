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
package org.netbeans.modules.rust.project.ui.src;

import java.awt.Image;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Responsible for holding all source folders of a Rust project. These folders
 * can be "src" (sources), "tests" (tests) and other folders if the project has
 * different workspaces.
 * 
 * @author antonio
 */
@NbBundle.Messages({
    "SOURCES=Sources",})
public final class RustProjectSrcNode extends AbstractNode {

    /**
     * Lists all files and subfolders of the RustProject main directory,
     * excluding those folders that correspond to workspace members, and
     * excluding the "Cargo.toml" file. These workspace members are included in
     * another "Workspace" node.
     */
    private static final class RustProjectSrcNodeChildren extends Children.Keys<File> implements ChangeListener, Predicate<File> {

        private final RustProject project;
        private final Sources sources;

        RustProjectSrcNodeChildren(RustProject project) {
            this.project = project;
            this.sources = ProjectUtils.getSources(project);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            SwingUtilities.invokeLater(() -> {
                refreshSources();
            });
        }

        @Override
        protected void removeNotify() {
            this.sources.removeChangeListener(this);
            super.removeNotify();
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            this.sources.addChangeListener(this);
            refreshSources();
        }

        @Override
        public boolean test(File file) {
            String name = file.getName();
            // The FileObject must be a folder
            boolean valid = file.isDirectory();
            // and must not be the "target" folder
            valid &= ! "target".equals(name); // NOI18N
            // ... and must not start with "." (such as ".github", or ".git")
            valid &= ! file.getName().startsWith("."); // NOI18N
            // ... and must not be one of the workspace members
            valid &= ! project.getCargoTOML().getWorkspace().keySet().contains(name);

            return valid;
        }

        private void refreshSources() {
            CargoTOML cargotoml = project.getCargoTOML();
            File projectDirectory = FileUtil.toFile(project.getProjectDirectory());
            List<File> filesAndFolders = Arrays.stream(projectDirectory.listFiles()).filter(this).collect(Collectors.toList());
            setKeys(filesAndFolders);
        }

        @Override
        protected Node[] createNodes(File key) {
            try {
                return new Node[]{DataObject.find(FileUtil.toFileObject(key)).getNodeDelegate()};
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
                return new Node[0];
            }
        }

    }

    public static final String NAME = "rust-src"; // NOI18N

    private final RustProject project;

    public RustProjectSrcNode(RustProject project) throws DataObjectNotFoundException {
        super(new RustProjectSrcNodeChildren(project), Lookups.fixed(
                project,
                project.getProjectDirectory(),
                project.getCargoTOML()
        ));
        this.project = project;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(RustProjectSrcNode.class, "SOURCES"); // NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return RustIconFactory.getSourceFolderIcon(true);
    }

    @Override
    public Image getIcon(int type) {
        return RustIconFactory.getSourceFolderIcon(false);
    }

}
