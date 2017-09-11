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

import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL Atomic Formula
 * Example Form: a.x = b.y
 */
public final class Predicate implements Expression {

    // Fields

    // ToDo: Generalize this, to allows arbitrary forms on both sides

    Value _val1, _val2;
//    ColumnNode _col1, _col2;
    String _op;


    // Constructors

    public Predicate() {
    }

    public Predicate(Value val1, Value val2) {
        this(val1, val2, "=");  // NOI18N
    }

    public Predicate (Value val1, Value val2, String op) {
        _val1 = val1;
        _val2 = val2;
        _op = op;
        }

    // Special ctor used when we have an FK
    public Predicate (String[] rel) {
        _val1 = new ColumnNode(rel[0], rel[1]);
        _val2 = new ColumnNode(rel[2], rel[3]);
        _op = "=";
    }

    // Methods

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        if ((_val1 instanceof ColumnNode) && (_val2 instanceof ColumnNode)) {
            ColumnNode col1 = (ColumnNode) _val1;
            ColumnNode col2 = (ColumnNode) _val2;
            if ((col1.matches(table1, column1) && col2.matches(table2, column2)) ||
                (col2.matches(table1, column1) && col1.matches(table2, column2))) {
                return this;
            }
        }
        return null;
    }

    // get the columns specified in the condition if any
    public void getReferencedColumns(Collection columns) {
        if (_val1 instanceof ColumnItem) {
            columns.add (((ColumnItem)_val1).getReferencedColumn());
        }

        if (_val2 instanceof ColumnItem) {
            columns.add(((ColumnItem)_val2).getReferencedColumn());
        }
    }

    // Return the Where clause as a SQL string
    public String genText(SQLIdentifiers.Quoter quoter) {
        return _val1.genText(quoter) + " " + _op + " " + _val2.genText(quoter); // NOI18N
    }

    public Value getVal1() {
        return _val1;
    }

    public void setVal1(Value val1 ) {
        _val1 = val1;
    }

    public Value getVal2() {
        return _val2;
    }

    public void setVal2(Value val2) {
        _val2 = val2;
    }

    public String getOp() {
        return _op;
    }

    public void setFields(String tableName1, String columnName1, String tableName2, String columnName2)
    {
        _val1 = new ColumnNode(tableName1, columnName1);
        _val2 = new ColumnNode(tableName2, columnName2);
    }

    /**
     * Return true if the Predicate is a criterion, rather than a relationship
     */
    public boolean isCriterion () {

        // If both sides of the predicate are columns, we have a relationship
        return ( ! ((getVal1() instanceof ColumnNode) && (getVal2() instanceof ColumnNode)));
    }

    /** Rename any column specs that use the old table spec
     */
    public void renameTableSpec(String oldTableSpec, String corrName) {

        if (_val1 instanceof ColumnNode)
            ((ColumnNode)_val1).renameTableSpec(oldTableSpec, corrName);
        if (_val2 instanceof ColumnNode)
            ((ColumnNode)_val2).renameTableSpec(oldTableSpec, corrName);
    }

    public boolean isParameterized() {
        return (_val1.isParameterized() || _val2.isParameterized());
    }

}
