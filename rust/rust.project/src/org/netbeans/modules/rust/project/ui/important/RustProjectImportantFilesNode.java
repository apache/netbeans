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
package org.netbeans.modules.rust.project.ui.important;

import java.awt.Image;
import org.netbeans.modules.rust.project.RustProject;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * The "important-files" node in a Rust project.
 */
public class RustProjectImportantFilesNode extends AbstractNode {

    public static final String NAME = "rust-important-files"; // NOI18N

    private enum ImportantFileNames {
        CARGO_TOUML("Cargo.toml"),
        CARGO_LOCK("Cargo.lock")
        ; // NOI18N

        final String fileName;

        ImportantFileNames(String fileName) {
            this.fileName = fileName;
        }
    }

    private static final class ImportantFilesChildren extends Children.Keys<ImportantFileNames> {

        private final RustProject project;

        ImportantFilesChildren(RustProject project) {
            this.project = project;
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(ImportantFileNames.values());
        }

        private static final Node[] EMPTY = new Node[0];

        @Override
        protected Node[] createNodes(ImportantFileNames key) {
            String filename = key.fileName;
            FileObject file = project.getProjectDirectory().getFileObject(filename);
            if (file != null) {
                try {
                    DataObject dataObject = DataObject.find(file);
                    Node node = dataObject == null ? null : dataObject.getNodeDelegate().cloneNode();
                    return node == null ? EMPTY : new Node[]{node};
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return EMPTY;
        }

    }

    private final RustProject project;

    public RustProjectImportantFilesNode(RustProject project) {
        this(project, new ImportantFilesChildren(project));
    }

    RustProjectImportantFilesNode(RustProject project, Children ch) {
        super(ch, Lookups.fixed(project, project.getProjectDirectory()));
        this.project = project;
    }

    public @Override
    String getName() {
        return NAME;
    }

    @NbBundle.Messages("display-name=Important files")
    public @Override
    String getDisplayName() {
        return NbBundle.getMessage(RustProjectImportantFilesNode.class, "display-name"); // NOI18N
    }

    public @Override
    Image getIcon(int type) {
        return RustIconFactory.getImportantFilesFolderIcon(false);
    }

    public @Override
    Image getOpenedIcon(int type) {
        return RustIconFactory.getImportantFilesFolderIcon(true);
    }

}
