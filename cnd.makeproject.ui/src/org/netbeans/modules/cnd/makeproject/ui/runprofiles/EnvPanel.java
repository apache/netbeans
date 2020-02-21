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

package org.netbeans.modules.cnd.makeproject.ui.runprofiles;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class EnvPanel extends javax.swing.JPanel implements HelpCtx.Provider, PropertyChangeListener, ListSelectionListener {
    private RunProfile currentProfile;

    private ListTableModel envvarModel = null;
    private JTable envvarTable = null;

    private final Env env;
    private final PropertyEditorSupport editor;
    
    /** Creates new form EnvPanel */
    public EnvPanel(Env env, PropertyEditorSupport editor, PropertyEnv propenv) {
        initComponents();
	this.env = env;
        this.editor = editor;
	envvarScrollPane.getViewport().setBackground(java.awt.Color.WHITE);

	// Environment Variables
	envvarModel = new ListTableModel(getString("EnvName"), getString("EnvValue"));
	envvarTable = new JTable(envvarModel);
	envvarModel.setTable(envvarTable);
	envvarScrollPane.setViewportView(envvarTable);

	initValues(env);
        
        propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
        propenv.addPropertyChangeListener(this);
        
        // Accessibility
        environmentLabel.setLabelFor(envvarTable);
        envvarTable.getAccessibleContext().setAccessibleDescription(getString("ACSD_ENV_VAR_TABLE"));   
        addButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_ADD_BUTTON"));
        removeButton.getAccessibleContext().setAccessibleDescription(getString("ACSD_REMOVE_BUTTON"));
        
        envvarTable.getSelectionModel().addListSelectionListener(this);
        
        validateButtons();
    }
    
    private void validateButtons() {                                         
	int[] selRows = envvarTable.getSelectedRows();
        removeButton.setEnabled(envvarModel.getRowCount() > 0 && selRows != null && selRows.length > 0);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        validateButtons();
    }

    public void initValues(Env env) {
	// Environment variables
	String[][] envvars = env.getenvAsPairs();
	if (envvars != null) {
	    int n = envvars.length;
	    ArrayList<String> col0 = new ArrayList<>(n+3); // Leave slop for inserts
	    ArrayList<String> col1 = new ArrayList<>(n+3);
	    for (int i = 0; i < n; i++) {
		col0.add(envvars[i][0]);
		col1.add(envvars[i][1]);
	    }
	    envvarModel.setData(n, col0, col1);
	}

	initFocus();
    }

    public void initFocus() {
    }

    @Override
    public HelpCtx getHelpCtx() {
	return new HelpCtx("Environment"); // NOI18N
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        environmentPanel = new javax.swing.JPanel();
        environmentLabel = new javax.swing.JLabel();
        envvarScrollPane = new javax.swing.JScrollPane();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setLayout(new java.awt.GridBagLayout());

        environmentPanel.setLayout(new java.awt.GridBagLayout());

        environmentLabel.setLabelFor(envvarScrollPane);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/runprofiles/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(environmentLabel, bundle.getString("ENVIRONMENT_LBL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        environmentPanel.add(environmentLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        environmentPanel.add(envvarScrollPane, gridBagConstraints);

        buttonPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addButton, bundle.getString("ADD_BUTTON")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        buttonPanel.add(addButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, bundle.getString("REMOVE_BUTTON")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        buttonPanel.add(removeButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 0);
        environmentPanel.add(buttonPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 12, 0, 12);
        add(environmentPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
	int[] selRows = envvarTable.getSelectedRows();
	if ((selRows != null) && (selRows.length > 0)) {
	    envvarModel.removeRows(selRows);
	}
        validateButtons();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
	envvarModel.addRow();
        validateButtons();
    }//GEN-LAST:event_addButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel environmentLabel;
    private javax.swing.JPanel environmentPanel;
    private javax.swing.JScrollPane envvarScrollPane;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables

    private Object getPropertyValue() throws IllegalStateException {
	env.removeAll();
	int numRows = envvarModel.getRowCount();
	if (numRows > 0) {
	    for (int j = 0; j < numRows; j++) {
		String name = ((String)envvarModel.getValueAt(j, 0)).trim();
		if (name.length() == 0)
		    continue;
		String value = ((String)envvarModel.getValueAt(j, 1)).trim();
		env.putenv(name, value);
	    }
	}
	return env;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
            editor.setValue(getPropertyValue());
        }
    }

    private String getString(String s) {
	return NbBundle.getMessage(EnvPanel.class, s);
    }
}
