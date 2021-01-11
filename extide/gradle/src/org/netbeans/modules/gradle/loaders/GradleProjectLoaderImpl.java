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
package org.netbeans.modules.gradle.loaders;

import org.netbeans.modules.gradle.loaders.FallbackProjectLoader;
import org.netbeans.modules.gradle.loaders.LegacyProjectLoader;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.GradleProjectLoader;
import org.netbeans.modules.gradle.api.NbGradleProject;

/**
 *
 * @author lkishalmi
 */
public class GradleProjectLoaderImpl implements GradleProjectLoader {

    final Project project;

    public GradleProjectLoaderImpl(Project project) {
        this.project = project;
    }

    @Override
    public GradleProject loadProject(NbGradleProject.Quality aim, boolean ignoreCache, boolean interactive, String... args) {
        List<GradleProjectLoader> loaders = Arrays.asList(
                new LegacyProjectLoader(project),
                new FallbackProjectLoader(project)
        );

        GradleProject ret = null;
        for (GradleProjectLoader loader : loaders) {
            ret = loader.loadProject(aim, ignoreCache, interactive, args);
            if (ret != null) {
                break;
            }
        }
        return ret;
    }
}
