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

package org.netbeans.modules.php.project;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.util.StringUtils;

/**
 * Helper class to get miscellaneous properties related to single PHP project
 * (like timestamp when a project has been uploaded last time etc.).
 * @author Tomas Mysik
 */
public final class ProjectSettings {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PROJECT_PREFERENCES_PATH = "projects"; // NOI18N

    private static final String LAST_UPLOAD = "lastUpload"; // NOI18N
    private static final String LAST_DONWLOAD = "lastDownload"; // NOI18N
    private static final String DEBUG_URLS = "debugUrls"; // NOI18N
    private static final String DEBUG_URLS_DELIMITER = "??NB??"; // NOI18N
    private static final int DEBUG_URLS_LIMIT = 10;
    // remote synchronization
    private static final String SYNC_TIMESTAMP = "sync.%s"; // NOI18N


    private ProjectSettings() {
    }

    private static Preferences getPreferences(Project project) {
        return PhpPreferences.getPreferences(false).node(PROJECT_PREFERENCES_PATH).
                node(ProjectUtils.getInformation(project).getName());
    }

    /**
     * @return timestamp <b>in seconds</b> of the last upload of a project or <code>-1</code> if not found.
     */
    public static long getLastUpload(Project project) {
        return getPreferences(project).getLong(LAST_UPLOAD, -1);
    }

    public static void setLastUpload(Project project, long timestamp) {
        getPreferences(project).putLong(LAST_UPLOAD, timestamp);
    }

    public static void resetLastUpload(Project project) {
        setLastUpload(project, -1);
    }

    /**
     * @return timestamp <b>in seconds</b> of the last download of a project or <code>-1</code> if not found.
     */
    public static long getLastDownload(Project project) {
        return getPreferences(project).getLong(LAST_DONWLOAD, -1);
    }

    public static void setLastDownload(Project project, long timestamp) {
        getPreferences(project).putLong(LAST_DONWLOAD, timestamp);
    }

    public static void resetLastDownload(Project project) {
        setLastDownload(project, -1);
    }

    public static List<String> getDebugUrls(Project project) {
        return StringUtils.explode(getPreferences(project).get(DEBUG_URLS, null), DEBUG_URLS_DELIMITER);
    }

    public static void setDebugUrls(Project project, List<String> debugUrls) {
        if (debugUrls.size() > DEBUG_URLS_LIMIT) {
            debugUrls = debugUrls.subList(0, DEBUG_URLS_LIMIT);
        }
        getPreferences(project).put(DEBUG_URLS, StringUtils.implode(debugUrls, DEBUG_URLS_DELIMITER));
    }

    public static long getSyncTimestamp(Project project, String key) {
        return getPreferences(project).getLong(String.format(SYNC_TIMESTAMP, key), -1);
    }

    public static void setSyncTimestamp(Project project, String key, long value) {
        getPreferences(project).putLong(String.format(SYNC_TIMESTAMP, key), value);
    }

}
