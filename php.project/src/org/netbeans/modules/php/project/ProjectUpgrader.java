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
