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

package org.netbeans.modules.gradle.javaee;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport;
import org.netbeans.modules.web.common.api.CssPreprocessors;
import org.netbeans.modules.web.common.api.CssPreprocessorsListener;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.WeakListeners;
import org.openide.windows.WindowManager;
import org.openide.windows.WindowSystemEvent;
import org.openide.windows.WindowSystemListener;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = ProjectOpenedHook.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/war")
public final class ProjectOpenedHookImpl extends ProjectOpenedHook {

    final Project project;

    public ProjectOpenedHookImpl(Project project) {
        this.project = project;
    }


    @Override
    protected void projectOpened() {
        final CssPreprocessorsListener cssSupport = project.getLookup().lookup(CssPreprocessorsListener.class);
        if (cssSupport != null) {
            CssPreprocessors.getDefault().addCssPreprocessorsListener(cssSupport);
        }

        WindowManager windowManager = WindowManager.getDefault();
        windowManager.addWindowSystemListener(WeakListeners.create(WindowSystemListener.class, windowSystemListener, windowManager));

    }

    @Override
    protected void projectClosed() {
        CssPreprocessorsListener cssSupport = project.getLookup().lookup(CssPreprocessorsListener.class);
        if (cssSupport != null) {
            CssPreprocessors.getDefault().removeCssPreprocessorsListener(cssSupport);
        }
    }

    private final WindowSystemListener windowSystemListener = new WindowSystemListener() {

        @Override
        public void beforeLoad(WindowSystemEvent event) {
        }

        @Override
        public void afterLoad(WindowSystemEvent event) {
        }

        @Override
        public void beforeSave(WindowSystemEvent event) {
            ClientSideDevelopmentSupport clientSideSupport = project.getLookup().lookup(ClientSideDevelopmentSupport.class);
            if (clientSideSupport != null) {
                clientSideSupport.close();
            }
        }

        @Override
        public void afterSave(WindowSystemEvent event) {
        }
    };

}
