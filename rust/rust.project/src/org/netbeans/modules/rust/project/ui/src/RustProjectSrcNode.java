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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.modules.rust.project.ui.RustProjectRootNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Responsible for showing the sources of a Rust project. We reject some files
 * (the "target" directory, or the "Cargo.toml" file, for instance. We also
 * decorate "members" of Rust workspaces with the Rust project icon, for
 * instance.
 */
@NbBundle.Messages({
    "SOURCES=Sources",})
public final class RustProjectSrcNode extends FilterNode {

//    /**
//     * Lists all files and subfolders of the RustProject main directory,
//     * excluding those folders that correspond to workspace members, and
//     * excluding the "Cargo.toml" file. These workspace members are included in
//     * another "Workspace" node.
//     */
//    private static final class RustProjectSrcNodeChildren
//            extends Children.Keys<FileObject>
//            implements ChangeListener, Predicate<FileObject>, Comparator<FileObject> {
//
//        private final RustProject project;
//        private final Sources sources;
//
//        RustProjectSrcNodeChildren(RustProject project) {
//            this.project = project;
//            this.sources = ProjectUtils.getSources(project);
//        }
//
//        @Override
//        public void stateChanged(ChangeEvent e) {
//            SwingUtilities.invokeLater(() -> {
//                refreshSources();
//            });
//        }
//
//        @Override
//        protected void removeNotify() {
//            this.sources.removeChangeListener(this);
//            super.removeNotify();
//        }
//
//        @Override
//        protected void addNotify() {
//            super.addNotify();
//            this.sources.addChangeListener(this);
//            refreshSources();
//        }
//
//        @Override
//        public boolean test(FileObject file) {
//            String name = file.getNameExt();
//            boolean valid = !name.startsWith(".");
//            valid &= !name.equals("Cargo.toml");
//            valid &= !name.equals("target");
//            return valid;
//        }
//
//        @Override
//        public int compare(FileObject a, FileObject b) {
//            boolean isFA = a.isFolder();
//            boolean isFB = b.isFolder();
//
//            if (isFA) {
//                if (isFB) {
//                    return a.getName().compareTo(b.getName());
//                }
//                return -1;
//            }
//            if (isFB) {
//                return 1;
//            }
//            return a.getName().compareTo(b.getName());
//        }
//
//        private void refreshSources() {
//            FileObject[] children = project.getProjectDirectory().getChildren();
//            List<FileObject> filesAndFolders
//                    = Arrays.stream(children).filter(this).sorted(this).collect(Collectors.toList());
//            setKeys(filesAndFolders);
//        }
//
//        @Override
//        protected Node[] createNodes(FileObject fo) {
//            if (fo.isFolder()) {
//                ProjectManager.Result r = ProjectManager.getDefault().isProject2(fo);
//                if (r != null) {
//                    // Check if this is a Rust project
//                    if (r.getProjectType().equals(RustProjectAPI.RUST_PROJECT_KEY)) {
//                        try {
//                            Project p = ProjectManager.getDefault().findProject(fo);
//                            RustProject rp = p.getLookup().lookup(RustProject.class);
//                            return new Node[]{new RustProjectRootNode(rp)};
//                        } catch (Exception ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                    // Any other kind of project
//                    return new Node[]{new DecoratedProjectNode(project, fo, r)};
//                }
//            }
//            try {
//                return new Node[]{DataObject.find(fo).getNodeDelegate().cloneNode()};
//            } catch (DataObjectNotFoundException ex) {
//                Exceptions.printStackTrace(ex);
//                return new Node[0];
//            }
//        }
//    }

    public static final String NAME = "rust-src"; // NOI18N

    private final RustProject project;

    public RustProjectSrcNode(RustProject project) {
        this(project, DataFolder.findFolder(project.getProjectDirectory()));
    }

    private RustProjectSrcNode(RustProject project, DataFolder projectFolder) {
        this(project, projectFolder, projectFolder.getNodeDelegate());
    }

    private RustProjectSrcNode(RustProject project, DataFolder projectFolder, Node node) {
        super(node,
                Children.create(new DecoratedDataFolderChildren(project, projectFolder), true),
                new ProxyLookup(node.getLookup(),
                        Lookups.fixed(
                                project,
                                project.getProjectDirectory(),
                                project.getCargoTOML()
                        )));
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
