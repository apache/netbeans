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
package org.netbeans.modules.selenium2.webclient.protractor.preferences;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.modules.web.common.ui.api.ExternalExecutableValidator;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class ProtractorPreferencesValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return result;
    }
    
    public ProtractorPreferencesValidator validate(Project project) {
        validateProtractor(ProtractorPreferences.getProtractor(project));
        validateUserConfigurationFile(project, ProtractorPreferences.getUserConfigurationFile(project));
        return this;
    }

    @NbBundle.Messages("ProtractorPreferencesValidator.protractor.name=Protractor")
    public ProtractorPreferencesValidator validateProtractor(String protractor) {
        String warning = ExternalExecutableValidator.validateCommand(protractor, Bundle.ProtractorPreferencesValidator_protractor_name());
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("path", warning)); // NOI18N
        }
        return this;
    }

    @NbBundle.Messages("ProtractorPreferencesValidator.userConfigurationFile.name=Configuration File")
    public ProtractorPreferencesValidator validateUserConfigurationFile(Project project, String userConfigurationFile) {
        String warning = validateFile(project, Bundle.ProtractorPreferencesValidator_userConfigurationFile_name(), userConfigurationFile, false);
        if (warning != null) {
            result.addWarning(new ValidationResult.Message("configuration file", warning)); // NOI18N
        }
        return this;
    }
    
    /**
     * Validate a file path and return {@code null} if it is valid, otherwise an error.
     * <p>
     * A valid file means that the <tt>filePath</tt> represents a valid, readable file
     * with absolute file path.
     * @param source source used in error message (e.g. "Script", "Config file")
     * @param filePath a file path to validate
     * @param writable {@code true} if the file must be writable, {@code false} otherwise
     * @return {@code null} if it is valid, otherwise an error
     */
    @NbBundle.Messages({
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.missing={0} must be selected.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notAbsolute={0} must be an absolute path.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notFile={0} must be a valid file.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notReadable={0} is not readable.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notWritable={0} is not writable.",
        "# {0} - source",
        "ProtractorPreferencesValidator.validateFile.notUnderProject={0} is not under project's directory."
    })
    @CheckForNull
    private static String validateFile(Project project, String source, String filePath, boolean writable) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_missing(source);
        }

        File file = new File(filePath);
        if (!file.isAbsolute()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notAbsolute(source);
        } else if (!file.isFile()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notFile(source);
        } else if (!file.canRead()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notReadable(source);
        } else if (writable && !file.canWrite()) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notWritable(source);
        }
        if(project != null && !project.equals(FileOwnerQuery.getOwner(FileUtil.toFileObject(file)))) {
            return Bundle.ProtractorPreferencesValidator_validateFile_notUnderProject(source);
        }
        return null;
    }
    
}
