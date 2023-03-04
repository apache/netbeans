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
package org.netbeans.modules.php.symfony2.preferences;

import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.symfony2.SymfonyPhpFrameworkProvider;
import org.netbeans.modules.php.symfony2.options.SymfonyOptions;

/**
 * Symfony 2/3 preferences for PHP module.
 */
public final class SymfonyPreferences {

    private static final String ENABLED = "enabled"; // NOI18N
    private static final String APP_DIR = "appDir-path"; // NOI18N
    private static final String IGNORE_CACHE_DIRECTORY = "cacheDir-ignored"; // NOI18N

    private static final String DEFAULT_APP_DIR = "app"; // NOI18N


    private SymfonyPreferences() {
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
    @CheckForNull
    public static Boolean isEnabled(PhpModule module) {
        String enabled = getPreferences(module).get(ENABLED, null);
        if (enabled == null) {
            return null;
        }
        return Boolean.valueOf(enabled);
    }

    public static void setEnabled(PhpModule module, boolean enabled) {
        getPreferences(module).putBoolean(ENABLED, enabled);
    }

    public static String getAppDir(PhpModule module) {
        return getPreferences(module).get(APP_DIR, DEFAULT_APP_DIR);
    }

    public static void setAppDir(PhpModule module, String appDir) {
        if (appDir.equals(DEFAULT_APP_DIR)) {
            getPreferences(module).remove(APP_DIR);
        } else {
            getPreferences(module).put(APP_DIR, appDir);
        }
    }

    public static boolean isCacheDirIgnored(PhpModule phpModule) {
        String ignored = getPreferences(phpModule).get(IGNORE_CACHE_DIRECTORY, null);
        if (ignored == null) {
            return SymfonyOptions.getInstance().getIgnoreCache();
        }
        return Boolean.valueOf(ignored);
    }

    public static void setCacheDirIgnored(PhpModule phpModule, boolean ignored) {
        getPreferences(phpModule).putBoolean(IGNORE_CACHE_DIRECTORY, ignored);
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(SymfonyPhpFrameworkProvider.class, true);
    }

}
