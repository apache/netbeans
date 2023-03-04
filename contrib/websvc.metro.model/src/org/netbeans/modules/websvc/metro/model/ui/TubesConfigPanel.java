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
package org.netbeans.modules.websvc.metro.model.ui;

import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class TubesConfigPanel extends JPanel {

    private Project project;
    private TubeFactoryList tubeList;
    private boolean isChanged;
    private boolean client;
    private boolean overrideDefaults;

    /** Creates new form TubesConfigPanel */
    public TubesConfigPanel(Project project, TubeFactoryList tubeList, boolean client, boolean overrideDefault) {
        this.project = project;
        this.client = client;
        this.tubeList = tubeList;
        this.overrideDefaults = overrideDefault;
        initComponents();
        populateValues();
        addBtn.addActionListener(new AddButtonActionListener());
        removeBtn.addActionListener(new RemoveButtonActionListener());
        isChanged = false;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public boolean isOverride() {
        return overrideDefChBox.isSelected();
    }

    public List<String> getTubeList() {
        List<String> retList = new ArrayList<String>();
        for (int i=0; i < tubeTableModel.getRowCount(); i++) {
            retList.add((String)tubeTableModel.getValueAt(i, 0));
        }
        return retList;
    }

    private void populateValues() {

        overrideDefChBox.setSelected(overrideDefaults);

        List<TubeFactoryConfig> tubeFacConfigs = tubeList.getTubeFactoryConfigs();
        for (TubeFactoryConfig cfg : tubeFacConfigs) {
            tubeTableModel.addRow(new Object[]{cfg.getClassName()});
        }
        if (tubeTableModel.getRowCount() > 0) {
            ((ListSelectionModel) tubeTable.getSelectionModel()).setSelectionInterval(0, 0);
        }

        enableDisable();
    }

    class RemoveButtonActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int[] selectedRows = tubeTable.getSelectedRows();
            Arrays.sort(selectedRows);
            if ((selectedRows == null) || (selectedRows.length <= 0)) {
                return;
            }
            StringBuilder className = new StringBuilder();
            for (int i : selectedRows) {
                className.append((String) tubeTableModel.getValueAt(i, 0));
		className.append(", \n");
            }
            if (confirmDeletion(className.toString())) {
                for (int i = selectedRows.length-1; i >= 0; i--) {
                    tubeTableModel.removeRow(i);
                }
                int newSelectedRow = selectedRows[0] - 1;
                tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
                isChanged = true;
            }
        }

        private boolean confirmDeletion(String className) {
            NotifyDescriptor.Confirmation notifyDesc = new NotifyDescriptor.Confirmation(NbBundle.getMessage(TubesConfigPanel.class, "MSG_CONFIRM_DELETE", className), NbBundle.getMessage(TubesConfigPanel.class, "TTL_CONFIRM_DELETE"), NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return notifyDesc.getValue() == NotifyDescriptor.YES_OPTION;
        }
    }

    class AddButtonActionListener implements ActionListener {

        //DialogDescriptor dlgDesc = null;

        public void actionPerformed(ActionEvent evt) {
            ClassDialog classDialog = new ClassDialog(project, null); //NOI18N
            classDialog.show();
            int newSelectedRow = 0;
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    tubeTableModel.addRow(new Object[]{selectedClass});
                    newSelectedRow = tubeTableModel.getRowCount() - 1;
                }
            }
            tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
            isChanged = true;
        }
    }

    class TubeTable extends JTable {
        public TubeTable() {
            JTableHeader header = getTableHeader();
            header.setResizingAllowed(false);
            header.setReorderingAllowed(false);
            ListSelectionModel model = getSelectionModel();
            model.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            model.addListSelectionListener(new TubeListSelectionListener());
        }

        @Override
        public Component prepareRenderer (final TableCellRenderer renderer, int row, int column) {
            Component comp = super.prepareRenderer (renderer, row, column);
            getTableHeader().setEnabled(isEnabled());
            comp.setEnabled (isEnabled ());
            return comp;
        }

    }

    class TubeListSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = getSelectedRow();
                if (selectedRow == 0) {
                    upBtn.setEnabled(false);
                } else {
                    if (!upBtn.isEnabled()) {
                        upBtn.setEnabled(true);
                    }
                }
                if (selectedRow == tubeTableModel.getRowCount() - 1) {
                    downBtn.setEnabled(false);
                } else {
                    if (!downBtn.isEnabled()) {
                        downBtn.setEnabled(true);
                    }
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addBtn = new javax.swing.JButton();
        removeBtn = new javax.swing.JButton();
        upBtn = new javax.swing.JButton();
        downBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tubeTable = new TubeTable();
        overrideDefChBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(addBtn, org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "LBL_Add")); // NOI18N
        addBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "HINT_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeBtn, org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "LBL_Remove")); // NOI18N
        removeBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "HINT_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upBtn, org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "LBL_Move_Up")); // NOI18N
        upBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "HINT_Move_Up")); // NOI18N
        upBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpHandler(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downBtn, org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "LBL_Move_Down")); // NOI18N
        downBtn.setToolTipText(org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "HINT_Move_Down")); // NOI18N
        downBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownHandler(evt);
            }
        });

        tubeTableModel = new TubeTableModel(new String[]{NbBundle.getMessage(TubesProjectConfigPanel.class, "HEADING_TUBES")}, 0);
        tubeTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tubeTable.setModel(tubeTableModel);
        jScrollPane2.setViewportView(tubeTable);
        tubeTable.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "ACSD_MessageHandlerTable")); // NOI18N
        tubeTable.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "ACSD_MessageHandlerTable")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(overrideDefChBox, org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "LBL_OverrideDefaults")); // NOI18N
        overrideDefChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overrideDefChBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(upBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(removeBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(downBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)))
                    .addComponent(overrideDefChBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(overrideDefChBox)
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(addBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeBtn)
                        .addGap(23, 23, 23)
                        .addComponent(upBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downBtn))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))
                .addContainerGap())
        );

        addBtn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TubesConfigPanel.class, "LBL_Add")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void moveUpHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpHandler
    int selectedRow = getSelectedRow();
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow - 1;
    tubeTableModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_moveUpHandler

private void moveDownHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownHandler
    int selectedRow = getSelectedRow();
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow + 1;
    tubeTableModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTable.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_moveDownHandler

private void overrideDefChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overrideDefChBoxActionPerformed
    isChanged = true;
    enableDisable();
}//GEN-LAST:event_overrideDefChBoxActionPerformed

    private void enableDisable() {
        boolean override = overrideDefChBox.isSelected();
        this.tubeTable.setEnabled(override);
        this.tubeTable.setFocusable(override);
        this.tubeTable.setOpaque(!override);
        this.addBtn.setEnabled(override);
        this.downBtn.setEnabled(override);
        this.jScrollPane2.setEnabled(override);
        this.removeBtn.setEnabled(override);
        this.upBtn.setEnabled(override);
    }

    private int getSelectedRow() {
        ListSelectionModel lsm = (ListSelectionModel) tubeTable.getSelectionModel();
        if (lsm.isSelectionEmpty()) {
            return -1;
        } else {
            return lsm.getMinSelectionIndex();
        }
    }

    static class TubeTableModel extends DefaultTableModel {

        public TubeTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBtn;
    private javax.swing.JButton downBtn;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox overrideDefChBox;
    private javax.swing.JButton removeBtn;
    private javax.swing.JTable tubeTable;
    private javax.swing.table.DefaultTableModel tubeTableModel;
    private javax.swing.JButton upBtn;
    // End of variables declaration//GEN-END:variables
}
