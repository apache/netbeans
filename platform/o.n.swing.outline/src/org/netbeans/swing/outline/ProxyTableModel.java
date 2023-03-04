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
package org.netbeans.swing.outline;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/** A TableModel which is driven by a RowModel - the RowModel
 * supplies row contents, based on nodes suppled by the tree
 * column of an OutlineModel.  This model supplies the additional
 * rows of the TableModel to the OutlineModel.
 *
 * @author  Tim Boudreau
 */
final class ProxyTableModel implements TableModel {
    private List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    private RowModel rowmodel;
    private OutlineModel outlineModel;
    /** Creates a new instance of ProxyTableModel that will use the supplied
     * RowModel to produce its values.  */
    public ProxyTableModel(RowModel rowmodel) {
        this.rowmodel = rowmodel;
    }
    
    /** Set the OutlineModel that will be used to find nodes for
     * rows.  DefaultOutlineModel will do this in its constructor. */
    void setOutlineModel (OutlineModel mdl) {
        this.outlineModel = mdl;
    }
    
    /** Get the outline model used to provide column 0 nodes to the
     * RowModel for setting the values.  */
    OutlineModel getOutlineModel () {
        return outlineModel;
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return rowmodel.getColumnClass(columnIndex);
    }
    
    @Override
    public int getColumnCount() {
        return rowmodel.getColumnCount();
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return rowmodel.getColumnName(columnIndex);
    }
    
    @Override
    public int getRowCount() {
        //not interesting, will never be called - the outline model
        //handles this
        return -1;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        if (node == null) {
            assert false : "Some node should exist on row " + rowIndex + " and on column " + columnIndex + ", but was null.";
            return null;
        }
        return rowmodel.getValueFor(node, columnIndex);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        return rowmodel.isCellEditable (node, columnIndex);
    }
    
    @Override
    public synchronized void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
    
    @Override
    public synchronized void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }
    
    private void fire (TableModelEvent e) {
        TableModelListener[] l;
        synchronized (this) {
            l = new TableModelListener[listeners.size()];
            l = listeners.toArray (l);
        }
        for (int i=0; i < l.length; i++) {
            l[i].tableChanged(e);
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Object node = getNodeForRow(rowIndex);
        rowmodel.setValueFor (node, columnIndex, aValue);
        TableModelEvent e = new TableModelEvent (this, rowIndex, rowIndex, 
            columnIndex);
        fire(e);
    }
    
    /** Get the object that will be passed to the RowModel to fetch values
     * for the given row. 
     * @param row The row we need the tree node for */
    private Object getNodeForRow(int row) {
        return getOutlineModel().getValueAt(row, 0);
    }    

    
}
