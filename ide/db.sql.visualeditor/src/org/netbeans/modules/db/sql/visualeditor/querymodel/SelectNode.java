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
package org.netbeans.modules.db.sql.visualeditor.querymodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

public class SelectNode implements Select {

    // Fields

    // A vector of Column specifications
    // This will eventually include functions, but for now is simple columns

    // ToDo: consider replacing this with a HashMap
    private List<Column>   _selectItemList;
    private String _quantifier;


    // Constructor

    public SelectNode() {
    }

    public SelectNode(List columnList, String quantifier) {
        _selectItemList = columnList;
        _quantifier = quantifier;
    }

    public SelectNode(List columnList) {
        this(columnList, "");  // NOI18N
    }


    // Return the Select clause as a SQL string

    public String genText(SQLIdentifiers.Quoter quoter) {
        String res = "";  // NOI18N
        String res_select_quantifier = "";  // NOI18N

        if (_selectItemList.size() > 0) {
            res_select_quantifier = (_quantifier.length() == 0) ? "SELECT " : "SELECT " + _quantifier + " " ; // NOI18N
            res = res_select_quantifier
		+ ((ColumnItem)_selectItemList.get(0)).genText(quoter, true);  // NOI18N

            for (int i=1; i<_selectItemList.size(); i++) {
                ColumnItem col = (ColumnItem)_selectItemList.get(i);
                if (col != null)
                {
                    res += ", "  + col.genText(quoter, true);  // NOI18N
                }
            }
        }
        return res;
    }


    // Accessors/Mutators

    public void setColumnList(List columnList) {
        _selectItemList = columnList;
    }

    public void getReferencedColumns(Collection columns) {
        for (int i = 0; i < _selectItemList.size(); i++)
            columns.add(((ColumnItem)_selectItemList.get(i)).getReferencedColumn());
    }

    public int getSize() {
        return _selectItemList.size();
    }
    
    public void addColumn(Column col) {
        _selectItemList.add(col);
    }

    public void addColumn(String tableSpec, String columnName) {
        _selectItemList.add(new ColumnNode(tableSpec, columnName));
    }

    // Remove the specified column from the SELECT list
    // Iterate back-to-front for stability under deletion
    public void removeColumn(String tableSpec, String columnName) {
        for (int i=_selectItemList.size()-1; i>=0; i--) {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if ((c != null) && (c.getTableSpec().equals(tableSpec)) && (c.getColumnName().equals(columnName)))
            {
                _selectItemList.remove(i);
            }
        }
    }

    /**
     * set column name
     */
    public void setColumnName (String oldColumnName, String newColumnName) {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnNode c = (ColumnNode) _selectItemList.get(i);
            if ( c != null) {
                c.setColumnName(oldColumnName, newColumnName);
            }
        }
    }

    public boolean hasAsteriskQualifier() {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            if (item instanceof ColumnNode) {
                ColumnNode c = (ColumnNode) item;
                if (c.getColumnName().equals("*")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove any SELECT targets that reference this table
     */
    void removeTable (String tableSpec) {
        for (int i=_selectItemList.size()-1; i>=0; i--) {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if (c != null) {
                String tabSpec = c.getTableSpec();
                if (tabSpec != null && tabSpec.equals(tableSpec))
                    _selectItemList.remove(i);
            }
        }
    }

    /**
     * Rename a table
     */
    void renameTableSpec (String oldTableSpec, String corrName) {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if ( c != null)
            {
                c.renameTableSpec(oldTableSpec, corrName);
            }
        }
    }

}
