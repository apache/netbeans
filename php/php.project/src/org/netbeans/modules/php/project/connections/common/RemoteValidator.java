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

package org.netbeans.modules.php.project.connections.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.util.NbBundle;

/**
 * Validator for remote properties like port, uypload directory etc.
 */
public final class RemoteValidator {

    public static final int MINIMUM_PORT = 0;
    public static final int MAXIMUM_PORT = 65535;
    public static final String INVALID_SEPARATOR = "\\"; // NOI18N


    private RemoteValidator() {
    }

    public static String validateIp(String externalIp) {
        try {
            InetAddress.getAllByName(externalIp);
        } catch (UnknownHostException ex) {
            return NbBundle.getMessage(RemoteValidator.class, "MSG_NoIpAddress");
        }
        return null;
    }

    public static String validateHost(String host) {
        if (!StringUtils.hasText(host)) {
            return NbBundle.getMessage(RemoteValidator.class, "MSG_NoHostName");
        } else if (host.contains(" ")) { // NOI18N
            return NbBundle.getMessage(RemoteValidator.class, "MSG_HostNameSpaces");
        }
        return null;
    }

    public static String validateUser(String username) {
        if (!StringUtils.hasText(username)) {
            return NbBundle.getMessage(RemoteValidator.class, "MSG_NoUserName");
        }
        return null;
    }

    public static String validatePort(String port) {
        String err = null;
        try {
            int p = Integer.parseInt(port);
            if (p < MINIMUM_PORT || p > MAXIMUM_PORT) { // see InetSocketAddress
                err = NbBundle.getMessage(RemoteValidator.class, "MSG_PortInvalid", String.valueOf(MINIMUM_PORT), String.valueOf(MAXIMUM_PORT));
            }
        } catch (NumberFormatException nfe) {
            err = NbBundle.getMessage(RemoteValidator.class, "MSG_PortNotNumeric");
        }
        return err;
    }

    @NbBundle.Messages({
        "MSG_TimeoutNotNumeric=Timeout must be a number.",
        "MSG_TimeoutNotPositive=Timeout must be higher than or equal to 0."
    })
    public static String validateTimeout(String timeout) {
        return validatePositiveNumber(timeout, Bundle.MSG_TimeoutNotPositive(), Bundle.MSG_TimeoutNotNumeric());
    }

    /**
     * Validate keep-alive interval.
     * @param keepAliveInterval value to be validated
     * @return error message or {@code null} if keep-alive interval is correct
     */
    @NbBundle.Messages({
        "MSG_KeepAliveNotNumeric=Keep-alive interval must be a number.",
        "MSG_KeepAliveNotPositive=Keep-alive interval must be higher than or equal to 0."
    })
    public static String validateKeepAliveInterval(String keepAliveInterval) {
        return validatePositiveNumber(keepAliveInterval, Bundle.MSG_KeepAliveNotPositive(), Bundle.MSG_KeepAliveNotNumeric());
    }

    @NbBundle.Messages({
        "RemoteValidator.error.uploadDirectory.missing=Upload directory must be specified.",
        "# {0} - remote path separator",
        "RemoteValidator.error.uploadDirectory.start=Upload directory must start with \"{0}\".",
        "# {0} - invalid path separator",
        "RemoteValidator.error.uploadDirectory.content=Upload directory cannot contain \"{0}\"."
    })
    public static String validateUploadDirectory(String uploadDirectory) {
        if (!StringUtils.hasText(uploadDirectory)) {
            return Bundle.RemoteValidator_error_uploadDirectory_missing();
        } else if (!uploadDirectory.startsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
            return Bundle.RemoteValidator_error_uploadDirectory_start(TransferFile.REMOTE_PATH_SEPARATOR);
        } else if (uploadDirectory.contains(INVALID_SEPARATOR)) {
            return Bundle.RemoteValidator_error_uploadDirectory_content(INVALID_SEPARATOR);
        }
        return null;
    }


    /**
     * Validate input as a positive number.
     * @param number input to be validated
     * @param errorNotPositive error used if input is not positive number
     * @param errorNotNumeric error used if input is not number
     * @return error message or {@code null} if input is positive number
     */
    static String validatePositiveNumber(String number, String errorNotPositive, String errorNotNumeric) {
        String err = null;
        try {
            int t = Integer.parseInt(number);
            if (t < 0) {
                err = errorNotPositive;
            }
        } catch (NumberFormatException nfe) {
            err = errorNotNumeric;
        }
        return err;
    }

}
