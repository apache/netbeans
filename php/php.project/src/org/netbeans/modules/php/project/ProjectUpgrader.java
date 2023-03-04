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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

/**
 * Helper class for upgrading project.
 */
public final class ProjectUpgrader {

    private static final Map<String, String> UPGRADE_KEYS = new HashMap<>();

    private final PhpProject project;


    static {
        setPhpUnitKeys();
    }


    public ProjectUpgrader(PhpProject project) {
        this.project = project;
    }

    public void upgrade() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                upgradeProjectProperties();
            }
        });
    }


    void upgradeProjectProperties() {
        EditableProperties properties = project.getHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // specific upgrades
        upgradePhpUnit(properties);
        // general key replace
        for (Map.Entry<String, String> entry : UPGRADE_KEYS.entrySet()) {
            String property = properties.getProperty(entry.getKey());
            if (property != null) {
                properties.remove(entry.getKey());
                properties.put(entry.getValue(), property);
            }
        }
        project.getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);
    }

    //~ PhpUnit

    private static final String PHP_UNIT_BOOTSTRAP = "phpunit.bootstrap"; // NOI18N
    private static final String PHP_UNIT_CONFIGURATION = "phpunit.configuration"; // NOI18N
    private static final String PHP_UNIT_SUITE = "phpunit.suite"; // NOI18N
    private static final String PHP_UNIT_SCRIPT = "phpunit.script"; // NOI18N

    private static void setPhpUnitKeys() {
        UPGRADE_KEYS.put(PHP_UNIT_BOOTSTRAP, "auxiliary.org-netbeans-modules-php-phpunit.bootstrap_2e_path"); // NOI18N
        UPGRADE_KEYS.put("phpunit.bootstrap.create.tests", "auxiliary.org-netbeans-modules-php-phpunit.bootstrap_2e_create_2e_tests"); // NOI18N
        UPGRADE_KEYS.put(PHP_UNIT_CONFIGURATION, "auxiliary.org-netbeans-modules-php-phpunit.configuration_2e_path"); // NOI18N
        UPGRADE_KEYS.put(PHP_UNIT_SUITE, "auxiliary.org-netbeans-modules-php-phpunit.customSuite_2e_path"); // NOI18N
        UPGRADE_KEYS.put(PHP_UNIT_SCRIPT, "auxiliary.org-netbeans-modules-php-phpunit.phpUnit_2e_path"); // NOI18N
        UPGRADE_KEYS.put("phpunit.run.test.files", "auxiliary.org-netbeans-modules-php-phpunit.test_2e_run_2e_all"); // NOI18N
        UPGRADE_KEYS.put("phpunit.test.groups.ask", "auxiliary.org-netbeans-modules-php-phpunit.test_2e_groups_2e_ask"); // NOI18N
        UPGRADE_KEYS.put("phpunit.test.groups.last.used", "auxiliary.org-netbeans-modules-php-phpunit.test_2e_groups"); // NOI18N
    }

    private void upgradePhpUnit(EditableProperties properties) {
        Map<String, String> newlyEnabledProperties = new HashMap<>();
        newlyEnabledProperties.put(PHP_UNIT_BOOTSTRAP, "auxiliary.org-netbeans-modules-php-phpunit.bootstrap_2e_enabled"); // NOI18N
        newlyEnabledProperties.put(PHP_UNIT_CONFIGURATION, "auxiliary.org-netbeans-modules-php-phpunit.configuration_2e_enabled"); // NOI18N
        newlyEnabledProperties.put(PHP_UNIT_SUITE, "auxiliary.org-netbeans-modules-php-phpunit.customSuite_2e_enabled"); // NOI18N
        newlyEnabledProperties.put(PHP_UNIT_SCRIPT, "auxiliary.org-netbeans-modules-php-phpunit.phpUnit_2e_enabled"); // NOI18N
        for (Map.Entry<String, String> entry : newlyEnabledProperties.entrySet()) {
            if (StringUtils.hasText(properties.get(entry.getKey()))) {
                properties.setProperty(entry.getValue(), "true"); // NOI18N
            }
        }
    }

}
