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

package org.netbeans.modules.php.nette.tester.preferences;

import java.io.File;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.nette.tester.TesterTestingProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * Nette Tester preferences specific for each PHP module.
 */
public final class TesterPreferences {

    private static final String PHP_INI_ENABLED = "php.ini.enabled"; // NOI18N
    private static final String PHP_INI_PATH = "php.ini.path"; // NOI18N
    private static final String TESTER_ENABLED = "tester.enabled"; // NOI18N
    private static final String TESTER_PATH = "tester.path"; // NOI18N
    private static final String BINARY_ENABLED = "binary.enabled"; // NOI18N
    private static final String BINARY_EXECUTABLE = "binary.executable"; // NOI18N
    private static final String COVERAGE_SOURCE_PATH_ENABLED = "coverage.source.path.enabled"; // NOI18N
    private static final String COVERAGE_SOURCE_PATH = "coverage.source.path"; // NOI18N


    private TesterPreferences() {
    }

    public static boolean isPhpIniEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(PHP_INI_ENABLED, false);
    }

    public static void setPhpIniEnabled(PhpModule phpModule, boolean phpIniEnabled) {
        getPreferences(phpModule).putBoolean(PHP_INI_ENABLED, phpIniEnabled);
    }

    @CheckForNull
    public static String getPhpIniPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(PHP_INI_PATH, null));
    }

    public static void setPhpIniPath(PhpModule phpModule, String phpIniPath) {
        getPreferences(phpModule).put(PHP_INI_PATH, relativizePath(phpModule, phpIniPath));
    }

    public static boolean isTesterEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(TESTER_ENABLED, false);
    }

    public static void setTesterEnabled(PhpModule phpModule, boolean testerEnabled) {
        getPreferences(phpModule).putBoolean(TESTER_ENABLED, testerEnabled);
    }

    @CheckForNull
    public static String getTesterPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(TESTER_PATH, null));
    }

    public static void setTesterPath(PhpModule phpModule, String testerPath) {
        getPreferences(phpModule).put(TESTER_PATH, relativizePath(phpModule, testerPath));
    }

    public static boolean isBinaryEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(BINARY_ENABLED, false);
    }

    public static void setBinaryEnabled(PhpModule phpModule, boolean binaryEnabled) {
        getPreferences(phpModule).putBoolean(BINARY_ENABLED, binaryEnabled);
    }

    @CheckForNull
    public static String getBinaryExecutable(PhpModule phpModule) {
        return getPreferences(phpModule).get(BINARY_EXECUTABLE, null);
    }

    public static void setBinaryExecutable(PhpModule phpModule, @NullAllowed String binaryExecutable) {
        if (binaryExecutable == null) {
            getPreferences(phpModule).remove(BINARY_EXECUTABLE);
        } else {
            getPreferences(phpModule).put(BINARY_EXECUTABLE, binaryExecutable);
        }
    }

    public static boolean isCoverageSourcePathEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(COVERAGE_SOURCE_PATH_ENABLED, false);
    }

    public static void setCoverageSourcePathEnabled(PhpModule phpModule, boolean pathEnabled) {
        getPreferences(phpModule).putBoolean(COVERAGE_SOURCE_PATH_ENABLED, pathEnabled);
    }

    @CheckForNull
    public static String getCoverageSourcePath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(COVERAGE_SOURCE_PATH, null));
    }

    public static void setCoverageSourcePath(PhpModule phpModule, String sourcePath) {
        getPreferences(phpModule).put(COVERAGE_SOURCE_PATH, relativizePath(phpModule, sourcePath));
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(TesterTestingProvider.class, true);
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
