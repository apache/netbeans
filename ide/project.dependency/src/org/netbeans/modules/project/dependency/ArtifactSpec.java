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
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
 * <p>
 * The ArtifactSpec may provide additional tags, that can further describe the artifact,
 * but those tags are not part of "identity" of the artifact, for dependencies or build
 * systems, only 
 * <ul>
 * <li>group
 * <li>artifact
 * <li>version
 * <li>classifier
 * <Li>extension
 * </ul>
 * are important.
 * 
 * @author sdedic
 */
public final class ArtifactSpec<T> {
    
    /**
     * A tag for an artifact with basic output of the project's code/contents.
     * You almost never want this, usually you want {@code null} classifier to 
     * identify the <b>default</b> output. But in rare cases you really do want
     * to avoid post-processing or shading, this (abstract) classifier should
     * identify an artifact before those steps.
     * <p>
     * If used in a query, a non-tagged artifact may be still returned if the implementation
     * does not support the tag.
     */
    public static final String TAG_BASE = "<basic>"; // NOI18N
    
    /**
     * Tag for an artifact, that eventually contains dependencies bundled in. If used
     * in a query, an ordinary (non-tagged) artifact may be returned from the query in case
     * the implementation does not support the tag. Implementations may use additional, more
     * specific tags on the returned artifacts.
     */
    public static final String TAG_SHADED = "<shaded>";

    /**
     * Classifier for an artifact that contains sources.
     */
    public static final String CLASSIFIER_SOURCES = "sources"; // NOI18N

    /**
     * Classifier for an artifact that contains test code
     */
    public static final String CLASSIFIER_TESTS = "tests"; // NOI18N

    /**
     * Classifier for an artifact that contains test sources.
     */
    public static final String CLASSIFIER_TEST_SOURCES = "test-sources"; // NOI18N
    
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
    
    // note: tags is NOT a part of hascode / equals, as externally only the classifier
    // is visible, e.g. to the build system.
    private final Set<String> tags;
    private FileObject localFile;
    final T data;

    ArtifactSpec(VersionKind kind, String groupId, String artifactId, String versionSpec, String type, String classifier, boolean optional, URI location, FileObject localFile, Set<String> tags, T impl) {
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
        this.tags = tags == null ? Collections.emptySet() : tags;
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
     * @return local file, if it exists.
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
        // note: config root is used as 'nothing is here' marker
        return f == FileUtil.getConfigRoot() ? null : f;
    }
    
    /**
     * Checks if the artifact has the specific tag. Tags are optional indicators of artifact's purpose or 
     * characteristics, they are typically technology and/or build system specific. Two 'abstract' tags
     * are defined (implementations may use additional more specific tags, too):
     * <ul>
     * <li>{@link #TAG_BASE} for product of a project, and
     * <li>{@link #TAG_SHADED} for a bundled product (e.g. with dependencies).
     * </ul>
     * The exact meaning is build-system specific.
     * <p>
     * Tags are typically not used in dependency specifications.
     * 
     * @param tag the tag to test
     * @return true, if the artifact is tagged.
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
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
     * Returns opaque project-specific data. You must use project-specific API to
     * extract information that may be linked in here.
     * 
     * @return unspecified underlying project data
     */
    public T getProjectData() {
        return data;
    }
    
    public static <V> ArtifactSpec<V> createVersionSpec(
            @NullAllowed String groupId, @NonNull String artifactId, 
            @NullAllowed String type, @NullAllowed String classifier, 
            @NullAllowed String versionSpec, boolean optional, @NullAllowed FileObject localFile, @NonNull V data) {
        URL u = localFile == null ? null : URLMapper.findURL(localFile, URLMapper.EXTERNAL);
        URI uri = null;
        if (u != null) {
            try {
                uri = u.toURI();
            } catch (URISyntaxException ex) {
                // should not happen
            }
        }
        return new ArtifactSpec<V>(VersionKind.REGULAR, groupId, artifactId, versionSpec, type, classifier, optional, uri, localFile, Collections.emptySet(), data);
    }
    
    /**
     * Creates a partial artifact specification, usable as a description. The artifact does not contain all the metadata, but serves as a match
     * for artifacts managed by the build system.
     * @param groupId
     * @param artifactId
     * @return spec instance
     * @since 1.7
     */
    public static ArtifactSpec make(String groupId, String artifactId) {
        return createVersionSpec(groupId, artifactId, null, null, null, false, null, null);
    }

    /**
     * Creates a partial artifact specification, usable as a description. The artifact does not contain all the metadata, but serves as a match
     * for artifacts managed by the build system.
     * @param groupId group ID
     * @param artifactId artifact ID
     * @param versionSpec version
     * @return spec instance
     * @since 1.7
     */
    public static ArtifactSpec make(String groupId, String artifactId, String versionSpec) {
        return createVersionSpec(groupId, artifactId, null, null, versionSpec, false, null, null);
    }

    public static <V> ArtifactSpec<V> createSnapshotSpec(
            @NullAllowed String groupId, @NullAllowed String artifactId, 
            @NullAllowed String type, @NullAllowed String classifier, 
            @NullAllowed String versionSpec, boolean optional, @NullAllowed FileObject localFile, @NonNull V data) {
        URI uri = null;
        if (localFile != null) {
            URL u = URLMapper.findURL(localFile, URLMapper.EXTERNAL);
            if (u != null) {
                try {
                    uri = u.toURI();
                } catch (URISyntaxException ex) {
                    // should not happen
                }
            }
        }
        return new ArtifactSpec<V>(VersionKind.SNAPSHOT, groupId, artifactId, versionSpec, type, classifier, optional, uri, localFile, Collections.emptySet(), data);
    }
    
    public static final <T> Builder<T> describe(String group, String artifact) {
        return new Builder(group, artifact, null, null);
    }
    
    public static final <T> Builder<T> builder(String group, String artifact, String version, T projectData) {
        return new Builder(group, artifact, version, projectData);
    }

    public final static class Builder<T> {
        private final T data;
        private final String groupId;
        private final String artifactId;
        private String versionSpec;
        private VersionKind kind = VersionKind.REGULAR;
        private String type;
        private String classifier;
        private boolean optional;
        private FileObject localFile;
        private URI location;
        private Set<String> tags;
        
        public Builder(String groupId, String artifactId, String versionSpec, T data) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.versionSpec = versionSpec;
            this.data = data;
        }
        
        public Builder versionKind(VersionKind kind) {
            this.kind = kind;
            return this;
        }
        
        public Builder version(String versionSpec) {
            this.versionSpec = versionSpec;
            return this;
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
        
        public Builder tag(String tag) {
            if (tags == null) {
                tags = new HashSet<>();
            }
            tags.add(tag);
            return this;
        }
        
        public Builder tags(String... tags) {
            if (tags == null || tags.length == 0) {
                return this;
            } else {
                for (String t : tags) {
                    tag(t);
                }
                return this;
            }
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
            // note: config root is used as 'nothing is here' marker
            this.localFile = localFile == null ? FileUtil.getConfigRoot() : localFile;
            return this;
        }

        public Builder location(URI location) {
            this.location = location;
            return this;
        }
        
        public ArtifactSpec build() {
            return new ArtifactSpec(kind, groupId, artifactId, versionSpec, type, classifier, optional, location, localFile, tags, data);
        }
    }
}
