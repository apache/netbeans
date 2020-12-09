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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.modules.gradle.cache.SubProjectDiskCache.SubProjectInfo;
import org.netbeans.modules.gradle.spi.GradleFiles;
import org.gradle.tooling.model.GradleProject;
import org.netbeans.modules.gradle.api.GradleBaseProject;

/**
 *
 * @author lkishalmi
 */
public class SubProjectDiskCache extends AbstractDiskCache<File, SubProjectInfo> {

    private static final String SUBPROJECT_CACHE_FILE_NAME = ".gradle/nb-cache/subprojects.ser"; //NOI18N
    private static final int COMPATIBLE_CACHE_VERSION = 1;

    private static Map<File, SubProjectDiskCache> diskCaches = new WeakHashMap<>();

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
        SubProjectDiskCache ret = diskCaches.get(key);
        if (ret == null) {
            ret = new SubProjectDiskCache(key);
            diskCaches.put(key, ret);
        }
        return ret;
    }

    public static final class SubProjectInfo implements Serializable {
        String rootProjectName;
        Map<String, String> path2Name;
        Map<File, String> file2Path;

        protected SubProjectInfo() {}

        public SubProjectInfo(GradleProject prj) {
            assert prj.getParent() == null : "This shall be called only on a root project!";
            rootProjectName = prj.getName();
            path2Name = new HashMap<>();
            file2Path = new HashMap<>();
            for (GradleProject child : prj.getChildren()) {
                path2Name.put(child.getPath(), child.getName());
                File dir = child.getProjectDirectory();
                if (!dir.isAbsolute()) {
                    dir = new File(prj.getProjectDirectory(), dir.toString());
                }
                file2Path.put(dir, child.getPath());
            }
        }

        public SubProjectInfo(GradleBaseProject gbp) {
            assert gbp.isRoot() : "This shall be called only on a root project!";
            rootProjectName = gbp.getName();
            path2Name = new HashMap<>();
            file2Path = new HashMap<>();
            for (Map.Entry<String, File> sprj : gbp.getSubProjects().entrySet()) {
                file2Path.put(sprj.getValue(), sprj.getKey());
                path2Name.put(sprj.getKey(), sprj.getKey());
            }
        }

        public String gerRootProjectName() {
            return rootProjectName;
        }

        public String getProjectName(String path) {
            return path2Name.get(path);
        }

        public String getProjectName(File dir) {
            String path = file2Path.get(dir);
            return path != null ? path2Name.get(path) : null;
        }

        public String getProjectPath(File dir) {
            return  file2Path.get(dir);
        }

        public boolean isSubproject(File dir) {
            return file2Path.containsKey(dir);
        }

        @Override
        public String toString() {
            return "SubProjects of [" + rootProjectName + "]: " + file2Path.keySet();
        }

    }
}
