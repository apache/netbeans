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

package org.netbeans.modules.cnd.debugger.common2.debugger.options;

import java.util.ArrayList;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.HelpCtx;

import org.openide.explorer.propertysheet.PropertyEnv;

import org.netbeans.modules.cnd.debugger.common2.utils.ListUI;

class PathmapPanel extends JPanel implements PropertyChangeListener, HelpCtx.Provider {
    private Pathmap pathmap;
    private ListUI pathmapList;
    private PropertyEditorSupport propertyEditor;

    /** Creates new form CustomizerCompile */
    public PathmapPanel(PropertyEditorSupport propertyEditor,
			PropertyEnv env,
			Pathmap pathmap) {
        this.propertyEditor = propertyEditor;
        initComponents();                

	adjustButtons();

	// Arrange for propertyChange to get called when OK is pressed.
        env.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        env.addPropertyChangeListener(this);

	this.pathmap = pathmap;

	initValues(pathmap);
    }
    
    /**
     * was: part of RunConfigDialog.configToGui()
     */

    private void initValues(Pathmap pathmap) {
	this.pathmap = pathmap;

	if (pathmap.getPathmap() != null) {
	    int n = pathmap.getPathmap().length;
	    ArrayList<String> col0 = new ArrayList<String>(n+3); // Leave slop for inserts
	    ArrayList<String> col1 = new ArrayList<String>(n+3);
	    for (int i = 0; i < n; i++) {
		col0.add(pathmap.getPathmap()[i].from());
		if (pathmap.getPathmap()[i].to() == null) {
		    col1.add(""); // NOI18N
		} else {
		    col1.add(pathmap.getPathmap()[i].to());
		}
	    }
	    pathmapList.model.setData(n, col0, col1);
	}
    } 


    /**
     * Transfer information from GUI to profile object.
     */
    private Object getPropertyValue() throws IllegalStateException {
	pathmapList.model.finishEditing();

	int numRows = pathmapList.model.getRowCount();
	if (numRows > 0) {
	    // Do real count (ignore empty rows)
	    int num = 0;
	    for (int j = 0; j < numRows; j++) {
		String from = (String) pathmapList.model.getValueAt(j, 0);
		if (from.length() == 0)
		    continue;
		String to = (String) pathmapList.model.getValueAt(j, 1);
		// Empty right hand side is legal
		num++;
	    }
	    
	    Pathmap.Item [] map = new Pathmap.Item[num];
	    num = 0;
	    for (int j = 0; j < numRows; j++) {
		String from = (String) pathmapList.model.getValueAt(j, 0);
		if (from.length() == 0)
		    continue;
		String to = (String) pathmapList.model.getValueAt(j, 1);
		if (to.length() == 0) {
		    to = null;
		}
		Pathmap.Item p = new Pathmap.Item(from, to, false);
		map[num++] = p;
	    }
	    pathmap.setPathmap(map);
	} else {
	    pathmap.setPathmap(null);
	}
	return pathmap;
    }
        
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) &&
	    evt.getNewValue() == PropertyEnv.STATE_VALID) {

	    // OK was pressed
            propertyEditor.setValue(getPropertyValue());
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("DebuggerOptions" );
    }
    

    private void initComponents() {
        GridBagConstraints gbc;
        setLayout(new GridBagLayout());

	Insets insets = new Insets(12, 12, 12, 12);
	Border margin = new EmptyBorder(insets);
	Border border = new CompoundBorder(new EtchedBorder(), margin);
	setBorder(border);

	pathmapList = new ListUI();
	pathmapList.labelText = Catalog.get("Pathmap");		// NOI18N
	pathmapList.labelMnemonic =
	    Catalog.getMnemonic("MNEM_Pathmap");		// NOI18N
	pathmapList.column0Text = Catalog.get("ExistingPath");	// NOI18N
	pathmapList.column1Text = Catalog.get("ReplacementPath");// NOI18N
	pathmapList.accessibleDescription =
	    Catalog.get("ACSD_PathmapTable");			// NOI18N
	pathmapList.addText = Catalog.get("AddMap");		// NOI18N
	pathmapList.addActionListener = new ActionListener() {
            @Override
	    public void actionPerformed(ActionEvent evt) {
		addPathmapRow(evt);
	    }
	};
	pathmapList.remText = Catalog.get("RemoveMap");		// NOI18N
	pathmapList.remActionListener = new ActionListener() {
            @Override
	    public void actionPerformed(ActionEvent evt) {
		deletePathmapRow(evt);
	    }
	};

	JPanel pathmapPanel = pathmapList.make(true);

	gbc = new GridBagConstraints();
	gbc.gridwidth = GridBagConstraints.REMAINDER;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.0;
	gbc.weighty = 1.0;
	gbc.insets = new Insets(11, 0, 0, 0);
	this.add(pathmapPanel, gbc);
    }

    private void adjustButtons() {
	pathmapList.adjustButtons(false);
    }

    private void deletePathmapRow(ActionEvent evt) {
	int[] selRows = pathmapList.table.getSelectedRows();
	if ((selRows != null) && (selRows.length > 0)) {
	    pathmapList.model.removeRows(selRows);
	    adjustButtons();
	}
    }

    private void addPathmapRow(ActionEvent evt) {
	pathmapList.model.addRow();
	adjustButtons();
    }
}
