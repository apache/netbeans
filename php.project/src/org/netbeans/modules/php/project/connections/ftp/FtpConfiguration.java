/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.ftp;

import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.openide.util.NbBundle.Messages;

/**
 * Class representing an FTP configuration.
 * @author Tomas Mysik
 * @see RemoteConfiguration
 * @see org.netbeans.modules.php.project.connections.RemoteConnections
 */
public final class FtpConfiguration extends RemoteConfiguration {

    private static final String PATH_SEPARATOR = "/"; // NOI18N

    private final String host;
    private final int port;
    private final Security security;
    private final String userName;
    private final boolean anonymousLogin;
    private final String initialDirectory;
    private final int timeout;
    private final int keepAliveInterval;
    private final boolean passiveMode;
    private final String activeExternalIp;
    private final int activePortMin;
    private final int activePortMax;
    private final boolean ignoreDisconnectErrors;

    // @GuardedBy(this)
    private String password;


    public FtpConfiguration(final ConfigManager.Configuration cfg) {
        super(cfg);

        host = cfg.getValue(FtpConnectionProvider.HOST);
        port = readNumber(FtpConnectionProvider.PORT, FtpConnectionProvider.DEFAULT_PORT);
        security = new Security(
                readEnum(Encryption.class, FtpConnectionProvider.ENCRYPTION, FtpConnectionProvider.DEFAULT_ENCRYPTION),
                readBoolean(FtpConnectionProvider.ONLY_LOGIN_ENCRYPTED, FtpConnectionProvider.DEFAULT_ONLY_LOGIN_ENCRYPTED));
        userName = cfg.getValue(FtpConnectionProvider.USER);
        anonymousLogin = Boolean.valueOf(cfg.getValue(FtpConnectionProvider.ANONYMOUS_LOGIN));
        initialDirectory = cfg.getValue(FtpConnectionProvider.INITIAL_DIRECTORY);
        timeout = readNumber(FtpConnectionProvider.TIMEOUT, FtpConnectionProvider.DEFAULT_TIMEOUT);
        keepAliveInterval = readNumber(FtpConnectionProvider.KEEP_ALIVE_INTERVAL, FtpConnectionProvider.DEFAULT_KEEP_ALIVE_INTERVAL);
        passiveMode = Boolean.valueOf(cfg.getValue(FtpConnectionProvider.PASSIVE_MODE));
        activeExternalIp = cfg.getValue(FtpConnectionProvider.ACTIVE_EXTERNAL_IP);
        activePortMin = readNumber(FtpConnectionProvider.ACTIVE_PORT_MIN, -1);
        activePortMax = readNumber(FtpConnectionProvider.ACTIVE_PORT_MAX, -1);
        ignoreDisconnectErrors = Boolean.valueOf(cfg.getValue(FtpConnectionProvider.IGNORE_DISCONNECT_ERRORS));
    }

    public boolean isAnonymousLogin() {
        return anonymousLogin;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String getInitialDirectory() {
        return initialDirectory;
    }

    public int getPort() {
        return port;
    }

    public Security getSecurity() {
        return security;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public boolean isPassiveMode() {
        return passiveMode;
    }

    public String getActiveExternalIp() {
        return activeExternalIp;
    }

    public int getActivePortMin() {
        return activePortMin;
    }

    public int getActivePortMax() {
        return activePortMax;
    }

    public boolean getIgnoreDisconnectErrors() {
        return ignoreDisconnectErrors;
    }

    /**
     * Get the user name or "anonymous" if the configuration uses anonymous login.
     * @return the user name or "anonymous".
     */
    public String getUserName() {
        if (anonymousLogin) {
            return "anonymous"; // NOI18N
        }
        return userName;
    }

    /**
     * Get the password or "nobody@nowhere.net" if the configuration uses anonymous login.
     * @return the password or "nobody@nowhere.net".
     */
    public String getPassword() {
        if (anonymousLogin) {
            return "nobody@nowhere.net"; // NOI18N
        }
        synchronized (this) {
            if (password == null) {
                password = readPassword(FtpConnectionProvider.PASSWORD);
            }
            if (password == null) {
                password = ""; // NOI18N
            }
            return password;
        }
    }

    @Override
    public String getUrl(String directory) {
        assert directory != null;
        String path = initialDirectory;
        if (directory.trim().length() > 0) {
            path += directory;
        }
        return "ftp://" + host + path.replaceAll(PATH_SEPARATOR + "{2,}", PATH_SEPARATOR); // NOI18N
    }

    @Override
    public boolean saveProperty(String key, String value) {
        if (FtpConnectionProvider.PASSWORD.equals(key)) {
            savePassword(ConfigManager.decode(value), FtpConnectionProvider.get().getDisplayName());
            return true;
        }
        return false;
    }

    @Override
    public void notifyDeleted() {
        deletePassword();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final FtpConfiguration other = (FtpConfiguration) obj;
        if ((this.host == null) ? (other.host != null) : !this.host.equals(other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if ((this.userName == null) ? (other.userName != null) : !this.userName.equals(other.userName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + (host != null ? host.hashCode() : 0);
        hash = 97 * hash + port;
        hash = 97 * hash + (userName != null ? userName.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [displayName: "); // NOI18N
        sb.append(getDisplayName());
        sb.append(", name: "); // NOI18N
        sb.append(getName());
        sb.append(", host: "); // NOI18N
        sb.append(host);
        sb.append(", port: "); // NOI18N
        sb.append(port);
        sb.append(", userName: "); // NOI18N
        sb.append(getUserName());
        sb.append(", password: *****"); // NOI18N
        sb.append(", anonymousLogin: "); // NOI18N
        sb.append(anonymousLogin);
        sb.append(", initialDirectory: "); // NOI18N
        sb.append(initialDirectory);
        sb.append(", timeout: "); // NOI18N
        sb.append(timeout);
        sb.append(", passiveMode: "); // NOI18N
        sb.append(passiveMode);
        sb.append(", activeExternalIp: "); // NOI18N
        sb.append(activeExternalIp);
        sb.append(", activePortMin: "); // NOI18N
        sb.append(activePortMin);
        sb.append(", activePortMax: "); // NOI18N
        sb.append(activePortMax);
        sb.append(", ignoreDisconnectErrors: "); // NOI18N
        sb.append(ignoreDisconnectErrors);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    //~ Inner classes

    /**
     * Security of the FTP connection.
     */
    public static final class Security {

        private final Encryption encryption;
        private final boolean onlyLoginEncrypted;

        /**
         *
         * @param encryption {@link Encryption encryption}
         * @param onlyLoginEncrypted {@code true} if only authentication process is encrypted,
         * {@code false} if the whole connection is encrypted
         */
        public Security(Encryption encryption, boolean onlyLoginEncrypted) {
            this.encryption = encryption;
            this.onlyLoginEncrypted = onlyLoginEncrypted;
        }

        /**
         * See {@link Encryption#isPresent()}.
         */
        public boolean isPresent() {
            return encryption.isPresent();
        }

        /**
         * Get {@link Encryption encryption} of the FTP connection.
         * @return {@link Encryption encryption} of the FTP connection.
         */
        public Encryption getEncryption() {
            return encryption;
        }

        /**
         * Return {@code true} if encryption {@link Encryption#isPresent() is present} and
         * if only authentication process is encrypted, {@code false} if the whole connection is encrypted.
         * @return {@code true} if encryption {@link Encryption#isPresent() is present} and
         * if only authentication process is encrypted, {@code false} if the whole connection is encrypted
         */
        public boolean isOnlyLoginEncrypted() {
            return onlyLoginEncrypted;
        }

    }

    /**
     * Enum representing encryption of FTP connection.
     */
    @Messages({
        "LBL_EncryptionNone=Pure FTP",
        "LBL_EncryptionTlsExplicit=Explicit FTP using TLS",
        "LBL_EncryptionTlsImplicit=Implicit FTP using TLS"
    })
    public static enum Encryption {

        /**
         * No encryption, pure FTP.
         */
        NONE(Bundle.LBL_EncryptionNone()) {

            @Override
            public boolean isPresent() {
                return false;
            }

            @Override
            public String getProtocol() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public boolean isImplicit() {
                throw new UnsupportedOperationException("Not supported.");
            }

        },

        /**
         * Encryption using TLS protocol, explicitly invoked.
         */
        TLS_EXPLICIT(Bundle.LBL_EncryptionTlsExplicit()) {

            @Override
            public boolean isPresent() {
                return true;
            }

            @Override
            public String getProtocol() {
                return TLS;
            }

            @Override
            public boolean isImplicit() {
                return false;
            }

        },

        /**
         * Encryption using TLS protocol, implicitly invoked.
         */
        TLS_IMPLICIT(Bundle.LBL_EncryptionTlsImplicit()) {

            @Override
            public boolean isPresent() {
                return true;
            }

            @Override
            public String getProtocol() {
                return TLS;
            }

            @Override
            public boolean isImplicit() {
                return true;
            }

        };

        private static final String TLS = "TLS"; // NOI18N

        private final String label;


        private Encryption(String title) {
            this.label = title;
        }

        /**
         * Return {@code true} if the connection is encrypted, {@code false} otherwise.
         * @return {@code true} if the connection is encrypted, {@code false} otherwise
         */
        public abstract boolean isPresent();

        /**
         * Get the protocol as a string (e.g. "TLS", "SSL").
         * @return the protocol as a string
         * @throws UnsupportedOperationException if there is {@link #isPresent() no encryption}
         */
        public abstract String getProtocol();

        /**
         * Return {@code true} if the encryption is implicitly invoked, {@code false} otherwise.
         * @return {@code true} if the encryption is implicit, {@code false} otherwise
         * @throws UnsupportedOperationException if there is {@link #isPresent() no encryption}
         */
        public abstract boolean isImplicit();

        /**
         * Get the human-readable label of the encryption.
         * @return the human-readable label of the encryption.
         */
        public String getLabel() {
            return label;
        }

    }

}
