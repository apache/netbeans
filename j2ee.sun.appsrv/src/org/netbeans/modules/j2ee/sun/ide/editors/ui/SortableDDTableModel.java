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
 * SortableDDTableModel.java -- synopsis.
 *
 */
package org.netbeans.modules.j2ee.sun.ide.editors.ui;

import java.util.*;

import javax.swing.table.*;

/**
 * Table model using the composite pattern allowing DDTableModels to be sorted.
 * The model allows any column containing a java.util.Comparable type to be
 * sorted. The sorting model is index based and therefore only sorts the view
 * into the underlying model and not the model itself.
 *
 * @author Chris Webster
 */
//
// 29-may-2001
//	Change for bug 4457984 - pass the new methods of DDTableModel onto
//      the delegate and added parameter to addRowAt. (joecorto)
//
public class SortableDDTableModel extends AbstractTableModel 
implements DDTableModel {
    private DDTableModel modelDelegate;
    private boolean needsSorting;
    private List modelIndex;
    private Comparator comp;
    private int sortColumn;

    public SortableDDTableModel(DDTableModel model) {
        modelDelegate = model;
	modelIndex = new ArrayList(getRowCount());
	for (int i =0; i < getRowCount(); i++) {
	    modelIndex.add(new DelegateReference(i));
	}

	comp = new Comparator() {
	    public boolean equals(Object other) {
	        return this == other;	
	    }

	    public int compare(Object o1, Object o2) {
                if (!(o1 instanceof DelegateReference) ||
		    !(o2 instanceof DelegateReference))
                {
		    throw new ClassCastException();
		}
		DelegateReference d1 = (DelegateReference) o1;
		DelegateReference d2 = (DelegateReference) o2;
		Comparable compo1 = 
                    (Comparable) modelDelegate.getValueAt(d1.ref,
                                                          getSortColumn());
	        Object comp2 = modelDelegate.getValueAt(d2.ref, 
							  getSortColumn());

		  
		if (compo1 == null && comp2 == null) {
		    return 0;
		}
		      
		if (compo1 == null) {
		    return -1;	  
		}

		if (comp2 == null) {
		    return 1;
		}
			    
		return compo1.compareTo(comp2);
            }
        };
	setSortColumn(-1);
    }

    public int getSortColumn() {
        return sortColumn;
    }

    public boolean isSortable() {
	return Comparable.class.isAssignableFrom(
	     getColumnClass(getSortColumn()));
    }

    private static class DelegateReference {
        public DelegateReference(int ref) {
	    this.ref = ref;
	}
	public int ref;
    }

    public void setSortColumn(int col) {
        if (col < 0 || col >= getColumnCount()) {
	    sortColumn = col;
	    return;
        }

        if (sortColumn != col || needsSorting) {
	    sortColumn = col;
	    sort(); 
	    needsSorting = false;
	}
    }

    private void sort() {
        Collections.sort(modelIndex,comp);
        fireTableDataChanged();
    } 

    private int getInd(int row) {
        return ((DelegateReference) modelIndex.get(row)).ref;
    }

    public int getColumnCount() {
        return modelDelegate.getColumnCount();
    }

    public String getColumnName(int col) {
	return modelDelegate.getColumnName(col);
    }

    public DDTableModelEditor getEditor() {
        return modelDelegate.getEditor();
    }
    
    public boolean isEditValid (Object value, int row) {
	return modelDelegate.isEditValid (value, row);
    }

    public List canRemoveRow (int row) {
	return modelDelegate.canRemoveRow (row);
    }

    public List isValueValid(Object value, int fromRow) {
        return modelDelegate.isValueValid(value, 
					  fromRow==-1?-1:getInd(fromRow));
    }
    
    public Object getValueAt(int row, int col) {
	return modelDelegate.getValueAt(getInd(row), col);
    }

    public String getModelName() {
	return modelDelegate.getModelName();
    }
        
    public Object [] getValue () {
	Object[] rv = modelDelegate.getValue();
	/* XXXX Ask Chris about this.
	for (int i = 0; i < rv.length; i++) {
	    rv[i] = getValueAt(i);
	}*/
	return rv;
    }
   
    public int getRowCount () {
        return modelDelegate.getRowCount();
    }

    public Class getColumnClass (int col) {
	return modelDelegate.getColumnClass(col);
    }

    public boolean isCellEditable(int row, int col) {
	return modelDelegate.isCellEditable(getInd(row),col);
    }

    public Object getValueAt (int row) {
	return modelDelegate.getValueAt(getInd(row));    
    }

    public void setValueAt (int row, Object value) {
        modelDelegate.setValueAt(getInd(row), value);
	needsSorting = true;
	setSortColumn(getSortColumn());
    }

    public void setValueAt (Object value, int row, int col) {
	modelDelegate.setValueAt(value, getInd(row), col);
	needsSorting = true;
	setSortColumn(getSortColumn());
    }

    public Object makeNewElement() {
	return modelDelegate.makeNewElement();
    }

    public void newElementCancelled(Object obj) {
	modelDelegate.newElementCancelled(obj);
    }

    public void editsCancelled() {
	modelDelegate.editsCancelled();
    }
       
    public void addRowAt(int row, Object newVal, Object editVal) {
        if (row == -1) {
	    row = getRowCount();
	} else {
	    row++;
	}

	modelIndex.add(row, new DelegateReference(getRowCount()));
	modelDelegate.addRowAt(-1, newVal, editVal);
	fireTableRowsInserted(row,row);
	needsSorting = true;
	setSortColumn(getSortColumn());
    }

    public void removeRowAt(int row) {
	int delegateRow = getInd(row);
	modelDelegate.removeRowAt(delegateRow);
	modelIndex.remove(row);
	Iterator it = modelIndex.iterator();
	while (it.hasNext()) {
	    DelegateReference del = (DelegateReference) it.next();
		if (del.ref >= delegateRow) {
		    del.ref--;
		}
	    fireTableRowsDeleted(row, row);
        }
    }
}
