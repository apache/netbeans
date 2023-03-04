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

import org.netbeans.modules.j2ee.dd.api.ejb.Query;

import javax.swing.table.TableCellEditor;

/**
 * @author pfiala
 */
public class SelectMethodsTableModel extends QueryMethodsTableModel {
    
    protected static final String[] COLUMN_NAMES = {Utils.getBundleMessage("LBL_Method"),
    Utils.getBundleMessage("LBL_ReturnType"),
    Utils.getBundleMessage("LBL_Query"),
    Utils.getBundleMessage("LBL_Description")};
    protected static final int[] COLUMN_WIDTHS = new int[]{200, 100, 200, 100};
    
    public SelectMethodsTableModel(EntityHelper.Queries queries) {
        super(COLUMN_NAMES, COLUMN_WIDTHS, queries);
    }
    
    public int addRow() {
        return getRowCount() - 1;
    }
    
    
    public boolean editRow(int row) {
        return true;
    }
    
    public QueryMethodHelper getQueryMethodHelper(int row) {
        return queries.getSelectMethodHelper(row);
    }
    
    public int getRowCount() {
        return queries.getSelectMethodCount();
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        QueryMethodHelper queryMethodHelper = getQueryMethodHelper(rowIndex);
        switch (columnIndex) {
            case 0:
                return queryMethodHelper.getQueryMethod().getMethodName();
            case 1:
                return queryMethodHelper.getReturnType();
            case 2:
                return queryMethodHelper.getEjbQl();
            case 3:
                return queryMethodHelper.getDefaultDescription();
        }
        return null;
    }
    
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Query query = (Query) queries.getSelecMethod(rowIndex).clone();
        if (columnIndex == 3) {
            query.setDescription((String) value);
        }
    }
    
    @Override
    public TableCellEditor getCellEditor(int columnIndex) {
        return null;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 3) {
            return true;
        } else {
            return super.isCellEditable(rowIndex, columnIndex);
        }
    }
}
