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
package org.netbeans.modules.gradle.cache;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.gradle.cache.SubProjectDiskCache.SubProjectInfo;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.gradle.tooling.model.GradleProject;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.GradleProjectStructure;
import org.netbeans.modules.gradle.api.GradleBaseProject;

/**
 *
 * @author lkishalmi
 */
public final class SubProjectDiskCache extends AbstractDiskCache<File, SubProjectInfo> {

    private static final String SUBPROJECT_CACHE_FILE_NAME = ".gradle/nb-cache/subprojects.ser"; //NOI18N
    private static final int COMPATIBLE_CACHE_VERSION = 2;

    private static final Map<File, SubProjectDiskCache> DISK_CACHES = new WeakHashMap<>();

    protected SubProjectDiskCache() {}

    private SubProjectDiskCache(File key) {
        super(key);
    }

    @Override
    protected int cacheVersion() {
        return COMPATIBLE_CACHE_VERSION;
    }

    @Override
    protected File cacheFile() {
        return new File(key, SUBPROJECT_CACHE_FILE_NAME);
    }

    @Override
    protected Set<File> cacheInvalidators() {
        return Collections.singleton(new GradleFiles(key).getSettingsScript());
    }

    public static SubProjectDiskCache get(File key) {
        SubProjectDiskCache ret = DISK_CACHES.get(key);
        if (ret == null) {
            ret = new SubProjectDiskCache(key);
            DISK_CACHES.put(key, ret);
        }
        return ret;
    }

    @Override
    protected boolean doStoreEntry(CacheEntry<SubProjectInfo> entry) {
        boolean ret = super.doStoreEntry(entry);
        ProjectManager.getDefault().clearNonProjectCache();
        return ret;
    }
    

    public static final class SubProjectInfo implements GradleProjectStructure, Serializable {
        public static final String ROOT_PATH = ":"; //NOI18N

        private Map<String, String> path2Name;
        private Map<File, String> file2Path;
        private Map<String, File> path2File;
        private Map<String, String> path2Description;

        protected SubProjectInfo() {}

        public SubProjectInfo(GradleProject prj) {
            assert prj.getParent() == null : "This shall be called only on a root project!";
            if (prj.getChildren().isEmpty()) {
                path2Name = Collections.singletonMap(ROOT_PATH, prj.getName());
                file2Path = Collections.singletonMap(prj.getProjectDirectory(), ROOT_PATH);
                path2File = Collections.singletonMap(ROOT_PATH, prj.getProjectDirectory());
                path2Description = Collections.singletonMap(ROOT_PATH, prj.getDescription());
            } else {
                path2Name = new HashMap<>();
                file2Path = new HashMap<>();
                path2File = new HashMap<>();
                path2Description = new HashMap<>();

                path2Name.put(ROOT_PATH, prj.getName());
                file2Path.put(prj.getProjectDirectory(), ROOT_PATH);
                path2File.put(ROOT_PATH, prj.getProjectDirectory());
                if (prj.getDescription() != null) {
                    path2Description.put(ROOT_PATH, prj.getDescription());
                }

                for (GradleProject child : prj.getChildren()) {
                    processGradleProject(child);
                }
            }
        }

        private void processGradleProject(GradleProject prj) {
            String path = prj.getPath();
            path2Name.put(path, prj.getName());
            File dir = prj.getProjectDirectory();
            if (!dir.isAbsolute()) {
                dir = new File(prj.getProjectDirectory(), dir.toString());
            }
            file2Path.put(dir, path);
            path2File.put(path, dir);
            if (prj.getDescription() != null) {
                path2Description.put(path, prj.getDescription());
            }
            for (GradleProject child : prj.getChildren()) {
                processGradleProject(child);
            }
        }
        
        public SubProjectInfo(GradleBaseProject gbp) {
            assert gbp.isRoot() : "This shall be called only on a root project!";
            if (gbp.getSubProjects().isEmpty()) {
                path2Name = Collections.singletonMap(ROOT_PATH, gbp.getName());
                file2Path = Collections.singletonMap(gbp.getProjectDir(), ROOT_PATH);
                path2File = Collections.singletonMap(ROOT_PATH, gbp.getProjectDir());
                path2Description = Collections.singletonMap(ROOT_PATH, gbp.getDescription());
            } else {
                path2Name = new HashMap<>();
                file2Path = new HashMap<>();
                path2File = new HashMap<>();
                path2Description = Collections.emptyMap();

                path2Name.put(ROOT_PATH, gbp.getName());
                file2Path.put(gbp.getProjectDir(), ROOT_PATH);
                path2File.put(ROOT_PATH, gbp.getProjectDir());
                for (Map.Entry<String, File> sprj : gbp.getSubProjects().entrySet()) {
                    file2Path.put(sprj.getValue(), sprj.getKey());
                    path2File.put(sprj.getKey(), sprj.getValue());
                    path2Name.put(sprj.getKey(), sprj.getKey());
                }
            }
        }

        public String getProjectName(String path) {
            return path2Name.get(path);
        }

        public String getProjectName(File dir) {
            String path = file2Path.get(dir);
            return path != null ? path2Name.get(path) : null;
        }

        public String getProjectDescription(String path) {
            return path2Description.get(path);
        }

        public String getProjectDescription(File dir) {
            String path = file2Path.get(dir);
            return path != null ? path2Description.get(path) : null;
        }

        public String getProjectPath(File dir) {
            return  file2Path.get(dir);
        }

        public boolean isSubproject(File dir) {
            return file2Path.containsKey(dir);
        }

        @Override
        public Set<String> getProjectPaths() {
            return path2Name.keySet();
        }

        @Override
        public File getProjectDir(String path) {
            return path2File.get(path);
        }

        @Override
        public String toString() {
            return "SubProjects of [" + path2Name.get(ROOT_PATH) + "]: " + file2Path.keySet();
        }


    }
}
