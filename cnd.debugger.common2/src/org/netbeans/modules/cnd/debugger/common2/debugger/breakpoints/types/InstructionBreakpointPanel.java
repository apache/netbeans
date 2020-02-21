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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

class InstructionBreakpointPanel extends BreakpointPanel {

    private InstructionBreakpoint fb;
    
    @Override
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (InstructionBreakpoint) breakpoint;

	addressField.setText(fb.getAddress());
    }

    /*
     * Constructors
     */
    public InstructionBreakpointPanel() {
	this (new InstructionBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public InstructionBreakpointPanel(NativeBreakpoint b) {
	this ((InstructionBreakpoint)b, true);
    }
    
    /** Creates new form InstructionBreakpointPanel */
    public InstructionBreakpointPanel(InstructionBreakpoint breakpoint,
				      boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;
	initComponents();
	addCommonComponents(2);


	seed(breakpoint);

	// Arrange to revalidate on changes
	addressField.getDocument().addDocumentListener(this);
    }

    @Override
    public void setDescriptionEnabled(boolean enabled) {
	// addressLabel.setEnabled(false);
	addressField.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	addressLabel = new javax.swing.JLabel();
	addressField = new javax.swing.JTextField();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	addressLabel.setText(Catalog.get("AddressLabel"));	// NOI18N
	addressLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_AddressLabel"));		// NOI18N
	addressLabel.setLabelFor(addressField);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(addressLabel, gridBagConstraints1);

	addressField.setColumns(12);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(addressField, gridBagConstraints1);

	// a11y
	addressField.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Address") // NOI18N
	);

    }

    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressField;

    @Override
    protected void assignProperties() {
	String address = addressField.getText ();
	fb.setAddress(address);
    }
    
    @Override
    protected boolean propertiesAreValid() {
	return ! IpeUtils.isEmpty(addressField.getText());
    }
}
