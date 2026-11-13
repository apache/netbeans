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

package org.netbeans.modules.generic.project;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.spi.project.ProjectContainerProvider;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 * Exposes the projects contained in the project directory as
 * subprojects: the project is a container for the projects in its
 * directory, so the stock subprojects group and "Open Required
 * Projects" discover them through this project instead of a
 * hard-coded directory scan.
 *
 * <p>Implements both the newer {@link ProjectContainerProvider}
 * and the legacy {@link SubprojectProvider} over the same set.
 */
final class GpProjectContainerProvider implements ProjectContainerProvider, SubprojectProvider {

    private final GenericProject prj;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    GpProjectContainerProvider(GenericProject prj) {
        this.prj = prj;
    }

    @Override
    public Result getContainedProjects() {
        Set<Project> projects = new LinkedHashSet<>();
        collectProjects(prj.getProjectDirectory(), projects, true);
        projects.remove(prj);
        return new Result(projects, false);
    }

    @Override
    public Set<? extends Project> getSubprojects() {
        return getContainedProjects().getProjects();
    }

    /**
     * Collects the projects contained in the given folder: folders
     * only, a {@code findProject} per folder, convertor projects
     * skipped, and the children of a {@code NOT_SHARABLE} folder
     * skipped (the check is skipped once the folder is proven
     * {@code SHARABLE}).
     */
    private void collectProjects(FileObject dir, Set<Project> projects, boolean checkShare) {
        Enumeration<? extends FileObject> e = dir.getFolders(false);
        while (e.hasMoreElements()) {
            if (Thread.interrupted()) {
                return;
            }
            FileObject child = e.nextElement();
            SharabilityQuery.Sharability share = SharabilityQuery.Sharability.UNKNOWN;
            if (checkShare) {
                share = SharabilityQuery.getSharability(dir);
                if (share == SharabilityQuery.Sharability.NOT_SHARABLE) {
                    continue;
                }
            }
            Project p = null;
            try {
                p = ProjectManager.getDefault().findProject(child);
            } catch (IOException x) {
                Exceptions.printStackTrace(x);
            }
            if (p != null && !ProjectConvertors.isConvertorProject(p)) {
                projects.add(p);
            }
            checkShare = share != SharabilityQuery.Sharability.SHARABLE;
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
}
