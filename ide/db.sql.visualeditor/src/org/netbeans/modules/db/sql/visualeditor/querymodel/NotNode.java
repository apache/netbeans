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
 * Represents a SQL And term in a WHERE clause
 * Example Form: ((a.x = b.y) AND (c.w = d.v))
 */
public class NotNode implements Expression {

    // Fields

    // A condition

    Expression _cond;


    // Constructor

    public NotNode(Expression cond) {

        _cond = cond;
    }


    // Methods

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return _cond.findExpression(table1, column1, table2, column2);
    }

    // get the column specified in the condition if any
    public void getReferencedColumns(Collection comlumns) {
        _cond.getReferencedColumns(comlumns);
    }

    // Return the Where clause as a SQL string
    public String genText(SQLIdentifiers.Quoter quoter) {
        return " ( NOT " + _cond.genText(quoter) + ") ";  // NOI18N
    }

    public String toString() {
        return "";    // NOI18N
    }

    public boolean isParameterized() {
        return _cond.isParameterized();
    }

    public void renameTableSpec(String oldTableSpec, String corrName) {
        _cond.renameTableSpec(oldTableSpec, corrName);
    }
}
