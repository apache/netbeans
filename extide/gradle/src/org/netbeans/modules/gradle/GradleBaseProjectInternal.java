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
package org.netbeans.modules.gradle;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.netbeans.modules.gradle.spi.ProjectInfoExtractor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Values that could be eventually exposed through GradleBaseProject or other APIs,
 * but are currently not, but they are useful for e.g. tests.
 * @author sdedic
 */
public class GradleBaseProjectInternal {
    /**
     * Gradle version for fallback info, or when the version info was not reported (assuming some error).
     * The version should be old enough to fail all reasonable later-than checks.
     */
    public static final String VERSION_UNKNOWN = "1.0"; // NOI18N
    
    /**
     * Gradle version that actually loaded the project.
     */
    private final String gradleVersion;
    
    /**
     * Gradle home directory
     */
    private final File gradleHome;

    /**
     * Returns version of Gradle that has loaded the project information. Returns 
     * @return 
     */
    public String getGradleVersion() {
        return gradleVersion;
    }

    /**
     * Returns the gradle home directory. May return {@code null}.
     * @return gradle home directory, or {@code null}.
     */
    public File getGradleHome() {
        return gradleHome;
    }

    GradleBaseProjectInternal(String gradleVersion, File gradleHome) {
        this.gradleVersion = gradleVersion;
        this.gradleHome = gradleHome;
    }
    
    @ServiceProvider(service = ProjectInfoExtractor.class)
    public static class Extractor implements ProjectInfoExtractor {

        @Override
        public Result fallback(GradleFiles files) {
            return new DefaultResult(new GradleBaseProjectInternal(VERSION_UNKNOWN, null), Collections.emptySet());
        }

        @Override
        public Result extract(Map<String, Object> props, Map<Class, Object> otherInfo) {
            String ver = (String)props.getOrDefault("gradle_version", VERSION_UNKNOWN);
            File home = (File)props.get("gradle_home");
            return new DefaultResult(new GradleBaseProjectInternal(ver, home), Collections.emptySet());
        }
    }
}
