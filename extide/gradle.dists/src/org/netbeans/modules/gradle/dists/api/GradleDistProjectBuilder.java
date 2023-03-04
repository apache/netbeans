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
package org.netbeans.modules.gradle.dists.api;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lkishalmi
 */
class GradleDistProjectBuilder  {

    @ServiceProvider(service = ProjectInfoExtractor.class, position = Integer.MIN_VALUE)
    @SuppressWarnings("rawtypes")
    public static final class Extractor implements ProjectInfoExtractor {

        @Override
        public ProjectInfoExtractor.Result extract(Map<String, Object> props, Map<Class, Object> otherInfo) {
            Set<String> dists = (Set<String>) props.get("distributions");
            File projectDir = (File) props.get("project_projectDir");
            return (projectDir != null) && (dists != null) ? new ProjectInfoExtractor.DefaultResult(new GradleDistProject(projectDir, dists)) : Result.NONE;
        }

        @Override
        public ProjectInfoExtractor.Result fallback(GradleFiles files) {
            File src = new File(files.getProjectDir(), "src"); //NOI18N
            Set<String> dists = new HashSet<>();
            if (src.isDirectory()) {
                for (File dist : src.listFiles(f -> f.isDirectory())) {
                    if (new File(dist, "dist").isDirectory()) {
                        dists.add(dist.getName());
                    }
                }
            }
            return !dists.isEmpty() ? new ProjectInfoExtractor.DefaultResult(new GradleDistProject(files.getProjectDir(), dists)) : Result.NONE;
        }

    }

    
}
