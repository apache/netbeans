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

package org.netbeans.modules.db.dataview.table;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DataViewUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * @author Ahimanikya Satapathy
 */
public class ResultSetTableModel extends AbstractTableModel {

    private boolean editable = false;
    private DBColumn[] columns;
    private final List<Object[]> data = new ArrayList<Object[]>();
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected static Class<? extends Object> getTypeClass(DBColumn col) {
        int colType = col.getJdbcType();

        if (colType == Types.BIT && col.getPrecision() <= 1) {
            colType = Types.BOOLEAN;
        }

        switch (colType) {
            case Types.BOOLEAN:
                return Boolean.class;
            case Types.TIME:
                return Time.class;
            case Types.DATE:
                return Date.class;
            case Types.TIMESTAMP:
            case DBReadWriteHelper.SQL_TYPE_ORACLE_TIMESTAMP:
            case DBReadWriteHelper.SQL_TYPE_ORACLE_TIMESTAMP_WITH_TZ:
                return Timestamp.class;
            case Types.BIGINT:
                return BigInteger.class;
            case Types.DOUBLE:
                return Double.class;
            case Types.FLOAT:
            case Types.REAL:
                return Float.class;
            case Types.DECIMAL:
            case Types.NUMERIC:
                return BigDecimal.class;
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
                return Long.class;

            case Types.CHAR:
            case Types.VARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.ROWID:
                return String.class;

            case Types.BIT:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
            case Types.BLOB:
                return Blob.class;
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB: /*NCLOB */
                return Clob.class;
            case Types.OTHER:
            default:
                return Object.class;
        }
    }

    @SuppressWarnings("rawtypes")
    public ResultSetTableModel(DBColumn[] columns) {
        super();
        this.columns = columns;
    }

    public void setColumns(DBColumn[] columns) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        this.data.clear();
        this.columns = columns;
        fireTableStructureChanged();
    }

    public DBColumn[] getColumns() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return Arrays.copyOf(columns, columns.length);
    }

    public void setEditable(boolean editable) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        boolean old = this.editable;
        this.editable = editable;
        pcs.firePropertyChange("editable", old, editable);
    }

    public boolean isEditable() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return editable;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        if (!editable) {
            return false;
        }
        DBColumn col = this.columns[column];
        return (!col.isGenerated()) && col.isEditable();
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        Object oldVal = getValueAt(row, col);
        if (noUpdateRequired(oldVal, value)) {
            return;
        }
        try {
            if (!DataViewUtils.isSQLConstantString(value, columns[col])) {
                value = DBReadWriteHelper.validate(value, columns[col]);
            }
            data.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        } catch (Exception dbe) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(dbe.getMessage(),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Object> getColumnClass(int columnIndex) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        if (columns[columnIndex] == null) {
            return super.getColumnClass(columnIndex);
        } else {
            return getTypeClass(columns[columnIndex]);
        }
    }

    public DBColumn getColumn(int columnIndex) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return columns[columnIndex];
    }

    @Override
    public int getColumnCount() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return columns.length;
    }

    public String getColumnTooltip(int columnIndex) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return DataViewUtils.getColumnToolTip(columns[columnIndex]);
    }

    @Override
    public String getColumnName(int columnIndex) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        String displayName = columns[columnIndex].getDisplayName();
        return displayName != null ? displayName : "COL_" + columnIndex;
    }

    protected boolean noUpdateRequired(Object oldVal, Object value) {
        if (oldVal == null && value == null) {
            return true;
        } else if (oldVal != null) {
            return oldVal.equals(value);
        }
        return false;
    }

    @Override
    public int getRowCount() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return data.size();
    }
        
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        Object[] dataRow = data.get(rowIndex);
        return dataRow[columnIndex];
    }

    public Object[] getRowData(int rowIndex) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        Object[] dataRow = data.get(rowIndex);
        return Arrays.copyOf(dataRow, dataRow.length);
    }

    public void setData(List<Object[]> data) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        this.data.clear();
        for (Object[] dataRow : data) {
            this.data.add(Arrays.copyOf(dataRow, dataRow.length));
        }
        fireTableDataChanged();
    }

    public List<Object[]> getData() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        ArrayList<Object[]> result = new ArrayList<Object[]>();
        for (Object[] dataRow : this.data) {
            result.add(Arrays.copyOf(dataRow, dataRow.length));
        }
        return result;
    }

    public void addRow(Object[] dataRow) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        int addedRowIndex = this.data.size();
        this.data.add(Arrays.copyOf(dataRow, dataRow.length));
        fireTableRowsInserted(addedRowIndex, addedRowIndex);
    }

    public void removeRow(int row) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        this.data.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public void clear() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        this.data.clear();
        fireTableDataChanged();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
