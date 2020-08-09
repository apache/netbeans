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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Task to check sha1 from named files (generally  binaries such as ZIPs)
 * from a repository.
 */
public class CheckEmbeddedBinaries extends Task {

    private File dir;

    /**
     * Location of unzippped jar folder to be tested.
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    private File shalist;

    /**
     * List of chechcksum and coordinate
     * @param shaList
     */
    public void setShaList(File shaList) {
        this.shalist = shaList;
    }

    @Override
    public void execute() throws BuildException {
        boolean success = true;

        File manifest = shalist;
        Map<String,String> shamap = new HashMap<>();
        log("Scanning: " + manifest, Project.MSG_VERBOSE);
        try {
            try (InputStream is = new FileInputStream(manifest)) {
                BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.trim().length() == 0) {
                        continue;
                    }
                    String[] hashAndFile = line.split(";", 2);
                    if (hashAndFile.length < 2) {
                        throw new BuildException("Bad line '" + line + "' in " + manifest, getLocation());
                    }

                    if (MavenCoordinate.isMavenFile(hashAndFile[1])) {
                        MavenCoordinate mc = MavenCoordinate.fromGradleFormat(hashAndFile[1]);
                        shamap.put(hashAndFile[0], hashAndFile[1]);
                    } else {
                        throw new BuildException("Invalid manifest entry should be Maven coordinate", getLocation());
                    }
                }
            }
        } catch (IOException x) {
            throw new BuildException("Could not open " + manifest + ": " + x, x, getLocation());

        }
        try {
            StringBuilder errorList = new StringBuilder();
            Files.list(dir.toPath())
                    .forEach((t) -> {
                        String sha1 = hash(t.toFile());
                        if (!shamap.containsKey(sha1)) {
                            errorList.append("No sha1 (expected ").append(sha1).append(" for file: ").append(t).append("\n");
                        }                          
                    });
            if (errorList.toString().length()>0) {
                log(""+errorList.toString());
                throw new BuildException("Missing Sha1 file", getLocation());
            }
        } catch (IOException ex) {
            throw new BuildException("Invalid manifest entry should be Maven coordinate", getLocation());
        }
        if (!success) {
            throw new BuildException("Failed to download binaries - see log message for the detailed reasons.", getLocation());
        }
    }

    private String hash(File f) {
        try {
            try (FileInputStream is = new FileInputStream(f)) {
                return hash(is);
            }
        } catch (IOException x) {
            throw new BuildException("Could not get hash for " + f + ": " + x, x, getLocation());
        }
    }

    private String hash(InputStream is) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException x) {
            throw new BuildException(x, getLocation());
        }
        byte[] buf = new byte[4096];
        int r;
        while ((r = is.read(buf)) != -1) {
            digest.update(buf, 0, r);
        }
        return String.format("%040X", new BigInteger(1, digest.digest()));
    }

    static class MavenCoordinate {

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
            return (!classifier.isEmpty());
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
         * {@code artifact-version[-classifier].extension}
         */
        public String toArtifactFilename() {
            return String.format("%s-%s%s.%s",
                    getArtifactId(),
                    getVersion(),
                    hasClassifier() ? ("-" + getClassifier()) : "",
                    getExtension()
            );
        }

        /**
         * @return The repository path for an artifact by maven convention:
         * {@code group/artifact/version/artifact-version[-classifier].extension}.
         * In the group part all dots are replaced by a slash.
         */
        public String toMavenPath() {
            return String.format("%s/%s/%s/%s",
                    getGroupId().replace(".", "/"),
                    getArtifactId(),
                    getVersion(),
                    toArtifactFilename()
            );
        }

        public static boolean isMavenFile(String gradleFormat) {
            return gradleFormat.split(":").length > 2;
        }

        /**
         * The maven coordinate is supplied in the form:
         *
         * <p>
         * {@code group:name:version:classifier@extension}</p>
         *
         * <p>
         * For the DownloadBinaries task the parts group, name and version are
         * requiered. classifier and extension are optional. The extension has a
         * default value of "jar".
         *
         * @param gradleFormat artifact coordinated to be parse as a
         * MavenCoordinate
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
            if (coordinateExtension.length > 1
                    && (!coordinateExtension[1].trim().isEmpty())) {
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
    }
}
