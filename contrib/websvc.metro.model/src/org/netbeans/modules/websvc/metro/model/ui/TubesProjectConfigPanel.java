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

import com.sun.xml.ws.runtime.config.MetroConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import com.sun.xml.ws.runtime.config.TubeFactoryList;
import com.sun.xml.ws.runtime.config.TubelineDefinition;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.metro.model.MetroConfigLoader;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class TubesProjectConfigPanel extends JPanel {

    private Project project;
    private boolean isChanged;
    private MetroConfig cfg;
    private boolean override;

    private MetroConfigLoader cfgLoader = new MetroConfigLoader();
    
    /** Creates new form TubesProjectConfigPanel */
    public TubesProjectConfigPanel(Project project, MetroConfig cfg, boolean override) {
        this.project = project;
        this.cfg = cfg;
        this.override = override;
        initComponents();

        addBtnClient.addActionListener(new AddButtonActionListener(true));
        removeBtnClient.addActionListener(new RemoveButtonActionListener(true));

        addBtnService.addActionListener(new AddButtonActionListener(false));
        removeBtnService.addActionListener(new RemoveButtonActionListener(false));

        populateValues();
        isChanged = false;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public boolean isOverride() {
        return overrideChBox.isSelected();
    }

    public List<String> getTubeList(boolean client) {
        TableModel tModel = client ? tubeTableClientModel : tubeTableServiceModel;
        List<String> retList = new ArrayList<String>();
        for (int i=0; i < tModel.getRowCount(); i++) {
            retList.add((String)tModel.getValueAt(i, 0));
        }
        return retList;
    }

    protected void populateValues() {
        TubelineDefinition tDef = cfgLoader.getDefaultTubeline(cfg);
        if (tDef != null) {
            TubeFactoryList tClientList = tDef.getClientSide();
            TubeFactoryList tServiceList = tDef.getEndpointSide();
            if (tClientList != null) {
                List<TubeFactoryConfig> tubeFacConfigs = tClientList.getTubeFactoryConfigs();
                for (TubeFactoryConfig tubeCfg : tubeFacConfigs) {
                    tubeTableClientModel.addRow(new Object[]{tubeCfg.getClassName()});
                }
                if (tubeTableClientModel.getRowCount() > 0) {
                    ((ListSelectionModel) tubeTableClient.getSelectionModel()).setSelectionInterval(0, 0);
                }
            }
            if (tServiceList != null) {
                List<TubeFactoryConfig> tubeFacConfigs = tServiceList.getTubeFactoryConfigs();
                for (TubeFactoryConfig tubeCfg : tubeFacConfigs) {
                    tubeTableServiceModel.addRow(new Object[]{tubeCfg.getClassName()});
                }
                if (tubeTableServiceModel.getRowCount() > 0) {
                    ((ListSelectionModel) tubeTableService.getSelectionModel()).setSelectionInterval(0, 0);
                }
            }
        }
        overrideChBox.setSelected(override);
        enableDisable();
    }

    private void enableDisable() {

        boolean enable = overrideChBox.isSelected();

        this.serviceLbl.setEnabled(enable);
        this.downBtnService.setEnabled(enable);
        this.upBtnService.setEnabled(enable);
        this.addBtnService.setEnabled(enable);
        this.removeBtnService.setEnabled(enable);
        this.jScrollPane2.setEnabled(enable);
        this.tubeTableService.setEnabled(enable);

        this.clientLbl.setEnabled(enable);
        this.downBtnClient.setEnabled(enable);
        this.upBtnClient.setEnabled(enable);
        this.addBtnClient.setEnabled(enable);
        this.removeBtnClient.setEnabled(enable);
        this.jScrollPane3.setEnabled(enable);
        this.tubeTableClient.setEnabled(enable);
    }

    class RemoveButtonActionListener implements ActionListener {

        private boolean client;

        public RemoveButtonActionListener(boolean client) {
            this.client = client;
        }
        
        public void actionPerformed(ActionEvent e) {
            JTable table = client ? tubeTableClient : tubeTableService;
            int[] selectedRows = table.getSelectedRows();
            Arrays.sort(selectedRows);
            if ((selectedRows == null) || (selectedRows.length <= 0)) {
                return;
            }
            TubeTableModel tModel = client ? tubeTableClientModel : tubeTableServiceModel;
            StringBuilder className = new StringBuilder();
            for (int i : selectedRows) {
                className.append((String) tModel.getValueAt(i, 0));
		 className.append( ", \n");
            }
            if (confirmDeletion(className.toString())) {
                for (int i = selectedRows.length-1; i >= 0; i--) {
                    tModel.removeRow(selectedRows[i]);
                }
                int newSelectedRow = selectedRows[0] - 1;
                table.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
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

        private boolean client;
        //DialogDescriptor dlgDesc = null;

        public AddButtonActionListener(boolean client) {
            this.client = client;
        }
        public void actionPerformed(ActionEvent evt) {
            ClassDialog classDialog = new ClassDialog(project, null); //NOI18N
            classDialog.show();
            int newSelectedRow = 0;
            TubeTableModel tModel = client ? tubeTableClientModel : tubeTableServiceModel;
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    tModel.addRow(new Object[]{selectedClass});
                    newSelectedRow = tModel.getRowCount() - 1;
                }
            }
            JTable table = client ? tubeTableClient : tubeTableService;
            table.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
            isChanged = true;
        }
    }

    class TubeTable extends JTable {

        private boolean client;

        public TubeTable(boolean client) {
            super();
            this.client = client;

            JTableHeader header = getTableHeader();
            header.setResizingAllowed(false);
            header.setReorderingAllowed(false);

            ListSelectionModel model = getSelectionModel();
            model.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            model.addListSelectionListener(new TubeListSelectionListener(client));
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

        private boolean client;
        
        public TubeListSelectionListener(boolean client) {
            this.client = client;
        }
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {

                JButton upBtn = client ? upBtnClient : upBtnService;
                int selectedRow = getSelectedRow(client);
                if (selectedRow == 0) {
                    upBtn.setEnabled(false);
                } else {
                    if (!upBtn.isEnabled()) {
                        upBtn.setEnabled(true);
                    }
                }

                TubeTableModel tModel = client ? tubeTableClientModel : tubeTableServiceModel;
                JButton downBtn = client ? downBtnClient : downBtnService;
                if (selectedRow == tModel.getRowCount() - 1) {
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

        addBtnService = new javax.swing.JButton();
        removeBtnService = new javax.swing.JButton();
        upBtnService = new javax.swing.JButton();
        downBtnService = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tubeTableService = new TubeTable(false);
        jScrollPane3 = new javax.swing.JScrollPane();
        tubeTableClient = new TubeTable(true);
        addBtnClient = new javax.swing.JButton();
        upBtnClient = new javax.swing.JButton();
        removeBtnClient = new javax.swing.JButton();
        downBtnClient = new javax.swing.JButton();
        clientLbl = new javax.swing.JLabel();
        serviceLbl = new javax.swing.JLabel();
        overrideChBox = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(addBtnService, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Add")); // NOI18N
        addBtnService.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeBtnService, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Remove")); // NOI18N
        removeBtnService.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upBtnService, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Move_Up")); // NOI18N
        upBtnService.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Move_Up")); // NOI18N
        upBtnService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpHandler(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(downBtnService, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Move_Down")); // NOI18N
        downBtnService.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Move_Down")); // NOI18N
        downBtnService.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownHandler(evt);
            }
        });

        tubeTableServiceModel = new TubeTableModel(new String[]{NbBundle.getMessage(TubesProjectConfigPanel.class, "HEADING_TUBES")}, 0);
        tubeTableService.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tubeTableService.setModel(tubeTableServiceModel);
        jScrollPane2.setViewportView(tubeTableService);
        tubeTableService.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "ACSD_MessageHandlerTable")); // NOI18N
        tubeTableService.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "ACSD_MessageHandlerTable")); // NOI18N

        tubeTableClientModel = new TubeTableModel(new String[]{NbBundle.getMessage(TubesProjectConfigPanel.class, "HEADING_TUBES")}, 0);
        tubeTableClient.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        tubeTableClient.setModel(tubeTableClientModel);
        jScrollPane3.setViewportView(tubeTableClient);

        org.openide.awt.Mnemonics.setLocalizedText(addBtnClient, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Add")); // NOI18N
        addBtnClient.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Add")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(upBtnClient, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Move_Up")); // NOI18N
        upBtnClient.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Move_Up")); // NOI18N
        upBtnClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upBtnClientmoveUpHandler(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(removeBtnClient, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Remove")); // NOI18N
        removeBtnClient.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Remove")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(downBtnClient, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Move_Down")); // NOI18N
        downBtnClient.setToolTipText(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "HINT_Move_Down")); // NOI18N
        downBtnClient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downBtnClientmoveDownHandler(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(clientLbl, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_ClientTubeline")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(serviceLbl, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_ServiceTubeline")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(overrideChBox, org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_OverrideLibDefaults")); // NOI18N
        overrideChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overrideChBoxActionPerformed(evt);
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
                            .addComponent(addBtnService, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(upBtnService, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(removeBtnService, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(downBtnService, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)))
                    .addComponent(serviceLbl)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addBtnClient, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(upBtnClient, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(removeBtnClient, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                            .addComponent(downBtnClient, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)))
                    .addComponent(clientLbl)
                    .addComponent(overrideChBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(overrideChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serviceLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addBtnService)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeBtnService)
                        .addGap(23, 23, 23)
                        .addComponent(upBtnService)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downBtnService)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clientLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addBtnClient)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeBtnClient)
                        .addGap(23, 23, 23)
                        .addComponent(upBtnClient)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(downBtnClient)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        addBtnService.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TubesProjectConfigPanel.class, "LBL_Add")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void moveUpHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpHandler
    int selectedRow = getSelectedRow(false);
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow - 1;
    tubeTableServiceModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTableService.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_moveUpHandler

private void moveDownHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownHandler
    int selectedRow = getSelectedRow(false);
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow + 1;
    tubeTableServiceModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTableService.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_moveDownHandler

private void upBtnClientmoveUpHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upBtnClientmoveUpHandler
    int selectedRow = getSelectedRow(true);
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow - 1;
    tubeTableClientModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTableClient.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_upBtnClientmoveUpHandler

private void downBtnClientmoveDownHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downBtnClientmoveDownHandler
    int selectedRow = getSelectedRow(true);
    if (selectedRow == -1) {
        return;
    }
    int newSelectedRow = selectedRow + 1;
    tubeTableClientModel.moveRow(selectedRow, selectedRow, newSelectedRow);
    tubeTableClient.getSelectionModel().setSelectionInterval(newSelectedRow, newSelectedRow);
    isChanged = true;
}//GEN-LAST:event_downBtnClientmoveDownHandler

private void overrideChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overrideChBoxActionPerformed
    enableDisable();
    isChanged = true;
}//GEN-LAST:event_overrideChBoxActionPerformed

    protected int getSelectedRow(boolean client) {
        JTable table = client ? tubeTableClient : tubeTableService;
        ListSelectionModel lsm = (ListSelectionModel) table.getSelectionModel();
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
    private javax.swing.JButton addBtnClient;
    private javax.swing.JButton addBtnService;
    private javax.swing.JLabel clientLbl;
    private javax.swing.JButton downBtnClient;
    private javax.swing.JButton downBtnService;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JCheckBox overrideChBox;
    private javax.swing.JButton removeBtnClient;
    private javax.swing.JButton removeBtnService;
    private javax.swing.JLabel serviceLbl;
    private javax.swing.JTable tubeTableClient;
    private TubeTableModel tubeTableClientModel;
    private javax.swing.JTable tubeTableService;
    private TubeTableModel tubeTableServiceModel;
    private javax.swing.JButton upBtnClient;
    private javax.swing.JButton upBtnService;
    // End of variables declaration//GEN-END:variables
}
