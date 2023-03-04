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
package org.netbeans.modules.javascript.nodejs.options;

import java.util.List;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.javascript.nodejs.exec.ExpressExecutable;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.openide.util.NbPreferences;

public final class NodeJsOptions {

    public static final String NODE_PATH = "node.path"; // NOI18N
    public static final String NODE_SOURCES_PATH = "node.sources.path"; // NOI18N
    public static final String NPM_PATH = "npm.path"; // NOI18N
    public static final String NPM_IGNORE_NODE_MODULES = "npm.ignore.node_modules"; // NOI18N
    public static final String EXPRESS_PATH = "express.path"; // NOI18N

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "nodejs"; // NOI18N

    private static final NodeJsOptions INSTANCE = new NodeJsOptions();

    private final Preferences preferences;

    private volatile boolean nodeSearched = false;
    private volatile boolean npmSearched = false;
    private volatile boolean expressSearched = false;


    private NodeJsOptions() {
        preferences = NbPreferences.forModule(NodeJsOptions.class).node(PREFERENCES_PATH);
    }

    public static NodeJsOptions getInstance() {
        return INSTANCE;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        preferences.removePreferenceChangeListener(listener);
    }

    @CheckForNull
    public String getNode() {
        String path = preferences.get(NODE_PATH, null);
        if (path == null
                && !nodeSearched) {
            nodeSearched = true;
            path = NodeJsUtils.getNode();
            if (path != null) {
                setNode(path);
            }
        }
        return path;
    }

    public void setNode(String node) {
        preferences.put(NODE_PATH, node);
    }

    @CheckForNull
    public String getNodeSources() {
        return preferences.get(NODE_SOURCES_PATH, null);
    }

    public void setNodeSources(@NullAllowed String nodeSources) {
        if (nodeSources == null) {
            preferences.remove(NODE_SOURCES_PATH);
        } else {
            preferences.put(NODE_SOURCES_PATH, nodeSources);
        }
    }

    @CheckForNull
    public String getNpm() {
        String path = preferences.get(NPM_PATH, null);
        if (path == null
                && !npmSearched) {
            npmSearched = true;
            path = NodeJsUtils.getNpm();
            if (path != null) {
                setNpm(path);
            }
        }
        return path;
    }

    public void setNpm(String npm) {
        preferences.put(NPM_PATH, npm);
    }

    public boolean isNpmIgnoreNodeModules() {
        return preferences.getBoolean(NPM_IGNORE_NODE_MODULES, true);
    }

    public void setNpmIgnoreNodeModules(boolean npmIgnoreNodeModules) {
        preferences.putBoolean(NPM_IGNORE_NODE_MODULES, npmIgnoreNodeModules);
    }

    @CheckForNull
    public String getExpress() {
        String path = preferences.get(EXPRESS_PATH, null);
        if (path == null
                && !expressSearched) {
            expressSearched = true;
            List<String> files = FileUtils.findFileOnUsersPath(ExpressExecutable.EXPRESS_NAME);
            if (!files.isEmpty()) {
                path = files.get(0);
                setExpress(path);
            }
        }
        return path;
    }

    public void setExpress(String express) {
        preferences.put(EXPRESS_PATH, express);
    }

}
