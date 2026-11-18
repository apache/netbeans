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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import javax.swing.JButton;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRebaseInteractiveHandler;
import org.netbeans.libs.git.GitRebaseTodoLine;
import org.netbeans.libs.git.GitRebaseTodoLine.RebaseAction;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.commit.GitCommitParameters;
import org.netbeans.modules.git.ui.commit.RewordCommitPanel;
import org.netbeans.modules.git.ui.repository.RevisionInfoPanelController;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * The dialog listing the commits to be rebased in an interactive rebase
 * started from a commit of the current branch: the user selects for each
 * commit whether it is picked, squashed, fixup-ed or dropped and can
 * reorder the commits to change the order the commits are applied in.
 * A double-click on a row opens the diff of the commit, against its
 * parent, as a diff tab in the main window.
 *
 * @author mbien
 */
public class InteractiveRebaseDialog {

    private final InteractiveRebasePanel panel;
    private final RevisionInfoPanelController infoPanelController;
    private final JButton okButton;
    private final JButton cancelButton;
    private final File repository;
    private final String baseRevision;
    private String error;

    @NbBundle.Messages({
        "CTL_InteractiveRebaseDialog_okButton.text=&Rebase",
        "CTL_InteractiveRebaseDialog_cancelButton.text=&Cancel",
        "CTL_InteractiveRebaseDialog_ACSD=Select the actions of the rebase steps",
        "TTT_InteractiveRebaseDialog_okButton_allDropped=At least one commit must be kept: every commit is currently set to drop.",
        "MSG_InteractiveRebaseDialog.rangeTooSmall=An interactive rebase requires at least two commits between "
            + "the selected commit and the head of the current branch.",
        "MSG_InteractiveRebaseDialog.mergeInRange=An interactive rebase cannot contain merge commits. "
            + "The series of commits between the selected commit and the head of the current branch "
            + "is not linear.",
        "MSG_InteractiveRebaseDialog.notOnBranch=The selected commit is not on the current branch: "
            + "the head of the current branch is not built on top of it. An interactive rebase "
            + "replays the commits of the current branch above the selected commit, it cannot "
            + "replay commits of another branch.",
        "CTL_InteractiveRebaseDialog_rewordTitle=Reword Commit",
        "CTL_InteractiveRebaseDialog_squashTitle=Squashed Commit Message"
    })
    public InteractiveRebaseDialog(File repository, String baseRevision) {
        this.repository = repository;
        this.baseRevision = baseRevision;
        panel = new InteractiveRebasePanel(repository);
        infoPanelController = new RevisionInfoPanelController(repository);
        panel.getInfoPanel().add(infoPanelController.getPanel(), BorderLayout.CENTER);
        panel.getTableModel().setRevisions(List.of());
        panel.getTable().getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = panel.getTable().getSelectedRow();
            if (row >= 0) {
                infoPanelController.loadInfo(panel.getTableModel().getRevision(row).getRevision());
            } else {
                infoPanelController.loadInfo(null);
            }
        });
        okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, Bundle.CTL_InteractiveRebaseDialog_okButton_text());
        okButton.setEnabled(false);
        cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, Bundle.CTL_InteractiveRebaseDialog_cancelButton_text());
        panel.getTableModel().addTableModelListener(e -> updateOkButton());
        loadRange(repository);
    }

    /**
     * The rebase button is only enabled while the plan keeps at least one
     * commit: a plan dropping every commit is a reset, not a rebase, and the
     * driver refuses it.
     */
    private void updateOkButton() {
        boolean kept = planHasKeptStep();
        okButton.setEnabled(kept);
        okButton.setToolTipText(kept ? null : Bundle.TTT_InteractiveRebaseDialog_okButton_allDropped());
    }

    /**
     * Whether the plan keeps at least one commit.
     */
    private boolean planHasKeptStep() {
        InteractiveRebasePanel.TodoTableModel model = panel.getTableModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getAction(i) != RebaseAction.DROP) {
                return true;
            }
        }
        return false;
    }

    private void loadRange(File repository) {
        try (GitClient client = Git.getInstance().getClient(repository)) {
            SearchCriteria criteria = new SearchCriteria();
            criteria.setRevisionFrom(baseRevision);
            criteria.setRevisionTo(GitUtils.HEAD);
            GitRevisionInfo[] log = client.log(criteria, false, new ProgressMonitor.DefaultProgressMonitor());
            Map<String, GitRevisionInfo> byId = HashMap.newHashMap(log.length);
            List<GitRevisionInfo> range = new ArrayList<>(log.length);
            for (GitRevisionInfo entry : log) {
                byId.put(entry.getRevision(), entry);
                if (!entry.getRevision().equals(baseRevision)) {
                    range.add(entry);
                }
            }
            if (range.size() < 2) {
                error = Bundle.MSG_InteractiveRebaseDialog_rangeTooSmall();
                return;
            }
            for (GitRevisionInfo entry : range) {
                if (entry.getParents().length > 1) {
                    error = Bundle.MSG_InteractiveRebaseDialog_mergeInRange();
                    return;
                }
            }
            // the commit order of the branch, like the todo file of git
            // rebase -i: walk the parent chain from the head down to the
            // base; sorting by commit time is not safe, a commit may
            // carry a time older than the time of its parent
            Set<String> referencedAsParent = new HashSet<>();
            for (GitRevisionInfo entry : range) {
                referencedAsParent.addAll(Arrays.asList(entry.getParents()));
            }
            GitRevisionInfo head = null;
            for (GitRevisionInfo entry : range) {
                if (!referencedAsParent.contains(entry.getRevision())) {
                    head = entry;
                    break;
                }
            }
            List<GitRevisionInfo> chain = new ArrayList<>(range.size());
            GitRevisionInfo cur = head;
            while (cur != null && !cur.getRevision().equals(baseRevision)) {
                chain.add(cur);
                cur = byId.get(cur.getParents()[0]);
            }
            if (cur == null) {
                // the walk fell off the chain before reaching the base: the
                // base is not on the current branch, rebasing would re-parent
                // the branch onto the base
                error = Bundle.MSG_InteractiveRebaseDialog_notOnBranch();
                return;
            }
            chain = chain.reversed(); // oldest first
            panel.getTableModel().setRevisions(chain);
            panel.getTable().setRowSelectionInterval(0, 0);
            infoPanelController.loadInfo(chain.getFirst().getRevision());
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
        }
    }

    /**
     * The handler to pass to the rebase command; applies the plan the user
     * settled in the dialog to the rebase: the actions and the order of the
     * steps, the messages settled for the reworded commits and the squashed
     * runs, and the persistence of the plan into the rebase folder for the
     * recovery path.
     */
    GitRebaseInteractiveHandler getHandler() {
        InteractiveRebasePanel.TodoTableModel model = panel.getTableModel();
        List<RebasePlan.Row> rows = new ArrayList<>(model.getRowCount());
        for (int i = 0; i < model.getRowCount(); i++) {
            GitRevisionInfo revision = model.getRevision(i);
            rows.add(new RebasePlan.Row(
                    revision.getRevision(),
                    model.getAction(i),
                    revision.getFullMessage(),
                    model.getMessage(i),
                    model.getRunMessage(i)
            ));
        }
        return new InteractiveRebaseHandler(repository, RebasePlan.of(rows));
    }

    /**
     * The message explaining why the rebase cannot be started, <code>null</code>
     * if the dialog can be shown.
     */
    String getError() {
        return error;
    }

    public boolean showDialog() {
        if (error != null) {
            javax.swing.JOptionPane.showMessageDialog(org.openide.windows.WindowManager.getDefault().getMainWindow(),
                    error, Bundle.CTL_InteractiveRebaseDialog_ACSD(), javax.swing.JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // the rebase runs only once the commit messages are settled: after
        // the user confirms the plan, the dialog asks for the message of
        // every reworded commit and of every squashed run; canceling a
        // message dialog goes back to the plan, the rebase does not run
        while (true) {
            if (!showPlanDialog()) {
                return false;
            }
            if (promptMessages()) {
                return true;
            }
        }
    }

    private boolean showPlanDialog() {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, Bundle.CTL_InteractiveRebaseDialog_ACSD());
        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        dialogDescriptor.setModal(false); // must allow interaction with diff views
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.git.ui.rebase.InteractiveRebasePanel")); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(Bundle.CTL_InteractiveRebaseDialog_ACSD());
        dialog.setAlwaysOnTop(true);
        dialog.setResizable(true);
        openAndBlock(dialog);
        return dialogDescriptor.getValue() == okButton;
    }

    private static void openAndBlock(Dialog dialog) {
        CountDownLatch closed = new CountDownLatch(1);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closed.countDown();
            }
        });
        dialog.setVisible(true);
        try {
            closed.await();
        } catch (InterruptedException ex) {
            Thread.interrupted();
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Settles the commit messages of the plan before the rebase runs: the
     * user sees the commit dialog for every reworded commit (the message of
     * the commit) and for every run of squashed commits (the message the
     * driver would combine), and can edit it.
     *
     * @return <code>true</code> if the user confirmed every message,
     *         <code>false</code> if one of the dialogs was canceled (the
     *         rebase is not run, the plan is kept, the settled messages are
     *         kept too)
     */
    private boolean promptMessages() {
        InteractiveRebasePanel.TodoTableModel model = panel.getTableModel();
        GitUser user;
        try (GitClient client = Git.getInstance().getClient(repository)) {
            user = client.getUser();
        } catch (GitException ex) {
            GitClientExceptionHandler.notifyException(ex, true);
            return false;
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            RebaseAction action = model.getAction(i);
            if (action != RebaseAction.PICK && action != RebaseAction.REWORD) {
                continue; // the row does not survive on the branch
            }
            GitRevisionInfo revision = model.getRevision(i);
            if (action == RebaseAction.REWORD && model.getMessage(i) == null) {
                // the message of a reworded commit is settled once and kept
                // if a later prompt of the batch was canceled
                String result = promptCommitMessage(
                        revision.getParents()[0],
                        revision.getRevision(),
                        revision.getFullMessage(),
                        user,
                        Bundle.CTL_InteractiveRebaseDialog_rewordTitle()
                );
                if (result == null) {
                    return false;
                }
                model.setMessage(i, result);
            }
            int end = runEnd(i, model);
            if (end > i && runContainsSquash(i, end, model) && model.getRunMessage(i) == null) {
                String result = promptCommitMessage(
                        revision.getParents()[0],
                        model.getRevision(end).getRevision(),
                        combinedSquashMessage(model, i, end), user,
                        Bundle.CTL_InteractiveRebaseDialog_squashTitle()
                );
                if (result == null) {
                    return false;
                }
                model.setRunMessage(i, result);
            }
        }
        return true;
    }

    /**
     * Shows the commit dialog to settle the message of a commit, or of a
     * run of squashed commits: the files of the commit (or of the run) are
     * listed, the message area is pre-filled with the given message, the
     * user edits it and confirms it or cancels.
     *
     * @return the message the user settled, <code>null</code> if the dialog
     *         was canceled
     */
    private String promptCommitMessage(String baseRevision, String commitRevision, String message, GitUser user, String title) {
        RewordCommitPanel dialog = RewordCommitPanel.create(repository, baseRevision, commitRevision, message, user);
        boolean confirmed = dialog.open(VCSContext.EMPTY, new HelpCtx("org.netbeans.modules.git.ui.rebase.RewordCommitPanel"), title); //NOI18N
        return confirmed ? ((GitCommitParameters) dialog.getParameters()).getCommitMessage() : null;
    }

    /**
     * The last row of the run of squashed or fixup-ed rows after the row
     * <code>head</code>, or <code>head</code> itself if the row absorbs
     * nothing.
     */
    static int runEnd(int head, InteractiveRebasePanel.TodoTableModel model) {
        int end = head;
        while (end + 1 < model.getRowCount()
                && (model.getAction(end + 1) == RebaseAction.SQUASH || model.getAction(end + 1) == RebaseAction.FIXUP)) {
            end++;
        }
        return end;
    }

    /**
     * Whether the run of squashed or fixup-ed rows the row <code>head</code>
     * absorbs contains a squash: a fixup-only run keeps the message of the
     * head commit, no prompt is shown for it.
     */
    static boolean runContainsSquash(int head, int end, InteractiveRebasePanel.TodoTableModel model) {
        for (int i = head + 1; i <= end; i++) {
            if (model.getAction(i) == RebaseAction.SQUASH) {
                return true;
            }
        }
        return false;
    }

    /**
     * The message of a run of squashed commits the way the rebase driver
     * combines it: the message of the head commit (the reworded one, if the
     * user reworded it) followed by the messages of the folded commits,
     * with the comment lines of the combined message of the driver.
     */
    static String combinedSquashMessage(InteractiveRebasePanel.TodoTableModel model, int head, int end) {
        StringBuilder sb = new StringBuilder();
        sb.append("# This is a combination of ").append(end - head + 1).append(" commits.\n"); // NOI18N
        sb.append("# The first commit's message is:\n"); // NOI18N
        String headMessage = model.getMessage(head);
        sb.append(headMessage != null ? headMessage : model.getRevision(head).getFullMessage());
        for (int i = head + 1; i <= end; i++) {
            sb.append("\n\n# This is the ").append(ordinal(i - head + 1)).append(" commit message:\n"); // NOI18N
            sb.append(model.getRevision(i).getFullMessage());
        }
        return sb.toString();
    }

    /** The ordinal suffix the driver uses in the combined message. */
    static String ordinal(int count) {
        return switch (count % 10) {
            case 1 -> "st"; // NOI18N
            case 2 -> "nd"; // NOI18N
            case 3 -> "rd"; // NOI18N
            default -> "th"; // NOI18N
        };
    }

    /**
     * A {@link GitRebaseInteractiveHandler} applying the plan the user
     * settled in the dialog to the rebase: the actions and the order of the
     * steps of the driver, the settled messages to the commit messages, and
     * the persistence of the plan into the rebase folder, so that the
     * messages survive the stops of the rebase (a continued rebase is
     * re-attached a handler built from the persisted plan).
     */
    static final class InteractiveRebaseHandler implements GitRebaseInteractiveHandler {

        private final File repository;
        private final RebasePlan plan;
        private final List<String> ids;
        private final Map<String, RebaseAction> actions;
        private final Map<String, String> rewordMessages;
        private final List<RebasePlan.SquashRun> squashRuns;

        InteractiveRebaseHandler(File repository, RebasePlan plan) {
            this.repository = repository;
            this.plan = plan;
            this.ids = new ArrayList<>(plan.getRows().size());
            this.actions = new TreeMap<>();
            for (RebasePlan.Row row : plan.getRows()) {
                ids.add(row.id());
                actions.put(row.id(), row.action());
            }
            // the settled messages: the hook is invoked by the driver with
            // the original message of a reworded commit (the callback
            // receives no id, the original message is the key) and with the
            // combined message of a finished run of squashed commits (the
            // message of the head commit and the messages of the squashed
            // commits are part of it, matched by them)
            this.rewordMessages = plan.rewordMessages();
            this.squashRuns = plan.squashRuns();
        }

        @Override
        public void prepareSteps(List<GitRebaseTodoLine> steps) {
            // the plan and the driver's todo list the steps in the same
            // order (oldest first): walk the plan from the top (the oldest
            // commit) down; the steps the rebase already processed are not
            // in the list any more, they are skipped
            List<GitRebaseTodoLine> reordered = new ArrayList<>(steps.size());
            for (int row = 0; row < ids.size(); row++) {
                String id = ids.get(row);
                for (GitRebaseTodoLine step : steps) {
                    if (step.getCommit().isEmpty() || reordered.contains(step)) {
                        continue;
                    }
                    if (id.startsWith(step.getCommit())) {
                        // the driver's todo lines hold abbreviated ids
                        step.setAction(actions.get(id));
                        reordered.add(step);
                        break;
                    }
                }
            }
            // steps the plan does not cover are kept as-is, at the end
            for (GitRebaseTodoLine step : steps) {
                if (!reordered.contains(step)) {
                    reordered.add(step);
                }
            }
            steps.clear();
            steps.addAll(reordered);
            // persist the plan for the recovery path: the driver created the
            // rebase folder before invoking the callback; the fully qualified
            // name is needed because the import of the RebaseAction enum
            // hides the class of the same name
            plan.persistIfAbsent(org.netbeans.modules.git.ui.rebase.RebaseAction.getRebaseFolder(repository));
        }

        @Override
        public String modifyCommitMessage(String message) {
            String settled = rewordMessages.get(message);
            if (settled != null) {
                return settled;
            }
            for (RebasePlan.SquashRun run : squashRuns) {
                if (run.matches(message)) {
                    return run.settledMessage();
                }
            }
            return message;
        }

    }

}
