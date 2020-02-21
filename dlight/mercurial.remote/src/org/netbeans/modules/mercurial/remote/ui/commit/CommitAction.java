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
package org.netbeans.modules.mercurial.remote.ui.commit;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgFileNode;
import org.netbeans.modules.mercurial.remote.HgModuleConfig;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.WorkingCopyInfo;
import org.netbeans.modules.mercurial.remote.config.HgConfigFiles;
import org.netbeans.modules.mercurial.remote.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.ui.repository.HgURL;
import org.netbeans.modules.mercurial.remote.ui.status.StatusAction;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.remotefs.versioning.hooks.HgHook;
import org.netbeans.modules.remotefs.versioning.hooks.HgHookContext;
import org.netbeans.modules.remotefs.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Commit action for mercurial:
 * hg commit -  commit the specified files or all outstanding changes
 *
 * 
 */
public class CommitAction extends ContextAction {

    static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage"; // NOI18N
    static final String KEY_CANCELED_MESSAGE = "commit"; //NOI18N
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/remote/resources/icons/commit.png"; //NOI18N
    private static final String ERROR_COLOR;
    private static final String INFO_COLOR;
    static {
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            ERROR_COLOR = "#CC0000"; //NOI18N
        } else {
            ERROR_COLOR = HgUtils.getColorString(c);
        }
        c = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (c == null) {
            INFO_COLOR = "#002080"; //NOI18N
        } else {
            INFO_COLOR = HgUtils.getColorString(c);
        }
    }

    public CommitAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<VCSFileProxy> ctxFiles = context != null? context.getRootFiles(): null;
        if (!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Commit"; // NOI18N
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        final VCSFileProxy root = HgUtils.getRootFile(context);
        if (root == null) {
            OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed( NbBundle.getMessage(CommitAction.class,"MSG_COMMIT_TITLE")); // NOI18N
            logger.outputInRed( NbBundle.getMessage(CommitAction.class,"MSG_COMMIT_TITLE_SEP")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            logger.output(""); // NOI18N
            logger.closeLog();
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String contentTitle = VCSFileProxySupport.getContextDisplayName(context);

        commit(contentTitle, context);
    }

    @Messages({
        "# {0} - repository name", "MSG_CommitAction.interruptedRebase.error=Repository {0} is in the middle of an interrupted rebase.\n"
            + "Finish the rebase before committing changes."
    })
    public static void commit (final String contentTitle, final VCSContext ctx) {
        Utils.post(new Runnable() {
            @Override
            public void run () {
                VCSFileProxy root = HgUtils.getRootFile(ctx);
                if (root == null) {
                    Mercurial.LOG.log(Level.FINE, "CommitAction.commit: null owner for {0}", ctx.getRootFiles()); //NOI18N
                    return;
                }
                if (HgUtils.isRebasing(root)) {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                            Bundle.MSG_CommitAction_interruptedRebase_error(root.getName()),
                            NotifyDescriptor.ERROR_MESSAGE));
                    return;
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        commit(contentTitle, ctx, null);
                    }
                });
            }
        });
    }
    
    private static void commit (String contentTitle, final VCSContext ctx, final String branchName) {
        final VCSFileProxy repository = HgUtils.getRootFile(ctx);
        final boolean closingBranch = branchName != null;
        if (repository == null) {
            return;
        }

        // show commit dialog
        final CommitPanel panel = new CommitPanel(repository);
        final Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);

        panel.setHooks(hooks, new HgHookContext(ctx.getRootFiles().toArray( new VCSFileProxy[ctx.getRootFiles().size()]), null, new HgHookContext.LogEntry[] {}));
        final CommitTable data = new CommitTable(repository, panel.filesLabel, CommitTable.COMMIT_COLUMNS, new String[] {CommitTableModel.COLUMN_NAME_PATH });

        panel.setCommitTable(data);
        data.setCommitPanel(panel);
        panel.cbAllFiles.setVisible(closingBranch);
        final AtomicBoolean afterMerge = new AtomicBoolean(false);
        if (closingBranch) {
            panel.cbAllFiles.setSelected(false);
            panel.cbAllFiles.doClick();
            panel.cbAllFiles.setEnabled(false);
        }

        final JButton commitButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(commitButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Commit"));
        if (closingBranch) {
            commitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_CloseBranch"));
            commitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_CloseBranch"));
        } else {
            commitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Commit"));
            commitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Commit"));
        }
        final JButton cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Cancel"));
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Cancel"));

        final DialogDescriptor dd = new DialogDescriptor(panel,
              org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_CommitDialog_Title", contentTitle), // NOI18N
              true,
              new Object[] {commitButton, cancelButton},
              commitButton,
              DialogDescriptor.DEFAULT_ALIGN,
              new HelpCtx(CommitAction.class),
              null);
        ActionListener al;
        dd.setButtonListener(al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dd.setClosingOptions(new Object[] {commitButton, cancelButton});
                SaveCookie[] saveCookies = panel.getSaveCookies();
                if (cancelButton == e.getSource()) {
                    if (saveCookies.length > 0) {
                        if (SaveBeforeClosingDiffConfirmation.allSaved(saveCookies) || !panel.isShowing()) {
                            EditorCookie[] editorCookies = panel.getEditorCookies();
                            for (EditorCookie cookie : editorCookies) {
                                cookie.open();
                            }
                        } else {
                            dd.setClosingOptions(new Object[0]);
                        }
                    }
                    dd.setValue(cancelButton);
                } else if (commitButton == e.getSource()) {
                    if (saveCookies.length > 0 && !SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
                        dd.setClosingOptions(new Object[0]);
                    } else if (!panel.canCommit()) {
                        dd.setClosingOptions(new Object[0]);
                    }
                    dd.setValue(commitButton);
                }
            }
        });
        computeNodes(data, panel, ctx, repository, cancelButton, afterMerge);
        HgProgressSupport incomingChanges = checkForIncomingChanges(repository, panel, afterMerge);
        commitButton.setEnabled(false);
        panel.addVersioningListener(new VersioningListener() {
            @Override
            public void versioningEvent(VersioningEvent event) {
                refreshCommitDialog(panel, data, commitButton, branchName, afterMerge);
            }
        });
        data.getTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                refreshCommitDialog(panel, data, commitButton, branchName, afterMerge);
            }
        });
        commitButton.setEnabled(containsCommitable(data));

        panel.putClientProperty("contentTitle", contentTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        WindowListener windowListener = new DialogBoundsPreserver(HgModuleConfig.getDefault(repository).getPreferences(), "hg.commit.dialog"); // NOI18N
        dialog.addWindowListener(windowListener);
        dialog.pack();
        windowListener.windowOpened(new WindowEvent(dialog, WindowEvent.WINDOW_OPENED));
        dialog.setVisible(true);
        
        if (incomingChanges != null) {
            incomingChanges.cancel();
        }

        final String message = panel.getCommitMessage().trim();
        if (dd.getValue() != commitButton && !message.isEmpty()) {
            HgModuleConfig.getDefault(repository).setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE, message);
        }
        if (dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
            al.actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, null));
            panel.closed();
        } else if (dd.getValue() == commitButton) {
            panel.closed();
            final Map<HgFileNode, CommitOptions> commitFiles = data.getCommitFiles();
            final Map<VCSFileProxy, Set<VCSFileProxy>> rootFiles = HgUtils.sortUnderRepository(ctx, true);
            final boolean commitAllFiles = panel.cbAllFiles.isSelected() || afterMerge.get();
            HgModuleConfig.getDefault(repository).setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE, ""); //NOI18N
            Utils.insert(HgModuleConfig.getDefault(repository).getPreferences(), RECENT_COMMIT_MESSAGES, message.trim(), 20);
            final String user = panel.getUser();
            if (user != null) {
                HgModuleConfig.getDefault(repository).putRecentCommitAuthors(user);
            }
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
            HgProgressSupport support = new HgProgressSupport() {
                @Override
                public void perform() {
                    OutputLogger logger = getLogger();
                    performCommit(message, commitFiles, rootFiles, this, logger, hooks, user, commitAllFiles, closingBranch, afterMerge.get());
                }
            };
            support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
        }
    }
    
    public void closeBranch (String branchName, VCSContext ctx, String contentTitle) {
        commit(contentTitle, ctx, branchName);
    }

    private static void computeNodes(final CommitTable table, final CommitPanel panel, final VCSContext ctx, final VCSFileProxy repository, JButton cancel, final AtomicBoolean afterMerge) {
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        final HgProgressSupport support = new HgProgressSupport(NbBundle.getMessage(CommitAction.class, "Progress_Preparing_Commit"), cancel) {
            @Override
            public void perform() {
                try {
                    afterMerge.set(WorkingCopyInfo.getInstance(repository).getWorkingCopyParents().length > 1);
                    panel.progressPanel.setVisible(true);
                    setupUsers();
                    // Ensure that cache is uptodate
                    StatusAction.executeStatus(ctx, this);

                    FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                    VCSFileProxy[] roots = ctx.getRootFiles().toArray(new VCSFileProxy[ctx.getRootFiles().size()]);

                    VCSFileProxy[][] split = VCSFileProxySupport.splitFlatOthers(roots);
                    List<VCSFileProxy> fileList = new ArrayList<>();
                    for (int c = 0; c < split.length; c++) {
                        roots = split[c];
                        boolean recursive = c == 1;
                        if (recursive) {
                            Set<VCSFileProxy> repositories = HgUtils.getRepositoryRoots(ctx);
                            VCSFileProxy[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
                            for (int i = 0; i < files.length; i++) {
                                for(int r = 0; r < roots.length; r++) {
                                    if(repositories.contains(Mercurial.getInstance().getRepositoryRoot(files[i])) &&
                                       HgUtils.isParentOrEqual(roots[r], files[i]))
                                    {
                                        if(!fileList.contains(files[i])) {
                                            fileList.add(files[i]);
                                        }
                                    }
                                }
                            }
                        } else {
                            VCSFileProxy[] files = HgUtils.flatten(roots, FileInformation.STATUS_LOCAL_CHANGE);
                            for (int i= 0; i<files.length; i++) {
                                if(!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }

                    ArrayList<HgFileNode> nodesList = new ArrayList<>(fileList.size());

                    for (Iterator<VCSFileProxy> it = fileList.iterator(); it.hasNext();) {
                        VCSFileProxy file = it.next();
                        HgFileNode node = new HgFileNode(repository, file);
                        nodesList.add(node);
                    }
                    final HgFileNode[] nodes = nodesList.toArray(new HgFileNode[fileList.size()]);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            table.setNodes(nodes);
                            if (nodes.length > 0 && ctx.getRootFiles().size() == 1 && ctx.getRootFiles().iterator().next().equals(repository)) {
                                panel.cbAllFiles.setEnabled(true);
                            }
                        }
                    });
                } finally {
                    panel.progressPanel.setVisible(false);
                }
            }

            private void setupUsers () {
                HgConfigFiles config = new HgConfigFiles(repository);
                String userName = config.getUserName(false);
                if (userName.isEmpty()) {
                    config = HgConfigFiles.getSysInstance(repository);
                    userName = config.getUserName(false);
                }
                List<String> recentUsers = HgModuleConfig.getDefault(repository).getRecentCommitAuthors();
                if (!userName.isEmpty()) {
                    recentUsers.remove(userName);
                    recentUsers.add(0, userName);
                }
                final ComboBoxModel<String> model = new DefaultComboBoxModel<>(recentUsers.toArray(new String[recentUsers.size()]));
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        panel.cmbUser.setModel(model);
                        panel.cbAuthor.setEnabled(true);
                    }
                });
            }
        };
        panel.progressPanel.add(support.getProgressComponent());
        panel.progressPanel.setVisible(true);
        support.start(rp);
    }

    private static boolean containsCommitable(CommitTable data) {
        Map<HgFileNode, CommitOptions> map = data.getCommitFiles();
        for(CommitOptions co : map.values()) {
            if(co != CommitOptions.EXCLUDE) {
                return true;
            }
        }
        return false;
    }

    /**
     * User changed a commit action.
     *
     * @param panel
     * @param commit
     */
    @Messages({
        "MSG_CommitForm_ErrorInvalidAuthor=Invalid author"
    })
    private static void refreshCommitDialog(CommitPanel panel, CommitTable table, JButton commit, String branchToClose, AtomicBoolean afterMerge) {
        assert EventQueue.isDispatchThread();
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        Map<HgFileNode, CommitOptions> files = table.getCommitFiles();
        boolean errors = false;

        boolean enabled = commit.isEnabled();

        for (HgFileNode fileNode : files.keySet()) {

            CommitOptions options = files.get(fileNode);
            if (options == CommitOptions.EXCLUDE) {
                continue;
            }
            //stickyTags.add(HgUtils.getCopy(fileNode.getFile()));
            int status = fileNode.getInformation().getStatus();
            if ((status & FileInformation.STATUS_REMOTE_CHANGE) != 0 || status == FileInformation.STATUS_VERSIONED_CONFLICT) {
                enabled = false;
                String msg = (status == FileInformation.STATUS_VERSIONED_CONFLICT) ?
                        loc.getString("MSG_CommitForm_ErrorConflicts") : // NOI18N
                        loc.getString("MSG_CommitForm_ErrorRemoteChanges"); // NOI18N
                panel.setErrorLabel("<html><font color=\"" + ERROR_COLOR + "\">" + msg + "</font></html>");  // NOI18N
                errors = true;
            }
            //stickyTags.add(HgUtils.getCopy(fileNode.getFile()));

        }
        
        if (!errors && !panel.isUserValid()) {
            String msg = Bundle.MSG_CommitForm_ErrorInvalidAuthor();
            panel.setErrorLabel("<html><font color=\"" + INFO_COLOR + "\">" + msg + "</font></html>");  // NOI18N
            errors = true;
        }

        table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_COMMIT, CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_STATUS,
                                            CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });

        String contentTitle = (String) panel.getClientProperty("contentTitle"); // NOI18N
        DialogDescriptor dd = (DialogDescriptor) panel.getClientProperty("DialogDescriptor"); // NOI18N
        dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), new Object [] { contentTitle })); // NOI18N
        if (!errors) {
            if (afterMerge.get()) {
                panel.setErrorLabel("<html><font color=\"" + INFO_COLOR + "\">" //NOI18N
                        + NbBundle.getMessage(CommitAction.class, "CommitPanel.info.merge.allFiles") //NOI18N
                        + "</font></html>"); //NOI18N
            } else if (panel.cbAllFiles.isSelected()) {
                panel.setErrorLabel("<html><font color=\"" + INFO_COLOR + "\">" //NOI18N
                        + NbBundle.getMessage(CommitAction.class, "CommitPanel.info.closingBranch.allFiles", branchToClose) //NOI18N
                        + "</font></html>"); //NOI18N
            } else {
                panel.setErrorLabel(""); //NOI18N
            }
            enabled = true;
        }
        commit.setEnabled(enabled && (afterMerge.get() || panel.cbAllFiles.isSelected() || containsCommitable(table)));
    }

    public static void performCommit (String message, Map<HgFileNode, CommitOptions> commitFiles,
            Map<VCSFileProxy, Set<VCSFileProxy>> rootFiles, HgProgressSupport support, OutputLogger logger, Collection<HgHook> hooks) {
        performCommit(message, commitFiles, rootFiles, support, logger, hooks, null, false, false, false);
    }

    private static void performCommit(String message, Map<HgFileNode, CommitOptions> commitFiles,
            Map<VCSFileProxy, Set<VCSFileProxy>> rootFiles, HgProgressSupport support, OutputLogger logger, Collection<HgHook> hooks,
            String user, boolean commitAllFiles, boolean closeBranch, boolean afterMerge) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        Map<VCSFileProxy, List<VCSFileProxy>> addCandidates = new HashMap<>();
        Map<VCSFileProxy, List<VCSFileProxy>> deleteCandidates = new HashMap<>();
        Map<VCSFileProxy, List<VCSFileProxy>> commitCandidates = new HashMap<>();
        Map<VCSFileProxy, Set<VCSFileProxy>> filesToRefresh = new HashMap<>();

        List<String> excPaths = new ArrayList<>();
        Map<VCSFileProxy, Boolean> locallyModifiedExcluded = new HashMap<>();
        List<String> incPaths = new ArrayList<>();
        if (commitAllFiles && closeBranch) {
            assert rootFiles.size() == 1;
            for (VCSFileProxy root : rootFiles.keySet()) {
                commitCandidates.put(root, Collections.<VCSFileProxy>emptyList());
            }
        } else {
            for (Map.Entry<HgFileNode, CommitOptions> e : commitFiles.entrySet()) {
                 if (support.isCanceled()) {
                     return;
                 }
                 HgFileNode node = e.getKey();
                 CommitOptions option = e.getValue();
                 VCSFileProxy repository = Mercurial.getInstance().getRepositoryRoot(node.getFile());
                 if (option != CommitOptions.EXCLUDE) {
                     int  status = cache.getStatus(node.getFile()).getStatus();
                     if ((status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                         putCandidate(addCandidates, repository, node.getFile());
                     } else  if ((status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) {
                         putCandidate(deleteCandidates, repository, node.getFile());
                     }
                     putCandidate(commitCandidates, repository, node.getFile());
                     incPaths.add(node.getFile().getPath());
                 }else{
                     excPaths.add(node.getFile().getPath());
                     if (!Boolean.TRUE.equals(locallyModifiedExcluded.get(repository))) {
                         int status = cache.getCachedStatus(node.getFile()).getStatus();
                         locallyModifiedExcluded.put(repository, (status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) == 0); // not interested in excluded locally new files
                     }
                 }
            }
        }
        if (support.isCanceled()) {
            return;
        }

        if (!excPaths.isEmpty()) {
            HgModuleConfig.getDefault(rootFiles.entrySet().iterator().next().getKey()).addExclusionPaths(excPaths);
        }
        if (!incPaths.isEmpty()) {
            HgModuleConfig.getDefault(rootFiles.entrySet().iterator().next().getKey()).removeExclusionPaths(incPaths);
        }

        try {
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE_SEP")); // NOI18N
            logger.output(message); // NOI18N

            new Cmd.AddCmd(addCandidates, logger, null, "hg add {0} into {1}").handle(); //NOI18N
            new Cmd.RemoveCmd(deleteCandidates, logger, null, "hg delete {0} from {1}").handle(); //NOI18N
            removeDeletedTemporaryFiles(commitCandidates, deleteCandidates);
            if (support.isCanceled()) {
                return;
            }

            VCSFileProxy[] hookFiles = null;
            if(hooks.size() > 0) {
                List<VCSFileProxy> candidates = new LinkedList<>();
                for (List<VCSFileProxy> values : commitCandidates.values()) {
                    candidates.addAll(values);
                }
                hookFiles = candidates.toArray(new VCSFileProxy[candidates.size()]);
            }
            String originalMessage = message;
            HgHookContext context = new HgHookContext(hookFiles, message, new HgHookContext.LogEntry[] {});
            for (HgHook hook : hooks) {
                try {
                    // XXX handle returned context
                    context = hook.beforeCommit(context);
                    if(context != null) {
                        message = context.getMessage();
                    }
                } catch (IOException ex) {
                    // XXX handle veto
                }
            }
            final Cmd.CommitCmd commitCmd = new Cmd.CommitCmd(commitCandidates, logger, message, support, rootFiles,
                    locallyModifiedExcluded, filesToRefresh, user, closeBranch, afterMerge);
            commitCmd.setCommitHooks(context, hooks, hookFiles, originalMessage);
            commitCmd.handle();
        } catch (HgException.HgCommandCanceledException ex) {
            // canceled by user, do nothing
        } catch (HgException ex) {
            HgUtils.notifyException(ex);
        } finally {
            refreshFS(filesToRefresh);
            cache.refreshAllRoots(filesToRefresh);
            logger.outputInRed(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_DONE")); // NOI18N
            logger.output(""); // NOI18N
        }
    }

    private static void refreshFS (Map<VCSFileProxy, Set<VCSFileProxy>> filesPerRepository) {
        final Set<VCSFileProxy> files = new HashSet<>();
        for (Set<VCSFileProxy> values : filesPerRepository.values()) {
            files.addAll(values);
        }
        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                VersioningSupport.refreshFor(files.toArray(new VCSFileProxy[files.size()]));
            }
        }, 100);
    }

    private static void putCandidate(Map<VCSFileProxy, List<VCSFileProxy>> m, VCSFileProxy repository, VCSFileProxy file) {
        List<VCSFileProxy> l = m.get(repository);
        if(l == null) {
            l = new ArrayList<>();
            m.put(repository, l);
        }
        l.add(file);
    }

    private static boolean commitAfterMerge (boolean locallyModifiedExcluded, VCSFileProxy repository) {
        // XXX consider usage of repository to determine if there are any non-included files which have to be committed, too
        // and thus removing the option HgModuleConfig.getDefault().getConfirmCommitAfterMerge()
        if (locallyModifiedExcluded || HgModuleConfig.getDefault(repository).getConfirmCommitAfterMerge()) { // ask before commit?
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_AFTER_MERGE_QUERY")); // NOI18N
            descriptor.setTitle(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_AFTER_MERGE_TITLE")); // NOI18N
            descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
            descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

            Object res = DialogDisplayer.getDefault().notify(descriptor);
            return res == NotifyDescriptor.YES_OPTION;
        }
        return true;
    }
    
    @Messages({
        "MSG_CommitAction.warning.incomingChanges=There are incoming changes. You should pull from the remote repository first."
    })
    private static HgProgressSupport checkForIncomingChanges (final VCSFileProxy repository, final CommitPanel panel,
            final AtomicBoolean afterMerge) {
        HgProgressSupport supp = new HgProgressSupport() {
            @Override
            protected void perform () {
                if (afterMerge.get()) {
                    return;
                }
                try {
                    String defaultPush = new HgConfigFiles(repository).getDefaultPush(false);
                    if (!HgUtils.isNullOrEmpty(defaultPush)) {
                        try {
                            HgURL pushUrl = new HgURL(defaultPush);
                            if (pushUrl.getScheme().toString().contains("ssh")) { //NOI18N
                                Mercurial.LOG.log(Level.FINE, "Commit: Cannot handle ssh authentication silently: {0}", pushUrl.toHgCommandStringWithNoPassword());
                                return;
                            }
                        } catch (URISyntaxException ex) {
                            Mercurial.LOG.log(Level.INFO, "Commit: Invalid push url: {0}, falling back to command without target", defaultPush);
                        }
                    }
                    final String branch = HgCommand.getBranch(repository);
                    HgCommand.runWithoutUI(new Callable<Void>() {
                        @Override
                        public Void call () throws HgException {
                            if (HgCommand.getOutMessages(repository, null, branch, true, false, 1,
                                    OutputLogger.getLogger(null)).length == 0) {
                                if (!isCanceled() && HgCommand.getIncomingMessages(repository, null, branch, true, false, false, 1,
                                        OutputLogger.getLogger(null)).length > 0) {
                                    panel.setWarningMessage(Bundle.MSG_CommitAction_warning_incomingChanges());
                                }
                            }
                            return null;
                        }
                    });
                } catch (HgException.HgCommandCanceledException ex) {
                } catch (HgException ex) {
                    Logger.getLogger(CommitAction.class.getName()).log(Level.FINE, null, ex);
                }
            }

            @Override
            protected ProgressHandle getProgressHandle () {
                return null;
            }

            @Override
            protected void startProgress () { }

            @Override
            protected void finnishProgress () { }
        };
        supp.start(Mercurial.getInstance().getRequestProcessor(repository));
        return supp;
    }

    /**
     * Removes deleted uncommitted files (previously added but then deleted => no longer existing files)
     * from the commit candidate list.
     */
    private static void removeDeletedTemporaryFiles (Map<VCSFileProxy, List<VCSFileProxy>> commitCandidates, Map<VCSFileProxy, List<VCSFileProxy>> deleteCandidates) {
        for (Entry<VCSFileProxy, List<VCSFileProxy>> e : deleteCandidates.entrySet()) {
            VCSFileProxy root = e.getKey();
            List<VCSFileProxy> files = e.getValue();
            if (!files.isEmpty()) {
                try {
                    List<VCSFileProxy> commitFiles = commitCandidates.get(root);
                    Map<VCSFileProxy, FileInformation> status = HgCommand.getStatus(root, files, null, null);
                    for (VCSFileProxy f : files) {
                        if (status.get(f) == null) {
                            // status no longer interesting, do not commit
                            commitFiles.remove(f);
                        }
                    }
                } catch (HgException.HgCommandCanceledException ex) {
                    return;
                } catch (HgException ex) {
                    Logger.getLogger(CommitAction.class.getName()).log(Level.FINE, null, ex);
                }
            }
        }
    }

    private static abstract class Cmd {
        protected final Map<VCSFileProxy, List<VCSFileProxy>> m;
        protected final OutputLogger logger;
        protected final String logMsgFormat;
        protected final String msg;
        public Cmd(Map<VCSFileProxy, List<VCSFileProxy>> m, OutputLogger logger, String msg, String logMsgFormat) {
            this.m = m;
            this.logger = logger;
            this.logMsgFormat = logMsgFormat;
            this.msg = msg;
        }
        void handle() throws HgException {
            if(m.isEmpty()) {
                return;
            }
            for (Entry<VCSFileProxy, List<VCSFileProxy>> e : m.entrySet()) {
                doCmd(e.getKey(), e.getValue());
                for (VCSFileProxy f : e.getValue()) {
                    logger.output(MessageFormat.format(logMsgFormat, f.getName(), e.getKey()));
                }
            }
        }
        abstract void doCmd(VCSFileProxy repository, List<VCSFileProxy> candidates) throws HgException;
        static class AddCmd extends Cmd {
            public AddCmd(Map<VCSFileProxy, List<VCSFileProxy>> m, OutputLogger logger, String msgFormat, String msg) {
                super(m, logger, msgFormat, msg);
            }
            @Override
            void doCmd(VCSFileProxy repository, List<VCSFileProxy> candidates) throws HgException {
                HgCommand.doAdd(repository, candidates, logger);
            }
        }
        static class RemoveCmd extends Cmd {
            public RemoveCmd(Map<VCSFileProxy, List<VCSFileProxy>> m, OutputLogger logger, String msgFormat, String msg) {
                super(m, logger, msgFormat, msg);
            }
            @Override
            void doCmd(VCSFileProxy repository, List<VCSFileProxy> candidates) throws HgException {
                HgCommand.doRemove(repository, candidates, logger);
            }
        }
        static class CommitCmd extends Cmd {
            private HgHookContext context;
            private Collection<HgHook> hooks;
            private final HgProgressSupport support;
            private VCSFileProxy[] hookFiles;
            private final Map<VCSFileProxy, Set<VCSFileProxy>> rootFilesPerRepository;
            private final Map<VCSFileProxy, Set<VCSFileProxy>> refreshFilesPerRepository;
            private final Map<VCSFileProxy, Boolean> locallyModifiedExcluded;
            private final boolean closingBranch;
            private String originalMessage;
            private final String user;
            private final boolean afterMerge;

            public CommitCmd(Map<VCSFileProxy, List<VCSFileProxy>> m, OutputLogger logger, String commitMessage, HgProgressSupport support,
                    Map<VCSFileProxy, Set<VCSFileProxy>> rootFilesPerRepository, Map<VCSFileProxy, Boolean> locallyModifiedExcluded, Map<VCSFileProxy, Set<VCSFileProxy>> filesToRefresh,
                    String user, boolean closingBranch, boolean afterMerge) {
                super(m, logger, commitMessage, null);
                this.support = support;
                this.rootFilesPerRepository = rootFilesPerRepository;
                this.locallyModifiedExcluded = locallyModifiedExcluded;
                this.refreshFilesPerRepository = filesToRefresh;
                this.user = user;
                this.closingBranch = closingBranch;
                this.afterMerge = afterMerge;
            }

            public void setCommitHooks (HgHookContext context, Collection<HgHook> hooks, VCSFileProxy[] hookFiles, String originalMessage) {
                this.context = context;
                this.hooks = hooks;
                this.hookFiles = hookFiles;
                this.originalMessage = originalMessage;
            }

            @Override
            void handle() throws HgException {
                if(m.isEmpty()) {
                    return;
                }
                for (Entry<VCSFileProxy, List<VCSFileProxy>> e : m.entrySet()) {
                    doCmd(e.getKey(), e.getValue());
                }
            }

            @Override
            void doCmd(VCSFileProxy repository, List<VCSFileProxy> candidates) throws HgException {
                boolean commitAfterMerge = false;
                Set<VCSFileProxy> refreshFiles = new HashSet<>(candidates);
                List<VCSFileProxy> commitedFiles = null;
                try {
                    try {
                        if (afterMerge) {
                            if(commitAfterMerge(Boolean.TRUE.equals(locallyModifiedExcluded.get(repository)), repository)) {
                                HgCommand.doCommit(repository, Collections.<VCSFileProxy>emptyList(), msg, user, closingBranch, logger);
                                refreshFiles = new HashSet<>(Mercurial.getInstance().getSeenRoots(repository));
                                commitAfterMerge = true;
                            } else {
                                return;
                            }
                        } else {
                            HgCommand.doCommit(repository, candidates, msg, user, closingBranch, logger);
                            commitedFiles = candidates;
                        }
                    } catch (HgException.HgTooLongArgListException e) {
                        Mercurial.LOG.log(Level.INFO, null, e);
                        List<VCSFileProxy> reducedCommitCandidates;
                        StringBuilder offeredFileNames = new StringBuilder();
                        Set<VCSFileProxy> roots = rootFilesPerRepository.get(repository);
                        if (roots != null && roots.size() < 5) {
                            reducedCommitCandidates = new ArrayList<>(roots);
                            refreshFiles = new HashSet<>(roots);
                            for (VCSFileProxy f : reducedCommitCandidates) {
                                offeredFileNames.append('\n').append(f.getName());     //NOI18N
                            }
                        } else {
                            reducedCommitCandidates = Collections.<VCSFileProxy>emptyList();
                            refreshFiles = Collections.singleton(repository);
                            offeredFileNames.append('\n').append(repository.getName()); //NOI18N
                        }
                        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "MSG_LONG_COMMAND_QUERY", offeredFileNames.toString())); //NOI18N
                        descriptor.setTitle(NbBundle.getMessage(CommitAction.class, "MSG_LONG_COMMAND_TITLE")); //NOI18N
                        descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
                        descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

                        Object res = DialogDisplayer.getDefault().notify(descriptor);
                        if (res == NotifyDescriptor.NO_OPTION) {
                            return;
                        }
                        Mercurial.LOG.log(Level.INFO, "CommitAction: committing with a reduced set of files: {0}", reducedCommitCandidates.toString()); //NOI18N
                        HgCommand.doCommit(repository, reducedCommitCandidates, msg, user, closingBranch, logger);
                        commitedFiles = reducedCommitCandidates;
                    }
                } catch (HgException ex) {
                    if (HgCommand.COMMIT_AFTER_MERGE.equals(ex.getMessage())) {
                        // committing after a merge, all modified files have to be committed, even excluded files
                        // ask the user for confirmation
                        if (support.isCanceled()) {
                            return;
                        } else if(!commitAfterMerge(Boolean.TRUE.equals(locallyModifiedExcluded.get(repository)), repository)) {
                            return;
                        } else {
                            HgCommand.doCommit(repository, Collections.<VCSFileProxy>emptyList(), msg, user, closingBranch, logger);
                            refreshFiles = new HashSet<>(Mercurial.getInstance().getSeenRoots(repository));
                            commitAfterMerge = true;
                        }
                    } else {
                        throw ex;
                    }
                } finally {
                    refreshFilesPerRepository.put(repository, refreshFiles);
                    if(commitedFiles != null) {
                        Mercurial.getInstance().getMercurialHistoryProvider().fireHistoryChange(commitedFiles.toArray(new VCSFileProxy[commitedFiles.size()]));
                    }
                }

                HgLogMessage tip = HgCommand.doTip(repository, logger);
                
                context = new HgHookContext(hookFiles, originalMessage, new HgHookContext.LogEntry(
                        tip.getMessage(),
                        tip.getAuthor(),
                        tip.getCSetShortID(),
                        tip.getDate()));
                for (HgHook hook : hooks) {
                    hook.afterCommit(context);
                }

                if (commitAfterMerge) {
                    logger.output(
                            NbBundle.getMessage(CommitAction.class,
                            "MSG_COMMITED_FILES_AFTER_MERGE"));         //NOI18N
                } else {
                    if (candidates.size() == 1) {
                        logger.output(
                                NbBundle.getMessage(CommitAction.class,
                                "MSG_COMMIT_INIT_SEP_ONE", candidates.size())); //NOI18N
                    } else if (!candidates.isEmpty()) {
                        logger.output(
                                NbBundle.getMessage(CommitAction.class,
                                "MSG_COMMIT_INIT_SEP", candidates.size())); //NOI18N
                    }
                    for (VCSFileProxy f : candidates) {
                        logger.output("\t" + f.getPath());      //NOI18N
                    }
                }
                HgUtils.logHgLog(tip, logger);
                if (closingBranch) {
                    String branchName;
                    try {
                        branchName = HgCommand.getBranch(repository);
                    } catch (HgException ex) {
                        branchName = ""; //NOI18N
                    }
                    logger.output(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_BRANCH_CLOSED", branchName)); //NOI18N
                }
            }
        }
    }
}

