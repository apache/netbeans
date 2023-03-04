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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import org.openide.util.NbBundle;
import javax.swing.table.DefaultTableModel;

class QueryBuilderInputTableModel extends DefaultTableModel {

    // Variables

    final String[] columnNames = {
        // "Column",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "COLUMN"),       // NOI18N
        // "Alias",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "ALIAS"),         // NOI18N
        // "Table",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "TABLE"),        // NOI18N
        // "Output",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OUTPUT"),       // NOI18N
        // "Sort Type",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "SORT_TYPE"),        // NOI18N
        // "Sort Order",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "SORT_ORDER"),       // NOI18N
        // "Criteria",
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "CRITERIA"),         // NOI18N
        // "Criteria Order"
        NbBundle.getMessage(QueryBuilderInputTableModel.class, "CRITERIA_ORDER"),       // NOI18N
        // "Or...",
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
        // "Or...",
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
        // "Or..."
//        NbBundle.getMessage(QueryBuilderInputTableModel.class, "OR"),       // NOI18N
    };

    Object[][] data = {
        { "", "", "", "", Boolean.FALSE, "", "", "" /*, "", "", "" */ }       // NOI18N
    };


    // Constructor

    public QueryBuilderInputTableModel ()
    {
        super(0, 10);
        setColumnIdentifiers ( columnNames );
    }


    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        if ( getRowCount() == 0 ) return String.class;       // NOI18N
        if ( getValueAt(0,c) == null ) return String.class;      // NOI18N
        return getValueAt(0, c).getClass();
    }


    /*
     * Don't need to implement this method unless your table's editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if ((col==QueryBuilderInputTable.Column_COLUMN) ||
            (col==QueryBuilderInputTable.Table_COLUMN)) {
            return false;
        }
        else if ( col==QueryBuilderInputTable.Criteria_COLUMN &&
                  getValueAt(row, col).equals (
                      QueryBuilderInputTable.Criteria_Uneditable_String) ) {
            return false;
        }
        else if ( col==QueryBuilderInputTable.CriteriaOrder_COLUMN &&
                  getValueAt(row, col).equals (
                    QueryBuilderInputTable.CriteriaOrder_Uneditable_String ) ) {
            return false;
        }
        else {
            return true;
        }
    }
}

