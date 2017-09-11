/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        Map<Integer, Object> rowMap = oldData.get(new Integer(row));
        return rowMap != null && rowMap.containsKey(col);
    }
}
