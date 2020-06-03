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

package org.netbeans.modules.git.remote.ui.branch;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.remote.ui.checkout.SwitchBranchAction;
import org.netbeans.modules.git.remote.ui.output.OutputLogger;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.branch.CreateBranchAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_CreateBranchAction_Name")
@NbBundle.Messages("LBL_CreateBranchAction_Name=Create &Branch...")
public class CreateBranchAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CreateBranchAction.class.getName());

    @Override
    protected void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        createBranch(repository, info.getActiveBranch().getName().equals(GitBranch.NO_BRANCH) ? GitUtils.HEAD : info.getActiveBranch().getName());
    }

    @NbBundle.Messages("LBL_CreateBranchAction.progressName=Create Branch")
    public void createBranch (final VCSFileProxy repository, String preselectedRevision) {
        final CreateBranch createBranch = new CreateBranch(repository, preselectedRevision, getBranches(repository));
        if (createBranch.show()) {
            if (createBranch.isCheckoutSelected()) {
                // create and switch to branch
                SystemAction.get(SwitchBranchAction.class).checkoutRevision(repository, createBranch.getRevision(), 
                        createBranch.getBranchName(), Bundle.LBL_CreateBranchAction_progressName());
            } else {
                GitProgressSupport supp = new GitProgressSupport() {
                    @Override
                    protected void perform () {
                        try {
                            GitClient client = getClient();
                            String revision = createBranch.getRevision();
                            LOG.log(Level.FINE, "Creating a branch: {0}", revision); //NOI18N
                            GitBranch branch = client.createBranch(createBranch.getBranchName(), createBranch.getRevision(), getProgressMonitor());
                            log(revision, branch);
                        } catch (GitException ex) {
                            GitClientExceptionHandler.notifyException(ex, true);
                        }
                    }

                    private void log (String revision, GitBranch branch) {
                        OutputLogger logger = getLogger();
                        logger.outputLine(NbBundle.getMessage(CreateBranchAction.class, "MSG_CreateBranchAction.branchCreated", new Object[] { branch.getName(), revision, branch.getId() })); //NOI18N
                    }
                };
                supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_CreateBranchAction_progressName());
            }
        }
    }

    private Map<String, GitBranch> getBranches (VCSFileProxy repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refresh();
        return info.getBranches();
    }

}
