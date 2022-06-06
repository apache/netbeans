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

import java.util.Objects;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;

/**
 * Represents an artifact. Each artifact is identified by
 * <ul>
 * <li>group or organization id
 * <li>artifact id
 * <li>version
 * <li>(optional) classifier; no classifier shall be interpreted as a
 * regular build artifact
 * <li>(optional) type; not type shall be interepreted as the default type
 * for the processing compiler or builder
 * </ul>
 * The version specified is further classified by {@link VersionKind}, to 
 * distinguish versions possibly from repositories, development versions and
 * floating versions.
 * 
 * @author sdedic
 */
public final class ArtifactSpec<T> {
    /**
     * Kind of the artifact version
     */
    public enum VersionKind {
        /**
         * Regular publishable artifact
         */
        REGULAR, 
        
        /**
         * Snapshot artifact
         */
        SNAPSHOT
    };
    
    private final VersionKind kind;
    private final String type;
    private final String groupId;
    private final String artifactId;
    private final String versionSpec;
    private final String classifier;
    private final boolean optional;
    private final FileObject localFile;
    final T data;

    ArtifactSpec(VersionKind kind, String groupId, String artifactId, String versionSpec, String type, String classifier, boolean optional, FileObject localFile, T impl) {
        this.kind = kind;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionSpec = versionSpec;
        this.classifier = classifier;
        this.optional = optional;
        this.data = impl;
        this.type = type;
        this.localFile = localFile;
    }

    public T getData() {
        return data;
    }

    public FileObject getLocalFile() {
        return localFile;
    }

    public VersionKind getKind() {
        return kind;
    }

    public String getType() {
        return type;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersionSpec() {
        return versionSpec;
    }

    public String getClassifier() {
        return classifier;
    }

    public boolean isOptional() {
        return optional;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.kind);
        hash = 79 * hash + Objects.hashCode(this.type);
        hash = 79 * hash + Objects.hashCode(this.groupId);
        hash = 79 * hash + Objects.hashCode(this.artifactId);
        hash = 79 * hash + Objects.hashCode(this.versionSpec);
        hash = 79 * hash + Objects.hashCode(this.classifier);
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
        final ArtifactSpec<?> other = (ArtifactSpec<?>) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.groupId, other.groupId)) {
            return false;
        }
        if (!Objects.equals(this.artifactId, other.artifactId)) {
            return false;
        }
        if (!Objects.equals(this.versionSpec, other.versionSpec)) {
            return false;
        }
        if (!Objects.equals(this.classifier, other.classifier)) {
            return false;
        }
        return this.kind == other.kind;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder(
            String.format("%s:%s:%s", getGroupId(), getArtifactId(), getVersionSpec() == null ? "" : getVersionSpec())
        );
        if (classifier != null) {
            sb.append(":").append(classifier);
        }
        if (type != null) {
            sb.append("[").append(type).append("]");
        }
        if (optional) {
            sb.append("?");
        }
        return sb.toString();
    }
    
    /**
     * Returns opaque project-specific data. If searching for
     * a project-specific extension, use {@link ProjectDependencies#findAdapters} instead.
     * 
     * @return unspecified underlying project data
     */
    public T getProjectData() {
        return data;
    }
    
    public static <V> ArtifactSpec<V> createVersionSpec(
            @NonNull String groupId, @NonNull String artifactId, 
            @NullAllowed String type, @NullAllowed String classifier, 
            @NonNull String versionSpec, boolean optional, @NullAllowed FileObject localFile, @NonNull V data) {
        return new ArtifactSpec<V>(VersionKind.REGULAR, groupId, artifactId, versionSpec, type, classifier, optional, localFile, data);
    }

    public static <V> ArtifactSpec<V> createSnapshotSpec(
            @NonNull String groupId, @NonNull String artifactId, 
            @NullAllowed String type, @NullAllowed String classifier, 
            @NonNull String versionSpec, boolean optional, @NullAllowed FileObject localFile, @NonNull V data) {
        return new ArtifactSpec<V>(VersionKind.SNAPSHOT, groupId, artifactId, versionSpec, type, classifier, optional, localFile, data);
    }

}
