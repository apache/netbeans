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
package org.netbeans.modules.gradle;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.netbeans.modules.gradle.spi.GradleSettings;

/**
 *
 * @author lkishalmi
 */
public final class GradleModuleFileCache21 {
    private static final String CLASSIFIER_SOURCES = "sources"; //NOI18N
    private static final String CLASSIFIER_JAVADOC = "javadoc"; //NOI18N

    private static final String EXT_JAR = "jar"; //NOI18N
    private static final String EXT_POM = "pom"; //NOI18N

    private static final Path FILE_CACHE_BASE = Paths.get("caches", "modules-2", "files-2.1"); //NOI18N
    final Path cacheBaseDir;


    public final class CachedArtifactVersion {
        final Path path;
        final Map<String, Entry> entries = new HashMap<>();

        public final class Entry {
            final Path path;

            public Entry(String name, String hash) {
                this.path = CachedArtifactVersion.this.getPath().resolve(Paths.get(hash, name));
            }

            public Entry(Path path) throws IllegalArgumentException {
                if (!(path.startsWith(CachedArtifactVersion.this.path) && (path.getNameCount() - 2 == CachedArtifactVersion.this.path.getNameCount()))) {
                    throw new IllegalArgumentException("Not a Cached Gradle Atrifact: " + path.toString());
                }
                this.path = path;
            }

            public Path getPath() {
                return path;
            }

            public String getName() {
                return path.getFileName().toString();
            }

            public String getHash() {
                return path.getParent().toString();
            }

            public String getVersion() {
                return CachedArtifactVersion.this.getVersion();
            }
            
            public String getModule() {
                return CachedArtifactVersion.this.getModule();
            }

            public String getOrganization() {
                return CachedArtifactVersion.this.getOrganization();
            }

            public String getClassifier() {
                String ret = null;
                String prefix = getModule() + "-" + getVersion() + "-";
                String name = getName();
                if (name.startsWith(prefix)) {
                    int extDot = name.lastIndexOf('.');
                    if (extDot > prefix.length()) {
                        ret = name.substring(prefix.length(), extDot);
                    }
                }
                return ret;
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(this.getHash());
            }

            public CachedArtifactVersion getCachedArtifats() {
                return CachedArtifactVersion.this;
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
                final Entry other = (Entry) obj;
                if (!Objects.equals(this.getHash(), other.getHash())) {
                    return false;
                }
                return Objects.equals(getPath(), other.getPath());
            }
        }

        public CachedArtifactVersion(String organization, String module, String version) {
            path = cacheBaseDir.resolve(Paths.get(organization, module, version));
            try {
                readEntries();
            } catch (IOException ex) {
                //TODO: What
            }
        }

        public CachedArtifactVersion(Path path) throws IllegalArgumentException {
            if (!(path.startsWith(cacheBaseDir) && (path.getNameCount() - 3 == cacheBaseDir.getNameCount()))) {
                throw new IllegalArgumentException("Not a Cached Gradle Atrifact Version dir: " + path.toString());
            }
            this.path = path;
        }

        public Path getPath() {
            return path;
        }

        public String getVersion() {
            return path.getFileName().toString();
        }

        public String getModule() {
            return path.getParent().getFileName().toString();
        }

        public String getOrganization() {
            return path.getParent().getParent().getFileName().toString();
        }

        public Map<String, Entry> getEntries() {
            return Collections.unmodifiableMap(entries);
        }

        public Entry getSources() {
            return getClassifiedEntry(CLASSIFIER_SOURCES, EXT_JAR);
        }

        public Entry getJavaDoc() {
            return getClassifiedEntry(CLASSIFIER_JAVADOC, EXT_JAR);
        }

        public Entry getBinary() {
            return getClassifiedEntry(null, EXT_JAR);
        }

        public Entry getPom() {
            return getClassifiedEntry(null, EXT_POM);
        }

        public Entry getClassifiedEntry(String classifier, String extension) {
            String name = getModule() + "-" + getVersion() + (classifier != null ? "-" + classifier : "") + "." + extension;
            if(entries.isEmpty() && Files.exists(path)) {
                try {
                    readEntries();
                } catch (IOException ex) {}
            }
            return entries.get(name);
        }

        private void readEntries() throws IOException {
            try (Stream<Path> stream = Files.list(getPath())) {
                if (entries.size() != stream.count()) {
                    entries.clear();
                    Files.walkFileTree(getPath(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            try {
                                Entry entry = new Entry(file);
                                entries.put(entry.getName(), entry);
                            } catch(IllegalArgumentException ex) {
                                // Ignore non-artifact files, highly unlikely to happen
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
        }
    }

    GradleModuleFileCache21(Path gradleHome) {
        this.cacheBaseDir = gradleHome.resolve(FILE_CACHE_BASE);
    }

    public boolean contains(Path path) {
        return path.startsWith(cacheBaseDir);
    }

    public CachedArtifactVersion resolveCachedArtifactVersion(Path artifact) throws IllegalArgumentException {
        return artifact == null
                || artifact.getParent() == null
                || artifact.getParent().getParent() == null
                ?  null
                :  new CachedArtifactVersion(artifact.getParent().getParent());
    }

    public CachedArtifactVersion.Entry resolveEntry(Path artifact) throws IllegalArgumentException {
        CachedArtifactVersion av = resolveCachedArtifactVersion(artifact);
        return av != null ? av.entries.get(artifact.getFileName().toString()) : null;
    }

    public CachedArtifactVersion resolveModule(String moduleId) throws IllegalArgumentException {
        String[] gav = gavSplit(moduleId);
        return  new CachedArtifactVersion(gav[0], gav[1], gav[2]);
    }

    public static GradleModuleFileCache21 getGradleFileCache(Path gradleHome) {
        return new GradleModuleFileCache21(gradleHome);
    }

    public static GradleModuleFileCache21 getGradleFileCache() {
        return getGradleFileCache(GradleSettings.getDefault().getGradleUserHome().toPath());
    }

    public static String[] gavSplit(String gav) {
        // the general GAV format is - <group>:<artifact>:<version/snapshot>[:<classifier>][@extension]
        int firstColon = gav.indexOf(':'); // NOI18N
        int versionColon = gav.indexOf(':', firstColon + 1); // NOI18N
        int versionEnd = versionColon > firstColon ? gav.indexOf(':', versionColon + 1) : -1; // NO18N

        if (firstColon == -1 || versionColon == -1 || firstColon == versionColon) {
            throw new IllegalArgumentException("Invalid GAV format: '" + gav + "'"); //NOI18N
        }
        int end = versionEnd == -1 ? gav.length() : versionEnd;

        return new String[]{
            gav.substring(0, firstColon),
            gav.substring(firstColon + 1, versionColon),
            gav.substring(versionColon + 1, end)
        };
    }
}
