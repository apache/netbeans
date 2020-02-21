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
package org.netbeans.modules.git.remote.ui.diff;

import java.awt.EventQueue;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.git.remote.cli.GitClient.DiffMode;
import org.netbeans.modules.git.remote.FileInformation;
import org.netbeans.modules.git.remote.FileInformation.Status;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.GitModuleConfig;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.ui.output.OutputLogger;
import org.netbeans.modules.remotefs.versioning.api.ExportDiffSupport;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.diff.ExportUncommittedChangesAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_ExportUncommittedChangesAction_Name")
public class ExportUncommittedChangesAction extends SingleRepositoryAction {

    @Override
    protected void performAction (final VCSFileProxy repository, final VCSFileProxy[] roots, VCSContext context) {
        exportDiff(repository, roots, DiffMode.HEAD_VS_WORKINGTREE);
    }

    void exportDiff (final Node[] selectedNodes, final DiffMode diffMode) {
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                VCSContext context = getCurrentContext(selectedNodes);
                Map.Entry<VCSFileProxy, VCSFileProxy[]> actionRoots = getActionRoots(context);
                if (actionRoots != null) {
                    exportDiff(actionRoots.getKey(), actionRoots.getValue(), diffMode);
                }
            }
        }, 0);
    }
    
    private void exportDiff (final VCSFileProxy repository, final VCSFileProxy[] roots, final DiffMode diffMode) {
        final VCSFileProxy[] files;
        if (roots.length == 0 || (files = Git.getInstance().getFileStatusCache().listFiles(new HashSet<>(Arrays.asList(roots)), diffModeToStatusSet(diffMode))).length == 0) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.emptyContext"), NotifyDescriptor.INFORMATION_MESSAGE); //NOI18N
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                new ExportDiffSupport(roots, GitModuleConfig.getDefault().getPreferences()) {
                    @Override
                    public void writeDiffFile (final VCSFileProxy toFile) {
                        saveFolderToPrefs(toFile);
                        GitProgressSupport ps = new GitProgressSupport() {
                            @Override
                            protected void perform () {
                                boolean success = false;
                                OutputStream out = null;
                                OutputLogger logger = getLogger();
                                try {
                                    GitClient client = getClient();
                                    ensureParentExists(toFile);
                                    out = new BufferedOutputStream(VCSFileProxySupport.getOutputStream(toFile));
                                    client.addNotificationListener(new DefaultFileListener(roots));
                                    setProgress(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.preparingDiff")); //NOI18N
                                    client.exportDiff(files, diffMode, out, getProgressMonitor());
                                    if (!isCanceled()) {
                                        success = true;
                                    }
                                } catch (Exception ex) {
                                    logger.outputInRed(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.failed")); //NOI18N
                                    GitClientExceptionHandler.notifyException(ex, true);
                                } finally {
                                    if (out != null) {
                                        try {
                                            out.flush();
                                            out.close();
                                        } catch (IOException ex) { }
                                    }
                                    if (success && VCSFileProxySupport.length(toFile) > 0) {
                                        VCSFileProxySupport.openFile(toFile);
                                    } else {
                                        VCSFileProxySupport.delete(toFile);
                                    }
                                }
                            }
                        };
                        ps.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ExportUncommittedChangesAction.class, "LBL_ExportUncommittedChangesAction_Progress")).waitFinished();
                    }
                }.export();
            }
        });
    }
    
    static void saveFolderToPrefs (final VCSFileProxy file) {
        if (file.getParentFile()!= null) {
            GitModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", file.getParentFile().getPath()); //NOI18N
        }
    }

    private static void ensureParentExists(VCSFileProxy destination) {
        VCSFileProxy parent = destination.getParentFile();
        if (parent != null) {
            VCSFileProxySupport.mkdirs(parent);
        }
    }

    private EnumSet<Status> diffModeToStatusSet (DiffMode diffMode) {
        switch (diffMode) {
            case HEAD_VS_WORKINGTREE:
                return FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING;
            case HEAD_VS_INDEX:
                return FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX;
            case INDEX_VS_WORKINGTREE:
                return FileInformation.STATUS_MODIFIED_INDEX_VS_WORKING;
        }
        throw new IllegalArgumentException("Unknown diff mode " + diffMode);
    }
}
