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

package org.netbeans.modules.git.ui.commit;

import java.awt.EventQueue;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitUser;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.commit.GitCommitPanel.GitCommitPanelMerged;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.branch.CherryPickAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.git.utils.JGitUtils;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.GitHookContext.LogEntry;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.VCSCommitFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.commit.CommitAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CommitAction_Name")
@NbBundle.Messages({
    "LBL_CommitAction_Name=Co&mmit..."
})
public class CommitAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CommitAction.class.getName());
    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/commit.png"; //NOI18N
    
    public CommitAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    @NbBundle.Messages({
        "# {0} - counted context", "# {1} - current branch", "CTL_CommitPanel.title={0} - {1}"
    })
    protected void performAction (final File repository, final File[] roots, final VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refresh();
        if (!canCommit(repository, info)) {
            return;
        }
        final GitRepositoryState state = info.getRepositoryState();
        final GitUser user = identifyUser(repository);
        final String mergeCommitMessage = getMergeCommitMessage(repository, state);
        final String title = Bundle.CTL_CommitPanel_title(Utils.getContextDisplayName(context), info.getActiveBranch().getName());
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                GitCommitPanel panel = state == GitRepositoryState.MERGING_RESOLVED || state == GitRepositoryState.CHERRY_PICKING_RESOLVED
                        ? GitCommitPanelMerged.create(roots, repository, user, mergeCommitMessage)
                        : GitCommitPanel.create(roots, repository, user, isFromGitView(context));
                VCSCommitTable<GitLocalFileNode> table = panel.getCommitTable();
                boolean ok = panel.open(context, new HelpCtx("org.netbeans.modules.git.ui.commit.CommitAction"), title); //NOI18N

                if (ok) {
                    final List<GitLocalFileNode> commitFiles = table.getCommitFiles();

                    GitModuleConfig.getDefault().setLastCanceledCommitMessage(""); //NOI18N            
                    panel.getParameters().storeCommitMessage();

                    final VCSCommitFilter selectedFilter = panel.getSelectedFilter();
                    RequestProcessor rp = Git.getInstance().getRequestProcessor(repository);
                    GitProgressSupport support = new CommitProgressSupport(panel, commitFiles, selectedFilter, state);
                    support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
                } else if (!panel.getParameters().getCommitMessage().isEmpty()) {
                    GitModuleConfig.getDefault().setLastCanceledCommitMessage(panel.getParameters().getCommitMessage());
                }
            }
        });
    }

    private GitUser identifyUser (File repository) {
        GitUser user = null;
        GitClient client = null;
        try {
            client = Git.getInstance().getClient(repository);
            user = client.getUser();
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        } finally {
            if (client != null) {
                client.release();
            }
        }
        return user;
    }

    private String getMergeCommitMessage (File repository, GitRepositoryState state) {
        String message = null;
        if (EnumSet.of(GitRepositoryState.MERGING, GitRepositoryState.MERGING_RESOLVED,
                GitRepositoryState.CHERRY_PICKING, GitRepositoryState.CHERRY_PICKING_RESOLVED).contains(state)) {
            File f = new File(GitUtils.getGitFolderForRoot(repository), "MERGE_MSG"); //NOI18N
            try {
                message = new String(FileUtils.getFileContentsAsByteArray(f), StandardCharsets.UTF_8);
            } catch (IOException ex) {
                LOG.log(Level.FINE, null, ex);
            }
        }
        return message;
    }

    private static class CommitProgressSupport extends GitProgressSupport {
        private final GitCommitPanel panel;
        private final List<GitLocalFileNode> commitFiles;
        private final VCSCommitFilter selectedFilter;
        private final GitRepositoryState state;

        private CommitProgressSupport (GitCommitPanel panel, List<GitLocalFileNode> commitFiles, VCSCommitFilter selectedFilter, GitRepositoryState state) {
            this.panel = panel;
            this.commitFiles = commitFiles;
            this.selectedFilter = selectedFilter;
            this.state = state;
        }

        @Override
        public void perform() {
            try {
                List<File> addCandidates = new LinkedList<File>();
                List<File> deleteCandidates = new LinkedList<File>();
                Set<File> commitCandidates = new LinkedHashSet<File>();
                GitCommitParameters parameters = panel.getParameters();
                GitClient client = getClient();

                populateCandidates(addCandidates, deleteCandidates, commitCandidates);

                if (isCanceled()) {
                    return;
                }

                String message = parameters.getCommitMessage();
                GitUser author = parameters.getAuthor();
                GitUser commiter = parameters.getCommiter();
                boolean amend = parameters.isAmend();
                
                Collection<GitHook> hooks = panel.getHooks();
                try {

                    outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_TITLE")); // NOI18N
                    outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_TITLE_SEP")); // NOI18N

                    if(addCandidates.size() > 0) {
                        client.add(addCandidates.toArray(new File[0]), getProgressMonitor());
                    }
                    if(deleteCandidates.size() > 0) {
                        client.remove(deleteCandidates.toArray(new File[0]), false, getProgressMonitor());
                    }

                    if(GitModuleConfig.getDefault().getSignOff() && commiter != null) {
                        message += "\nSigned-off-by:" + GitCommitParameters.getUserString(commiter); // NOI18N
                    }
                    String origMessage = message;
                    message = beforeCommitHook(commitCandidates, hooks, message);

                    GitRepositoryState prevState = RepositoryInfo.getInstance(getRepositoryRoot()).getRepositoryState();
                    GitRevisionInfo info = commit(commitCandidates, message, author, commiter, amend);

                    GitModuleConfig.getDefault().putRecentCommitAuthors(GitCommitParameters.getUserString(author));
                    GitModuleConfig.getDefault().putRecentCommiter(GitCommitParameters.getUserString(commiter));

                    try {
                        commitCandidates.addAll(info.getModifiedFiles().keySet());
                    } catch (GitException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                    afterCommitHook(commitCandidates, hooks, info, origMessage);
                    if (prevState == GitRepositoryState.CHERRY_PICKING_RESOLVED) {
                        // should continue with cherry-picking
                        SystemAction.get(CherryPickAction.class).finish(getRepositoryRoot());
                    }

                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    refreshFS(commitCandidates);
                    Git.getInstance().getFileStatusCache().refreshAllRoots(commitCandidates);
                    outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_DONE")); // NOI18N
                    output(""); // NOI18N
                    Git.getInstance().getHistoryProvider().fireHistoryChange(commitCandidates.toArray(new File[0]));
                }
            } catch (GitException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
        
        private void populateCandidates (List<File> addCandidates, List<File> deleteCandidates, Collection<File> commitCandidates) {
            List<String> excPaths = new ArrayList<String>();
            List<String> incPaths = new ArrayList<String>();

            Iterator<GitLocalFileNode> it = commitFiles.iterator();
            while (it.hasNext()) {
                if (isCanceled()) {
                    return;
                }
                GitLocalFileNode node = (GitLocalFileNode) it.next();
                FileInformation info = node.getInformation();

                VCSCommitOptions option = node.getCommitOptions();
                File file = node.getFile();
                if (option != VCSCommitOptions.EXCLUDE) {
                    if (info.containsStatus(Status.NEW_INDEX_WORKING_TREE) 
                            || info.containsStatus(Status.MODIFIED_INDEX_WORKING_TREE) && selectedFilter == GitCommitPanel.FILTER_HEAD_VS_WORKING) {
                        addCandidates.add(file);
                    } else if (info.containsStatus(FileInformation.STATUS_REMOVED)) {
                        deleteCandidates.add(file);
                    }
                    commitCandidates.add(file);
                    incPaths.add(file.getAbsolutePath());
                } else {
                    excPaths.add(file.getAbsolutePath());
                }
            }

            if (!excPaths.isEmpty()) {
                GitModuleConfig.getDefault().addExclusionPaths(excPaths);
            }
            if (!incPaths.isEmpty()) {
                GitModuleConfig.getDefault().removeExclusionPaths(incPaths);
            }
        }

        private String beforeCommitHook (Collection<File> commitCandidates, Collection<GitHook> hooks, String message) {
            if(hooks.isEmpty()) {
                return message;
            }
            File[] hookFiles = commitCandidates.toArray(new File[0]);
            for (GitHook hook : hooks) {
                try {
                    GitHookContext context = new GitHookContext(hookFiles, message, new GitHookContext.LogEntry[] {});
                    context = hook.beforeCommit(context);

                    // XXX handle returned context - warning, ...
                    if(context != null && context.getMessage() != null && !context.getMessage().isEmpty()) {
                        // use message for next hook
                        message = context.getMessage();
                    }
                } catch (IOException ex) {
                    // XXX handle veto
                }
            }
            return message;
        }

        private void afterCommitHook(Collection<File> commitCandidates, Collection<GitHook> hooks, GitRevisionInfo info, String origMessage) {
            if(hooks.isEmpty()) {
                return;
            }
            File[] hookFiles = commitCandidates.toArray(new File[0]);
            LogEntry logEntry = new LogEntry(info.getFullMessage(),
                    info.getAuthor().toString(),
                    info.getRevision(),
                    new Date(info.getCommitTime()));

            GitHookContext context = new GitHookContext(hookFiles, origMessage, new LogEntry[] {logEntry});
            for (GitHook hook : hooks) {
                hook.afterCommit(context);
            }
        }

        private GitRevisionInfo commit (Collection<File> commitCandidates, String message, GitUser author, GitUser commiter, boolean amend) throws GitException {
            try {
                if (!JGitUtils.isUserSetup(getRepositoryRoot()) && askToPersistUser(author)) {
                    JGitUtils.persistUser(getRepositoryRoot(), author);
                }
                GitRevisionInfo info = getClient().commit(
                        state == GitRepositoryState.MERGING_RESOLVED || state == GitRepositoryState.CHERRY_PICKING_RESOLVED
                                ? new File[0] : commitCandidates.toArray(new File[0]),
                        message, author, commiter, amend, getProgressMonitor());
                printInfo(info);
                return info;
            } catch (GitException ex) {
                throw ex;
            }
        }

        private void printInfo (GitRevisionInfo info) {
            StringBuilder sb = new StringBuilder('\n');
            GitUtils.printInfo(sb, info);
            getLogger().outputLine(sb.toString());
        }

        @NbBundle.Messages({
            "LBL_CommitAction.askToPersistAuthor.title=Set Repository User",
            "# {0} - author",
            "MSG_CommitAction.askToPersistAuthor=Repository does not have fully specified user yet.\n\n"
                    + "Do you want to set {0} as the default author?"
        })
        private boolean askToPersistUser (GitUser author) {
            return DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(
                    Bundle.MSG_CommitAction_askToPersistAuthor(author.toString()),
                    Bundle.LBL_CommitAction_askToPersistAuthor_title(),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE
            )) == NotifyDescriptor.YES_OPTION;
        }
    }    

    private static void refreshFS (final Collection<File> filesToRefresh) {
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                FileUtil.refreshFor(filesToRefresh.toArray(new File[0]));
            }
        }, 100);
    }

    @NbBundle.Messages({
        "# {0} - repository folder name",
        "# translation hint - HEAD is a git specific term for last revisition in repository, do not translate this word",
        "MSG_CommitAction.detachedHeadState.warning=HEAD is detached in the repository {0}.\n"
            + "It is recommended to switch to a branch before committing your changes.\n\n"
            + "Do you still want to commit?",
        "# translation hint - HEAD is a git specific term for last revisition in repository, do not translate this word",
        "LBL_CommitAction.detachedHeadState.title=Detached HEAD State"
    })
    private boolean canCommit (File repository, RepositoryInfo info) {
        boolean commitPermitted = true;
        GitRepositoryState state = info.getRepositoryState();
        if (!state.canCommit()) {
            commitPermitted = false;
            Map<File, GitStatus> conflicts = Collections.emptyMap();
            if (state.equals(GitRepositoryState.MERGING) || state.equals(GitRepositoryState.CHERRY_PICKING)) {
                GitClient client = null;
                try {
                    client = Git.getInstance().getClient(repository);
                    conflicts = client.getConflicts(new File[] { repository }, GitUtils.NULL_PROGRESS_MONITOR);
                } catch (GitException ex) {
                    LOG.log(Level.INFO, null, ex);
                } finally {
                    if (client != null) {
                        client.release();
                    }
                }
            }
            NotifyDescriptor nd;
            if (conflicts.isEmpty()) {
                nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CommitNotAllowed_State", state.toString()), //NOI18N
                        NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CannotCommit"), NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.ERROR_MESSAGE); //NOI18N
            } else {
                nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CommitNotAllowed_Conflicts"), //NOI18N
                        NbBundle.getMessage(CommitAction.class, "LBL_CommitAction_CannotCommit"), NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE); //NOI18N
            }
            Object retval = DialogDisplayer.getDefault().notify(nd);
            if (retval == NotifyDescriptor.YES_OPTION) {
                GitUtils.openInVersioningView(conflicts.keySet(), repository, GitUtils.NULL_PROGRESS_MONITOR);
            }
        } else if (GitBranch.NO_BRANCH_INSTANCE != info.getActiveBranch() 
                && GitBranch.NO_BRANCH == info.getActiveBranch().getName()) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.MSG_CommitAction_detachedHeadState_warning(repository.getName()),
                    Bundle.LBL_CommitAction_detachedHeadState_title(),
                    NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.WARNING_MESSAGE);
            Object retval = DialogDisplayer.getDefault().notify(nd);
            commitPermitted = retval == NotifyDescriptor.YES_OPTION;
        }
        return commitPermitted;
    }

    protected boolean isFromGitView (VCSContext context) {
        return GitUtils.isFromInternalView(context);
    }

    public static class GitViewCommitAction extends CommitAction {
        @Override
        protected boolean isFromGitView(VCSContext context) {
            return true;
        }
    }
    
    @ActionID(id = "org.netbeans.modules.git.ui.commit.CommitProjectAction", category = "Git")
    @ActionRegistration(displayName = "#LBL_CommitProjectAction_Name", lazy = true)
    @NbBundle.Messages({
        "LBL_CommitProjectAction_Name=Commit Project..."
    })
    public static class CommitProjectAction extends GitAction {

        @Override
        protected final void performContextAction (final Node[] nodes) {
            Utils.postParallel(new Runnable () {
                @Override
                public void run() {
                    VCSContext context = getCurrentContext(nodes);
                    performAction(context);
                }
            }, 0);
        }

        private void performAction (VCSContext context) {
            Set<File> rootFiles = context.getRootFiles();
            Set<Project> projects = new HashSet<Project>();
            for (File root : rootFiles) {
                FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(root));
                if (fo != null) {
                    Project owner = FileOwnerQuery.getOwner(fo);
                    if (owner != null) {
                        projects.add(owner);
                    }
                }
            }
            if (projects.isEmpty()) {
                LOG.log(Level.FINE, "CommitProjectAction: no projects found for {0}", rootFiles); //NOI18N
            } else {
                List<Node> nodes = new ArrayList<Node>(projects.size());
                for (final Project p : projects) {
                    nodes.add(new AbstractNode(Children.LEAF, Lookups.fixed(p)) {

                        @Override
                        public String getName () {
                            return p.getProjectDirectory().getName();
                        }
                        
                    });
                }
                SystemAction.get(CommitAction.class).performAction(nodes.toArray(new Node[0]));
            }
        }
        
    }

}
