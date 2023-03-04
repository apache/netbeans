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
package org.netbeans.modules.php.atoum.preferences;

import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.atoum.AtoumTestingProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Atoum preferences specific for each PHP module.
 */
public final class AtoumPreferences {

    private static final String BOOTSTRAP_ENABLED = "bootstrap.enabled"; // NOI18N
    private static final String BOOTSTRAP_PATH = "bootstrap.path"; // NOI18N
    private static final String CONFIGURATION_ENABLED = "configuration.enabled"; // NOI18N
    private static final String CONFIGURATION_PATH = "configuration.path"; // NOI18N
    private static final String ATOUM_ENABLED = "atoum.enabled"; // NOI18N
    private static final String ATOUM_PATH = "atoum.path"; // NOI18N


    private AtoumPreferences() {
    }

    public static boolean isBootstrapEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(BOOTSTRAP_ENABLED, false);
    }

    public static void setBootstrapEnabled(PhpModule phpModule, boolean bootstrapEnabled) {
        getPreferences(phpModule).putBoolean(BOOTSTRAP_ENABLED, bootstrapEnabled);
    }

    public static String getBootstrapPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(BOOTSTRAP_PATH, null));
    }

    public static void setBootstrapPath(PhpModule phpModule, String bootstrapPath) {
        getPreferences(phpModule).put(BOOTSTRAP_PATH, relativizePath(phpModule, bootstrapPath));
    }

    public static boolean isConfigurationEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(CONFIGURATION_ENABLED, false);
    }

    public static void setConfigurationEnabled(PhpModule phpModule, boolean configurationEnabled) {
        getPreferences(phpModule).putBoolean(CONFIGURATION_ENABLED, configurationEnabled);
    }

    public static String getConfigurationPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(CONFIGURATION_PATH, null));
    }

    public static void setConfigurationPath(PhpModule phpModule, String configurationPath) {
        getPreferences(phpModule).put(CONFIGURATION_PATH, relativizePath(phpModule, configurationPath));
    }

    public static boolean isAtoumEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(ATOUM_ENABLED, false);
    }

    public static void setAtoumEnabled(PhpModule phpModule, boolean atoumEnabled) {
        getPreferences(phpModule).putBoolean(ATOUM_ENABLED, atoumEnabled);
    }

    public static String getAtoumPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(ATOUM_PATH, null));
    }

    public static void setAtoumPath(PhpModule phpModule, String atoumPath) {
        getPreferences(phpModule).put(ATOUM_PATH, relativizePath(phpModule, atoumPath));
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(AtoumTestingProvider.class, true);
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
