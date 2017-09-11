/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
    
    final public TreeModel getTreeModel() {
        return treeModel;
    }
    
    final void setTreeModel(final TreeModel treeModel) {
        removeTreeModelListener();
        this.treeModel = treeModel;
        addTreeModelListener();
    }
    
    final public JTree getTree() {
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
    final public int getRowCount() {
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
