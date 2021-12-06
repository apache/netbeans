/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.status;

import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.windows.TopComponent;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.LifecycleManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.versionvault.Clearcase;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.netbeans.modules.versionvault.FileInformation;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.modules.versionvault.ui.diff.Setup;
import org.netbeans.modules.versionvault.ui.diff.DiffAction;
import org.netbeans.modules.versionvault.ui.checkin.CheckinAction;
import org.netbeans.modules.versionvault.ui.update.UpdateAction;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * The main class of the Synchronize view, shows and acts on set of file roots. 
 * 
 * @author Maros Sandor 
 */
class VersioningPanel extends JPanel implements ExplorerManager.Provider, PreferenceChangeListener, PropertyChangeListener, VersioningListener, ActionListener {
    
    private ExplorerManager                 explorerManager;
    private final ClearcaseTopComponent     parentTopComponent;
    private Context                         context;
    private int                             displayStatuses;
    
    private SyncTable                       syncTable;
    
    private static final RequestProcessor   rp = new RequestProcessor("ClearCaseView", 1, true);  // NOI18N    
    private FileStatusCache.RefreshSupport  refreshSupport; 
    private final NoContentPanel            noContentComponent = new NoContentPanel();
    private RequestProcessor.Task           refreshViewTask;
    
        
    /**
     * Creates a new Synchronize Panel managed by the given versioning system.
     *  
     * @param parent enclosing top component
     */ 
    public VersioningPanel(ClearcaseTopComponent parent) {
        this.parentTopComponent = parent;
        explorerManager = new ExplorerManager ();
        displayStatuses = FileInformation.STATUS_LOCAL_CHANGE;
        refreshViewTask = rp.create(new RefreshViewTask());
        noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_All")); // NOI18N
        syncTable = new SyncTable();

        initComponents();
        tgbAll.setVisible(false);
        tgbLocal.setVisible(false);
        tgbRemote.setVisible(false);
        
        setComponentsState();
        setVersioningComponent(syncTable.getComponent());
        reScheduleRefresh(0);

        // XXX click it in form editor, probbaly requires  Mattisse >=v2
        jPanel2.setFloatable(false);
        jPanel2.putClientProperty("JToolBar.isRollover", Boolean.TRUE);  // NOI18N
        jPanel2.setLayout(new ToolbarLayout());
        
        parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        parent.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N
        parent.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N

        getActionMap().put("prevInnerView", new AbstractAction("") { // NOI18N
            public void actionPerformed(ActionEvent e) {
                onNextInnerView();
            }
        });
        getActionMap().put("nextInnerView", new AbstractAction("") { // NOI18N
            public void actionPerformed(ActionEvent e) {
                onPrevInnerView();
            }
        });
        
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");
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
    
    public void preferenceChange(PreferenceChangeEvent evt) {        
        if (evt.getKey().startsWith(ClearcaseModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        } 
    }

    /**
     * Sets roots (directories) to display in the view.
     * 
     * @param ctx new context if the Versioning panel
     */ 
    void setContext(Context ctx) {
        context = ctx;
        if(ctx != null) {
            Set<File> roots = ctx.getRootFiles();
            getProgressSupport().setRootFiles(roots.toArray(new File[roots.size()]));
            performRefreshAction();
        }
    }
    
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }
    
    public void addNotify() {
        super.addNotify();
        ClearcaseModuleConfig.getPreferences().addPreferenceChangeListener(this);
        Clearcase.getInstance().getFileStatusCache().addVersioningListener(this);        
        explorerManager.addPropertyChangeListener(this);
        reScheduleRefresh(0);   // the view does not listen for changes when it is not visible
    }

    public void removeNotify() {
        ClearcaseModuleConfig.getPreferences().removePreferenceChangeListener(this);        
        Clearcase.getInstance().getFileStatusCache().removeVersioningListener(this);
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
        else { 
            tgbAll.setSelected(true);
        }
    }


    public int getDisplayStatuses() {
        return displayStatuses;
    }

    /**
     * Performs the "checkin" command on all diplayed roots 
     * @see CheckinAction 
     */  
    private void onCommitAction() {
        if(context == null || context.getRootFiles().size() < 1) {
            return;
        }
        LifecycleManager.getDefault().saveAll();            
        CheckinAction.checkin(context.getVCSContext());
    }
    
    /**
     * Performs the "cleartool update" command on all diplayed roots.
     */ 
    private void onUpdateAction() {
        if(context == null || context.getRootFiles().size() < 1) {
            return;
        }
        UpdateAction.update(context.getVCSContext());
        parentTopComponent.contentRefreshed();
    }
    
    /**
     * Refreshes statuses of all files in the view. 
     */ 
    private void onRefreshAction() {
        if(context == null || context.getRootFiles().size() < 1) {
            return;
        }        
        LifecycleManager.getDefault().saveAll();
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
        if(context != null) {
            getProgressSupport().start();
        }
    }

    private FileStatusCache.RefreshSupport getProgressSupport() {
        if(refreshSupport == null) {
            refreshSupport = new FileStatusCache.RefreshSupport(rp, context.getVCSContext(), NbBundle.getMessage(VersioningPanel.class, "Progress_RefreshingStatus")) { //NOI18N
                @Override
                protected void perform() {
                    refresh();
                    setupModels();
                    parentTopComponent.contentRefreshed();                
                }
            };
        }
        return refreshSupport;
    }    
    
    /**
     * Shows Diff panel for all files in the view. The initial type of diff depends on the sync mode: Local, Remote, All.
     * In Local mode, the diff shows CURRENT <-> BASE differences. In Remote mode, it shows BASE<->HEAD differences. 
     */ 
    private void onDiffAction() {
        if(context == null || context.getRootFiles().size() < 1) {
            return;
        }        
        String title = parentTopComponent.getContentTitle();
        if (displayStatuses == FileInformation.STATUS_LOCAL_CHANGE) {            
            LifecycleManager.getDefault().saveAll();
            DiffAction.diff(context.getVCSContext(), Setup.DIFFTYPE_LOCAL, title);
        } else {
            LifecycleManager.getDefault().saveAll();
            DiffAction.diff(context.getVCSContext(), Setup.DIFFTYPE_ALL, title);
        }
    }

    private void onDisplayedStatusChanged() {
        if (tgbLocal.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_LOCAL_CHANGE);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_Local")); // NOI18N
        }
        else if (tgbRemote.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_LOCAL_CHANGE);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_Remote")); // NOI18N
        }
        else if (tgbAll.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_LOCAL_CHANGE);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_All")); // NOI18N
        }
    }

    private void setDisplayStatuses(int displayStatuses) {
        this.displayStatuses = displayStatuses;
        reScheduleRefresh(0);
    }

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
        File file = (File) event.getParams()[0];
        FileInformation oldInfo = (FileInformation) event.getParams()[1];
        FileInformation newInfo = (FileInformation) event.getParams()[2];
        if (oldInfo == null && newInfo != null) {
            if ((newInfo.getStatus() & displayStatuses) == 0) return false;
        } else if (newInfo != null) {
            if ((oldInfo.getStatus() & displayStatuses) + (newInfo.getStatus() & displayStatuses) == 0) return false;
        }    
        return context.contains(file);
    }

    /** Reloads data from cache */
    private void reScheduleRefresh(int delayMillis) {
        refreshViewTask.schedule(delayMillis);
    }

    // HACK copy&paste HACK, replace by save/restore of column width/position
    void deserialize() {
        if (syncTable != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    syncTable.setDefaultColumnSizes();
                }
            });
        }
    }

    void focus() {
        syncTable.focus();
    }

    /**
     * Cancels both:
     * <ul>
     * <li>cache data fetching
     * </ul>
     */
    public void cancelRefresh() {
        refreshViewTask.cancel();
        if(context != null) {
            getProgressSupport().cancel();
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

        public void removeLayoutComponent(Component comp) {
        }

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
                // in column layout use taller toolbar
                int height = getToolbarHeight(dim) -1;
                comp.setSize(width, height);  // 1 verySoftBevel compensation
                horizont += width;
            }
        }

        public void addLayoutComponent(String name, Component comp) {
        }

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
                BufferedImage image = new BufferedImage(1,1,BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = image.createGraphics();
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
                    public void mouseEntered(MouseEvent e) {
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }

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

    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        if (context == null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    syncTable.setTableModel(new SyncFileNode[0]);
                }
            });
            return;
        }
        // TODO: a little hacky
        btnUpdate.setEnabled(new UpdateAction("", context.getVCSContext()).isEnabled()); //NOI18N

        final SyncFileNode [] nodes = getNodes(context.getVCSContext(), displayStatuses);  // takes long
        if (nodes == null) {
            return;
            // finally section
        }

        final String [] tableColumns;            
        if (nodes.length > 0) {                
            tableColumns = new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH, SyncFileNode.COLUMN_NAME_RULE };                                    
        } else {
            tableColumns = null;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (nodes.length > 0) {
                    syncTable.setColumns(tableColumns);
                    setVersioningComponent(syncTable.getComponent());
                } else {
                    setVersioningComponent(noContentComponent);
                }
                syncTable.setTableModel(nodes);
                // finally section, it's enqueued after this request
            }
        });
    }

    private SyncFileNode [] getNodes(VCSContext context, int includeStatus) {
        FileNode [] fnodes = getFileNodes(context, includeStatus);
        SyncFileNode [] nodes = new SyncFileNode[fnodes.length];
        for (int i = 0; i < fnodes.length; i++) {
            FileNode fnode = fnodes[i];
            nodes[i] = new SyncFileNode(fnode, VersioningPanel.this);
        }
        return nodes;
    }

    public FileNode [] getFileNodes(VCSContext context, int includeStatus) {
        File [] files = Clearcase.getInstance().getFileStatusCache().listFiles(context, includeStatus);
        FileNode [] nodes = new FileNode[files.length];
        for (int i = 0; i < files.length; i++) {
            nodes[i] = new FileNode(files[i]);
        }
        return nodes;
    }
    
    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
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

        org.openide.awt.Mnemonics.setLocalizedText(tgbAll, org.openide.util.NbBundle.getBundle(VersioningPanel.class).getString("CTL_Synchronize_Action_All_Label")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/versionvault/ui/status/Bundle"); // NOI18N
        tgbAll.setToolTipText(bundle.getString("CTL_Synchronize_Action_All_Tooltip")); // NOI18N
        tgbAll.setEnabled(false);
        tgbAll.setFocusable(false);
        tgbAll.addActionListener(this);
        jPanel2.add(tgbAll);

        org.openide.awt.Mnemonics.setLocalizedText(tgbLocal, org.openide.util.NbBundle.getBundle(VersioningPanel.class).getString("CTL_Synchronize_Action_Local_Label")); // NOI18N
        tgbLocal.setToolTipText(bundle.getString("CTL_Synchronize_Action_Local_Tooltip")); // NOI18N
        tgbLocal.setFocusable(false);
        tgbLocal.addActionListener(this);
        jPanel2.add(tgbLocal);

        org.openide.awt.Mnemonics.setLocalizedText(tgbRemote, org.openide.util.NbBundle.getBundle(VersioningPanel.class).getString("CTL_Synchronize_Action_Remote_Label")); // NOI18N
        tgbRemote.setToolTipText(bundle.getString("CTL_Synchronize_Action_Remote_Tooltip")); // NOI18N
        tgbRemote.setEnabled(false);
        tgbRemote.setFocusable(false);
        tgbRemote.addActionListener(this);
        jPanel2.add(tgbRemote);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(2, 20));
        jPanel2.add(jSeparator1);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/refresh.png"))); // NOI18N
        btnRefresh.setToolTipText(bundle.getString("CTL_Synchronize_Action_Refresh_Tooltip")); // NOI18N
        btnRefresh.setActionCommand(bundle.getString("CTL_Synchronize_TopComponent_Title")); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setPreferredSize(new java.awt.Dimension(22, 23));
        btnRefresh.addActionListener(this);
        jPanel2.add(btnRefresh);
        btnRefresh.getAccessibleContext().setAccessibleName(NbBundle.getMessage(VersioningPanel.class, "VersioningPanel.btnRefresh.AccessibleContext.accessibleName")); // NOI18N

        btnDiff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/diff.png"))); // NOI18N
        btnDiff.setToolTipText(bundle.getString("CTL_Synchronize_Action_Diff_Tooltip")); // NOI18N
        btnDiff.setFocusable(false);
        btnDiff.setPreferredSize(new java.awt.Dimension(22, 25));
        btnDiff.addActionListener(this);
        jPanel2.add(btnDiff);
        btnDiff.getAccessibleContext().setAccessibleName(NbBundle.getMessage(VersioningPanel.class, "VersioningPanel.btnDiff.AccessibleContext.accessibleName")); // NOI18N

        jPanel3.setOpaque(false);
        jPanel2.add(jPanel3);

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/update.png"))); // NOI18N
        btnUpdate.setToolTipText(bundle.getString("CTL_Synchronize_Action_Update_Tooltip")); // NOI18N
        btnUpdate.setFocusable(false);
        btnUpdate.setPreferredSize(new java.awt.Dimension(22, 25));
        btnUpdate.addActionListener(this);
        jPanel2.add(btnUpdate);
        btnUpdate.getAccessibleContext().setAccessibleName(NbBundle.getMessage(VersioningPanel.class, "VersioningPanel.btnUpdate.AccessibleContext.accessibleName")); // NOI18N

        btnCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/commit.png"))); // NOI18N
        btnCommit.setToolTipText(bundle.getString("CTL_CommitForm_Action_Commit_Tooltip")); // NOI18N
        btnCommit.setFocusable(false);
        btnCommit.setPreferredSize(new java.awt.Dimension(22, 25));
        btnCommit.addActionListener(this);
        jPanel2.add(btnCommit);
        btnCommit.getAccessibleContext().setAccessibleName(NbBundle.getMessage(VersioningPanel.class, "VersioningPanel.btnCommit.AccessibleContext.accessibleName")); // NOI18N

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
        onDiffAction();
    }//GEN-LAST:event_btnDiffActionPerformed

    private void tgbAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbAllActionPerformed
        onDisplayedStatusChanged();
    }//GEN-LAST:event_tgbAllActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        onUpdateAction();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void tgbRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbRemoteActionPerformed
        onDisplayedStatusChanged();
    }//GEN-LAST:event_tgbRemoteActionPerformed

    private void tgbLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbLocalActionPerformed
        onDisplayedStatusChanged();
    }//GEN-LAST:event_tgbLocalActionPerformed

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
        onCommitAction();
    }//GEN-LAST:event_btnCommitActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        onRefreshAction();
    }//GEN-LAST:event_btnRefreshActionPerformed
    
    
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
