/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
