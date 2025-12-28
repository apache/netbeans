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

package org.netbeans.modules.subversion.ui.diff;

import org.openide.util.Cancellable;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.client.PropertiesClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.netbeans.modules.subversion.ui.status.StatusAction;
import org.netbeans.modules.subversion.ui.update.UpdateAction;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.LifecycleManager;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.subversion.Annotator;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.SvnFileNode;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.ui.blame.BlameAction;
import org.netbeans.modules.subversion.ui.commit.DeleteLocalAction;
import org.netbeans.modules.subversion.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.subversion.ui.history.SearchHistoryAction;
import org.netbeans.modules.subversion.ui.ignore.IgnoreAction;
import org.netbeans.modules.subversion.ui.properties.VersioningInfoAction;
import org.netbeans.modules.subversion.ui.status.OpenInEditorAction;
import org.netbeans.modules.subversion.ui.update.RevertModificationsAction;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.diff.DiffViewModeSwitcher;
import org.netbeans.modules.versioning.diff.EditorSaveCookie;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import static org.netbeans.modules.versioning.util.CollectionUtils.copyArray;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.openide.awt.Mnemonics;
import org.openide.util.actions.SystemAction;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 *
 * @author Maros Sandor
 */
public class MultiDiffPanel extends javax.swing.JPanel implements ActionListener, VersioningListener, DiffSetupSource, PropertyChangeListener, PreferenceChangeListener {
    
    /**
     * Array of DIFF setups that we show in the DIFF view. Contents of this array is changed if
     * the user switches DIFF types.
     */
    private Setup[] setups;
    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private EditorCookie[] editorCookies;
    
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 

    /**
     * Context in which to DIFF.
     */
    private final Context context;
    private final File diffedFile;

    private int displayStatuses;

    /**
     * Display name of the context of this diff.
     */ 
    private final String contextName;
    
    private int currentType = -1;
    private int currentIndex = -1;
    private int currentModelIndex = -1;
    
    private RequestProcessor.Task prepareTask;
    private DiffPrepareTask dpt;

    private AbstractAction nextAction;
    private AbstractAction          prevAction;

    private JComponent              diffView;
    private DiffFileTable           fileTable;
    private boolean                 dividerSet;
    private boolean                 initialRefreshDisabled;
    private final RepositoryFile repositoryTreeOriginalLeft;
    private final RepositoryFile repositoryTreeOriginalRight;
    private RepositoryFile repositoryTreeLeft;
    private RepositoryFile repositoryTreeRight;
    private SVNUrl repositoryUrl;
    private SVNUrl fileUrl;
    @NbBundle.Messages("MSG_Revision_Select=Select...")
    private static final String REVISION_SELECT = Bundle.MSG_Revision_Select();
    @NbBundle.Messages("MSG_Revision_Select_Separator=----------------------")
    private static final String REVISION_SELECT_SEP = Bundle.MSG_Revision_Select_Separator();
    private static final Logger LOG = Logger.getLogger(MultiDiffPanel.class.getName());
    private final boolean fixedRevisions;
    private final FileStatusCache cache = Subversion.getInstance().getStatusCache();

    /**
     * panel that is used for displaying the diff if {@code JSplitPane}
     * is not used
     */
    private final PlaceholderPanel diffViewPanel;
    private JComponent infoPanelLoadingFromRepo;

    private SvnProgressSupport refreshSetupsSupport;
    private SvnProgressSupport executeStatusSupport;
    private boolean internalChange;
    private boolean propertiesVisible;
    private int lastDividerLoc;
    private DiffViewModeSwitcher diffViewModeSwitcher;
    
    /**
     * Creates diff panel and immediatelly starts loading...
     */
    public MultiDiffPanel(Context context, int initialType, String contextName, boolean initialRefreshDisabled,
            SVNUrl repositoryUrl, SVNUrl fileUrl, RepositoryFile left, RepositoryFile right) {
        assert EventQueue.isDispatchThread();
        this.context = context;
        this.diffedFile = null;
        this.contextName = contextName;
        this.initialRefreshDisabled = initialRefreshDisabled;
        currentType = initialType;
        this.repositoryUrl = repositoryUrl;
        this.fileUrl = fileUrl;
        assert repositoryUrl == null && fileUrl == null || left != null && right != null;
        repositoryTreeLeft = repositoryTreeOriginalLeft = left;
        repositoryTreeRight = repositoryTreeOriginalRight = right;
        fixedRevisions = false;
        initComponents();
        aquaBackgroundWorkaround();
        initFileTable();
        initToolbarButtons();
        initNextPrevActions();
        initSelectionCombos();
        refreshSelectionCombos();
        
        diffViewPanel = null;
        refreshComponents();
        if (initialRefreshDisabled) {
            refreshSetups(); 
        } else {
            onRefreshButton(); // refresh statuses for the first time
        }
    }

    private void aquaBackgroundWorkaround() {
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            controlsToolBar.setBackground(color); 
            jPanel1.setBackground(color); 
            jPanel2.setBackground(color); 
            jPanel4.setBackground(color); 
            jPanel5.setBackground(color); 
            treeSelectionPanel.setBackground(color);
        }
    }
    
    /**
     * Construct diff component showing just one file.
     * It hides All, Local, Remote toggles and file chooser combo.
     */
    public MultiDiffPanel (File file, String rev1, String rev2, boolean forceNonEditable) {
        assert EventQueue.isDispatchThread();
        context = null;
        diffedFile = file;
        contextName = file.getName();
        repositoryTreeOriginalLeft = null;
        repositoryTreeOriginalRight = null;
        fixedRevisions = true;
        initComponents();
        initToolbarButtons();
        initNextPrevActions();
        initSelectionCombos();

        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);

        // mimics refreshSetups()
        Setup[] localSetups = new Setup[] {new Setup(file, rev1, rev2, forceNonEditable)};
        setSetups(localSetups, DiffUtils.setupsToEditorCookies(localSetups));
        setDiffIndex(0, 0, false);
        dpt = new DiffPrepareTask(setups);
        prepareTask = Subversion.getInstance().getRequestProcessor().post(dpt);
    }

    /**
     * Diff component with a differences between local and remote changes in a single file
     * Commit and refresh buttons are hidden, update button enabled and visible.
     * @param file diffed file
     * @param status remote status of the file
     */
    public MultiDiffPanel(File file, ISVNStatus status) {
        assert EventQueue.isDispatchThread();
        context = null;
        diffedFile = file;
        contextName = file.getName();
        repositoryTreeOriginalLeft = null;
        repositoryTreeOriginalRight = null;
        fixedRevisions = true;
        
        initComponents();
        initToolbarButtons();
        initNextPrevActions();
        initSelectionCombos();
        
        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);

        refreshButton.setVisible(false);

        // mimics refreshSetups()
        Setup[] localSetups = new Setup[] {new Setup(file, status)};
        setSetups(localSetups, DiffUtils.setupsToEditorCookies(localSetups));
        setDiffIndex(0, 0, false);
        dpt = new DiffPrepareTask(setups);
        prepareTask = Subversion.getInstance().getRequestProcessor().post(dpt);
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        removeAll();
        splitPane = null;
        setLayout(new BorderLayout());
        controlsToolBar.setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        add(controlsToolBar, BorderLayout.NORTH);
        add(replacement, BorderLayout.CENTER);
    }

    private void setSetups(Setup[] setups, EditorCookie[] cookies) {
        this.setups = setups;
        this.editorCookies = cookies;
    }

    private boolean fileTableSetSelectedIndexContext;

    void tableRowSelected(int viewIndex) {
        if (fileTableSetSelectedIndexContext) return;
        setDiffIndex(viewIndex, 0, true);
    }
    
    UndoRedo getUndoRedo() {
        return delegatingUndoRedo;
    }

    private void cancelBackgroundTasks() {
        if (prepareTask != null) {
            prepareTask.cancel();
        }
        if(executeStatusSupport!=null) {
            executeStatusSupport.cancel();
        }
        if (refreshSetupsSupport != null) {
            refreshSetupsSupport.cancel();
        }
    }

    boolean canClose() {
        if (setups == null) {
            return true;
        }

        SaveCookie[] saveCookies = getSaveCookies(true);

        return (saveCookies.length == 0)
               || SaveBeforeClosingDiffConfirmation.allSaved(saveCookies);
    }

    public SaveCookie[] getSaveCookies (boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = getEditorCookiesIntern(ommitOpened);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);
        return saveCookies;
    }

    public EditorCookie[] getEditorCookies (boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = getEditorCookiesIntern(ommitOpened);
        int count = 0, length = editorCookiesCopy.length;
        EditorCookie[] editorCookiesShorten = new EditorCookie[length];
        for (int i = 0; i < length; i++) {
            EditorCookie editorCookie = editorCookiesCopy[i];
            if (editorCookie == null) {
                continue;
            }
            editorCookiesShorten[count++] = editorCookie;
        }
        return CollectionUtils.shortenArray(editorCookiesShorten, count);
    }

    private EditorCookie[] getEditorCookiesIntern (boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        if (ommitOpened) {
            DiffUtils.cleanThoseWithEditorPaneOpen(editorCookiesCopy);
        }
        return editorCookiesCopy;
    }

    private static SaveCookie[] getSaveCookies(Setup[] setups,
                                               EditorCookie[] editorCookies) {
        assert setups.length == editorCookies.length;

        final int length = setups.length;
        SaveCookie[] proResult = new SaveCookie[length];

        int count = 0;
        for (int i = 0; i < length; i++) {
            EditorCookie editorCookie = editorCookies[i];
            if (editorCookie == null) {
                continue;
            }

            File baseFile = setups[i].getBaseFile();
            if (baseFile == null) {
                continue;
            }

            FileObject fileObj = FileUtil.toFileObject(baseFile);
            if (fileObj == null) {
                continue;
            }

            proResult[count++] = new EditorSaveCookie(editorCookie,
                                                      fileObj.getNameExt());
        }

        return CollectionUtils.shortenArray(proResult, count);
    }

    /**
     * Called by the enclosing TopComponent to interrupt the fetching task.
     */
    void componentClosed() {
        setSetups((Setup[]) null, null);
        /**
         * must disable these actions, otherwise key shortcuts would trigger them even after tab closure
         * see #159266
         */
        prevAction.setEnabled(false);
        nextAction.setEnabled(false);
        cancelBackgroundTasks();
        dpt = null;
        DiffViewModeSwitcher.release(this);
        diffViewModeSwitcher = null;
    }

    public void requestActive() {
        if (fileTable != null) {
            fileTable.getTable().requestFocusInWindow();
        } else if (diffView != null) {
            diffView.requestFocusInWindow();
        }
    }

    private void initFileTable() {
        fileTable = new DiffFileTable(this);
        splitPane.setTopComponent(fileTable.getComponent());
        splitPane.setBottomComponent(getInfoPanelLoading());
    }

    private void initToolbarButtons() {
        if (context != null) {
            commitButton.addActionListener(this);
            localToggle.addActionListener(this);
            remoteToggle.addActionListener(this);
            allToggle.addActionListener(this);
            filterPropertiesButton.addActionListener(this);

            commitButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Commit_Tooltip"));
            updateButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Update_Tooltip"));
            if (currentType == Setup.DIFFTYPE_LOCAL) localToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_REMOTE) remoteToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_ALL) allToggle.setSelected(true);

            commitButton.setEnabled(false);
            boolean propsVisible = SvnModuleConfig.getDefault().isFilterPropertiesEnabled();
            filterPropertiesButton.setSelected(propsVisible);
            if (currentType == -1) {
                filterPropertiesButton.setEnabled(false);
            }
            propertiesVisible = propsVisible && filterPropertiesButton.isEnabled();
        } else {
            commitButton.setVisible(false);
            updateButton.setVisible(false);
            localToggle.setVisible(false);
            remoteToggle.setVisible(false);
            allToggle.setVisible(false);
            refreshButton.setVisible(false);
            filterPropertiesButton.setVisible(false);
        }
    }

    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-next.png"))) {  // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Next_Tooltip"));                
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Prev_Tooltip"));                
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onPrevButton();
            }
        };
        nextButton.setAction(nextAction);
        prevButton.setAction(prevAction);
    }
    
    private JComponent getInfoPanelLoading() {
        if (infoPanelLoadingFromRepo == null) {
            infoPanelLoadingFromRepo = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
        }
        return infoPanelLoadingFromRepo;
    }

    private void refreshComponents() {
        assert EventQueue.isDispatchThread();
        DiffController view = setups != null && currentModelIndex != -1 ? setups[currentModelIndex].getView() : null;
        int currentDifferenceIndex = view != null ? view.getDifferenceIndex() : -1;
        if (view != null) {
            nextAction.setEnabled(currentIndex < setups.length - 1 || currentDifferenceIndex < view.getDifferenceCount() - 1);
        } else {
            nextAction.setEnabled(false);
        }
        prevAction.setEnabled(currentIndex > 0 || currentDifferenceIndex > 0);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (fileTable != null) {
            cache.addVersioningListener(this);
            SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        }
        JComponent parent = (JComponent) getParent();
        parent.getActionMap().put("jumpNext", nextAction);  // NOI18N
        parent.getActionMap().put("jumpPrev", prevAction); // NOI18N
        if (splitPane != null && lastDividerLoc != 0) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    splitPane.setDividerLocation(lastDividerLoc);
                }
            });
        }
    }

    private void updateSplitLocation() {
        if (dividerSet) return;
        JComponent parent = (JComponent) getParent();
        Dimension dim = parent == null ? new Dimension() : parent.getSize();
        if (dim.width <=0 || dim.height <=0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateSplitLocation();
                }
            });
            return;
        }
        dividerSet = true;
        JTable jt = fileTable.getTable();
        int optimalLocation = jt.getPreferredSize().height + jt.getTableHeader().getPreferredSize().height;
        if (optimalLocation > dim.height / 3) {
            optimalLocation = dim.height / 3;
        }
        if (optimalLocation <= jt.getTableHeader().getPreferredSize().height) {
            optimalLocation = jt.getTableHeader().getPreferredSize().height * 3;
        }
        splitPane.setDividerLocation(optimalLocation);
    }
    
    @Override
    public void removeNotify() {
        if (splitPane != null) {
            lastDividerLoc = splitPane.getDividerLocation();
        }
        cache.removeVersioningListener(this);
        if (fileTable != null) {
            SvnModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        }
        super.removeNotify();
    }
    
    @Override
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            if (!affectsView(event)) {
                return;
            }
            refreshSetups();
        }
    }
    
    JPopupMenu getPopup () {
        JPopupMenu menu = null;
        if (currentType != -1) {
            menu = new JPopupMenu();
            JMenuItem item;

            item = menu.add(new OpenInEditorAction());
            Mnemonics.setLocalizedText(item, item.getText());
            menu.addSeparator();
            item = menu.add(new SystemActionBridge(SystemAction.get(UpdateAction.class), actionString("CTL_PopupMenuItem_Update"))); // NOI18N
            Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(new SystemActionBridge(SystemAction.get(CommitAction.class), actionString("CTL_PopupMenuItem_Commit"))); // NOI18N
            Mnemonics.setLocalizedText(item, item.getText());

            menu.addSeparator();
            item = menu.add(new SystemActionBridge(SystemAction.get(BlameAction.class),
                                                   ((BlameAction)SystemAction.get(BlameAction.class)).visible(null) ?
                                                   actionString("CTL_PopupMenuItem_HideBlame") : // NOI18N
                                                   actionString("CTL_PopupMenuItem_Blame"))); // NOI18N
            Mnemonics.setLocalizedText(item, item.getText());

            item = menu.add(new SystemActionBridge(SystemAction.get(SearchHistoryAction.class), actionString("CTL_PopupMenuItem_SearchHistory"))); // NOI18N
            Mnemonics.setLocalizedText(item, item.getText());

            menu.addSeparator();
            String label;
            ExcludeFromCommitAction exclude = (ExcludeFromCommitAction) SystemAction.get(ExcludeFromCommitAction.class);
            if (exclude.getActionStatus(null) == ExcludeFromCommitAction.INCLUDING) {
                label = org.openide.util.NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_IncludeInCommit"); // NOI18N
            } else {
                label = org.openide.util.NbBundle.getMessage(Annotator.class, "CTL_PopupMenuItem_ExcludeFromCommit"); // NOI18N
            }
            item = menu.add(new SystemActionBridge(exclude, label));
            Mnemonics.setLocalizedText(item, item.getText());

            Action revertAction;
            boolean allLocallyNew = true;
            boolean allLocallyDeleted = true;
            File [] files = SvnUtils.getCurrentContext(null).getFiles();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                FileInformation info = cache.getStatus(file);
                if ((info.getStatus() & DeleteLocalAction.LOCALLY_DELETABLE_MASK) == 0 ) {
                    allLocallyNew = false;
                }
                if (info.getStatus() != FileInformation.STATUS_VERSIONED_DELETEDLOCALLY && info.getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                    allLocallyDeleted = false;
                }
            }
            if (allLocallyNew) {
                SystemAction systemAction = SystemAction.get(DeleteLocalAction.class);
                revertAction = new SystemActionBridge(systemAction, actionString("CTL_PopupMenuItem_Delete")); // NOI18N
            } else if (allLocallyDeleted) {
                revertAction = new SystemActionBridge(SystemAction.get(RevertModificationsAction.class), actionString("CTL_PopupMenuItem_RevertDelete")); // NOI18N
            } else {
                revertAction = new SystemActionBridge(SystemAction.get(RevertModificationsAction.class), actionString("CTL_PopupMenuItem_GetClean")); // NOI18N
            }
            item = menu.add(revertAction);
            Mnemonics.setLocalizedText(item, item.getText());

            item = menu.add(new AbstractAction(actionString("CTL_PopupMenuItem_ExportDiff")) { //NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    SystemAction.get(ExportDiffAction.class).performContextAction(TopComponent.getRegistry().getActivatedNodes(), true);
                }
            });
            Mnemonics.setLocalizedText(item, item.getText());
            Node[] activatedNodes = TopComponent.getRegistry().getActivatedNodes();
            if (activatedNodes.length > 0 && activatedNodes[0] instanceof DiffNode) {
                // we currently don't know how to export changes on folders or properties
                Setup setup = ((DiffNode) activatedNodes[0]).getSetup();
                FileInformation info = setup.getInfo();
                item.setEnabled(setup.getPropertyName() == null && info != null && !info.isDirectory());
            }

            Action ignoreAction = new SystemActionBridge(SystemAction.get(IgnoreAction.class),
               ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus(files) == IgnoreAction.UNIGNORING ?
               actionString("CTL_PopupMenuItem_Unignore") : // NOI18N
               actionString("CTL_PopupMenuItem_Ignore")); // NOI18N
            item = menu.add(ignoreAction);
            Mnemonics.setLocalizedText(item, item.getText());
            Action infoAction = new SystemActionBridge(SystemAction.get(VersioningInfoAction.class), actionString("CTL_PopupMenuItem_VersioningInfo")); // NOI18N
            item = menu.add(infoAction);
            Mnemonics.setLocalizedText(item, item.getText());
        }
        return menu;
    }

    private String actionString(String key) {
        ResourceBundle actionsLoc = NbBundle.getBundle(Annotator.class);
        return actionsLoc.getString(key);
    }
    
    private boolean affectsView(VersioningEvent event) {
        File file = (File) event.getParams()[0];
        FileInformation oldInfo = (FileInformation) event.getParams()[1];
        FileInformation newInfo = (FileInformation) event.getParams()[2];
        if (oldInfo == null) {
            if ((newInfo.getStatus() & displayStatuses) == 0) return false;
        } else {
            if ((oldInfo.getStatus() & displayStatuses) + (newInfo.getStatus() & displayStatuses) == 0) return false;
        }
        return context.contains(file);
    }

    private void setDiffIndex(int idx, int location, boolean restartPrepareTask) {
        assert EventQueue.isDispatchThread();
        currentIndex = idx;
        DiffController view = null;
        
        if (currentIndex != -1) {
            if (restartPrepareTask && dpt != null) {
                prepareTask.cancel();
                dpt.setTableIndex(currentIndex);
                prepareTask.schedule(100);
            }
            currentModelIndex = showingFileTable() ? fileTable.getModelIndex(currentIndex) : 0;
            view = setups[currentModelIndex].getView();

            TopComponent tc = (TopComponent) getClientProperty(TopComponent.class);
            if (tc != null) {
                Node node = setups[currentModelIndex].getNode();
                tc.setActivatedNodes(new Node[] {node == null ? Node.EMPTY : node});
            }
            
            diffView = null;
            if (view != null) {
                if (showingFileTable()) {
                    fileTableSetSelectedIndexContext = true;
                    fileTable.setSelectedIndex(currentIndex);
                    fileTableSetSelectedIndexContext = false;
                }
                getDiffViewModeSwitcher().setupMode(view);
                diffView = view.getJComponent();
                diffView.getActionMap().put("jumpNext", nextAction);  // NOI18N
                diffView.getActionMap().put("jumpPrev", prevAction);  // NOI18N
                displayDiffView();
                if (location == -1) {
                    location = view.getDifferenceCount() - 1;
                }
                if (location >=0 && location < view.getDifferenceCount()) {
                    view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, location);
                }
            } else {
                diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
                displayDiffView();
            }            
        } else {
            currentModelIndex = -1;
            diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoFileSelected"));
            displayDiffView();
        }

        delegatingUndoRedo.setDiffView(diffView);

        refreshComponents();
    }

    private DiffViewModeSwitcher getDiffViewModeSwitcher () {
        if (diffViewModeSwitcher == null) {
            diffViewModeSwitcher = DiffViewModeSwitcher.get(this);
        }
        return diffViewModeSwitcher;
    }

    private boolean showingFileTable() {
        return fileTable != null;
    }

    private void displayDiffView() {
        if (splitPane != null) {
            int gg = splitPane.getDividerLocation();
            splitPane.setBottomComponent(diffView);
            splitPane.setDividerLocation(gg);
        } else {
            diffViewPanel.setComponent(diffView);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == commitButton) onCommitButton();
        else if (source == localToggle || source == remoteToggle || source == allToggle) onDiffTypeChanged();
        else if (source == cmbDiffTreeSecond) {
            RepositoryFile oldSelection = repositoryTreeLeft;
            RepositoryFile newSelection = getSelectedRevision(cmbDiffTreeSecond, repositoryTreeLeft);
            if (newSelection != null) {
                repositoryTreeLeft = newSelection;
            }
            boolean refresh = !oldSelection.equals(repositoryTreeLeft);
            if (refresh) {
                synchronizeButtons();
                refreshStatuses();
            }
        } else if (source == cmbDiffTreeFirst) {
            RepositoryFile oldSelection = repositoryTreeRight;
            RepositoryFile newSelection = getSelectedRevision(cmbDiffTreeFirst, repositoryTreeRight);
            if (newSelection != null) {
                repositoryTreeRight = newSelection;
            }
            boolean refresh = !oldSelection.equals(repositoryTreeRight);
            if (refresh) {
                synchronizeButtons();
                refreshStatuses();
            }
        } else if (source == filterPropertiesButton) {
            boolean propsVisible = filterPropertiesButton.isSelected();
            SvnModuleConfig.getDefault().setFilterPropertiesEnabled(propsVisible);
            propertiesVisible = propsVisible && filterPropertiesButton.isEnabled();
            refreshStatuses();
        }
    }
    
    private RepositoryFile getSelectedRevision (JComboBox cmbDiffTree, RepositoryFile initial) {
        Object selectedItem = cmbDiffTree.getSelectedItem();
        RepositoryFile selection = null;
        if (selectedItem == REVISION_SELECT) {
            File[] roots = SvnUtils.getActionRoots(context, false);
            if (roots != null) {
                File interestingFile;
                if(roots.length == 1) {
                    interestingFile = roots[0];
                } else {
                    interestingFile = SvnUtils.getPrimaryFile(roots[0]);
                }
                SelectDiffTree dialog = new SelectDiffTree(repositoryTreeLeft, interestingFile);
                if (dialog.showDialog()) {
                    selection = dialog.getRepositoryFile();
                    addToModel(selection, cmbDiffTree);
                }
            }
        } else if (selectedItem instanceof RepositoryFile) {
            selection = (RepositoryFile) selectedItem;
        }
        return selection;
    }

    private void addToModel (final RepositoryFile newItem, final JComboBox cmbDiffTree) {
        DefaultComboBoxModel<RepositoryFile> model =
                (DefaultComboBoxModel<RepositoryFile>) cmbDiffTree.getModel();
        for (int i = 0; i < model.getSize(); ++i) {
            final Object item = model.getElementAt(i);
            if (newItem.toString().equals(item.toString())) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        cmbDiffTree.setSelectedItem(item);
                    }
                });
                return;
            }
        }
        model.addElement(newItem);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                cmbDiffTree.setSelectedItem(newItem);
            }
        });
    }

    private void onRefreshButton() {
        if (context == null || context.getRoots().isEmpty()) {
            return;
        }

        if(executeStatusSupport!=null) {
            executeStatusSupport.cancel();
            executeStatusSupport = null;
        }
        if (refreshSetupsSupport != null) {
            refreshSetupsSupport.cancel();
        }
        
        LifecycleManager.getDefault().saveAll();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor();
        final boolean contactServer = currentType != Setup.DIFFTYPE_LOCAL;
        SvnProgressSupport supp = new SvnProgressSupport() {
            @Override
            public void perform() {                                         
                if (currentType != -1) {
                    StatusAction.executeStatus(context, this, contactServer);
                }
                if (!isCanceled()) {
                    refreshSetups();
                }
            }
        };
        SVNUrl url;
        try {
            url = ContextAction.getSvnUrl(context); 
        } catch(SVNClientException ex)  {
            SvnClientExceptionHandler.notifyException(ex, true, true);     
            return;             
        }
        supp.start(rp, url, NbBundle.getMessage(MultiDiffPanel.class, "MSG_Refresh_Progress"));
        executeStatusSupport = supp;
    }                    

    private void onUpdateButton() {
        if (context != null) {
            UpdateAction.performUpdate(context, contextName);
        } else if (diffedFile != null) {
            UpdateAction.performUpdate(diffedFile);
        }
    }
    
    private void onCommitButton() {
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        if ((saveCookies.length == 0)
                || SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
            CommitAction.commit(contextName, context, false);
        }
    }

    /** Next that is driven by visibility. It continues to next not yet visible difference. */
    private void onNextButton() {
        assert setups != null : "setups is null";                       //NOI18N
        assert setups[currentModelIndex] != null
                        : "setups[" + currentModelIndex + "] is null";  //NOI18N
        if ((setups == null) || (setups[currentModelIndex] == null)) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    nextButton.setEnabled(false);
                }
            });
            return;
        }

        if (showingFileTable()) {
            currentIndex = fileTable.getSelectedIndex();
            currentModelIndex = fileTable.getSelectedModelIndex();
        }

        DiffController view = setups[currentModelIndex].getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (++currentDifferenceIndex >= view.getDifferenceCount()) {
                if (++currentIndex >= setups.length) {
                    currentIndex--;
                } else {
                    setDiffIndex(currentIndex, 0, true);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (++currentIndex >= setups.length) currentIndex = 0;
            setDiffIndex(currentIndex, 0, true);
        }
        refreshComponents();
    }

    private void onPrevButton() {
        DiffController view = setups[currentModelIndex].getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (--currentDifferenceIndex < 0) {
                if (--currentIndex < 0) {
                    currentIndex++;
                } else {
                    setDiffIndex(currentIndex, -1, true);
                }
            } else if (currentDifferenceIndex < view.getDifferenceCount()) {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (--currentIndex < 0) currentIndex = setups.length - 1;
            setDiffIndex(currentIndex, -1, true);
        }
        refreshComponents();
    }

    /**
     * @return setups, takes into account Local, Remote, All switch
     */
    @Override
    public Collection<Setup> getSetups() {
        if (setups == null) {
            return Collections.emptySet();
        } else {
            return Arrays.asList(setups);
        }
    }

    @Override
    public String getSetupDisplayName() {
        return contextName;
    }
    
    private void refreshSelectionCombos () {
        if (!fixedRevisions && repositoryTreeOriginalLeft != null && repositoryTreeOriginalRight != null) {
            List<Object> modelRight = new ArrayList<Object>(10);
            List<Object> modelLeft = new ArrayList<Object>(10);
            modelLeft.add(repositoryTreeOriginalLeft);
            if (!SVNRevision.BASE.equals(repositoryTreeOriginalLeft.getRevision())) {
                modelLeft.add(new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.BASE));
            }
            if (!SVNRevision.HEAD.equals(repositoryTreeOriginalLeft.getRevision())) {
                modelLeft.add(new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.HEAD));
            }
            modelRight.add(repositoryTreeOriginalRight);            
            if (!SVNRevision.WORKING.equals(repositoryTreeOriginalRight.getRevision())) {
                modelRight.add(new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.WORKING));
            }
            if (!SVNRevision.BASE.equals(repositoryTreeOriginalRight.getRevision())) {
                modelRight.add(new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.BASE));
            }
            if (!SVNRevision.HEAD.equals(repositoryTreeOriginalRight.getRevision())) {
                modelRight.add(new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.HEAD));
            }
            modelLeft.add(REVISION_SELECT_SEP);
            modelLeft.add(REVISION_SELECT);
            modelLeft.add(REVISION_SELECT_SEP);
            modelRight.add(REVISION_SELECT_SEP);
            modelRight.add(REVISION_SELECT);
            modelRight.add(REVISION_SELECT_SEP);
            cmbDiffTreeFirst.setModel(new DefaultComboBoxModel(modelRight.toArray(new Object[0])));
            cmbDiffTreeSecond.setModel(new DefaultComboBoxModel(modelLeft.toArray(new Object[0])));
            if (SvnUtils.getActionRoots(context, false) != null) {
                cmbDiffTreeFirst.setEnabled(true);
                cmbDiffTreeSecond.setEnabled(true);
            }
        }
    }

    private void initSelectionCombos () {
        if (fixedRevisions || repositoryTreeLeft == null || repositoryTreeRight == null) {
            treeSelectionPanel.setVisible(false);
        } else {
            cmbDiffTreeFirst.setMinimumSize(cmbDiffTreeFirst.getMinimumSize());
            cmbDiffTreeSecond.setMinimumSize(cmbDiffTreeSecond.getMinimumSize());
            treeSelectionPanel.setMinimumSize(treeSelectionPanel.getMinimumSize());
            ListCellRenderer renderer = new RepositoryFileCellRenderer();
            cmbDiffTreeFirst.setRenderer(renderer);
            cmbDiffTreeFirst.setModel(new DefaultComboBoxModel(new Object[] { repositoryTreeRight }));
            cmbDiffTreeFirst.addActionListener(this);
            cmbDiffTreeSecond.setRenderer(renderer);
            cmbDiffTreeSecond.setModel(new DefaultComboBoxModel(new Object[] { repositoryTreeLeft }));
            cmbDiffTreeSecond.addActionListener(this);
        }
    }

    private void selectItem (JComboBox cmbDiffTree, SVNRevision revision) {
        Object toSelect = null;
        if (fileUrl != null) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) cmbDiffTree.getModel();
            for (int i = 0; i < model.getSize(); ++i) {
                Object o = model.getElementAt(i);
                if (o instanceof RepositoryFile) {
                    RepositoryFile inModel = (RepositoryFile) o;
                    if (inModel.getFileUrl().equals(fileUrl) && revision.equals(inModel.getRevision())) {
                        toSelect = o;
                        break;
                    }
                }
            }
        }
        if (toSelect != null) {
            cmbDiffTree.setSelectedItem(toSelect);
        }
    }

    private void synchronizeButtons () {
        if (!internalChange && repositoryTreeOriginalLeft != null && repositoryTreeLeft != null
                && repositoryTreeOriginalRight != null && repositoryTreeRight != null) {
            boolean original = internalChange;
            try {
                internalChange = true;
                boolean none = false;
                if (repositoryTreeLeft.getFileUrl().equals(repositoryTreeOriginalLeft.getFileUrl())
                        && repositoryTreeRight.getFileUrl().equals(repositoryTreeOriginalRight.getFileUrl())
                        && repositoryTreeLeft.getFileUrl().equals(repositoryTreeRight.getFileUrl())) {
                    if (repositoryTreeLeft.getRevision().equals(SVNRevision.BASE)
                            && repositoryTreeRight.getRevision().equals(SVNRevision.WORKING)) {
                        localToggle.setSelected(true);
                    } else if (repositoryTreeLeft.getRevision().equals(SVNRevision.HEAD)
                            && repositoryTreeRight.getRevision().equals(SVNRevision.BASE)) {
                        remoteToggle.setSelected(true);
                    } else if (repositoryTreeLeft.getRevision().equals(SVNRevision.HEAD)
                            && repositoryTreeRight.getRevision().equals(SVNRevision.WORKING)) {
                        allToggle.setSelected(true);
                    } else {
                        none = true;
                    }
                } else {
                    none = true;
                }
                if (none) {
                    btnGroup.clearSelection();
                    filterPropertiesButton.setEnabled(false);
                    propertiesVisible = false;
                    currentType = -1;
                } else {
                    filterPropertiesButton.setEnabled(true);
                    propertiesVisible = filterPropertiesButton.isSelected();
                }
                commitButton.setEnabled(!none);
                updateButton.setEnabled(!none);
                onDiffTypeChanged();
            } finally {
                internalChange = original;
            }
        }
    }

    private void refreshSetups () {
        SvnProgressSupport supp = refreshSetupsSupport;
        if (supp != null) {
            supp.cancel();
        }
        supp = new SetupsPrepareSupport();
        supp.start(Subversion.getInstance().getRequestProcessor(), null,
            NbBundle.getMessage(MultiDiffPanel.class, "MSG_PrepareSetups_Progress")); //NOI18N
        refreshSetupsSupport = supp;
    }
    
    @NbBundle.Messages({
        "MSG_Revision_Select_Tooltip=Select a tree to diff"
    })
    private static class RepositoryFileCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String tooltip = null;
            if (value instanceof RepositoryFile) {
                RepositoryFile repoFile = (RepositoryFile) value;
                value = repoFile.getName() + "@" + repoFile.getRevision().toString() + " - " + repoFile.getPath();
                tooltip = repoFile.toString();
            } else if (value instanceof String) {
                value = "<html><i>" + value + "</i></html>"; //NOI18N
                tooltip = Bundle.MSG_Revision_Select_Tooltip();
            }
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (comp instanceof JComponent) {
                ((JComponent) comp).setToolTipText(tooltip);
            }
            return comp;
        }

    }

    private class SetupsPrepareSupport extends SvnProgressSupport {
        
        @Override
        protected void perform() {
            if (dpt != null) {
                prepareTask.cancel();
            }

            int status;
            switch (currentType) {
                case Setup.DIFFTYPE_LOCAL:
                    status = FileInformation.STATUS_LOCAL_CHANGE;
                    break;
                case Setup.DIFFTYPE_REMOTE:
                    status = FileInformation.STATUS_REMOTE_CHANGE;
                    break;
                case Setup.DIFFTYPE_ALL:
                    status = FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_REMOTE_CHANGE;
                    break;
                case -1:
                    status = 0;
                    break;
                default:
                    throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
            }
            final int localDisplayStatuses = status;
            final Setup[] localSetups;
            if (status == 0) {
                RevisionSetupsSupport rdc = new RevisionSetupsSupport(
                        repositoryTreeLeft, repositoryTreeRight, repositoryUrl, context);
                if (repositoryTreeRight.getRevision() == SVNRevision.WORKING) {
                    File[] files = SvnUtils.getModifiedFiles(context, FileInformation.STATUS_LOCAL_CHANGE);
                    if (isCanceled()) {
                        return;
                    }
                    rdc.setWCSetups(computeSetups(files, FileInformation.STATUS_LOCAL_CHANGE, Setup.DIFFTYPE_LOCAL));
                }
                localSetups = rdc.computeSetupsBetweenRevisions(this);
            } else {
                File[] files = SvnUtils.getModifiedFiles(context, status);
                if (isCanceled()) {
                    return;
                }
                localSetups = computeSetups(files, status, currentType);
            }
            if (localSetups == null) {
                return;
            }
            Arrays.sort(localSetups, new MultiDiffPanel.SetupsComparator());
            final EditorCookie[] cookies = DiffUtils.setupsToEditorCookies(localSetups);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    dividerSet = false;
                    displayStatuses = localDisplayStatuses;
                    setSetups(localSetups, cookies);
                    boolean propertyColumnVisible = false;
                    for (Setup setup : setups) {
                        if (setup.getPropertyName() != null) {
                            propertyColumnVisible = true;
                            break;
                        }
                    }
                    fileTable.setColumns(propertyColumnVisible ? new String[]{DiffNode.COLUMN_NAME_NAME, DiffNode.COLUMN_NAME_PROPERTY, DiffNode.COLUMN_NAME_STATUS, DiffNode.COLUMN_NAME_LOCATION} : new String[]{DiffNode.COLUMN_NAME_NAME, DiffNode.COLUMN_NAME_STATUS, DiffNode.COLUMN_NAME_LOCATION});
                    fileTable.setTableModel(setups, editorCookies);

                    if (setups.length == 0) {
                        String noContentLabel;
                        switch (currentType) {
                            case Setup.DIFFTYPE_LOCAL:
                                noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoLocalChanges");
                                break;
                            case Setup.DIFFTYPE_REMOTE:
                                noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoRemoteChanges");
                                break;
                            case Setup.DIFFTYPE_ALL:
                                noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoAllChanges");
                                break;
                            case -1:
                                noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoChanges");
                                break;
                            default:
                                throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
                        }
                        setSetups((Setup[]) null, null);
                        fileTable.getComponent().setEnabled(false);
                        fileTable.getComponent().setPreferredSize(null);
                        Dimension dim = fileTable.getComponent().getPreferredSize();
                        fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                        diffView = null;
                        diffView = new NoContentPanel(noContentLabel);
                        displayDiffView();
                        nextAction.setEnabled(false);
                        prevAction.setEnabled(false);
                        commitButton.setEnabled(false);
                        revalidate();
                        repaint();
                    } else {
                        fileTable.getComponent().setEnabled(true);
                        fileTable.getComponent().setPreferredSize(null);
                        Dimension dim = fileTable.getComponent().getPreferredSize();
                        fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                        setDiffIndex(0, 0, false);
                        commitButton.setEnabled(currentType != -1);
                        dpt = new DiffPrepareTask(setups);
                        prepareTask = Subversion.getInstance().getRequestProcessor().post(dpt);
                    }
                }
            };
            if (EventQueue.isDispatchThread()) {
                runnable.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(runnable);
                } catch (InterruptedException ex) {
                    Subversion.LOG.log(Level.FINE, null, ex);
                } catch (InvocationTargetException ex) {
                    Subversion.LOG.log(Level.FINE, null, ex);
                }
            }
        }

        private Setup[] computeSetups (final File[] files, final int displayStatus, final int setupType) {
            final List<Setup> newSetups = new ArrayList<Setup>(files.length);
            final int statusWithoutProperties = displayStatus & ~FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY_PROPERTY;
            final boolean[] canceled = new boolean[1];
            SvnUtils.runWithInfoCache(new Runnable() {
                @Override
                public void run () {
                    for (File file : files) {
                        if (!file.isDirectory() && (cache.getStatus(file).getStatus() & statusWithoutProperties) != 0) {
                            Setup setup = new Setup(file, null, setupType);
                            setup.setNode(new DiffNode(setup, new SvnFileNode(file), displayStatus));
                            newSetups.add(setup);
                        }
                        if (propertiesVisible) {
                            addPropertiesSetups(file, newSetups, displayStatus);
                        }
                        if (isCanceled()) {
                            canceled[0] = true;
                            return;
                        }
                    }
                }
            });
            return canceled[0] ? null : newSetups.toArray(new Setup[0]);
        }
    }
    

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }

    private void addPropertiesSetups(File base, List<Setup> newSetups, int displayStatus) {
        if (currentType == Setup.DIFFTYPE_REMOTE) return;

        DiffProvider diffAlgorithm = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        PropertiesClient client = new PropertiesClient(base);
        try {
            Map<String, byte[]> baseProps = client.getBaseProperties(currentType == Setup.DIFFTYPE_ALL);
            Map<String, byte[]> localProps = client.getProperties();

            Set<String> allProps = new TreeSet<String>(localProps.keySet());
            allProps.addAll(baseProps.keySet());
            for (String key : allProps) {
                boolean isBase = baseProps.containsKey(key);
                boolean isLocal = localProps.containsKey(key);
                boolean propertiesDiffer = true;
                if (isBase && isLocal) {
                    Property p1 = new Property(baseProps.get(key));
                    Property p2 = new Property(localProps.get(key));
                    Difference[] diffs = diffAlgorithm.computeDiff(p1.toReader(), p2.toReader());
                    propertiesDiffer = (diffs.length != 0);
                }
                if (propertiesDiffer) {
                    Setup setup = new Setup(base, key, currentType);
                    setup.setNode(new DiffNode(setup, new SvnFileNode(base), displayStatus));
                    newSetups.add(setup);
                }
            }
        } catch (IOException e) {
            // no need to litter log with expected exceptions:
            // when parent is not versioned, the exception will allways be thrown
            FileInformation parentInfo = cache.getCachedStatus(base.getParentFile());
            Level logLevel = parentInfo != null && (parentInfo.getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0
                    ? Level.FINE : Level.INFO;
            Subversion.LOG.log(logLevel, null, e);
        }
    }

    private void onDiffTypeChanged() {
        assert EventQueue.isDispatchThread();
        boolean orig = internalChange;
        try {
            internalChange = true;
            if (localToggle.isSelected()) {
                if (!orig) {
                    selectItem(cmbDiffTreeFirst, SVNRevision.WORKING);
                    selectItem(cmbDiffTreeSecond, SVNRevision.BASE);
                }
                if (currentType == Setup.DIFFTYPE_LOCAL) return;
                currentType = Setup.DIFFTYPE_LOCAL;
            } else if (remoteToggle.isSelected()) {
                if (!orig) {
                    selectItem(cmbDiffTreeFirst, SVNRevision.BASE);
                    selectItem(cmbDiffTreeSecond, SVNRevision.HEAD);
                }
                if (currentType == Setup.DIFFTYPE_REMOTE) return;
                currentType = Setup.DIFFTYPE_REMOTE;
            } else if (allToggle.isSelected()) {
                if (!orig) {
                    selectItem(cmbDiffTreeFirst, SVNRevision.WORKING);
                    selectItem(cmbDiffTreeSecond, SVNRevision.HEAD);
                }
                if (currentType == Setup.DIFFTYPE_ALL) return;
                currentType = Setup.DIFFTYPE_ALL;
            }
        } finally {
            internalChange = orig;
        }
        SvnModuleConfig.getDefault().setLastUsedModificationContext(currentType);
        refreshStatuses();
    }
    
    private void refreshStatuses () {
        if (internalChange) {
            return;
        }
        if (!initialRefreshDisabled && currentType != Setup.DIFFTYPE_LOCAL) {
            // current mode is not local and remote status has not yet been called, so remote statuses need to be refreshed
            onRefreshButton();
        } else {
            // local and remote statuses already refreshed, no need to call status action
            refreshSetups();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            refreshComponents();
        }
    }

    private class DiffPrepareTask implements Runnable, Cancellable {
        
        private final Setup[] prepareSetups;
        private int tableIndex; // index of a row in the table - viewIndex, needs to be translated to modelIndex
        private boolean canceled;

        public DiffPrepareTask(Setup [] prepareSetups) {
            this.prepareSetups = prepareSetups;
            this.tableIndex = 0;
        }

        @Override
        public void run() {
            canceled = false;
            IOException exception = null;
            int[] indexes = prepareIndexesToRefresh();
            for (int i : indexes) {
                if (prepareSetups != setups || Thread.interrupted()) return;
                int modelIndex = fileTable == null ? i : fileTable.getModelIndex(i);
                if (prepareSetups[modelIndex].getView() != null) {
                    continue;
                }
                try {
                    prepareSetups[modelIndex].initSources();  // slow network I/O
                    final int fi = modelIndex;
                    if (Thread.interrupted() || canceled) {
                        return;
                    }
                    StreamSource ss1 = prepareSetups[fi].getFirstSource();
                    StreamSource ss2 = prepareSetups[fi].getSecondSource();
                    final DiffController view = DiffController.createEnhanced(ss1, ss2);  // possibly executing slow external diff
                    view.addPropertyChangeListener(MultiDiffPanel.this);
                    if (Thread.interrupted() || canceled) {
                        return;
                    }
                    prepareSetups[fi].setView(view);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (prepareSetups != setups) {
                                return;
                            }
                            if (currentModelIndex == fi) {
                                setDiffIndex(currentIndex, 0, false);
                            }
                            if (splitPane != null) {
                                updateSplitLocation();
                            }
                        }
                    });
                } catch (IOException e) {
                    Subversion.LOG.log(Level.INFO, null, e);
                    if (exception == null) {
                        // save only the first exception
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                // notify user of the failure
                final IOException e = exception;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SvnClientExceptionHandler.notifyException(e, true, true);
                    }
                });
            }
        }

        private int[] prepareIndexesToRefresh () {
            int index = tableIndex;
            int min = Math.max(0, index - 2);
            int max = Math.min(prepareSetups.length - 1, index + 2);
            int[] indexes = new int[max - min + 1];
            // adding tableIndex, tableIndex - 1, tableIndex + 1, tableIndex - 2, tableIndex + 2, etc.
            for (int i = index, j = index + 1, k = 0; i >= min || j <= max; --i, ++j) {
                if (i >= min) {
                    indexes[k++] = i;
                }
                if (j <= max) {
                    indexes[k++] = j;
                }
            }
            return indexes;
        }

        private void setTableIndex(int index) {
            this.tableIndex = index;
        }

        @Override
        public boolean cancel() {
            return this.canceled = true;
        }
    }

    private static class SetupsComparator implements Comparator<Setup> {

        private SvnUtils.ByImportanceComparator delegate = new SvnUtils.ByImportanceComparator();
        private FileStatusCache cache;

        public SetupsComparator() {
            cache = Subversion.getInstance().getStatusCache();
        }

        @Override
        public int compare(Setup setup1, Setup setup2) {
            int cmp = delegate.compare(cache.getStatus(setup1.getBaseFile()), cache.getStatus(setup2.getBaseFile()));
            if (cmp == 0) {
                return setup1.getBaseFile().getName().compareToIgnoreCase(setup2.getBaseFile().getName());
            }
            return cmp;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroup = new javax.swing.ButtonGroup();
        controlsToolBar = new javax.swing.JToolBar();
        allToggle = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        localToggle = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        remoteToggle = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        commitButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        filterPropertiesButton = new javax.swing.JToggleButton();
        splitPane = new javax.swing.JSplitPane();
        jLabel1 = new javax.swing.JLabel();
        cmbDiffTreeFirst = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        controlsToolBar.setFloatable(false);
        controlsToolBar.setRollover(true);

        btnGroup.add(allToggle);
        allToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/remote_vs_local.png"))); // NOI18N
        allToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_All_Tooltip")); // NOI18N
        allToggle.setFocusable(false);
        allToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        allToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(allToggle);

        jPanel3.setMaximumSize(new java.awt.Dimension(12, 32767));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 11, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel3);

        btnGroup.add(localToggle);
        localToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/local_vs_local.png"))); // NOI18N
        localToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Local_Tooltip")); // NOI18N
        localToggle.setFocusable(false);
        localToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        localToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(localToggle);

        jPanel4.setMaximumSize(new java.awt.Dimension(12, 32767));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 11, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel4);

        btnGroup.add(remoteToggle);
        remoteToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/remote_vs_remote.png"))); // NOI18N
        remoteToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Remote_Tooltip")); // NOI18N
        remoteToggle.setFocusable(false);
        remoteToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        remoteToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(remoteToggle);

        jPanel1.setMaximumSize(new java.awt.Dimension(80, 32767));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 79, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel1);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-next.png"))); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Next_Tooltip")); // NOI18N
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(nextButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-prev.png"))); // NOI18N
        prevButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Prev_Tooltip")); // NOI18N
        prevButton.setFocusable(false);
        prevButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(prevButton);

        jPanel2.setMaximumSize(new java.awt.Dimension(30, 32767));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 29, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel2);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "refreshButton.toolTipText")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        controlsToolBar.add(refreshButton);

        updateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/update.png"))); // NOI18N
        updateButton.setFocusable(false);
        updateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        controlsToolBar.add(updateButton);

        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/commit.png"))); // NOI18N
        commitButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MSG_CommitDiff_Tooltip")); // NOI18N
        commitButton.setFocusable(false);
        commitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        commitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(commitButton);

        jPanel5.setMaximumSize(new java.awt.Dimension(20, 32767));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 28, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel5);

        filterPropertiesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/properties.png"))); // NOI18N
        filterPropertiesButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.filterPropertiesButton.toolTipText")); // NOI18N
        filterPropertiesButton.setFocusable(false);
        controlsToolBar.add(filterPropertiesButton);

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jLabel1.setLabelFor(cmbDiffTreeFirst);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.jLabel1.TTtext")); // NOI18N

        cmbDiffTreeFirst.setEnabled(false);

        jLabel2.setText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.jLabel2.TTtext")); // NOI18N

        cmbDiffTreeSecond.setEnabled(false);

        javax.swing.GroupLayout treeSelectionPanelLayout = new javax.swing.GroupLayout(treeSelectionPanel);
        treeSelectionPanel.setLayout(treeSelectionPanelLayout);
        treeSelectionPanelLayout.setHorizontalGroup(
            treeSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(treeSelectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDiffTreeFirst, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbDiffTreeSecond, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        treeSelectionPanelLayout.setVerticalGroup(
            treeSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(treeSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jLabel1)
                .addComponent(cmbDiffTreeFirst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel2)
                .addComponent(cmbDiffTreeSecond, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controlsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
            .addComponent(splitPane)
            .addComponent(treeSelectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controlsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(treeSelectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        onUpdateButton();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        onRefreshButton();
    }//GEN-LAST:event_refreshButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton allToggle;
    private javax.swing.ButtonGroup btnGroup;
    private javax.swing.JComboBox cmbDiffTreeFirst;
    final javax.swing.JComboBox cmbDiffTreeSecond = new javax.swing.JComboBox();
    private javax.swing.JButton commitButton;
    private javax.swing.JToolBar controlsToolBar;
    private javax.swing.JToggleButton filterPropertiesButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JToggleButton localToggle;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToggleButton remoteToggle;
    private javax.swing.JSplitPane splitPane;
    final javax.swing.JPanel treeSelectionPanel = new javax.swing.JPanel();
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
    
    /** Interprets property blob. */
    static final class Property {
        final byte[] value;

        Property(Object value) {
            this.value = (byte[]) value;
        }

        String getMIME() {            
            return "text/plain"; // NOI18N
        }

        Reader toReader() {
            if (SvnUtils.isBinary(value)) {
                return new StringReader(NbBundle.getMessage(MultiDiffPanel.class, "LBL_Diff_NoBinaryDiff"));  // hexa-flexa txt? // NOI18N
            } else {
                try {
                    return new InputStreamReader(new ByteArrayInputStream(value), "utf8");  // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    Subversion.LOG.log(Level.SEVERE, null, ex);
                    return new StringReader("[ERROR: " + ex.getLocalizedMessage() + "]"); // NOI18N
                }
            }
        }
    }
}
