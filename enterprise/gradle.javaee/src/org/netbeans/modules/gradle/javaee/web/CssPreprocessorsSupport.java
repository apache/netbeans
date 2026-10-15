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

package org.netbeans.modules.gradle.javaee.web;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.javaee.api.GradleWebProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.CssPreprocessor;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Janicek
 */
@ProjectServiceProvider(service = CssPreprocessorsListener.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/war")
public class CssPreprocessorsSupport implements CssPreprocessorsListener {

    private final Project project;

    public CssPreprocessorsSupport(Project project) {
        this.project = project;
    }

    public void recompileSources(CssPreprocessor cssPreprocessor) {
        assert cssPreprocessor != null;
        GradleWebProject wp = GradleWebProject.get(project);
        if (wp != null) {
            FileObject docBase = FileUtil.toFileObject(wp.getWebAppDir());
            if (docBase != null) {
                CssPreprocessors.getDefault().process(cssPreprocessor, project, docBase);
            }
        }
    }

    @Override
    public void preprocessorsChanged() {
    }

    @Override
    public void optionsChanged(CssPreprocessor cssPreprocessor) {
        recompileSources(cssPreprocessor);
    }

    @Override
    public void customizerChanged(Project project, CssPreprocessor cssPreprocessor) {
        if (project.equals(project)) {
            recompileSources(cssPreprocessor);
        }
    }

    @Override
    public void processingErrorOccured(Project project, CssPreprocessor cssPreprocessor, String error) {
    }
}
