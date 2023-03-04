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

import java.io.File;
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
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Upload files to remote connection.
 * @author Tomas Mysik
 */
public class UploadCommand extends RemoteCommand implements Displayable {
    public static final String ID = "upload"; // NOI18N
    public static final String DISPLAY_NAME = NbBundle.getMessage(UploadCommand.class, "LBL_UploadCommand");


    public UploadCommand(PhpProject project) {
        super(project);
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

        uploadFiles(selectedFiles, (FileObject[]) null);
    }

    public void uploadFiles(FileObject[] filesToUpload, FileObject[] preselectedFiles) {

        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(getProject());
        assert sources != null;

        if (!sourcesFilesOnly(sources, filesToUpload)) {
            return;
        }

        InputOutput remoteLog = getRemoteLog(RunConfigRemote.forProject(getProject()).getRemoteConfiguration().getDisplayName());
        RemoteClient remoteClient = getRemoteClient(remoteLog);

        Set<TransferFile> forUpload = prepareUpload(sources, filesToUpload, preselectedFiles, remoteClient);
        upload(forUpload, sources, filesToUpload, remoteLog, remoteClient);
    }

    private Set<TransferFile> prepareUpload(FileObject sources, FileObject[] filesToUpload, FileObject[] preselectedFiles, RemoteClient remoteClient) {
        Set<TransferFile> forUpload = Collections.emptySet();
        ProgressHandle progressHandle = ProgressHandle.createHandle(NbBundle.getMessage(UploadCommand.class, "MSG_UploadingFiles", getProject().getName()), remoteClient);
        try {
            progressHandle.start();
            forUpload = remoteClient.prepareUpload(sources, filesToUpload);

            RemoteUtils.fetchAllFiles(false, forUpload, sources, filesToUpload);

            // manage preselected files - it is just enough to touch the file
            if (preselectedFiles != null && preselectedFiles.length > 0) {
                File baseLocalDir = FileUtil.toFile(sources);
                String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
                for (FileObject fo : preselectedFiles) {
                    // we need to touch the _original_ transfer file because of its parent!
                    TransferFile transferFile = TransferFile.fromFileObject(remoteClient.createRemoteClientImplementation(baseLocalAbsolutePath), null, fo);
                    for (TransferFile file : forUpload) {
                        if (transferFile.equals(file)) {
                            file.touch();
                            break;
                        }
                    }
                }
            }

            boolean showDialog = true;
            if (forUpload.size() == 1
                    && forUpload.iterator().next().isFile()) {
                // do not show transfer dialog for exactly one file (not folder!)
                showDialog = false;
            }
            if (showDialog) {
                forUpload = TransferFilesChooser.forUpload(forUpload, RemoteUtils.getLastTimestamp(true, getProject())).showDialog();
            }
        } finally {
            progressHandle.finish();
        }
        return forUpload;
    }

    private void upload(Set<TransferFile> forUpload, FileObject sources, FileObject[] filesToUpload, InputOutput remoteLog, RemoteClient remoteClient) {
        TransferInfo transferInfo = null;
        try {
            if (forUpload.size() > 0) {
                final boolean askSync = !remoteClient.listFiles(getRemoteRoot(remoteClient, sources)).isEmpty();
                ProgressHandle progressHandle = ProgressHandle.createHandle(
                        NbBundle.getMessage(UploadCommand.class, "MSG_UploadingFiles", getProject().getName()), remoteClient);
                DefaultOperationMonitor uploadOperationMonitor = new DefaultOperationMonitor(progressHandle, forUpload);
                remoteClient.setOperationMonitor(uploadOperationMonitor);
                transferInfo = remoteClient.upload(forUpload);
                remoteClient.setOperationMonitor(null);
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(UploadCommand.class, "MSG_UploadFinished", getProject().getName()));
                if (isSourcesSelected(sources, filesToUpload)
                        && !remoteClient.isCancelled()
                        && transferInfo.hasAnyTransfered()) { // #153406
                    PhpProject project = getProject();
                    storeLastUpload(project);
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

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    private static void storeLastUpload(PhpProject project) {
        ProjectSettings.setLastUpload(project, TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

}
