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
package org.netbeans.modules.project.dependency;

import java.util.Objects;
import org.openide.filesystems.FileObject;

/**
 * Describes a project dependency. Each project is identified by an unique ID
 * like path in Gradle or relative path from the outermost POM in a maven reactor. The ID
 * is build-system dependent. And each project has a directory represented by a FileObject.
 * 
 * @author sdedic
 */
public final class ProjectSpec {
    private final String projectId;
    private final FileObject location;

    private ProjectSpec(String projectId, FileObject location) {
        this.projectId = projectId;
        this.location = location;
    }

    public String getProjectId() {
        return projectId;
    }

    public FileObject getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.location);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectSpec other = (ProjectSpec) obj;
        return Objects.equals(this.location, other.location);
    }
    
    /**
     * Creates a project specification for a dependency.
     * @param projectId the project's unique ID
     * @param location the project's directory
     * @return ProjectSpec instance
     */
    public static ProjectSpec create(String projectId, FileObject location) {
        return new ProjectSpec(projectId, location);
    }
}
