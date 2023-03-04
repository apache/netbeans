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
package org.netbeans.modules.php.phpunit.options;

import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.phpunit.commands.PhpUnit;
import org.netbeans.modules.php.phpunit.commands.SkeletonGenerator;
import org.openide.util.NbPreferences;

/**
 * PhpUnit options.
 */
public class PhpUnitOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "phpunit"; // NOI18N

    private static final PhpUnitOptions INSTANCE = new PhpUnitOptions();

    // phpunit
    private static final String PHP_UNIT_PATH = "phpUnit.path"; // NOI18N
    // skeleton generator
    private static final String SKELETON_GENERATOR_PATH = "skeletonGenerator.path"; // NOI18N

    private volatile boolean phpUnitSearched = false;
    private volatile boolean skeletonGeneratorSearched = false;


    public static PhpUnitOptions getInstance() {
        return INSTANCE;
    }

    public String getPhpUnitPath() {
        String phpUnitPath = getPreferences().get(PHP_UNIT_PATH, null);
        if (phpUnitPath == null && !phpUnitSearched) {
            phpUnitSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(PhpUnit.SCRIPT_NAME, PhpUnit.SCRIPT_NAME_LONG, PhpUnit.SCRIPT_NAME_PHAR);
            if (!scripts.isEmpty()) {
                phpUnitPath = scripts.get(0);
                setPhpUnitPath(phpUnitPath);
            }
        }
        return phpUnitPath;
    }

    public void setPhpUnitPath(String phpUnitPath) {
        getPreferences().put(PHP_UNIT_PATH, phpUnitPath);
    }

    public String getSkeletonGeneratorPath() {
        String skeletonGeneratorPath = getPreferences().get(SKELETON_GENERATOR_PATH, null);
        if (skeletonGeneratorPath == null && !skeletonGeneratorSearched) {
            skeletonGeneratorSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(
                    SkeletonGenerator.SCRIPT_NAME, SkeletonGenerator.SCRIPT_NAME_LONG, SkeletonGenerator.SCRIPT_NAME_PHAR);
            if (!scripts.isEmpty()) {
                skeletonGeneratorPath = scripts.get(0);
                setSkeletonGeneratorPath(skeletonGeneratorPath);
            }
        }
        return skeletonGeneratorPath;
    }

    public void setSkeletonGeneratorPath(String skeletonGeneratorPath) {
        getPreferences().put(SKELETON_GENERATOR_PATH, skeletonGeneratorPath);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(PhpUnitOptions.class).node(PREFERENCES_PATH);
    }

}
