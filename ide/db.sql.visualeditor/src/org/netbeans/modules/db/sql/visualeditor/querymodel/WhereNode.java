/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

// ToDo: Decide whether a null WHERE clause is better represented as a null
// ptr, or a Where with null condition.

/**
 * Represents a SQL WHERE clause
 * Example Form: WHERE ((a.x = b.y) AND (c.w = d.v))
 */
public class WhereNode implements Where {

    // Fields

    // This could be an AND or (in future) OR
    private Expression _cond;

    // Constructors

    public WhereNode() {
        _cond = null;
    }

    public WhereNode(Expression cond) {
        _cond = cond;
//         if (cond instanceof Predicate)
//             _cond = new And((Predicat)_cond);
//         else
//             _cond = cond;
    }


    // Accessors/mutators

    public void resetExpression() {
        _cond = null;
    }

    public void replaceExpression(Expression expression) {
        _cond = expression;
    }

    public Expression getExpression () {
        return _cond;
    }

    // Methods

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return _cond.findExpression(table1, column1, table2, column2);
    }

    // Remove any WHERE clauses that mention the table in question,
    // since the table itself is being removed from the model
    void removeTable(String tableSpec) {
        if (_cond instanceof ExpressionList) {
            ExpressionList list = (ExpressionList)_cond;
            list.removeTable(tableSpec);
            if (list.size() == 0)
                _cond = null;
        }
        else {
            List<Column> column = new ArrayList<>();
            _cond.getReferencedColumns(column);
            for (int i = 0; i < column.size(); i++) {
                Column col = column.get(i);
                if (col.matches(tableSpec)) {
                    _cond = null;
                }
            }
        }
    }

    // adds any column in the condition to the ArrayList of columns
    public void getReferencedColumns (Collection columns) {
        if (_cond != null)
            _cond.getReferencedColumns (columns);
    }

    // Return the Where clause as a SQL string
    public String genText(SQLIdentifiers.Quoter quoter) {
        if (_cond!=null)
            return " WHERE " + _cond.genText(quoter) ;  // NOI18N
        else
            return "";  // NOI18N
    }

    // See if we have a parameter marker (string literal "?")
    public boolean isParameterized() {
        if (_cond!=null)
            return _cond.isParameterized();
        else
            return false;
    }


    void renameTableSpec(String oldTableSpec, String corrName) {
        _cond.renameTableSpec(oldTableSpec, corrName);
    }
}
