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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

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
    static final Logger LOG = Logger.getLogger(ProjectDependencies.class.getName());
    
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
    private final URI location;
    private FileObject localFile;
    final T data;

    ArtifactSpec(VersionKind kind, String groupId, String artifactId, String versionSpec, String type, String classifier, boolean optional, URI location, FileObject localFile, T impl) {
        this.kind = kind;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.versionSpec = versionSpec;
        this.classifier = classifier;
        this.optional = optional;
        this.data = impl;
        this.type = type;
        this.location = location;
        this.localFile = localFile;
    }

    public T getData() {
        return data;
    }

    /**
     * Returns local file which the artifact represents. For library (dependencies) artifacts,
     * the file is the library consumed, e.g. from a local repository. For outputs, the artifact
     * represents the build output, usually in project's build directory. Note that FileObject for 
     * a dependency and its corresponding Project may not be the same.
     * 
     * @return 
     */
    public FileObject getLocalFile() {
        // It's not locked well, but localFile will eventually become non-null, even though
        // more lookups could be needed under contention.
        FileObject f = localFile;
        if (f == null) {
            if (location != null) {
                try {
                    synchronized (this) {
                        return this.localFile = URLMapper.findFileObject(location.toURL());
                    }
                } catch (MalformedURLException ex) {
                    LOG.log(Level.WARNING, "Artifact location cannot be converted to URL: {0}", location);
                }
                f = localFile = FileUtil.getConfigRoot();
            }
        }
        return f == FileUtil.getConfigRoot() ? null : f;
    }

    public URI getLocation() {
        return location;
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
        hash = 79 * hash + Objects.hashCode(this.location);
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
        if (!Objects.equals(this.location, other.location)) {
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
            @NullAllowed String groupId, @NonNull String artifactId, 
            @NullAllowed String type, @NullAllowed String classifier, 
            @NonNull String versionSpec, boolean optional, @NullAllowed FileObject localFile, @NonNull V data) {
        URL u = localFile == null ? null : URLMapper.findURL(localFile, URLMapper.EXTERNAL);
        URI uri = null;
        if (u != null) {
            try {
                uri = u.toURI();
            } catch (URISyntaxException ex) {
                // should not happen
            }
        }
        return new ArtifactSpec<V>(VersionKind.REGULAR, groupId, artifactId, versionSpec, type, classifier, optional, uri, localFile, data);
    }

    public static <V> ArtifactSpec<V> createSnapshotSpec(
            @NonNull String groupId, @NonNull String artifactId, 
            @NullAllowed String type, @NullAllowed String classifier, 
            @NonNull String versionSpec, boolean optional, @NullAllowed FileObject localFile, @NonNull V data) {
        URL u = URLMapper.findURL(localFile, URLMapper.EXTERNAL);
        URI uri = null;
        if (u != null) {
            try {
                uri = u.toURI();
            } catch (URISyntaxException ex) {
                // should not happen
            }
        }
        return new ArtifactSpec<V>(VersionKind.SNAPSHOT, groupId, artifactId, versionSpec, type, classifier, optional, uri, localFile, data);
    }
    
    public static final <T> Builder<T> builder(String group, String artifact, String version, T projectData) {
        return new Builder(group, artifact, version, projectData);
    }

    public final static class Builder<T> {
        private final T data;
        private final String groupId;
        private final String artifactId;
        private final String versionSpec;
        private VersionKind kind = VersionKind.REGULAR;
        private String type;
        private String classifier;
        private boolean optional;
        private FileObject localFile;
        private URI location;
        
        public Builder(String groupId, String artifactId, String versionSpec, T data) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.versionSpec = versionSpec;
            this.data = data;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder classifier(String classifier) {
            this.classifier = classifier;
            return this;
        }

        public Builder optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder localFile(FileObject localFile) {
            this.localFile = localFile;
            return this;
        }

        /**
         * Forces the local file reference. Unlike {@link #localFile}, if {@code null} is
         * passed, the {@link ArtifactSpec#getLocalFile()} will not attempt to resole the URI
         * to a FileObject. Might be useful to indicate that no file was known <b>at the time 
         * of the ArtifactSpec creation</b>
         * @param localFile the local file, {@code null} to disallows URI to FileObject implicit conversion.
         * @return builder instance.
         */
        public Builder forceLocalFile(FileObject localFile) {
            this.localFile = localFile == null ?FileUtil.getConfigRoot() : localFile;
            return this;
        }

        public Builder location(URI location) {
            this.location = location;
            return this;
        }
        
        public ArtifactSpec build() {
            return new ArtifactSpec(kind, groupId, artifactId, versionSpec, type, classifier, optional, location, localFile, data);
        }
    }
}
