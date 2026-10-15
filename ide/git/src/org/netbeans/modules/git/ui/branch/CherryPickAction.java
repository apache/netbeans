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

import java.awt.EventQueue;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.libs.git.GitCherryPickResult;
import org.netbeans.libs.git.GitClient.CherryPickOperation;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRepositoryState;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.GitAction;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsExecutor;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.status.StatusAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.git.utils.ResultProcessor;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.branch.CherryPickAction", category = "Git")
@ActionRegistration(displayName = "#LBL_CherryPickAction_Name", lazy = false)
@NbBundle.Messages({
    "LBL_CherryPickAction_Name=C&herry Pick...",
    "LBL_CherryPickAction_PopupName=Cherry Pick...",
    "CTL_CherryPickAction_continueButton_text=&Continue",
    "CTL_CherryPickAction_continueButton_TTtext=Continue in cherry-picking scheduled commits.",
    "CTL_CherryPickAction_abortButton_text=&Abort",
    "CTL_CherryPickAction_abortButton_TTtext=Abort interrupted cherry-picking and rollback to the original commit.",
    "CTL_CherryPickAction_quitButton_text=&Quit",
    "CTL_CherryPickAction_quitButton_TTtext=Finish the currently cherry-picked commit but do not apply any other.",
    "LBL_CherryPick_cherryPickingState_title=Unfinished Cherry-Pick",
    "# {0} - repository name", "MSG_CherryPick_cherryPickingState_text=Repository {0} seems to be in the middle of an unfinished cherry-pick.\n\n"
            + "You may continue with applying all scheduled commits\n"
            + "or abort and rollback to the state before the cherry-pick started.",
    "# {0} - repository name", "MSG_CherryPick_cherryPickingScheduledState_text=Repository {0} seems to be in the middle of an unfinished cherry-pick.\n\n"
            + "You may continue with applying all scheduled commits,\n"
            + "abort and rollback to the state before the cherry-pick started\n"
            + "or quit the cherry-pick and leave the already applied commits.",
    "# {0} - repository state", "MSG_CherryPickAction_notAllowed=Cherry picking not allowed in this state: \"{0}\"."
})
public class CherryPickAction extends SingleRepositoryAction {

    private static final Logger LOG = Logger.getLogger(CherryPickAction.class.getName());
    
    public void cherryPick (File repository, String preselectedRevision) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refresh();
        GitRepositoryState state = info.getRepositoryState();
        boolean interrupted = isInterrupted(repository, state);
        if (state == GitRepositoryState.SAFE && !interrupted) {
            CherryPick cherryPick = new CherryPick(repository, preselectedRevision);
            if (cherryPick.showDialog()) {
                runCherryPick(repository, CherryPickOperation.BEGIN, new String[] { cherryPick.getRevision() });
            }
        } else if (interrupted) {
            // abort or continue?
            JButton btnContinue = new JButton();
            Mnemonics.setLocalizedText(btnContinue, Bundle.CTL_CherryPickAction_continueButton_text());
            btnContinue.setToolTipText(Bundle.CTL_CherryPickAction_continueButton_TTtext());
            JButton btnAbort = new JButton();
            Mnemonics.setLocalizedText(btnAbort, Bundle.CTL_CherryPickAction_abortButton_text());
            btnAbort.setToolTipText(Bundle.CTL_CherryPickAction_abortButton_TTtext());
            JButton btnQuit = new JButton();
            Mnemonics.setLocalizedText(btnQuit, Bundle.CTL_CherryPickAction_quitButton_text());
            btnQuit.setToolTipText(Bundle.CTL_CherryPickAction_quitButton_TTtext());
            Map<Object, CherryPickOperation> operations = new HashMap<>();
            operations.put(btnContinue, CherryPickOperation.CONTINUE);
            operations.put(btnQuit, CherryPickOperation.QUIT);
            operations.put(btnAbort, CherryPickOperation.ABORT);
            Object[] options = interrupted
                    ? new Object[] { btnContinue, btnAbort, btnQuit, NotifyDescriptor.CANCEL_OPTION }
                    : new Object[] { btnContinue, btnAbort, NotifyDescriptor.CANCEL_OPTION };
            Object value = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    interrupted
                            ? Bundle.MSG_CherryPick_cherryPickingScheduledState_text(repository.getName())
                            : Bundle.MSG_CherryPick_cherryPickingState_text(repository.getName()),
                    Bundle.LBL_CherryPick_cherryPickingState_title(),
                    NotifyDescriptor.YES_NO_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    options, 
                    btnContinue));
            CherryPickOperation op = operations.get(value);
            if (op != null) {
                runCherryPick(repository, op, null);
            }
        } else {
            GitClientExceptionHandler.annotate(Bundle.MSG_CherryPickAction_notAllowed(state));
        }
        
    }

    public void finish (File repository) {
        RepositoryInfo info = RepositoryInfo.getInstance(repository);
        info.refresh();
        if (isInterrupted(repository, info.getRepositoryState())) {
            cherryPick(repository, null);
        }
    }

    @Override
    protected void performAction (File repository, File[] roots, VCSContext context) {
        cherryPick(repository, null);
    }

    private boolean isInterrupted (File repository, GitRepositoryState state) {
        if (state == GitRepositoryState.CHERRY_PICKING || state == GitRepositoryState.CHERRY_PICKING_RESOLVED) {
            return true;
        }
        File sequencer = new File(GitUtils.getGitFolderForRoot(repository), "sequencer");
        String[] fileNames = sequencer.list();
        return fileNames != null && Arrays.asList(fileNames).contains("todo");
    }

    @NbBundle.Messages("MSG_CherryPickAction_progress=Cherry Picking...")
    private void runCherryPick (final File repository, final CherryPickOperation op, final String[] revisions) {
        GitProgressSupport supp = new GitProgressSupport() {

            @Override
            protected void perform () {
                try {
                    GitUtils.runWithoutIndexing(new Callable<Void>() {
                        @Override
                        public Void call () throws Exception {
                            GitClient client = getClient();
                            CherryPickResultProcessor rp = new CherryPickResultProcessor(client, repository, getProgressSupport());
                            CherryPickOperation nextAction = op;
                            while (nextAction != null && !isCanceled()) {
                                GitCherryPickResult result = client.cherryPick(nextAction, revisions, getProgressMonitor());
                                rp.processResult(result, nextAction);
                                nextAction = rp.getNextAction();
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
        supp.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.MSG_CherryPickAction_progress());
    }
    
    public static class CherryPickResultProcessor extends ResultProcessor {

        private final OutputLogger logger;
        private CherryPickOperation nextAction;
        private final GitProgressSupport supp;
        
        public CherryPickResultProcessor (GitClient client, File repository, GitProgressSupport supp) {
            super(client, repository, GitUtils.HEAD, supp.getProgressMonitor());
            this.logger = supp.getLogger();
            this.supp = supp;
        }
        
        @NbBundle.Messages({
            "# {0} - rebase status", "MSG_CherryPickAction.result=Cherry-Pick Result: {0}\n",
            "# {0} - head commit id", "MSG_CherryPickAction.result.aborted=Cherry-picking aborted and the current branch reset to {0}\n",
            "MSG_CherryPickAction.result.failed=Working tree modifications prevent from cherry-picking:\n",
            "MSG_CherryPickAction.result.conflict=Cherry-picking interrupted because of conflicts in:\n",
            "MSG_CherryPickAction.result.ok=Cherry-picking successfully finished\n"
        })
        public void processResult (GitCherryPickResult result, CherryPickOperation currentOp) {
            nextAction = null;
            StringBuilder sb = new StringBuilder(Bundle.MSG_CherryPickAction_result(result.getCherryPickStatus().toString()));
            GitRevisionInfo info = result.getCurrentHead();
            switch (result.getCherryPickStatus()) {
                case ABORTED:
                    sb.append(Bundle.MSG_CherryPickAction_result_aborted(info.getRevision()));
                    GitUtils.printInfo(sb, info);
                    break;
                case FAILED:
                    sb.append(Bundle.MSG_CherryPickAction_result_failed());
                    printConflicts(logger, sb, result.getFailures());
                    try {
                        if (resolveLocalChanges(result.getFailures().toArray(new File[result.getFailures().size()]))) {
                            nextAction = CherryPickOperation.CONTINUE;
                        } else if (currentOp == CherryPickOperation.BEGIN) {
                            nextAction = CherryPickOperation.QUIT;
                        }
                    } catch (GitException ex) {
                        GitClientExceptionHandler.notifyException(ex, true);
                    }
                    break;
                case CONFLICTING:
                    sb.append(Bundle.MSG_CherryPickAction_result_conflict());
                    printConflicts(logger, sb, result.getConflicts());
                    nextAction = resolveCherryPickConflicts(result.getConflicts());
                    break;
                case OK:
                    sb.append(Bundle.MSG_CherryPickAction_result_ok());
                    break;
                case UNCOMMITTED:
                    askForCommit();
                    break;
            }
            for (GitRevisionInfo commit : result.getCherryPickedCommits()) {
                GitUtils.printInfo(sb, commit);
            }
            if (sb.length() > 0) {
                logger.outputLine(sb.toString());
            }
        }

        public CherryPickOperation getNextAction () {
            return nextAction;
        }

        @NbBundle.Messages({
            "LBL_CherryPickResultProcessor.abortButton.text=&Abort",
            "LBL_CherryPickResultProcessor.abortButton.TTtext=Abort the interrupted process and reset back to the original commit.",
            "LBL_CherryPickResultProcessor.resolveButton.text=&Resolve",
            "LBL_CherryPickResultProcessor.resolveButton.TTtext=Files in conflict will be opened in the Resolve Conflict dialog.",
            "LBL_CherryPickResultProcessor.resolveConflicts=Resolve Conflicts",
            "MSG_CherryPickResultProcessor.resolveConflicts=Cherry-picking produced unresolved conflicts.\n"
                + "You can resolve them manually, review them in the Versioning view\n"
                + "or completely abort the process and reset back to the original state.",
            "LBL_CherryPickResultProcessor.revertButton.text=&Revert",
            "LBL_CherryPickResultProcessor.revertButton.TTtext=Revert local changes to the state in the HEAD and removes unversioned files.",
            "LBL_CherryPickResultProcessor.reviewButton.text=Re&view",
            "LBL_CherryPickResultProcessor.reviewButton.TTtext=Opens the Versioning view and lists the conflicted files.",
            "MSG_CherryPick.resolving=Resolving conflicts..."
        })
        private CherryPickOperation resolveCherryPickConflicts (Collection<File> conflicts) {
            CherryPickOperation action = null;
            JButton abort = new JButton();
            Mnemonics.setLocalizedText(abort, Bundle.LBL_CherryPickResultProcessor_abortButton_text());
            abort.setToolTipText(Bundle.LBL_CherryPickResultProcessor_abortButton_TTtext());
            JButton resolve = new JButton();
            Mnemonics.setLocalizedText(resolve, Bundle.LBL_CherryPickResultProcessor_resolveButton_text());
            resolve.setToolTipText(Bundle.LBL_CherryPickResultProcessor_resolveButton_TTtext());
            JButton review = new JButton();
            Mnemonics.setLocalizedText(review, Bundle.LBL_CherryPickResultProcessor_reviewButton_text());
            review.setToolTipText(Bundle.LBL_CherryPickResultProcessor_reviewButton_TTtext());
            Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    Bundle.MSG_CherryPickResultProcessor_resolveConflicts(),
                    Bundle.LBL_CherryPickResultProcessor_resolveConflicts(),
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] { resolve, review, abort, NotifyDescriptor.CANCEL_OPTION }, resolve));
            if (o == review) {
                openInVersioningView(conflicts);
            } else if (o == resolve) {
                GitProgressSupport executor = new ResolveConflictsExecutor(conflicts.toArray(new File[0]));
                executor.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.MSG_CherryPick_resolving());
            } else if (o == abort) {
                action = CherryPickOperation.ABORT;
            }
            return action;
        }

        @NbBundle.Messages({
            "LBL_CherryPickResultProcessor.commit=Commit Required",
            "MSG_CherryPickResultProcessor.commit=Commit changes were applied into the current branch\n"
                    + "but a manual commit is required to make these changes permanent.\n\n"
                + "Do you want to commit the changes now or review them first?",
            "LBL_CherryPickResultProcessor.commit.commitButton.text=&Commit",
            "LBL_CherryPickResultProcessor.commit.commitButton.TTtext=Opens the commit dialog.",
            "LBL_CherryPickResultProcessor.commit.reviewButton.text=&Review",
            "LBL_CherryPickResultProcessor.commit.reviewButton.TTtext=Review the changes in the status window."
        })
        private void askForCommit () {
            JButton commit = new JButton();
            Mnemonics.setLocalizedText(commit, Bundle.LBL_CherryPickResultProcessor_commit_commitButton_text());
            commit.setToolTipText(Bundle.LBL_CherryPickResultProcessor_commit_commitButton_TTtext());
            JButton review = new JButton();
            Mnemonics.setLocalizedText(review, Bundle.LBL_CherryPickResultProcessor_commit_reviewButton_text());
            review.setToolTipText(Bundle.LBL_CherryPickResultProcessor_commit_reviewButton_TTtext());
            Object o = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    Bundle.MSG_CherryPickResultProcessor_commit(),
                    Bundle.LBL_CherryPickResultProcessor_commit(),
                    NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                    new Object[] { commit, review, NotifyDescriptor.CANCEL_OPTION }, commit));
            final VCSContext context = GitUtils.getContextForFile(repository);
            if (o == commit) {
                SystemAction.get(CommitAction.class).performAction(context);
            } else if (o == review) {
                Mutex.EVENT.readAccess(new Runnable () {

                    @Override
                    public void run () {
                        SystemAction.get(StatusAction.class).performContextAction(context);
                    }
                    
                });
            }
        }
        
    }

}
