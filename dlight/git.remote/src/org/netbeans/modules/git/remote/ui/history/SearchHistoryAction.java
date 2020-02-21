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
import java.util.List;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.remote.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.history.SearchHistoryAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_SearchHistoryAction_Name")
public class SearchHistoryAction extends MultipleRepositoryAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/remote/resources/icons/search_history.png"; //NOI18N

    public SearchHistoryAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected Task performAction (final VCSFileProxy repository, final VCSFileProxy[] roots, final VCSContext context) {
        openSearch(repository, roots, VCSFileProxySupport.getContextDisplayName(context));
        return null;
    }
    
    public static void openSearch(final VCSFileProxy repository, final VCSFileProxy[] roots, final String contextName) {
        openSearch(repository, roots, getActiveBranchName(repository), contextName);
    }
    
    public static void openSearch (final VCSFileProxy repository, final VCSFileProxy[] roots, final String branchName, final String contextName) {
        final String title = NbBundle.getMessage(SearchHistoryTopComponent.class, "LBL_SearchHistoryTopComponent.title", contextName);
        final RepositoryInfo info = RepositoryInfo.getInstance(repository);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                SearchHistoryTopComponent tc = new SearchHistoryTopComponent(repository, info, roots);
                tc.setBranch(branchName);
                tc.setDisplayName(title);
                tc.open();
                tc.requestActive();
                if (roots != null && (roots.length == 1 && roots[0].isFile() || roots.length > 1 && VCSFileProxySupport.shareCommonDataObject(roots))) {
                    tc.search(false);
                }
            }
        });
    }
    
    public static void openSearch (final VCSFileProxy repository, final VCSFileProxy root, final String contextName,
            final String commitIdFrom, final String commitIdTo) {
        final String title = NbBundle.getMessage(SearchHistoryTopComponent.class, "LBL_SearchHistoryTopComponent.title", contextName);
        final RepositoryInfo info = RepositoryInfo.getInstance(repository);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run () {
                SearchHistoryTopComponent tc = new SearchHistoryTopComponent(repository, info, new VCSFileProxy[] { root });
                tc.setDisplayName(title);
                tc.open();
                tc.requestActive();
                tc.setSearchCommitFrom(commitIdFrom);
                tc.setSearchCommitTo(commitIdTo);
                tc.search(true);
            }
        });
    }
    
    public static void openSearch (final VCSFileProxy repository, final VCSFileProxy root, final String contextName, final int lineNumber) {
        final String title = NbBundle.getMessage(SearchHistoryTopComponent.class, "LBL_SearchHistoryTopComponent.title", contextName);
        final RepositoryInfo info = RepositoryInfo.getInstance(repository);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                SearchHistoryTopComponent tc = new SearchHistoryTopComponent(repository, info, root, new SearchHistoryTopComponent.DiffResultsViewFactory() {
                    @Override
                    DiffResultsView createDiffResultsView(SearchHistoryPanel panel, List<RepositoryRevision> results) {
                        return new DiffResultsViewForLine(panel, results, lineNumber);
                    }
                });
                tc.setDisplayName(title);
                tc.open();
                tc.requestActive();
                tc.search(true);
                tc.activateDiffView(true);
            }
        });
    }

    private static String getActiveBranchName (VCSFileProxy repository) {
        GitBranch activeBranch = RepositoryInfo.getInstance(repository).getActiveBranch();
        String branchName = null;
        if (activeBranch != GitBranch.NO_BRANCH_INSTANCE) {
            if (activeBranch.getName() == GitBranch.NO_BRANCH) {
                branchName = GitUtils.HEAD;
            } else {
                branchName = activeBranch.getName();
            }
        }
        return branchName;
    }

}
