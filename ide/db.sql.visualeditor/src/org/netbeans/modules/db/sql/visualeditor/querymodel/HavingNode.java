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
import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;


/**
 * Represents a HAVING clause in a SQL Table Expression
 */
public class HavingNode implements Having {

    // Fields

    private Expression   _condition; // simplified WHERE clause

    // Constructors

    public HavingNode () {
    }

    public HavingNode(Expression condition) {
        _condition = condition;
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    // For now, assume no joins
    public String genText(SQLIdentifiers.Quoter quoter) {
        String res="";    // NOI18N
        if (_condition != null) {
            res = " HAVING " + _condition.genText(quoter);  // NOI18N
        }

        return res;
    }


    // Methods

    // Accessors/Mutators

    public Expression getExpression() {
        return _condition;
    }

    void renameTableSpec(String oldTableSpec, String corrName) {

        if (_condition instanceof Predicate)
            ((Predicate) _condition).renameTableSpec(oldTableSpec, corrName);
    }

    // adds any column in the condition to the ArrayList of columns
    public void  getReferencedColumns (Collection columns) {
        if (_condition != null)
            _condition.getReferencedColumns(columns);
    }
}


