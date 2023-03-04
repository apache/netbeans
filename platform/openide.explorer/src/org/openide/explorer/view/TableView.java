/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Arrays;
import java.util.EventObject;
import java.util.Properties;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.TableColumnSelector;
import org.openide.awt.Mnemonics;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * Explorer view displaying nodes in a table.
 * @author David Strupl
 */
public class TableView extends JScrollPane {

    /** The table */
    private ETable table;
    /** Explorer manager, valid when this view is showing */
    private ExplorerManager manager;
    /** not null if popup menu enabled */
    transient PopupAdapter popupListener;
    /** the most important listener (on four types of events */
    transient TableSelectionListener managerListener = null;
    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;
    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;
    /** */
    private NodePopupFactory popupFactory;
    /** true if drag support is active */
    private transient boolean dragActive = true;

    /** true if drop support is active */
    private transient boolean dropActive = true;

    /** Drag support */
    transient TableViewDragSupport dragSupport;

    /** Drop support */
    transient TableViewDropSupport dropSupport;
    transient boolean dropTargetPopupAllowed = true;
//    transient private List storeSelectedPaths;

    // default DnD actions
    private transient int allowedDragActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    private transient int allowedDropActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    
    /** Creates a new instance of TableView */
    public TableView() {
        this(new NodeTableModel());
    }
    
    /** Creates a new instance of TableView */
    public TableView(NodeTableModel ntm) {
        table = new TableViewETable();
        table.setModel(ntm);
        SheetCell tableCell = new SheetCell.TableSheetCell(ntm, table);
        table.setDefaultRenderer(Node.Property.class, tableCell);
        table.setDefaultEditor(Node.Property.class, tableCell);
        setViewportView(table);
        setPopupAllowed(true);
        // do not care about focus
        setRequestFocusEnabled (false);
        table.setRequestFocusEnabled(true);
        getActionMap().put("org.openide.actions.PopupAction", new PopupAction());
        popupFactory = new NodePopupFactory();
        java.awt.Color c = javax.swing.UIManager.getColor("Table.background1");
        if (c == null) {
            c = javax.swing.UIManager.getColor("Table.background");
        }
        if (c != null) {
            getViewport().setBackground(c);
        }
        setDragSource(true);
        setDropTarget(true);
        TableColumnSelector tcs = Lookup.getDefault ().lookup (TableColumnSelector.class);
        if (tcs != null) {
            table.setColumnSelector(tcs);
        }

    }
    
    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public void requestFocus () {
        table.requestFocus();
    }
    
    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow () {
        return table.requestFocusInWindow();
    }
    
    /**
     * Getter for the embeded table component.
     */
    public ETable getTable() {
        return table;
    }
    
    /** Is it permitted to display a popup menu?
     * @return <code>true</code> if so
     */
    public boolean isPopupAllowed () {
        return popupListener != null;
    }

    /** Enable/disable displaying popup menus on tree view items.
    * Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed (boolean value) {
        if (popupListener == null && value) {
            // on
            popupListener = new PopupAdapter ();
            table.addMouseListener (popupListener);
            addMouseListener(popupListener);
            return;
        }
        if (popupListener != null && !value) {
            // off
            table.removeMouseListener (popupListener);
            removeMouseListener (popupListener);
            popupListener = null;
            return;
        }
    }
    
    /** Initializes the component and lookup explorer manager.
     */
    @Override
    public void addNotify () {
        super.addNotify ();
        lookupExplorerManager ();
    }
    
    /**
     * Method allowing to read stored values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void readSettings(Properties p, String propertyPrefix) {
        table.readSettings(p, propertyPrefix);
    }

    /**
     * Method allowing to store customization values.
     * The stored values should be only those that the user has customized,
     * it does not make sense to store the values that were set using 
     * the initialization code because the initialization code can be run
     * in the same way after restart.
     */
    public void writeSettings(Properties p, String propertyPrefix) {
        table.writeSettings(p, propertyPrefix);
    }

    /**
     * Allows customization of the popup menus.
     */
    public void setNodePopupFactory(NodePopupFactory newFactory) {
        popupFactory = newFactory;
    }
    
    /**
     * Getter for the current popup customizer factory.
     */
    public NodePopupFactory getNodePopupFactory() {
        return popupFactory;
    }
    
    /** Registers in the tree of components.
     */
    private void lookupExplorerManager () {
        // Enter key in the tree

        if (managerListener == null) {
            managerListener = new TableSelectionListener();
        }
        
        ExplorerManager newManager = ExplorerManager.find(this);
        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener (wlvc);
                manager.removePropertyChangeListener (wlpc);
            }

            manager = newManager;

            manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(managerListener, manager));

            synchronizeRootContext();
            synchronizeSelectedNodes ();
        }

        // Sometimes the listener is registered twice and we get the 
        // selection events twice. Removing the listener before adding it
        // should be a safe fix.
        table.getSelectionModel().removeListSelectionListener(managerListener);
        table.getSelectionModel().addListSelectionListener(managerListener);
    }
    
    /** Synchronize the root context from the manager of this Explorer.
    */
    final void synchronizeRootContext() {
        NodeTableModel ntm = (NodeTableModel)table.getModel();
        ntm.setNodes(manager.getRootContext().getChildren().getNodes());
    }

    /** Synchronize the selected nodes from the manager of this Explorer.
     */
    final void synchronizeSelectedNodes() {
        Node[] arr = manager.getSelectedNodes ();
        table.getSelectionModel().clearSelection();
        NodeTableModel ntm = (NodeTableModel)table.getModel();
        int size = ntm.getRowCount();
        int firstSelection = -1;
        for (int i = 0; i < size; i++) {
            Node n = getNodeFromRow(i);
            for (int j = 0; j < arr.length; j++) {
                if (n.equals(arr[j])) {
                    table.getSelectionModel().addSelectionInterval(i, i);
                    if (firstSelection == -1) {
                        firstSelection = i;
                    }
                }
            }
        }
        if (firstSelection >= 0) {
            Rectangle rect = table.getCellRect(firstSelection, 0, true);
            if (!getViewport().getViewRect().contains(rect.getLocation())) {
                rect.height = Math.max(rect.height, getHeight() - 30);
                table.scrollRectToVisible(rect);
            }
        }
    }
    
    /**
     * Deinitializes listeners.
     */
    @Override
    public void removeNotify () {
        super.removeNotify ();
        table.getSelectionModel().removeListSelectionListener(managerListener);
    }

    /**
     * Shows popup menu invoked on the table.
     */
    private void showPopup(int xpos, int ypos, final JPopupMenu popup) {
        if ((popup != null) && (popup.getSubElements().length > 0)) {
            final PopupMenuListener p = new PopupMenuListener() {
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    
                }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popup.removePopupMenuListener(this);
                    table.requestFocus();
                }
                public void popupMenuCanceled(PopupMenuEvent e) {
                    
                }
            };
            popup.addPopupMenuListener(p);
            popup.show(this, xpos, ypos);
        }
    }    
    
    /**
     * Find relevant actions and call the factory to create a popup.
     */
    private JPopupMenu createPopup(Point p) {
        int[] selRows = table.getSelectedRows();
        Node[] arr = new Node[selRows.length];
        for (int i = 0; i < selRows.length; i++) {
            arr[i] = getNodeFromRow(selRows[i]);
        }
        if (arr.length == 0) {
            // hack to show something even when no rows are selected
            arr = new Node[] { manager.getRootContext() };
        }

        p = SwingUtilities.convertPoint(this, p, table);
        int column = table.columnAtPoint(p);
        int row = table.rowAtPoint(p);
        return popupFactory.createPopupMenu(row, column, arr, table);
    }
    
    /**
     * 
     */
    Node getNodeFromRow(int rowIndex) {
        int row = table.convertRowIndexToModel(rowIndex);
        TableModel tm = table.getModel();
        if (tm instanceof NodeTableModel) {
            NodeTableModel ntm = (NodeTableModel)tm;
            return ntm.nodeForRow(row);
        }
        return null;
    }
    
    /** Returns the point at which the popup menu is to be shown. May return null.
     * @return the point or null
     */    
    private Point getPositionForPopup () {
        int i = table.getSelectionModel().getLeadSelectionIndex();
        if (i < 0) return null;
        int j = table.getColumnModel().getSelectionModel().getLeadSelectionIndex();
        if (j < 0) {
            j = 0;
        }

        Rectangle rect = table.getCellRect(i, j, true);
        if (rect == null) return null;

        Point p = new Point(rect.x + rect.width / 3,
                rect.y + rect.height / 2);
        
        // bugfix #36984, convert point by TableView.this
        p =  SwingUtilities.convertPoint (table, p, TableView.this);

        return p;
    }

    /**
     * Action registered in the component's action map.
     */
    private class PopupAction extends javax.swing.AbstractAction implements Runnable {
        public void actionPerformed(ActionEvent evt) {
            SwingUtilities.invokeLater(this);
        }
        public void run() {
            Point p = getPositionForPopup ();
            if (p == null) {
                return ;
            }
            if (isPopupAllowed()) {
                JPopupMenu pop = createPopup(p);
                showPopup(p.x, p.y, pop);
            }
        }
    };
    
    /**
     * Mouse listener that invokes popup.
     */
    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

	PopupAdapter() {}
	
        protected void showPopup (MouseEvent e) {
            int selRow = table.rowAtPoint(e.getPoint());

            if (selRow != -1) {
                if (! table.getSelectionModel().isSelectedIndex(selRow)) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
            } else {
                table.getSelectionModel().clearSelection();
            }
            Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), TableView.this);
            if (isPopupAllowed()) {
                JPopupMenu pop = createPopup(p);
                TableView.this.showPopup(p.x, p.y, pop);
                e.consume();
            }
        }
    }
    
    /**
     * Called when selection in tree is changed.
     */
    final void callSelectionChanged (Node[] nodes) {
        manager.removePropertyChangeListener (wlpc);
        manager.removeVetoableChangeListener (wlvc);
        try {
            manager.setSelectedNodes(nodes);
        } catch (PropertyVetoException e) {
            synchronizeSelectedNodes ();
        } finally {
            manager.addPropertyChangeListener (wlpc);
            manager.addVetoableChangeListener (wlvc);
        }
    }
    
    /** 
     * Check if selection of the nodes could break
     * the selection mode set in the ListSelectionModel.
     * @param nodes the nodes for selection
     * @return true if the selection mode is broken
     */
    private boolean isSelectionModeBroken(Node[] nodes) {
        
        // if nodes are empty or single then everthing is ok
        // or if discontiguous selection then everthing ok
        if (nodes.length <= 1 || table.getSelectionModel().getSelectionMode() == 
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION) {
            return false;
        }

        // if many nodes
        
        // breaks single selection mode
        if (table.getSelectionModel().getSelectionMode() == 
            ListSelectionModel.SINGLE_SELECTION) {
            return true;
        }
        
        // check the contiguous selection mode

        // check selection's rows
        
        // all is ok
        return false;
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
            dragSupport = new TableViewDragSupport(this, table);
        }

        // activate / deactivate support according to the state
        dragActive = state;

        if (dragSupport != null) {
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
            dropSupport = new TableViewDropSupport(this, table, dropTargetPopupAllowed);
        }

        // activate / deactivate support according to the state
        dropActive = state;

        if (dropSupport != null) {
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
    
    /**
     * Listener attached to the explorer manager and also to the
     * changes in the table selection.
     */
    private class TableSelectionListener implements VetoableChangeListener, ListSelectionListener, PropertyChangeListener {
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (manager == null) return; // the tree view has been removed before the event got delivered
            if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
                synchronizeRootContext();
            }
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                synchronizeSelectedNodes();
            }
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent listSelectionEvent) {
            int selectedRows[] = table.getSelectedRows();
            Node []selectedNodes = new Node[selectedRows.length];
            for (int i = 0; i < selectedNodes.length;i++) {
                selectedNodes[i] = getNodeFromRow(selectedRows[i]);
            }
            callSelectionChanged(selectedNodes);
        }

        public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                // issue 11928 check if selecetion mode will be broken
                Node[] nodes = (Node[])evt.getNewValue();
                if (isSelectionModeBroken(nodes)) {
                    throw new PropertyVetoException("selection mode " +  " broken by " + Arrays.asList(nodes), evt); // NOI18N
                }
            }
        }
    }

    /**
     * Extension of the ETable that allows adding a special comparator
     * for sorting the rows.
     */
    private static class TableViewETable extends ETable {
        public TableViewETable() {
            super();
        }
        @Override
        protected TableColumn createColumn(int modelIndex) {
            return new TableViewETableColumn(modelIndex);
        }

        @Override
        public Object transformValue(Object value) {
            if (value instanceof ETableColumn) {
                ETableColumn c = (ETableColumn) value;
                return c.getHeaderValue ().toString ();
            } else if (value instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) value;
                Mnemonics.setLocalizedText (b, b.getText ());
                return b;
            } else if (value instanceof VisualizerNode) {
                return Visualizer.findNode (value);
            }
            return PropertiesRowModel.getValueFromProperty(value);
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public boolean editCellAt(int row, int column, EventObject e) {
            Object o = getValueAt(row, column);
            if (o instanceof Node.Property) { // && (e == null || e instanceof KeyEvent)) {
                //Toggle booleans without instantiating an editor
                Node.Property p = (Node.Property)o;
                if (!p.canWrite()) {
                    return false;
                }
                if (p.getValueType() == Boolean.class || p.getValueType() == Boolean.TYPE) {
                    PropertiesRowModel.toggleBooleanProperty(p);
                    Rectangle r = getCellRect(row, column, true);
                    repaint (r.x, r.y, r.width, r.height);
                    return false;
                }
            }
            return super.editCellAt(row, column, e);
        }
        
        /**
         */
        private class TableViewETableColumn extends ETableColumn {
            private String tooltip;
            public TableViewETableColumn(int index) {
                super(index, TableViewETable.this);
            }
            @Override
            public boolean isSortingAllowed() {
                TableModel model = getModel();
                if (model instanceof NodeTableModel) {
                    NodeTableModel ntm = (NodeTableModel)model;
                    return ntm.isComparableColumn(getModelIndex());
                }
                return true;
            }

            final String getShortDescription (String defaultValue) {
                TableModel model = getModel();
                if (model.getRowCount() <= 0) {
                    return null;
                }
                if (0 == getModelIndex()) {
                    // 1st column
                    return defaultValue;
                }

                if (model instanceof NodeTableModel) {
                    NodeTableModel ntm = (NodeTableModel)model;
                    Node.Property propertyForColumn = ntm.propertyForColumn(getModelIndex());
                    return propertyForColumn.getShortDescription();
                }
                return defaultValue;
            }

            @Override
            protected TableCellRenderer createDefaultHeaderRenderer() {
                TableCellRenderer orig = super.createDefaultHeaderRenderer();
                TableViewHeaderRenderer ovohr = new TableViewHeaderRenderer(orig);
                return ovohr;
            }

            /** This is here to compute and set the header tooltip. */
            class TableViewHeaderRenderer implements TableCellRenderer {
                private TableCellRenderer orig;
                public TableViewHeaderRenderer(TableCellRenderer delegate) {
                    orig = delegate;
                }
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component oc = orig.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (tooltip == null) {
                        tooltip = getShortDescription(value.toString());
                    }
                    if ((tooltip != null) && (oc instanceof JComponent)) {
                        JComponent jc = (JComponent)oc;
                        jc.setToolTipText(tooltip);
                    }
                    return oc;
                }
            }
        }
    }
}
