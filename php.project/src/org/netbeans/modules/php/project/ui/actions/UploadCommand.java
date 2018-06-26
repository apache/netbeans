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
