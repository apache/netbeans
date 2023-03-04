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

package org.netbeans.modules.web.monitor.client;

import java.util.*;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.openide.util.NbBundle;

/**
 * DisplayTableSorter.java
 *
 *
 * Created: Fri Jan 25 13:37:39 2002
 *
 * @author Ana von Klopp
 * @version
 */
public class DisplayTableSorter extends AbstractTableModel implements
    TableModelListener {

    private int[] index = null;
    private int sort = DisplayTable.NEUTRAL; 
    protected TableModel model; 

    private static final boolean debug = false;
    
    public DisplayTableSorter(TableModel model) {
	setModel(model);
	resetIndices();
    }

    // PENDING: we could speed this up considerably given that we can
    // just reverse the order of the indices if we know that the table
    // hasn't changed, for example...
    public void sort(int sort) {
	
        this.sort = sort; 
	if(debug) log(" sort: " + String.valueOf(this.sort)); //NOI18N
	if(sort == DisplayTable.NEUTRAL) { 
	    tableChanged(new TableModelEvent(this)); 
	    return; 
	}

	if(debug) {
	    StringBuffer buf = 
		new StringBuffer("Order of indices before sorting: ");//NOI18N
	    for(int i=0; i<index.length; ++i) {
		buf.append(String.valueOf(index[i]));
		buf.append(", "); //NOI18N
	    }
	    log(buf.toString());
	}

	resetIndices();
	sort(index.clone(), index, 0, index.length);
	 
	if(debug) {
	    StringBuffer buf = 
		new StringBuffer("Order of indices after sorting: ");//NOI18N
	    for(int i=0; i<index.length; ++i) {
		buf.append(String.valueOf(index[i]));
		buf.append(", ");  //NOI18N
	    }
	    log(buf.toString());
	}

	tableChanged(new TableModelEvent(this)); 
    }

    public void sort(int[] from, int[] to, int low, int high) {

	if (high - low < 2) {
	    return;
	}
        
	int middle = (low + high)/2;
	sort(to, from, low, middle);
	sort(to, from, middle, high);

	int p = low;
	int q = middle;

	if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
	    for (int i = low; i < high; i++) {
		to[i] = from[i];
	    }
	    return;
        }

        for (int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            }
            else {
                to[i] = from[q++];
            }
        }
    }

    public int compare(int row1, int row2) {

	String str1 = (String)model.getValueAt(row1, 0);
	String str2 = (String)model.getValueAt(row2, 0);

     
	if(debug) log(" comparing " + row1 + ":" + str1 + //NOI18N
		      " and " + row2 + ":" + str2); //NOI18N

	int result = str1.compareTo(str2); 
	if(result == 0) return 0;
	boolean ascending = (sort == DisplayTable.A2Z);
	return ascending ? result : -result; 
    }
    
    

    public void resetIndices() {
        int rowCount = getRowCount();
	
	if(debug) log("rows=" + //NOI18N
		      String.valueOf(rowCount));
	
        // Set up a new array of indexes with the right number of elements
        // for the new data model.
        index = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++) {
            index[row] = row;
        }
    }


    public TableModel getModel() {
        return model;
    }

    public void setModel(TableModel model) {
        this.model = model; 
        model.addTableModelListener(this); 
    }

    // By default, implement TableModel by forwarding all messages 
    // to the model. 

    public Object getValueAt(int aRow, int aColumn) {
	if(sort == DisplayTable.NEUTRAL) return model.getValueAt(aRow,
							       aColumn);
	
	if(debug) {
	    String value = (String)(model.getValueAt(index[aRow], aColumn)); 
	    value = value + String.valueOf(aRow) + "+" + index[aRow]; //NOI18N
	    return value;
	}
        return model.getValueAt(index[aRow], aColumn); 
    }
        
    public void setValueAt(Object aValue, int aRow, int aColumn) {
	if(sort == DisplayTable.NEUTRAL) { 
	    model.setValueAt(aValue, aRow, aColumn); 
	    return; 
	}
        model.setValueAt(aValue, index[aRow], aColumn); 
    }

    public int getRowCount() {
        return (model == null) ? 0 : model.getRowCount(); 
    }

    public int getColumnCount() {
        return (model == null) ? 0 : model.getColumnCount(); 
    }
        
    public String getColumnName(int aColumn) {
        return model.getColumnName(aColumn); 
    }

    public Class getColumnClass(int aColumn) {
        return model.getColumnClass(aColumn); 
    }
        
    public boolean isCellEditable(int row, int column) { 
         return model.isCellEditable(row, column); 
    }

    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }

    private void log(String s) {
	System.out.println("DisplayTableSorter::" + s); //NOI18N
    }
    
} // DisplayTableSorter
