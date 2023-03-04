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
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.api.NbGradleProject;

/**
 *
 * @author lkishalmi
 */
public final class GradleDistProject {

    final File projectDir;
    final Set<String> distributions;

    GradleDistProject(File projectDir, Set<String> distributions) {
        this.projectDir = projectDir;
        this.distributions = distributions;
    }

    public Set<String> getDistributions() {
        return distributions != null ? distributions : Collections.emptySet();
    }
    
    public Set<String> getAvailableDistributions() {
        return getDistributions().stream().filter((String dist) -> getDistributionSource(dist).isDirectory()).collect(Collectors.toSet());
    }
    
    public File getDistributionSource(String distribution) {
        File src = new File(projectDir, "src"); //NOI18N
        File dist = new File(src, distribution);
        return new File(dist, "dist"); //NOI18N
    }
    
    public static GradleDistProject get(Project project) {
        NbGradleProject gp = NbGradleProject.get(project);
        return gp != null ? gp.projectLookup(GradleDistProject.class) : null;
    }
}
