/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        modelIndex.sort(comp);
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
