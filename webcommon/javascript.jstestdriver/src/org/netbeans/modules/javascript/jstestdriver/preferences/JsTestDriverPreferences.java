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

package org.netbeans.modules.javascript.jstestdriver.preferences;

import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Project specific js-test-driver preferences.
 */
public final class JsTestDriverPreferences {

    public static final String CONFIG = "config"; // NOI18N

    private static final String ENABLED = "enabled"; // NOI18N

    // @GuardedBy("CACHE")
    private static final Map<Project, Preferences> CACHE = new WeakHashMap<>();


    private JsTestDriverPreferences() {
    }

    public static boolean isEnabled(Project project) {
        return getPreferences(project).getBoolean(ENABLED, false);
    }

    public static void setEnabled(Project project, boolean enabled) {
        getPreferences(project).putBoolean(ENABLED, enabled);
    }

    @CheckForNull
    public static String getConfig(Project project) {
        return resolvePath(project, getPreferences(project).get(CONFIG, null));
    }

    public static void setConfig(Project project, String config) {
        getPreferences(project).put(CONFIG, relativizePath(project, config));
    }

    public static void addPreferenceChangeListener(Project project, PreferenceChangeListener listener) {
        getPreferences(project).addPreferenceChangeListener(listener);
    }

    public static void removePreferenceChangeListener(Project project, PreferenceChangeListener listener) {
        getPreferences(project).removePreferenceChangeListener(listener);
    }

    private static String relativizePath(Project project, String filePath) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return ""; // NOI18N
        }
        File file = new File(filePath);
        String path = PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), file);
        if (path == null
                || path.startsWith("../")) { // NOI18N
            // cannot be relativized or outside project
            path = file.getAbsolutePath();
        }
        return path;
    }

    private static String resolvePath(Project project, String filePath) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return null;
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), filePath).getAbsolutePath();
    }

    private static Preferences getPreferences(Project project) {
        assert project != null;
        synchronized (CACHE) {
            Preferences preferences = CACHE.get(project);
            if (preferences == null) {
                preferences = ProjectUtils.getPreferences(project, JsTestDriverPreferences.class, false);
                CACHE.put(project, preferences);
            }
            assert preferences != null : project.getProjectDirectory();
            return preferences;
        }
    }

}
