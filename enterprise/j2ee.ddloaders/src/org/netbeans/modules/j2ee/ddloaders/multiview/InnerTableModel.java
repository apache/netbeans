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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
public abstract class InnerTableModel extends AbstractTableModel {

    private XmlMultiViewDataSynchronizer synchronizer;
    protected final String[] columnNames;
    private int[] columnWidths;
    private int rowCount = -1;

    public InnerTableModel(XmlMultiViewDataSynchronizer synchronizer, String[] columnNames, int[] columnWidths) {
        this.synchronizer = synchronizer;
        this.columnNames = columnNames;
        this.columnWidths = columnWidths;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public TableCellEditor getCellEditor(int columnIndex) {
        return null;
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int[] getColumnWidths() {
        return columnWidths;
    }

    public abstract int addRow();

    public abstract void removeRow(int selectedRow);

    public int getDefaultColumnWidth(int i) {
        return columnWidths[i];
    }

    public void refreshView() {
        fireTableDataChanged();
    }

    protected void tableChanged() {
        if (!checkRowCount()) {
            fireTableDataChanged();
        }
    }

    private boolean checkRowCount() {
        int n = getRowCount();
        if (rowCount == -1) {
            rowCount = n;
        }
        if (n != rowCount) {
            while (rowCount < n) {
                rowCount++;
                fireTableRowsInserted(0, 0);
            }
            while (rowCount > n) {
                rowCount--;
                fireTableRowsDeleted(0, 0);
            }
            return true;
        } else {
            return false;
        }
    }

    protected void modelUpdatedFromUI() {
        if (synchronizer != null) {
            synchronizer.requestUpdateData();
        }
    }

    public TableCellEditor getTableCellEditor(int column) {
        return null;
    }

    protected TableCellEditor createComboBoxCellEditor(Object[] items) {
        return createComboBoxCellEditor(items, false);
    }

    private static TableCellEditor createComboBoxCellEditor(Object[] items, final boolean editable) {
        final JComboBox comboBox = new JComboBox(items);
        comboBox.setEditable(editable);
        return new DefaultCellEditor(comboBox);
    }
}
