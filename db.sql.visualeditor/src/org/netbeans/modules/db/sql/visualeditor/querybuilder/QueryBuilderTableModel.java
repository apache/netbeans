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

/**
 *
 * @author  Sanjay Dhamankar
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import javax.swing.JTable;

import javax.swing.table.DefaultTableModel;

// The model for a QueryBuilderTable

// This is a table with one row for each DB column (plus one for "all columns").
// The table has three columns:
//     0 - selected status
//     1 - URL for icon for key status
//     2 - column name.

public class QueryBuilderTableModel extends DefaultTableModel {

    // Private variables

    private boolean DEBUG = false;

    // tableSpec may be redundant with tableName
    private String _tableName = null;
    private String _corrName = null;
    private String _schemaName = null;

    // Constructor

    public QueryBuilderTableModel ( String fullTableName, String corrName,
                                    String[] iColumnNames, Object[][] iData )
    {
        super ( iData, iColumnNames );
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
            _schemaName=table[0];
            _tableName = table[1];
        } else
            _tableName=table[0];

        _corrName = corrName;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        if ( c == 1 )
            return javax.swing.ImageIcon.class;
        Object o = getValueAt(0, c);
        if ( o != null )
        {
            return o.getClass();
        }
        else return Object.class;
    }

    public String getTableName () {
        return ( _tableName );
    }

    public String getFullTableName () {
        return ( (_schemaName!=null ? _schemaName+"." : "")     // NOI18N
         + _tableName);
    }

    public String getCorrName () {
        return ( _corrName );
    }

    public String getTableSpec () {
        return ( (_corrName!=null) ?
         _corrName :
         getFullTableName());
    }

    // Mark a particular column as Selected/Deselected
    void selectColumn (String columnName, Boolean select) {
        int row=-1;
        int size = getRowCount();
        for (int i=0; i<size; i++)
            if (getValueAt(i,2).equals(columnName)) {
                row=i;
                break;
            }

        if (row!=-1) {
            // Prevent loops - Only make a change to the model if the current value is wrong
            if ((select==Boolean.TRUE) && (getValueAt(row,0)!=Boolean.TRUE))
                setValueAt(Boolean.TRUE,row,0);
            else if ((select==Boolean.FALSE) && (getValueAt(row,0)!=Boolean.FALSE))
                setValueAt(Boolean.FALSE,row,0);
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        // Temporary fix to make "*{All Columns}" unselectable.
        /*
        if (row == 0)
            return false;
        */
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col > 0) {
            return false;
        } else {
            return true;
        }
    }
}

