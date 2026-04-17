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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Icon;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;

/**
 * A RustSourceGroup represents a "folder" with sources, this is used for the
 * "test" directory, for example, and for each one of the subfolders in Rust
 * workspaces.
 *
 * @author antonio
 */
public final class RustSourceGroup implements SourceGroup {

    private final RustProject project;
    private final FileObject folder;
    private final PropertyChangeSupport propertyChangeSupport;

    public RustSourceGroup(RustProject project, FileObject folder) {
        this.project = project;
        this.folder = folder;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    @Override
    public FileObject getRootFolder() {
        return folder;
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public String getDisplayName() {
        return folder.getName();
    }

    @Override
    public Icon getIcon(boolean opened) {
        return ImageUtilities.image2Icon(RustIconFactory.getSourceFolderIcon(opened));
    }

    @Override
    public boolean contains(FileObject file) {
        FileObject rootFolder = getRootFolder();
        return FileUtil.isParentOf(rootFolder, file);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

}
