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
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;

/**
 * @deprecated Use the same class from common instead
 */
@Deprecated
class VariableBreakpointPanel extends BreakpointPanel {

    private VariableBreakpoint fb;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (VariableBreakpoint) breakpoint;

	variableText.setText(fb.getVariable());
    }

    /*
     * Constructors
     */
    public VariableBreakpointPanel() {
	this (new VariableBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public VariableBreakpointPanel(NativeBreakpoint b) {
	this ((VariableBreakpoint)b, true);
    }
    
    /** Creates new form VariableBreakpointPanel */
    public VariableBreakpointPanel(VariableBreakpoint breakpoint,
				    boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(1);

	if (!customizing) {
	    String selection = EditorBridge.getCurrentSelection();
	    if (selection != null)
		breakpoint.setVariable(selection);
	}

	seed(breakpoint);

	// Arrange to revalidate on changes
	variableText.getDocument().addDocumentListener(this);
    }
    
    public void setDescriptionEnabled(boolean enabled) {
	// variableLabel.setEnabled(false);
	variableText.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	variableLabel = new javax.swing.JLabel();
	variableText = new javax.swing.JTextField();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	variableLabel.setText(Catalog.get("Variable"));	// NOI18N
	variableLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Variable"));	// NOI18N
	variableLabel.setLabelFor(variableText);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(variableLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(variableText, gridBagConstraints1);

	// a11y
	variableText.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Variable") // NOI18N
	);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel variableLabel;
    private javax.swing.JTextField variableText;

    protected void assignProperties() {
	fb.setVariable(variableText.getText());
    }
    
    protected boolean propertiesAreValid() {
	return ! IpeUtils.isEmpty(variableText.getText());
    }
}
