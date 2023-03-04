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
package org.netbeans.modules.gradle.loaders;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.GradleProject;
import org.netbeans.modules.gradle.api.GradleReport;
import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import org.openide.util.Lookup;

/**
 *
 * @author lkishalmi
 */
//@ProjectServiceProvider(service = GradleProjectLoader.class, projectType = NbGradleProject.GRADLE_PROJECT_TYPE)
public class FallbackProjectLoader extends AbstractProjectLoader {


    final GradleFiles files;

    FallbackProjectLoader(ReloadContext ctx) {
        super(ctx);
        this.files = ctx.project.getGradleFiles();
    }

    @Override
    public GradleProject load() {
        return createFallbackProject(files);
    }

    public static GradleProject createFallbackProject(GradleFiles files) {
        Collection<? extends ProjectInfoExtractor> extractors = Lookup.getDefault().lookupAll(ProjectInfoExtractor.class);
        Map<Class, Object> infos = new HashMap<>();
        Set<GradleReport> problems = new LinkedHashSet<>();

        for (ProjectInfoExtractor extractor : extractors) {
            ProjectInfoExtractor.Result result = extractor.fallback(files);
            for (String s : result.getProblems()) {
                problems.add(GradleProject.createGradleReport(files.getBuildScript().toPath(), s));
            }
            for (Object extract : result.getExtract()) {
                infos.put(extract.getClass(), extract);
            }

        }
        return new GradleProject(NbGradleProject.Quality.FALLBACK, problems, infos.values());        
    }
    
    @Override
    boolean isEnabled() {
        return true;
    }

    @Override
    boolean needsTrust() {
        return false;
    }
}
