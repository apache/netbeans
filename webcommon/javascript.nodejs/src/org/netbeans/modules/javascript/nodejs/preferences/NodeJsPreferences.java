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

package org.netbeans.modules.javascript.nodejs.preferences;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;

/**
 * Project specific Node.js preferences.
 */
public final class NodeJsPreferences {

    public static final String ENABLED = "enabled"; // NOI18N
    public static final String NODE_PATH = "node.path"; // NOI18N
    public static final String NODE_SOURCES_PATH = "node.sources.path"; // NOI18N
    public static final String NODE_DEFAULT = "node.default"; // NOI18N
    public static final String START_FILE = "start.file"; // NOI18N
    public static final String START_ARGS = "start.args"; // NOI18N
    public static final String RUN_ENABLED = "run.enabled"; // NOI18N
    public static final String RUN_RESTART = "run.restart"; // NOI18N
    public static final String DEBUG_PORT = "debug.port"; // NOI18N
    public static final String SYNC_ENABLED = "sync.enabled"; // NOI18N
    public static final String ASK_RUN_CONFIGURATION = "ask.run.enabled"; // NOI18N
    public static final String ASK_SYNC_ENABLED = "ask.sync.enabled"; // NOI18N
    public static final String DEBUG_PROTOCOL = "debug.protocol"; // NOI18N

    private final Project project;

    // @GuardedBy("this")
    private Preferences privatePreferences;
    // @GuardedBy("this")
    private Preferences sharedPreferences;


    public NodeJsPreferences(Project project) {
        assert project != null;
        this.project = project;
    }

    public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        getPrivatePreferences().addPreferenceChangeListener(listener);
        getSharedPreferences().addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        getPrivatePreferences().removePreferenceChangeListener(listener);
        getSharedPreferences().removePreferenceChangeListener(listener);
    }

    public boolean isEnabled() {
        return getSharedPreferences().getBoolean(ENABLED, false);
    }

    public void setEnabled(boolean enabled) {
        getSharedPreferences().putBoolean(ENABLED, enabled);
    }

    @CheckForNull
    public String getNode() {
        return FileUtils.resolvePath(project, getPrivatePreferences().get(NODE_PATH, null));
    }

    public void setNode(String node) {
        getPrivatePreferences().put(NODE_PATH, FileUtils.relativizePath(project, node));
    }

    @CheckForNull
    public String getNodeSources() {
        return FileUtils.resolvePath(project, getPrivatePreferences().get(NODE_SOURCES_PATH, null));
    }

    public void setNodeSources(@NullAllowed String nodeSources) {
        if (nodeSources == null) {
            getPrivatePreferences().remove(NODE_SOURCES_PATH);
        } else {
            getPrivatePreferences().put(NODE_SOURCES_PATH, FileUtils.relativizePath(project, nodeSources));
        }
    }

    public boolean isDefaultNode() {
        return getSharedPreferences().getBoolean(NODE_DEFAULT, true);
    }

    public void setDefaultNode(boolean defaultNode) {
        getSharedPreferences().putBoolean(NODE_DEFAULT, defaultNode);
    }

    @CheckForNull
    public String getStartFile() {
        return FileUtils.resolvePath(project, getSharedPreferences().get(START_FILE, null));
    }

    public void setStartFile(@NullAllowed String startFile) {
        if (startFile == null) {
            getSharedPreferences().remove(START_FILE);
        } else {
            getSharedPreferences().put(START_FILE, FileUtils.relativizePath(project, startFile));
        }
    }

    @CheckForNull
    public String getStartArgs() {
        return getSharedPreferences().get(START_ARGS, null);
    }

    public void setStartArgs(@NullAllowed String startArgs) {
        if (startArgs == null) {
            getSharedPreferences().remove(START_ARGS);
        } else {
            getSharedPreferences().put(START_ARGS, startArgs);
        }
    }

    public boolean isRunEnabled() {
        return getSharedPreferences().getBoolean(RUN_ENABLED, false);
    }

    public void setRunEnabled(boolean enabled) {
        getSharedPreferences().putBoolean(RUN_ENABLED, enabled);
    }

    public boolean isRunRestart() {
        return getSharedPreferences().getBoolean(RUN_RESTART, false);
    }

    public void setRunRestart(boolean restart) {
        getSharedPreferences().putBoolean(RUN_RESTART, restart);
    }

    public int getDebugPort() {
        return getPrivatePreferences().getInt(DEBUG_PORT, NodeExecutable.DEFAULT_DEBUG_PORT);
    }

    public void setDebugPort(int debugPort) {
        getPrivatePreferences().putInt(DEBUG_PORT, debugPort);
    }

    public boolean isSyncEnabled() {
        return getSharedPreferences().getBoolean(SYNC_ENABLED, true);
    }

    public void setSyncEnabled(boolean enabled) {
        getSharedPreferences().putBoolean(SYNC_ENABLED, enabled);
    }

    public boolean isAskRunEnabled() {
        boolean ask = getPrivatePreferences().getBoolean(ASK_RUN_CONFIGURATION, true);
        if (ask) {
            getPrivatePreferences().putBoolean(ASK_RUN_CONFIGURATION, false);
        }
        return ask;
    }

    public boolean isAskSyncEnabled() {
        boolean ask = getPrivatePreferences().getBoolean(ASK_SYNC_ENABLED, true);
        if (ask) {
            getPrivatePreferences().putBoolean(ASK_SYNC_ENABLED, false);
        }
        return ask;
    }

    @CheckForNull
    public String getDebugProtocol() {
        return getPrivatePreferences().get(DEBUG_PROTOCOL, null);
    }

    public void setDebugProtocol(@NullAllowed String debugProtocol) {
        if (debugProtocol == null) {
            getPrivatePreferences().remove(DEBUG_PROTOCOL);
        } else {
            getPrivatePreferences().put(DEBUG_PROTOCOL, debugProtocol);
        }
    }

    private synchronized Preferences getPrivatePreferences() {
        if (privatePreferences == null) {
            privatePreferences = ProjectUtils.getPreferences(project, NodeJsPreferences.class, false);
        }
        return privatePreferences;
    }

    private synchronized Preferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = ProjectUtils.getPreferences(project, NodeJsPreferences.class, true);
        }
        return sharedPreferences;
    }

}
