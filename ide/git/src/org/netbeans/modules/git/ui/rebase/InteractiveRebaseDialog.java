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

import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JButton;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRebaseInteractiveHandler;
import org.netbeans.libs.git.GitRebaseTodoLine;
import org.netbeans.libs.git.GitRebaseTodoLine.RebaseAction;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.ui.repository.RevisionInfoPanelController;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

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
            + "is not linear."
    })
    public InteractiveRebaseDialog(File repository, String baseRevision) {
        this.repository = repository;
        this.baseRevision = baseRevision;
        panel = new InteractiveRebasePanel();
        infoPanelController = new RevisionInfoPanelController(repository);
        panel.getInfoPanel().add(infoPanelController.getPanel(), java.awt.BorderLayout.CENTER);
        panel.getTableModel().setRevisions(EMPTY);
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
        panel.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // a double-click opens the diff of the commit against its
                // parent in the main window; on the action column the
                // double-click still opens the action combo
                if (e.getClickCount() != 2 || e.getButton() != MouseEvent.BUTTON1
                        || panel.getTable().columnAtPoint(e.getPoint()) == 1) {
                    return;
                }
                int row = panel.getTable().rowAtPoint(e.getPoint());
                if (row >= 0) {
                    showDiff(row);
                }
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

    /**
     * Opens the diff of the commit of a todo row, against its parent, as a
     * diff tab in the main window, like the diff action of the git file
     * nodes does.
     */
    private void showDiff(int row) {
        GitRevisionInfo revision = panel.getTableModel().getRevision(row);
        // the range is linear (checked when the dialog is created), so the
        // commit has a single parent; for the oldest commit it is the base
        // of the range
        String parent = revision.getParents()[0];
        SystemAction.get(DiffAction.class).diff(
            VCSContext.forNodes(new Node[] { new AbstractNode(Children.LEAF, Lookups.fixed(repository)) }),
            new Revision(parent, parent.substring(0, 7)),
            new Revision(revision.getRevision(), revision.getRevision().substring(0, 7), revision.getShortMessage(), null)
        );
    }

    private static final List<GitRevisionInfo> EMPTY = new ArrayList<>(0);

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
            for (GitRevisionInfo cur = head; cur != null && !cur.getRevision().equals(baseRevision);
                    cur = byId.get(cur.getParents()[0])) {
                chain.add(cur);
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
     * The handler to pass to the rebase command; applies the actions and the
     * order selected by the user to the driver's list of steps and leaves the
     * combined commit messages untouched.
     */
    GitRebaseInteractiveHandler getHandler() {
        InteractiveRebasePanel.TodoTableModel model = panel.getTableModel();
        List<String> ids = new ArrayList<>(model.getRowCount());
        Map<String, RebaseAction> actions = new TreeMap<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            ids.add(model.getRevision(i).getRevision());
            actions.put(ids.get(i), model.getAction(i));
        }
        return new InteractiveRebaseHandler(ids, actions);
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
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, Bundle.CTL_InteractiveRebaseDialog_ACSD());
        dialogDescriptor.setOptions(new Object[] { okButton, cancelButton });
        dialogDescriptor.setModal(false);
        dialogDescriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.git.ui.rebase.InteractiveRebasePanel")); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(Bundle.CTL_InteractiveRebaseDialog_ACSD());
        dialog.setVisible(true);
        dialog.setResizable(true);
        return dialogDescriptor.getValue() == okButton;
    }

    /**
     * A {@link GitRebaseInteractiveHandler} applying the actions and the
     * order chosen by the user to the steps of the rebase driver.
     */
    static final class InteractiveRebaseHandler implements GitRebaseInteractiveHandler {

        /** The commit ids in the order of the todo table, oldest first. */
        private final List<String> ids;
        private final Map<String, RebaseAction> actions;

        InteractiveRebaseHandler(List<String> ids, Map<String, RebaseAction> actions) {
            this.ids = ids;
            this.actions = actions;
        }

        @Override
        public void prepareSteps(List<GitRebaseTodoLine> steps) {
            // the table and the driver's todo list the steps in the same
            // order (oldest first): walk the table from the top (the oldest
            // commit) down
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
        }

        @Override
        public String modifyCommitMessage(String message) {
            return message;
        }

    }

}
