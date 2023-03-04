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

package org.netbeans.modules.apisupport.project.ui.wizard.layer;

import javax.swing.JTextField;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.common.WizardUtils;
import static org.netbeans.modules.apisupport.project.ui.wizard.layer.Bundle.*;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

final class LayerPanel extends BasicWizardIterator.Panel {

    private final BasicWizardIterator.BasicDataModel data;
    private final CreatedModifiedFiles cmf;

    @Messages("panel_title=XML Layer")
    LayerPanel(WizardDescriptor wiz, BasicWizardIterator.BasicDataModel data, CreatedModifiedFiles cmf) {
        super(wiz);
        this.data = data;
        this.cmf = cmf;
        initComponents();
        putClientProperty("NewFileWizard_Title", panel_title());
    }

    @Messages("panel_name=Layer Location")
    @Override protected String getPanelName() {
        return panel_name();
    }
    
    @Override protected void storeToDataModel() {}

    @Messages({"already_layer=This project already has an XML layer.",
            "no manifest_exists=No manifest.mf file exists."})
    @Override protected void readFromDataModel() {
        createdFiles.setText(WizardUtils.generateTextAreaContent(cmf.getCreatedPaths()));
        modifiedFiles.setText(WizardUtils.generateTextAreaContent(cmf.getModifiedPaths()));
        NbModuleProvider provider = data.getProject().getLookup().lookup(NbModuleProvider.class);
        if (LayerHandle.forProject(data.getProject()).getLayerFile() != null) {
            setError(already_layer());
        } else if(provider.getManifestFile() == null) {
            setError(no_manifest_exists());
        } else {
            markValid();
        }
    }
    
    @Override protected HelpCtx getHelp() {
        return new HelpCtx("apisupport_about_xml_layers");
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        projectLabel = new javax.swing.JLabel();
        project = new JTextField(ProjectUtils.getInformation(data.getProject()).getDisplayName());
        createdFilesLabel = new javax.swing.JLabel();
        createdFiles = new javax.swing.JTextArea();
        modifiedFilesLabel = new javax.swing.JLabel();
        modifiedFiles = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        projectLabel.setLabelFor(project);
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, org.openide.util.NbBundle.getMessage(LayerPanel.class, "LBL_ProjectName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 12);
        add(projectLabel, gridBagConstraints);

        project.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 6, 0);
        add(project, gridBagConstraints);

        createdFilesLabel.setLabelFor(modifiedFiles);
        org.openide.awt.Mnemonics.setLocalizedText(createdFilesLabel, org.openide.util.NbBundle.getMessage(LayerPanel.class, "LBL_CreatedFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(createdFilesLabel, gridBagConstraints);

        createdFiles.setEditable(false);
        createdFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        createdFiles.setColumns(20);
        createdFiles.setRows(5);
        createdFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(createdFiles, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(modifiedFilesLabel, "&Modified Files");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 12);
        add(modifiedFilesLabel, gridBagConstraints);

        modifiedFiles.setEditable(false);
        modifiedFiles.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        modifiedFiles.setColumns(20);
        modifiedFiles.setRows(5);
        modifiedFiles.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(36, 0, 6, 0);
        add(modifiedFiles, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(LayerPanel.class, "ACN_LayerPanel", new Object[] {})); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(LayerPanel.class, "ACD_LayerPanel", new Object[] {})); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea createdFiles;
    private javax.swing.JLabel createdFilesLabel;
    private javax.swing.JTextArea modifiedFiles;
    private javax.swing.JLabel modifiedFilesLabel;
    private javax.swing.JTextField project;
    private javax.swing.JLabel projectLabel;
    // End of variables declaration//GEN-END:variables
    
}
