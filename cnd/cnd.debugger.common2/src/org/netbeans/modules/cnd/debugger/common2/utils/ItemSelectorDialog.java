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
import javax.swing.border.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.openide.*;

/**
 * Let's you select items from a dialog.
 *
 * This dialog is reusable. Simply call showWindow() with new parameters.
 */


// XXX todo - should implement cancellable in a better way
// cancelable seems to never be set to false by dbx, so will worry about
// this later.
// Just desensitizing Cancel won't do since the window can be closed by the
// WM.

public final class ItemSelectorDialog extends JPanel implements ActionListener {

    private int nitems = 0;		 // initial set of items
    private boolean multiple_selections;

    private JButton selectAllButton = null;
    private JButton clearButton = null;
    private JButton invertButton = null;
    private JList list = null;
    private JButton okButton;
    private JButton cancelButton;

    private boolean okPressed = false;

    // stuff to be passed back via ItemSelectorResult
    private int[] selected_indices = null;
    private boolean cancelled = false;


    public ItemSelectorDialog() {
	super();
    }

    public ItemSelectorResult getResult() {
	if (cancelled)
	    return ItemSelectorResult.cancelled();
	else
	    return ItemSelectorResult.select(selected_indices);
    }

    public void showWindow(String title, final int nitems, String item[],
			   final boolean cancelable,
			   final boolean multiple_selections) {

	this.nitems = nitems;
	this.multiple_selections = multiple_selections;

	// 6440360
	//setSize(600, 300);
	setPreferredSize(new Dimension(600, 300));
	this.setBorder(new EmptyBorder(10, 10, 0, 10));

	setLayout(new BorderLayout());

	list = new JList(item);
	list.setSelectionMode(multiple_selections ? 
			      ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
			      ListSelectionModel.SINGLE_SELECTION);
	Catalog.setAccessibleName(list,
					 "ACSN_OverloadList");	// NOI18N
	Catalog.setAccessibleDescription(list,
					 "ACSD_OverloadList");	// NOI18N
	list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {
                doubleClicked(evt);
            }
        });

	JScrollPane scroll_pane = new JScrollPane(list);
	add(scroll_pane, BorderLayout.CENTER);

	JPanel topPanel = new JPanel();
	topPanel.setLayout(new BorderLayout());

	    JLabel jl = new JLabel(title);
	    jl.setBorder(new EmptyBorder(5, 5, 5, 5));
	    topPanel.add(jl, BorderLayout.NORTH);

	    JPanel selControls = new JPanel();
	    selControls.setBorder(new EmptyBorder(0, 0, 5, 0));
	    topPanel.add(selControls, BorderLayout.SOUTH);

		selectAllButton = new JButton();
		selectAllButton.setText(Catalog.get("CTL_SelectAll"));// NOI18N
		selectAllButton.setMnemonic
		    (Catalog.getMnemonic("MNEM_SelectAll"));	// NOI18N
		Catalog.setAccessibleDescription
		    (selectAllButton, "ACSD_SelectAll");	// NOI18N
		selectAllButton.addActionListener(this);
		selControls.add(selectAllButton);

		
		clearButton = new JButton();
		clearButton.setText(Catalog.get("CTL_Clear"));	// NOI18N
		clearButton.setMnemonic
		    (Catalog.getMnemonic("MNEM_Clear"));	// NOI18N
		Catalog.setAccessibleDescription
		    (clearButton, "ACSD_Clear");		// NOI18N
		clearButton.addActionListener(this);
		selControls.add(clearButton);

		
		invertButton = new JButton();
		invertButton.setText(Catalog.get("CTL_Invert"));// NOI18N
		invertButton.setMnemonic
		    (Catalog.getMnemonic("MNEM_Invert"));	// NOI18N
		Catalog.setAccessibleDescription
		    (invertButton, "ACSD_Invert");		// NOI18N
		invertButton.addActionListener(this);
		selControls.add(invertButton);


	add(topPanel, BorderLayout.NORTH);

	
	/* XXX
	if (!cancelable)
	    b_cancel.setEnabled(false);
	*/


	DialogDescriptor dlg = new DialogDescriptor(
		this,
		Catalog.get("TTL_OverloadDialog"), // NOI18N
		true,	// isModal
		this);

	okButton = new JButton(Catalog.get("CTL_OK"));		 // NOI18N
	Catalog.setAccessibleDescription(okButton, "ACSD_OK");	 // NOI18N

	cancelButton = new JButton(Catalog.get("CTL_Cancel"));	 // NOI18N
	Catalog.setAccessibleDescription
	    (cancelButton, "ACSD_Cancel");			 // NOI18N

	dlg.setOptions (new JButton[] {okButton, cancelButton });
	dlg.setClosingOptions(dlg.getOptions());



	// setup listener after we have all the buttons ready

	list.addListSelectionListener(new ListSelectionListener() {
            @Override
	    public void valueChanged(ListSelectionEvent e) {
		sensitizeButtons();
	    }
	});

	sensitizeButtons();

	if (!multiple_selections) {
	    // pre-select first item
	    list.setSelectedIndex(0);
	}

	final Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
	Catalog.setAccessibleDescription(dialog,
					 "ACSD_Overload");	// NOI18N
	dialog.setVisible(true);

	// -------------------- we block here ---------------------

	int selected_item = list.getSelectedIndex();
	if (!cancelable && selected_item == -1) {
	    // Keep trying
	    showWindow(title, nitems, item, cancelable,
		       multiple_selections);
	    return;
	} 	    

	if (okPressed) {
	    selected_indices = list.getSelectedIndices();
	    cancelled = false;
	} else {
	    // Cancel, or Esc: do nothing
	    selected_indices = null;
	    cancelled = true;
	    // OLD selected_item = -1;
	}
    }

    private void doubleClicked(MouseEvent evt) {
	if (evt.getClickCount() == 2) {
	    selected_indices = list.getSelectedIndices();
	    okPressed = true;
	    cancelled = false;
	    Component c = this;
	    // First dismiss the overload dialog. We have no
	    // direct access to it, but can find it by walking
	    // through component parents until found.
	    while (c != null) {
		if (c instanceof JDialog) {
		    JDialog d = (JDialog) c;
		    d.setVisible(false);
		    d.dispose();
		    break;
		}
		c = c.getParent();
	    }
	}
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {

	if (actionEvent.getSource() == okButton) {
	    okPressed = true;
	    // We do everything else when we return from dialog.setVisible();

	} else if (actionEvent.getSource() == selectAllButton) {
	    // Select all
	    int [] indices = new int [nitems];
	    for (int i = 0; i < nitems; i++) {
		indices[i] = i;
	    }
	    list.setSelectedIndices(indices);

	} else if (actionEvent.getSource() == clearButton) {
	    // Clear
	    list.setSelectedIndices(new int[0]);

	    // shift focus to selectAll
	    selectAllButton.requestFocusInWindow();

	} else if (actionEvent.getSource() == invertButton) {
	    // Invert selection
	    int [] selected = list.getSelectedIndices();
	    int numSelected = selected.length;
	    int [] indices = new int [nitems-numSelected];
	    int next = 0;
	    for (int i = 0; i < nitems; i++) {
		boolean isSelected = false;
		for (int j = 0; j < numSelected; j++) {
		    if (selected[j] == i) {
			isSelected = true;
			break;
		    }
		}
		if (!isSelected) {
		    indices[next++] = i;
		}
	    }
	    assert next == nitems-numSelected;
	    list.setSelectedIndices(indices);
	}
    }

    private void sensitizeButtons() {
	// figure out whether we have all selected, none selected or
	// one selected
	int [] selected = list.getSelectedIndices();

	okButton.setEnabled(selected.length > 0);

	if (! multiple_selections) {
	    // single selection
	    selectAllButton.setEnabled(false);
	    clearButton.setEnabled(false);
	    invertButton.setEnabled(false);
	    return;
	}

	if (selected.length == 0) {
	    // none seleted
	    selectAllButton.setEnabled(true);
	    clearButton.setEnabled(false);
	    invertButton.setEnabled(false);

	} else if (selected.length == nitems) {
	    // all selected
	    selectAllButton.setEnabled(false);
	    clearButton.setEnabled(true);
	    invertButton.setEnabled(false);

	} else {
	    // some selected
	    selectAllButton.setEnabled(true);
	    clearButton.setEnabled(true);
	    invertButton.setEnabled(true);
	}
    }
} 
