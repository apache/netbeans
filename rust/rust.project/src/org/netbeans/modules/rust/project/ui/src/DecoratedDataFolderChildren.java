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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustProjectAPI;
import org.netbeans.modules.rust.project.ui.RustProjectRootNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 */
public class DecoratedDataFolderChildren
        extends ChildFactory<FileObject>
        implements Comparator<FileObject>, Predicate<FileObject>, PropertyChangeListener {

    private final RustProject project;
    private final DataFolder folder;

    public DecoratedDataFolderChildren(RustProject project, DataFolder folder) {
        this.project = project;
        this.folder = folder;
        folder.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String ename = evt.getPropertyName();
        if (DataFolder.PROP_CHILDREN.equals(ename)
                || DataFolder.PROP_FILES.equals(ename)
                || DataFolder.PROP_ORDER.equals(ename)
                || DataFolder.PROP_MODIFIED.equals(ename)) {
            refresh(true);
        }
    }

    @Override
    public int compare(FileObject a, FileObject b
    ) {
        boolean isFolderA = a.isFolder();
        boolean isFolderB = b.isFolder();
        if (isFolderA) {
            if (isFolderB) {
                return a.getName().compareTo(b.getName());
            }
            return -1;
        }
        if (isFolderB) {
            return 1;
        }
        return a.getName().compareTo(b.getName());
    }

    @Override
    public boolean test(FileObject fo
    ) {
        boolean valid = true;
        String name = fo.getNameExt();
        valid &= !"Cargo.toml".equals(name); // NOI18N
        valid &= !"Cargo.lock".equals(name); // NOI18N
        valid &= !"target".equals(name); // NOI18N
        valid &= !name.startsWith(".");
        return valid;
    }

    @Override
    protected boolean createKeys(List<FileObject> toPopulate
    ) {
        List<FileObject> files = Arrays.stream(folder.getChildren()).map(DataObject::getPrimaryFile)
        // List<FileObject> files = Arrays.stream(folder.getPrimaryFile().getChildren())
                .filter(this)
                // .sorted(this)
                .collect(Collectors.toList());
        toPopulate.addAll(files);
        return true;
    }

    @Override
    protected Node createNodeForKey(FileObject fo
    ) {
        Node node = null;
        try {
            node = DataObject.find(fo).getNodeDelegate().cloneNode();
        } catch (DataObjectNotFoundException ex) {
            node = Node.EMPTY;
        }
        // Is this a project?
        if (fo.isFolder()) {
            ProjectManager.Result r = ProjectManager.getDefault().isProject2(fo);
            if (r != null) {
                if (r.getProjectType().equals(RustProjectAPI.RUST_PROJECT_KEY)) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(fo);
                        RustProject rp = p.getLookup().lookup(RustProject.class);
                        return new RustProjectRootNode(rp);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                // Yes, this is a project. Let's decorate
                return new DecoratedProjectNode(project, fo, r);
            }
        }
        return node;
    }

}
