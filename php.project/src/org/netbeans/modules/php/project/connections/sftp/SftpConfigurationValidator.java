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
