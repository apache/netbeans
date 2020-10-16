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

package org.netbeans.modules.viewmodel;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;

import org.netbeans.spi.viewmodel.DnDNodeModel;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.awt.Actions;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.NodeNotFoundException;
import org.openide.nodes.NodeOp;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;


/**
 * Implements table visual representation of the data from models, using Outline view.
 *
 * @author   Martin Entlicher
 */
public class OutlineTable extends JPanel implements
ExplorerManager.Provider, PropertyChangeListener {

    private static final Logger logger = Logger.getLogger(OutlineTable.class.getName());
    
    private ExplorerManager     explorerManager;
    final MyTreeTable           treeTable; // Accessed from tests
    Node.Property[]             columns; // Accessed from tests
    private TableColumn[]       tableColumns;
    private int[]               columnVisibleMap; // Column index -> visible index
    private boolean             ignoreCreateDefaultColumnsFromModel;
    //private IndexedColumn[]     icolumns;
    private boolean             isDefaultColumnAdded;
    private int                 defaultColumnIndex; // The index of the tree column
    private boolean             ignoreMove; // Whether to ignore column movement events
    private boolean             isSettingModelUp; // Whether a model is being set up
    //private List                expandedPaths = new ArrayList ();
    TreeModelRoot               currentTreeModelRoot; // Accessed from test
    
    //private TreeTableView       ttv;
    //private TreeView            tv;
    
    public OutlineTable () {
        setLayout (new BorderLayout ());
            treeTable = new MyTreeTable ();
            treeTable.getOutline().setRootVisible (false);
            treeTable.setVerticalScrollBarPolicy 
                (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            treeTable.setHorizontalScrollBarPolicy 
                (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            treeTable.setTreeHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add (treeTable, "Center");  //NOI18N
//        ttv = new TreeTableView(); // To test only
//        add(ttv, "East");
//        tv = new BeanTreeView(); // To test only
//        add(tv, "West");
        treeTable.getTable().addPropertyChangeListener("createdDefaultColumnsFromModel", new CreatedDefaultColumnsFromModel());
        treeTable.getTable().getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            // Track column visibility changes.
            //   No impact on order property
            //   Change visibility map

            @Override
            public void columnAdded(TableColumnModelEvent e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("columnAdded("+e+") to = "+e.getToIndex());
                    //logger.log(Level.FINE, "  called from", new IllegalStateException("TEST"));
                    TableColumnModel tcme = (TableColumnModel) e.getSource();
                    logger.fine(" column header = '"+tcme.getColumn(e.getToIndex()).getHeaderValue()+"'");
                    dumpColumnVisibleMap();
                }
                if (tableColumns != null && e.getToIndex() >= 0) {
                    // It does not say *which* column was added to the toIndex.
                    int visibleIndex = e.getToIndex();
                    int columnIndex = -1;
                    TableColumnModel tcm = treeTable.getTable().getColumnModel();
                    ETableColumnModel ecm = (ETableColumnModel) tcm;
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (tableColumns[i] != null) {
                            boolean wasHidden = columns[i].isHidden();
                            boolean isHidden = ecm.isColumnHidden(tableColumns[i]);
                            if (wasHidden == true && isHidden == false) {
                                columnIndex = i;
                                break;
                            }
                        }
                    }
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("  to index = "+visibleIndex+", column index = "+columnIndex);
                    }
                    if (columnIndex != -1) {
                        int prefferedVisibleIndex = columnVisibleMap[columnIndex];
                        // check if there's a visible column with the same visible index and lower order
                        int columnVisibleIndex = prefferedVisibleIndex;
                        int corder = getColumnOrder(columns[columnIndex]);
                        for (int i = 0; i < columnVisibleMap.length; i++) {
                            if (columnVisibleMap[i] == prefferedVisibleIndex) {
                                if (corder > getColumnOrder(columns[i]) && !columns[i].isHidden()) {
                                    prefferedVisibleIndex++;
                                    break;
                                }
                            }
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("  to index = "+visibleIndex+", column = "+columns[columnIndex].getDisplayName()+", columnVisibleIndex = "+columnVisibleIndex+", prefferedVisibleIndex = "+prefferedVisibleIndex);
                        }
                        columns[columnIndex].setHidden(false);
                        columnVisibleMap[columnIndex] = prefferedVisibleIndex;
                        for (int i = 0; i < columnVisibleMap.length; i++) {
                            if (columnVisibleMap[i] >= columnVisibleIndex && i != columnIndex &&
                                getColumnOrder(columns[i]) >= corder) {
                                
                                columnVisibleMap[i]++;
                            }
                        }
                        if (logger.isLoggable(Level.FINE)) {
                            dumpColumnVisibleMap();
                        }
                        if (prefferedVisibleIndex >= 0 && prefferedVisibleIndex != visibleIndex) {
                            if (logger.isLoggable(Level.FINE)) {
                                logger.fine("moveColumn("+visibleIndex+", "+prefferedVisibleIndex+")");
                            }
                            ignoreMove = true;
                            try {
                                treeTable.getTable().getColumnModel().moveColumn(visibleIndex, prefferedVisibleIndex);
                            } finally {
                                ignoreMove = false;
                            }
                        }
                    }
                }
                if (logger.isLoggable(Level.FINE)) {
                    dumpColumnVisibleMap();
                    logger.fine("columnAdded() done.");
                }
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("columnRemoved("+e+") from = "+e.getFromIndex());
                    //logger.log(Level.FINE, "  called from", new IllegalStateException("TEST"));
                    dumpColumnVisibleMap();
                }
                if (tableColumns != null && e.getFromIndex() >= 0) {
                    int visibleIndex = e.getFromIndex();
                    logger.log(Level.FINE, "  from index = {0}", visibleIndex);
                    int columnIndex = getColumnIndex(visibleIndex);
                    if (columnIndex != -1) {
                        columns[columnIndex].setHidden(true);
                        for (int i = 0; i < columnVisibleMap.length; i++) {
                            if (columnVisibleMap[i] >= visibleIndex && columnVisibleMap[i] > 0) {
                                columnVisibleMap[i]--;
                            }
                        }
                    }
                }
                if (logger.isLoggable(Level.FINE)) {
                    dumpColumnVisibleMap();
                    logger.fine("columnRemoved() done.");
                }
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (tableColumns == null || ignoreMove) {
                    return ;
                }
                int from = e.getFromIndex();
                int to = e.getToIndex();
                if (from == to) {
                    // Ignore Swing strangeness
                    return ;
                }
                int fc = getColumnIndex(from);
                int tc = getColumnIndex(to);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("columnMoved("+e+") from = "+from+", to = "+to);
                    logger.fine("  from = "+from+", to = "+to);
                    logger.fine("  fc = "+fc+", tc = "+tc);
                    TableColumnModel tcme = (TableColumnModel) e.getSource();
                    logger.fine(" column headers = '"+tcme.getColumn(e.getFromIndex()).getHeaderValue()+"' => '"+tcme.getColumn(e.getToIndex()).getHeaderValue()+"'");
                    dumpColumnVisibleMap();
                }
                
                int toColumnOrder = getColumnOrder(columns[tc]);
                int fromColumnOrder = getColumnOrder(columns[fc]);
                setColumnOrder(columns[fc], toColumnOrder);
                setColumnOrder(columns[tc], fromColumnOrder);
                fromColumnOrder = columnVisibleMap[fc];
                columnVisibleMap[fc] = columnVisibleMap[tc];
                columnVisibleMap[tc] = fromColumnOrder;

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("After move:");
                    dumpColumnVisibleMap();
                }
            }
            
            @Override
            public void columnMarginChanged(ChangeEvent e) {}
            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {}
        });
        ActionMap map = getActionMap();
        ExplorerManager manager = getExplorerManager();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, false));
        setFocusable(false);
    }

    private void dumpColumnVisibleMap() {
        logger.fine("");
        logger.fine("Column Visible Map ("+columnVisibleMap.length+"):");
        for (int i = 0; i < columnVisibleMap.length; i++) {
            logger.fine(" {"+columns[i].getDisplayName()+"} \tvisible map["+i+"] = "+columnVisibleMap[i]+"; columnOrder["+i+"] = "+getColumnOrder(columns[i])+"\t"+(columns[i].isHidden() ? "hidden" : ""));
        }
        logger.fine("");
    }

    private int getColumnOrder(Node.Property column) {
        Integer order = (Integer) column.getValue(Column.PROP_ORDER_NUMBER);
        if (order == null) {
            return -1;
        } else {
            return order.intValue();
        }
    }

    private void setColumnOrder(Node.Property column, int order) {
        column.setValue(Column.PROP_ORDER_NUMBER, order);
        if (order != getColumnOrder(column)) {
            Exceptions.printStackTrace(new IllegalStateException("The order "+order+" could not be set to column "+column));
        }
    }

    private int getColumnIndex(int visibleIndex) {
        for (int i = 0; i < columnVisibleMap.length; i++) {
            if (visibleIndex == columnVisibleMap[i] && !columns[i].isHidden()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Set list of models.
     * Columns are taken from the first model. Children are listed
     * @param models
     */
    public void setModel (Models.CompoundModel model) {
        setModel(model, null);
    }
    
    /**
     * Set list of models.
     * Columns are taken from the first model. Children are listed
     * @param models
     */
    public void setModel (Models.CompoundModel model, MessageFormat treeNodeDisplayFormat) {
        isSettingModelUp = true;
        try {
        // 2) save current settings (like columns, expanded paths)
        //List ep = treeTable.getExpandedPaths ();
        if (currentTreeModelRoot == null || currentTreeModelRoot.getTreeNodeDisplayFormat() == null) {
            saveWidths ();
            saveSortedState();
        }
        
        //this.model = model;
        
        // 1) destroy old model
        if (currentTreeModelRoot != null) {
            currentTreeModelRoot.destroy ();
            currentTreeModelRoot = null;
        }
        
        // 3) no model => set empty root node & return
        if (model == null) {
            getExplorerManager ().setRootContext (
                new AbstractNode (Children.LEAF)
            );
            return;
        }
        
        // 4) set columns for given model
        String[] nodesColumnName = new String[] { null, null };
        ColumnModel[] cs = model.getColumns ();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setModel(): creating columns: ("+cs.length+")");
            for (int i = 0; i < cs.length; i++) {
                logger.fine("  ColumnModel["+i+"] = "+cs[i].getDisplayName()+", ID = "+cs[i].getID()+", visible = "+cs[i].isVisible());
            }
        }
        Node.Property[] columnsToSet = createColumns (cs, nodesColumnName);
        ignoreCreateDefaultColumnsFromModel = true;
        treeTable.setNodesColumnName(nodesColumnName[0], nodesColumnName[1]);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setModel(): setNodesColumnName("+Arrays.toString(nodesColumnName)+") done");
        }
        currentTreeModelRoot = new TreeModelRoot (model, treeTable);
        currentTreeModelRoot.setTreeNodeDisplayFormat(treeNodeDisplayFormat);
        TreeModelNode rootNode = currentTreeModelRoot.getRootNode ();
        getExplorerManager ().setRootContext (rootNode);
        // The root node must be ready when setting the columns
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setModel(): setProperties("+Arrays.toString(columnsToSet)+")");
        }
        if (treeNodeDisplayFormat == null) {
            treeTable.setProperties (columnsToSet);
            updateTableColumns(columnsToSet, null);
        } else {
            treeTable.setProperties (new Property[]{});
        }
        ignoreCreateDefaultColumnsFromModel = false;
        treeTable.setAllowedDragActions(model.getAllowedDragActions());
        treeTable.setAllowedDropActions(model.getAllowedDropActions(null));
        treeTable.setDynamicDropActions(model);

        //treeTable.getTable().tableChanged(new TableModelEvent(treeTable.getOutline().getModel()));
        //getExplorerManager ().setRootContext (rootNode);
        
        // 5) set root node for given model
        // Moved to 4), because the new root node must be ready when setting columns

        // 6) update column widths & expanded nodes
        if (treeNodeDisplayFormat == null) {
            updateColumnWidthsAndSorting();
        }
        //treeTable.expandNodes (expandedPaths);
        // TODO: this is a workaround, we should find a better way later
        /* We must not call children here - it can take a long time...
         * the expansion is performed in TreeModelNode.TreeModelChildren.applyChildren()
        final List backupPath = new ArrayList (expandedPaths);
        if (backupPath.size () == 0)
            TreeModelNode.getRequestProcessor ().post (new Runnable () {
                public void run () {
                    try {
                        final Object[] ch = TreeTable.this.model.getChildren 
                            (TreeTable.this.model.getRoot (), 0, 0);
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                expandDefault (ch);
                            }
                        });
                    } catch (UnknownTypeException ex) {}
                }
            });
        else
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    treeTable.expandNodes (backupPath);
                }
            });
         */
        //if (ep.size () > 0) expandedPaths = ep;

        // Sort of hack(?) After close/open of the view the table becomes empty,
        // it looks like the root node stays unexpanded for some reason.
        //treeTable.expandNode(rootNode);
        } finally {
            isSettingModelUp = false;
        }
    }
    
    /**
     * Set list of models.
     * Columns are taken from the first model. Children are listed
     * @param models
     */
    public void setModel (HyperCompoundModel model, MessageFormat treeNodeDisplayFormat) {
        isSettingModelUp = true;
        try {
        // 2) save current settings (like columns, expanded paths)
        //List ep = treeTable.getExpandedPaths ();
        if (currentTreeModelRoot == null || currentTreeModelRoot.getTreeNodeDisplayFormat() == null) {
            saveWidths ();
            saveSortedState();
        }

        //this.model = model;

        // 1) destroy old model
        if (currentTreeModelRoot != null) {
            currentTreeModelRoot.destroy ();
            currentTreeModelRoot = null;
        }

        // 3) no model => set empty root node & return
        if (model == null) {
            getExplorerManager ().setRootContext (
                new AbstractNode (Children.LEAF)
            );
            return;
        }

        // 4) set columns for given model
        String[] nodesColumnName = new String[] { null, null };
        ColumnModel[] cs = model.getColumns ();
        Node.Property[] columnsToSet = createColumns (cs, nodesColumnName);
        ignoreCreateDefaultColumnsFromModel = true;
        treeTable.setNodesColumnName(nodesColumnName[0], nodesColumnName[1]);
        currentTreeModelRoot = new TreeModelRoot (model, treeTable);
        currentTreeModelRoot.setTreeNodeDisplayFormat(treeNodeDisplayFormat);
        TreeModelNode rootNode = currentTreeModelRoot.getRootNode ();
        getExplorerManager ().setRootContext (rootNode);
        // The root node must be ready when setting the columns
        if (treeNodeDisplayFormat == null) {
            treeTable.setProperties (columnsToSet);
            updateTableColumns(columnsToSet, null);
        } else {
            treeTable.setProperties (new Property[]{});
        }
        ignoreCreateDefaultColumnsFromModel = false;
        treeTable.setAllowedDragActions(model.getAllowedDragActions());
        treeTable.setAllowedDropActions(model.getAllowedDropActions(null));

        // 5) set root node for given model
        // Moved to 4), because the new root node must be ready when setting columns

        // 6) update column widths & expanded nodes
        if (treeNodeDisplayFormat == null) {
            updateColumnWidthsAndSorting();
        }
        /* We must not call children here - it can take a long time...
         * the expansion is performed in TreeModelNode.TreeModelChildren.applyChildren()
         */
        } finally {
            isSettingModelUp = false;
        }
    }

    @Override
    public ExplorerManager getExplorerManager () {
        if (explorerManager == null) {
            explorerManager = new ExplorerManager ();
        }
        return explorerManager;
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName ();
        TopComponent tc = (TopComponent) SwingUtilities.
            getAncestorOfClass (TopComponent.class, this);
        if (tc == null) {
            return;
        }
        if (propertyName.equals (TopComponent.Registry.PROP_CURRENT_NODES)) {
            ExplorerUtils.activateActions(getExplorerManager(), equalNodes());
        } else
        if (propertyName.equals (ExplorerManager.PROP_SELECTED_NODES)) {
            tc.setActivatedNodes ((Node[]) evt.getNewValue ());
        }
    }
    
    private boolean equalNodes () {
        Node[] ns1 = TopComponent.getRegistry ().getCurrentNodes ();
        Node[] ns2 = getExplorerManager ().getSelectedNodes ();
        if (ns1 == ns2) {
            return true;
        }
        if ( (ns1 == null) || (ns2 == null) ) {
            return false;
        }
        if (ns1.length != ns2.length) {
            return false;
        }
        int i, k = ns1.length;
        for (i = 0; i < k; i++) {
            if (!ns1 [i].equals (ns2 [i])) {
                return false;
            }
        }
        return true;
    }
    
    private Node.Property[] createColumns (ColumnModel[] cs, String[] nodesColumnNameAndDescription) {
        int i, k = cs.length;
        // Check column IDs:
        {
            Map<String, ColumnModel> IDs = new HashMap<String, ColumnModel>(k);
            for (i = 0; i < k; i++) {
                String id = cs[i].getID();
                if (IDs.containsKey(id)) {
                    ColumnModel csi = IDs.get(id);
                    logger.severe("\nHave two columns with identical IDs \""+id+"\": "+csi+" ["+csi.getDisplayName()+"] and "+cs[i]+" ["+cs[i].getDisplayName()+"]\n");
                } else {
                    IDs.put(id, cs[i]);
                }
            }
        }
        columns = new Column[k];
        //icolumns = new IndexedColumn[k];
        columnVisibleMap = new int[k];
        isDefaultColumnAdded = false;
        ColumnModel treeColumn = null;
        boolean addDefaultColumn = true;
        List<Node.Property> columnList = new ArrayList<Node.Property>(k);
        int d = 0;
        boolean[] originalOrder = new boolean[k];
        for (i = 0; i < k; i++) {
            Column c = new Column(cs [i]);
            columns[i] = c;
            //IndexedColumn ic = new IndexedColumn(c, i, cs[i].getCurrentOrderNumber());
            //icolumns[i] = ic;
            int order = cs[i].getCurrentOrderNumber();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("createColumns(): column {"+c.getDisplayName()+"}: order = "+order+", i = "+i+", d = "+d);
            }
            if (order == -1) {
                order = i;
            } else {
                originalOrder[i] = true;
            }
            order += d;
            columnVisibleMap[i] = order;
            if (cs[i].getType() != null) {
                columnList.add(c);
            } else {
                treeColumn = cs[i];
                nodesColumnNameAndDescription[0] = Actions.cutAmpersand(cs[i].getDisplayName());
                nodesColumnNameAndDescription[1] = cs[i].getShortDescription();
                addDefaultColumn = false;
                defaultColumnIndex = i;
                if (cs[i].getCurrentOrderNumber() == -1) {
                    // By default let this be the first column and increase the orders
                    columnVisibleMap[i] = 0;
                    for (int j = 0; j < i; j++) {
                        columnVisibleMap[j]++;
                    }
                    d = 1;
                }
                c.setHidden(false); // The tree column can not be hidden
            }
        }
        if (addDefaultColumn) {
            PropertySupport.ReadWrite[] columns2 =
                new PropertySupport.ReadWrite [columns.length + 1];
            System.arraycopy (columns, 0, columns2, 1, columns.length);
            columns2 [0] = new DefaultColumn ();
            nodesColumnNameAndDescription[0] = columns2[0].getDisplayName();
            nodesColumnNameAndDescription[1] = columns2[0].getShortDescription();
            columns = columns2;
            int[] columnVisibleMap2 = new int[columnVisibleMap.length + 1];
            columnVisibleMap2[0] = 0;
            for (i = 0; i < k; i++) {
                columnVisibleMap2[i + 1] = columnVisibleMap[i] + 1;
            }
            columnVisibleMap = columnVisibleMap2;
            isDefaultColumnAdded = true;
            defaultColumnIndex = 0;
        }
        if (treeColumn != null) {
            treeTable.setTreeSortable(treeColumn.isSortable());
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createColumns(): columns before checkOrder()");
            dumpColumnVisibleMap();
        }
        // Check visible map (order) for duplicities and gaps
        checkOrder(columnVisibleMap, originalOrder);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createColumns(): columns after checkOrder()");
            dumpColumnVisibleMap();
        }

        int[] columnOrder = new int[columnVisibleMap.length];
        System.arraycopy(columnVisibleMap, 0, columnOrder, 0, columnOrder.length);

        for (i = 0; i < columnVisibleMap.length; i++) {
            setColumnOrder(columns[i], columnOrder[i]);
            if (columns[i].isHidden()) {
                int order = columnOrder[i];
                for (int j = 0; j < columnVisibleMap.length; j++) {
                    if (columnOrder[j] >= order && columnVisibleMap[j] > 0) {
                        columnVisibleMap[j]--;
                    }
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createColumns:");
            dumpColumnVisibleMap();
        }

        Node.Property[] columnProps = columnList.toArray(new Node.Property[]{});
        tableColumns = null;
        return columnProps;
    }

    /** Squeeze gaps and split duplicities to make it a permutation. */
    private void checkOrder(int[] orders, boolean[] originalOrder) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("checkOrder(");
            for (int i = 0; i < orders.length; i++) {
                msg.append(orders[i]);
                msg.append(", ");
            }
            msg.append("\b\b)");
            logger.fine(msg.toString());
        }
        int n = orders.length;
        // Find and squeeze gaps:
        for (int i = 0; i < n; i++) {
            // Check if 'i' order is there:
            int min = Integer.MAX_VALUE;
            int j;
            for (j = 0; j < n; j++) {
                if (i == orders[j]) {
                    break;
                } else if (orders[j] > i) {
                    min = Math.min(min, orders[j]);
                }
            }
            if (j == n && min != Integer.MAX_VALUE) {
                // 'i' not found, shift:
                int shift = min - i;
                for (j = 0; j < n; j++) {
                    if (orders[j] > i) {
                        orders[j] -= shift;
                    }
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("  squeezed: ");
            for (int i = 0; i < orders.length; i++) {
                msg.append(orders[i]);
                msg.append(", ");
            }
            msg.append("\b\b)");
            logger.fine(msg.toString());
        }
        // Find and split duplicities:
        int[] duplicates = new int[n];
        for (int i = 0; i < n; i++) {
            int d = ++duplicates[orders[i]];
            if (d > 1) {
                int o = orders[i];
                boolean isOriginalOrder = originalOrder[i];
                boolean shiftedOther = false;
                for (int j = 0; j < n; j++) {
                    if (orders[j] > o || orders[j] == o) {
                        if (orders[j] == o) {
                            if (shiftedOther) {
                                continue;
                            }
                            // If the current duplicity has the original order
                            // and the other has not, shift the other.
                            if (j < i && isOriginalOrder && !originalOrder[j]) {
                                shiftedOther = true;
                            } else if (j < i) {
                                // Otherwise we will do the shift when j == i.
                                continue;
                            }
                        }
                        if (j <= i) {
                            duplicates[orders[j]]--;
                        }
                        orders[j]++;
                        if (j <= i) {
                            duplicates[orders[j]]++;
                        }
                    }
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder msg = new StringBuilder("  splitted: ");
            for (int i = 0; i < orders.length; i++) {
                msg.append(orders[i]);
                msg.append(", ");
            }
            msg.append("\b\b)");
            logger.fine(msg.toString());
        }
    }

    private void updateTableColumns(Property[] columnsToSet, TableColumn[] newTColumns) {
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        List<TableColumn> allColumns = getAllColumns(ecm);
        //int d = (isDefaultColumnAdded) ? 1 : 0;
        int ci = 0;
        int tci = 0;//d;
        TableColumn[] tableColumns = new TableColumn[columns.length];
        if (defaultColumnIndex > 0) {
            tci++;
        }
        for (int i = 0; i < columns.length; i++) {
            if (ci < columnsToSet.length && columns[i] == columnsToSet[ci] && i != defaultColumnIndex) {
                TableColumn tc = allColumns.get(tci); //tcm.getColumn(tci);
                tableColumns[i] = tc;
                if (columns[i] instanceof Column) {
                    Column c = (Column) columns[i];
                    TableCellEditor cellEditor = tc.getCellEditor();
                    if (cellEditor == null) {
                        cellEditor = treeTable.getTable().getDefaultEditor(Node.Property.class);
                    }
                    tc.setCellEditor(new DelegatingCellEditor(
                            c.getName(),
                            cellEditor));
                    TableCellRenderer cellRenderer = tc.getCellRenderer();
                    if (cellRenderer == null) {
                        cellRenderer = treeTable.getTable().getDefaultRenderer(Node.Property.class);
                    }
                    tc.setCellRenderer(new DelegatingCellRenderer(
                            c.getName(),
                            cellRenderer));
                    tc.setPreferredWidth(c.getColumnWidth());
                }
                if (columns[i].isHidden()) {
                    ecm.setColumnHidden(tc, true);
                } else {
                    if (columns[i] instanceof Column) {
                        Column c = (Column) columns[i];
                        tc.setPreferredWidth(c.getColumnWidth());
                    }
                }
                tci++;
                ci++;
            } else {
                TableColumn tc = allColumns.get(0); //tcm.getColumn(0);
                tableColumns[i] = tc;
                if (columns[i] instanceof Column) {
                    Column c = (Column) columns[i];
                    tc.setCellEditor(c.getTableCellEditor());
                    tc.setPreferredWidth(c.getColumnWidth());
                }
                String name = tc.getHeaderValue().toString();
                tc.setCellEditor(new DelegatingCellEditor(
                        name,
                        treeTable.getTable().getCellEditor(0, 0)));
                tc.setCellRenderer(new DelegatingCellRenderer(
                        name,
                        treeTable.getTable().getCellRenderer(0, 0)));
                if (defaultColumnIndex == 0) {
                    tci++;
                }
            }
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateTableColumns("+columns.length+"):");
            for (int i = 0; i < columns.length; i++) {
                logger.fine("Column["+i+"] ("+columns[i].getDisplayName()+") = "+((tableColumns[i] != null) ? tableColumns[i].getHeaderValue() : "null")+"\t"+(columns[i].isHidden() ? "hidden" : ""));
            }
        }
        setColumnsOrder();
        this.tableColumns = tableColumns;
    }
    
    private List<TableColumn> getAllColumns(ETableColumnModel etcm) {
        try {
            Method getAllColumnsMethod = ETableColumnModel.class.getDeclaredMethod("getAllColumns");
            getAllColumnsMethod.setAccessible(true);
            Object allColumns = getAllColumnsMethod.invoke(etcm);
            return (List<TableColumn>) allColumns;
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptyList();
    }

    // Re-order the UI columns according to the defined order
    private void setColumnsOrder() {
        logger.fine("setColumnsOrder()");
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        //int[] shift = new int[columns.length];
        int defaultColumnVisibleIndex = 0;
        for (int i = 0; i < defaultColumnIndex; i++) {
            if (!columns[i].isHidden()) {
                defaultColumnVisibleIndex++;
            }
        }
        if (defaultColumnVisibleIndex != 0 && defaultColumnVisibleIndex < tcm.getColumnCount()) {
            logger.log(Level.FINE, " move default column({0}, {1})", new Object[]{0, defaultColumnVisibleIndex});
            tcm.moveColumn(0, defaultColumnVisibleIndex);
        }

        int n = tcm.getColumnCount();
        int[] order = new int[n];
        int ci = 0;
        for (int i = 0; i < n; i++, ci++) {
            while (ci < columns.length && columns[ci].isHidden()) {
                ci++;
            }
            if (ci >= columns.length) {
                break;
            }
            order[i] = columnVisibleMap[ci];
            logger.log(Level.FINE, "    order[{0}] = {1}", new Object[]{i, order[i]});
        }
        for (int i = 0; i < n; i++) {
            int j = 0;
            for (; j < n; j++) {
                if (order[j] == i) {
                    break;
                }
            }
            if (j == n) {
                // No "j" for order[j] == i.
                continue;
            }
            logger.log(Level.FINE, "  order[{0}] = {1}", new Object[]{j, i});
            if (j != i) {
                for (int k = j; k > i; k--) {
                    order[k] = order[k-1];
                }
                order[i] = i;
                logger.log(Level.FINE, " move column({0}, {1})", new Object[]{j, i});
                tcm.moveColumn(j, i);
            }
        }
    }

    private boolean isHiddenColumn(int index) {
        if (tableColumns == null) {
            return false;
        }
        if (tableColumns[index] == null) {
            return true;
        }
        ETableColumnModel ecm = (ETableColumnModel) treeTable.getTable().getColumnModel();
        return ecm.isColumnHidden(tableColumns[index]);
    }

    void updateColumnWidthsAndSorting() {
        logger.fine("\nupdateColumnWidthsAndSorting():");
        int i, k = columns.length;
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        ecm.clearSortedColumns();
        for (i = 0; i < k; i++) {
            if (isHiddenColumn(i)) {
                continue;
            }
            int visibleOrder = columnVisibleMap[i];
            logger.log(Level.FINE, "  visibleOrder[{0}] = {1}, ", new Object[]{i, visibleOrder});
            ETableColumn tc;
            try {
                tc = (ETableColumn) tcm.getColumn (visibleOrder);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
                logger.log(Level.SEVERE,
                        "Column("+i+") "+columns[i].getName()+" visible index = "+visibleOrder+
                        ", columnVisibleMap = "+java.util.Arrays.toString(columnVisibleMap)+
                        ", num of columns = "+tcm.getColumnCount(),
                        aioobex);
                continue ;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("  GUI column = "+tc.getHeaderValue());
            }
            if (columns[i] instanceof Column) {
                Column c = (Column) columns[i];
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("    Retrieved width "+c.getColumnWidth()+" from "+columns[i].getDisplayName()+"["+i+"] for "+tc.getHeaderValue());
                }
                tc.setPreferredWidth(c.getColumnWidth());
                if (c.isSorted()) {
                    ecm.setColumnSorted(tc, !c.isSortedDescending(), 1);
                }
            }
        }
    }

    private void saveWidths () {
        if (columns == null) {
            return;
        }
        int i, k = columns.length;
        if (k == 0) {
            return ;
        }
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        Enumeration<TableColumn> etc = tcm.getColumns();
        boolean defaultState = true;
        while(etc.hasMoreElements()) {
            if (etc.nextElement().getWidth() != 75) {
                defaultState = false;
                break;
            }
        }
        if (defaultState) {
            // All columns have the default width 75.
            // It's very likely that the table was not fully initialized => do not save anything.
            return ;
        }
        logger.fine("\nsaveWidths():");
        for (i = 0; i < k; i++) {
            if (isHiddenColumn(i)) {
                continue;
            }
            int visibleOrder = columnVisibleMap[i];
            logger.log(Level.FINE, "  visibleOrder[{0}] = {1}, ", new Object[]{i, visibleOrder});
            if (visibleOrder >= tcm.getColumnCount()) {
                continue;
            }
            TableColumn tc;
            try {
                tc = tcm.getColumn (visibleOrder);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
                logger.log(Level.SEVERE,
                        "Column("+i+") "+columns[i].getName()+" visible index = "+visibleOrder+
                        ", columnVisibleMap = "+java.util.Arrays.toString(columnVisibleMap)+
                        ", num of columns = "+tcm.getColumnCount(),
                        aioobex);
                continue ;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "  GUI column = {0}", tc.getHeaderValue());
            }
            if (columns[i] instanceof Column) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("    Setting width "+tc.getWidth()+" from "+tc.getHeaderValue()+" to "+columns[i].getDisplayName()+"["+i+"]");
                }
                ((Column) columns[i]).setColumnWidth(tc.getWidth());
            }
        }
    }
    
    private void saveSortedState () {
        if (columns == null) {
            return;
        }
        int i, k = columns.length;
        if (k == 0) {
            return ;
        }
        TableColumnModel tcm = treeTable.getTable().getColumnModel();
        ETableColumnModel ecm = (ETableColumnModel) tcm;
        Enumeration<TableColumn> etc = tcm.getColumns();
        logger.fine("\nsaveSortedState():");
        for (i = 0; i < k; i++) {
            if (isHiddenColumn(i)) {
                continue;
            }
            int visibleOrder = columnVisibleMap[i];
            logger.log(Level.FINE, "  visibleOrder[{0}] = {1}, ", new Object[]{i, visibleOrder});
            if (visibleOrder >= tcm.getColumnCount()) {
                continue;
            }
            ETableColumn tc;
            try {
                tc = (ETableColumn) tcm.getColumn (visibleOrder);
            } catch (ArrayIndexOutOfBoundsException aioobex) {
                logger.log(Level.SEVERE,
                        "Column("+i+") "+columns[i].getName()+" visible index = "+visibleOrder+
                        ", columnVisibleMap = "+java.util.Arrays.toString(columnVisibleMap)+
                        ", num of columns = "+tcm.getColumnCount(),
                        aioobex);
                continue ;
            }
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("  GUI column = "+tc.getHeaderValue());
            }
            if (columns[i] instanceof Column) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("    Setting sorted "+tc.isSorted()+" descending "+(!tc.isAscending())+" to "+columns[i].getDisplayName()+"["+i+"]");
                }
                ((Column) columns[i]).setSorted(tc.isSorted());
                ((Column) columns[i]).setSortedDescending(!tc.isAscending());
            }
        }
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow();
        return treeTable.requestFocusInWindow ();
    }
    
    @Override
    public void addNotify () {
        TopComponent.getRegistry ().addPropertyChangeListener (this);
        ExplorerUtils.activateActions(getExplorerManager (), true);
        getExplorerManager ().addPropertyChangeListener (this);
        super.addNotify ();
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        TopComponent.getRegistry ().removePropertyChangeListener (this);
        ExplorerUtils.activateActions(getExplorerManager (), false);
        getExplorerManager ().removePropertyChangeListener (this);
        setModel(null);
    }
    
    public boolean isExpanded (Object node) {
        Node[] ns = currentTreeModelRoot.findNode (node);
        if (ns.length == 0) {
            return false; // Something what does not exist is not expanded ;-)
        }
        return treeTable.isExpanded (ns[0]);
    }

    public void expandNode (Object node) {
        Node[] ns = currentTreeModelRoot.findNode (node);
        for (Node n : ns) {
            treeTable.expandNode (n);
        }
    }

    public void collapseNode (Object node) {
        Node[] ns = currentTreeModelRoot.findNode (node);
        for (Node n : ns) {
            treeTable.collapseNode (n);
        }
    }
    
    private class CreatedDefaultColumnsFromModel implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            TableColumn[] columns = (TableColumn[]) evt.getNewValue();
            if (columns == null) {
                if (currentTreeModelRoot != null && !isSettingModelUp) {
                    // Refreshing a set up table, need to save the column widths
                    saveWidths();
                }
                tableColumns = null;
            } else if (!ignoreCreateDefaultColumnsFromModel) {
                // Update the columns after they are reset:
                Property[] properties = treeTable.getProperties();
                if (properties != null) {
                    updateTableColumns(properties, columns);
                }
            }
        }
        
    }
    
    static class MyTreeTable extends OutlineView {  // Accessed from tests

        private Reference dndModelRef = new WeakReference(null);
        private Property[] properties;

        MyTreeTable () {
            super ();
            Outline outline = getOutline();
            outline.setShowHorizontalLines (true);
            outline.setShowVerticalLines (false);
            filterInputMap(outline, JComponent.WHEN_FOCUSED);
            filterInputMap(outline, JComponent.WHEN_IN_FOCUSED_WINDOW);
            filterInputMap(outline, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            outline.putClientProperty("PropertyToolTipShortDescription", Boolean.TRUE);
        }
        
        private void filterInputMap(JComponent component, int condition) {
            InputMap imap = component.getInputMap(condition);
            if (imap instanceof ComponentInputMap) {
                imap = new F8FilterComponentInputMap(component, imap);
            } else {
                imap = new F8FilterInputMap(imap);
            }
            component.setInputMap(condition, imap);
        }
        
        JTable getTable () {
            return getOutline();
        }

        @Override
        public void setProperties(Property[] newProperties) {
            this.properties = newProperties;
            super.setProperties(newProperties);
        }
        
        Property[] getProperties() {
            return properties;
        }
        
        void setNodesColumnName(String name, String description) {
            OutlineModel m = getOutline().getOutlineModel();
            if (m instanceof DefaultOutlineModel) {
                ((DefaultOutlineModel) m).setNodesColumnLabel(name);
            }
            setPropertyColumnDescription(name, description);
        }

        /*
        public List getExpandedPaths () {
            List result = new ArrayList ();
            ExplorerManager em = ExplorerManager.find (this);
            TreeNode rtn = Visualizer.findVisualizer (
                em.getRootContext ()
            );
            TreePath tp = new TreePath (rtn); // Get the root
            
            Enumeration exPaths = tree.getExpandedDescendants (tp); 
            if (exPaths == null) return result;
            for (;exPaths.hasMoreElements ();) {
                TreePath ep = (TreePath) exPaths.nextElement ();
                Node en = Visualizer.findNode (ep.getLastPathComponent ());
                String[] path = NodeOp.createPath (en, em.getRootContext ());
                result.add (path);
            }
            return result;
        }
         */
        
        /** Expands all the paths, when exists
         */
        public void expandNodes (List exPaths) {
            for (Iterator it = exPaths.iterator (); it.hasNext ();) {
                String[] sp = (String[]) it.next ();
                TreePath tp = stringPath2TreePath (sp);
                if (tp != null) {
                    getOutline().expandPath(tp);
                    Rectangle rect = getOutline().getPathBounds(tp);
                    if (rect != null) {
                        getOutline().scrollRectToVisible(rect);
                    }
                }
            }
        }

        /** Converts path of strings to TreePath if exists null otherwise
         */
        private TreePath stringPath2TreePath (String[] sp) {
            ExplorerManager em = ExplorerManager.find (this);
            try {
                Node n = NodeOp.findPath (em.getRootContext (), sp); 
                
                // Create the tree path
                TreeNode tns[] = new TreeNode [sp.length + 1];
                
                for (int i = sp.length; i >= 0; i--) {
                    tns[i] = Visualizer.findVisualizer (n);
                    n = n.getParentNode ();
                }                
                return new TreePath (tns);
            } catch (NodeNotFoundException e) {
                return null;
            }
        }

        void setDynamicDropActions(DnDNodeModel model) {
            dndModelRef = new WeakReference(model);
        }

        void setDynamicDropActions(HyperCompoundModel model) {
            dndModelRef = new WeakReference(model);
        }

        @Override
        protected int getAllowedDropActions(Transferable t) {
            Object model = dndModelRef.get();
            if (model instanceof DnDNodeModel) {
                return ((DnDNodeModel) model).getAllowedDropActions(t);
            } else if (model instanceof HyperCompoundModel) {
                return ((HyperCompoundModel) model).getAllowedDropActions(t);
            } else {
                return super.getAllowedDropActions();
            }
        }

    }
    
    private static final class F8FilterComponentInputMap extends ComponentInputMap {
        
        private KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        
        public F8FilterComponentInputMap(JComponent component, InputMap imap) {
            super(component);
            setParent(imap);
        }

        @Override
        public Object get(KeyStroke keyStroke) {
            if (f8.equals(keyStroke)) {
                return null;
            } else {
                return super.get(keyStroke);
            }
        }
    }
    
    private static final class F8FilterInputMap extends InputMap {
        
        private KeyStroke f8 = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        
        public F8FilterInputMap(InputMap imap) {
            setParent(imap);
        }

        @Override
        public Object get(KeyStroke keyStroke) {
            if (f8.equals(keyStroke)) {
                return null;
            } else {
                return super.get(keyStroke);
            }
        }
    }
    
}

