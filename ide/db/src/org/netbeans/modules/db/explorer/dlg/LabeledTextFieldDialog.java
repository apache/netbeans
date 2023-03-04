/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.explorer.dlg;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Rob Englander
 */
public class LabeledTextFieldDialog extends javax.swing.JPanel {
    
    /** Creates new form LabeledTextFieldDialog */
    public LabeledTextFieldDialog(String notes) 
    {
        String title = NbBundle.getMessage (LabeledTextFieldDialog.class, "RecreateTableRenameTable"); // NOI18N
        String lab = NbBundle.getMessage (LabeledTextFieldDialog.class, "RecreateTableNewName"); // NOI18N
        original_notes = notes;
        
        initComponents();
        
        try
        {
            Mnemonics.setLocalizedText(titleLabel, lab);
            titleLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_RecreateTableNewNameA11yDesc"));  // NOI18N

            Mnemonics.setLocalizedText(descLabel, NbBundle.getMessage (LabeledTextFieldDialog.class, "RecreateTableRenameNotes")); // NOI18N
            descLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_RecreateTableRenameNotesA11yDesc"));  // NOI18N
            Mnemonics.setLocalizedText(editButton, NbBundle.getMessage (LabeledTextFieldDialog.class, "EditCommand")); // NOI18N
            editButton.setToolTipText(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_EditCommandA11yDesc"));  // NOI18N

            updateState();

            ActionListener listener = new ActionListener() 
            {
                public void actionPerformed(ActionEvent event) 
                {
                    result = event.getSource() == DialogDescriptor.OK_OPTION;
                }
            };

            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_RecreateTableDialogA11yDesc")); // NOI18N

            DialogDescriptor descriptor = new DialogDescriptor(this, title, true, listener);
            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setResizable(true);
        }
        catch (MissingResourceException e)
        {
            e.printStackTrace();
        }

    }
    
    public boolean run() 
    {
        if (dialog != null)
        {
            dialog.setVisible(true);
        }
        
        return result;
    }

    public String getStringValue() 
    {
        return textField.getText();
    }

    public String getEditedCommand() 
    {
        return notesArea.getText();
    }

    public boolean isEditable() 
    {
        return notesArea.isEditable();
    }

    public void setStringValue(String val) 
    {
        textField.setText(val);
    }

    public void setErrors(String errors) {
        if (errors != null) {
            errorTextPane.setText(errors);
            errorScrollPane.setVisible(true);
        } else {
            errorScrollPane.setVisible(false);
        }
    }
    
    private void updateState()
    {
        isEditMode = !isEditMode;
        
        if (isEditMode) { // NOI18N
            Mnemonics.setLocalizedText(editButton, NbBundle.getMessage (LabeledTextFieldDialog.class, "ReloadCommand")); // NOI18N
            editButton.setToolTipText(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_ReloadCommandA11yDesc"));  // NOI18N
            notesArea.setEditable( true );
            notesArea.setEnabled(true);
            notesArea.setBackground(textField.getBackground()); // white
            notesArea.requestFocus();
            textField.setEditable( false );
            textField.setBackground(titleLabel.getBackground()); // grey
        } else {
            // reload script from file
            Mnemonics.setLocalizedText(editButton, NbBundle.getMessage (LabeledTextFieldDialog.class, "EditCommand")); // NOI18N
            editButton.setToolTipText(NbBundle.getMessage (LabeledTextFieldDialog.class, "ACS_EditCommandA11yDesc"));  // NOI18N
            notesArea.setText(original_notes);
            notesArea.setEditable( false );
            notesArea.setEnabled(false);
            notesArea.setDisabledTextColor(javax.swing.UIManager.getColor("Label.foreground")); // NOI18N
            textField.setEditable( true );
            textField.setBackground(notesArea.getBackground()); // grey
            notesArea.setBackground(titleLabel.getBackground()); // white
            textField.requestFocus();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        textField = new javax.swing.JTextField();
        descLabel = new javax.swing.JLabel();
        notesAreaScrollPane = new javax.swing.JScrollPane();
        notesArea = new javax.swing.JTextArea();
        editButton = new javax.swing.JButton();
        errorScrollPane = new javax.swing.JScrollPane();
        errorTextPane = new javax.swing.JTextPane();

        titleLabel.setLabelFor(textField);
        titleLabel.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.titleLabel.text")); // NOI18N

        textField.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.textField.text")); // NOI18N

        descLabel.setLabelFor(notesArea);
        descLabel.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.descLabel.text")); // NOI18N

        notesArea.setEditable(false);
        notesArea.setColumns(20);
        notesArea.setLineWrap(true);
        notesArea.setRows(5);
        notesArea.setWrapStyleWord(true);
        notesArea.setEnabled(false);
        notesAreaScrollPane.setViewportView(notesArea);
        notesArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableTableScriptTextAreaA11yName")); // NOI18N
        notesArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableTableScriptTextAreaA11yDesc")); // NOI18N

        editButton.setText(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "LabeledTextFieldDialog.editButton.text")); // NOI18N
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        errorTextPane.setForeground(new java.awt.Color(255, 0, 51));
        errorScrollPane.setViewportView(errorTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(errorScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(notesAreaScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addGap(18, 18, 18)
                        .addComponent(textField, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(descLabel)
                            .addComponent(editButton))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(titleLabel)
                    .addComponent(textField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(notesAreaScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(editButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(errorScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        textField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableNewNameTextFieldA11yName")); // NOI18N
        textField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LabeledTextFieldDialog.class, "ACS_RecreateTableNewNameTextFieldA11yDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        updateState();
}                                          
    
    // init edit mode to true so that the first call to
    // updateState will toggle into read mode
    boolean isEditMode = true;
    boolean result = false;
    Dialog dialog = null;
    private String original_notes;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descLabel;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane errorScrollPane;
    private javax.swing.JTextPane errorTextPane;
    private javax.swing.JTextArea notesArea;
    private javax.swing.JScrollPane notesAreaScrollPane;
    private javax.swing.JTextField textField;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    
}
