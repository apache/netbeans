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

package org.netbeans.modules.subversion.ui.status;

import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.ui.commit.*;
import org.netbeans.modules.subversion.ui.diff.*;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.windows.TopComponent;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.LifecycleManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.subversion.ui.update.UpdateAction;
import org.tigris.subversion.svnclientadapter.*;

/**
 * The main class of the Synchronize view, shows and acts on set of file roots. 
 * 
 * @author Maros Sandor 
 */
class VersioningPanel extends JPanel implements ExplorerManager.Provider, PreferenceChangeListener, PropertyChangeListener, VersioningListener, ActionListener {
    
    private ExplorerManager             explorerManager;
    private final SvnVersioningTopComponent parentTopComponent;
    private final Subversion            subversion;
    private Context                     context;
    private int                         displayStatuses;
    
    private SyncTable                   syncTable;
    private RequestProcessor.Task       refreshViewTask;

    private VersioningPanelProgressSupport          svnProgressSupport;
    private static final RequestProcessor   rp = new RequestProcessor("SubversionView", 1, true);  // NOI18N

    private final NoContentPanel noContentComponent = new NoContentPanel();
    private final ModeKeeper modeKeeper;
    private boolean remoteStatusCalled;
    private boolean focused;

    /**
     * Creates a new Synchronize Panel managed by the given versioning system.
     *  
     * @param parent enclosing top component
     */ 
    public VersioningPanel(SvnVersioningTopComponent parent) {
        this.parentTopComponent = parent;
        this.subversion = Subversion.getInstance();
        refreshViewTask = rp.create(new RefreshViewTask());
        explorerManager = new ExplorerManager ();
        displayStatuses = getDefaultDisplayStatus();
        noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_All")); // NOI18N
        modeKeeper = new ModeKeeper(SvnModuleConfig.getDefault().getLastUsedModificationContext());
        syncTable = new SyncTable(modeKeeper);

        initComponents();
        setComponentsState();
        setVersioningComponent(syncTable.getComponent());
//        reScheduleRefresh(0);

        jPanel2.setFloatable(false);
        jPanel2.putClientProperty("JToolBar.isRollover", Boolean.TRUE);  // NOI18N
        jPanel2.setLayout(new ToolbarLayout());

        parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        parent.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N
        parent.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N

        getActionMap().put("prevInnerView", new AbstractAction("") { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextInnerView();
            }
        });
        getActionMap().put("nextInnerView", new AbstractAction("") { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                onPrevInnerView();
            }
        });
        
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            jPanel2.setBackground(color); 
        }        
    }

    private void onPrevInnerView() {
        if (tgbLocal.isSelected()) {
            tgbRemote.setSelected(true);
        } else if (tgbRemote.isSelected()) {
            tgbAll.setSelected(true);
        } else {
            tgbLocal.setSelected(true);
        }
        onDisplayedStatusChanged();
    }

    private void onNextInnerView() {
        if (tgbLocal.isSelected()) {
            tgbAll.setSelected(true);
        } else if (tgbRemote.isSelected()) {
            tgbLocal.setSelected(true);
        } else {
            tgbRemote.setSelected(true);
        }
        onDisplayedStatusChanged();
    }
    
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        } else if (FileStatusCache.PROP_CACHE_READY.equals(evt.getPropertyName()) && Boolean.TRUE.equals(evt.getNewValue())) {
            reScheduleRefresh(0);
        }
    }

    /**
     * Sets roots (directories) to display in the view.
     * 
     * @param ctx new context if the Versioning panel
     */ 
    void setContext(Context ctx) {
        context = ctx;
        if (EventQueue.isDispatchThread()) {
            syncTable.setTableModel(new SyncFileNode[0]);
        }
        reScheduleRefresh(0);
    }
    
    @Override
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        subversion.getStatusCache().addVersioningListener(this);
        subversion.getStatusCache().addPropertyChangeListener(this);
        explorerManager.addPropertyChangeListener(this);
        subversion.addPropertyChangeListener(syncTable);
        reScheduleRefresh(0);   // the view does not listen for changes when it is not visible
    }

    @Override
    public void removeNotify() {
        SvnModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        subversion.getStatusCache().removeVersioningListener(this);
        subversion.getStatusCache().removePropertyChangeListener(this);
        subversion.removePropertyChangeListener(syncTable);
        explorerManager.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    private void setVersioningComponent(JComponent component)  {
        Component [] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child != jPanel2) {
                if (child == component) {
                    return;
                } else {
                    remove(child);
                    break;
                }
            }
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        
        add(component, gbc);            
        revalidate();
        repaint();
    }
    
    private void setComponentsState() {
        ButtonGroup grp = new ButtonGroup();
        grp.add(tgbLocal);
        grp.add(tgbRemote);
        grp.add(tgbAll);
        if (displayStatuses == FileInformation.STATUS_LOCAL_CHANGE) {
            tgbLocal.setSelected(true);
        }
        else if (displayStatuses == FileInformation.STATUS_REMOTE_CHANGE) { 
            tgbRemote.setSelected(true);
        }
        else { 
            tgbAll.setSelected(true);
        }
    }

    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        if (context == null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    syncTable.setTableModel(new SyncFileNode[0]);
                }
            });
            return;
        }
        SvnUtils.runWithInfoCache(new Runnable() {
            @Override
            public void run () {
                // XXX attach Cancelable hook
                final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(VersioningPanel.class, "MSG_Refreshing_Versioning_View")); // NOI18N
                try {
                    Thread.interrupted();  // clear interupted status
                    ph.start();
                    final SyncFileNode [] nodes = getNodes(context, displayStatuses);  // takes long
                    if (nodes == null) {
                        return;
                        // finally section
                    }

                    final String [] tableColumns;
                    final String branchTitle;
                    if (nodes.length > 0) {
                        boolean stickyCommon = true;
                        String currentSticky = nodes[0].getCopy();
                        for (int i = 1; i < nodes.length; i++) {
                            if (Thread.interrupted()) {
                                // TODO set model that displays that fact to user
                                return;
                            }
                            String sticky = nodes[i].getCopy(); // copy must be initialized on all nodes
                            if (stickyCommon && !Objects.equals(sticky, currentSticky)) {
                                stickyCommon = false;
                            }
                        }
                        if (stickyCommon) {
                            tableColumns = new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH };
                            branchTitle = currentSticky == null ? null : NbBundle.getMessage(VersioningPanel.class, "CTL_VersioningView_BranchTitle_Single", currentSticky); // NOI18N
                        } else {
                            tableColumns = new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_BRANCH, SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH };
                            branchTitle = NbBundle.getMessage(VersioningPanel.class, "CTL_VersioningView_BranchTitle_Multi"); // NOI18N
                        }
                    } else {
                        tableColumns = null;
                        branchTitle = null;
                    }

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (nodes.length > 0) {
                                syncTable.setColumns(tableColumns);
                                parentTopComponent.setBranchTitle(branchTitle);
                                setVersioningComponent(syncTable.getComponent());
                                if (focused) {
                                    syncTable.focus();
                                }
                            } else {
                                setVersioningComponent(noContentComponent);
                                if (focused) {
                                    noContentComponent.requestFocusInWindow();
                                }
                            }
                            syncTable.setTableModel(nodes);
                            // finally section, it's enqueued after this request
                        }
                    });
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ph.finish();
                        }
                    });
                }
            }
        });
    }
    
    private SyncFileNode [] getNodes(Context context, int includeStatus) {
        File [] files = Subversion.getInstance().getStatusCache().listFiles(context.getRootFiles(), includeStatus);
        SvnFileNode [] fnodes = new SvnFileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            fnodes[i] = new SvnFileNode(files[i]);
        }
        SyncFileNode [] nodes = new SyncFileNode[fnodes.length];
        for (int i = 0; i < fnodes.length; i++) {
            if (Thread.interrupted()) return null;
            SvnFileNode fnode = fnodes[i];
            nodes[i] = new SyncFileNode(fnode, this);
        }
        return nodes;
    }

    public int getDisplayStatuses() {
        return displayStatuses;
    }

    /**
     * Performs the "cvs commit" command on all diplayed roots plus "cvs add" for files that are not yet added.
     */ 
    private void onCommitAction() {
        LifecycleManager.getDefault().saveAll();            
        CommitAction.commit(parentTopComponent.getContentTitle(), context, false);
    }
    
    /**
     * Performs the "cvs update" command on all diplayed roots.
     */ 
    private void onUpdateAction() {      
        UpdateAction.performUpdate(context, parentTopComponent.getContentTitle());
        parentTopComponent.contentRefreshed();
    }
    
    /**
     * Refreshes statuses of all files in the view. It does
     * that by issuing the "svn status -u" command, updating the cache
     * and refreshing file nodes.
     */ 
    private void onRefreshAction() {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }          
        LifecycleManager.getDefault().saveAll();
        if(context == null || context.getRootFiles().length < 1) {
            return;
        }
        // XXX #168094 logging
        if (!SvnUtils.isManaged(context.getRootFiles()[0])) {
            Subversion.LOG.warning("VersioningPanel.onRefreshAction: context contains unmanaged file " + context.getRootFiles()[0].getAbsolutePath()); //NOI18N
        }
        refreshStatuses();
    }

    /**
     * Programmatically invokes the Refresh action.
     * Connects to repository and gets recent status.
     */ 
    void performRefreshAction() {
        refreshStatuses();
    }

    /* Async Connects to repository and gets recent status. */
    private void refreshStatuses() {
        if(svnProgressSupport!=null) {
            svnProgressSupport.cancel();
            svnProgressSupport = null;
        }

        SVNUrl repository;
        try {
            repository = CommitAction.getSvnUrl(context);
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);     
            return; 
        }
        // XXX #168094 logging
        if (repository == null) {
            if (context.getRoots().isEmpty()) {
                return;
            }
            Subversion.LOG.info("VersioningPanel.refreshStatuses: null repositoryUrl for " + context.getRootFiles()[0].getAbsolutePath()); //NOI18N
            boolean allUnmanaged = true;
            for (File root : context.getRootFiles()) {
                if (SvnUtils.isManaged(root)) {
                    allUnmanaged = false;
                    break;
                }
            }
            if (allUnmanaged) {
                Exception e = new Exception("VersioningPanel.refreshStatuses: null repositoryUrl for " + context.getRootFiles()[0].getAbsolutePath()); //NOI18N
                Subversion.LOG.log(Level.INFO, null, e);
            }
        }
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor(repository);
        final boolean contactServer = remoteStatusCalled = FileInformation.STATUS_LOCAL_CHANGE != displayStatuses;
        svnProgressSupport = new VersioningPanelProgressSupport() {
            @Override
            public void perform() {
                try {
                    StatusAction.executeStatus(context, this, contactServer);
                } finally {
                    setFinished(true); // stops skipping versioning events
                }
                reScheduleRefresh(0);
            }            
        };
        parentTopComponent.contentRefreshed();
        svnProgressSupport.start(rp, repository, org.openide.util.NbBundle.getMessage(VersioningPanel.class, "LBL_Refresh_Progress")); // NOI18N
    }

    /**
     * Shows Diff panel for all files in the view. The initial type of diff depends on the sync mode: Local, Remote, All.
     * In Local mode, the diff shows CURRENT <-> BASE differences. In Remote mode, it shows BASE<->HEAD differences. 
     */ 
    private void onDiffAction() {   
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }          
        String title = parentTopComponent.getContentTitle();
        if (displayStatuses == FileInformation.STATUS_LOCAL_CHANGE) {            
            LifecycleManager.getDefault().saveAll();
            DiffAction.diff(context, Setup.DIFFTYPE_LOCAL, title, true); // do not trigger status refresh, statuses already known
        } else if (displayStatuses == FileInformation.STATUS_REMOTE_CHANGE) {
            DiffAction.diff(context, Setup.DIFFTYPE_REMOTE, title, true); // do not trigger status refresh, statuses already known
        } else {
            LifecycleManager.getDefault().saveAll();
            DiffAction.diff(context, Setup.DIFFTYPE_ALL, title, true); // do not trigger status refresh, statuses already known
        }
    }

    
    
    private void onDisplayedStatusChanged() {
        if (tgbLocal.isSelected()) {
            modeKeeper.setMode(Setup.DIFFTYPE_LOCAL);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_Local")); // NOI18N
            setDisplayStatuses(FileInformation.STATUS_LOCAL_CHANGE);
        }
        else if (tgbRemote.isSelected()) {
            modeKeeper.setMode(Setup.DIFFTYPE_REMOTE);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_Remote")); // NOI18N
            setDisplayStatuses(FileInformation.STATUS_REMOTE_CHANGE);
        }
        else if (tgbAll.isSelected()) {
            modeKeeper.setMode(Setup.DIFFTYPE_ALL);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_All")); // NOI18N
            setDisplayStatuses(FileInformation.STATUS_REMOTE_CHANGE | FileInformation.STATUS_LOCAL_CHANGE);
        }
    }

    private void setDisplayStatuses(int displayStatuses) {
        this.displayStatuses = displayStatuses;
        if (remoteStatusCalled || displayStatuses == FileInformation.STATUS_LOCAL_CHANGE) {
            reScheduleRefresh(0);
        } else {
            refreshStatuses();
        }
    }

    @Override
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            if (!affectsView(event)) return;
            reScheduleRefresh(1000);
        }
    }

    private boolean affectsView(VersioningEvent event) {
        if(context == null) {
            return false;
        }
        VersioningPanelProgressSupport supp = svnProgressSupport;
        if (supp != null && !supp.isFinished()) {
            // refresh is running, skipping this event; setupModels will be called at the end of the refresh
            // that's because the FileStatusCache fires the event from inside the refresh
            // and we would be listening for our own events
            return false;
        }
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

    /** Reloads data from cache */
    private void reScheduleRefresh(int delayMillis) {
        cancelRefresh();
        refreshViewTask.schedule(delayMillis);
    }

    // HACK copy&paste HACK, replace by save/restore of column width/position
    void deserialize() {
        if (syncTable != null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    syncTable.setDefaultColumnSizes();
                }
            });
        }
    }

    void focus (boolean focused) {
        this.focused = focused;
        if (focused) {
            requestFocusInWindow();
            syncTable.focus();
        }
    }

    /**
     * Cancels both:
     * <ul>
     * <li>cache data fetching
     * <li>background cvs -N update
     * </ul>
     */
    public void cancelRefresh() {
        refreshViewTask.cancel();
    }

    private static int getDefaultDisplayStatus () {
        int displayStatus;
        int selectedMode = SvnModuleConfig.getDefault().getLastUsedModificationContext();
        if (selectedMode == Setup.DIFFTYPE_ALL) {
            displayStatus = FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_REMOTE_CHANGE;
        } else if (selectedMode == Setup.DIFFTYPE_REMOTE) {
            displayStatus = FileInformation.STATUS_REMOTE_CHANGE;
        } else {
            displayStatus = FileInformation.STATUS_LOCAL_CHANGE;
        }
        return displayStatus;
    }

    private class RefreshViewTask implements Runnable {
        @Override
        public void run() {
            setupModels();
        }
    }

    /**
     * Hardcoded toolbar layout. It eliminates need
     * for nested panels their look is hardly maintanable
     * accross several look and feels
     * (e.g. strange layouting panel borders on GTK+).
     *
     * <p>It sets authoritatively component height and takes
     * "prefered" width from components itself.
     *
     */
    private class ToolbarLayout implements LayoutManager {

        /** Expected border height */
        private int TOOLBAR_HEIGHT_ADJUSTMENT = 4;

        private int TOOLBAR_SEPARATOR_MIN_WIDTH = 12;

        /** Cached toolbar height */
        private int toolbarHeight = -1;

        /** Guard for above cache. */
        private Dimension parentSize;

        private Set<JComponent> adjusted = new HashSet<JComponent>();

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public void layoutContainer(Container parent) {
            Dimension dim = VersioningPanel.this.getSize();
            Dimension max = parent.getSize();

            int reminder = max.width - minimumLayoutSize(parent).width;

            int components = parent.getComponentCount();
            int horizont = 0;
            for (int i = 0; i<components; i++) {
                JComponent comp = (JComponent) parent.getComponent(i);
                if (comp.isVisible() == false) continue;
                comp.setLocation(horizont, 0);
                Dimension pref = comp.getPreferredSize();
                int width = pref.width;
                if (comp instanceof JSeparator && ((dim.height - dim.width) <= 0)) {
                    width = Math.max(width, TOOLBAR_SEPARATOR_MIN_WIDTH);
                }
                if (comp instanceof JProgressBar && reminder > 0) {
                    width += reminder;
                }
//                if (comp == getMiniStatus()) {
//                    width = reminder;
//                }

                // in column layout use taller toolbar
                int height = getToolbarHeight(dim) -1;
                comp.setSize(width, height);  // 1 verySoftBevel compensation
                horizont += width;
            }
        }

        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {

            // in column layout use taller toolbar
            Dimension dim = VersioningPanel.this.getSize();
            int height = getToolbarHeight(dim);

            int components = parent.getComponentCount();
            int horizont = 0;
            for (int i = 0; i<components; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible() == false) continue;
                if (comp instanceof AbstractButton) {
                    adjustToobarButton((AbstractButton)comp);
                } else {
                    adjustToolbarComponentSize((JComponent)comp);
                }
                Dimension pref = comp.getPreferredSize();
                int width = pref.width;
                if (comp instanceof JSeparator && ((dim.height - dim.width) <= 0)) {
                    width = Math.max(width, TOOLBAR_SEPARATOR_MIN_WIDTH);
                }
                horizont += width;
            }

            return new Dimension(horizont, height);
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            // Eliminates double height toolbar problem
            Dimension dim = VersioningPanel.this.getSize();
            int height = getToolbarHeight(dim);

            return new Dimension(Integer.MAX_VALUE, height);
        }

        /**
         * Computes vertical toolbar components height that can used for layout manager hinting.
         * @return size based on font size and expected border.
         */
        private int getToolbarHeight(Dimension parent) {

            if (parentSize == null || (parentSize.equals(parent) == false)) {
                parentSize = parent;
                toolbarHeight = -1;
            }

            if (toolbarHeight == -1) {
                Graphics g = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                .getDefaultScreenDevice()
                                                .getDefaultConfiguration()
                                                .createCompatibleImage(1, 1)
                                                .getGraphics();
                UIDefaults def = UIManager.getLookAndFeelDefaults();

                int height = 0;
                String[] fonts = {"Label.font", "Button.font", "ToggleButton.font"};      // NOI18N
                for (int i=0; i<fonts.length; i++) {
                    Font f = def.getFont(fonts[i]);
                    FontMetrics fm = g.getFontMetrics(f);
                    height = Math.max(height, fm.getHeight());
                }
                toolbarHeight = height + TOOLBAR_HEIGHT_ADJUSTMENT;
                if ((parent.height - parent.width) > 0) {
                    toolbarHeight += TOOLBAR_HEIGHT_ADJUSTMENT;
                }
            }

            return toolbarHeight;
        }


        /** Toolbar controls must be smaller and should be transparent*/
        private void adjustToobarButton(final AbstractButton button) {

            if (adjusted.contains(button)) return;

            // workaround for Ocean L&F clutter - toolbars use gradient.
            // To make the gradient visible under buttons the content area must not
            // be filled. To support rollover it must be temporarily filled
            if (button instanceof JToggleButton == false) {
                button.setContentAreaFilled(false);
                button.setMargin(new Insets(0, 3, 0, 3));
                button.setBorderPainted(false);
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        button.setContentAreaFilled(false);
                        button.setBorderPainted(false);
                    }
                });
            }

            adjustToolbarComponentSize(button);
        }

        private void adjustToolbarComponentSize(JComponent button) {

            if (adjusted.contains(button)) return;

            // as we cannot get the button small enough using the margin and border...
            if (button.getBorder() instanceof CompoundBorder) { // from BasicLookAndFeel
                Dimension pref = button.getPreferredSize();

                // XXX #41827 workaround w2k, that adds eclipsis (...) instead of actual text
                if ("Windows".equals(UIManager.getLookAndFeel().getID())) {  // NOI18N
                    pref.width += 9;
                }
                button.setPreferredSize(pref);
            }

            adjusted.add(button);
        }
    }

    private abstract class VersioningPanelProgressSupport extends SvnProgressSupport {
        private boolean finished;

        public boolean isFinished() {
            return finished;
        }

        protected void setFinished (boolean flag) {
            finished = flag;
        }

    }

    static class ModeKeeper {
        private int mode;

        private ModeKeeper (int defaultMode) {
            mode = defaultMode;
        }

        void storeMode () {
            SvnModuleConfig.getDefault().setLastUsedModificationContext(mode);
        }

        private void setMode (int mode) {
            this.mode = mode;
            storeMode();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JToolBar();
        tgbAll = new javax.swing.JToggleButton();
        tgbLocal = new javax.swing.JToggleButton();
        tgbRemote = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnRefresh = new javax.swing.JButton();
        btnDiff = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnUpdate = new javax.swing.JButton();
        btnCommit = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        tgbAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/remote_vs_local.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/status/Bundle"); // NOI18N
        tgbAll.setToolTipText(bundle.getString("CTL_Synchronize_Action_All_Tooltip")); // NOI18N
        tgbAll.setFocusable(false);
        tgbAll.addActionListener(this);
        jPanel2.add(tgbAll);

        tgbLocal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/local_vs_local.png"))); // NOI18N
        tgbLocal.setToolTipText(bundle.getString("CTL_Synchronize_Action_Local_Tooltip")); // NOI18N
        tgbLocal.setFocusable(false);
        tgbLocal.addActionListener(this);
        jPanel2.add(tgbLocal);

        tgbRemote.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/remote_vs_remote.png"))); // NOI18N
        tgbRemote.setToolTipText(bundle.getString("CTL_Synchronize_Action_Remote_Tooltip")); // NOI18N
        tgbRemote.setFocusable(false);
        tgbRemote.addActionListener(this);
        jPanel2.add(tgbRemote);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(2, 20));
        jPanel2.add(jSeparator1);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/refresh.png"))); // NOI18N
        btnRefresh.setToolTipText(bundle.getString("CTL_Synchronize_Action_Refresh_Tooltip")); // NOI18N
        btnRefresh.setActionCommand("null"); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setPreferredSize(new java.awt.Dimension(22, 23));
        btnRefresh.addActionListener(this);
        jPanel2.add(btnRefresh);
        btnRefresh.getAccessibleContext().setAccessibleName("Refresh Status");

        btnDiff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff.png"))); // NOI18N
        btnDiff.setToolTipText(bundle.getString("CTL_Synchronize_Action_Diff_Tooltip")); // NOI18N
        btnDiff.setFocusable(false);
        btnDiff.setPreferredSize(new java.awt.Dimension(22, 25));
        btnDiff.addActionListener(this);
        jPanel2.add(btnDiff);
        btnDiff.getAccessibleContext().setAccessibleName("Diff All");

        jPanel3.setOpaque(false);
        jPanel2.add(jPanel3);

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/update.png"))); // NOI18N
        btnUpdate.setToolTipText(bundle.getString("CTL_Synchronize_Action_Update_Tooltip")); // NOI18N
        btnUpdate.setFocusable(false);
        btnUpdate.setPreferredSize(new java.awt.Dimension(22, 25));
        btnUpdate.addActionListener(this);
        jPanel2.add(btnUpdate);
        btnUpdate.getAccessibleContext().setAccessibleName("Update");

        btnCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/commit.png"))); // NOI18N
        btnCommit.setToolTipText(bundle.getString("CTL_CommitForm_Action_Commit_Tooltip")); // NOI18N
        btnCommit.setFocusable(false);
        btnCommit.setPreferredSize(new java.awt.Dimension(22, 25));
        btnCommit.addActionListener(this);
        jPanel2.add(btnCommit);
        btnCommit.getAccessibleContext().setAccessibleName("Commit");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(jPanel2, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == tgbAll) {
            VersioningPanel.this.tgbAllActionPerformed(evt);
        }
        else if (evt.getSource() == tgbLocal) {
            VersioningPanel.this.tgbLocalActionPerformed(evt);
        }
        else if (evt.getSource() == tgbRemote) {
            VersioningPanel.this.tgbRemoteActionPerformed(evt);
        }
        else if (evt.getSource() == btnRefresh) {
            VersioningPanel.this.btnRefreshActionPerformed(evt);
        }
        else if (evt.getSource() == btnDiff) {
            VersioningPanel.this.btnDiffActionPerformed(evt);
        }
        else if (evt.getSource() == btnUpdate) {
            VersioningPanel.this.btnUpdateActionPerformed(evt);
        }
        else if (evt.getSource() == btnCommit) {
            VersioningPanel.this.btnCommitActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void btnDiffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiffActionPerformed
        onDiffAction();//GEN-LAST:event_btnDiffActionPerformed
    }                                       

    private void tgbAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbAllActionPerformed
        onDisplayedStatusChanged();//GEN-LAST:event_tgbAllActionPerformed
    }                                      

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        onUpdateAction();//GEN-LAST:event_btnUpdateActionPerformed
    }                                         

    private void tgbRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbRemoteActionPerformed
        onDisplayedStatusChanged();//GEN-LAST:event_tgbRemoteActionPerformed
    }                                         

    private void tgbLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbLocalActionPerformed
        onDisplayedStatusChanged();//GEN-LAST:event_tgbLocalActionPerformed
    }                                        

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
        onCommitAction();//GEN-LAST:event_btnCommitActionPerformed
    }                                         

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        onRefreshAction();//GEN-LAST:event_btnRefreshActionPerformed
    }                                          
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnDiff;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JToolBar jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton tgbAll;
    private javax.swing.JToggleButton tgbLocal;
    private javax.swing.JToggleButton tgbRemote;
    // End of variables declaration//GEN-END:variables

}
