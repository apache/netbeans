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

/**
 *
 * @author  Sanjay Dhamankar
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

// The model for a QueryBuilderTable

// This is a table with one row for each DB column (plus one for "all columns").
// The table has three columns:
//     0 - selected status
//     1 - URL for icon for key status
//     2 - column name.

public class QueryBuilderTableModel extends DefaultTableModel {

    // Private variables

    private boolean DEBUG = false;

    // tableSpec may be redundant with tableName
    private String _tableName = null;
    private String _corrName = null;
    private String _schemaName = null;

    // Constructor

    public QueryBuilderTableModel ( String fullTableName, String corrName,
                                    String[] iColumnNames, Object[][] iData )
    {
        super ( iData, iColumnNames );
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
            _schemaName=table[0];
            _tableName = table[1];
        } else
            _tableName=table[0];

        _corrName = corrName;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        if ( c == 1 )
            return javax.swing.ImageIcon.class;
        Object o = getValueAt(0, c);
        if ( o != null )
        {
            return o.getClass();
        }
        else return Object.class;
    }

    public String getTableName () {
        return ( _tableName );
    }

    public String getFullTableName () {
        return ( (_schemaName!=null ? _schemaName+"." : "")     // NOI18N
         + _tableName);
    }

    public String getCorrName () {
        return ( _corrName );
    }

    public String getTableSpec () {
        return ( (_corrName!=null) ?
         _corrName :
         getFullTableName());
    }

    // Mark a particular column as Selected/Deselected
    void selectColumn (String columnName, Boolean select) {
        int row=-1;
        int size = getRowCount();
        for (int i=0; i<size; i++)
            if (getValueAt(i,2).equals(columnName)) {
                row=i;
                break;
            }

        if (row!=-1) {
            // Prevent loops - Only make a change to the model if the current value is wrong
            if ((select==Boolean.TRUE) && (getValueAt(row,0)!=Boolean.TRUE))
                setValueAt(Boolean.TRUE,row,0);
            else if ((select==Boolean.FALSE) && (getValueAt(row,0)!=Boolean.FALSE))
                setValueAt(Boolean.FALSE,row,0);
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        // Temporary fix to make "*{All Columns}" unselectable.
        /*
        if (row == 0)
            return false;
        */
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col > 0) {
            return false;
        } else {
            return true;
        }
    }
}

