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
/*
 * AbstractDDTableModel.java -- synopsis.
 */
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.util.*;

import javax.swing.table.*;


/**
 * Table model used for displaying Deployment
 * Descriptor entries that contain multiple key/value
 * pairs (ie. can be modeled as arrays).
 *
 * @author Joe Warzecha
 */
//
// 29-may-2001
//	Changes for bug 4457984. Changed the signature for addRowAt to
//	match the interface and added null implementations of the
// 	new methods newElementCancelled and editsCancelled. (joecorto)
//
public abstract class AbstractDDTableModel extends AbstractTableModel 
implements DDTableModel {

    protected Vector   data;

    public AbstractDDTableModel (Object [] refs) {
	data = new Vector ();
	changeRefs(refs);
    }

    //
    // This protected constructor is for the subclass
    // AbstractBaseBeanDDTableModel which initializes differently.
    //
    protected AbstractDDTableModel() {
    }

    protected void changeRefs(Object[] refs) {
        if (data == null) {
	    data = new Vector (refs.length);
	} else {
	    data.removeAllElements();
	}
	for (int i = 0; i < refs.length; i++) {
	    data.addElement (refs [i]);
	}
	fireTableDataChanged();
    }

    public int getRowCount () {
	return data.size ();
    }

    public Class getColumnClass (int col) {
	return String.class;
    }

    protected boolean valueInColumn(Object value, int col, int skipRow) {
        for (int i = data.size()-1; i >= 0; i--) {
	    if (i != skipRow && value.equals(getValueAt(i, col))) {
	        return true;
	    }
        }
	return false;
    }

    public Object getValueAt (int row) {
	if (row >= data.size ()) {
	    return null;
	}

	Object o = data.elementAt (row);
	return o;
    }

    protected abstract void setValueAt (String strVal, Object rowElement, 
					int col);

    public void setValueAt (Object value, int row, int col) {
        String strVal = (String) value;

        if (row >= data.size()) {
            return;
        }
        Object o = data.elementAt (row);
        if (o == null) {
            return;
        }

	setValueAt (strVal, o, col);
	fireTableCellUpdated (row, col);
    }

    public void setValueAt(int row, Object value) {
	data.setElementAt(value, row);
	fireTableRowsUpdated(row,row);
    }

    public void addRowAt (int row, Object newVal, Object editedVal) {
        /*
	 * A value of -1 means there is no selected row in
	 * the table, so add to the end in that case.
	 * Otherwise we want to add after the given row.
	 */
	if (row == -1) {
	    row = data.size();
	} else {
	    row++;
	}

	data.insertElementAt(editedVal, row);
	fireTableRowsInserted(row, row);
    }

    public void newElementCancelled(Object obj) {
	// Nothing to do in the generic implementation.
    }

    public void editsCancelled() {
	// Nothing to do in the generic implementation.
    }

    public boolean isEditValid (Object rowValue, int row) {
	return true;
    }

    public java.util.List canRemoveRow (int row) {
	return Collections.EMPTY_LIST;
    }

    public void removeRowAt(int row) {
	data.removeElementAt(row);
	fireTableRowsDeleted(row, row);
    }
}
