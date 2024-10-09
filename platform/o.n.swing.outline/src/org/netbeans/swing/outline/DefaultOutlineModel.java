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
package org.netbeans.swing.outline;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.AbstractLayoutCache;
import javax.swing.tree.FixedHeightLayoutCache;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.VariableHeightLayoutCache;

/** Proxies a standard TreeModel and TableModel, translating events between
 * the two.  Note that the constructor is not public;  the TableModel that is
 * proxied is the OutlineModel's own.  To make use of this class, implement
 * RowModel - that is a mini-table model in which the TreeModel is responsible
 * for defining the set of rows; it is passed an object from the tree, which
 * it may use to generate values for the other columns.  Pass that and the
 * TreeModel you want to use to <code>createOutlineModel</code>.
 * <p>
 * A note on TableModelEvents produced by this model:  There is a slight 
 * impedance mismatch between TableModelEvent and TreeModelEvent.  When the
 * tree changes, it is necessary to fire TableModelEvents to update the display.
 * However, TreeModelEvents support changes to discontiguous segments of the
 * model (i.e. &quot;child nodes 3, 4 and 9 were deleted&quot;).  TableModelEvents
 * have no such concept - they operate on contiguous ranges of rows.  Therefore,
 * one incoming TreeModelEvent may result in more than one TableModelEvent being
 * fired.  Discontiguous TreeModelEvents will be broken into their contiguous
 * segments, which will be fired sequentially (in the case of removals, in
 * reverse order).  So, the example above would generate two TableModelEvents,
 * the first indicating that row 9 was removed, and the second indicating that
 * rows 3 and 4 were removed.
 * <p>
 * Clients which need to know whether the TableModelEvent they have just 
 * received is one of a group (perhaps they update some data structure, and
 * should not do so until the table's state is fully synchronized with that
 * of the tree model) may call <code>areMoreEventsPending()</code>.
 * <p>
 * In the case of TreeModelEvents which add items to an unexpanded tree node,
 * a simple value change TableModelEvent will be fired for the row in question
 * on the tree column index.
 * <p>
 * Note also that if the model is large-model, removal events may only indicate
 * those indices which were visible at the time of removal, because less data
 * is retained about the position of nodes which are not displayed.  In this
 * case, the only issue is the accuracy of the scrollbar in the model; in
 * practice this is a non-issue, since it is based on the Outline's row count,
 * which will be accurate.
 * <p>
 * A note to subclassers, if we even leave this class non-final:  If you do
 * not use ProxyTableModel and RowMapper (which probably means you are doing
 * something wrong), <strong>do not fire structural changes from the TableModel</strong>.
 * This class is designed such that the TreeModel is entirely in control of the
 * count and contents of the rows of the table.  It and only it may fire 
 * structural changes.
 * <p>
 * Note that this class enforces access only on the event dispatch thread
 * with assertions.  All events fired by the underlying table and tree model
 * must be fired on the event dispatch thread.
 *
 * @author  Tim Boudreau
 */
public class DefaultOutlineModel implements OutlineModel {
    private TreeModel treeModel;
    private TableModel tableModel;
    private AbstractLayoutCache layout;
    private TreePathSupport treePathSupport;
    private EventBroadcaster broadcaster;
    private String nodesColumnLabel = "Nodes";
    //Some constants we use to have a single method handle all translated
    //event firing
    private static final int NODES_CHANGED = 0;
    private static final int NODES_INSERTED = 1;
    private static final int NODES_REMOVED = 2;
    private static final int STRUCTURE_CHANGED = 3;
    
    /** Create a small model OutlineModel using the supplied tree model and row model 
     * @param treeModel The tree model that is the data model for the expandable
     *  tree column of an Outline
     * @param rowModel The row model which will supply values for each row based
     *  on the tree node in that row in the tree model
     */
    public static OutlineModel createOutlineModel(TreeModel treeModel, RowModel rowModel) {
        return createOutlineModel (treeModel, rowModel, false, null);
    }

    /** Create an OutlineModel using the supplied tree model and row model,
     * specifying if it is a large-model tree
     * @param treeModel The tree model
     * @param rowModel The row model
     * @param isLargeModel <code>true</code> if it's a large model tree, <code>false</code> otherwise.
     */
    public static OutlineModel createOutlineModel(TreeModel treeModel, RowModel rowModel, boolean isLargeModel) {
        return createOutlineModel (treeModel, rowModel, isLargeModel, null);
    }
    /** Create an OutlineModel using the supplied tree model and row model,
     * specifying if it is a large-model tree
     * @param treeModel The tree model
     * @param rowModel The row model
     * @param isLargeModel <code>true</code> if it's a large model tree, <code>false</code> otherwise.
     * @param nodesColumnLabel Label of the node's column
     */
    public static OutlineModel createOutlineModel(TreeModel treeModel, RowModel rowModel, boolean isLargeModel, String nodesColumnLabel) {
        return new DefaultOutlineModel (treeModel, rowModel, isLargeModel, nodesColumnLabel);
    }
    
    /** Create a new instance of DefaultOutlineModel using the supplied tree model and row model,
     * specifying if it is a large-model tree
     * @param treeModel The tree model
     * @param rowModel The row model
     * @param largeModel <code>true</code> if it's a large model tree, <code>false</code> otherwise.
     * @param nodesColumnLabel Label of the node's column
     */
    protected DefaultOutlineModel(TreeModel treeModel, RowModel rowModel, boolean largeModel, String nodesColumnLabel) {
        this( treeModel, new ProxyTableModel(rowModel), largeModel, nodesColumnLabel );
    }
    
    /** Creates a new instance of DefaultOutlineModel.  <strong><b>Note</b></strong> 
     * Do not fire table structure changes from the wrapped TableModel (value
     * changes are okay).  Changes that affect the number of rows must come
     * from the TreeModel.
     * @param treeModel The tree model
     * @param tableModel The table model
     * @param largeModel <code>true</code> if it's a large model tree, <code>false</code> otherwise.
     * @param nodesColumnLabel Label of the node's column
     */
    protected DefaultOutlineModel(TreeModel treeModel, TableModel tableModel, boolean largeModel, String nodesColumnLabel) {
        this.treeModel = treeModel;
        this.tableModel = tableModel;
        if (nodesColumnLabel != null) {
            this.nodesColumnLabel = nodesColumnLabel;
        }
        
        layout = largeModel ? (AbstractLayoutCache) new FixedHeightLayoutCache() 
            : (AbstractLayoutCache) new VariableHeightLayoutCache();
            
        broadcaster = new EventBroadcaster (this);
        
        layout.setRootVisible(true);
        layout.setModel(this);
        treePathSupport = new TreePathSupport(this, layout, broadcaster);
        treeModel.addTreeModelListener(broadcaster);
        tableModel.addTableModelListener(broadcaster);
        if (tableModel instanceof ProxyTableModel) {
            ((ProxyTableModel) tableModel).setOutlineModel(this);
        }
    }
    
    @Override
    public final TreePathSupport getTreePathSupport() {
        return treePathSupport;
    }    
    
    @Override
    public final AbstractLayoutCache getLayout() {
        return layout;
    }

    /** Flag which is set to true while multiple TableModelEvents generated
     * from a single TreeModelEvent are being fired, so clients can avoid
     * any model queries until all pending changes have been fired.  The
     * main thing to avoid is any mid-process repaints, which can only happen
     * if the response to an event will be to call paintImmediately().
     * <p>
     * This value is guaranteed to be true for the first group of
     * related events, and false if tested in response to the final event.
     * 
     * @return <code>true</code> if more events are pending, <code>false</code> otherwise.
     */
    public boolean areMoreEventsPending() {
        return broadcaster.areMoreEventsPending();
    }
    
    /** Accessor for EventBroadcaster */
    TreeModel getTreeModel() {
        return treeModel;
    }
    
    /** Accessor for EventBroadcaster */
    TableModel getTableModel() {
        return tableModel;
    }
    
    @Override
    public final Object getChild(Object parent, int index) {
        return treeModel.getChild (parent, index);
    }
    
    @Override
    public final int getChildCount(Object parent) {
        return treeModel.getChildCount (parent);
    }
    
    /** Delegates to the RowMapper for > 0 columns; column 0 always
     * returns Object.class */
    @Override
    public final Class getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Object.class;
        } else {
            return tableModel.getColumnClass(columnIndex-1);
        }
    }
    
    @Override
    public final int getColumnCount() {
        return tableModel.getColumnCount()+1;
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return nodesColumnLabel;
        } else {
            return tableModel.getColumnName(columnIndex-1);
        }
    }
    
    /**
     * Change the label of the 'tree' column.
     * @param label New label for tree column.
     */
    public void setNodesColumnLabel( String label ) {
        this.nodesColumnLabel = label;
        broadcaster.fireTableChange( new TableModelEvent( this, -1, -1, 0, TableModelEvent.HEADER_ROW ) );
    }
    
    @Override
    public final int getIndexOfChild(Object parent, Object child) {
        return treeModel.getIndexOfChild(parent, child);
    }
    
    @Override
    public final Object getRoot() {
        return treeModel.getRoot();
    }
    
    @Override
    public final int getRowCount() {
        return layout.getRowCount();
    }
    
    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object result;
        if (columnIndex == 0) { //XXX need a column ID - columnIndex = 0 depends on the column model
            TreePath path = getLayout().getPathForRow(rowIndex);
            if (path != null) {
                result = path.getLastPathComponent();
            } else {
                result = null;
            }
        } else {
            result = (tableModel.getValueAt(rowIndex, columnIndex -1));
        }
        return result;
    }
     
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return false; //XXX support editing of node names
        } else {
            return tableModel.isCellEditable(rowIndex, columnIndex-1);
        }
    }
    
    @Override
    public final boolean isLeaf(Object node) {
        return null != node && treeModel.isLeaf(node);
    }

    // Delegates to the EventBroadcaster for this model
    @Override
    public final synchronized void addTableModelListener(TableModelListener l) {
        broadcaster.addTableModelListener (l);
    }
    
    // Delegates to the EventBroadcaster for this model
    @Override
    public final synchronized void addTreeModelListener(TreeModelListener l) {
        broadcaster.addTreeModelListener (l);
    }    
    
    // Delegates to the EventBroadcaster for this model
    @Override
    public final synchronized void removeTableModelListener(TableModelListener l) {
        broadcaster.removeTableModelListener(l);
    }
    
    // Delegates to the EventBroadcaster for this model
    @Override
    public final synchronized void removeTreeModelListener(TreeModelListener l) {
        broadcaster.removeTreeModelListener(l);
    }
    
    /** Delegates to the RowModel (or TableModel) for non-0 columns */
    @Override
    public final void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex != 0) {
            tableModel.setValueAt (aValue, rowIndex, columnIndex-1);
        } else {
            setTreeValueAt(aValue, rowIndex);
        }
    }
    
    /**
     * Sets the value of a 'tree' cell at given row number. 
     * The default implementation does nothing.
     * 
     * @param aValue
     * @param rowIndex
     */
    protected void setTreeValueAt(Object aValue, int rowIndex) {
        //do nothing
    }
    
    @Override
    public final void valueForPathChanged(javax.swing.tree.TreePath path, Object newValue) {
        //if the model is correctly implemented, this will trigger a change
        //event
        treeModel.valueForPathChanged(path, newValue);
    }

    @Override
    public boolean isLargeModel() {
        return layout instanceof FixedHeightLayoutCache;
    }
    

    
}
