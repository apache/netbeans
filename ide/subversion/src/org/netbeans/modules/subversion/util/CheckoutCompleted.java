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
package org.netbeans.modules.subversion.util;

import org.netbeans.modules.versioning.util.ProjectUtilities;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Stupka
 */
public class CheckoutCompleted {

    public enum Type {
        EXPORT,
        CHECKOUT
    }

    private final File workingFolder;
    private String[] checkedOutFolders;

    public CheckoutCompleted(File workingFolder, String[] checkedOutFolders) {
        this.checkedOutFolders = checkedOutFolders;
        this.workingFolder = workingFolder;
    }

    public void scanForProjects(SvnProgressSupport support, Type type) {

        Map<Project, Set<Project>> checkedOutProjects = new HashMap<Project, Set<Project>>();
        checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
        File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
        // checkout creates new folders and cache must be aware of them
        SvnUtils.refreshParents(normalizedWorkingFolder);
        FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
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
                ProjectUtilities.openExportedProjects(checkedOutProjects, workingFolder);
                break;
            case CHECKOUT:
                ProjectUtilities.openCheckedOutProjects(checkedOutProjects, workingFolder);
                break;
        }
    }
}
