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

package org.netbeans.modules.cnd.debugger.common2.utils.masterdetail;

import java.awt.GridBagConstraints;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.JDialog;
import javax.accessibility.AccessibleContext;

import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;


/**
 *
 */
public class MasterDetailView extends TopComponent {

    public static interface Actions {
	void handleCommit();
	void handleClose();
    }

    private final Actions actions;
    private final int button_config;
    private final String okActionName;

    public static final int WITHLIST = (1<<2);
    public static final int CONFIGCURRENT = (1<<3);


    private MasterView masterView;
    private DetailView detailView;

    private Validator validator = new Validator() {
        @Override
	public void fireChanged() {
	    if (Log.MasterDetail.debug)
		System.out.printf("MDV.fireChanged() ...\n"); // NOI18N
	    boolean valid = isValid();
	    boolean dirty = isDirty();

	    applyButton.setEnabled(true);
	    okButton.setEnabled(true);
	}

        @Override
	public boolean isRecordValid() {
	    boolean valid = true;
	    if (masterView != null) {
		boolean v = masterView.isValid();
		if (Log.MasterDetail.debug)
		    System.out.printf("masterView.isValid() -> %s\n", v); // NOI18N
		valid &= v;
	    }
	    if (detailView != null) {
		boolean v = detailView.isValid();
		if (Log.MasterDetail.debug)
		    System.out.printf("detailView.isValid() -> %s\n", v); // NOI18N
		valid &= v;
	    }
	    return valid;
	}

        @Override
	public boolean isDirty() {
	    boolean dirty = false;
	    if (masterView != null) {
		boolean d = masterView.isDirty();
		if (Log.MasterDetail.debug)
		    System.out.printf("masterView.isDirty() -> %s\n", d); // NOI18N
		dirty |= d;
	    }
	    if (detailView != null) {
		boolean d = detailView.isDirty();
		if (Log.MasterDetail.debug)
		    System.out.printf("detailView.isDirty() -> %s\n", d); // NOI18N
		dirty |= d;
	    }
	    return dirty;
	}
    };

    public MasterDetailView(Actions actions,
			    int button_config,
			    String okActionName) {

	this.actions = actions;
	this.button_config = button_config;
	this.okActionName = okActionName;
        
        initComponents();
        
	resetButton.setEnabled(false);
	updateButton.setEnabled(false);

	initAccessibility();

	helpButton.setEnabled(getHelpCtx() != null);
    }

    public void addMasterView(MasterView masterView) {
	assert this.masterView == null :
	    "Can only add masterView to MasterDetailView once"; // NOI18N
	this.masterView = masterView;


	// master at gridy of 2
	// detail at gridy of 3
	// buttons at gridy of 4
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gbc.anchor = java.awt.GridBagConstraints.NORTH;
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

	this.add(masterView, gbc);

	masterView.setValidator(validator);
	validator.fireChanged();
    }

    public void addDetailView(DetailView detailView) {
	assert this.detailView == null :
	    "Can only add detailView to MasterDetailView once"; // NOI18N
	this.detailView = detailView;

	// master at gridy of 2
	// detail at gridy of 3
	// buttons at gridy of 4
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = java.awt.GridBagConstraints.BOTH;
        gbc.anchor = java.awt.GridBagConstraints.SOUTH;
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);

	this.add(detailView, gbc);

	detailView.setValidator(validator);
	validator.fireChanged();
    }

    private void initAccessibility() {
	AccessibleContext context;

	context = getAccessibleContext();
	//context.setAccessibleName(getTitle());

	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	context.setAccessibleName(Catalog.get("LISTEDITDIALOG_ACSN")); // NOI18N
	context.setAccessibleDescription(Catalog.get("LISTEDITDIALOG_ACSD")); // NOI18N

	resetButton.getAccessibleContext().setAccessibleDescription(resetButton.getText());
	applyButton.getAccessibleContext().setAccessibleDescription(applyButton.getText());
	okButton.getAccessibleContext().setAccessibleDescription(okButton.getText());
	cancelButton.getAccessibleContext().setAccessibleDescription(cancelButton.getText());
	helpButton.getAccessibleContext().setAccessibleDescription(helpButton.getText());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        updateButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        buttonPanel = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();

        this.setLayout(new java.awt.GridBagLayout());

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));

	buttonPanel.add(okButton);
	if (okActionName != null)
	    okButton.setText(okActionName);
	else
	    okButton.setText(Catalog.get("LISTEDITDIALOG_OK_BUTTON_TXT")); // NOI18N

        okButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
		okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText(Catalog.get("LISTEDITDIALOG_CANCEL_BUTTON_TXT")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(cancelButton);
	
        applyButton.setText(Catalog.get("LISTEDITDIALOG_APPLY_BUTTON_TXT")); // NOI18N
	applyButton.setMnemonic(Catalog.getMnemonic("MNEM_Apply")); // NOI18N
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

	if ((button_config & WITHLIST) != 0 ||
	    (button_config & CONFIGCURRENT) != 0)
	    buttonPanel.add(applyButton);

        helpButton.setMnemonic(Catalog.get("LISTEDITDIALOG_HELP_BUTTON_MN").charAt(0)); // NOI18N
        helpButton.setText(Catalog.get("LISTEDITDIALOG_HELP_BUTTON_TXT")); // NOI18N
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(helpButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 12, 10);
        this.add(buttonPanel, gridBagConstraints);
    }

    private final static String ESCAPE_COMMAND = "Escape";	// NOI18N
    private final static String ENTER_COMMAND = "Enter";	// NOI18N

    public void installInDialog(JDialog dialog) {

	JRootPane rp = dialog.getRootPane();

	// Accept on ENTER key
	rp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), ENTER_COMMAND);
	rp.getActionMap().put(ENTER_COMMAND, new AbstractAction() {
            @Override
	    public void actionPerformed(ActionEvent e) {
		okButtonActionPerformed(e);
	    }
	});

	// Cancel on ESC key
	rp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).
	    put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), ESCAPE_COMMAND);
	rp.getActionMap().put(ESCAPE_COMMAND, new AbstractAction() {
            @Override
	    public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed(e);
	    }
	});
    }

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {
	doCommit();
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
	handleClose();
    }

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {
        HelpCtx help = getHelpCtx();
        if (help != null) {
            help.display();
        }
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
	doCommit();
	handleClose();
    }

    private void doCommit() {

	if (masterView != null) {
	    // Full master/detail mode
	    masterView.commitPending();
	    masterView.updateView();
	    handleCommit();
	    masterView.setDirty(false);

	} else {
	    // detail-only mode
	    detailView.commit();
	    handleCommit();
	}
    }

    /**
     * Either override this method or register an Actions.
     */
    protected void handleCommit() {
	if (actions != null)
	    actions.handleCommit();
    }

    /**
     * Either override this method or register an Actions.
     */
    protected void handleClose() {
	if (actions != null)
	    actions.handleClose();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;

    private javax.swing.JButton okButton;
    private javax.swing.JButton applyButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton helpButton;

    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables


    /* Should be overridden */
    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }

    private static class EscapeKeyAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent evt) {
        }
    }
}
