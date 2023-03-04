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

package org.netbeans.modules.websvc.wsitconf.ui.service;

import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.TxModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.openide.nodes.Node;
import javax.swing.*;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

/**
 *
 * @author Martin Grebac
 */
public class OperationPanel extends SectionInnerPanel {

    private WSDLModel model;
    private Node node;
    private BindingOperation operation;
    private boolean inSync = false;
    private Project project;
    private ConfigVersion cfgVersion;
    
    public OperationPanel(SectionView view, Node node, Project p, BindingOperation operation) {
        super(view);
        this.model = operation.getModel();
        this.node = node;
        this.project = p;
        this.operation = operation;

        cfgVersion = PolicyModelHelper.getWrittenConfigVersion(operation.getParent());
        if (cfgVersion == null) {
            cfgVersion = ConfigVersion.getDefault();
        }

        initComponents();
        
        /* issue 232988: the background color issues with dark metal L&F
        txCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        txLbl.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */

        addImmediateModifier(txCombo);

        inSync = true;
        txCombo.removeAllItems();
//        txCombo.addItem(ComboConstants.TX_NEVER);
        txCombo.addItem(ComboConstants.TX_NOTSUPPORTED);
        txCombo.addItem(ComboConstants.TX_MANDATORY);
        txCombo.addItem(ComboConstants.TX_REQUIRED);
        txCombo.addItem(ComboConstants.TX_REQUIRESNEW);
        txCombo.addItem(ComboConstants.TX_SUPPORTED);

        inSync = false;
        sync();
        
        model.addComponentListener(new ComponentListener() {
            public void valueChanged(ComponentEvent evt) {
                sync();
            }
            public void childrenAdded(ComponentEvent evt) {
                sync();
            }
            public void childrenDeleted(ComponentEvent evt) {
                sync();
            }
        });
    }

    private void sync() {
        inSync = true;
        
        String txValue = TxModelHelper.getTx(operation, node);
        txCombo.setSelectedItem(txValue);
                
        enableDisable();
        inSync = false;
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) return;
        if (source.equals(txCombo)) {

            cfgVersion = PolicyModelHelper.getWrittenConfigVersion(operation.getParent());
            if (cfgVersion == null) {
                cfgVersion = ConfigVersion.getDefault();
            }
            
            String selected = (String) txCombo.getSelectedItem();
            if ((selected != null) && (!selected.equals(TxModelHelper.getTx(operation, node)))) {
                TxModelHelper.getInstance(cfgVersion).setTx(operation, node, selected);
            }
        }

        enableDisable();
    }
    
    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        SectionView view = getSectionView();
        enableDisable();
        if (view != null) {
            view.getErrorPanel().clearError();
        }
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }
    
    @Override
    protected void endUIChange() { }

    public void linkButtonPressed(Object ddBean, String ddProperty) { }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return new JButton();
    }

    private void enableDisable() {
    
        boolean isTomcat = ServerUtils.isTomcat(project);
//        boolean isWebProject = Util.isWebProject(project);
        
        boolean txConfigEnabled = !isTomcat;// && isWebProject;
        txCombo.setEnabled(txConfigEnabled);
        txLbl.setEnabled(txConfigEnabled);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txLbl = new javax.swing.JLabel();
        txCombo = new javax.swing.JComboBox();

        txLbl.setLabelFor(txCombo);
        org.openide.awt.Mnemonics.setLocalizedText(txLbl, org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_Section_Operation_Tx")); // NOI18N

        txCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Not Supported", "Required", "Requires New", "Mandatory", "Supported" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(156, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txLbl)
                    .addComponent(txCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_OperationPanel_Tx_ACSD")); // NOI18N
        txCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_OperationPanel_TxCombo_ACSN")); // NOI18N
        txCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(OperationPanel.class, "LBL_OperationPanel_TxCombo_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox txCombo;
    private javax.swing.JLabel txLbl;
    // End of variables declaration//GEN-END:variables
    
}
