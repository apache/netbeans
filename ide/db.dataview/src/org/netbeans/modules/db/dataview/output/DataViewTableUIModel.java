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
package org.netbeans.modules.db.dataview.output;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.table.ResultSetTableModel;

/**
 * DataViewTableUIModel
 *
 * Extends ResultSetTableModel with update tracking
 *
 * @author matthias
 */
public class DataViewTableUIModel extends ResultSetTableModel {

    private final Map<Integer, Map<Integer, Object>> oldData = new LinkedHashMap<>();
    private int rowOffset = 0;

    protected DataViewTableUIModel(DBColumn[] columns) {
        super(columns);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        Object oldval = getValueAt(row, col);
        if (noUpdateRequired(oldval, value)) {
            return;
        }
        addUpdates(row, col, oldval);
        super.setValueAt(value, row, col);
    }

    public Object getOriginalValueAt(int row, int col) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        if(hasUpdates(row, col)) {
            return oldData.get(row).get(col);
        } else {
            return getValueAt(row, col);
        }
    }

    public int getRowOffset() {
        return rowOffset;
    }

    public void setRowOffset(int rowOffset) {
        this.rowOffset = rowOffset;
        fireTableDataChanged();
    }

    public int getTotalRowOffset(int row) {
        return rowOffset + row;
    }
    
    @Override
    public void setData(List<Object[]> data) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        super.setData(data);
        oldData.clear();
    }

    @Override
    public void removeRow(int row) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        super.removeRow(row);
        oldData.remove(row);
    }

    @Override
    public void clear() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        super.clear();
        oldData.clear();
    }

    private void addUpdates(int row, int col, Object value) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        Map<Integer, Object> rowMap = oldData.get(row);
        if (rowMap == null) {
            rowMap = new LinkedHashMap<>();
            oldData.put(row, rowMap);
        }

        if (!rowMap.containsKey(col)) {
            rowMap.put(col, value);
        }
    }

    public void removeAllUpdates(boolean discardNewValue) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        for(Integer rowIndex: new HashSet<>(oldData.keySet())) {
            Map<Integer,Object> oldRow = oldData.remove(rowIndex);
            if (discardNewValue) {
                for (Integer columnIndex : new HashSet<>(oldRow.keySet())) {
                    super.setValueAt(oldRow.remove(columnIndex), rowIndex,
                            columnIndex);
                }
            }
        }
    }

    public void removeUpdateForSelectedRow(int row, boolean discardNewValue) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        if (oldData.containsKey(row)) {
            Map<Integer, Object> oldRow = oldData.remove(row);
            if (discardNewValue) {
                for (Integer columnIndex : new HashSet<>(oldRow.keySet())) {
                    super.setValueAt(oldRow.remove(columnIndex), row,
                            columnIndex);
                }
            }
        }
    }

    public void removeUpdate(int row, int col, boolean discardNewValue) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        if (oldData.containsKey(row)) {
            Map<Integer, Object> oldRow = oldData.get(row);
            if (oldRow.containsKey(col)) {
                Object value = oldRow.get(col);
                if (oldRow.isEmpty()) {
                    oldData.remove(row);
                }
                if (discardNewValue) {
                    super.setValueAt(value, row, col);
                }
            }
        }
        fireTableCellUpdated(row, col);
    }

    public Set<Integer> getUpdateKeys() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return oldData.keySet();
    }

    public Map<Integer, Object> getChangedData(int row) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        Set<Integer> changedColumns = oldData.get(row).keySet();
        Map<Integer,Object> result = new HashMap<>();
        for(Integer column: changedColumns) {
            result.put(column, getValueAt(row, column));
        }
        return result;
    }

    boolean hasUpdates() {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        return oldData.size() > 0;
    }

    boolean hasUpdates(int row, int col) {
        assert SwingUtilities.isEventDispatchThread() : "Not on EDT";
        Map<Integer, Object> rowMap = oldData.get(Integer.valueOf(row));
        return rowMap != null && rowMap.containsKey(col);
    }
}
