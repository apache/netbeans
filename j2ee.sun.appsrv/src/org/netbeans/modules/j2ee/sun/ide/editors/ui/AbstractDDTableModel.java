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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
