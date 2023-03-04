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

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a column in an ORDER BY clause
 */
public final class SortSpecification implements QueryItem {

    // Fields

    private ColumnItem _column;

    // direction is one of standard SQL 'ASC' or DESC'
    private String _direction;


    // Constructors

    public SortSpecification(ColumnItem col, String direction) {
        _column = col;
        _direction = direction;
    }

    public SortSpecification(ColumnItem col) {
        this(col, "ASC");  // NOI18N
    }


    // Methods

    public String genText(SQLIdentifiers.Quoter quoter) {
        return _column.genText(quoter) + " " +  // NOI18N
              _direction;
    }


    // Accessors/Mutators

    public String getDirection() {
        return _direction;
    }

    public Column getColumn() {
        if (_column instanceof ColumnNode) return (Column)_column;  return null;
    }

    public void  getReferencedColumns(Collection columns) {
        columns.add(_column.getReferencedColumn());
    }

    void renameTableSpec(String oldTableSpec, String corrName) {
        _column.renameTableSpec(oldTableSpec, corrName);
    }

}


