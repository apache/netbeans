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


package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

public class EditMaxFramePanel extends javax.swing.JPanel {

    private JLabel maxFrameLabel = null;
    private JTextField maxFrameText = null;

    private String max_frames ;
    private NativeDebugger debugger;

    public EditMaxFramePanel(NativeDebugger debugger, String initialMaxFrame) {
	if (initialMaxFrame != null) {
	    max_frames = initialMaxFrame;
	}
	this.debugger = debugger;

	initComponents();

	maxFrameText.setText(max_frames);
    }

    private final MaxFrameController controller = new MaxFrameController();

    public Controller getController() {
	return controller;
    }

    public void refocus() {
	if (maxFrameText != null)
	    maxFrameText.requestFocusInWindow();
    }

    private void initComponents() {
	setLayout(new GridBagLayout());
	GridBagConstraints gbc;

	this.setBorder (new EmptyBorder (11, 12, 1, 11));

	maxFrameLabel =
	    new JLabel(Catalog.get("LBL_max_frames"));		// NOI18N
	maxFrameLabel.setDisplayedMnemonic(Catalog.
	    getMnemonic("MNEM_MaxFrames"));			// NOI18N

	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 10);
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 0.0;
	    gbc.weighty = 0.0;

	    add(maxFrameLabel, gbc);
	
	maxFrameText = new JTextField();
	    Catalog.setAccessibleDescription(maxFrameText,
					     "ACSD_MaxFrames");	// NOI18N
	    maxFrameText.setBorder(new CompoundBorder(maxFrameText.getBorder (),
				                   new EmptyBorder (2, 0, 2, 0)));
	    maxFrameText.setColumns(25);
	    maxFrameText.selectAll();

	    gbc = new GridBagConstraints();
	    gbc.gridx = 1;
	    gbc.gridy = 0;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 0);
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 1.0;
	    gbc.weighty = 0.0;
	    gbc.gridwidth = GridBagConstraints.REMAINDER;

	    add(maxFrameText, gbc);

	maxFrameLabel.setLabelFor(maxFrameText);


	// Arrange to revalidate on changes
	maxFrameText.getDocument().addDocumentListener(new DocumentListener() {
            @Override
	    public void changedUpdate(DocumentEvent e) {
		controller.validChanged();
	    }

            @Override
	    public void insertUpdate(DocumentEvent e) {
		controller.validChanged();
	    }

            @Override
	    public void removeUpdate(DocumentEvent e) {
		controller.validChanged();
	    }
	});

	refocus();
    }

    public boolean validateFields() {
	String expr = maxFrameText.getText();
	if (expr.equals(""))					// NOI18N
	    return false;
	else
	    return true;
    }

    private class MaxFrameController implements Controller {

	private final PropertyChangeSupport pcs =
	    new PropertyChangeSupport(this);

	// interface Controller
        @Override
	public boolean ok() {
	    if (!validateFields())
		return false;

	    post();
	    return true;
	}

	// interface Controller
        @Override
	public boolean cancel() {
	    return true;
	}

        // interface Controller
        @Override
        public boolean isValid() {
	    if (IpeUtils.isEmpty(maxFrameText.getText()))
		return false;
	    try {
		int i = Integer.parseInt(maxFrameText.getText());
		if (i < 1)
		    return false;
	    } catch (NumberFormatException e) {
		return false;
	    }
	    return true;
        }

        // interface Controller
        @Override
        final public void addPropertyChangeListener(PropertyChangeListener l) {
	    pcs.addPropertyChangeListener(l);
        }

        // interface Controller
        @Override
        final public void removePropertyChangeListener(PropertyChangeListener l) {
	    pcs.removePropertyChangeListener(l);
        }

	void validChanged() {
	    pcs.firePropertyChange(Controller.PROP_VALID, null, null);
	}
    }

    /**
     * Send the DBX_stack_max_size to dbx
     */
    protected void post() {
	debugger.setOption("DBX_stack_max_size", maxFrameText.getText()); // NOI18N
    }
}
