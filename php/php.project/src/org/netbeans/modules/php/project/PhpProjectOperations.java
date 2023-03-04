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
package org.netbeans.modules.php.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.openide.filesystems.FileObject;


/**
 * @author ads
 */
public class PhpProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation,
        MoveOrRenameOperationImplementation {

    private final PhpProject project;

    public PhpProjectOperations(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public void notifyDeleted() throws IOException {
        project.getHelper().notifyDeleted();
    }

    @Override
    public void notifyDeleting() throws IOException {
    }

    @Override
    public void notifyCopied(Project originalProject, File file, String newName) throws IOException {
        if (originalProject == null) {
            // do nothing for the original project.
            return;
        }
        project.setName(newName);
    }

    @Override
    public void notifyCopying() throws IOException {
    }

    @Override
    public void notifyMoved(Project originalProject, File file, String newName) throws IOException {
        if (originalProject == null) {
            project.getHelper().notifyDeleted();
            return;
        }
        project.setName(newName);
    }

    @Override
    public void notifyMoving() throws IOException {
    }

    @Override
    public void notifyRenaming() throws IOException {
    }

    @Override
    public void notifyRenamed(String nueName) throws IOException {
        project.setName(nueName);
    }

    @Override
    public List<FileObject> getDataFiles() {
        // all the sources, including external
        return Arrays.asList(PhpProjectUtils.getSourceObjects(project));
    }

    @Override
    public List<FileObject> getMetadataFiles() {
        List<FileObject> files = new ArrayList<>(1);
        // add nbproject dir
        FileObject nbProject = project.getHelper().getProjectDirectory().getFileObject("nbproject"); // NOI18N
        if (nbProject != null) {
            files.add(nbProject);
        }
        return files;
    }

}
