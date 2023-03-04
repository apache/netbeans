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
package org.netbeans.modules.selenium2.webclient.mocha.preferences;

import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Theofanis Oikonomou
 */
public final class MochaJSPreferences {

    private static final String ENABLED = "js.enabled"; // NOI18N
    private static final String MOCHA_DIR = "js.mocha.dir"; // NOI18N
    private static final String TIMEOUT = "js.timeout"; // NOI18N
    private static final int TIMEOUT_DEFAULT = 10000; // 10 seconds
    public static final String AUTO_WATCH = "js.auto.watch"; // NOI18N 

    private MochaJSPreferences() {
    }

    public static boolean isEnabled(Project project) {
        return getPreferences(project).getBoolean(ENABLED, false);
    }

    public static void setEnabled(Project project, boolean enabled) {
        getPreferences(project).putBoolean(ENABLED, enabled);
    }

    @CheckForNull
    public static String getMochaDir(Project project) {
        return resolvePath(project, getPreferences(project).get(MOCHA_DIR, null));
    }

    public static void setMochaDir(Project project, String installDir) {
        getPreferences(project).put(MOCHA_DIR, installDir);
    }
    
    public static int getTimeout(Project project) {
        return getPreferences(project).getInt(TIMEOUT, TIMEOUT_DEFAULT);
    }

    public static void setTimeout(Project project, int timeout) {
        getPreferences(project).putInt(TIMEOUT, timeout);
    }

    public static boolean isAutoWatch(Project project) {
        return getPreferences(project).getBoolean(AUTO_WATCH, false);
    }

    public static void setAutoWatch(Project project, boolean autoWatch) {
        getPreferences(project).putBoolean(AUTO_WATCH, autoWatch);
    }

    private static String resolvePath(Project project, String filePath) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return null;
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), filePath).getAbsolutePath();
    }

    private static Preferences getPreferences(Project project) {
        return ProjectUtils.getPreferences(project, MochaJSPreferences.class, false);
    }
    
}
