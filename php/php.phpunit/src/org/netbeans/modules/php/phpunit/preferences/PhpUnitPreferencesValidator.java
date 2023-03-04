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
package org.netbeans.modules.php.phpunit.preferences;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.phpunit.util.PhpUnitUtils;
import org.openide.util.NbBundle;

public final class PhpUnitPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public PhpUnitPreferencesValidator validate(PhpModule phpModule) {
        validateBootstrap(PhpUnitPreferences.isBootstrapEnabled(phpModule), PhpUnitPreferences.getBootstrapPath(phpModule));
        validateConfiguration(PhpUnitPreferences.isConfigurationEnabled(phpModule), PhpUnitPreferences.getConfigurationPath(phpModule));
        validateCustomSuite(PhpUnitPreferences.isCustomSuiteEnabled(phpModule), PhpUnitPreferences.getCustomSuitePath(phpModule));
        validatePhpUnit(PhpUnitPreferences.isPhpUnitEnabled(phpModule), PhpUnitPreferences.getPhpUnitPath(phpModule));
        return this;
    }

    @NbBundle.Messages("PhpUnitPreferencesValidator.bootstrap.label=Bootstrap")
    public PhpUnitPreferencesValidator validateBootstrap(boolean bootstrapEnabled, String bootstrapPath) {
        validatePath(bootstrapEnabled, bootstrapPath, Bundle.PhpUnitPreferencesValidator_bootstrap_label(), "bootstrapPath"); // NOI18N
        return this;
    }

    @NbBundle.Messages("PhpUnitPreferencesValidator.configuration.label=XML configuration")
    public PhpUnitPreferencesValidator validateConfiguration(boolean configurationEnabled, String configurationPath) {
        validatePath(configurationEnabled, configurationPath, Bundle.PhpUnitPreferencesValidator_configuration_label(), "configurationPath"); // NOI18N
        return this;
    }

    @NbBundle.Messages("PhpUnitPreferencesValidator.customSuite.label=Custom suite")
    public PhpUnitPreferencesValidator validateCustomSuite(boolean customSuiteEnabled, String customSuitePath) {
        validatePath(customSuiteEnabled, customSuitePath, Bundle.PhpUnitPreferencesValidator_customSuite_label(), "customSuitePath"); // NOI18N
        return this;
    }

    public PhpUnitPreferencesValidator validatePhpUnit(boolean phpUnitEnabled, String phpUnitPath) {
        if (phpUnitEnabled) {
            String warning = PhpUnitUtils.validatePhpUnitPath(phpUnitPath);
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("phpUnitPath", warning)); // NOI18N
            }
        }
        return this;
    }

    private void validatePath(boolean pathEnabled, String path, String label, String source) {
        if (!pathEnabled) {
            return;
        }
        String warning = FileUtils.validateFile(label, path, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message(source, warning));
        }
    }

}
