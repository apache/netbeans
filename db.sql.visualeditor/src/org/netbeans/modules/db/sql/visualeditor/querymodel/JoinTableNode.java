/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.db.sql.visualeditor.querymodel;

/**
 * Represents a generalized table specification in a SQL FROM  clause
 * Example forms:
 *      employees e
 *      INNER JOIN employees e ON e.id = e.id
 */

import org.netbeans.modules.db.sql.visualeditor.querybuilder.QueryBuilderMetaData;

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

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
