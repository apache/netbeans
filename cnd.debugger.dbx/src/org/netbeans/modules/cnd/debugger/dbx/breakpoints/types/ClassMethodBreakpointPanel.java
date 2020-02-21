/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
