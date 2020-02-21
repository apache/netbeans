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
import javax.swing.*;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.values.ProcessEvent;

class ProcessBreakpointPanel extends BreakpointPanel {

    private ProcessBreakpoint fb;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel processLabel;
    private javax.swing.JComboBox processCombo;
    private javax.swing.JLabel exitCodeLabel;
    private javax.swing.JTextField exitCodeField;

    // OLD private boolean exitCodeFieldEnabled;
    
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (ProcessBreakpoint) breakpoint;

	ProcessEvent pe = fb.getSubEvent();
	processCombo.setSelectedItem(pe.toString());

	exitCodeField.setText(fb.getExitCode());

	adjustexitcode(pe);
    }

    /*
     * Constructors
     */
    public ProcessBreakpointPanel() {
	this (new ProcessBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public ProcessBreakpointPanel(NativeBreakpoint b) {
	this ((ProcessBreakpoint)b, true);
    }
    
    /** Creates new form ProcessBreakpointPanel */
    public ProcessBreakpointPanel(ProcessBreakpoint breakpoint, 
				  boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;
	initComponents();
	addCommonComponents(1);
	processCombo.setEditable(false);
	
	/** Items in the combo boxes */
	final String[] comboValues = ProcessEvent.getTags();
	processCombo.setModel(new DefaultComboBoxModel(comboValues));

	processCombo.addActionListener(new java.awt.event.ActionListener() {
	    public void actionPerformed(java.awt.event.ActionEvent evt) {
		String value = processCombo.getSelectedItem().toString();
		if (value == null)
		    return;
		ProcessEvent processType = ProcessEvent.byTag(value);
		adjustexitcode(processType);
	    }
	});

	seed(breakpoint);

	// Arrange to revalidate on changes
	exitCodeField.getDocument().addDocumentListener(this);
	processCombo.addItemListener(this);
    }

    public void setDescriptionEnabled(boolean enabled) {
	if (!enabled) {
	    // processLabel.setEnabled(false);
	    processCombo.setEnabled(false);
	    // exitCodeLabel.setEnabled(false);
	    // OLD exitCodeFieldEnabled = false;
	    exitCodeField.setEnabled(false);
	} else {
	    // OLD exitCodeFieldEnabled = true;
	}
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	processLabel = new javax.swing.JLabel();
	processCombo = new javax.swing.JComboBox();
	exitCodeLabel = new javax.swing.JLabel();;
	exitCodeField = new javax.swing.JTextField();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	processLabel.setText(Catalog.get("Event"));	// NOI18N
	processLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_Event"));		// NOI18N
	processLabel.setLabelFor(processCombo);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 5;
	gridBagConstraints1.ipady = 0;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(processLabel, gridBagConstraints1);

	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridwidth = 3;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.NONE;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(processCombo, gridBagConstraints1);
	
	exitCodeLabel.setText(Catalog.get("ExitCode"));	// NOI18N
	exitCodeLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_ExitCode"));	// NOI18N
	exitCodeLabel.setLabelFor(exitCodeField);
	exitCodeLabel.setEnabled(false);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.ipadx = 6;
	gridBagConstraints1.ipady = 0;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(exitCodeLabel, gridBagConstraints1);

	exitCodeField.setColumns(3);
	exitCodeField.setEditable(false);
	exitCodeField.setEnabled(false);
	javax.swing.JPanel exitCodePanel = new javax.swing.JPanel();
	exitCodePanel.setLayout(new java.awt.BorderLayout());
	exitCodePanel.add(exitCodeField, java.awt.BorderLayout.WEST);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 6;
	gridBagConstraints1.gridy = 0;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(exitCodePanel, gridBagConstraints1);

	// a11y
	//exitCodeField.getAccessibleContext().setAccessibleDescription(
	//    Catalog.get("ACSD_ExitCode") // NOI18N
	//);

	processCombo.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Event") // NOI18N
	);
    }

    protected void assignProperties() {
	String selection = processCombo.getSelectedItem().toString();
	ProcessEvent processType = ProcessEvent.byTag(selection);

	fb.setSubEvent(processType);
	if (processType == ProcessEvent.EXIT)
	    fb.setExitCode(exitCodeField.getText());
	adjustexitcode(processType);
    }
    
    protected boolean propertiesAreValid() {

	String selection = processCombo.getSelectedItem().toString();
	if (IpeUtils.isEmpty(selection))
	    return false;
	ProcessEvent processType = ProcessEvent.byTag(selection);

	// If we're an exit event ensure that exit code is unspecified or
	// is a valid number.

	if (processType == ProcessEvent.EXIT) {
	    String exitCodeString = exitCodeField.getText();
	    if (!IpeUtils.isEmpty(exitCodeString)) {
		try {
		    int i = Integer.parseInt (exitCodeString);
		} catch (NumberFormatException e) {
		    return false;
		}
	    }
	}

	return true;
    }
 
    /**
     * Set the enableness of the exitcode component based on the event
     */

    private void adjustexitcode(ProcessEvent processType) {
	// 6485888
	if (processType == ProcessEvent.EXIT) {
	    exitCodeLabel.setEnabled(true);
	    exitCodeField.setEditable(true);
	    exitCodeField.setEnabled(true);
	} else {
	    exitCodeLabel.setEnabled(false);
	    exitCodeField.setEditable(false);
	    exitCodeField.setEnabled(false);
	}
    }
}
