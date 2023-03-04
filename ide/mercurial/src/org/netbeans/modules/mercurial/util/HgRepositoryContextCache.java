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
package org.netbeans.modules.mercurial.util;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.config.HgConfigFiles;

/**
 * A class to encapsulate a Repository and allow us to cache some values
 *
 * @author John Rice
 */
public class HgRepositoryContextCache {
    private Map<File, Map<String, String>> rootToDefaultPaths;

    private static HgRepositoryContextCache instance;

    private HgRepositoryContextCache() {
    }

    public static HgRepositoryContextCache getInstance() {
        if(instance == null) {
            instance = new HgRepositoryContextCache();
        }
        return instance;
    }

    public synchronized void reset() {
        getRootToDefaultPaths().clear();
    }

    public synchronized String getPullDefault(File file) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
        if(repoRoot == null) return null;
        Map<String, String> paths = getPaths(repoRoot);
        return paths.get(HgConfigFiles.HG_DEFAULT_PULL_VALUE);
    }

    public synchronized String getPushDefault(File file) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(file);
        if(repoRoot == null) return null;
        Map<String, String> paths = getPaths(repoRoot);
        return paths.get(HgConfigFiles.HG_DEFAULT_PUSH);
    }

    public String getPathValue (File root, String path) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(root);
        if(repoRoot == null) return null;
        Map<String, String> paths = getPaths(repoRoot);
        return paths.get(path);
    }

    public Map<String, String> getPathValues (File root) {
        File repoRoot = Mercurial.getInstance().getRepositoryRoot(root);
        if (repoRoot == null) return Collections.<String, String>emptyMap();
        Map<String, String> paths = getPaths(repoRoot);
        return new HashMap<String, String>(paths);
    }

    private Map<String, String> getPaths(File repoRoot) {
        Map<File, Map<String, String>> map = getRootToDefaultPaths();
        Map<String, String> paths = map.get(repoRoot);
        if (paths == null) {
            HgConfigFiles config = new HgConfigFiles(repoRoot);
            String pull = config.getDefaultPull(true);
            String push = config.getDefaultPush(true);
            paths = new HashMap<String, String>();
            paths.put(HgConfigFiles.HG_DEFAULT_PULL_VALUE, pull);
            paths.put(HgConfigFiles.HG_DEFAULT_PUSH, push);
            for (Map.Entry<Object, Object> e : config.getProperties(HgConfigFiles.HG_PATHS_SECTION).entrySet()) {
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                if (key != null && value != null && !key.isEmpty() && !value.isEmpty()) {
                    paths.put(key.trim(), value.trim());
                }
            }
            map.put(repoRoot, paths);
        }
        return paths;
    }

    private Map<File, Map<String, String>> getRootToDefaultPaths() {
        if(rootToDefaultPaths == null) {
            rootToDefaultPaths = new HashMap<File, Map<String, String>>();
        }
        return rootToDefaultPaths;
    }
}

