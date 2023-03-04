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

/**
 * DisplayTable.java
 *
 *
 * Created: Mon Jan 29 16:43:09 2001
 *
 * @author Ana von Klopp
 * @version
 */


package org.netbeans.modules.web.monitor.client;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;     
import javax.swing.table.TableModel;     
import javax.swing.event.TableModelEvent; 
import javax.swing.event.TableModelListener;  
import java.awt.Color;
import java.awt.Dimension;
import java.awt.SystemColor;

import org.netbeans.modules.web.monitor.data.Param;
import org.openide.util.NbBundle;

public class DisplayTable extends JTable {

    private static final boolean debug = false;
     
    // Type of data displayed
    public static final int UNEDITABLE = 0;
    public static final int REQUEST = 1;
    public static final int SERVER = 2;
    public static final int HEADERS = 3;
    public static final int PARAMS = 4;
    public static final int COOKIES = 5;

    // Sorting states
    public static final int NEUTRAL = 0;
    public static final int A2Z = 1;
    public static final int Z2A = 2;

    private int numRows = 0;
    private int numCols = 3;

    private Object[][] data = null;

    private TableCellEditor[][] cellEditors = null;

    // Can we edit the fields?
    private boolean editableNames = false;
    private int editable = UNEDITABLE;

    // Do we sort? 
    private int sort = NEUTRAL;
    private boolean sortable = false; 

    // Handle resizing for larger fonts
    boolean fontChanged = true;
    
    public DisplayTable(String[] categories) {
	this(categories, null, UNEDITABLE, false);
    }

    public DisplayTable(String[] categories, boolean sortable) {
	this(categories, null, UNEDITABLE, sortable);
    }

    public DisplayTable(String[] categories, int editable) {
	this(categories, null, editable, false);
    }

    public DisplayTable(String[] categories, int editable, boolean sortable) {
	this(categories, null, editable, sortable);
    }

    public DisplayTable(String[] names, String[] values) {
	this(names, values, UNEDITABLE, false);
    }

    public DisplayTable(String[] names, String[] values, boolean sortable) {
	this(names, values, UNEDITABLE, sortable);
    }

    public DisplayTable(String[] names, String[] values, int editable) {
	this(names, values, editable, false); 
    } 

    public DisplayTable(String[] names, String[] values, int editable, 
			boolean sortable) {
	
	super();
	numRows = names.length;
	editableNames = false;
	this.editable = editable;
	this.sortable = sortable;
	
	data = new Object[numRows][numCols];
	cellEditors = new TableCellEditor[numRows][numCols];
	for(int i=0; i<numRows; ++i) {
	    data[i][0] = names[i];
	    if (values == null) {
		data[i][1] = new String(""); // NOI18N
	    } else {
		data[i][1] = values[i];
	    }
	    data[i][2] = NbBundle.getBundle(DisplayTable.class).getString("MON_Edit_dots"); // NOI18N
	    cellEditors[i][2] =
		NameValueCellEditor.createCellEditor((JTable)this, data,
						     false, i, editable);
	}
	setMyModel(data, editable > UNEDITABLE); 
	setup();
    }
    
    public DisplayTable(Param[] params) {
	this(params, UNEDITABLE, false);
    }

    public DisplayTable(Param[] params, boolean sortable) {
	this(params, UNEDITABLE, sortable);
    }
   
    
    public DisplayTable(Param[] params, int editable) {
	this(params, editable, false);
    }
    

    public DisplayTable(Param[] params, int editable, boolean sortable) {

	super();

	if(editable < 3) 
	    editableNames = false; 
	else
	    editableNames = true; 

	this.editable = editable; 
	this.sortable = sortable;

	numRows = params.length;
	data = new Object[numRows][numCols];
	cellEditors = new TableCellEditor[numRows][numCols];
	for(int i=0; i<numRows; ++i) {
	    data[i][0] = params[i].getAttributeValue("name");   // NOI18N
	    data[i][1] = params[i].getAttributeValue("value");  // NOI18N
	    data[i][2] = NbBundle.getBundle(DisplayTable.class).getString("MON_Edit_dots"); // NOI18N
	    cellEditors[i][2] =
		NameValueCellEditor.createCellEditor((JTable)this, data,
						     true, i, editable);
	}
	setMyModel(data, editable > UNEDITABLE); 
	setup();
    }

    private void setup() {
	setBorderAndColorScheme();
	Dimension margins = new Dimension(6, 4);
	setIntercellSpacing(margins);
	sort();
    }

    /**
     * Set the border and colors for the table.
     * Depends on whether the table is ediable or not.
     */
    private void setBorderAndColorScheme() {
	setBorderAndColorScheme(editable != UNEDITABLE); 
    }

    private void setBorderAndColorScheme(boolean editable) {
	Color bg;
	this.setBorder(BorderFactory.createLoweredBevelBorder());
	if (!editable) { 
	    //bg = SystemColor.control;
	    bg = this.getBackground().darker();
	} else {
	    bg = Color.white;
	}
	this.setTableHeader(null);
	this.setBackground(bg);
    }
    
    /**
     * Creates a combobox for a cell editor. 
     *
     * @return the combobox that is used as the editor.
     */
    public JComboBox setChoices(int row, int col, String[] choices,
				boolean editable) { 
        TableCellEditor ed = new ComboBoxTableCellEditor(choices);                          
	cellEditors[row][col] = ed;

	// if the table is editable, we should turn off the [...] editor
	// when there's a choice on the row.
	data[row][2]=NbBundle.getBundle(DisplayTable.class).getString("MON_Editing");  
	cellEditors[row][2] = null;
        
        return ((ComboBoxTableCellEditor)ed).getComboBox();
    }

    /**
     * Override the getter for the cell editors, so that customized
     * cell editors will show up.
     */
    public TableCellEditor getCellEditor(int row, int col) {
	TableCellEditor ed = cellEditors[row][col];
	if (ed == null) {
	    return super.getCellEditor(row, col);
	}
	return ed;
    }

    public void setSorting(int state) {
	sort = state; 
	if(getModel() instanceof DisplayTableSorter)
	    ((DisplayTableSorter)getModel()).sort(sort); 
    }

    public void sort() {
	if(getModel() instanceof DisplayTableSorter)
	    ((DisplayTableSorter)getModel()).sort(sort); 
    }

    private void setMyModel(Object[][] data, boolean canEdit) {
	 
	DisplayTableModel model = new DisplayTableModel(data, 
							canEdit, 
							editable > 2); 
	if(sortable) {
	    DisplayTableSorter sorter = new DisplayTableSorter(model); 
	    setModel(sorter);
	}
	else {
	    setModel(model);
	}

	// PENDING - the column size does not shrink the way it should 
	TableColumnModel tcm = getColumnModel();
	if (tcm.getColumnCount() > 0) {
	    TableColumn column = tcm.getColumn(0);     
	    column.setPreferredWidth(10);
	    tcm.getColumn(2).setMaxWidth(5);
	}
    }

    public void addTableModelListener(TableModelListener tml) {
	TableModel tableModel = getModel();
	if (tableModel != null) {
	    tableModel.addTableModelListener(tml);
	}
    }

    public void removeTableModelListener(TableModelListener tml) {
	TableModel tableModel = getModel();
	if (tableModel != null) {
	    tableModel.removeTableModelListener(tml);
	}
    }

    public Object[][] getData() {
	return data;
    }

    public void setFont(Font f) {
	fontChanged = true;
	super.setFont(f);
    }

    /** 
     * When paint is first invoked, we set the rowheight based on the
     * size of the font. */
    public void paint(Graphics g) {
	if (fontChanged) {
	    Font f = getFont();
	    FontMetrics fm = g.getFontMetrics(f);
	    int rowHeight = fm.getHeight(); 
	    fontChanged = false;
	    //triggers paint, just return afterwards
	    this.setRowHeight(rowHeight);
	    return;
	}
	super.paint(g);
    }
    
    private void log(String s) {
	System.out.println("DisplayTable::" + s);  //NOI18N
    }

} // DisplayTable
