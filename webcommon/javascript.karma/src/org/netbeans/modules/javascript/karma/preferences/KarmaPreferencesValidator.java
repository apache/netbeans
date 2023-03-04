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

package org.netbeans.modules.javascript.karma.preferences;

import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.util.FileUtils;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.util.NbBundle;

public final class KarmaPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public KarmaPreferencesValidator validate(Project project) {
        validateConfig(KarmaPreferences.getConfig(project));
        validateDebug(KarmaPreferences.isDebug(project), KarmaPreferences.getDebugBrowserId(project));
        return this;
    }

    @NbBundle.Messages("KarmaPreferencesValidator.config.name=Configuration")
    public KarmaPreferencesValidator validateConfig(String config) {
        String warning = FileUtils.validateFile(Bundle.KarmaPreferencesValidator_config_name(), config, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("config", warning)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("KarmaPreferencesValidator.debug.browser=Browser for debugging must be set.")
    public KarmaPreferencesValidator validateDebug(boolean debug, String debugBrowserId) {
        if (debug
                && debugBrowserId == null) {
            result.addWarning(new ValidationResult.Message("debugBrowser", Bundle.KarmaPreferencesValidator_debug_browser())); // NOI18N
        }
        return this;
    }

}
