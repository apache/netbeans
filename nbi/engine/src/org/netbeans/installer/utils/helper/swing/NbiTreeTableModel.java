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

package org.netbeans.installer.utils.helper.swing;

import java.util.Vector;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class NbiTreeTableModel implements TableModel {
    private Vector<TableModelListener> listeners = new Vector<TableModelListener>();
    
    private TreeModel treeModel;
    private JTree tree;
    
    private TreeExpansionListener treeExpansionListener;
    private TreeModelListener treeModelListener;
    
    private boolean consumeNextExpansionEvent = false;
    
    public NbiTreeTableModel(final TreeModel treeModel) {
        setTreeModel(treeModel);
    }
    
    public abstract int getTreeColumnIndex();
    
    public final TreeModel getTreeModel() {
        return treeModel;
    }
    
    final void setTreeModel(final TreeModel treeModel) {
        removeTreeModelListener();
        this.treeModel = treeModel;
        addTreeModelListener();
    }
    
    public final JTree getTree() {
        return tree;
    }
    
    final void setTree(final JTree tree) {
        removeTreeExpansionListener();
        this.tree = tree;
        addTreeExpansionListener();
    }
    
    final void consumeNextExpansionEvent() {
        consumeNextExpansionEvent = true;
    }
    
    final void cancelConsume() {
        consumeNextExpansionEvent = false;
    }
    
    // partial TableModel implementation ////////////////////////////////////////////
    public final int getRowCount() {
        return tree.getRowCount();
    }
    
    public abstract int getColumnCount();
    
    public abstract String getColumnName(int column);
    
    public abstract Class<?> getColumnClass(int column);
    
    public abstract boolean isCellEditable(int row, int column);
    
    public abstract Object getValueAt(int row, int column);
    
    public abstract void setValueAt(Object value, int row, int column);
    
    public void addTableModelListener(TableModelListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeTableModelListener(TableModelListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    // protected stuff - to be used by extending classes ////////////////////////////
    protected void fireTableRowsInserted(int firstRow, int lastRow) {
        fireTableDataChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }
    
    protected void fireTableRowsDeleted(int firstRow, int lastRow) {
        fireTableDataChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE));
    }
    
    protected void fireTableRowsUpdated(int firstRow, int lastRow) {
        fireTableDataChanged(new TableModelEvent(this, firstRow, lastRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
    }
    
    // private stuff ////////////////////////////////////////////////////////////////
    private void removeTreeModelListener() {
        if (treeModel != null) {
            treeModel.removeTreeModelListener(treeModelListener);
        }
    }
    
    private void addTreeModelListener() {
        if (treeModelListener == null) {
            treeModelListener = new TreeModelListener() {
                public void treeNodesChanged(TreeModelEvent event) {
                    delayedFireTableDataChanged();
                }
                public void treeNodesInserted(TreeModelEvent event) {
                    delayedFireTableDataChanged();
                }
                public void treeNodesRemoved(TreeModelEvent event) {
                    delayedFireTableDataChanged();
                }
                public void treeStructureChanged(TreeModelEvent event) {
                    delayedFireTableDataChanged();
                }
            };
        }
        
        treeModel.addTreeModelListener(treeModelListener);
    }
    
    private void removeTreeExpansionListener() {
        if (tree != null) {
            tree.removeTreeExpansionListener(treeExpansionListener);
        }
    }
    
    private void addTreeExpansionListener() {
        if (treeExpansionListener == null) {
            treeExpansionListener = new TreeExpansionListener() {
                public void treeCollapsed(TreeExpansionEvent event) {
                    if (consumeNextExpansionEvent) {
                        consumeNextExpansionEvent = false;
                        return;
                    }
                    
                    int row = tree.getRowForPath(event.getPath());
                    int childrenCount = treeModel.getChildCount(event.getPath().getLastPathComponent());
                    fireTableRowsDeleted(row + 1, row + childrenCount);
                    fireTableRowsUpdated(row, row);
                }
                
                public void treeExpanded(TreeExpansionEvent event) {
                    if (consumeNextExpansionEvent) {
                        consumeNextExpansionEvent = false;
                        return;
                    }
                    
                    int row = tree.getRowForPath(event.getPath());
                    int childrenCount = treeModel.getChildCount(event.getPath().getLastPathComponent());
                    fireTableRowsInserted(row + 1, row + childrenCount);
                    fireTableRowsUpdated(row, row);
                }
            };
        }
        
        tree.addTreeExpansionListener(treeExpansionListener);
    }
    
    private void fireTableDataChanged(TableModelEvent event) {
        for (TableModelListener listener: listeners.toArray(new TableModelListener[0])) {
            listener.tableChanged(event);
        }
    }
    
    private void fireTableDataChanged() {
        fireTableDataChanged(new TableModelEvent(this));
    }
    
    private void delayedFireTableDataChanged() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableDataChanged();
            }
        });
    }
}
