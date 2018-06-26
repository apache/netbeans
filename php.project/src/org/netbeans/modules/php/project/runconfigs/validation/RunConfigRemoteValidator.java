/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
