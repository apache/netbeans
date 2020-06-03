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
import javax.swing.*;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

class SignalBreakpointPanel extends BreakpointPanel {

    private SignalBreakpoint fb;
    private Vector<String> subcodeValues = null;

    private SystemInfo.Subcodes si_subcodes;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (SignalBreakpoint) breakpoint;

	// LATER
	// The following doesn't work because what the engine tells us might
	// not be the same strings as we have in the combo-box,
	// so SystemInfo.Signals needs to be enhanced to map stuff?

	signalCombo.setSelectedItem(fb.getSignal());

	String subCode = fb.getSubcode();
	if (subCode == null)
	    subcodeCombo.setSelectedItem(si_subcodes.all());
	else
	    subcodeCombo.setSelectedItem(subCode);
    }

    /*
     * Constructors
     */

    public SignalBreakpointPanel() {
	this(new SignalBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public SignalBreakpointPanel(NativeBreakpoint b) {
	this((SignalBreakpoint)b, true);
    }


    /** Creates new form SignalBreakpointPanel */
    public SignalBreakpointPanel(SignalBreakpoint breakpoint,
				 boolean customizing ) {
	super(breakpoint,customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(2);

	Component c = signalCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    //((javax.swing.text.JTextComponent)c).addCaretListener(this);
	    javax.swing.text.Document d =
		((javax.swing.text.JTextComponent)c).getDocument();
	    d.addDocumentListener(this);
	}

	signalCombo.setEditable(true);
	subcodeCombo.setEditable(true);

	c = subcodeCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    javax.swing.text.Document d =
		((javax.swing.text.JTextComponent)c).getDocument();
	    d.addDocumentListener(this);
	}

	/** Items in the combo boxes */
	Vector<String> comboValues = new Vector<String>(250);
	comboValues.add("SIGSEGV"); // NOI18N Default. Yes, will be duplicate.
	SystemInfo si_signals = new SystemInfo.Signals(comboValues);
	si_signals.stuffInto /* OLD Async */(signalCombo);

	// Subcodes
	subcodeValues = new Vector<String>(30);
	si_subcodes = new SystemInfo.Subcodes(subcodeValues);
	si_subcodes.stuffInto /* OLD Async */(subcodeCombo);

	// The above stuffInto populates 'subcodeValues' and 'subcodeCombo'
	// with _all_ the possible subcode values.
	// Now repopulate 'subcodeCombo' with signal-specific ones.
	updateSubcodeList();

	seed(breakpoint);

	// Arrange to revalidate on changes
	signalCombo.addItemListener(this);
	subcodeCombo.addItemListener(this);
    }
    
    public void setDescriptionEnabled(boolean enabled) {
	// signalLabel.setEnabled(false);
	signalCombo.setEnabled(false);
	// subcodeLabel.setEnabled(false);
	subcodeCombo.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	signalLabel = new javax.swing.JLabel();
	signalCombo = new javax.swing.JComboBox();
	subcodeLabel = new javax.swing.JLabel();
	subcodeCombo = new javax.swing.JComboBox();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	signalLabel.setText(Catalog.get("Signal"));	// NOI18N
	signalLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Signal"));	// NOI18N
	signalLabel.setLabelFor(signalCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(signalLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(signalCombo, gridBagConstraints1);

	subcodeLabel.setText(Catalog.get("Subcode"));	// NOI18N
	subcodeLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Subcode"));	// NOI18N
	subcodeLabel.setLabelFor(subcodeCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(subcodeLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(subcodeCombo, gridBagConstraints1);

	// a11y
	signalCombo.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Signal") // NOI18N
	);
	subcodeCombo.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Subcode") // NOI18N
	);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel signalLabel;
    private javax.swing.JComboBox signalCombo;
    private javax.swing.JLabel subcodeLabel;
    private javax.swing.JComboBox subcodeCombo;


    protected void assignProperties() {
	String sig = signalCombo.getSelectedItem().toString();
	fb.setSignal(sig);

	if (subcodeCombo.getSelectedItem() != null) {
	    String code = subcodeCombo.getSelectedItem().toString();
	    if (code.equals(si_subcodes.all())) {
		fb.setSubcode(null);
	    } else {
		fb.setSubcode(code);
	    }
	}
    }

    protected boolean propertiesAreValid() {
	// Called on every keystroke - update the subcode alternatives list
	updateSubcodeList();

	// XXX Apply some logic here for figuring out if you've entered a
	//   valid signal???
	
	Component c = signalCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    String text = ((javax.swing.text.JTextComponent)c).getText();
	    if (IpeUtils.isEmpty(text)) {
		return false;
	    } else {
		return true;
	    }
	} else if ((signalCombo.getSelectedItem() != null) &&
	   (signalCombo.getSelectedItem().toString() != null) &&
	   (signalCombo.getSelectedItem().toString().trim().length() != 0)){
	    return true;
	}
	
	return true;
    }

    private boolean lastWasEmpty = false; // first time it should trigger
    private String lastSignal = null;

    private void clearOutList() {
	if (!lastWasEmpty) {
	    DefaultComboBoxModel dfm =
		(DefaultComboBoxModel)subcodeCombo.getModel();
	    dfm.removeAllElements();
	    lastWasEmpty = true;
	}
    }
    
    private void updateSubcodeList() {
	if (subcodeValues.size() <= 1)
	    return;

	Component c = signalCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    String text = ((javax.swing.text.JTextComponent)c).getText();

	    // This isn't so much for redundancy control as for ...
	    // ... if we don't do this then everytime we set the model
	    // for the combo-box te selected item becomes 0. Since the
	    // combo box, through the doc listener, ends up calling isValid
	    // and us on every change no matter what the user selects we'll
	    // end up reverting to the first selection.

	    if ((lastSignal != null) && (lastSignal.equals(text))) {
		return;
	    }
	    lastSignal = text;


	    Vector<String> nw = si_subcodes.subcodesFor(text);
	    if (nw == null) {
		clearOutList();
	    } else {
		subcodeCombo.setModel(new DefaultComboBoxModel(nw));
	    }
	}
    }
}
