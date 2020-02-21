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

package org.netbeans.modules.cnd.debugger.dbx.breakpoints.types;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointPanel;
import javax.swing.*;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.values.ProcessEvent;

class ProcessBreakpointPanel extends BreakpointPanel {

    private ProcessBreakpoint fb;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel processLabel;
    private javax.swing.JComboBox processCombo;
    private javax.swing.JLabel exitCodeLabel;
    private javax.swing.JTextField exitCodeField;

    // OLD private boolean exitCodeFieldEnabled;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (ProcessBreakpoint) breakpoint;

	ProcessEvent pe = fb.getSubEvent();
	processCombo.setSelectedItem(pe.toString());

	exitCodeField.setText(fb.getExitCode());

	adjustexitcode(pe);
    }

    /*
     * Constructors
     */
    public ProcessBreakpointPanel() {
	this (new ProcessBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public ProcessBreakpointPanel(NativeBreakpoint b) {
	this ((ProcessBreakpoint)b, true);
    }
    
    /** Creates new form ProcessBreakpointPanel */
    public ProcessBreakpointPanel(ProcessBreakpoint breakpoint, 
				  boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;
	initComponents();
	addCommonComponents(1);
	processCombo.setEditable(false);
	
	/** Items in the combo boxes */
	final String[] comboValues = ProcessEvent.getTags();
	processCombo.setModel(new DefaultComboBoxModel(comboValues));

	processCombo.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		String value = processCombo.getSelectedItem().toString();
		if (value == null)
		    return;
		ProcessEvent processType = ProcessEvent.byTag(value);
		adjustexitcode(processType);
	    }
	});

	seed(breakpoint);

	// Arrange to revalidate on changes
	exitCodeField.getDocument().addDocumentListener(this);
	processCombo.addItemListener(this);
    }

    public void setDescriptionEnabled(boolean enabled) {
	if (!enabled) {
	    // processLabel.setEnabled(false);
	    processCombo.setEnabled(false);
	    // exitCodeLabel.setEnabled(false);
	    // OLD exitCodeFieldEnabled = false;
	    exitCodeField.setEnabled(false);
	} else {
	    // OLD exitCodeFieldEnabled = true;
	}
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	processLabel = new javax.swing.JLabel();
	processCombo = new javax.swing.JComboBox();
	exitCodeLabel = new javax.swing.JLabel();;
	exitCodeField = new javax.swing.JTextField();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	processLabel.setText(Catalog.get("Event"));	// NOI18N
	processLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Event"));		// NOI18N
	processLabel.setLabelFor(processCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.ipady = 0;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(processLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(processCombo, gridBagConstraints1);
	
	exitCodeLabel.setText(Catalog.get("ExitCode"));	// NOI18N
	exitCodeLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_ExitCode"));	// NOI18N
	exitCodeLabel.setLabelFor(exitCodeField);
	exitCodeLabel.setEnabled(false);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 6;
	gridBagConstraints1.ipady = 0;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(exitCodeLabel, gridBagConstraints1);

	exitCodeField.setColumns(3);
	exitCodeField.setEditable(false);
	exitCodeField.setEnabled(false);
	javax.swing.JPanel exitCodePanel = new javax.swing.JPanel();
	exitCodePanel.setLayout(new java.awt.BorderLayout());
	exitCodePanel.add(exitCodeField, java.awt.BorderLayout.WEST);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 6;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(exitCodePanel, gridBagConstraints1);

	// a11y
	//exitCodeField.getAccessibleContext().setAccessibleDescription(
	//    Catalog.get("ACSD_ExitCode") // NOI18N
	//);

	processCombo.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Event") // NOI18N
	);
    }

    protected void assignProperties() {
	String selection = processCombo.getSelectedItem().toString();
	ProcessEvent processType = ProcessEvent.byTag(selection);

	fb.setSubEvent(processType);
	if (processType == ProcessEvent.EXIT)
	    fb.setExitCode(exitCodeField.getText());
	adjustexitcode(processType);
    }
    
    protected boolean propertiesAreValid() {

	String selection = processCombo.getSelectedItem().toString();
	if (IpeUtils.isEmpty(selection))
	    return false;
	ProcessEvent processType = ProcessEvent.byTag(selection);

	// If we're an exit event ensure that exit code is unspecified or
	// is a valid number.

	if (processType == ProcessEvent.EXIT) {
	    String exitCodeString = exitCodeField.getText();
	    if (!IpeUtils.isEmpty(exitCodeString)) {
		try {
		    int i = Integer.parseInt (exitCodeString);
		} catch (NumberFormatException e) {
		    return false;
		}
	    }
	}

	return true;
    }
 
    /**
     * Set the enableness of the exitcode component based on the event
     */

    private void adjustexitcode(ProcessEvent processType) {
	// 6485888
	if (processType == ProcessEvent.EXIT) {
	    exitCodeLabel.setEnabled(true);
	    exitCodeField.setEditable(true);
	    exitCodeField.setEnabled(true);
	} else {
	    exitCodeLabel.setEnabled(false);
	    exitCodeField.setEditable(false);
	    exitCodeField.setEnabled(false);
	}
    }
}
