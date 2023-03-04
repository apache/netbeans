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
package org.netbeans.modules.php.project.connections.sftp;

import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.connections.common.RemoteValidator;
import org.openide.util.NbBundle;

/**
 * Validator for SFTP configuration.
 */
public class SftpConfigurationValidator {

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return new ValidationResult(result);
    }

    public SftpConfigurationValidator validate(SftpConfiguration configuration) {
        return validate(configuration.getHost(),
                String.valueOf(configuration.getPort()),
                configuration.getUserName(),
                configuration.getIdentityFile(),
                configuration.getKnownHostsFile(),
                configuration.getInitialDirectory(),
                String.valueOf(configuration.getTimeout()),
                String.valueOf(configuration.getKeepAliveInterval()));
    }

    @NbBundle.Messages({
        "SftpConfigurationValidator.identityFile=Private Key",
        "SftpConfigurationValidator.knownHosts=Known Hosts"
    })
    public SftpConfigurationValidator validate(String host, String port, String user, String identityFile, String knownHostsFile, String initialDirectory,
            String timeout, String keepAliveInterval) {
        String err = RemoteValidator.validateHost(host);
        if (err != null) {
            result.addError(new ValidationResult.Message("host", err)); // NOI18N
        }

        err = RemoteValidator.validatePort(port);
        if (err != null) {
            result.addError(new ValidationResult.Message("port", err)); // NOI18N
        }

        err = RemoteValidator.validateUser(user);
        if (err != null) {
            result.addError(new ValidationResult.Message("user", err)); // NOI18N
        }

        if (StringUtils.hasText(identityFile)) {
            err = FileUtils.validateFile(Bundle.SftpConfigurationValidator_identityFile(), identityFile, false);
            if (err != null) {
                result.addError(new ValidationResult.Message("identityFile", err)); // NOI18N
            }
        }

        if (StringUtils.hasText(knownHostsFile)) {
            err = FileUtils.validateFile(Bundle.SftpConfigurationValidator_knownHosts(), knownHostsFile, false);
            if (err != null) {
                result.addError(new ValidationResult.Message("knownHostsFile", err)); // NOI18N
            }
        }

        err = RemoteValidator.validateUploadDirectory(initialDirectory);
        if (err != null) {
            result.addError(new ValidationResult.Message("initialDirectory", err)); // NOI18N
        }

        err = RemoteValidator.validateTimeout(timeout);
        if (err != null) {
            result.addError(new ValidationResult.Message("timeout", err)); // NOI18N
        }

        err = RemoteValidator.validateKeepAliveInterval(keepAliveInterval);
        if (err != null) {
            result.addError(new ValidationResult.Message("keepAliveInterval", err)); // NOI18N
        }
        return this;
    }

}
