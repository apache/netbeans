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
import java.io.File;
import java.util.Collections;
import java.util.Map;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitException;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.fetch.FetchUtils;
import org.netbeans.modules.git.ui.history.RepositoryRevision;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Ondra Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.diff.DiffCurrentToRepositoryAction", category = "Git")
@ActionRegistration(displayName = "#LBL_DiffCurrentToRepositoryAction_Name")
@NbBundle.Messages({
    "LBL_DiffCurrentToRepositoryAction_Name=Diff To &Repository HEAD",
    "LBL_DiffCurrentToRepositoryAction_PopupName=Diff To Repository HEAD"
})
public class DiffCurrentToRepositoryAction extends GitAction {

    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return GitUtils.getRepositoryRoots(context).size() == 1;
    }

    @Override
    protected void performContextAction (Node[] nodes) {
        VCSContext context = getCurrentContext(nodes);
        diffToRepositoryHead(context);
    }

    @NbBundle.Messages({
        "LBL_DiffCurrentToRepositoryAction.progress=Checking remote repository",
        "# {0} - branch name", "CTL_RemoteRepository.HEAD=Remote {0} HEAD"
    })
    public void diffToRepositoryHead (final VCSContext context) {
        if (GitUtils.getRepositoryRoots(context).size() == 1) {
            final File repository = GitUtils.getRootFile(context);
            new GitProgressSupport() {

                @Override
                protected void perform () {
                    RepositoryInfo info = RepositoryInfo.getInstance(repository);
                    GitBranch branch = info.getActiveBranch();
                    try {
                        final Revision fetchedHead = FetchUtils.fetchToTemp(getClient(), getProgressMonitor(), branch);
                        EventQueue.invokeLater(new Runnable() {

                            @Override
                            public void run () {
                                if (fetchedHead == null) {
                                    SystemAction.get(DiffCurrentToTrackedAction.class).diffToTracked(context);
                                    return;
                                }
                                SystemAction.get(DiffAction.class).diff(context, new Revision.BranchReference(
                                        Bundle.CTL_RemoteRepository_HEAD(fetchedHead.getRevision()),
                                        fetchedHead.getCommitId()), Revision.HEAD);
                            }
                        });
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                        return;
                    }
                }
            }.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_DiffCurrentToRepositoryAction_progress());
            
        }
    }
}
