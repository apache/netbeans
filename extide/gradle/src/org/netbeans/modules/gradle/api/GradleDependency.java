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

package org.netbeans.modules.gradle.api;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.netbeans.modules.gradle.GradleModuleFileCache21;

/**
 * This object represents a Gradle dependency element in a {@link GradleConfiguration}.
 *
 * @since 1.0
 * @author Laszlo Kishalmi
 */
public abstract class GradleDependency implements Serializable, Comparable<GradleDependency> {

    /** The type of a Dependency. */
    public static enum Type {
        /** Dependency type for modules which was not able to be resolved by Gradle. */
        UNRESOLVED,
        /** Dependency type for sub-project in the current multi-project setup. */
        PROJECT,
        /** Dependency type for modules usually downloaded from a remote repository. */
        MODULE,
        /** Dependency type for files available on local filesystem. */
        FILE
    }

    final String id;

    GradleDependency(String id) {
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    @Override
    public int compareTo(GradleDependency o) {
        int ret = getType().ordinal() - o.getType().ordinal();
        return ret != 0 ? ret : id.compareToIgnoreCase(o.id);
    }

    public abstract Type getType();

    /**
     * Dependency for modules usually downloaded from a remote repository.
     * @since 1.0
     */
    public static final class ModuleDependency extends GradleDependency {

        final Set<File> artifacts;
        Set<File> sources;
        Set<File> javadoc;
        String group;
        String name;
        String version;

        ModuleDependency(String id, Set<File> artifacts) {
            super(id);
            this.artifacts = artifacts;
            String[] parts = GradleModuleFileCache21.gavSplit(id);
            group = parts[0];
            name = parts[1];
            version = parts[2];
        }

        public Set<File> getArtifacts() {
            return artifacts != null ? artifacts : Collections.<File>emptySet();
        }

        public Set<File> getSources() {
            return sources != null ? sources : Collections.<File>emptySet();
        }

        public Set<File> getJavadoc() {
            return javadoc != null ? javadoc : Collections.<File>emptySet();
        }

        public String getGroup() {
            return group;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public boolean hasSources() {
            return !getSources().isEmpty();
        }

        public boolean hasJavadocs() {
            return !getJavadoc().isEmpty();
        }

        @Override
        public int compareTo(GradleDependency o) {
            int ret = getType().ordinal() - o.getType().ordinal();
            if (ret == 0) {
                ModuleDependency dep = (ModuleDependency) o;
                ret = name.compareToIgnoreCase(dep.name);
                ret = ret != 0 ? ret : version.compareTo(dep.version);
                ret = ret != 0 ? ret : group.compareToIgnoreCase(dep.group);
            }
            return ret;
        }

        @Override
        public Type getType() {
            return Type.MODULE;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ModuleDependency other = (ModuleDependency) obj;
            return Objects.equals(this.id, other.id);
        }

        @Override
        public String toString() {
            return "ComponentDependency{" + "artifacts=" + artifacts + ", sources=" + sources + ", javadoc=" + javadoc + '}';
        }

    }

    /**
     * Dependency for sub-project in the current multi-project setup.
     * @since 1.0
     */
    public static final class ProjectDependency extends GradleDependency {

        final File path;
        String description;

        ProjectDependency(String id, File path) {
            super(id);
            this.path = path;
        }

        public File getPath() {
            return path;
        }

        public String getDescription() {
            return description != null ? description : id;
        }

        @Override
        public Type getType() {
            return Type.PROJECT;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.id);
            hash = 37 * hash + Objects.hashCode(this.path);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProjectDependency other = (ProjectDependency) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return Objects.equals(this.path, other.path);
        }

        @Override
        public String toString() {
            return "ProjectDependency{" + "path=" + path + ", description=" + description + '}';
        }

    }

    /**
     * Dependency for files available on local filesystem.
     * @since 1.0
     */
    public static final class FileCollectionDependency extends GradleDependency {

        final Set<File> files;

        FileCollectionDependency(Set<File> files) {
            super("Files");
            this.files = files;
        }

        @Override
        public Type getType() {
            return Type.FILE;
        }

        public Set<File> getFiles() {
            return files;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 73 * hash + Objects.hashCode(this.files);
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
            final FileCollectionDependency other = (FileCollectionDependency) obj;
            return Objects.equals(this.files, other.files);
        }


    }

    /**
     * Dependency for modules which was not able to be resolved by Gradle.
     */
    public static final class UnresolvedDependency extends GradleDependency {

        String problem;

        UnresolvedDependency(String id) {
            super(id);
        }

        public String getProblem() {
            return problem;
        }

        @Override
        public Type getType() {
            return Type.UNRESOLVED;
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UnresolvedDependency other = (UnresolvedDependency) obj;
            return Objects.equals(this.id, other.id);
        }

        @Override
        public String toString() {
            return "UnresolvedDependency{" + "problem=" + problem + '}';
        }

    }
}
