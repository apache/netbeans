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

public class GroupByNode implements GroupBy {

    // Fields

    // A vector of Column specifications
    // This will eventually include functions, but for now is simple columns

    // ToDo: consider replacing this with a HashMap
    private List<Column> _columnList;


    // Constructor

    public GroupByNode() {
    }

    public GroupByNode(List columnList) {
        _columnList = columnList;
    }


    // Return the Select clause as a SQL string

    public String genText(SQLIdentifiers.Quoter quoter) {
        String res = " GROUP BY ";  // NOI18N

        if (_columnList.size() > 0) {
            res += ((ColumnNode)_columnList.get(0)).genText(quoter);
            for (int i=1; i<_columnList.size(); i++) {
                res += ", " + ((ColumnNode)_columnList.get(i)).genText(quoter);    // NOI18N
            }
        }

        return res;
    }


    // Accessors/Mutators

    public void setColumnList(List columnList) {
        _columnList = columnList;
    }

    // adds any column in the condition to the ArrayList of columns
    public void getReferencedColumns(Collection columns) {
        if (_columnList != null)
            columns.addAll(_columnList);
    }

    public void addColumn(Column col) {
        _columnList.add(col);
    }

    public void addColumn(String tableSpec, String columnName) {
        _columnList.add(new ColumnNode(tableSpec, columnName));
    }

    // Remove the specified column from the SELECT list
    // Iterate back-to-front for stability under deletion
    public void removeColumn(String tableSpec, String columnName) {
        for (int i=_columnList.size()-1; i>=0; i--) {
            ColumnNode c = (ColumnNode) _columnList.get(i);
            if ((c.getTableSpec().equals(tableSpec)) &&
                (c.getColumnName().equals(columnName))) {
                _columnList.remove(i);
            }
        }
    }

    /**
     * Remove any GroupBy targets that reference this table
     */
    void removeTable (String tableSpec) {
        for (int i=_columnList.size()-1; i>=0; i--) {
            ColumnNode c = (ColumnNode) _columnList.get(i);
            if (c.getTableSpec().equals(tableSpec))
                _columnList.remove(i);
        }
    }

    void renameTableSpec(String oldTableSpec, String corrName) {

        for (int i=0; i<_columnList.size(); i++)
            ((ColumnNode)_columnList.get(i)).renameTableSpec(oldTableSpec, corrName);
    }
}
