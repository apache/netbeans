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
package org.netbeans.modules.php.api.executable;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.util.FileUtils;

/**
 * Validator for {@link PhpExecutable}.
 */
public final class PhpExecutableValidator {

    private PhpExecutableValidator() {
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
            executable = PhpExecutable.parseCommand(command).first();
        }
        if (executableName == null) {
            return FileUtils.validateFile(executable, false);
        }
        return FileUtils.validateFile(executableName, executable, false);
    }

    /**
     * Validate the given command and return error if it is not valid, {@code null} otherwise.
     * @param command command to be validated
     * @param validationHandler handler to be used for validation
     * @return error if it is not valid, {@code null} otherwise
     */
    public static String validateCommand(@NonNull String command, @NonNull ValidationHandler validationHandler) {
        return validationHandler.validate(PhpExecutable.parseCommand(command).first());
    }

    //~ Inner classes

    /**
     * Handler for {@link PhpExecutableValidator#validateCommand(String, PhpExecutableValidator.ValidationHandler) custom validation}.
     */
    public interface ValidationHandler {

        /**
         * Validate the given executable.
         * @param executable executable to be validated (typically the full path of a file)
         * @return error if it is not valid, {@code null} otherwise
         */
        String validate(String executable);

    }

}
