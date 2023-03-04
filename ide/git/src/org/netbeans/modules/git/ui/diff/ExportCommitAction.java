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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.ExportDiffSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.diff.ExportCommitAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ExportCommitAction_Name")
public class ExportCommitAction extends SingleRepositoryAction {

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        exportCommit(repository, GitUtils.HEAD);
    }
    
    public void exportCommit (final File repository, final String preselectedRevision) {
        Mutex.EVENT.readAccess(new Runnable () {
            @Override
            public void run () {
                ExportDiffSupport exportDiffSupport = new ExportCommit(repository, preselectedRevision) {
                    @Override
                    public void writeDiffFile (final File toFile) {
                        final String revStr = getSelectionRevision();
                        saveFolderToPrefs(toFile);
                        GitProgressSupport support = new GitProgressSupport() {
                            @Override
                            public void perform() {
                                boolean success = false;
                                    OutputStream out = null;
                                    try {
                                        GitClient client = getClient();
                                        ensureParentExists(toFile);
                                        out = new BufferedOutputStream(new FileOutputStream(toFile));
                                        client.addNotificationListener(new DefaultFileListener(new File[0]));
                                        setProgress(NbBundle.getMessage(ExportUncommittedChangesAction.class, "MSG_ExportCommitAction.preparingDiff")); //NOI18N
                                        client.exportCommit(revStr, out, getProgressMonitor());
                                        if (!isCanceled()) {
                                            success = true;
                                        }
                                    } catch (Exception ex) {
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
                        support.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ExportCommitAction.class, "LBL_ExportCommitAction_Progress")).waitFinished(); //NOI18N
                    }
                };
                exportDiffSupport.export();
            }
        });
    }
    
    private static void saveFolderToPrefs (final File file) {
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
}
