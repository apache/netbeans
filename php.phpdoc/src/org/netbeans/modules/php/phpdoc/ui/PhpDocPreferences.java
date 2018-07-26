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

package org.netbeans.modules.php.phpdoc.ui;

import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;

public final class PhpDocPreferences {

    private static final String PHPDOC_ENABLED = "enabled"; // NOI18N
    private static final String PHPDOC_TARGET = "target"; // NOI18N
    private static final String PHPDOC_TITLE = "title"; // NOI18N


    private PhpDocPreferences() {
    }

    public static boolean isEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(PHPDOC_ENABLED, false);
    }

    public static void setEnabled(PhpModule phpModule, boolean enabled) {
        getPreferences(phpModule).putBoolean(PHPDOC_ENABLED, enabled);
    }

    public static String getPhpDocTarget(PhpModule phpModule, boolean showPanel) {
        Preferences preferences = getPreferences(phpModule);
        String phpDocTarget = preferences.get(PHPDOC_TARGET, null);
        if ((phpDocTarget == null || phpDocTarget.isEmpty()) && showPanel) {
            phpDocTarget = BrowseFolderPanel.open(phpModule);
            if (phpDocTarget == null) {
                // cancelled
                return null;
            }
            setPhpDocTarget(phpModule, phpDocTarget);
        }
        return phpDocTarget;
    }

    public static void setPhpDocTarget(PhpModule phpModule, String phpDocTarget) {
        getPreferences(phpModule).put(PHPDOC_TARGET, phpDocTarget);
    }

    public static String getPhpDocTitle(PhpModule phpModule) {
        return getPreferences(phpModule).get(PHPDOC_TITLE, getDefaultPhpDocTitle(phpModule));
    }

    public static void setPhpDocTitle(PhpModule phpModule, String phpDocTitle) {
        if (phpDocTitle.equals(getDefaultPhpDocTitle(phpModule))) {
            return;
        }
        getPreferences(phpModule).put(PHPDOC_TITLE, phpDocTitle);
    }

    private static Preferences getPreferences(PhpModule phpModule) {
        return phpModule.getPreferences(PhpDocPreferences.class, false);
    }

    private static String getDefaultPhpDocTitle(PhpModule phpModule) {
        return phpModule.getDisplayName();
    }
}
