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

class CondBreakpointPanel extends BreakpointPanel {

    private CondBreakpoint fb;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (CondBreakpoint) breakpoint;

	condText.setText(fb.getCond());
    }

    /*
     * Constructors
     */
    public CondBreakpointPanel() {
	this (new CondBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public CondBreakpointPanel(NativeBreakpoint b) {
	this ((CondBreakpoint)b, true);
    }

    /** Creates new form CondBreakpointPanel */
    public CondBreakpointPanel(CondBreakpoint breakpoint,
			       boolean customizing ) {
	super(breakpoint, customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(1);

	if (!customizing) {
	    String selection = EditorBridge.getCurrentSelection();
	    if (selection != null)
		breakpoint.setCond(selection);
	}
	seed(breakpoint);


	// Arrange to revalidate on changes
	condText.getDocument().addDocumentListener(this);
    }
    
    public void setDescriptionEnabled(boolean enabled) {
	// condLabel.setEnabled(false);
	condText.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	condLabel = new javax.swing.JLabel();
	condText = new javax.swing.JTextField();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	condLabel.setText(Catalog.get("Expression")); // NOI18N
	condLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Expression")); // NOI18N
	condLabel.setLabelFor(condText);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(condLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(condText, gridBagConstraints1);

	// a11y
	condText.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Expression") // NOI18N
	);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel condLabel;
    private javax.swing.JTextField condText;

    protected void assignProperties() {
	fb.setCond(condText.getText());
    }
    
    protected boolean propertiesAreValid() {
	if (IpeUtils.isEmpty(condText.getText())) {
	    return false;
	}
	return true;
    }
}
