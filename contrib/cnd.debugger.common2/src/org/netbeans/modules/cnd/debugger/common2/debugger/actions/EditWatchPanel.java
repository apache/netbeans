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
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.border.*;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;

import org.openide.*;

import org.netbeans.api.debugger.Watch;

import org.netbeans.spi.debugger.ui.Controller;

import org.netbeans.modules.cnd.debugger.common2.utils.IpeUtils;

import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebuggerManager;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeDebugger;
import org.netbeans.modules.cnd.debugger.common2.debugger.EditorBridge;
import org.netbeans.modules.cnd.debugger.common2.debugger.NativeWatch;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.filesystems.FileObject;


/**
 * Visual layout partly influenced by
 * ui/src/org/netbeans/modules/debugger/jpda/ui/WatchPanel.java
 *
 * class design is a merge of breakpoints.BreakpointPanel and
 * breakpoint type-specific panels.
 *
 * dbx cannot "edit" watches (display items) yet, but we write this panel
 * as an editor for LATER.
 */

public class EditWatchPanel extends javax.swing.JPanel
    implements NativeDebugger.QualifiedExprListener {

    private NativeDebugger debugger;

    private JLabel watchLabel = null;
    private JTextComponent watchText = null;

    private JLabel qWatchLabel = null;
    private JTextField qWatchText = null;

    private JLabel useQLabel = null;
//    private JCheckBox useQCheckBox = null;

    private JRadioButton restrictedRB;
    private JRadioButton unrestrictedRB;
    private JLabel scopeLabel;
    private JTextField scopeText = null;


    private Watch watch = null;
    private NativeWatch nativeWatch = null;
    private boolean customizing = false;

    private final DocumentListener documentListener = new DocumentListener() {
        @Override
	public void changedUpdate(DocumentEvent e) {
	    checkValid();
	}
        @Override
	public void insertUpdate(DocumentEvent e) {
	    checkValid();
	}
        @Override
	public void removeUpdate(DocumentEvent e) {
	    checkValid();
	}
    };

    public EditWatchPanel(NativeDebugger debugger,
			  NativeWatch initialNativeWatch,
			  String scope,
			  Watch initialWatch) {

	this.debugger = debugger;

	if (initialNativeWatch != null) {
	    nativeWatch = initialNativeWatch;
	    customizing = true;

	} else if (initialWatch != null) {
	    watch = initialWatch;
	    customizing = true;

	} else {
	    // Watch will be created on OK
	}

	initComponents();

	seed(nativeWatch, watch);
    }

    public final void seed(NativeWatch nativeWatch, Watch watch) {
	watchText.setText(EditorBridge.getCurrentSelection());
        watchText.selectAll();
    }

    private final WatchController controller = new WatchController(this);

    public Controller getController() {
	return controller;
    }

    public void refocus() {
	if (watchText != null)
	    watchText.requestFocusInWindow();
    }

    private void initComponents() {
	Catalog.setAccessibleDescription(this, "ACSD_NewWatch"); // NOI18N

	setLayout(new GridBagLayout());
	GridBagConstraints gbc;

	this.setBorder (new EmptyBorder (11, 12, 1, 11));

	watchLabel = new JLabel(Catalog.get("LBL_WatchExpression"));// NOI18N

	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 10);
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 0.0;
	    gbc.weighty = 0.0;

	    add(watchLabel, gbc);

	//Add JEditorPane and context
        JComponent [] editorComponents = createEditorComponent();

	watchText = (JTextComponent) editorComponents[1];
        watchText.setBorder(new CompoundBorder(watchText.getBorder (),
				                   new EmptyBorder (2, 0, 2, 0)));

	    gbc = new GridBagConstraints();
	    gbc.gridx = 1;
	    gbc.gridy = 0;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 0);
	    gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 1.0;
	    gbc.weighty = 0.0;
	    gbc.gridwidth = GridBagConstraints.REMAINDER;

	    add(editorComponents[0], gbc);
            
	Catalog.setAccessibleDescription(watchText, "ACSD_WatchExpression"); // NOI18N
	watchLabel.setDisplayedMnemonic
	    (Catalog.getMnemonic("MNEM_WatchExpression")); // NOI18N
	watchLabel.setLabelFor(watchText);


	qWatchLabel = new JLabel(Catalog.get("LBL_QForm"));	// NOI18N
	    gbc = new GridBagConstraints();
	    gbc.gridx = 0;
	    gbc.gridy = 1;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 10);
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 0.0;
	    gbc.weighty = 0.0;

	    add(qWatchLabel, gbc);
	
	qWatchText = new JTextField();
	    qWatchText.setBorder(new CompoundBorder(qWatchText.getBorder (),
				                   new EmptyBorder (2, 0, 2, 0)));
	    qWatchText.setEditable(false);
            qWatchText.setEnabled(false);
	    qWatchText.setColumns(25);
	    qWatchText.selectAll();

	    gbc = new GridBagConstraints();
	    gbc.gridx = 1;
	    gbc.gridy = 1;
	    gbc.insets = new java.awt.Insets(0, 0, 5, 0);
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
	    gbc.anchor = java.awt.GridBagConstraints.WEST;
	    gbc.weightx = 1.0;
	    gbc.weighty = 0.0;
	    gbc.gridwidth = GridBagConstraints.REMAINDER;

	    add(qWatchText, gbc);

	qWatchLabel.setLabelFor(qWatchText);




//	useQLabel = new JLabel(Catalog.get("LBL_UseQForm"));// NOI18N
//	    gbc = new GridBagConstraints();
//	    gbc.gridx = 0;
//	    gbc.gridy = 2;
//	    gbc.insets = new java.awt.Insets(0, 0, 5, 10);
//	    gbc.anchor = java.awt.GridBagConstraints.WEST;
//	    gbc.weightx = 0.0;
//	    gbc.weighty = 0.0;

	    // TMP add(useQLabel, gbc);
	
//	useQCheckBox = new JCheckBox(Catalog.get("LBL_UseQForm"));
//	    gbc = new GridBagConstraints();
//	    gbc.gridx = 1;
//	    gbc.gridy = 2;
//	    gbc.insets = new java.awt.Insets(0, 0, 5, 0);
//	    gbc.anchor = java.awt.GridBagConstraints.WEST;
//	    gbc.weightx = 1.0;
//	    gbc.weighty = 0.0;
//	    gbc.gridwidth = GridBagConstraints.REMAINDER;
//
//	    add(useQCheckBox, gbc);
//
//	useQCheckBox.setEnabled(false);
//	useQCheckBox.setFocusable(false);
//	useQCheckBox.setMnemonic(Catalog.getMnemonic("MNEM_UseQForm"));
//	Catalog.setAccessibleDescription(useQCheckBox, "ACSD_UseQForm");
	/* OLD
	useQLabel.setDisplayedMnemonic(Catalog.getMnemonic("MNEM_UseQForm"));
	useQLabel.setLabelFor(useQCheckBox);
	*/

	watchText.getDocument().addDocumentListener(documentListener);
	refocus();
    }

    protected boolean badField(String err) {
	Exception ex = new IllegalArgumentException();
	ErrorManager.getDefault().annotate(ex, err);
	ErrorManager.getDefault().notify(ErrorManager.USER, ex);
	return false;
    }

    public boolean validateFields() {
	String expr = watchText.getText().trim();
	if (expr.equals(""))					// NOI18N
	    return badField(Catalog.get("MSG_EMPTY_WATCH"));	// NOI18N
	return true;
    }

    private class WatchController implements Controller {
	private final EditWatchPanel owner;

	private final PropertyChangeSupport pcs =
	    new PropertyChangeSupport(this);

	WatchController(EditWatchPanel owner) {
	    this.owner = owner;
	}

	// interface Controller
        @Override
	public boolean ok() {
	    if (!validateFields())
		return false;

	    // point of no return
	    NativeDebuggerManager manager = NativeDebuggerManager.get();
	    String expr = watchText.getText();
//	    if (useQCheckBox.isEnabled() && useQCheckBox.isSelected())
//		expr = qWatchText.getText();
//	    else
//		expr = watchText.getText();
	    manager.createWatch(expr.trim());
	    // we get control back in DbxDebuggerManager.watchAdded

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
	    if (Log.Watch.dialog)
		System.out.printf("EditWatchPanel.isValid()\n"); // NOI18N
	    String expr = watchText.getText().trim();

	    if (debugger != null)
		debugger.postExprQualify(expr, EditWatchPanel.this);

	    if (IpeUtils.isEmpty(expr))
		return false;
	    else {
		return true;
	    }
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

        private void validChanged() {
            pcs.firePropertyChange(Controller.PROP_VALID, null, null);
        }

    }

    // interface QualifiedExprListener
    @Override
    public void qualifiedExpr(String qualifiedForm, boolean ok) {
	if (Log.Watch.dialog)
	    System.out.printf("qualifiedExpr('%s')\n", qualifiedForm); // NOI18N

	if (watchText.getText().trim().equals(""))
	    qWatchText.setText("");
	else
	    qWatchText.setText(qualifiedForm);

//	useQCheckBox.setEnabled(ok);
//	useQCheckBox.setFocusable(ok);
//	useQCheckBox.setFocusPainted(true);
    }

    private void checkValid() {
	if (Log.Watch.dialog)
	    System.out.printf("EditWatchPanel.checkValid()\n"); // NOI18N
	// If you run into trouble try and do it like
	// breakpoints.BreakpointPanel

	// Will come back via isValid() ...
	// OLD firePropertyChange(Controller.PROP_VALID, null, null);
	controller.validChanged();
    }
    
    public static JComponent[] createEditorComponent() {
        FileObject file = EditorContextDispatcher.getDefault().getMostRecentFile();
        int line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();
        
        // Add JEditorPane and context
        // There is no need to define the mimetype here!
        JComponent[] editorComponents = Utilities.createSingleLineEditor("text/plain"); // NOI18N

	JTextComponent textComponent = (JTextComponent) editorComponents[1];
        if (file != null && line >= 0) {
            DialogBinding.bindComponentToFile(file, line, 0, 0, textComponent);
        }
        
        return editorComponents;
    }
}
