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

import java.awt.*;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.NodeOp;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallbackSystemAction;

/** Explorer view to display items in a list.
 * <p>
 * This class is a <em>view</em>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link org.openide.explorer.ExplorerUtils}. Then just use 
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <em>views</em> under one container implementing {@link Provider}. Select from
 * range of predefined ones or write your own:
 * </p>
 * <ul>
 *      <li>{@link org.openide.explorer.view.BeanTreeView} - shows a tree of nodes</li>
 *      <li>{@link org.openide.explorer.view.ContextTreeView} - shows a tree of nodes without leaf nodes</li>
 *      <li>{@link org.openide.explorer.view.ListView} - shows a list of nodes</li>
 *      <li>{@link org.openide.explorer.view.IconView} - shows a rows of nodes with bigger icons</li>
 *      <li>{@link org.openide.explorer.view.ChoiceView} - creates a combo box based on the explored nodes</li>
 *      <li>{@link org.openide.explorer.view.TreeTableView} - shows tree of nodes together with a set of their {@link Property}</li>
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link javax.swing.JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controller. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sense together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
 * @author   Ian Formanek, Jan Jancura, Jaroslav Tulach
 */
public class ListView extends JScrollPane implements Externalizable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7540940974042262975L;

    /** Explorer manager to work with. Is not null only if the component is showing
    * in components hierarchy
    */
    private transient ExplorerManager manager;

    /** The actual JList list */
    protected transient JList list;

    /** model to use */
    protected transient NodeListModel model;

    //
    // listeners
    //

    /** Listener to nearly everything */
    transient Listener managerListener;

    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;

    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;

    /** popup */
    transient PopupSupport popupSupport;

    //
    // properties
    //

    /** if true, the icon view displays a popup on right mouse click, if false, the popup is not displayed */
    private boolean popupAllowed = true;

    /** if true, the hierarchy traversal is allowed, if false, it is disabled */
    private boolean traversalAllowed = true;

    /** show parent node */
    private boolean showParentNode;

    /** action preformer */
    private ActionListener defaultProcessor;

    //
    // Dnd
    //

    /** true if drag support is active */
    transient boolean dragActive = false;

    /** true if drop support is active */
    transient boolean dropActive = false;

    /** Drag support */
    transient ListViewDragSupport dragSupport;

    /** Drop support */
    transient ListViewDropSupport dropSupport;

    // default DnD actions
    private transient int allowedDragActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    private transient int allowedDropActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;

    /** True, if the selection listener is attached. */
    transient boolean listenerActive;

    // init .................................................................................

    /** Default constructor.
    */
    public ListView() {
        initializeList();

        // activation of drop target
        setDropTarget( DragDropUtilities.dragAndDropEnabled );

        // no border, window system manages outer border itself
        setBorder(BorderFactory.createEmptyBorder());
        setViewportBorder(BorderFactory.createEmptyBorder());
    }

    /** Initializes the tree & model.
    */
    private void initializeList() {
        // initilizes the JTree
        model = createModel();
        list = createList();
        list.setModel(model);

        setViewportView(list);

        {
            AbstractAction action = new GoUpAction();
            KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0);
            list.registerKeyboardAction(action, key, JComponent.WHEN_FOCUSED);
        }

        {
            AbstractAction action = new EnterAction();
            KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            list.registerKeyboardAction(action, key, JComponent.WHEN_FOCUSED);
        }

        managerListener = new Listener();
        popupSupport = new PopupSupport();
        list.getActionMap().put("org.openide.actions.PopupAction", popupSupport); // NOI18N

        list.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(list);
    }

    /*
    * Write view's state to output stream.
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(popupAllowed ? Boolean.TRUE : Boolean.FALSE);
        out.writeObject(traversalAllowed ? Boolean.TRUE : Boolean.FALSE);
        out.writeObject(new Integer(getSelectionMode()));
    }

    /*
    * Reads view's state form output stream.
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        popupAllowed = ((Boolean) in.readObject()).booleanValue();
        traversalAllowed = ((Boolean) in.readObject()).booleanValue();
        setSelectionMode(((Integer) in.readObject()).intValue());
    }

    // properties ...........................................................................

    /** Test whether display of a popup menu is enabled.
     * @return <code>true</code> if so */
    public boolean isPopupAllowed() {
        return popupAllowed;
    }

    /** Enable/disable displaying popup menus on list view items. Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed(boolean value) {
        popupAllowed = value;
    }

    /** Test whether hierarchy traversal shortcuts are permitted.
    * @return <code>true</code> if so */
    public boolean isTraversalAllowed() {
        return traversalAllowed;
    }

    /** Enable/disable hierarchy traversal using <code>CTRL+click</code> (down) and <code>Backspace</code> (up), default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setTraversalAllowed(boolean value) {
        traversalAllowed = value;
    }

    /** Is parent node (e.g. explored context shown)?
     * @return true or false. Default is false.
     * @since 6.28
     */
    public boolean isShowParentNode() {
        return showParentNode;
    }

    /** Shall the first node in the list be ".." representing currently
     * explored context? By default it is not, but if you want to simplify
     * the navigation in the {@link  Node} hierarchy, you can turn this
     * property on.
     * 
     * @param show true to show the "..", false to not to do so
     * @since 6.28
     */
    public void setShowParentNode(boolean show) {
        showParentNode = show;
    }

    /** Get the current processor for default actions.
    * If not <code>null</code>, double-clicks or pressing Enter on
    * items in the view will not perform the default action on the selected node; rather the processor
    * will be notified about the event.
    * @return the current default-action processor, or <code>null</code>
    */
    public ActionListener getDefaultProcessor() {
        return defaultProcessor;
    }

    /** Set a new processor for default actions.
    * @param value the new default-action processor, or <code>null</code> to restore use of the selected node's declared default action
    * @see #getDefaultProcessor
    */
    public void setDefaultProcessor(ActionListener value) {
        defaultProcessor = value;
    }

    /**
     * Set whether single-item or multiple-item
     * selections are allowed.
     * @param selectionMode one of {@link ListSelectionModel#SINGLE_SELECTION}, {@link ListSelectionModel#SINGLE_INTERVAL_SELECTION}, or  {@link ListSelectionModel#MULTIPLE_INTERVAL_SELECTION}
     * @see ListSelectionModel#setSelectionMode
     */
    public void setSelectionMode(int selectionMode) {
        list.setSelectionMode(selectionMode);
    }

    /** Get the selection mode.
     * @return the mode
     * @see #setSelectionMode
     */
    public int getSelectionMode() {
        return list.getSelectionMode();
    }

    /********** Support for the Drag & Drop operations *********/
    /** @return true if dragging from the view is enabled, false
    * otherwise.<br>
    * Drag support is disabled by default.
    */
    public boolean isDragSource() {
        return dragActive;
    }

    /** Enables/disables dragging support.
    * @param state true enables dragging support, false disables it.
    */
    public void setDragSource(boolean state) {
        if (state == dragActive) {
            return;
        }

        dragActive = state;

        // create drag support if needed
        if (dragActive && (dragSupport == null)) {
            dragSupport = new ListViewDragSupport(this, list);
        }

        // activate / deactivate support according to the state
        dragSupport.activate(dragActive);
    }

    /** @return true if dropping to the view is enabled, false
    * otherwise<br>
    * Drop support is disabled by default.
    */
    public boolean isDropTarget() {
        return dropActive;
    }

    /** Enables/disables dropping support.
    * @param state true means drops into view are allowed,
    * false forbids any drops into this view.
    */
    public void setDropTarget(boolean state) {
        if (state == dropActive) {
            return;
        }

        dropActive = state;

        // create drop support if needed
        if (dropActive && (dropSupport == null)) {
            dropSupport = new ListViewDropSupport(this, list);
        }

        // activate / deactivate support according to the state
        dropSupport.activate(dropActive);
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
        // PENDING check parameters
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
        // PENDING check parameters
        allowedDropActions = actions;
    }

    //
    // Methods to override
    //

    /** Creates the list that will display the data.
    */
    protected JList createList() {
        JList list = new NbList();
        list.setCellRenderer(new NodeRenderer());

        return list;
    }

    /** Allows subclasses to change the default model used for
    * the list.
    */
    protected NodeListModel createModel() {
        return new NodeListModel();
    }

    /** Called when the list changed selection and the explorer manager
    * should be updated.
    * @param nodes list of nodes that should be selected
    * @param em explorer manager
    * @exception PropertyVetoException if the manager does not allow the
    *   selection
    */
    protected void selectionChanged(Node[] nodes, ExplorerManager em)
    throws PropertyVetoException {
        em.setSelectedNodes(nodes);
    }

    /** Called when explorer manager is about to change the current selection.
    * The view can forbid the change if it is not able to display such
    * selection.
    *
    * @param nodes the nodes to select
    * @return false if the view is not able to change the selection
    */
    protected boolean selectionAccept(Node[] nodes) {
        // if the selection is just the root context, confirm the selection
        if ((nodes.length == 1) && manager.getRootContext().equals(nodes[0])) {
            // XXX shouldn't this be exploredContext?
            return true;
        }

        // we do not allow selection in other than the exploredContext
        for (int i = 0; i < nodes.length; i++) {
            VisualizerNode v = VisualizerNode.getVisualizer(null, nodes[i]);

            if (model.getIndex(v) == -1) {
                return false;
            }
        }

        return true;
    }

    /** Shows selection.
    * @param indexes indexes of objects to select
    */
    protected void showSelection(int[] indexes) {
        list.setSelectedIndices(indexes);
    }

    private class GuardedActions implements Mutex.Action<Object> {

        private int type;
        private Object p1;
        final Object ret;

        public GuardedActions(int type, Object p1) {
            this.type = type;
            this.p1 = p1;
            if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess()) {
                ret = run();
            } else {
                ret = Children.MUTEX.readAccess(this);
            }
        }

        public Object run() {
            switch (type) {
                case 0:
                    ListView.super.paint((Graphics) p1);
                    break;
                case 1:
                    ListView.super.validateTree();
                    break;
                case 2:
                    ListView.super.doLayout();
                    break;
                case 4:
                    ListView.super.processEvent((AWTEvent) p1);
                    break;
                case 5:
                    return ListView.super.getPreferredSize();
                case 6:
                    updateSelectionImpl();
                    break;
                default:
                    throw new IllegalStateException("type: " + type);
            }

            return null;
        }
    }

    @Override
    public void paint(Graphics g) {
        new GuardedActions(0, g);
    }

    @Override
    protected void validateTree() {
        new GuardedActions(1, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return (Dimension) new GuardedActions(5, null).ret;
    }

    @Override
    public void doLayout() {
        new GuardedActions(2, null);
    }

    //
    // Working methods
    //

    final void setNode(Node n) {
        boolean show = showParentNode && n != manager.getRootContext();
        model.setNode(n, show);
    }

    /* Initilizes the view.
    */
    @Override
    public void addNotify() {
        super.addNotify();

        // run under mutex
        ExplorerManager em = ExplorerManager.find(this);

        if (em != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener(wlvc);
                manager.removePropertyChangeListener(wlpc);
            }

            manager = em;

            manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(managerListener, manager));

            setNode(manager.getExploredContext());

            updateSelection();
        } else {
            // bugfix #23509, the listener were removed --> add it again
            if (!listenerActive && (manager != null)) {
                manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(managerListener, manager));
                manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(managerListener, manager));
            }
        }

        if (!listenerActive) {
            listenerActive = true;
            list.getSelectionModel().addListSelectionListener(managerListener);
            model.addListDataListener(managerListener);

            // bugfix #23974, model doesn't reflect an explorer context change
            // because any listener was not active
            setNode(manager.getExploredContext());
            list.addMouseListener(popupSupport);
        }
    }

    /** Removes listeners.
    */
    @Override
    public void removeNotify() {
        super.removeNotify();
        listenerActive = false;
        list.getSelectionModel().removeListSelectionListener(managerListener);

        // bugfix #23509, remove useless listeners
        if (manager != null) {
            manager.removeVetoableChangeListener(wlvc);
            manager.removePropertyChangeListener(wlpc);
        }

        model.removeListDataListener(managerListener);
        list.removeMouseListener(popupSupport);

        // #112536: [dafe] I wasn't able to find out real reason for #112536
        // Following delaying works, but is hacky a bit and we have to check
        // if ListView is reused - addNotify called again, which we check through isDisplayable
        SwingUtilities.invokeLater(new Runnable () {
            public void run() {
                if (!isDisplayable()) {
                    // #109123: clear the model, as it may become invalid, because we stopped 
                    // tracking ExplorerManager changes through listeners
                    model.setNode(Node.EMPTY);
                }
            }
        });
    }

    /* Requests focus for the list component. Overrides superclass method. */
    @Override
    public void requestFocus() {
        list.requestFocus();
    }

    /* Requests focus for the list component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow() {
        return list.requestFocusInWindow();
    }

    /** This method is called when user double-clicks on some object or
    * presses Enter key.
    * @param index Index of object in current explored context
    */
    final void performObjectAt(int index, int modifiers) {
        if ((index < 0) || (index >= model.getSize())) {
            return;
        }

        VisualizerNode v = (VisualizerNode) model.getElementAt(index);
        Node node = v.node;

        // if DefaultProcessor is set, the default action is notified to it overriding the default action on nodes
        if (defaultProcessor != null) {
            defaultProcessor.actionPerformed(new ActionEvent(node, 0, null, modifiers));

            return;
        }

        if (showParentNode && NodeListModel.findVisualizerDepth(model, v) == -1) {
            try {
                manager.setExploredContextAndSelection(node.getParentNode(), new Node[] { node });
            } catch (PropertyVetoException ex) {
                // OK, let it be
            }
            return;
        }

        // on double click - invoke default action, if there is any
        // (unless user holds CTRL key what means that we should always dive into the context)
        Action a = node.getPreferredAction();

        if ((a != null) && ((modifiers & InputEvent.CTRL_MASK) == 0)) {
            a = TreeView.takeAction(a, node);

            if (a.isEnabled()) {
                a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            } else {
                Utilities.disabledActionBeep();
            }
        }
        // otherwise dive into the context
        else if (traversalAllowed && (!node.isLeaf())) {
            manager.setExploredContext(node, manager.getSelectedNodes());
        }
    }
    
    private void updateSelection() {
        new GuardedActions(6, null);
    }

    /** Called when selection has been changed. Make selection visible (at least partly).
    */
    private void updateSelectionImpl() {
        Node[] sel = manager.getSelectedNodes();
        int[] indices = new int[sel.length];

        // bugfix #27094, make sure a selection is visible
        int firstVisible = list.getFirstVisibleIndex();
        int lastVisible = list.getLastVisibleIndex();
        boolean ensureVisible = indices.length > 0;

        for (int i = 0; i < sel.length; i++) {
            VisualizerNode v = VisualizerNode.getVisualizer(null, sel[i]);
            indices[i] = model.getIndex(v);
            ensureVisible = ensureVisible && ((indices[i] < firstVisible) || (indices[i] > lastVisible));
        }

        // going to change list because of E.M.'s order -- temp disable the
        // listener
        if (listenerActive) {
            list.getSelectionModel().removeListSelectionListener(managerListener);
        }

        try {
            showSelection(indices);

            if (ensureVisible) {
                list.ensureIndexIsVisible(indices[0]);
            }
        } finally {
            if (listenerActive) {
                list.getSelectionModel().addListSelectionListener(managerListener);
            }
        }
    }

    void createPopup(int xpos, int ypos, boolean contextMenu) {
        if (manager == null) {
            return;
        }

        if (!popupAllowed) {
            return;
        }
        
        if (!isShowing()) {
            return;
        }

        JPopupMenu popup;

        if (contextMenu) {
            popup = Utilities.actionsToPopup(manager.getExploredContext().getActions(true), this);
        } else {
            Action[] actions = NodeOp.findActions(manager.getSelectedNodes());
            popup = Utilities.actionsToPopup(actions, this);
        }

        if ((popup != null) && (popup.getSubElements().length > 0)) {
            Point p = getViewport().getViewPosition();
            p.x = xpos - p.x;
            p.y = ypos - p.y;

            SwingUtilities.convertPointToScreen(p, ListView.this);

            Dimension popupSize = popup.getPreferredSize();
            Rectangle screenBounds = Utilities.getUsableScreenBounds(getGraphicsConfiguration());

            if ((p.x + popupSize.width) > (screenBounds.x + screenBounds.width)) {
                p.x = (screenBounds.x + screenBounds.width) - popupSize.width;
            }

            if ((p.y + popupSize.height) > (screenBounds.y + screenBounds.height)) {
                p.y = (screenBounds.y + screenBounds.height) - popupSize.height;
            }

            SwingUtilities.convertPointFromScreen(p, ListView.this);
            popup.show(this, p.x, p.y);
        }
    }

    // innerclasses .........................................................................

    /**
     * Enhancement of standard JList.
     * Provides access to the Node's ToolTips, Accessibility and Autoscrolling.
     */
    final class NbList extends JList implements Autoscroll {
        static final long serialVersionUID = -7571829536335024077L;

        /** The worker for the scrolling */
        AutoscrollSupport support;

        // navigator
        int SEARCH_FIELD_PREFERRED_SIZE = 160;
        int SEARCH_FIELD_SPACE = 3;
        private String maxPrefix;

        // searchTextField manages focus because it handles VK_TAB key
        private JTextField searchTextField = new JTextField() {
            @Override
                public boolean isManagingFocus() {
                    return true;
                }

            @Override
                public void processKeyEvent(KeyEvent ke) {
                    //override the default handling so that
                    //the parent will never receive the escape key and
                    //close a modal dialog
                    if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        removeSearchField();
                        ke.consume(); // #44394

                        // bugfix #32909, reqest focus when search field is removed
                        NbList.this.requestFocus();
                    } else {
                        super.processKeyEvent(ke);
                    }
                }
            };

        private final int heightOfTextField = searchTextField.getPreferredSize().height;
        private int originalScrollMode;
        private JPanel searchpanel = null;

        NbList() {
            super();

            // fix for #18292
            // default action map for JList defines these shortcuts
            // but we use our own mechanism for handling them
            // following lines disable default L&F handling (if it is
            // defined on Ctrl-c, Ctrl-v and Ctrl-x)
            getInputMap().put(KeyStroke.getKeyStroke("control C"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control V"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control X"), "none"); // NOI18N
            setupSearch();
        }

        @Override
        public void addNotify() {
            super.addNotify();
            ViewTooltips.register(this);
        }
        
        @Override
        public void removeNotify() {
            super.removeNotify();
            ViewTooltips.unregister(this);
        }        

        @Override
        protected void processFocusEvent(FocusEvent fe) {
            super.processFocusEvent(fe);
            repaintSelection();
        }
        private class GuardedActions implements Mutex.Action<Object> {

            private int type;
            private Object p1;
            final Object ret;

            public GuardedActions(int type, Object p1) {
                this.type = type;
                this.p1 = p1;
                if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess()) {
                    ret = run();
                } else {
                    ret = Children.MUTEX.readAccess(this);
                }
            }

            public Object run() {
                switch (type) {
                    case 0:
                        NbList.super.paint((Graphics) p1);
                        break;
                    case 1:
                        NbList.super.validateTree();
                        break;
                    case 2:
                        doLayoutImpl();
                        break;
                    case 3:
                        repaintSelection();
                        break;
                    case 4:
                        NbList.super.processEvent((AWTEvent) p1);
                        break;
                    case 5:
                        return NbList.super.getPreferredSize();
                    case 6:
                        return getToolTipTextImpl((MouseEvent) p1);
                    case 7:
                        return NbList.super.indexToLocation((Integer) p1);
                    case 8:
                        return NbList.super.locationToIndex((Point) p1);
                    case 9:
                        return NbList.super.getSelectedValues();
                    case 10:
                        Object[] arr = (Object[]) p1;
                        return NbList.super.processKeyBinding(
                                (KeyStroke) arr[0],
                                (KeyEvent) arr[1],
                                (Integer) arr[2],
                                (Boolean) arr[3]);                      
                    default:
                        throw new IllegalStateException("type: " + type);
                }

                return null;
            }
        }

        @Override
        public void paint(Graphics g) {
            new GuardedActions(0, g);
        }

        @Override
        protected void validateTree() {
            new GuardedActions(1, null);
        }

        @Override
        public Dimension getPreferredSize() {
            return (Dimension) new GuardedActions(5, null).ret;
        }

        @Override
        public Point indexToLocation(int index) {
            return (Point) new GuardedActions(7, index).ret;
        }

        @Override
        public int locationToIndex(Point location) {
            return (Integer) new GuardedActions(8, location).ret;
        }

        @Override
        public Object[] getSelectedValues() {
            return (Object[]) new GuardedActions(9, null).ret;
        }

        private void repaintSelection() {
            if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess()) {
                int[] idx = getSelectedIndices();

                if (idx.length == 0) {
                    return;
                }

                for (int i = 0; i < idx.length; i++) {
                    Rectangle r = getCellBounds(idx[i], idx[i]);
                    repaint(r.x, r.y, r.width, r.height);
                }
            } else {
                new GuardedActions(3, null);
            }
        }

        // ToolTips:

        /**
         * Overrides JComponent's getToolTipText method in order to allow
         * Node's tips to be used if they are useful.
         *
         * @param event the MouseEvent that initiated the ToolTip display
         */
        @Override
        public String getToolTipText(MouseEvent event) {
            return (String) new GuardedActions(6, event).ret;
        }

        final String getToolTipTextImpl(MouseEvent event) {
            if (event != null) {
                Point p = event.getPoint();
                int row = locationToIndex(p);

                if (row >= 0) {
                    VisualizerNode v = (VisualizerNode) model.getElementAt(row);
                    String tooltip = v.getShortDescription();
                    String displayName = v.getDisplayName();

                    if ((tooltip != null) && !tooltip.equals(displayName)) {
                        return tooltip;
                    }
                }
            }

            return null;
        }

        // Autoscroll:

        /** notify the Component to autoscroll */
        public void autoscroll(Point cursorLoc) {
            getSupport().autoscroll(cursorLoc);
        }

        /** @return the Insets describing the autoscrolling region or border
         * relative to the geometry of the implementing Component.
         */
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

        // Accessibility:
        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleExplorerList();
            }

            return accessibleContext;
        }

        private void setupSearch() {
            // Remove the default key listeners
            KeyListener[] keyListeners = getListeners(KeyListener.class);

            for (int i = 0; i < keyListeners.length; i++) {
                removeKeyListener(keyListeners[i]);
            }

            // Add new key listeners
            addKeyListener(
                new KeyAdapter() {
                @Override
                    public void keyPressed(KeyEvent e) {
                        int modifiers = e.getModifiers();
                        int keyCode = e.getKeyCode();

                        if (((modifiers > 0) && (modifiers != KeyEvent.SHIFT_MASK)) || e.isActionKey()) {
                            return;
                        }

                        char c = e.getKeyChar();

                        if (!Character.isISOControl(c) && (keyCode != KeyEvent.VK_SHIFT)) {
                            searchTextField.setText(String.valueOf(c));
                            displaySearchField();
                        }
                    }
                }
            );

            // Create a the "multi-event" listener for the text field. Instead of
            // adding separate instances of each needed listener, we're using a
            // class which implements them all. This approach is used in order
            // to avoid the creation of 4 instances which takes some time
            SearchFieldListener searchFieldListener = new SearchFieldListener();
            searchTextField.addKeyListener(searchFieldListener);
            searchTextField.addFocusListener(searchFieldListener);
            searchTextField.getDocument().addDocumentListener(searchFieldListener);
        }

        private List<Integer> doSearch(String prefix) {
            List<Integer> results = new ArrayList<Integer>();

            // do search forward the selected index
            int startIndex = (getSelectedIndex() == -1) ? 0 : getSelectedIndex();
            int size = getModel().getSize();
            if (size == 0) return results; // nothing to search

            while (true) {
                startIndex = startIndex % size;
                startIndex = getNextMatch(prefix, startIndex, Position.Bias.Forward);

                if ((startIndex != -1) && !results.contains(new Integer(startIndex))) {
                    results.add(Integer.valueOf(startIndex));

                    String elementName = getModel().getElementAt(startIndex).toString();

                    // initialize prefix
                    if (maxPrefix == null) {
                        maxPrefix = elementName;
                    }

                    maxPrefix = findMaxPrefix(maxPrefix, elementName);

                    // try next element
                    startIndex++;
                } else {
                    break;
                }
            }

            return results;
        }

        private String findMaxPrefix(String str1, String str2) {
            String res = null;

            for (int i = 0; str1.regionMatches(true, 0, str2, 0, i); i++) {
                res = str1.substring(0, i);
            }

            return res;
        }

        private void prepareSearchPanel() {
            if (searchpanel == null) {
                searchpanel = new JPanel();

                JLabel lbl = new JLabel(NbBundle.getMessage(TreeView.class, "LBL_QUICKSEARCH")); //NOI18N
                searchpanel.setLayout(new BorderLayout(5, 0));
                searchpanel.add(lbl, BorderLayout.WEST);
                searchpanel.add(searchTextField, BorderLayout.CENTER);
                lbl.setLabelFor(searchTextField);
                searchpanel.setBorder(BorderFactory.createRaisedBevelBorder());
            }
        }

        /**
         * Adds the search field to the tree.
         */
        private void displaySearchField() {
            if ((getModel().getSize() > 0) && !searchTextField.isDisplayable()) {
                JViewport viewport = ListView.this.getViewport();
                originalScrollMode = viewport.getScrollMode();
                viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

                //Rectangle visibleTreeRect = getVisibleRect();
                prepareSearchPanel();
                add(searchpanel);
                revalidate();
                repaint();

                // bugfix #28501, avoid the chars duplicated on jdk1.3
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            searchTextField.requestFocus();
                        }
                    }
                );
            }
        }

        @Override
        public void doLayout() {
            new GuardedActions(2, null);
        }
        
        final void doLayoutImpl() {
            super.doLayout();

            if ((searchpanel != null) && searchpanel.isDisplayable()) {
                Rectangle visibleRect = getVisibleRect();
                int width = Math.min(
                        visibleRect.width - (SEARCH_FIELD_SPACE * 2),
                        SEARCH_FIELD_PREFERRED_SIZE - SEARCH_FIELD_SPACE);

                int height = heightOfTextField + searchpanel.getInsets().top + searchpanel.getInsets().bottom;
                searchpanel.setBounds(
                        Math.max(SEARCH_FIELD_SPACE, (visibleRect.x + visibleRect.width) - width),
                        visibleRect.y + SEARCH_FIELD_SPACE, Math.min(visibleRect.width, width) - SEARCH_FIELD_SPACE,
                        height);
            }        
        }

        /**
         * Removes the search field from the tree.
         */
        private void removeSearchField() {
            if ((searchpanel != null) && searchpanel.isDisplayable()) {
                remove(searchpanel);
                ListView.this.getViewport().setScrollMode(originalScrollMode);
                this.repaint(searchpanel.getBounds());
                requestFocus();
            }
        }

        private class AccessibleExplorerList extends AccessibleJList {
            AccessibleExplorerList() {
            }

            @Override
            public String getAccessibleName() {
                return ListView.this.getAccessibleContext().getAccessibleName();
            }

            @Override
            public String getAccessibleDescription() {
                return ListView.this.getAccessibleContext().getAccessibleDescription();
            }
        }

        private class SearchFieldListener extends KeyAdapter implements DocumentListener, FocusListener {
            /** The last search results */
            private List results = new ArrayList();

            /** The last selected index from the search results. */
            private int currentSelectionIndex;

            SearchFieldListener() {
            }

            public void changedUpdate(DocumentEvent e) {
                searchForNode();
            }

            public void insertUpdate(DocumentEvent e) {
                searchForNode();
            }

            public void removeUpdate(DocumentEvent e) {
                searchForNode();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_ESCAPE) {
                    removeSearchField();
                    NbList.this.requestFocus();
                } else if (keyCode == KeyEvent.VK_UP) {
                    currentSelectionIndex--;
                    displaySearchResult();

                    // Stop processing the event here. Otherwise it's dispatched
                    // to the tree too (which scrolls)
                    e.consume();
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    currentSelectionIndex++;
                    displaySearchResult();

                    // Stop processing the event here. Otherwise it's dispatched
                    // to the tree too (which scrolls)
                    e.consume();
                } else if (keyCode == KeyEvent.VK_TAB) {
                    if (maxPrefix != null) {
                        searchTextField.setText(maxPrefix);
                    }

                    e.consume();
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    removeSearchField();
                    NbList.this.requestFocus();
                    NbList.this.dispatchEvent(e);
                }
            }

            /** Searches for a node in the tree. */
            private void searchForNode() {
                currentSelectionIndex = 0;
                results.clear();
                maxPrefix = null;

                String text = searchTextField.getText();

                if (text.length() > 0) {
                    results = doSearch(text);
                    displaySearchResult();
                }
            }

            private void displaySearchResult() {
                int sz = results.size();

                if (sz > 0) {
                    if (currentSelectionIndex < 0) {
                        currentSelectionIndex = sz - 1;
                    } else if (currentSelectionIndex >= sz) {
                        currentSelectionIndex = 0;
                    }

                    Integer index = (Integer) results.get(currentSelectionIndex);
                    list.setSelectedIndex(index.intValue());
                    list.ensureIndexIsVisible(index.intValue());
                } else {
                    list.clearSelection();
                }
            }

            public void focusGained(FocusEvent e) {
                // make sure nothing is selected
                searchTextField.select(1, 1);
            }

            public void focusLost(FocusEvent e) {
                removeSearchField();
            }
        }

        // end of navigator
    }

    private final class PopupSupport extends MouseUtils.PopupMouseAdapter implements Action, Runnable {
        
        CallbackSystemAction csa;
        
        public PopupSupport() {}

        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
                int index = list.locationToIndex(e.getPoint());
                performObjectAt(index, e.getModifiers());
            }
        }

        protected void showPopup(MouseEvent e) {
            Point p = new Point(e.getX(), e.getY());
            int i = list.locationToIndex(p);

            if (!list.isSelectedIndex(i)) {
                list.setSelectedIndex(i);
            }

            // the area of selected 
            Rectangle r = list.getCellBounds(i, i);
            boolean contextMenu = (r == null) || !r.contains(p);

            createPopup(e.getX(), e.getY(), contextMenu);
        }

        public void actionPerformed(ActionEvent e) {
            // XXX why later?
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            boolean multisel = (list.getSelectionMode() != ListSelectionModel.SINGLE_SELECTION);
            int i = (multisel ? list.getLeadSelectionIndex() : list.getSelectedIndex());

            if (i < 0) {
                return;
            }

            Point p = list.indexToLocation(i);

            if (p == null) {
                return;
            }

            createPopup(p.x, p.y, false);
        }

        public Object getValue(String key) {
            return null;
        }

        public void putValue(String key, Object value) {}

        public void setEnabled(boolean b) {}

        public boolean isEnabled() {
            // XXX should maybe use logic in {@link #run}?
            return true;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        public void removePropertyChangeListener(PropertyChangeListener listener) {}

    }

    private final class Listener implements ListDataListener, ListSelectionListener, PropertyChangeListener,
        VetoableChangeListener {
        Listener() {
        }

        /** Implements <code>ListDataListener</code> interface. */
        public void intervalAdded(ListDataEvent evt) {
            updateSelection();
        }

        /** Implements <code>ListDataListener</code>. */
        public void intervalRemoved(ListDataEvent evt) {
            updateSelection();
        }

        /** Implemetns <code>ListDataListener</code>. */
        public void contentsChanged(ListDataEvent evt) {
            updateSelection();
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] newNodes = (Node[]) evt.getNewValue();

                if (!selectionAccept(newNodes)) {
                    throw new PropertyVetoException("", evt); // NOI18N
                }
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                updateSelection();

                return;
            }

            if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals(evt.getPropertyName())) {
                setNode(manager.getExploredContext());

                //System.out.println("Children: " + java.util.Arrays.asList (list.getValues ())); // NOI18N
                return;
            }
        }

        public void valueChanged(ListSelectionEvent e) {
            int curSize = model.getSize();
            int[] indices = list.getSelectedIndices();

            // bugfix #24193, check if the nodes in selection are in the view's root context
            List<Node> ll = new ArrayList<Node>(indices.length);

            for (int i = 0; i < indices.length; i++) {
                if (indices[i] < curSize) {
                    Node n = Visualizer.findNode(model.getElementAt(indices[i]));

                    if ((n == manager.getRootContext()) || (n.getParentNode() != null)) {
                        ll.add(n);
                    }
                } else {
                    // something went wrong?
                    updateSelection();

                    return;
                }
            }

            Node[] nodes = ll.toArray(new Node[0]);

            // forwarding TO E.M., so we won't listen to its cries for a while
            manager.removePropertyChangeListener(wlpc);
            manager.removeVetoableChangeListener(wlvc);

            try {
                selectionChanged(nodes, manager);
            } catch (PropertyVetoException ex) {
                // selection vetoed - restore previous selection
                updateSelection();
            } finally {
                manager.addPropertyChangeListener(wlpc);
                manager.addVetoableChangeListener(wlvc);
            }
        }
    }

    // Backspace jumps to parent folder of explored context
    private final class GoUpAction extends AbstractAction {
        static final long serialVersionUID = 1599999335583246715L;

        public GoUpAction() {
            super("GoUpAction"); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            if (traversalAllowed) {
                Node pan = manager.getExploredContext();
                pan = pan.getParentNode();

                if (pan != null) {
                    manager.setExploredContext(pan, manager.getSelectedNodes());
                }
            }
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    //Enter key performObjectAt selected index.
    private final class EnterAction extends AbstractAction {
        static final long serialVersionUID = -239805141416294016L;

        public EnterAction() {
            super("Enter"); // NOI18N
        }

        public void actionPerformed(ActionEvent e) {
            int index = list.getSelectedIndex();
            performObjectAt(index, e.getModifiers());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
