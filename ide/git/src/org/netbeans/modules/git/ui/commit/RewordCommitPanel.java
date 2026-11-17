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
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JProgressBar;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo.GitFileInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitFileNode.GitHistoryFileNode;
import org.netbeans.modules.git.GitFileNode.HistoryFileInformation;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.ui.diff.MultiDiffPanelController;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.versioning.util.common.VCSCommitDiffProvider;
import org.netbeans.modules.versioning.util.common.VCSCommitFilter;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.netbeans.modules.versioning.util.common.VCSCommitPanel;
import org.netbeans.modules.versioning.util.common.VCSCommitTable;
import org.netbeans.modules.versioning.util.common.VCSCommitTableModel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * A commit dialog for editing the message of an existing commit.
 * 
 * It shows the files of the commit (or of the commits of a squashed run) and a
 * pre-filled commit message, reusing some of the functionality of the regular
 * commit dialog (and disables not applicable features).
 *
 * <p>The interactive rebase shows the dialog before the rebase runs, to
 * reword a commit or to compose the message of a squashed run, so that all
 * the commit messages are settled before the rebase is executed.
 *
 * @author mbien
 */
public final class RewordCommitPanel extends VCSCommitPanel<GitHistoryFileNode> {

    private static final RequestProcessor REPOSITORY = new RequestProcessor("RewordCommitPanel"); //NOI18N

    private final File repository;
    private final String baseRevision;
    private final String commitRevision;

    private RewordCommitPanel(File repository, String baseRevision, String commitRevision, String commitMessage, GitUser user) {
        super(
            new RewordTable(),
            new GitCommitParameters(GitModuleConfig.getDefault().getPreferences(), commitMessage, true, user),
            GitModuleConfig.getDefault().getPreferences(),
            List.of(),
            null,
            List.of(new AllFilter()),
            new DiffProvider(baseRevision, commitRevision)
        );
        this.repository = repository;
        this.baseRevision = baseRevision;
        this.commitRevision = commitRevision;
        // the message form of the commit dialog: the amend switch is
        // irrelevant (the reword is not a commit of the working tree) and
        // the author and the committer of the commit are not changed
        CommitPanel form = (CommitPanel) getParameters().getPanel();
        form.amendCheckBox.setVisible(false);
        form.authorComboBox.setEnabled(false);
        form.commiterComboBox.setEnabled(false);
    }

    /**
     * Creates the dialog to edit the message of a commit.
     *
     * @param repository the repository of the commit
     * @param baseRevision the revision the commit is built on, its parent;
     *             the files of the commit are listed against this revision
     * @param commitRevision the revision of the commit
     * @param commitMessage the initial text of the message area
     * @param user the user of the repository, shown in the author and the
     *             committer fields
     * @return the dialog
     */
    public static RewordCommitPanel create(File repository, String baseRevision, String commitRevision, String commitMessage, GitUser user) {
        return new RewordCommitPanel(repository, baseRevision, commitRevision, commitMessage, user);
    }

    @Override
    protected void computeNodes() {
        JProgressBar progress = new JProgressBar();
        setupProgress("Loading the files of the commit", progress);
        showProgress();
        REPOSITORY.create(() -> {
            Map<File, GitFileInfo> files;
            try (GitClient client = Git.getInstance().getClient(repository)) {
                files = client.getStatus(
                        new File[] { repository }, baseRevision, commitRevision,
                        new ProgressMonitor.DefaultProgressMonitor()
                );
            } catch (GitException ex) {
                GitClientExceptionHandler.notifyException(ex, true);
                files = Collections.emptyMap();
            }
            GitHistoryFileNode[] nodes = new GitHistoryFileNode[files.size()];
            int i = 0;
            for (Map.Entry<File, GitFileInfo> entry : files.entrySet()) {
                nodes[i++] = new RewordFileNode(
                        repository,
                        entry.getKey(),
                        new HistoryFileInformation(entry.getValue())
                );
            }
            EventQueue.invokeLater(() -> {
                stopProgress();
                getCommitTable().setNodes(nodes);
            });
        }).schedule(0);
    }

    /**
     * The node of a file of the commit: the file is included in the table
     * but the option cannot be changed, the table is only a view of the
     * files the commit changes.
     */
    private static final class RewordFileNode extends GitHistoryFileNode {

        RewordFileNode(File root, File file, HistoryFileInformation info) {
            super(root, file, info);
        }

        @Override
        public VCSCommitOptions getDefaultCommitOption(boolean withExclusions) {
            return VCSCommitOptions.COMMIT;
        }
    }

    /**
     * The table of the files of the commit: the files always exist and the
     * commit button does not depend on the selection of the table. The
     * checkbox and the include/exclude columns of the commit dialog are
     * dropped: the files of the commit cannot be selected or unselected.
     */
    private static final class RewordTable extends VCSCommitTable<GitHistoryFileNode> {

        RewordTable() {
            super(new VCSCommitTableModel<>(), false);
        }

        @Override
        protected String[] getColumns() {
            return new String[] {
                VCSCommitTableModel.COLUMN_NAME_NAME,
                VCSCommitTableModel.COLUMN_NAME_STATUS,
                VCSCommitTableModel.COLUMN_NAME_PATH
            };
        }

        @Override
        public boolean containsCommitable() {
            return true;
        }

        @Override
        public String getErrorMessage() {
            return null;
        }
    }

    /**
     * The provider of the diff a double-click on a file opens: the diff of
     * the file between the parent of the commit and the commit, in a tab of
     * the main window.
     */
    private static final class DiffProvider extends VCSCommitDiffProvider<GitHistoryFileNode> {

        private final String baseRevision;
        private final String commitRevision;

        DiffProvider(String baseRevision, String commitRevision) {
            this.baseRevision = baseRevision;
            this.commitRevision = commitRevision;
        }

        @Override
        protected JComponent createDiffComponent(File file) {
            return new MultiDiffPanelController(
                file,
                new Revision(baseRevision, baseRevision.substring(0, 7)),
                new Revision(commitRevision, commitRevision.substring(0, 7)),
                0
            ).getPanel();
        }
    }

    @NbBundle.Messages({
        "LBL_RewordCommitPanel.filter=All files of the commit",
        "TTT_RewordCommitPanel.filter=Show all files the commit changes"
    })
    private static final class AllFilter extends VCSCommitFilter {

        private static final Icon ICON =
                ImageUtilities.loadIcon("org/netbeans/modules/git/resources/icons/head_vs_index.png");

        AllFilter() {
            super(true);
        }

        @Override
        public Icon getIcon() {
            return ICON;
        }

        @Override
        public String getTooltip() {
            return Bundle.TTT_RewordCommitPanel_filter();
        }

        @Override
        public String getID() {
            return "all"; //NOI18N
        }
    }

}
