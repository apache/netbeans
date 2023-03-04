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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.spi.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.netbeans.modules.php.project.connections.spi.RemoteConnectionProvider;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public final class FtpConnectionProvider implements RemoteConnectionProvider {
    public static final String FTP_CONNECTION_TYPE = "FTP"; // NOI18N

    static final String TYPE = "type"; // NOI18N
    static final String HOST = "host"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String ENCRYPTION = "encryption"; // NOI18N
    static final String ONLY_LOGIN_ENCRYPTED = "onlyLoginEncrypted"; // NOI18N
    static final String USER = "user"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    static final String ANONYMOUS_LOGIN = "anonymousLogin"; // NOI18N
    static final String INITIAL_DIRECTORY = "initialDirectory"; // NOI18N
    static final String TIMEOUT = "timeout"; // NOI18N
    static final String KEEP_ALIVE_INTERVAL = "keepAliveInterval"; // NOI18N
    static final String PASSIVE_MODE = "passiveMode"; // NOI18N
    static final String ACTIVE_EXTERNAL_IP = "activeExternalIp"; // NOI18N
    static final String ACTIVE_PORT_MIN = "activePortMin"; // NOI18N
    static final String ACTIVE_PORT_MAX = "activePortMax"; // NOI18N
    static final String IGNORE_DISCONNECT_ERRORS = "ignoreDisconnectErrors"; // NOI18N

    private static final Set<String> PROPERTIES = new HashSet<>(Arrays.asList(
        TYPE,
        HOST,
        PORT,
        ENCRYPTION,
        ONLY_LOGIN_ENCRYPTED,
        USER,
        PASSWORD,
        ANONYMOUS_LOGIN,
        INITIAL_DIRECTORY,
        TIMEOUT,
        KEEP_ALIVE_INTERVAL,
        PASSIVE_MODE,
        ACTIVE_EXTERNAL_IP,
        ACTIVE_PORT_MIN,
        ACTIVE_PORT_MAX,
        IGNORE_DISCONNECT_ERRORS
    ));
    static final int DEFAULT_PORT = 21;
    static final FtpConfiguration.Encryption DEFAULT_ENCRYPTION = FtpConfiguration.Encryption.NONE;
    static final boolean DEFAULT_ONLY_LOGIN_ENCRYPTED = false;
    static final int DEFAULT_TIMEOUT = 30;
    static final int DEFAULT_KEEP_ALIVE_INTERVAL = 30;
    private static final String DEFAULT_INITIAL_DIRECTORY = "/"; // NOI18N

    private FtpConnectionProvider() {
    }

    public static FtpConnectionProvider get() {
        return new FtpConnectionProvider();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(FtpConnectionProvider.class, "LBL_Ftp");
    }

    @Override
    public Set<String> getPropertyNames() {
        return PROPERTIES;
    }

    @Override
    public RemoteConfiguration createRemoteConfiguration(ConfigManager.Configuration configuration) {
        configuration.putValue(TYPE, FTP_CONNECTION_TYPE);
        configuration.putValue(HOST, ""); // NOI18N
        configuration.putValue(PORT, String.valueOf(DEFAULT_PORT));
        configuration.putValue(ENCRYPTION, DEFAULT_ENCRYPTION.name());
        configuration.putValue(ONLY_LOGIN_ENCRYPTED, String.valueOf(DEFAULT_ONLY_LOGIN_ENCRYPTED));
        configuration.putValue(USER, ""); // NOI18N
        configuration.putValue(PASSWORD, ""); // NOI18N
        configuration.putValue(ANONYMOUS_LOGIN, String.valueOf(false));
        configuration.putValue(INITIAL_DIRECTORY, DEFAULT_INITIAL_DIRECTORY);
        configuration.putValue(TIMEOUT, String.valueOf(DEFAULT_TIMEOUT));
        configuration.putValue(KEEP_ALIVE_INTERVAL, String.valueOf(DEFAULT_KEEP_ALIVE_INTERVAL));
        configuration.putValue(PASSIVE_MODE, String.valueOf(true));
        configuration.putValue(ACTIVE_EXTERNAL_IP, "");
        configuration.putValue(ACTIVE_PORT_MIN, "");
        configuration.putValue(ACTIVE_PORT_MAX, "");
        configuration.putValue(IGNORE_DISCONNECT_ERRORS, String.valueOf(true));

        assert accept(configuration) : "Not my configuration?!";

        return new FtpConfiguration(configuration);
    }

    @Override
    public RemoteConfiguration getRemoteConfiguration(ConfigManager.Configuration configuration) {
        if (accept(configuration)) {
            return new FtpConfiguration(configuration);
        }
        return null;
    }

    @Override
    public RemoteClient getRemoteClient(RemoteConfiguration remoteConfiguration, InputOutput io) {
        if (remoteConfiguration instanceof FtpConfiguration) {
            return new FtpClient((FtpConfiguration) remoteConfiguration, io);
        }
        return null;
    }

    @Override
    public RemoteConfigurationPanel getRemoteConfigurationPanel(ConfigManager.Configuration configuration) {
        if (accept(configuration)) {
            return new FtpConfigurationPanel();
        }
        return null;
    }

    private boolean accept(ConfigManager.Configuration configuration) {
        String type = configuration.getValue(TYPE);
        return FTP_CONNECTION_TYPE.equals(type);
    }

    @Override
    public ValidationResult validate(RemoteConfiguration remoteConfiguration) {
        if (remoteConfiguration instanceof FtpConfiguration) {
            return new FtpConfigurationValidator().validate((FtpConfiguration) remoteConfiguration).getResult();
        }
        return null;
    }

}
