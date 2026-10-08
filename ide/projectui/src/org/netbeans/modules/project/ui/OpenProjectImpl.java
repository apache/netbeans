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
package org.netbeans.modules.project.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

final class OpenProjectImpl {
    /** Main project */
    private Project mainProject;
    /** List which holds the open projects */
    private List<Project> openProjects = List.of();
    private volatile OpenProjectOperation LOAD;

    OpenProjectImpl() {
        
    }

    final List<Project> openProjects() {
        return openProjects;
    }

    final void replaceProjectsImpl(OpenProjectsLoading.Callback callback, List<LazyProject> projects, URL mainProject, Lookup.Result<FileObject> selectedFiles) {
        openProjects = new ArrayList<>(projects);
        var urls = projects.stream().map(p -> p.url).toList();
        var load = new OpenProjectsLoading(callback, selectedFiles, urls, mainProject);
        LOAD = load;
        WindowManager.getDefault().invokeWhenUIReady(load);
    }

    boolean waitFinished(long timeout) {
        return LOAD.waitFinished(timeout);
    }

    void preferredProject(Set<FileObject> singleton) {
        LOAD.preferredProject(singleton);
    }

    boolean isDone() {
        return LOAD.isDone();
    }

    void enter() {
        LOAD.enter();
    }

    void exit() {
        LOAD.exit();
    }

    boolean closeBeforeOpen(Project[] someProjects) {
        return LOAD.closeBeforeOpen(someProjects);
    }

    void updateGlobalState(List<Project> projects, Project mainProject) {
        this.openProjects = projects;
        this.mainProject = mainProject;
    }

    Project mainProject() {
        return this.mainProject;
    }
}
