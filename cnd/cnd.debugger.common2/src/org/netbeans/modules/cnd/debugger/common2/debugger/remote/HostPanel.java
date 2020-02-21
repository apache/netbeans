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

package org.netbeans.modules.cnd.debugger.common2.debugger.remote;

import javax.swing.JPanel;
import javax.swing.event.DocumentListener;

import org.openide.util.HelpCtx;


import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.DetailView;
import org.netbeans.modules.cnd.debugger.common2.utils.masterdetail.DummyPropertySheet;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;


public class HostPanel extends DetailView<CustomizableHost> {

    private CustomizableHost original;
    private CustomizableHost editable;

    /* OLD
    private DocumentListener modifiedValidateDocumentListener = null;
    private DocumentListener modifiedRunDirectoryListener = null;

    private String[] platformChoices = null;
    private static int lastPlatformChoice = 0;
    private JLabel platformLabel;
    private javax.swing.JComboBox platformComboBox;
     */
    private final JPanel execControlsPanel = new javax.swing.JPanel();

    private final DummyPropertySheet.Listener listener =
	new DummyPropertySheet.Listener() {
            @Override
	    public void propertyChanged() {
		setDirty(true);
	    }
	};

    private final DummyPropertySheet propertySheet =
	new DummyPropertySheet(Catalog.get("REMOTE_DIALOG_GUIDANCE_TXT"), // NOI18N
			       listener);

    // dirtiness is a property of a model not the view
    // this SHOULD be moved to Host

    // is the implicit model of this view (state of it's fields) dirty?
    private boolean dirty;
    
    private boolean updating;
    
    public HostPanel(CustomizableHost host) {
	if (host != null) {
	    original = host;

	    // xfer rememberPassword information from PassswordManager to original
	    PasswordManager pm = PasswordManager.getInstance();
	    original.setRememberPassword(pm.isRememberPassword(original.executionEnvironment()));
	} else {
	    original = new CustomizableHost();
	}

	editable = original.cloneRecord();

	initComponents();
    }

    private java.awt.GridBagConstraints gridBagConstraints;

    private void initComponents() {
	setLayout(new java.awt.BorderLayout(5, 0));

        execControlsPanel.setLayout(new java.awt.GridBagLayout());
        execControlsPanel.setPreferredSize(new java.awt.Dimension(600, 300));
	execControlsPanel.setBorder(new javax.swing.border.EtchedBorder());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 8, 12);
	execControlsPanel.add(propertySheet, gridBagConstraints);

        add(execControlsPanel, java.awt.BorderLayout.CENTER);
    }

    // implement DetailView
    @Override
    public void setRecord(CustomizableHost newhost) {
        original = newhost;	// switch to new original


	// The below is a "reset" operation: throw away existing editable in
	// favor of original
	if (original != null) {
	    // xfer rememberPassword information from PassswordManager to original
	    PasswordManager pm = PasswordManager.getInstance();
	    original.setRememberPassword(pm.isRememberPassword(original.executionEnvironment()));

	    editable = original.cloneRecord();
	} else {
	    editable = new CustomizableHost();       // "empty" model
	}
	updateView();
	setDirty(false);
    }

    // implement DetailView
    @Override
    public void commit() {
	if (original != null) {
	    // Changes in sheet are directly applied to the editable
	    original.assign(editable);

	    // xfer rememberPassword information to PassswordManager
	    PasswordManager pm = PasswordManager.getInstance();
	    pm.setRememberPassword(original.executionEnvironment(), original.isRememberPassword());
	}
	setDirty(false);
    }

    // implement DetailView
    @Override
    public void updateView() {
	updating = true;

	try {
	    propertySheet.update(editable.getSheet());
	} finally {
	    updating = false;
	}
    }
    
    /* OLD
    private void platformComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedIndex = platformComboBox.getSelectedIndex();
        if (selectedIndex == 0) {
            //host.setPaltform(null);
        } else {
	    ; //host.setPaltform(platformChoices[selectedIndex-1]);
        }

        lastPlatformChoice = selectedIndex;
    }
     */

    public void validateFields(javax.swing.event.DocumentEvent documentEvent) {
	// setModified();
	setDirty(true);
    }

    // ModifiedDocumentListener
    public class ModifiedValidateDocumentListener implements DocumentListener {
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
        
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
            validateFields(documentEvent);
        }
    }

    // implement Validator
    @Override
    public boolean isRecordValid() {
	return true;
    }

    // implement Validator
    @Override
    public boolean isDirty() {
	return dirty || editable.getOptions().isDirty();
    }

    private void setDirty(boolean dirty) {
	if (updating)
	    return;
	if (dirty == false)
	    editable.getOptions().clearDirty();
	this.dirty = dirty;
	fireChanged();
    }

    protected HelpCtx getHelpCtx() {
	return new HelpCtx ("RemoteHost"); // NOI18N
    }
}
