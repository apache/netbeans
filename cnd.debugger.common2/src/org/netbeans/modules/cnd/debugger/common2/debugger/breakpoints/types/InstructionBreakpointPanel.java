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

package org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.types;

import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.BreakpointPanel;
import org.netbeans.modules.cnd.debugger.common2.debugger.breakpoints.NativeBreakpoint;
import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

class InstructionBreakpointPanel extends BreakpointPanel {

    private InstructionBreakpoint fb;
    
    @Override
    public void seed(NativeBreakpoint breakpoint) {
	seedCommonComponents(breakpoint);
	fb = (InstructionBreakpoint) breakpoint;

	addressField.setText(fb.getAddress());
    }

    /*
     * Constructors
     */
    public InstructionBreakpointPanel() {
	this (new InstructionBreakpoint(NativeBreakpoint.TOPLEVEL), false);
    }

    public InstructionBreakpointPanel(NativeBreakpoint b) {
	this ((InstructionBreakpoint)b, true);
    }
    
    /** Creates new form InstructionBreakpointPanel */
    public InstructionBreakpointPanel(InstructionBreakpoint breakpoint,
				      boolean customizing) {
	super(breakpoint, customizing);
	fb = breakpoint;
	initComponents();
	addCommonComponents(2);


	seed(breakpoint);

	// Arrange to revalidate on changes
	addressField.getDocument().addDocumentListener(this);
    }

    @Override
    public void setDescriptionEnabled(boolean enabled) {
	// addressLabel.setEnabled(false);
	addressField.setEnabled(false);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
	addressLabel = new javax.swing.JLabel();
	addressField = new javax.swing.JTextField();

	panel_settings.setLayout(new java.awt.GridBagLayout());
	java.awt.GridBagConstraints gridBagConstraints1;

	addressLabel.setText(Catalog.get("AddressLabel"));	// NOI18N
	addressLabel.setDisplayedMnemonic(
	    Catalog.getMnemonic("MNEM_AddressLabel"));		// NOI18N
	addressLabel.setLabelFor(addressField);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 0;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	panel_settings.add(addressLabel, gridBagConstraints1);

	addressField.setColumns(12);
	gridBagConstraints1 = new java.awt.GridBagConstraints();
	gridBagConstraints1.gridx = 1;
	gridBagConstraints1.gridy = 1;
	gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
	gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
	gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
	gridBagConstraints1.weightx = 1.0;
	panel_settings.add(addressField, gridBagConstraints1);

	// a11y
	addressField.getAccessibleContext().setAccessibleDescription(
	    Catalog.get("ACSD_Address") // NOI18N
	);

    }

    private javax.swing.JLabel addressLabel;
    private javax.swing.JTextField addressField;

    @Override
    protected void assignProperties() {
	String address = addressField.getText ();
	fb.setAddress(address);
    }
    
    @Override
    protected boolean propertiesAreValid() {
	return ! IpeUtils.isEmpty(addressField.getText());
    }
}
