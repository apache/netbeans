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
import java.awt.Component;
import javax.swing.*;
import javax.swing.text.Document;

class ClassMethodBreakpointPanel extends BreakpointPanel {

    private ClassMethodBreakpoint fb;
    
    
    public void seed (NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (ClassMethodBreakpoint) breakpoint;

	String className = fb.getClassName();
	if (className == null) {
	    className = Catalog.get("ClassMethod_AllClass");// NOI18N
	    
	}
	classNameCombo.setSelectedItem(className);

	String methodName = fb.getMethodName();
	if (methodName == null) {
	    methodName = Catalog.get("ClassMethod_AllMethod");// NOI18N
	} 
	methodNameCombo.setSelectedItem(methodName);

	baseToggle.setSelected(fb.isRecurse());
    }

    /*
     * Constructors
     */
    public ClassMethodBreakpointPanel() {
	this(new ClassMethodBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public ClassMethodBreakpointPanel(NativeBreakpoint b) {
	this((ClassMethodBreakpoint) b, true);
    }

    private ClassMethodBreakpointPanel(ClassMethodBreakpoint breakpoint,
				      boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;

	initComponents();
	addCommonComponents(3);

	/** Items in the combo boxes */
	final String[] comboValues1 = {
	    // Don't reorder or insert in the middle here - these indices
	    // are used in optionsNotify() and other places. Grep for usage
	    // of intervalCombo.{get,set}SelectedIndex
	    Catalog.get("ClassMethod_AllClass") // NOI18N
	    // XXX should generate these from the system!
	};
	classNameCombo.setModel(new DefaultComboBoxModel(comboValues1));
	Component c = classNameCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    Document d = ((javax.swing.text.JTextComponent)c).getDocument();
	    d.addDocumentListener(this);
	}
	classNameCombo.setEditable(true);
	c.requestFocus();

	

	final String[] comboValues2 = {
	    // Don't reorder or insert in the middle here - these indices
	    // are used in optionsNotify() and other places. Grep for usage
	    // of intervalCombo.{get,set}SelectedIndex
	    Catalog.get("ClassMethod_AllMethod") // NOI18N
	    // XXX should generate these from the system!
	};
	methodNameCombo.setModel(new DefaultComboBoxModel(comboValues2));
	c = methodNameCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    Document d = ((javax.swing.text.JTextComponent)c).getDocument();
	    d.addDocumentListener(this);
	}
	methodNameCombo.setEditable(true);
	// or should we seed it with the method name here?


	seed(breakpoint);

	propertiesAreValid();	// force adjustment of baseToggle
    }
    
    public void setDescriptionEnabled(boolean enabled) {
	if (enabled) {
	    // OLD baseToggleEnabled = true;
	} else {
	    // classMethodLabel.setEnabled(false);
	    classNameCombo.setEnabled(false);
	    // methodLabel.setEnabled(false);
	    methodNameCombo.setEnabled(false);
	    // OLD baseToggleEnabled = false;
	    baseToggle.setEnabled(false);
	}
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	classMethodLabel = new javax.swing.JLabel();
	classNameCombo = new javax.swing.JComboBox();
	methodLabel = new javax.swing.JLabel();
	methodNameCombo = new javax.swing.JComboBox();
	baseToggle = new javax.swing.JCheckBox();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	classMethodLabel.setText(Catalog.get("Class")); // NOI18N
	classMethodLabel.setDisplayedMnemonic(Catalog.
			 getMnemonic("MNEM_Class")); // NOI18N
	classMethodLabel.setLabelFor(classNameCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(classMethodLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(classNameCombo, gridBagConstraints1);

	baseToggle.setText(Catalog.get("IncludeParentMethods")); // NOI18N
	baseToggle.setMnemonic(Catalog.
		   getMnemonic("MNEM_IncludeParentMethods")); // NOI18N
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 4;
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(baseToggle, gridBagConstraints1);
       
	methodLabel.setText(Catalog.get("Method")); // NOI18N
	methodLabel.setDisplayedMnemonic(Catalog.
		    getMnemonic("MNEM_Method")); // NOI18N
	methodLabel.setLabelFor(methodNameCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 2;
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(methodLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 2;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(methodNameCombo, gridBagConstraints1);

	// a11y
	Catalog.setAccessibleDescription(classNameCombo, "ACSD_Class"); // NOI18N
	Catalog.setAccessibleDescription(methodNameCombo, "ACSD_Method"); // NOI18N
	Catalog.setAccessibleDescription(baseToggle,
					 "ACSD_IncludeParentMethods"); // NOI18N
    }

    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel classMethodLabel;
    private javax.swing.JComboBox classNameCombo;
    private javax.swing.JLabel methodLabel;
    private javax.swing.JComboBox methodNameCombo;
    private javax.swing.JCheckBox baseToggle;
    // OLD private boolean baseToggleEnabled = true;

    
    protected void assignProperties() {
	String cls = classNameCombo.getSelectedItem().toString().trim();
	String mtd = methodNameCombo.getSelectedItem().toString().trim();

	if (cls.equals(
		Catalog.get("ClassMethod_AllClass"))) { // NOI18N
	    cls = null;
	}

	if (cls != null && ! cls.equals(fb.getClassName())) {
	    fb.setClassName(cls);
	    fb.setQclassName(cls);
	}

	/* OLD
	fb.setClassName(cls);
	fb.setQclassName(cls);
	*/
	if (mtd.equals(
		Catalog.get("ClassMethod_AllMethod"))) { // NOI18N
	    mtd = null;
	}

	if (mtd != null && ! mtd.equals(fb.getMethodName())) {
	    fb.setMethodName(mtd);
	    fb.setQmethodName(mtd);
	}

	/* OLD
	fb.setMethodName(mtd);
	fb.setQmethodName(mtd);
	*/
	fb.setRecurse(baseToggle.isSelected());
    }

    private static final String AllClasses =
	Catalog.get("ClassMethod_AllClass");	// NOI18N
    private static final String AllMethods =
	Catalog.get("ClassMethod_AllMethod");	// NOI18N
    
    protected boolean propertiesAreValid() {
	String cls = null;
	String mtd = null;

	Component c = classNameCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    cls = ((javax.swing.text.JTextComponent)c).getText();
	} else {
	    cls = classNameCombo.getSelectedItem().toString().trim();
	}

	
	c = methodNameCombo.getEditor().getEditorComponent();
	if (c instanceof javax.swing.text.JTextComponent) {
	    mtd = ((javax.swing.text.JTextComponent)c).getText();
	} else {
	    mtd = methodNameCombo.getSelectedItem().toString().trim();
	}

	boolean classEmpty = cls == null ||
			     cls.length() == 0 ||
			     cls.equals(AllClasses);

	boolean methodEmpty = mtd == null ||
			      mtd.length() == 0 ||
			      mtd.equals(AllMethods);

	if (!classEmpty && methodEmpty) {
	    baseToggle.setEnabled(true /* OLD && baseToggleEnabled */);
	} else {
	    baseToggle.setEnabled(false /* OLD && baseToggleEnabled */);
	}

	if (classEmpty && methodEmpty)
	    return false;

	return true;
    }
}
