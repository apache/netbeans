/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.clientproject.ui.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.util.ClientSideProjectUtilities;
import org.netbeans.modules.web.common.spi.ImportantFilesImplementation;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.openide.filesystems.FileObject;

/**
 * Default project operations.
 */
public class ProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOrRenameOperationImplementation {

    private final ClientSideProject project;


    public ProjectOperations(ClientSideProject project) {
        this.project = project;
    }

    @Override
    public void notifyDeleting() throws IOException {
        // noop
    }

    @Override
    public void notifyDeleted() throws IOException {
        project.getProjectHelper().notifyDeleted();
    }

    @Override
    public void notifyCopying() throws IOException {
        // noop
    }

    @Override
    public void notifyCopied(Project original, File originalPath, String nueName) throws IOException {
        if (original == null) {
            // do nothing for the original project.
            return;
        }
        project.setName(nueName);
        project.getReferenceHelper().fixReferences(originalPath);
    }

    @Override
    public void notifyMoving() throws IOException {
        // noop
    }

    @Override
    public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
        if (original == null) {
            project.getProjectHelper().notifyDeleted();
            return;
        }
        project.setName(nueName);
        project.getReferenceHelper().fixReferences(originalPath);
    }

    @Override
    public void notifyRenaming() throws IOException {
        // noop
    }

    @Override
    public void notifyRenamed(String nueName) throws IOException {
        project.setName(nueName);
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        FileObject nbproject = project.getProjectDirectory().getFileObject("nbproject"); // NOI18N
        if (nbproject != null) {
            return Collections.singletonList(nbproject);
        }
        return Collections.emptyList();
    }

    @Override
    public List<FileObject> getDataFiles() {
        List<FileObject> files = new ArrayList<>();
        // all the sources
        files.addAll(Arrays.asList(ClientSideProjectUtilities.getSourceObjects(project)));
        // important files
        for (ImportantFilesImplementation importantFiles : project.getLookup().lookupAll(ImportantFilesImplementation.class)) {
            for (ImportantFilesImplementation.FileInfo fileInfo : importantFiles.getFiles()) {
                files.add(fileInfo.getFile());
            }
        }
        return files;
    }

}
