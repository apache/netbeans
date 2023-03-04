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

import java.io.File;
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
import org.openide.util.Utilities;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public final class SftpConnectionProvider implements RemoteConnectionProvider {
    private static final String SFTP_CONNECTION_TYPE = "SFTP"; // NOI18N
    private static final SshFiles UNKNOWN_SSH_FILES = new SshFiles();

    static final String TYPE = "type"; // NOI18N
    static final String HOST = "host"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String USER = "user"; // NOI18N
    static final String KNOWN_HOSTS_FILE = "knownHostsFile"; // NOI18N
    static final String IDENTITY_FILE = "identityFile"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    static final String INITIAL_DIRECTORY = "initialDirectory"; // NOI18N
    static final String TIMEOUT = "timeout"; // NOI18N
    static final String KEEP_ALIVE_INTERVAL = "keepAliveInterval"; // NOI18N

    private static final Set<String> PROPERTIES = new HashSet<>(Arrays.asList(
        TYPE,
        HOST,
        PORT,
        USER,
        PASSWORD,
        KNOWN_HOSTS_FILE,
        IDENTITY_FILE,
        INITIAL_DIRECTORY,
        TIMEOUT,
        KEEP_ALIVE_INTERVAL
    ));
    static final int DEFAULT_PORT = 22;
    static final int DEFAULT_TIMEOUT = 30;
    static final int DEFAULT_KEEP_ALIVE_INTERVAL = 30;
    private static final String DEFAULT_INITIAL_DIRECTORY = "/var/www"; // NOI18N

    private SftpConnectionProvider() {
    }

    public static SftpConnectionProvider get() {
        return new SftpConnectionProvider();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(SftpConnectionProvider.class, "LBL_Sftp");
    }

    @Override
    public Set<String> getPropertyNames() {
        return PROPERTIES;
    }

    @Override
    public RemoteConfiguration createRemoteConfiguration(ConfigManager.Configuration configuration) {
        SshFiles sshFiles = getDefaultSshFiles();
        configuration.putValue(TYPE, SFTP_CONNECTION_TYPE);
        configuration.putValue(HOST, ""); // NOI18N
        configuration.putValue(PORT, String.valueOf(DEFAULT_PORT));
        configuration.putValue(USER, ""); // NOI18N
        configuration.putValue(PASSWORD, ""); // NOI18N
        configuration.putValue(KNOWN_HOSTS_FILE, sshFiles.getKnownHostsFile());
        configuration.putValue(IDENTITY_FILE, sshFiles.getIdentityFile());
        configuration.putValue(INITIAL_DIRECTORY, DEFAULT_INITIAL_DIRECTORY);
        configuration.putValue(TIMEOUT, String.valueOf(DEFAULT_TIMEOUT));

        assert accept(configuration) : "Not my configuration?!";

        return new SftpConfiguration(configuration);
    }

    @Override
    public RemoteConfiguration getRemoteConfiguration(ConfigManager.Configuration configuration) {
        if (accept(configuration)) {
            return new SftpConfiguration(configuration);
        }
        return null;
    }

    @Override
    public RemoteClient getRemoteClient(RemoteConfiguration remoteConfiguration, InputOutput io) {
        if (remoteConfiguration instanceof SftpConfiguration) {
            return new SftpClient((SftpConfiguration) remoteConfiguration, io);
        }
        return null;
    }

    @Override
    public RemoteConfigurationPanel getRemoteConfigurationPanel(ConfigManager.Configuration configuration) {
        if (accept(configuration)) {
            return new SftpConfigurationPanel();
        }
        return null;
    }

    @Override
    public ValidationResult validate(RemoteConfiguration remoteConfiguration) {
        if (remoteConfiguration instanceof SftpConfiguration) {
            return new SftpConfigurationValidator().validate((SftpConfiguration) remoteConfiguration).getResult();
        }
        return null;
    }


    private boolean accept(ConfigManager.Configuration configuration) {
        String type = configuration.getValue(TYPE);
        return SFTP_CONNECTION_TYPE.equals(type);
    }

    private SshFiles getDefaultSshFiles() {
        if (Utilities.isWindows()) {
            return getDefaultWindowsSshFiles();
        } else if (Utilities.isUnix()) {
            return getDefaultUnixSshFiles();
        }
        return UNKNOWN_SSH_FILES;
    }

    private SshFiles getDefaultUnixSshFiles() {
        String identityFile = ""; // NOI18N
        String knownHostsFile = ""; // NOI18N

        File sshDir = new File(System.getProperty("user.home"), ".ssh"); // NOI18N
        File dsaKey = new File(sshDir, "id_dsa"); // NOI18N
        File rsaKey = new File(sshDir, "id_rsa"); // NOI18N
        if (dsaKey.isFile()) {
            identityFile = dsaKey.getAbsolutePath();
        } else if (rsaKey.isFile()) {
            identityFile = rsaKey.getAbsolutePath();
        }
        File knownHosts = new File(sshDir, "known_hosts"); // NOI18N
        if (knownHosts.isFile()) {
            knownHostsFile = knownHosts.getAbsolutePath();
        }

        return new SshFiles(identityFile, knownHostsFile);
    }

    // XXX who knows? who cares...
    private SshFiles getDefaultWindowsSshFiles() {
        return UNKNOWN_SSH_FILES;
    }

    private static final class SshFiles {
        private final String identityFile;
        private final String knownHostsFile;

        /** Constructor for no SSH files. */
        public SshFiles() {
            this("", ""); // NOI18N
        }

        public SshFiles(String identityFile, String knownHostsFile) {
            assert identityFile != null;
            assert knownHostsFile != null;

            this.identityFile = identityFile;
            this.knownHostsFile = knownHostsFile;
        }

        public String getIdentityFile() {
            return identityFile;
        }

        public String getKnownHostsFile() {
            return knownHostsFile;
        }
    }
}
