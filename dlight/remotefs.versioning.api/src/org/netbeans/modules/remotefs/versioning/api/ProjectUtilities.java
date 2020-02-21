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
package org.netbeans.modules.remotefs.versioning.api;

import org.netbeans.api.project.Project;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.remotefs.versioning.util.projects.ProjectOpener;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;

/**
 * Simpliied nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectUtilities.java,
 * nb_all/projects/projectui/src/org/netbeans/modules/project/ui/ProjectTab.java and
 * nb_all/ide/welcome/src/org/netbeans/modules/welcome/ui/TitlePanel.java copy.
 *
 */
public final class ProjectUtilities {

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned checkout projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openExportedProjects(Map<Project, Set<Project>> checkedOutProjects, VCSFileProxy workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.EXPORT, checkedOutProjects, workingFolder);
        opener.openProjects();
    }

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned checkout projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openCheckedOutProjects(Map<Project, Set<Project>> checkedOutProjects, VCSFileProxy workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.CHECKOUT, checkedOutProjects, workingFolder);
        opener.openProjects();
    }

    /**
     * Guides user through a project opening process.
     * @param checkedOutProjects list of scanned cloned projects
     * @param workingFolder implicit folder to create a new project in if user selects <em>Create New Project</em> in the following dialog
     */
    public static void openClonedOutProjects(Map<Project, Set<Project>> checkedOutProjects, VCSFileProxy workingFolder) {
        ProjectOpener opener = new ProjectOpener(ProjectOpener.ProjectOpenerType.CLONE, checkedOutProjects, workingFolder);
        opener.openProjects();
    }
}
