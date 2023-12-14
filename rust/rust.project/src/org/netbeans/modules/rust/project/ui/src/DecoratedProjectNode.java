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
import java.io.IOException;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.rust.project.RustProject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUIUtils;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * A folder that is a Project (either a Rust Project or a Maven Project or
 * whatever).
 */
public class DecoratedProjectNode extends FilterNode {

    public DecoratedProjectNode(RustProject parentProject, FileObject folder, ProjectManager.Result r) {
        this(parentProject, folder, r, DataFolder.findFolder(folder).getNodeDelegate());
    }

    private DecoratedProjectNode(RustProject parentProject, FileObject folder,
            ProjectManager.Result r, Node folderNode) {
        super(folderNode,
                Children.create(
                        new DecoratedDataFolderChildren(parentProject, DataFolder.findFolder(folder)),
                        true),
                new ProxyLookup(
                        folderNode.getLookup(),
                        Lookups.fixed(parentProject)
                )
        );
    }

    public @Override
    Image getIcon(int type) {
        return swap(super.getIcon(type), type);
    }

    public @Override
    Image getOpenedIcon(int type) {
        return swap(super.getOpenedIcon(type), type);
    }

    private Image swap(Image base, int type) {
        FileObject folder = getOriginal().getLookup().lookup(FileObject.class);
        if (folder != null && folder.isFolder()) {
            ProjectManager.Result r = ProjectManager.getDefault().isProject2(folder);
            if (r != null) {
                Icon icon = r.getIcon();

                if (icon != null) {
                    Image img = ImageUtilities.icon2Image(icon);
                    try {
                        //#217008
                        DataFolder df = getOriginal().getLookup().lookup(DataFolder.class);
                        img = FileUIUtils.getImageDecorator(folder.getFileSystem()).annotateIcon(img, type, df.files());
                    } catch (FileStateInvalidException e) {
                        // no fs, do nothing
                    }
                    return img;
                }
            }
        }
        return base;
    }

    public @Override
    String getShortDescription() {
        FileObject folder = getOriginal().getLookup().lookup(FileObject.class);
        if (folder != null && folder.isFolder()) {
            try {
                Project p = ProjectManager.getDefault().findProject(folder);
                if (p != null) {
                    return ProjectUtils.getInformation(p).getDisplayName();
                }
            } catch (IOException x) {
                // Ignored LOG.log(Level.FINE, null, x);
            }
        }
        return super.getShortDescription();
    }

}
