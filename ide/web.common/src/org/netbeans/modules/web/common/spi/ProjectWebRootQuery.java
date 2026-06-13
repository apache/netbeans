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
package org.netbeans.modules.web.common.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.util.Parameters;

/**
 * Clients uses this class to obtain a web root for a file within a web-like project.
 *
 * @author marekfukala
 */
public final class ProjectWebRootQuery {

    private ProjectWebRootQuery() {
    }

    /**
     * Gets a web root for given file residing in a project.
     *
     * @param a file which you want to get a web root for
     * @return a found web root or null
     */
    public static FileObject getWebRoot(FileObject file) {
        if (file == null) {
            throw new NullPointerException("The file paramater cannot be null."); //NOI18N
        }

        Project project = FileOwnerQuery.getOwner(file);
        if (project != null) {
            ProjectWebRootProvider provider = project.getLookup().lookup(ProjectWebRootProvider.class);
            if (provider != null) {
                FileObject root = provider.getWebRoot(file);
                if (root == null) {
                    return null;
                }

                return root;
            }
        }
        return null;
    }

    /**
     * Gets all web roots for given project.
     *
     * @param project a project which you want to get web roots for
     * @return collection of web roots of the given project, can be empty but never {@code null}
     *
     * @since 1.57
     */
    @NonNull
    public static Collection<FileObject> getWebRoots(@NonNull Project project) {
        Parameters.notNull("project", project); // NOI18N
        ProjectWebRootProvider provider = project.getLookup().lookup(ProjectWebRootProvider.class);
        if (provider == null) {
            List<FileObject> objects = new ArrayList<>();
            Sources sources = ProjectUtils.getSources(project);
            for (SourceGroup group : sources.getSourceGroups("java")) {
                FileObject root = group.getRootFolder();
                objects.add(root);
            }
            objects.add(project.getProjectDirectory());
            return objects;
        }
        Collection<FileObject> webRoots = provider.getWebRoots();
        assert webRoots != null : "WebRoots cannot be null in " + provider.getClass().getName();
        return webRoots;
    }

}
