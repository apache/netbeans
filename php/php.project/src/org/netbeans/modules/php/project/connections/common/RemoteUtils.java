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

import java.net.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NetworkSettings;

/**
 * Utility methods for remote connections.
 */
public final class RemoteUtils {

    private static final Logger LOGGER = Logger.getLogger(RemoteUtils.class.getName());

    private static final boolean FETCH_ALL_LOCAL_FILES = Boolean.getBoolean("nb.php.remote.localFiles.eager"); // NOI18N
    private static final boolean FETCH_ALL_REMOTE_FILES = Boolean.getBoolean("nb.php.remote.remoteFiles.eager"); // NOI18N
    private static final boolean FETCH_ALL_FILES = Boolean.getBoolean("nb.php.remote.files.eager"); // NOI18N


    private RemoteUtils() {
    }

    public static JComboBox.KeySelectionManager createRemoteConfigurationKeySelectionManager() {
        return new RemoteConfigurationKeySelectionManager();
    }

    public static boolean allFilesFetched(boolean remote) {
        if (FETCH_ALL_FILES) {
            return true;
        }
        return remote ? FETCH_ALL_REMOTE_FILES : FETCH_ALL_LOCAL_FILES;
    }

    public static long getLastTimestamp(boolean upload, Project project) {
        if (!allFilesFetched(!upload)) {
            // using lazy children, return far future
            return 4102441200L; // 2100/1/1 ;)
        }
        return upload ? ProjectSettings.getLastUpload(project) : ProjectSettings.getLastDownload(project);
    }

    // #237253
    /**
     * Add all children recursively to the given transfer files if:
     * <ul>
     * <li>proper system property is set,</li>
     * <li>preselected files contain only sources.</li>
     * </ul>
     * @param remote {@code true} for remote files, {@code false} for local ones
     * @param files files to be fetched
     * @param sources source directory
     * @param preselectedFiles preselected files
     */
    public static void fetchAllFiles(boolean remote, Set<TransferFile> files, FileObject sources, FileObject[] preselectedFiles) {
        if (!allFilesFetched(remote)) {
            return;
        }
        if (preselectedFiles.length != 1) {
            // some files selected for upload
            return;
        }
        if (!preselectedFiles[0].equals(sources)) {
            // not source dir
            return;
        }
        Set<TransferFile> tmp = new HashSet<>();
        for (TransferFile transferFile : files) {
            fetchAllFiles(remote, tmp, transferFile);
        }
        files.clear();
        files.addAll(tmp);
    }

    private static void fetchAllFiles(boolean remote, Set<TransferFile> allFiles, TransferFile transferFile) {
        allFiles.add(transferFile);
        List<TransferFile> children = remote ? transferFile.getRemoteChildren() : transferFile.getLocalChildren();
        for (TransferFile child : children) {
            fetchAllFiles(remote, allFiles, child);
        }
    }

    /**
     * Display remote exception in a dialog window to inform user about error
     * on a remote server.
     * @param remoteException remote exception to be displayed
     */
    @NbBundle.Messages({
        "LBL_RemoteError=Remote Error",
        "# {0} - reason of the failure",
        "MSG_RemoteErrorReason=\n\nReason: {0}"
    })
    public static void processRemoteException(RemoteException remoteException) {
        String title = Bundle.LBL_RemoteError();
        StringBuilder message = new StringBuilder(remoteException.getMessage());
        String remoteServerAnswer = remoteException.getRemoteServerAnswer();
        Throwable cause = remoteException.getCause();
        if (remoteServerAnswer != null && remoteServerAnswer.length() > 0) {
            message.append(Bundle.MSG_RemoteErrorReason(remoteServerAnswer));
        } else if (cause != null) {
            message.append(Bundle.MSG_RemoteErrorReason(cause.getMessage()));
        }
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                message.toString(),
                title,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notifyLater(notifyDescriptor);
    }

    /**
     * Remote trailing {@value TransferFile#REMOTE_PATH_SEPARATOR} from the given
     * directory path.
     * <p>
     * If the path is <i>root</i> (it equals just {@value TransferFile#REMOTE_PATH_SEPARATOR}),
     * no sanitation is done.
     * @param directoryPath directory to be sanitized
     * @return sanitized directory path
     */
    public static String sanitizeDirectoryPath(String directoryPath) {
        while (!directoryPath.equals(TransferFile.REMOTE_PATH_SEPARATOR)
                && directoryPath.endsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
            LOGGER.log(Level.FINE, "Removing ending slash from directory {0}", directoryPath);
            directoryPath = directoryPath.substring(0, directoryPath.length() - TransferFile.REMOTE_PATH_SEPARATOR.length());
        }
        return directoryPath;
    }

    /**
     * Sanitize upload directory, see issue #169793 for more information.
     * @param uploadDirectory upload directory to sanitize
     * @param allowEmpty <code>true</code> if the string can be empty
     * @return sanitized upload directory
     */
    public static String sanitizeUploadDirectory(String uploadDirectory, boolean allowEmpty) {
        if (StringUtils.hasText(uploadDirectory)) {
            while (uploadDirectory.length() > 1
                    && uploadDirectory.endsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
                uploadDirectory = uploadDirectory.substring(0, uploadDirectory.length() - 1);
            }
        } else if (!allowEmpty) {
            uploadDirectory = TransferFile.REMOTE_PATH_SEPARATOR;
        }
        if (allowEmpty
                && (uploadDirectory == null || TransferFile.REMOTE_PATH_SEPARATOR.equals(uploadDirectory))) {
            uploadDirectory = ""; // NOI18N
        }
        return uploadDirectory;
    }

    /**
     * Get parent path for the given path.
     * @param path file path
     * @return parent path or "/" for absolute top-level path
     * or {@code null} if parent path does not exist
     */
    public static String getParentPath(String path) {
        if (path.equals(TransferFile.REMOTE_PATH_SEPARATOR)) {
            return null;
        }
        boolean absolute = path.startsWith(TransferFile.REMOTE_PATH_SEPARATOR);
        if (absolute) {
            path = path.substring(1);
        }
        String parent;
        List<String> parts = new ArrayList<>(StringUtils.explode(path, TransferFile.REMOTE_PATH_SEPARATOR));
        if (parts.size() <= 1) {
            return absolute ? TransferFile.REMOTE_PATH_SEPARATOR : null;
        }
        parts.remove(parts.size() - 1);
        parent = StringUtils.implode(parts, TransferFile.REMOTE_PATH_SEPARATOR);
        if (absolute) {
            return TransferFile.REMOTE_PATH_SEPARATOR + parent;
        }
        return parent;
    }

    /**
     * Get name of the file for the given path.
     * @param path file path
     * @return name of the file for the given path
     */
    public static String getName(String path) {
        if (path.equals(TransferFile.REMOTE_PATH_SEPARATOR)) {
            return TransferFile.REMOTE_PATH_SEPARATOR;
        }
        List<String> parts = new ArrayList<>(StringUtils.explode(path, TransferFile.REMOTE_PATH_SEPARATOR));
        return parts.get(parts.size() - 1);
    }

    public static boolean hasHttpProxy(String host) {
        return getHttpProxy(host) != null;
    }

    @CheckForNull
    public static ProxyInfo getHttpProxy(String host) {
        return getProxy(Proxy.Type.HTTP, host);
    }

    @CheckForNull
    public static ProxyInfo getSocksProxy(String host) {
        return getProxy(Proxy.Type.SOCKS, host);
    }

    // #226006 - avoid nb apis, does not work correctly
    private static ProxyInfo getProxy(Proxy.Type type, String host) {
        assert type != null;
        assert type != Proxy.Type.DIRECT;
        URI uri;
        switch (type) {
            case HTTP:
                uri = URI.create("http://" + sanitizeHost(host)); // NOI18N
                break;
            case SOCKS:
                uri = URI.create("socks://" + sanitizeHost(host)); // NOI18N
                break;
            default:
                throw new IllegalStateException("Unexpected proxy type: " + type);
        }
        String proxyHost = NetworkSettings.getProxyHost(uri);
        if (proxyHost == null) {
            return null;
        }
        return new ProxyInfo(type, proxyHost, Integer.parseInt(NetworkSettings.getProxyPort(uri)),
                NetworkSettings.getAuthenticationUsername(uri), NetworkSettings.getKeyForAuthenticationPassword(uri));
    }

    private static String sanitizeHost(String host) {
        if (StringUtils.hasText(host)) {
            return host;
        }
        return "oracle.com"; // NOI18N
    }

    //~ Inner classes

    public static final class ProxyInfo {

        private final Proxy.Type type;
        private final String host;
        private final int port;
        private final String username;
        private final String passwordKey;


        public ProxyInfo(Proxy.Type type, String host, int port, String username, String passwordKey) {
            assert type != null;
            assert host != null;

            this.type = type;
            this.host = host;
            this.port = port;
            this.username = username;
            this.passwordKey = passwordKey;
        }

        public Proxy.Type getType() {
            return type;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            if (passwordKey == null) {
                return null;
            }
            char[] chars = Keyring.read(passwordKey);
            if (chars == null) {
                return null;
            }
            return new String(chars);
        }

        @Override
        public String toString() {
            return "ProxyInfo{" + "type=" + type + ", host=" + host + ", port=" + port + ", username=" + username + ", passwordKey=" + passwordKey + '}'; // NOI18N
        }

    }

    private static final class RemoteConfigurationKeySelectionManager implements JComboBox.KeySelectionManager {

        @Override
        public int selectionForKey(char key, ComboBoxModel model) {
            char firstChar = ("" + key).toLowerCase().charAt(0); // NOI18N
            for (int i = 0; i < model.getSize(); ++i) {
                RemoteConfiguration configuration = (RemoteConfiguration) model.getElementAt(i);
                if (configuration.getDisplayName().charAt(0) == firstChar) {
                    return i;
                }
            }
            return -1;
        }

    }


}
