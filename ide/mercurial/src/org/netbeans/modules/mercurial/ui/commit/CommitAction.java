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
package org.netbeans.modules.mercurial.ui.commit;

import java.awt.event.ActionEvent;
import java.io.IOException;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.status.StatusAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mercurial.WorkingCopyInfo;
import org.netbeans.modules.mercurial.config.HgConfigFiles;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.repository.HgURL;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Utilities;

/**
 * Commit action for mercurial:
 * hg commit -  commit the specified files or all outstanding changes
 *
 * @author John Rice
 */
public class CommitAction extends ContextAction {

    static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage"; // NOI18N
    static final String KEY_CANCELED_MESSAGE = "commit"; //NOI18N
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/commit.png"; //NOI18N
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
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
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
        final File root = HgUtils.getRootFile(context);
        if (root == null) {
            OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed( NbBundle.getMessage(CommitAction.class,"MSG_COMMIT_TITLE")); // NOI18N
            logger.outputInRed( NbBundle.getMessage(CommitAction.class,"MSG_COMMIT_TITLE_SEP")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            logger.output(""); // NOI18N
            logger.closeLog();
            JOptionPane.showMessageDialog(Utilities.findDialogParent(),
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String contentTitle = Utils.getContextDisplayName(context);

        commit(contentTitle, context);
    }

    @NbBundle.Messages({
        "# {0} - repository name", "MSG_CommitAction.interruptedRebase.error=Repository {0} is in the middle of an interrupted rebase.\n"
            + "Finish the rebase before committing changes."
    })
    public static void commit (final String contentTitle, final VCSContext ctx) {
        Utils.post(new Runnable() {
            @Override
            public void run () {
                File root = HgUtils.getRootFile(ctx);
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
        final File repository = HgUtils.getRootFile(ctx);
        final boolean closingBranch = branchName != null;
        if (repository == null) {
            return;
        }

        // show commit dialog
        final CommitPanel panel = new CommitPanel();
        final Collection<HgHook> hooks = VCSHooks.getInstance().getHooks(HgHook.class);

        panel.setHooks(hooks, new HgHookContext(ctx.getRootFiles().toArray( new File[ctx.getRootFiles().size()]), null, new HgHookContext.LogEntry[] {}));
        final CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS, new String[] {CommitTableModel.COLUMN_NAME_PATH });

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
        WindowListener windowListener = new DialogBoundsPreserver(HgModuleConfig.getDefault().getPreferences(), "hg.commit.dialog"); // NOI18N
        dialog.addWindowListener(windowListener);
        dialog.pack();
        windowListener.windowOpened(new WindowEvent(dialog, WindowEvent.WINDOW_OPENED));
        dialog.setVisible(true);
        
        if (incomingChanges != null) {
            incomingChanges.cancel();
        }

        final String message = panel.getCommitMessage().trim();
        if (dd.getValue() != commitButton && !message.isEmpty()) {
            HgModuleConfig.getDefault().setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE, message);
        }
        if (dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
            al.actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, null));
            panel.closed();
        } else if (dd.getValue() == commitButton) {
            panel.closed();
            final Map<HgFileNode, CommitOptions> commitFiles = data.getCommitFiles();
            final Map<File, Set<File>> rootFiles = HgUtils.sortUnderRepository(ctx, true);
            final boolean commitAllFiles = panel.cbAllFiles.isSelected() || afterMerge.get();
            HgModuleConfig.getDefault().setLastCanceledCommitMessage(KEY_CANCELED_MESSAGE, ""); //NOI18N
            Utils.insert(HgModuleConfig.getDefault().getPreferences(), RECENT_COMMIT_MESSAGES, message.trim(), 20);
            final String user = panel.getUser();
            if (user != null) {
                HgModuleConfig.getDefault().putRecentCommitAuthors(user);
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

    private static void computeNodes(final CommitTable table, final CommitPanel panel, final VCSContext ctx, final File repository, JButton cancel, final AtomicBoolean afterMerge) {
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
                    File[] roots = ctx.getRootFiles().toArray(new File[ctx.getRootFiles().size()]);

                    File[][] split = Utils.splitFlatOthers(roots);
                    List<File> fileList = new ArrayList<File>();
                    for (int c = 0; c < split.length; c++) {
                        roots = split[c];
                        boolean recursive = c == 1;
                        if (recursive) {
                            Set<File> repositories = HgUtils.getRepositoryRoots(ctx);
                            File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
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
                            File[] files = HgUtils.flatten(roots, FileInformation.STATUS_LOCAL_CHANGE);
                            for (int i= 0; i<files.length; i++) {
                                if(!fileList.contains(files[i])) {
                                    fileList.add(files[i]);
                                }
                            }
                        }
                    }

                    ArrayList<HgFileNode> nodesList = new ArrayList<HgFileNode>(fileList.size());

                    for (Iterator<File> it = fileList.iterator(); it.hasNext();) {
                        File file = it.next();
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
                    config = HgConfigFiles.getSysInstance();
                    userName = config.getUserName(false);
                }
                List<String> recentUsers = HgModuleConfig.getDefault().getRecentCommitAuthors();
                if (!userName.isEmpty()) {
                    recentUsers.remove(userName);
                    recentUsers.add(0, userName);
                }
                final ComboBoxModel<String> model = new DefaultComboBoxModel<String>(recentUsers.toArray(new String[0]));
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
    @NbBundle.Messages({
        "MSG_CommitForm_ErrorInvalidAuthor=Invalid author"
    })
    private static void refreshCommitDialog(CommitPanel panel, CommitTable table, JButton commit, String branchToClose, AtomicBoolean afterMerge) {
        assert EventQueue.isDispatchThread();
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        Map<HgFileNode, CommitOptions> files = table.getCommitFiles();
        boolean errors = false;

        boolean enabled = commit.isEnabled();

        for (Entry<HgFileNode, CommitOptions> entry : files.entrySet()) {

            HgFileNode fileNode = entry.getKey();
            CommitOptions options = entry.getValue();
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
            Map<File, Set<File>> rootFiles, HgProgressSupport support, OutputLogger logger, Collection<HgHook> hooks) {
        performCommit(message, commitFiles, rootFiles, support, logger, hooks, null, false, false, false);
    }

    private static void performCommit(String message, Map<HgFileNode, CommitOptions> commitFiles,
            Map<File, Set<File>> rootFiles, HgProgressSupport support, OutputLogger logger, Collection<HgHook> hooks,
            String user, boolean commitAllFiles, boolean closeBranch, boolean afterMerge) {
        FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
        Map<File, List<File>> addCandidates = new HashMap<File, List<File>>();
        Map<File, List<File>> deleteCandidates = new HashMap<File, List<File>>();
        Map<File, List<File>> commitCandidates = new HashMap<File, List<File>>();
        Map<File, Set<File>> filesToRefresh = new HashMap<File, Set<File>>();

        List<String> excPaths = new ArrayList<String>();
        Map<File, Boolean> locallyModifiedExcluded = new HashMap<File, Boolean>();
        List<String> incPaths = new ArrayList<String>();
        if (commitAllFiles && closeBranch) {
            assert rootFiles.size() == 1;
            for (File root : rootFiles.keySet()) {
                commitCandidates.put(root, Collections.<File>emptyList());
            }
        } else {
            for (Map.Entry<HgFileNode, CommitOptions> e : commitFiles.entrySet()) {
                 if (support.isCanceled()) {
                     return;
                 }
                 HgFileNode node = e.getKey();
                 CommitOptions option = e.getValue();
                 File repository = Mercurial.getInstance().getRepositoryRoot(node.getFile());
                 if (option != CommitOptions.EXCLUDE) {
                     int  status = cache.getStatus(node.getFile()).getStatus();
                     if ((status & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0) {
                         putCandidate(addCandidates, repository, node.getFile());
                     } else  if ((status & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) {
                         putCandidate(deleteCandidates, repository, node.getFile());
                     }
                     putCandidate(commitCandidates, repository, node.getFile());
                     incPaths.add(node.getFile().getAbsolutePath());
                 }else{
                     excPaths.add(node.getFile().getAbsolutePath());
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
            HgModuleConfig.getDefault().addExclusionPaths(excPaths);
        }
        if (!incPaths.isEmpty()) {
            HgModuleConfig.getDefault().removeExclusionPaths(incPaths);
        }

        try {
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(CommitAction.class,
                    "MSG_COMMIT_TITLE_SEP")); // NOI18N
            logger.output(message); // NOI18N

            new Cmd.AddCmd(addCandidates, logger, null, "hg add {0} into {1}").handle();
            new Cmd.RemoveCmd(deleteCandidates, logger, null, "hg delete {0} from {1}").handle();
            removeDeletedTemporaryFiles(commitCandidates, deleteCandidates);
            if (support.isCanceled()) {
                return;
            }

            File[] hookFiles = null;
            if(hooks.size() > 0) {
                List<File> candidates = new LinkedList<File>();
                for (List<File> values : commitCandidates.values()) {
                    candidates.addAll(values);
                }
                hookFiles = candidates.toArray(new File[0]);
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

    private static void refreshFS (Map<File, Set<File>> filesPerRepository) {
        final Set<File> files = new HashSet<File>();
        for (Set<File> values : filesPerRepository.values()) {
            files.addAll(values);
        }
        Mercurial.getInstance().getParallelRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                FileUtil.refreshFor(files.toArray(new File[0]));
            }
        }, 100);
    }

    private static void putCandidate(Map<File, List<File>> m, File repository, File file) {
        List<File> l = m.get(repository);
        if(l == null) {
            l = new ArrayList<File>();
            m.put(repository, l);
        }
        l.add(file);
    }

    private static boolean commitAfterMerge (boolean locallyModifiedExcluded, File repository) {
        // XXX consider usage of repository to determine if there are any non-included files which have to be committed, too
        // and thus removing the option HgModuleConfig.getDefault().getConfirmCommitAfterMerge()
        if (locallyModifiedExcluded || HgModuleConfig.getDefault().getConfirmCommitAfterMerge()) { // ask before commit?
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_AFTER_MERGE_QUERY")); // NOI18N
            descriptor.setTitle(NbBundle.getMessage(CommitAction.class, "MSG_COMMIT_AFTER_MERGE_TITLE")); // NOI18N
            descriptor.setMessageType(JOptionPane.WARNING_MESSAGE);
            descriptor.setOptionType(NotifyDescriptor.YES_NO_OPTION);

            Object res = DialogDisplayer.getDefault().notify(descriptor);
            return res == NotifyDescriptor.YES_OPTION;
        }
        return true;
    }
    
    @NbBundle.Messages({
        "MSG_CommitAction.warning.incomingChanges=There are incoming changes. You should pull from the remote repository first."
    })
    private static HgProgressSupport checkForIncomingChanges (final File repository, final CommitPanel panel,
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
                            if (pushUrl.getScheme().toString().contains("ssh")) {
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
    private static void removeDeletedTemporaryFiles (Map<File, List<File>> commitCandidates, Map<File, List<File>> deleteCandidates) {
        for (Entry<File, List<File>> e : deleteCandidates.entrySet()) {
            File root = e.getKey();
            List<File> files = e.getValue();
            if (!files.isEmpty()) {
                try {
                    List<File> commitFiles = commitCandidates.get(root);
                    Map<File, FileInformation> status = HgCommand.getStatus(root, files, null, null);
                    for (File f : files) {
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

    private abstract static class Cmd {
        protected final Map<File, List<File>> m;
        protected final OutputLogger logger;
        protected final String logMsgFormat;
        protected final String msg;
        public Cmd(Map<File, List<File>> m, OutputLogger logger, String msg, String logMsgFormat) {
            this.m = m;
            this.logger = logger;
            this.logMsgFormat = logMsgFormat;
            this.msg = msg;
        }
        void handle() throws HgException {
            if(m.isEmpty()) return;
            for (Entry<File, List<File>> e : m.entrySet()) {
                doCmd(e.getKey(), e.getValue());
                for (File f : e.getValue()) {
                    logger.output(MessageFormat.format(logMsgFormat, f.getName(), e.getKey()));
                }
            }
        }
        abstract void doCmd(File repository, List<File> candidates) throws HgException;
        static class AddCmd extends Cmd {
            public AddCmd(Map<File, List<File>> m, OutputLogger logger, String msgFormat, String msg) {
                super(m, logger, msgFormat, msg);
            }
            @Override
            void doCmd(File repository, List<File> candidates) throws HgException {
                HgCommand.doAdd(repository, candidates, logger);
            }
        }
        static class RemoveCmd extends Cmd {
            public RemoveCmd(Map<File, List<File>> m, OutputLogger logger, String msgFormat, String msg) {
                super(m, logger, msgFormat, msg);
            }
            @Override
            void doCmd(File repository, List<File> candidates) throws HgException {
                HgCommand.doRemove(repository, candidates, logger);
            }
        }
        static class CommitCmd extends Cmd {
            private HgHookContext context;
            private Collection<HgHook> hooks;
            private final HgProgressSupport support;
            private File[] hookFiles;
            private final Map<File, Set<File>> rootFilesPerRepository;
            private final Map<File, Set<File>> refreshFilesPerRepository;
            private final Map<File, Boolean> locallyModifiedExcluded;
            private final boolean closingBranch;
            private String originalMessage;
            private final String user;
            private boolean afterMerge;

            public CommitCmd(Map<File, List<File>> m, OutputLogger logger, String commitMessage, HgProgressSupport support,
                    Map<File, Set<File>> rootFilesPerRepository, Map<File, Boolean> locallyModifiedExcluded, Map<File, Set<File>> filesToRefresh,
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

            public void setCommitHooks (HgHookContext context, Collection<HgHook> hooks, File[] hookFiles, String originalMessage) {
                this.context = context;
                this.hooks = hooks;
                this.hookFiles = hookFiles;
                this.originalMessage = originalMessage;
            }

            @Override
            void handle() throws HgException {
                if(m.isEmpty()) return;
                for (Entry<File, List<File>> e : m.entrySet()) {
                    doCmd(e.getKey(), e.getValue());
                }
            }

            @Override
            void doCmd(File repository, List<File> candidates) throws HgException {
                boolean commitAfterMerge = false;
                Set<File> refreshFiles = new HashSet<File>(candidates);
                List<File> commitedFiles = null;
                try {
                    try {
                        if (afterMerge) {
                            if(commitAfterMerge(Boolean.TRUE.equals(locallyModifiedExcluded.get(repository)), repository)) {
                                HgCommand.doCommit(repository, Collections.<File>emptyList(), msg, user, closingBranch, logger);
                                refreshFiles = new HashSet<File>(Mercurial.getInstance().getSeenRoots(repository));
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
                        List<File> reducedCommitCandidates;
                        String offeredFileNames = "";                       //NOI18N
                        Set<File> roots = rootFilesPerRepository.get(repository);
                        if (roots != null && roots.size() < 5) {
                            reducedCommitCandidates = new ArrayList<File>(roots);
                            refreshFiles = new HashSet<File>(roots);
                            for (File f : reducedCommitCandidates) {
                                offeredFileNames += "\n" + f.getName();     //NOI18N
                            }
                        } else {
                            reducedCommitCandidates = Collections.<File>emptyList();
                            refreshFiles = Collections.singleton(repository);
                            offeredFileNames = "\n" + repository.getName(); //NOI18N
                        }
                        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(NbBundle.getMessage(CommitAction.class, "MSG_LONG_COMMAND_QUERY", offeredFileNames)); //NOI18N
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
                            HgCommand.doCommit(repository, Collections.<File>emptyList(), msg, user, closingBranch, logger);
                            refreshFiles = new HashSet<File>(Mercurial.getInstance().getSeenRoots(repository));
                            commitAfterMerge = true;
                        }
                    } else {
                        throw ex;
                    }
                } finally {
                    refreshFilesPerRepository.put(repository, refreshFiles);
                    if(commitedFiles != null) {
                        Mercurial.getInstance().getMercurialHistoryProvider().fireHistoryChange(commitedFiles.toArray(new File[0]));
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
                    for (File f : candidates) {
                        logger.output("\t" + f.getAbsolutePath());      //NOI18N
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

