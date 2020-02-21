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


package org.netbeans.modules.cnd.debugger.common2.debugger.actions;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

public class EditMaxObjectPanel extends javax.swing.JPanel {

    private JLabel maxObjectLabel = null;
    private JTextField maxObjectText = null;

    private String max_object_size ;
    private NativeDebugger debugger;

    public EditMaxObjectPanel(NativeDebugger debugger, String initialMaxObject) {
	if (initialMaxObject != null) {
	    max_object_size = initialMaxObject;
	}
	this.debugger = debugger;

	initComponents();

	maxObjectText.setText(max_object_size);
    }

    private final MaxObjectController controller = new MaxObjectController();

    public Controller getController() {
	return controller;
    }

    public void refocus() {
	if (maxObjectText != null)
	    maxObjectText.requestFocusInWindow();
    }

    private void initComponents() {
	setLayout(new GridBagLayout());
	GridBagConstraints gbc;

	this.setBorder (new EmptyBorder (11, 12, 1, 11));

	maxObjectLabel =
	    new JLabel(Catalog.get("LBL_max_object_size"));	// NOI18N
	maxObjectLabel.setDisplayedMnemonic(Catalog.
	    getMnemonic("MNEM_MaxObjectSize"));			// NOI18N

	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 10);
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 0.0;
	    gbc.weighty = 0.0;

	    add(maxObjectLabel, gbc);
	
	maxObjectText = new JTextField();
	    Catalog.setAccessibleDescription(maxObjectText,
					     "ACSD_MaxObjectSize");	//NOI18N
	    maxObjectText.setBorder(new CompoundBorder(maxObjectText.getBorder (),
				                   new EmptyBorder (2, 0, 2, 0)));
	    maxObjectText.setColumns(25);
	    maxObjectText.selectAll();

	    gbc = new GridBagConstraints();
	    gbc.gridx = 1;
	    gbc.gridy = 0;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 0);
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 1.0;
	    gbc.weighty = 0.0;
	    gbc.gridwidth = GridBagConstraints.REMAINDER;

	    add(maxObjectText, gbc);

	maxObjectLabel.setLabelFor(maxObjectText);

	// Arrange to revalidate on changes
	maxObjectText.getDocument().addDocumentListener(new DocumentListener() {
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
	String expr = maxObjectText.getText();
	if (IpeUtils.isEmpty(expr))
	    return false;
	else
	    return true;
    }

    private class MaxObjectController implements Controller {
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
	    if (IpeUtils.isEmpty(maxObjectText.getText()))
		return false;
	    try {
		int i = Integer.parseInt(maxObjectText.getText());
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
        final public void removePropertyChangeListener(PropertyChangeListener l)
 {
	    pcs.removePropertyChangeListener(l);
        }

	void validChanged() {
	    pcs.firePropertyChange(Controller.PROP_VALID, null, null);
	}
    }

    /**
     * Send the DBX_max_object_size to dbx
     */
    protected void post() {
	debugger.setOption("DBX_output_max_object_size", maxObjectText.getText()); // NOI18N
    }
}
