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
package org.netbeans.modules.form.j2ee.wizard;

import java.awt.Component;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.form.j2ee.J2EEUtils;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Wizard panel for detail information of master/detail wizard.
 *
 * @author Jan Stola
 */
public final class DetailPanel implements WizardDescriptor.Panel {
    /** Determines whether we can proceed to the next panel. */
    private boolean valid;
    /** List of <code>ChangeListener</code> objects. */
    private EventListenerList listenerList;
    /** Connection to the selected database. */
    private DatabaseConnection connection;
    /** Name of the master table. */
    private String masterTable;
    /** Names of selected master table columns. */
    private List masterColumns;
    /** Image with the preview of the fields layout. */
    private ImageIcon fieldsIcon;
    /** Image with the preview of the table layout. */
    private ImageIcon tableIcon;
    
    /** For acessing info/error label */
    private WizardDescriptor wizardDesc;

    /**
     * Initializes GUI of this panel.
     */
    private void initGUI() {
        initComponents();
        initLists();
        fieldsIcon = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/j2ee/resources/md_fields.gif")); // NOI18N
        tableIcon = new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/j2ee/resources/md_table.gif")); // NOI18N
        previewLabel.setIcon(fieldsIcon);
    }

    /**
     * Initializes lists of columns.
     */
    private void initLists() {
        availableList.setModel(new DefaultListModel());
        includeList.setModel(new DefaultListModel());
        ListDataListener listener = new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                contentsChanged(e);
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                contentsChanged(e);
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                Object source = e.getSource();
                if (source == availableList.getModel()) {
                    addAllButton.setEnabled(availableList.getModel().getSize() != 0);
                } else if (source == includeList.getModel()) {
                    boolean empty = includeList.getModel().getSize() == 0;
                    removeAllButton.setEnabled(!empty);
                    setValid(!empty);
                }
            }
        };
        availableList.getModel().addListDataListener(listener);
        includeList.getModel().addListDataListener(listener);
    }

    /**
     * Fills the content of <code>tableCombo</code>.
     */
    private void fillTableCombo() {
        Connection con = connection.getJDBCConnection();
        try {
            DefaultComboBoxModel model = new DefaultComboBoxModel();
            ResultSet rs = con.getMetaData().getExportedKeys(con.getCatalog(), connection.getSchema(), masterTable);
            while (rs.next()) {
                String tableName = rs.getString("FKTABLE_NAME"); // NOI18N
                boolean hasPK = J2EEUtils.hasPrimaryKey(connection, tableName);
                ForeignKey fk = new ForeignKey(
                    rs.getString("PKTABLE_NAME"), // NOI18N
                    rs.getString("PKCOLUMN_NAME"), // NOI18N
                    tableName,
                    rs.getString("FKCOLUMN_NAME"), // NOI18N
                    hasPK
                );
                model.addElement(fk);
            }
            tableCombo.setModel(model);
            rs.close();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
        boolean empty = (tableCombo.getModel().getSize() == 0);
        if (empty) {
            fieldsChoice.setSelected(true);
            tableCombo.setEnabled(false);
            Mnemonics.setLocalizedText(availableLabel, NbBundle.getMessage(DetailPanel.class, "LBL_DetailAvailableFields")); // NOI18N
            Mnemonics.setLocalizedText(includeLabel, NbBundle.getMessage(DetailPanel.class, "LBL_DetailFieldsToInclude")); // NOI18N
            previewLabel.setIcon(fieldsIcon);
        }
        tableChoice.setEnabled(!empty);
        refreshLists();
    }

    /**
     * Refreshes the content of the lists.
     */
    private void refreshLists() {
        if (tableChoice.isSelected()) {
            tableCombo.setSelectedItem(tableCombo.getSelectedItem());
        } else{
            fillLists(masterTable);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        masterPanel = new javax.swing.JPanel();
        availableLabel = new javax.swing.JLabel();
        availablePane = new javax.swing.JScrollPane();
        availableList = new javax.swing.JList();
        addAllButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        includePane = new javax.swing.JScrollPane();
        includeList = new javax.swing.JList();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        includeLabel = new javax.swing.JLabel();
        detailLabel = new javax.swing.JLabel();
        fieldsChoice = new javax.swing.JRadioButton();
        tableChoice = new javax.swing.JRadioButton();
        tableCombo = new javax.swing.JComboBox();
        previewLabel = new javax.swing.JLabel();
        buttonGroup = new javax.swing.ButtonGroup();

        FormListener formListener = new FormListener();

        masterPanel.setName(org.openide.util.NbBundle.getMessage(DetailPanel.class, "TITLE_DetailPanel")); // NOI18N

        availableLabel.setLabelFor(availableList);
        org.openide.awt.Mnemonics.setLocalizedText(availableLabel, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailAvailableFields")); // NOI18N

        availableList.addListSelectionListener(formListener);
        availablePane.setViewportView(availableList);
        availableList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailAvailable_ACSD")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addAllButton, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailAddAll")); // NOI18N
        addAllButton.setEnabled(false);
        addAllButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailAdd")); // NOI18N
        addButton.setEnabled(false);
        addButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(removeButton, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailRemove")); // NOI18N
        removeButton.setEnabled(false);
        removeButton.addActionListener(formListener);

        org.openide.awt.Mnemonics.setLocalizedText(removeAllButton, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailRemoveAll")); // NOI18N
        removeAllButton.setEnabled(false);
        removeAllButton.addActionListener(formListener);

        includeList.addListSelectionListener(formListener);
        includePane.setViewportView(includeList);
        includeList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailToInclude_ACSD")); // NOI18N

        upButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/j2ee/resources/up.gif")));
        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailUp")); // NOI18N
        upButton.setEnabled(false);
        upButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        if (!Utilities.isMac()) {
            upButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        }
        upButton.addActionListener(formListener);

        downButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/form/j2ee/resources/down.gif")));
        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailDown")); // NOI18N
        downButton.setEnabled(false);
        downButton.setHorizontalAlignment(javax.swing.SwingConstants.LEADING);
        if (!Utilities.isMac()) {
            downButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        }
        downButton.addActionListener(formListener);

        includeLabel.setLabelFor(includeList);
        org.openide.awt.Mnemonics.setLocalizedText(includeLabel, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailFieldsToInclude")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(detailLabel, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailType")); // NOI18N

        buttonGroup.add(fieldsChoice);
        fieldsChoice.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(fieldsChoice, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailTextfields")); // NOI18N
        fieldsChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fieldsChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        fieldsChoice.addActionListener(formListener);

        buttonGroup.add(tableChoice);
        org.openide.awt.Mnemonics.setLocalizedText(tableChoice, org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailTable")); // NOI18N
        tableChoice.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        tableChoice.setMargin(new java.awt.Insets(0, 0, 0, 0));
        tableChoice.addActionListener(formListener);

        tableCombo.setEnabled(false);
        tableCombo.setRenderer(new ForeignKeyRenderer());
        tableCombo.addActionListener(formListener);

        javax.swing.GroupLayout masterPanelLayout = new javax.swing.GroupLayout(masterPanel);
        masterPanel.setLayout(masterPanelLayout);
        masterPanelLayout.setHorizontalGroup(
            masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(masterPanelLayout.createSequentialGroup()
                        .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, masterPanelLayout.createSequentialGroup()
                                .addComponent(availablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(removeAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(addAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(availableLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(includeLabel)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, masterPanelLayout.createSequentialGroup()
                                .addComponent(includePane, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(upButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(downButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(masterPanelLayout.createSequentialGroup()
                        .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(detailLabel)
                            .addGroup(masterPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(masterPanelLayout.createSequentialGroup()
                                        .addComponent(tableChoice)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tableCombo, 0, 311, Short.MAX_VALUE))
                                    .addComponent(fieldsChoice))))
                        .addGap(10, 10, 10)
                        .addComponent(previewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        masterPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {addAllButton, addButton, downButton, removeAllButton, removeButton, upButton});

        masterPanelLayout.setVerticalGroup(
            masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(masterPanelLayout.createSequentialGroup()
                        .addComponent(detailLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fieldsChoice)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tableChoice)
                            .addComponent(tableCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(previewLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(availableLabel)
                    .addComponent(includeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(includePane, 0, 0, Short.MAX_VALUE)
                    .addGroup(masterPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addAllButton)
                            .addComponent(upButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(masterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(removeButton)
                            .addComponent(downButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeAllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE))
                    .addComponent(availablePane, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
                .addContainerGap())
        );

        addAllButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailAddAll_ACSD")); // NOI18N
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailAdd_ACSD")); // NOI18N
        removeButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailRemove_ACSD")); // NOI18N
        removeAllButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailRemoveAll_ACSD")); // NOI18N
        upButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailUp_ACSD")); // NOI18N
        downButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailDown_ACSD")); // NOI18N
        fieldsChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailTextfields_ACSD")); // NOI18N
        tableChoice.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailTable_ACSD")); // NOI18N
        tableCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailTable_ACSN")); // NOI18N
        tableCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "LBL_DetailTable_ACSD")); // NOI18N

        masterPanel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DetailPanel.class, "TITLE_DetailPanel")); // NOI18N
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, javax.swing.event.ListSelectionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == addAllButton) {
                DetailPanel.this.addAllButtonActionPerformed(evt);
            }
            else if (evt.getSource() == addButton) {
                DetailPanel.this.addButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeButton) {
                DetailPanel.this.removeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeAllButton) {
                DetailPanel.this.removeAllButtonActionPerformed(evt);
            }
            else if (evt.getSource() == upButton) {
                DetailPanel.this.upButtonActionPerformed(evt);
            }
            else if (evt.getSource() == downButton) {
                DetailPanel.this.downButtonActionPerformed(evt);
            }
            else if (evt.getSource() == fieldsChoice) {
                DetailPanel.this.fieldsChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == tableChoice) {
                DetailPanel.this.tableChoiceActionPerformed(evt);
            }
            else if (evt.getSource() == tableCombo) {
                DetailPanel.this.tableComboActionPerformed(evt);
            }
        }

        public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
            if (evt.getSource() == availableList) {
                DetailPanel.this.availableListValueChanged(evt);
            }
            else if (evt.getSource() == includeList) {
                DetailPanel.this.includeListValueChanged(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void tableComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableComboActionPerformed
        ForeignKey key = getForeignKey();
        fillLists(key.isValid() ? getForeignKey().getFKTable() : null);
    }//GEN-LAST:event_tableComboActionPerformed

    private void tableChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableChoiceActionPerformed
        boolean selected = tableChoice.isSelected();
        tableCombo.setEnabled(selected);
        if (selected) {
            Mnemonics.setLocalizedText(availableLabel, NbBundle.getMessage(DetailPanel.class, "LBL_DetailAvailableColumns")); // NOI18N
            Mnemonics.setLocalizedText(includeLabel, NbBundle.getMessage(DetailPanel.class, "LBL_DetailColumnsToInclude")); // NOI18N
            ForeignKey key = getForeignKey();
            fillLists(key.isValid() ? getForeignKey().getFKTable() : null);
            previewLabel.setIcon(tableIcon);
        }
    }//GEN-LAST:event_tableChoiceActionPerformed

    private void fieldsChoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldsChoiceActionPerformed
        if (fieldsChoice.isSelected()) {
            tableCombo.setEnabled(false);
            Mnemonics.setLocalizedText(availableLabel, NbBundle.getMessage(DetailPanel.class, "LBL_DetailAvailableFields")); // NOI18N
            Mnemonics.setLocalizedText(includeLabel, NbBundle.getMessage(DetailPanel.class, "LBL_DetailFieldsToInclude")); // NOI18N
            fillLists(masterTable);
            previewLabel.setIcon(fieldsIcon);
        }
    }//GEN-LAST:event_fieldsChoiceActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed
        DefaultListModel model = (DefaultListModel)includeList.getModel();
        int index = includeList.getSelectedIndex();
        Object item = model.remove(index);
        model.add(index+1, item);
        includeList.setSelectedIndex(index+1);
        includeList.ensureIndexIsVisible(index+1);
    }//GEN-LAST:event_downButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed
        DefaultListModel model = (DefaultListModel)includeList.getModel();
        int index = includeList.getSelectedIndex();
        Object item = model.remove(index);
        model.add(index-1, item);
        includeList.setSelectedIndex(index-1);
        includeList.ensureIndexIsVisible(index-1);
    }//GEN-LAST:event_upButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        moveListItems(includeList, availableList, false);
        showMsg("MSG_AtLeastOneColumnIncluded"); // NOI18N
    }//GEN-LAST:event_removeAllButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        moveListItems(includeList, availableList, true);
        if (includeList.getModel().getSize() == 0) {
            showMsg("MSG_AtLeastOneColumnIncluded"); // NOI18N
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        moveListItems(availableList, includeList, true);
        hideMsg();
    }//GEN-LAST:event_addButtonActionPerformed

    private void addAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllButtonActionPerformed
        moveListItems(availableList, includeList, false);
        hideMsg();
    }//GEN-LAST:event_addAllButtonActionPerformed

    private void includeListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_includeListValueChanged
        int[] index = includeList.getSelectedIndices();
        boolean single = (index.length == 1);
        upButton.setEnabled(single && (index[0] != 0));
        downButton.setEnabled(single && (index[0] != includeList.getModel().getSize()-1));
        boolean any = (index.length > 0);
        removeButton.setEnabled(any);
        if (any) {
            availableList.clearSelection();
        }
    }//GEN-LAST:event_includeListValueChanged

    private void availableListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_availableListValueChanged
        boolean enabled = (availableList.getSelectedIndex() != -1);
        addButton.setEnabled(enabled);
        if (enabled) {
            includeList.clearSelection();
        }
    }//GEN-LAST:event_availableListValueChanged

    private void fillLists(String tableName) {
        hideMsg();
        Connection con = connection.getJDBCConnection();
        try {
            DefaultListModel model = (DefaultListModel)availableList.getModel();
            model.clear();
            model = (DefaultListModel)includeList.getModel();
            model.clear();
            if (tableName != null) {
                ResultSet rs = con.getMetaData().getColumns(con.getCatalog(), connection.getSchema(), tableName, "%"); // NOI18N
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME"); // NOI18N
                    model.addElement(columnName);
                }
                rs.close();
                if (defaultPreselect) {
                    if (masterTable.equals(tableName)) {
                        // pre-select master columns
                        preSelectColumns(masterColumns);
                    } else {
                        ForeignKey key = getForeignKey();
                        if (key != null) {
                            includeList.setSelectedValue(key.getFKColumn(), false);
                            moveListItems(includeList, availableList, true);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, ex.getMessage(), ex);
        }
    }

    /**
     * Pre-selects specified columns.
     * 
     * @param columnNames names of columns to pre-select.
     */
    private void preSelectColumns(List columnNames) {
        DefaultListModel availableModel = (DefaultListModel)availableList.getModel();
        DefaultListModel includeModel = (DefaultListModel)includeList.getModel();
        for (int i=includeModel.getSize()-1; i>=0; i--) {
            String column = (String)includeModel.getElementAt(i);
            if (!columnNames.contains(column)) {
                // The column is not selected
                includeModel.removeElementAt(i);
                availableModel.add(0, column);
            }
        }
    }
    
    /**
     * Returns selected foreign key.
     *
     * @return selected foreignkey.
     */
    private ForeignKey getForeignKey() {
        return tableChoice.isSelected() ? (ForeignKey)tableCombo.getSelectedItem() : null;
    }

    /**
     * Returns selected columns.
     *
     * @return selected columns.
     */
    private List getSelectedColumns() {
        DefaultListModel model = (DefaultListModel)includeList.getModel();
        return Arrays.asList(model.toArray());
    }

    /**
     * Moves items of <code>fromList</code> into <code>toList</code>.
     *
     * @param fromList list to move the items from.
     * @param toList list to move the items to.
     * @param selected determines whether to move all items or just the selected ones.
     */
    private static void moveListItems(JList fromList, JList toList, boolean selected) {
        DefaultListModel fromModel = (DefaultListModel)fromList.getModel();
        DefaultListModel toModel = (DefaultListModel)toList.getModel();
        if (selected) {
            int[] index = fromList.getSelectedIndices();
            for (int i=0; i<index.length; i++) {
                Object item = fromModel.getElementAt(index[i]);
                toModel.addElement(item);
            }
            for (int i=index.length-1; i>=0; i--) {
                fromModel.removeElementAt(index[i]);
            }
        } else {
            Enumeration items = fromModel.elements();
            while (items.hasMoreElements()) {
                toModel.addElement(items.nextElement());
            }
            fromModel.clear();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAllButton;
    private javax.swing.JButton addButton;
    private javax.swing.JLabel availableLabel;
    private javax.swing.JList availableList;
    private javax.swing.JScrollPane availablePane;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JLabel detailLabel;
    private javax.swing.JButton downButton;
    private javax.swing.JRadioButton fieldsChoice;
    private javax.swing.JLabel includeLabel;
    private javax.swing.JList includeList;
    private javax.swing.JScrollPane includePane;
    private javax.swing.JPanel masterPanel;
    private javax.swing.JLabel previewLabel;
    private javax.swing.JButton removeAllButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JRadioButton tableChoice;
    private javax.swing.JComboBox tableCombo;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns component that represents this wizard panel.
     *
     * @return component that represents this wizard panel.
     */
    @Override
    public Component getComponent() {
        if (masterPanel == null) {
            initGUI();
        }
        return masterPanel;
    }

    /**
     * Returns help context for this wizard panel.
     *
     * @return default help.
     */
    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("gui.masterdetail-wizard.detail"); // NOI18N
    }

    private boolean defaultPreselect = true;
    @Override
    public void readSettings(Object settings) {
        WizardDescriptor wizard = (WizardDescriptor) settings;
        wizardDesc = wizard;
        connection = (DatabaseConnection)wizard.getProperty("connection"); // NOI18N
        masterTable = (String)wizard.getProperty("master"); // NOI18N
        masterColumns = (List)wizard.getProperty("masterColumns"); // NOI18N
        
        // restore detail settings if the rest remained the same
        ForeignKey key = getForeignKey();
        List detailColumns = (List)wizard.getProperty("detailColumns"); // NOI18N
        if (key == null) {
            String detailTable = (String)wizard.getProperty("detailTable"); // NOI18N
            if ((masterTable.equals(detailTable)) && (detailColumns != null)) {
                defaultPreselect = false;
            }
        } else {
            String pkTable = (String)wizard.getProperty("detailPKTable"); // NOI18N
            String pkColumn = (String)wizard.getProperty("detailPKColumn"); // NOI18N
            String fkTable = (String)wizard.getProperty("detailFKTable"); // NOI18N
            String fkColumn = (String)wizard.getProperty("detailFKColumn"); // NOI18N
            if (key.getPKTable().equals(pkTable)
                && key.getPKColumn().equals(pkColumn)
                && key.getFKTable().equals(fkTable)
                && key.getFKColumn().equals(fkColumn)) {
                defaultPreselect = false;
            }
        }
        
        fillTableCombo();
        if (!defaultPreselect) {
            preSelectColumns(detailColumns);
            defaultPreselect = true;
        }
        
        if (this.includeList.getModel().getSize() == 0) {
            showMsg("MSG_AtLeastOneColumnIncluded"); // NOI18N
        } else {
            hideMsg();
        }
    }

    /**
     * Stores settings of this panel.
     *
     * @param settings wizard descriptor to store the settings in.
     */
    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor wizard = (WizardDescriptor) settings;
        ForeignKey key = getForeignKey();
        wizard.putProperty("detailTable", (key == null) ? masterTable : null); // NOI18N
        wizard.putProperty("detailPKTable", (key == null) ? null : key.getPKTable()); // NOI18N
        wizard.putProperty("detailPKColumn", (key == null) ? null : key.getPKColumn()); // NOI18N
        wizard.putProperty("detailFKTable", (key == null) ? null : key.getFKTable()); // NOI18N
        wizard.putProperty("detailFKColumn", (key == null) ? null : key.getFKColumn()); // NOI18N
        wizard.putProperty("detailColumns", getSelectedColumns()); // NOI18N
    }
    
    /**
     * Determines whether we can move to the next panel.
     *
     * @return <code>true</code> if we can proceed to the next
     * panel, returns <code>false</code> otherwise.
     */
    @Override
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the valid property.
     *
     * @param valid new value of the valid property.
     */
    void setValid(boolean valid) {
        if (valid == this.valid) return;
        this.valid = valid;
        fireStateChanged();
    }

    /**
     * Adds change listener to this wizard panel.
     *
     * @param listener listener to add.
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }
        listenerList.add(ChangeListener.class, listener);
    }

    /**
     * Removes change listener from this wizard panel.
     *
     * @param listener listener to remove.
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null) {
            listenerList.remove(ChangeListener.class, listener);
        }
    }

    /**
     * Fires change of the valid property.
     */
    private void fireStateChanged() {
        if (listenerList == null) return;

        ChangeEvent e = null;
        ChangeListener[] listeners = listenerList.getListeners(ChangeListener.class);
        for (int i=listeners.length-1; i>=0; i--) {
            if (e == null) {
                e = new ChangeEvent(this);
            }
            listeners[i].stateChanged(e);
        }
    }

    /**
     * Information about foreign key.
     */
    static class ForeignKey {
        /** Name of the primary table. */
        private String pkTable;
        /** Name of the primary column. */
        private String pkColumn;
        /** Name of the foreign table. */
        private String fkTable;
        /** Name of the foreign column. */
        private String fkColumn;
        /** Determines whether the foreign table has PK */
        private boolean valid;

        /**
         * Creates new <code>ForeignKey</code>.
         *
         * @param pkTable primary table.
         * @param pkColumn primary column.
         * @param fkTable foreign table.
         * @param fkColumn foreign column.
         */
        ForeignKey(String pkTable, String pkColumn, String fkTable, String fkColumn, boolean valid) {
            this.pkTable = pkTable;
            this.pkColumn = pkColumn;
            this.fkTable = fkTable;
            this.fkColumn = fkColumn;
            this.valid = valid;
        }

        /**
         * Returns name of the primary table.
         *
         * @return name of the primary table.
         */
        String getPKTable() {
            return pkTable;
        }

        /**
         * Returns name of the primary column.
         *
         * @return name of the primary column.
         */
        String getPKColumn() {
            return pkColumn;
        }

        /**
         * Returns name of the foreign table.
         *
         * @return name of the foreign table.
         */
        String getFKTable() {
            return fkTable;
        }

        /**
         * Returns name of the foreign column.
         *
         * @return name of the foreign column.
         */
        String getFKColumn() {
            return fkColumn;
        }

        /**
         * Determines whether the foreign table has PK.
         * 
         * @return <code>true</code> if the foreign table has primary key,
         * returns <code>false</code> otherwise.
         */
        boolean isValid() {
            return valid;
        }
        
    }
    
    static class ForeignKeyRenderer extends DefaultListCellRenderer {
        private final String FORMAT = NbBundle.getMessage(ForeignKeyRenderer.class, "FMT_ForeignKey"); //NOI18N
        private final String NO_PK = NbBundle.getMessage(ForeignKeyRenderer.class, "FMT_NoPrimaryKey"); //NOI18N
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ForeignKey) {
                ForeignKey key = (ForeignKey)value;
                String message = key.isValid() ? "" : NO_PK; // NOI18N
                String label = MessageFormat.format(FORMAT, key.getPKTable(), key.getPKColumn(), key.getFKTable(), key.getFKColumn(), message);
                setText(label);
                setEnabled(key.isValid());
            }
            return this;
        }
    }
    
    /** Hides info/warning/error wizard label */
    private void hideMsg() {
        showMsg(null);
    }
 
    /** Sets info/warning/error wizard label */
    private void showMsg(String msg) {
        // TODO: add something like MsgLevel param (MsgLevel.Info, MsgLevel.Warning, etc...) 
        // Waiting for fixed issue 137737
        if (wizardDesc != null) {
            wizardDesc.putProperty(
                    "WizardPanel_errorMessage", // NOI18N
                    (msg != null) ? NbBundle.getMessage(getClass(), msg) : null
                    );
        }
    }
    
}
