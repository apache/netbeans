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

package org.netbeans.modules.javascript.jstestdriver.preferences;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.util.NbBundle;

public final class JsTestDriverPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    public JsTestDriverPreferencesValidator validate(Project project) {
        validateConfig(JsTestDriverPreferences.getConfig(project));
        return this;
    }

    @NbBundle.Messages("JsTestDriverPreferencesValidator.config.name=Configuration")
    public JsTestDriverPreferencesValidator validateConfig(String config) {
        String warning = validateFile(Bundle.JsTestDriverPreferencesValidator_config_name(), config, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("config", warning)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages({
        "# {0} - source",
        "JsTestDriverPreferencesValidator.validateFile.missing={0} must be selected.",
        "# {0} - source",
        "JsTestDriverPreferencesValidator.validateFile.notAbsolute={0} must be an absolute path.",
        "# {0} - source",
        "JsTestDriverPreferencesValidator.validateFile.notFile={0} must be a valid file.",
        "# {0} - source",
        "JsTestDriverPreferencesValidator.validateFile.notReadable={0} is not readable.",
        "# {0} - source",
        "JsTestDriverPreferencesValidator.validateFile.notWritable={0} is not writable."
    })
    @CheckForNull
    private String validateFile(String source, String filePath, boolean writable) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return Bundle.JsTestDriverPreferencesValidator_validateFile_missing(source);
        }

        File file = new File(filePath);
        if (!file.isAbsolute()) {
            return Bundle.JsTestDriverPreferencesValidator_validateFile_notAbsolute(source);
        } else if (!file.isFile()) {
            return Bundle.JsTestDriverPreferencesValidator_validateFile_notFile(source);
        } else if (!file.canRead()) {
            return Bundle.JsTestDriverPreferencesValidator_validateFile_notReadable(source);
        } else if (writable && !file.canWrite()) {
            return Bundle.JsTestDriverPreferencesValidator_validateFile_notWritable(source);
        }
        return null;
    }

}
