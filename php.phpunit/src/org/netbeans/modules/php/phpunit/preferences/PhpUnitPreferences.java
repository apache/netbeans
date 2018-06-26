/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phpunit.preferences;

import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.phpunit.PhpUnitTestingProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

/**
 * PhpUnit preferences specific for each PHP module.
 */
public final class PhpUnitPreferences {

    private static final String BOOTSTRAP_ENABLED = "bootstrap.enabled"; // NOI18N
    private static final String BOOTSTRAP_PATH = "bootstrap.path"; // NOI18N
    private static final String BOOTSTRAP_FOR_CREATE_TESTS = "bootstrap.create.tests"; // NOI18N
    private static final String CONFIGURATION_ENABLED = "configuration.enabled"; // NOI18N
    private static final String CONFIGURATION_PATH = "configuration.path"; // NOI18N
    private static final String CUSTOM_SUITE_ENABLED = "customSuite.enabled"; // NOI18N
    private static final String CUSTOM_SUITE_PATH = "customSuite.path"; // NOI18N
    private static final String PHP_UNIT_ENABLED = "phpUnit.enabled"; // NOI18N
    private static final String PHP_UNIT_PATH = "phpUnit.path"; // NOI18N
    private static final String RUN_PHPUNIT_ONLY = "test.run.phpunit.only"; // NOI18N
    private static final String RUN_ALL_TEST_FILES = "test.run.all"; // NOI18N
    private static final String ASK_FOR_TEST_GROUPS = "test.groups.ask"; // NOI18N
    private static final String TEST_GROUPS = "test.groups"; // NOI18N
    private static final String TEST_GROUPS_DELIMITER = ","; // NOI18N


    private PhpUnitPreferences() {
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

    public static boolean isBootstrapForCreateTests(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(BOOTSTRAP_FOR_CREATE_TESTS, false);
    }

    public static void setBootstrapForCreateTests(PhpModule phpModule, boolean bootstrapEnabled) {
        getPreferences(phpModule).putBoolean(BOOTSTRAP_FOR_CREATE_TESTS, bootstrapEnabled);
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

    public static boolean isCustomSuiteEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(CUSTOM_SUITE_ENABLED, false);
    }

    public static void setCustomSuiteEnabled(PhpModule phpModule, boolean customSuiteEnabled) {
        getPreferences(phpModule).putBoolean(CUSTOM_SUITE_ENABLED, customSuiteEnabled);
    }

    public static String getCustomSuitePath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(CUSTOM_SUITE_PATH, null));
    }

    public static void setCustomSuitePath(PhpModule phpModule, String customSuitePath) {
        getPreferences(phpModule).put(CUSTOM_SUITE_PATH, relativizePath(phpModule, customSuitePath));
    }

    public static boolean isPhpUnitEnabled(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(PHP_UNIT_ENABLED, false);
    }

    public static void setPhpUnitEnabled(PhpModule phpModule, boolean phpUnitEnabled) {
        getPreferences(phpModule).putBoolean(PHP_UNIT_ENABLED, phpUnitEnabled);
    }

    public static String getPhpUnitPath(PhpModule phpModule) {
        return resolvePath(phpModule, getPreferences(phpModule).get(PHP_UNIT_PATH, null));
    }

    public static void setPhpUnitPath(PhpModule phpModule, String phpUnitPath) {
        getPreferences(phpModule).put(PHP_UNIT_PATH, relativizePath(phpModule, phpUnitPath));
    }

    public static boolean getRunPhpUnitOnly(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(RUN_PHPUNIT_ONLY, false);
    }

    public static void setRunPhpUnitOnly(PhpModule phpModule, boolean runPhpUnitOnly) {
        getPreferences(phpModule).putBoolean(RUN_PHPUNIT_ONLY, runPhpUnitOnly);
    }

    public static boolean getRunAllTestFiles(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(RUN_ALL_TEST_FILES, false);
    }

    public static void setRunAllTestFiles(PhpModule phpModule, boolean runAllTestFiles) {
        getPreferences(phpModule).putBoolean(RUN_ALL_TEST_FILES, runAllTestFiles);
    }

    public static boolean getAskForTestGroups(PhpModule phpModule) {
        return getPreferences(phpModule).getBoolean(ASK_FOR_TEST_GROUPS, false);
    }

    public static void setAskForTestGroups(PhpModule phpModule, boolean askForTestGroups) {
        getPreferences(phpModule).putBoolean(ASK_FOR_TEST_GROUPS, askForTestGroups);
    }

    public static List<String> getTestGroups(PhpModule phpModule) {
        return StringUtils.explode(getPreferences(phpModule).get(TEST_GROUPS, null), TEST_GROUPS_DELIMITER);
    }

    public static void setTestGroups(PhpModule phpModule, List<String> testGroups) {
        getPreferences(phpModule).put(TEST_GROUPS, StringUtils.implode(testGroups, TEST_GROUPS_DELIMITER));
    }

    private static Preferences getPreferences(PhpModule module) {
        return module.getPreferences(PhpUnitTestingProvider.class, true);
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
