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
 * Represents a SQL WHERE clause
 * Example Form: WHERE ((a.x = b.y) AND (c.w = d.v))
 */
// ToDo: Decide whether a null WHERE clause is better represented as a null
// ptr, or a Where with null condition.

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

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
            ArrayList column = new ArrayList();
            _cond.getReferencedColumns(column);
            for (int i = 0; i < column.size(); i++) {
                Column col = (Column)column.get(i);
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
