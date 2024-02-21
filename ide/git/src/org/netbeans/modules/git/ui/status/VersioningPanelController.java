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

package org.netbeans.modules.git.ui.status;

import org.netbeans.modules.git.GitStatusNode;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.GitFileNode.GitLocalFileNode;
import org.netbeans.modules.git.ui.actions.AddAction;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.commit.DeleteLocalAction;
import org.netbeans.modules.git.ui.commit.ExcludeFromCommitAction;
import org.netbeans.modules.git.ui.commit.IncludeInCommitAction;
import org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction;
import org.netbeans.modules.git.ui.diff.DiffAction;
import org.netbeans.modules.git.ui.ignore.IgnoreAction;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.OpenInEditorAction;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.common.FileViewComponent;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author ondra
 */
class VersioningPanelController implements ActionListener, PropertyChangeListener, PreferenceChangeListener {

    private final VersioningPanel panel;
    private VCSContext context;
    private EnumSet<Status> displayStatuses;
    private final NoContentPanel noContentComponent = new NoContentPanel();
    private static final RequestProcessor RP = new RequestProcessor("GitVersioningWindow", 1, true); //NOI18N
    private final RequestProcessor.Task refreshNodesTask = RP.create(new RefreshNodesTask());
    private final ApplyChangesTask applyChangeTask = new ApplyChangesTask();
    private final RequestProcessor.Task changeTask = RP.create(applyChangeTask);
    static final Logger LOG = Logger.getLogger(VersioningPanelController.class.getName());
    private Mode mode;
    private GitProgressSupport refreshStatusSupport;
    private final ModeKeeper modeKeeper;
    private PreferenceChangeListener list;
    private FileViewComponent<GitStatusNodeImpl> activeComponent;
    private GitStatusTable fileListComponent;
    private FileTreeViewImpl fileTreeComponent;
    private static final int VIEW_MODE_TABLE = 1;
    private static final int VIEW_MODE_TREE = 2;
    private int popupViewIndex;

    VersioningPanelController () {
        this.panel = new VersioningPanel();
        modeKeeper = new ModeKeeper();
        initPanelMode();
        initFileComponent();
        attachListeners();
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

    void focus () {
        noContentComponent.requestFocusInWindow();
        getActiveFileComponent().focus();
    }

    JPanel getPanel () {
        return panel;
    }

    void setContext (VCSContext context) {
        if (context != this.context) {
            this.context = context;
            refreshNodes();
        }
    }

    void cancelRefresh() {

    }

    JPopupMenu getPopupFor (final Node[] nodes) {
        final JPopupMenu menu = new JPopupMenu();
        final int popupIndex = ++popupViewIndex;
        File[] files = toFiles(nodes);
        JMenuItem item;
        item = menu.add(new OpenInEditorAction(files));
        Mnemonics.setLocalizedText(item, item.getText());

        menu.addSeparator();
        final JMenuItem dummyItem = menu.add(Bundle.CTL_GitStatusTable_popup_initializing());
        dummyItem.setEnabled(false);
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run () {
                Lookup lkp = Lookups.fixed((Object[]) nodes);
                boolean displayAdd = false;
                boolean allLocallyNew = true;
                for (Node node : nodes) {
                    GitLocalFileNode statusNode = node.getLookup().lookup(GitLocalFileNode.class);
                    if (statusNode != null) {
                        FileInformation info = statusNode.getInformation();
                        // is there any change between index and WT?
                        if (info.containsStatus(EnumSet.of(Status.NEW_INDEX_WORKING_TREE,
                                Status.IN_CONFLICT,
                                Status.MODIFIED_INDEX_WORKING_TREE))) {
                            displayAdd = true;
                        }
                        if (!info.containsStatus(EnumSet.of(Status.NEW_HEAD_INDEX, Status.NEW_HEAD_WORKING_TREE))) {
                            allLocallyNew = false;
                        }
                    } else {
                        allLocallyNew = false;
                        displayAdd = true;
                    }
                }
                if (popupIndex != popupViewIndex) {
                    return;
                }
                final List<Action> actions = new ArrayList<>();
                actions.add(SystemActionBridge.createAction(SystemAction.get(CommitAction.class), NbBundle.getMessage(CommitAction.class, "LBL_CommitAction.popupName"), lkp)); //NOI18N
                if (popupIndex != popupViewIndex) {
                    return;
                }
                actions.add(new SystemActionBridge(SystemAction.get(DiffAction.class).createContextAwareInstance(lkp), NbBundle.getMessage(DiffAction.class, "LBL_DiffAction_PopupName")) { //NOI18N
                    @Override
                    public void actionPerformed (ActionEvent e) {
                        modeKeeper.storeMode();
                        super.actionPerformed(e);
                    }
                });
                if (displayAdd) {
                    actions.add(SystemActionBridge.createAction(SystemAction.get(AddAction.class), NbBundle.getMessage(AddAction.class, "LBL_AddAction.popupName"), lkp)); //NOI18N
                }

                if (allLocallyNew) {
                    SystemAction systemAction = SystemAction.get(DeleteLocalAction.class);
                    actions.add(SystemActionBridge.createAction(systemAction, NbBundle.getMessage(DeleteLocalAction.class, "CTL_PopupMenuItem_Delete"), lkp)); //NOI18N
                }
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
        return menu;
    }
    
    private void initFileComponent () {
        fileListComponent = new GitStatusTable(this, new VCSStatusTableModel<>(new GitStatusNodeImpl[0]), modeKeeper);
        fileListComponent.addPropertyChangeListener(this);
        fileTreeComponent = new FileTreeViewImpl(this, modeKeeper);
        int viewMode = GitModuleConfig.getDefault().getStatusViewMode(VIEW_MODE_TABLE);
        if (viewMode == VIEW_MODE_TREE) {
            panel.treeButton.setSelected(true);
            setActiveComponent(fileTreeComponent);
        } else {
            panel.listButton.setSelected(true);
            setActiveComponent(fileListComponent);
        }
    }
    
    private FileViewComponent<GitStatusNodeImpl> getActiveFileComponent () {
        return activeComponent;
    }

    private void setActiveComponent (FileViewComponent<GitStatusNodeImpl> fileComponent) {
        if (activeComponent == fileComponent) {
            return;
        }
        activeComponent = fileComponent;
        GitModuleConfig.getDefault().setStatusViewMode(activeComponent == fileListComponent
                ? VIEW_MODE_TABLE : VIEW_MODE_TREE);
        setVersioningComponent(activeComponent.getComponent());
    }

    private void attachListeners() {
        panel.tgbHeadVsWorking.addActionListener(this);
        panel.tgbHeadVsIndex.addActionListener(this);
        panel.tgbIndexVsWorking.addActionListener(this);
        panel.btnCommit.addActionListener(this);
        panel.btnRevert.addActionListener(this);
        panel.btnDiff.addActionListener(this);
        panel.btnRefresh.addActionListener(this);
        panel.listButton.addActionListener(this);
        panel.treeButton.addActionListener(this);
        Git.getInstance().getFileStatusCache().addPropertyChangeListener(this);
        GitModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(
                list = WeakListeners.create(PreferenceChangeListener.class, this, GitModuleConfig.getDefault().getPreferences()));
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
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanelController.class, "MSG_No_Changes_HeadWorking")); // NOI18N
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING);
            modeKeeper.setMode(mode);
        } else if (panel.tgbHeadVsIndex.isSelected()) {
            mode = Mode.HEAD_VS_INDEX;
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanelController.class, "MSG_No_Changes_HeadIndex")); // NOI18N
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX);
            modeKeeper.setMode(mode);
        } else {
            mode = Mode.INDEX_VS_WORKING_TREE;
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanelController.class, "MSG_No_Changes_IndexWorking")); // NOI18N
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_INDEX_VS_WORKING);
            modeKeeper.setMode(mode);
        }
    }

    private void setDisplayStatuses (EnumSet<Status> displayStatuses) {
        this.displayStatuses = displayStatuses;
        refreshNodes();
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        final Object source = e.getSource();
        if (source == panel.tgbHeadVsIndex || source == panel.tgbHeadVsWorking
                || source == panel.tgbIndexVsWorking) {
            onDisplayedStatusChanged();
        } else if (source == panel.btnDiff) {
            modeKeeper.storeMode();
            SystemAction.get(DiffAction.class).diff(context);
        } else if (source == panel.listButton) {
            setActiveComponent(fileListComponent);
            fileListComponent.focus();
        } else if (source == panel.treeButton) {
            setActiveComponent(fileTreeComponent);
            fileTreeComponent.focus();
        } else {
            Utils.postParallel(new Runnable() {
                @Override
                public void run() {
                    if (source == panel.btnRevert) {
                        SystemAction.get(RevertChangesAction.class).performAction(context);
                    } else if (source == panel.btnCommit) {
                        SystemAction.get(CommitAction.GitViewCommitAction.class).performAction(context);
                    } else if (source == panel.btnRefresh) {
                        refreshStatusSupport = SystemAction.get(StatusAction.class).scanStatus(context);
                        if (refreshStatusSupport != null) {
                            refreshStatusSupport.getTask().waitFinished();
                            if (!refreshStatusSupport.isCanceled()) {
                                refreshNodes();
                            }
                        }
                    }
                }
            }, 0);
        }
    }

    private void applyChange (FileStatusCache.ChangedEvent event) {
        if (context != null) {
            synchronized (changes) {
                changes.put(event.getFile(), event);
            }
            changeTask.schedule(1000);
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (FileStatusCache.PROP_FILE_STATUS_CHANGED.equals(evt.getPropertyName())) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) evt.getNewValue();
            if (affectsView((FileStatusCache.ChangedEvent) evt.getNewValue())) {
                applyChange(changedEvent);
            }
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(GitModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            panel.repaint();
        }
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

    private void setVersioningComponent (final JComponent component)  {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                Component [] children = panel.getComponents();
                for (int i = 0; i < children.length; i++) {
                    Component child = children[i];
                    if (child != panel.jPanel2) {
                        if (child == component) {
                            return;
                        } else {
                            panel.remove(child);
                            break;
                        }
                    }
                }
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.FIRST_LINE_START; gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1; gbc.weighty = 1;

                panel.add(component, gbc);
                panel.revalidate();
                panel.repaint();
            }
        });
    }

    private void refreshNodes () {
        if (context != null) {
            refreshNodesTask.cancel();
            refreshNodesTask.schedule(0);
        }
    }

    private File[] toFiles (Node[] nodes) {
        List<File> files = new ArrayList<>(nodes.length);
        for (Node n : nodes) {
            File f = n.getLookup().lookup(File.class);
            if (f != null) {
                files.add(f);
            }
        }
        return files.toArray(new File[0]);
    }

    private class RefreshNodesTask implements Runnable {
        @Override
        public void run() {
            final List<GitStatusNodeImpl> nodes = new LinkedList<>();
            Git git = Git.getInstance();
            File[] interestingFiles = git.getFileStatusCache().listFiles(context.getRootFiles(), displayStatuses);
            for (File f : interestingFiles) {
                File root = git.getRepositoryRoot(f);
                if (root != null) {
                    if (f.equals(root)) {
                        // gitlink???
                        File parentFile = f.getParentFile();
                        File parentRepository = parentFile == null ? null : git.getRepositoryRoot(parentFile);
                        if (parentRepository != null) {
                            root = parentRepository;
                        }
                    }
                    nodes.add(new GitStatusNodeImpl(new GitLocalFileNode(root, f), mode));
                }
            }
            final GitStatusNodeImpl[] nodeArray = nodes.toArray(new GitStatusNodeImpl[0]);
            final Object modelDataList = fileListComponent.prepareModel(nodeArray);
            final Object modelDataTree = fileTreeComponent.prepareModel(nodeArray);
            Mutex.EVENT.readAccess(new Runnable () {
                @Override
                public void run() {
                    fileListComponent.setModel(nodeArray, new EditorCookie[0], modelDataList);
                    fileTreeComponent.setModel(nodeArray, new EditorCookie[0], modelDataTree);
                    if (nodes.isEmpty()) {
                        setVersioningComponent(noContentComponent);
                        noContentComponent.requestFocusInWindow();
                    } else {
                        setVersioningComponent(getActiveFileComponent().getComponent());
                        getActiveFileComponent().focus();
                    }
                }
            });
        }
    }

    private final Map<File, FileStatusCache.ChangedEvent> changes = new HashMap<File, FileStatusCache.ChangedEvent>();
    /**
     * Eliminates unnecessary cache.listFiles call as well as the whole node creation process ()
     */
    private class ApplyChangesTask implements Runnable {

        @Override
        public void run() {
            final Set<FileStatusCache.ChangedEvent> events;
            synchronized (changes) {
                events = new HashSet<FileStatusCache.ChangedEvent>(changes.values());
                changes.clear();
            }
            // remove irrelevant changes
            for (Iterator<FileStatusCache.ChangedEvent> it = events.iterator(); it.hasNext(); ) {
                FileStatusCache.ChangedEvent evt = it.next();
                if (!affectsView(evt)) {
                    it.remove();
                }
            }
            Git git = Git.getInstance();
            final Map<File, GitStatusNodeImpl> nodes = Mutex.EVENT.readAccess(new Mutex.Action<Map<File, GitStatusNodeImpl>>() {
                @Override
                public Map<File, GitStatusNodeImpl> run() {
                    return fileListComponent.getNodes();
                }
            });
            // sort changes
            final List<GitStatusNodeImpl> toRemove = new LinkedList<GitStatusNodeImpl>();
            final List<GitStatusNodeImpl> toRefresh = new LinkedList<GitStatusNodeImpl>();
            final List<GitStatusNodeImpl> toAdd = new LinkedList<GitStatusNodeImpl>();
            for (FileStatusCache.ChangedEvent evt : events) {
                FileInformation newInfo = evt.getNewInfo();
                GitStatusNodeImpl node = nodes.get(evt.getFile());
                if (newInfo.containsStatus(displayStatuses)) {
                    if (node != null) {
                        toRefresh.add(node);
                    } else {
                        File root = git.getRepositoryRoot(evt.getFile());
                        if (root != null) {
                            // gitlink???
                            File parentFile = evt.getFile().getParentFile();
                            File parentRepository = parentFile == null ? null : git.getRepositoryRoot(parentFile);
                            if (parentRepository != null) {
                                root = parentRepository;
                            }
                            toAdd.add(new GitStatusNodeImpl(new GitLocalFileNode(root, evt.getFile()), mode));
                        }
                    }
                } else if (node != null) {
                    toRemove.add(node);
                }
            }

            for (GitStatusNodeImpl n : toRemove) {
                nodes.remove(n.getFile());
            }
            for (GitStatusNodeImpl n : toAdd) {
                nodes.put(n.getFile(), n);
            }
            for (GitStatusNodeImpl n : toRefresh) {
                nodes.put(n.getFile(), n);
            }
            final GitStatusNodeImpl[] statusNodes = nodes.values().toArray(new GitStatusNodeImpl[nodes.size()]);
            final Object modelDataTree = fileTreeComponent.prepareModel(statusNodes);
            Mutex.EVENT.readAccess(new Runnable () {
                @Override
                public void run() {
                    fileListComponent.updateNodes(toRemove, toRefresh, toAdd);
                    fileTreeComponent.setModel(statusNodes, new EditorCookie[0], modelDataTree);
                    if (fileListComponent.getNodes().isEmpty()) {
                        setVersioningComponent(noContentComponent);
                    } else {
                        setVersioningComponent(getActiveFileComponent().getComponent());
                    }
                }
            });
        }
    }

    static class ModeKeeper {
        private Mode selectedMode;

        private ModeKeeper () {
        }

        void storeMode () {
            GitModuleConfig.getDefault().setLastUsedModificationContext(selectedMode);
        }

        private void setMode (Mode mode) {
            this.selectedMode = mode;
            storeMode();
        }
    }
    
    static class GitStatusNodeImpl extends GitStatusNode<GitLocalFileNode> {
        private final Mode mode;

        private GitStatusNodeImpl (GitLocalFileNode gitLocalFileNode, Mode mode) {
            super(gitLocalFileNode, new ProxyLookup(
                    Lookups.fixed(gitLocalFileNode.getLookupObjects()),
                    Lookups.fixed(gitLocalFileNode)));
            this.mode = mode;
        }

        @Override
        public Action getPreferredAction () {
            return getNodeAction();
        }

        @Override
        public Action getNodeAction () {
            if (node.getInformation().containsStatus(FileInformation.Status.IN_CONFLICT)) {
                return SystemAction.get(ResolveConflictsAction.class);
            } else {
                return SystemAction.get(DiffAction.class);
            }
        }
        
        @Override
        public String getStatusText () {
            return node.getInformation().getStatusText(mode);
        }

    }
}
