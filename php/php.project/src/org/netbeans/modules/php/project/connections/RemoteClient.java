/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.php.project.connections;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.spi.RemoteConnectionProvider;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.netbeans.modules.php.project.connections.transfer.LocalTransferFile;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.transfer.TransferInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.windows.InputOutput;

/**
 * Remote client able to connect/disconnect to a remote server
 * as well as download/upload files from/to a remote server.
 * <p>
 * Every method throws {@link RemoteException} if any error occurs.
 * <p>
 * This class is thread-safe.
 * @author Tomas Mysik
 */
public final class RemoteClient implements Cancellable {
    private static final Logger LOGGER = Logger.getLogger(RemoteClient.class.getName());

    public static final AtomicAction DOWNLOAD_ATOMIC_ACTION = new DownloadAtomicAction(null);

    private static final AdvancedProperties DEFAULT_ADVANCED_PROPERTIES = new AdvancedProperties();
    private static final OperationMonitor DEV_NULL_OPERATION_MONITOR = new DevNullOperationMonitor();
    private static final Set<String> IGNORED_DIRS = new HashSet<>(Arrays.asList(".", "..", "nbproject")); // NOI18N
    private static final int TRIES_TO_TRANSFER = 3; // number of tries if file download/upload fails
    private static final String REMOTE_TMP_NEW_SUFFIX = ".new"; // NOI18N
    private static final String REMOTE_TMP_OLD_SUFFIX = ".old"; // NOI18N
    private static final int MAX_FILE_SIZE_FOR_MEMORY = 500 * 1024; // 500 kB


    public static enum Operation { UPLOAD, DOWNLOAD, DELETE, LIST };

    private final RemoteConfiguration configuration;
    private final AdvancedProperties properties;
    // @GuardedBy(this) to avoid over-complicated code, can be improved
    private final org.netbeans.modules.php.project.connections.spi.RemoteClient remoteClient;

    private volatile String baseRemoteDirectory;
    private volatile boolean cancelled = false;
    private volatile OperationMonitor operationMonitor;

    /**
     * @see RemoteClient#RemoteClient(org.netbeans.modules.php.project.connections.spi.RemoteConfiguration, org.openide.windows.InputOutput, java.lang.String, boolean)
     */
    public RemoteClient(RemoteConfiguration configuration) {
        this(configuration, DEFAULT_ADVANCED_PROPERTIES);
    }

    /**
     * Create a new remote client.
     * @param configuration {@link RemoteConfiguration remote configuration} of a connection.
     * @param properties advanced properties of a connection.
     */
    public RemoteClient(RemoteConfiguration configuration, AdvancedProperties properties) {
        assert configuration != null;
        assert properties != null;

        this.configuration = configuration;
        this.properties = properties;

        setOperationMonitor(properties.getOperationMonitor());

        // base remote directory
        StringBuilder baseDirBuffer = new StringBuilder(configuration.getInitialDirectory());
        String additionalInitialSubdirectory = properties.getAdditionalInitialSubdirectory();
        if (StringUtils.hasText(additionalInitialSubdirectory)) {
            if (!additionalInitialSubdirectory.startsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
                throw new IllegalArgumentException("additionalInitialSubdirectory must start with " + TransferFile.REMOTE_PATH_SEPARATOR);
            }
            baseDirBuffer.append(additionalInitialSubdirectory);
        }
        String baseDir = baseDirBuffer.toString();
        // #150646 - should not happen now, likely older nb project metadata
        if (baseDir.length() > 1
                && baseDir.endsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
            baseDir = baseDir.substring(0, baseDir.length() - 1);
        }

        baseRemoteDirectory = baseDir.replaceAll(TransferFile.REMOTE_PATH_SEPARATOR + "{2,}", TransferFile.REMOTE_PATH_SEPARATOR); // NOI18N

        assert baseRemoteDirectory.startsWith(TransferFile.REMOTE_PATH_SEPARATOR) : "base directory must start with " + TransferFile.REMOTE_PATH_SEPARATOR + ": " + baseRemoteDirectory;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Remote client created with configuration: %s, advanced properties: %s, base remote directory: %s",
                    configuration, properties, baseRemoteDirectory));
        }


        // remote client itself
        org.netbeans.modules.php.project.connections.spi.RemoteClient client = null;
        for (RemoteConnectionProvider provider : RemoteConnections.get().getConnectionProviders()) {
            client = provider.getRemoteClient(configuration, properties.getInputOutput());
            if (client != null) {
                break;
            }
        }
        assert client != null : "no suitable remote client for configuration: " + configuration;
        this.remoteClient = client;
    }

    String getBaseRemoteDirectory() {
        return baseRemoteDirectory;
    }

    public OperationMonitor getOperationMonitor() {
        if (operationMonitor == null) {
            return DEV_NULL_OPERATION_MONITOR;
        }
        return operationMonitor;
    }

    public void setOperationMonitor(OperationMonitor operationMonitor) {
        this.operationMonitor = operationMonitor;
    }

    public synchronized void connect() throws RemoteException {
        remoteClient.connect();
        assert remoteClient.isConnected() : "Remote client should be connected";

        // cd to base remote directory
        if (!cdBaseRemoteDirectory()) {
            if (remoteClient.isConnected()) {
                disconnect(true);
            }
            throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", baseRemoteDirectory), remoteClient.getReplyString());
        }
        // #204680 - symlinks on remote server
        String pwd = getWorkingDirectory();
        if (!pwd.equals(baseRemoteDirectory)) {
            LOGGER.log(Level.FINE, "Changing base remote directory (symlink?): {0} -> {1}", new Object[] {baseRemoteDirectory, pwd});
            baseRemoteDirectory = pwd;
        }
    }

    public synchronized void disconnect(boolean force) throws RemoteException {
        remoteClient.disconnect(force);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public void reset() {
        cancelled = false;
    }

    public synchronized boolean exists(TransferFile file) throws RemoteException {
        ensureConnected();

        LOGGER.fine(String.format("Checking whether file %s exists", file));
        cdBaseRemoteDirectory();
        boolean exists = remoteClient.exists(file.getParentRemotePath(), file.getName());
        LOGGER.fine(String.format("Exists: %b", exists));
        return exists;
    }

    public synchronized boolean rename(TransferFile from, TransferFile to) throws RemoteException {
        ensureConnected();

        LOGGER.fine(String.format("Moving file from %s to %s", from, to));
        cdBaseRemoteDirectory();
        boolean success = remoteClient.rename(from.getRemotePath(), to.getRemotePath());
        LOGGER.fine(String.format("Success: %b", success));
        return success;
    }

    public List<TransferFile> listFiles(TransferFile file) throws RemoteException {
        ensureConnected();

        LOGGER.log(Level.FINE, "Getting children for {0}", file);
        try {
            getOperationMonitor().operationStart(Operation.LIST, Collections.singleton(file));
            List<RemoteFile> remoteFiles = Collections.emptyList();
            synchronized (this) {
                if (cdBaseRemoteDirectory(file.getRemotePath(), false)) {
                    remoteFiles = remoteClient.listFiles();
                }
            }
            if (remoteFiles.isEmpty()) {
                LOGGER.log(Level.FINE, "No children found for {0}", file);
                return Collections.emptyList();
            }
            RemoteClientImplementation remoteClientImplementation = createRemoteClientImplementation(file.getBaseLocalDirectoryPath());
            List<TransferFile> transferFiles = new ArrayList<>(remoteFiles.size());
            for (RemoteFile remoteFile : remoteFiles) {
                if (isVisible(getLocalFile(new File(file.getBaseLocalDirectoryPath()), file, remoteFile))) {
                    LOGGER.log(Level.FINE, "File {0} added to download queue", remoteFile);
                    TransferFile transferFile = TransferFile.fromRemoteFile(remoteClientImplementation, file, remoteFile);
                    getOperationMonitor().operationProcess(Operation.LIST, transferFile);
                    transferFiles.add(transferFile);
                } else {
                    LOGGER.log(Level.FINE, "File {0} NOT added to download queue [invisible]", remoteFile);
                }
            }
            LOGGER.log(Level.FINE, "{0} children found for {1}", new Object[] {transferFiles.size(), file});
            return transferFiles;
        } finally {
            getOperationMonitor().operationFinish(Operation.LIST, Collections.singleton(file));
        }
    }

    public Set<TransferFile> prepareUpload(FileObject baseLocalDirectory, FileObject... filesToUpload) {
        assert baseLocalDirectory != null;
        assert filesToUpload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToUpload.length > 0 : "At least one file to upload must be specified";

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        baseLocalDir = FileUtil.normalizeFile(baseLocalDir);
        String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
        RemoteClientImplementation remoteClientImpl = createRemoteClientImplementation(baseLocalAbsolutePath);
        List<TransferFile> baseFiles = new LinkedList<>();
        for (FileObject fo : filesToUpload) {
            File f = FileUtil.toFile(fo);
            if (f == null) {
                // ???
                continue;
            }
            if (isVisible(f)) {
                LOGGER.log(Level.FINE, "File {0} added to upload queue", fo);
                baseFiles.add(TransferFile.fromFileObject(remoteClientImpl, null, fo));
            } else {
                LOGGER.log(Level.FINE, "File {0} NOT added to upload queue [invisible]", fo);
            }
        }

        Set<TransferFile> files = new HashSet<>();
        for (TransferFile file : baseFiles) {
            if (cancelled) {
                LOGGER.fine("Prepare upload cancelled");
                break;
            }

            if (!files.add(file)) {
                LOGGER.log(Level.FINE, "File {0} already in queue", file);

                // file already in set => remove the file from set and add this one (the previous can be root but apparently it is not)
                files.remove(file);
                files.add(file);
            }

            if (file.isDirectory()) {
                files.addAll(file.getLocalChildren());
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Prepared for upload: {0}", files);
        }
        return files;
    }

    public TransferInfo upload(Set<TransferFile> filesToUpload) throws RemoteException {
        assert filesToUpload != null;
        assert filesToUpload.size() > 0 : "At least one file to upload must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        TransferInfo transferInfo = new TransferInfo();


        // XXX order filesToUpload?
        try {
            getOperationMonitor().operationStart(Operation.UPLOAD, filesToUpload);
            for (TransferFile file : filesToUpload) {
                uploadFile(transferInfo, file);
            }
        } finally {
            getOperationMonitor().operationFinish(Operation.UPLOAD, filesToUpload);
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void uploadFile(TransferInfo transferInfo, TransferFile file) throws RemoteException {
        if (cancelled) {
            LOGGER.fine("Upload cancelled");
            return;
        }
        getOperationMonitor().operationProcess(Operation.UPLOAD, file);
        try {
            uploadFileInternal(transferInfo, file);
        } catch (IOException | RemoteException exc) {
            LOGGER.log(Level.INFO, null, exc);
            transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
        }
    }

    private void uploadFileInternal(TransferInfo transferInfo, TransferFile file) throws IOException, RemoteException {
        // xxx upload cannot check symlinks because project files of /path/<symlink>/my/project would not be uploaded at all!
//        if (file.isLink()) {
//            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_Symlink", file.getRemotePath()));
//        } else if (isParentLink(file)) {
//            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ParentSymlink", file.getRemotePath()));
        if (file.isDirectory()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Uploading directory: {0}", file);
            }
            // in fact, useless but probably expected
            cdBaseRemoteDirectory(file.getRemotePath(), true);
            transferSucceeded(transferInfo, file);
            if (!file.hasLocalChildrenFetched()) {
                // upload all children as well
                List<TransferFile> localChildren = file.getLocalChildren();
                // update monitor
                getOperationMonitor().addUnits(localChildren);
                // upload files
                for (TransferFile child : localChildren) {
                    uploadFile(transferInfo, child);
                }
            }
        } else if (file.isFile()) {
            // file => simply upload it

            assert file.getParentRemotePath() != null : "Must be underneath base remote directory! [" + file + "]";
            if (!cdBaseRemoteDirectory(file.getParentRemotePath(), true)) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", file.getParentRemotePath()));
                return;
            }

            String fileName = file.getName();

            int oldPermissions = -1;
            if (properties.isPreservePermissions()) {
                synchronized (this) {
                    oldPermissions = remoteClient.getPermissions(fileName);
                }
                LOGGER.fine(String.format("Original permissions of %s: %d", fileName, oldPermissions));
            } else {
                LOGGER.fine("Permissions are not preserved.");
            }

            String tmpFileName = null;
            if (properties.isUploadDirectly()) {
                LOGGER.fine("File will be uploaded directly.");
                tmpFileName = fileName;
            } else {
                tmpFileName = fileName + REMOTE_TMP_NEW_SUFFIX;
                LOGGER.fine("File will be uploaded using a temporary file.");
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                synchronized (this) {
                    LOGGER.log(Level.FINE, "Uploading file {0} => {1}", new Object[] {fileName, getWorkingDirectory() + TransferFile.REMOTE_PATH_SEPARATOR + tmpFileName});
                }
            }
            // XXX lock the file?
            boolean success = false;
            try (InputStream is = new BufferedInputStream(new FileInputStream(new File(new File(file.getBaseLocalDirectoryPath()), file.getLocalPath())))) {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    boolean fileStored;
                    synchronized (this) {
                        fileStored = remoteClient.storeFile(tmpFileName, is);
                    }
                    if (fileStored) {
                        success = true;
                        if (LOGGER.isLoggable(Level.FINE)) {
                            String f = file.getRemotePath() + (properties.isUploadDirectly() ? "" : REMOTE_TMP_NEW_SUFFIX);
                            LOGGER.fine(String.format("The %d. attempt to upload '%s' was successful", i, f));
                        }
                        break;
                    } else if (LOGGER.isLoggable(Level.FINE)) {
                        String f = file.getRemotePath() + (properties.isUploadDirectly() ? "" : REMOTE_TMP_NEW_SUFFIX);
                        LOGGER.fine(String.format("The %d. attempt to upload '%s' was NOT successful", i, f));
                    }
                }
            } finally {
                if (success) {
                    if (!properties.isUploadDirectly()) {
                        success = moveRemoteFile(tmpFileName, fileName);
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("File %s renamed to %s: %s", tmpFileName, fileName, success));
                        }
                    }

                    if (properties.isPreservePermissions() && success && oldPermissions != -1) {
                        int newPermissions;
                        synchronized (this) {
                            newPermissions = remoteClient.getPermissions(fileName);
                        }
                        LOGGER.fine(String.format("New permissions of %s: %d", fileName, newPermissions));
                        if (oldPermissions != newPermissions) {
                            LOGGER.fine(String.format("Setting permissions %d for %s.", oldPermissions, fileName));
                            boolean permissionsSet;
                            synchronized (this) {
                                permissionsSet = remoteClient.setPermissions(oldPermissions, fileName);
                            }
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine(String.format("Permissions for %s set: %s", fileName, permissionsSet));
                                synchronized (this) {
                                    LOGGER.fine(String.format("Permissions for %s read: %s", fileName, remoteClient.getPermissions(fileName)));
                                }
                            }
                            if (!permissionsSet) {
                                transferPartiallyFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_PermissionsNotSet", oldPermissions, file.getName()));
                            }
                        }
                    }
                }
                if (success) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getOperationFailureMessage(Operation.UPLOAD, fileName));
                    if (!properties.isUploadDirectly()) {
                        // delete tmp file if it has been uploaded
                        boolean deleted;
                        synchronized (this) {
                            deleted = remoteClient.deleteFile(tmpFileName);
                        }
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("Unsuccessfully uploaded file %s deleted: %s", file.getRemotePath() + REMOTE_TMP_NEW_SUFFIX, deleted));
                        }
                    }
                }
            }
        } else {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_UnknownFileType", file.getRemotePath()));
        }
    }

    private synchronized boolean moveRemoteFile(String source, String target) throws RemoteException {
        boolean moved = remoteClient.rename(source, target);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("File %s directly renamed to %s: %s", source, target, moved));
        }
        if (moved) {
            return true;
        }
        // possible cleanup
        String oldPath = target + REMOTE_TMP_OLD_SUFFIX;
        remoteClient.deleteFile(oldPath);

        // try to move the old file, move the new file, delete the old file
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Renaming in chain: (1) <file> -> <file>.old~ ; (2) <file>.new~ -> <file> ; (3) rm <file>.old~");
        }
        moved = remoteClient.rename(target, oldPath);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(1) File %s renamed to %s: %s", target, oldPath, moved));
        }
        if (!moved) {
            return false;
        }
        moved = remoteClient.rename(source, target);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(2) File %s renamed to %s: %s", source, target, moved));
        }
        if (!moved) {
            // try to restore the original file
            boolean restored = remoteClient.rename(oldPath, target);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("(-) File %s restored to original %s: %s", oldPath, target, restored));
            }
        } else {
            boolean deleted = remoteClient.deleteFile(oldPath);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("(3) File %s deleted: %s", oldPath, deleted));
            }
        }
        return moved;
    }

    public synchronized TransferFile listFile(FileObject baseLocalDir, FileObject file) throws RemoteException {
        ensureConnected();

        String baseLocalAbsolutePath = FileUtil.toFile(baseLocalDir).getAbsolutePath();
        TransferFile localTransferFile = TransferFile.fromFile(createRemoteClientImplementation(baseLocalAbsolutePath), null, FileUtil.toFile(file));
        RemoteFile remoteFile = remoteClient.listFile(localTransferFile.getRemoteAbsolutePath());
        if (remoteFile != null) {
            // remote file found
            LOGGER.log(Level.FINE, "Remote file {0} found", localTransferFile.getRemotePath());
            return TransferFile.fromRemoteFile(createRemoteClientImplementation(localTransferFile.getBaseLocalDirectoryPath()), localTransferFile.hasParent() ? localTransferFile.getParent() : null, remoteFile);
        }
        // remote file not found
        LOGGER.log(Level.FINE, "Remote file {0} not found", localTransferFile.getRemotePath());
        return null;
    }

    public Set<TransferFile> prepareDownload(FileObject baseLocalDirectory, FileObject... filesToDownload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToDownload != null;
        assert filesToDownload.length > 0 : "At least one file to download must be specified";

        List<File> files = new ArrayList<>(filesToDownload.length);
        for (FileObject fo : filesToDownload) {
            File f = FileUtil.toFile(fo);
            if (f != null) {
                files.add(f);
            }
        }
        return prepareDownload(FileUtil.toFile(baseLocalDirectory), files.toArray(new File[0]));
    }

    public Set<TransferFile> prepareDownload(File baseLocalDir, File... filesToDownload) throws RemoteException {
        assert baseLocalDir != null;
        assert filesToDownload != null;
        assert filesToDownload.length > 0 : "At least one file to download must be specified";

        ensureConnected();

        String baseLocalAbsolutePath = FileUtil.normalizeFile(baseLocalDir).getAbsolutePath();
        RemoteClientImplementation remoteClientImpl = createRemoteClientImplementation(baseLocalAbsolutePath);
        List<TransferFile> baseFiles = new LinkedList<>();
        for (File f : filesToDownload) {
            f = FileUtil.normalizeFile(f);
            if (isVisible(f)) {
                LOGGER.log(Level.FINE, "File {0} added to download queue", f);
                TransferFile tf;
                if (f.exists()) {
                    tf = TransferFile.fromFile(remoteClientImpl, null, f);
                } else {
                    // assume folder for non-existing file => recursive fetch
                    tf = TransferFile.fromDirectory(remoteClientImpl, null, f);
                }
                baseFiles.add(tf);
            } else {
                LOGGER.log(Level.FINE, "File {0} NOT added to download queue [invisible]", f);
            }
        }

        Set<TransferFile> files = new HashSet<>();
        for (TransferFile file : baseFiles) {
            if (cancelled) {
                LOGGER.fine("Prepare download cancelled");
                break;
            }

            if (!files.add(file)) {
                LOGGER.log(Level.FINE, "File {0} already in queue", file);

                // file already in set => remove the file from set and add this one (the previous can be root but apparently it is not)
                files.remove(file);
                files.add(file);
            }

            if (file.isDirectory()) {
                files.addAll(file.getRemoteChildren());
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Prepared for download: {0}", files);
        }
        return files;
    }

    public TransferInfo download(Set<TransferFile> filesToDownload) throws RemoteException {
        assert filesToDownload != null;
        assert filesToDownload.size() > 0 : "At least one file to download must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        final TransferInfo transferInfo = new TransferInfo();

        // XXX order filesToDownload?
        try {
            getOperationMonitor().operationStart(Operation.DOWNLOAD, filesToDownload);
            for (final TransferFile file : filesToDownload) {
                downloadFile(transferInfo, file);
            }
        } finally {
            getOperationMonitor().operationFinish(Operation.DOWNLOAD, filesToDownload);
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void downloadFile(final TransferInfo transferInfo, final TransferFile file) {
        if (cancelled) {
            LOGGER.fine("Download cancelled");
            return;
        }

        getOperationMonitor().operationProcess(Operation.DOWNLOAD, file);
        try {
            FileUtil.runAtomicAction(new DownloadAtomicAction(new Runnable() {
                @Override
                public void run() {
                    try {
                        downloadFileInternal(transferInfo, file);
                    } catch (IOException | RemoteException exc) {
                        LOGGER.log(Level.INFO, null, exc);
                        transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                    }
                }
            }));
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", ex.getMessage().trim()));
        }
    }

    @NbBundle.Messages("RemoteClient.download.ignored.byUser=Download ignored by user.")
    private void downloadFileInternal(TransferInfo transferInfo, TransferFile file) throws IOException, RemoteException {
        File localFile = getLocalFile(new File(file.getBaseLocalDirectoryPath()), file);
        if (file.isLink()) {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_Symlink", file.getRemotePath()));
        } else if (isParentLink(file)) {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ParentSymlink", file.getRemotePath()));
        } else if (file.isDirectory()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Downloading directory: {0}", file);
            }
            if (!cdBaseRemoteDirectory(file.getRemotePath(), false)) {
                LOGGER.log(Level.FINE, "Remote directory {0} does not exist => ignoring", file.getRemotePath());
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", file.getRemotePath()));
                return;
            }
            // in fact, useless but probably expected
            if (!localFile.exists()) {
                if (!mkLocalDirs(localFile)) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDir", localFile));
                    return;
                }
            } else if (localFile.isFile()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_DirFileCollision", file));
                return;
            }
            transferSucceeded(transferInfo, file);
            if (!file.hasRemoteChildrenFetched()) {
                // download all children as well
                for (TransferFile child : file.getRemoteChildren()) {
                    downloadFile(transferInfo, child);
                }
            }
        } else if (file.isFile()) {
            // file => simply download it

            // #142682 - because from the ui we get only files (folders are removed) => ensure parent folder exists
            File parent = localFile.getParentFile();
            assert parent != null : "File " + localFile + " has no parent file?!";
            if (!parent.exists()) {
                if (!mkLocalDirs(parent)) {
                    transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDir", parent));
                    return;
                }
            } else if (parent.isFile()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_DirFileCollision", file));
                return;
            } else if (localFile.exists() && !localFile.canWrite()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FileNotWritable", localFile));
                return;
            }
            assert parent.isDirectory() : "Parent file of " + localFile + " must be a directory";

            TmpLocalFile tmpLocalFile = createTmpLocalFile(file);
            if (tmpLocalFile == null) {
                // definitely should not happen
                LOGGER.log(Level.INFO, "Local temporary file could not be created for {0}", file.getRemotePath());
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateTmpLocalFile", file.getRemotePath()));
                return;
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Downloading {0} => {1}", new Object[] {file.getRemotePath(), tmpLocalFile.isInMemory() ? "memory" : "tmp local file on disk"});
            }

            if (!cdBaseRemoteDirectory(file.getParentRemotePath(), false)) {
                LOGGER.log(Level.FINE, "Remote directory {0} does not exist => ignoring file {1}", new Object[] {file.getParentRemotePath(), file.getRemotePath()});
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", file.getParentRemotePath()));
                return;
            }

            boolean success = false;
            OutputStream os = tmpLocalFile.getOutputStream();
            if (os == null) {
                // definitely should not happen
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotOpenTmpLocalFile", tmpLocalFile));
                return;
            }
            try {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    boolean fileRetrieved;
                    synchronized (this) {
                        fileRetrieved = remoteClient.retrieveFile(file.getName(), os);
                    }
                    if (fileRetrieved) {
                        success = true;
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("The %d. attempt to download '%s' was successful", i, file.getRemotePath()));
                        }
                        break;
                    } else if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("The %d. attempt to download '%s' was NOT successful", i, file.getRemotePath()));
                    }
                }
            } finally {
                os.close();
                try {
                    if (success) {
                        // move the file
                        success = copyTmpLocalFile(tmpLocalFile, localFile);
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("File %s copied to %s: %s", tmpLocalFile, localFile, success));
                        }
                    }
                    if (success) {
                        transferSucceeded(transferInfo, file);
                    } else {
                        transferFailed(transferInfo, file, getOperationFailureMessage(Operation.DOWNLOAD, file.getName()));
                    }
                } catch (DownloadSkipException ex) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s ignored by user (unsaved changes in the local file)", localFile));
                    }
                    transferIgnored(transferInfo, file, Bundle.RemoteClient_download_ignored_byUser());
                } finally {
                    LOGGER.fine("Tmp local file cleanup");
                    tmpLocalFile.cleanup();
                }
            }
        } else {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_UnknownFileType", file.getRemotePath()));
        }
    }

    /**
     * Download {@link TransferFile remote file} to a given {@link TmpLocalFile temporary file}.
     * <p>
     * @param tmpFile temporary file to be used for download
     * @param file remote file to be downloaded
     * @return {@code true} if successful, {@code false} otherwise
     * @throws RemoteException if any remote error occurs (not a file, file not found, cannot be read etc.)
     */
    @NbBundle.Messages({
        "RemoteClient.error.notFile=Given remote file is not a file.",
        "# {0} - file name",
        "RemoteClient.error.cannotOpenTmpLocalFile=Cannot open a local temporary file {0}."
    })
    public boolean downloadTemporary(TmpLocalFile tmpFile, TransferFile file) throws RemoteException {
        if (!file.isFile()) {
            throw new RemoteException(Bundle.RemoteClient_error_notFile());
        }
        if (!cdBaseRemoteDirectory(file.getParentRemotePath(), false)) {
            LOGGER.log(Level.FINE, "Remote directory {0} does not exist => cannot download file {1}", new Object[] {file.getParentRemotePath(), file.getRemotePath()});
            return false;
        }

        boolean success = false;
        OutputStream os = tmpFile.getOutputStream();
        if (os == null) {
            // definitely should not happen
            throw new RemoteException(Bundle.RemoteClient_error_cannotOpenTmpLocalFile(tmpFile));
        }
        try {
            for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                boolean fileRetrieved;
                synchronized (this) {
                    fileRetrieved = remoteClient.retrieveFile(file.getName(), os);
                }
                if (fileRetrieved) {
                    success = true;
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("The %d. attempt to download '%s' was successful", i, file.getRemotePath()));
                    }
                    break;
                } else if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("The %d. attempt to download '%s' was NOT successful", i, file.getRemotePath()));
                }
            }
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                // can be safely ignored, in fact, cannot happen
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        return success;
    }

    private TmpLocalFile createTmpLocalFile(TransferFile file) {
        final long size = file.getSize();
        if (size <= MAX_FILE_SIZE_FOR_MEMORY
                && !(file instanceof LocalTransferFile)) { // #258947
            // #258947: happens for the locally _selected_ files (their transfer files are "backed" by local files, not remote files)
            return TmpLocalFile.inMemory((int) size);
        }
        return TmpLocalFile.onDisk();
    }

    @NbBundle.Messages({
        "RemoteClient.file.replaceUnsavedContent.title=Confirm replacement",
        "# {0} - file name",
        "RemoteClient.file.replaceUnsavedContent.question=Replace unsaved file \"{0}\" with the content from the server?"
    })
    private boolean copyTmpLocalFile(final TmpLocalFile source, final File target) throws DownloadSkipException {
        boolean moved = false;
        boolean downloadSkipped = false;
        FileObject foTarget = ensureTargetExists(FileUtil.normalizeFile(target));
        if (foTarget == null) {
            return false;
        }

        // replace content of the target file with the source file
        FileLock lock;
        try {
            // lock the target file
            lock = lockFile(foTarget);
            if (lock == null) {
                return false;
            }
            try (InputStream in = source.getInputStream()) {
                if (in == null) {
                    // definitely should not happen
                    return false;
                }
                try (OutputStream out = foTarget.getOutputStream(lock)) {
                    // TODO the doewnload action shoudln't save all file before
                    // executing, then the ide will ask, whether user wants
                    // to replace currently editted file.
                    FileUtil.copy(in, out);
                    moved = true;
                }
            } finally {
                lock.releaseLock();
            }
        } catch (IOException | InvalidPathException ex) {
            LOGGER.log(Level.INFO, "Error while moving local file", ex);
            moved = false;
        } catch (DownloadSkipException ex) {
            downloadSkipped = true;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Content of tmp local file copied into %s: %s", target.getName(), moved));
        }
        if (downloadSkipped) {
            throw new DownloadSkipException();
        }
        return moved;
    }

    private FileObject ensureTargetExists(File target) {
        if (target.exists()) {
            // exists
            FileObject targetFo = FileUtil.toFileObject(target);
            if (targetFo == null) {
                LOGGER.log(Level.WARNING, "Cannot get file object for existing file {0}", target);
                return null;
            }
            return targetFo;
        }
        // does not exist -> create it
        LOGGER.log(Level.FINE, "Local file {0} does not exists so it will be created", target.getName());

        File parent = target.getParentFile();
        boolean parentExists = parent.isDirectory();
        if (!parentExists) {
            parentExists = parent.mkdirs();
        }
        if (!parentExists) {
            LOGGER.log(Level.INFO, "Cannot create parent directory {0}", parent);
            return null;
        }
        FileObject parentFo = FileUtil.toFileObject(parent);
        if (parentFo == null) {
            LOGGER.log(Level.WARNING, "Cannot get file object for existing file {0}", parent);
            return null;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Data file %s created.", target.getName()));
        }
        try {
            return parentFo.createData(target.getName());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Error while creating local file '" + target + "'", ex);
            return null;
        }
    }

    private FileLock lockFile(FileObject fo) throws IOException, DownloadSkipException {
        try {
            return fo.lock();
        } catch (FileAlreadyLockedException lockedException) {
            if (warnChangedFile(fo)) {
                FileUtils.saveFile(fo);
                // XXX remove once #213141 is fixed
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                return fo.lock();
            } else {
                throw new DownloadSkipException();
            }
        }
    }

    private boolean warnChangedFile(FileObject file) {
        NotifyDescriptor.Confirmation desc = new NotifyDescriptor.Confirmation(Bundle.RemoteClient_file_replaceUnsavedContent_question(file.getNameExt()),
                Bundle.RemoteClient_file_replaceUnsavedContent_title(), NotifyDescriptor.Confirmation.OK_CANCEL_OPTION);
        return DialogDisplayer.getDefault().notify(desc).equals(NotifyDescriptor.OK_OPTION);
    }


    private File getLocalFile(File localFile, TransferFile transferFile) {
        File newFile = transferFile.resolveLocalFile(localFile);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(newFile));
        if (fo != null) {
            fo.refresh();
        }
        return newFile;
    }

    // #169778
    private File getLocalFile(File localFile, TransferFile parent, RemoteFile file) {
        File newFile = new File(getLocalFile(localFile, parent), file.getName());
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(newFile));
        if (fo != null) {
            fo.refresh();
        }
        return newFile;
    }

    /**
     * Check whether any of parent file (but not {@link TransferFile#isProjectRoot() project root}) is a symlink.
     * @param file file to check
     * @return {@code true} if any of parent file (but not {@link TransferFile#isProjectRoot() project root}) is a symlink, {@code false} otherwise
     */
    private boolean isParentLink(TransferFile file) {
        while (file.hasParent()) {
            TransferFile parent = file.getParent();
            if (parent.isProjectRoot()) {
                return false;
            }
            if (parent.isLink()) {
                return true;
            }
            file = parent;
        }
        return false;
    }

    public Set<TransferFile> prepareDelete(FileObject baseLocalDirectory, FileObject... filesToDelete) {
        LOGGER.fine("Preparing files to delete => calling prepareUpload because in fact the same operation is done");
        return prepareUpload(baseLocalDirectory, filesToDelete);
    }

    public TransferInfo delete(TransferFile fileToDelete) throws RemoteException {
        return delete(Collections.singleton(fileToDelete));
    }

    public TransferInfo delete(Set<TransferFile> filesToDelete) throws RemoteException {
        assert filesToDelete != null;
        assert filesToDelete.size() > 0 : "At least one file to upload must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        TransferInfo transferInfo = new TransferInfo();

        try {
            getOperationMonitor().operationStart(Operation.DELETE, filesToDelete);
            // first, remove all the files
            //  then remove _empty_ directories (motivation is to prevent data loss; somebody else could upload some file there)
            Set<TransferFile> files = getFiles(filesToDelete);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Only files: %s => %s", filesToDelete, files));
            }
            delete(transferInfo, files);

            Set<TransferFile> dirs = getDirectories(filesToDelete);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Only dirs: %s => %s", filesToDelete, dirs));
            }
            delete(transferInfo, dirs);

            assert filesToDelete.size() == files.size() + dirs.size() : String.format("%s does not match files and dirs: %s %s", filesToDelete, files, dirs);
        } finally {
            getOperationMonitor().operationFinish(Operation.DELETE, filesToDelete);
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void delete(TransferInfo transferInfo, Set<TransferFile> filesToDelete) {
        for (TransferFile file : filesToDelete) {
            if (cancelled) {
                LOGGER.fine("Delete cancelled");
                break;
            }

            try {
                getOperationMonitor().operationProcess(Operation.DELETE, file);
                deleteFile(transferInfo, file);
            } catch (IOException | RemoteException exc) {
                LOGGER.log(Level.INFO, null, exc);
                transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                continue;
            }
        }
    }

    private synchronized void deleteFile(TransferInfo transferInfo, TransferFile file) throws IOException, RemoteException {
        boolean success = false;
        cdBaseRemoteDirectory();
        if (file.isDirectory()) {
            // folder => try to delete it but it can fail (most probably when it's not empty)
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Deleting directory: {0}", file);
            }
            success = remoteClient.deleteDirectory(file.getRemotePath());
            LOGGER.log(Level.FINE, "Folder deleted: {0}", success);
        } else {
            // file => simply delete it
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Deleting file: {0}", file);
            }

            success = remoteClient.deleteFile(file.getRemotePath());
            LOGGER.log(Level.FINE, "File deleted: {0}", success);
        }

        if (success) {
            transferSucceeded(transferInfo, file);
        } else {
            String msg = null;
            if (!remoteClient.exists(file.getParentRemotePath(), file.getName())) {
                msg = NbBundle.getMessage(RemoteClient.class, "MSG_FileNotExists", file.getName());
            } else {
                // maybe non empty dir?
                if (file.isDirectory()
                        && cdBaseRemoteDirectory(file.getParentRemotePath(), false)
                        && remoteClient.listFiles().size() > 0) {
                    msg = NbBundle.getMessage(RemoteClient.class, "MSG_FolderNotEmpty", file.getName());
                } else {
                    msg = getOperationFailureMessage(Operation.DELETE, file.getName());
                }
            }
            transferFailed(transferInfo, file, msg);
        }
    }

    private void transferSucceeded(TransferInfo transferInfo, TransferFile file) {
        transferInfo.addTransfered(file);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Transfered: {0}", file);
        }
    }

    private void transferFailed(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isFailed(file)) {
            transferInfo.addFailed(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Failed: {0}, reason: {1}", new Object[] {file, reason});
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Failed: {0}, reason: {1} [ignored, failed already]", new Object[] {file, reason});
            }
        }
    }

    private void transferPartiallyFailed(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isPartiallyFailed(file)) {
            transferInfo.addPartiallyFailed(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Partially failed: {0}, reason: {1}", new Object[] {file, reason});
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Partially failed: {0}, reason: {1} [ignored, partially failed already]", new Object[] {file, reason});
            }
        }
    }

    private void transferIgnored(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isIgnored(file)) {
            transferInfo.addIgnored(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Ignored: {0}, reason: {1}", new Object[] {file, reason});
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Ignored: {0}, reason: {1} [ignored, ignored already]", new Object[] {file, reason});
            }
        }
    }

    private synchronized String getOperationFailureMessage(Operation operation, String fileName) {
        String message = remoteClient.getNegativeReplyString();
        if (message == null) {
            String key = null;
            switch (operation) {
                case UPLOAD:
                    key = "MSG_CannotUploadFile"; // NOI18N
                    break;
                case DOWNLOAD:
                    key = "MSG_CannotDownloadFile"; // NOI18N
                    break;
                case DELETE:
                    key = "MSG_CannotDeleteFile"; // NOI18N
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operation type: " + operation);
            }
            message = NbBundle.getMessage(RemoteClient.class, key, fileName);
        }
        return message;
    }

    private synchronized void ensureConnected() throws RemoteException {
        if (!remoteClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }
    }

    private boolean cdBaseRemoteDirectory() throws RemoteException {
        return cdRemoteDirectory(baseRemoteDirectory, true);
    }

    private boolean cdBaseRemoteDirectory(String subdirectory, boolean create) throws RemoteException {
        assert subdirectory == null || !subdirectory.startsWith(TransferFile.REMOTE_PATH_SEPARATOR) : "Subdirectory must be null or relative [" + subdirectory + "]";

        String path = baseRemoteDirectory;
        if (subdirectory != null && !subdirectory.equals(TransferFile.REMOTE_PROJECT_ROOT)) {
            path = baseRemoteDirectory
                    + (baseRemoteDirectory.equals(TransferFile.REMOTE_PATH_SEPARATOR) ? "" : TransferFile.REMOTE_PATH_SEPARATOR) // upload directly to root directory "/"? // NOI18N
                    + subdirectory;
        }
        return cdRemoteDirectory(path, create);
    }

    private synchronized boolean cdRemoteDirectory(String directory, boolean create) throws RemoteException {
        LOGGER.log(Level.FINE, "Changing directory to {0}", directory);
        boolean success = remoteClient.changeWorkingDirectory(directory);
        if (!success && create) {
            return createAndCdRemoteDirectory(directory);
        }
        return success;
    }

    /**
     * Create file path on remote server <b>in the current directory</b>.
     * @param filePath file path to create, can be even relative (e.g. "a/b/c/d").
     */
    private synchronized boolean createAndCdRemoteDirectory(String filePath) throws RemoteException {
        LOGGER.log(Level.FINE, "Creating file path {0}", filePath);
        if (filePath.startsWith(TransferFile.REMOTE_PATH_SEPARATOR)) {
            // enter root directory
            if (!remoteClient.changeWorkingDirectory(TransferFile.REMOTE_PATH_SEPARATOR)) {
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", "/"), remoteClient.getReplyString());
            }
        }
        for (String dir : filePath.split(TransferFile.REMOTE_PATH_SEPARATOR)) {
            if (dir.length() == 0) {
                // handle paths like "a//b///c/d" (dir can be "")
                continue;
            }
            if (!remoteClient.changeWorkingDirectory(dir)) {
                if (!remoteClient.makeDirectory(dir)) {
                    // XXX check 52x codes
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Cannot create directory: {0}", getWorkingDirectory() + TransferFile.REMOTE_PATH_SEPARATOR + dir);
                    }
                    throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDirectory", dir), remoteClient.getReplyString());
                } else if (!remoteClient.changeWorkingDirectory(dir)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Cannot enter directory: {0}", getWorkingDirectory() + TransferFile.REMOTE_PATH_SEPARATOR + dir);
                    }
                    return false;
                    // XXX
                    //throw new RemoteException("Cannot change directory '" + dir + "' [" + remoteClient.getReplyString() + "]");
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Directory ''{0}'' created and entered", getWorkingDirectory());
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [remote configuration: "); // NOI18N
        sb.append(configuration);
        sb.append(", baseRemoteDirectory: "); // NOI18N
        sb.append(baseRemoteDirectory);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    private boolean isVisible(String name) {
        assert name != null;
        // XXX
        return !IGNORED_DIRS.contains(name);
    }

    private boolean isVisible(File file) {
        assert file != null;
        if (!isVisible(file.getName())) {
            return false;
        }
        return properties.getPhpVisibilityQuery().isVisible(file);
    }

    private static boolean mkLocalDirs(File folder) {
        try {
            FileUtil.createFolder(folder);
        } catch (IOException exc) {
            LOGGER.log(Level.INFO, null, exc);
            return false;
        }
        return true;
    }

    private Set<TransferFile> getFiles(Set<TransferFile> all) {
        Set<TransferFile> files = new HashSet<>();
        for (TransferFile file : all) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }

    private Set<TransferFile> getDirectories(Set<TransferFile> all) {
        // we need to get longest paths first to be able to delete directories properly
        //  (e.g. to have [a/b, a] and not [a, a/b])
        Set<TransferFile> dirs = new TreeSet<>(new Comparator<TransferFile>() {
            private final String SEPARATOR = Pattern.quote(TransferFile.REMOTE_PATH_SEPARATOR);
            @Override
            public int compare(TransferFile o1, TransferFile o2) {
                int cmp = o2.getRemotePath().split(SEPARATOR).length - o1.getRemotePath().split(SEPARATOR).length;
                // do not miss any item
                return cmp != 0 ? cmp : 1;
            }
        });
        for (TransferFile file : all) {
            if (file.isDirectory()) {
                dirs.add(file);
            }
        }
        return dirs;
    }

    // #204874 - some servers return ending '/' for directories => remove it
    private synchronized String getWorkingDirectory() throws RemoteException {
        return RemoteUtils.sanitizeDirectoryPath(remoteClient.printWorkingDirectory());
    }

    public RemoteClientImplementation createRemoteClientImplementation(final String baseLocalDirectory) {
        return new RemoteClientImplementation() {

            @Override
            public String getBaseLocalDirectory() {
                return baseLocalDirectory;
            }

            @Override
            public String getBaseRemoteDirectory() {
                return RemoteClient.this.getBaseRemoteDirectory();
            }

            @Override
            public List<TransferFile> listLocalFiles(TransferFile file) {
                List<TransferFile> kids = new ArrayList<>();
                File[] children = file.resolveLocalFile().listFiles();
                if (children != null) {
                    for (File child : children) {
                        if (isVisible(child)) {
                            LOGGER.log(Level.FINE, "File {0} added to children", child);
                            kids.add(TransferFile.fromFile(this, file, child));
                        } else {
                            LOGGER.log(Level.FINE, "File {0} NOT added to children [invisible]", child);
                        }
                    }
                }
                return kids;
            }

            @Override
            public List<TransferFile> listRemoteFiles(TransferFile file) throws RemoteException {
                return RemoteClient.this.listFiles(file);
            }
        };
    }

    //~ Inner classes

    public static interface OperationMonitor {
        /**
         * {@link Operation} started for the files.
         * @param operation {@link Operation} currently run
         * @param forFiles collection of files for which the operation started
         */
        void operationStart(Operation operation, Collection<TransferFile> forFiles);

        /**
         * {@link Operation} process for the file.
         * @param operation {@link Operation} currently run
         * @param forFile files for which the operation is run
         */
        void operationProcess(Operation operation, TransferFile forFile);

        /**
         * {@link Operation} finished for the files.
         * @param operation {@link Operation} currently run
         * @param forFiles collection of files for which the operation finished
         */
        void operationFinish(Operation operation, Collection<TransferFile> forFiles);
        /**
         * Called when new files are added to this monitor (lazily listed files).
         * @param files new files to be added
         */
        void addUnits(Collection<TransferFile> files);
    }

    private static final class DevNullOperationMonitor implements OperationMonitor {
        @Override
        public void operationStart(Operation operation, Collection<TransferFile> forFiles) {
        }
        @Override
        public void operationProcess(Operation operation, TransferFile forFile) {
        }
        @Override
        public void operationFinish(Operation operation, Collection<TransferFile> forFiles) {
        }
        @Override
        public void addUnits(Collection<TransferFile> files) {
        }
    }

    /**
     * Advanced properties for a {@link RemoteClient}.
     * <p>
     * This class is thread-safe.
     */
    public static final class AdvancedProperties {
        private final InputOutput io;
        private final String additionalInitialSubdirectory;
        private final boolean preservePermissions;
        private final boolean uploadDirectly;
        private final OperationMonitor operationMonitor;
        private final PhpVisibilityQuery phpVisibilityQuery;

        /**
         * Create advanced properties for a {@link RemoteClient}.
         */
        public AdvancedProperties() {
            this(new AdvancedPropertiesBuilder());
        }

        private AdvancedProperties(AdvancedPropertiesBuilder builder) {
            io = builder.io;
            additionalInitialSubdirectory = builder.additionalInitialSubdirectory;
            preservePermissions = builder.preservePermissions;
            uploadDirectly = builder.uploadDirectly;
            operationMonitor = builder.operationMonitor;
            phpVisibilityQuery = builder.phpVisibilityQuery;
        }

        /**
         * Get additional initial subdirectory (directory which starts with {@value TransferFile#SEPARATOR} and is appended
         * to {@link RemoteConfiguration#getInitialDirectory()} and set as default base remote directory. Can be <code>null</code>.
         * @return additional initial subdirectory, can be <code>null</code>.
         */
        public String getAdditionalInitialSubdirectory() {
            return additionalInitialSubdirectory;
        }

        /**
         * Return properties with configured additional initial subdirectory.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param additionalInitialSubdirectory additional directory which must start with {@value TransferFile#SEPARATOR} and is appended
         *                                      to {@link RemoteConfiguration#getInitialDirectory()} and
         *                                      set as default base remote directory.
         * @return new properties with configured additional initial subdirectory, can be <code>null</code>.
         */
        public AdvancedProperties setAdditionalInitialSubdirectory(String additionalInitialSubdirectory) {
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setAdditionalInitialSubdirectory(additionalInitialSubdirectory));
        }

        /**
         * Get {@link InputOutput}, the displayer of protocol commands, can be <code>null</code>.
         * Displays all the commands received from server.
         * @return {@link InputOutput}, the displayer of protocol commands, can be <code>null</code>.
         */
        public InputOutput getInputOutput() {
            return io;
        }

        /**
         * Return properties with configured displayer of protocol commands.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param io {@link InputOutput}, the displayer of protocol commands.
         *           Displays all the commands received from server.
         * @return new properties with configured displayer of protocol commands
         */
        public AdvancedProperties setInputOutput(InputOutput io) {
            Parameters.notNull("io", io);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setInputOutput(io));
        }

        /**
         * Get {@link OperationMonitor monitor of commands}.
         * @return {@link OperationMonitor monitor of commands}, can be <code>null</code>.
         */
        public OperationMonitor getOperationMonitor() {
            return operationMonitor;
        }

        /**
         * Return properties with configured {@link OperationMonitor monitor of commands}.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param operationMonitor {@link OperationMonitor monitor of commands}.
         * @return new properties with configured {@link OperationMonitor monitor of commands}
         */
        public AdvancedProperties setOperationMonitor(OperationMonitor operationMonitor) {
            Parameters.notNull("operationMonitor", operationMonitor);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setOperationMonitor(operationMonitor));
        }

        /**
         * <code>True</code> if permissions should be preserved; please note that this is not supported for local
         * files (possible in Java 6 and newer only) and also it will very likely cause slow down of file transfer.
         * @return <code>true</code> if permissions should be preserved
         */
        public boolean isPreservePermissions() {
            return preservePermissions;
        }

        /**
         * Return properties with configured preserved permissions.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param preservePermissions <code>true</code> if permissions should be preserved; please note that this is not supported for local
         *                            files (possible in Java 6 and newer only) and also it will very likely cause slow down of file transfer.
         * @return new properties with configured preserved permissions
         */
        public AdvancedProperties setPreservePermissions(boolean preservePermissions) {
            Parameters.notNull("preservePermissions", preservePermissions);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setPreservePermissions(preservePermissions));
        }

        /**
         * <code>True</code> if file upload is done <b>without</b> a temporary file.
         * <b>Warning:</b> can be dangerous.
         * @return <code>true</code> if file upload is done <b>without</b> a temporary file
         */
        public boolean isUploadDirectly() {
            return uploadDirectly;
        }

        /**
         * Return properties with configured direct upload.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param uploadDirectly whether to upload files <b>without</b> a temporary file. <b>Warning:</b> can be dangerous.
         * @return new properties with configured direct upload
         */
        public AdvancedProperties setUploadDirectly(boolean uploadDirectly) {
            Parameters.notNull("uploadDirectly", uploadDirectly);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setUploadDirectly(uploadDirectly));
        }

        /**
         * Get {@link PhpVisibilityQuery PHP project specific visibility query}, fallback is
         * the {@link org.netbeans.api.queries.VisibilityQuery#getDefault() default visibility query}.
         * @return {@link PhpVisibilityQuery PHP project specific visibility query}, cannot be <code>null</code>.
         */
        public PhpVisibilityQuery getPhpVisibilityQuery() {
            if (phpVisibilityQuery != null) {
                return phpVisibilityQuery;
            }
            return PhpVisibilityQuery.getDefault();
        }

        /**
         * Return properties with configured {@link PhpVisibilityQuery PHP project specific visibility query}.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param phpVisibilityQuery {@link PhpVisibilityQuery PHP project specific visibility query}.
         * @return new properties with configured {@link PhpVisibilityQuery PHP project specific visibility query}
         */
        public AdvancedProperties setPhpVisibilityQuery(PhpVisibilityQuery phpVisibilityQuery) {
            Parameters.notNull("phpVisibilityQuery", phpVisibilityQuery);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setPhpVisibilityQuery(phpVisibilityQuery));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("AdvancedProperties [ io: ");
            sb.append(io);
            sb.append(", additionalInitialSubdirectory: ");
            sb.append(additionalInitialSubdirectory);
            sb.append(", preservePermissions: ");
            sb.append(preservePermissions);
            sb.append(", uploadDirectly: ");
            sb.append(uploadDirectly);
            sb.append(", operationMonitor: ");
            sb.append(operationMonitor);
            sb.append(", phpVisibilityQuery: ");
            sb.append(phpVisibilityQuery);
            sb.append(" ]");
            return sb.toString();
        }
    }

    private static final class AdvancedPropertiesBuilder {
        InputOutput io;
        String additionalInitialSubdirectory;
        boolean preservePermissions = false;
        boolean uploadDirectly = false;
        OperationMonitor operationMonitor;
        PhpVisibilityQuery phpVisibilityQuery;

        AdvancedPropertiesBuilder() {
        }

        public AdvancedPropertiesBuilder(AdvancedProperties properties) {
            io = properties.getInputOutput();
            additionalInitialSubdirectory = properties.getAdditionalInitialSubdirectory();
            preservePermissions = properties.isPreservePermissions();
            uploadDirectly = properties.isUploadDirectly();
            operationMonitor = properties.getOperationMonitor();
            phpVisibilityQuery = properties.getPhpVisibilityQuery();
        }

        public AdvancedPropertiesBuilder setAdditionalInitialSubdirectory(String additionalInitialSubdirectory) {
            this.additionalInitialSubdirectory = additionalInitialSubdirectory;
            return this;
        }

        public AdvancedPropertiesBuilder setInputOutput(InputOutput io) {
            this.io = io;
            return this;
        }

        public AdvancedPropertiesBuilder setOperationMonitor(OperationMonitor operationMonitor) {
            this.operationMonitor = operationMonitor;
            return this;
        }

        public AdvancedPropertiesBuilder setPreservePermissions(boolean preservePermissions) {
            this.preservePermissions = preservePermissions;
            return this;
        }

        public AdvancedPropertiesBuilder setUploadDirectly(boolean uploadDirectly) {
            this.uploadDirectly = uploadDirectly;
            return this;
        }

        public AdvancedPropertiesBuilder setPhpVisibilityQuery(PhpVisibilityQuery phpVisibilityQuery) {
            this.phpVisibilityQuery = phpVisibilityQuery;
            return this;
        }
    }

    private static final class DownloadAtomicAction implements AtomicAction {
        private final Runnable runnable;

        public DownloadAtomicAction(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() throws IOException {
            if (runnable != null) {
                runnable.run();
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            return getClass() == obj.getClass();
        }

        @Override
        public int hashCode() {
            return 42;
        }
    }

}
