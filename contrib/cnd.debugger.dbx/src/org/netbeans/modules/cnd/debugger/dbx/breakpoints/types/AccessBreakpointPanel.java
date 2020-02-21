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
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.values.AccessBA;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;

class AccessBreakpointPanel extends BreakpointPanel {
	
    private AccessBreakpoint fb;
	
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (AccessBreakpoint) breakpoint;

	addressField.setText(fb.getAddress());

	if (fb.getSize() == null)
	    lengthField.setText(Catalog.get("CTL_Default"));
	else
	    lengthField.setText(fb.getSize());

	readToggle.setSelected(fb.isRead());
	writeToggle.setSelected(fb.isWrite());
	executeToggle.setSelected(fb.isExecute());

	if (fb.getWhen() == AccessBA.BEFORE)
	    beforeToggle.setSelected(true);
	else
	    afterToggle.setSelected(true);
    }

    /*
     * Constructors
     */

    public AccessBreakpointPanel() {
	this(new AccessBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public AccessBreakpointPanel(NativeBreakpoint b) {
	this((AccessBreakpoint)b, true);
    }

    /** Creates new form AccessBreakpointPanel */
    private AccessBreakpointPanel(AccessBreakpoint breakpoint, 
				 boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;
	initComponents();
	addCommonComponents(5);

	if (!customizing) {
	    String selection = EditorBridge.getCurrentSelection();
	    if (selection != null)
		breakpoint.setAddress("& " +	// NOI18N
				      selection);
	}

	seed(breakpoint);


	// Arrange to revalidate on changes

	addressField.getDocument().addDocumentListener(this);
	lengthField.getDocument().addDocumentListener(this);
	readToggle.addItemListener(this);
	writeToggle.addItemListener(this);
	executeToggle.addItemListener(this);

    
	//addressField.requestDefaultFocus();
	addressField.requestFocus();
    }

    public void setDescriptionEnabled(boolean enabled) {
	// jLabel6.setEnabled(false);
	addressField.setEnabled(false);
	// jLabel3.setEnabled(false);
	lengthField.setEnabled(false);
	// jLabel5.setEnabled(false);
	beforeToggle.setEnabled(false);
	afterToggle.setEnabled(false);
	// jLabel4.setEnabled(false);
	readToggle.setEnabled(false);
	writeToggle.setEnabled(false);
	executeToggle.setEnabled(false);
    }


    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	buttonGroup1 = new javax.swing.ButtonGroup();
	jLabel6 = new javax.swing.JLabel();
	addressField = new javax.swing.JTextField();
	jLabel3 = new javax.swing.JLabel();
	lengthField = new javax.swing.JTextField();
	jLabel5 = new javax.swing.JLabel();
	beforeToggle = new javax.swing.JRadioButton();
	afterToggle = new javax.swing.JRadioButton();
	jLabel4 = new javax.swing.JLabel();
	readToggle = new javax.swing.JCheckBox();
	writeToggle = new javax.swing.JCheckBox();
	executeToggle = new javax.swing.JCheckBox();
	
	
	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;
	
	jLabel6.setText(Catalog.get("Address"));	// NOI18N
	jLabel6.setDisplayedMnemonic(
		Catalog.getMnemonic("MNEM_Address"));	// NOI18N
	jLabel6.setLabelFor(addressField);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(jLabel6, gridBagConstraints1);
	
	addressField.setColumns(12);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	panel_settings.add(addressField, gridBagConstraints1);
	
	jLabel3.setText(Catalog.get("Length"));		// NOI18N
	jLabel3.setDisplayedMnemonic(
		Catalog.getMnemonic("MNEM_Length"));	// NOI18N
	jLabel3.setLabelFor(lengthField);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(jLabel3, gridBagConstraints1);
	
	lengthField.setColumns(12);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	panel_settings.add(lengthField, gridBagConstraints1);
	
	jLabel5.setText(Catalog.get("When")); // NOI18N
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 2;
	gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(jLabel5, gridBagConstraints1);
	
	beforeToggle.setText(Catalog.get("Before"));		// NOI18N
	beforeToggle.setMnemonic(
		     Catalog.getMnemonic("MNEM_Before"));	// NOI18N
	buttonGroup1.add(beforeToggle);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 2;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(beforeToggle, gridBagConstraints1);
	
	afterToggle.setText(Catalog.get("After")); // NOI18N
	afterToggle.setMnemonic(
		    Catalog.getMnemonic("MNEM_After")); // NOI18N
	buttonGroup1.add(afterToggle);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 2;
	gridBagConstraints1.gridy = 2;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(afterToggle, gridBagConstraints1);
	
	jLabel4.setText(Catalog.get("Operation"));	// NOI18N
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 3;
	gridBagConstraints1.insets = new java.awt.Insets(5, 5, 5, 5);
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(jLabel4, gridBagConstraints1);
	
	readToggle.setText(Catalog.get("Read"));	// NOI18N
	readToggle.setMnemonic(
		   Catalog.getMnemonic("MNEM_Read"));	// NOI18N
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 3;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(readToggle, gridBagConstraints1);
	
	writeToggle.setText(Catalog.get("Write"));	// NOI18N
	writeToggle.setMnemonic(
		    Catalog.getMnemonic("MNEM_Write")); // NOI18N
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 2;
	gridBagConstraints1.gridy = 3;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(writeToggle, gridBagConstraints1);
	
	executeToggle.setText(Catalog.get("Execute"));	// NOI18N
	executeToggle.setMnemonic(
		      Catalog.getMnemonic("MNEM_Execute")); // NOI18N
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 3;
	gridBagConstraints1.gridy = 3;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(executeToggle, gridBagConstraints1);


	writeToggle.setSelected(true);
	afterToggle.setSelected(true);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JTextField addressField;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField lengthField;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JRadioButton beforeToggle;
    private javax.swing.JRadioButton afterToggle;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JCheckBox readToggle;
    private javax.swing.JCheckBox writeToggle;
    private javax.swing.JCheckBox executeToggle;


    protected void assignProperties() {
	fb.setAddress(addressField.getText().trim());

	String len = lengthField.getText().trim();
	if (len.equals(Catalog.get("CTL_Default"))) { // NOI18N
	    fb.setSize(null);
	} else if (len.length() == 0) {
	    fb.setSize(null);
	} else {
	    fb.setSize(len);
	}
	
	fb.setRead(readToggle.isSelected());
	fb.setWrite(writeToggle.isSelected());
	fb.setExecute(executeToggle.isSelected());
	if (beforeToggle.isSelected())
	    fb.setWhen(AccessBA.BEFORE);
	else
	    fb.setWhen(AccessBA.AFTER);
    }
	
    protected boolean propertiesAreValid() {
	if (IpeUtils.isEmpty(addressField.getText())) {
	    return false;
	}

	// At least one of r, w or x should be selected.
	if (!readToggle.isSelected() &&
	    !writeToggle.isSelected() &&
	    !executeToggle.isSelected()) {
	    return false;
	}

	return true;
    }
}
