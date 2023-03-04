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
package org.netbeans.modules.php.codeception.preferences;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.codeception.CodeceptionTestingProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

public final class CodeceptionPreferences {

    private static final String CUSTOM_CODECEPT_ENABLED = "custom.codecept.enabled"; // NOI18N
    private static final String CUSTOM_CODECEPT_PATH = "custom.codecept.path"; // NOI18N
    public static final String CUSTOM_CODECEPTION_YML_ENABLED = "custom.codeception.yml.enabled"; // NOI18N
    public static final String CUSTOM_CODECEPTION_YML_PATH = "custom.codeception.yml.path"; // NOI18N
    private static final String ASK_FOR_ADDITIONAL_PARAMS = "additional.params.ask"; // NOI18N

    private static final ConcurrentMap<PhpModule, Preferences> CACHE = new ConcurrentHashMap<>();

    private CodeceptionPreferences() {
    }

    public static boolean isCustomCodeceptEnabled(PhpModule phpModule) {
        return getPreference(phpModule).getBoolean(CUSTOM_CODECEPT_ENABLED, false);
    }

    public static void setCustomCodeceptEnabled(PhpModule phpModule, boolean customCodeceptEnabled) {
        getPreference(phpModule).putBoolean(CUSTOM_CODECEPT_ENABLED, customCodeceptEnabled);
    }

    @CheckForNull
    public static String getCustomCodeceptPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreference(phpModule).get(CUSTOM_CODECEPT_PATH, null));
    }

    public static void setCustomCodeceptPath(PhpModule phpModule, String codeceptPath) {
        getPreference(phpModule).put(CUSTOM_CODECEPT_PATH, relativizePath(phpModule, codeceptPath));
    }

    public static boolean isCustomCodeceptionYmlEnabled(PhpModule phpModule) {
        return getPreference(phpModule).getBoolean(CUSTOM_CODECEPTION_YML_ENABLED, false);
    }

    public static void setCustomCodeceptionYmlEnabled(PhpModule phpModule, boolean customCodeceptionYmlEnabled) {
        getPreference(phpModule).putBoolean(CUSTOM_CODECEPTION_YML_ENABLED, customCodeceptionYmlEnabled);
    }

    @CheckForNull
    public static String getCustomCodeceptionYmlPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreference(phpModule).get(CUSTOM_CODECEPTION_YML_PATH, null));
    }

    public static void setCustomCodeceptionYmlPath(PhpModule phpModule, String codeceptPath) {
        getPreference(phpModule).put(CUSTOM_CODECEPTION_YML_PATH, relativizePath(phpModule, codeceptPath));
    }

    public static boolean askForAdditionalParameters(PhpModule phpModule) {
        return getPreference(phpModule).getBoolean(ASK_FOR_ADDITIONAL_PARAMS, false);
    }

    public static void setAskForAdditionalParameters(PhpModule phpModule, boolean ask) {
        getPreference(phpModule).putBoolean(ASK_FOR_ADDITIONAL_PARAMS, ask);
    }

    public static void addPreferenceChangeListener(PhpModule phpModule, PreferenceChangeListener listener) {
        getPreference(phpModule).addPreferenceChangeListener(listener);
    }

    public static void removePreferenceChangeListener(PhpModule phpModule, PreferenceChangeListener listener) {
        getPreference(phpModule).removePreferenceChangeListener(listener);
    }

    private static Preferences getPreference(PhpModule phpModule) {
        Preferences preferences = CACHE.get(phpModule);
        if (preferences == null) {
            preferences = phpModule.getPreferences(CodeceptionTestingProvider.class, true);
            Preferences currentPreferences = CACHE.putIfAbsent(phpModule, preferences);
            if (currentPreferences != null) {
                preferences = currentPreferences;
            }
        }
        assert preferences != null;
        return preferences;
    }

    private static String relativizePath(PhpModule phpModule, String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return ""; // NOI18N
        }
        File file = new File(filePath);
        String path = PropertyUtils.relativizeFile(FileUtil.toFile(phpModule.getProjectDirectory()), file);
        if (path == null) {
            // sorry, cannot be relativized
            path = file.getAbsolutePath();
        }
        return path;
    }

    private static String resolvePath(PhpModule phpModule, String filePath) {
        if (!StringUtils.hasText(filePath)) {
            return null;
        }
        return PropertyUtils.resolveFile(FileUtil.toFile(phpModule.getProjectDirectory()), filePath).getAbsolutePath();
    }

}
