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

package org.netbeans.modules.git.ui.diff;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.libs.git.GitBranch;
import org.netbeans.libs.git.GitClient.DiffMode;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitFileNode;
import org.netbeans.modules.git.GitFileNode.GitHistoryFileNode;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClient;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.actions.AddAction;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;
import org.netbeans.modules.git.ui.commit.IncludeInCommitAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.ui.diff.DiffNode.DiffHistoryNode;
import org.netbeans.modules.git.ui.diff.DiffNode.DiffLocalNode;
import org.netbeans.modules.git.ui.ignore.IgnoreAction;
import org.netbeans.modules.git.ui.repository.RepositoryInfo;
import org.netbeans.modules.git.ui.repository.Revision;
import org.netbeans.modules.git.ui.repository.RevisionPicker;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.netbeans.modules.git.ui.status.StatusAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.diff.DiffViewModeSwitcher;
import org.netbeans.modules.versioning.diff.EditorSaveCookie;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.FileViewComponent;
import org.netbeans.modules.versioning.util.common.VCSCommitOptions;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.awt.Mnemonics;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author ondra
 */
public class MultiDiffPanelController implements ActionListener, PropertyChangeListener, PreferenceChangeListener {
    private final VCSContext context;
    private EnumSet<Status> displayStatuses;
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo();
    private Mode mode;
    private final MultiDiffPanel panel;
    private AbstractAction nextAction;
    private AbstractAction prevAction;

    /**
     * panel that is used for displaying the diff if {@code JSplitPane}
     * is not used
     */
    private PlaceholderPanel diffViewPanel;
    private JComponent infoPanelLoadingFromRepo;
    static final Logger LOG = Logger.getLogger(MultiDiffPanelController.class.getName());
    private DiffFileViewComponent<DiffNode> activeComponent;
    private DiffFileTable fileListComponent;
    private DiffFileTreeImpl fileTreeComponent;
    private static final RequestProcessor RP = new RequestProcessor("GitDiffWindow", 1); //NOI18N
    private final RefreshNodesTask refreshNodesTask = new RefreshNodesTask();
    private final ApplyChangesTask changeTask = new ApplyChangesTask();
    private final RequestProcessor.Task refreshNodesRPTask = RP.create(refreshNodesTask);
    private final RequestProcessor.Task changeRPTask = RP.create(changeTask);
    private static final String TEXT_DIFF = "text/x-diff"; //NOI18N
    private GitProgressSupport multiTextDiffSupport;
    private static final String PROP_SEARCH_CONTAINER = "diff.search.container"; //NOI18N

    private boolean dividerSet;

    // TODO Merge with GitVersioningPanelController

    /**
     * DIFF setups that we show in the DIFF view. Contents is changed when the user switches DIFF types or a file under the context changes.
     */
    private final Map<File, Setup> setups = new HashMap<File, Setup>();
    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private final Map<File, EditorCookie> editorCookies = new HashMap<File, EditorCookie>();
    private JComponent diffView;

    private RequestProcessor.Task prepareTask;
    private DiffPrepareTask dpt;

    private Setup currentSetup;

    private GitProgressSupport statusRefreshSupport;
    private PreferenceChangeListener prefList;
    
    private final Revision revisionOriginalLeft;
    private final Revision revisionOriginalRight;
    private Revision revisionLeft;
    private Revision revisionRight;
    private final boolean fixedRevisions;
    @NbBundle.Messages("MSG_Revision_Select=Select...")
    private static final String REVISION_SELECT = Bundle.MSG_Revision_Select();
    @NbBundle.Messages("MSG_Revision_Select_Separator=----------------------")
    private static final String REVISION_SELECT_SEP = Bundle.MSG_Revision_Select_Separator();
    private RequestProcessor.Task refreshComboTask;
    private boolean activated = true;
    private int popupViewIndex;
    private int requestedRightLine = -1;
    private int requestedLeftLine = -1;
    
    private static final int VIEW_MODE_TABLE = 1;
    private static final int VIEW_MODE_TREE = 2;
    private int currentSetupDiffLengthChanged;
    private boolean fileComponentSetSelectedIndexContext;
    private boolean popupAllowed;
    private DiffViewModeSwitcher diffViewModeSwitcher;

    public MultiDiffPanelController (VCSContext context, Revision rev1, Revision rev2) {
        this(context, rev1, rev2, false);
        this.popupAllowed = true;
        initFileComponent();
        initToolbarButtons();
        initNextPrevActions();
        initPanelMode();
        attachListeners();
        refreshComponents();
        refreshComboTask = Git.getInstance().getRequestProcessor().create(new RefreshComboTask());
        refreshSelectionCombos();
    }

    public MultiDiffPanelController (GitFileNode[] files, Mode mode) {
        this(null, Revision.HEAD, Revision.LOCAL, true);
        for (JComponent c : new JComponent[] { panel.tgbHeadVsIndex, panel.tgbHeadVsWorking, panel.tgbIndexVsWorking }) {
            c.setVisible(false);
        }
        this.popupAllowed = false;
        panel.treeSelectionPanel.setVisible(false);
        initFileComponent();
        initToolbarButtons();
        initNextPrevActions();
        changeFiles(files, mode);
        refreshComponents();
    }

    public MultiDiffPanelController (File file, Revision rev1, Revision rev2, int requestedRightLine) {
        this(null, rev1, rev2, true);
        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        this.requestedRightLine = requestedRightLine;
        this.popupAllowed = false;
        replaceVerticalSplitPane(diffViewPanel);
        initToolbarButtons();
        initNextPrevActions();
        for (JComponent c : new JComponent[] { panel.tgbHeadVsIndex, panel.tgbHeadVsWorking, panel.tgbIndexVsWorking }) {
            c.setVisible(false);
        }
        // mimics refreshSetups()
        Setup s = new Setup(file, rev1, rev2, null);
        GitLocalFileNode fNode = new GitLocalFileNode(Git.getInstance().getRepositoryRoot(file), file);
        EditorCookie cookie = DiffUtils.getEditorCookie(s);
        s.setNode(new DiffLocalNode(fNode, s, cookie, Mode.HEAD_VS_WORKING_TREE));
        Map<File, Setup> localSetups = Collections.singletonMap(file, s);
        setSetups(localSetups, getCookiesFromSetups(localSetups));
        setDiffIndex(s, 0, false);
        dpt = new DiffPrepareTask(setups.values().toArray(new Setup[setups.size()]));
        prepareTask = RP.create(dpt);
        prepareTask.schedule(0);
    }
    
    private MultiDiffPanelController (VCSContext context, Revision revisionLeft, Revision revisionRight, boolean fixedRevisions) {
        this.context = context;
        this.revisionLeft = revisionOriginalLeft = revisionLeft;
        this.revisionRight = revisionOriginalRight = revisionRight;
        this.fixedRevisions = fixedRevisions;
        panel = new MultiDiffPanel();
        diffViewPanel = null;
        if (fixedRevisions) {
            panel.treeSelectionPanel.setVisible(false);
        }
        initSelectionCombos();
    }

    private void initSelectionCombos () {
        if (fixedRevisions) {
            panel.treeSelectionPanel.setVisible(false);
        } else {
            panel.cmbDiffTreeFirst.setMinimumSize(panel.cmbDiffTreeFirst.getMinimumSize());
            panel.cmbDiffTreeSecond.setMinimumSize(panel.cmbDiffTreeSecond.getMinimumSize());
            panel.treeSelectionPanel.setMinimumSize(panel.treeSelectionPanel.getMinimumSize());
            panel.cmbDiffTreeFirst.setRenderer(new RevisionCellRenderer());
            panel.cmbDiffTreeFirst.setModel(new DefaultComboBoxModel(new Revision[] { revisionRight }));
            panel.cmbDiffTreeFirst.addActionListener(this);
            panel.cmbDiffTreeSecond.setRenderer(new RevisionCellRenderer());
            panel.cmbDiffTreeSecond.setModel(new DefaultComboBoxModel(new Revision[] { revisionLeft }));
            panel.cmbDiffTreeSecond.addActionListener(this);
        }
    }

    private DiffFileViewComponent<DiffNode> getActiveFileComponent () {
        return activeComponent;
    }

    private void setActiveComponent (DiffFileViewComponent<DiffNode> fileComponent) {
        if (activeComponent == fileComponent) {
            return;
        }
        activeComponent = fileComponent;
        GitModuleConfig.getDefault().setDiffViewMode(activeComponent == fileListComponent
                ? VIEW_MODE_TABLE : VIEW_MODE_TREE);
        int gg = panel.splitPane.getDividerLocation();
        panel.splitPane.setTopComponent(getActiveFileComponent().getComponent());
        panel.splitPane.setDividerLocation(gg);
    }
    
    private void refreshSelectionCombos () {
        if (!fixedRevisions && GitUtils.getRepositoryRoots(context).size() == 1) {
            panel.cmbDiffTreeFirst.setEnabled(false);
            panel.cmbDiffTreeSecond.setEnabled(false);
            refreshComboTask.schedule(100);
        }
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(panel.controlToolbar, BorderLayout.NORTH);
        panel.add(replacement, BorderLayout.CENTER);
    }

    void setActions (JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N
        comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N

        panel.getActionMap().put("prevInnerView", new AbstractAction("") { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextInnerView();
            }
        });
        panel.getActionMap().put("nextInnerView", new AbstractAction("") { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                onPrevInnerView();
            }
        });
    }

    public JPanel getPanel () {
        return panel;
    }

    PropertyChangeListener list;
    private void attachListeners() {
        panel.tgbHeadVsWorking.addActionListener(this);
        panel.tgbHeadVsIndex.addActionListener(this);
        panel.tgbIndexVsWorking.addActionListener(this);
        panel.btnCommit.addActionListener(this);
        panel.btnRevert.addActionListener(this);
        panel.btnRefresh.addActionListener(this);
        Git.getInstance().getFileStatusCache().addPropertyChangeListener(list = WeakListeners.propertyChange(this, Git.getInstance().getFileStatusCache()));
        GitModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(
                prefList = WeakListeners.create(PreferenceChangeListener.class, this, GitModuleConfig.getDefault().getPreferences()));
    }

    boolean canClose() {
        if (setups.isEmpty()) {
            return true;
        }
        Map<File, SaveCookie> saveCookies = getSaveCookies(true);
        return (saveCookies.isEmpty()) || SaveBeforeClosingDiffConfirmation.allSaved(saveCookies.values().toArray(new SaveCookie[0]));
    }

    // <editor-fold defaultstate="collapsed" desc="save cookie support">
    public Map<File, SaveCookie> getSaveCookies(boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = getEditorCookiesIntern(ommitOpened);
        return getSaveCookies(editorCookiesCopy);
    }

    public EditorCookie[] getEditorCookies(boolean ommitOpened) {
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

    private EditorCookie[] getEditorCookiesIntern(boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = editorCookies.values().toArray(new EditorCookie[editorCookies.values().size()]);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        if (ommitOpened) {
            DiffUtils.cleanThoseWithEditorPaneOpen(editorCookiesCopy);
        }
        return editorCookiesCopy;
    }

    private Map<File, SaveCookie> getSaveCookies(EditorCookie[] editorCookies) {
        Map<File, SaveCookie> proResult = new HashMap<>();
        Set<EditorCookie> editorCookieSet = new HashSet<>(Arrays.asList(editorCookies));
        for (Map.Entry<File, EditorCookie> e : this.editorCookies.entrySet()) {
            if (editorCookieSet.contains(e.getValue())) {
                File baseFile = e.getKey();
                FileObject fileObj = FileUtil.toFileObject(baseFile);
                if (fileObj == null) {
                    continue;
                }
                proResult.put(baseFile, new EditorSaveCookie(e.getValue(), fileObj.getNameExt()));
            }
        }
        return proResult;
    }// </editor-fold>

    UndoRedo getUndoRedo () {
        return delegatingUndoRedo;
    }

    public void componentClosed () {
        setSetups(Collections.<File, Setup>emptyMap(), Collections.<File, EditorCookie>emptyMap());
        prevAction.setEnabled(false);
        nextAction.setEnabled(false);
        cancelBackgroundTasks();
        setups.clear();
        editorCookies.clear();
        
        if (list != null) {
            Git.getInstance().getFileStatusCache().removePropertyChangeListener(list);
        }
        if (prefList != null) {
            GitModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(prefList);
        }
        this.dpt = null;
        DiffViewModeSwitcher.release(this);
        diffViewModeSwitcher = null;
    }

    private void cancelBackgroundTasks() {
        if (multiTextDiffSupport != null) {
            multiTextDiffSupport.cancel();
        }
        if (prepareTask != null) {
            prepareTask.cancel();
        }
        GitProgressSupport supp = statusRefreshSupport;
        if(supp != null) {
            supp.cancel();
        }
        refreshNodesTask.cancel();
        refreshNodesRPTask.cancel();
    }

    void setFocused (boolean focused) {
        if (focused) {
            FileViewComponent comp = getActiveFileComponent();
            if (comp != null) {
                comp.getComponent().requestFocusInWindow();
            }
            synchronized (changes) {
                activated = true;
            }
            changeRPTask.schedule(100);
        } else {
            synchronized (changes) {
                activated = false;
            }
        }
    }

    private void displayDiffView() {
        if (diffViewPanel == null) {
            int gg = panel.splitPane.getDividerLocation();
            panel.splitPane.setBottomComponent(diffView);
            panel.splitPane.setDividerLocation(gg);
        } else {
            diffViewPanel.setComponent(diffView);
        }
    }

    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/diff-next.png", false)) {  //NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.nextButton.toolTipText")); //NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, org.openide.util.ImageUtilities.loadImageIcon("/org/netbeans/modules/git/resources/icons/diff-prev.png", false)) { //NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.prevButton.toolTipText")); //NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onPrevButton();
            }
        };
        panel.nextButton.setAction(nextAction);
        panel.prevButton.setAction(prevAction);
    }

    private int lastDividerLoc;
    private void initFileComponent () {
        fileListComponent = new DiffFileTable(new VCSStatusTableModel<DiffNode>(new DiffNode[0]), this);
        fileListComponent.addPropertyChangeListener(this);
        fileTreeComponent = new DiffFileTreeImpl(this);
        int viewMode = GitModuleConfig.getDefault().getDiffViewMode(VIEW_MODE_TABLE);
        if (viewMode == VIEW_MODE_TREE) {
            panel.treeButton.setSelected(true);
            setActiveComponent(fileTreeComponent);
        } else {
            panel.listButton.setSelected(true);
            setActiveComponent(fileListComponent);
        }
        panel.splitPane.setBottomComponent(getInfoPanelLoading());
        panel.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded (AncestorEvent event) {
                JComponent parent = (JComponent) panel.getParent();
                parent.getActionMap().put("jumpNext", nextAction); //NOI18N
                parent.getActionMap().put("jumpPrev", prevAction); //NOI18N
                if (lastDividerLoc != 0) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            panel.splitPane.setDividerLocation(lastDividerLoc);
                        }
                    });
                }
            }

            @Override
            public void ancestorRemoved (AncestorEvent event) {
                if (dividerSet) {
                    lastDividerLoc = panel.splitPane.getDividerLocation();
                }
            }

            @Override
            public void ancestorMoved (AncestorEvent event) {
            }
        });
    }

    private void initToolbarButtons () {
        if (context != null) {
            panel.btnCommit.setEnabled(false);
            panel.btnRevert.setEnabled(false);
        } else {
            panel.btnCommit.setVisible(false);
            panel.btnRevert.setVisible(false);
            panel.btnRefresh.setVisible(false);
        }
        if (showingFileComponent()) {
            panel.listButton.addActionListener(this);
            panel.treeButton.addActionListener(this);
        } else {
            panel.listButton.setVisible(false);
            panel.treeButton.setVisible(false);
        }
    }
    
    private JComponent getInfoPanelLoading () {
        if (infoPanelLoadingFromRepo == null) {
            infoPanelLoadingFromRepo = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
        }
        return infoPanelLoadingFromRepo;
    }

    private void refreshComponents () {
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

    private void onPrevInnerView() {
        if (panel.tgbHeadVsWorking.isSelected()) {
            panel.tgbHeadVsIndex.setSelected(true);
        } else if (panel.tgbHeadVsIndex.isSelected()) {
            panel.tgbIndexVsWorking.setSelected(true);
        } else {
            panel.tgbHeadVsWorking.setSelected(true);
        }
        onDisplayedStatusChanged();
    }

    private void onNextInnerView() {
        if (panel.tgbHeadVsWorking.isSelected()) {
            panel.tgbIndexVsWorking.setSelected(true);
        } else if (panel.tgbIndexVsWorking.isSelected()) {
            panel.tgbHeadVsIndex.setSelected(true);
        } else {
            panel.tgbHeadVsWorking.setSelected(true);
        }
        onDisplayedStatusChanged();
    }

    private void onDisplayedStatusChanged () {
        if (panel.tgbHeadVsWorking.isSelected()) {
            mode = Mode.HEAD_VS_WORKING_TREE;
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING);
            if (context != null) GitModuleConfig.getDefault().setLastUsedModificationContext(mode);
        } else if (panel.tgbHeadVsIndex.isSelected()) {
            mode = Mode.HEAD_VS_INDEX;
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX);
            if (context != null) GitModuleConfig.getDefault().setLastUsedModificationContext(mode);
        } else {
            mode = Mode.INDEX_VS_WORKING_TREE;
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_INDEX_VS_WORKING);
            if (context != null) GitModuleConfig.getDefault().setLastUsedModificationContext(mode);
        }
    }

    private void setDisplayStatuses (EnumSet<Status> displayStatuses) {
        this.displayStatuses = displayStatuses;
        if (context != null) {
            refreshNodes();
        }
    }

    private void setSetups (Map<File, Setup> setups, Map<File, EditorCookie> editorCookies) {
        for (Map.Entry<File, Setup> e : this.setups.entrySet()) {
            Setup setup = e.getValue();
            if (setup != null) {
                setup.getFirstSource().close();
                setup.getSecondSource().close();
            }
        }
        this.setups.clear();
        this.setups.putAll(setups);
        this.editorCookies.clear();
        this.editorCookies.putAll(editorCookies);
    }

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

    @NbBundle.Messages({
        "MSG_DiffPanel.multiTextualDiff.preparing=Preparing textual diff"
    })
    private void setDiffIndex (Setup selectedSetup, int location, boolean restartPrepareTask) {
        currentSetup = selectedSetup;
        currentSetupDiffLengthChanged = -1;

        if (currentSetup != null) {
            if (multiTextDiffSupport != null) {
                multiTextDiffSupport.cancel();
            }
            if (restartPrepareTask) {
                if (dpt != null) {
                    dpt.cancel();
                }
                startPrepareTask(selectedSetup);
            }
            DiffController view = currentSetup.getView();

            TopComponent tc = (TopComponent) panel.getClientProperty(TopComponent.class);
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
                if (location >= 0 && location < view.getDifferenceCount()) {
                    view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, location);
                }
            } else {
                diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
                displayDiffView();
            }
        } else {
            DiffFileViewComponent<DiffNode> comp = getActiveFileComponent();
            final JPanel p = new NoContentPanel(comp == null || comp.getSelectedFiles().length == 0
                    ? NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoFileSelected") //NOI18N
                    : NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_MoreFilesSelected")); //NOI18N
            diffView = p;
            p.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked (MouseEvent e) {
                    final Map.Entry<File, File[]> actionRoots = getSelectedActionRoots();
                    if (p == diffView) {
                        if (actionRoots == null) {
                            diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoDiff")); //NOI18N
                            displayDiffView();
                            return;
                        }
                        GitProgressSupport supp = new GitProgressSupport() {

                            @Override
                            protected void perform () {
                                try {
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    exportDiff(bos);
                                    if (p == diffView && !isCanceled()) {
                                        final JEditorPane editorPane = new JEditorPane();
                                        final EditorKit editorKit = CloneableEditorSupport.getEditorKit(TEXT_DIFF);
                                        final Document doc = prepareDoc(bos.toString(findEncoding()), editorKit);
                                        bos.reset();
                                        EventQueue.invokeLater(new Runnable() {

                                            @Override
                                            public void run () {
                                                if (p == diffView) {
                                                    editorPane.setEditorKit(editorKit);
                                                    editorPane.setDocument(doc);
                                                    editorPane.setEditable(false);
                                                    JPanel searchContainer = new JPanel();
                                                    searchContainer.setLayout(new BoxLayout(searchContainer, BoxLayout.Y_AXIS));
                                                    JPanel view = new JPanel(new BorderLayout());
                                                    view.add(searchContainer, BorderLayout.PAGE_END);
                                                    view.add(new JScrollPane(editorPane), BorderLayout.CENTER);
                                                    editorPane.putClientProperty(PROP_SEARCH_CONTAINER, searchContainer);
                                                    diffView = view;
                                                    displayDiffView();
                                                }
                                            }
                                        });
                                    }
                                } catch (GitException | BadLocationException | UnsupportedEncodingException ex) {
                                    GitClientExceptionHandler.notifyException(ex, true);
                                } finally {
                                    EventQueue.invokeLater(new Runnable() {

                                        @Override
                                        public void run () {
                                            multiTextDiffSupport = null;
                                        }
                                        
                                    });
                                }
                            }
                            
                            private String findEncoding () {
                                for (File f : actionRoots.getValue()) {
                                    FileObject fo = FileUtil.toFileObject(f);
                                    if (fo != null && fo.isValid() && fo.isData()) {
                                        return FileEncodingQuery.getEncoding(fo).name();
                                    }
                                }
                                return "UTF-8"; //NOI18N
                            }

                            private Document prepareDoc (String content, EditorKit editorKit) throws BadLocationException {
                                Document doc = editorKit.createDefaultDocument();
                                doc.putProperty("mimeType", TEXT_DIFF); //NOI18N
                                doc.remove(0, doc.getLength());
                                doc.insertString(0, content, null);
                                return doc;
                            }

                            private void exportDiff (ByteArrayOutputStream bos) throws GitException {
                                GitClient client = getClient();
                                String revBase, revOther;
                                if (isLocal()) {
                                    if (mode == Mode.INDEX_VS_WORKING_TREE) {
                                        revBase = org.netbeans.libs.git.GitClient.INDEX;
                                        revOther = org.netbeans.libs.git.GitClient.WORKING_TREE;
                                    } else if (mode == Mode.HEAD_VS_INDEX) {
                                        revBase = revisionLeft.getCommitId();
                                        revOther = org.netbeans.libs.git.GitClient.INDEX;
                                    } else {
                                        revBase = revisionLeft.getCommitId();
                                        revOther = org.netbeans.libs.git.GitClient.WORKING_TREE;
                                    }
                                } else {
                                    revBase = revisionLeft.getCommitId();
                                    revOther = revisionRight.getCommitId();
                                }
                                client.exportDiff(actionRoots.getValue(), revBase, revOther, bos, getProgressMonitor());
                            }
                        };
                        multiTextDiffSupport = supp;
                        supp.start(Git.getInstance().getRequestProcessor(actionRoots.getKey()),
                                actionRoots.getKey(), Bundle.MSG_DiffPanel_multiTextualDiff_preparing());
                    }
                }
                
            });
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

    private Map.Entry<File, File[]> getSelectedActionRoots () {
        VCSContext ctx = context;
        DiffFileViewComponent<DiffNode> comp = getActiveFileComponent();
        File[] selectedFiles = new File[0];
        if (comp != null) {
            selectedFiles = comp.getSelectedFiles();
        }
        if (selectedFiles.length == 0) {
            selectedFiles = setups.keySet().toArray(new File[setups.keySet().size()]);
        }
        ctx = GitUtils.getContextForFiles(filterExcluded(selectedFiles));
        return GitUtils.getActionRoots(ctx);
    }
    
    private File[] filterExcluded (File[] files) {
        List<File> filtered = new ArrayList<>(files.length);
        for (File f : files) {
            Setup s = setups.get(f);
            if (s != null && (!isLocal()
                    || s.getNode().getFileNode().getCommitOptions() != VCSCommitOptions.EXCLUDE)) {
                filtered.add(f);
            }
        }
        return filtered.toArray(new File[0]);
    }
    
    private boolean showingFileComponent() {
        return getActiveFileComponent() != null;
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        final Object source = e.getSource();
        if (source == panel.tgbHeadVsIndex || e.getSource() == panel.tgbHeadVsWorking
                || source == panel.tgbIndexVsWorking) {
            onDisplayedStatusChanged();
        } else if (source == panel.cmbDiffTreeSecond) {
            Revision oldSelection = revisionLeft;
            Revision newSelection = getSelectedRevision(panel.cmbDiffTreeSecond);
            if (newSelection != null) {
                revisionLeft = newSelection;
            }
            boolean refresh = !oldSelection.getCommitId().equals(revisionLeft.getCommitId());
            if (refresh) {
                refreshNodes();
            }
        } else if (source == panel.cmbDiffTreeFirst) {
            Revision oldSelection = revisionRight;
            Revision newSelection = getSelectedRevision(panel.cmbDiffTreeFirst);
            if (newSelection != null) {
                revisionRight = newSelection;
            }
            boolean refresh = !oldSelection.getCommitId().equals(revisionRight.getCommitId());
            if (refresh) {
                refreshNodes();
            }
        } else if (source == panel.listButton) {
            setActiveComponent(fileListComponent);
            setActiveDiff();
        } else if (source == panel.treeButton) {
            setActiveComponent(fileTreeComponent);
            setActiveDiff();
        } else {
            Utils.postParallel(new Runnable() {
                @Override
                public void run() {
                    if (source == panel.btnRevert) {
                        SystemAction.get(RevertChangesAction.class).performAction(context);
                    } else if (source == panel.btnCommit) {
                        SystemAction.get(CommitAction.GitViewCommitAction.class).performAction(context);
                    } else if (source == panel.btnRefresh) {
                        if (isLocal()) {
                            statusRefreshSupport = SystemAction.get(StatusAction.class).scanStatus(context);
                            if (statusRefreshSupport != null) {
                                statusRefreshSupport.getTask().waitFinished();
                                if (!statusRefreshSupport.isCanceled()) {
                                    refreshNodes();
                                }
                            }
                        } else {
                            refreshNodes();
                        }
                    }
                }
            }, 0);
        }
    }

    private void setActiveDiff () {
        DiffNode selectedNode = activeComponent.getSelectedNode();
        setDiffIndex(selectedNode == null ? null : selectedNode.getSetup(), 0, false);
    }

    @NbBundle.Messages({
        "CTL_MultiDiffPanelController.popup.initializing=Initializing..."
    })
    JPopupMenu getPopupFor (final Node[] selectedNodes, File[] selectedFiles) {
        if (!popupAllowed) {
            return null;
        }
        final JPopupMenu menu = new JPopupMenu();
        final int popupIndex = ++popupViewIndex;
        JMenuItem item = menu.add(new OpenInEditorAction(selectedFiles));
        Mnemonics.setLocalizedText(item, item.getText());
        if (isLocal()) {
            if (revisionLeft == Revision.HEAD) {
                menu.addSeparator();
                final JMenuItem dummyItem = menu.add(Bundle.CTL_MultiDiffPanelController_popup_initializing());
                dummyItem.setEnabled(false);
                Git.getInstance().getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run () {
                        if (popupIndex != popupViewIndex) {
                            return;
                        }
                        Lookup lkp = Lookups.fixed((Object[]) selectedNodes);
                        final List<Action> actions = new ArrayList<Action>();
                        actions.add(SystemActionBridge.createAction(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "LBL_AddAction.popupName"), lkp)); //NOI18N
                        if (popupIndex != popupViewIndex) {
                            return;
                        }
                        actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"), lkp)); //NOI18N
                        SystemActionBridge efca = SystemActionBridge.createAction(SystemAction.get(ExcludeFromCommitAction.class), NbBundle.getMessage(ExcludeFromCommitAction.class, "LBL_ExcludeFromCommitAction_PopupName"), lkp);
                        SystemActionBridge iica = SystemActionBridge.createAction(SystemAction.get(IncludeInCommitAction.class), NbBundle.getMessage(IncludeInCommitAction.class, "LBL_IncludeInCommitAction_PopupName"), lkp);
                        if (efca.isEnabled() || iica.isEnabled()) {
                            if (efca.isEnabled()) {
                                actions.add(efca);
                            } else if (iica.isEnabled()) {
                                actions.add(iica);
                            }
                        }
                        SystemActionBridge ia = SystemActionBridge.createAction(SystemAction.get(IgnoreAction.class),
                                NbBundle.getMessage(IgnoreAction.class, "LBL_IgnoreAction_PopupName"), lkp);
                        if (ia.isEnabled()) {
                            actions.add(ia);
                        }
                        actions.add(SystemActionBridge.createAction(SystemAction.get(RevertChangesAction.class), NbBundle.getMessage(CheckoutPathsAction.class, "LBL_RevertChangesAction_PopupName"), lkp)); //NOI18N
                        actions.add(new AbstractAction(NbBundle.getMessage(ExportUncommittedChangesAction.class, "LBL_ExportUncommittedChangesAction_PopupName")) { //NOI18N
                            @Override
                            public void actionPerformed (ActionEvent e) {
                                SystemAction.get(ExportUncommittedChangesAction.class).exportDiff(selectedNodes, getDiffMode());
                            }
                        });
                        actions.add(SystemActionBridge.createAction(SystemAction.get(CheckoutPathsAction.class), NbBundle.getMessage(CheckoutPathsAction.class, "LBL_CheckoutPathsAction_PopupName"), lkp)); //NOI18N

                        ResolveConflictsAction a = SystemAction.get(ResolveConflictsAction.class);
                        if (a.isEnabled()) {
                            actions.add(null);
                            actions.add(SystemActionBridge.createAction(a, NbBundle.getMessage(ResolveConflictsAction.class, "LBL_ResolveConflictsAction_PopupName"), lkp)); //NOI18N
                        }
                        if (popupIndex == popupViewIndex) {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run () {
                                    if (popupIndex == popupViewIndex && menu.isShowing()) {
                                        menu.remove(dummyItem);
                                        for (Action a : actions) {
                                            if (a == null) {
                                                menu.addSeparator();
                                            } else {
                                                JMenuItem item = menu.add(a);
                                                Mnemonics.setLocalizedText(item, item.getText());
                                            }
                                        }
                                        menu.pack();
                                        menu.repaint();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }
        return menu;
    }

    private DiffMode getDiffMode () {
        DiffMode diffMode = DiffMode.HEAD_VS_WORKINGTREE;
        if (mode == Mode.HEAD_VS_INDEX) {
            diffMode = DiffMode.HEAD_VS_INDEX;
        } else if (mode == Mode.INDEX_VS_WORKING_TREE) {
            diffMode = DiffMode.INDEX_VS_WORKINGTREE;
        }
        return diffMode;
    }
    
    private Revision getSelectedRevision (JComboBox cmbDiffTree) {
        Object selectedItem = cmbDiffTree.getSelectedItem();
        Revision selection = null;
        if (selectedItem instanceof Revision) {
            selection = (Revision) selectedItem;
        } else if (selectedItem == REVISION_SELECT) {
            RevisionPicker picker = new RevisionPicker(GitUtils.getRootFile(context), new File[0]);
            if (picker.open()) {
                Revision selectedRevision = picker.getRevision();
                addToModel(selectedRevision, cmbDiffTree);
            }
        }
        return selection;
    }

    private void addToModel (final Revision newItem, final JComboBox cmbDiffTree) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) cmbDiffTree.getModel();
        for (int i = 0; i < model.getSize(); ++i) {
            final Object item = model.getElementAt(i);
            if (item instanceof Revision && ((Revision) item).getCommitId().equals(newItem.getCommitId())
                    && ((Revision) item).getRevision().equals(newItem.getRevision())) {
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

    private void applyChange (FileStatusCache.ChangedEvent event) {
        if (context != null) {
            LOG.log(Level.FINE, "Planning refresh for {0}", event.getFile());
            boolean start = true;
            synchronized (changes) {
                changes.put(event.getFile(), event);
                start = activated;
            }
            if (start) {
                changeRPTask.schedule(1000);
            }
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (FileStatusCache.PROP_FILE_STATUS_CHANGED.equals(evt.getPropertyName())) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) evt.getNewValue();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "File status for file {0} changed from {1} to {2}", new Object[] { 
                    changedEvent.getFile(), 
                    changedEvent.getOldInfo(),
                    changedEvent.getNewInfo() } );
            }
            if (revisionLeft == Revision.HEAD // remove when we're able to refresh single file changes for Local vs. any revision 
                    && revisionRight == Revision.LOCAL && affectsView(changedEvent)) {
                applyChange(changedEvent);
            }
        } else if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            // something has changed
            Setup setup = currentSetup;
            if (setup != null && setup.getView() != null) {
                final DiffController view = setup.getView();
                if (setup.getView().getDifferenceCount() < currentSetupDiffLengthChanged) {
                    currentSetupDiffLengthChanged = -1;
                } else if (currentSetupDiffLengthChanged != -1) {
                    currentSetupDiffLengthChanged = setup.getView().getDifferenceCount();
                }
                if (view.getDifferenceCount() > 0 && requestedRightLine != -1) {
                    final int leftLine = requestedLeftLine;
                    final int rightLine = requestedRightLine;
                    requestedRightLine = requestedLeftLine = -1;
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
        } else if (VCSStatusTable.PROP_SELECTED_FILES.equals(evt.getPropertyName())
                && evt.getSource() == getActiveFileComponent()) {
            filesSelected((File[]) evt.getNewValue());
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(GitModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            panel.repaint();
        }
    }

    public final void selectFile (File file) {
        Setup setup = setups.get(file);
        FileViewComponent<DiffNode> comp = getActiveFileComponent();
        if (setup != null && comp != null) {
            comp.setSelectedNode(setup.getNode());
        }
    }

    public final void changeFiles (GitFileNode[] files, Mode mode) {
        lastDividerLoc = 0;
        dividerSet = false;
        this.mode = mode;
        final List<DiffNode> nodes = new ArrayList<>(files.length);
        final Map<File, Setup> localSetups = new HashMap<>(files.length);
        Git git = Git.getInstance();
        for (GitFileNode<FileInformation> fNode : files) {
            File root = git.getRepositoryRoot(fNode.getFile());
            if (root != null) {
                Setup setup = new Setup(fNode, mode, Revision.HEAD);
                DiffNode diffNode = new DiffNode.DiffImmutableNode(fNode, setup, DiffUtils.getEditorCookie(setup));
                nodes.add(diffNode);
                setup.setNode(diffNode);
                localSetups.put(fNode.getFile(), setup);
            }
        }
        setupsChanged(localSetups, nodes);
    }

    private boolean affectsView (FileStatusCache.ChangedEvent changedEvent) {
        File file = changedEvent.getFile();
        FileInformation oldInfo = changedEvent.getOldInfo();
        FileInformation newInfo = changedEvent.getNewInfo();
        if (oldInfo == null) {
            if (!newInfo.containsStatus(displayStatuses)) return false;
        } else {
            if (!oldInfo.containsStatus(displayStatuses) && !newInfo.containsStatus(displayStatuses)) return false;
        }
        return context == null ? false : GitUtils.contains(context.getRootFiles(), file);
    }

    private void initPanelMode () {
        mode = GitModuleConfig.getDefault().getLastUsedModificationContext();
        panel.tgbHeadVsWorking.setSelected(true);
        switch (mode) {
            case HEAD_VS_WORKING_TREE:
                panel.tgbHeadVsWorking.setSelected(true);
                break;
            case HEAD_VS_INDEX:
                panel.tgbHeadVsIndex.setSelected(true);
                break;
            case INDEX_VS_WORKING_TREE:
                panel.tgbIndexVsWorking.setSelected(true);
                break;
        }
        onDisplayedStatusChanged();
    }

    private void updateSplitLocation () {
        JComponent parent = (JComponent) panel.getParent();
        Dimension dim = parent == null ? new Dimension() : parent.getSize();
        if (dim.width <= 0 || dim.height <= 0) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateSplitLocation();
                }
            });
            return;
        }
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
        if (dividerSet && panel.splitPane.getDividerLocation() <= optimalLocation) return;
        panel.splitPane.setDividerLocation(optimalLocation);
        dividerSet = true;
    }

    void filesSelected (File... files) {
        if (fileComponentSetSelectedIndexContext) return;
        Setup s = files.length == 1 ? setups.get(files[0]) : null;
        setDiffIndex(s, 0, true);
    }

    // <editor-fold defaultstate="collapsed" desc="refreshing tasks">
    private void refreshNodes () {
        if (context != null) {
            synchronized (changes) {
                changes.clear();
            }
            changeTask.cancel();
            changeRPTask.cancel();
            refreshNodesTask.cancel();
            refreshNodesRPTask.cancel();
            panel.btnCommit.setEnabled(false);
            panel.btnRevert.setEnabled(false);
            panel.btnRefresh.setEnabled(false);
            boolean enabledToggles = revisionRight == Revision.LOCAL;
            panel.tgbHeadVsIndex.setEnabled(enabledToggles);
            panel.tgbHeadVsWorking.setEnabled(enabledToggles);
            panel.tgbIndexVsWorking.setEnabled(enabledToggles);
            refreshNodesRPTask.schedule(0);
        }
    }

    private void startPrepareTask (Setup activeSetup) {
        if (setups.size() > 0) {
            dpt = new DiffPrepareTask(setups.values().toArray(new Setup[setups.size()]));
            dpt.setSelectedSetup(activeSetup);
            prepareTask = RP.create(dpt);
            prepareTask.schedule(0);
        }
    }

    private static Map<File, EditorCookie> getCookiesFromSetups(Map<File, Setup> localSetups) {
        Setup[] retSetups = localSetups.values().toArray(new Setup[localSetups.values().size()]);
        EditorCookie[] cookies = DiffUtils.setupsToEditorCookies(retSetups);
        Map<File, EditorCookie> map = new HashMap<File, EditorCookie>();
        for (int i = 0; i < cookies.length; ++i) {
            if (cookies[i] != null) {
                map.put(retSetups[i].getBaseFile(), cookies[i]);
            }
        }
        return map;
    }

    @NbBundle.Messages({
        "MSG_No_Changes_Revisions=<No Changes Between Revisions>",
        "MSG_No_Changes_HeadWorking=<No Head/Working Tree Changes>",
        "MSG_No_Changes_HeadIndex=<No Head/Index Tree Changes>",
        "MSG_No_Changes_IndexWorking=<No Index/Working Tree Changes>",
        "# {0} - revision", "MSG_No_Changes_RevisionIndex=<No Changes Between {0} and Index>",
        "# {0} - revision", "MSG_No_Changes_RevisionWorking=<No Changes Between {0} and Working Tree>"
    })
    private void updateView () {
        FileViewComponent<DiffNode> activeFileComponent = getActiveFileComponent();
        if (setups.isEmpty()) {
            String noContentLabel = ""; //NOI18N
            if (isLocal()) {
                switch (mode) {
                    case HEAD_VS_WORKING_TREE:
                        noContentLabel = revisionLeft == Revision.HEAD
                                ? Bundle.MSG_No_Changes_HeadWorking()
                                : Bundle.MSG_No_Changes_RevisionWorking(revisionLeft.getRevision());
                        break;
                    case HEAD_VS_INDEX:
                        noContentLabel = revisionLeft == Revision.HEAD
                                ? Bundle.MSG_No_Changes_HeadIndex()
                                : Bundle.MSG_No_Changes_RevisionIndex(revisionLeft.getRevision());
                        break;
                    case INDEX_VS_WORKING_TREE:
                        noContentLabel = Bundle.MSG_No_Changes_IndexWorking();
                        break;
                }
            } else {
                noContentLabel = Bundle.MSG_No_Changes_Revisions();
            }
            fileListComponent.getComponent().setEnabled(false);
            fileListComponent.getComponent().setPreferredSize(null);
            fileTreeComponent.getComponent().setEnabled(false);
            fileTreeComponent.getComponent().setPreferredSize(null);
            Dimension dim = activeFileComponent.getComponent().getPreferredSize();
            activeFileComponent.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
            diffView = new NoContentPanel(noContentLabel);
            displayDiffView();
            nextAction.setEnabled(false);
            prevAction.setEnabled(false);
            panel.btnCommit.setEnabled(false);
            panel.btnRevert.setEnabled(false);
        } else {
            fileListComponent.getComponent().setEnabled(true);
            fileListComponent.getComponent().setPreferredSize(null);
            fileTreeComponent.getComponent().setEnabled(true);
            fileTreeComponent.getComponent().setPreferredSize(null);
            Dimension dim = activeFileComponent.getComponent().getPreferredSize();
            activeFileComponent.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
            DiffNode node = activeFileComponent.getNodeAtPosition(0);
            Setup toSelect = node == null ? null : node.getSetup();
            setDiffIndex(toSelect, 0, true);
            boolean buttonsEnabled = revisionRight == Revision.LOCAL && revisionLeft == Revision.HEAD;
            panel.btnCommit.setEnabled(buttonsEnabled);
            panel.btnRevert.setEnabled(buttonsEnabled);
        }
        panel.btnRefresh.setEnabled(true);
        if (panel.splitPane != null) {
            updateSplitLocation();
        }
        panel.revalidate();
        panel.repaint();
    }

    private EditorCookie[] sort (Map<File, EditorCookie> cookies, DiffNode[] nodes) {
        List<EditorCookie> sorted = new ArrayList<>(cookies.size());
        for (DiffNode n : nodes) {
            sorted.add(cookies.get(n.getFile()));
        }
        return sorted.toArray(new EditorCookie[0]);
    }

    private void setupsChanged (final Map<File, Setup> newSetups, final List<DiffNode> nodes) {
        final Map<File, EditorCookie> cookies = getCookiesFromSetups(newSetups);
        final DiffNode[] nodeArray = nodes.toArray(new DiffNode[0]);
        final Object modelDataList = fileListComponent.prepareModel(nodeArray);
        final Object modelDataTree = fileTreeComponent.prepareModel(nodeArray);
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                dividerSet = false;
                setSetups(newSetups, cookies);
                fileListComponent.setModel(nodeArray, new HashMap<File, EditorCookie>(cookies), modelDataList);
                fileTreeComponent.setModel(nodeArray, sort(cookies, nodeArray), modelDataTree);
                updateView();
            }
        });
    }

    private final class RefreshNodesTask implements Runnable, Cancellable {
        private volatile boolean canceled;

        @Override
        public void run() {
            canceled = false;
            final List<DiffNode> nodes = new LinkedList<DiffNode>();
            final Map<File, Setup> localSetups;
            if (isLocal()) {
                if (revisionLeft == Revision.HEAD || mode == Mode.INDEX_VS_WORKING_TREE) {
                    localSetups = getLocalToBaseSetups(nodes);
                } else {
                    localSetups = getLocalToRevisionSetups(nodes);
                }
            } else {
                localSetups = getRevisionToRevisionSetups(nodes);
            }
            if (canceled) {
                return;
            }
            setupsChanged(localSetups, nodes);
        }

        @Override
        public boolean cancel () {
            this.canceled = true;
            return true;
        }

        private Map<File, Setup> getLocalToBaseSetups (final List<DiffNode> nodes) {
            Git git = Git.getInstance();
            File[] interestingFiles = git.getFileStatusCache().listFiles(context.getRootFiles(), displayStatuses);
            final Map<File, Setup> localSetups = new HashMap<File, Setup>(interestingFiles.length);
            for (File f : interestingFiles) {
                if (canceled) {
                    break;
                }
                File root = git.getRepositoryRoot(f);
                if (root != null) {
                    GitUtils.logRemoteRepositoryAccess(root);
                    GitLocalFileNode fNode = new GitLocalFileNode(root, f);
                    Setup setup = new Setup(fNode, mode, Revision.HEAD);
                    DiffNode diffNode = new DiffLocalNode(fNode, setup, DiffUtils.getEditorCookie(setup), mode);
                    nodes.add(diffNode);
                    setup.setNode(diffNode);
                    localSetups.put(f, setup);
                }
            }
            return localSetups;
        }

        private Map<File, Setup> getLocalToRevisionSetups (final List<DiffNode> nodes) {
            Git git = Git.getInstance();
            File repository = GitUtils.getRootFile(context);
            GitClient client = null;
            try {
                GitUtils.logRemoteRepositoryAccess(repository);
                client = git.getClient(repository);
                Map<File, GitStatus> statuses = client.getStatus(context.getRootFiles().toArray(new File[context.getRootFiles().size()]),
                        revisionLeft.getCommitId(), GitUtils.NULL_PROGRESS_MONITOR);
                statuses.keySet().retainAll(Utils.flattenFiles(context.getRootFiles().toArray(
                        new File[context.getRootFiles().size()]), statuses.keySet()));
                final Map<File, Setup> localSetups = new HashMap<File, Setup>(statuses.size());
                for (Map.Entry<File, GitStatus> e : statuses.entrySet()) {
                    if (canceled) {
                        break;
                    }
                    File f = e.getKey();
                    GitStatus status = e.getValue();
                    if (status.isFolder()) {
                        continue;
                    }
                    final FileInformation fi = new FileInformation(status);
                    if (!fi.containsStatus(displayStatuses)) {
                        continue;
                    }
                    GitLocalFileNode fNode = new GitLocalFileNode(repository, f) {
                        @Override
                        public FileInformation getInformation () {
                            return fi;
                        }
                    };
                    Setup setup = new Setup(fNode, mode, revisionLeft);
                    DiffNode diffNode = new DiffLocalNode(fNode, setup, DiffUtils.getEditorCookie(setup), mode);
                    nodes.add(diffNode);
                    setup.setNode(diffNode);
                    localSetups.put(f, setup);
                }
                return localSetups;
            } catch (GitException ex) {
                LOG.log(Level.INFO, null, ex);
            } finally {
                if (client != null) {
                    client.release();
                }
            }
            return Collections.<File, Setup>emptyMap();
        }

        private Map<File, Setup> getRevisionToRevisionSetups (final List<DiffNode> nodes) {
            Git git = Git.getInstance();
            File repository = GitUtils.getRootFile(context);
            GitClient client = null;
            try {
                GitUtils.logRemoteRepositoryAccess(repository);
                client = git.getClient(repository);
                Map<File, GitRevisionInfo.GitFileInfo> statuses = client.getStatus(context.getRootFiles().toArray(new File[context.getRootFiles().size()]),
                        revisionLeft.getCommitId(), revisionRight.getCommitId(), GitUtils.NULL_PROGRESS_MONITOR);
                final Map<File, Setup> historySetups = new HashMap<File, Setup>();
                for (Map.Entry<File, GitRevisionInfo.GitFileInfo> e : statuses.entrySet()) {
                    if (canceled) {
                        break;
                    }
                    File f = e.getKey();
                    GitRevisionInfo.GitFileInfo info = e.getValue();
                    GitFileNode.HistoryFileInformation fi = new GitFileNode.HistoryFileInformation(info);
                    Setup setup = new Setup(f, revisionLeft, revisionRight, fi);
                    DiffNode historyNode = new DiffHistoryNode(new GitHistoryFileNode(repository, f, fi), setup);
                    setup.setNode(historyNode);
                    nodes.add(historyNode);
                    historySetups.put(f, setup);
                }
                return historySetups;
            } catch (GitException ex) {
                LOG.log(Level.INFO, null, ex);
            } finally {
                if (client != null) {
                    client.release();
                }
            }
            return Collections.<File, Setup>emptyMap();
        }
    }

    private boolean isLocal () {
        return revisionRight == Revision.LOCAL;
    }

    private final Map<File, FileStatusCache.ChangedEvent> changes = new HashMap<File, FileStatusCache.ChangedEvent>();
    /**
     * Eliminates unnecessary cache.listFiles call as well as the whole node creation process ()
     */
    private final class ApplyChangesTask implements Runnable, Cancellable {
        
        private volatile boolean canceled;

        @Override
        public void run() {
            canceled = false;
            final Set<FileStatusCache.ChangedEvent> events;
            synchronized (changes) {
                events = new HashSet<FileStatusCache.ChangedEvent>(changes.values());
                changes.clear();
            }
            // remove irrelevant changes
            for (Iterator<FileStatusCache.ChangedEvent> it = events.iterator(); it.hasNext();) {
                FileStatusCache.ChangedEvent evt = it.next();
                if (!affectsView(evt)) {
                    LOG.log(Level.FINE, "ApplyChanges: file {0} does not affect view", evt.getFile());
                    it.remove();
                }
            }
            if (canceled || events.isEmpty()) {
                return;
            }
            Git git = Git.getInstance();
            Map<File, DiffNode> nodes = Mutex.EVENT.readAccess(new Mutex.Action<Map<File, DiffNode>>() {
                @Override
                public Map<File, DiffNode> run() {
                    return fileListComponent.getNodes();
                }
            });
            // sort changes
            final List<DiffNode> toRemove = new LinkedList<DiffNode>();
            final List<DiffNode> toRefresh = new LinkedList<DiffNode>();
            final List<DiffNode> toAdd = new LinkedList<DiffNode>();
            final Map<File, Setup> localSetups = new HashMap<File, Setup>(nodes.size());
            for (FileStatusCache.ChangedEvent evt : events) {
                if (canceled) {
                    break;
                }
                FileInformation newInfo = evt.getNewInfo();
                DiffNode node = nodes.get(evt.getFile());
                if (newInfo.containsStatus(displayStatuses)) {
                    if (node != null) {
                        if (node instanceof DiffLocalNode) {
                            toRefresh.add(node);
                            Setup setup = new Setup(((DiffLocalNode) node).getFileNode(), mode, Revision.HEAD);
                            setup.setNode(node);
                            localSetups.put(evt.getFile(), setup);
                            LOG.log(Level.FINE, "ApplyChanges: refreshing node {0}", node);
                        } else {
                            toRemove.add(node);
                        }
                    } else {
                        File root = git.getRepositoryRoot(evt.getFile());
                        if (root != null) {
                            GitLocalFileNode fNode = new GitLocalFileNode(root, evt.getFile());
                            Setup setup = new Setup(fNode, mode, Revision.HEAD);
                            DiffNode toAddNode = new DiffLocalNode(fNode, setup, DiffUtils.getEditorCookie(setup), mode);
                            setup.setNode(toAddNode);
                            localSetups.put(evt.getFile(), setup);
                            toAdd.add(toAddNode);
                            LOG.log(Level.FINE, "ApplyChanges: adding node {0}", toAddNode);
                        }
                    }
                } else if (node != null) {
                    toRemove.add(node);
                    LOG.log(Level.FINE, "ApplyChanges: removing node {0}", node);
                }
            }
            if (canceled) {
                return;
            }
            for (DiffNode n : toRemove) {
                nodes.remove(n.getFile());
            }
            for (DiffNode n : toAdd) {
                nodes.put(n.getFile(), n);
            }
            for (DiffNode n : toRefresh) {
                nodes.put(n.getFile(), n);
            }
            final DiffNode[] diffNodes = nodes.values().toArray(new DiffNode[nodes.size()]);
            final Object modelDataTree = fileTreeComponent.prepareModel(diffNodes);

            final Map<File, EditorCookie> cookies = getCookiesFromSetups(localSetups);
            if (canceled) {
                return;
            }
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    for (DiffNode node : toAdd) {
                        setups.put(node.getFile(), localSetups.get(node.getFile()));
                        EditorCookie cookie = cookies.get(node.getFile());
                        if(cookie != null) {
                            editorCookies.put(node.getFile(), cookie);
                        }
                    }
                    for (DiffNode node : toRemove) {
                        setups.remove(node.getFile());
                        editorCookies.remove(node.getFile());
                    }
                    fileListComponent.updateNodes(new HashMap<File, EditorCookie>(MultiDiffPanelController.this.editorCookies), toRemove, toRefresh, toAdd);
                    fileTreeComponent.setModel(diffNodes, sort(editorCookies, diffNodes), modelDataTree);
                    updateView();
                }
            });
        }

        @Override
        public boolean cancel () {
            this.canceled = true;
            return true;
        }
    }
    
    private class RefreshComboTask implements Runnable {

        @Override
        public void run () {
            File repository = GitUtils.getRootFile(context);
            final List<Object> modelRight = new ArrayList<Object>(10);
            final List<Object> modelLeft = new ArrayList<Object>(10);
            modelLeft.add(revisionOriginalLeft);
            if (revisionOriginalLeft != Revision.HEAD) {
                modelLeft.add(Revision.HEAD);
            }
            modelRight.add(revisionOriginalRight);            
            if (revisionOriginalRight != Revision.LOCAL) {
                modelRight.add(Revision.LOCAL);
            }
            if (revisionOriginalRight != Revision.HEAD) {
                modelRight.add(Revision.HEAD);
            }
            modelLeft.add(REVISION_SELECT_SEP);
            modelRight.add(REVISION_SELECT_SEP);
            RepositoryInfo info = RepositoryInfo.getInstance(repository);
            info.refresh();
            boolean added = false;
            for (Map.Entry<String, GitBranch> e : info.getBranches().entrySet()) {
                String branchName = e.getValue().getName();
                if (branchName != GitBranch.NO_BRANCH) {
                    Revision revision = new Revision.BranchReference(e.getValue());
                    modelLeft.add(revision);
                    modelRight.add(revision);
                    added = true;
                }
            }
            if (added) {
                modelLeft.add(REVISION_SELECT_SEP);
                modelRight.add(REVISION_SELECT_SEP);
            }
            modelLeft.add(REVISION_SELECT);
            modelLeft.add(REVISION_SELECT_SEP);
            modelRight.add(REVISION_SELECT);
            modelRight.add(REVISION_SELECT_SEP);
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run () {
                    panel.cmbDiffTreeFirst.setModel(new DefaultComboBoxModel(modelRight.toArray(new Object[0])));
                    panel.cmbDiffTreeSecond.setModel(new DefaultComboBoxModel(modelLeft.toArray(new Object[0])));
                    panel.cmbDiffTreeFirst.setEnabled(true);
                    panel.cmbDiffTreeSecond.setEnabled(true);
                }
            });
        }
    }

    private class DiffPrepareTask implements Runnable, Cancellable {

        private final Setup[] prepareSetups;
        private Setup selectedSetup;
        private volatile boolean canceled;

        public DiffPrepareTask(Setup[] prepareSetups) {
            assert EventQueue.isDispatchThread();
            assert !Arrays.asList(prepareSetups).contains(null);
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
                if (canceled) return;
                if (s.getView() != null) {
                    continue;
                }
                try {
                    s.initSources();  // slow network I/O
                    if (canceled) {
                        return;
                    }
                    StreamSource ss1 = s.getFirstSource();
                    StreamSource ss2 = s.getSecondSource();
                    if (requestedRightLine != -1) {
                        requestedLeftLine = getMatchingLine(ss2, ss1, requestedRightLine);
                    }
                    final DiffController view = DiffController.createEnhanced(ss1, ss2);  // possibly executing slow external diff
                    view.addPropertyChangeListener(MultiDiffPanelController.this);
                    if (canceled) {
                        return;
                    }
                    s.setView(view);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (s.equals(currentSetup)) {
                                setDiffIndex(s, 0, false);
                            }
                        }
                    });
                } catch (IOException e) {
                    if (!GitClientExceptionHandler.isCancelledAction(e)) {
                        LOG.log(Level.INFO, null, e);
                        if (exception == null) {
                            // save only the first exception
                            exception = e;
                        }
                    }
                }
            }
            if (exception != null) {
                // notify user of the failure
                GitClientExceptionHandler.notifyException(exception, true);
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
    }// </editor-fold>
    
    @NbBundle.Messages({
        "MSG_Revision_Select_Tooltip=Select a revision from the picker"
    })
    private static class RevisionCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String tooltip = null;
            if (value instanceof Revision) {
                Revision rev = (Revision) value;
                value = rev.toString(true);
                tooltip = rev.getFullMessage();
                if (tooltip != null) {
                    tooltip = tooltip.replace("\r\n", "\n").replace("\n", "<br>"); //NOI18N
                    StringBuilder sb = new StringBuilder("<html><p>"); //NOI18N
                    sb.append(tooltip).append("</p></html>"); //NOI18N
                    tooltip = sb.toString();
                }
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
}
