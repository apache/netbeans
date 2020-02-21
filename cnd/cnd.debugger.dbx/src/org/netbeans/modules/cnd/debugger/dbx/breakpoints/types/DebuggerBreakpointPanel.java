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

import org.netbeans.modules.cnd.debugger.common2.values.DebuggerEvent;

class DebuggerBreakpointPanel extends BreakpointPanel {

    private DebuggerBreakpoint fb;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (DebuggerBreakpoint) breakpoint;

	DebuggerEvent de = fb.getSubEvent();
	eventCombo.setSelectedItem(de.toString());
    }

    /*
     * Constructors
     */
    public DebuggerBreakpointPanel() {
	this (new DebuggerBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public DebuggerBreakpointPanel(NativeBreakpoint b) {
	this ((DebuggerBreakpoint)b, true);
    }
    
    /** Creates new form DebuggerBreakpointPanel */
    public DebuggerBreakpointPanel(DebuggerBreakpoint breakpoint,
				    boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(1);
	eventCombo.setEditable(false);
	
	/** Items in the combo boxes */
	final String[] comboValues = DebuggerEvent.getTags();
	eventCombo.setModel(new DefaultComboBoxModel(comboValues));

	seed(breakpoint);

	// Arrange to revalidate on changes
	eventCombo.addItemListener(this);
    }
    
    public void setDescriptionEnabled(boolean enabled) {
	// processLabel.setEnabled(false);
	eventCombo.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	processLabel = new javax.swing.JLabel();
	eventCombo = new javax.swing.JComboBox();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	processLabel.setText(Catalog.get("Event"));	// NOI18N
	processLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Event"));		// NOI18N
	processLabel.setLabelFor(eventCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(processLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(eventCombo, gridBagConstraints1);

	// a11y
	eventCombo.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Event") // NOI18N
	);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel processLabel;
    private javax.swing.JComboBox eventCombo;

    protected void assignProperties() {
	String selection = eventCombo.getSelectedItem().toString();
	fb.setSubEvent(DebuggerEvent.byTag(selection));
    }
    
    protected boolean propertiesAreValid() {
	// We just have a non-editable combo-box so no way to go wrong.
	return true;
    }
}
