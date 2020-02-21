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

package org.netbeans.modules.cnd.debugger.common2.utils;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


import org.netbeans.modules.cnd.debugger.common2.utils.ListTableModel;

public class ListUI {
    public JTable table;
    public String labelText;
    public char labelMnemonic;

    public JLabel label;
    public ListTableModel model;
    public String column0Text;
    public String column1Text;
    public String accessibleDescription;

    JButton b_add = new JButton();
    public String addText;
    public char addMnemonic =
	Catalog.getMnemonic("MNEM_AddItem");	// NOI18N
    public ActionListener addActionListener;

    JButton b_rem = new JButton();
    public String remText;
    public char remMnemonic =
	Catalog.getMnemonic("MNEM_RemoveItem");	// NOI18N
    public ActionListener remActionListener;

    public void adjustButtons(boolean all) {
	int[] selRows;

	b_add.setEnabled(!all);
	label.setEnabled(!all);

	if (!all && table != null) {
	    selRows = table.getSelectedRows();
	    if (model.getRowCount() <= 0)
		selRows = null;
	    if (selRows != null && selRows.length > 0) {
		b_rem.setEnabled(true);
	    } else {
		b_rem.setEnabled(false);
	    }
	} else {
	    b_rem.setEnabled(false);
	}

	table.setEnabled(!all);
    }

    public JPanel make(boolean withButtons) {
        GridBagConstraints gbc;

	//
	// Exceptions to Intercept
	// 
	JPanel panel = new JPanel();
	panel.setLayout(new java.awt.GridBagLayout());

	    label = new JLabel();
	    label.setText(labelText);
	    label.setDisplayedMnemonic(labelMnemonic);
	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.gridwidth = GridBagConstraints.REMAINDER;
	    gbc.anchor = GridBagConstraints.WEST;
	    panel.add(label, gbc);


	    JScrollPane scrollPane;
	    scrollPane = new JScrollPane();
		model = new ListTableModel(column0Text, column1Text);
		table = makeJTable(model);
		// LATER table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// LATER table.setAutoCreateColumnsFromModel(false);
		if (accessibleDescription != null) {
		    table.getAccessibleContext().
			setAccessibleDescription(accessibleDescription);
		}
		Catalog.setAccessibleName(table,
		    "ACSN_ItemTable");		// NOI18N
		    
		model.setTable(table);
		scrollPane.setViewportView(table);

	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    gbc.fill = GridBagConstraints.BOTH;
	    gbc.weightx = 1.0;
	    gbc.weighty = 1.0;
	    gbc.insets = new Insets(4, 0, 0, 0);
	    panel.add(scrollPane, gbc);

	    label.setLabelFor(table);


	    //
	    //  "add"/"remove" buttons
	    // 
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.setLayout(new java.awt.GridBagLayout());
		
		b_add.setText(addText);
		b_add.addActionListener(addActionListener);
		Catalog.setAccessibleDescription(b_add,
		    "ACSD_AddItem");			// NOI18N
		b_add.setMnemonic(addMnemonic);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		if (withButtons)
		    buttonPanel.add(b_add, gbc);


		b_rem.setText(remText);
		b_rem.addActionListener(remActionListener);
		Catalog.setAccessibleDescription(b_rem,
		    "ACSD_RemoveItem");			// NOI18N
		b_rem.setMnemonic(remMnemonic);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.insets = new Insets(4, 0, 0, 0);
		if (withButtons)
		    buttonPanel.add(b_rem, gbc);

	    gbc = new GridBagConstraints();
	    gbc.gridx = 1;
	    gbc.gridy = 1;
	    gbc.anchor = GridBagConstraints.NORTHWEST;
	    // gbc.insets = new Insets(5, 12, 11, 11);
	    gbc.insets = new Insets(4, 4, 0, 0);
	    panel.add(buttonPanel, gbc);
	

	return panel;
    }

    private JTable makeJTable(TableModel model) {
	JTable table = new JTable(model);

	table.getSelectionModel().
	    addListSelectionListener( new ListSelectionListener() {
                @Override
		public void valueChanged(ListSelectionEvent e) {
		    // DEBUG System.out.println("JTable Selection changed");
		    adjustButtons(false);
		}
	    });

	if (column1Text == null) {
	    // single column, skip the header
	    table.setTableHeader(null);
	}
	return table;
    }
}
