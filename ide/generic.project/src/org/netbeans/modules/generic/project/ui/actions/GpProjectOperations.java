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

package org.netbeans.modules.generic.project.ui.actions;

import org.netbeans.modules.generic.project.GenericProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.openide.filesystems.FileObject;

/**
 * Supports the default move, rename, copy and delete operations of
 * generic projects.
 *
 * <p>The project is just a directory, everything in it is data (the
 * marker file, when present, being the only metadata), so move and
 * copy relocate or duplicate the directory as a whole; data files
 * are listed deepest-first for the delete flow. Since the copy thus
 * also inherits the project-local auxiliary data, {@link #notifyCopied}
 * drops the display name override from the original, just like a
 * move or folder rename, after which the folder name is
 * authoritative again.
 *
 * <p>A pure rename (the folder is not renamed) does not touch the
 * directory; the new name is kept as a display name override.
 */
public final class GpProjectOperations implements MoveOrRenameOperationImplementation, CopyOperationImplementation, DeleteOperationImplementation {

    private final GenericProject prj;

    public GpProjectOperations(GenericProject prj) {
        this.prj = prj;
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        // The marker file (when present) is the only project
        // metadata; local (attribute-based) projects keep their
        // recognition in a FileObject attribute, which needs no file.
        FileObject marker = prj.getProjectDirectory().getFileObject(GenericProject.MARKER_FILE);
        return marker != null ? List.of(marker) : List.of();
    }

    @Override
    public List<FileObject> getDataFiles() {
        // The whole directory is the project, so all of its content
        // is data. Listed deepest-first so that the stock delete
        // flow (which deletes each entry and afterwards the project
        // folder if it is empty) can remove everything.
        List<FileObject> result = new ArrayList<>();
        collectDataFiles(prj.getProjectDirectory(), result);
        return result;
    }

    private void collectDataFiles(FileObject dir, List<FileObject> result) {
        for (FileObject child : dir.getChildren()) {
            if (child.isFolder()) {
                collectDataFiles(child, result);
                result.add(child);
            } else if (dir.equals(prj.getProjectDirectory())
                    && GenericProject.MARKER_FILE.equals(child.getName())) {
                // the marker file is metadata, not data: the stock
                // flow deletes it unconditionally
            } else {
                result.add(child);
            }
        }
    }

    @Override
    public void notifyMoving() throws IOException {
        // Nothing to do: the marker file and the project attribute
        // both follow the folder when it is moved.
    }

    @Override
    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        if (original != null) {
            // Called on the new project instance: the folder (and the
            // attribute holding any display name override) has been
            // renamed, so the new folder name is authoritative again.
            GenericProject.setDisplayNameOverride(prj, nueName);
        }
    }

    @Override
    public void notifyRenaming() throws IOException {
        // Nothing to do.
    }

    @Override
    public void notifyRenamed(String nueName) throws IOException {
        GenericProject.setDisplayNameOverride(prj, nueName);
    }

    @Override
    public void notifyCopying() throws IOException {
        // Nothing to do: the stock flow copies the visible
        // directory content as-is.
    }

    @Override
    public void notifyCopied(Project original, File originalPath, String nueName) throws IOException {
        if (original != null) {
            // Called on the new project instance: keep the folder
            // name authoritative, like after a move. The copy
            // duplicates the project-local auxiliary data file (when
            // the marker is present) as-is, so this drops the
            // display name override inherited from the original.
            GenericProject.setDisplayNameOverride(prj, nueName);
        }
    }

    @Override
    public void notifyDeleting() throws IOException {
        // Remember that this project has been explicitly deleted so
        // that saveProject (e.g. from a preferences flush) never
        // resurrects it, and drop the recognition attribute of local
        // (attribute-based) projects so the folder stops being a
        // project even when it survives the delete.
        prj.markDeleted();
        FileObject dir = prj.getProjectDirectory();
        if (GenericProject.isAttributeProject(dir)) {
            dir.setAttribute(GenericProject.MARKER_ATTRIBUTE, null);
        }
    }

    @Override
    public void notifyDeleted() throws IOException {
        // Nothing to do: the stock flow has already deleted the
        // listed files and the (now empty) project folder.
    }

}
