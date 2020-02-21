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
package org.netbeans.modules.cnd.makeproject.ui.runprofiles;

import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Model for editing lists in a table. Used for editing lists with
 * one or two columns where the values are strings.
 *
 * Design note: I could have made this slightly more general for use
 * with N columns instead of 1 and 2, but I don't want to do the
 * extra array creation
 */
public class ListTableModel extends AbstractTableModel {

    int rowCount = 0;
    private JTable table = null;
    private String header0 = null;
    private String header1 = null;
    int colCount = 0;
    private ArrayList<String> column0 = null;
    private ArrayList<String> column1 = null;

    public ListTableModel() {
        super();
    }

    public ListTableModel(String h1, String h2) {
        super();
        if (h2 == null) {
            colCount = 1;
        } else {
            colCount = 2;
        }

        header0 = h1;
        header1 = h2;
        column0 = new ArrayList<>(5);
        column1 = new ArrayList<>(5);

        // Start out with one empty row
        column0.add(""); // NOI18N
        column1.add(""); // NOI18N
        rowCount = 1;
    }

    @Override
    public int getRowCount() {
        return rowCount;
    }

    @Override
    public int getColumnCount() {
        return colCount;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return header0;
        } else {
            return header1;
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return column0.get(rowIndex);
        } else {
            return column1.get(rowIndex);
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        //System.out.println("Setting value " + value + " at rowIndex " + rowIndex + " and columnIndex " + columnIndex + " where rowCount is " + rowCount);
        if (columnIndex == 0) {
            column0.set(rowIndex, (String) value);
        } else {
            column1.set(rowIndex, (String) value);
        }
    }

    public void addRow() {
        // Create new row
        column0.add(""); // NOI18N
        if (colCount > 1) {
            column1.add(""); // NOI18N
        }
        rowCount++;
        // XXX cha
        fireTableRowsInserted(rowCount, rowCount);
    //fireTableStructureChanged(); // XXX just rows inserted!
    }

    public void removeRows(int[] selectedRows) {
        // Go in reverse array order since we don't want the deletion
        // indices to shift underneath us...
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            column0.remove(selectedRows[i]);
            if (colCount > 1) {
                column1.remove(selectedRows[i]);
            }
            rowCount--;
            fireTableRowsDeleted(rowCount, rowCount);
        }
    //fireTableStructureChanged(); // XXX just rows inserted!
    }

    public void setData(int rows, ArrayList<String> col0, ArrayList<String> col1) {
        rowCount = rows;
        column0 = col0;
        column1 = col1;
        fireTableRowsInserted(rowCount, rowCount);
    }

    /** Technically, the model object shouldn't know anything about
    the table (other than indirectly through updatge listening).
    This is however done for code convenience -- see finishEditing
    function below. */
    public void setTable(JTable newTable) {
        table = newTable;
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE); // NOI18N
    }
}
