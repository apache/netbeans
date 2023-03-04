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
package org.netbeans.modules.mercurial.ui.clone;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.versioning.util.ProjectUtilities;

/**
 *
 * @author Tomas Stupka
 */
public class CloneCompleted {

    private final File workingFolder;

    public CloneCompleted(File workingFolder) {
        this.workingFolder = workingFolder;
    }

    public void scanForProjects(HgProgressSupport support) {
        Map<Project, Set<Project>> checkedOutProjects = new HashMap<Project, Set<Project>>();
        checkedOutProjects.put(null, new HashSet<Project>()); // initialize root project container
        File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
        FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
        if (fo != null) {
            ProjectUtilities.scanForProjects(fo, checkedOutProjects);
        }
        if (support != null && support.isCanceled()) {
            return;
        }
        // open project selection
        ProjectUtilities.openClonedOutProjects(checkedOutProjects, workingFolder);
    }
}
