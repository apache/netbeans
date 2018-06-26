/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
