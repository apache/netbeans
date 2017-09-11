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
import java.util.Collection;

import org.netbeans.api.db.sql.support.SQLIdentifiers;

public class SelectNode implements Select {

    // Fields

    // A vector of Column specifications
    // This will eventually include functions, but for now is simple columns

    // ToDo: consider replacing this with a HashMap
    private ArrayList _selectItemList;
    private String _quantifier;


    // Constructor

    public SelectNode() {
    }

    public SelectNode(ArrayList columnList, String quantifier) {
        _selectItemList = columnList;
        _quantifier = quantifier;
    }

    public SelectNode(ArrayList columnList) {
        this(columnList, "");  // NOI18N
    }


    // Return the Select clause as a SQL string

    public String genText(SQLIdentifiers.Quoter quoter) {
        String res = "";  // NOI18N
        String res_select_quantifier = "";  // NOI18N

        if (_selectItemList.size() > 0) {
            res_select_quantifier = (_quantifier.length() == 0) ? "SELECT " : "SELECT " + _quantifier + " " ; // NOI18N
            res = res_select_quantifier
		+ ((ColumnItem)_selectItemList.get(0)).genText(quoter, true);  // NOI18N

            for (int i=1; i<_selectItemList.size(); i++) {
                ColumnItem col = (ColumnItem)_selectItemList.get(i);
                if (col != null)
                {
                    res += ", "  + col.genText(quoter, true);  // NOI18N
                }
            }
        }
        return res;
    }


    // Accessors/Mutators

    public void setColumnList(ArrayList columnList) {
        _selectItemList = columnList;
    }

    public void getReferencedColumns(Collection columns) {
        for (int i = 0; i < _selectItemList.size(); i++)
            columns.add(((ColumnItem)_selectItemList.get(i)).getReferencedColumn());
    }

    public int getSize() {
        return _selectItemList.size();
    }
    
    public void addColumn(Column col) {
        _selectItemList.add(col);
    }

    public void addColumn(String tableSpec, String columnName) {
        _selectItemList.add(new ColumnNode(tableSpec, columnName));
    }

    // Remove the specified column from the SELECT list
    // Iterate back-to-front for stability under deletion
    public void removeColumn(String tableSpec, String columnName) {
        for (int i=_selectItemList.size()-1; i>=0; i--) {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if ((c != null) && (c.getTableSpec().equals(tableSpec)) && (c.getColumnName().equals(columnName)))
            {
                _selectItemList.remove(i);
            }
        }
    }

    /**
     * set column name
     */
    public void setColumnName (String oldColumnName, String newColumnName) {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnNode c = (ColumnNode) _selectItemList.get(i);
            if ( c != null) {
                c.setColumnName(oldColumnName, newColumnName);
            }
        }
    }

    public boolean hasAsteriskQualifier() {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            if (item instanceof ColumnNode) {
                ColumnNode c = (ColumnNode) item;
                if (c.getColumnName().equals("*")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove any SELECT targets that reference this table
     */
    void removeTable (String tableSpec) {
        for (int i=_selectItemList.size()-1; i>=0; i--) {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if (c != null) {
                String tabSpec = c.getTableSpec();
                if (tabSpec != null && tabSpec.equals(tableSpec))
                    _selectItemList.remove(i);
            }
        }
    }

    /**
     * Rename a table
     */
    void renameTableSpec (String oldTableSpec, String corrName) {
        for (int i=0; i<_selectItemList.size(); i++)  {
            ColumnItem item = (ColumnItem) _selectItemList.get(i);
            ColumnNode c = (ColumnNode) item.getReferencedColumn();
            if ( c != null)
            {
                c.renameTableSpec(oldTableSpec, corrName);
            }
        }
    }

}
