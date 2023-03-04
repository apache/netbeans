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
package org.netbeans.modules.php.codeception.preferences;

import java.io.File;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.codeception.commands.Codecept;
import org.netbeans.modules.php.codeception.util.CodeceptionUtils;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class CodeceptionPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public CodeceptionPreferencesValidator validate(PhpModule phpModule) {
        validateCodecept(CodeceptionPreferences.isCustomCodeceptEnabled(phpModule), CodeceptionPreferences.getCustomCodeceptPath(phpModule));
        return this;
    }

    public CodeceptionPreferencesValidator validateCodecept(boolean codeceptEnabled, @NullAllowed String codeceptPath) {
        if (codeceptEnabled) {
            String warning = CodeceptionUtils.validateCodeceptPath(codeceptPath);
            if (warning != null) {
                result.addWarning(new ValidationResult.Message("codeceptPath", warning)); // NOI18N
            }
        }
        return this;
    }

    @NbBundle.Messages({
        "CodeceptionPreferencesValidator.incorrect.codeception.yml.fileName=codeception.yml or codeception.dist.yml must be set.",
        "CodeceptionPreferencesValidator.valid.codeception.yml.fileNames=codeception.yml or codeception.dist.yml"
    })
    public CodeceptionPreferencesValidator validateCodeceptionYml(boolean codeceptionYmlEnabled, @NullAllowed String codeceptionYmlPath) {
        if (codeceptionYmlEnabled) {
            validatePath(codeceptionYmlEnabled, codeceptionYmlPath, Bundle.CodeceptionPreferencesValidator_valid_codeception_yml_fileNames(), "codeceptionYmlPath"); // NOI18N
            if (codeceptionYmlPath != null) {
                File file = new File(codeceptionYmlPath);
                if (file.exists()) {
                    String fileName = FileUtil.toFileObject(file).getNameExt();
                    if (!Codecept.CODECEPTION_CONFIG_FILE_NAME.equals(fileName)
                            && !Codecept.CODECEPTION_DIST_CONFIG_FILE_NAME.equals(fileName)) {
                        result.addWarning(new ValidationResult.Message("codeceptionYmlPath", // NOI18N
                                Bundle.CodeceptionPreferencesValidator_incorrect_codeception_yml_fileName()));
                    }
                }
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
            result.addError(new ValidationResult.Message(source, warning));
        }
    }

    public ValidationResult getResult() {
        return result;
    }

}
