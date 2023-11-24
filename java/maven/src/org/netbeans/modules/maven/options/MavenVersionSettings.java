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
package org.netbeans.modules.maven.options;

import java.util.Map;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;

import static java.util.Map.entry;

/**
 * Utility class for internal artifact version queries.
 *
 * Will usually return the latest known version.
 *
 * @author mbien
 */
public final class MavenVersionSettings {

    @Deprecated    
    public static final String VERSION_COMPILER = Constants.PLUGIN_COMPILER; //NOI18N
    @Deprecated    
    public static final String VERSION_RESOURCES = Constants.PLUGIN_RESOURCES; //NOI18N

    private static final Map<String, String> fallback;

    static {
        // TODO update periodically - modifications might require unit test adjustments
        String nb_version = "RELEASE200";
        String nb_utilities_version = "14.0";
        fallback = Map.ofEntries(
            entry(key("org.netbeans.api", "org-netbeans-modules-editor"), nb_version), // represents all other nb artifacts
            entry(key(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER), "3.11.0"),
            entry(key(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES), "3.3.1"),
            entry(key(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_FAILSAFE), "3.2.2"),
            entry(key(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_SUREFIRE), "3.2.2"),
            entry(key("org.apache.netbeans.utilities", "utilities-parent"), nb_utilities_version),
            entry(key("org.apache.netbeans.utilities", "nbm-maven-harness"), nb_utilities_version),
            entry(key("org.apache.netbeans.utilities", "nbm-shared"), nb_utilities_version),
            entry(key("org.apache.netbeans.utilities", "nbm-repository-plugin"), nb_utilities_version),
            entry(key("org.apache.netbeans.utilities", "nbm-maven-plugin"), nb_utilities_version),
            entry(key("org.apache.netbeans.archetypes", "nbm-archetype"), "1.18"),
            entry(key("org.apache.netbeans.archetypes", "netbeans-platform-app-archetype"), "1.23")
        );
    }

    private static final MavenVersionSettings INSTANCE = new MavenVersionSettings();

    private MavenVersionSettings() {}

    public static MavenVersionSettings getDefault() {
        return INSTANCE;
    }

    public String getNBVersion() {
        return getVersion("org.netbeans.api", "org-netbeans-modules-editor");
    }

    @Deprecated
    public String getVersion(String artifactId) {
        return getVersion(Constants.GROUP_APACHE_PLUGINS, artifactId);
    }

    public String getVersion(String groupId, String artifactId) {
        String key = key(groupId, artifactId);
        return queryLatestKnownArtifactVersion(groupId, artifactId, fallback.get(key));
    }

    // non blocking query, might not succeed if index not available
    private static String queryLatestKnownArtifactVersion(String gid, String aid, String min) {
        RepositoryQueries.Result<NBVersionInfo> query = RepositoryQueries.getVersionsResult(gid, aid, null);
        // Versions are sorted in descending order
        return query.getResults().stream()
                    .map(NBVersionInfo::getVersion)
                    .filter(v -> !v.endsWith("-SNAPSHOT"))
                    .findFirst()
                    .filter(v -> min == null || new ComparableVersion(v).compareTo(new ComparableVersion(min)) > 0) // don't downgrade
                    .orElse(min);
    }

    private static String key(String gid, String aid) {
        if (gid == null || gid.isBlank()) {
            throw new IllegalArgumentException("empty group id");
        }
        if (aid == null || aid.isBlank()) {
            throw new IllegalArgumentException("empty artifact id");
        }
        return gid + ":" + aid;
    }

}
