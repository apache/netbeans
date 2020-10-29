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
package org.netbeans.modules.gradle.cache;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gradle.GradleProjectCache;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache.QualifiedProjectInfo;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.netbeans.modules.gradle.api.NbProjectInfo;
import org.netbeans.modules.gradle.spi.GradleFiles;

/**
 *
 * @author lkishalmi
 */
public final class ProjectInfoDiskCache extends AbstractDiskCache<GradleFiles, QualifiedProjectInfo> {

    // Increase this number if new info is gathered from the projects.
    private static final int COMPATIBLE_CACHE_VERSION = 16;
    private static final String INFO_CACHE_FILE_NAME = "project-info.ser"; //NOI18N

    public ProjectInfoDiskCache(GradleFiles gf) {
        super(gf);
    }
    
    @Override
    protected int cacheVersion() {
        return COMPATIBLE_CACHE_VERSION;
    }

    @Override
    protected File cacheFile() {
        return new File(GradleProjectCache.getCacheDir(key), INFO_CACHE_FILE_NAME);
    }

    @Override
    protected Set<File> cacheInvalidators() {
        Set<File> ret = new HashSet<>(key.getProjectFiles());
        if (key.hasWrapper()) ret.add(key.getWrapperProperties());
        return ret;
    }

    public static final class QualifiedProjectInfo implements NbProjectInfo {

        private final Quality quality;
        private final Map<String, Object> info;
        private final Set<String> problems;
        private final String gradleException;

        public QualifiedProjectInfo(Quality quality, NbProjectInfo pinfo) {
            this.quality = quality;
            info = new LinkedHashMap<>(pinfo.getInfo());
            problems = new LinkedHashSet<>(pinfo.getProblems());
            gradleException = pinfo.getGradleException();
        }

        @Override
        public Map<String, Object> getInfo() {
            return info;
        }

        @Override
        public Map<String, Object> getExt() {
            return Collections.emptyMap();
        }

        @Override
        public Set<String> getProblems() {
            return problems;
        }

        @Override
        public String getGradleException() {
            return gradleException;
        }

        @Override
        public boolean hasException() {
            return gradleException != null;
        }

        @Override
        public boolean getMiscOnly() {
            return false;
        }

        public Quality getQuality() {
            return quality;
        }

        @Override
        public String toString() {
            return "QualifiedProjectInfo{" + "quality=" + quality + '}';
        }

    }
}
