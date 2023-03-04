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

package org.netbeans.modules.git.ui.history;

import java.awt.EventQueue;
import java.io.File;
import java.util.List;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.ui.actions.MultipleRepositoryAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.history.SearchHistoryAction", category = "Git")
@ActionRegistration(displayName = "#LBL_SearchHistoryAction_Name")
public class SearchHistoryAction extends MultipleRepositoryAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/search_history.png"; //NOI18N

    public SearchHistoryAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected Task performAction (final File repository, final File[] roots, final VCSContext context) {
        openSearch(repository, roots, Utils.getContextDisplayName(context));
        return null;
    }
    
    public static void openSearch(final File repository, final File[] roots, final String contextName) {
        openSearch(repository, roots, getActiveBranchName(repository), contextName);
    }
    
    public static void openSearch (final File repository, final File[] roots, final String branchName, final String contextName) {
        openSearch(repository, roots, branchName, contextName, roots != null && (roots.length == 1 || roots.length > 1 && Utils.shareCommonDataObject(roots)));
    }
    
    public static void openSearch (final File repository, final File[] roots, final String branchName, final String contextName, final boolean invokeSearch) {
        final String title = NbBundle.getMessage(SearchHistoryTopComponent.class, "LBL_SearchHistoryTopComponent.title", contextName);
        final RepositoryInfo info = RepositoryInfo.getInstance(repository);
        EventQueue.invokeLater(() -> {
            SearchHistoryTopComponent tc = new SearchHistoryTopComponent(repository, info, roots);
            tc.setBranch(branchName);
            tc.setDisplayName(title);
            tc.open();
            tc.requestActive();
            if (invokeSearch) {
                tc.search(false);
            }
        });
    }
    
    public static void openSearch (final File repository, final File root, final String contextName, final String commitIdFrom, final String commitIdTo) {
        final String title = NbBundle.getMessage(SearchHistoryTopComponent.class, "LBL_SearchHistoryTopComponent.title", contextName);
        final RepositoryInfo info = RepositoryInfo.getInstance(repository);
        Mutex.EVENT.readAccess(() -> {
            SearchHistoryTopComponent tc = new SearchHistoryTopComponent(repository, info, new File[] { root });
            tc.setDisplayName(title);
            tc.open();
            tc.requestActive();
            tc.setSearchCommitFrom(commitIdFrom);
            tc.setSearchCommitTo(commitIdTo);
            tc.search(true);
        });
    }
    
    public static void openSearch (final File repository, final File root, final String contextName, final int lineNumber) {
        final String title = NbBundle.getMessage(SearchHistoryTopComponent.class, "LBL_SearchHistoryTopComponent.title", contextName);
        final RepositoryInfo info = RepositoryInfo.getInstance(repository);
        EventQueue.invokeLater(() -> {
            SearchHistoryTopComponent tc = new SearchHistoryTopComponent(repository, info, root,
                    new SearchHistoryTopComponent.DiffResultsViewFactory() {
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
        });
    }

    private static String getActiveBranchName (File repository) {
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
