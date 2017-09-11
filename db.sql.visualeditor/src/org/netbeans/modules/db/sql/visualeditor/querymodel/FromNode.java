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

import org.netbeans.api.db.sql.support.SQLIdentifiers;

/**
 * Represents a SQL FROM clause
 */
public class FromNode implements From {

    // Fields

    // A vector of generalized Table objects (JoinTables)

    ArrayList _tableList;


    // Constructors

    public FromNode() {
    }

    public FromNode(ArrayList tableList) {
        _tableList = tableList;
    }


    // Methods

    // Return the SQL string that corresponds to this From clause
    public String genText(SQLIdentifiers.Quoter quoter) {
        String res = "";    // NOI18N

        if (_tableList.size() > 0) {

            res = " FROM " + ((JoinTableNode)_tableList.get(0)).genText(quoter, true);  // NOI18N

            for (int i=1; i<_tableList.size(); i++)
                res += ((JoinTableNode)_tableList.get(i)).genText(quoter);
        }

        return res;
    }


    public String toString() {

        String res = "FROM:";  // NOI18N

        for (int i=0; i<_tableList.size(); i++)
            res += ((JoinTableNode)_tableList.get(i)).toString() + "";    // NOI18N

        return res;
    }


    // Methods

    // Accessors/Mutators

    public List getTableList() {

        return _tableList;
    }

    // Return the Table objects in the table list

    ArrayList getTables() {
        ArrayList tableRefs = new ArrayList();
        for (int i=0; i<_tableList.size(); i++)
            tableRefs.add(((JoinTableNode)_tableList.get(i)).getTable());
        return tableRefs;
    }

    // Return the name of the penultimate table in the current FROM list,
    // to see if it's a join candidate
    public String getPreviousTableFullName() {
        JoinTableNode jt = (JoinTableNode)_tableList.get(_tableList.size()-2);
        return jt.getTable().getFullTableName();
    }


    // Return the Table object with this tablespec

    public Table findTable(String tableSpec) {
        for (int i=0; i<_tableList.size(); i++) {
            JoinTableNode jt = (JoinTableNode) _tableList.get(i);
            if (jt.getTableSpec().equals(tableSpec))
                return jt.getTable();
        }
        return null;
    }

    public JoinTable findJoinTable(String table1, String column1, String table2, String column2) {
        ArrayList tableList = _tableList;
        for (int i=0; i<tableList.size(); i++) {
            JoinTableNode jt = (JoinTableNode) tableList.get(i);
            Expression cond = jt.getExpression();
            if (cond instanceof Predicate) {
                Predicate pred = (Predicate) cond;
                Value val1 = pred.getVal1();
                Value val2 = pred.getVal2();
                if ((val1 instanceof ColumnNode) && (val2 instanceof ColumnNode)) {
                    ColumnNode col1 = (ColumnNode) val1;
                    ColumnNode col2 = (ColumnNode) val2;
                    if (((col1.getTableSpec().equals(table1)) &&
                         (col1.getColumnName().equals(column1)) &&
                         (col2.getTableSpec().equals(table2)) &&
                         (col2.getColumnName().equals(column2))) ||
                        ((col2.getTableSpec().equals(table1)) &&
                         (col2.getColumnName().equals(column1)) &&
                         (col1.getTableSpec().equals(table2)) &&
                         (col1.getColumnName().equals(column2)))) {
                        return jt;
                    }
                }
            }
        }
        return null;
    }

    public String getFullTableName(String corrName) {
        for (int i=0; i<_tableList.size(); i++) {
            JoinTable jt = (JoinTable) _tableList.get(i);
            String cn=jt.getTableSpec();
            if ((cn!=null) && (cn.equals(corrName)))
                return jt.getFullTableName();
        }
        return null;
    }

    public String getTableSpec(String fullTableName) {
        for (int i=0; i<_tableList.size(); i++) {
            JoinTable jt = (JoinTable) _tableList.get(i);
            String cn=jt.getFullTableName();
            if ((cn!=null) && (cn.equals(fullTableName)))
                return jt.getTableSpec();
        }
        return null;
    }

    
    // Graph manipulation methods

    public void addTable(JoinTable jt) {
        _tableList.add(jt);
    }

    // Remove this table from the FROM list
    void removeTable(String tableSpec) {
        for (int i=_tableList.size()-1; i>=0; i--) 
            if (((JoinTableNode)_tableList.get(i)).getTableSpec().equals(tableSpec))
                _tableList.remove(i);
    }

    /**
     * Rename a table
     */
    void renameTableSpec(String oldTableSpec, String corrName) {
        for (int i=0; i<_tableList.size(); i++) {
            JoinTableNode jt = (JoinTableNode) _tableList.get(i);
            jt.renameTableSpec(oldTableSpec, corrName);
        }
    }
    

    /**
     * set table name
     */
    public void setTableSpec(String oldTableSpec, String newTableSpec) {
        for (int i=0; i<_tableList.size(); i++) {
            JoinTableNode jt = (JoinTableNode) _tableList.get(i);
            jt.setTableSpec(oldTableSpec, newTableSpec);
        }
    }

    public void getReferencedColumns(Collection columns) {}
}
