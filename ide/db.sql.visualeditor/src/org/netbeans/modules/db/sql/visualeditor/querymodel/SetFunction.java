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
 * Represents a SQL Set function (AVG, COUNT, MAX, MIN, SUM)
 * Example Form: SUM(Orders.Quantity), MAX(Employee.Salary), COUNT(Employee.Name)
 */

public class SetFunction extends ColumnItem implements UnaryExpression {

    public static final int NONE = 0;
    public static final int AVG = 1;
    public static final int COUNT = 2;
    public static final int MAX = 3;
    public static final int MIN = 4;
    public static final int SUM = 5;

    private int _type;
    private ColumnNode _argument;
    private Identifier _alias;

    private SetFunction() { }

    public SetFunction(int type, ColumnNode argument, Identifier alias) {
        _type = type;
        _argument = argument;
        _alias = alias;
    }

    Column getReferencedColumn() {
        return _argument;
    }

    public void getReferencedColumns(Collection columns) {
        columns.add(_argument);
    }

    public String genText(SQLIdentifiers.Quoter quoter) {
        String funcType = null;
        switch (_type) {
            case AVG:
                funcType = "AVG(";
                break;
            case COUNT:
                funcType = "COUNT(";
                break;
            case MAX:
                funcType = "MAX(";
                break;
            case MIN:
                funcType = "MIN(";
                break;
            case SUM:
                funcType = "SUM(";
                break;
            default:
                break;
        }
        funcType += _argument.genText(quoter);
        funcType += ")";
        if (_alias != null) {
            funcType += " AS " + _alias.genText(quoter);
        }
        return funcType;
    }

    public Expression findExpression(String table1, String column1, String table2, String column2) {
        return null;
    }

    /**
     * Rename the table part of the column spec
     */
    public void renameTableSpec(String oldTableSpec, String corrName) {
        _argument.renameTableSpec(oldTableSpec, corrName);
    }

    public boolean isParameterized() {
        return false;
    }

    public Expression getOperand() {
        return _argument;
    }

}
