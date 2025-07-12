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
package org.netbeans.modules.git.ui.branch;

import org.netbeans.libs.git.GitException.NotMergedException;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.history.BranchSelector;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
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
@ActionID(id = "org.netbeans.modules.git.ui.branch.DeleteBranchAction", category = "Git")
@ActionRegistration(displayName = "#LBL_DeleteBranchAction_Name")
public class DeleteBranchAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(DeleteBranchAction.class.getName());

    @Override
    protected void performAction(File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        HashMap<String, GitBranch> branches = new HashMap<>(info.getBranches());
        branches.remove(info.getActiveBranch().getName());

        if (branches.isEmpty()) {
            return;
        }

        BranchSelector selector = new BranchSelector(repository, branches);
        if (selector.open()) {
            deleteBranch(repository, selector.getSelectedBranch());
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes)) {
            return false;
        }

        // require 2+ branches
        Map.Entry<File, File[]> actionRoots = getActionRoots(getCurrentContext(activatedNodes));
        if (actionRoots != null) {
            RepositoryInfo info = RepositoryInfo.getInstance(actionRoots.getKey());

            return info != null && info.getBranches().size() > 1;
        }

        return false;
    }

    public void deleteBranch(final File repository, final String branchName) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteBranchAction.class, "MSG_DeleteBranchAction.confirmation", branchName), //NOI18N
            NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction.confirmation"), //NOI18N
            NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE);

        if (NotifyDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(nd)) {
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform() {
                    try {
                        GitClient client = getClient();
                        boolean forceDelete = false;
                        boolean cont;
                        do {
                            try {
                                LOG.log(Level.FINE, "Deleting a branch: {0}/{1}", new Object[]{branchName, forceDelete}); //NOI18N
                                client.deleteBranch(branchName, forceDelete, getProgressMonitor());
                                cont = false;
                            } catch (GitException.NotMergedException ex) {
                                cont = forceDelete = handleException(ex);
                            }
                        } while (cont);
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }

                private boolean handleException(NotMergedException ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(DeleteBranchAction.class, "MSG_DeleteBranchAction.notMerged", ex.getUnmergedRevision()), //NOI18N
                        NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction.notMerged"), //NOI18N
                        NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE);
                    return NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd);
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(DeleteBranchAction.class, "LBL_DeleteBranchAction.progressName", branchName)); //NOI18N
        }
    }
}
