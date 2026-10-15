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
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.javaee.BaseEEModuleProvider;
import org.netbeans.modules.gradle.javaee.api.GradleWebProject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleFactory;
import org.netbeans.modules.web.spi.webmodule.WebModuleProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(
    service = {
        WebModuleProviderImpl.class,
        WebModuleProvider.class,
    },
    projectType = {
        NbGradleProject.GRADLE_PLUGIN_TYPE + "/war",
    }
)
public class WebModuleProviderImpl extends BaseEEModuleProvider implements WebModuleProvider {

    private WebModuleImpl implementation;
    volatile WebModule webModule;

    public WebModuleProviderImpl(Project project) {
        super(project);
    }

    @Override
    public WebModule findWebModule(FileObject file) {
        WebModuleImpl impl = getModuleImpl();
        if (impl != null) {
            if (webModule == null) {
                webModule = WebModuleFactory.createWebModule(impl);
            }
            return webModule;
        }
        return null;
    }

    @Override
    public WebModuleImpl getModuleImpl() {
        if (implementation == null) {
            implementation = new WebModuleImpl(project, this);
        }
        return implementation;
    }

    @Override
    public FileObject[] getSourceRoots() {
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (gjp != null && gjp.getMainSourceSet() != null) {
            GradleJavaSourceSet main = gjp.getMainSourceSet();
            List<File> roots = new ArrayList<>();
            roots.addAll(main.getResourcesDirs());
            GradleWebProject gwp = GradleWebProject.get(project);
            if (gwp != null) {
                roots.add(gwp.getWebAppDir());
            }
            roots.addAll(main.getJavaDirs());
            roots.addAll(main.getGroovyDirs());
            roots.addAll(main.getScalaDirs());

            List<FileObject> fos = new ArrayList<>();
            for (File root : roots) {
                FileObject fo = FileUtil.toFileObject(root);
                if (root != null) {
                    fos.add(fo);
                }
            }
            return fos.toArray(new FileObject[0]);
        }
        return super.getSourceRoots();
    }


}
