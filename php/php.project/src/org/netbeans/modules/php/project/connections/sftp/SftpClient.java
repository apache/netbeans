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

import com.jcraft.jsch.AgentConnector;
import com.jcraft.jsch.AgentIdentityRepository;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.IdentityRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.PageantConnector;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.SSHAgentConnector;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.jsch.agentproxy.ConnectorFactory;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.common.PasswordPanel;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Tomas Mysik
 */
public class SftpClient implements RemoteClient {

    private static final Logger LOGGER = Logger.getLogger(SftpClient.class.getName());

    // #226820
    private static final boolean NO_PROXY_PROPERTY = Boolean.getBoolean("nb.php.sftp.noProxy"); // NOI18N

    private static final Map<Integer, String> PASSWORDS = new HashMap<>();
    private static final Map<Integer, String> PASSPHRASES = new HashMap<>();
    private static final Map<Integer, Set<String>> MESSAGES = new HashMap<>();

    private static final SftpLogger DEV_NULL_LOGGER = new DevNullLogger();
    private final SftpConfiguration configuration;
    private final SftpLogger sftpLogger;
    // @GuardedBy("this")
    private Session sftpSession;
    // @GuardedBy("this")
    private ChannelSftp sftpClient;

    public SftpClient(SftpConfiguration configuration, InputOutput io) {
        assert configuration != null;
        this.configuration = configuration;

        sftpLogger = new SftpLogger(io);
    }

    @NbBundle.Messages("SftpConfiguration.bug.knownHosts=<html><b>Error in SFTP library detected:</b><br><br>Your Known Hosts file is too big and will not be used.")
    private void init() throws RemoteException {
        assert Thread.holdsLock(this);
        if (sftpClient != null && sftpClient.isConnected()) {
            LOGGER.log(Level.FINE, "SFTP client already created and connected");
            return;
        }
        LOGGER.log(Level.FINE, "SFTP client creating");
        JSch.setLogger(sftpLogger);

        String knownHostsFile = configuration.getKnownHostsFile();
        String identityFile = configuration.getIdentityFile();

        JSch jsch = new JSch();
        if (StringUtils.hasText(knownHostsFile)) {
            try {
                jsch.setKnownHosts(knownHostsFile);
            } catch (JSchException ex) {
                // #220328
                LOGGER.log(Level.INFO, "Error in JSCH library", ex);
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                        Bundle.SftpConfiguration_bug_knownHosts(), NotifyDescriptor.ERROR_MESSAGE));
            }
        }

        boolean agentUsed = false;
        try {
            // first try agent
            LOGGER.fine("Trying to set ssh-agent");
            agentUsed = setAgent(jsch, identityFile, true);
            LOGGER.fine("Trying to create session");
            sftpSession = createSftpSession(jsch, !agentUsed);
            try {
                LOGGER.log(Level.FINE, "Trying to connect with agent used: {0}", agentUsed);
                sftpClient = connectSftpClient();
            } catch (Exception exc) {
                // catch rather all exceptions. In case jsch-agent-proxy is broken again we should
                // at least fall back on key/pasphrase
                if (agentUsed) {
                    LOGGER.log(exc instanceof JSchException ? Level.FINE : Level.INFO, null, exc);
                    LOGGER.fine("Trying to create another session");
                    sftpSession = createSftpSession(jsch, true);
                    setAgent(jsch, identityFile, false);
                    LOGGER.fine("Trying to connect with agent used: false");
                    sftpClient = connectSftpClient();
                } else {
                    throw exc;
                }
            }
        } catch (JSchException exc) {
            // remove password from a memory storage
            PASSWORDS.remove(configuration.hashCode());
            PASSPHRASES.remove(configuration.hashCode());
            MESSAGES.remove(configuration.hashCode());
            disconnect(true);
            LOGGER.log(Level.FINE, "Exception while connecting", exc);
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotConnect", configuration.getHost()), exc);
        }
    }

    private Session createSftpSession(JSch jsch, boolean withUserInfo) throws JSchException {
        LOGGER.fine("Creating new SFTP session...");
        String host = configuration.getHost();
        int port = configuration.getPort();
        int timeout = configuration.getTimeout() * 1000;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Will connect to {0} [timeout: {1} ms]", new Object[] {host, timeout});
        }
        int keepAliveInterval = configuration.getKeepAliveInterval() * 1000;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Keep-alive interval is {0} ms", keepAliveInterval);
        }
        String username = configuration.getUserName();
        String password = configuration.getPassword();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Login as {0}", username);
        }
        Session session = jsch.getSession(username, host, port);
        if (StringUtils.hasText(password)) {
            session.setPassword(password);
        }
        // proxy
        setProxy(session, host);
        if (withUserInfo) {
            LOGGER.fine("Setting user info...");
            session.setUserInfo(new SftpUserInfo(configuration));
        }
        session.setTimeout(timeout);
        // keep-alive
        if (keepAliveInterval > 0) {
            session.setServerAliveInterval(keepAliveInterval);
        }
        return session;
    }

    private boolean setAgent(JSch jsch, String identityFile, boolean preferAgent) throws JSchException {
        boolean agentUsed = false;
        if (preferAgent) {
            AgentConnector agentConnector = ConnectorFactory.getInstance().createConnector(ConnectorFactory.ConnectorKind.ANY);
            if (agentConnector != null) {
                IdentityRepository irepo = new AgentIdentityRepository(agentConnector);
                if (irepo.getStatus() == IdentityRepository.RUNNING) {
                    jsch.setIdentityRepository(irepo);
                    agentUsed = true;
                }
            }
        }
        if (!agentUsed) {
            jsch.setIdentityRepository(null);
            // remove all identity files
            jsch.removeAllIdentity();
            // and add the one specified by CredentialsProvider
            if (StringUtils.hasText(identityFile)) {
                jsch.addIdentity(identityFile);
            }
        }
        return agentUsed;
    }

    private void setProxy(Session session, String host) {
        assert Thread.holdsLock(this);
        if (NO_PROXY_PROPERTY) {
            LOGGER.log(Level.FINE, "No proxy will be used (disabled via system property)");
            return;
        }
        Proxy proxy = null;
        // prefer socks proxy
        RemoteUtils.ProxyInfo proxyInfo = RemoteUtils.getSocksProxy(host);
        if (proxyInfo != null) {
            LOGGER.log(Level.FINE, "SOCKS proxy will be used");
            ProxySOCKS5 socksProxy = new ProxySOCKS5(proxyInfo.getHost(), proxyInfo.getPort());
            if (StringUtils.hasText(proxyInfo.getUsername())) {
                socksProxy.setUserPasswd(proxyInfo.getUsername(), proxyInfo.getPassword());
            }
            proxy = socksProxy;
        } else {
            proxyInfo = RemoteUtils.getHttpProxy(host);
            if (proxyInfo != null) {
                LOGGER.log(Level.FINE, "HTTP proxy will be used");
                ProxyHTTP httpProxy = new ProxyHTTP(proxyInfo.getHost(), proxyInfo.getPort());
                if (StringUtils.hasText(proxyInfo.getUsername())) {
                    httpProxy.setUserPasswd(proxyInfo.getUsername(), proxyInfo.getPassword());
                }
                proxy = httpProxy;
            }
        }
        if (proxy != null) {
            session.setProxy(proxy);
        }
    }

    private ChannelSftp connectSftpClient() throws JSchException {
        assert Thread.holdsLock(this);
        assert sftpSession != null;
        sftpSession.connect();
        Channel channel = sftpSession.openChannel("sftp"); // NOI18N
        channel.connect();
        return (ChannelSftp) channel;
    }

    @Override
    public synchronized void connect() throws RemoteException {
        init();
        assert sftpClient.isConnected();
        try {

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Remote server version is {0}", sftpClient.getServerVersion());
            }
        } catch (SftpException exc) {
            // can be ignored
            LOGGER.log(Level.FINE, "Exception while getting server version", exc);
        }
    }

    @Override
    public synchronized void disconnect(boolean force) throws RemoteException {
        if (sftpSession == null) {
            // nothing to do
            LOGGER.log(Level.FINE, "Remote client not created yet => nothing to do");
            return;
        }
        if (!force
                && sftpSession.getServerAliveInterval() > 0) {
            LOGGER.log(Level.FINE, "Keep-alive running and disconnecting not forced -> do nothing");
            return;
        }
        LOGGER.log(Level.FINE, "Remote client trying to disconnect");
        if (sftpSession.isConnected()) {
            LOGGER.log(Level.FINE, "Remote client connected -> disconnecting");
            JSch.setLogger(DEV_NULL_LOGGER);
            sftpSession.disconnect();
            LOGGER.log(Level.FINE, "Remote client disconnected");
        }
        sftpClient = null;
        sftpSession = null;

        sftpLogger.info("QUIT"); // NOI18N
        sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_Goodbye"));
    }

    /** not supported by JSCh */
    @Override
    public String getReplyString() {
        return null;
    }

    /** not supported by JSCh */
    @Override
    public String getNegativeReplyString() {
        return null;
    }

    @Override
    public synchronized boolean isConnected() {
        if (sftpClient == null) {
            return false;
        }
        return sftpClient.isConnected();
    }

    @Override
    public synchronized String printWorkingDirectory() throws RemoteException {
        try {
            sftpLogger.info("PWD"); // NOI18N

            String pwd = sftpClient.pwd();

            sftpLogger.info(pwd);
            return pwd;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while pwd", ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotPwd", configuration.getHost()), ex);
        }
    }

    @Override
    public synchronized boolean storeFile(String remote, InputStream local) throws RemoteException {
        try {
            sftpLogger.info("STOR " + remote); // NOI18N

            sftpClient.put(local, remote);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_FileReceiveOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while storing file " + remote, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotStoreFile", remote), ex);
        }
    }

    @Override
    public boolean deleteFile(String pathname) throws RemoteException {
        return delete(pathname, false);
    }

    @Override
    public boolean deleteDirectory(String pathname) throws RemoteException {
        return delete(pathname, true);
    }

    private synchronized boolean delete(String pathname, boolean directory) throws RemoteException {
        try {
            sftpLogger.info("DELE " + pathname); // NOI18N

            if (directory) {
                sftpClient.rmdir(pathname);
            } else {
                sftpClient.rm(pathname);
            }

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_FileDeleteOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while deleting file " + pathname, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public synchronized boolean rename(String from, String to) throws RemoteException {
        try {
            sftpLogger.info("RNFR " + from); // NOI18N
            sftpLogger.info("RNTO " + to); // NOI18N

            sftpClient.rename(from, to);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_RenameSuccessful"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, String.format("Error while renaming file %s -> %s", from, to), ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public synchronized List<RemoteFile> listFiles() throws RemoteException {
        List<RemoteFile> result = null;
        String pwd = null;
        try {
            pwd = sftpClient.pwd();
            sftpLogger.info("LIST"); // NOI18N
            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirListing"));

            @SuppressWarnings("unchecked")
            Collection<ChannelSftp.LsEntry> files = sftpClient.ls(pwd);
            result = new ArrayList<>(files.size());
            for (ChannelSftp.LsEntry entry : files) {
                result.add(new RemoteFileImpl(entry, pwd));
            }

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirectorySendOk"));
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while listing files for " + pwd, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotListFiles", pwd), ex);
        }
        return result;
    }

    @Override
    public synchronized RemoteFile listFile(String absolutePath) throws RemoteException {
        assert absolutePath.startsWith(TransferFile.REMOTE_PATH_SEPARATOR) : "Not absolute path give but: " + absolutePath;

        RemoteFile result = null;
        try {
            sftpLogger.info("LIST"); // NOI18N
            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirListing"));

            SftpATTRS attrs = sftpClient.stat(absolutePath);
            if (!attrs.isDir()) {
                @SuppressWarnings("unchecked")
                Collection<ChannelSftp.LsEntry> files = sftpClient.ls(absolutePath);
                if (files.size() == 1) {
                    for (ChannelSftp.LsEntry file : files) {
                        if (file.getFilename().equals(RemoteUtils.getName(absolutePath))) {
                            String parentPath = RemoteUtils.getParentPath(absolutePath);
                            assert parentPath != null : "Parent path should exist for " + absolutePath;
                            result = new RemoteFileImpl(file, parentPath);
                        }
                    }

                } else {
                    assert false : "Only one file should be found and not " + files.size();
                }
            }

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirectorySendOk"));
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while listing file for " + absolutePath, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotListFile", absolutePath), ex);
        }
        return result;
    }

    @Override
    public synchronized boolean retrieveFile(String remote, OutputStream local) throws RemoteException {
        try {
            sftpLogger.info("RETR " + remote); // NOI18N

            sftpClient.get(remote, local);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_FileSendOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while retrieving file " + remote, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotStoreFile", remote), ex);
        }
    }

    @Override
    public synchronized boolean changeWorkingDirectory(String pathname) throws RemoteException {
        try {
            sftpLogger.info("CWD " + pathname); // NOI18N

            sftpClient.cd(pathname);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_CdOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while changing directory " + pathname, ex);
            sftpLogger.error(NbBundle.getMessage(SftpClient.class, "LOG_CdKo"));
            return false;
        }
    }

    @Override
    public synchronized boolean makeDirectory(String pathname) throws RemoteException {
        try {
            sftpLogger.info("MKD " + pathname); // NOI18N

            sftpClient.mkdir(pathname);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_MkDirOk", pathname));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while creating directory " + pathname, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public synchronized int getPermissions(String path) throws RemoteException {
        int permissions = -1;
        try {
            sftpLogger.info("LIST " + path); // NOI18N
            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirListing"));

            ChannelSftp.LsEntry file = getFile(path);
            if (file != null) {
                permissions = file.getAttrs().getPermissions();
            }

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirectorySendOk"));
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while getting permissions for " + path, ex);
        }
        return permissions;
    }

    @Override
    public synchronized boolean setPermissions(int permissions, String path) throws RemoteException {
        try {
            sftpLogger.info(String.format("chmod %d %s", permissions, path)); // NOI18N

            sftpClient.chmod(permissions, path);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_ChmodOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while setting permissions for " + path, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public synchronized boolean exists(String parent, String name) throws RemoteException {
        String fullPath = parent + "/" + name; // NOI18N
        try {
            sftpClient.ls(fullPath);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while checking existence of " + fullPath, ex);
        }
        return false;
    }

    private ChannelSftp.LsEntry getFile(String path) throws SftpException {
        assert Thread.holdsLock(this);
        assert path != null && path.trim().length() > 0;

        @SuppressWarnings("unchecked")
        List<ChannelSftp.LsEntry> files = sftpClient.ls(path);
        // in fact, the size of the list should be exactly 1
        LOGGER.fine(String.format("Exactly 1 file should be found for %s; found %d", path, files.size()));
        if (files.size() > 0) {
            return files.get(0);
        }
        return null;
    }

    static String getPasswordForUser(SftpConfiguration configuration) {
        String password = PASSWORDS.get(configuration.hashCode());
        if (password == null) {
            PasswordPanel passwordPanel = PasswordPanel.forUser(configuration.getDisplayName(), configuration.getUserName());
            if (passwordPanel.open()) {
                password = passwordPanel.getPassword();
                PASSWORDS.put(configuration.hashCode(), password);
            }
        }
        return password;
    }

    static String getPasswordForCertificate(SftpConfiguration configuration) {
        String password = PASSPHRASES.get(configuration.hashCode());
        if (password == null) {
            PasswordPanel passwordPanel = PasswordPanel.forCertificate(configuration.getDisplayName());
            if (passwordPanel.open()) {
                password = passwordPanel.getPassword();
                PASSPHRASES.put(configuration.hashCode(), password);
            }
        }
        return password;
    }

    static void showMessageForConfiguration(SftpConfiguration configuration, String message) {
        if (!StringUtils.hasText(message)
                || getMessages(configuration).contains(message)) {
            return;
        }
        MessagePanel messagePanel = new MessagePanel(message);
        DialogDescriptor descriptor = new DialogDescriptor(
                messagePanel,
                configuration.getDisplayName(),
                true,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION) {
            if (messagePanel.doNotShowThisMessageAgain()) {
                getMessages(configuration).add(message);
            }
        }
    }

    private static Set<String> getMessages(SftpConfiguration configuration) {
        Set<String> messages;
        synchronized (MESSAGES) {
            messages = MESSAGES.get(configuration.hashCode());
            if (messages == null) {
                messages = new HashSet<>();
                MESSAGES.put(configuration.hashCode(), messages);
            }
        }
        return messages;
    }

    private static final class RemoteFileImpl implements RemoteFile {
        private final ChannelSftp.LsEntry entry;
        private final String parentDirectory;

        public RemoteFileImpl(ChannelSftp.LsEntry entry, String parentDirectory) {
            assert entry != null;
            assert parentDirectory != null;
            this.entry = entry;
            this.parentDirectory = parentDirectory;
        }

        @Override
        public String getName() {
            return entry.getFilename();
        }

        @Override
        public String getParentDirectory() {
            return parentDirectory;
        }

        @Override
        public boolean isDirectory() {
            return entry.getAttrs().isDir();
        }

        @Override
        public boolean isFile() {
            return !isDirectory();
        }

        @Override
        public boolean isLink() {
            return entry.getAttrs().isLink();
        }

        @Override
        public long getSize() {
            return entry.getAttrs().getSize();
        }

        @Override
        public long getTimestamp() {
            return entry.getAttrs().getMTime();
        }

        @Override
        public String toString() {
            return "SftpFile[name: " + getName() + ", parent directory: " + getParentDirectory() + "]";
        }

    }

    private static class SftpLogger implements com.jcraft.jsch.Logger {
        private final InputOutput io;

        public SftpLogger(InputOutput io) {
            this.io = io;
        }

        @Override
        public boolean isEnabled(int level) {
            return true;
        }

        @Override
        public void log(int level, String message) {
            if (io != null && level >= com.jcraft.jsch.Logger.INFO) {
                OutputWriter writer;
                if (level <= com.jcraft.jsch.Logger.INFO) {
                    writer = io.getOut();
                } else {
                    writer = io.getErr();
                }
                writer.println(message.trim());
                writer.flush();
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Command listener: {0}", message.trim());
            }
        }

        public void info(String message) {
            log(com.jcraft.jsch.Logger.INFO, message);
        }

        public void error(String message) {
            log(com.jcraft.jsch.Logger.ERROR, message);
        }
    }

    /**
     * Because {@link java.nio.channels.ClosedByInterruptException} is raised while disconnecting SFTP session
     * (this exception makes oproblems to NB output window).
     * @see #disconnect()
     */
    private static final class DevNullLogger extends SftpLogger {

        public DevNullLogger() {
            super(null);
        }

        @Override
        public boolean isEnabled(int level) {
            return false;
        }

        @Override
        public void log(int level, String message) {
        }
    }

    private static final class SftpUserInfo implements UserInfo, UIKeyboardInteractive {

        private static final Logger LOGGER = Logger.getLogger(SftpUserInfo.class.getName());

        private final SftpConfiguration configuration;
        private volatile String passwd;
        private volatile String passphrase;

        public SftpUserInfo(SftpConfiguration configuration) {
            assert configuration != null;

            this.configuration = configuration;
        }

        @Override
        public boolean promptYesNo(String message) {
            NotifyDescriptor descriptor = new NotifyDescriptor(
                    message,
                    NbBundle.getMessage(SftpClient.class, "LBL_Warning"),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION},
                    NotifyDescriptor.YES_OPTION);
            return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public boolean promptPassphrase(String message) {
            passphrase = getPasswordForCertificate(configuration);
            return passphrase != null;
        }

        @Override
        public String getPassword() {
            return passwd;
        }

        @Override
        public boolean promptPassword(String message) {
            passwd = getPasswordForUser(configuration);
            return passwd != null;
        }

        @Override
        public void showMessage(String message) {
            showMessageForConfiguration(configuration, message);
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction,
                String[] prompt, boolean[] echo) {

            // diagnostics for #230248
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("promptKeyboardInteractive called with these params:");
                LOGGER.log(Level.FINE, "destination: {0}", destination);
                LOGGER.log(Level.FINE, "name: {0}", name);
                LOGGER.log(Level.FINE, "instruction: {0}", instruction);
                LOGGER.log(Level.FINE, "prompt: {0}", Arrays.toString(prompt));
                LOGGER.log(Level.FINE, "echo: {0}", Arrays.toString(echo));
            }

            // #166555
            if (prompt.length == 1
                    && echo.length == 1 && !echo[0]) {
                // ask for password
                passwd = configuration.getPassword();
                if (!StringUtils.hasText(passwd)) {
                    passwd = getPasswordForUser(configuration);
                }
                if (StringUtils.hasText(passwd)) {
                    return new String[] {passwd};
                }
            }
            return null;
        }
    }

}
