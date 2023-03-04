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
package org.netbeans.modules.web.common.ui.api;

import java.io.File;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;

/**
 *
 * @since 1.74
 */
public final class ExternalExecutableValidator {

    private ExternalExecutableValidator() {
    }

    /**
     * Return {@code true} if the given command is {@link #validateCommand(String, String) valid}.
     * @param command command to be validated, can be {@code null}
     * @return {@code true} if the given command is {@link #validateCommand(String, String) valid}, {@code false} otherwise
     */
    public static boolean isValidCommand(@NullAllowed String command) {
        return validateCommand(command, (String) null) == null;
    }

    /**
     * Validate the given command and return error if it is not valid, {@code null} otherwise.
     * @param command command to be validated, can be {@code null}
     * @param executableName the name of the executable (e.g. "Doctrine script"), can be {@code null} (in such case, "File" is used)
     * @return error if it is not valid, {@code null} otherwise
     */
    public static String validateCommand(@NullAllowed String command, @NullAllowed String executableName) {
        String executable = null;
        if (command != null) {
            executable = ExternalExecutable.parseCommand(command).first();
        }
        if (executableName == null) {
            return validateFile(executable, false);
        }
        return validateFile(executableName, executable, false);
    }

    @NbBundle.Messages("ExternalExecutableValidator.validateFile.file=File")
    @CheckForNull
    private static String validateFile(String filePath, boolean writable) {
        return validateFile(Bundle.ExternalExecutableValidator_validateFile_file(), filePath, writable);
    }

    @NbBundle.Messages({
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.missing={0} must be selected.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notAbsolute={0} must be an absolute path.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notFile={0} must be a valid file.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notReadable={0} is not readable.",
        "# {0} - source",
        "ExternalExecutableValidator.validateFile.notWritable={0} is not writable."
    })
    @CheckForNull
    private static String validateFile(String source, String filePath, boolean writable) {
        if (filePath == null
                || filePath.trim().isEmpty()) {
            return Bundle.ExternalExecutableValidator_validateFile_missing(source);
        }

        File file = new File(filePath);
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
