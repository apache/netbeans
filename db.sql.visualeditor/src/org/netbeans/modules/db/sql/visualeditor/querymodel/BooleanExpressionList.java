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

    static protected void flattenExpression(List expressionsToFlatten, Class typeToLookFor, List expressions) {
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


