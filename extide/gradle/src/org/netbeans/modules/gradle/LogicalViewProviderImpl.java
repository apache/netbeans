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

package org.netbeans.modules.gradle;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.execute.navigator.TasksNavigatorHint;
import org.netbeans.modules.gradle.nodes.GradleProjectNode;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.PathFinder;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = LogicalViewProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class LogicalViewProviderImpl implements LogicalViewProvider {

    private final Project proj;

    public LogicalViewProviderImpl(Project proj) {
        this.proj = proj;
    }

    @Override
    public Node createLogicalView() {
        NbGradleProjectImpl project = proj.getLookup().lookup(NbGradleProjectImpl.class);
        return new GradleProjectNode(createLookup(project), project);
    }

    private static Lookup createLookup(NbGradleProjectImpl project) {
        if (!project.getProjectDirectory().isValid()) {
            return Lookups.fixed(project);
        }
        DataFolder rootFolder = DataFolder.findFolder(project.getProjectDirectory());
        return Lookups.fixed(project, rootFolder, project.getProjectDirectory(), new TasksNavigatorHint());
    }

    @Override
    public Node findPath(Node node, Object target) {
        NbGradleProjectImpl prj = node.getLookup().lookup(NbGradleProjectImpl.class);
        if (prj == null) {
            return null;
        }

        if (target instanceof FileObject) {
            FileObject fo = (FileObject) target;

            if (isOtherProjectSource(fo, prj)) {
                return null; // Don't waste time if project does not own the fo
            }
            Node[] nodes = node.getChildren().getNodes(true);
            for (Node n : nodes) {
                PathFinder pf = n.getLookup().lookup(PathFinder.class);
                if (pf != null) {
                    Node result = pf.findPath(n, target);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isOtherProjectSource(
            @NonNull final FileObject fo,
            @NonNull final Project me) {
        final Project owner = FileOwnerQuery.getOwner(fo);
        if (owner == null) {
            return false;
        }
        if (me.equals(owner)) {
            return false;
        }
        return false;
    }

}
