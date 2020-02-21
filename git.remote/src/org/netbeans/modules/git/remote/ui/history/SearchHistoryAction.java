/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
