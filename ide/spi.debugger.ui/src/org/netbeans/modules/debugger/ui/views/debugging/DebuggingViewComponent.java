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

package org.netbeans.modules.debugger.ui.views.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.spi.debugger.ui.DebuggingView.DVSupport;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThread;
import org.netbeans.spi.debugger.ui.DebuggingView.DVThreadGroup;
import org.netbeans.spi.debugger.ui.DebuggingView.Deadlock;
import org.netbeans.spi.debugger.ui.ViewFactory;
import org.netbeans.spi.debugger.ui.ViewLifecycle;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.Models.CompoundModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author  Dan
 */
public class DebuggingViewComponent extends TopComponent implements org.openide.util.HelpCtx.Provider,
       ExplorerManager.Provider, PropertyChangeListener, TreeExpansionListener, TreeModelListener,
       AdjustmentListener, ChangeListener, MouseWheelListener,
       ViewLifecycle.ModelUpdateListener {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "debuggingView"; //NOI18N
    public static final int BAR_WIDTH = 8;
    
    static final Color hitsColor;
    static final Color hitsBarColor;
    static final Color deadlockColor;
    static final Color greenBarColor;
    private transient Color treeBackgroundColor = getTreeBackgroundColor();
    
    private transient RequestProcessor requestProcessor = new RequestProcessor("DebuggingView Refresh Scheduler", 1);
    private transient AtomicBoolean refreshScheduled = new AtomicBoolean(false);
    private transient ExplorerManager manager = new ExplorerManager();
    private transient ViewLifecycle viewLifecycle;
    private Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
    private PreferenceChangeListener prefListener;
    private SessionsComboBoxListener sessionsComboListener;
    private VisibleTreePosition visibleTreePosition = null;
    private boolean ignoreScrollAdjustment = false;

    private transient ImageIcon resumeIcon;
    private transient ImageIcon focusedResumeIcon;
    private transient ImageIcon pressedResumeIcon;
    private transient ImageIcon suspendIcon;
    private transient ImageIcon focusedSuspendIcon;
    private transient ImageIcon pressedSuspendIcon;
    
    private DebugTreeView treeView;
    private TapPanel tapPanel;
    private InfoPanel infoPanel;
    private DVSupport debugger;
    private Session session;
    private DVSupport previousDebugger;
    private Reference<DVThread> threadMadeCurrentRef;
    private Reference<DVThread> threadToScrollRef;

    private ViewRefresher viewRefresher = new ViewRefresher();
    private BarsPanel leftPanel;
    private IconsPanel rightPanel;
    
    private ThreadsListener threadsListener = null;

    private final Object lock = new Object();
    
    static {
        // Init colors:
        Color c = UIManager.getColor("nb.debugger.debugging.BPHits");
        if (c == null) {
            c = new Color(255, 255, 178);
            Color tbc = getTreeBackgroundColor();
            int dl = Math.abs(luminance(c) - luminance(tbc));
            if (dl > 125) {
                c = new Color(70, 70, 0);
            }
        }
        hitsColor = c;
        
        deadlockColor = UIManager.getColor("nb.errorForeground"); // NOI18N
        
        c = UIManager.getColor("nb.debugger.debugging.bars.BPHits");
        if (c == null) {
            c = new Color(230, 230, 130);
            Color tbc = getTreeBackgroundColor();
            int dl = Math.abs(luminance(c) - luminance(tbc));
            if (dl > 125) {
                c = new Color(120, 120, 25);
            }
        }
        hitsBarColor = c;
        
        c = UIManager.getColor("nb.debugger.debugging.bars.currentThread");
        if (c == null) {
            c = new Color(189, 230, 170);
            Color tbc = getTreeBackgroundColor();
            int dl = Math.abs(luminance(c) - luminance(tbc));
            if (dl > 125) {
                c = new Color(40, 100, 35);
            }
        }
        greenBarColor = c;
    }
    
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static Reference<DebuggingViewComponent> instance = null;
    
    /** Creates new form DebuggingView */
    public DebuggingViewComponent() {
        setIcon(ImageUtilities.loadImage ("org/netbeans/modules/debugger/resources/debuggingView/debugging_16.png")); // NOI18N
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE);    // NOI18N
        
        initComponents();
    
        resumeIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/resume_button_16.png", false);
        focusedResumeIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/resume_button_focused_16.png", false);
        pressedResumeIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/resume_button_pressed_16.png", false);
        suspendIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/suspend_button_16.png", false);
        focusedSuspendIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/suspend_button_focused_16.png", false);
        pressedSuspendIcon = ImageUtilities.loadImageIcon("org/netbeans/modules/debugger/resources/debuggingView/suspend_button_pressed_16.png", false);
        
        setBackground(treeBackgroundColor);
        
        leftPanel = new BarsPanel();
        rightPanel = new IconsPanel();
        mainPanel.setBackground(treeBackgroundColor);
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        tapPanel = new TapPanel();
        tapPanel.setOrientation(TapPanel.DOWN);
        tapPanel.setExpanded(true);
        
        infoPanel = new InfoPanel(tapPanel, this);
        tapPanel.add(infoPanel);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(tapPanel, gridBagConstraints);
        
        manager.addPropertyChangeListener(this);
        
        prefListener = new DebuggingPreferenceChangeListener();
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, preferences));
        sessionsComboListener = new SessionsComboBoxListener();

        scrollBarPanel.setVisible(false);
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            scrollBarPanel.setBackground(tapPanel.getBackground());
            scrollBarPanel.setOpaque(true);
        }
        treeScrollBar.addAdjustmentListener(this);

        setSuspendTableVisible(preferences.getBoolean(FiltersDescriptor.SHOW_SUSPEND_TABLE, true));

        mainScrollPane.getVerticalScrollBar().addAdjustmentListener(
                new AdjustmentListener() {

                    @Override
                    public void adjustmentValueChanged(AdjustmentEvent e) {
                        if (!e.getValueIsAdjusting() && !ignoreScrollAdjustment) {
                            storeScrollPosition();
                        }
                    }
                });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sessionComboBox = new javax.swing.JComboBox();
        mainScrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        scrollBarPanel = new javax.swing.JPanel();
        treeScrollBar = new javax.swing.JScrollBar();
        leftPanel1 = new javax.swing.JPanel();
        rightPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        sessionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Java Project" }));
        sessionComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(sessionComboBox, gridBagConstraints);
        sessionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DebuggingViewComponent.class, "DebuggingViewComponent.sessionComboBox.AccessibleContext.accessibleName")); // NOI18N
        sessionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebuggingViewComponent.class, "DebuggingViewComponent.sessionComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        mainScrollPane.setBorder(null);
        mainScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setPreferredSize(new java.awt.Dimension(32, 10));

        mainPanel.setLayout(new java.awt.BorderLayout());
        mainScrollPane.setViewportView(mainPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mainScrollPane, gridBagConstraints);

        scrollBarPanel.setMaximumSize(new java.awt.Dimension(2147483647, 17));
        scrollBarPanel.setLayout(new java.awt.BorderLayout());

        treeScrollBar.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollBarPanel.add(treeScrollBar, java.awt.BorderLayout.CENTER);

        leftPanel1.setOpaque(false);
        leftPanel1.setPreferredSize(new java.awt.Dimension(8, 0));
        leftPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollBarPanel.add(leftPanel1, java.awt.BorderLayout.WEST);

        rightPanel1.setOpaque(false);
        rightPanel1.setPreferredSize(new java.awt.Dimension(24, 0));
        rightPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        scrollBarPanel.add(rightPanel1, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(scrollBarPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel leftPanel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel rightPanel1;
    private javax.swing.JPanel scrollBarPanel;
    private javax.swing.JComboBox sessionComboBox;
    private javax.swing.JScrollBar treeScrollBar;
    // End of variables declaration//GEN-END:variables

    private static Color getTreeBackgroundColor() {
        Color c = null;
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) {                //NOI18N
            c = UIManager.getColor("NbExplorerView.background");                //NOI18N
        }
        if (c == null) {
            c = UIManager.getColor("Tree.textBackground");                      // NOI18N
        }
        return c;
    }
    
    static int luminance(Color c) {
        return (299*c.getRed() + 587*c.getGreen() + 114*c.getBlue()) / 1000;
    }

    public void setRootContext(final Models.CompoundModel model, final DebuggerEngine engine) {
        {   // Destroy the old node
            Node root = manager.getRootContext();
            if (root != null) {
                try {
                    root.destroy();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (threadsListener == null) {
            threadsListener = ThreadsListener.getDefault();
        }
        if (engine != null) {
            final DVSupport deb = engine.lookupFirst(null, DVSupport.class);
            if (deb != null) {
                if (threadsListener != null) {
                    threadsListener.setDebuggingView(deb, this);
                }
            }
            synchronized (lock) {
                if (previousDebugger != null) {
                    previousDebugger.removePropertyChangeListener(this);
                }
                previousDebugger = this.debugger;
                this.debugger = deb;
                if (deb != null) {
                    this.session = engine.lookupFirst(null, Session.class);
                    deb.addPropertyChangeListener(this);
                } else {
                    this.session = null;
                }
            }
            if (threadsListener != null) {
                requestProcessor.post(new Runnable() {
                    @Override
                    public void run() {
                        threadsListener.changeDebugger(deb);
                    }
                });
            }
            FiltersDescriptor.getInstance().setUpFilters(deb);
        } else {
            synchronized (lock) {
                if (previousDebugger != null) {
                    previousDebugger.removePropertyChangeListener(this);
                }
                previousDebugger = null;
                this.debugger = null;
                this.session = null;
            }
            if (threadsListener != null) {
                requestProcessor.post(new Runnable() {
                    @Override
                    public void run() {
                        threadsListener.changeDebugger(null);
                    }
                });
            }
            FiltersDescriptor.getInstance().setUpFilters(null);
            visibleTreePosition = null;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Node root;
                if (model == null) {
                    root = Node.EMPTY;
                    releaseTreeView();
                } else {
                    DebugTreeView theTreeView;
                    synchronized(lock) {
                        if (treeView == null) {
                            createTreeView();
                        }
                        theTreeView = treeView;
                    }
                    root = Models.createNodes(model, theTreeView);
                    theTreeView.setExpansionModel(model);
                }
                manager.setRootContext(root);
                refreshView();
                updateSessionsComboBox();
                adjustTreeScrollBar(-1);
                if (engine == null) {
                    // Clean up the UI from memory leaks:
                    setActivatedNodes (new Node[] {});
                    DebugTreeView tView = getTreeView();
                    if (tView != null) {
                        tView.resetSelection();
                    }
                    //treeView.updateUI(); -- corrupts the UI!
                }
            }
        });
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public static DebuggingViewComponent getInstance() {
        DebuggingViewComponent view;
        view = (DebuggingViewComponent) WindowManager.getDefault().findTopComponent(ID);
        if (view == null) {
            view = getDefault();
        }
        return view;
    }

    /*public Action[] getFilterActions() {
        return FiltersDescriptor.getInstance().getFilterActions();
    }*/
    
    public void setSuspendTableVisible(boolean visible) {
        rightPanel.setVisible(visible);
    }
    
    /**
     * Singleton accessor reserved for the window systemm only. The window
     * system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code. </em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized DebuggingViewComponent getDefault() {
        DebuggingViewComponent view;
        if (instance == null) {
            view = new DebuggingViewComponent();
            instance = new WeakReference<DebuggingViewComponent>(view);
        } else {
            view = instance.get();
            if (view == null) {
                view = new DebuggingViewComponent();
                instance = new WeakReference<DebuggingViewComponent>(view);
            }
        }
        return view;
    }
    
    @Override
    protected String preferredID() {
        return ID;
    }

    @Override
    protected void componentShowing() {
        super.componentShowing ();
        viewLifecycle = ViewFactory.getDefault().createViewLifecycle("DebuggingView", null);
        viewLifecycle.addModelUpdateListener(this);
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden ();
        if (viewLifecycle != null) {
            viewLifecycle.destroy();
            viewLifecycle = null;
            setRootContext(null, null);
        }
    }

    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }

    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("DebuggingView"); // NOI18N
    }
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public boolean requestFocusInWindow() {
        DebugTreeView tView = getTreeView();
        if (tView != null) {
            return tView.requestFocusInWindow ();
        }
        return super.requestFocusInWindow();
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage (DebuggingViewComponent.class, "CTL_Debugging_view"); // NOI18N
    }
    
    @Override
    public String getToolTipText() {
        return NbBundle.getMessage (DebuggingViewComponent.class, "CTL_Debugging_tooltip"); // NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (ExplorerManager.PROP_ROOT_CONTEXT.equals(propertyName) || 
                ExplorerManager.PROP_NODE_CHANGE.equals(propertyName)) {
            refreshView();
        } else if (DVSupport.PROP_CURRENT_THREAD.equals(propertyName)) {
            DVThread currentThread;
            synchronized (lock) {
                currentThread = (debugger != null) ? debugger.getCurrentThread() : null;
            }
            if (currentThread != null) {
                DVThread thread = threadMadeCurrentRef != null ? threadMadeCurrentRef.get() : null;
                if (thread != currentThread) {
                    threadToScrollRef = new WeakReference<>(currentThread);
                }
            }
            refreshView();
        } else if (propertyName.equals (ExplorerManager.PROP_SELECTED_NODES)) {
            final Node[] nodes = (Node[]) evt.getNewValue();
            if (SwingUtilities.isEventDispatchThread()) {
                setActivatedNodes (nodes);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setActivatedNodes (nodes);
                    }
                });
            }
        }
    }

    private static boolean isDebuggingSession(Session s) {
        DebuggerEngine engine = s.getCurrentEngine ();
        if (engine == null) {
            return false;
        }
        return engine.lookupFirst(null, DVSupport.class) != null;
    }
    
    void updateSessionsComboBox() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                sessionComboBox.removeActionListener(sessionsComboListener);
                sessionComboBox.removePopupMenuListener(sessionsComboListener);
                ComboBoxModel model = sessionComboBox.getModel();
                sessionComboBox.removeAllItems();
                DebuggerManager dm = DebuggerManager.getDebuggerManager();
                Session[] sessions = dm.getSessions();
                for (int x = 0; x < sessions.length; x++) {
                    if (isDebuggingSession(sessions[x])) {
                        sessionComboBox.addItem(new SessionItem(sessions[x]));
                    }
                }
                if (model.getSize() == 0) {
                    sessionComboBox.addItem(new SessionItem(null));
                }
                sessionComboBox.setSelectedItem(new SessionItem(dm.getCurrentSession()));
                sessionComboBox.setVisible(model.getSize() > 1);
                sessionComboBox.addActionListener(sessionsComboListener);
                sessionComboBox.addPopupMenuListener(sessionsComboListener);
            }
        });
    }

    void makeThreadCurrent(DVThread thread) {
        threadMadeCurrentRef = new WeakReference<>(thread);
        thread.makeCurrent();
    }

    private void createTreeView() {
        synchronized (lock) {
            releaseTreeView();
            treeView = new DebugTreeView();
            treeView.setRootVisible(false);
            treeView.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            treeView.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DebuggingViewComponent.class, "DebuggingViewComponent.treeView.AccessibleContext.accessibleName")); // NOI18N
            treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DebuggingViewComponent.class, "DebuggingViewComponent.treeView.AccessibleContext.accessibleDescription")); // NOI18N
            treeView.getTree().addMouseWheelListener(this);
            treeView.addTreeExpansionListener(this);
            TreeModel model = treeView.getTree().getModel();
            model.addTreeModelListener(this);
            treeView.getViewport().addChangeListener(this);
            treeView.getTree().setScrollsOnExpand(false);
            mainPanel.add(treeView, BorderLayout.CENTER);
        }
    }

    private void releaseTreeView() {
        synchronized (lock) {
            if (treeView == null) {
                return ;
            }
            treeView.getTree().removeMouseWheelListener(this);
            treeView.removeTreeExpansionListener(this);
            TreeModel model = treeView.getTree().getModel();
            model.removeTreeModelListener(this);
            treeView.getViewport().removeChangeListener(this);
            treeView.resetSelection();
            mainPanel.remove(treeView);
            treeView = null;
        }
    }

    private DebugTreeView getTreeView() {
        return treeView;
    }

    @Override
    protected void validateTree() {
        ignoreScrollAdjustment = true;
        try {
            super.validateTree();
        } finally {
            ignoreScrollAdjustment = false;
        }
    }
    
    // **************************************************************************
    // implementation of TreeExpansion and TreeModel listener
    // **************************************************************************
    
    @Override
    public void treeExpanded(TreeExpansionEvent event) {
        TreePath path = event.getPath();
        checkIfWeShouldScrollToCurrentThread(path);
        refreshView();
    }

    /**
     * Check whether we should scroll to the current thread.
     *
     * @param path Path of node that has been just expanded, or that has new
     * children.
     */
    private void checkIfWeShouldScrollToCurrentThread(TreePath path) {
        Node node = Visualizer.findNode(path.getLastPathComponent());
        if (node == null) {
            return;
        }
        DVThread dvThread = node.getLookup().lookup(DVThread.class);
        DVThread currentThread;
        synchronized (lock) {
            currentThread = (debugger != null) ? debugger.getCurrentThread() : null;
        }
        if (currentThread != null && currentThread == dvThread) {
            threadToScrollRef = new WeakReference<DVThread>(dvThread);
        }
    }

    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
        refreshView();
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        refreshView();
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        TreePath path = e.getTreePath();
        checkIfWeShouldScrollToCurrentThread(path);
        refreshView(true);
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        refreshView(true);
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        refreshView();
    }
    
    // **************************************************************************
    
    InfoPanel getInfoPanel() {
        return infoPanel;
    }
    
    void refreshView() {
        refreshView(false);
    }
    
    private boolean isDelayScrollWithMarkingDirtyRegion = false;
    
    private void refreshView(boolean delayScrollWithMarkingDirtyRegion) {
        if (delayScrollWithMarkingDirtyRegion) {
            isDelayScrollWithMarkingDirtyRegion = true;
        }
        if (refreshScheduled.getAndSet(true)) {
            return;
        }
        // Delay refresh to the next event queue cycle so that any components changes are processed.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshScheduled.set(false);
                restoreScrollPosition(isDelayScrollWithMarkingDirtyRegion);
                isDelayScrollWithMarkingDirtyRegion = false;
                requestProcessor.post(new Runnable() {
                    @Override
                    public void run() {
                        DVThread currentThread = null;
                        Set<Deadlock> deadlocks = null;
                        DVSupport supp;
                        synchronized (DebuggingViewComponent.this.lock) {
                            supp = debugger;
                        }
                        if (supp != null) {
                            currentThread = supp.getCurrentThread();
                            deadlocks = supp.getDeadlocks();
                        }
                        // collect all deadlocked threads
                        Set<DVThread> deadlockedThreads;
                        if (deadlocks == null) {
                            deadlockedThreads = Collections.emptySet();
                        } else {
                            deadlockedThreads = new HashSet<DVThread>();
                            for (Deadlock deadlock : deadlocks) {
                                deadlockedThreads.addAll(deadlock.getThreads());
                            }
                        }
                        viewRefresher.setup(currentThread, deadlockedThreads);
                        try {
                            SwingUtilities.invokeAndWait(viewRefresher);
                        } catch (InterruptedException ex) {
                        } catch (InvocationTargetException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }, 20);
            }
        });
    }

    private void adjustTreeScrollBar(int treeViewWidth) {
        DebugTreeView tView = getTreeView();
        if (tView == null) {
            scrollBarPanel.setVisible(false);
            return;
        }
        JViewport viewport = tView.getViewport();
        Point point = viewport.getViewPosition();
        if (point.y < 0) {
            viewport.setViewPosition(new Point(point.x, 0));
        }
        Dimension viewSize = viewport.getExtentSize();
        Dimension treeSize = viewport.getViewSize();
        if (treeViewWidth < 0) {
            treeViewWidth = treeSize.width;
        }
        int unitHeight = tView.getUnitHeight();
        if (unitHeight > 0) {
            JScrollBar sbar = mainScrollPane.getVerticalScrollBar();
            if (sbar.getUnitIncrement() != unitHeight) {
                sbar.setUnitIncrement(unitHeight);
            }
        }
        if (treeViewWidth <= viewSize.width) {
            scrollBarPanel.setVisible(false);
        } else {
            treeScrollBar.setMaximum(treeViewWidth);
            treeScrollBar.setVisibleAmount(viewSize.width);
            if (unitHeight > 0) {
                treeScrollBar.setUnitIncrement(unitHeight / 2);
            }
            treeScrollBar.setBlockIncrement(viewSize.width);
            scrollBarPanel.setVisible(true);
        } // else
    }
    
    // **************************************************************************
    // implementation of AdjustmentListener (listens on horizontal scrollbar
    // connected to treeView)
    // **************************************************************************
    
    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        DebugTreeView tView = getTreeView();
        if (tView == null) {
            return;
        }
        JViewport viewport = tView.getViewport();
        Point position = viewport.getViewPosition();
        viewport.setViewPosition(new Point(e.getValue(), position.y));
    }
    
    // **************************************************************************
    // implementation of ChangeListener on treeView
    // **************************************************************************
    
    @Override
    public void stateChanged(ChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                adjustTreeScrollBar(-1);
            }
        });
    }

    // **************************************************************************
    // implementation of MouseWheelListener on treeView
    // **************************************************************************
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        JScrollBar scrollBar = mainScrollPane.getVerticalScrollBar();
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int totalScrollAmount = e.getUnitsToScroll() * scrollBar.getUnitIncrement();
            scrollBar.setValue(scrollBar.getValue() + totalScrollAmount);
        }
    }

    @Override
    public void modelUpdated(CompoundModel compoundModel, DebuggerEngine de) {
        setRootContext(compoundModel, de);
    }
    
    /**
     * Store first visible node and its offset. Called when the view is scrolled
     * by the user.
     */
    private void storeScrollPosition() {
        JTree tree = getJTree();
        if (tree != null) {
            int scrollTop = mainScrollPane.getViewport().getViewPosition().y;
            int row = tree.getClosestRowForLocation(tree.getRowBounds(0).x + 1,
                    scrollTop);
            if (row >= 0) {
                TreePath path = tree.getPathForRow(row);
                if (path != null) {
                    int offset = tree.getRowBounds(row).y - scrollTop;
                    visibleTreePosition = new VisibleTreePosition(
                            path, offset);
                    return;
                }
            } else {
                return;
            }
        }
        visibleTreePosition = null;
    }

    /**
     * Store visible offset of an important node. Called when the view is
     * scrolled by ViewRefresher
     *
     * @param path Path to a node that should stay visible at the same position.
     */
    private void storeScrollPosition(TreePath path) {
        JTree tree = getJTree();
        if (tree != null && path != null) {
            int row = tree.getRowForPath(path);
            if (row >= 0) {
                int scrollTop = mainScrollPane.getViewport().getViewPosition().y;
                int offset = tree.getRowBounds(row).y - scrollTop;
                visibleTreePosition = new VisibleTreePosition(
                        path, offset);
                return;
            } else {
                return;
            }
        }
        visibleTreePosition = null;
    }

    private JTree getJTree() {
        DebugTreeView treeView1 = getTreeView();
        if (treeView1 != null) {
            JTree tree = treeView1.getTree();
            if (tree != null && tree.getRowCount() > 0) {
                return tree;
            }
        }
        return null;
    }

    /**
     * Restore stored scroll position.
     */
    private void restoreScrollPosition(boolean delayScrollWithMarkingDirtyRegion) {
        if (visibleTreePosition != null) {
            JTree tree = getJTree();
            if (tree != null) {
                int row = tree.getRowForPath(visibleTreePosition.getPath());
                if (row != -1) {
                    Rectangle bounds = tree.getRowBounds(row);
                    if (bounds != null) {
                        int scrollY = bounds.y - visibleTreePosition.getOffset();
                        JViewport viewport = mainScrollPane.getViewport();
                        Rectangle rect = viewport.getViewRect();
                        rect.y = scrollY;
                        if (!rect.isEmpty()) {
                            JComponent view = (JComponent) viewport.getView();
                            if (delayScrollWithMarkingDirtyRegion) {
                                RepaintManager.currentManager(viewport).addDirtyRegion(
                                        view,
                                        rect.x, rect.x, rect.width, rect.height);
                            }
                            ignoreScrollAdjustment = true;
                            try {
                                view.scrollRectToVisible(
                                        rect);
                            } finally {
                                ignoreScrollAdjustment = false;
                            }
                        }
                    }
                }
            }
        }
    }

    // **************************************************************************
    // inner classes
    // **************************************************************************
    
    private final class DebuggingPreferenceChangeListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (FiltersDescriptor.SHOW_SUSPEND_TABLE.equals(key)) {
                setSuspendTableVisible(evt.getNewValue().equals("true"));
            }
        }

    }

    private final class ViewRefresher implements Runnable {
        
        private DVThread currentThread;
        private Set<DVThread> deadlockedThreads;
        
        void setup(DVThread currentThread, Set<DVThread> deadlockedThreads) {
            this.currentThread = currentThread;
            this.deadlockedThreads = deadlockedThreads;
        }

        @Override
        public void run() {
            DebugTreeView tView = getTreeView();
            leftPanel.clearBars();
            rightPanel.startReset();
            int sx = (rightPanel.getWidth() - ClickableIcon.CLICKABLE_ICON_WIDTH) / 2;
            int sy = 0;

            DVThread threadToScroll = threadToScrollRef != null ? threadToScrollRef.get() : null;
            threadToScrollRef = null;
            int scrollStart = -1, scrollEnd = -1;

            int mainPanelHeight = 0;
            int treeViewWidth = 0;
            int leftBarHeight = 0;
            boolean isCurrent = false;
            boolean isAtBreakpoint = false;
            boolean isInDeadlock = false;
            Object currentObject = null;
            int currentSY = 0;
            int height = 0;
            TreePath scrollPath = null;

            if (tView != null) {
                for (TreePath path : tView.getVisiblePaths()) {
                    Node node = Visualizer.findNode(path.getLastPathComponent());
                    DVThread dvThread = node.getLookup().lookup(DVThread.class);
                    DVThreadGroup dvThreadGroup = node.getLookup().lookup(DVThreadGroup.class);

                    JTree tree = tView.getTree();
                    Rectangle rect = tree.getRowBounds(tree.getRowForPath(path));
                    height = rect != null ? (int) Math.round(rect.getHeight()) : 0;

                    if (dvThread != null || dvThreadGroup != null) {
                        if (dvThread == threadToScroll) {
                            scrollStart = mainPanelHeight;
                            scrollEnd = scrollStart + height;
                            scrollPath = path;
                        }
                        if (currentObject != null) {
                            addPanels(currentObject, isCurrent, isAtBreakpoint, isInDeadlock,
                                    leftBarHeight, sx, currentSY, height);
                        }
                        leftBarHeight = 0;
                        if (dvThread != null) {
                            isCurrent = dvThread == currentThread;/* && (dvThread.isSuspended() ||
                                    DebuggingTreeModel.isMethodInvoking(dvThread));*/
                            if (threadsListener != null) {
                                isAtBreakpoint = threadsListener.isBreakpointHit(dvThread);
                            }
                            isInDeadlock = deadlockedThreads.contains(dvThread);
                        } else {
                            isCurrent = false;
                            isAtBreakpoint = false;
                            isInDeadlock = false;
                        }
                        currentObject = dvThread != null ? dvThread : dvThreadGroup;
                        currentSY = sy;
                    }

                    mainPanelHeight += height;
                    treeViewWidth = rect != null ? Math.max(treeViewWidth, (int) Math.round(rect.getX() + rect.getWidth())) : treeViewWidth;
                    leftBarHeight += height;
                    sy += height;

                    if (scrollPath != null && scrollPath.equals(path.getParentPath())) {
                        scrollEnd += height;
                    }
                } // for
            } // if
            if (currentObject != null) {
                addPanels(currentObject, isCurrent, isAtBreakpoint, isInDeadlock,
                        leftBarHeight, sx, currentSY, height);
            }

            rightPanel.endReset();
            leftPanel.repaint();
            rightPanel.revalidate();
            rightPanel.repaint();
            if (tView != null) {
                tView.getTree().setPreferredSize(new Dimension(treeViewWidth, 0));
            }
            mainPanel.setPreferredSize(new Dimension(0, mainPanelHeight));
            mainScrollPane.revalidate();
            mainPanel.revalidate();
            if (tView != null) {
                tView.repaint();
            }

            adjustTreeScrollBar(treeViewWidth);
            if (scrollStart > -1) {
                JViewport viewport = mainScrollPane.getViewport();
                int aRectHeight = Math.min(scrollEnd - scrollStart + 1, viewport.getHeight());
                Rectangle aRect = new Rectangle(0, scrollStart, 1, aRectHeight);
                if (!aRect.isEmpty()) {
                    ignoreScrollAdjustment = true;
                    try {
                        ((JComponent)viewport.getView()).scrollRectToVisible(aRect);
                        storeScrollPosition(scrollPath);
                    } finally {
                        ignoreScrollAdjustment = false;
                    }
                }
            } else {
                restoreScrollPosition(false);
            }
        }

        private void addPanels(Object dvObject, boolean current, boolean atBreakpoint,
                boolean inDeadlock, int height, int sx, int sy, int rowHeight) {
            if (current || atBreakpoint || inDeadlock) {
                leftPanel.addBar(current, atBreakpoint, inDeadlock, height, sy);
            }
            if (dvObject instanceof DVThread) {
                rightPanel.addIcon(sx, sy, rowHeight, (DVThread) dvObject);
            }
        }
    }
    
    private class BarsPanel extends JPanel implements MouseMotionListener {
        
        private ArrayList<Bar> bars = new ArrayList<Bar>();

        BarsPanel() {
            setBackground(treeBackgroundColor);
            setPreferredSize(new Dimension(BAR_WIDTH, 0));
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
            addMouseMotionListener(this);
        }

        public void clearBars() {
            bars.clear();
        }

        public void addBar(boolean isCurrent, boolean atBreakpoint, boolean inDeadlock, int height, int sy) {
            String toolTipText = null;
            Color color = null;
            Color secondaryColor = null;
            if (inDeadlock) {
                color = deadlockColor;
                toolTipText = NbBundle.getMessage(DebuggingViewComponent.class, "LBL_DEADLOCKED_THREAD_TIP");
            } else if (isCurrent) {
                color = greenBarColor;
                toolTipText = NbBundle.getMessage(DebuggingViewComponent.class, "LBL_CURRENT_BAR_TIP");
            } else if (atBreakpoint) {
                color = hitsBarColor;
                toolTipText = NbBundle.getMessage(DebuggingViewComponent.class, "LBL_BREAKPOINT_HIT_TIP");
            }
            if (isCurrent && inDeadlock) {
                secondaryColor = greenBarColor;
                toolTipText = NbBundle.getMessage(DebuggingViewComponent.class, "LBL_CURRENT_DEADLOCKED_TIP");
            }
            Bar bar = new Bar(sy, height, color, secondaryColor, toolTipText);
            bars.add(bar);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Color originalColor = g.getColor();
            Rectangle clipRect = g.getClipBounds();
            double clipYStart = clipRect.getY();
            double clipYEnd = clipYStart + clipRect.getHeight() - 1;
            int width = (int)getBounds().getWidth();
            for (Bar bar : bars) {
                if (bar.sy + bar.height - 1 < clipYStart) {
                    continue;
                }
                if (bar.sy > clipYEnd) {
                    break;
                }
                if (bar.color != null) {
                    Rectangle rect = new Rectangle (0, bar.sy + 1, width, bar.height - 1);
                    rect = rect.intersection(clipRect);
                    g.setColor(bar.color);
                    g.fillRect(rect.x, rect.y, rect.width, rect.height);
                }
                if (bar.secondaryColor != null) {
                    Rectangle rect = new Rectangle (width / 2 - 1, bar.sy + 1, (width + 1) / 2 + 1, bar.height - 1);
                    rect = rect.intersection(clipRect);
                    if (!rect.isEmpty()) {
                        g.setColor(bar.secondaryColor);
                        g.fillRect(rect.x, rect.y, rect.width, rect.height);
                    }
                }
            } // for
            g.setColor(originalColor);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            computeToolTipText(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            computeToolTipText(e);
        }

        private void computeToolTipText(MouseEvent evt) {
            int sy = evt.getY();
            try {
                for (Bar bar : bars) {
                    if (sy >= bar.sy && sy < bar.sy + bar.height) {
                        setToolTipText(bar.toolTipText);
                        return;
                    }
                }
            } catch (ConcurrentModificationException e) {
            }
            setToolTipText(null);
        }
        
        private class Bar {
            private int sy;
            private int height;
            private Color color;
            private Color secondaryColor = null;
            private String toolTipText;

            Bar(int sy, int height, Color color, Color secondary, String toolTipText) {
                this.sy = sy;
                this.height = height;
                this.color = color;
                this.secondaryColor = secondary;
                this.toolTipText = toolTipText;
            }
        }
        
    }
    
    private class IconsPanel extends JPanel {
        
        private int endSY;
        private int counter;
        private ArrayList<IconItem> icons = new ArrayList<IconItem>();
        
        IconsPanel() {
            setBackground(treeBackgroundColor);
            setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, 0));
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        }

        public void startReset() {
            counter = 0;
            endSY = 0;
        }
        
        public void endReset() {
            int size = icons.size();
            for (int x = size - 1; x >= counter; x--) {
                remove(x);
                icons.remove(x);
            }
        }
        
        public void addIcon(int sx, int sy, int rowHeight, DVThread dvThread) {
            int height = sy - endSY + rowHeight;
            IconItem item;
            if (counter < icons.size()) {
                item = icons.get(counter);
                item.icon.changeThread(dvThread, DebuggingViewComponent.this, sx, sy,
                        ClickableIcon.CLICKABLE_ICON_WIDTH, height);
                item.panel.setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, height));
                item.height = height;
            } else {
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBackground(treeBackgroundColor);
                panel.setOpaque(false);
                panel.setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, height));
            
                ClickableIcon icon = new ClickableIcon(resumeIcon, focusedResumeIcon, pressedResumeIcon,
                        suspendIcon, focusedSuspendIcon, pressedSuspendIcon, dvThread, treeView);
                icon.setBackground(treeBackgroundColor);
                
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                int delta = rowHeight - ClickableIcon.CLICKABLE_ICON_HEIGHT;
                int insetTop = delta / 2;
                int insetBottom = delta - insetTop;
                gridBagConstraints.insets = new Insets(insetTop, 0, insetBottom, 0);
                
                panel.add(icon, gridBagConstraints);
                icon.initializeState(DebuggingViewComponent.this, sx, sy, ClickableIcon.CLICKABLE_ICON_WIDTH, height);
                
                item = new IconItem(height, icon, panel);
                icons.add(item);
                add(panel);
            }
            counter++;
            endSY += height;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            DebugTreeView tView = getTreeView();
            if (tView != null) {
                tView.paintStripes(g, this);
            }
        }
        
        private class IconItem {
            private int height;
            private ClickableIcon icon;
            private JPanel panel;
            
            IconItem(int height, ClickableIcon icon, JPanel panel) {
                this.height = height;
                this.icon = icon;
                this.panel = panel;
            }
        }
        
    }
    
    private class SessionsComboBoxListener implements ActionListener, PopupMenuListener {

        SessionItem selectedItem = null;
        boolean popupVisible = false;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            SessionItem si = (SessionItem)sessionComboBox.getSelectedItem();
            if (popupVisible) {
                selectedItem = si;
            } else {
                changeSession(si);
            }
        }

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            popupVisible = true;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            changeSession(selectedItem);
            selectedItem = null;
            popupVisible = false;
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
            selectedItem = null;
            popupVisible = false;
        }
        
        private void changeSession(SessionItem si) {
            if (si != null) {
                Session ses = si.getSession();
                DebuggerManager dm = DebuggerManager.getDebuggerManager();
                if (ses != null && ses != dm.getCurrentSession()) {
                    dm.setCurrentSession(ses);
                }
            }
        }
        
    }
    
    private class SessionItem {
        
        private Session session;

        SessionItem(Session session) {
            this.session = session;
        }
        
        public Session getSession() {
            return session;
        }

        @Override
        public String toString() {
            if (session != null) {
                return session.getName();
            } else {
                return '<' + NbBundle.getMessage(DebuggingViewComponent.class, "LBL_No_Session_Running") + '>';
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SessionItem)) {
                return false;
            }
            Session s = ((SessionItem)obj).getSession();
            if (session == null) {
                return s == null;
            } else {
                return session.equals(s);
            }
        }

        @Override
        public int hashCode() {
            return 29 * 3 + (this.session != null ? this.session.hashCode() : 0);
        }

    }

    private class VisibleTreePosition {

        final TreePath path;
        final int offset;

        public VisibleTreePosition(TreePath path, int offset) {
            this.path = path;
            this.offset = offset;
        }

        public TreePath getPath() {
            return path;
        }

        public int getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return "VisibleTreePosition[" + path + ", " + offset + "]"; //NOI18N
        }
    }
}
