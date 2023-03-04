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
package org.netbeans.modules.php.project.connections.ftp;

import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.common.RemoteValidator;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Validator for FTP configuration.
 */
public class FtpConfigurationValidator {

    //@GuardedBy("FtpConfigurationValidator.class")
    private static Pair<String, Boolean> lastProxy = null;

    private final ValidationResult result = new ValidationResult();


    public ValidationResult getResult() {
        return new ValidationResult(result);
    }

    public FtpConfigurationValidator validate(FtpConfiguration configuration) {
        return validate(configuration.getHost(),
                String.valueOf(configuration.getPort()),
                configuration.isAnonymousLogin(),
                configuration.getUserName(),
                configuration.getInitialDirectory(),
                String.valueOf(configuration.getTimeout()),
                String.valueOf(configuration.getKeepAliveInterval()),
                configuration.isPassiveMode(),
                configuration.getActiveExternalIp(),
                String.valueOf(configuration.getActivePortMin()),
                String.valueOf(configuration.getActivePortMax()));
    }

    @NbBundle.Messages("FtpConfigurationValidator.port.range.invalid=Min port must be lower than max port")
    public FtpConfigurationValidator validate(String host, String port, boolean isAnonymousLogin, String user, String initialDirectory,
            String timeout, String keepAliveInterval, boolean passiveMode, String externalIp, String minPort, String maxPort) {
        String err = RemoteValidator.validateHost(host);
        if (err != null) {
            result.addError(new ValidationResult.Message("host", err)); // NOI18N
        }

        err = RemoteValidator.validatePort(port);
        if (err != null) {
            result.addError(new ValidationResult.Message("port", err)); // NOI18N
        }

        validateUser(isAnonymousLogin, user);

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

        if (!passiveMode) {
            if (StringUtils.hasText(externalIp)) {
                err = RemoteValidator.validateIp(externalIp);
                if (err != null) {
                    result.addError(new ValidationResult.Message("external.ip", err)); // NOI18N
                }
            }

            if (StringUtils.hasText(minPort)
                    || StringUtils.hasText(maxPort)) {
                err = RemoteValidator.validatePort(minPort);
                if (err != null) {
                    result.addError(new ValidationResult.Message("port.min", err)); // NOI18N
                }

                err = RemoteValidator.validatePort(maxPort);
                if (err != null) {
                    result.addError(new ValidationResult.Message("port.max", err)); // NOI18N
                }

                try {
                    if (Integer.parseInt(minPort) > Integer.parseInt(maxPort)) {
                        result.addError(new ValidationResult.Message("port.min", Bundle.FtpConfigurationValidator_port_range_invalid())); // NOI18N
                    }
                } catch (NumberFormatException nfe) {
                    // ignore, already handled above
                }
            }
        }

        if (result.isFaultless()) {
            validateProxy(host, passiveMode);
        }
        return this;
    }

    private void validateUser(boolean anonymousLogin, String user) {
        if (anonymousLogin) {
            return;
        }
        String err = RemoteValidator.validateUser(user);
        if (err != null) {
            result.addError(new ValidationResult.Message("user", err)); // NOI18N
        }
    }

    // #195879
    @NbBundle.Messages({
        "FtpConfigurationValidator.error.proxyAndNotPassive=Only passive mode is supported with HTTP proxy.",
        "FtpConfigurationValidator.warning.proxy=Configured HTTP proxy will be used only for Pure FTP. To avoid problems, do not use any SOCKS proxy."
    })
    private void validateProxy(final String host, boolean passiveMode) {
        if (hasProxy(host)) {
            if (!passiveMode) {
                result.addError(new ValidationResult.Message("proxy", Bundle.FtpConfigurationValidator_error_proxyAndNotPassive())); // NOI18N
            }
            result.addWarning(new ValidationResult.Message("proxy", Bundle.FtpConfigurationValidator_warning_proxy())); // NOI18N
        }
    }

    @NbBundle.Messages("FtpConfigurationValidator.proxy.detecting=Detecting HTTP proxy...")
    private static synchronized boolean hasProxy(final String host) {
        assert Thread.holdsLock(FtpConfigurationValidator.class);
        if (lastProxy != null
                && lastProxy.first().equals(host)) {
            return lastProxy.second();
        }
        final AtomicBoolean hasProxy = new AtomicBoolean();
        BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
            @Override
            public void run() {
                hasProxy.set(RemoteUtils.hasHttpProxy(host));
            }
        }, Bundle.FtpConfigurationValidator_proxy_detecting(), new AtomicBoolean(), false);
        lastProxy = Pair.of(host, hasProxy.get());
        return lastProxy.second();
    }

}
