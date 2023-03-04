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

package org.netbeans.modules.maven;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.classpath.DependencyProjectsProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Utilities;

/**
 * finds subprojects (projects this one depends on) that are locally available
 * and can be build as one unit. Uses maven multiproject infrastructure. (maven.multiproject.includes)
 * @author  Milos Kleint
 */
@ProjectServiceProvider(service=DependencyProjectsProvider.class, projectType="org-netbeans-modules-maven")
public class DependencyProviderImpl implements DependencyProjectsProvider {

    private final Project project;

    public DependencyProviderImpl(Project proj) {
        project = proj;
    }


    @Override
    public Set<Pair> getDependencyProjects() {
        Set<Pair> projects = new HashSet<Pair>();
        addKnownOwners(projects);
        return projects;
    }

    private void addKnownOwners(Set<Pair> resultset) {
        Set<Artifact> artifacts = project.getLookup().lookup(NbMavenProject.class).getMavenProject().getArtifacts();
        for (Artifact ar : artifacts) {
            File f = ar.getFile();
            if (f != null) {
                Project p = MavenFileOwnerQueryImpl.getInstance().getOwner(Utilities.toURI(f));
                if (p == project) {
                    continue;
                }
                if (p != null) {
                    resultset.add(new Pair(p, ar));
                }
            }
        }
    }    
}
