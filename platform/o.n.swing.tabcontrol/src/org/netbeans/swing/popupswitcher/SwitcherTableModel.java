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

package org.netbeans.swing.popupswitcher;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import org.openide.util.Utilities;

/**
 * <code>TableModel</code> for <code>SwitcherTable</code>.
 *
 * @see SwitcherTable
 *
 * @author mkrauskopf
 */
class SwitcherTableModel extends AbstractTableModel {

    /**
     * Used to estimate number of cells fitting to given space Event object for
     * this TableModel.
     */
    private TableModelEvent event;

    /** Number of rows */
    private int rows;
    
    /** Number of columns */
    private int cols;
    
    /** Items */
    private SwitcherTableItem[] items;
    
    /**
     * Use whole screen for table height during number of columns/row
     * computing.
     */
    SwitcherTableModel(SwitcherTableItem[] items, int rowHeight) {
        this(items, rowHeight, Utilities.getUsableScreenBounds().height);
    }
    
    /** Use specified table height during number of columns/row computing. */
    SwitcherTableModel(SwitcherTableItem[] items, int rowHeight, int tableHeight) {
        super();
        this.items = items;
        computeRowsAndCols(rowHeight, tableHeight);
    }
    
    private void computeRowsAndCols(int rowHeight, int tableHeight) {
        // Default algorithm - use whole screen for SwitcherTable
        int nOfItems = items.length;
        int maxRowsPerCol = 0;
        if( rowHeight > 0 )
            maxRowsPerCol = tableHeight / rowHeight;
        if (nOfItems > 0 && maxRowsPerCol > 0) { // avoid div by 0
            // Compute number of rows in one column
            int nOfColumns = (nOfItems / maxRowsPerCol);
            if (nOfItems % maxRowsPerCol > 0) {
                nOfColumns++;
            }
            nOfColumns = Math.max( nOfColumns, 1 );
            int nOfRows = nOfItems / nOfColumns;
            if (nOfItems % nOfColumns > 0) {
                nOfRows++;
            }
            setRowsAndColumns(nOfRows, nOfColumns);
        } else {
            setRowsAndColumns(0, 0);
        }
    }
    
    private void setRowsAndColumns(int rows, int cols) {
        if ((this.rows != rows) || (this.cols != cols)) {
            this.rows = rows;
            this.cols = cols;
            if (event == null) {
                event = new TableModelEvent(this);
            }
            fireTableChanged(event);
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return SwitcherTableItem.class;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return "";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        if ((rowIndex == -1) || (columnIndex == -1)) {
            return null;
        }
        int docIdx = (columnIndex * getRowCount()) + rowIndex;
        return (docIdx < items.length ? items[docIdx] : null);
    }
    
    public int getRowCount() {
        return rows;
    }
    
    public int getColumnCount() {
        return cols;
    }
}
