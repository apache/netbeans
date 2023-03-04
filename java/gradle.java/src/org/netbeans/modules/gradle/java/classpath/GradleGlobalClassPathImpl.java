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

package org.netbeans.modules.gradle.java.classpath;

import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import static org.netbeans.modules.gradle.java.classpath.AbstractGradleClassPathImpl.addAllFile;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class GradleGlobalClassPathImpl extends AbstractGradleClassPathImpl {
    final boolean excludeTests;

    public GradleGlobalClassPathImpl(Project proj, boolean excludeTests) {
        super(proj);
        this.excludeTests = excludeTests;
    }

    @Override
    protected final List<URL> createPath() {
        GradleJavaProject jp = GradleJavaProject.get(project);
        List<URL> ret = Collections.<URL>emptyList();
        if (jp != null) {
            Set<URL> sources = new LinkedHashSet<>();
            for (GradleJavaSourceSet sourceSet : jp.getSourceSets().values()) {
                if (!excludeTests || !sourceSet.isTestSourceSet()) {
                    addAllFile(sources, selectFromSourceSet(sourceSet));
                }
            }
            ret = new ArrayList<>(sources);
        }
        return ret;
    }

    protected abstract Set<File> selectFromSourceSet(GradleJavaSourceSet sourceSet);

    public static class ProjectSourceClassPathImpl extends GradleGlobalClassPathImpl {

        public ProjectSourceClassPathImpl(Project proj, boolean excludeTests) {
            super(proj, excludeTests);
        }

        @Override
        protected Set<File> selectFromSourceSet(GradleJavaSourceSet sourceSet) {
            return (Set<File>) sourceSet.getAllDirs();
        }
    }

    public static class ProjectCompileClassPathImpl extends GradleGlobalClassPathImpl {

        public ProjectCompileClassPathImpl(Project proj, boolean excludeTests) {
            super(proj, excludeTests);
        }

        @Override
        protected Set<File> selectFromSourceSet(GradleJavaSourceSet sourceSet) {
            return sourceSet.getCompileClassPath();
        }
    }

    public static class ProjectRuntimeClassPathImpl extends GradleGlobalClassPathImpl {

        public ProjectRuntimeClassPathImpl(Project proj, boolean excludeTests) {
            super(proj, excludeTests);
        }

        @Override
        protected Set<File> selectFromSourceSet(GradleJavaSourceSet sourceSet) {
            return sourceSet.getRuntimeClassPath();
        }

    }
}
