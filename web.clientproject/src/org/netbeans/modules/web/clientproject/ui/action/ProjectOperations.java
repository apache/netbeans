/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
