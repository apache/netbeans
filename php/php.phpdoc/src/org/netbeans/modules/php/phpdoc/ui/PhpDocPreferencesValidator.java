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
package org.netbeans.modules.php.phpdoc.ui;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.openide.util.NbBundle;

public class PhpDocPreferencesValidator {

    private final ValidationResult result = new ValidationResult();

    public ValidationResult getResult() {
        return result;
    }

    public PhpDocPreferencesValidator validatePhpModule(PhpModule phpModule) {
        validateTarget(PhpDocPreferences.getPhpDocTarget(phpModule, false));
        validateTitle(PhpDocPreferences.getPhpDocTitle(phpModule));
        validateConfiguration(PhpDocPreferences.isConfigurationEnabled(phpModule), PhpDocPreferences.getPhpDocConfigurationPath(phpModule));
        return this;
    }

    @NbBundle.Messages({
        "PhpDocPreferencesValidator.target.label=Target",
        "PhpDocPreferencesValidator.message.ask.for.dir=NetBeans will ask for the directory before generating documentation.",
    })
    public PhpDocPreferencesValidator validateTarget(String targetPath) {
        validateDirectory(true, targetPath, Bundle.PhpDocPreferencesValidator_target_label(), "targetPath"); // NOI18N
        if (!StringUtils.hasText(targetPath)) {
            result.addWarning(new ValidationResult.Message("targetPath", Bundle.PhpDocPreferencesValidator_message_ask_for_dir())); // NOI18N
        }

        return this;
    }

    @NbBundle.Messages("PhpDocPreferencesValidator.message.invalid.title=Title must be provided.")
    public PhpDocPreferencesValidator validateTitle(String title) {
        if (!StringUtils.hasText(title)) {
            result.addError(new ValidationResult.Message("title", Bundle.PhpDocPreferencesValidator_message_invalid_title())); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("PhpDocPreferencesValidator.configuration.label=XML configuration")
    public PhpDocPreferencesValidator validateConfiguration(boolean configurationEnabled, String configurationPath) {
        validatePath(configurationEnabled, configurationPath, Bundle.PhpDocPreferencesValidator_configuration_label(), "configurationPath"); // NOI18N
        return this;
    }

    private void validateDirectory(boolean pathEnabled, String path, String label, String source) {
        if (!pathEnabled) {
            return;
        }
        if (StringUtils.hasText(path)) {
            String error = FileUtils.validateDirectory(label, path, true);
            if (error != null) {
                result.addError(new ValidationResult.Message(source, error));
            }
        }
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
