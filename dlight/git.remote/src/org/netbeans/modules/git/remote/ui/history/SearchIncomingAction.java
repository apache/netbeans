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

package org.netbeans.modules.git.remote.ui.history;

import java.awt.EventQueue;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.history.SearchIncomingAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_SearchIncomingAction_Name")
@NbBundle.Messages({
    "LBL_SearchIncomingAction_Name=Show &Incoming",
    "LBL_SearchIncomingAction_PopupName=Show Incoming",
    "# {0} - context name", "# {1} - branch name", "LBL_SearchIncomingTopComponent.title=Show Incoming - {0}/{1}",
    "MSG_SearchIncomingTopComponent.err.noBranch=Search cannot be started because you are currently not on a branch."
})
public class SearchIncomingAction extends MultipleRepositoryAction {

    @Override
    protected Task performAction (final VCSFileProxy repository, final VCSFileProxy[] roots, final VCSContext context) {
        openSearch(repository, roots, VCSFileProxySupport.getContextDisplayName(context));
        return null;
    }
    
    public void openSearch(final VCSFileProxy repository, final VCSFileProxy[] roots, final String contextName) {
        String branchName = getActiveBranchName(repository);
        if (branchName.equals(GitBranch.NO_BRANCH)) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(Bundle.MSG_SearchIncomingTopComponent_err_noBranch(),
                NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        openSearch(repository, roots, branchName, contextName);
    }
    
    public void openSearch (final VCSFileProxy repository, final VCSFileProxy[] roots, final String branchName, final String contextName) {
        final String title = Bundle.LBL_SearchIncomingTopComponent_title(contextName, branchName);
        final RepositoryInfo info = RepositoryInfo.getInstance(repository);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                SearchIncomingTopComponent tc = new SearchIncomingTopComponent(repository, info, roots);
                tc.setBranch(branchName);
                tc.setDisplayName(title);
                tc.open();
                tc.requestActive();
                tc.search(true);
            }
        });
    }

    private static String getActiveBranchName (VCSFileProxy repository) {
        GitBranch activeBranch = RepositoryInfo.getInstance(repository).getActiveBranch();
        String branchName = GitBranch.NO_BRANCH;
        if (activeBranch != GitBranch.NO_BRANCH_INSTANCE) {
            branchName = activeBranch.getName();
        }
        return branchName;
    }

}
