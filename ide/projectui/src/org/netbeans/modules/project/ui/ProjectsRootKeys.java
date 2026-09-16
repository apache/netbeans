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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Union2;

/**
 * Encapsulation of opened project root keys. Subclass and "connect" to source
 * of projects, but overwriting {@link #listProjects()}.
 */
abstract class ProjectsRootKeys {
    private final int type;
    //@GuardedBy("this")
    private final Map <FileObject,int[]> projects2Depths = new WeakHashMap<>();
    //@GuardedBy("this")
    private final Map <Project,Reference<ProjectsRootKeys.PrjInfo>> projects2Pairs = new WeakHashMap<>();

    ProjectsRootKeys(int type) {
        this.type = type;
    }

    /** The project to process. Called by the internals of this class whenever
     * list of projects is needed.
     *
     * @return non-empty array of projects to create keys for
     */
    abstract Project[] listProjects();

    /** Called when a depth of a project got updated */
    abstract void depthUpdated(PrjInfo info);

    final void update(Project project) {
        Reference<PrjInfo> ref;
        synchronized (this) {
            ref = projects2Pairs.get(project);
        }
        if (ref != null) {
            var info = ref.get();
            if (info != null) {
                info.update(project);
            }
        }
    }

    synchronized final Set<PrjInfo> clear() {
        projects2Pairs.clear();
        return Collections.<ProjectsRootKeys.PrjInfo>emptySet();
    }

    Collection<PrjInfo> getKeys() {
        var projects = Arrays.asList(listProjects());
        projects.sort(OpenProjectList.projectByPath());

        var dirs = new ArrayList<PrjInfo>(projects.size());
        final java.util.Map<Project,ProjectsRootKeys.PrjInfo> snapshot = new HashMap<>();
        var nested = new LinkedList<FileObject>();
        for (Project prj : projects) {
            while (!nested.isEmpty()) {
                if (FileUtil.isParentOf(nested.peekLast(), prj.getProjectDirectory())) {
                    break;
                }
                nested.removeLast();
            }
            int originalNestedSize;
            int[] nestedArr;
            synchronized (this) {
                var arr = projects2Depths.get(prj.getProjectDirectory());
                if (arr == null) {
                    originalNestedSize = -1;
                    nestedArr = new int[1];
                } else {
                    originalNestedSize = arr[0];
                    nestedArr = arr;
                }
            }
            var nestedSize = nested.size();
            nestedArr[0] = nestedSize;

            var p = new ProjectsRootKeys.PrjInfo(prj, type, nestedArr);
            nested.add(prj.getProjectDirectory());
            dirs.add(p);
            snapshot.put(prj, p);
            synchronized (this) {
                projects2Depths.put(prj.getProjectDirectory(), nestedArr);
            }
            if (originalNestedSize != -1 && originalNestedSize != nestedArr[0]) {
                depthUpdated(p);
            }
        }
        synchronized (this) {
            projects2Pairs.clear();
            snapshot.entrySet()
                    .forEach((e) -> projects2Pairs.put(
                            e.getKey(),
                            new WeakReference<>(e.getValue())));
        }
        return dirs;
    }

    int type() {
        return type;
    }

    PrjInfo createInfo(Project newProj, boolean logicalView) {
        int[] depth = this.projects2Depths.get(newProj.getProjectDirectory());
        if (depth == null) {
            depth = new int[1];
        }
        return new ProjectsRootKeys.PrjInfo(
            newProj,
            logicalView ? ProjectsRootNode.LOGICAL_VIEW : ProjectsRootNode.PHYSICAL_VIEW,
            depth
        );
    }

    /**
     * Object that comparers two projects just by their directory. This allows
     * to replace a LazyProject with real one without discarding the nodes.
     */
    static final class PrjInfo extends Object {
        final FileObject fo;
        private final int type;
        private Project project;
        private Union2<LogicalViewProvider, org.openide.util.Pair<Sources, SourceGroup[]>> data;
        private final int[] depth;

        private PrjInfo(Project project,int type, int[] depth) {
            this.project = project;
            this.fo = project.getProjectDirectory();
            this.type = type;
            this.depth = depth;
            this.data = createData(project, type);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PrjInfo other = (PrjInfo) obj;
            if (this.fo != other.fo && (this.fo == null || !this.fo.equals(other.fo))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.fo != null ? this.fo.hashCode() : 0);
            return hash;
        }

        void update(@NonNull final Project project) {
            assert project != null;
            this.project = project;
            this.data = createData(project, type);
        }

        Sources getSources() {
            return data.second().first();
        }

        SourceGroup[] getSourceGroups() {
            return data.second().second();
        }

        LogicalViewProvider getLocalViewProvider() {
            return data.hasFirst() ? data.first() : null;
        }

        @SuppressWarnings("fallthrough")
        private static Union2<LogicalViewProvider, org.openide.util.Pair<Sources, SourceGroup[]>> createData(
                final Project p,
                final int type) {
            switch (type) {
                case ProjectsRootNode.LOGICAL_VIEW:
                    final LogicalViewProvider lvp = p.getLookup().lookup(LogicalViewProvider.class);
                    if (lvp != null) {
                        return Union2.createFirst(lvp);
                    }
                case ProjectsRootNode.PHYSICAL_VIEW:
                    final Sources s = ProjectUtils.getSources(p);
                    final SourceGroup[] groups = s.getSourceGroups(Sources.TYPE_GENERIC);
                    return Union2.createSecond(org.openide.util.Pair.of(s, groups));
                default:
                    throw new IllegalArgumentException(Integer.toString(type));
            }
        }

        final int depth() {
            return depth[0];
        }

        final Project project() {
            return project;
        }
    }
}
