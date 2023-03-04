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
package org.netbeans.modules.selenium2.webclient.mocha.preferences;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class MochaPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }

    @NbBundle.Messages("MochaPreferencesValidator.mocha.name=Mocha")
    public MochaPreferencesValidator validateMochaInstallFolder(String mochaInstallFolder) {
        String warning = validateMochaExec(Bundle.MochaPreferencesValidator_mocha_name(), mochaInstallFolder, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("path", warning)); // NOI18N
        }
        return this;
    }
    
    @NbBundle.Messages({
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.missing={0} install location must be specified.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notAbsolute={0} install location must be an absolute path.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notFile={0} install location is invalid (\"./bin/mocha\" executable could not be located).",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notReadable={0} is not readable.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notWritable={0} is not writable."
    })
    @CheckForNull
    private static String validateMochaExec(String source, String filePath, boolean writable) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return Bundle.ExternalExecutableValidator_validateFile_missing(source);
        }

        File file = new File(filePath + "/bin/mocha");
        if (!file.isAbsolute()) {
            return Bundle.ExternalExecutableValidator_validateFile_notAbsolute(source);
        } else if (!file.isFile()) {
            return Bundle.ExternalExecutableValidator_validateFile_notFile(source);
        } else if (!file.canRead()) {
            return Bundle.ExternalExecutableValidator_validateFile_notReadable(source);
        } else if (writable && !file.canWrite()) {
            return Bundle.ExternalExecutableValidator_validateFile_notWritable(source);
        }
        return null;
    }
    
}
