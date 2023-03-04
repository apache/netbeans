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
package org.netbeans.modules.php.project.runconfigs.validation;

import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpInterpreter;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.runconfigs.RunConfigScript;
import org.openide.util.NbBundle;

/**
 * Validator for {@link RunConfigScript}.
 */
public final class RunConfigScriptValidator {

    private RunConfigScriptValidator() {
    }

    public static String validateNewProject(RunConfigScript config) {
        // default interpreter will be used => no validation needed for new project (validation is done on running)
        return null;
    }

    public static String validateCustomizer(RunConfigScript config) {
        return validate(config, true);
    }

    public static String validateConfigAction(RunConfigScript config, boolean validateIndex) {
        return validate(config, validateIndex);
    }

    public static String validateRunFileWithoutProject(RunConfigScript config) {
        String error;
        error = validateWorkDir(config.getWorkDir(), false);
        if (error != null) {
            return error;
        }
        return null;
    }

    private static String validate(RunConfigScript config, boolean validateIndex) {
        String error;
        error = validateInterpreter(config.getInterpreter());
        if (error != null) {
            return error;
        }
        error = validateWorkDir(config.getWorkDir(), true);
        if (error != null) {
            return error;
        }
        if (validateIndex) {
            String indexRelativePath = config.getIndexRelativePath();
            if (StringUtils.hasText(indexRelativePath)) {
                error = BaseRunConfigValidator.validateIndexFile(config.getIndexParentDir(), indexRelativePath, false);
                if (error != null) {
                    return error;
                }
            }
        }
        return null;
    }

    private static String validateInterpreter(String interpreter) {
        try {
            PhpInterpreter.getCustom(interpreter);
        } catch (InvalidPhpExecutableException ex) {
            return ex.getLocalizedMessage();
        }
        return null;
    }

    @NbBundle.Messages("RunConfigScriptValidator.workDir.label=Working directory")
    static String validateWorkDir(String workDir, boolean allowEmptyString) {
        boolean hasText = StringUtils.hasText(workDir);
        if (allowEmptyString && !hasText) {
            return null;
        }
        return FileUtils.validateDirectory(Bundle.RunConfigScriptValidator_workDir_label(), workDir, false);
    }

}
