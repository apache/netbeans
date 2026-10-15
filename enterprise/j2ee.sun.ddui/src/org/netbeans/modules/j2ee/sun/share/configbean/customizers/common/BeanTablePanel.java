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
 * BeanTablePanel.java
 *
 * Created on October 4, 2003, 5:35 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.common;

import java.util.ResourceBundle;
import javax.accessibility.AccessibleContext;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;


/** Generic panel containing the table and NEW - EDIT - DELETE buttons.
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public abstract class BeanTablePanel extends javax.swing.JPanel {
    
	protected JTable table;
	protected JButton moveUpButton, moveDownButton, sourceButton;
	private boolean reordable;
	protected BeanTableModel model;
	private boolean isSource;

	private final ResourceBundle bundle =
		ResourceBundle.getBundle(
			"org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.Bundle"); //NOI18N

	/** Creates a new BeanTablePanel.
	* @param model AbstractTableModel for included table
	*/
	public BeanTablePanel(BeanTableModel model) {
		this(model, false,  false);
	}

	/** Creates a new BeanTablePanel.
	* @param model AbstractTableModel for included table
	* @param reordable specifies whether the order of the rows is
	* important(in DD filter-mappings for example the order of elements
	* is important)
	*/
	public BeanTablePanel(BeanTableModel model, boolean reordable) {
		this(model, reordable,  false);
	}

	/** Creates a new BeanTablePanel.
	* @param model AbstractTableModel for included table
	* @param reordable specifies whether the order of the rows is important
	* (in DD filter-mappings for example the order of elements is important)
	* @param isSource specifies if there is a reasonable source file/link
	* related to the table row
	*/
	public BeanTablePanel(BeanTableModel model, final boolean reordable,
			final boolean isSource) {
		this.model=model;
		this.reordable=reordable;
		this.isSource=isSource;

        initComponents();

        table = new FixedHeightJTable();
        
        table.getAccessibleContext().setAccessibleName(getAccessibleName());
        table.getAccessibleContext().setAccessibleDescription(getAccessibleDesc());
        getAccessibleContext().setAccessibleName(getAccessibleName());
        getAccessibleContext().setAccessibleDescription(getAccessibleDesc());
        
		//table = new SortableTable(new SortableTableModel(model));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setModel(model);
		table.getSelectionModel().addListSelectionListener(
					new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e){
				// ignore extra messages
				if (e.getValueIsAdjusting()){
					return;
				}

				if(((ListSelectionModel)e.getSource()).isSelectionEmpty() ||
					table.getModel().getRowCount() == 0 // BUG 5030679
				  ) {
					editButton.setEnabled(false);
					removeButton.setEnabled(false);
					if (reordable) {
						moveUpButton.setEnabled(false);
						moveDownButton.setEnabled(false);
					}
					if (isSource) {
						sourceButton.setEnabled(false);
					}
				} else {
					editButton.setEnabled(true);
					removeButton.setEnabled(true);
					if (reordable) {
						int row = table.getSelectedRow();
						if (row<table.getModel().getRowCount()-1){
							moveDownButton.setEnabled(true);
						}else{
							moveDownButton.setEnabled(false);
						}
						if (row>0){
							moveUpButton.setEnabled(true);
						}else{
							moveUpButton.setEnabled(false);
						}
					}
					if (isSource) {
						sourceButton.setEnabled(true);
					}
				}
			}
		});

		jScrollPane1.setViewportView(table);
		if (reordable) {
			moveUpButton = new JButton(bundle.getString("LBL_Move_Up"));        //NOI18N
			moveDownButton = new JButton(bundle.getString("LBL_Move_Down"));    //NOI18N
			moveUpButton.setEnabled(false);
			moveDownButton.setEnabled(false);
			buttonPanel.add(moveUpButton);
			buttonPanel.add(moveDownButton);
		}
		if (isSource) {
			sourceButton = new JButton(bundle.getString("LBL_Go_To_Source"));   //NOI18N
			sourceButton.setEnabled(false);
			rightPanel.add(sourceButton);
		}

		removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int row = table.getSelectedRow();
				//ddView.setModelChanged(true);
				if(row >= 0) {
					getModel().removeRow(row);

					if(row >= getModel().getRowCount()) {
						--row;
					}

					if(row >= 0) {
						table.getSelectionModel().setSelectionInterval(row, row);
					}
				}
			}
		});

		editButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				displayDialog(true);
			}
		});

		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				displayDialog(false);
			}
		});
	}


	public void setModel(Object parent, CommonDDBean[] children) {
		model.setData(parent, children);
	}


	protected void displayDialog(boolean isEditOperation){
		int row = -1;
		BeanInputDialog  dialog = null;

		if(isEditOperation) {
			row = table.getSelectedRow();
			if(row < 0) {
				return;
			}

			Object[] values = model.getValues(row);
			dialog = getInputDialog(values);
		} else {
			dialog = getInputDialog();
		}

		do {
			int result = dialog.display();
			if(result == dialog.CANCEL_OPTION) {
				break;
			}

			if(result == dialog.OK_OPTION) {
				if (dialog.hasErrors()) {
					dialog.showErrors();
				} else {
					//ddView.setModelChanged(true);
					if(isEditOperation){
						model.editRow(row, dialog.getValues());
					} else {
						model.addRow(dialog.getValues());
					}
				}
			}
		} while (dialog.hasErrors());
	}


	private void initComponents() {
		jScrollPane1 = new javax.swing.JScrollPane();
		southPanel = new javax.swing.JPanel();
		buttonPanel = new javax.swing.JPanel();
		addButton = new javax.swing.JButton();
		editButton = new javax.swing.JButton();
		removeButton = new javax.swing.JButton();
		rightPanel = new javax.swing.JPanel();

		setLayout(new java.awt.BorderLayout());
		add(jScrollPane1, java.awt.BorderLayout.CENTER);
		southPanel.setLayout(new java.awt.BorderLayout());

        String buttonText = bundle.getString("LBL_New");
        int mnemonicIndex = buttonText.indexOf(bundle.getString("MNC_New").charAt(0));
        if(mnemonicIndex < 0) {
            mnemonicIndex = 0;
        }
        addButton.setText(buttonText); //NOI18N
        addButton.setDisplayedMnemonicIndex(mnemonicIndex);
        AccessibleContext accessibleContext = addButton.getAccessibleContext();
		accessibleContext.setAccessibleName(bundle.getString("ACSN_New"));	// NOI18N
		accessibleContext.setAccessibleDescription(bundle.getString("ACSD_New"));	// NOI18N
		buttonPanel.add(addButton);

        buttonText = bundle.getString("LBL_Edit");
        mnemonicIndex = buttonText.indexOf(bundle.getString("MNC_Edit").charAt(0));
        if(mnemonicIndex < 0) {
            mnemonicIndex = 0;
        }
        editButton.setText(buttonText); //NOI18N
        editButton.setDisplayedMnemonicIndex(mnemonicIndex);
		editButton.setEnabled(false);
		accessibleContext = editButton.getAccessibleContext();
		accessibleContext.setAccessibleName(bundle.getString("ACSN_Edit"));	// NOI18N
		accessibleContext.setAccessibleDescription(bundle.getString("ACSD_Edit"));	// NOI18N
		buttonPanel.add(editButton);

        buttonText = bundle.getString("LBL_Delete");
        mnemonicIndex = buttonText.indexOf(bundle.getString("MNC_Delete").charAt(0));
        if(mnemonicIndex < 0) {
            mnemonicIndex = 0;
        }
        removeButton.setText(buttonText); //NOI18N
        removeButton.setDisplayedMnemonicIndex(mnemonicIndex);
		removeButton.setEnabled(false);
		accessibleContext = removeButton.getAccessibleContext();
		accessibleContext.setAccessibleName(bundle.getString("ACSN_Delete"));	// NOI18N
		accessibleContext.setAccessibleDescription(bundle.getString("ACSD_Delete"));	// NOI18N
		buttonPanel.add(removeButton);

		southPanel.add(buttonPanel, java.awt.BorderLayout.CENTER);
		southPanel.add(rightPanel, java.awt.BorderLayout.EAST);
		add(southPanel, java.awt.BorderLayout.SOUTH);
	}

	private javax.swing.JPanel southPanel;
	protected javax.swing.JButton addButton;
	private javax.swing.JPanel buttonPanel;
	private javax.swing.JScrollPane jScrollPane1;
	protected javax.swing.JButton editButton;
	protected javax.swing.JButton removeButton;
	private javax.swing.JPanel rightPanel;

	abstract public BeanInputDialog getInputDialog(Object[] values);
	abstract public BeanInputDialog getInputDialog();

        protected String getAccessibleName(){
            //default value; used in case derived class does not provide one
            return bundle.getString("ACSN_Table");               //NOI18N
        }

        protected String getAccessibleDesc(){
            //default value; used in case derived class does not provide one
            return bundle.getString("ACSD_Table");               //NOI18N
        }


	public BeanTableModel getModel() {
		return model;
	}


	public void setTitle(String title) {
		javax.swing.JLabel label = new javax.swing.JLabel(title);
		label.setBorder(new javax.swing.border.EmptyBorder(5,5,5,0));
		add(label, java.awt.BorderLayout.NORTH);
	}


	public void setSelectedRow(int row) {
		table.setRowSelectionInterval(row,row);
	}


	public void setButtons(boolean b1, boolean b2, boolean b3) {
		addButton.setEnabled(b1);
		editButton.setEnabled(b2);
		removeButton.setEnabled(b3);
	}


	public void setButtons(boolean b1, boolean b2, boolean b3, boolean b4,
			boolean b5, boolean b6) {
		this.setButtons(b1,b2,b3);
		moveUpButton.setEnabled(b4);
		moveDownButton.setEnabled(b5);
	}


	public boolean isReordable() {
		return reordable;
	}


	public boolean isSource() {
		return isSource;
	}
}
