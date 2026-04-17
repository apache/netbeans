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

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author christian lenz
 */
/**
 * Action to rename an existing branch.
 */
@ActionID(id = "org.netbeans.modules.git.ui.branch.RenameBranchAction", category = "Git")
@ActionRegistration(displayName = "#LBL_RenameBranchAction_Name")
public class RenameBranchAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(RenameBranchAction.class.getName());

    @Override
    protected void performAction(File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        GitBranch activeBranch = info.getActiveBranch();
        if (activeBranch != null && !GitBranch.NO_BRANCH.equals(activeBranch.getName())) {
            renameBranch(repository, activeBranch.getName());
        }
    }

    public void renameBranch(final File repository, final String oldName) {
        NotifyDescriptor.InputLine nd = new NotifyDescriptor.InputLine(NbBundle.getMessage(RenameBranchAction.class, "LBL_RenameBranchAction.prompt"), NbBundle.getMessage(RenameBranchAction.class, "LBL_RenameBranchAction.title"));
        nd.setInputText(oldName);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            final String newName = nd.getInputText().trim();
            if (newName.isEmpty() || newName.equals(oldName)) {
                return;
            }
            GitProgressSupport supp = new GitProgressSupport() {
                @Override
                protected void perform() {
                    try {
                        GitClient client = getClient();
                        LOG.log(Level.FINE, "Renaming branch: {0} -> {1}", new Object[]{oldName, newName});
                        client.renameBranch(oldName, newName, getProgressMonitor());
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                }
            };
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(RenameBranchAction.class, "LBL_RenameBranchAction.progressName"));
        }
    }
}
