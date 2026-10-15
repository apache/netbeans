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
package org.netbeans.modules.git.ui.rebase;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient.RebaseOperationType;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRebaseResult;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.client.ProgressDelegate;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsExecutor;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.git.utils.ResultProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.utils.LogUtils;
import org.netbeans.modules.versioning.hooks.GitHook;
import org.netbeans.modules.versioning.hooks.GitHookContext;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.FileUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.windows.WindowManager;

/**
 *
 * @author Ondrej Vrabec
 */
@ActionID(id = "org.netbeans.modules.git.ui.rebase.RebaseAction", category = "Git")
@ActionRegistration(displayName = "#LBL_RebaseAction_Name")
@NbBundle.Messages({
    "LBL_RebaseAction_Name=&Rebase...", "LBL_RebaseAction_PopupName=Rebase..."
})
public class RebaseAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(RebaseAction.class.getName());
    private static final String NETBEANS_REBASE_ORIGHEAD = "netbeans-rebase.orighead"; //NOI18N
    private static final String NETBEANS_REBASE_UPSTREAM = "netbeans-rebase.upstream"; //NOI18N
    private static final String NETBEANS_REBASE_ONTO = "netbeans-rebase.onto"; //NOI18N
    private static final String REBASE_MERGE_DIR = "rebase-merge"; //NOI18N
    private static final String REBASE_APPLY_DIR = "rebase-apply"; //NOI18N
    private static final String MESSAGE = "message"; //NOI18N

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refresh();
        rebase(repository, info);
    }

    @NbBundle.Messages({
        "# {0} - repository state", "MSG_RebaseAction_rebaseNotAllowed=Rebase is not allowed in this state: {0}",
        "CTL_RebaseAction.continueButton.text=C&ontinue",
        "CTL_RebaseAction.continueButton.TTtext=Continue the interrupted rebase",
        "CTL_RebaseAction.abortButton.text=Abo&rt",
        "CTL_RebaseAction.abortButton.TTtext=Abort the interrupted rebase",
        "CTL_RebaseAction.skipButton.text=&Skip",
        "CTL_RebaseAction.skipButton.TTtext=Skip the current commit and continue the interrupted rebase",
        "LBL_Rebase.rebasingState.title=Unfinished Rebase",
        "# {0} - repository name", "MSG_Rebase.rebasingState.text=Repository {0} is in the middle of an unfinished rebase.\n"
            + "Do you want to continue or abort the unfinished rebase\n"
            + "or skip the current commit from the rebase?"
    })
    private void rebase (File repository, RepositoryInfo info) {
        GitRepositoryState state = info.getRepositoryState();
        if (state == GitRepositoryState.SAFE) {
            GitBranch current = info.getActiveBranch();
            Rebase rebase = new Rebase(repository, info.getBranches(), current);
            if (rebase.showDialog()) {
                runRebase(repository, RebaseOperationType.BEGIN, rebase.getRevisionSource(), rebase.getRevisionBase(),
                        rebase.getRevisionDest());
            }
        } else if (state == GitRepositoryState.REBASING) {
            // abort or continue?
            JButton btnContinue = new JButton();
            Mnemonics.setLocalizedText(btnContinue, Bundle.CTL_RebaseAction_continueButton_text());
            btnContinue.setToolTipText(Bundle.CTL_RebaseAction_continueButton_TTtext());
            JButton btnAbort = new JButton();
            Mnemonics.setLocalizedText(btnAbort, Bundle.CTL_RebaseAction_abortButton_text());
            btnAbort.setToolTipText(Bundle.CTL_RebaseAction_abortButton_TTtext());
            JButton btnSkip = new JButton();
            Mnemonics.setLocalizedText(btnSkip, Bundle.CTL_RebaseAction_skipButton_text());
            btnSkip.setToolTipText(Bundle.CTL_RebaseAction_skipButton_TTtext());
            Map<Object, RebaseOperationType> operations = new HashMap<Object, RebaseOperationType>();
            operations.put(btnContinue, RebaseOperationType.CONTINUE);
            operations.put(btnSkip, RebaseOperationType.SKIP);
            operations.put(btnAbort, RebaseOperationType.ABORT);
            Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    Bundle.MSG_Rebase_rebasingState_text(repository.getName()),
                    Bundle.LBL_Rebase_rebasingState_title(),
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] { btnContinue, btnSkip, btnAbort, NotifyDescriptor.CANCEL_OPTION }, 
                    btnContinue));
            RebaseOperationType op = operations.get(value);
            if (op != null) {
                runRebase(repository, op, null, null, null);
            }
        } else {
            GitClientExceptionHandler.annotate(Bundle.MSG_RebaseAction_rebaseNotAllowed(state));
        }
    }

    @NbBundle.Messages("MSG_RebaseAction_progress=Rebasing...")
    private void runRebase (final File repository, final RebaseOperationType op, final String source, final String upstream,
    final String onto) {
        GitProgressSupport supp = new GitProgressSupport() {

            @Override
            protected void perform () {
                try {
                    final ProgressDelegate progress = getProgress();
                    GitUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            String lSource = source;
                            String lOnto = onto;
                            String lUpstream = upstream;
                            if (op == RebaseOperationType.BEGIN) {
                                
                            } else {
                                if (op != RebaseOperationType.ABORT && !checkRebaseFolder(repository)) {
                                    return null;
                                }
                                lSource = getRebaseFileContent(repository, NETBEANS_REBASE_ORIGHEAD);
                                lOnto = getOnto(repository);
                                lUpstream = getRebaseFileContent(repository, NETBEANS_REBASE_UPSTREAM);
                            }
                            GitClient client = getClient();
                            RebaseResultProcessor rrp = new RebaseResultProcessor(client, repository, lOnto, lUpstream, lSource,
                                    progress, getProgressSupport().getLogger());
                            RebaseOperationType nextAction = op;
                            while (nextAction != null && !isCanceled()) {
                                GitRebaseResult result = client.rebase(nextAction, lOnto, getProgressMonitor());
                                rrp.processResult(result);
                                nextAction = rrp.getNextAction();
                            }
                            return null;
                        }
                    });
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    setDisplayName(NbBundle.getMessage(GitAction.class, "LBL_Progress.RefreshingStatuses")); //NOI18N
                    Git.getInstance().getFileStatusCache().refreshAllRoots(Collections.<File, Collection<File>>singletonMap(repository, Git.getInstance().getSeenRoots(repository)));
                    GitUtils.headChanged(repository);
                }
            }

            private GitProgressSupport getProgressSupport () {
                return this;
            }
        };
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.MSG_RebaseAction_progress());
    }

    @NbBundle.Messages({
        "LBL_RebaseAction.continueExternalRebase.title=Cannot Continue Rebase",
        "MSG_RebaseAction.continueExternalRebase.text=Rebase was probably started by an external tool "
                + "in non-interactive mode. \nNetBeans IDE cannot continue and finish the action.\n\n"
                + "Please use the external tool or abort and restart rebase inside NetBeans."
    })
    private boolean checkRebaseFolder (File repository) {
        File folder = getRebaseFolder(repository);
        File messageFile = new File(folder, MESSAGE);
        if (messageFile.exists()) {
            return true;
        } else {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run () {
                    JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        Bundle.MSG_RebaseAction_continueExternalRebase_text(),
                        Bundle.LBL_RebaseAction_continueExternalRebase_title(),
                        JOptionPane.ERROR_MESSAGE);
                }
            });
            return false;
        }
    }

    @NbBundle.Messages({
        "MSG_RebaseAction.noMergeStrategies=Cannot continue rebase. It was probably started externally "
            + "with an unknown algorithm to the NetBeans IDE.\n"
            + "Please use the external tool you started rebase with to finish it."
    })
    private static String getRebaseFileContent (File repository, String filename) throws GitException {
        File rebaseFolder = getRebaseFolder(repository);
        if (rebaseFolder.exists()) {
            File file = new File(rebaseFolder, filename);
            if (file.canRead()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    return br.readLine();
                } catch (IOException ex) {
                    LOG.log(Level.FINE, null, ex);
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException ex) {}
                    }
                }
            }
        } else {
            throw new GitException(Bundle.MSG_RebaseAction_noMergeStrategies());
        }
        return null;
    }

    private static File getRebaseFolder (File repository) {
        File rebaseFolder = new File(GitUtils.getGitFolderForRoot(repository), REBASE_APPLY_DIR);
        if (!rebaseFolder.exists()) {
            rebaseFolder = new File(GitUtils.getGitFolderForRoot(repository), REBASE_MERGE_DIR);
        }
        return rebaseFolder;
    }

    private static String getOnto (File repository) throws GitException {
        String onto = getRebaseFileContent(repository, NETBEANS_REBASE_ONTO);
        if (onto == null) {
            onto = getRebaseFileContent(repository, "onto-name"); //NOI18N
        }
        if (onto == null) {
            onto = getRebaseFileContent(repository, "onto_name"); //NOI18N
        }
        if (onto == null) {
            onto = getRebaseFileContent(repository, "onto"); //NOI18N
        }
        return onto;
    }
    
    public static class RebaseResultProcessor extends ResultProcessor {

        private final OutputLogger logger;
        private final String onto;
        private final String origHead;
        private final String upstream;
        private RebaseOperationType nextAction;
        private final ProgressDelegate progress;
        
        public RebaseResultProcessor (GitClient client, File repository, String onto, String upstream, String origHead,
                ProgressDelegate progress, OutputLogger logger) {
            super(client, repository, onto, progress.getProgressMonitor());
            this.origHead = origHead;
            this.onto = onto;
            this.upstream = upstream;
            this.logger = logger;
            this.progress = progress;
        }
        
        @NbBundle.Messages({
            "# {0} - rebase status", "MSG_RebaseAction.result=Rebase Result: {0}\n",
            "# {0} - head commit id", "MSG_RebaseAction.result.aborted=Rebase aborted and the current HEAD reset to {0}\n",
            "MSG_RebaseAction.result.conflict=Rebase interrupted because of conflicts in:\n",
            "# {0} - head commit id", "MSG_RebaseAction.result.ok=Rebase successfully finished and HEAD now points to {0}:\n",
            "# {0} - rebase target revision", "MSG_RebaseAction.result.alreadyUpToDate=HEAD already in sync with {0}"
        })
        public void processResult (GitRebaseResult result) {
            nextAction = null;
            StringBuilder sb = new StringBuilder(Bundle.MSG_RebaseAction_result(result.getRebaseStatus().toString()));
            GitRevisionInfo info;
            String base = null;
            try {
                info = client.log(GitUtils.HEAD, GitUtils.NULL_PROGRESS_MONITOR);
                if (origHead != null && onto != null) {
                    GitRevisionInfo i = client.getCommonAncestor(new String[] { origHead, onto }, pm);
                    if (i != null) {
                        base = i.getRevision();
                    }
                }
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
                return;
            }
            persistNBConfig();
            boolean logActions = false;
            switch (result.getRebaseStatus()) {
                case ABORTED:
                    sb.append(Bundle.MSG_RebaseAction_result_aborted(info.getRevision()));
                    GitUtils.printInfo(sb, info);
                    break;
                case FAILED:
                case CONFLICTS:
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.log(Level.FINE, "Local modifications in WT during rebase: {0} - {1}", new Object[] { repository, result.getFailures() }); //NOI18N
                    }
                    try {
                        if (resolveLocalChanges(result.getFailures().toArray(new File[result.getFailures().size()]))) {
                            nextAction = RebaseOperationType.BEGIN;
                        }
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                    break;
                case NOTHING_TO_COMMIT:
                    nextAction = resolveNothingToCommit();
                    break;
                case FAST_FORWARD:
                case OK:
                    sb.append(Bundle.MSG_RebaseAction_result_ok(info.getRevision()));
                    GitUtils.printInfo(sb, info, false);
                    logActions = true;
                    updatePushHooks();
                    break;
                case STOPPED:
                    sb.append(Bundle.MSG_RebaseAction_result_conflict());
                    printConflicts(logger, sb, result.getConflicts());
                    nextAction = resolveRebaseConflicts(result.getConflicts());
                    break;
                case UP_TO_DATE:
                    sb.append(Bundle.MSG_RebaseAction_result_alreadyUpToDate(onto));
                    break;
            }
            if (sb.length() > 0) {
                logger.outputLine(sb.toString());
            }
            if (logActions) {
                logRebaseResult(info.getRevision(), base);
            }
        }

        public RebaseOperationType getNextAction () {
            return nextAction;
        }

        @NbBundle.Messages({
            "LBL_RebaseResultProcessor.abortButton.text=&Abort",
            "LBL_RebaseResultProcessor.abortButton.TTtext=Abort the interrupted rebase and reset back to the original commit.",
            "LBL_RebaseResultProcessor.resolveButton.text=&Resolve",
            "LBL_RebaseResultProcessor.resolveButton.TTtext=Files in conflict will be opened in a Resolve Conflict dialog.",
            "LBL_RebaseResultProcessor.resolveConflicts=Resolve Conflicts",
            "MSG_RebaseResultProcessor.resolveConflicts=Rebase produced unresolved conflicts.\n"
                + "You can resolve them manually or review them in the Versioning view\n"
                + "or completely abort the rebase.",
            "LBL_RebaseResultProcessor.revertButton.text=&Revert",
            "LBL_RebaseResultProcessor.revertButton.TTtext=Revert local changes to the state in the HEAD and removes unversioned files.",
            "LBL_RebaseResultProcessor.reviewButton.text=Re&view",
            "LBL_RebaseResultProcessor.reviewButton.TTtext=Opens the Versioning view and lists the conflicted files.",
            "MSG_Rebase.resolving=Resolving conflicts..."
        })
        private RebaseOperationType resolveRebaseConflicts (Collection<File> conflicts) {
            RebaseOperationType action = null;
            JButton abort = new JButton();
            Mnemonics.setLocalizedText(abort, Bundle.LBL_RebaseResultProcessor_abortButton_text());
            abort.setToolTipText(Bundle.LBL_RebaseResultProcessor_abortButton_TTtext());
            JButton resolve = new JButton();
            Mnemonics.setLocalizedText(resolve, Bundle.LBL_RebaseResultProcessor_resolveButton_text());
            resolve.setToolTipText(Bundle.LBL_RebaseResultProcessor_resolveButton_TTtext());
            JButton review = new JButton();
            Mnemonics.setLocalizedText(review, Bundle.LBL_RebaseResultProcessor_reviewButton_text());
            review.setToolTipText(Bundle.LBL_RebaseResultProcessor_reviewButton_TTtext());
            Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    Bundle.MSG_RebaseResultProcessor_resolveConflicts(),
                    Bundle.LBL_RebaseResultProcessor_resolveConflicts(),
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] { resolve, review, abort, NotifyDescriptor.CANCEL_OPTION }, resolve));
            if (o == review) {
                openInVersioningView(conflicts);
            } else if (o == resolve) {
                GitProgressSupport supp = new ResolveConflictsExecutor(conflicts.toArray(new File[0]));
                supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.MSG_Rebase_resolving());
            } else if (o == abort) {
                action = RebaseOperationType.ABORT;
            }
            return action;
        }

        @NbBundle.Messages({
            "LBL_RebaseResultProcessor.nothingToCommit=Nothing to Commit",
            "MSG_RebaseResultProcessor.nothingToCommit=No modifications to commit for the current rebase step.\n"
                + "Do you want to skip the commit from the rebase and continue?",
            "LBL_RebaseResultProcessor.skipButton.text=&Skip",
            "LBL_RebaseResultProcessor.skipButton.TTtext=Skip the commit and continue rebase."
        })
        private RebaseOperationType resolveNothingToCommit () {
            RebaseOperationType action = null;
            JButton abort = new JButton();
            Mnemonics.setLocalizedText(abort, Bundle.LBL_RebaseResultProcessor_abortButton_text());
            abort.setToolTipText(Bundle.LBL_RebaseResultProcessor_abortButton_TTtext());
            JButton skip = new JButton();
            Mnemonics.setLocalizedText(skip, Bundle.LBL_RebaseResultProcessor_skipButton_text());
            skip.setToolTipText(Bundle.LBL_RebaseResultProcessor_skipButton_TTtext());
            Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    Bundle.MSG_RebaseResultProcessor_nothingToCommit(),
                    Bundle.LBL_RebaseResultProcessor_nothingToCommit(),
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] { skip, abort, NotifyDescriptor.CANCEL_OPTION }, skip));
            if (o == skip) {
                action = RebaseOperationType.SKIP;
            } else if (o == abort) {
                action = RebaseOperationType.ABORT;
            }
            return action;
        }

        private void persistNBConfig () {
            if (onto != null && upstream != null && origHead != null) {
                File rebaseFolder = getRebaseFolder(repository);
                if (rebaseFolder.canWrite()) {
                    try {
                        FileUtils.copyStreamToFile(new ByteArrayInputStream(onto.getBytes()), new File(rebaseFolder, NETBEANS_REBASE_ONTO));
                        FileUtils.copyStreamToFile(new ByteArrayInputStream(upstream.getBytes()), new File(rebaseFolder, NETBEANS_REBASE_UPSTREAM));
                        FileUtils.copyStreamToFile(new ByteArrayInputStream(origHead.getBytes()), new File(rebaseFolder, NETBEANS_REBASE_ORIGHEAD));
                    } catch (IOException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        }

        private void logRebaseResult (String newHeadId, String base) {
            if (base != null && newHeadId != null) {
                String oldId = base;
                String newId = newHeadId;
                String branchName = RepositoryInfo.getInstance(repository).getActiveBranch().getName();
                LogUtils.logBranchUpdateReview(repository, branchName, oldId, newId, logger);
            }
        }
        
        @NbBundle.Messages("MSG_RebaseAction.updatingHooks=Updating push hooks")
        private void updatePushHooks () {
            if (onto != null && upstream != null && origHead != null) {
                Collection<GitHook> hooks = VCSHooks.getInstance().getHooks(GitHook.class);
                if (!hooks.isEmpty() && !pm.isCanceled()) {
                    progress.setProgress(Bundle.MSG_RebaseAction_updatingHooks());
                    try {
                        GitHookContext.LogEntry[] originalEntries = getEntries(client, upstream, origHead, pm);
                        if (pm.isCanceled()) {
                            return;
                        }
                        GitHookContext.LogEntry[] newEntries = getEntries(client, onto, GitUtils.HEAD, pm);
                        if (pm.isCanceled()) {
                            return;
                        }
                        Map<String, String> mapping = findChangesetMapping(originalEntries, newEntries);
                        for (GitHook gitHook : hooks) {
                            gitHook.afterCommitReplace(
                                    new GitHookContext(new File[] { repository }, null, originalEntries),
                                    new GitHookContext(new File[] { repository }, null, newEntries),
                                    mapping);
                        }
                    } catch (GitException ex) {
                        LOG.log(Level.INFO, null, ex);
                    }
                }
            }
        }
    }
    
    private static Map<String, String> findChangesetMapping (GitHookContext.LogEntry[] originalEntries, GitHookContext.LogEntry[] newEntries) {
        Map<String, String> mapping = new HashMap<String, String>(originalEntries.length);
        for (GitHookContext.LogEntry original : originalEntries) {
            boolean found = false;
            for (GitHookContext.LogEntry newEntry : newEntries) {
                if (original.getChangeset().equals(newEntry.getChangeset()) || (
                        original.getDate().equals(newEntry.getDate())
                        && original.getAuthor().equals(newEntry.getAuthor())
                        && original.getMessage().equals(newEntry.getMessage()))) {
                    // is it really the same commit???
                    mapping.put(original.getChangeset(), newEntry.getChangeset());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // delete ????
                mapping.put(original.getChangeset(), null);
            }
        }
        return mapping;
    }
    
    private static GitHookContext.LogEntry[] getEntries (GitClient client, String revisionFrom, String revisionTo,
            ProgressMonitor pm) throws GitException {
        SearchCriteria crit = new SearchCriteria();
        crit.setRevisionFrom(revisionFrom);
        crit.setRevisionTo(revisionTo);
        GitRevisionInfo[] log = client.log(crit, false, pm);
        return convertToEntries(log);
    }    

    private static GitHookContext.LogEntry[] convertToEntries (GitRevisionInfo[] messages) {
        List<GitHookContext.LogEntry> entries = new ArrayList<GitHookContext.LogEntry>(messages.length);
        for (GitRevisionInfo msg : messages) {
            entries.add(new GitHookContext.LogEntry(
                    msg.getFullMessage(),
                    msg.getAuthor().toString(),
                    msg.getRevision(),
                    new Date(msg.getCommitTime())));
        }
        return entries.toArray(new GitHookContext.LogEntry[0]);
    }
    
}
