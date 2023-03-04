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


package org.netbeans.modules.i18n.java;


import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.AbstractListModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JPanel;

import org.netbeans.modules.i18n.I18nUtil;

import org.openide.util.HelpCtx;


/**
 * Panel for adding parameters to MessageFormat.format code by i18n action.
 *
 * @author  Petr Jiricka
 */
public class ParamsPanel extends JPanel {

    /** List of arguments. */
    private ArrayList arguments = new ArrayList();

    /** Edited row. */
    private int editingRow = -1;

    /** List model for parameters. */
    private ParamsListModel model;

    private final ResourceBundle bundle;    

    /** Generated serailized version UID. */
    static final long serialVersionUID =-3754019215574878093L;
    
    
    /** Creates new form ParamsPanel */
    public ParamsPanel() {
        bundle = org.openide.util.NbBundle.getBundle(ParamsPanel.class);
        initComponents ();
        initAccessibility();
        paramsList.setModel(getListModel());
        paramsList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (paramsList.getSelectedIndex() != -1)
                        updateEditor(paramsList.getSelectedIndex());
                    removeParamButton.setEnabled(paramsList.getSelectedIndex() != -1);
                }
            }
        );
        removeParamButton.setEnabled(paramsList.getSelectedIndex() != -1);
        HelpCtx.setHelpIDString(this, I18nUtil.HELP_ID_ADDPARAMS);
    }

    /** Sets arguments. */
    public void setArguments(String[] args) {
        arguments.clear();
        for (int i = 0; i < args.length; i++) {
            arguments.add(args[i]);
        }
        //    equalize();
        if (getListModel().getSize() > 0)
            getListModel().fireIntervalAdded(0, getListModel().getSize() - 1);
        if (getListModel().getSize() > 0)
            editRow(0);
        else
            editRow(-1);
    }

    /** Gets arguments. */
    public String[] getArguments() {
        commitChanges();

        // j is the last non-empty index
        int j = -1;
        for (int i = 0; i < arguments.size(); i++)
            if (((String)arguments.get(i)).trim().length() > 0)
                j = i;

        String[] args = new String[j + 1];
        for (int i = 0; i <= j; i++)
            args[i] = (String)arguments.get(i);

        return args;
    }

    /** Commits changes. */
    private void commitChanges() {
        //mainComment = mainCommentTextArea.getText();
        if (editingRow != -1) {
            //comments.set (editingRow, commentTextArea.getText());
            arguments.set(editingRow, codePane.getText());
            getListModel().fireContentsChanged(editingRow, editingRow);
        }
    }

    /** Sets the index of the row being edited to row or disables editing if row == -1.
     * Should only be called  with -1 if there is no data. */
    private void editRow(int row) {
        if (row != -1)
            paramsList.setSelectedIndex(row);
        else
            paramsList.setSelectedIndices(new int[0]);
    }

    /** Updates editor. */
    private void updateEditor(int row) {
        commitChanges();
        editingRow = row;
        if (row == -1) {
            //commentTextArea.setText("");
            codePane.setText(""); // NOI18N
            //commentTextArea.setEnabled(false);
            codePane.setEnabled(false);
        }
        else {
            //commentTextArea.setText((String)comments.get(editingRow));
            codePane.setText((String)arguments.get(editingRow));
            //commentTextArea.setEnabled(true);
            codePane.setEnabled(true);
            codePane.requestFocus();
        }
    }

    /** Gets list model for parameters. */
    private ParamsListModel getListModel() {
        if (model == null)
            model = new ParamsListModel ();
        return model;
    }

    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_ParamsPanel"));        
        removeParamButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CTL_RemoveButton"));        
        addParamButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CTL_AddButton"));        
        paramsList.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_paramsList"));        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        paramLabel = new javax.swing.JLabel();
        codeLabel = new javax.swing.JLabel();
        paramsScroll = new javax.swing.JScrollPane();
        paramsList = new javax.swing.JList();
        addRemovePanel = new javax.swing.JPanel();
        addParamButton = new javax.swing.JButton();
        removeParamButton = new javax.swing.JButton();
        codeScroll = new javax.swing.JScrollPane();
        codePane = new javax.swing.JEditorPane();

        setLayout(new java.awt.GridBagLayout());

        paramLabel.setLabelFor(paramsList);
        org.openide.awt.Mnemonics.setLocalizedText(paramLabel, bundle.getString("LBL_Parameters")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(paramLabel, gridBagConstraints);

        codeLabel.setLabelFor(codePane);
        org.openide.awt.Mnemonics.setLocalizedText(codeLabel, bundle.getString("LBL_Code")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(codeLabel, gridBagConstraints);

        paramsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        paramsList.setVisibleRowCount(3);
        paramsScroll.setViewportView(paramsList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 0, 0);
        add(paramsScroll, gridBagConstraints);

        addRemovePanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(addParamButton, bundle.getString("CTL_AddButton")); // NOI18N
        addParamButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addParamButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        addRemovePanel.add(addParamButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(removeParamButton, bundle.getString("CTL_RemoveButton")); // NOI18N
        removeParamButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeParamButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        addRemovePanel.add(removeParamButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 11, 0, 11);
        add(addRemovePanel, gridBagConstraints);

        codePane.setContentType("text/x-java");
        codePane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                codePaneFocusLost(evt);
            }
        });
        codeScroll.setViewportView(codePane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 12, 11, 0);
        add(codeScroll, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void codePaneFocusLost (java.awt.event.FocusEvent evt) {//GEN-FIRST:event_codePaneFocusLost
        commitChanges();
    }//GEN-LAST:event_codePaneFocusLost

    private void removeParamButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeParamButtonActionPerformed
        int index = paramsList.getSelectedIndex();
        if (index == -1) return;
        arguments.remove(index);
        getListModel().fireIntervalRemoved(index, index);
        if (index >= arguments.size()) index--;
        editingRow = -1; // so the row is not updated
        editRow(index);
    }//GEN-LAST:event_removeParamButtonActionPerformed

    private void addParamButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addParamButtonActionPerformed
        arguments.add("");
        //comments.add("");
        getListModel().fireIntervalAdded(getListModel().getSize() - 1, getListModel().getSize() - 1);
        editRow(getListModel().getSize() - 1);
    }//GEN-LAST:event_addParamButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addParamButton;
    private javax.swing.JPanel addRemovePanel;
    private javax.swing.JLabel codeLabel;
    private javax.swing.JEditorPane codePane;
    private javax.swing.JScrollPane codeScroll;
    private javax.swing.JLabel paramLabel;
    private javax.swing.JList paramsList;
    private javax.swing.JScrollPane paramsScroll;
    private javax.swing.JButton removeParamButton;
    // End of variables declaration//GEN-END:variables

    /** List model for the list of parameters */
    protected class ParamsListModel extends AbstractListModel {

        /** Generated serial version UID. */
        static final long serialVersionUID =6832148996617470334L;
        
        /** DEfault constructor. */
        public ParamsListModel () {
        }


        /** Gets number of arguments in model. */
        public int getSize() {
            return arguments.size();
        }

        /** Gets n-th arguments from list model. 
         * @param index index of argument from list to get */
        public Object getElementAt(int index) {
            return "{" + index + "}  " + (String)arguments.get(index);
        }

        /** Fires that one or more elements from interval were changed. 
         * @param index0 start index 
         * @param index1 end index */
        public void fireContentsChanged(int index0, int index1) {
            super.fireContentsChanged(this, index0, index1);
        }

        /** Fires that one or more elements from interval were added.
         * @param index0 start index 
         * @param index1 end index */
        public void fireIntervalAdded(int index0, int index1) {
            super.fireIntervalAdded(this, index0, index1);
        }

        /** Fires that one or more elements from interval were removed.
         * @param index0 start index 
         * @param index1 end index */
        public void fireIntervalRemoved(int index0, int index1) {
            super.fireIntervalRemoved(this, index0, index1);
        }
    }
}
