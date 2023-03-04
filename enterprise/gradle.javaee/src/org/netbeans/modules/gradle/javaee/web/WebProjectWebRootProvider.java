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

package org.netbeans.modules.gradle.javaee.web;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.javaee.api.GradleWebProject;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.spi.ProjectWebRootProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider( service = ProjectWebRootProvider.class, projectType = NbGradleProject.GRADLE_PLUGIN_TYPE + "/war")
public class WebProjectWebRootProvider implements ProjectWebRootProvider{

    final Project project;

    public WebProjectWebRootProvider(Project project) {
        this.project = project;
    }
    
    
    @Override
    public FileObject getWebRoot(FileObject file) {
        return getDefaultWebRoot();
    }

    @Override
    public Collection<FileObject> getWebRoots() {
        FileObject webRoot = getDefaultWebRoot();
        return webRoot != null ? Collections.singleton(webRoot) : Collections.<FileObject>emptySet();
    }
    
    FileObject getDefaultWebRoot() {
        GradleWebProject wp = GradleWebProject.get(project);
        return wp != null ? FileUtil.toFileObject(wp.getWebAppDir()) : null;
    }
}
