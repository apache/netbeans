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


package org.netbeans.modules.cnd.debugger.common2.utils;

import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Model for editing lists in a table. Used for editing lists with
 * one or two columns where the values are strings.
 *
 * Design note: I could have made this slightly more general for use
 * with N columns instead of 1 and 2, but I don't want to do the
 * extra array creation
 */
public class ListTableModel extends AbstractTableModel {

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
	column0 = new ArrayList<String>(5);
	column1 = new ArrayList<String>(5);

	// Start out with one empty row
	column0.add("");
	column1.add("");
	rowCount = 1;
    }

    int rowCount = 0;

    private JTable table = null;
    private String header0 = null;
    private String header1 = null;
    int colCount = 0;

    private ArrayList<String> column0 = null;
    private ArrayList<String> column1 = null;
    private Class<?> column0Class = null;
    private Class<?> column1Class = null;

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

    public void setColumnClass(Class<?> columnClass, int columnIndex) {
	if (columnIndex == 0)
	    column0Class = columnClass;
	else
	    column1Class = columnClass;
	
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
	if (columnIndex == 0)
	    if (column0Class != null)
		return column0Class;
	    else
		return String.class; // default value
	else
	    if (column1Class != null)
		return column1Class;
	    else
		return String.class; // default value
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
	    if (column1Class == Boolean.class) {
		Boolean b = Boolean.valueOf(column1.get(rowIndex));
		return b == null ? false : b.booleanValue();
	    } else
		// Currently we have only String and Boolean
		// can be extended to other column type
		return column1.get(rowIndex);
	}
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
	//System.out.println("Setting value " + value + " at rowIndex " + rowIndex + " and columnIndex " + columnIndex + " where rowCount is " + rowCount);
	if (columnIndex == 0) {
	    column0.set(rowIndex, (String)value);
	} else {
	    if (column1Class == Boolean.class && (value instanceof Boolean)) {
		column1.set(rowIndex, ((Boolean)value).toString());
	    } else
		// Currently we have only String and Boolean
		// can be extended to other column type
		column1.set(rowIndex, (String)value);
	}
    }

    public void addRow() {
	// Create new row
	column0.add("");
	if (colCount > 1) {
	    column1.add("");
	}
	rowCount++;
	// XXX cha
	fireTableRowsInserted(rowCount, rowCount);
	//fireTableStructureChanged(); // XXX just rows inserted!
    }

    public void removeRows(int[] selectedRows) {
	// Go in reverse array order since we don't want the deletion
	// indices to shift underneath us...
	for(int i = selectedRows.length-1; i >= 0; i--) {
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
	column0 = (col0 == null)? new ArrayList<String>(5): col0;
	column1 = (col1 == null)? new ArrayList<String>(5): col1;
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
    
    public void finishEditing() {
	/* OLD

	This doesn't work.
	getCellEditor() returns 'null' if the editor is the default editor,
	which is the case here.
	Instead we set the client property 'terminateEditOnFocusLost' up
	above.

	if ((table != null) && (table.isEditing())) {
	    TableColumnModel columnModel = table.getColumnModel();
	    //System.out.println("Finishing editing in table with header0 is " + header0);
 	    for (int i = 0; i < colCount; i++) {
		TableColumn tc = columnModel.getColumn(i);
		if (tc == null) {
		    //System.out.println("table column i " + i + " is null");
		    continue;
		}
		TableCellEditor ce = tc.getCellEditor();
		if (ce != null) {
		    ce.stopCellEditing();
		//} else {
		//    System.out.println("The cell editor is null");
		}
	    }
	}
	*/
    }
}
