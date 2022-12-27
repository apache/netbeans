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
package org.netbeans.modules.gradle.execute;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gradle.api.NbGradleProject;
import static org.netbeans.modules.gradle.customizer.GradleExecutionPanel.HINT_JDK_PLATFORM;
import org.netbeans.modules.gradle.spi.execute.GradleJavaPlatformProvider;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager;
import org.netbeans.modules.gradle.spi.execute.JavaRuntimeManager.JavaRuntime;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Laszlo Kishalmi
 */
@ProjectServiceProvider(service = GradleJavaPlatformProvider.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public final class GradleJavaPlatformProviderImpl implements GradleJavaPlatformProvider {

    final Project project;

    public GradleJavaPlatformProviderImpl(Project project) {
        this.project = project;
    }

    private String runtimeId() {
        Project root = ProjectUtils.rootOf(project);
        AuxiliaryProperties aux = root.getLookup().lookup(AuxiliaryProperties.class);
        String id = aux.get(HINT_JDK_PLATFORM, true);
        return id != null ? id : JavaRuntimeManager.DEFAULT_RUNTIME_ID;
    }

    @Override
    public File getJavaHome() throws FileNotFoundException {
        JavaRuntimeManager mgr = Lookup.getDefault().lookup(JavaRuntimeManager.class);
        Map<String, JavaRuntime> runtimes = mgr.getAvailableRuntimes();
        JavaRuntime rt = runtimes.get(runtimeId());

        return rt != null ? rt.getJavaHome() : null;
    }
}
