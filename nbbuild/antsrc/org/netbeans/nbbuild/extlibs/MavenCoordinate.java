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
package org.netbeans.nbbuild.extlibs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;

class MavenCoordinate {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String extension;
    private final String classifier;

    private MavenCoordinate(String groupId, String artifactId, String version, String extension, String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.extension = extension;
        this.classifier = classifier;
    }

    public boolean hasClassifier() {
        return !classifier.isEmpty();
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getExtension() {
        return extension;
    }

    public String getClassifier() {
        return classifier;
    }

    /**
     * @return filename of the artifact by maven convention:
     *         {@code artifact-version[-classifier].extension}
     */
    public String toArtifactFilename() {
        return String.format("%s-%s%s.%s", getArtifactId(), getVersion(), hasClassifier() ? ("-" + getClassifier()) : "", getExtension());
    }

    /**
     * @return The repository path for an artifact by maven convention:
     *         {@code group/artifact/version/artifact-version[-classifier].extension}.
     *         In the group part all dots are replaced by a slash.
     */
    public String toMavenPath() {
        return String.format("%s/%s/%s/%s", getGroupId().replace(".", "/"), getArtifactId(), getVersion(), toArtifactFilename());
    }

    public static boolean isMavenFile(String gradleFormat) {
        if (gradleFormat.startsWith("http://") ||
            gradleFormat.startsWith("https://") ||
            gradleFormat.startsWith("file://")) {
            return false;
        }
        return gradleFormat.split(":").length > 2;
    }

    /**
     * The maven coordinate is supplied in the form:
     *
     * <p>{@code group:name:version:classifier@extension}</p>
     *
     * <p>For the DownloadBinaries task the parts group, name and version
     * are requiered. classifier and extension are optional. The extension
     * has a default value of "jar".
     *
     * @param gradleFormat artifact coordinated to be parse as a MavenCoordinate
     * @return
     * @throws IllegalArgumentException if provided string fails to parse
     */
    public static MavenCoordinate fromGradleFormat(String gradleFormat) {
        if (!isMavenFile(gradleFormat)) {
            throw new IllegalArgumentException("Supplied string is not in gradle dependency format: " + gradleFormat);
        }
        String[] coordinateExtension = gradleFormat.split("@", 2);
        String extension;
        String coordinate = coordinateExtension[0];
        if (coordinateExtension.length > 1 && (!coordinateExtension[1].trim().isEmpty())) {
            extension = coordinateExtension[1];
        } else {
            extension = "jar";
        }
        String[] coordinates = coordinate.split(":");
        String group = coordinates[0];
        String artifact = coordinates[1];
        String version = coordinates[2];
        String classifier = "";
        if (coordinates.length > 3) {
            classifier = coordinates[3].trim();
        }
        return new MavenCoordinate(group, artifact, version, extension, classifier);
    }

    /**
     * The maven coordinate is supplied in the form:
     *
     * <p>{@code m2:/group:name:version:extension:classifier}</p>
     *
     * <p>For the DownloadBinaries task the parts group, name, version and
     * extension are requiered. classifier is optional.
     *
     * @param m2Url artifact coordinated to be parse as a MavenCoordinate
     * @return
     * @throws IllegalArgumentException if provided string fails to parse
     */
    public static MavenCoordinate fromM2Url(URI m2Url) throws IOException {
        if (!"m2".equals(m2Url.getScheme())) {
            throw new IOException("Only m2 URL is supported: " + m2Url);
        }
        if (!m2Url.getRawPath().startsWith("/")) {
            throw new IOException("Invalid m2 URL. Expected format m2:/group:name:version:extension:classifier (classifier is optional)");
        }
        String[] coordinates = m2Url.getRawPath().substring(1).split(":");
        if (coordinates.length < 4) {
            throw new IOException("Invalid m2 URL. Expected format m2:/group:name:version:extension:classifier (classifier is optional)");
        }
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = URLDecoder.decode(coordinates[i], "UTF-8");
        }
        String group = coordinates[0];
        String artifact = coordinates[1];
        String version = coordinates[2];
        String extension = coordinates[3];
        String classifier = "";
        if (coordinates.length > 4) {
            classifier = coordinates[4];
        }
        return new MavenCoordinate(group, artifact, version, extension, classifier);
    }

    public URI toM2Url() {
        try {
            return new URI(String.format("m2:/%s:%s:%s:%s%s",
                    URLEncoder.encode(groupId, "UTF-8"),
                    URLEncoder.encode(artifactId, "UTF-8"),
                    URLEncoder.encode(version, "UTF-8"),
                    URLEncoder.encode(extension, "UTF-8"),
                    (!classifier.isEmpty()) ? (":" + URLEncoder.encode(classifier, "UTF-8")) : ""));
        } catch (URISyntaxException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
