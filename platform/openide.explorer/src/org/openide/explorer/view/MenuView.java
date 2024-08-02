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

import org.openide.explorer.*;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.nodes.*;
import org.openide.nodes.Node.Property;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;


import javax.swing.*;
import javax.swing.event.*;
import org.openide.util.ImageUtilities;


/** An explorer view that shows the context hierarchy in
* a popup menu. Initially, it shows a left button which opens a popup
* menu from the root context and a right button which opens a popup menu from the currently
* explored context.
 *
 * <p>
 * This class is a <em>view</em>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link ExplorerUtils}. Then just use 
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
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controler. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sence together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
 *
 *
 * @author  Ian Formanek, Jaroslav Tulach
 */
public class MenuView extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4970665063421766904L;

    /** default listener that opens explorer */
    static final NodeAcceptor DEFAULT_LISTENER = new NodeAcceptor() {
            public boolean acceptNodes(Node[] nodes) {
                // don't allow multiple selections
                if ((nodes == null) || (nodes.length != 1)) {
                    return false;
                }

                Node n = nodes[0];
                Action a = n.getPreferredAction();

                if ((a != null) && a.isEnabled()) {
                    a.actionPerformed(new ActionEvent(n, 0, "")); // NOI18N

                    return true;
                }

                return false;
            }
        };

    /** The explorerManager that manages this view */
    private transient ExplorerManager explorerManager;

    /** button to open root view */
    private JButton root;

    /** button to open view from current node */
    private JButton current;

    /** property change listener */
    private transient Listener listener;

    /* This is the constructor implementation
    * recommended by ExplorerView class that only calls the inherited
    * constructor and leaves the initialization for method initialize().
    * @see #initialize  */

    /** Construct a new menu view.
    */
    public MenuView() {
        setLayout(new java.awt.FlowLayout());

        root = new JButton(NbBundle.getMessage(MenuView.class, "MenuViewStartFromRoot"));
        add(root);

        current = new JButton(NbBundle.getMessage(MenuView.class, "MenuViewStartFromCurrent"));
        add(current);

        init();
    }

    /** Initializes listeners */
    private void init() {
        root.addMouseListener(listener = new Listener(true));
        current.addMouseListener(new Listener(false));
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        init();
    }

    /* Initializes view.
    */
    @Override
    public void addNotify() {
        super.addNotify();
        explorerManager = ExplorerManager.find(this);
        explorerManager.addPropertyChangeListener(listener);
        doChecks();
    }

    /* Deinitializes view.
    */
    @Override
    public void removeNotify() {
        super.removeNotify();
        explorerManager.removePropertyChangeListener(listener);
        explorerManager = null;
    }

    /** Does some checks */
    private void doChecks() {
        current.setEnabled(explorerManager.getSelectedNodes().length == 1);
    }

    /** Acceptor that can be passed to constructor of {@link MenuView.Menu}.
     * It permits determination of which nodes should be accepted upon a click.
     *
     * @deprecated This interface is almost the same as {@link NodeAcceptor}
     * so it is redundant and obsoleted. Use {@link NodeAcceptor}
     * interface instead.
     */
    public static @Deprecated interface Acceptor {
        /** Test whether to accept the node or not. Can also perform some actions (such as opening the node, etc.).
         * @param n the node
         * @return true if the <code>menu</code> should close
         * @deprecated whole interface is obsoleted, use {@link NodeAcceptor#acceptNodes} instead.
         */
        public @Deprecated boolean accept(Node n);
    }

    /** Listener that opens the menu and listens to its actions
    */
    private class Listener extends MouseAdapter implements NodeAcceptor, PropertyChangeListener {
        /** from root */
        private boolean root;

        public Listener(boolean root) {
            this.root = root;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getComponent().isEnabled()) {
                // open the popup menu
                Node context = null;

                if (!root) {
                    Node[] sel = explorerManager.getSelectedNodes();

                    if (sel.length > 0) {
                        context = sel[0];
                    }
                }

                if (context == null) {
                    context = explorerManager.getRootContext();
                }

                Menu menu = new Menu(context, listener);

                JPopupMenu popupMenu = menu.getPopupMenu();
                java.awt.Point p = new java.awt.Point(e.getX(), e.getY());
                p.x = e.getX() - p.x;
                p.y = e.getY() - p.y;
                SwingUtilities.convertPointToScreen(p, e.getComponent());

                Dimension popupSize = popupMenu.getPreferredSize();
                Rectangle screenBounds = Utilities.getUsableScreenBounds(getGraphicsConfiguration());

                if ((p.x + popupSize.width) > (screenBounds.x + screenBounds.width)) {
                    p.x = (screenBounds.x + screenBounds.width) - popupSize.width;
                }

                if ((p.y + popupSize.height) > (screenBounds.y + screenBounds.height)) {
                    p.y = (screenBounds.y + screenBounds.height) - popupSize.height;
                }

                SwingUtilities.convertPointFromScreen(p, e.getComponent());
                popupMenu.show(e.getComponent(), p.x, p.y);
            }
        }

        /** Is the set of nodes acceptable?
        * @param nodes the nodes to consider
        * @return <CODE>true</CODE> if so
        */
        public boolean acceptNodes(Node[] nodes) {
            // don't allow multiple selections
            if ((nodes == null) || (nodes.length != 1)) {
                return false;
            }

            Node n = nodes[0];
            Node parent = n.getParentNode();

            if (parent != null) {
                explorerManager.setExploredContext(parent, new Node[] { n });
            }

            return true;
        }

        public void propertyChange(PropertyChangeEvent ev) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(ev.getPropertyName())) {
                doChecks();
            }
        }
    }

    /** Menu item representing a node (with children) in a menu hierarchy.
     * One can attach an acceptor to the menu that will be informed
     * each time a user selects an item whether
     * to close the menu or not.
     * The submenu content is taken as a blocking snapshot of the Node's
     * Children at the time of the submenu popup and does not reflect
     * subsequent changes to Node's Children until next popup.
     * This means that the Node's Children should properly implement
     * blocking getNodes(true) in order to show proper content in MenuView.
     */
    public static class Menu extends org.openide.awt.JMenuPlus {
        static final long serialVersionUID = -1505289666675423991L;

        /** The node represented. */
        protected Node node;

        /** Action listener to attach to all menu items. */
        protected NodeAcceptor action;

        /** A boolean flag that notes the content was already created */
        private boolean filled = false;

        /** Constructor that assigns the node a default action.
         * For example, open the Explorer or a property sheet.
         * @param node node to represent
         */
        public Menu(Node node) {
            this(node, DEFAULT_LISTENER);
        }

        /** Constructor that permits specification of the action on the node.
         *
         * @param node node to represent
         * @param action action called when node is selected
         */
        public Menu(Node node, NodeAcceptor action) {
            this(node, action, true);
        }

        /** @deprecated use {@link #Menu(Node, NodeAcceptor)}
         */
        public @Deprecated Menu(Node node, Acceptor action) {
            this(node, new AcceptorProxy(action), true);
        }

        /** @deprecated use {@link #Menu(Node, NodeAcceptor, boolean)}
         */
        public @Deprecated Menu(Node node, Acceptor action, boolean setName) {
            this(node, new AcceptorProxy(action), setName);
        }

        /** Constructor that permits specification of the action on the node,
         * and permits overriding the name and icon of the menu.
         *
         * @param node node to represent
         * @param action action called when node selected
         * @param setName <code>true</code> to automatically set the name and icon of the item
         */
        public Menu(final Node node, NodeAcceptor action, boolean setName) {
            this.node = node;
            this.action = action;

            if (setName) {
                MenuItem.initialize(this, node);

                HelpCtx help = node.getHelpCtx();

                if ((help != null) && !help.equals(HelpCtx.DEFAULT_HELP) && (help.getHelpID() != null)) {
                    HelpCtx.setHelpIDString(this, help.getHelpID());
                }
            }
        }

        /** Overriden to fill the submenu with the real content lazily */
        public JPopupMenu getPopupMenu() {
            final JPopupMenu popup = super.getPopupMenu();
            fillSubmenu(popup);

            return popup;
        }

        private void fillSubmenu(JPopupMenu popup) {
            if (!filled) {
                filled = true;

                Helper h = new Helper(popup);

                Node[] nodes = node.getChildren().getNodes(true);

                // Fill in the popup.
                removeAll();

                for (int i = 0; i < nodes.length; i++)
                    add(createMenuItem(nodes[i]));

                // also work with empty element
                if (getMenuComponentCount() == 0) {
                    add(createEmptyMenuItem());
                }
            }
        }

        /** Checks for {@link MouseEvent#isPopupTrigger right click} to ask the acceptor whether
         * to accept the selection.
         * @param e the mouse event
         * @param path used by the superclass
         * @param manager used by the superclass
         */
        public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager manager) {
            super.processMouseEvent(e, path, manager);

            if (e.isPopupTrigger() && action.acceptNodes(new Node[] { node })) {
                MenuSelectionManager.defaultManager().clearSelectedPath();
            }
        }

        /** Helper method. Creates empty menu item. */
        private static JMenuItem createEmptyMenuItem() {
            JMenuItem empty = new JMenuItem(NbBundle.getMessage(MenuView.class, "EmptySubMenu"));

            empty.setEnabled(false);

            return empty;
        }

        /** Create a menu element for a node. The default implementation creates
         * {@link MenuView.MenuItem}s for leafs and <code>Menu</code> for other nodes.
         *
         * @param n node to create element for
         * @return the created node
         */
        protected JMenuItem createMenuItem(Node n) {
            return n.isLeaf() ? (JMenuItem) new MenuItem(n, action) : (JMenuItem) new Menu(n, action);
        }

        /** Little class that will reset our status on menu hide */
        private class Helper implements PopupMenuListener {
            private JPopupMenu popup;

            Helper(JPopupMenu master) {
                popup = master;
                popup.addPopupMenuListener(this);
            }

            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                filled = false; // clear the status and stop listening
                popup.removePopupMenuListener(this);
            }

            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }
        }
    }
     // End of class Menu.

    /** Proxy that allows to use now deprecated MenuView.Acceptor interface
     * on places where NodeAcceptor is requested.
     * This class can be deleted together with MenuView.Acceptor deletion.
     */
    private static final class AcceptorProxy implements NodeAcceptor {
        private Acceptor original;

        AcceptorProxy(Acceptor original) {
            this.original = original;
        }

        public boolean acceptNodes(Node[] nodes) {
            // don't allow multiple selections
            if ((nodes == null) || (nodes.length != 1)) {
                return false;
            }

            return original.accept(nodes[0]);
        }
    }
     // end of AcceptorProxy inner class

    /** Menu item that can represent one node in the tree. */
    public static class MenuItem extends JMenuItem implements HelpCtx.Provider {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -918973978614344429L;

        /** The node represented. */
        protected Node node;

        /** The action listener to attach to all menu items. */
        protected NodeAcceptor action;

        /** Construct item for given node with the node's default action.
         * @param node the node to represent
         */
        public MenuItem(Node node) {
            this(node, DEFAULT_LISTENER);
        }

        /** Construct item for given node, specifying an action.
         * @param node the node to represent
         * @param l the acceptor to decide whether to accept this node or not
         */
        public MenuItem(Node node, NodeAcceptor l) {
            this(node, l, true);
        }

        /** @deprecated Use proper constructor with (@link NodeAcceptor). */
        public @Deprecated MenuItem(Node node, Acceptor action) {
            this(node, new AcceptorProxy(action), true);
        }

        /** @deprecated Use proper constructor with (@link NodeAcceptor). */
        public @Deprecated MenuItem(Node node, Acceptor action, boolean setName) {
            this(node, new AcceptorProxy(action), setName);
        }

        /** Construct item for given node, specifying the action and whether to create the icon and name automatically.
         * @param node the node to represent
         * @param l the acceptor to decide whether to accept this node or not
         * @param setName <code>false</code> if the name and icon should not be set
         */
        public MenuItem(Node node, NodeAcceptor l, boolean setName) {
            super();

            this.node = node;
            this.action = l;

            if (setName) {
                initialize(this, node);
            }

            // [pnejedly] HelpCtx is now provided through HelpCtx.Provider
            // HelpCtx help = node.getHelpCtx ();
            // if (help != null && ! help.equals (HelpCtx.DEFAULT_HELP) && help.getHelpID () != null)
            //    HelpCtx.setHelpIDString (this, help.getHelpID ());
        }

        /**
         * @return HelpCtx of the underlying node.
         * @since 3.38
         */
        public HelpCtx getHelpCtx() {
            return node.getHelpCtx();
        }

        /** Inform the acceptor.
         * @param time see superclass
         */
        @Override
        public void doClick(int time) {
            action.acceptNodes(new Node[] { node });
        }

        /** Initialize an item for a node. */
        static void initialize(final JMenuItem item, final Node node) {
            final class NI implements Runnable, NodeListener, ItemListener {
                public void run() {
                    item.setIcon(ImageUtilities.image2Icon(node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16)));
                    item.setText(node.getDisplayName());

                    /*
                          item.setMargin(new java.awt.Insets(0, 0, 0, 0));
                          item.setHorizontalTextPosition(RIGHT);
                          item.setHorizontalAlignment(LEFT);
                    */
                }

                public void childrenAdded(NodeMemberEvent ev) {
                }

                public void childrenRemoved(NodeMemberEvent ev) {
                }

                public void childrenReordered(NodeReorderEvent ev) {
                }

                public void nodeDestroyed(NodeEvent ev) {
                }

                /** Update a visualizer (change of name, icon, description, etc.)
                */
                public void propertyChange(PropertyChangeEvent ev) {
                    if (Node.PROP_ICON.equals(ev.getPropertyName())) {
                        Mutex.EVENT.readAccess(this);

                        return;
                    }

                    if (Node.PROP_DISPLAY_NAME.equals(ev.getPropertyName())) {
                        Mutex.EVENT.readAccess(this);

                        return;
                    }
                }

                public void itemStateChanged(ItemEvent ev) {
                }
            }

            NI ni = new NI();

            // update this immediatelly
            ni.run();

            // attach the listener to the menu item, to prevent it from garbage
            // collection until the menu item exists
            item.addItemListener(ni);

            // listen to changes in node, but weakly, to allow garbage collection
            // event the node exists
            node.addNodeListener(NodeOp.weakNodeListener(ni, node));
        }
    }
}
