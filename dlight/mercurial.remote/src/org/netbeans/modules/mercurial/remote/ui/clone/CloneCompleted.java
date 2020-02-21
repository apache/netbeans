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
package org.netbeans.modules.mercurial.remote.ui.clone;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.ProjectUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * 
 */
public class CloneCompleted {

    private final VCSFileProxy workingFolder;

    public CloneCompleted(VCSFileProxy workingFolder) {
        this.workingFolder = workingFolder;
    }

    public void scanForProjects(HgProgressSupport support) {
        Map<Project, Set<Project>> checkedOutProjects = new HashMap<>();
        checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
        VCSFileProxy normalizedWorkingFolder = workingFolder.normalizeFile();
        FileObject fo = normalizedWorkingFolder.toFileObject();
        if (fo != null) {
            ProjectUtilities.scanForProjects(fo, checkedOutProjects);
        }
        if (support != null && support.isCanceled()) {
            return;
        }
        // open project selection
        org.netbeans.modules.remotefs.versioning.api.ProjectUtilities.openClonedOutProjects(checkedOutProjects, workingFolder);
    }
}
