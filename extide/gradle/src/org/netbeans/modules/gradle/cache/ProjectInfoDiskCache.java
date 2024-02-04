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
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.gradle.NbGradleProjectImpl;
import org.netbeans.modules.gradle.cache.ProjectInfoDiskCache.QualifiedProjectInfo;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo;
import org.netbeans.modules.gradle.tooling.internal.NbProjectInfo.Report;
import org.netbeans.modules.gradle.spi.GradleFiles;

/**
 *
 * @author lkishalmi
 */
public final class ProjectInfoDiskCache extends AbstractDiskCache<GradleFiles, QualifiedProjectInfo> {

    // Increase this number if new info is gathered from the projects.
    private static final int COMPATIBLE_CACHE_VERSION = 25;
    private static final String INFO_CACHE_FILE_NAME = "project-info.ser"; //NOI18N
    private static final Map<GradleFiles, ProjectInfoDiskCache> DISK_CACHES = Collections.synchronizedMap(new WeakHashMap<>());

    public static ProjectInfoDiskCache get(GradleFiles gf) {
        // get & put synchronized
        ProjectInfoDiskCache ret = DISK_CACHES.computeIfAbsent(gf, (k) -> new ProjectInfoDiskCache(k));
        return ret;
    }
    
    private ProjectInfoDiskCache(GradleFiles gf) {
        super(gf);
    }
    
    /**
     * For testing only. Test that wants to ensure the data is loaded from the
     * disk anew instead of 
     */
    public static void testFlushCaches() {
        DISK_CACHES.clear();
    }
    
    /**
     * For testing only. Destroys disk cache for the given project.
     */
    public static boolean testDestroyCache(GradleFiles gf) {
        ProjectInfoDiskCache cache = ProjectInfoDiskCache.get(gf);
        if (cache == null) {
            return false;
        }
        File f = cache.cacheFile();
        return f.exists() && f.delete();
    }
    
    @Override
    protected int cacheVersion() {
        return COMPATIBLE_CACHE_VERSION;
    }

    @Override
    protected File cacheFile() {
        return new File(NbGradleProjectImpl.getCacheDir(key), INFO_CACHE_FILE_NAME);
    }

    @Override
    protected Set<File> cacheInvalidators() {
        Set<File> ret = new HashSet<>(key.getProjectFiles());
        if (key.hasWrapper()) ret.add(key.getWrapperProperties());
        return ret;
    }
    
    private static CacheReport makeReport(Report r) {
        CacheReport nested;
        
        if (r.getCause() != null) {
            nested = makeReport(r.getCause());
        } else {
            nested = null;
        }
        return new CacheReport(r.getSeverity(), r.getErrorClass(), r.getScriptLocation(), r.getLineNumber(), r.getMessage(), r.getDetail(), nested);
    }
    
    private static Set<Report> makeReports(Collection<Report> reps) {
        Set<Report> res = new LinkedHashSet<>();
        for (Report r : reps) {
            res.add(makeReport(r));
        }
        return res;
    }

    final static class CacheReport implements Report, Serializable {
        private final String errorClass;
        private final String location;
        private final int line;
        private final String message;
        private final String detail;
        private final Report causedBy;
        private final Severity severity;

        public CacheReport(Severity severity, String errorClass, String location, int line, String message, String detail, Report causedBy) {
            this.severity = severity;
            this.errorClass = errorClass;
            this.location = location;
            this.line = line;
            this.message = message;
            this.detail = detail;
            this.causedBy = causedBy;
        }

        public CacheReport(Severity severity, String message, String detail) {
            this.severity = severity;
            this.errorClass = null;
            this.location = null;
            this.line = -1;
            this.message = message;
            this.detail = detail;
            this.causedBy = null;
        }
        public String getErrorClass() {
            return errorClass;
        }

        @Override
        public String getScriptLocation() {
            return location;
        }

        public int getLineNumber() {
            return line;
        }
        
        public @NonNull String getMessage() {
            return message;
        }

        public @CheckForNull Report getCause() {
            return causedBy;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getDetail() {
            return detail;
        }

        @Override
        public int hashCode() {
            int hash = 3;
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
            final CacheReport other = (CacheReport) obj;
            if (this.line != other.line) {
                return false;
            }
            if (!Objects.equals(this.errorClass, other.errorClass)) {
                return false;
            }
            if (!Objects.equals(this.location, other.location)) {
                return false;
            }
            if (!Objects.equals(this.severity, other.severity)) {
                return false;
            }
            return Objects.equals(this.message, other.message);
        }

    }

    public static final class QualifiedProjectInfo implements NbProjectInfo {

        private final Quality quality;
        private final Map<String, Object> info;
        private final transient Map<String, Object> ext;
        private final Set<String> problems;
        private final Set<Report> reports;
        private final String gradleException;

        public QualifiedProjectInfo(Quality quality, NbProjectInfo pinfo) {
            this.quality = quality;
            info = new TreeMap<>(pinfo.getInfo());
            ext = new TreeMap<>(pinfo.getExt());
            problems = new LinkedHashSet<>(pinfo.getProblems());
            gradleException = pinfo.getGradleException();
            reports = makeReports(pinfo.getReports());
        }

        @Override
        public Map<String, Object> getInfo() {
            return info;
        }

        @Override
        public Map<String, Object> getExt() {
            return ext != null ? ext : Collections.emptyMap();
        }

        @Override
        public Set<String> getProblems() {
            return problems;
        }

        @Override
        public Set<Report> getReports() {
            return reports;
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
