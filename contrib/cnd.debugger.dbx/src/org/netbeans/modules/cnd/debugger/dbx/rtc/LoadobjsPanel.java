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

package org.netbeans.modules.cnd.debugger.dbx.rtc;

import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

import org.openide.util.HelpCtx;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import org.openide.explorer.propertysheet.PropertyEnv;

import org.netbeans.modules.cnd.debugger.common2.utils.ListTableModel;
import org.netbeans.modules.cnd.debugger.common2.utils.ListUI;

class LoadobjsPanel extends JPanel implements PropertyChangeListener, HelpCtx.Provider {
    private Loadobjs loadobj;
    private ListUI loadobjList;
    private JTextField libsTextField;
    private final PropertyEditorSupport editor;
    private final boolean accessEnabled;

    /** Creates new form CustomizerCompile */
    public LoadobjsPanel(PropertyEditorSupport editor, PropertyEnv env, boolean access_enabled) {
        this.editor = editor;
	accessEnabled = access_enabled;
        initComponents();                
	if (accessEnabled)
	    env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
	else
	    env.setState(PropertyEnv.STATE_INVALID);
        env.addPropertyChangeListener(this);
    }

    /**
     * was: part of RunConfigDialog.configToGui()
     */

    // interface CustomizerPanel
    public void initValues(Loadobjs loadobj) {
	this.loadobj = loadobj;

	if (loadobj.getLoadobjs() != null) {
	    int n = loadobj.getLoadobjs().length;
	    ArrayList<String> col0 = new ArrayList<String>(n+3); // Leave slop for inserts
	    ArrayList<String> col1 = new ArrayList<String>(n+3);
	    for (int i = 0; i < n; i++) {
		col0.add(loadobj.getLoadobjs()[i].lo);
		col1.add(String.valueOf(loadobj.getLoadobjs()[i].skip));
	    }
	    loadobjList.model.setData(n, col0, col1);
	}
    } 


    /**
     * Transfer information from GUI to profile object.
     * Two parts : List, textfield
     */
    private Object getPropertyValue() throws IllegalStateException {
	loadobjList.model.finishEditing();

	// loadobjs that added from "Other Loadobjs" textfield was merged into loadobjList
	int numRows = loadobjList.model.getRowCount();

	// List part
	if (numRows > 0) {
	    Loadobj[] lo_list = new Loadobj [numRows];
	    int index = 0;

	    for (int j = 0; j < numRows; j++) {
		String lo = (String) loadobjList.model.getValueAt(j, 0);

		/* should have no empty row
		if (lo.length() == 0)
		    continue;
		*/
		boolean skip = ((Boolean) loadobjList.model.getValueAt(j, 1)).booleanValue();
		Loadobj p = new Loadobj ();
		p.lo = lo;
		p.skip = skip;
		lo_list[index++] = p;
	    }
	    loadobj.setLoadobjs(lo_list);
	}

	// textfield part
	String other_libs = libsTextField.getText();
	if (other_libs.equals(""))
	    other_libs = null;

	String[] libs_array = null;
	int libs_array_length;
	if (other_libs != null) {
	    libs_array = other_libs.split(" "); // NOI18N
	    libs_array_length = libs_array.length;
	} else
	    libs_array_length = 0;

	Loadobj[] lo_list = new Loadobj [libs_array_length];
	int index = 0;
	for (int j = 0; j < libs_array_length; j++) {
	    String lo = libs_array[j];

	    if (lo.length() == 0)
		continue;
	    Loadobj p = new Loadobj ();
	    p.lo = lo;
	    p.skip = true;
	    lo_list[index++] = p;
	}
	loadobj.mergeLoadobjs(lo_list);
	return loadobj;
    }
    
    /**
     * Transfer information from GUI to profile object.
    private Object getPropertyValue() throws IllegalStateException {
	loadobjList.model.finishEditing();

	String other_libs = libsTextField.getText();
	if (other_libs.equals(""))
	    other_libs = null;
	int numRows = loadobjList.model.getRowCount();
	int num = 0;
	int libs_array_length = 0;

	if (numRows > 0 || other_libs != null) {
	    if (numRows > 0) {
		for (int j = 0; j < numRows; j++) {
		    String lo = (String) loadobjList.model.getValueAt(j, 0);
		    if (lo.length() == 0)
			continue;
		    num++;
		}
	    }

	    String[] libs_array = null;
	    if (other_libs != null) {
		libs_array = other_libs.split(" ");
		libs_array_length = libs_array.length;
	    } else
		libs_array_length = 0;

	    Loadobj[] lo_list = new Loadobj [num + libs_array_length];
	    int index = 0;

	    for (int j = 0; j < num; j++) {
		String lo = (String) loadobjList.model.getValueAt(j, 0);
		if (lo.length() == 0)
		    continue;
		boolean skip = ((Boolean) loadobjList.model.getValueAt(j, 1)).booleanValue();
		Loadobj p = new Loadobj ();
		p.lo = lo;
		p.skip = skip;
		lo_list[index++] = p;
	    }

	    for (int j = 0; j < libs_array_length; j++) {
		String lo = libs_array[j];
		if (lo.length() == 0)
		    continue;
		Loadobj p = new Loadobj ();
		p.lo = lo;
		p.skip = true;
		lo_list[index++] = p;
	    }
	    loadobj.mergeLoadobjs(lo_list);
	    //loadobj.setLoadobjs(lo_list);
	} else {
	    loadobj.setLoadobjs(null);
	}
	return loadobj;
    }
     */
		
    public void propertyChange(PropertyChangeEvent evt) {
	if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && 
			evt.getNewValue() == PropertyEnv.STATE_VALID) {
	    editor.setValue(getPropertyValue());
	}
    }

    public HelpCtx getHelpCtx() {
	return new HelpCtx("RTCSettings");
    }
	

    private void initComponents() {

	GridBagConstraints gbc;
	setLayout(new GridBagLayout());

	Insets insets = new Insets(12, 12, 12, 12);
	Border margin = new EmptyBorder(insets);
	Border border = new CompoundBorder(new EtchedBorder(), margin);
	setBorder(border);

	loadobjList = new ListUI();
	loadobjList.labelText =
	    Catalog.get("LABEL_Loadobjs");		// NOI18N
	loadobjList.accessibleDescription =
	    Catalog.get("ACSD_Loadobjs");		// NOI18N
	loadobjList.labelMnemonic =
	    Catalog.getMnemonic("MNEM_Loadobjs");	// NOI18N
	loadobjList.column0Text =
		Catalog.get("Column_Load_Obj");		// NOI18N
	loadobjList.column1Text =
	    Catalog.get("Column_Skip_Patch");		// NOI18N
	loadobjList.accessibleDescription =
	    Catalog.get("ACSD_LoadobjsTable");		// NOI18N

	JPanel loadobjPanel = loadobjList.make(false);

	JTable loadobjTable = loadobjList.table;
	loadobjTable.setAutoCreateColumnsFromModel(false);

	TableModel tableModel =  loadobjTable.getModel();
	((ListTableModel)tableModel).setColumnClass(Boolean.class, 1); // customize column 1 to checkbox

	int gridy = 0;

	if (!accessEnabled) {
	    gbc = new GridBagConstraints();
	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.gridx = 0;
	    gbc.gridy = gridy++;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    gbc.insets = new Insets(11, 0, 0, 0);
	    JLabel errorLabel = new JLabel();
	    errorLabel.setForeground(javax.swing.UIManager.getColor("nb.errorForeground")); // NOI18N
	    errorLabel.setText(Catalog.get("RTC_ACCESS_CHECKING_FIRST"));
	    this.add(errorLabel, gbc);
	}

	gbc = new GridBagConstraints();
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = gridy++;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = new Insets(11, 0, 0, 0);
	this.add(loadobjPanel, gbc);

	JLabel libsLabel = new JLabel();
	libsLabel.setText(Catalog.get("OTHER_LIBS"));	// NOI18N
	libsLabel.setDisplayedMnemonic(Catalog.
	    getMnemonic("MNEM_OtherLoadobjs"));		// NOI18N

	gbc = new GridBagConstraints();
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = gridy++;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = new Insets(11, 0, 0, 0);
	this.add(libsLabel, gbc);

	libsTextField = new JTextField();
	Catalog.setAccessibleDescription(libsTextField,
	    "ACSD_OtherLoadobjs");			// NOI18N
	libsTextField.setText(null);
	if (accessEnabled) 
	    libsTextField.setEditable(true);
	else
	    libsTextField.setEditable(false);
	libsLabel.setLabelFor(libsTextField);

	gbc = new GridBagConstraints();
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.gridx = 0;
	gbc.gridy = gridy++;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = new Insets(11, 0, 0, 0);
	this.add(libsTextField, gbc);
    }

    private void adjustButtons() {
	loadobjList.adjustButtons(false);
    }

    private void deleteLoadobjsRow(ActionEvent evt) {
	int[] selRows = loadobjList.table.getSelectedRows();
	if ((selRows != null) && (selRows.length > 0)) {
	    loadobjList.model.removeRows(selRows);
	    adjustButtons();
	}
    }

    private void addLoadobjsRow(ActionEvent evt) {
	loadobjList.model.addRow();
	adjustButtons();
    }
}
