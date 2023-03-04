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

abstract class BooleanExpressionList implements ExpressionList {
    protected List _expressions;

    // New: Return the count of criteria
    public Expression findExpression(String table1, String column1, String table2, String column2) {
        if (_expressions != null) {
            for (int index=0; index<_expressions.size(); index++) {
                Expression cond = (Expression)_expressions.get(index);
                cond = cond.findExpression(table1, column1, table2, column2);
                if (cond != null)
                    return cond;
            }
        }
        return null;
    }

    // get the columns specified in the condition if any
    public void getReferencedColumns(Collection comlumns) {
        if (_expressions != null) {
            for (int index=0; index<_expressions.size(); index++) {
                Expression expr = (Expression)_expressions.get(index);
                expr.getReferencedColumns(comlumns);
            }
        }
    }

    public int size() {
        if (_expressions != null)
            return _expressions.size();
        return 0;
    }

    public Expression getExpression(int i) {
        return (Expression)_expressions.get(i);
    }

    public void addExpression(Expression expression) {
        _expressions.add(expression);
    }

    public void addExpression(int index, Expression expression) {
        _expressions.add(index, expression);
    }

    public void replaceExpression(int index, Expression expression) {
        _expressions.remove(index);
        _expressions.add(index, expression);
    }

    public void removeExpression(int index) {
        _expressions.remove(index);
    }

    public void removeTable(String tableSpec) {
        int size = _expressions.size();
        for (int i = size - 1; i >= 0; i--) {
            Expression expr = (Expression)_expressions.get(i);
            if (expr instanceof ExpressionList) {
                ExpressionList list = (ExpressionList)expr;
                list.removeTable(tableSpec);
                if (list.size() == 0) {
                    // this is odd and needs a bit more thought. It should never happen though.
                    // anyway remove the expressions and run this method again
                    _expressions.remove(i);
                }
            }
            else {
                // for any other kind of expression remove the whole expression if the table is referenced
                ArrayList column = new ArrayList();
                expr.getReferencedColumns(column);
                for (int j = 0; j < column.size(); j++) {
                    Column col = (Column)column.get(j);
                    if (col.matches(tableSpec)) {
                        _expressions.remove(i);
                        break;
                    }
                }
            }
        }
    }

    public boolean isParameterized() {
        if (_expressions != null) {
            for (int index=0; index<_expressions.size(); index++) {
                Expression expr = (Expression)_expressions.get(index);
                if (expr.isParameterized())
                    return true;
            }
        }
        return false;
    }

    public void renameTableSpec(String oldTableSpec, String corrName) {
        if (_expressions != null) {
            for (int index=0; index<_expressions.size(); index++) {
                Expression expr = (Expression)_expressions.get(index);
                expr.renameTableSpec(oldTableSpec, corrName);
            }        
        }
    }

    protected static void flattenExpression(List expressionsToFlatten, Class typeToLookFor, List expressions) {
        int size = expressionsToFlatten.size();
        for (int i = 0; i < size; i++) {
            Object expr = expressionsToFlatten.get(i);
            if (expr.getClass() == typeToLookFor) {
                flattenExpression(((BooleanExpressionList)expr)._expressions, typeToLookFor, expressions);
            }
            else {
                expressions.add(expr);
            }
        }
    }
}


