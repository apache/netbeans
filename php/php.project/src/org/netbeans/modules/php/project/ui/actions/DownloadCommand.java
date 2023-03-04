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

package org.netbeans.modules.php.project.ui.actions;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.transfer.TransferInfo;
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooser;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Download files from remote connection.
 * @author Tomas Mysik
 */
public class DownloadCommand extends RemoteCommand implements Displayable {
    public static final String ID = "download"; // NOI18N
    public static final String DISPLAY_NAME = NbBundle.getMessage(DownloadCommand.class, "LBL_DownloadCommand");

    public DownloadCommand(PhpProject project) {
        super(project);
    }

    @Override
    public boolean saveRequired() {
        return false;
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    protected Runnable getContextRunnable(final Lookup context) {
        return new Runnable() {
            @Override
            public void run() {
                invokeActionImpl(context);
            }
        };
    }

    void invokeActionImpl(Lookup context) {
        FileObject[] selectedFiles = CommandUtils.filesForContextOrSelectedNodes(context);
        // #161202
        if (selectedFiles.length == 0) {
            // one selects project node e.g.
            return;
        }

        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(getProject());
        assert sources != null;
        if (!sourcesFilesOnly(sources, selectedFiles)) {
            return;
        }

        InputOutput remoteLog = getRemoteLog(RunConfigRemote.forProject(getProject()).getRemoteConfiguration().getDisplayName());
        RemoteClient remoteClient = getRemoteClient(remoteLog);
        String projectName = getProject().getName();
        download(remoteClient, remoteLog, projectName, true, sources, selectedFiles, null, getProject());
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public static void download(RemoteClient remoteClient, InputOutput remoteLog, String projectName,
            FileObject sources, Set<TransferFile> forDownload) {
        download(remoteClient, remoteLog, projectName, false, sources, null, forDownload, null);
    }

    private static void download(RemoteClient remoteClient, InputOutput remoteLog, String projectName,
            boolean showDownloadDialog, FileObject sources, FileObject[] filesToDownload, Set<TransferFile> transferFilesToDownload, PhpProject project) {

        Set<TransferFile> forDownload = prepareDownload(transferFilesToDownload, sources, filesToDownload, project, projectName, showDownloadDialog, remoteClient);
        download(forDownload, project, projectName, sources, filesToDownload, remoteLog, remoteClient);
    }

    private static Set<TransferFile> prepareDownload(Set<TransferFile> transferFilesToDownload, FileObject sources, FileObject[] filesToDownload,
            PhpProject project, String projectName, boolean showDownloadDialog, RemoteClient remoteClient) {
        Set<TransferFile> forDownload = Collections.emptySet();
        ProgressHandle progressHandle = ProgressHandle.createHandle(
                NbBundle.getMessage(DownloadCommand.class, "MSG_DownloadingFiles", projectName), remoteClient);
        try {
            progressHandle.start();
            forDownload = transferFilesToDownload != null ? transferFilesToDownload : remoteClient.prepareDownload(sources, filesToDownload);

            RemoteUtils.fetchAllFiles(true, forDownload, sources, filesToDownload);

            if (showDownloadDialog) {
                boolean reallyShowDialog = true;
                if (forDownload.size() == 1
                        && forDownload.iterator().next().isFile()) {
                    // do not show transfer dialog for exactly one file (not folder!)
                    reallyShowDialog = false;
                }

                if (reallyShowDialog) {
                    long timestamp = project != null ? RemoteUtils.getLastTimestamp(false, project) : -1;
                    forDownload = TransferFilesChooser.forDownload(forDownload, timestamp).showDialog();
                }
            }
        } catch (RemoteException ex) {
            RemoteUtils.processRemoteException(ex);
        } finally {
            progressHandle.finish();
        }
        return forDownload;
    }

    private static void download(Set<TransferFile> forDownload, PhpProject project, String projectName, FileObject sources, FileObject[] filesToDownload,
            InputOutput remoteLog, RemoteClient remoteClient) {
        TransferInfo transferInfo = null;
        try {
            if (forDownload.size() > 0) {
                final boolean askSync = hasAnyChildren(sources);
                ProgressHandle progressHandle = ProgressHandle.createHandle(
                        NbBundle.getMessage(DownloadCommand.class, "MSG_DownloadingFiles", projectName), remoteClient);
                DefaultOperationMonitor downloadOperationMonitor = new DefaultOperationMonitor(progressHandle, forDownload);
                remoteClient.setOperationMonitor(downloadOperationMonitor);
                transferInfo = remoteClient.download(forDownload);
                remoteClient.setOperationMonitor(null);
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(DownloadCommand.class, "MSG_DownloadFinished", projectName));
                if (project != null
                        && isSourcesSelected(sources, filesToDownload)
                        && !remoteClient.isCancelled()
                        && transferInfo.hasAnyTransfered()) { // #153406
                    storeLastDownload(project);
                    storeLastSync(project, remoteClient, sources, askSync);
                }
            }
        } catch (RemoteException ex) {
            RemoteUtils.processRemoteException(ex);
        } finally {
            try {
                remoteClient.disconnect(true);
            } catch (RemoteException ex) {
                RemoteUtils.processRemoteException(ex);
            }
            if (transferInfo != null) {
                processTransferInfo(transferInfo, remoteLog);
            }
        }
    }

    private static void storeLastDownload(PhpProject project) {
        ProjectSettings.setLastDownload(project, TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    private static boolean hasAnyChildren(FileObject file) {
        for (FileObject child : file.getChildren()) {
            if ("nbproject".equals(child.getNameExt())) { // NOI18N
                continue;
            }
            return true;
        }
        return false;
    }

}
