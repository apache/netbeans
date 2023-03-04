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
 * EditPanelHeaders.java
 *
 *
 * Created: Fri Feb 9 2001
 *
 * @author Ana von Klopp
 * @author Simran Gleason
 * @version
 */

/**
 * Contains the Request sub-panel for the EditPanel
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.netbeans.modules.web.monitor.data.*;
import org.openide.awt.Mnemonics;

class EditPanelHeaders extends DataDisplay {

    private static final boolean debug = false;
    
    private DisplayTable headerTable = null;    
    private MonitorData monitorData = null;
    private boolean setParams = false;

    //
    // Widgets
    //
    JButton newHeaderB;
    JButton editHeaderB;
    JButton deleteHeaderB;
    
    EditPanelHeaders() {
	super();
    }

    // Replace this. Inefficient and prevents us from maintaining
    // sorting. 
    void redisplayData() {
	setData(monitorData);
	this.revalidate(); 
	this.repaint(); 
    }

    void setData(MonitorData md) {

	this.monitorData = md;
	setHeaderTable();
	if(debug) log("setData()"); // NOI18N
	 
	this.removeAll();
	
	int gridy = -1;
	int fullGridWidth = java.awt.GridBagConstraints.REMAINDER;


	addGridBagComponent(this, createTopSpacer(), 0, ++gridy,
			    fullGridWidth, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    topSpacerInsets,
			    0, 0);

	String msg = NbBundle.getBundle(EditPanelHeaders.class).getString("MON_HTTP_Headers_2"); 
	addGridBagComponent(this, createSortButtonLabel(msg, headerTable, NbBundle.getBundle(EditPanelHeaders.class).getString("ACS_MON_HTTP_HeadersA11yDesc")), 0, ++gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    labelInsets,
			    0, 0);

	JScrollPane scrollpane = new JScrollPane(headerTable);
	addGridBagComponent(this, scrollpane, 0, ++gridy,
			    fullGridWidth, 1, 1.0, 1.0, 
			    java.awt.GridBagConstraints.WEST,
			    //java.awt.GridBagConstraints.HORIZONTAL, 
			    java.awt.GridBagConstraints.BOTH,
			    tableInsets,
			    0, 0);

	newHeaderB = new JButton();
        Mnemonics.setLocalizedText(newHeaderB, NbBundle.getBundle(EditPanel.class).getString("MON_New_header"));
        newHeaderB.setToolTipText(NbBundle.getBundle(EditPanelHeaders.class).getString("ACS_MON_New_headerA11yDesc"));
	newHeaderB.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    ParamEditor pe = new ParamEditor("", "", //NOI18N
						     ParamEditor.Editable.BOTH, 
						     ParamEditor.Condition.HEADER,
						     newHeaderB.getText()); 

		    if(debug) log("Now showing dialog");// NOI18N
		    
		    pe.showDialog();

		    if(debug) log("Dialog closed"); // NOI18N

		    if (pe.getDialogOK()) {

			if(debug) log("Dialog returned OK"); // NOI18N
			
			String name = pe.getName(); 
			int status = 0; 

			if(name.equalsIgnoreCase("cookie"))  
			    status = monitorData.getRequestData().addCookie(pe.getValue()); 
			else 
			    status = monitorData.getRequestData().getHeaders().addParam(pe.getName(), pe.getValue());

			if(debug) 
			    log("Headers are " + // NOI18N
				monitorData.getRequestData().getHeaders().toString()); 
			// if(status == 1) { /
			//  The new value was added to an existing header
		        // }
			// if(status == -1) {
			//  The new value was not added because it was
			//  already in there.
		        //}
			    
			redisplayData();
		    }
		}});

	deleteHeaderB = new JButton();
        Mnemonics.setLocalizedText(deleteHeaderB, NbBundle.getBundle(EditPanel.class).getString("MON_Delete_header"));
        deleteHeaderB.setToolTipText(NbBundle.getBundle(EditPanelHeaders.class).getString("ACS_MON_Delete_headerA11yDesc"));

	deleteHeaderB.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {

		    int numRows = headerTable.getRowCount();

		    StringBuffer buf = new StringBuffer
			(NbBundle.getBundle(EditPanelHeaders.class).getString("MON_Confirm_Delete_Headers")); 
		    buf.append("\n"); // NOI18N

		    for(int i=0; i<numRows; ++i) {

			if(headerTable.isRowSelected(i)) {
			    buf.append(headerTable.getValueAt(i, 0));
			    buf.append(" ");  // NOI18N
			    buf.append(headerTable.getValueAt(i, 1));
			    buf.append("\n"); // NOI18N
			}
		    }

		    showConfirmDialog(buf.toString()); 
		    
		    if(setParams) {

			Headers hd = monitorData.getRequestData().getHeaders();

			for(int i=0; i<numRows; ++i) {
			    if(headerTable.isRowSelected(i)) {

				String name =
				    (String)headerTable.getValueAt(i, 0); 
				String value =
				    (String)headerTable.getValueAt(i, 1); 
				
				// Note that we get the params each
				// time through that we don't run into
				// null pointer exceptions. 
				Param[] myParams = hd.getParam();
				Param param = findParam(myParams, name, value);
				if (param != null) 
				    hd.removeParam(param);
			    }
			}
			redisplayData();
		    }
		}});
	
	int gridx = -1;
	addGridBagComponent(this, createGlue(), ++gridx, ++gridy,
			    1, 1, 1.0, 0, 
			    java.awt.GridBagConstraints.WEST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);
	addGridBagComponent(this, newHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	addGridBagComponent(this, deleteHeaderB, ++gridx, gridy,
			    1, 1, 0, 0, 
			    java.awt.GridBagConstraints.EAST,
			    java.awt.GridBagConstraints.NONE,
			    buttonInsets,
			    0, 0);

	setEnablings();

	this.setMaximumSize(this.getPreferredSize()); 
	this.repaint();
    }
    
    


    void showConfirmDialog(String msg) {

	Object[] options = { NotifyDescriptor.OK_OPTION, 
			   NotifyDescriptor.CANCEL_OPTION 
	};
	
	NotifyDescriptor confirmDialog = 
	    new NotifyDescriptor((Object)msg, 
				 NbBundle.getBundle(EditPanelHeaders.class).getString("MON_Confirmation_Required"),
				 NotifyDescriptor.OK_CANCEL_OPTION,
				 NotifyDescriptor.QUESTION_MESSAGE, 
				 options,
				 NotifyDescriptor.CANCEL_OPTION);

	DialogDisplayer.getDefault().notify(confirmDialog);
	if(confirmDialog.getValue().equals(NotifyDescriptor.OK_OPTION)) 
	    setParams = true;
	else 
	    setParams = false;
    }


    void showErrorDialog() {

	Object[] options = { NotifyDescriptor.OK_OPTION };
	
	NotifyDescriptor errorDialog = 
	    new NotifyDescriptor((Object)NbBundle.getBundle(EditPanelHeaders.class).getString("MON_Bad_header"),
				 NbBundle.getBundle(EditPanelHeaders.class).getString("MON_Invalid_input"),
				 NotifyDescriptor.DEFAULT_OPTION,
				 NotifyDescriptor.ERROR_MESSAGE, 
				 options,
				 NotifyDescriptor.OK_OPTION);

	DialogDisplayer.getDefault().notify(errorDialog);
    }

     
    void setEnablings() {

	// Always enable the Add button.
	newHeaderB.setEnabled(true);

	// The delete row button is enabled if any rows are selected.
	int selectedRows[] = headerTable.getSelectedRows();
	deleteHeaderB.setEnabled(selectedRows.length > 0);
    }

    void setHeaderTable() {

	Param[] params = monitorData.getRequestData().getHeaders().getParam();
	if(params == null) params = new Param[0];
	
	headerTable = 
	    new DisplayTable(params, DisplayTable.HEADERS, true);
	headerTable.getAccessibleContext().setAccessibleName(NbBundle.getBundle(EditPanelHeaders.class).getString("ACS_MON_HTTP_HeadersTableA11yName"));
        headerTable.setToolTipText(NbBundle.getBundle(EditPanelHeaders.class).getString("ACS_MON_HTTP_HeadersTableA11yDesc"));


	ListSelectionModel selma = headerTable.getSelectionModel();
	selma.addListSelectionListener(new ListSelectionListener() {
	    public void valueChanged(ListSelectionEvent evt) {
		if(debug) log("got list selection event"); // NOI18N
		setEnablings();
	    }
	});

	headerTable.addTableModelListener(new TableModelListener() {
	    public void tableChanged(TableModelEvent evt) {
		
		if(debug) 
		    log("got table changed event"); //NOI18N
		updateHeaders();
	    }
	});
    }
    
    private void updateHeaders() {
		
	int num = headerTable.getRowCount();
	Headers hd = monitorData.getRequestData().getHeaders();
	Param[] params = hd.getParam();
		    
	boolean inputOK = true;
		    
	for(int i=0; i < num; i++) {
	    String name = (String)headerTable.getValueAt(i, 0);
	    name = name.trim();

	    if(debug) 
		log("Name is " + name); //NOI18N
		       
	    if(name.equals("")) { // NOI18N
		headerTable.setValueAt(params[i].getName(), i, 0);
		inputOK = false;
	    }
	    String value = (String)headerTable.getValueAt(i, 1);
	    value = value.trim();
	    
	    if(debug)
		log("Value is " + value); //NOI18N
	    
	    if(value.equals("")) { // NOI18N
		headerTable.setValueAt(params[i].getValue(), i, 1);
		inputOK = false;
	    }
	    
	    if(!inputOK) {
		showErrorDialog();
		return;
	    }
	    params[i].setName(name);
	    params[i].setValue(value);
	}
    }

    public void repaint() {
	super.repaint();
	//if (editPanel != null) 
	//editPanel.repaint();
    }

    void log(String s) {
	System.out.println("EditPanelHeaders::" + s); //NOI18N
    }
    

} // EditPanelHeader

