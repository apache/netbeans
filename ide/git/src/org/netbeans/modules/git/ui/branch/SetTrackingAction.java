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

package org.netbeans.modules.git.ui.branch;

import org.netbeans.modules.git.client.GitClientExceptionHandler;
import java.io.File;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.branch.SetTrackingAction", category = "Git")
@ActionRegistration(displayName = "#LBL_SetTrackingAction_Name", lazy = true)
@NbBundle.Messages("LBL_SetTrackingAction_Name=Set Tra&cked Branch...")
public class SetTrackingAction extends SingleRepositoryAction {

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        GitBranch activeBranch = info.getActiveBranch();
        if (activeBranch.getName() != GitBranch.NO_BRANCH) {
            setupTrackedBranch(repository, activeBranch.getName(),
                    activeBranch.getTrackedBranch() == null ? null : activeBranch.getTrackedBranch().getName());
        }
    }

    public void setupTrackedBranch (final File repository, final String branchName, String currentTrackedBranch) {
        SelectTrackedBranch selectBranch = new SelectTrackedBranch(repository, branchName, currentTrackedBranch);
        if (selectBranch.open()) {
            setupTrackedBranchImmediately(repository, branchName, selectBranch.getSelectedBranch());
        }
    }
    
    @NbBundle.Messages({
        "LBL_SetTrackingAction.progressName=Setting Tracked Branch",
        "# {0} - branch name", "# {1} - tracked branch name",
        "MSG_SetTrackingAction.result=Branch \"{0}\" marked to track branch \"{1}\""
    })
    public void setupTrackedBranchImmediately (final File repository, final String branchName, final String targetBranch) {
        GitProgressSupport supp = new GitProgressSupport() {
            @Override
            protected void perform () {
                try {
                    GitClient client = getClient();
                    client.updateTracking(branchName, targetBranch, getProgressMonitor());
                    log(branchName, targetBranch);
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
            }

            private void log (String branchName, String targetBranchName) {
                OutputLogger logger = getLogger();
                logger.outputLine(Bundle.MSG_SetTrackingAction_result(branchName, targetBranchName));
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_SetTrackingAction_progressName());
    }

}
