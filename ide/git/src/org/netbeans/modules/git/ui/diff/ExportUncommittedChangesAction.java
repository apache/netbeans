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
package org.netbeans.modules.git.ui.diff;

import java.awt.EventQueue;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.libs.git.GitClient.DiffMode;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.diff.ExportUncommittedChangesAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ExportUncommittedChangesAction_Name")
public class ExportUncommittedChangesAction extends SingleRepositoryAction {

    @Override
    protected void performAction (final File repository, final File[] roots, VCSContext context) {
        exportDiff(repository, roots, DiffMode.HEAD_VS_WORKINGTREE);
    }

    void exportDiff (final Node[] selectedNodes, final DiffMode diffMode) {
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                VCSContext context = getCurrentContext(selectedNodes);
                Map.Entry<File, File[]> actionRoots = getActionRoots(context);
                if (actionRoots != null) {
                    exportDiff(actionRoots.getKey(), actionRoots.getValue(), diffMode);
                }
            }
        }, 0);
    }
    
    private void exportDiff (final File repository, final File[] roots, final DiffMode diffMode) {
        final File[] files;
        if (roots.length == 0 || (files = Git.getInstance().getFileStatusCache().listFiles(new HashSet<File>(Arrays.asList(roots)), diffModeToStatusSet(diffMode))).length == 0) {
            NotifyDescriptor msg = new NotifyDescriptor.Message(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportUncommittedChangesAction.emptyContext"), NotifyDescriptor.INFORMATION_MESSAGE); //NOI18N
            DialogDisplayer.getDefault().notify(msg);
            return;
        }
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                new ExportDiffSupport(roots, GitModuleConfig.getDefault().getPreferences()) {
                    @Override
                    public void writeDiffFile (final File toFile) {
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
                                    out = new BufferedOutputStream(new FileOutputStream(toFile));
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
                                    if (success && toFile.length() > 0) {
                                        Utils.openFile(toFile);
                                    } else {
                                        toFile.delete();
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
    
    static void saveFolderToPrefs (final File file) {
        if (file.getParent() != null) {
            GitModuleConfig.getDefault().getPreferences().put("ExportDiff.saveFolder", file.getParent()); //NOI18N
        }
    }

    private static void ensureParentExists(File destination) {
        File parent = destination.getParentFile();
        if (parent != null) {
            parent.mkdirs();
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
