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

public class GroupByNode implements GroupBy {

    // Fields

    // A vector of Column specifications
    // This will eventually include functions, but for now is simple columns

    // ToDo: consider replacing this with a HashMap
    private List _columnList;


    // Constructor

    public GroupByNode() {
    }

    public GroupByNode(ArrayList columnList) {
        _columnList = columnList;
    }


    // Return the Select clause as a SQL string

    public String genText(SQLIdentifiers.Quoter quoter) {
        String res = " GROUP BY ";  // NOI18N

        if (_columnList.size() > 0) {
            res += ((ColumnNode)_columnList.get(0)).genText(quoter);
            for (int i=1; i<_columnList.size(); i++) {
                res += ", " + ((ColumnNode)_columnList.get(i)).genText(quoter);    // NOI18N
            }
        }

        return res;
    }


    // Accessors/Mutators

    public void setColumnList(List columnList) {
        _columnList = columnList;
    }

    // adds any column in the condition to the ArrayList of columns
    public void getReferencedColumns(Collection columns) {
        if (_columnList != null)
            columns.addAll(_columnList);
    }

    public void addColumn(Column col) {
        _columnList.add(col);
    }

    public void addColumn(String tableSpec, String columnName) {
        _columnList.add(new ColumnNode(tableSpec, columnName));
    }

    // Remove the specified column from the SELECT list
    // Iterate back-to-front for stability under deletion
    public void removeColumn(String tableSpec, String columnName) {
        for (int i=_columnList.size()-1; i>=0; i--) {
            ColumnNode c = (ColumnNode) _columnList.get(i);
            if ((c.getTableSpec().equals(tableSpec)) &&
                (c.getColumnName().equals(columnName))) {
                _columnList.remove(i);
            }
        }
    }

    /**
     * Remove any GroupBy targets that reference this table
     */
    void removeTable (String tableSpec) {
        for (int i=_columnList.size()-1; i>=0; i--) {
            ColumnNode c = (ColumnNode) _columnList.get(i);
            if (c.getTableSpec().equals(tableSpec))
                _columnList.remove(i);
        }
    }

    void renameTableSpec(String oldTableSpec, String corrName) {

        for (int i=0; i<_columnList.size(); i++)
            ((ColumnNode)_columnList.get(i)).renameTableSpec(oldTableSpec, corrName);
    }
}
