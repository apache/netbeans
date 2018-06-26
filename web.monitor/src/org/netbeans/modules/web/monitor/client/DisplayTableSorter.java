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
	sort((int[])index.clone(), index, 0, index.length);
	 
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
