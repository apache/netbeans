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

package org.netbeans.modules.git.ui.fetch;

import java.awt.EventQueue;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient.RebaseOperationType;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitMergeResult;
import org.netbeans.libs.git.GitRebaseResult;
import org.netbeans.libs.git.GitRemoteConfig;
import org.netbeans.libs.git.GitRepository;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitTransportUpdate;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.client.ProgressDelegate;
import org.netbeans.modules.git.ui.actions.ActionProgress;
import org.netbeans.modules.git.ui.actions.ActionProgress.DefaultActionProgress;
import org.netbeans.modules.git.ui.actions.ActionProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.merge.MergeRevisionAction;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.rebase.RebaseAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.fetch.PullAction", category = "Git")
@ActionRegistration(displayName = "#LBL_PullAction_Name")
@NbBundle.Messages({"#PullAction", "LBL_PullAction_Name=P&ull..."})
public class PullAction extends SingleRepositoryAction {
    
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/pull-setting.png"; //NOI18N
    
    public PullAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    private static final Logger LOG = Logger.getLogger(PullAction.class.getName());

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        pull(repository);
    }
    
    public void pull (final File repository, GitRemoteConfig remote, String branchToMerge) {
        if (remote.getUris().size() != 1) {
            Utils.post(new Runnable () {
                @Override
                public void run () {
                    pull(repository);
                }
            });
        } else {
            pull(repository, remote.getUris().get(0), remote.getFetchRefSpecs(), branchToMerge, null);
        }
    }
    
    private void pull (final File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        try {
            info.refreshRemotes();
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        }
        final Map<String, GitRemoteConfig> remotes = info.getRemotes();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                PullWizard wiz = new PullWizard(repository, remotes);
                if (wiz.show()) {
                    Utils.logVCSExternalRepository("GIT", wiz.getFetchUri()); //NOI18N
                    pull(repository, wiz.getFetchUri(), wiz.getFetchRefSpecs(), wiz.getBranchToMerge(), wiz.getRemoteToPersist());
                }
            }
        });
    }
    
    @NbBundle.Messages({
        "# {0} - repository name", "LBL_PullAction.progressName=Pulling - {0}",
        "MSG_PullAction.fetching=Fetching remote changes",
        "MSG_PullAction.merging=Merging remote changes",
        "MSG_PullAction.rebasing=Rebasing onto fetched head"
    })
    public ActionProgress pull (File repository, final String target, final List<String> fetchRefSpecs, final String branchToMerge, final String remoteNameToUpdate) {
        GitProgressSupport supp = new GitProgressSupportImpl(fetchRefSpecs, branchToMerge, target, remoteNameToUpdate);
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.LBL_PullAction_progressName(repository.getName()));
        return new DefaultActionProgress(supp);
    }

    private static class GitProgressSupportImpl extends GitProgressSupport {

        private final List<String> fetchRefSpecs;
        private final String branchToMerge;
        private final String target;
        private final String remoteNameToUpdate;

        public GitProgressSupportImpl (List<String> fetchRefSpecs, String branchToMerge, String target, String remoteNameToUpdate) {
            this.fetchRefSpecs = fetchRefSpecs;
            this.branchToMerge = branchToMerge;
            this.target = target;
            this.remoteNameToUpdate = remoteNameToUpdate;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - branch name", "MSG_PullAction.branchDeleted=Branch {0} deleted.",
            "MSG_PullAction.progress.syncBranches=Synchronizing tracking branches"
        })
        protected void perform () {
            final File repository = getRepositoryRoot();
            LOG.log(Level.FINE, "Pulling {0}/{1} from {2}", new Object[] { fetchRefSpecs, branchToMerge, target }); //NOI18N
            try {
                final GitClient client = getClient();
                final Set<String> toDelete = new HashSet<String>();
                for(ListIterator<String> it = fetchRefSpecs.listIterator(); it.hasNext(); ) {
                    String refSpec = it.next();
                    if (refSpec.startsWith(GitUtils.REF_SPEC_DEL_PREFIX)) {
                        // branches are deleted separately
                        it.remove();
                        toDelete.add(refSpec.substring(GitUtils.REF_SPEC_DEL_PREFIX.length()));
                    }
                }
                if (remoteNameToUpdate != null) {
                    GitRemoteConfig config = client.getRemote(remoteNameToUpdate, getProgressMonitor());
                    if (isCanceled()) {
                        return;
                    }
                    config = GitUtils.prepareConfig(config, remoteNameToUpdate, target, fetchRefSpecs);
                    client.setRemote(config, getProgressMonitor());
                    if (isCanceled()) {
                        return;
                    }
                }
                GitUtils.runWithoutIndexing(new Callable<Void>() {
                    @Override
                    public Void call () throws Exception {
                        for (String branch : toDelete) {
                            client.deleteBranch(branch, true, getProgressMonitor());
                            getLogger().outputLine(Bundle.MSG_PullAction_branchDeleted(branch));
                        }
                        setDisplayName(Bundle.MSG_PullAction_fetching());
                        Map<String, GitTransportUpdate> fetchResult = FetchAction.fetchRepeatedly(
                                client, getProgressMonitor(), target, fetchRefSpecs);
                        if (isCanceled()) {
                            return null;
                        }
                        FetchUtils.log(repository, fetchResult, getLogger());
                        if (!isCanceled()) {
                            setDisplayName(Bundle.MSG_PullAction_progress_syncBranches());
                            FetchUtils.syncTrackingBranches(repository, fetchResult, GitProgressSupportImpl.this, GitProgressSupportImpl.this.getProgress(), false);
                        }
                        if (isCanceled() || branchToMerge == null) {
                            return null;
                        }
                        new BranchSynchronizer(branchToMerge, repository, new BranchSynchronizer.GitProgressSupportDelegate() {

                            @Override
                            public GitClient getClient () throws GitException {
                                return client;
                            }

                            @Override
                            public OutputLogger getLogger () {
                                return GitProgressSupportImpl.this.getLogger();
                            }
                            
                            @Override
                            public ProgressDelegate getProgress () {
                                return GitProgressSupportImpl.this.getProgress();
                            }
                            
                        }).execute();
                        return null;
                    }
                }, repository);
            } catch (GitException ex) {
                setError(true);
                GitClientExceptionHandler.notifyException(ex, true);
            } finally {
                setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Git.getInstance().getSeenRoots(repository)));
                GitUtils.headChanged(repository);
            }
        }
    }
    
    public static final class BranchSynchronizer extends ActionProgressSupport {
        
        public static interface GitProgressSupportDelegate extends ActionProgressSupport.GitProgressSupportDelegate {

            public GitClient getClient () throws GitException;

            public OutputLogger getLogger ();
            
        }
        
        private final GitProgressSupportDelegate delegate;
        private final File repository;
        private final String branchToMerge;

        public BranchSynchronizer (String branchToMerge, File repository, GitProgressSupportDelegate delegate) {
            super(delegate);
            this.repository = repository;
            this.branchToMerge = branchToMerge;
            this.delegate = delegate;
        }
        
        protected Callable<ActionProgress> getNextAction () {
            Callable<ActionProgress> nextAction = null;
            try {
                GitClient client = delegate.getClient();
                String currentHeadId = null;
                String branchId = null;
                Map<String, GitBranch> branches = client.getBranches(true, GitUtils.NULL_PROGRESS_MONITOR);
                for (Map.Entry<String, GitBranch> e : branches.entrySet()) {
                    if (e.getValue().isActive()) {
                        currentHeadId = e.getValue().getId();
                    }
                    if (e.getKey().equals(branchToMerge)) {
                        branchId = e.getValue().getId();
                    }
                }
                if (branchId == null || currentHeadId == null) {
                    nextAction = new Merge(); // just for sure
                } else if (!branchId.equals(currentHeadId)) {
                    GitRevisionInfo info = client.getCommonAncestor(new String[] { currentHeadId, branchId }, GitUtils.NULL_PROGRESS_MONITOR);
                    if (info == null || !(info.getRevision().equals(branchId) || info.getRevision().equals(currentHeadId))) {
                        // ask
                        return askForNextAction();
                    } else if (info.getRevision().equals(currentHeadId)) {
                        // FF merge
                        nextAction = new Merge();
                    }                    
                }
            } catch (GitException ex) {
                LOG.log(Level.INFO, null, ex);
            }
            return nextAction;
        }

        @NbBundle.Messages({
            "# {0} - branch to merge",
            "MSG_PullAction_mergeNeeded_text=A merge commit is needed to synchronize current branch with {0}.\n\n"
                + "Do you want to Rebase the current branch onto {0} or Merge it with {0}?",
            "LBL_PullAction_mergeNeeded_title=Merge Commit Needed",
            "CTL_PullAction_mergeButton_text=&Merge",
            "CTL_PullAction_mergeButton_TTtext=Merge the two created heads",
            "CTL_PullAction_rebaseButton_text=&Rebase",
            "CTL_PullAction_rebaseButton_TTtext=Rebase current branch on top of the fetched branch"
        })
        private Callable<ActionProgress> askForNextAction () {
            JButton btnMerge = new JButton();
            Mnemonics.setLocalizedText(btnMerge, Bundle.CTL_PullAction_mergeButton_text());
            btnMerge.setToolTipText(Bundle.CTL_PullAction_mergeButton_TTtext());
            JButton btnRebase = new JButton();
            Mnemonics.setLocalizedText(btnRebase, Bundle.CTL_PullAction_rebaseButton_text());
            btnRebase.setToolTipText(Bundle.CTL_PullAction_rebaseButton_TTtext());
            Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    Bundle.MSG_PullAction_mergeNeeded_text(branchToMerge),
                    Bundle.LBL_PullAction_mergeNeeded_title(),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] { btnRebase, btnMerge, NotifyDescriptor.CANCEL_OPTION },
                    btnRebase));
            if (value == btnMerge) {
                return new Merge();
            } else if (value == btnRebase) {
                return new Rebase();
            }
            return null;
        }
        
        private class Merge implements Callable<ActionProgress> {

            @Override
            public ActionProgress call () throws GitException {
                GitClient client = delegate.getClient();
                delegate.getProgress().setDisplayName(Bundle.MSG_PullAction_merging());
                MergeRevisionAction.MergeContext ctx = new MergeRevisionAction.MergeContext(branchToMerge, null);
                MergeRevisionAction.MergeResultProcessor mrp = new MergeRevisionAction.MergeResultProcessor(client, repository, ctx, delegate.getLogger(), delegate.getProgress().getProgressMonitor());
                do {
                    ctx.setContinue(false);
                    GitRepository.FastForwardOption ffOption = GitRepository.FastForwardOption.FAST_FORWARD;
                    try {
                        GitMergeResult result = client.merge(branchToMerge, ffOption, delegate.getProgress().getProgressMonitor());
                        mrp.processResult(result);
                        if (result.getMergeStatus() == GitMergeResult.MergeStatus.ALREADY_UP_TO_DATE
                                || result.getMergeStatus() == GitMergeResult.MergeStatus.FAST_FORWARD
                                || result.getMergeStatus() == GitMergeResult.MergeStatus.MERGED) {
                            return new ActionProgress.ActionResult(false, false);
                        }
                    } catch (GitException.CheckoutConflictException ex) {
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Local modifications in WT during merge: {0} - {1}", new Object[] { repository, Arrays.asList(ex.getConflicts()) }); //NOI18N
                        }
                        ctx.setContinue(mrp.resolveLocalChanges(ex.getConflicts()));
                    }
                } while (ctx.isContinue() && !delegate.getProgress().isCanceled());
                return new ActionProgress.ActionResult(delegate.getProgress().isCanceled(), true);
            }

        }

        private class Rebase implements Callable<ActionProgress> {

            @Override
            public ActionProgress call () throws GitException  {
                delegate.getProgress().setDisplayName(Bundle.MSG_PullAction_rebasing());
                RebaseOperationType op = RebaseOperationType.BEGIN;
                GitClient client = delegate.getClient();
                String origHead = client.log(GitUtils.HEAD, delegate.getProgress().getProgressMonitor()).getRevision();
                RebaseAction.RebaseResultProcessor rrp = new RebaseAction.RebaseResultProcessor(client, repository,
                        branchToMerge, branchToMerge, origHead, delegate.getProgress(), delegate.getLogger());
                while (op != null && !delegate.getProgress().isCanceled()) {
                    GitRebaseResult result = client.rebase(op, branchToMerge, delegate.getProgress().getProgressMonitor());
                    rrp.processResult(result);
                    op = rrp.getNextAction();
                    if (op == null && (result.getRebaseStatus() == GitRebaseResult.RebaseStatus.FAST_FORWARD
                            || result.getRebaseStatus() == GitRebaseResult.RebaseStatus.NOTHING_TO_COMMIT
                            || result.getRebaseStatus() == GitRebaseResult.RebaseStatus.NOTHING_TO_COMMIT
                            || result.getRebaseStatus() == GitRebaseResult.RebaseStatus.OK
                            || result.getRebaseStatus() == GitRebaseResult.RebaseStatus.UP_TO_DATE)) {
                        return new ActionProgress.ActionResult(false, false);
                    }
                }
                return new ActionProgress.ActionResult(delegate.getProgress().isCanceled(), true);
            }
        }
    }
    
}
