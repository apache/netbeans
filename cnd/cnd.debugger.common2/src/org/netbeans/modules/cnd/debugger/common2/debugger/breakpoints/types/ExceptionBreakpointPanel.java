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

import java.awt.Component;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.values.ExceptionSpec;

class ExceptionBreakpointPanel extends BreakpointPanel {

    private ExceptionBreakpoint fb;
    
    @Override
    protected final void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (ExceptionBreakpoint) breakpoint;

	exceptionCombo.setSelectedItem(fb.getException().toString());
    }

    /*
     * Constructors
     */
    public ExceptionBreakpointPanel() {
	this (new ExceptionBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public ExceptionBreakpointPanel(NativeBreakpoint b) {
	this ((ExceptionBreakpoint)b, true);
    }

    /** Creates new form ExceptionBreakpointPanel */
    public ExceptionBreakpointPanel(ExceptionBreakpoint breakpoint,
				    boolean customizing ) {
	super(breakpoint, customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(1);

	final String[] comboValues = ExceptionSpec.getTags();
	exceptionCombo.setModel(new DefaultComboBoxModel(comboValues));
	exceptionCombo.setEditable(true);
	Component c = exceptionCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    javax.swing.text.Document d =
		((javax.swing.text.JTextComponent)c).getDocument();
	    d.addDocumentListener(this);
	}
	exceptionCombo.setSelectedIndex(0);

	seed(breakpoint);

	// Arrange to revalidate on changes
	exceptionCombo.addItemListener(this);
    }
    
    @Override
    public void setDescriptionEnabled(boolean enabled) {
	// exceptionLabel.setEnabled(false);
	exceptionCombo.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	exceptionLabel = new javax.swing.JLabel();
	exceptionCombo = new javax.swing.JComboBox();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	exceptionLabel.setText(Catalog.get("Type"));	// NOI18N
	exceptionLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Type"));		// NOI18N
	exceptionLabel.setLabelFor(exceptionCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(exceptionLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(exceptionCombo, gridBagConstraints1);

	// a11y
	exceptionCombo.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Type") // NOI18N
	);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel exceptionLabel;
    private javax.swing.JComboBox exceptionCombo;

    @Override
    protected void assignProperties() {
	String xs = exceptionCombo.getSelectedItem().toString();
	ExceptionSpec x = ExceptionSpec.byTag(xs);
	fb.setException(x);
    }
    
    @Override
    protected boolean propertiesAreValid() {
	return !IpeUtils.isEmpty(exceptionCombo.getSelectedItem().toString());
    }
}
