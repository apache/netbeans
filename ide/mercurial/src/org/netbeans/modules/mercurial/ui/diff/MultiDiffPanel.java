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

package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.util.common.FileViewComponent;
import org.openide.util.Cancellable;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.status.StatusAction;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
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
import java.util.logging.Level;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.add.AddAction;
import org.netbeans.modules.mercurial.ui.annotate.AnnotateAction;
import org.netbeans.modules.mercurial.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.mercurial.ui.repository.HeadRevisionPicker;
import org.netbeans.modules.mercurial.ui.status.OpenInEditorAction;
import org.netbeans.modules.mercurial.ui.update.RevertModificationsAction;
import org.netbeans.modules.mercurial.util.HgCommand;
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
import static org.netbeans.modules.versioning.util.CollectionUtils.copyArray;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Mnemonics;
import org.openide.util.Mutex;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Maros Sandor
 */
public class MultiDiffPanel extends javax.swing.JPanel implements ActionListener, DiffSetupSource, PropertyChangeListener, PreferenceChangeListener {
    
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
    private final VCSContext context;

    private int displayStatuses;

    /**
     * Display name of the context of this diff.
     */ 
    private final String contextName;
    
    private int currentType;
    private Setup currentSetup;
    
    private RequestProcessor.Task prepareTask;
    private DiffPrepareTask dpt;

    private AbstractAction nextAction;
    private AbstractAction          prevAction;
    
    /**
     * null for view that are not
     */
    private RequestProcessor.Task   refreshTask;

    private JComponent              diffView;
    private FileViewComponent<DiffNode> activeComponent;
    private FileViewComponent<DiffNode> fileListComponent;
    private FileViewComponent<DiffNode> fileTreeComponent;
    private boolean                 dividerSet;

    /**
     * panel that is used for displaying the diff if {@code JSplitPane}
     * is not used
     */
    private final PlaceholderPanel diffViewPanel;
    private JComponent infoPanelLoadingFromRepo;

    private HgProgressSupport executeStatusSupport;
    private final HgRevision revisionOriginalLeft;
    private final HgRevision revisionOriginalRight;
    private HgRevision revisionLeft;
    private HgRevision revisionRight;
    private final boolean fixedRevisions;
    @NbBundle.Messages("MSG_Revision_Select=Select...")
    private static final String REVISION_SELECT = Bundle.MSG_Revision_Select();
    @NbBundle.Messages("MSG_Revision_Select_Separator=----------------------")
    private static final String REVISION_SELECT_SEP = Bundle.MSG_Revision_Select_Separator();
    private boolean displayUnversionedFiles = true;
    private RequestProcessor.Task refreshComboTask;
    private static final Logger LOG = Logger.getLogger(MultiDiffPanel.class.getName());
    
    private static final int VIEW_MODE_TABLE = 1;
    private static final int VIEW_MODE_TREE = 2;
    private int currentSetupDiffLengthChanged;
    private int lastDividerLoc;
    private int requestedRightLine = -1;
    private int requestedLeftLine = -1;
    private DiffViewModeSwitcher diffViewModeSwitcher;
    
    /**
     * Creates diff panel and immediatelly starts loading...
     */
    public MultiDiffPanel (VCSContext context, int initialType, String contextName) {
        this(context, contextName, HgLogMessage.HgRevision.BASE, HgLogMessage.HgRevision.CURRENT, false);
        currentType = initialType;
        refreshStatuses();
    }

    private MultiDiffPanel (VCSContext context, String contextName, HgRevision revisionLeft,
            HgRevision revisionRight, boolean fixedRevisions) {
        this.context = context;
        this.contextName = contextName;
        this.revisionLeft = revisionOriginalLeft = revisionLeft;
        this.revisionRight = revisionOriginalRight = revisionRight;
        this.fixedRevisions = fixedRevisions;
        initComponents();
        setAquaBackground();

        diffViewPanel = null;
        initFileComponent();
        initToolbarButtons();
        initNextPrevActions();
        splitPane.setDividerLocation(getActiveFileComponent().getPreferredHeaderHeight());
        initSelectionCombos();
        refreshComboTask = Mercurial.getInstance().getRequestProcessor().create(new RefreshComboTask());
        refreshSelectionCombos();
        refreshComponents();

        refreshTask = Mercurial.getInstance().getRequestProcessor().create(new RefreshViewTask());
    }

    public MultiDiffPanel (File[] roots, HgRevision revisionLeft, HgRevision revisionRight,
            boolean fixedRevisions, boolean displayUnversionedFiles) {
        this(HgUtils.buildVCSContext(roots), null, revisionLeft, revisionRight, fixedRevisions);
        currentType = Setup.DIFFTYPE_LOCAL;        
        this.displayUnversionedFiles = displayUnversionedFiles;
        refreshStatuses();
    }

    private void setAquaBackground() {
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            controlsToolBar.setBackground(color); 
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
    public MultiDiffPanel(File file, HgRevision rev1, HgRevision rev2, boolean forceNonEditable) {
        this(file, rev1, rev2, new FileInformation(), forceNonEditable, -1);
    }

    /**
     * Construct diff component showing just one file.
     * It hides All, Local, Remote toggles and file chooser combo.
     */
    public MultiDiffPanel(File file, HgRevision rev1, HgRevision rev2, FileInformation fi, boolean forceNonEditable,
            int requestedRightLine) {
        context = null;
        contextName = file.getName();
        revisionOriginalLeft = rev1;
        revisionOriginalRight = rev2;
        fixedRevisions = true;
        this.requestedRightLine = requestedRightLine;
        initComponents();
        initSelectionCombos();
        setAquaBackground();

        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);
        initToolbarButtons();
        initNextPrevActions();

        // mimics refreshSetups()
        Setup[] localSetups = new Setup[] {new Setup(file, rev1, rev2, fi, forceNonEditable)};
        File root = Mercurial.getInstance().getRepositoryRoot(file);
        localSetups[0].setNode(new DiffNode(localSetups[0], new HgFileNode(root, file)));
        setSetups(localSetups, DiffUtils.setupsToEditorCookies(localSetups));
        setDiffIndex(localSetups[0], 0, false);
        dpt = new DiffPrepareTask(setups);
        prepareTask = Mercurial.getInstance().getRequestProcessor().post(dpt);
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        removeAll();
        splitPane = null;
        setLayout(new BorderLayout());
        controlsToolBar.setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        add(controlsToolBar, BorderLayout.NORTH);
        add(replacement, BorderLayout.CENTER);
    }

    private void setSetups(Setup[] setups, EditorCookie[] editorCookies) {
        if (this.setups != null) {
            for (Setup setup : this.setups) {
                if (setup != null) {
                    setup.getFirstSource().close();
                    setup.getSecondSource().close();
                }
            }
        }
        this.setups = setups;
        this.editorCookies = editorCookies;
    }

    private boolean fileComponentSetSelectedIndexContext;

    void nodeSelected (DiffNode node) {
        if (fileComponentSetSelectedIndexContext) return;
        setDiffIndex(node == null ? null : node.getSetup(), 0, true);
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
        if (refreshTask != null) {
            refreshTask.cancel();
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
    public void componentClosed() {
        setSetups((Setup[]) null, null);
        /**
         * must disable these actions, otherwise key shortcuts would trigger them even after tab closure
         * see #159266
         */
        prevAction.setEnabled(false);
        nextAction.setEnabled(false);
        cancelBackgroundTasks(); 
        this.dpt = null;
        DiffViewModeSwitcher.release(this);
        diffViewModeSwitcher = null;
    }

    public JPopupMenu getPopup() {
        JPopupMenu menu = null;

        if (isLocal()) {
            menu = new JPopupMenu();
            JMenuItem item;
            item = menu.add(new OpenInEditorAction());
            Mnemonics.setLocalizedText(item, item.getText());
            if (isLocalToBase()) {
                menu.addSeparator();
                item = menu.add(new SystemActionBridge(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "CTL_PopupMenuItem_Add"))); // NOI18N
                Mnemonics.setLocalizedText(item, item.getText());
                item = menu.add(new SystemActionBridge(SystemAction.get(CommitAction.class), actionString("CTL_PopupMenuItem_Commit"))); // NOI18N
                Mnemonics.setLocalizedText(item, item.getText());

                menu.addSeparator();
                item = menu.add(new SystemActionBridge(SystemAction.get(AnnotateAction.class),
                                                       ((AnnotateAction)SystemAction.get(AnnotateAction.class)).visible(null) ?
                                                       actionString("CTL_PopupMenuItem_HideAnnotations") : //NOI18N
                                                       actionString("CTL_PopupMenuItem_ShowAnnotations"))); //NOI18N
                Mnemonics.setLocalizedText(item, item.getText());
                menu.addSeparator();

                boolean allLocallyDeleted = true;
                FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
                Set<File> files = HgUtils.getCurrentContext(null).getRootFiles();

                for (File file : files) {
                    FileInformation info = cache.getStatus(file);
                    if (info.getStatus() != FileInformation.STATUS_VERSIONED_DELETEDLOCALLY && info.getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                        allLocallyDeleted = false;
                    }
                }
                if (allLocallyDeleted) {
                    item = menu.add(new SystemActionBridge(SystemAction.get(RevertModificationsAction.class), actionString("CTL_PopupMenuItem_RevertDelete"))); //NOI18N
                } else {
                    item = menu.add(new SystemActionBridge(SystemAction.get(RevertModificationsAction.class), actionString("CTL_PopupMenuItem_GetClean"))); //NOI18N
                }
                Mnemonics.setLocalizedText(item, item.getText());

                item = menu.add(new AbstractAction(actionString("CTL_PopupMenuItem_ExportDiffChanges")) { //NOI18N
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SystemAction.get(ExportDiffChangesAction.class).performContextAction(null, true);
                    }
                });
                Mnemonics.setLocalizedText(item, item.getText());

                String label;
                ExcludeFromCommitAction exclude = (ExcludeFromCommitAction) SystemAction.get(ExcludeFromCommitAction.class);
                if (exclude.getActionStatus(null) == ExcludeFromCommitAction.INCLUDING) {
                    label = actionString("CTL_PopupMenuItem_IncludeInCommit");  //NOI18N
                } else {
                    label = actionString("CTL_PopupMenuItem_ExcludeFromCommit"); //NOI18N
                }
                item = menu.add(new SystemActionBridge(exclude, label));
                Mnemonics.setLocalizedText(item, item.getText());
            }
        }

        return menu;
    }

    /**
     * Workaround.
     * I18N Test Wizard searches for keys in syncview package Bundle.properties
     */
    private String actionString(String key) {
        ResourceBundle actionsLoc = NbBundle.getBundle(MercurialAnnotator.class);
        return actionsLoc.getString(key);
    }

    public void requestActive() {
        FileViewComponent comp = getActiveFileComponent();
        if (comp != null) {
            comp.getComponent().requestFocusInWindow();
        } else if (diffView != null) {
            diffView.requestFocusInWindow();
        }
    }

    private void initFileComponent() {
        fileListComponent = new DiffFileTable(this);
        fileTreeComponent = new DiffFileTreeImpl(this);
        int viewMode = HgModuleConfig.getDefault().getDiffViewMode(VIEW_MODE_TABLE);
        if (viewMode == VIEW_MODE_TREE) {
            treeButton.setSelected(true);
            setActiveComponent(fileTreeComponent);
        } else {
            listButton.setSelected(true);
            setActiveComponent(fileListComponent);
        }
        splitPane.setBottomComponent(getInfoPanelLoading());
    }

    private void initToolbarButtons() {
        if (context != null) {
            commitButton.addActionListener(this);
            listButton.addActionListener(this);
            treeButton.addActionListener(this);
            commitButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "CTL_CommitDiff_Tooltip"));
            commitButton.setEnabled(false);     //until the diff is loaded
        } else {
            commitButton.setVisible(false);
            refreshButton.setVisible(false);
            listButton.setVisible(false);
            treeButton.setVisible(false);
        }
    }

    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-next.png"))) {  // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/mercurial/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Next_Tooltip"));                
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/mercurial/ui/diff/Bundle").
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
        if (currentSetup == null) {
            nextAction.setEnabled(false);
            prevAction.setEnabled(false);
            return;
        }
        DiffController view = currentSetup.getView();
        int currentDifferenceIndex = view != null ? view.getDifferenceIndex() : -1;
        if (view != null) {
            nextAction.setEnabled(showingFileComponent() && getActiveFileComponent().hasNextNode(currentSetup.getNode())
                    || currentDifferenceIndex < view.getDifferenceCount() - 1);
        } else {
            nextAction.setEnabled(false);
        }
        prevAction.setEnabled(showingFileComponent() && getActiveFileComponent().hasPreviousNode(currentSetup.getNode())
                || currentDifferenceIndex > 0);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (refreshTask != null) {
            Mercurial.getInstance().getFileStatusCache().addPropertyChangeListener(this);
            HgModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
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
        FileViewComponent fileComp = getActiveFileComponent();
        int preferredHeaderHeight = fileComp.getPreferredHeaderHeight();
        int preferredHeight = fileComp.getPreferredHeight();
        int optimalLocation = preferredHeight + preferredHeaderHeight + 5;
        if (optimalLocation > dim.height / 3) {
            optimalLocation = dim.height / 3;
        }
        if (optimalLocation <= preferredHeaderHeight) {
            optimalLocation = preferredHeaderHeight * 3;
        }
        splitPane.setDividerLocation(optimalLocation);
    }
    
    @Override
    public void removeNotify() {
        if (splitPane != null) {
            lastDividerLoc = splitPane.getDividerLocation();
        }
        Mercurial.getInstance().getFileStatusCache().removePropertyChangeListener(this);
        if (refreshTask != null) {
            HgModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        }
        if (diffView != null) {
            // Traverse children components of controller panel and release the editor panes
            // from editor registry and annotation holder
            releaseChildrenPanes(diffView);
        }
        super.removeNotify();
    }
    
    private static void releaseChildrenPanes(JComponent c) {
        for (int i = c.getComponentCount() - 1; i >= 0; i--) {
            java.awt.Component ac = c.getComponent(i);
            if (ac instanceof JComponent) {
                JComponent ch = (JComponent) ac;
                if (Boolean.TRUE.equals(ch.getClientProperty("usedByCloneableEditor"))) {
                    ch.putClientProperty("usedByCloneableEditor", Boolean.FALSE);
                } else {
                    releaseChildrenPanes(ch);
                }
            }
        }
    }
    
    
    private boolean affectsView(PropertyChangeEvent event) {
        FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
        File file = changedEvent.getFile();
        FileInformation oldInfo = changedEvent.getOldInfo();
        FileInformation newInfo = changedEvent.getNewInfo();
        if (oldInfo == null) {
            if ((newInfo.getStatus() & displayStatuses) == 0) return false;
        } else {
            if ((oldInfo.getStatus() & displayStatuses) + (newInfo.getStatus() & displayStatuses) == 0) return false;
        }
        return isLocal() && containsFile(file);
    }
    
    private boolean containsFile (File file) {
        if (context != null) {
            return HgUtils.contains(context.getRootFiles(), file);
        } else {
            return false;
        }
    }
    
    private void setDiffIndex(Setup selectedSetup, int location, boolean restartPrepareTask) {
        currentSetup = selectedSetup;
        currentSetupDiffLengthChanged = -1;
        
        if (currentSetup != null) {
            if (restartPrepareTask && dpt != null) {
                dpt.cancel();
                dpt.setSelectedSetup(currentSetup);
                prepareTask.schedule(100);
            }
            DiffController view = currentSetup.getView();

            TopComponent tc = (TopComponent) getClientProperty(TopComponent.class);
            if (tc != null) {
                Node node = currentSetup.getNode();
                tc.setActivatedNodes(new Node[] {node == null ? Node.EMPTY : node});
            }
            
            diffView = null;
            if (view != null) {
                currentSetupDiffLengthChanged = view.getDifferenceCount();
                if (showingFileComponent()) {
                    fileComponentSetSelectedIndexContext = true;
                    getActiveFileComponent().setSelectedNode(currentSetup.getNode());
                    fileComponentSetSelectedIndexContext = false;
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
            diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoFileSelected"));
            displayDiffView();
        }

        delegatingUndoRedo.setDiffView(diffView);

        refreshComponents();
    }

    private boolean showingFileComponent() {
        return getActiveFileComponent() != null;
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
        if (source == commitButton) {
            onCommitButton();
        } else if (source == cmbDiffTreeSecond) {
            HgRevision oldSelection = revisionLeft;
            HgRevision newSelection = getSelectedRevision(cmbDiffTreeSecond);
            if (newSelection != null) {
                revisionLeft = newSelection;
            }
            boolean refresh = !oldSelection.getChangesetId().equals(revisionLeft.getChangesetId());
            if (refresh) {
                refreshStatuses();
            }
        } else if (source == cmbDiffTreeFirst) {
            HgRevision oldSelection = revisionRight;
            HgRevision newSelection = getSelectedRevision(cmbDiffTreeFirst);
            if (newSelection != null) {
                revisionRight = newSelection;
            }
            boolean refresh = !oldSelection.getChangesetId().equals(revisionRight.getChangesetId());
            if (refresh) {
                refreshStatuses();
            }
        } else if (source == listButton) {
            setActiveComponent(fileListComponent);
            setActiveDiff();
        } else if (source == treeButton) {
            setActiveComponent(fileTreeComponent);
            setActiveDiff();
        }
    }

    private void setActiveDiff () {
        DiffNode selectedNode = activeComponent.getSelectedNode();
        setDiffIndex(selectedNode == null ? null : selectedNode.getSetup(), 0, false);
    }
    
    private HgRevision getSelectedRevision (JComboBox cmbDiffTree) {
        Object selectedItem = cmbDiffTree.getSelectedItem();
        HgRevision selection = null;
        if (selectedItem instanceof HgRevision) {
            selection = (HgRevision) selectedItem;
        } else if (selectedItem instanceof HgLogMessage) {
            selection = ((HgLogMessage) selectedItem).getHgRevision();
        } else if (selectedItem == REVISION_SELECT) {
            File repository = HgUtils.getRootFile(context);
            if (repository == null) {
                LOG.log(Level.INFO, "getSelectedRevision: No repository for {0}", context.getRootFiles()); //NOI18N
                return null;
            }
            HeadRevisionPicker picker = new HeadRevisionPicker(repository, null);
            if (picker.showDialog()) {
                HgLogMessage selectedRevision = picker.getSelectionRevision();
                selection = selectedRevision.getHgRevision();
                addToModel(selectedRevision, cmbDiffTree);
            }
        }
        return selection;
    }

    private void addToModel (final HgLogMessage newItem, final JComboBox cmbDiffTree) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) cmbDiffTree.getModel();
        for (int i = 0; i < model.getSize(); ++i) {
            final Object item = model.getElementAt(i);
            if (item instanceof HgLogMessage && ((HgLogMessage) item).getCSetShortID().equals(newItem.getCSetShortID())) {
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
        refreshStatuses();
    }

    private void refreshStatuses() {
        commitButton.setEnabled(false);
        if ((context == null || context.getRootFiles().isEmpty())) {
            return;
        }

        if(executeStatusSupport!=null) {
            executeStatusSupport.cancel();
            executeStatusSupport = null;
        }
        
        LifecycleManager.getDefault().saveAll();
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        executeStatusSupport = new HgProgressSupport() {
            @Override
            public void perform() {
                if (context != null && revisionLeft == HgRevision.BASE && isLocal()) {
                    StatusAction.executeStatus(context, this);
                }
                refreshSetups();
            }
        };
        File repositoryRoot = HgUtils.getRootFile(context);
        if (repositoryRoot == null) {
            LOG.log(Level.INFO, "getSelectedRevision: No repository for {0}", context.getRootFiles()); //NOI18N
            return;
        }
        executeStatusSupport.start(rp, repositoryRoot, NbBundle.getMessage(MultiDiffPanel.class, "MSG_Refresh_Progress"));
    }
    
    private void onCommitButton() {
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        if ((saveCookies.length == 0)
                || SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
            CommitAction.commit(contextName, context);
        }
    }

    /** Next that is driven by visibility. It continues to next not yet visible difference. */
    private void onNextButton() {
        DiffController view = currentSetup.getView();
        DiffNode next = showingFileComponent() ? getActiveFileComponent().getNextNode(currentSetup.getNode()) : null;
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (currentSetupDiffLengthChanged >= 0) {
                // jump to the next difference only when the diffs did not change (no difference was removed)
                // otherwise the next difference might be missed
                ++currentDifferenceIndex;
            }
            currentSetupDiffLengthChanged = view.getDifferenceCount();
            if (currentDifferenceIndex >= view.getDifferenceCount()) { // also passes for view.getDifferenceCount() == 0
                if (next != null) {
                    setDiffIndex(next.getSetup(), 0, true);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (next != null) {
                setDiffIndex(next.getSetup(), 0, true);
            }
        }
        refreshComponents();
    }

    private void onPrevButton() {
        DiffController view = currentSetup.getView();
        DiffNode prev = showingFileComponent() ? getActiveFileComponent().getPreviousNode(currentSetup.getNode()) : null;
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (currentSetupDiffLengthChanged >= 0) {
                // jump to the previous difference only when the diffs did not change (no difference was removed)
                // otherwise the previous difference might be missed
                --currentDifferenceIndex;
            }
            currentSetupDiffLengthChanged = view.getDifferenceCount();
            if (currentDifferenceIndex < 0) {
                if (prev != null) {
                    setDiffIndex(prev.getSetup(), -1, true);
                }
            } else if (currentDifferenceIndex < view.getDifferenceCount()) {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (prev != null) {
                setDiffIndex(prev.getSetup(), -1, true);
            }
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


    private void refreshSetups() {
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
        default:
            throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
        }
        final int localDisplayStatuses = status;
        final Setup[] localSetups;
        if (revisionLeft == HgRevision.BASE && isLocal()) {
            File [] files = HgUtils.getModifiedFiles(context, status, true);
            localSetups = computeSetups(files);
        } else {
            localSetups = computeSetupsBetweenRevisions();
        }
        Arrays.sort(localSetups, new Comparator<Setup>() {
            @Override
            public int compare (Setup o1, Setup o2) {
                return o2.getNode().getLocation().compareTo(o1.getNode().getLocation());
            }
        });
        final EditorCookie[] cookies = DiffUtils.setupsToEditorCookies(localSetups);
        final DiffNode[] nodes = setupsToNodes(localSetups);
        final Object modelDataList = fileListComponent.prepareModel(nodes);
        final Object modelDataTree = fileTreeComponent.prepareModel(nodes);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dividerSet = false;
                displayStatuses = localDisplayStatuses;
                setSetups(localSetups, cookies);
                FileViewComponent<DiffNode> activeFileComponent = getActiveFileComponent();
                fileListComponent.setModel(nodes, editorCookies, modelDataList);
                fileTreeComponent.setModel(nodes, editorCookies, modelDataTree);
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
                        default:
                            throw new IllegalStateException("Unknown DIFF type:" + currentType);
                    }
                    setSetups((Setup[]) null, null);
                    fileListComponent.getComponent().setEnabled(false);
                    fileListComponent.getComponent().setPreferredSize(null);
                    fileTreeComponent.getComponent().setEnabled(false);
                    fileTreeComponent.getComponent().setPreferredSize(null);
                    Dimension dim = activeFileComponent.getComponent().getPreferredSize();
                    activeFileComponent.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                    diffView = null;
                    diffView = new NoContentPanel(noContentLabel);
                    displayDiffView();
                    nextAction.setEnabled(false);
                    prevAction.setEnabled(false);
                    commitButton.setEnabled(false);
                    revalidate();
                    repaint();
                } else {
                    fileListComponent.getComponent().setEnabled(true);
                    fileListComponent.getComponent().setPreferredSize(null);
                    fileTreeComponent.getComponent().setEnabled(true);
                    fileTreeComponent.getComponent().setPreferredSize(null);
                    Dimension dim = activeFileComponent.getComponent().getPreferredSize();
                    activeFileComponent.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                    Setup toSelect = activeFileComponent.getNodeAtPosition(0).getSetup();
                    setDiffIndex(toSelect, 0, false);
                    commitButton.setEnabled(isLocalToBase());
                    dpt = new DiffPrepareTask(setups);
                    dpt.setSelectedSetup(toSelect);
                    prepareTask = Mercurial.getInstance().getRequestProcessor().post(dpt);
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException ex) {
                Mercurial.LOG.log(Level.FINE, null, ex);
            } catch (InvocationTargetException ex) {
                Mercurial.LOG.log(Level.FINE, null, ex);
            }
        }
    }
    
    private static DiffNode[] setupsToNodes (Setup[] setups) {
        DiffNode[] nodes = new DiffNode[setups.length];
        for (int i = 0; i < setups.length; i++) {
            nodes[i] = setups[i].getNode();
        }
        return nodes;
    }

    private boolean isLocal () {
        return revisionRight == HgRevision.CURRENT;
    }

    private boolean isLocalToBase () {
        return isLocal() && (revisionLeft == HgRevision.BASE || revisionLeft == HgRevision.QDIFF_BASE);
    }

    private Setup[] computeSetups(File[] files) {
        List<Setup> newSetups = new ArrayList<Setup>(files.length);
        Mercurial hg = Mercurial.getInstance();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()) {
                Setup setup = new Setup(file, null, currentType);
                setup.setNode(new DiffNode(setup, new HgFileNode(hg.getRepositoryRoot(file), file)));
                newSetups.add(setup);
            }
        }
        newSetups.sort(new SetupsComparator());
        return newSetups.toArray(new Setup[0]);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            // something has changed
            if (currentSetup != null && currentSetup.getView() != null) {
                if (currentSetup.getView().getDifferenceCount() < currentSetupDiffLengthChanged) {
                    currentSetupDiffLengthChanged = -1;
                } else if (currentSetupDiffLengthChanged != -1) {
                    currentSetupDiffLengthChanged = currentSetup.getView().getDifferenceCount();
                }
                if (currentSetup.getView().getDifferenceCount() > 0 && requestedRightLine != -1) {
                    final int leftLine = requestedLeftLine;
                    final int rightLine = requestedRightLine;
                    requestedRightLine = requestedLeftLine = -1;
                    final DiffController view = currentSetup.getView();
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.LineNumber, rightLine);
                            if (leftLine != -1) {
                                view.getJComponent().putClientProperty("diff.smartScrollDisabled", Boolean.TRUE);
                                view.setLocation(DiffController.DiffPane.Base, DiffController.LocationType.LineNumber, leftLine);
                            }
                        }
                    });
                }
            }
            refreshComponents();
        } else if (FileStatusCache.PROP_FILE_STATUS_CHANGED.equals(evt.getPropertyName())) {
            if (!affectsView(evt)) {
                return;
            }
            refreshTask.schedule(200);
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }

    private Setup[] computeSetupsBetweenRevisions () {
        File repository = HgUtils.getRootFile(context);
        HgRevision revLeft = revisionLeft;
        HgRevision revRight = revisionRight;
        try {
            if (revisionLeft == HgRevision.BASE || revisionRight == HgRevision.BASE) {
                // optimalization, let's not run the command when the revisions are the same
                HgRevision parent = HgCommand.getParent(repository, null, null);
                if (revLeft == HgRevision.BASE) {
                    revLeft = parent;
                }
                if (revRight == HgRevision.BASE) {
                    revRight = parent;
                }
            }
            // optimalization, let's not run the command when the revisions are the same
            if (!revLeft.getChangesetId().equals(revRight.getChangesetId())) {
                Map<File, FileInformation> statuses = HgCommand.getStatus(repository, new ArrayList<File>(context.getRootFiles()),
                        revisionLeft.getChangesetId(), revisionRight.getChangesetId(), isLocalToBase());
                statuses.keySet().retainAll(Utils.flattenFiles(context.getRootFiles().toArray(
                        new File[context.getRootFiles().size()]), statuses.keySet()));
                List<Setup> newSetups = new ArrayList<Setup>(statuses.size());
                Mercurial hg = Mercurial.getInstance();
                for (Map.Entry<File, FileInformation> e : statuses.entrySet()) {
                    File file = e.getKey();
                    FileInformation fi = e.getValue();
                    if (isVisible(file, fi)) {
                        Setup setup = new Setup(file, revisionLeft, revisionRight, fi, false);
                        setup.setNode(new DiffNode(setup, new HgFileNode(hg.getRepositoryRoot(file), file)));
                        newSetups.add(setup);
                    }
                }
                return newSetups.toArray(new Setup[0]);
            }
        } catch (HgException.HgCommandCanceledException ex) {
        } catch (HgException ex) {
            Mercurial.LOG.log(Level.INFO, null, ex);
        }
        return new Setup[0];
    }
    
    private boolean isVisible (File file, FileInformation fi) {
        boolean allowed = (fi.getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) == 0;
        if (!allowed && displayUnversionedFiles) {
            // check it's not ignored
            FileStatusCache cache = Mercurial.getInstance().getFileStatusCache();
            allowed = (cache.getStatus(file).getStatus() & FileInformation.STATUS_NOTVERSIONED_EXCLUDED) == 0;
        }
        return allowed;
    }

    private void refreshSelectionCombos () {
        if (!fixedRevisions && HgUtils.getRepositoryRoots(context).size() == 1) {
            cmbDiffTreeFirst.setEnabled(false);
            cmbDiffTreeSecond.setEnabled(false);
            refreshComboTask.schedule(100);
        }
    }

    private void initSelectionCombos () {
        if (fixedRevisions) {
            treeSelectionPanel.setVisible(false);
        } else {
            cmbDiffTreeFirst.setMinimumSize(cmbDiffTreeFirst.getMinimumSize());
            cmbDiffTreeSecond.setMinimumSize(cmbDiffTreeSecond.getMinimumSize());
            treeSelectionPanel.setMinimumSize(treeSelectionPanel.getMinimumSize());
            cmbDiffTreeFirst.setRenderer(new HgRevisionCellRenderer());
            cmbDiffTreeFirst.setModel(new DefaultComboBoxModel(new HgRevision[] { revisionRight }));
            cmbDiffTreeFirst.addActionListener(this);
            cmbDiffTreeSecond.setRenderer(new HgRevisionCellRenderer());
            cmbDiffTreeSecond.setModel(new DefaultComboBoxModel(new HgRevision[] { revisionLeft }));
            cmbDiffTreeSecond.addActionListener(this);
        }
    }

    private FileViewComponent<DiffNode> getActiveFileComponent () {
        return activeComponent;
    }

    private void setActiveComponent (FileViewComponent<DiffNode> fileComponent) {
        if (activeComponent == fileComponent) {
            return;
        }
        activeComponent = fileComponent;
        HgModuleConfig.getDefault().setDiffViewMode(activeComponent == fileListComponent
                ? VIEW_MODE_TABLE : VIEW_MODE_TREE);
        int gg = splitPane.getDividerLocation();
        splitPane.setTopComponent(getActiveFileComponent().getComponent());
        splitPane.setDividerLocation(gg);
    }

    private DiffViewModeSwitcher getDiffViewModeSwitcher () {
        if (diffViewModeSwitcher == null) {
            diffViewModeSwitcher = DiffViewModeSwitcher.get(this);
        }
        return diffViewModeSwitcher;
    }
    
    private class RefreshComboTask implements Runnable {

        @Override
        public void run () {
            File repository = HgUtils.getRootFile(context);
            final List<Object> modelRight = new ArrayList<Object>(10);
            final List<Object> modelLeft = new ArrayList<Object>(10);
            modelLeft.add(revisionOriginalLeft);
            if (revisionOriginalLeft != HgRevision.BASE) {
                modelLeft.add(HgRevision.BASE);
            }
            modelRight.add(revisionOriginalRight);            
            if (revisionOriginalRight != HgRevision.CURRENT) {
                modelRight.add(HgRevision.CURRENT);
            }
            if (revisionOriginalRight != HgRevision.BASE) {
                modelRight.add(HgRevision.BASE);
            }
            try {
                HgLogMessage[] heads = HgCommand.getHeadRevisionsInfo(repository, false, OutputLogger.getLogger(null));
                Map<String, Collection<HgLogMessage>> branchHeads = HgUtils.sortByBranch(heads);
                for (Map.Entry<String, Collection<HgLogMessage>> e : branchHeads.entrySet()) {
                    for (HgLogMessage msg : e.getValue()) {
                        modelLeft.add(msg);
                        modelRight.add(msg);
                    }
                }
            } catch (HgException.HgCommandCanceledException ex) {
            } catch (HgException ex) {
                LOG.log(Level.INFO, null, ex);
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    modelLeft.add(REVISION_SELECT_SEP);
                    modelLeft.add(REVISION_SELECT);
                    modelLeft.add(REVISION_SELECT_SEP);
                    modelRight.add(REVISION_SELECT_SEP);
                    modelRight.add(REVISION_SELECT);
                    modelRight.add(REVISION_SELECT_SEP);
                    cmbDiffTreeFirst.setModel(new DefaultComboBoxModel(modelRight.toArray(new Object[0])));
                    cmbDiffTreeSecond.setModel(new DefaultComboBoxModel(modelLeft.toArray(new Object[0])));
                    cmbDiffTreeFirst.setEnabled(true);
                    cmbDiffTreeSecond.setEnabled(true);
                }
            });
        }
    }

    @NbBundle.Messages({
        "MSG_Revision_Select_Tooltip=Select a revision from the picker"
    })
    private static class HgRevisionCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String tooltip = null;
            if (value instanceof HgRevision) {
                HgRevision rev = (HgRevision) value;
                value = rev.toString();
            } else if (value instanceof HgLogMessage) {
                HgLogMessage message = (HgLogMessage) value;
                StringBuilder sb = new StringBuilder(60).append(message.getRevisionNumber());
                StringBuilder labels = new StringBuilder();
                for (String branch : message.getBranches()) {
                    labels.append(branch).append(' ');
                }
                for (String tag : message.getTags()) {
                    labels.append(tag).append(' ');
                    break; // just one tag
                }
                sb.append(" (").append(labels).append(labels.length() == 0 ? "" : "- ").append(message.getCSetShortID().substring(0, 7)).append(")"); //NOI18N
                String shortMsg = message.getShortMessage();
                
                if (!shortMsg.isEmpty()) {
                    sb.append(" - ").append(shortMsg);
                    if (sb.length() > 50) {
                        tooltip = shortMsg;
                        sb.setLength(47);
                        sb.append("..."); //NOI18N
                    }
                }
                value = sb.toString();
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

    private class DiffPrepareTask implements Runnable, Cancellable {
        
        private final Setup[] prepareSetups;
        private Setup selectedSetup;
        private boolean canceled;

        public DiffPrepareTask(Setup [] prepareSetups) {
            this.prepareSetups = prepareSetups;
            this.selectedSetup = prepareSetups[0];
        }

        @Override
        public void run() {
            canceled = false;
            IOException exception = null;
            DiffNode[] neighbourNodes = prepareSetupsToRefresh();
            for (DiffNode n : neighbourNodes) {
                final Setup s = n.getSetup();
                if (prepareSetups != setups || Thread.interrupted()) return;
                if (s.getView() != null) {
                    continue;
                }
                try {
                    s.initSources();  // slow network I/O
                    if (Thread.interrupted() || canceled) {
                        return;
                    }
                    StreamSource ss1 = s.getFirstSource();
                    StreamSource ss2 = s.getSecondSource();
                    if (requestedRightLine != -1) {
                        requestedLeftLine = getMatchingLine(ss2, ss1, requestedRightLine);
                    }
                    final DiffController view = DiffController.createEnhanced(ss1, ss2);  // possibly executing slow external diff
                    view.addPropertyChangeListener(MultiDiffPanel.this);
                    if (Thread.interrupted() || canceled) {
                        return;
                    }
                    s.setView(view);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (prepareSetups != setups) {
                                return;
                            }
                            if (MultiDiffPanel.this.currentSetup == s) {
                                setDiffIndex(s, 0, false);
                            }
                            if (splitPane != null) {
                                updateSplitLocation();
                            }
                        }
                    });
                } catch (IOException e) {
                    if (HgUtils.isCanceled(e)) {
                        Logger.getLogger(MultiDiffPanel.class.getName()).log(Level.FINE, null, e);
                        return;
                    } else {
                        Mercurial.LOG.log(Level.INFO, null, e);
                        if (exception == null) {
                            // save only the first exception
                            exception = e;
                        }
                    }
                }
            }
            if (exception != null) {
                HgUtils.notifyException(exception);
            }
        }

        private DiffNode[] prepareSetupsToRefresh () {
            return Mutex.EVENT.readAccess(new Mutex.Action<DiffNode[]>() {
                @Override
                public DiffNode[] run () {
                    return showingFileComponent()
                            ? getActiveFileComponent().getNeighbouringNodes(selectedSetup.getNode(), 2)
                            : new DiffNode[] { selectedSetup.getNode() };
                }
            });
        }

        private void setSelectedSetup (Setup setup) {
            this.selectedSetup = setup;
        }

        @Override
        public boolean cancel() {
            return this.canceled = true;
        }

        private int getMatchingLine (StreamSource ss2, StreamSource ss1, int requestedRightLine) {
            Reader currentReader = null, previousReader = null;
            try {
                currentReader = ss2.createReader();
                previousReader = ss1.createReader();
                return DiffUtils.getMatchingLine(currentReader, previousReader, requestedRightLine);
            } catch (IOException ex) {
                return -1;
            } finally {
                if (currentReader != null) {
                    try {
                        currentReader.close();
                    } catch (IOException ex) {}
                }
                if (previousReader != null) {
                    try {
                        previousReader.close();
                    } catch (IOException ex) {}
                }
            }
        }
    }

    private static class SetupsComparator implements Comparator<Setup> {

        private HgUtils.ByImportanceComparator delegate = new HgUtils.ByImportanceComparator();
        private FileStatusCache cache;

        public SetupsComparator() {
            cache = Mercurial.getInstance().getFileStatusCache();
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

    private class RefreshViewTask implements Runnable {
        @Override
        public void run() {
            refreshSetups();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewTypeGroup = new javax.swing.ButtonGroup();
        controlsToolBar = new javax.swing.JToolBar();
        listButton = new javax.swing.JToggleButton();
        treeButton = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        commitButton = new javax.swing.JButton();
        treeSelectionPanel = new javax.swing.JPanel();
        cmbDiffTreeFirst = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cmbDiffTreeSecond = new javax.swing.JComboBox();
        splitPane = new javax.swing.JSplitPane();

        controlsToolBar.setFloatable(false);
        controlsToolBar.setRollover(true);

        viewTypeGroup.add(listButton);
        listButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/file_view.png"))); // NOI18N
        listButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.listButton.toolTipText")); // NOI18N
        listButton.setFocusable(false);
        controlsToolBar.add(listButton);

        viewTypeGroup.add(treeButton);
        treeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/logical_view.png"))); // NOI18N
        treeButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.treeButton.toolTipText")); // NOI18N
        treeButton.setFocusable(false);
        controlsToolBar.add(treeButton);

        jPanel4.setMaximumSize(new java.awt.Dimension(80, 32767));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel4);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-next.png"))); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Next_Tooltip")); // NOI18N
        nextButton.setFocusable(false);
        controlsToolBar.add(nextButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-prev.png"))); // NOI18N
        prevButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Prev_Tooltip")); // NOI18N
        prevButton.setFocusable(false);
        controlsToolBar.add(prevButton);

        jPanel2.setMaximumSize(new java.awt.Dimension(30, 32767));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel2);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "refreshButton.toolTipText")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        controlsToolBar.add(refreshButton);

        jPanel5.setMaximumSize(new java.awt.Dimension(20, 32767));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 31, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel5);

        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/commit.png"))); // NOI18N
        commitButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MSG_CommitDiff_Tooltip")); // NOI18N
        commitButton.setFocusable(false);
        controlsToolBar.add(commitButton);

        cmbDiffTreeFirst.setEnabled(false);

        jLabel1.setLabelFor(cmbDiffTreeFirst);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.jLabel1.text")); // NOI18N
        jLabel1.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.jLabel1.TTtext")); // NOI18N

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
            .addGroup(treeSelectionPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(treeSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cmbDiffTreeFirst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(cmbDiffTreeSecond, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controlsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
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
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
        onRefreshButton();
    }                                             
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbDiffTreeFirst;
    private javax.swing.JComboBox cmbDiffTreeSecond;
    private javax.swing.JButton commitButton;
    private javax.swing.JToolBar controlsToolBar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JToggleButton listButton;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JToggleButton treeButton;
    private javax.swing.JPanel treeSelectionPanel;
    private javax.swing.ButtonGroup viewTypeGroup;
    // End of variables declaration//GEN-END:variables
    
}
