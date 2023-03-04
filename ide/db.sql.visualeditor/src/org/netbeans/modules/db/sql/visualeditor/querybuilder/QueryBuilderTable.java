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

import javax.swing.table.TableColumn;

import java.awt.*;


import org.netbeans.modules.db.sql.visualeditor.Log;
import org.openide.util.NbBundle;

// Represents the information presented inside a table node, which includes
// selected status, key status, and column name

public class QueryBuilderTable extends JTable {

    private boolean DEBUG = false;


    // Constructor

    public QueryBuilderTable( QueryBuilderTableModel model) {

        super();
        super.setModel( model );

        Log.getLogger().entering("QueryBuilderTable", "constructor", model); // NOI18N

        this.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

        // This may not be required afterall. We need to keep the size of the cell fixed.
        this.initColumnSizes(this, model);
        this.setShowHorizontalLines(false);
        this.setShowVerticalLines(false);
        this.setBackground(Color.white);
        this.setRowHeight(this.getRowHeight() + 2);
        this.setRowSelectionAllowed (false);
        this.setTableHeader (null);

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(QueryBuilderTable.class, "ACS_QueryBuilderTableName"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(QueryBuilderTable.class, "ACS_QueryBuilderTableDescription"));
    }


    // Methods

    private void initColumnSizes(JTable table, QueryBuilderTableModel model) {

        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;

        for (int i = 0; i < getColumnCount(); i++) {

            column = table.getColumnModel().getColumn(i);

            comp = table.getDefaultRenderer(column.getClass()).
                getTableCellRendererComponent(
                    table, column.getHeaderValue(),
                    false, false, -1, 0);
            headerWidth = comp.getPreferredSize().width;

            try {
                comp = column.getHeaderRenderer().
                    getTableCellRendererComponent(
                        null, column.getHeaderValue(),
                        false, false, 0, 0);
                headerWidth = comp.getPreferredSize().width;
            } catch (NullPointerException e) {
            }

            if ( i  != 0 )
            {
                for (int j=0; j< table.getRowCount(); j++)
                {
                    comp = table.getDefaultRenderer(model.getColumnClass(i)).
                        getTableCellRendererComponent(
                            table, getValueAt(j, i),
                            false, false, 0, i);
                    int tmpCellWidth = comp.getPreferredSize().width;

                    if ( tmpCellWidth > cellWidth )
                        cellWidth = tmpCellWidth;
                }
            }

            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(headerWidth+15, cellWidth+15));
        }

        table.addNotify();
    }
}
