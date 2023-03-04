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
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.common.RemoteValidator;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.ui.customizer.RunAsRemoteWeb;
import org.openide.util.NbBundle;

/**
 * Validator for {@link RunConfigRemote}.
 */
public final class RunConfigRemoteValidator {

    private RunConfigRemoteValidator() {
    }

    public static String validateNewProject(RunConfigRemote config) {
        return validate(config, false);
    }

    public static String validateCustomizer(RunConfigRemote config) {
        return validate(config, true);
    }

    public static String validateRemoteTransfer(RunConfigRemote config) {
        String error;
        error = validateRemoteConfiguration(config.getRemoteConfiguration());
        if (error != null) {
            return error;
        }
        String uploadDirectory = config.getUploadDirectory();
        if (StringUtils.hasText(uploadDirectory)) {
            error = validateUploadDirectory(uploadDirectory);
            if (error != null) {
                return error;
            }
        }
        return null;
    }

    public static String validateConfigAction(RunConfigRemote config, boolean validateIndex) {
        return validate(config, validateIndex);
    }

    private static String validate(RunConfigRemote config, boolean validateIndex) {
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
        error = validateRemoteConfiguration(config.getRemoteConfiguration());
        if (error != null) {
            return error;
        }
        String uploadDirectory = config.getUploadDirectory();
        if (StringUtils.hasText(uploadDirectory)) {
            error = validateUploadDirectory(uploadDirectory);
            if (error != null) {
                return error;
            }
        }
        error = validateUploadFilesType(config.getUploadFilesType());
        if (error != null) {
            return error;
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - error",
        "RunConfigRemoteValidator.error.remoteConnection=Remote Connection: {0}"
    })
    static String validateRemoteConfiguration(RemoteConfiguration remoteConfiguration) {
        if (remoteConfiguration == null
                || remoteConfiguration == RunConfigRemote.NO_REMOTE_CONFIGURATION) {
            return NbBundle.getMessage(RunAsRemoteWeb.class, "MSG_NoConfigurationSelected");
        } else if (remoteConfiguration == RunConfigRemote.MISSING_REMOTE_CONFIGURATION) {
            return NbBundle.getMessage(RunAsRemoteWeb.class, "MSG_NonExistingConfigurationSelected");
        }
        ValidationResult validationResult = RemoteConnections.get().validateRemoteConfiguration(remoteConfiguration);
        if (validationResult != null && validationResult.hasErrors()) {
            return Bundle.RunConfigRemoteValidator_error_remoteConnection(validationResult.getErrors().get(0).getMessage());
        }
        return null;
    }

    static String validateUploadDirectory(String uploadDirectory) {
        return RemoteValidator.validateUploadDirectory(uploadDirectory);
    }

    @NbBundle.Messages("RunConfigRemoteValidator.error.uploadFilesType.none=Upload files type must be selected.")
    static String validateUploadFilesType(UploadFiles uploadFilesType) {
        if (uploadFilesType == null) {
            return Bundle.RunConfigRemoteValidator_error_uploadFilesType_none();
        }
        return null;
    }

}
