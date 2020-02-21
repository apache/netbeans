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
package org.netbeans.modules.subversion.remote.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.subversion.remote.client.SvnProgressSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.ProjectUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * 
 */
public class CheckoutCompleted {

    public enum Type {
        EXPORT,
        CHECKOUT
    }

    private final VCSFileProxy workingFolder;
    private final String[] checkedOutFolders;

    public CheckoutCompleted(VCSFileProxy workingFolder, String[] checkedOutFolders) {
        this.checkedOutFolders = checkedOutFolders;
        this.workingFolder = workingFolder;
    }

    public void scanForProjects(SvnProgressSupport support, Type type) {

        Map<Project, Set<Project>> checkedOutProjects = new HashMap<>();
        checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
        VCSFileProxy normalizedWorkingFolder = workingFolder.normalizeFile();
        // checkout creates new folders and cache must be aware of them
        SvnUtils.refreshParents(normalizedWorkingFolder);
        FileObject fo = normalizedWorkingFolder.toFileObject();
        if (fo != null) {
            for (int i = 0; i < checkedOutFolders.length; i++) {
                if (support != null && support.isCanceled()) {
                    return;
                }
                String module = checkedOutFolders[i];
                if (".".equals(module)) {                   // NOI18N
                    // root folder is scanned, skip remaining modules
                    ProjectUtilities.scanForProjects(fo, checkedOutProjects);
                    break;
                } else {
                    FileObject subfolder = fo.getFileObject(module);
                    if (subfolder != null) {
                        ProjectUtilities.scanForProjects(subfolder, checkedOutProjects);
                    }
                }
            }
        }
        // open project selection
        switch(type) {
            case EXPORT:
                org.netbeans.modules.remotefs.versioning.api.ProjectUtilities.openExportedProjects(checkedOutProjects, workingFolder);
                break;
            case CHECKOUT:
                org.netbeans.modules.remotefs.versioning.api.ProjectUtilities.openCheckedOutProjects(checkedOutProjects, workingFolder);
                break;
        }
    }
}
