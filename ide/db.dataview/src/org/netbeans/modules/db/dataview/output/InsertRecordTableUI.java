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

import org.netbeans.modules.db.dataview.table.ResultSetJXTable;
import java.sql.Types;
import java.util.Arrays;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import org.netbeans.modules.db.dataview.meta.DBColumn;

/**
 * @author Shankari
 */
class InsertRecordTableUI extends ResultSetJXTable {

    boolean isRowSelectionAllowed = rowSelectionAllowed;

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        if (dataModel.getColumnCount() < 7) {
            setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        } else {
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }
    }   

    protected void appendEmptyRow() {
        Object[] row = new Object[getModel().getColumnCount()];
        for (int i = 0, I = getModel().getColumnCount(); i < I; i++) {
            DBColumn col = getModel().getColumn(i);
            if (col.isGenerated()) {
                row[i] = SQLConstant.GENERATED;
            } else if (col.hasDefault()) {
                row[i] = SQLConstant.DEFAULT;
            } else if (col.getJdbcType() == Types.TIMESTAMP) {
                row[i] = SQLConstant.CURRENT_TIMESTAMP;
            } else if (col.getJdbcType() == Types.DATE) {
                row[i] = SQLConstant.CURRENT_DATE;
            } else if (col.getJdbcType() == Types.TIME) {
                row[i] = SQLConstant.CURRENT_TIME;
            }
        }
        getModel().addRow(row);
    }

    protected void removeRows() {
        if (isEditing()) {
            getCellEditor().cancelCellEditing();
        }

        int[] rows = getSelectedRows();

        if (rows.length == 0) {
            return ;
        }

        int[] modelRows = new int[rows.length];

        for(int i = 0; i < modelRows.length; i++) {
            modelRows[i] = convertRowIndexToModel(rows[i]);
        }

        Arrays.sort(modelRows);

        for (int i = (modelRows.length - 1); i >= 0; i--) {
            getModel().removeRow(modelRows[i]);
        }
        if (getRowCount() == 0) {
            appendEmptyRow();
        }
    }
}
