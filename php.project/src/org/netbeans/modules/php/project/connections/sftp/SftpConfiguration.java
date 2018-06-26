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

package org.netbeans.modules.php.project.connections.sftp;

import java.util.logging.Logger;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;

/**
 * Class representing an SFTP configuration.
 * @author Tomas Mysik
 * @see RemoteConfiguration
 * @see org.netbeans.modules.php.project.connections.RemoteConnections
 */
public final class SftpConfiguration extends RemoteConfiguration {

    private static final Logger LOGGER = Logger.getLogger(SftpConfiguration.class.getName());
    private static final String PATH_SEPARATOR = "/"; // NOI18N

    private final String host;
    private final int port;
    private final String userName;
    private final String knownHostsFile;
    private final String identityFile;
    private final String initialDirectory;
    private final int timeout;
    private final int keepAliveInterval;

    // @GuardedBy(this)
    private String password;


    public SftpConfiguration(final ConfigManager.Configuration cfg) {
        super(cfg);

        host = cfg.getValue(SftpConnectionProvider.HOST);
        port = readNumber(SftpConnectionProvider.PORT, SftpConnectionProvider.DEFAULT_PORT);
        userName = cfg.getValue(SftpConnectionProvider.USER);
        knownHostsFile = cfg.getValue(SftpConnectionProvider.KNOWN_HOSTS_FILE);
        identityFile = cfg.getValue(SftpConnectionProvider.IDENTITY_FILE);
        initialDirectory = cfg.getValue(SftpConnectionProvider.INITIAL_DIRECTORY);
        timeout = readNumber(SftpConnectionProvider.TIMEOUT, SftpConnectionProvider.DEFAULT_TIMEOUT);
        keepAliveInterval = readNumber(SftpConnectionProvider.KEEP_ALIVE_INTERVAL, SftpConnectionProvider.DEFAULT_KEEP_ALIVE_INTERVAL);
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

    public int getTimeout() {
        return timeout;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public String getUserName() {
        return userName;
    }

    public synchronized String getPassword() {
        if (password == null) {
            password = readPassword(SftpConnectionProvider.PASSWORD);
        }
        if (password == null) {
            password = ""; // NOI18N
        }
        return password;
    }

    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    public String getIdentityFile() {
        return identityFile;
    }

    @Override
    public String getUrl(String directory) {
        assert directory != null;
        String path = initialDirectory;
        if (directory.trim().length() > 0) {
            path += directory;
        }
        return "sftp://" + host + path.replaceAll(PATH_SEPARATOR + "{2,}", PATH_SEPARATOR); // NOI18N
    }

    @Override
    public boolean saveProperty(String key, String value) {
        if (SftpConnectionProvider.PASSWORD.equals(key)) {
            savePassword(ConfigManager.decode(value), SftpConnectionProvider.get().getDisplayName());
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
        final SftpConfiguration other = (SftpConfiguration) obj;
        if ((this.host == null) ? (other.host != null) : !this.host.equals(other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if ((this.userName == null) ? (other.userName != null) : !this.userName.equals(other.userName)) {
            return false;
        }
        if ((this.identityFile == null) ? (other.identityFile != null) : !this.identityFile.equals(other.identityFile)) {
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
        hash = 97 * hash + (identityFile != null ? identityFile.hashCode() : 0);
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
        sb.append(", knownHostsFile: "); // NOI18N
        sb.append(knownHostsFile);
        sb.append(", identityFile: "); // NOI18N
        sb.append(identityFile);
        sb.append(", initialDirectory: "); // NOI18N
        sb.append(initialDirectory);
        sb.append(", timeout: "); // NOI18N
        sb.append(timeout);
        sb.append("]"); // NOI18N
        return sb.toString();
    }
}
