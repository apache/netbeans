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
package org.netbeans.modules.project.dependency;

import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.project.Project;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.netbeans.modules.project.dependency.spi.ProjectDependenciesImplementation;
import org.openide.filesystems.FileObject;

/**
 * Project Query that collects dependencies using project-specific services.
 * @author sdedic
 */
public class ProjectDependencies {
    
    public static ArtifactSpec getProjectArtifact(Project target) {
        ProjectDependenciesImplementation pds = target.getLookup().lookup(ProjectDependenciesImplementation.class);
        if (pds == null) {
            return null;
        }
        return pds.getProjectArtifact();
    }
    
    public static DependencyResult findDependencies(Project target, Dependency.Filter filterOrNull, Scope... scopes) throws ProjectOperationException {
        ProjectDependenciesImplementation pds = target.getLookup().lookup(ProjectDependenciesImplementation.class);
        if (pds == null) {
            return null;
        } else {
            return pds.findDependencies(
                    scopes == null ? Collections.singletonList(Scopes.COMPILE) : Arrays.asList(scopes), 
                    filterOrNull != null ? filterOrNull : (s, a) -> true);
        }
    }
}
