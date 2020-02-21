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

import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointPanel;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

class ObjectBreakpointPanel extends BreakpointPanel {

    private ObjectBreakpoint fb;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (ObjectBreakpoint) breakpoint;

	objectText.setText(fb.getObject());
	baseToggle.setSelected(fb.isRecurse());
    }

    /*
     * Constructors
     */
    public ObjectBreakpointPanel() {
	this (new ObjectBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public ObjectBreakpointPanel(NativeBreakpoint b) {
	this ((ObjectBreakpoint)b, true);
    }


    /** Creates new form ObjectBreakpointPanel */
    public ObjectBreakpointPanel(ObjectBreakpoint breakpoint,
				 boolean customizing ) {
	super(breakpoint, customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(2);

	if (!customizing) {
	    String selection = EditorBridge.getCurrentSelection();
	    if (selection != null)
		breakpoint.setObject(selection);
	}

	seed(breakpoint);

	// Arrange to revalidate on changes
	objectText.getDocument().addDocumentListener(this);
    }
    
    public void setDescriptionEnabled(boolean enabled) {
	// objectLabel.setEnabled(false);
	objectText.setEnabled(false);
	baseToggle.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	objectLabel = new javax.swing.JLabel();
	objectText = new javax.swing.JTextField();
	baseToggle = new javax.swing.JCheckBox();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	objectLabel.setText(Catalog.get("AllObjMethods"));	// NOI18N
	objectLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_AllObjMethods"));		// NOI18N
	objectLabel.setLabelFor(objectText);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(objectLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(objectText, gridBagConstraints1);

	baseToggle.setText(Catalog.get("IncludeParentClasses"));// NOI18N
	baseToggle.setMnemonic(
	    Catalog.getMnemonic("MNEM_IncludeParentClasses"));	// NOI18N
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 4;
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(baseToggle, gridBagConstraints1);

	// a11y
	objectText.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Object") // NOI18N
	);
	baseToggle.getAccessibleContext().setAccessibleDescription(
	    baseToggle.getText()
	);
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel objectLabel;
    private javax.swing.JTextField objectText;
    private javax.swing.JCheckBox baseToggle;

    protected void assignProperties() {
	fb.setObject(objectText.getText());
	fb.setRecurse(baseToggle.isSelected());
    }
    
    protected boolean propertiesAreValid() {
	if (IpeUtils.isEmpty(objectText.getText())) {
	    return false;
	}
	return true;
    }
}
