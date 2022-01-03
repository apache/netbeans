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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 */
public class MasterView<R extends Record> extends JPanel implements Validator {

    private final RecordList<R> recordList;
    private final DetailView<R> detailView;

    private JList list = null;

    // Set targetSelection to the intended selection and call updateView
    private int targetSelection = 0;

    private boolean updating = false;
    private boolean dirty = false;

    // How many elements will be shown in the pick-list menu
    // The MasterView can show more but it will mark the boundry 
    public final static int MAX_VISIBLE_IN_MENU = 20;

    /** Creates new form Make */
    public MasterView (RecordList<R> recordList, DetailView<R> detailView) {
	this.recordList = recordList;
	this.detailView = detailView;

        initComponents();

	list = new JList();
	list.setCellRenderer(new MyCellRenderer());
	list.setVisibleRowCount(8);
	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	list.setListData(recordList.getRecordsDisplayName());
	scrollPane.setViewportView(list);
	list.setSelectedIndex(0);

	initAccessibility();

	// Arrange to get notification when a different record is selected.
	// We'll get these as the list gets modified as well.

	list.addListSelectionListener(new ListSelectionListener() {
            @Override
	    public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting())
		    return;
		if (updating)
		    return;
		if (!canChangeSelection())         
		    return;	
		commitPending();
		targetSelection = list.getSelectedIndex();
		updateView();
	    }
	});

	list.addKeyListener(new java.awt.event.KeyAdapter() {
	    @Override
	    public void keyPressed(java.awt.event.KeyEvent evt) {
		if (evt.isConsumed())
		    return;
		if (evt.getKeyChar() == KeyEvent.VK_DELETE) {
		    evt.consume();
		    deleteAction();
		    //closeAction(dialog, CANCEL_OPTION);
		}
	    }
	});

	// Arrange to update ourselves when our model changes
	recordList.addRecordListListener(new RecordListListener() {
            @Override
	    public void contentsChanged(RecordListEvent e) {
		updateView();
	    }
	});

	updateView();
    }

    boolean canChangeSelection() {
    	if (detailView.isDirty()) {
	    // ask user whether they want to discard or commit or
	    // continue editing
	    String msg = Catalog.get("MSG_APPLY_FIRST");	// NOI18N
	    NotifyDescriptor.Message descriptor =
		new NotifyDescriptor.Message(msg,
			 NotifyDescriptor.WARNING_MESSAGE);
	    DialogDisplayer.getDefault().notify(descriptor);
	    return false;
    	} else {
	    return true;
    	}
    }

    private void initAccessibility() {
	AccessibleContext context;

	context = getAccessibleContext();
	context.setAccessibleDescription(guidanceText.getText());

	context = list.getAccessibleContext();
	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	context = scrollPane.getAccessibleContext();
	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	context = scrollPane.getHorizontalScrollBar().getAccessibleContext();
	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	context = scrollPane.getVerticalScrollBar().getAccessibleContext();
	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	duplicateButton.getAccessibleContext().setAccessibleDescription(duplicateButton.getText());
	deleteButton.getAccessibleContext().setAccessibleDescription(deleteButton.getText());
	deleteAllButton.getAccessibleContext().setAccessibleDescription(deleteAllButton.getText());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        guidanceText = new javax.swing.JTextArea();
        scrollPane = new javax.swing.JScrollPane();

        listButtonPanel = new javax.swing.JPanel();
        duplicateButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        deleteAllButton = new javax.swing.JButton();

        this.setLayout(new java.awt.GridBagLayout());

        guidanceText.setBackground(new java.awt.Color(204, 204, 204));
        guidanceText.setEditable(false);
        guidanceText.setLineWrap(true);
        //guidanceText.setText(Catalog.get("LOADLISTEDITDIALOG_GUIDANCE_TXT"));
        guidanceText.setWrapStyleWord(true);
        guidanceText.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);

	this.add(guidanceText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        //gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);

	this.add(scrollPane, gridBagConstraints);

        listButtonPanel.setLayout(new java.awt.GridBagLayout());

        duplicateButton.setMnemonic(Catalog.get("LISTEDITDIALOG_DUPLICATE_BUTTON_MN").charAt(0)); // NOI18N
        duplicateButton.setText(Catalog.get("LISTEDITDIALOG_DUPLICATE_BUTTON_TXT")); // NOI18N
        duplicateButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                duplicateButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        listButtonPanel.add(duplicateButton, gridBagConstraints);

        deleteButton.setMnemonic(Catalog.get("LISTEDITDIALOG_DELETE_BUTTON_MN").charAt(0)); // NOI18N
	deleteButton.setEnabled(false);
        deleteButton.setText(Catalog.get("LISTEDITDIALOG_DELETE_BUTTON_TXT")); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        listButtonPanel.add(deleteButton, gridBagConstraints);

        deleteAllButton.setMnemonic(Catalog.get("LISTEDITDIALOG_DELETE_ALL_BUTTON_MN").charAt(0)); // NOI18N
        deleteAllButton.setText(Catalog.get("LISTEDITDIALOG_DELETE_ALL_BUTTON_TXT")); // NOI18N
        deleteAllButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAllButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        listButtonPanel.add(deleteAllButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 7, 0, 11);

	this.add(listButtonPanel, gridBagConstraints);
    }

    private void duplicateAction() {

        int selectedIndex = list.getSelectedIndex();

        if (selectedIndex < 0)
            return;


	if (detailView != null && detailView.isDirty()) {
	    // What to do if the detailPanel contains a modified clone of
	    // what we're duplicating? 
	    // For now abort the operation.

	    String msg = Catalog.get("MSG_CANNOT_DUPLICATE");	// NOI18N
	    NotifyDescriptor.Message descriptor =
		new NotifyDescriptor.Message(msg,
					     NotifyDescriptor.WARNING_MESSAGE);
	    DialogDisplayer.getDefault().notify(descriptor);
	    return;
	}

	R record = recordList.getRecordAt(selectedIndex);
	@SuppressWarnings("unchecked")
	R dup = (R) record.cloneRecord();
	String newKey = recordList.newKey();
	if (newKey != null)
	    dup.setKey(newKey);
	targetSelection = selectedIndex+1;
	recordList.addRecordAfter(dup, selectedIndex);

	setDirty(true);
    }

    private void duplicateButtonActionPerformed(java.awt.event.ActionEvent evt) {
	duplicateAction();
    }

    private void deleteAllAction() {
        recordList.removeAllButArchetype();
	setDirty(true);
    }

    private void deleteAllButtonActionPerformed(java.awt.event.ActionEvent evt) {
	deleteAllAction();
    }

    private void deleteAction() {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex < 0)
            return;
        recordList.removeRecordAt(selectedIndex);
	setDirty(true);
    }

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
	deleteAction();
    }
    
    protected void closeAction() {
	// to be overridden
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables

    private javax.swing.JPanel listButtonPanel;
//    private javax.swing.JButton cancelButton;
    private javax.swing.JButton deleteAllButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton duplicateButton;
    private javax.swing.JTextArea guidanceText;
    private javax.swing.JScrollPane scrollPane;

    // End of variables declaration//GEN-END:variables

    private void updateButtons() {
	int selection = list.getSelectedIndex();
	if (Log.MasterDetail.debug) {
	    System.out.printf("ModelView.updateButtons(): selection = %d\n", // NOI18N
		selection);
	}


	if (recordList.getSize() > 0) {
	    if (selection >= 0) {
		// Have a selection
		R record = recordList.getRecordAt(selection);

		duplicateButton.setEnabled(true);

		// Cannot delete archetypes
		deleteButton.setEnabled(! record.isArchetype());

		// Cannot delete all if all we have is the archetype
		boolean soleArchetype = recordList.getSize() == 1 &&
					record.isArchetype();
		deleteAllButton.setEnabled(! soleArchetype);

	    } else {
		// No selection
		duplicateButton.setEnabled(false);
		deleteButton.setEnabled(false);
		deleteAllButton.setEnabled(true);
	    }

	} else {
	    // Empty list
	    duplicateButton.setEnabled(false);
	    deleteButton.setEnabled(false);

	    deleteAllButton.setEnabled(false);
	}
    }

    public int getSelectedIndex() {
	return list.getSelectedIndex();
    }

    void commitPending() {
	if (detailView.isRecordValid() && detailView.isDirty()) {
	    detailView.commit();
	    setDirty(true);
	}
    }

    public final void updateView() {
	if (Log.MasterDetail.debug) {
	    System.out.printf("MasterView.updateView(): targetSelection=%d\n", // NOI18N
		targetSelection);
	}

	// Normalize targetSelection
	if (recordList.getSize() <= 0)
	    targetSelection = -1;
	else if (targetSelection < 0)
	    targetSelection = 0;
	else if (targetSelection >= recordList.getSize())
	    targetSelection = recordList.getSize()-1;

	// absorb new model
	try {
	    updating = true;

	    list.setListData(recordList.getRecordsDisplayName());
	    list.setSelectedIndex(targetSelection);
	    list.ensureIndexIsVisible(targetSelection);

	} finally {
	    updating = false;
	}

	updateButtons();

	// Get the detailView to show the selected Record
	detailView.setRecord(recordList.getRecordAt(targetSelection));
    }

    /* Should be overridden */
    protected HelpCtx getHelpCtx() {
	return null;
    }

    class MyCellRenderer extends JLabel implements ListCellRenderer {
        @Override
	public Component getListCellRendererComponent(
	JList list,
	Object value,            // value to display
	int index,               // cell index
	boolean isSelected,      // is the cell selected
	boolean cellHasFocus)    // the list and the cell have the focus
	{
	    String s = value.toString();
	    setText(s); // NOI18N

	    // Mark the boundry where the Records aren't available on 
	    // the picklist menu.

	    if (index == MAX_VISIBLE_IN_MENU) {
		setBorder(new javax.swing.border.MatteBorder(new java.awt.Insets(2, 0, 0, 0), java.awt.Color.gray));
	    }
	    else {
		setBorder(null); // No border
	    }

	    if (isSelected) {
		setBackground(list.getSelectionBackground());
		setForeground(list.getSelectionForeground());
	    } else {
		setBackground(list.getBackground());
		setForeground(list.getForeground());
	    }
	    setEnabled(list.isEnabled());
	    setFont(list.getFont());
	    setOpaque(true);
	    return this;
	}
    }

    public boolean updateModifiedDialog(int selectedIndex) {
	JButton updateButton = new JButton(Catalog.get("MODIFIEDDIALOG_UPDATE_BUTTON_TXT")); // NOI18N
	JButton cancelButton = new JButton(Catalog.get("MODIFIEDDIALOG_CANCEL_BUTTON_TXT")); // NOI18N
	updateButton.getAccessibleContext().setAccessibleDescription(
		Catalog.get("MODIFIEDDIALOG_UPDATE_BUTTON_ACSD") // NOI18N
	);
	cancelButton.getAccessibleContext().setAccessibleDescription(
		Catalog.get("MODIFIEDDIALOG_CANCEL_BUTTON_ACSD") // NOI18N
	);
	NotifyDescriptor d = new NotifyDescriptor(
	    Catalog.get("MODIFIEDDIALOG_COMMANDS_MODIFIED"), // NOI18N
	    Catalog.get("MODIFIEDDIALOG_UNSAVED_MODIFICATIONS"), // NOI18N
	    NotifyDescriptor.OK_CANCEL_OPTION,
	    NotifyDescriptor.WARNING_MESSAGE,
	    new JButton[] {updateButton, cancelButton},
	    updateButton
	);
	if (DialogDisplayer.getDefault().notify(d) == updateButton) {
	    // TMP elementPanel.reset();
	    // TMP applyAction(selectedIndex);
	    return true;
	}
	else {
	    return false;
	}
    }

    public JList getList() {
        return list;
    }

    private Validator validator;

    void setValidator(Validator validator) {
	this.validator = validator;
    }

    public void setDirty(boolean dirty) {
	this.dirty = dirty;
	fireChanged();
    }

    // implement Validator
    @Override
    public void fireChanged() {
	if (validator != null)
	    validator.fireChanged();
    }

    // implement Validator
    @Override
    public boolean isRecordValid() {
	return true;
    }

    // implement Validator
    @Override
    public boolean isDirty() {
	return dirty;
    }
}
