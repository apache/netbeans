/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
