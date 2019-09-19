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
package org.netbeans.modules.gradle.java.queries;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.spi.java.queries.CompilerOptionsQueryImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 *
 * @author lkishalmi
 */
@ProjectServiceProvider(service = CompilerOptionsQueryImplementation.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/java-base")
public final class GradleCompilerOptionsQuery implements CompilerOptionsQueryImplementation {

    final Project project;
    private final PropertyChangeListener listener;
    final Map<String, ResultImpl> cache = new HashMap<>();

    public GradleCompilerOptionsQuery(Project project) {
        this.project = project;
        final NbGradleProject watcher = NbGradleProject.get(project);
        listener = (evt) -> {
            if (watcher.isUnloadable()) return;
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                //TODO: How shall we handle source set removal?
                synchronized(GradleCompilerOptionsQuery.this) {
                    for (ResultImpl res : cache.values()) {
                        res.support.fireChange();
                    }
                }
            }
        };
    }

    @Override
    public Result getOptions(FileObject file) {
        File f = FileUtil.toFile(file);
        GradleJavaProject gjp = GradleJavaProject.get(project);
        GradleJavaSourceSet sourceSet = gjp.containingSourceSet(f);
        ResultImpl ret = null;
        if (sourceSet != null) {
            GradleJavaSourceSet.SourceType sourceType = sourceSet.getSourceType(f);
            if (sourceType != GradleJavaSourceSet.SourceType.RESOURCES) {
                String key = sourceSet.getName() + "." + sourceType.name();
                synchronized(this) {
                    ret = cache.get(key);
                    if (ret == null) {
                        ret = new ResultImpl(sourceSet.getName(), sourceType);
                        cache.put(key, ret);
                    }
                }
            }
        }
        return ret;
    }

    final class ResultImpl extends Result {

        final String sourceSetName;
        final GradleJavaSourceSet.SourceType type;
        final ChangeSupport support;

        public ResultImpl(String sourceSetName, GradleJavaSourceSet.SourceType type) {
            this.sourceSetName = sourceSetName;
            this.type = type;
            support = new ChangeSupport(this);
        }


        @Override
        public List<? extends String> getArguments() {
            GradleJavaProject gjp = GradleJavaProject.get(project);
            GradleJavaSourceSet ss = gjp.getSourceSets().get(sourceSetName);
            return ss.getCompilerArgs(type);
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            support.addChangeListener(listener);
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            support.removeChangeListener(listener);
        }

    }
}
