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

package org.netbeans.modules.subversion.ui.commit;

import java.io.IOException;
import java.text.ParseException;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.SVNBaseDir;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.text.DateFormat;
import java.util.*;
import java.util.List;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.PanelProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ActionUtils;
import org.netbeans.modules.versioning.hooks.SvnHook;
import org.netbeans.modules.versioning.hooks.SvnHookContext;
import org.netbeans.modules.subversion.ui.diff.DiffNode;
import org.netbeans.modules.subversion.ui.status.SyncFileNode;
import org.netbeans.modules.subversion.util.ClientCheckSupport;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.hooks.VCSHooks;
import org.netbeans.modules.versioning.util.TableSorter;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Commit action
 *
 * @author Petr Kuzel
 */
@NbBundle.Messages({
    "CTL_MenuItem_Commit=Co&mmit...",
    "CTL_MenuItem_Commit_Context=Co&mmit...",
    "CTL_MenuItem_Commit_Context_Multiple=Co&mmit Files...",
    "# {0} - number of selected projects",
    "CTL_MenuItem_Commit_Projects=Co&mmit {0} Projects..."
})
public class CommitAction extends ContextAction {

    public static final String RECENT_COMMIT_MESSAGES = "recentCommitMessage";
    private static final String PANEL_PREFIX = "commit"; //NOI18N
    private static final String ICON_RESOURCE = "org/netbeans/modules/subversion/resources/icons/commit.png"; //NOI18N
    private static final long COMMIT_PAUSE = Long.getLong("versioning.subversion.commit.pause", 5000); //NOI18N
    private static final String ERROR_COLOR;
    private static final String INFO_COLOR;
    static {
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            ERROR_COLOR = "#CC0000"; //NOI18N
        } else {
            ERROR_COLOR = SvnUtils.getColorString(c);
        }
        c = UIManager.getColor("nb.warningForeground"); //NOI18N
        if (c == null) {
            INFO_COLOR = "#002080"; //NOI18N
        } else {
            INFO_COLOR = SvnUtils.getColorString(c);
        }
    }

    public CommitAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Commit";    // NOI18N
    }

    @Override
    protected boolean enable(Node[] nodes) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if(!isSvnNodes(nodes) && !isDeepRefreshDisabledGlobally()) {
            // allway true as we have will accept and check for external changes
            // and we don't about them yet
            return cache.ready();
        }
        // XXX could be a performace issue, maybe a msg box in commit would be enough
        return cache.ready() && cache.containsFiles(getCachedContext(nodes), FileInformation.STATUS_LOCAL_CHANGE, true);
    }

    /** Run commit action. Shows UI */
    public static void commit(String contentTitle, Context ctx, boolean deepScanEnabled) {
        if(!Subversion.getInstance().checkClientAvailable()) {
            return;
        }
        if (ctx.getRoots().size() < 1) {
            Subversion.LOG.info("Svn context contains no files");       //NOI18N
            return;
        }
        commitChanges(contentTitle, ctx, deepScanEnabled && !isDeepRefreshDisabledGlobally());
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    /**
     * Returns true if the given nodes are from the versioning view or the diff view.
     * In such case the deep scan is not required because the files and their statuses should already be known
     * @param nodes
     * @return
     */
    private static boolean isSvnNodes (Node[] nodes) {
        boolean fromSubversionView = true;
        for (Node node : nodes) {
            if (!(node instanceof SyncFileNode || node instanceof DiffNode)) {
                fromSubversionView = false;
                break;
            }
        }
        return fromSubversionView;
    }

    private static boolean isDeepRefreshDisabledGlobally () {
        return "false".equals(System.getProperty("netbeans.subversion.commit.deepStatusRefresh")); // NOI18N
    }

    /**
     * Opens the commit dialog displaying changed files from the status cache which belong to the given context.
     * If deepScan switch is enabled, the status for files will be refrehed first and the commit button in the dialog stays disabled until then
     * and it may take a while until the dialog is setup.
     *
     * @param contentTitle
     * @param ctx
     * @param deepScanEnabled
     */
    private static void commitChanges(String contentTitle, final Context ctx, boolean deepScanEnabled) {
        final CommitPanel panel = new CommitPanel();
        Collection<SvnHook> hooks = VCSHooks.getInstance().getHooks(SvnHook.class);
        File file = ctx.getRootFiles()[0];
        panel.setHooks(hooks, new SvnHookContext(new File[] { file }, null, null));

        Map<String, Integer> sortingStatus = SvnModuleConfig.getDefault().getSortingStatus(PANEL_PREFIX);
        if (sortingStatus == null) {
            sortingStatus = Collections.singletonMap(CommitTableModel.COLUMN_NAME_PATH, TableSorter.ASCENDING);
        }
        final CommitTable data = new CommitTable(panel.filesLabel, CommitTable.COMMIT_COLUMNS, sortingStatus);
        panel.setCommitTable(data);
        data.setCommitPanel(panel);
        final JButton commitButton = new JButton();

        // start backround prepare
        SVNUrl repository = null;
        try {
            repository = getSvnUrl(ctx);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
        }
        List<File> roots = ctx.getRoots();
        SvnProgressSupport prepareSupport = getProgressSupport(ctx, roots, data, panel.progressPanel, deepScanEnabled);
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        prepareSupport.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "BK1009")); // NOI18N

        // show commit dialog
        boolean startCommit = showCommitDialog(panel, data, commitButton, contentTitle, ctx) == commitButton;
        String message = panel.getCommitMessage().trim();
        if (!startCommit && !message.isEmpty()) {
            SvnModuleConfig.getDefault().setLastCanceledCommitMessage(message);
        }
        SvnModuleConfig.getDefault().setSortingStatus(PANEL_PREFIX, data.getSortingState());
        if (startCommit) {
            // if OK setup sequence of add, remove and commit calls
            startCommitTask(panel, data, ctx, roots.toArray(new File[0]), hooks);
        } else {
            prepareSupport.cancel();
        }
    }

    private static Set<File> getUnversionedParents(Collection<File> files, boolean onlyCached) {
        Set<File> checked = new HashSet<File>();
        Set<File> ret = new HashSet<File>();
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        for (File file : files) {
            File parent = file;
            while((parent = parent.getParentFile()) != null) {
                if (checked.contains(parent)) {
                    break;
                }
                checked.add(parent);
                if (files.contains(parent)) {
                    break;
                }
                if (!SvnUtils.isManaged(parent)) {
                    break;
                }
                FileInformation info = onlyCached ? cache.getCachedStatus(parent) : cache.getStatus(parent);
                if (info == null) {
                    continue;
                }
                if(info.getStatus() == FileInformation.STATUS_VERSIONED_ADDEDLOCALLY ||
                   info.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY)
                {
                    ret.add(parent);
                }
            }
        }
        return ret;
    }

    /**
     * Returns a SvnFileNode for each given file
     *
     * @param files
     * @param supp running progress support
     * @return
     */
    private static SvnFileNode[] getFileNodes(final Collection<File> files, final SvnProgressSupport supp) {
        SvnFileNode[] nodes;
        final ArrayList<SvnFileNode> nodesList = new ArrayList<SvnFileNode>(files.size());

        SvnUtils.runWithInfoCache(new Runnable() {
            @Override
            public void run () {
                for (Iterator<File> it = files.iterator(); it.hasNext();) {
                    if (supp.isCanceled()) {
                        break;
                    }
                    File file = it.next();
                    SvnFileNode node = new SvnFileNode(file);
                    // initialize node properties
                    node.initializeProperties();
                    nodesList.add(node);
                }
            }
        });
        nodes = nodesList.toArray(new SvnFileNode[0]);
        return nodes;
    }

    /**
     * Opens the commit dlg
     *
     * @param panel
     * @param data
     * @param commitButton
     * @param contentTitle
     * @param ctx
     * @return
     */
    private static Object showCommitDialog(final CommitPanel panel, final CommitTable data, final JButton commitButton, String contentTitle, final Context ctx) {
        org.openide.awt.Mnemonics.setLocalizedText(commitButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Commit")); // NOI18N
        commitButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Commit")); // NOI18N
        commitButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Commit")); // NOI18N
        final JButton cancelButton = new JButton(org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(CommitAction.class, "CTL_Commit_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSN_Commit_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CommitAction.class, "ACSD_Commit_Action_Cancel")); // NOI18N
        cancelButton.setDefaultCapable(false);

        commitButton.setEnabled(false);

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
        panel.addVersioningListener(new VersioningListener() {
            @Override
            public void versioningEvent(VersioningEvent event) {
                refreshCommitDialog(panel, data, commitButton);
            }
        });
        data.getTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                refreshCommitDialog(panel, data, commitButton);
            }
        });
        commitButton.setEnabled(containsCommitable(data));

        panel.putClientProperty("contentTitle", contentTitle);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        WindowListener windowListener = new DialogBoundsPreserver(SvnModuleConfig.getDefault().getPreferences(), "svn.commit.dialog"); // NOI18N
        dialog.addWindowListener(windowListener);
        dialog.pack();
        windowListener.windowOpened(new WindowEvent(dialog, WindowEvent.WINDOW_OPENED));
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
            al.actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, null));
        }
        return dd.getValue();
    }

    private static void startCommitTask(final CommitPanel panel, final CommitTable data, final Context ctx, final File[] rootFiles, final Collection<SvnHook> hooks) {
        final Map<SvnFileNode, CommitOptions> commitFiles = data.getCommitFiles();
        final String message = panel.getCommitMessage();
        SvnModuleConfig.getDefault().setLastCanceledCommitMessage(""); //NOI18N
        org.netbeans.modules.versioning.util.Utils.insert(SvnModuleConfig.getDefault().getPreferences(), RECENT_COMMIT_MESSAGES, message.trim(), 20);

        SVNUrl repository = null;
        try {
            repository = getSvnUrl(ctx);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            public void perform() {
                performCommit(message, commitFiles, ctx, rootFiles, this, hooks);
            }
        };
        support.start(rp, repository, org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N
    }

    private static SvnProgressSupport getProgressSupport (final Context ctx, final List<File> roots, final CommitTable data, JPanel progressPanel, final boolean deepScanEnabled) {
        SvnProgressSupport support = new PanelProgressSupport(progressPanel) {
            
            @Override
            public void perform() {
                // get files without exclusions
                File[] contextFiles = ctx.getFiles();
                if (contextFiles.length == 0) {
                    return;
                }

                // The commits are made non recursively, so
                // add also the roots to the to be commited list.
                Set<File> filesSet = new HashSet<File>();
                filesSet.addAll(Arrays.asList(contextFiles));
                for (File file : roots) {
                    filesSet.add(file);
                }
                contextFiles = filesSet.toArray(new File[0]);

                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                if (deepScanEnabled) {
                    // make a deep refresh to get the not yet notified external changes
                    for (File f : contextFiles) {
                        if (isCanceled()) {
                            return;
                        }
                        cache.refreshRecursively(f);
                    }
                }
                // get all changed files while honoring the flat folder logic
                File[][] split = Utils.splitFlatOthers(contextFiles);
                Set<File> fileSet = new LinkedHashSet<File>();
                for (int c = 0; c < split.length; c++) {
                    contextFiles = split[c];
                    boolean recursive = c == 1;
                    if (recursive) {
                        File[] files = cache.listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);
                        for (int i = 0; i < files.length; i++) {
                            for (int r = 0; r < contextFiles.length; r++) {
                                if (isCanceled()) {
                                    return;
                                }
                                if (SvnUtils.isParentOrEqual(contextFiles[r], files[i])) {
                                    if (!fileSet.contains(files[i])) {
                                        fileSet.add(files[i]);
                                    }
                                }
                            }
                        }
                    } else {
                        if (isCanceled()) {
                            return;
                        }
                        File[] files = SvnUtils.flatten(contextFiles, FileInformation.STATUS_LOCAL_CHANGE);
                        for (int i = 0; i < files.length; i++) {
                            if (!fileSet.contains(files[i])) {
                                fileSet.add(files[i]);
                            }
                        }
                    }
                }

                if (fileSet.isEmpty()) {
                    return;
                }
                fileSet.addAll(getUnversionedParents(fileSet, false));
                roots.addAll(addDeletedFiles(fileSet, cache));
                final SvnFileNode[] nodes = getFileNodes(fileSet, this);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        data.setNodes(nodes);
                    }
                });
            }

            private Collection<File> addDeletedFiles (Set<File> fileSet, FileStatusCache cache) {
                List<File> added = new LinkedList<File>();
                // at first fill with already scheduled deletes
                Map<SVNUrl, File> deletedCandidates = new HashMap<SVNUrl, File>();
                for (File f : fileSet) {
                    FileInformation fi = cache.getCachedStatus(f);
                    ISVNStatus st;
                    if (fi != null && (fi.getStatus() & (FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) != 0 && (st = fi.getEntry(null)) != null) {
                        if (checkUrl(st, f)) {
                            deletedCandidates.put(st.getUrl(), f);
                        }
                    }
                }
                for (File f : fileSet) {
                    // try to locate a deleted source
                    FileInformation fi = cache.getCachedStatus(f);
                    ISVNStatus st;
                    SVNUrl copiedUrl;
                    if (fi != null && (fi.getStatus() & FileInformation.STATUS_VERSIONED_ADDEDLOCALLY) != 0 && (st = fi.getEntry(f)) != null 
                            && st.isCopied() && (copiedUrl = SvnUtils.getCopiedUrl(f)) != null && !deletedCandidates.containsKey(copiedUrl)) {
                        if (checkUrl(st, f)) {
                            // file is copied, it means it has a source file copied from
                            File copiedFrom = getCopiedFromFile(st, f, copiedUrl); 
                            fi = cache.getCachedStatus(copiedFrom);
                            // if the source is deleted add it into the candidate list
                            if (fi != null && (fi.getStatus() & (FileInformation.STATUS_VERSIONED_DELETEDLOCALLY | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) != 0) {
                                deletedCandidates.put(copiedUrl, copiedFrom);
                            }
                        }
                    }
                }
                // add deletes not yet scheduled
                for (File f : deletedCandidates.values()) {
                    if (!fileSet.contains(f)) {
                        added.add(f);
                        fileSet.add(f);
                    }
                }
                return added;
            }

            private File getCopiedFromFile (ISVNStatus st, File f, SVNUrl copiedUrl) {
                String relativized = "."; //NOI18N
                String[] urlSegments = SvnUtils.decode(st.getUrl()).getPathSegments();
                String[] copiedUrlSegments = SvnUtils.decode(copiedUrl).getPathSegments();
                int i = 0;
                for (; i < Math.min(urlSegments.length, copiedUrlSegments.length); ++i) {
                    if (!urlSegments[i].equals(copiedUrlSegments[i])) {
                        break;
                    }
                }
                for (int j = i; j < urlSegments.length; ++j) {
                    relativized += "/.."; //NOI18N
                }
                for (int j = i; j < copiedUrlSegments.length; ++j) {
                    relativized += "/" + copiedUrlSegments[j]; //NOI18N
                }
                File copiedFrom = FileUtil.normalizeFile(new File(f, relativized.replace("/", File.separator))); //NOI18N
                return copiedFrom;
            }

            private boolean checkUrl (ISVNStatus st, File f) {
                if (st.getUrl() == null) {
                    Subversion.LOG.log(Level.INFO, null, new IllegalStateException("Null URL for: " + f.getAbsolutePath() + ", " + st));
                    return false;
                }
                return true;
            }
        };
        return support;
    }

    private static boolean containsCommitable(CommitTable data) {
        Map<SvnFileNode, CommitOptions> map = data.getCommitFiles();
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
    private static void refreshCommitDialog(CommitPanel panel, CommitTable table, JButton commit) {
        assert EventQueue.isDispatchThread();
        ResourceBundle loc = NbBundle.getBundle(CommitAction.class);
        Map<SvnFileNode, CommitOptions> files = table.getCommitFiles();
        Set<String> stickyTags = new HashSet<String>();
        boolean conflicts = false;

        boolean enabled = commit.isEnabled();
        commit.setEnabled(false);

        for (Map.Entry<SvnFileNode, CommitOptions> entry : files.entrySet()) {
            SvnFileNode fileNode = entry.getKey();
            CommitOptions options = entry.getValue();
            if (options == CommitOptions.EXCLUDE) {
                continue;
            }
            stickyTags.add(fileNode.getCopy());
            int status = fileNode.getInformation().getStatus();
            if ((status & FileInformation.STATUS_REMOTE_CHANGE) != 0 || (status & FileInformation.STATUS_VERSIONED_CONFLICT) != 0) {
                enabled = false;
                String msg = (status & FileInformation.STATUS_VERSIONED_CONFLICT) != 0 ?
                        loc.getString("MSG_CommitForm_ErrorConflicts") :
                        loc.getString("MSG_CommitForm_ErrorRemoteChanges");
                panel.setErrorLabel("<html><font color=\"" + INFO_COLOR + "\">" + msg + "</font></html>");  // NOI18N
                conflicts = true;
            }
        }

        if (stickyTags.size() > 1) {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_COMMIT, CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_BRANCH,
                                                CommitTableModel.COLUMN_NAME_STATUS, CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        } else {
            table.setColumns(new String [] { CommitTableModel.COLUMN_NAME_COMMIT, CommitTableModel.COLUMN_NAME_NAME, CommitTableModel.COLUMN_NAME_STATUS,
                                                CommitTableModel.COLUMN_NAME_ACTION, CommitTableModel.COLUMN_NAME_PATH });
        }

        String contentTitle = (String) panel.getClientProperty("contentTitle"); // NOI18N
        DialogDescriptor dd = (DialogDescriptor) panel.getClientProperty("DialogDescriptor"); // NOI18N
        String errorLabel;
        if (stickyTags.size() <= 1) {
            String stickyTag = stickyTags.isEmpty() ? null : (String) stickyTags.iterator().next();
            if (stickyTag == null) {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title"), new Object [] { contentTitle }));
                errorLabel = ""; // NOI18N
            } else {
                dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branch"), new Object [] { contentTitle, stickyTag }));
                String msg = MessageFormat.format(loc.getString("MSG_CommitForm_InfoBranch"), new Object [] { stickyTag });
                errorLabel = "<html><font color=\"" + INFO_COLOR + "\">" + msg + "</font></html>"; // NOI18N
            }
        } else {
            dd.setTitle(MessageFormat.format(loc.getString("CTL_CommitDialog_Title_Branches"), new Object [] { contentTitle }));
            String msg = loc.getString("MSG_CommitForm_ErrorMultipleBranches");
            errorLabel = "<html><font color=\"" + ERROR_COLOR + "\">" + msg + "</font></html>"; // NOI18N
        }
        if (!conflicts) {
            panel.setErrorLabel(errorLabel);
            enabled = true;
        }
        commit.setEnabled(enabled && containsCommitable(table));
    }

    @Override
    protected void performContextAction(final Node[] nodes) {
        ClientCheckSupport.getInstance().runInAWTIfAvailable(ActionUtils.cutAmpersand(getRunningName(nodes)), new Runnable() {
            @Override
            public void run() {
                Context ctx = getContext(nodes);
                commit(getContextDisplayName(nodes), ctx, !isSvnNodes(nodes));
            }
        });
    }

    private static void performCommit(String message, Map<SvnFileNode, CommitOptions> commitFiles, Context ctx, File[] rootFiles, SvnProgressSupport support, Collection<SvnHook> hooks) {
        SvnClient client = getClient(ctx, support);
        if(client == null) {
            return;
        }
        performCommit(client, message, commitFiles, rootFiles, support, false, hooks);
    }

    public static void performCommit(String message, Map<SvnFileNode, CommitOptions> commitFiles, Context ctx, SvnProgressSupport support, boolean rootUpdate) {
        SvnClient client = getClient(ctx, support);
        if(client == null) {
            return;
        }
        performCommit(client, message, commitFiles, ctx.getRootFiles(), support, rootUpdate, new ArrayList<SvnHook>(0));
    }

    public static void performCommit(SvnClient client, String message, Map<SvnFileNode, CommitOptions> commitFiles, File[] rootFiles, SvnProgressSupport support, boolean rootUpdate, Collection<SvnHook> hooks) {
        try {
            support.setCancellableDelegate(client);
            client.addNotifyListener(support);
            support.setDisplayName(org.openide.util.NbBundle.getMessage(CommitAction.class, "LBL_Commit_Progress")); // NOI18N

            List<SvnFileNode> addCandidates = new ArrayList<SvnFileNode>();
            List<File> removeCandidates = new ArrayList<File>();
            List<File> missingFiles = new ArrayList<File>();
            Set<File> commitCandidates = new LinkedHashSet<File>();
            Set<File> binnaryCandidates = new HashSet<File>();

            Iterator<SvnFileNode> it = commitFiles.keySet().iterator();
            // XXX refactor the olowing loop. there seem to be redundant blocks
            while (it.hasNext()) {
                if(support.isCanceled()) {
                    return;
                }
                SvnFileNode node = it.next();
                CommitOptions option = commitFiles.get(node);
                if (CommitOptions.ADD_BINARY == option) {
                    List<File> l = listUnmanagedParents(node);
                    Iterator<File> dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }

                    if(support.isCanceled()) {
                        return;
                    }
                    binnaryCandidates.add(node.getFile());

                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.ADD_TEXT == option || CommitOptions.ADD_DIRECTORY == option) {
                    // assute no MIME property or startin gwith text
                    List<File> l = listUnmanagedParents(node);
                    Iterator<File> dit = l.iterator();
                    while (dit.hasNext()) {
                        if(support.isCanceled()) {
                            return;
                        }
                        File file = dit.next();
                        addCandidates.add(new SvnFileNode(file));
                        commitCandidates.add(file);
                    }
                    if(support.isCanceled()) {
                        return;
                    }
                    addCandidates.add(node);
                    commitCandidates.add(node.getFile());
                } else if (CommitOptions.COMMIT_REMOVE == option) {
                    removeCandidates.add(node.getFile());
                    commitCandidates.add(node.getFile());
                    if ((node.getInformation().getStatus() & FileInformation.STATUS_VERSIONED_DELETEDLOCALLY) != 0) {
                        missingFiles.add(node.getFile());
                    }
                } else if (CommitOptions.COMMIT == option) {
                    commitCandidates.add(node.getFile());
                } else {
                    Logger.getLogger(CommitAction.class.getName()).log(Level.FINEST, "Ignoring file for commit: {0}", node.getFile()); //NOI18N
                }
            }
            
            Logger.getLogger(CommitAction.class.getName()).log(Level.FINEST, "All commit candidates: {0}", commitCandidates); //NOI18N

            // perform adds
            performAdds(client, support, addCandidates);
            if(support.isCanceled()) {
                return;
            }

            // ensure all ignored properties are set.
            // This is more a hack than a clean solution but still seems to be
            // more reasonable than changing Subverion.isIgnored due to:
            // 1.) we didn't need it until now
            // 2.) the hilarious potential of Subverion.isIgnored and SQ to cause trouble ...
            setIgnoredProperties(client, support, addCandidates);
            if(support.isCanceled()) {
                return;
            }

            // TODO perform removes. especialy package removes where
            // metadata must be replied from SvnMetadata (hold by FileSyatemHandler)

            // set binary mimetype and group commitCandidates by managed trees
            List<List<File>> managedTrees = getManagedTrees(client, support, commitCandidates, binnaryCandidates);
            if(support.isCanceled()) {
                return;
            }

            List<ISVNLogMessage> logs = new ArrayList<ISVNLogMessage>();
            List<File> hookFiles = new ArrayList<File>();
            boolean handleHooks = false;
            String originalMessage = message;
            if(hooks.size() > 0) {
                for (List<File> l : managedTrees) {
                    hookFiles.addAll(l);
                }
                SvnHookContext context = new SvnHookContext(hookFiles.toArray(new File[0]), message, null);
                for (SvnHook hook : hooks) {
                    try {
                        // XXX handle returned context
                        context = hook.beforeCommit(context);
                        if(context != null) {
                            handleHooks = true;
                            message = context.getMessage();
                        }
                    } catch (IOException ex) {
                        // XXX handle veto
                    }
                }
            }
            if (!missingFiles.isEmpty()) {
                // we need to correct metadata for externally deleted files and folders
                deleteMissingFiles(missingFiles, client);
                if (support.isCanceled()) {
                    return;
                }
            }
            // finally commit
            Logger.getLogger(CommitAction.class.getName()).log(Level.FINEST, "All commit managed trees: {0} - {1}", new Object[] { managedTrees.size(), managedTrees } ); //NOI18N
            for (Iterator<List<File>> itCandidates = managedTrees.iterator(); itCandidates.hasNext();) {

                // one commit for each wc
                List<File> commitList = itCandidates.next();
                File[] commitedFiles = commitList.toArray(new File[0]);

                CommitCmd cmd = new CommitCmd(client, support, message, handleHooks ? logs : null);
                // handle recursive commits - deleted and copied folders can't be commited non recursively
                List<File> recursiveCommits = getRecursiveCommits(commitList, removeCandidates);
                Logger.getLogger(CommitAction.class.getName()).log(Level.FINEST, "Committing files: {0}", commitList); //NOI18N
                if(recursiveCommits.size() > 0) {
                    // remove from the commits list all files which are supposed to be commited recursively
                    // or are children from recursively commited folders
                    commitList.removeAll(getAllChildren(recursiveCommits, commitList));
                    // leave in recursive commits only top-level parents, it does not make sense to name children explicitely
                    // moreover svn 1.7 complains when we list the children for copied folder
                    recursiveCommits = filterChildren(recursiveCommits);
                    // commit recursively
                    Logger.getLogger(CommitAction.class.getName()).log(Level.FINEST, "Committing files recursively: {0}", recursiveCommits); //NOI18N
                    cmd.commitFiles(recursiveCommits, true);
                    if(support.isCanceled()) {
                        return;
                    }
                }

                // commit the remaining files non recursively
                if(commitList.size() > 0) {
                    Logger.getLogger(CommitAction.class.getName()).log(Level.FINEST, "Committing files non-recursively: {0}", commitList); //NOI18N
                    cmd.commitFiles(commitList, false);
                    if(support.isCanceled()) {
                        return;
                    }
                }
                
                // notify change in the history
                Subversion.getInstance().getHistoryProvider().fireHistoryChange(commitedFiles);
                
                if(handleHooks) {
                    afterCommit(hooks, hookFiles, originalMessage, logs);
                }

                // update and refresh
                FileStatusCache cache = Subversion.getInstance().getStatusCache();
                if(rootUpdate) {
                    for (int i = 0; i < rootFiles.length; i++) {
                        client.update(rootFiles[i], SVNRevision.HEAD, false);
                    }
                    for (int i = 0; i < rootFiles.length; i++) {
                        cache.refresh(rootFiles[i], FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                    }
                }

                // XXX it's probably already catched by cache's onNotify()
                refreshFiles(cache, commitList);
                if(support.isCanceled()) {
                    return;
                }
                refreshFiles(cache, recursiveCommits);
                if(support.isCanceled()) {
                    return;
                }
            }
            SvnUtils.refreshFS(commitCandidates.toArray(new File[0]));
        } catch (SVNClientException ex) {
            support.annotate(ex);
        } finally {
            client.removeNotifyListener(support);
        }
    }

    private static class CommitCmd {
        private final SvnClient client;
        private final SvnProgressSupport supp;
        private final List<ISVNLogMessage> logs;
        private final String message;
        private SVNUrl repositoryRootUrl;

        public CommitCmd (SvnClient client, SvnProgressSupport supp, String message, List<ISVNLogMessage> logs) {
            this.client = client;
            this.supp = supp;
            this.logs = logs;
            this.message = message;
        }
        
        private void commitFiles (List<File> commitFiles, boolean recursive) throws SVNClientException {
            File[] files = commitFiles.toArray(new File[0]);
            long revision = client.commit(files, message, recursive);
            if (files.length > 0 && !supp.isCanceled() && revision > -1) {
                ISVNLogMessage revisionLog = getLogMessage (files, revision);
                if (revisionLog != null) {
                    Subversion.getInstance().getLogger(getRepositoryRootUrl(files[0])).logMessage(NbBundle.getMessage(CommitAction.class, "MSG_OutputCommitMessage",
                            new Object[]{
                                revisionLog.getRevision(),
                                revisionLog.getAuthor(),
                                DateFormat.getDateTimeInstance().format(revisionLog.getDate()),
                                revisionLog.getMessage()
                            }));
                    if (logs != null) {
                        logs.add(revisionLog);
                    }
                }
            }
        }

        private SVNUrl getRepositoryRootUrl(File file) throws SVNClientException {
            if (repositoryRootUrl == null) {
                repositoryRootUrl = SvnUtils.getRepositoryRootUrl(file);
            }
            return repositoryRootUrl;
        }

        private ISVNLogMessage getLogMessage (File[] files, long revision) throws SVNClientException {
            ISVNLogMessage revisionLog = null;
            long maxPause = COMMIT_PAUSE;
            long nextPause = 500;
            for (int i = 0; i < files.length && revisionLog == null; ++i) {
                try {
                    File f = files[i];
                    // an existing file needs to be found, log over a deleted file fails
                    while (!f.exists()) {
                        f = f.getParentFile();
                    }
                    revisionLog = CommitAction.getLogMessage(client, f, revision);
                } catch (SVNClientException ex) {
                    if (SvnClientExceptionHandler.isNoSuchRevision(ex.getMessage())) {
                        Logger.getLogger(CommitAction.class.getName()).log(Level.INFO,
                                "After commit pause for {0} ms. No such revision {1}", //NOI18N
                                new Object[] { nextPause, revision });
                        if (maxPause > 0) {
                            try {
                                Thread.sleep(nextPause);
                            } catch (InterruptedException ex1) {
                                // not interested
                            }
                            maxPause -= nextPause;
                            nextPause = nextPause * 2;
                            i--;
                            continue;
                        }
                    }
                    if (!SvnClientExceptionHandler.isFileNotFoundInRevision(ex.getMessage())) {
                        throw ex;
                    }
                }
            }
            return revisionLog;
        }
    }

    private static void afterCommit(Collection<SvnHook> hooks, List<File> files, String message, List<ISVNLogMessage> logs) {
        if(hooks.isEmpty()) {
            return;
        }
        List<SvnHookContext.LogEntry> entries = new ArrayList<SvnHookContext.LogEntry>(logs.size());
        for (int i = 0; i < logs.size(); i++) {
            entries.add(
                new SvnHookContext.LogEntry(
                        logs.get(i).getMessage(),
                        logs.get(i).getAuthor(),
                        logs.get(i).getRevision().getNumber(),
                        logs.get(i).getDate()));
        }
        SvnHookContext context = new SvnHookContext(files.toArray(new File[0]), message, entries);
        for (SvnHook hook : hooks) {
            hook.afterCommit(context);
        }
    }

    /**
     * Returns log message for given revision
     * @param client
     * @param file
     * @param revision
     * @return log message
     * @throws org.tigris.subversion.svnclientadapter.SVNClientException
     */
    private static ISVNLogMessage getLogMessage (ISVNClientAdapter client, File file, long revision) throws SVNClientException {
        SVNRevision rev = SVNRevision.HEAD;
        ISVNLogMessage log = null;
        try {
            rev = SVNRevision.getRevision(String.valueOf(revision));
        } catch (ParseException ex) {
            Subversion.LOG.log(Level.WARNING, "" + revision, ex);
        }
        if (Subversion.LOG.isLoggable(Level.FINER)) {
            Subversion.LOG.log(Level.FINER, "{0}: getting last commit message for svn hooks", CommitAction.class.getName());
        }
        // log has to be called directly on the file
        final SVNUrl fileRepositoryUrl = SvnUtils.getRepositoryUrl(file);
        ISVNLogMessage[] ls = client.getLogMessages(fileRepositoryUrl, rev, rev);
        if (ls.length > 0) {
            log = ls[0];
        } else {
            Subversion.LOG.log(Level.WARNING, "no logs available for file {0} with repo url {1}", new Object[]{file, fileRepositoryUrl});
        }
        return log;
    }

    /**
     * Groups files by distinct working copies and sets the binary mimetypes
     */
    private static List<List<File>> getManagedTrees(SvnClient client, SvnProgressSupport support, Set<File> commitCandidates, Set<File> binnaryCandidates) throws SVNClientException {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        List<List<File>> managedTrees = new ArrayList<List<File>>();
        for (Iterator<File> itCommitCandidates = commitCandidates.iterator(); itCommitCandidates.hasNext();) {
            File commitCandidateFile = itCommitCandidates.next();

            // set MIME property application/octet-stream
            if(binnaryCandidates.contains(commitCandidateFile)) {
                ISVNProperty prop = client.propertyGet(commitCandidateFile, ISVNProperty.MIME_TYPE);
                if(prop != null) {
                    String s = prop.getValue();
                    if (s == null || s.startsWith("text/")) { // NOI18N
                        client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false); // NOI18N
                    }
                } else {
                     client.propertySet(commitCandidateFile, ISVNProperty.MIME_TYPE, "application/octet-stream", false); // NOI18N
                }
            }
            if(support.isCanceled()) {
                return null;
            }

            List<File> managedTreesList = null;
            for (Iterator<List<File>> itManagedTrees = managedTrees.iterator(); itManagedTrees.hasNext();) {
                List<File> list = itManagedTrees.next();
                File managedTreeFile = list.get(0);

                File base = SVNBaseDir.getRootDir(new File[] {commitCandidateFile, managedTreeFile});
                if(base != null) {
                    FileInformation status = cache.getStatus(base);
                    if ((status.getStatus() & FileInformation.STATUS_MANAGED) != 0) {
                        // found a list with files from the same working copy
                        managedTreesList = list;
                        break;
                    }
                }
                if(support.isCanceled()) {
                    return null;
                }
            }
            if(managedTreesList == null) {
                // no list for files from the same wc as commitCandidateFile created yet
                managedTreesList = new ArrayList<File>();
                managedTrees.add(managedTreesList);
            }
            managedTreesList.add(commitCandidateFile);
        }

        return managedTrees;
    }

    /**
     * Calls the svn add command on not yet added files
     */
    private static void performAdds(SvnClient client, SvnProgressSupport support, List<SvnFileNode> addCandidates) throws SVNClientException {
        List<File> addFiles = new ArrayList<File>();
        List<File> addDirs = new ArrayList<File>();
        // XXX waht if user denied directory add but wants to add a file in it?
        Iterator<SvnFileNode> it = addCandidates.iterator();
        while (it.hasNext()) {
            if(support.isCanceled()) {
                return;
            }
            SvnFileNode svnFileNode = it.next();
            File file = svnFileNode.getFile();
            if (file.isDirectory()) {
                addDirs.add(file);
            } else if (file.isFile()) {
                addFiles.add(file);
            }
        }
        if(support.isCanceled()) {
            return;
        }

        Iterator<File> itFiles = addDirs.iterator();
        List<File> dirsToAdd = new ArrayList<File>();
        while (itFiles.hasNext()) {
            File dir = itFiles.next();
            if (!dirsToAdd.contains(dir)) {
                dirsToAdd.add(dir);
            }
        }
        if(dirsToAdd.size() > 0) {
            for (File file : dirsToAdd) {
                client.addDirectory(file, false);
            }
        }
        if(support.isCanceled()) {
            return;
        }

        if(addFiles.size() > 0) {
            for (File file : addFiles) {
                client.addFile(file);
            }
        }
    }

    /**
     * In case a newly added file contains a ignored file, this mothod ensures the ignored property is also set.
     * Couldn't be done earlier as the file might have been unversioned (no svn add was invoked yet) until this moment.
     *
     * @param client
     * @param support
     * @param addCandidates
     */
    private static void setIgnoredProperties(SvnClient client, SvnProgressSupport support, List<SvnFileNode> addCandidates) {
        for (SvnFileNode fileNode : addCandidates) {
            File file = fileNode.getFile();
            if(file.isDirectory()) {
                File[] children = file.listFiles();
                if(children != null || children.length > 0) {
                    for (File child : children) {
                        final FileStatusCache cache = Subversion.getInstance().getStatusCache();
                        FileInformation info = cache.getStatus(child);
                        if(info.getStatus() == FileInformation.STATUS_NOTVERSIONED_EXCLUDED) {
                            File parent = child.getParentFile();
                            if ((cache.getStatus(parent).getStatus() & FileInformation.STATUS_VERSIONED) == 0) {
                                // ensure parents added status is set
                                cache.refresh(parent, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
                            }
                            cache.refresh(child, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns all files which have to be commited recursively (deleted and copied folders)
     */
    private static List<File> getRecursiveCommits(List<File> nonRecursiveComits, List<File> removeCandidates) {
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        List<File> recursiveCommits = new ArrayList<File>();

        // 1. if there is at least one directory which isn't removed or copied
        //    we have to commit it nonrecursively ...
        boolean nonRecursiveDirs = false;
        for(File file : nonRecursiveComits) {
            ISVNStatus st = null;
            if( file.isDirectory() &&
                !( removeCandidates.contains(file) ||
                   ((st = cache.getStatus(file).getEntry(file)) != null && st.isCopied())))
            {
                nonRecursiveDirs = true;
                break;
            }
        }
        if(!nonRecursiveDirs) {
            // 2. ... otherwise we may commit all files recursivelly
            recursiveCommits.addAll(recursiveCommits);
            recursiveCommits.addAll(nonRecursiveComits);
        } else {
            // 3. ... well, this is the worst case. we have folders which were deleted or copied
            //        and such have to be commited recursively (svn restriction). On the other hand,
            //        there are also folders which have to be commited and doing it recursivelly
            //        could cause that the commit would also apply to files which because of exclusion or
            //        the (bloody) flat-folder loginc aren't supposed to be commited at all =>
            //        => the commit has to be split in two parts.
            for(File file : nonRecursiveComits) {
                ISVNStatus st = null;
                FileInformation fi = cache.getStatus(file);
                if((file.isDirectory() || fi.isDirectory()) &&
                        (removeCandidates.contains(file) || ((st = fi.getEntry(file)) != null && st.isCopied()))) {
                    recursiveCommits.add(file);
                }
            }
        }

        return recursiveCommits;
    }

    /**
     * Returns all files from the children list which have a parent in or are equal to a folder from the parents list
     */
    private static List<File> getAllChildren(List<File> parents, List<File> children) {
        List<File> ret = new ArrayList<File>();
        if(parents.size() > 0) {
            for(File child : children) {
                File parent = child;
                while(parent != null) {
                    if(parents.contains(parent)) {
                        ret.add(child);
                    }
                    parent = parent.getParentFile();
                }
            }
        }
        return ret;
    }

    private static void refreshFiles(FileStatusCache cache, List<File> files) {
        for (File file : files) {
            cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }

    private static List<File> listUnmanagedParents(SvnFileNode node) {
        List<File> unmanaged = new ArrayList<File>();
        File file = node.getFile();
        File parent = file.getParentFile();
        FileStatusCache cache = Subversion.getInstance().getStatusCache();
        while (true) {
            // added parent does not have metadata in 1.7, now, does it?
            // in that case we need to check it's status
            if (SvnUtils.hasMetadata(parent) || (cache.getStatus(parent).getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) == 0) {
                break;
            }
            unmanaged.add(0, parent);
            parent = parent.getParentFile();
            if (parent == null) {
                break;
            }
        }

        List<File> ret = new ArrayList<File>();
        Iterator<File> it = unmanaged.iterator();
        while (it.hasNext()) {
            File un = it.next();
            ret.add(un);
        }

        return ret;
    }

    private static SvnClient getClient(Context ctx, SvnProgressSupport support) {
        try {
            return Subversion.getInstance().getClient(ctx, support);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true); // should not hapen
            return null;
        }
    }

    private static List<File> filterChildren (List<File> files) {
        Set<File> filteredFiles = new LinkedHashSet<File>(files);
        for (File parent : files) {
            Set<File> toRemove = new HashSet<File>(filteredFiles.size());
            for (File f : filteredFiles) {
                if (Utils.isAncestorOrEqual(f, parent)) {
                    continue;
                } else if (Utils.isAncestorOrEqual(parent, f)) {
                    toRemove.add(f);
                } 
            }
            filteredFiles.removeAll(toRemove);
        }
        return new ArrayList<File>(filteredFiles);
    }

    private static void deleteMissingFiles (List<File> removeCandidates, SvnClient client) throws SVNClientException {
        client.remove(removeCandidates.toArray(new File[0]), true);
    }
}
