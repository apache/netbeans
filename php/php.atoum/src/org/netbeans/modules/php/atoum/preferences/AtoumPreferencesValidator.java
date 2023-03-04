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
package org.netbeans.modules.php.atoum.preferences;

import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.atoum.util.AtoumUtils;
import org.openide.util.NbBundle;

public final class AtoumPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public AtoumPreferencesValidator validate(PhpModule phpModule) {
        validateBootstrap(AtoumPreferences.isBootstrapEnabled(phpModule), AtoumPreferences.getBootstrapPath(phpModule));
        validateConfiguration(AtoumPreferences.isConfigurationEnabled(phpModule), AtoumPreferences.getConfigurationPath(phpModule));
        validateAtoum(AtoumPreferences.isAtoumEnabled(phpModule), AtoumPreferences.getAtoumPath(phpModule));
        return this;
    }

    @NbBundle.Messages("AtoumPreferencesValidator.bootstrap.label=Bootstrap")
    public AtoumPreferencesValidator validateBootstrap(boolean bootstrapEnabled, String bootstrapPath) {
        validatePath(bootstrapEnabled, bootstrapPath, Bundle.AtoumPreferencesValidator_bootstrap_label(), "bootstrap.path"); // NOI18N
        return this;
    }

    @NbBundle.Messages("AtoumPreferencesValidator.configuration.label=Configuration")
    public AtoumPreferencesValidator validateConfiguration(boolean configurationEnabled, String configurationPath) {
        validatePath(configurationEnabled, configurationPath, Bundle.AtoumPreferencesValidator_configuration_label(), "configuration.path"); // NOI18N
        return this;
    }

    public AtoumPreferencesValidator validateAtoum(boolean atoumEnabled, String atoumPath) {
        if (atoumEnabled) {
            String warning = AtoumUtils.validateAtoumPath(atoumPath);
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("atoum.path", warning)); // NOI18N
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
