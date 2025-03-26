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
package org.netbeans.modules.php.project.connections.sync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TmpLocalFile;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.transfer.TransferInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Controller for synchronization.
 */
public final class SyncController implements Cancellable {

    static enum SourceFiles {
        PROJECT,
        DIRECTORIES_ONLY,
        INDIVIDUAL_FILES;

        static SourceFiles forFiles(@NullAllowed FileObject[] files) {
            if (files == null) {
                return PROJECT;
            }
            for (FileObject file : files) {
                if (file.isData()) {
                    return INDIVIDUAL_FILES;
                }
            }
            return DIRECTORIES_ONLY;
        }
    }

    static final Logger LOGGER = Logger.getLogger(SyncController.class.getName());
    static final RequestProcessor SYNC_RP = new RequestProcessor("Remote PHP Synchronization", 1); // NOI18N

    final FileObject[] files;
    final PhpProject phpProject;
    final RemoteClient remoteClient;
    final RemoteConfiguration remoteConfiguration;
    final TimeStamps timeStamps;
    final SourceFiles sourceFiles;

    volatile boolean cancelled = false;


    private SyncController(FileObject[] files, PhpProject phpProject, RemoteClient remoteClient, RemoteConfiguration remoteConfiguration) {
        this.files = files;
        this.phpProject = phpProject;
        this.remoteClient = remoteClient;
        this.remoteConfiguration = remoteConfiguration;
        timeStamps = new TimeStamps(phpProject);
        sourceFiles = SourceFiles.forFiles(files);
    }

    public static SyncController forProject(PhpProject phpProject, RemoteClient remoteClient, RemoteConfiguration remoteConfiguration) {
        return new SyncController(null, phpProject, remoteClient, remoteConfiguration);
    }

    public static SyncController forFiles(FileObject[] files, PhpProject phpProject, RemoteClient remoteClient, RemoteConfiguration remoteConfiguration) {
        assert files != null;
        return new SyncController(files, phpProject, remoteClient, remoteConfiguration);
    }

    public void synchronize(final SyncResultProcessor resultProcessor) {
        SYNC_RP.post(new Runnable() {
            @Override
            public void run() {
                showPanel(fetchSyncItems(), resultProcessor);
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "SyncController.fetching.project=Fetching {0} files",
        "# {0} - file count",
        "SyncController.fetching.files=Fetching {0} individual files"
    })
    SyncItems fetchSyncItems() {
        assert !SwingUtilities.isEventDispatchThread();
        SyncItems items = null;
        String displayName;
        if (sourceFiles == SourceFiles.PROJECT) {
            displayName = Bundle.SyncController_fetching_project(phpProject.getName());
        } else {
            displayName = Bundle.SyncController_fetching_files(files.length);
        }
        final ProgressHandle progressHandle = ProgressHandle.createHandle(displayName, this);
        try {
            progressHandle.start();
            FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
            assert sources != null;
            Set<TransferFile> remoteFiles = getRemoteFiles(sources);
            Set<TransferFile> localFiles = getLocalFiles(sources);
            items = pairItems(remoteFiles, localFiles);
        } catch (RemoteException ex) {
            disconnect();
            RemoteUtils.processRemoteException(ex);
        } finally {
            progressHandle.finish();
        }
        return items;
    }

    private Set<TransferFile> getRemoteFiles(FileObject sources) throws RemoteException {
        Set<TransferFile> remoteFiles = new HashSet<>();
        if (sourceFiles == SourceFiles.PROJECT) {
            initRemoteFiles(remoteFiles, remoteClient.prepareDownload(sources, sources));
        } else {
            // fetch individual files (can be directories as well)...
            // cannot simply call prepareDownload() since we need to fetch _all_ files from server (to get proper metadata)
            for (FileObject file : files) {
                TransferFile transferFile = remoteClient.listFile(sources, file);
                if (transferFile != null) {
                    // remote file exists and is a file
                    remoteFiles.add(transferFile);
                } else {
                    // might be a directory
                    initRemoteFiles(remoteFiles, remoteClient.prepareDownload(sources, file));
                }
            }
        }
        return remoteFiles;
    }

    private Set<TransferFile> getLocalFiles(FileObject sources) {
        Set<TransferFile> localFiles = new HashSet<>();
        if (sourceFiles == SourceFiles.PROJECT) {
            initLocalFiles(localFiles, remoteClient.prepareUpload(sources, sources));
        } else {
            // fetch individual files (can be directories as well)...
            initLocalFiles(localFiles, remoteClient.prepareUpload(sources, files));
        }
        return localFiles;
    }

    void showPanel(final SyncItems items, final SyncResultProcessor resultProcessor) {
        if (cancelled || items == null) {
            if (items != null) {
                items.cleanup();
            }
            return;
        }
        try {
            ProjectManager.getDefault().saveProject(phpProject);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SyncPanel panel = new SyncPanel(phpProject, remoteConfiguration.getDisplayName(), items.getItems(), remoteClient, sourceFiles);
                if (panel.open()) {
                    List<SyncItem> itemsToSynchronize = panel.getItems();
                    doSynchronize(items, itemsToSynchronize, panel.getSyncInfo(itemsToSynchronize), resultProcessor);
                } else {
                    disconnect();
                    items.cleanup();
                }
            }
        });
    }

    void doSynchronize(SyncItems syncItems, List<SyncItem> itemsToSynchronize, SyncPanel.SyncInfo syncInfo, SyncResultProcessor resultProcessor) {
        assert SwingUtilities.isEventDispatchThread();

        if (cancelled) {
            // in fact, cannot happen here
            return;
        }
        new Synchronizer(syncItems, itemsToSynchronize, syncInfo, resultProcessor).sync();
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        remoteClient.cancel();
        disconnect();
        return true;
    }

    void disconnect() {
        try {
            remoteClient.disconnect(true);
        } catch (RemoteException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private SyncItems pairItems(Set<TransferFile> remoteFiles, Set<TransferFile> localFiles) {
        List<TransferFile> remoteFilesSorted = new ArrayList<>(remoteFiles);
        remoteFilesSorted.sort(TransferFile.TRANSFER_FILE_COMPARATOR);
        List<TransferFile> localFilesSorted = new ArrayList<>(localFiles);
        localFilesSorted.sort(TransferFile.TRANSFER_FILE_COMPARATOR);

        removeProjectRoot(remoteFilesSorted);
        removeProjectRoot(localFilesSorted);

        SyncItems items = new SyncItems();
        Iterator<TransferFile> remoteFilesIterator = remoteFilesSorted.iterator();
        Iterator<TransferFile> localFilesIterator = localFilesSorted.iterator();
        TransferFile remote = null;
        TransferFile local = null;
        while (remoteFilesIterator.hasNext()
                || localFilesIterator.hasNext()) {
            if (remote == null
                    && remoteFilesIterator.hasNext()) {
                remote = remoteFilesIterator.next();
            }
            if (local == null
                    && localFilesIterator.hasNext()) {
                local = localFilesIterator.next();
            }
            if (remote == null
                    || local == null) {
                items.add(remote, local, timeStamps.getSyncTimestamp(remote != null ? remote : local));
                remote = null;
                local = null;
            } else {
                int compare = TransferFile.TRANSFER_FILE_COMPARATOR.compare(remote, local);
                if (compare == 0) {
                    // same remote paths
                    items.add(remote, local, timeStamps.getSyncTimestamp(remote));
                    remote = null;
                    local = null;
                } else if (compare < 0) {
                    items.add(remote, null, timeStamps.getSyncTimestamp(remote));
                    remote = null;
                } else {
                    items.add(null, local, timeStamps.getSyncTimestamp(local));
                    local = null;
                }
            }
        }
        return items;
    }

    private void removeProjectRoot(List<TransferFile> files) {
        if (files.isEmpty()) {
            return;
        }
        if (files.get(0).isProjectRoot()) {
            files.remove(0);
        }
    }

    /**
     * Remote files are downloaded lazily so we need to fetch all children.
     */
    private void initRemoteFiles(Set<TransferFile> allRemoteFiles, Collection<TransferFile> remoteFiles) {
        allRemoteFiles.addAll(remoteFiles);
        for (TransferFile file : remoteFiles) {
            initRemoteFiles(allRemoteFiles, file.getRemoteChildren());
        }
    }

    /**
     * Local files are uploaded lazily so we need to fetch all children.
     */
    private void initLocalFiles(Set<TransferFile> allLocalFiles, Collection<TransferFile> localFiles) {
        allLocalFiles.addAll(localFiles);
        for (TransferFile file : localFiles) {
            initLocalFiles(allLocalFiles, file.getLocalChildren());
        }
    }

    //~ Inner classes

    public static final class SyncResult {

        private final TransferInfo downloadTransferInfo = new TransferInfo();
        private final TransferInfo uploadTransferInfo = new TransferInfo();
        private final TransferInfo localDeleteTransferInfo = new TransferInfo();
        private final TransferInfo remoteDeleteTransferInfo = new TransferInfo();


        SyncResult() {
        }

        public TransferInfo getDownloadTransferInfo() {
            return downloadTransferInfo;
        }

        public TransferInfo getUploadTransferInfo() {
            return uploadTransferInfo;
        }

        public TransferInfo getLocalDeleteTransferInfo() {
            return localDeleteTransferInfo;
        }

        public TransferInfo getRemoteDeleteTransferInfo() {
            return remoteDeleteTransferInfo;
        }

    }

    public interface SyncResultProcessor {
        void process(SyncResult result);
    }

    private final class Synchronizer {

        private final SyncItems syncItems;
        private final List<SyncItem> itemsToSynchronize;
        private final SyncResultProcessor resultProcessor;
        final ProgressPanel progressPanel;
        final AtomicBoolean cancel = new AtomicBoolean();


        public Synchronizer(SyncItems syncItems, List<SyncItem> itemsToSynchronize, SyncPanel.SyncInfo syncInfo, SyncResultProcessor resultProcessor) {
            assert SwingUtilities.isEventDispatchThread();
            this.syncItems = syncItems;
            this.itemsToSynchronize = itemsToSynchronize;
            this.resultProcessor = resultProcessor;
            progressPanel = new ProgressPanel(syncInfo);
        }

        public void sync() {
            assert SwingUtilities.isEventDispatchThread();
            progressPanel.createPanel(cancel);
            SYNC_RP.post(new Runnable() {
                @Override
                public void run() {
                    progressPanel.start(itemsToSynchronize);
                    try {
                        doSync();
                    } finally {
                        if (cancel.get()) {
                            progressPanel.cancel();
                        } else {
                            progressPanel.finish();
                        }
                    }
                }
            });
        }

        @NbBundle.Messages("SyncController.error.tmpFileCopyFailed=Failed to copy content of temporary file.")
        void doSync() {
            assert !SwingUtilities.isEventDispatchThread();

            Set<TransferFile> remoteFilesForDelete = new HashSet<>();
            Set<TransferFile> localFilesForDelete = new HashSet<>();
            final SyncResult syncResult = new SyncResult();
            for (SyncItem syncItem : itemsToSynchronize) {
                if (cancel.get()) {
                    break;
                }
                TransferFile remoteTransferFile = syncItem.getRemoteTransferFile();
                TransferFile localTransferFile = syncItem.getLocalTransferFile();
                switch (syncItem.getOperation()) {
                    case SYMLINK:
                        // noop
                        break;
                    case NOOP:
                        progressPanel.decreaseNoopNumber();
                        break;
                    case DOWNLOAD:
                    case DOWNLOAD_REVIEW:
                        try {
                            TransferInfo downloadInfo = remoteClient.download(Collections.singleton(remoteTransferFile));
                            if (!mergeTransferInfo(downloadInfo, syncResult.getDownloadTransferInfo())) {
                                progressPanel.downloadErrorOccured();
                            }
                        } catch (RemoteException ex) {
                            syncResult.getDownloadTransferInfo().addFailed(remoteTransferFile, ex.getLocalizedMessage());
                            progressPanel.downloadErrorOccured();
                        } finally {
                            progressPanel.decreaseDownloadNumber(syncItem);
                        }
                        break;
                    case UPLOAD:
                    case UPLOAD_REVIEW:
                        try {
                            // tmp files?
                            if (copyContent(syncItem.getTmpLocalFile(), localTransferFile.resolveLocalFile())) {
                                TransferInfo uploadInfo = remoteClient.upload(Collections.singleton(localTransferFile));
                                if (mergeTransferInfo(uploadInfo, syncResult.getUploadTransferInfo())) {
                                    progressPanel.decreaseUploadNumber(syncItem);
                                } else {
                                    progressPanel.uploadErrorOccured();
                                }
                            } else {
                                // valid fileobject not found??
                                LOGGER.log(Level.WARNING, "Cannot find FileObject for file {0}", localTransferFile.resolveLocalFile());
                                syncResult.getUploadTransferInfo().addFailed(localTransferFile, Bundle.SyncController_error_tmpFileCopyFailed());
                                progressPanel.uploadErrorOccured();
                            }
                        } catch (RemoteException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                            syncResult.getUploadTransferInfo().addFailed(localTransferFile, ex.getLocalizedMessage());
                            progressPanel.uploadErrorOccured();
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                            syncResult.getUploadTransferInfo().addFailed(localTransferFile, Bundle.SyncController_error_tmpFileCopyFailed());
                            progressPanel.uploadErrorOccured();
                        }
                        break;
                    case DELETE:
                        // local
                        if (localTransferFile != null) {
                            localFilesForDelete.add(localTransferFile);
                        }
                        // remote
                        if (remoteTransferFile != null) {
                            remoteFilesForDelete.add(remoteTransferFile);
                        }
                        break;
                    default:
                        assert false : "Unsupported synchronization operation: " + syncItem.getOperation();
                }
                // set timestamp
                setTimeStamp(remoteTransferFile != null ? remoteTransferFile : localTransferFile);
            }
            if (!cancel.get()) {
                deleteFiles(syncResult, remoteFilesForDelete, localFilesForDelete);
            }
            if (sourceFiles == SourceFiles.PROJECT) {
                // set timestamp for project source dir itself
                File sources = FileUtil.toFile(ProjectPropertiesSupport.getSourcesDirectory(phpProject));
                assert sources != null;
                TransferFile transferFile = TransferFile.fromFile(remoteClient.createRemoteClientImplementation(sources.getAbsolutePath()),
                        null, sources);
                setTimeStamp(transferFile);
            }
            syncItems.cleanup();
            disconnect();
            resultProcessor.process(syncResult);
        }

        private void setTimeStamp(TransferFile transferFile) {
            assert transferFile != null;
            timeStamps.setSyncTimestamp(transferFile);
        }

        private boolean copyContent(TmpLocalFile source, File target) throws IOException {
            if (source == null) {
                // no tmp files
                return true;
            }
            FileObject fileObject = FileUtil.toFileObject(target);
            if (fileObject == null || !fileObject.isValid()) {
                return false;
            }
            try (InputStream inputStream = source.getInputStream(); OutputStream outputStream = fileObject.getOutputStream()) {
                FileUtil.copy(inputStream, outputStream);
            }
            return true;
        }

        /**
         * @return {@code true} if no transfer error occured
         */
        private boolean mergeTransferInfo(TransferInfo from, TransferInfo to) {
            to.setRuntime(to.getRuntime() + from.getRuntime());
            to.getTransfered().addAll(from.getTransfered());
            to.getIgnored().putAll(from.getIgnored());
            Map<TransferFile, String> partiallyFailed = from.getPartiallyFailed();
            to.getPartiallyFailed().putAll(partiallyFailed);
            Map<TransferFile, String> failed = from.getFailed();
            to.getFailed().putAll(failed);
            return partiallyFailed.isEmpty() && failed.isEmpty();
        }

        private void deleteFiles(SyncResult syncResult, Set<TransferFile> remoteFiles, Set<TransferFile> localFiles) {
            deleteRemoteFiles(syncResult, remoteFiles);
            deleteLocalFiles(syncResult, localFiles);
            // any failed deletions?
            int failed = 0;
            TransferInfo localDeleteTransferInfo = syncResult.getLocalDeleteTransferInfo();
            failed += localDeleteTransferInfo.getFailed().size() + localDeleteTransferInfo.getPartiallyFailed().size();
            TransferInfo remoteDeleteTransferInfo = syncResult.getRemoteDeleteTransferInfo();
            failed += remoteDeleteTransferInfo.getFailed().size() + remoteDeleteTransferInfo.getPartiallyFailed().size();
            progressPanel.setDeleteNumber(failed);
        }

        private void deleteRemoteFiles(SyncResult syncResult, Set<TransferFile> remoteFiles) {
            if (remoteFiles.isEmpty()) {
                return;
            }
            try {
                TransferInfo deleteInfo = remoteClient.delete(remoteFiles);
                if (!mergeTransferInfo(deleteInfo, syncResult.getRemoteDeleteTransferInfo())) {
                    progressPanel.deleteErrorOccured();
                }
            } catch (RemoteException ex) {
                // should not happen, can be 'ignored', we are simply not connected
                LOGGER.log(Level.INFO, null, ex);
                for (TransferFile transferFile : remoteFiles) {
                    syncResult.getRemoteDeleteTransferInfo().addFailed(transferFile, ex.getLocalizedMessage());
                    break;
                }
                progressPanel.deleteErrorOccured();
            }
        }

        @NbBundle.Messages({
            "SyncController.error.deleteLocalFile=Cannot delete local file.",
            "SyncController.error.localFolderNotEmpty=Cannot delete local folder because it is not empty."
        })
        private void deleteLocalFiles(SyncResult syncResult, Set<TransferFile> localFiles) {
            // first, delete files
            long start = System.currentTimeMillis();
            TransferInfo deleteInfo = syncResult.getLocalDeleteTransferInfo();
            try {
                Set<TransferFile> folders = new TreeSet<>(new Comparator<TransferFile>() {
                    @Override
                    public int compare(TransferFile file1, TransferFile file2) {
                        // longest paths first to be able to delete directories properly
                        int cmp = StringUtils.explode(file2.getLocalPath(), File.separator).size() - StringUtils.explode(file1.getLocalPath(), File.separator).size();
                        return cmp != 0 ? cmp : 1;
                    }

                });
                // first, delete files
                for (TransferFile transferFile : localFiles) {
                    File localFile = transferFile.resolveLocalFile();
                    if (localFile.isDirectory()) {
                        folders.add(transferFile);
                    } else if (localFile.isFile()) {
                        if (localFile.delete()) {
                            deleteInfo.addTransfered(transferFile);
                        } else {
                            deleteInfo.addFailed(transferFile, Bundle.SyncController_error_deleteLocalFile());
                            progressPanel.deleteErrorOccured();
                        }
                    }
                }
                // now delete non-empty directories
                for (TransferFile folder : folders) {
                    File localDir = folder.resolveLocalFile();
                    String[] children = localDir.list();
                    if (children != null && children.length == 0) {
                        if (localDir.delete()) {
                            deleteInfo.addTransfered(folder);
                        } else {
                            deleteInfo.addFailed(folder, Bundle.SyncController_error_deleteLocalFile());
                            progressPanel.deleteErrorOccured();
                        }
                    } else {
                        deleteInfo.addIgnored(folder, Bundle.SyncController_error_localFolderNotEmpty());
                    }
                }
            } finally {
                deleteInfo.setRuntime(System.currentTimeMillis() - start);
            }
        }

    }

}
