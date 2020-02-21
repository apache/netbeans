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
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.SystemInfo;
import java.util.Vector;
import java.awt.Component;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

class FaultBreakpointPanel extends BreakpointPanel {

    private FaultBreakpoint fb;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (FaultBreakpoint) breakpoint;

	// LATER
	// The following doesn't work as explained in SignalBreakpointType
	faultCombo.setSelectedItem(fb.getFault());
    }

    /*
     * Constructors
     */
    public FaultBreakpointPanel() {
	this (new FaultBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public FaultBreakpointPanel(NativeBreakpoint b) {
	this ((FaultBreakpoint)b, true);
    }
    
    
    /** Creates new form FaultBreakpointPanel */
    public FaultBreakpointPanel(FaultBreakpoint breakpoint,
				boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(1);

	/** Items in the combo boxes */
	Component c = faultCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    javax.swing.text.Document d =
		((javax.swing.text.JTextComponent)c).getDocument();
	    d.addDocumentListener(this);
	} else {
	    assert false;
	}
	faultCombo.setEditable(true);

	Vector<String> items = new Vector<String>(20);
	SystemInfo si_faults = new SystemInfo.Faults(items);
	si_faults.stuffInto(faultCombo);


	seed(breakpoint);

	// Arrange to revalidate on changes
	faultCombo.addItemListener(this);
    }

    public void setDescriptionEnabled(boolean enabled) {
	// faultLabel.setEnabled(false);
	faultCombo.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	faultLabel = new javax.swing.JLabel();
	faultCombo = new javax.swing.JComboBox();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	faultLabel.setText(Catalog.get("Fault"));	// NOI18N
	faultLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Fault"));		// NOI18N
	faultLabel.setLabelFor(faultCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(faultLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(faultCombo, gridBagConstraints1);

	// a11y
	faultCombo.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Fault") // NOI18N
	);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel faultLabel;
    private javax.swing.JComboBox faultCombo;

    protected void assignProperties() {
	fb.setFault(faultCombo.getSelectedItem().toString());
    }
    
    protected boolean propertiesAreValid() {
	Component c = faultCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    String text = ((javax.swing.text.JTextComponent)c).getText();
	    if (IpeUtils.isEmpty(text)) {
		return false;
	    } else {
		return true;
	    }
	} else if ((faultCombo.getSelectedItem() != null) &&
		   (faultCombo.getSelectedItem().toString() != null) &&
		   (faultCombo.getSelectedItem().toString().trim().length() != 0)){
	    return true;
	}
	return false;
    }
}
