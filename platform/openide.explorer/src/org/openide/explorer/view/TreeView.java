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
package org.openide.explorer.view;

import java.awt.Component;
import java.awt.Container;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.UIResource;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.awt.QuickSearch;


/**
 * Base class for tree-style explorer views. 
 * @see BeanTreeView
 * @see ContextTreeView
 */
public abstract class TreeView extends JScrollPane {
    static {
        // Workaround for issue #42794 on JDK1.5
        UIManager.put("Tree.scrollsHorizontallyAndVertically", Boolean.TRUE);
    }

    //
    // static fields
    //

    static final Logger LOG = Logger.getLogger(TreeView.class.getName());

    /** generated Serialized Version UID */
    static final long serialVersionUID = -1639001987693376168L;

    /** How long it takes before collapsed nodes are released from the tree's cache
    */
    private static final int TIME_TO_COLLAPSE = (System.getProperty("netbeans.debug.heap") != null) ? 0 : 15000;

    /** Minimum width of this component. */
    private static final int MIN_TREEVIEW_WIDTH = 400;

    /** Minimum height of this component. */
    private static final int MIN_TREEVIEW_HEIGHT = 400;

    //
    // components
    //

    /** Main <code>JTree</code> component. */
    protected transient JTree tree;

    /** model */
    transient NodeTreeModel treeModel;

    /** Explorer manager, valid when this view is showing */
    transient ExplorerManager manager;

    // Attributes

    /** Mouse and action listener. */
    transient PopupSupport defaultActionListener;

    /** Property indicating whether the default action is enabled. */
    transient boolean defaultActionEnabled;

    /** not null if popup menu enabled */
    transient PopupAdapter popupListener;

    /** the most important listener (on four types of events */
    transient TreePropertyListener managerListener = null;

    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;

    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;

    /** true if drag support is active */
    private transient boolean dragActive = true;

    /** true if drop support is active */
    private transient boolean dropActive = true;

    /** Drag support */
    transient TreeViewDragSupport dragSupport;

    /** Drop support */
    transient TreeViewDropSupport dropSupport;
    transient boolean dropTargetPopupAllowed = true;
    
    // default DnD actions
    private transient int allowedDragActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    private transient int allowedDropActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    
    /** Quick Search support */
    private transient QuickSearch qs;
    
    /** wait cursor is shown automatically during expanding */
    private transient boolean autoWaitCursor = true;

    /** Holds VisualizerChildren for all visible nodes */
    private final VisualizerHolder visHolder = new VisualizerHolder();
    /** reference to the last visible search field */
    private static Reference<TreeView> lastSearchField = new WeakReference<TreeView>(null);

    private transient boolean removedNodeWasSelected = false;

    /** Constructor.
    */
    public TreeView() {
        this(true, true);
    }

    /** Constructor.
    * @param defaultAction should double click on a node open its default action?
    * @param popupAllowed should right-click open popup?
    */
    public TreeView(boolean defaultAction, boolean popupAllowed) {
        setLayout(new ExplorerScrollPaneLayout());

        initializeTree();

//        // activation of drop target
//        if (DragDropUtilities.dragAndDropEnabled) {
//            setdroptExplorerDnDManager.getDefault().addFutureDropTarget(this);
//
//            // note: drag target is activated on focus gained
//        }
        setDropTarget(DragDropUtilities.dragAndDropEnabled);

        setPopupAllowed(popupAllowed);
        setDefaultActionAllowed(defaultAction);

        Dimension dim = null;

        try {
            dim = getPreferredSize();

            if (dim == null) {
                dim = new Dimension(MIN_TREEVIEW_WIDTH, MIN_TREEVIEW_HEIGHT);
            }
        } catch (NullPointerException npe) {
            dim = new Dimension(MIN_TREEVIEW_WIDTH, MIN_TREEVIEW_HEIGHT);
        }

        if (dim.width < MIN_TREEVIEW_WIDTH) {
            dim.width = MIN_TREEVIEW_WIDTH;
        }

        if (dim.height < MIN_TREEVIEW_HEIGHT) {
            dim.height = MIN_TREEVIEW_HEIGHT;
        }

        setPreferredSize(dim);
    }

    @Override
    public void updateUI() {
        Set<VisualizerChildren> tmp = visHolder;
        if (tmp != null) {
            tmp.clear();
        }

        super.updateUI();

        //On GTK L&F, the viewport border must be set to empty (not null!) or we still get border buildup
        setViewportBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    /** Initializes the tree & model.
     * [dafe] Horrible technique - overridable method called from constructor
     * may result in subclass code invoked when this object is not fully
     * constructed.
     * However I don't have enough knowledge about this code to change it.
    */
    void initializeTree() {
        // initilizes the JTree
        treeModel = createModel();
        treeModel.addView(this);

        tree = new ExplorerTree(treeModel);

        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        NodeRenderer rend = new NodeRenderer();
        tree.setCellRenderer(rend);
        tree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
        setViewportView(tree);

        // Init of the editor
        tree.setCellEditor(new TreeViewCellEditor(tree));
        tree.setEditable(true);
        tree.setInvokesStopCellEditing(true);
        int rowHeight = rend.getTreeCellRendererComponent(tree, null, false, false, false, 0, true).getPreferredSize().height;
        tree.setRowHeight(rowHeight);
        tree.setLargeModel(true);

        // set selection mode to DISCONTIGUOUS_TREE_SELECTION as default
        setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(tree);

        // init listener & attach it to closing of
        managerListener = new TreePropertyListener();
        tree.addTreeExpansionListener(managerListener);
        tree.addTreeWillExpandListener(managerListener);

        // do not care about focus
        setRequestFocusEnabled(false);

        defaultActionListener = new PopupSupport();
        getInputMap( JTree.WHEN_FOCUSED ).put( 
                KeyStroke.getKeyStroke( KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction" );
        getActionMap().put("org.openide.actions.PopupAction", defaultActionListener.popup);
        tree.addFocusListener(defaultActionListener);
        tree.addMouseListener(defaultActionListener);
    }

    /** Is it permitted to display a popup menu?
     * @return <code>true</code> if so
     */
    public boolean isPopupAllowed() {
        return popupListener != null && isShowing() && isDisplayable();
    }

    /** Enable/disable displaying popup menus on tree view items.
    * Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed(boolean value) {
        if ((popupListener == null) && value) {
            // on
            popupListener = new PopupAdapter();
            tree.addMouseListener(popupListener);

            return;
        }

        if ((popupListener != null) && !value) {
            // off
            tree.removeMouseListener(popupListener);
            popupListener = null;

            return;
        }
    }

    void setDropTargetPopupAllowed(boolean value) {
        dropTargetPopupAllowed = value;

        if (dropSupport != null) {
            dropSupport.setDropTargetPopupAllowed(value);
        }
    }

    boolean isDropTargetPopupAllowed() {
        return (dropSupport != null) ? dropSupport.isDropTargetPopupAllowed() : dropTargetPopupAllowed;
    }

    /** Does a double click invoke the default node action?
     * @return <code>true</code> if so
     */
    public boolean isDefaultActionEnabled() {
        return defaultActionEnabled;
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public void requestFocus() {
        tree.requestFocus();
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow() {
        return tree.requestFocusInWindow();
    }

    /** Enable/disable double click to invoke default action.
     * If defaultAction is not enabled double click expand/collapse node.
     * @param value <code>true</code> to enable
     */
    public void setDefaultActionAllowed(boolean value) {
        defaultActionEnabled = value;

        if (value) {
            tree.registerKeyboardAction(
                defaultActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED
            );
        } else {
            // Switch off.
            tree.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        }
    }

    /**
    * Is the root node of the tree displayed?
    *
    * @return <code>true</code> if so
    */
    public boolean isRootVisible() {
        return tree.isRootVisible();
    }

    /** Set whether or not the root node from
    * the <code>TreeModel</code> is visible.
    *
    * @param visible <code>true</code> if it is to be displayed
    */
    public void setRootVisible(boolean visible) {
        tree.setRootVisible(visible);
        tree.setShowsRootHandles(!visible);
    }


    /**
     * Set the <code>scrollsOnExpand</code> property on the
     * underlying tree component.
     * @see javax.swing.JTree#setScrollsOnExpand(boolean)
     *
     * @param newValue the new value of the property
     * @since 6.73
     */
    public void setScrollsOnExpand(boolean newValue) {
        tree.setScrollsOnExpand(newValue);
    }

    /**
     * Returns the value of the <code>scrollsOnExpand</code> property of
     * the underlying tree component.
     *
     * @return the value of the <code>scrollsOnExpand</code> property
     * @since 6.73
     */
    public boolean getScrollsOnExpand() {
        return tree.getScrollsOnExpand();
    }

    /**
     * Test whether the quick search feature is enabled or not.
     * Default is enabled (true).
     * @since 6.33
     * @return true if quick search feature is enabled, false otherwise.
     */
    public boolean isQuickSearchAllowed() {
        return qs.isEnabled();
    }
    
    /**
     * Set whether the quick search feature is enabled or not.
     * @since 6.33
     * @param allowedQuickSearch <code>true</code> if quick search shall be enabled
     */
    public void setQuickSearchAllowed(boolean allowedQuickSearch) {
        qs.setEnabled(allowedQuickSearch);
    }

    
    /**
     * Set whether the quick search feature uses substring or prefix
     * matching for the typed characters. Defaults to prefix (false).
     * @since 6.11
     * @param useSubstring <code>true</code> if substring search is used in quick search
     * @deprecated Since 6.42 does nothing, as the quick search feature uses both substring and prefix
     * matching. All prefix matches for the typed characters will appear before substring
     * matches.
     */
    @Deprecated
    public void setUseSubstringInQuickSearch(boolean useSubstring) {
    }
    
    /********** Support for the Drag & Drop operations *********/
    /** Drag support is enabled by default.
    * @return true if dragging from the view is enabled, false
    * otherwise.
    */
    public boolean isDragSource() {
        return dragActive;
    }

    /** Enables/disables dragging support.
    * @param state true enables dragging support, false disables it.
    */
    public void setDragSource(boolean state) {
        // create drag support if needed
        if (state && (dragSupport == null)) {
            dragSupport = new TreeViewDragSupport(this, tree);
        }

        // activate / deactivate support according to the state
        dragActive = state;

        if (dragSupport != null && !GraphicsEnvironment.isHeadless()) {
            dragSupport.activate(dragActive);
        }
    }

    /** Drop support is enabled by default.
    * @return true if dropping to the view is enabled, false
    * otherwise<br>
    */
    public boolean isDropTarget() {
        return dropActive;
    }

    /** Enables/disables dropping support.
    * @param state true means drops into view are allowed,
    * false forbids any drops into this view.
    */
    public void setDropTarget(boolean state) {
        // create drop support if needed
        if (dropActive && (dropSupport == null)) {
            dropSupport = new TreeViewDropSupport(this, tree, dropTargetPopupAllowed);
        }

        // activate / deactivate support according to the state
        dropActive = state;

        if (dropSupport != null && !GraphicsEnvironment.isHeadless()) {
            dropSupport.activate(dropActive);
        }
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions (copy, move, link) are allowed by default.
    * @return int representing set of actions which are allowed when dragging from
    * asociated component.
     */
    public int getAllowedDragActions() {
        return allowedDragActions;
    }

    /** Sets allowed actions for dragging
    * @param actions new drag actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDragActions(int actions) {
        // PENDING: check parameters
        allowedDragActions = actions;
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions are allowed by default.
    * @return int representing set of actions which are allowed when dropping
    * into the asociated component.
    */
    public int getAllowedDropActions() {
        return allowedDropActions;
    }

    /** Sets allowed actions for dropping.
    * @param actions new allowed drop actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDropActions(int actions) {
        // PENDING: check parameters
        allowedDropActions = actions;
    }

    //
    // Control over expanded state
    //

    /** Collapses the tree under given node.
    *
    * @param n node to collapse
    */
    public void collapseNode(final Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {
            @Override
            public void run() {
                final TreePath path = getTreePath(n);
                LOG.log(Level.FINE, "collapseNode: {0} {1}", new Object[] { n, path });
                tree.collapsePath(path);
                LOG.fine("collapsePath done");
            }
        });
    }

    /** Expands the node in the tree. This method can be called outside
     * of AWT dispatch thread. It gets the children fro the node immediately,
     * and then switches to AWT via {@link EventQueue#invokeLater(java.lang.Runnable)}
     * and really expands the node
     *
     * @param n node
     * @exception IllegalArgumentException if the node is null
     */
    public void expandNode(final Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        lookupExplorerManager();
        final List<Node> prepare = n.getChildren().snapshot();
        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {
            @Override
            public void run() {
                LOG.log(Level.FINEST, "Just print the variable so it is not GCed: {0}", prepare);
                final TreePath p = getTreePath(n);
                LOG.log(Level.FINE, "expandNode: {0} {1}", new Object[] { n, p });
                tree.expandPath(p);
                LOG.fine("expandPath done");
            }
        });
    }

    /** Test whether a node is expanded in the tree or not
    * @param n the node to test
    * @return true if the node is expanded
    */
    public boolean isExpanded(Node n) {
        return tree.isExpanded(getTreePath(n));
    }

    /** Expands all paths.
    */
    public void expandAll() {
        try {
            tree.setUI(null);
            TreeNode root = (TreeNode) tree.getModel().getRoot();
            expandOrCollapseAll(new TreePath(root), true);
        } finally {
            tree.updateUI();
        }
    }

    private void expandOrCollapseAll(TreePath parent, boolean expand) {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() > 0) {
            for (Enumeration<? extends TreeNode> e = node.children(); e.hasMoreElements();) {
                TreeNode n = e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandOrCollapseAll(path, expand);
            }
        }
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    //
    // Processing functions
    //

    @Override
    public void validate() {
        Children.MUTEX.readAccess(new Runnable() {
            @Override
            public void run() {
                TreeView.super.validate();
            }
        });
    }

    /** Initializes the component and lookup explorer manager.
    */
    @Override
    public void addNotify() {
        super.addNotify();
        lookupExplorerManager();
    }

    /** Registers in the tree of components.
     */
    private void lookupExplorerManager() {
        // Enter key in the tree
        ExplorerManager newManager = ExplorerManager.find(TreeView.this);

        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener(wlvc);
                manager.removePropertyChangeListener(wlpc);
            }

            manager = newManager;

            manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(managerListener, manager));

            synchronizeRootContext();
            synchronizeExploredContext();
            synchronizeSelectedNodes();
        }

        // Sometimes the listener is registered twice and we get the 
        // selection events twice. Removing the listener before adding it
        // should be a safe fix.
        tree.getSelectionModel().removeTreeSelectionListener(managerListener);
        tree.getSelectionModel().addTreeSelectionListener(managerListener);
    }

    /** Deinitializes listeners.
    */
    @Override
    public void removeNotify() {
        super.removeNotify();

        tree.getSelectionModel().removeTreeSelectionListener(managerListener);
    }

    // *************************************
    // Methods to be overriden by subclasses
    // *************************************

    /** Allows subclasses to provide own model for displaying nodes.
    * @return the model to use for this view
    */
    protected abstract NodeTreeModel createModel();

    /** Called to allow subclasses to define the behaviour when a
    * node(s) are selected in the tree.
    *
    * @param nodes the selected nodes
    * @param em explorer manager to work on (change nodes to it)
    * @throws PropertyVetoException if the change cannot be done by the explorer
    *    (the exception is silently consumed)
    */
    protected abstract void selectionChanged(Node[] nodes, ExplorerManager em)
    throws PropertyVetoException;

    /** Called when explorer manager is about to change the current selection.
    * The view can forbid the change if it is not able to display such
    * selection.
    *
    * @param nodes the nodes to select
    * @return false if the view is not able to change the selection
    */
    protected abstract boolean selectionAccept(Node[] nodes);

    /** Show a given path in the screen. It depends on the kind of <code>TreeView</code>
    * if the path should be expanded or just made visible.
    *
    * @param path the path
    */
    protected abstract void showPath(TreePath path);

    /** Shows selection to reflect the current state of the selection in the explorer.
    *
    * @param paths array of paths that should be selected
    */
    protected abstract void showSelection(TreePath[] paths);

    /** Specify whether a context menu of the explored context should be used.
    * Applicable when no nodes are selected and the user wants to invoke
    * a context menu (clicks right mouse button).
    *
    * @return <code>true</code> if so; <code>false</code> in the default implementation
    */
    protected boolean useExploredContextMenu() {
        return false;
    }

    /** Check if selection of the nodes could break the selection mode set in TreeSelectionModel.
     * @param nodes the nodes for selection
     * @return true if the selection mode is broken */
    private boolean isSelectionModeBroken(Node[] nodes) {
        // if nodes are empty or single the everthing is ok
        // or if discontiguous selection then everthing ok
        if ((nodes.length <= 1) || (getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)) {
            return false;
        }

        // if many nodes
        // brakes single selection mode
        if (getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION) {
            return true;
        }

        // check the contiguous selection mode
        TreePath[] paths = new TreePath[nodes.length];
        RowMapper rowMapper = tree.getSelectionModel().getRowMapper();

        // if rowMapper is null then tree bahaves as discontiguous selection mode is set
        if (rowMapper == null) {
            return false;
        }

        ArrayList<Node> toBeExpaned = new ArrayList<Node>(3);

        for (int i = 0; i < nodes.length; i++) {
            toBeExpaned.clear();

            Node n = nodes[i];

            while (n.getParentNode() != null) {
                if (!isExpanded(n)) {
                    toBeExpaned.add(n);
                }

                n = n.getParentNode();
            }

            for (int j = toBeExpaned.size() - 1; j >= 0; j--) {
                expandNode(toBeExpaned.get(j));
            }
            paths[i] = getTreePath(nodes[i]);
        }

        int[] rows = rowMapper.getRowsForPaths(paths);

        // check selection's rows
        Arrays.sort(rows);

        for (int i = 1; i < rows.length; i++) {
            if (rows[i] != (rows[i - 1] + 1)) {
                return true;
            }
        }

        // all is ok
        return false;
    }
    
    TreePath getTreePath(Node node) {
        return new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, node)));
    }

    //
    // synchronizations
    //

    /** Called when selection in tree is changed.
    */
    final void callSelectionChanged(Node[] nodes) {
        manager.removePropertyChangeListener(wlpc);
        manager.removeVetoableChangeListener(wlvc);

        try {
            selectionChanged(nodes, manager);
        } catch (PropertyVetoException e) {
            synchronizeSelectedNodes();
        } finally {
            manager.addPropertyChangeListener(wlpc);
            manager.addVetoableChangeListener(wlvc);
        }
    }

    /** Synchronize the root context from the manager of this Explorer.
    */
    final void synchronizeRootContext() {
        // #151850 cancel editing before changing root node
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                TreeCellEditor cellEditor = tree.getCellEditor();
                if (cellEditor instanceof TreeViewCellEditor) {
                    ((TreeViewCellEditor) cellEditor).abortTimer();
                }
                tree.cancelEditing();
                final Node rc = manager.getRootContext();
                LOG.log(Level.FINE, "synchronizeRootContext {0}", rc);
                treeModel.setNode(rc, visHolder);
            }
        });
    }

    /** Synchronize the explored context from the manager of this Explorer.
    */
    final void synchronizeExploredContext() {
        final Node n = manager.getExploredContext();
        if (n == null) {
            return;
        }

        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {
            @Override
            public void run() {
                final TreePath tp = getTreePath(n);
                LOG.log(Level.FINE, "synchronizeExploredContext {0} path {1}", new Object[] { n, tp });
                showPath(tp);
            }
        });
    }

    /** Sets the selection model, which must be one of
     * TreeSelectionModel.SINGLE_TREE_SELECTION,
     * TreeSelectionModel.CONTIGUOUS_TREE_SELECTION or
     * TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION.
     * <p>
     * This may change the selection if the current selection is not valid
     * for the new mode. For example, if three TreePaths are
     * selected when the mode is changed to <code>TreeSelectionModel.SINGLE_TREE_SELECTION</code>,
     * only one TreePath will remain selected. It is up to the particular
     * implementation to decide what TreePath remains selected.
     * Note: TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION is set as default.
     * @since 2.15
     * @param mode selection mode
     */
    public void setSelectionMode(int mode) {
        tree.getSelectionModel().setSelectionMode(mode);
    }

    /** Returns the current selection mode, one of
     * <code>TreeSelectionModel.SINGLE_TREE_SELECTION</code>,
     * <code>TreeSelectionModel.CONTIGUOUS_TREE_SELECTION</code> or
     * <code>TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION</code>.
     * @since 2.15
     * @return selection mode
     */
    public int getSelectionMode() {
        return tree.getSelectionModel().getSelectionMode();
    }

    /**
     * Controls automatic setting of wait cursor when node is expanded
     * @param enable true if wait cursor should be shown automatically
     * @since 6.21
     */
    public void setAutoWaitCursor(boolean enable) {
        autoWaitCursor = enable;
    }

    //
    // showing and removing the wait cursor
    //
    private void showWaitCursor (boolean show) {
        JRootPane rPane = getRootPane();
        if (rPane == null) {
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            doShowWaitCursor(rPane.getGlassPane(), show);
        } else {
            SwingUtilities.invokeLater(new CursorR(rPane.getGlassPane(), show));
        }
    }

    private static void doShowWaitCursor (Component glassPane, boolean show) {
        if (show) {
            glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glassPane.setVisible(true);
        } else {
            glassPane.setVisible(false);
            glassPane.setCursor(null);
        }
    }

    private static class CursorR implements Runnable {
        private Component glassPane;
        private boolean show;

        private CursorR(Component cont, boolean show) {
            this.glassPane = cont;
            this.show = show;
        }

        @Override
        public void run() {
            doShowWaitCursor(glassPane, show);
        }
    }

    private void prepareWaitCursor(final Node node) {
        // check type of node
        if (node == null || !autoWaitCursor) {
            return;
        }

        showWaitCursor(true);
        // not sure whenter throughput 1 is OK...
        ViewUtil.uiProcessor().post(new Runnable() {
            @Override
            public void run() {
                try {
                    node.getChildren().getNodesCount(true);
                } catch (Exception e) {
                    // log a exception
                    LOG.log(Level.WARNING, null, e);
                } finally {
                    // show normal cursor above all
                    showWaitCursor(false);
                }
            }
        });
    }
    
   
    
    /** Synchronize the selected nodes from the manager of this Explorer.
    * The default implementation does nothing.
    */
    final void synchronizeSelectedNodes() {
        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {
            @Override
            public void run() {
                Node[] arr = manager.getSelectedNodes();
                TreePath[] paths = new TreePath[arr.length];
                final boolean log = LOG.isLoggable(Level.FINE);
                if (log) {
                    LOG.log(Level.FINE, "synchronizeSelectedNodes: {0}", arr.length);
                }
                for (int i = 0; i < arr.length; i++) {
                    paths[i] = getTreePath(arr[i]);
                    if (log) {
                        LOG.log(Level.FINE, "paths[{0}] = {1} node: {2}", new Object[]{i, paths[i], arr[i]});
                    }
                }

                tree.getSelectionModel().removeTreeSelectionListener(managerListener);
                showSelection(paths);
                tree.getSelectionModel().addTreeSelectionListener(managerListener);
                if (log) {
                    LOG.fine("synchronizeSelectedNodes done");
                }
            }
        });
    }

    void scrollTreeToVisible(TreePath path, TreeNode child) {
        Rectangle base = tree.getVisibleRect();
        Rectangle b1 = tree.getPathBounds(path);
        Rectangle b2 = tree.getPathBounds(new TreePath(treeModel.getPathToRoot(child)));

        if ((base != null) && (b1 != null) && (b2 != null)) {
            tree.scrollRectToVisible(new Rectangle(base.x, b1.y, 1, b2.y - b1.y + b2.height));
        }
    }

    private void createPopup(int xpos, int ypos, JPopupMenu popup) {
        if (popup.getSubElements().length > 0) {
            popup.show(TreeView.this, xpos, ypos);
        }
    }

    void createPopup(int xpos, int ypos) {
        // bugfix #23932, don't create if it's disabled
        if (isPopupAllowed()) {
            Node[] selNodes = manager.getSelectedNodes();

            if (selNodes.length > 0) {
                Action[] actions = NodeOp.findActions(selNodes);
                if (actions.length > 0) {
                    createPopup(xpos, ypos, Utilities.actionsToPopup(actions, this));
                }                
            } else if (manager.getRootContext() != null) {
                JPopupMenu popup = manager.getRootContext().getContextMenu();
                if (popup != null) {
                    createPopup(xpos, ypos, popup);
                }
            }                
        }
    }

    /* create standard popup menu and add newMenu to it
     */
    void createExtendedPopup(int xpos, int ypos, JMenu newMenu) {
        Node[] ns = manager.getSelectedNodes();
        JPopupMenu popup = null;

        if (ns.length > 0) {
            // if any nodes are selected --> find theirs actions
            Action[] actions = NodeOp.findActions(ns);
            popup = Utilities.actionsToPopup(actions, this);
        } else {
            // if none node is selected --> get context actions from view's root
            if (manager.getRootContext() != null) {
                popup = manager.getRootContext().getContextMenu();
            }
        }

        int cnt = 0;

        if (popup == null) {
            popup = SystemAction.createPopupMenu(new SystemAction[] {  });
        }

        popup.add(newMenu);

        createPopup(xpos, ypos, popup);
    }

    /** Returns the the point at which the popup menu is to be showed. May return null.
     * @return the point or null
     */
    Point getPositionForPopup() {
        int i = tree.getLeadSelectionRow();

        if (i < 0) {
            return null;
        }

        Rectangle rect = tree.getRowBounds(i);

        if (rect == null) {
            return null;
        }

        Point p = new Point(rect.x, rect.y);

        // bugfix #36984, convert point by TreeView.this
        p = SwingUtilities.convertPoint(tree, p, TreeView.this);

        return p;
    }

    static Action takeAction(Action action, Node ... nodes) {
        // bugfix #42843, use ContextAwareAction if possible
        if (action instanceof ContextAwareAction) {
            Lookup contextLookup = getLookupFor(nodes);

            Action contextInstance = ((ContextAwareAction) action).createContextAwareInstance(contextLookup);
            assert contextInstance != action : "Cannot be same. ContextAwareAction:  " + action +
            ", ContextAwareInstance: " + contextInstance;
            action = contextInstance;
        }

        return action;
    }
    
    private static Lookup getLookupFor(Node ... nodes) {
        if (nodes.length == 1) {
            Lookup contextLookup = nodes[0].getLookup ();
            Object o = contextLookup.lookup(nodes[0].getClass());
             // #55826, don't added the node twice
            if (!nodes[0].equals (o)) {
                 contextLookup = new ProxyLookup (new Lookup[] { Lookups.singleton (nodes[0]), contextLookup });
            }
            return contextLookup;
        } else {
            Lookup[] lkps = new Lookup[nodes.length];
            for (int i=0; i<nodes.length; i++) {
                lkps[i] = nodes[i].getLookup();
            }
            Lookup contextLookup = new ProxyLookup(lkps);
            Set<Node> toAdd = new HashSet<Node>(Arrays.asList(nodes));
            toAdd.removeAll(contextLookup.lookupAll(Node.class));

            if (!toAdd.isEmpty()) {
                contextLookup = new ProxyLookup(
                    contextLookup,
                    Lookups.fixed((Object[])toAdd.toArray(new Node[0])));
            }
            return contextLookup;
        }
    }

    /** Returns the tree path nearby to given tree node. Either a sibling if there is or the parent.
     * @param parentPath tree path to parent of changed nodes
     * @param childIndices indexes of changed children
     * @return the tree path or null if there no changed children
     */
    static TreePath findSiblingTreePath(TreePath parentPath, int[] childIndices) {
        if (childIndices == null) {
            throw new IllegalArgumentException("Indexes of changed children are null."); // NOI18N
        }

        if (parentPath == null) {
            throw new IllegalArgumentException("The tree path to parent is null."); // NOI18N
        }

        // bugfix #29342, if childIndices is the empty then don't change the selection
        if (childIndices.length == 0) {
            return null;
        }

        TreeNode parent = (TreeNode) parentPath.getLastPathComponent();
        Object[] parentPaths = parentPath.getPath();
        TreePath newSelection;
        
        int childCount = parent.getChildCount();
        if (childCount > 0) {
            // get parent path, add child to it
            int childPathLength = parentPaths.length + 1;
            Object[] childPath = new Object[childPathLength];
            System.arraycopy(parentPaths, 0, childPath, 0, parentPaths.length);

            int selectedChild = Math.min(childIndices[0], childCount-1);

            childPath[childPathLength - 1] = parent.getChildAt(selectedChild);
            newSelection = new TreePath(childPath);
        } else {
            // all children removed, select parent
            newSelection = new TreePath(parentPaths);
        }

        return newSelection;
    }

    // Workaround for JDK issue 6472844 (NB #84970)
    void removedNodes(List<VisualizerNode> removed) {
        TreeSelectionModel sm = tree.getSelectionModel();
	TreePath[] selPaths = (sm != null) ? sm.getSelectionPaths() : null;
        
        List<TreePath> remSel = null;
        for (VisualizerNode vn : removed) {
            visHolder.removeRecur(vn.getChildren(false));
            if (selPaths != null) {
                TreePath path = new TreePath(vn.getPathToRoot());
                for(TreePath tp : selPaths) {
                    if (path.isDescendant(tp)) {
                        if (remSel == null) {
                            remSel = new ArrayList<TreePath>();
                        }
                        remSel.add(tp);
                    }
                }
            }
        }
        
        removedNodeWasSelected = remSel != null;
        if (remSel != null) {
            try {
                sm.removeSelectionPaths(remSel.toArray(new TreePath[0]));
            } catch (NullPointerException e) {
                // if editing of label of removed node was in progress
                // BasicTreeUI will try to cancel editing and repaint node 
                // which fails because node is already removed so it cannot get bounds of it
                // catch and ignore (issue #136123)
            }
        }
    }

    /** Listens to the property changes on tree */
    class TreePropertyListener implements VetoableChangeListener, PropertyChangeListener, TreeExpansionListener,
        TreeWillExpandListener, TreeSelectionListener, Runnable {
        private RequestProcessor.Task scheduled;
        private TreePath[] readAccessPaths;
        
        TreePropertyListener() {
        }

        @Override
        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                // issue 11928 check if selecetion mode will be broken
                Node[] nodes = (Node[]) evt.getNewValue();

                if (isSelectionModeBroken(nodes)) {
                    throw new PropertyVetoException(
                        "selection mode " + getSelectionMode() + " broken by " + Arrays.asList(nodes), evt
                    ); // NOI18N
                }

                if (!selectionAccept(nodes)) {
                    throw new PropertyVetoException("selection " + Arrays.asList(nodes) + " rejected", evt); // NOI18N
                }
            }
        }

        public @Override final void propertyChange(PropertyChangeEvent evt) {
            if (manager == null) {
                return; // the tree view has been removed before the event got delivered
            }
            final String prop = evt.getPropertyName();
            if (!prop.equals(ExplorerManager.PROP_ROOT_CONTEXT) &&
                    !prop.equals(ExplorerManager.PROP_EXPLORED_CONTEXT) &&
                    !prop.equals(ExplorerManager.PROP_SELECTED_NODES)) {
                return;
            }
            Children.MUTEX.readAccess(new Runnable() {
                public @Override void run() {
                    if (prop.equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
                        synchronizeRootContext();
                    }

                    if (prop.equals(ExplorerManager.PROP_EXPLORED_CONTEXT)) {
                        synchronizeExploredContext();
                    }

                    if (prop.equals(ExplorerManager.PROP_SELECTED_NODES)) {
                        synchronizeSelectedNodes();
                    }
                }
            });
        }

        @Override
        public synchronized void treeExpanded(TreeExpansionEvent ev) {
            VisualizerNode vn = (VisualizerNode) ev.getPath().getLastPathComponent();
            visHolder.add(vn.getChildren());
            
            if (!tree.getScrollsOnExpand()) {
                return;
            }
            
            RequestProcessor.Task t = scheduled;

            if (t != null) {
                t.cancel();
            }

            class Request implements Runnable {
                private TreePath path;

                public Request(TreePath path) {
                    this.path = path;
                }

                @Override
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);
                        return;
                    }
                    if (!Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess()) {
                        Children.MUTEX.readAccess(this);
                        return;
                    }
                    try {
                        if (!tree.isVisible(path)) {
                            // if the path is not visible - don't check the children
                            return;
                        }

                        if (treeModel == null) {
                            // no model, no action, no problem
                            return;
                        }

                        TreeNode myNode = (TreeNode) path.getLastPathComponent();

                        if (treeModel.getPathToRoot(myNode)[0] != treeModel.getRoot()) {
                            // the way from the path no longer
                            // goes to the root, probably someone
                            // has removed the node on the way up
                            // System.out.println("different roots.");
                            return;
                        }

                        // show wait cursor
                        //showWaitCursor ();
                        int lastChildIndex = myNode.getChildCount() - 1;

                        if (lastChildIndex >= 0) {
                            TreeNode lastChild = myNode.getChildAt(lastChildIndex);

                            Rectangle base = tree.getVisibleRect();
                            Rectangle b1 = tree.getPathBounds(path);
                            Rectangle b2 = tree.getPathBounds(new TreePath(treeModel.getPathToRoot(lastChild)));

                            if ((base != null) && (b1 != null) && (b2 != null)) {
                                tree.scrollRectToVisible(new Rectangle(base.x, b1.y, 1, b2.y - b1.y + b2.height));
                            }

                            //                        scrollTreeToVisible(path, lastChild);
                        }
                    } finally {
                        path = null;
                    }
                }
            }

            // It is OK to use multithreaded shared RP as the requests
            // will be serialized in event queue later
            scheduled = ViewUtil.uiProcessor().post(new Request(ev.getPath()), 250); // hope that all children are there after this time
        }

        @Override
        public synchronized void treeCollapsed(final TreeExpansionEvent ev) {
            class Request implements Runnable {
                private TreePath path;

                public Request(TreePath path) {
                    this.path = path;
                }

                @Override
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);

                        return;
                    }

                    // we are delayed, another treeExpanded() could arrive meanwhile
                    boolean expanded = true;

                    try {
                        expanded = tree.isExpanded(path);
                        if (expanded) {
                            // the tree shows the path - do not collapse
                            // the tree
                            return;
                        }

                        if (!tree.isVisible(path)) {
                            // if the path is not visible do not collapse
                            // the tree
                            return;
                        }

                        if (treeModel == null) {
                            // no model, no action, no problem
                            return;
                        }

                        TreeNode myNode = (TreeNode) path.getLastPathComponent();

                        if (treeModel.getPathToRoot(myNode)[0] != treeModel.getRoot()) {
                            // the way from the path no longer
                            // goes to the root, probably someone
                            // has removed the node on the way up
                            // System.out.println("different roots.");
                            return;
                        }

                        treeModel.nodeStructureChanged(myNode);
                    } finally {
                        if (!expanded) {
                            VisualizerNode vn = (VisualizerNode) path.getLastPathComponent();
                            visHolder.removeRecur(vn.getChildren(false));
                        }
                        this.path = null;
                    }
                }
            }

            // It is OK to use multithreaded shared RP as the requests
            // will be serialized in event queue later
            // bugfix #37420, children of all collapsed folders will be throw out
            ViewUtil.uiProcessor().post(new Request(ev.getPath()), TIME_TO_COLLAPSE);
        }

        /* Called whenever the value of the selection changes.
        * @param ev the event that characterizes the change.
        */
        @Override
        public void valueChanged(TreeSelectionEvent ev) {
            TreePath[] paths = tree.getSelectionPaths();

            if (paths == null) {
                // part of bugfix #37279, if DnD is active then is useless select a nearby node
                if (ExplorerDnDManager.getDefault().isDnDActive()) {
                    return;
                }

                callSelectionChanged(new Node[0]);
            } else {
                // we need to force no changes to nodes hierarchy =>
                // we are requesting read request, but it is not necessary
                // to execute the next action immediatelly, so postReadRequest
                // should be enough
                readAccessPaths = paths;
                Children.MUTEX.postReadRequest(this);
            }
        }

        /** Called under Children.MUTEX to refresh the currently selected nodes.
        */
        @Override
        public void run() {
            if (readAccessPaths == null) {
                return;
            }

            TreePath[] paths = readAccessPaths;

            // non null value caused leak in
            // ComponentInspector
            // When the last Form was closed then the ComponentInspector was
            // closed as well. Since this variable was not null - 
            // last selected Node (RADComponentNode) was held ---> FormManager2 was held, etc.
            readAccessPaths = null;

            java.util.List<Node> ll = new java.util.ArrayList<Node>(paths.length);

            for (int i = 0; i < paths.length; i++) {
                Node n = Visualizer.findNode(paths[i].getLastPathComponent());
                n = getOriginalNode (n);

                if( isUnderRoot( manager.getRootContext(), n ) ) {
                    ll.add(n);
                }
            }
            callSelectionChanged(ll.toArray(new Node[0]));
        }

        /** Checks whether given Node is a subnode of rootContext.
        * @return true if specified Node is under current rootContext
        */
        private boolean isUnderRoot(Node rootContext, Node node) {
            while (node != null) {
                if (node.equals(rootContext)) {
                    return true;
                }

                node = node.getParentNode();
            }

            return false;
        }
            
        @Override
        public void treeWillCollapse(TreeExpansionEvent event)
        throws ExpandVetoException {
        }

        @Override
        public void treeWillExpand(TreeExpansionEvent event)
        throws ExpandVetoException {
            // prepare wait cursor and optionally show it
            TreePath path = event.getPath();
            prepareWaitCursor(DragDropUtilities.secureFindNode(path.getLastPathComponent()));
        }
    }
     // end of TreePropertyListener

    Node getOriginalNode (Node n) {
        return n;
    }

    /** Popup adapter.
    */
    class PopupAdapter extends MouseUtils.PopupMouseAdapter {
        PopupAdapter() {
        }

        @Override
        protected void showPopup(MouseEvent e) {
            tree.cancelEditing();
            int selRow = tree.getRowForLocation(e.getX(), e.getY());

            if ((selRow == -1) && !isRootVisible()) {
                // clear selection
                try {
                    manager.setSelectedNodes(new Node[]{});
                } catch (PropertyVetoException exc) {
                    assert false : exc; // not permitted to be thrown
                }
            } else if (!tree.isRowSelected(selRow)) {
                // This will set ExplorerManager selection as well.
                // If selRow == -1 the selection will be cleared.
                tree.setSelectionRow(selRow);
            }

            if ((selRow != -1) || !isRootVisible()) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), TreeView.this);

                createPopup((int) p.getX(), (int) p.getY());
            }
        }
    }

    final class PopupSupport extends MouseAdapter implements Runnable, FocusListener, ActionListener {
        public final Action popup = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    SwingUtilities.invokeLater(PopupSupport.this);
                }

                /**
                 * Returns true if the action is enabled.
                 *
                 * @return true if the action is enabled, false otherwise
                 * @see Action#isEnabled
                 */
            @Override
                public boolean isEnabled() {
                    return TreeView.this.isFocusOwner() || tree.isFocusOwner();
                }
            };

        //CallbackSystemAction csa;
        @Override
        public void run() {
            Point p = getPositionForPopup();

            if (p == null) {
                //we're going to create a popup menu for the root node
                p = new Point(0, 0);
            }

            createPopup(p.x, p.y);
        }

        @Override
        public void focusGained(java.awt.event.FocusEvent ev) {
            // unregister
            ev.getComponent().removeFocusListener(this);

            // lazy activation of drag source
            if (DragDropUtilities.dragAndDropEnabled && dragActive) {
                setDragSource(true);

                // note: dropTarget is activated in constructor
            }
        }

        @Override
        public void focusLost(FocusEvent ev) {
        }

        /* clicking adapter */
        @Override
        public void mouseClicked(MouseEvent e) {
            tree.stopEditing();
            int selRow = tree.getRowForLocation(e.getX(), e.getY());

            if ((selRow != -1) && SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
                // Default action.
                if (defaultActionEnabled) {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    Node node = Visualizer.findNode(selPath.getLastPathComponent());

                    Action a = takeAction(node.getPreferredAction(), node);

                    if (a != null) {
                        if (a.isEnabled()) {
                            a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                        } else {
                            Utilities.disabledActionBeep();
                        }

                        e.consume();

                        return;
                    }
                }

                if (tree.isExpanded(selRow)) {
                    tree.collapseRow(selRow);
                } else {
                    tree.expandRow(selRow);
                }
            }
        }

        /* VK_ENTER key processor */
        @Override
        public void actionPerformed(ActionEvent evt) {
            Node[] nodes = manager.getSelectedNodes();
            performPreferredActionOnNodes(nodes);
        }
    }
    
    static void performPreferredActionOnNodes(Node[] nodes) {
        if (nodes.length > 0) {
            Action a = nodes[0].getPreferredAction();
            if (a == null) {
                return;
            }
            for (int i=1; i<nodes.length; i++) {
                Action ai = nodes[i].getPreferredAction();
                if (ai == null || !ai.equals(a)) {
                    return;
                }
            }

            // switch to replacement action if there is some
            a = takeAction(a, nodes);
            if (a != null && a.isEnabled()) {
                a.actionPerformed(new ActionEvent(
                        nodes.length == 1 ? nodes[0] : nodes,
                        ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            } else {
                Utilities.disabledActionBeep();
            }
        }
    }

    @Override
    public void add(Component comp, Object constraints) {
        if (constraints == searchConstraints) {
            searchPanel = comp;
            constraints = null;
        }
        super.add(comp, constraints);
    }
    
    @Override
    public void remove(Component comp) {
        if (comp == searchPanel) {
            searchPanel = null;
        }
        super.remove(comp);
    }
    
    @Override
    public Insets getInsets() {
        Insets res = getInnerInsets();
        res = new Insets(res.top, res.left, res.bottom, res.right);
        if( null != searchPanel && searchPanel.isVisible() ) {
            res.bottom += searchPanel.getPreferredSize().height;
        }
        return res;
    }

    private Insets getInnerInsets() {
        Insets res = super.getInsets();
        if( null == res ) {
            res = new Insets(0,0,0,0);
        }
        return res;
    }

    TreePath[] origSelectionPaths = null;
    private Component searchPanel = null;
    private final Object searchConstraints = new Object();
    
    /** Called from tests */
    Component getSearchPanel() {
        return searchPanel;
    }

    private class ExplorerScrollPaneLayout extends ScrollPaneLayout {

        @Override
        public void layoutContainer( Container parent ) {
            super.layoutContainer(parent);
            if( null != searchPanel && searchPanel.isVisible() ) {
                Insets innerInsets = getInnerInsets();
                Dimension prefSize = searchPanel.getPreferredSize();
                searchPanel.setBounds(innerInsets.left, parent.getHeight()-innerInsets.bottom-prefSize.height,
                        parent.getWidth()-innerInsets.left-innerInsets.right, prefSize.height);
            }
        }
    }

    private final class ExplorerTree extends JTree implements Autoscroll, QuickSearch.Callback {
        AutoscrollSupport support;
        private String maxPrefix;
        int SEARCH_FIELD_SPACE = 3;
        private boolean firstPaint = true;
        /** The last search searchResults */
        private List<TreePath> searchResults = new ArrayList<TreePath>();
        /** The last selected index from the search searchResults. */
        private int currentSelectionIndex;
        private String lastSearchText;


        ExplorerTree(TreeModel model) {
            super(model);
            toggleClickCount = 0;

            // fix for #18292
            // default action map for JTree defines these shortcuts
            // but we use our own mechanism for handling them
            // following lines disable default L&F handling (if it is
            // defined on Ctrl-c, Ctrl-v and Ctrl-x)
            getInputMap().put(KeyStroke.getKeyStroke("control C"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control V"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control X"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("COPY"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("PASTE"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("CUT"), "none"); // NOI18N

            if (Utilities.isMac()) {
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_MASK), "none"); // NOI18N
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_MASK), "none"); // NOI18N
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_MASK), "none"); // NOI18N
            }

            setupSearch();

            if (!GraphicsEnvironment.isHeadless()) {
                setDragEnabled(true);
            }
        }

        /** removeNotify() call count sometimes does not match addNotify(), use special flag */
        private boolean registered = false;

        @Override
        public void addNotify() {
            super.addNotify();
            if (!registered) {
                ViewTooltips.register(this);
                registered = true;
            }
            ViewUtil.adjustBackground( this );
        }
        
        @Override
        public void removeNotify() {
            super.removeNotify();
            if (registered) {
                ViewTooltips.unregister(this);
                registered = false;
            }
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setBorder(BorderFactory.createEmptyBorder());
            if( getTransferHandler() instanceof UIResource ) {
                //we handle drag and drop in our own way, so let's just fool the UI with a dummy
                //TransferHandler to ensure that multiple selection is not lost when drag starts
                setTransferHandler( new DummyTransferHandler() );
            }
        }
        
        private void calcRowHeight(Graphics g) {
            int height = Math.max(18, 2 + g.getFontMetrics(getFont()).getHeight());

            //Issue 42743/"Jesse mode"
            String s = System.getProperty("nb.cellrenderer.fixedheight"); //NOI18N

            if (s != null) {
                try {
                    height = Integer.parseInt(s);
                } catch (Exception e) {
                    //do nothing, height not changed
                }
            }

            if (getRowHeight() != height) {
	        setRowHeight(height);
            } else {
                revalidate();
                repaint();
            }
        }

        @Override
        public Rectangle getRowBounds(int row) {
            Rectangle r = super.getRowBounds(row);
            if (r == null) {
                LOG.log(Level.WARNING, "No bounds for row {0} in three view: {1}", new Object[]{row, this});
                return new Rectangle();
            }
            return r;
        }

        //
        // Certain operation should be executed in guarded mode - e.g.
        // not allow changes in nodes during the operation being executed
        //
        @Override
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        public void paint(final Graphics g) {
            new GuardedActions(0, g);
        }

        @Override
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        protected void validateTree() {
            new GuardedActions(1, null);
        }

        @Override
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        public void doLayout() {
            new GuardedActions(2, null);
        }

        private void guardedPaint(Graphics g) {
            if (firstPaint) {
                firstPaint = false;
                calcRowHeight(g);

                //This will generate a repaint, so don't bother continuing with super.paint()
                //but do paint the background color so it doesn't paint gray the first time
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());

                return;
            }

            try {
                ExplorerTree.super.paint(g);
            } catch (NullPointerException ex) {
                // #139696: Making this issue more acceptable by not showing a dialog
                // still it deserves more investigation later
               LOG.log(Level.INFO, "Problems while painting", ex);  // NOI18N
            }
        }

        private void guardedValidateTree() {
            super.validateTree();
        }

        private void guardedDoLayout() {
            super.doLayout();
        }

        @Override
        public void setFont(Font f) {
            if (f != getFont()) {
                firstPaint = true;
                super.setFont(f);
            }
        }

        @Override
        @SuppressWarnings("ResultOfObjectAllocationIgnored")
        protected void processFocusEvent(FocusEvent fe) {
            new GuardedActions(3, fe);
        }

        @Override
        protected void processKeyEvent(KeyEvent e) {
            qs.processKeyEvent(e);
            if (!e.isConsumed()) {
                super.processKeyEvent(e);
            }
        }
        
        private void repaintSelection() {
            int first = getSelectionModel().getMinSelectionRow();
            int last = getSelectionModel().getMaxSelectionRow();

            if (first != -1) {
                if (first == last) {
                    Rectangle r = getRowBounds(first);
                    if (r == null) {
                        repaint();
                        return;
                    }
                    repaint(r.x, r.y, r.width, r.height);
                } else {
                    Rectangle top = getRowBounds(first);
                    Rectangle bottom = getRowBounds(last);
                    if (top == null || bottom == null) {
                        repaint();
                        return;
                    }
                    Rectangle r = new Rectangle();
                    r.x = Math.min(top.x, bottom.x);
                    r.y = top.y;
                    r.width = getWidth();
                    r.height = (bottom.y + bottom.height) - top.y;
                    repaint(r.x, r.y, r.width, r.height);
                }
            }
        }

        private void setupSearch() {
            // Remove the default key listeners
//            KeyListener[] keyListeners = getListeners(KeyListener.class);
//
//            for (int i = 0; i < keyListeners.length; i++) {
//                removeKeyListener(keyListeners[i]);
//            }
            
            qs = QuickSearch.attach(TreeView.this, searchConstraints, this);
        }

        @Override
        public void quickSearchUpdate(String searchText) {
            lastSearchText = searchText;
            currentSelectionIndex = 0;
            searchResults.clear();
            maxPrefix = null;

            String text = searchText.toUpperCase();

            origSelectionPaths = getSelectionPaths();
            if (origSelectionPaths != null && origSelectionPaths.length == 0) {
                origSelectionPaths = null;
            }

            if (text.length() > 0) {
                searchResults = doSearch(text);
            }
            displaySearchResult();
        }

        @Override
        public void showNextSelection(boolean forward) {
            if (forward) {
                currentSelectionIndex++;
            } else {
                currentSelectionIndex--;
            }
            displaySearchResult();
        }

        @Override
        public String findMaxPrefix(String prefix) {
            return maxPrefix;
        }

        @Override
        public void quickSearchConfirmed() {
            TreePath selectedTPath = getSelectionPath();
            if (selectedTPath != null) {
                TreeNode selectedTNode = (TreeNode) selectedTPath.getLastPathComponent();
                Node selectedNode = Visualizer.findNode(selectedTNode);
                performPreferredActionOnNodes(new Node[] { selectedNode });
            }
            origSelectionPaths = null;
            searchResults.clear();
            lastSearchText = null;
        }

        @Override
        public void quickSearchCanceled() {
            origSelectionPaths = null;
            searchResults.clear();
            lastSearchText = null;
        }

        private List<TreePath> doSearch(String prefix) {
            List<TreePath> results = new ArrayList<TreePath>();
            Set<TreePath> resSet = new HashSet<TreePath>();
            
            // starting index of substring matches within the results
            int startOfSubstringMatches = 0;

            int startIndex = origSelectionPaths != null ? Math.max(0, getRowForPath(origSelectionPaths[0])) : 0;
            int size = getRowCount();

            if (size == 0) {
                // Empty tree (no root visible); cannot match anything.
                return results;
            }

            while (true) {
                startIndex = startIndex % size;

                SubstringSearchResult substringSearchResult = getNextSubstringMatch(prefix, startIndex, true);
                TreePath path = substringSearchResult != null? substringSearchResult.treePath: null;

                if ((path != null) && !resSet.contains(path)) {
                    startIndex = tree.getRowForPath(path);
                    boolean isPrefixMatch = true;
                    // put all prefix matches to the top of the list while
                    // substring matches remains in the end of the list
                    if (substringSearchResult.index == 0) {
                        results.add(startOfSubstringMatches++, path);
                    } else {
                        isPrefixMatch = false;
                        results.add(path);
                    }
                    resSet.add(path);

                    // calculate max prefix only with prefix matches
                    if (isPrefixMatch) {
                        String elementName = ((VisualizerNode) path.getLastPathComponent()).getDisplayName();

                        // initialize prefix
                        if (maxPrefix == null) {
                            maxPrefix = elementName;
                        }

                        maxPrefix = QuickSearch.findMaxPrefix(maxPrefix, elementName, true);
                    }
                    // try next element
                    startIndex++;
                } else {
                    break;
                }
            }

            return results;
        }

        /**
         * Copied and adapted from JTree.getNextMatch(...).
         * 
         * @return An instance of SubstringSearchResult containing the matching TreePath
         *         and the index of the first occurrence of the substring in TreePath.
         */
        private SubstringSearchResult getNextSubstringMatch(
                String substring, int startingRow, boolean forward) {

            int max = getRowCount();
            if (substring == null) {
                throw new IllegalArgumentException("Substring is null");
            }
            if (startingRow < 0 || startingRow >= max) {
                throw new IllegalArgumentException("startingRow = " + startingRow + " rowCount = " + max);
            }
            substring = substring.toUpperCase();

            // start search from the next/previous element froom the 
            // selected element
            int increment = (forward) ? 1 : -1;
            int row = startingRow;
            do {
                TreePath path = getPathForRow(row);
                String text = convertValueToText(
                    path.getLastPathComponent(), isRowSelected(row),
                    isExpanded(row), true, row, false);
                
                int index = text.toUpperCase().indexOf(substring);
                if (index >= 0) {
                    return new SubstringSearchResult(path, index);
                }
                row = (row + increment + max) % max;
            } while (row != startingRow);
            return null;
        }

        private void displaySearchResult() {
            int sz = searchResults.size();

            if (sz > 0) {
                if (currentSelectionIndex < 0) {
                    currentSelectionIndex = sz - 1;
                } else if (currentSelectionIndex >= sz) {
                    currentSelectionIndex = 0;
                }

                TreePath path = searchResults.get(currentSelectionIndex);
                setSelectionPath(path);
                scrollPathToVisible(path);
            } else {
                if (lastSearchText.isEmpty() && origSelectionPaths != null) {
                    setSelectionPaths(origSelectionPaths);
                    scrollPathToVisible(origSelectionPaths[0]);
                } else {
                    clearSelection();
                }
            }
        }

        /** notify the Component to autoscroll */
        @Override
        public void autoscroll(Point cursorLoc) {
            getSupport().autoscroll(cursorLoc);
        }

        /** @return the Insets describing the autoscrolling
         * region or border relative to the geometry of the
         * implementing Component.
         */
        @Override
        public Insets getAutoscrollInsets() {
            return getSupport().getAutoscrollInsets();
        }

        /** Safe getter for autoscroll support. */
        AutoscrollSupport getSupport() {
            if (support == null) {
                support = new AutoscrollSupport(this, new Insets(15, 10, 15, 10));
            }

            return support;
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            if (event != null) {
                Point p = event.getPoint();
                int selRow = getRowForLocation(p.x, p.y);

                if (selRow != -1) {
                    TreePath path = getPathForRow(selRow);
                    VisualizerNode v = (VisualizerNode) path.getLastPathComponent();
                    String tooltip = v.getShortDescription();
                    String displayName = v.getDisplayName();

                    if ((tooltip != null) && !tooltip.equals(displayName)) {
                        return tooltip;
                    }
                }
            }

            return null;
        }

        @Override
        protected TreeModelListener createTreeModelListener() {
            return new ModelHandler();
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleExplorerTree();
            }

            return accessibleContext;
        }

        private class GuardedActions implements Mutex.Action<Object> {
            private int type;
            private Object p1;
            final Object ret;

            @SuppressWarnings({"OverridableMethodCallInConstructor", "LeakingThisInConstructor"})
            public GuardedActions(int type, Object p1) {
                this.type = type;
                this.p1 = p1;
                if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess()) {
                    ret = run();
                } else {
                    ret = Children.MUTEX.readAccess(this);
                }
            }

            @Override
            public Object run() {
                switch (type) {
                case 0:
                    guardedPaint((Graphics) p1);
                    break;
                case 1:
                    guardedValidateTree();
                    break;
                case 2:
                    guardedDoLayout();
                    break;
                case 3:
                    ExplorerTree.super.processFocusEvent((FocusEvent)p1);
                    //Since the selected when focused is different, we need to force a
                    //repaint of the entire selection, but let's do it in guarded more
                    //as any other repaint
                    repaintSelection();
                    break;
                default:
                    throw new IllegalStateException("type: " + type);
                }

                return null;
            }
        }

        private class AccessibleExplorerTree extends JTree.AccessibleJTree {
            AccessibleExplorerTree() {
            }

            @Override
            public String getAccessibleName() {
                return TreeView.this.getAccessibleContext().getAccessibleName();
            }

            @Override
            public String getAccessibleDescription() {
                return TreeView.this.getAccessibleContext().getAccessibleDescription();
            }
        }

        private class ModelHandler extends JTree.TreeModelHandler {
            ModelHandler() {
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                // Remember selections and expansions
                TreePath[] selectionPaths = getSelectionPaths();
                java.util.Enumeration expanded = getExpandedDescendants(e.getTreePath());

                // Restructure the node
                super.treeStructureChanged(e);

                // Expand previously expanded paths
                if (expanded != null) {
                    while (expanded.hasMoreElements()) {
                        expandPath((TreePath) expanded.nextElement());
                    }
                }

                // Select previously selected paths
                if ((selectionPaths != null) && (selectionPaths.length > 0)) {
                    boolean wasSelected = isPathSelected(selectionPaths[0]);

                    setSelectionPaths(selectionPaths);

                    if (!wasSelected) {
                        // do not scroll if the first selection path survived structure change
                        scrollPathToVisible(selectionPaths[0]);
                    }
                }
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                // called to removed from JTree.expandedState
                super.treeNodesRemoved(e);
                
                boolean wasSelected = removedNodeWasSelected;
                removedNodeWasSelected = false;

                // part of bugfix #37279, if DnD is active then is useless select a nearby node
                if (ExplorerDnDManager.getDefault().isDnDActive()) {
                    return;
                }
                if (wasSelected && tree.getSelectionCount() == 0) {
                    TreePath path = findSiblingTreePath(e.getTreePath(), e.getChildIndices());

                    // bugfix #39564, don't select again the same object
                    if ((path == null) || e.getChildIndices().length == 0) {
                        return;
                    } else if (path.getPathCount() > 0) {
                        tree.setSelectionPath(path);
                    }
                }
            }
        }
        
        private class SubstringSearchResult {
            
            TreePath treePath;  // holds the matching TreePath
            int index;          // holds the index of the first occurrence of the substring in TreePath

            public SubstringSearchResult(TreePath treePath, int index) {
                this.treePath = treePath;
                this.index = index;
            }
            
        }
    }
    
    private static class DummyTransferHandler extends TransferHandler /*implements UIResource*/ {
        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            //do nothing - ExplorerDnDManager will kick in when necessary
        }
        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action)
                                                      throws IllegalStateException {
            //do nothing - Node actions will hande this
        }
        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return false; //TreeViewDropSupport will decided
        }
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            return false;
        }
        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }
    }

    static class VisualizerHolder extends HashSet<VisualizerChildren> {

        /** removes recursively VisualizerChildren */
        void removeRecur(VisualizerChildren visChildren) {
            Enumeration<VisualizerNode> vnodes = visChildren.children(false);
            while (vnodes.hasMoreElements()) {
                VisualizerNode vn = vnodes.nextElement();
                if (vn != null) {
                    removeRecur(vn.getChildren(false));
                }
            }
            remove(visChildren);
        }
    }
}
