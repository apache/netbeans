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

import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.runconfigs.RunConfigLocal;

/**
 * Validator for {@link RunConfigLocal}.
 */
public final class RunConfigLocalValidator {

    private RunConfigLocalValidator() {
    }

    public static String validateNewProject(RunConfigLocal config) {
        String error;
        error = RunConfigWebValidator.validateUrl(config.getUrl());
        if (error != null) {
            return error;
        }
        return null;
    }

    public static String validateCustomizer(RunConfigLocal config) {
        return validate(config, true);
    }

    public static String validateConfigAction(RunConfigLocal config, boolean validateIndex) {
        return validate(config, validateIndex);
    }

    private static String validate(RunConfigLocal config, boolean validateIndex) {
        String error;
        error = RunConfigWebValidator.validateUrl(config.getUrl());
        if (error != null) {
            return error;
        }
        if (validateIndex) {
            String indexRelativePath = config.getIndexRelativePath();
            if (StringUtils.hasText(indexRelativePath)) {
                error = BaseRunConfigValidator.validateIndexFile(config.getIndexParentDir(), indexRelativePath);
                if (error != null) {
                    return error;
                }
            }
        }
        return null;
    }

}
