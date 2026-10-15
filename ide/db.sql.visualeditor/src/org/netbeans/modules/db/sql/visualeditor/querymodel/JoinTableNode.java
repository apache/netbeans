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
 * Represents a generalized table specification in a SQL FROM  clause
 * Example forms:
 *      employees e
 *      INNER JOIN employees e ON e.id = e.id
 */
public class JoinTableNode implements JoinTable {

    // Fields

    private String      _joinType;  // INNER, OUTER, CROSS, NATURAL, ,
    private TableNode   _table;
    private Expression   _condition; // simplified WHERE clause

    // Constructors

    public JoinTableNode () {
    }

    public JoinTableNode(TableNode table, String joinType, Expression condition) {
        _table = table;
        _joinType = joinType;
        _condition = condition;
    }

    public JoinTableNode(TableNode table) {
        this(table, null, null);
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    // For now, assume no joins
    public String genText(SQLIdentifiers.Quoter quoter) {
        String res =
            (((_joinType==null)||(_joinType.equals("CROSS")))
             ? ", "
             : "          " +_joinType + " JOIN ")  // NOI18N
            + _table.genText(quoter, true);

        if (_condition != null) {
            res += " ON " + _condition.genText(quoter);  // NOI18N
        }

        return res;
    }


    // Special processing for the first table in the list
    // Omit the join specification
    public String genText(SQLIdentifiers.Quoter quoter, boolean first) {
        return (first ? _table.genText(quoter, true) : this.genText(quoter));
    }


    // Methods

    // Accessors/Mutators

    public Table getTable() {
        return _table;
    }

    public String getTableName() {
        return _table.getTableName();
    }

    public String getCorrName() {
        return _table.getCorrName();
    }

    public String getTableSpec() {
        return _table.getTableSpec();
    }

    public String getFullTableName() {
        return _table.getFullTableName();
    }

    public String getJoinType () {
        return _joinType;
    }

    public void setJoinType (String joinType) {
        _joinType = joinType;
    }

    public Expression getExpression() {
        return _condition;
    }

    public void setExpression(Expression condition) {
        _condition = condition;
    }

    // adds any column in the condition to the ArrayList of columns
    public void getReferencedColumns (Collection columns) {
        if (_condition != null)
            _condition.getReferencedColumns(columns);
    }

    void renameTableSpec(String oldTableSpec, String corrName) {
        ((TableNode)this.getTable()).renameTableSpec(oldTableSpec, corrName);

        if (_condition instanceof Predicate)
            ((Predicate)_condition).renameTableSpec(oldTableSpec, corrName);
    }

    void setTableSpec(String oldTableSpec, String newTableSpec)
    {
        ((TableNode)this.getTable()).setTableSpec(oldTableSpec, newTableSpec);
    }

    public void addJoinCondition(String[] rel) {

        // Convert relationship into join
        ColumnNode col1 = new ColumnNode(rel[0], rel[1]);
        ColumnNode col2 = new ColumnNode(rel[2], rel[3]);
        Predicate pred = new Predicate(col1, col2);

        // Update the JoinTable object with join information
        setJoinType("INNER");  // NOI18N
        setExpression(pred);
    }
}
